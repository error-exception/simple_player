package com.simple.player.activity

import android.content.ContentValues
import android.os.Bundle
import androidx.activity.compose.setContent
import com.simple.player.*
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.database.SongDao
import com.simple.player.model.Song
import com.simple.player.playlist.PlaylistManager
import com.simple.player.screen.ScanResultScreen
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.AppConfigure
import com.simple.player.util.ArtworkProvider
import com.simple.player.util.ProgressHandler

class ScanMusicResult : BaseActivity2() {

    private var addImmediately = false
    private var selected = ArrayList<Song>()
    private var isActivityStart = false

    private val screen = ScanResultScreen(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addImmediately = intent.getBooleanExtra(EXTRA_ADD_IMMEDIATELY, false)

        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }
        val songs = mSongs
        if (songs != null) {
            screen.setResultSource(songs)
        }
        screen.onApplySelected = { selectedList ->
            selected = selectedList
            initList()
        }

    }

    override fun onStart() {
        super.onStart()
        if (!isActivityStart && addImmediately) {
            initList()
            isActivityStart = true
        }
    }

    private fun initList() {
        ProgressHandler.handle(before = {
            Util.showProgressDialog(this, 10, "正在初始化列表......")
        }, handle = {
            if (selected.isEmpty()) {
                return@handle
            }
            if (AppConfigure.Settings.autoSortAfterScan) {
                selected.sortBy {
                    it.title
                }
            }

            val database = SQLiteDatabaseHelper.database
            database.beginTransaction()
            database.delete("song_in_list", "list_id = ?", arrayOf(PlaylistManager.localPlaylist.id.toString()))
            database.delete("song", null, null)
            val contentValues = ContentValues()
            val localPlaylist = PlaylistManager.localPlaylist
            localPlaylist.clear()
            for (song in selected) {
                contentValues.put(SongDao.ID, song.id)
                contentValues.put(SongDao.TITLE, song.title)
                contentValues.put(SongDao.ARTIST, song.artist)
                contentValues.put(SongDao.TYPE, song.type)
                contentValues.put(SongDao.PATH, song.uri)
                contentValues.put(SongDao.BITRATE, song.bitrate)
                database.insertOrThrow("song", null, contentValues)
                contentValues.clear()
                contentValues.put("list_id", localPlaylist.id)
                contentValues.put("song_id", song.id)
                database.insertOrThrow("song_in_list", null, contentValues)
                contentValues.clear()
                localPlaylist += song
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            ArtworkProvider.clearArtworkCache(this)
        }, after = {
            Util.closeProgressDialog(10)
            Util.toast("初始化完成")
            finish()
        })
    }

    companion object {
        const val EXTRA_ADD_IMMEDIATELY = "add_immediately"
        private var mSongs: List<Song>? = null
        fun setResult(result: List<Song>?) {
            mSongs = result
        }
    }
}