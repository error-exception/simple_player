package com.simple.player.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.graphics.ColorUtils
import com.simple.player.ext.toast
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.screen.PlaylistScreen
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ComposeTestTheme

class NewPlaylistActivity: BaseActivity2() {

    private val screen = PlaylistScreen(this)
    lateinit var playlist: AbsPlaylist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listName = intent.getStringExtra(EXTRA_LIST_NAME)
        playlist = PlaylistManager.getList(listName!!)!!

        screen.setListSource(playlist = playlist)
        screen.setPlayingPosition(playlist.position(SimplePlayer.currentSong))

        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }

        screen.onItemClick = {
            val song = playlist[it]!!
            if (SimplePlayer.currentSong == song && SimplePlayer.activePlaylist.id == playlist.id) {
                SimplePlayer.startOrPause()
            } else {
                SimplePlayer.loadMusicOrStart(song = playlist[it]!!, isNoFade = true)
                screen.setPlayingPosition(it)
                SimplePlayer.activePlaylist = playlist
            }
        }
        initNormalMenu()
        initMultiSelectionMenu()
        initPlaylistSelection()
    }

    private fun initMultiSelectionMenu() {

    }

    private fun initPlaylistSelection() {
        screen.onPlaylistSelectClick = { index, name ->
            toast("noting")
        }
    }

    private fun initNormalMenu() {
        screen.onNormalMenuItemClick = onNormalMenuItemClick@{ index, listIndex ->
            when (index) {
                0 -> {
                    SimplePlayer.loadMusicOrStart(song = playlist[listIndex]!!, isNoFade = true)
                    screen.setPlayingPosition(listIndex)
                }

                2 -> {
                    val song = playlist[listIndex]!!
                    val hasSong = PlaylistManager.favoriteList.hasSong(song)
                    if (hasSong) {
                        toast("该歌曲已存在")
                    } else {
                        PlaylistManager.addSong(PlaylistManager.FAVORITE_LIST, song = song)
                        toast("已添加")
                    }
                }

                1 -> {
                    val song = playlist[listIndex]!!
                    PlaylistManager.removeSong(playlist.name, song = song)
                    screen.remove(listIndex)
                }

                3 -> {
                    val allCustomLists = PlaylistManager.allCustomLists()
                    if (allCustomLists.isEmpty()) {
                        toast("无列表")
                        return@onNormalMenuItemClick
                    }
                    val names = Array(allCustomLists.size) {
                        allCustomLists[it].name
                    }
                    screen.openPlaylistNameList(names)
                }

                4 -> {
                    val song = playlist[listIndex]!!
                    startActivity(Intent(this, MusicInfo2::class.java).apply {
                        putExtra(MusicInfo2.EXTRA_MUSIC_ID, song.id)
                    })
                }
            }
        }
    }

    override fun onSongChanged(newSongId: Long) {
        val newPosition = playlist.position(playlist[newSongId]!!)
        screen.setPlayingPosition(newPosition)
    }

    companion object {
        const val EXTRA_LIST_NAME = "list_name"
    }

}