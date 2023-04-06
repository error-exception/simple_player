package com.simple.player.activity

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.os.Message
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.util.SimpleHandler
import com.simple.player.model.Song
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ColumnSpace
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.NRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KGPrettySoundActivity: BaseActivity2() {

    companion object {
        const val MSG_UPDATE_SONG_PROGRESS = 12
        const val MSG_MUSIC_PLAY = 23
        const val TAG = "KGPrettySoundActivity"
    }

    private val handler = ProgressHandler(this)
    private val leftPlayer = MediaPlayer()
    private val rightPlayer = MediaPlayer()

    private var isWindowFocused = false

    private var isPlaying = mutableStateOf(false)
    private var song = mutableStateOf<Song?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(text = "3D丽音") }
                    )
                    Content()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!isWindowFocused && hasFocus) {
            isWindowFocused = true
            initData()
        }
    }

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val isPlaying by remember {
                this@KGPrettySoundActivity.isPlaying
            }
            val scope = rememberCoroutineScope()
            SongMessage()
            Button(
                onClick = {
                    scope.launch {
                        if (isPlaying) {
                            pause()
                        } else {
                            play()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_play_dark),
                    contentDescription = "play",
                    tint = Color.White
                )
            }
        }
    }

    @Composable
    fun SongMessage() {
        if (song.value == null) {
            Text(text = "no song to play")
        } else {
            Text(text = "歌曲名：${song.value!!.title}")
            Text(text = "艺术家：${song.value!!.artist}")
        }
        Text(
            text = "点击播放按钮后，等待 5 秒。由于歌曲文件文件大小，设备等原因，会和酷狗的效果有一定的差异。如果要更换歌曲，请退出后，选择播放你想要的歌曲，然后再进入此界面。在预览播放结束前，你可以不断的点击播放按钮，来找到最佳效果（每次都要等待5秒）。",
            color = NRed
        )
        Text(
            text = "时间长了耳朵遭不住",
            color = NRed,
            fontWeight = FontWeight.Bold
        )
        ColumnSpace(height = 16.dp)
    }

    private fun initData() {
        val audioAttributes = AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        }
        leftPlayer.setAudioAttributes(audioAttributes)
        rightPlayer.setAudioAttributes(audioAttributes)
        song.value = SimplePlayer.currentSong
        val uri = Uri.parse(song.value!!.path)
        leftPlayer.setDataSource(this, uri)
        rightPlayer.setDataSource(this, uri)
        leftPlayer.prepare()
        rightPlayer.prepare()
//        leftPlayer.pause()
//        rightPlayer.pause()
        adjustVolume()
//        leftPlayer.seekTo(25L, MediaPlayer.SEEK_CLOSEST)
    }

    private suspend fun play() {
        if (isWindowFocused) {
            if (SimplePlayer.isPlaying) {
                SimplePlayer.pause(isNoFade = true)
            }
//            val a = System.currentTimeMillis()
//            val b = System.currentTimeMillis()
            leftPlayer.start()
            rightPlayer.start()
            delay(5000)
            leftPlayer.seekTo(25, MediaPlayer.SEEK_CLOSEST)
            rightPlayer.seekTo(0, MediaPlayer.SEEK_CLOSEST)
//            rightPlayer.start()
//            leftPlayer.start()
//            Log.e(TAG, "play: 1 cast: $a")
//            Log.e(TAG, "play: 2 cast: $b")
//            Log.e(TAG, "play: 3 cast: ${System.currentTimeMillis()}")

//            handler.sendEmptyMessageDelayed(MSG_MUSIC_PLAY, 4)
            isPlaying.value = true
        }
    }

    private fun pause() {
        if (isWindowFocused && (leftPlayer.isPlaying || rightPlayer.isPlaying)) {
            leftPlayer.pause()
            rightPlayer.pause()
            isPlaying.value = false
        }
    }

    private fun adjustVolume() {
        leftPlayer.setVolume(1F, 0F)
        rightPlayer.setVolume(0F, 1F)
    }

    fun updateProgress() {

    }

    override fun onDestroy() {
        super.onDestroy()
        leftPlayer.stop()
        leftPlayer.reset()
        leftPlayer.release()
        rightPlayer.stop()
        rightPlayer.reset()
        rightPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }

    class ProgressHandler(parent: KGPrettySoundActivity): SimpleHandler<KGPrettySoundActivity>(Looper.getMainLooper(), parent) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_UPDATE_SONG_PROGRESS -> {
                    parent?.updateProgress()
                }
                MSG_MUSIC_PLAY -> {
                    val p = parent
                    val left = p?.leftPlayer
                    val right= p?.rightPlayer
                    if (p != null && left != null && right != null) {
//                        val current = left.currentPosition - 1000
//                        left.seekTo(current)
//                        right.seekTo(current - 25)
//                        p.adjustVolume()
//                        left.seekTo(right.currentPosition.toLong() + 25L, MediaPlayer.SEEK_CLOSEST)
                        right.start()

                    }
                }
            }
        }

    }

}