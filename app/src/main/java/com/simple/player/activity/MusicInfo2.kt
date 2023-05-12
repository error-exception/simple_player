package com.simple.player.activity

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.R
import com.simple.player.Util.copyText
import com.simple.player.constant.PreferencesData
import com.simple.player.model.Song
import com.simple.player.playlist.PlaylistManager
import com.simple.player.ui.theme.windowBackgroundAlpha
import com.simple.player.util.AppConfigure
import com.simple.player.util.ArtworkProvider

class MusicInfo2: BaseActivity2() {

    private var title = mutableStateOf("歌曲信息")
    private var backIcon = mutableStateOf(R.drawable.ic_baseline_arrow_back_24)
    private lateinit var song: Song
    private var isAlbumExist = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        setContent {
            ComposeTestTheme {
                Surface {
                    Column (modifier = Modifier
                        .fillMaxSize()
                    ) {
                        Toolbar(title, backIcon = backIcon, backClick = { finish() })
                        SongInfo()
                    }
                }
            }
        }
    }

    @Composable
    fun SongInfo() {

        Column (modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = windowBackgroundAlpha) {
                Row (modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ArtworkProvider.getArtworkDataForCoil(song))
                            .allowHardware(true)
                            .build(),
                        modifier = Modifier
                            .size(80.dp)
                            .border(2.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp)),
                        contentDescription = "专辑图片",
                        onError = {
                            isAlbumExist = false
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column (modifier = Modifier.height(80.dp), verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)) {
                        Text(
                            text = song.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1
                        )
                        Text(
                            text = song.artist,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1F), color = windowBackgroundAlpha) {
                Column (modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1F)) {
                    for (pair in info) {
                        Row {
                            Text(modifier = Modifier.weight(1F), text = pair.first, fontWeight = FontWeight.SemiBold)
                            Text(
                                modifier = Modifier.weight(4F)
                                    .clickable {
                                        copyText(pair.second)
                                    },
                                text = pair.second
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1F)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = edit),
                    color = windowBackgroundAlpha
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "修改封面", fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1F)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = export),
                    color = windowBackgroundAlpha
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(text = "导出封面", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    private val edit = {

    }

    private val export: () -> Unit = {
//        if (!isAlbumExist) {
//            Toast.makeText(this, "当前歌曲的专辑图片不存在", Toast.LENGTH_LONG).show()
//        }
//        var outFile: File? = null
//        var bit: Bitmap? = null
//        ProgressHandler.handle(
//            before = {
//                Util.showProgressDialog(this, 69, "正在导出……")
//            },
//            after = {
//                Util.closeProgressDialog(69)
//                if (outFile!!.exists())
//                    android.app.AlertDialog.Builder(this)
//                        .setTitle("保存成功")
//                        .setMessage("当前歌曲的专辑图片已保存在" + outFile.absolutePath + "中")
//                        .setPositiveButton("确定", null)
//                        .show()
//            },
//            handle = {
//                val uri = ArtworkProvider.getArtworkUriString(song)
//                if (uri != null) {
//                    val data = FileUtil.readBytes(uri)
//                    FileUtil.writeBytes(FileUtil.mDataDirectory, data)
//                }
//            }
//        )
    }

    private var info: ArrayList<Pair<String, String>> = ArrayList()

    private fun initData() {
        val songId = intent.getLongExtra(EXTRA_MUSIC_ID, -1)
        song = PlaylistManager.getLocalList().getSong(songId)!!
        info.apply {
            this += "标题：" to song.title
            this += "艺术家：" to song.artist
            this += "ID：" to song.id.toString(10)
            this += "Uri：" to Uri.decode(song.uri)
            this += "比特率：" to song.bitrate.toString(10)
            this += "类型：" to song.type
            if (AppConfigure.Settings.musicSource == PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE) {
                this += "大小：" to "%.2fMB".format(Uri.parse(song.uri).toFile().length() / (1024 * 1024.0))
            }
        }
    }

    companion object {
        const val EXTRA_MUSIC_ID = "music_id"
    }

}



@Composable
fun Toolbar(title: MutableState<String>, optionIcon: MutableState<Int> = mutableStateOf(0), backIcon: MutableState<Int> = mutableStateOf(0), backClick: (() -> Unit)? = null, optionClick: (() -> Unit)? = null) {
    val t by remember { title }
    val option by remember { optionIcon }
    val back by remember { backIcon }
    val elevation = 8.dp
    if (back != 0) {
        TopAppBar(
            elevation = elevation,
            title = {
                Text(text = t)
            },
            navigationIcon = {
                IconButton(onClick = { backClick?.invoke() }) {
                    Icon(painter = painterResource(id = back), contentDescription = "返回")
                }
            },
            actions = {
                if (option != 0) {
                    IconButton(onClick = { optionClick?.invoke() }) {
                        Icon(painter = painterResource(id = option), contentDescription = "选项", tint = Color.White)
                    }
                }
            }
        )
    } else {
        TopAppBar(
            elevation = elevation,
            title = {
                Text(text = t)
            },
            actions = {
                if (option != 0) {
                    IconButton(onClick = { optionClick?.invoke() }) {
                        Icon(painter = painterResource(id = option), contentDescription = "选项", tint = Color.White)
                    }
                }
            }
        )
    }
}