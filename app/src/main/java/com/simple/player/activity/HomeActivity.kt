package com.simple.player.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.simple.player.*
import com.simple.player.R
import com.simple.player.service.SimpleService
import com.simple.player.Util.dps
import com.simple.player.ext.toast
import com.simple.player.model.HomeMainModel
import com.simple.player.model.Song
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.PlayBinder
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.*
import com.simple.player.util.*
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

//android:listSelector="@android:color/transparent"
@OptIn(ExperimentalMaterialApi::class)
class HomeActivity : BaseActivity(),
    MusicEvent.OnMusicPlayListener,
    MusicEvent.OnMusicPauseListener,
    MusicEvent.OnSongAddToListListener,
    MusicEvent.OnSongRemovedFromListListener,
    MusicEvent.OnSongChangedListener,
    MusicEvent.OnPlayModeChangedListener,
    View.OnClickListener {

    companion object {
        const val TAG = "HomeActivity"
    }

    lateinit var bin: PlayBinder
    private var isStarted = false
    private val model = HomeMainModel()

    private val localList = Item(R.drawable.ic_baseline_phone_android_24, "本地列表")
    private val favorite = Item(R.drawable.ic_baseline_favorite_24, "我喜欢")
    private val history = Item(R.drawable.ic_baseline_history_24, "播放历史")

    private val drawerList = listOf(
        Item(R.drawable.ic_baseline_search_24, "歌曲扫描") {
            startActivity(Intent(this@HomeActivity, ScanMusicActivity::class.java).apply {
                setPackage(packageName)
            })
        },
        Item(R.drawable.ic_baseline_delete_24, "清除专辑封面缓存") {
            toast("none")
        },
        Item(R.drawable.ic_baseline_headset_24, "佩戴检查") {
            startActivity(Intent(this@HomeActivity, CheckHeadset::class.java))
        },
        Item(R.drawable.ic_baseline_developer_mode_24, "开发者") {
            startActivity(Intent(this@HomeActivity, DeveloperActivity::class.java))
        },
        Item(R.drawable.ic_baseline_web_24, "网页播放器") {

        },
        Item(R.drawable.ic_baseline_settings_24, "设置") {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        },
        Item(R.drawable.ic_baseline_exit_to_app_24, "退出") {exit()}
    )

    private var selectImageResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (result != null) {
                cropImageResult.launch(result)
            }
        }

    private var cropImageResult =
        registerForActivityResult(CropImage()) {
//            Glide.with(this)
//                .load(it)
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into(model.holder.headPicture)
        }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        bin = iBinder as PlayBinder
        MusicEventHandler.register(this)
    }
    private lateinit var backClick: () -> Boolean
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.taskId = taskId
        showActionBar = false
        setContent {
            val state = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            backClick = {
                val isClosed = state.isClosed
                scope.launch {
                    state.close()
                }
                isClosed
            }
            Log.e(TAG, "repaint")
            ComposeTestTheme {
                ModalDrawer(
                    drawerShape = RectangleShape,
                    drawerState = state,
                    drawerContent = {
                        DrawerCompose()
                    }
                ) {
                    HomeCompose()
                }
            }
        }
        // 启动服务
        val intentService = Intent().apply {
            action = SimpleService.ACTION_SIMPLE_SERVICE
            setPackage(packageName)
        }

        bindService(intentService, this, BIND_AUTO_CREATE)
        if (!isStarted)
            startService(intentService)
        isStarted = true
        /*with(model.holder) {
            headPicture.setOnClickListener(this@HomeActivity)
        }

        val drawerLayout = model.holder.drawerLayout
        Util.setCustomLeftEdgeSize(
            this,
            drawerLayout,
            0.1f
        )

        drawerLayout.setScrimColor(android.graphics.Color.parseColor("#80000000"))
        drawerLayout.addDrawerListener(object: DrawerLayout.SimpleDrawerListener() {
            val extraSpace = 65.dps

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

                val maxOffset = 1 - extraSpace / drawerLayout.width.toFloat()
                val offset = slideOffset * maxOffset
                val view = drawerLayout.getChildAt(0)

                drawerView.x = (drawerLayout.width) * (1 - offset)
                drawerView.alpha = 0.5f + 0.5f * slideOffset
                val targetWidth = view.width * 0.65f
                view.translationX = -(targetWidth - extraSpace) * slideOffset

            }
        })

        if (FileUtil.mHeaderPicture.exists()) {
            Glide.with(this)
                .load(FileUtil.mHeaderPicture)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(model.holder.headPicture)
        }
        model.holder.exit.setOnClickListener(this)
        model.holder.setting.setOnClickListener(this)

        loadDrawerList()

        model.holder.viewPager2.setPageTransformer { page, position ->
            if (-1.0f <= position && position <= 0.0f){   //2
                page.x = -(page.width * 0.5f) * abs(position)
                page.alpha = 1 - 0.5f * abs(position)
            }
        }*/
    }



    @Composable
    fun DrawerCompose() {
        DrawerSongInfo()
        LazyColumn (modifier = Modifier.fillMaxWidth()) {
            items(drawerList) { e ->
                ListItem(e)
            }
        }
    }

    @Composable
    fun DrawerSongInfo() {
        val song = remember {
            model.currentSong
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(NRed),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ArtworkProvider.getArtworkUri(song.value))
                    .crossfade(false)
                    .allowHardware(true)
                    .allowRgb565(true)
                    .build(),
                contentDescription = "",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
            )
            Text(
                text = song.value.title,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)
            )
        }
    }

    @Composable
    fun ListItem(item: Item) {
        Row(modifier = Modifier.fillMaxWidth().height(48.dp).clickable(onClick = item.click ?: {}), verticalAlignment = Alignment.CenterVertically) {
            RowSpace(width = 16.dp)
            RoundIcon(
                painter = painterResource(id = item.icon),
                contentPadding = 8.dp,
                iconSize = 22.dp,
                contentDescription = "",
                color = NRed,
                tint = Color.White
            )
            Text(text = item.text, modifier = Modifier.padding(start = 16.dp, end = 16.dp), fontSize = 16.sp)
        }
    }

    val height = 160.dps

    @Composable
    fun HomeCompose() {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDEDED))) {
            val listState = rememberLazyListState()


            HomeBackground {
                listState
            }

            LazyColumn (modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                state = listState
            ) {
                item {
                    Spacer(modifier = Modifier.height(160.dp))
                }
                item {
                    PlayerCard {model.currentSong}
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    Row (modifier = Modifier.fillMaxWidth()) {
                        ClickableCard(modifier = Modifier.weight(1F), item = history) {
                            startActivity(Intent(this@HomeActivity, PlayHistoryActivity::class.java))
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        ClickableCard(item = favorite, modifier = Modifier.weight(1F)) {
                            val intent = Intent(this@HomeActivity, PlaylistActivity::class.java)
                            intent.putExtra(
                                PlaylistActivity.EXTRA_LIST_NAME,
                                PlaylistManager.FAVORITE_LIST)
                            intent.setPackage(this@HomeActivity.packageName)
                            startActivity(intent)
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    ClickableCard(item = localList, modifier = Modifier.fillMaxWidth()) {
                        val intent = Intent(this@HomeActivity, PlaylistActivity::class.java)
                        intent.putExtra(PlaylistActivity.EXTRA_LIST_NAME, PlaylistManager.LOCAL_LIST)
                        startActivity(intent)
                    }
                }

            }
        }


    }

    @Composable
    fun HomeBackground(listState: () -> LazyListState) {
        val alpha = remember {
            mutableStateOf(1F)
        }
        var currentOffset by remember {
            mutableStateOf(listState().firstVisibleItemScrollOffset)
        }
        if (listState().isScrollInProgress) {
            DisposableEffect(Unit) {
                onDispose {
                    alpha.value = 1F
                }
            }
            alpha.value = if (listState().firstVisibleItemIndex == 0) {
                Log.d("TAG", "current offset ${currentOffset}")
                (height.toFloat() - currentOffset.toFloat()) / height.toFloat()
            } else {
                0F
            }
            currentOffset = listState().firstVisibleItemScrollOffset
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha.value)
            .aspectRatio(1.5F, true)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(FileUtil.mHeaderPicture)
                    .crossfade(false)
                    .allowHardware(true)
                    .allowConversionToBitmap(true)
                    .build(),
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                contentDescription = "asf"
            )
            val fact = 50F
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xFFEDEDED)
                        )
//                        startY = (height.toFloat() / fact) * (fact - 1)
                    )
                )
            )
        }
    }

    data class Item(
        val icon: Int,
        val text: String,
        val click: (() -> Unit)? = null
    )

    @Composable
    fun PlayerCard(song: () -> MutableState<Song>) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    startActivity(
                        Intent(
                            this@HomeActivity,
                            PlayerContentNew::class.java
                        )
                    )
                },
            backgroundColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column (modifier = Modifier.padding(16.dp)) {
                SongMessage {
                    song()
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    val modifier = Modifier
                        .size(24.dp)
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { SimplePlayer.nextPlayMode() }) {
                        PlayModeButton()
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { SimplePlayer.playPrevious() }) {
                        Icon(
                            modifier = modifier,
                            painter = painterResource(id = R.drawable.ic_baseline_skip_previous_24),
                            contentDescription = ""
                        )
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { SimplePlayer.startOrPause(false) }) {
                        PlayButton()
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = { SimplePlayer.playNext() }) {
                        Icon(
                            modifier = modifier,
                            painter = painterResource(id = R.drawable.ic_skip_next),
                            contentDescription = ""
                        )
                    }
                    IconButton(modifier = Modifier
                        .weight(1F)
                        .size(24.dp), onClick = {
                        with(PlaylistManager) {
                            val song = SimplePlayer.currentSong
                            if (favoriteList.hasSong(song)) {
                                removeSong(FAVORITE_LIST, song)
                            } else {
                                addSong(FAVORITE_LIST, song)
                            }
                        }
                    }
                    ) {
                        LikeButton()
                    }
                }
            }
        }
    }

    @Composable
    fun SongMessage(song: () -> MutableState<Song>) {

        Row {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(ArtworkProvider.getArtworkUri(song().value))
                    .crossfade(false)
                    .allowHardware(true)
                    .allowRgb565(true)
                    .allowConversionToBitmap(true)
                    .transformations(RoundedCornersTransformation(radius = 4.dps.toFloat()))
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
                    text = song().value.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = song().value.artist,
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
    }

    @Composable
    fun PlayButton() {
        val state by remember {
            model.playState
        }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = if (state) R.drawable.ic_baseline_pause_24 else R.drawable.ic_play_dark),
            contentDescription = ""
        )
    }

    @Composable
    fun PlayModeButton() {
        val mode by remember {
            model.playMode
        }
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
    fun LikeButton() {
        val state by remember {
            model.likeState
        }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = if (state) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24),
            contentDescription = "",
            tint = if (state) {
                Color.Red
            } else Color.Black
        )
    }

    @Composable
    fun ClickableCard(
        modifier: Modifier = Modifier,
        item: Item,
        onClick: (() -> Unit)? = null
    ) {
        val m = modifier
            .clip(shape = RoundedCornerShape(12.dp))
            .clickable { onClick?.invoke() }
        Card (modifier = m, shape = RoundedCornerShape(12.dp), elevation = 0.dp) {
            Row (
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = { /*TODO*/ }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(id = item.icon),
                        contentDescription = "",
                        tint = NRed
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    override fun onSongChanged(newSongId: Long) {
        model.playState.value = true
        if (!SimplePlayer.isPlaying) {
            SimplePlayer.start()
        }
        model.currentSong.value = SimplePlayer.currentSong
        model.likeState.value = SimplePlayer.isCurrentSongLike
    }

    override fun onSongAddToList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == SimplePlayer.currentSong.id) {
                model.likeState.value = true
            }
        }
    }

    override fun onSongRemovedFromList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == SimplePlayer.currentSong.id) {
                model.likeState.value = false
            }
        }
    }

    override fun onPlayModeChanged(oldMode: Int, newMode: Int) {
        model.playMode.value = newMode
    }

    override fun onMusicPause() {
        model.playState.value = false
    }

    override fun onMusicPlay() {
        model.playState.value = true
    }


    override fun onResume() {
        if (!isStarted) {
            isStarted = true
        }
        super.onResume()
    }

    override fun onClick(p1: View) {
        when (p1.id) {
            R.id.home_slide_menu_head_pic -> {
                selectImageResult.launch("image/*")
            }
        }
    }



    private fun exit() {
        finish()
        MusicEventHandler.unregister(this)
        bin.close()
        unbindService(this)
        val intentService = Intent().apply {
            action = SimpleService.ACTION_SIMPLE_SERVICE
            setPackage(packageName)
        }
        ProgressHandler.shutdown()
        stopService(intentService)
        PlaylistManager.clear()
        System.gc()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    override fun onBackPressed() {
        val canBack = backClick()
        if (canBack) {
            moveTaskToBack(true)
        }
    }

    private fun deleteAllArtworkCache() {
        var isEmpty = false

        ProgressHandler.handle(before = {
            Util.showProgressDialog(this@HomeActivity, 6, "正在清除……")
        }, handle = {
            isEmpty = ArtworkProvider.clearArtworkCache(this@HomeActivity)
        }, after = {
            Util.closeProgressDialog(6)
            toast(if (isEmpty) "缓存已清除" else "尚未发现缓存")
        })
    }


    private class CropImage: ActivityResultContract<Uri, Uri>() {

        private var outUri: Uri? = null

        override fun createIntent(context: Context, input: Uri): Intent {
            input ?: return Intent()
            outUri = Uri.fromFile(FileUtil.mHeaderPicture)
            val intent = Intent("com.android.camera.action.CROP")
            val mineTypes = context.contentResolver.getType(input)
            intent.setDataAndType(input, mineTypes)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
            }
            context.grantUriPermission(context.packageName, outUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
            with(intent) {
                putExtra("crop", "true")
                putExtra("scale", true)
                putExtra("aspectX", 4)
                putExtra("aspectY", 3)
                //intent.putExtra("outputX",deviceWidth);
                //intent.putExtra("outputY",deviceHeight);
                putExtra("return-data", false)
                putExtra(MediaStore.EXTRA_OUTPUT, outUri)
                putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString())
                putExtra("noFaceDetection", true) // no face detection
            }
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri {
            outUri ?: return Uri.parse("")
            return outUri!!
        }
    }
}