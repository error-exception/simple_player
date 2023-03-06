package com.simple.player.screen


import android.content.Intent
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.simple.player.R
import com.simple.player.Store
import com.simple.player.Util
import com.simple.player.Util.copyText
import com.simple.player.Util.setStatusBarStyle
import com.simple.player.activity.BaseActivity2
import com.simple.player.activity.HomeActivity
import com.simple.player.activity.KgListActivity
import com.simple.player.activity.MusicInfo2
import com.simple.player.activity.PlaylistActivity
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.handler.SimpleHandler
import com.simple.player.lyrics.LyricsBooster
import com.simple.player.lyrics.LyricsProvider
import com.simple.player.lyrics.LyricsWord
import com.simple.player.model.Song
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.RoundIconButton2
import com.simple.player.ui.theme.SlideDrawerState
import com.simple.player.ui.theme.windowBackgroundAlpha
import com.simple.player.util.ArtworkProvider
import com.simple.player.util.ColorUtil
import com.simple.player.util.ProgressHandler

class PlayerContentScreen(private val activity: HomeActivity): DefaultLifecycleObserver, MusicEventListener {

    var isChangingByHand = false
    var isActivityPaused = false
    var isVisible = mutableStateOf(false)
        private set

    private val emptySong = Song(-1).apply {
        title = "Title"
        artist = "artist"
    }

    private val currentSong = mutableStateOf(emptySong)
    private val mainColor = mutableStateOf(Color.White)
    private val current = mutableStateOf(0)
    private var currentSongId = mutableStateOf(0L)
    private val handler = UpdateHandler(Looper.myLooper()!!, this)
    private val artworkModel = mutableStateOf<Any?>(null)



    init {
        val lifecycle = activity.lifecycle
        lifecycle.addObserver(this)
    }

    @Composable
    fun ComposeContent(slideDrawerState: () -> SlideDrawerState) {
        ComposeTestTheme {
            var isVisible by remember {
                isVisible
            }
            LaunchedEffect(key1 = slideDrawerState().isOpen) {
                if (slideDrawerState().isOpen) {
                    isVisible = true
                    val androidMainColor = ColorUtil.toAndroidColorInt(mainColor.value)
                    activity.window.statusBarColor = androidMainColor
                    activity.setStatusBarStyle(ColorUtils.calculateLuminance(androidMainColor) > 0.5)
                    MusicEvent2.register(this@PlayerContentScreen)
                    handler.sendEmptyMessage(MSG_UPDATE_PROGRESS)
                    if (currentSongId.value != SimplePlayer.currentSong.id) {
                        Log.e(TAG, "ComposeContent: first update info")
                        currentSongId.value = SimplePlayer.currentSong.id
                        updateInfo()
                    }
                    if (showLyrics.value) {
                        lyricsBooster.start()
                    }
                } else {
                    isVisible = false
                    lyricsBooster.pause()
                    activity.window.statusBarColor = BaseActivity2.primaryColor
                    activity.setStatusBarStyle(isDark = false)
                    MusicEvent2.unregister(this@PlayerContentScreen)
                    handler.removeMessages(MSG_UPDATE_PROGRESS)
                }
            }
            Box(
                modifier = Modifier
                    .background(mainColor.value)
                    .background(
                        Brush.verticalGradient(
                            0f to mainColor.value,
                            0.6f to Color.White
                        )
                    )
                    .fillMaxSize()
            ) {
                PlayerContent()
            }
        }

    }

    private var oldMainColor = mainColor.value
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun PlayerContent() {
        LaunchedEffect(key1 = showLyrics.value) {
            if (showLyrics.value) {
                lyricsBooster.start()
                oldMainColor = mainColor.value
                mainColor.value = Color.White
            } else {
                lyricsBooster.pause()
                mainColor.value = oldMainColor
            }
            if (isVisible.value) {
                val androidMainColor = ColorUtil.toAndroidColorInt(mainColor.value)
                activity.window.statusBarColor = androidMainColor
                activity.setStatusBarStyle(
                    ColorUtils.calculateLuminance(androidMainColor) > 0.5
                )
            }

        }
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Column (
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = rememberRipple(
                            radius = .1.dp,
                            color = Color(0x01000000)
                        )
                    ) {
                        showLyrics.value = !showLyrics.value
                    }
                    .aspectRatio(1F),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = showLyrics.value,
                    modifier = Modifier.fillMaxSize()
                ) { showLyrics ->
                    if (showLyrics) {
                        Lyrics()
                    } else {
                        Artwork(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .aspectRatio(1F)
                                .fillMaxWidth()
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F)
            ) {
                AppBar(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
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
        IconButton(onClick = { changeSongLikeState() }) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = if (songLike) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24),
                contentDescription = "",
                tint = if (songLike) Color.Red else Color.Gray
            )
        }
    }

    @Composable
    private fun Artwork(modifier: Modifier) {
        val data by remember { artworkModel }
        AsyncImage(
            model = data,
            contentDescription = "artwork",
            modifier = modifier
        )
    }


    private val activeLine = mutableStateOf(0)
    private val lyricsBooster = LyricsBooster(activity = activity).apply {
        setActiveLineTarget(activeLine = activeLine)
    }
    private val showLyrics = mutableStateOf(false)
    @Composable
    private fun Lyrics() {
        var height = 60.dp
        val lyricsList = remember { mutableStateListOf<LyricsWord>() }
        val activeLine by remember { activeLine }
        LaunchedEffect(key1 = currentSongId.value) {
            Log.e(TAG, "Lyrics: loading lrc")
            val lrc = LyricsProvider.setSongUri(SimplePlayer.currentSong.path.toUri())
            Log.e(TAG, "Lyrics: loading view")
            lrc?.let {
                lyricsList.clear()
                lyricsList.addAll(it.lrcLineList)
            }
            Log.e(TAG, "Lyrics: setting booster ${lrc?.lrcLineList?.toTypedArray().contentToString()}")
            lyricsBooster.setLyrics(lrc = lrc)
        }
        val state = rememberLazyListState()
        LaunchedEffect(key1 = activeLine) {
//            state.animateScrollToItem(activeLine)
            if (activeLine - 3 > 0) {
                state.animateScrollToItem(activeLine - 3)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .onSizeChanged { height = it.height }
        ) {
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(height = (height / 2)))
                }
                for ((index, lrc) in lyricsList.withIndex()) {
                    item {
                        Text(
                            text = lrc.content,
                            color = if (index == activeLine) MaterialTheme.colors.primary else Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(height = (height / 2)))
                }

            }
        }
    }

    @Composable
    private fun AppBar(modifier: Modifier) {
        Column (
            modifier = modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SongMessage()
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
            modifier = Modifier
                .fillMaxWidth()
                .clickable { ->
                    activity.copyText(text = title)
                },
            text = title,
            fontSize = 20.sp,
            textAlign = TextAlign.Start ,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable { ->
                    activity.copyText(artist)
                },
            text = artist,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Start,
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
                isChangingByHand = true
            } ,
            onValueChangeFinished = {
                isChangingByHand = false
                SimplePlayer.seekTo(current)
            },
            valueRange = 0F..1F,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colors.primary,
                inactiveTrackColor = windowBackgroundAlpha,
                thumbColor = MaterialTheme.colors.primary
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
                tint = Color.Gray
            )
        }
    }

    @Composable
    fun PlayerControls() {

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)
        ) {
            RoundIconButton2(
                painter = painterResource(id = R.drawable.ic_baseline_skip_previous_24),
                iconSize = 32.dp,
                tint = Color.Black,
                color = windowBackgroundAlpha,
                contentDescription = "previous",
                contentPadding = 12.dp
            ) { SimplePlayer.playPrevious() }
            PlayButton()
            RoundIconButton2(
                painter = painterResource(id = R.drawable.ic_skip_next),
                iconSize = 32.dp,
                tint = Color.Black,
                contentDescription = "next",
                contentPadding = 12.dp,
                color = windowBackgroundAlpha
            ) { SimplePlayer.playNext() }
        }
    }

    @Composable
    fun OtherControls() {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            PlayModeButton()
            IconButton(
                onClick = { musicInfo() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_info_24),
                    contentDescription = "info",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            SongLike()
            IconButton(onClick = { playlist() }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_baseline_format_list_bulleted_24),
                    contentDescription = "",
                    tint = Color.Gray
                )
            }

        }
    }

    private fun musicInfo() {
        val info = Intent(activity, MusicInfo2::class.java)
        info.putExtra(MusicInfo2.EXTRA_MUSIC_ID, SimplePlayer.currentSong.id)
        activity.startActivity(info)
    }

    fun playlist() {
        if (SimplePlayer.activePlaylist.name == KgListActivity.LIST_NAME) {
            activity.startActivity(Intent(activity, KgListActivity::class.java).apply {
                setPackage(activity.application.packageName)
            })
            return
        }
        val intent = Intent(activity, PlaylistActivity::class.java)
        intent.putExtra(
            PlaylistActivity.EXTRA_LIST_NAME,
            SimplePlayer.activePlaylist.name
        )
        activity.startActivity(intent)
    }

    @Composable
    fun PlayButton() {
        val state by remember { Store.state.playState }
        RoundIconButton2(
            painter = painterResource(id = if (!state) R.drawable.ic_play_dark else R.drawable.ic_baseline_pause_24),
            color = MaterialTheme.colors.primary,
            tint = Color.White,
            iconSize = 36.dp,
            contentPadding = 16.dp,
            contentDescription = "play"
        ) { SimplePlayer.startOrPause(false) }
    }

    fun setCurrent(current: Int) {
        this.current.value = current
    }

    private fun setSong(song: Song) {
        currentSong.value = song
    }

    private fun setMainColor(color: Int) {
        if (color == 0) {
            mainColor.value = Color.White
        } else {
            mainColor.value = Color(color = color)
        }
        activity.setStatusBarStyle(ColorUtils.calculateLuminance(color) > 0.5)
        activity.window.statusBarColor = ColorUtil.toAndroidColorInt(mainColor.value)
    }

    private fun changeSongLikeState() {
        with (PlaylistManager) {
            val song = SimplePlayer.currentSong
            if (favoriteList.hasSong(song)) {
                removeSong(FAVORITE_LIST, song)
            } else {
                addSong(FAVORITE_LIST, song)
            }
        }
    }
    private fun updateInfo() {
        val defaultColor = android.graphics.Color.WHITE
        setSong(SimplePlayer.currentSong)
        var data = ArtworkProvider.getArtworkDataForCoil(SimplePlayer.currentSong)
        var noData = false
        if (data == null) {
            data = R.drawable.default_artwork
            setMainColor(defaultColor)
            noData = true
        }
        val request = ImageRequest.Builder(activity)
            .data(data)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .error(R.drawable.default_artwork)
            .listener(
                onSuccess = { _, result ->
                    if (noData) {
                        return@listener
                    }
                    ProgressHandler.handle {
                        val bitmap = result.drawable.toBitmap(
                            width = result.drawable.intrinsicWidth,
                            height = result.drawable.intrinsicHeight
                        )
                        Palette.from(bitmap).generate {
                            it?.let {
                                val color: Int
                                val vibrantSwatch = it.vibrantSwatch
                                val mutedSwatch = it.mutedSwatch
                                color = if (vibrantSwatch != null && mutedSwatch != null) {
                                    if (vibrantSwatch.population >= mutedSwatch.population) {
                                        vibrantSwatch.rgb
                                    } else {
                                        mutedSwatch.rgb
                                    }
                                } else if (vibrantSwatch != null && mutedSwatch == null) {
                                    vibrantSwatch.rgb
                                } else if (vibrantSwatch == null && mutedSwatch != null) {
                                    mutedSwatch.rgb
                                } else {
                                    it.getDominantColor(defaultColor)
                                }
                                setMainColor(color)

                            }
                        }
                    }
                },
                onError = { _, _ ->
                    setMainColor(android.graphics.Color.WHITE)
                }
            )
            .build()
        artworkModel.value = request
    }
    override fun onDestroy(owner: LifecycleOwner) {
        activity.lifecycle.removeObserver(this)
        MusicEvent2.unregister(this)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        isActivityPaused = true
        handler.removeMessages(MSG_UPDATE_PROGRESS)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        isActivityPaused = false
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (isVisible.value) {
            handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 400)
        }
    }

    override fun onSongChanged(newSongId: Long) {
        super.onSongChanged(newSongId)
        currentSongId.value = newSongId
        if (isVisible.value) {
            updateInfo()
        }
    }

    private class UpdateHandler(looper: Looper, parent: PlayerContentScreen) :
        SimpleHandler<PlayerContentScreen>(looper, parent) {

        override fun handleMessage(msg: Message) {
            val parent = this.parent
            if (msg.what == MSG_UPDATE_PROGRESS && parent != null) {
                // 当 Activity 不处于 paused 且没有拖动进度条时，更新进度条
                if (
                    !parent.isActivityPaused
                    && !parent.isChangingByHand
                    && parent.isVisible.value
                ) {
                    parent.setCurrent(SimplePlayer.current)
                }
                sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 400)
            }
            super.handleMessage(msg)
        }
    }

    companion object {
        private const val MSG_UPDATE_PROGRESS = 101
        const val TAG = "PlayerContentScreen"
    }

}