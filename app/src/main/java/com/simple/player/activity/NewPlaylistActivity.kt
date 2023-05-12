package com.simple.player.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.graphics.ColorUtils
import com.simple.player.ext.toast
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.playlist.SongList
import com.simple.player.screen.PlaylistScreen
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ComposeTestTheme

class NewPlaylistActivity: BaseActivity2() {

    private val screen = PlaylistScreen(this)
    lateinit var playlist: SongList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listId = intent.getLongExtra(EXTRA_LIST_ID, PlaylistManager.LOCAL_LIST_ID)
        playlist = PlaylistManager.getSongList(listId = listId)!!

        screen.setListSource(playlist = playlist)
        screen.setPlayingPosition(playlist.indexOf(SimplePlayer.currentSong))

        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }

        screen.onItemClick = {
            val song = playlist.getSongAt(it)
            if (SimplePlayer.currentSong == song && SimplePlayer.activePlaylist == playlist) {
                SimplePlayer.startOrPause()
            } else {
                SimplePlayer.loadMusicOrStart(song = song, isNoFade = true)
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
            val song = playlist.getSongAt(listIndex)
            when (index) {
                0 -> {
                    SimplePlayer.loadMusicOrStart(song, isNoFade = true)
                    screen.setPlayingPosition(listIndex)
                }

                2 -> {
                    val hasSong = PlaylistManager.getFavoriteList().hasSong(song.id)
                    if (hasSong) {
                        toast("该歌曲已存在")
                    } else {
                        PlaylistManager.addSong(PlaylistManager.FAVORITE_LIST_ID, song = song)
                        toast("已添加")
                    }
                }

                1 -> {
                    PlaylistManager.removeSong(playlist.getId(), song = song)
                    screen.remove(listIndex)
                }

                3 -> {
                    val allCustomLists = PlaylistManager.getAllExternalSongLists()
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
                    startActivity(Intent(this, MusicInfo2::class.java).apply {
                        putExtra(MusicInfo2.EXTRA_MUSIC_ID, song.id)
                    })
                }
            }
        }
    }

    override fun onSongChanged(newSongId: Long) {
        val newPosition = playlist.indexOf(newSongId)
        screen.setPlayingPosition(newPosition)
    }

    companion object {
        const val EXTRA_LIST_ID = "list_id"
    }

}