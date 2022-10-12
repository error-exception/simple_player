package com.simple.player.activity


import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.BlurTransformation
import com.simple.player.*
import com.simple.player.R
import com.simple.player.handler.SimpleHandler
import com.simple.player.model.Song
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.*
import com.simple.player.util.ArtworkProvider
import kotlinx.coroutines.launch

class PlayerContentNew : AppCompatActivity(),
    MusicEvent.OnMusicPauseListener,
    MusicEvent.OnMusicPlayListener,
    MusicEvent.OnSongChangedListener,
    MusicEvent.OnPlayModeChangedListener,
    MusicEvent.OnSongAddToListListener,
    MusicEvent.OnSongRemovedFromListListener{

    // 进度条进度是否在人为改变
    private var isChangingByHand = false
    // Activity是否处于Pause状态
    private var isActivityPaused = false
    private var player = SimplePlayer
    private var mHandler: Handler? = null

    private val currentPlayMode = mutableStateOf(SimplePlayer.playMode)
    private val playState = mutableStateOf(0)
    private val current = mutableStateOf(256)
    private val duration = mutableStateOf(666)
    private val currentSong = mutableStateOf(SimplePlayer.currentSong)
    private val isCurrentSongLike = mutableStateOf(false)
    private val artworkDegree = mutableStateOf(0F)

    companion object {
        private const val MSG_UPDATE_PROGRESS = 101
        const val STATE_PLAY = 1
        const val STATE_PAUSE = 0
    }

    private var isInitialed = false
    private fun initBase() {
        if (isInitialed) {
            return
        }
        updateInfo()
        MusicEventHandler.register(this)
        mHandler = UpdateHandler(Looper.getMainLooper(), this)
        mHandler?.sendEmptyMessage(MSG_UPDATE_PROGRESS)
        isInitialed = true
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            initBase()
        }
    }

    override fun onPause() {
        isActivityPaused = true
        mHandler?.removeMessages(MSG_UPDATE_PROGRESS)
        super.onPause()
    }

    override fun onResume() {
        isActivityPaused = false
        super.onResume()
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                val drawerState = rememberBottomDrawerState(initialValue = BottomDrawerValue.Closed)
                val scope = rememberCoroutineScope()
                Box(modifier = Modifier
                    .background(windowBackground)
                    .fillMaxSize()
                )
                BottomDrawer(
                    drawerContent = {
                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            CenterRow (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)) {
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
                        val currentTime = remember { current }
                        val durationTime = remember { duration }
                        val state = remember { playState }
                        val playMode = remember { currentPlayMode }
                        val songLike = remember { isCurrentSongLike }
                        TitleBar(currentSong)
                        CenterColumn (
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .weight(1F),
                        ) {
                            Artwork(currentSong)
                        }
                        Row (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp, 0.dp, 4.dp, 16.dp)
                        ) {
                            SongLike {songLike}
                            Spacer(modifier = Modifier
                                .height(24.dp)
                                .weight(1F))
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "",
                                    tint = Gary
                                )
                            }
                        }
                        MusicProgress(cur = { currentTime }, dur = { durationTime })
                        PlayerControls(state = {state})
                        OtherControls(mode = {playMode})
                    }
                }

            }
        }
    }

    @Composable
    fun SongLike(state: () -> MutableState<Boolean>) {
        IconButton(onClick = { changeSongLikeState() }) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = if (state().value) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24),
                contentDescription = "",
                tint = if (state().value) Color.Red else Gary
            )
        }
    }

    @Composable
    fun BlurBackground(song: MutableState<Song>) {
        val currentSong = remember {
            song
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(ArtworkProvider.getArtworkUri(currentSong.value))
                .crossfade(false)
                .allowHardware(true)
                .allowRgb565(true)
                .allowConversionToBitmap(true)
                .size(240)
                .transformations(BlurTransformation(applicationContext, radius = 20F, config = Bitmap.Config.ARGB_8888))
                .build(),
            contentDescription = "专辑图片",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .blur(72.dp)
                .fillMaxSize(),
        )

    }

    @Composable
    fun TitleBar(song: MutableState<Song>) {
        val currentSong = remember {
            song
        }
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
            ) { finish() }
            Column (modifier = Modifier.weight(1F)) {
                SongMessage { currentSong }
            }
            RoundIconButton(
                modifier = Modifier
                    .size(36.dp),
                painter = painterResource(id = R.drawable.ic_outline_info_24),
                backgroundColor = Color.White,
                iconSize = 28.dp
            ) { musicInfo() }
        }
    }

    @Composable
    fun SongMessage(song: () -> MutableState<Song>) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = song().value.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center ,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = song().value.artist,
            fontSize = 14.sp,
            color = Gary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    @Composable
    fun Artwork(song: MutableState<Song>) {
        val s = remember {
            song
        }
        val rotate = remember {
            artworkDegree
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(ArtworkProvider.getArtworkUri(s.value))
                .crossfade(true)
                .allowHardware(true)
                .allowRgb565(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build(),
            contentDescription = "专辑图片",
//            placeholder = painterResource(id = R.drawable.header),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(240.dp)
                .clip(shape = CircleShape)
                .border(4.dp, Color(0xFF, 0xFF, 0xFF, 0x80), shape = CircleShape)
                .rotate(artworkDegree.value)
        )
    }

    @Composable
    fun MusicProgress(cur: () -> MutableState<Int>, dur: () -> MutableState<Int>) {
        val progress = if (dur().value != 0) {
             cur().value.toFloat() / dur().value.toFloat()
        } else {
            0F
        }
        Slider(
            value = progress,
            modifier = Modifier
                .height(25.dp)
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            onValueChange = {
                cur().value = (dur().value * it).toInt()
                isChangingByHand = true
            } ,
            onValueChangeFinished = {
                isChangingByHand = false
                SimplePlayer.seekTo(cur().value)
            },
            valueRange = 0F..1F,
            colors = SliderDefaults.colors(
                activeTrackColor = NRed,
                inactiveTrackColor = Color.White
            )
        )
        Row (
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = Util.timeString(cur().value),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.weight(1F))
            Text(
                text = Util.timeString(dur().value),
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }


    }

    @Composable
    fun PlayButton(state: () -> MutableState<Int>) {
        RoundIconButton(
            modifier = Modifier
                .size(72.dp),
            painter = painterResource(id = if (state().value == 0) R.drawable.ic_play_dark else R.drawable.ic_baseline_pause_24),
            backgroundColor = NRed,
            tint = Color.White,
            iconSize = 36.dp
        ) { SimplePlayer.startOrPause(false) }
    }

    @Composable
    fun PlayModeButton(mode: () -> MutableState<Int>) {
        IconButton(onClick = { SimplePlayer.nextPlayMode() }) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(
                    id = when (mode().value) {
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
    fun PlayerControls(state: () -> MutableState<Int>) {

        CenterRow (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            RoundIconButton(
                modifier = Modifier
                    .size(56.dp),
                painter = painterResource(id = R.drawable.ic_baseline_skip_previous_24),
                backgroundColor = Color.White,
                iconSize = 28.dp
            ) { SimplePlayer.playPrevious() }
            Spacer(modifier = Modifier.width(32.dp))
            PlayButton { state() }

            Spacer(modifier = Modifier.width(32 .dp))
            RoundIconButton(
                modifier = Modifier
                    .size(56.dp),
                painter = painterResource(id = R.drawable.ic_skip_next),
                backgroundColor = Color.White,
                iconSize = 28.dp
            ) { SimplePlayer.playNext() }
        }
    }

    @Composable
    fun OtherControls(mode: () -> MutableState<Int>) {
        Row (modifier = Modifier.padding(start = 16.dp, end = 16.dp)){
            PlayModeButton {
                mode()
            }
            Spacer(modifier = Modifier
                .height(24.dp)
                .weight(1F))
            IconButton(onClick = { playlist() }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_baseline_format_list_bulleted_24),
                    contentDescription = "",
                    tint = Gary
                )
            }
        }
    }

    private val musicInfo: () -> Unit = {
        val info = Intent(this, MusicInfo2::class.java)
        info.putExtra(MusicInfo2.EXTRA_MUSIC_ID, player.currentSong.id)
        startActivity(info)
    }

    private val playlist: () -> Unit = {
        val intent = Intent(this, PlaylistActivity::class.java)
        intent.putExtra(
            PlaylistActivity.EXTRA_LIST_NAME,
            player.activePlaylist.name
        )
        startActivity(intent)
    }

    private val changeSongLikeState: () -> Unit = {
        with (PlaylistManager) {
            val song = player.currentSong
            if (favoriteList.hasSong(song)) {
                removeSong(FAVORITE_LIST, song)
            } else {
                addSong(FAVORITE_LIST, song)
            }
        }
    }
//        rotation = RotationAnimation(model.artworkView, -1, 30000)

    override fun onStart() {
        super.onStart()
        mHandler?.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 400)
    }

    override fun onMusicPlay() {
        playState.value = STATE_PLAY
    }

    override fun onMusicPause() {
        playState.value = STATE_PAUSE
    }

    override fun onSongChanged(newSongId: Long) {
        updateInfo()
        playState.value = STATE_PLAY

    }

    override fun onPlayModeChanged(oldMode: Int, newMode: Int) {
        currentPlayMode.value = newMode
    }

    override fun onSongAddToList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == player.currentSong.id) {
                isCurrentSongLike.value = true
            }
        }
    }

    override fun onSongRemovedFromList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == player.currentSong.id) {
                isCurrentSongLike.value = false
            }
        }
    }

    private fun updateInfo() {
        isCurrentSongLike.value = player.isCurrentSongLike
        currentSong.value = SimplePlayer.currentSong
        duration.value = SimplePlayer.duration
        playState.value = if (SimplePlayer.isPlaying) STATE_PLAY else STATE_PAUSE

    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler?.removeCallbacksAndMessages(null)
        MusicEventHandler.unregister(this)
    }

    private class UpdateHandler(looper: Looper, parent: PlayerContentNew) :
        SimpleHandler<PlayerContentNew>(looper, parent) {

        override fun handleMessage(msg: Message) {
            val parent: PlayerContentNew = parent!!
            if (msg.what == MSG_UPDATE_PROGRESS) {
                // 当 Activity 不处于 paused 且没有拖动进度条时，更新进度条
                if (!parent.isActivityPaused && !parent.isChangingByHand) {
                    parent.current.value = parent.player.current
                }
                sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 400)
            }
            super.handleMessage(msg)
        }
    }
}

@Preview
@Composable
fun Test() {
    RoundIconButton(
        painter = painterResource(id = R.drawable.ic_skip_next),
        backgroundColor = Color.White,
        iconSize = 48.dp,
        modifier = Modifier
            .size(80.dp)
            .shadow(2.dp, shape = CircleShape)
    ) {

    }

}