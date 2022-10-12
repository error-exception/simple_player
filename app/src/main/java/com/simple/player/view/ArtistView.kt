package com.simple.player.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.simple.player.MusicEvent
import com.simple.player.MusicEventHandler
import com.simple.player.Store
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import java.io.Closeable

class ArtistView: AppCompatTextView, Closeable, MusicEvent.OnSongChangedListener {

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun close() {
        MusicEventHandler.unregister(this)
    }

    override fun onSongChanged(newSongId: Long) {
        val playlist = PlaylistManager.localPlaylist
        text = playlist[newSongId]?.artist
    }

    init {
        val song = SimplePlayer.currentSong
        text = song.artist
        MusicEventHandler.register(this)
    }

}