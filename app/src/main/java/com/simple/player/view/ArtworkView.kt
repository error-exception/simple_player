package com.simple.player.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.bumptech.glide.Glide
import com.simple.player.MusicEvent
import com.simple.player.MusicEventHandler
import com.simple.player.R
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import com.simple.player.util.ArtworkProvider
import java.io.Closeable
import java.lang.Exception

class ArtworkView: AppCompatImageView, Closeable, MusicEvent.OnSongChangedListener, LifecycleObserver {

    private var artworkUri: Uri? = null
    private var changeSongId = 0L
    private var showSongId = 0L
    private var lifecycle: Lifecycle? = null
    private var animation: RotationAnimation? = null

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun close() {
        lifecycle?.removeObserver(this)
        lifecycle = null
        MusicEventHandler.unregister(this)
        animation?.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun pause() {
        animation?.pause()
    }

    override fun onSongChanged(newSongId: Long) {
        val song = PlaylistManager.localPlaylist[newSongId]!!
        artworkUri = ArtworkProvider.getArtworkUri(song)
        changeSongId = newSongId
        applyImage()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun applyImage() {
        animation?.start()
        if (lifecycle != null && lifecycle!!.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            if (showSongId != changeSongId) {
                try {
                    Glide.with(this)
                        .load(artworkUri)
                        .placeholder(R.drawable.default_artwork)
                        .skipMemoryCache(true)
                        .into(this)
                } catch (e: Exception) {
                    setImageResource(R.drawable.default_artwork)
                }
                showSongId = changeSongId
            }
        }
    }

    fun setRotation(bool: Boolean) {
        animation = if (bool) {
            RotationAnimation(this, -1, 30000)
        } else {
            animation?.cancel()
            null
        }
    }

    init {
        artworkUri = ArtworkProvider.getArtworkUri(SimplePlayer.currentSong)
        if (context is LifecycleOwner) {
            val c = context as LifecycleOwner
            lifecycle = c.lifecycle
            lifecycle?.addObserver(this)
        }
        MusicEventHandler.register(this)
    }
}