package com.simple.player.activity

import android.content.Intent
import android.os.*
import android.provider.MediaStore
import androidx.activity.compose.setContent
import com.simple.player.constant.PreferencesData
import com.simple.player.database.IdPathDao
import com.simple.player.ext.startActivity
import com.simple.player.ext.toast
import com.simple.player.handler.SimpleHandler
import com.simple.player.model.Song
import com.simple.player.scan.FileMusicScanner
import com.simple.player.scan.MediaStoreMusicScanner
import com.simple.player.scan.MusicScanner
import com.simple.player.scan.MusicScannerProvider
import com.simple.player.screen.ScanMusicScreen
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import com.simple.player.util.MusicUtil
import com.simple.player.util.ProgressHandler
import java.io.File
import java.util.ArrayList

class ScanMusicActivity : BaseActivity2() {

    private lateinit var stateHandler: StateHandler
    private lateinit var scanner: MusicScanner
    private var scanImmediately = false
    private var autoAdd = false
    private var isActivityStarted = false

    private val screen = ScanMusicScreen(this)

    var result: ArrayList<Song> = ArrayList()
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanImmediately = intent.getBooleanExtra(EXTRA_SCAN_IMMEDIATELY, false)
        autoAdd = intent.getBooleanExtra(EXTRA_AUTO_ADD, false)

        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }
        screen.onScanSettingsClick = {
            startActivity(ScanMusicSettingsActivity::class.java)
        }
        screen.onStartClick = { mode ->
            when (mode) {
                ScanMusicScreen.MODE_FULL_SCAN -> startScanMusic()
                ScanMusicScreen.MODE_UPDATE_SCAN -> updateScan()
            }
        }
        stateHandler = StateHandler(Looper.getMainLooper(), this)
    }

    private fun updateScan() {
        val s = MusicScannerProvider.getScanner()
        if (s is MediaStoreMusicScanner) {
            return
        }
        scanner = s
        if (s is FileMusicScanner) {
            val dirsString = AppConfigure.Player.musicDirectories.toTypedArray()
            val dirs = Array(dirsString.size) { i ->
                File(dirsString[i])
            }
            s.setDirectories(dirs)
            s.config(
                swallowSearch = true,
                searchInclude = true
            )
        }
        scanner.onComplete {
            stateHandler.sendEmptyMessage(MSG_FINISH)
        }
        scanner.onEach { _, uri, _, name ->
            val uriString = uri.toString()
            val songId = IdPathDao.addUri(uriString)
            val song = Song(songId).apply {
                this.path = uriString
                type = FileUtil.getFileType(name)
                title = MusicUtil.getTitle(name)
                artist = MusicUtil.getArtist(name)
                bitrate = 250
            }
            result.add(song)
            val msg = Message.obtain()
            with(msg) {
                arg1 = scanner.resultCount
                what = MSG_UPDATE_SONG_COUNT
            }
            stateHandler.sendMessage(msg)
        }
        ProgressHandler.handle {
            scanner.scan()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isActivityStarted && scanImmediately) {
            startScanMusic()
        }
        isActivityStarted = true
    }

    private fun startScanMusic() {
        when (AppConfigure.Settings.musicSource) {
            PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_MEDIA_STORE -> scanMediaStore()
            PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE -> scanFile()
        }
    }

    private fun scanFile() {
        scanner = MusicScannerProvider.getScanner()
        scanner.onComplete {
            stateHandler.sendEmptyMessage(MSG_FINISH)
        }
        scanner.onEach { id, uri, _, name ->
            val uriString = uri.toString()
            val songId = IdPathDao.addUri(uriString)
            val song = Song(songId).apply {
                this.path = uriString
                type = FileUtil.getFileType(name)
                title = MusicUtil.getTitle(name)
                artist = MusicUtil.getArtist(name)
                bitrate = 250
            }
            result.add(song)
            val msg = Message.obtain()
            with(msg) {
                arg1 = scanner.resultCount
                what = MSG_UPDATE_SONG_COUNT
            }
            stateHandler.sendMessage(msg)
        }
        ProgressHandler.handle {
            scanner.scan()
        }
    }

    private fun scanMediaStore() {
        scanner = MusicScannerProvider.getScanner()
        scanner.onComplete {
            stateHandler.sendEmptyMessage(MSG_FINISH)
        }
        scanner.onEach { id, uri, cursor, name ->
            val uriString = uri.toString()
            IdPathDao.addUri(id, uriString)
            val song = Song(id).apply {
                this.path = uriString
                type =
                    cursor!!.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
                title = MusicUtil.getTitle(name)
                artist = MusicUtil.getArtist(name)
                bitrate = 250
            }
            result.add(song)
            val msg = Message.obtain()
            with(msg) {
                arg1 = scanner.resultCount
                what = MSG_UPDATE_SONG_COUNT
            }
            stateHandler.sendMessage(msg)
        }
        ProgressHandler.handle {
            scanner.scan()
        }
    }

    fun updateProgressViews(count: Int) {
        screen.setSongCount(count)
    }

    override fun onDestroy() {
        stateHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!screen.isScanOver()) {
            super.onBackPressed()
        } else {
            toast("请等待扫描结束")
        }
    }

    companion object {

        const val EXTRA_SCAN_IMMEDIATELY = "scan_immediately"
        const val EXTRA_AUTO_ADD = "auto_add"

        const val MSG_FINISH = 224
        const val MSG_RESULT_ACTIVITY = 225
        const val MSG_UPDATE_SONG_COUNT = 226

        private class StateHandler(looper: Looper, activity: ScanMusicActivity) :
            SimpleHandler<ScanMusicActivity>(looper, activity) {

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MSG_UPDATE_SONG_COUNT -> parent!!.updateProgressViews(msg.arg1)
                    MSG_RESULT_ACTIVITY -> {
                        val intent = Intent(parent, ScanMusicResult::class.java)
                        intent.putExtra(
                            ScanMusicResult.EXTRA_ADD_IMMEDIATELY,
                            parent?.autoAdd ?: false
                        )
                        ScanMusicResult.setResult(parent!!.result)
                        parent!!.startActivity(intent)
                        parent!!.finish()
                    }

                    MSG_FINISH -> {
                        parent!!.screen.setScanOver(true)
                        sendEmptyMessageDelayed(MSG_RESULT_ACTIVITY, 1000)
                    }
                }
            }

        }
    }
}