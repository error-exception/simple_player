package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.simple.player.event.MusicEventListener
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.screen.PlaylistScreen
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ComposeTestTheme

class NewPlaylistActivity: BaseActivity2() {

    private val screen = PlaylistScreen(this)
    private lateinit var playlist: AbsPlaylist

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
            SimplePlayer.loadMusicOrStart(song = playlist[it]!!)
            screen.setPlayingPosition(it)
        }
    }

    companion object {
        const val EXTRA_LIST_NAME = "list_name"
    }

}