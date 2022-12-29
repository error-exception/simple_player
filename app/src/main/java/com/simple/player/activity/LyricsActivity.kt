package com.simple.player.activity

import android.animation.TimeAnimator
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.lyrics.LyricsWord
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.recomposeHighlighter
import com.simple.player.view.LyricsProvider
import kotlinx.coroutines.launch

class LyricsActivity: BaseActivity2() {

    private val activeLine = mutableStateOf(1)
    private val timeAnimator = TimeAnimator().apply {
        start()
    }

    private var player = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player.apply {
            setAudioAttributes(AudioAttributes.Builder().run {
                this.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                setUsage(AudioAttributes.USAGE_MEDIA)
                build()
            })
            setDataSource(assets.openFd("audio/银临 Aki阿杰-牵丝戏.mp3"))
            prepare()
        }
        setContent {
            ComposeTestTheme {
                Column {
                    TopAppBar(
                        title = { Text(text = "歌词测试") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { player.start() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_play_dark), contentDescription = "", tint = Color.White)
                            }
                        }
                    )
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content() {
        LyricsWord()
    }

    @Preview
    @Composable
    fun LyricsWord() {
        val state = rememberLazyListState()
        val list = mutableStateListOf<LyricsWord>()
        val scope = rememberCoroutineScope()
        val activeLine by remember { activeLine }
        LaunchedEffect(key1 = Unit) {
            val lrc = LyricsProvider.lrc
            lrc ?: return@LaunchedEffect
            list.addAll(lrc.lrcLineList)
        }
        var elapsedLine = 0
        timeAnimator.setTimeListener { animation, totalTime, deltaTime ->
            scope.launch {
                var i = list.size - 1
                val current = player.currentPosition.toLong()
                while (i >= 0) {
                    val lyricsWord = list[i]
                    if (lyricsWord.startTime < current) {
//                        Log.e(this@LyricsActivity.javaClass.simpleName, "LyricsWord: ${lyricsWord.startTime}")
//                        if (i >= (8 shr 1)) {
//                            if (this@LyricsActivity.activeLine.value != i) {
////                                state.animateScrollToItem(i)
//                                elapsedLine++
                                this@LyricsActivity.activeLine.value = i
//                            }
//                        }
                        break
                    }
                    i--
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .recomposeHighlighter(),
            state = state,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(list) { index, item ->
                Text(
                    text = item.content,
                    textAlign = TextAlign.Center,
                    color = if (index == activeLine) MaterialTheme.colors.primary else Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        timeAnimator.cancel()
        player.stop()
        player.reset()
        player.release()
    }

}