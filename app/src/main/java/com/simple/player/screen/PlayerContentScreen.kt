package com.simple.player.screen


import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomDrawer
import androidx.compose.material.BottomDrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberBottomDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.simple.player.R
import com.simple.player.Store
import com.simple.player.Util
import com.simple.player.Util.dps
import com.simple.player.activity.PlayerContentNew
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.model.Song
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.CenterColumn
import com.simple.player.ui.theme.CenterRow
import com.simple.player.ui.theme.Gary
import com.simple.player.ui.theme.GaryAlpha
import com.simple.player.ui.theme.NRed
import com.simple.player.ui.theme.RoundIconButton
import com.simple.player.ui.theme.windowBackgroundAlpha
import kotlinx.coroutines.launch

class PlayerContentScreen(private val activity: PlayerContentNew): DefaultLifecycleObserver, MusicEventListener {

    init {
        val lifecycle = activity.lifecycle
        lifecycle.addObserver(this)
        MusicEvent2.register(this)
    }

    var rotation: Float = 0F
        set(value) {
            field = value
            image?.rotation = value
        }

    private val currentSong = mutableStateOf(SimplePlayer.currentSong)
    private val backgroundColor = mutableStateOf(Color.White)
    private val current = mutableStateOf(0)
    private var image: ImageView? = null
    private var artwork: Bitmap? = PlayerContentNew.defaultArtwork
    private var lastArtwork: Bitmap? = null

    @Composable
    fun ComposeContent() {
        Box(modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
        ) {
            BlurBackground()
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xB0FFFFFF),
                                Color(0xB0FFFFFF),
                                Color.White,
                                Color.White,
                                Color.White
                            ),
                        )
                    )
//                    .background(Color(0xCCFFFFFF))
            ) {
                PlayerContent()
            }

        }
    }

    @Composable
    private fun BlurBackground() {
        Box(modifier = Modifier
            .background(backgroundColor.value)
            .fillMaxSize()
        )

    }


    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun PlayerContent() {
        val drawerState = rememberBottomDrawerState(initialValue = BottomDrawerValue.Closed)
        val scope = rememberCoroutineScope()
        BottomDrawer(
            drawerContent = {
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    CenterRow (
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                    ) {
                        Surface (shape = RoundedCornerShape(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(GaryAlpha)
                                    .size(72.dp, 4.dp)
                            )
                        }
                    }
                    Row (modifier = Modifier.padding(16.dp)) {
                        for (i in 0..3) {
                            CenterColumn (
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(16.dp)
                                    .clickable { },
                            ) {
                                Surface (shape = CircleShape, color = NRed, modifier = Modifier.padding(8.dp)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_add_24),
                                        contentDescription = "",
                                        tint = Color.White
                                    )
                                }
                                Text(modifier = Modifier.padding(top = 8.dp), text = "添加至", fontSize = 12.sp)
                            }
                        }


                    }
                }
            },
            drawerShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            drawerElevation = 0.dp,
            drawerState = drawerState
        ) {
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                AppBar()
                CenterColumn (
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .weight(1F),
                ) {
                    Artwork()
                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp, 0.dp, 4.dp, 16.dp)
                ) {
                    SongLike()
                    Spacer(modifier = Modifier
                        .height(24.dp)
                        .weight(1F))
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "",
                            tint = Gary
                        )
                    }
                }
                MusicProgress()
                PlayerControls()
                OtherControls()
            }
        }

    }


    @Composable
    private fun SongLike() {
        val songLike by remember {
            Store.state.isCurrentSongLike
        }
        IconButton(onClick = { activity.changeSongLikeState() }) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = if (songLike) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24),
                contentDescription = "",
                tint = if (songLike) Color.Red else Gary
            )
        }
    }

    @Composable
    private fun Artwork() {
        AndroidView(
            factory = {
                image = ImageView(it).apply {
                    layoutParams = LinearLayout.LayoutParams(240.dps, 240.dps)
                    setImageBitmap(artwork)
                    post(recyclePost)
                }
                image!!
            },
            modifier = Modifier.clip(CircleShape)
                .border(4.dp, Color.Black, shape = CircleShape)
        )
//        Image(
//            bitmap = artwork.value,
//            contentDescription = "专辑图片",
//            contentScale = ContentScale.Fit,
//            modifier = Modifier
//                .size(240.dp)
//                .clip(shape = CircleShape)
//                .border(4.dp, Color(0xFF, 0xFF, 0xFF, 0x80), shape = CircleShape)
//                .rotate(rotate)
//        )
    }


    @Composable
    private fun AppBar() {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(
                modifier = Modifier
                    .size(36.dp),
                painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                backgroundColor = Color.White,
                iconSize = 28.dp
            ) { activity.finish() }
            Column (modifier = Modifier
                .weight(1F)
                .padding(start = 16.dp, end = 16.dp)) {
                SongMessage()
            }
            RoundIconButton(
                modifier = Modifier
                    .size(36.dp),
                painter = painterResource(id = R.drawable.ic_outline_info_24),
                backgroundColor = Color.White,
                iconSize = 28.dp
            ) { activity.musicInfo() }
        }
    }


    @Composable
    fun SongMessage() {
        val title by remember {
            Store.state.songTitle
        }
        val artist by remember {
            Store.state.songArtist
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center ,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = artist,
            fontSize = 14.sp,
            color = Gary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    @Composable
    fun MusicProgress() {
        var current by remember { current }
        val duration by remember { Store.state.duration }
        Slider(
            value = if (duration != 0) {
                current.toFloat() / duration.toFloat()
            } else {
                0F
            },
            modifier = Modifier
                .height(25.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            onValueChange = {
                current = (duration * it).toInt()
                activity.isChangingByHand = true
            } ,
            onValueChangeFinished = {
                activity.isChangingByHand = false
                SimplePlayer.seekTo(current)
            },
            valueRange = 0F..1F,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colors.primary,
                inactiveTrackColor = windowBackgroundAlpha
            )
        )
        Row (
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = Util.timeString(current),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.weight(1F))
            Text(
                text = Util.timeString(duration),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }

    @Composable
    fun PlayModeButton() {
        val playMode by remember { Store.state.currentPlayMode }
        IconButton(onClick = { SimplePlayer.nextPlayMode() }) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(
                    id = when (playMode) {
                        SimplePlayer.PLAY_MODE_NONE -> R.drawable.ic_baseline_repeat_24
                        SimplePlayer.PLAY_MODE_REPEAT -> R.drawable.ic_baseline_repeat_one_24
                        else -> R.drawable.ic_baseline_shuffle_24
                    }
                ),
                contentDescription = "",
                tint = Gary
            )
        }
    }


    @Composable
    fun PlayerControls() {

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)
        ) {
            RoundIconButton(
                modifier = Modifier
                    .size(56.dp),
                painter = painterResource(id = R.drawable.ic_baseline_skip_previous_24),
                backgroundColor = windowBackgroundAlpha,
                iconSize = 28.dp
            ) { SimplePlayer.playPrevious() }
            PlayButton()
            RoundIconButton(
                modifier = Modifier
                    .size(56.dp),
                painter = painterResource(id = R.drawable.ic_skip_next),
                backgroundColor = windowBackgroundAlpha,
                iconSize = 28.dp
            ) { SimplePlayer.playNext() }
        }
    }

    @Composable
    fun OtherControls() {
        Row (modifier = Modifier.padding(start = 16.dp, end = 16.dp)){
            PlayModeButton()
            Spacer(modifier = Modifier
                .height(24.dp)
                .weight(1F))
            IconButton(onClick = { activity.playlist() }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_baseline_format_list_bulleted_24),
                    contentDescription = "",
                    tint = Gary
                )
            }
        }
    }

    @Composable
    fun PlayButton() {
        val state by remember { Store.state.playState }
        RoundIconButton(
            modifier = Modifier
                .size(72.dp),
            painter = painterResource(id = if (!state) R.drawable.ic_play_dark else R.drawable.ic_baseline_pause_24),
            backgroundColor = MaterialTheme.colors.primary,
            tint = Color.White,
            iconSize = 36.dp
        ) { SimplePlayer.startOrPause(false) }
    }

    private val recyclePost = Runnable {
        lastArtwork?.let {bitmap ->
            if (!bitmap.isRecycled && bitmap != PlayerContentNew.defaultArtwork) {
                bitmap.recycle()
                Log.d(this@PlayerContentScreen.javaClass.simpleName, "Artwork: recycle");
            }
        }
        lastArtwork = null
    }

    fun setArtwork(bitmap: Bitmap?) {
        lastArtwork = artwork
        artwork = bitmap
        image?.setImageBitmap(artwork)
        image?.post(recyclePost)
//        artwork.value = bitmap
//        background.value = bitmap
    }

    fun setCurrent(current: Int) {
        this.current.value = current
    }

    fun setSong(song: Song) {
        currentSong.value = song
    }

    fun setMainColor(color: Int) {
        backgroundColor.value = Color(color = color)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.lifecycle.removeObserver(this)
        MusicEvent2.unregister(this)
    }

}