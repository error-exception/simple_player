package com.simple.player.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.*
import com.simple.player.R
import com.simple.player.constant.IconCode
import com.simple.player.database.IdPathManager
import com.simple.player.ext.toast
import com.simple.player.handler.SimpleHandler
import com.simple.player.model.Song
import com.simple.player.scan.MusicScanner
import com.simple.player.scan.MusicScannerProvider
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.Gary
import com.simple.player.ui.theme.NRed
import com.simple.player.ui.theme.windowBackground
import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import com.simple.player.util.MusicUtil
import com.simple.player.util.ProgressHandler
import com.simple.player.view.IconButton
import java.util.ArrayList

class ScanMusicActivity : AppCompatActivity(), View.OnClickListener {
//    private lateinit var count: TextView
//    private lateinit var btn: IconButton
    private lateinit var stateHandler: StateHandler
    private lateinit var scanner: MusicScanner
    private var scanImmediately = false
    private var autoAdd = false
    private var isActivityStarted = false
//    private var isScanStarted = false
    private var songCount = mutableStateOf(0)
    private var scanState = mutableStateOf(0)
    private var title = mutableStateOf("歌曲扫描")
    private var backIcon = mutableStateOf(R.drawable.ic_baseline_arrow_back_24)

    var result: ArrayList<Song> = ArrayList()
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanImmediately = intent.getBooleanExtra(EXTRA_SCAN_IMMEDIATELY, false)
        autoAdd = intent.getBooleanExtra(EXTRA_AUTO_ADD, false)
//        setContentView(R.layout.scan_music)

        setContent {
            ComposeTestTheme {
                Column (modifier = Modifier.fillMaxSize()) {
                    Toolbar(title = title, backIcon = backIcon, backClick = { onBackPressed() })
                    ScanCompose(state = scanState, songCount = songCount) {
                        scanState.value = 1
                        startScanMusic()
                    }
                }
            }
        }
//        actionTitle = "歌曲扫描"
//        count = findViewById(R.id.scan_music_count)
//        btn = findViewById(R.id.scan_music_btn)
        stateHandler = StateHandler(Looper.getMainLooper(), this)
//        btn.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (!isActivityStarted && scanImmediately) {
            startScanMusic()
        }
        isActivityStarted = true
    }

    override fun onBackPressed() {
        if (scanState.value == 0) {
            super.onBackPressed()
        } else if (scanState.value == 2) {
            toast("请等待扫描结束")
        }
    }

    override fun onClick(p1: View) {
        startScanMusic()
    }

    private fun startScanMusic() {
//        btn.isClickable = false
        if (AppConfigure.Settings.musicSource == "MediaStore") {
            scanMediaStore()
        } else if (AppConfigure.Settings.musicSource == "ExternalStorage") {
            scanFile()
        }
    }

    private fun scanFile() {
        scanner = MusicScannerProvider.getScanner()
        scanner.onComplete {
            stateHandler.sendEmptyMessage(MSG_FINISH)
        }
        scanner.onEach { id, uri, _, name ->
            val uriString = uri.toString()
            val songId = IdPathManager.addUri(uriString)
            val song = Song(songId).apply {
                this.path = uriString
                type = FileUtil.getFileType(name)
                title = MusicUtil.getTitle(name)
                artist = MusicUtil.getArtist(name)
                bitrate = 250
            }
            result.add(song)
            val msg = Message.obtain()
            with (msg) {
                arg1 = scanner.resultCount
                what = MSG_ITEM
            }
            stateHandler.sendMessage(msg)
        }
        ProgressHandler.handle {
            scanner.scan()
        }
//        btn.typeface = Typeface.DEFAULT
//        btn.text = "扫描中"
//        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
    }

    private fun scanMediaStore() {
        scanner = MusicScannerProvider.getScanner()
        scanner.onComplete {
            stateHandler.sendEmptyMessage(MSG_FINISH)
        }
        scanner.onEach { id, uri, cursor, name ->
            val uriString = uri.toString()
            IdPathManager.addUri(id, uriString)
            val song = Song(id).apply {
                this.path = uriString
                type = cursor!!.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
                title = MusicUtil.getTitle(name)
                artist = MusicUtil.getArtist(name)
                bitrate = 250
            }
            result.add(song)
            val msg = Message.obtain()
            with (msg) {
                arg1 = scanner.resultCount
                what = MSG_ITEM
            }
            stateHandler.sendMessage(msg)
        }
        ProgressHandler.handle {
            scanner.scan()
        }
//        btn.typeface = Typeface.DEFAULT
//        btn.text = "扫描中"
//        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24F)
    }

    fun updateProgressViews(count: Int) {
//        this.count.text = count.toString()
        songCount.value = count
    }

    fun updateEndViews() {
//        btn.typeface = Typeface.DEFAULT
//        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
//        btn.text = "OK"
    }

    override fun onDestroy() {
        stateHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {

        const val EXTRA_SCAN_IMMEDIATELY = "scan_immediately"
        const val EXTRA_AUTO_ADD = "auto_add"

        const val MSG_FINISH = 224
        const val MSG_RESULT_ACTIVITY = 225
        const val MSG_ITEM = 226

        private class StateHandler(looper: Looper, activity: ScanMusicActivity): SimpleHandler<ScanMusicActivity>(looper, activity) {

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MSG_ITEM -> parent!!.updateProgressViews(msg.arg1)
                    MSG_RESULT_ACTIVITY -> {
                        val intent = Intent(parent, ScanMusicResult::class.java)
                        intent.putExtra(ScanMusicResult.EXTRA_ADD_IMMEDIATELY,
                            parent?.autoAdd ?: false
                        )
                        ScanMusicResult.setResult(parent!!.result)
                        parent!!.startActivity(intent)
                        parent!!.finish()
                    }
                    MSG_FINISH -> {
//                        parent!!.updateEndViews()
//                        parent!!.btn.typeface = Store.iconTypeface
//                        parent!!.btn.icon = IconCode.ICON_SEARCH
//                        parent!!.btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48F)
                        parent!!.scanState.value = 2
                        sendEmptyMessageDelayed(MSG_RESULT_ACTIVITY, 1000)
                    }
                }
            }

        }
    }
}

@Composable
fun ScanCompose(state: MutableState<Int>, songCount: MutableState<Int>, click: () -> Unit) {
    val scanState = remember {
        state
    }
    val count = remember {
        songCount
    }
    Column (modifier = Modifier
        .fillMaxSize()
        .background(windowBackground)
        .padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(12.dp)) {
            Row (modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)) {
                Log.e("C", "asdasddsad")
                Surface (shape = CircleShape) {
                    SearchButton(click = click) {
                        scanState.value
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column (modifier = Modifier
                    .weight(1F)
                    .height(128.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    SongCount { count.value }
                    Text(modifier = Modifier.padding(top = 8.dp), text = "歌曲数量", color = Color.Gray, fontSize = 13.sp)
                }

            }
        }
    }
}

@Composable
fun SearchButton(click: () -> Unit, scanState: () -> Int) {
    Log.e("", "")
    if (scanState() > 0) {
        TextButton(modifier = Modifier
            .size(128.dp)
            .background(NRed), onClick = {  }) {
            Text(text = if (scanState() == 1) "扫描中" else "OK", fontSize = 24.sp, color = Color.White)
        }
    } else {
        IconButton(modifier = Modifier
            .size(128.dp)
            .background(NRed), onClick = { click() }) {
            Icon(modifier = Modifier.size(48.dp), imageVector = Icons.Default.Search, contentDescription = "搜索", tint = Color.White)
        }
    }
}

@Composable
fun SongCount(count: () -> Int) {
    Text(text = "${count()}", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 32.sp)
}