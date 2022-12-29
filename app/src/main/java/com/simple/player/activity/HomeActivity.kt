package com.simple.player.activity

import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simple.player.*
import com.simple.player.service.SimpleService
import com.simple.player.Util.dps
import com.simple.player.constant.PreferencesData
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.ext.startActivity
import com.simple.player.ext.toast
import com.simple.player.playlist.PlaylistManager
import com.simple.player.screen.*
import com.simple.player.service.PlayBinder
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.*
import com.simple.player.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

//android:listSelector="@android:color/transparent"
class HomeActivity : BaseActivity2(), ServiceConnection {

    companion object {
        const val TAG = "HomeActivity"
    }

    private var bin: PlayBinder? = null
    private var isStarted = false

    private val homeContentScreen = HomeContentScreen(this)
    private val homeDrawerScreen = HomeDrawerScreen()
    private val splashScreen = SplashScreen(this)

//    private val customList = mutableStateListOf<PlayListItem>()

    private var selectImageResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (result != null) {
                cropImageResult.launch(result)
            }
        }

    private var cropImageResult =
        registerForActivityResult(CropImage()) {
            homeContentScreen.setHeadImageUri(it)
//            model.headImageState.value++
        }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        bin = iBinder as PlayBinder
    }

    override fun onServiceDisconnected(p0: ComponentName?) {}

    private lateinit var backClick: () -> Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.taskId = taskId
        setContent {
            ComposeTestTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        Splash {navController}
                    }
                    composable("main") {
                        Log.e(TAG, "onCreate: main navigator")
                        Main()
                    }
                }

            }
        }
    }

    @Composable
    private fun Splash(navController: () -> NavHostController) {
        val scope = rememberCoroutineScope()
        splashScreen.composeContent()
        splashScreen.startInit(
            onSuccess = {
                scope.launch {
                    navController().popBackStack()
                    navController().navigate("main")
                    delay(200L)
                    Log.e(TAG, "onCreate: LoadMain")
                    loadMain()
                }
            },
            onFailure = {
                finish()
            }
        )

    }

    @Composable
    private fun Main() {
        val state = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        backClick = {
            val isClosed = state.isClosed
            scope.launch {
                state.close()
            }
            isClosed
        }
        ComposeTestTheme {
            ModalDrawer(
                drawerShape = RectangleShape,
                drawerState = state,
                drawerContent = { homeDrawerScreen.ComposeContent() },
                content = {
                    homeContentScreen.ComposeContent()
                }
            )
        }

    }

    private fun loadMain() {
        val customLists = PlaylistManager.allCustomLists()
        for (customList in customLists) {
            val song = if (customList.count <= 0) null else customList[customList.count - 1]
            val item = HomeContentScreen.PlayListItem(
                id = mutableStateOf(customList.id),
                artwork = mutableStateOf(if (song == null) null else ArtworkProvider.getArtworkDataForCoil(song)),
                name = mutableStateOf(customList.name),
                songCount = mutableStateOf(customList.count),
                isPlaying = mutableStateOf(customList.id == SimplePlayer.activePlaylist.id)
            )
            homeContentScreen.customList.add(item)
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
        initHomeContentScreen()
        initHomeDrawerScreen()
    }

    private fun initHomeDrawerScreen() {
        homeDrawerScreen.onDrawerMenuItemClick = { item, index ->
            when (index) {
                0 -> {
                    startActivity(Intent(this@HomeActivity, ScanMusicActivity::class.java).apply {
                        setPackage(packageName)
                    })
                }
                1 -> toast("none")
                2 -> startActivity(Intent(this@HomeActivity, CheckHeadset::class.java))
                3 -> if (BuildConfig.DEBUG)
                    startActivity(Intent(this@HomeActivity, DeveloperActivity::class.java))
                else toast("仅在调试版本可用")
                4 -> startActivity(ExperimentalActivity::class.java)
                5 -> startActivity(NewSettingsActivity::class.java)
                6 -> exit()
            }
        }

    }

    private fun initHomeContentScreen() {
        homeContentScreen.apply {
            onPlayerBarClick = {
                startActivity(PlayerContentNew::class.java)
            }
            onNextButtonClick = {
                SimplePlayer.playNext()
            }
            onPreviousButtonClick = {
                SimplePlayer.playPrevious()
            }
            onPlayButtonClick = {
                SimplePlayer.startOrPause(isNoFade = false)
            }
            onOpenCustomListClick = {
                val intent = Intent(this@HomeActivity, PlaylistActivity::class.java)
                intent.setPackage(packageName)
                intent.putExtra(PlaylistActivity.EXTRA_LIST_NAME, it.name.value)
                startActivity(intent)
            }
            onDeleteCustomListClick = {
                DialogUtil.confirm(
                    context = this@HomeActivity,
                    title = "提示",
                    message = "是否删除播放列表 ${it.name.value} ?",
                    positive = { _, _ -> PlaylistManager.delete(it.name.value) }
                )
            }
            onPlayCustomListClick = {
                val playlist = PlaylistManager.getList(it.name.value)
                if (playlist != null) {
                    SimplePlayer.activePlaylist = playlist
                    if (playlist.count > 0) {
                        SimplePlayer.loadMusicOrStart(playlist[0]!!, isNoFade = true)
                    }
                }
            }
            onHeadImageClick = {
                selectImageResult.launch("image/*")
            }
            onAddCustomListClick = {
                DialogUtil.input(
                    this@HomeActivity,
                    title = "添加播放列表",
                    hint = "列表名称"
                ) { _, _, content ->
                    content ?: return@input
                    if (PlaylistManager.hasList(content)) {
                        toast("该列表已存在")
                    } else {
                        PlaylistManager.create(content)
                    }
                }
            }
            onPlayModeButtonClick = {
                SimplePlayer.nextPlayMode()
            }
            onLikeButtonClick = {
                with(PlaylistManager) {
                    val song = SimplePlayer.currentSong
                    if (favoriteList.hasSong(song)) {
                        removeSong(FAVORITE_LIST, song)
                    } else {
                        addSong(FAVORITE_LIST, song)
                    }
                }
            }
            onHistoryPlaylistClick = {
                startActivity(PlayHistoryActivity::class.java)
            }
            onFavoritePlaylistClick = {
                val intent = Intent(this@HomeActivity, PlaylistActivity::class.java)
                intent.putExtra(
                    PlaylistActivity.EXTRA_LIST_NAME,
                    PlaylistManager.FAVORITE_LIST)
                intent.setPackage(this@HomeActivity.packageName)
                startActivity(intent)
            }
            onLocalPlaylistClick = {
                if (AppConfigure.Settings.enableNewPlaylist) {
                    val intent = Intent(this@HomeActivity, NewPlaylistActivity::class.java)
                    intent.putExtra(NewPlaylistActivity.EXTRA_LIST_NAME, PlaylistManager.LOCAL_LIST)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@HomeActivity, PlaylistActivity::class.java)
                    intent.putExtra(PlaylistActivity.EXTRA_LIST_NAME, PlaylistManager.LOCAL_LIST)
                    startActivity(intent)
                }

            }
            onKgPlaylistClick = {
                if (AppConfigure.Settings.musicSource == PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE) {
                    startActivity(KgListActivity::class.java)
                } else {
                    toast("当前模式不支持")
                }
            }
        }
    }


    val height = 160.dps

    data class Item(
        val icon: Int,
        val text: String,
        val click: (() -> Unit)? = null
    )

    override fun onSongAddToList(songId: Long, listName: String) {
        if (listName != PlaylistManager.FAVORITE_LIST) {
            val list = PlaylistManager.getList(listName)
            list ?: return
            val song = list[list.count - 1]
            song ?: return
            for (listItem in homeContentScreen.customList) {
                if (listItem.name.value == listName) {
                    listItem.songCount.value = list.count
                }
            }
        }
    }

    override fun onSongsAddToList(songIds: LongArray, listName: String) {
        val playlist = PlaylistManager.getList(listName)
        playlist ?: return
        val song = playlist[songIds.last()]
        song ?: return
        for (listItem in homeContentScreen.customList) {
            if (listItem.name.value == listName) {
                listItem.songCount.value = playlist.count
                listItem.artwork.value = ArtworkProvider.getArtworkDataForCoil(song)
                break
            }
        }
    }

    override fun onSongRemovedFromList(songId: Long, listName: String) {
        if (listName != PlaylistManager.FAVORITE_LIST) {
            val list = PlaylistManager.getList(listName)
            list ?: return
            val song = list[list.count - 1]
            song ?: return
            for (listItem in homeContentScreen.customList) {
                if (listItem.name.value == listName) {
                    listItem.songCount.value = list.count
                }
            }
        }
    }

    override fun onHeadImageVisibilityChanged(visible: Boolean) {
        homeContentScreen.setHeadImageVisible(visible = visible)
    }

    override fun onBottomPlayerBarStyleChanged(style: String) {
        homeContentScreen.setBottomPlayerBarStyle(style = style)
    }

    override fun onPlaylistCreated(listName: String) {
        val list = PlaylistManager.getList(listName)
        list ?: return
        val item = HomeContentScreen.PlayListItem(
            id = mutableStateOf(list.id),
            artwork = mutableStateOf(null),
            name = mutableStateOf(list.name),
            songCount = mutableStateOf(list.count)
        )
        homeContentScreen.customList.add(item)
    }

    override fun onPlaylistRenamed(oldName: String, newName: String) {
        for (listItem in homeContentScreen.customList) {
            if (listItem.name.value == oldName) {
                listItem.name.value = newName
                break
            }
        }
    }

    override fun onPlaylistDeleted(listName: String) {
        val allList = PlaylistManager.allCustomLists()
        homeContentScreen.customList.clear()
        for (playlist in allList) {
            val song = if (playlist.count == 0) null else playlist[playlist.count - 1]
            homeContentScreen.customList += HomeContentScreen.PlayListItem(
                id = mutableStateOf(playlist.id),
                artwork = mutableStateOf(if (song == null) null else ArtworkProvider.getArtworkDataForCoil(song)),
                name = mutableStateOf(playlist.name),
                isPlaying = mutableStateOf(false),
                songCount = mutableStateOf(playlist.count)
            )
        }
    }

    override fun onPlayingPlaylistChanged(listId: Long) {
        for (item in homeContentScreen.customList) {
            item.isPlaying.value = item.id.value == listId
        }
    }

    override fun onResume() {
        if (!isStarted) {
            isStarted = true
        }
        super.onResume()
    }

    private fun exit() {
        AppConfigure.Player.rememberProgress = SimplePlayer.current.toLong()
        AppConfigure.Player.rememberId = SimplePlayer.currentSong.id
        finish()
        bin?.close()
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
                putExtra("aspectX", 16)
                putExtra("aspectY", 9)
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