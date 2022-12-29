package com.simple.player.screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.simple.player.R
import com.simple.player.Store
import com.simple.player.Util.dps
import com.simple.player.activity.HomeActivity
import com.simple.player.constant.PreferencesData
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ColumnSpace
import com.simple.player.ui.theme.RowSpace
import com.simple.player.ui.theme.windowBackgroundAlpha
import com.simple.player.util.AppConfigure
import com.simple.player.util.ArtworkProvider
import com.simple.player.util.FileUtil

class HomeContentScreen(private val context: Context) {

    private val localList = HomeActivity.Item(R.drawable.ic_baseline_phone_android_24, "本地列表")
    private val favorite = HomeActivity.Item(R.drawable.ic_baseline_favorite_24, "我喜欢")
    private val history = HomeActivity.Item(R.drawable.ic_baseline_history_24, "播放历史")
    private val kgList = HomeActivity.Item(R.drawable.ic_play_dark, "酷狗缓存")

    private val headImageUri = mutableStateOf(Uri.fromFile(FileUtil.mHeaderPicture))
    private val headImageVisible = mutableStateOf(AppConfigure.Settings.showHeadImage)
    private val simpleBottomPlayerBar = mutableStateOf(AppConfigure.Settings.bottomPlayerBar == PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_SIMPLE)
    private val customPlaylistShowState = mutableStateOf(true)
    val customList = mutableStateListOf<PlayListItem>()

    var onHeadImageClick: ClickFunction? = null
    var onPlayButtonClick: ClickFunction? = null
    var onPlayModeButtonClick: ClickFunction? = null
    var onLocalPlaylistClick: ClickFunction? = null
    var onFavoritePlaylistClick: ClickFunction? = null
    var onKgPlaylistClick: ClickFunction? = null
    var onHistoryPlaylistClick: ClickFunction? = null
    var onLikeButtonClick: ClickFunction? = null
    var onNextButtonClick: ClickFunction? = null
    var onPreviousButtonClick: ClickFunction? = null
    var onAddCustomListClick: ClickFunction? = null
    var onPlayerBarClick: ClickFunction? = null
    var onDeleteCustomListClick: ((item: PlayListItem) -> Unit)? = null
    var onRenameCustomListClick: ((item: PlayListItem) -> Unit)? = null
    var onPlayCustomListClick: ((item: PlayListItem) -> Unit)? = null
    var onOpenCustomListClick: ((item: PlayListItem) -> Unit)? = null

    private var headImageView: ImageView? = null
    private var lastHeadImage: Bitmap? = null
    private var currentHeadImage: Bitmap? = null
    private var imageViewWidth: Int = 0
    private var imageViewHeight: Int = 0

    @Composable
    fun ComposeContent() {
        HomeCompose()
    }

    @Composable
    private fun HomeCompose() {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)) {
            val listState = rememberLazyListState()

            Column (modifier = Modifier.fillMaxSize()) {

                TopAppBar(title = { Text(text = "Simple Player") })

                Column(modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth()
                    .padding(16.dp)
                ) {
                    HeadImage2(
//                        imageUri = { headImageUri }
                    )
                    val list = remember {
                        customList
                    }
                    LazyColumn (modifier = Modifier
                        .fillMaxSize()
                        .weight(1F),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row (modifier = Modifier.fillMaxWidth()) {
                                HomeClickableCard(modifier = Modifier.weight(1F), item = history) {
                                    onHistoryPlaylistClick?.invoke()
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                HomeClickableCard(item = favorite, modifier = Modifier.weight(1F)) {
                                    onFavoritePlaylistClick?.invoke()
                                }
                            }
                        }
                        item{
                            Row (modifier = Modifier.fillMaxWidth()) {
                                HomeClickableCard(modifier = Modifier.weight(1F), item = localList) {
                                    onLocalPlaylistClick?.invoke()
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                HomeClickableCard(item = kgList, modifier = Modifier.weight(1F)) {
                                    onKgPlaylistClick?.invoke()
                                }
                            }
                        }
                        item {
                            CustomPlaylistArea()
                        }
                        itemsIndexed(list) { _, item ->
                            AnimatedVisibility(visible = customPlaylistShowState.value) {
                                CustomPlaylistItem(
                                    item = item,
                                    onClick = {
                                        onOpenCustomListClick?.invoke(item)
                                    },
                                    onItemClick = { index ->
                                        when (index) {
                                            0 -> onPlayCustomListClick?.invoke(item)
                                            1 -> onRenameCustomListClick?.invoke(item)
                                            2 -> onDeleteCustomListClick?.invoke(item)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    BottomPlayerBar()
                }
            }
        }
    }

    @Composable
    private fun CustomPlaylistArea() {
        CustomPlaylistControl(
            expandClick = {
                customPlaylistShowState.value = !customPlaylistShowState.value
            },
            addClick = {
                onAddCustomListClick?.invoke()
            }
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun BottomPlayerBar() {
        var showSimple by remember {
            simpleBottomPlayerBar
        }
        val longClick = { showSimple = !showSimple }
        AnimatedContent(targetState = showSimple) { target ->
            if (target) {
                SimplePlayerCard(onLongPress = longClick)
            } else {
                PlayerCard(onLongPress = longClick)
            }
        }
    }

    data class Item(
        val icon: Int,
        val text: String,
        val click: (() -> Unit)? = null
    )

    data class PlayListItem(
        val id: MutableState<Long>,
        val artwork: MutableState<Any?>,
        val name: MutableState<String>,
        val songCount: MutableState<Int>,
        val isPlaying: MutableState<Boolean> = mutableStateOf(false)
    )

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun PlayerCard(onLongPress: (() -> Unit)?) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = {
                        onPlayerBarClick?.invoke()
                    },
                    onLongClick = onLongPress
                ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column (modifier = Modifier
                .background(windowBackgroundAlpha)
                .padding(16.dp)
            ) {
                SongMessage()
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { onPlayModeButtonClick?.invoke() }) {
                        PlayModeIcon()
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { onPreviousButtonClick?.invoke() }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = R.drawable.ic_baseline_skip_previous_24),
                            contentDescription = ""
                        )
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { onPlayButtonClick?.invoke() }) {
                        PlayIcon()
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { onNextButtonClick?.invoke() }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = R.drawable.ic_skip_next),
                            contentDescription = ""
                        )
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp),
                        onClick = { onLikeButtonClick?.invoke() }
                    ) {
                        LikeIcon()
                    }
                }
            }
        }
    }

    @Composable
    private fun HeadImage(
        imageUri: () -> MutableState<Uri>
    ) {
        if (!headImageVisible.value) {
            return
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) { ->
                    detectTapGestures(
                        onTap = {
                            onHeadImageClick?.invoke()
                        }
                    )
                }
            ,
            shape = RoundedCornerShape(12.dp),
            elevation = 0.dp
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri().value)
                    .crossfade(false)
                    .allowHardware(true)
                    .allowConversionToBitmap(true)
                    .build(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .aspectRatio(16F / 9F),
                contentScale = ContentScale.Crop,
                contentDescription = "asf",
                alignment = Alignment.TopCenter
            )
        }
        ColumnSpace(height = 16.dp)
    }

    @Composable
    private fun HeadImage2() {
        if (!headImageVisible.value) {
            return
        }
        AndroidView(
            factory = {
                headImageView = ImageView(it).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                }
                imageViewWidth = Store.screenWidth - 32.dps
                imageViewHeight = (imageViewWidth * 9.0 / 16.0).toInt()
                applyImageBitmap()
                headImageView!!
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16F / 9F)
                .clip(shape = RoundedCornerShape(12.dp))
                .pointerInput(Unit) { ->
                    detectTapGestures(
                        onTap = {
                            onHeadImageClick?.invoke()
                        }
                    )
                }
        )
        ColumnSpace(height = 16.dp)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SimplePlayerCard(onLongPress: (() -> Unit)?) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(windowBackgroundAlpha)
                .combinedClickable(
                    onClick = {
//                        startActivity(PlayerContentNew::class.java)
                        onPlayerBarClick?.invoke()
                    },
                    onLongClick = onLongPress
                ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row (modifier = Modifier
                .background(windowBackgroundAlpha)
                .padding(16.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Store.state.songTitle.value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1F),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                RowSpace(width = 64.dp)
                IconButton(
                    modifier = Modifier
                        .size(24.dp),
                    onClick = {
//                        SimplePlayer.startOrPause(false)
                        onPlayButtonClick?.invoke()
                    }
                ) { PlayIcon() }
                RowSpace(width = 16.dp)
                IconButton(modifier = Modifier
                    .size(24.dp), onClick = { onNextButtonClick?.invoke() }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.ic_skip_next),
                        contentDescription = ""
                    )
                }
            }
        }
    }

    @Composable
    private fun SongMessage() {

        Row {
            Log.e("", SimplePlayer.currentSong.id.toString())
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ArtworkProvider.getArtworkDataForCoil(
                        SimplePlayer.currentSong
                    ))
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .error(R.drawable.default_artwork)
//                    .allowHardware(true)
                    .build(),
                contentDescription = "",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Column(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .height(64.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = Store.state.songTitle.value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Store.state.songArtist.value,
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }

    @Composable
    private fun PlayIcon() {
        val state by remember { Store.state.playState }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = if (state) R.drawable.ic_baseline_pause_24 else R.drawable.ic_play_dark),
            contentDescription = ""
        )
    }

    @Composable
    private fun PlayModeIcon() {
        val mode by remember { Store.state.currentPlayMode }
        val icon = when (mode) {
            SimplePlayer.PLAY_MODE_REPEAT -> R.drawable.ic_baseline_repeat_one_24
            SimplePlayer.PLAY_MODE_RANDOM -> R.drawable.ic_baseline_shuffle_24
            else -> R.drawable.ic_baseline_repeat_24
        }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = icon),
            contentDescription = "",
        )
    }

    @Composable
    private fun LikeIcon() {
        val state by remember { Store.state.isCurrentSongLike }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = if (state) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24),
            contentDescription = "",
            tint = if (state) Color.Red else Color.Black
        )
    }

    fun setHeadImageVisible(visible: Boolean) {
        headImageVisible.value = visible
        if (visible) {
            applyImageBitmap()
        } else {
            headImageView?.setImageDrawable(null)
            headImageView?.post {
                val current = currentHeadImage
                val last = lastHeadImage
                if (current != null && !current.isRecycled) {
                    current.recycle()
                }
                if (last != null && !last.isRecycled) {
                    last.recycle()
                }
            }
        }
    }

    fun setBottomPlayerBarStyle(style: String) {
        simpleBottomPlayerBar.value = style == PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_SIMPLE
    }

    private val recyclePost = Runnable {
        val last = lastHeadImage
        last ?: return@Runnable
        if (currentHeadImage == lastHeadImage) {
            return@Runnable
        }
        if (!last.isRecycled) {
            last.recycle()
        }
    }

    fun setHeadImageUri(uri: Uri) {
        headImageUri.value = uri
        applyImageBitmap()
    }

    private fun applyImageBitmap() {
        val imageView = headImageView
        imageView ?: return
        val imageRequest = ImageRequest.Builder(context = context)
            .data(headImageUri.value)
            .allowRgb565(true)
            .size(
                width = imageViewWidth,
                height = imageViewHeight
            )
            .listener(
                onSuccess = { _, result ->
                    lastHeadImage = currentHeadImage
                    currentHeadImage = result.drawable.toBitmap(
                        width = imageViewWidth,
                        height = imageViewHeight
                    )
                    imageView.setImageBitmap(currentHeadImage)
                    imageView.post(recyclePost)
                }
            )
            .build()
        Coil.imageLoader(context).enqueue(imageRequest)
    }
}

typealias ClickFunction = () -> Unit