package com.simple.player.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import com.simple.player.model.Song
import com.simple.player.playlist.Playlist
import com.simple.player.screen.KgListScreen
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.DarkGreen
import com.simple.player.util.FileUtil
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class KgListActivity: BaseActivity2() {

    private val screen = KgListScreen(this)

    private val paths = arrayOf(
        File("/storage/emulated/0/kugou/down_c/default/"),
        File("/storage/emulated/0/Android/data/com.kugou.android.lite/files/kugou/down_c/default/"),
        File("/storage/emulated/0/Android/data/com.kugou.android/files/kugou/down_c/default/")
    )
    private val tags = arrayOf(
        "旧版" to Color.LightGray, "概念版" to DarkGreen, "普通版" to Color.Blue
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }

        screen.onRefresh = {
            loadKgeFile()
        }
        screen.onItemClick = { position, song ->
            if (SimplePlayer.activePlaylist.name != LIST_NAME) {
                SimplePlayer.activePlaylist = playlist
            }
            SimplePlayer.loadMusicOrStart(song = song)
            screen.playPosition.value = position
        }
        MainScope().launch {
            delay(300)
            loadKgeFile()
            if (SimplePlayer.activePlaylist.name == LIST_NAME) {
                screen.playPosition.value = playlist.position(SimplePlayer.currentSong)
            }
        }
    }

    override fun onSongChanged(newSongId: Long) {
        if (SimplePlayer.activePlaylist.name == LIST_NAME) {
            screen.playPosition.value = playlist.position(SimplePlayer.currentSong)
        }
    }

    private fun loadKgeFile() {
        playlist.clear()
        initKgePlaylist()
    }

    private fun initKgePlaylist() {
        screen.list.clear()
        var id = 0L
        for ((index, path) in paths.withIndex()) {
            if (!path.exists()) {
                Log.e(TAG, "initKgePlaylist: path not found")
                continue
            }
            val listFiles = path.listFiles()
            listFiles ?: continue
            for (file in listFiles) {
                Log.e(TAG, "initKgePlaylist: ${file.name}")
                if (!file.name.endsWith(KGE_EXTENSION) && !file.name.endsWith("kgtmp")) {
                    continue
                }
                if (file.length() < 1024 * 200) {
                    continue
                }
                val filename = file.name
                val song = Song(--id).apply {
                    title = filename
                    artist = "未知艺术家"
                    type = FileUtil.getFileType(filename)
                    bitrate = 250
                    this.path = Uri.fromFile(file).toString()
                }
                playlist += song
                screen.list.add(KgListScreen.KgListItem(
                    song = song,
                    tag = tags[index]
                ))
            }
        }
    }

    companion object {
        const val TAG = "KgListActivity"
        const val KGE_EXTENSION = "kge"
        const val LIST_NAME = "_KG_LIST_"
        val playlist = Playlist(LIST_NAME)
    }

}