package com.simple.player.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import android.view.animation.LinearInterpolator
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.simple.player.R
import com.simple.player.Util.dps
import com.simple.player.animate.LegacyAnimation.close
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.handler.SimpleHandler
import com.simple.player.playlist.PlaylistManager
import com.simple.player.screen.PlayerContentScreen
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.*
import com.simple.player.util.ArtworkProvider
import com.simple.player.util.ProgressHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerContentNew : AppCompatActivity(), MusicEventListener {

    // 进度条进度是否在人为改变
    var isChangingByHand = false
    // Activity是否处于Pause状态
    private var isActivityPaused = false
    private var mHandler: Handler? = null
    private lateinit var artworkAnimator: ObjectAnimator


    companion object {
        private const val MSG_UPDATE_PROGRESS = 101
        const val TAG = "PlayerContentNew"
        @JvmStatic
        var defaultArtwork: Bitmap? = null
    }

    private var isInitialed = false
    private fun initBase() {
        if (isInitialed) {
            return
        }
        updateInfo()
        MusicEvent2.register(this)
        mHandler = UpdateHandler(Looper.getMainLooper(), this)
        mHandler?.sendEmptyMessage(MSG_UPDATE_PROGRESS)
        isInitialed = true
    }

    override fun onPause() {
        isActivityPaused = true
        mHandler?.removeMessages(MSG_UPDATE_PROGRESS)
        if (artworkAnimator.isStarted || artworkAnimator.isRunning) {
            artworkAnimator.pause()
        }
        super.onPause()
    }

    override fun onResume() {
        isActivityPaused = false
        if (SimplePlayer.isPlaying) {
            if (!artworkAnimator.isStarted) {
                artworkAnimator.start()
            }
            if (artworkAnimator.isPaused) {
                artworkAnimator.resume()
            }
        }
        super.onResume()
    }

    private lateinit var screen: PlayerContentScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        defaultArtwork = defaultArtwork ?: resources.getDrawable(R.drawable.default_artwork, null)
            .toBitmap(
                width = 300,
                height = 300
            )
        screen = PlayerContentScreen(this)
        artworkAnimator = ObjectAnimator.ofFloat(screen, "rotation", 0F, 360F)
        artworkAnimator.apply {
            duration = 20000L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
        }
        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }
        MainScope().launch {
            delay(500)
            initBase()
        }
    }


    fun musicInfo() {
        val info = Intent(this, MusicInfo2::class.java)
        info.putExtra(MusicInfo2.EXTRA_MUSIC_ID, SimplePlayer.currentSong.id)
        startActivity(info)
    }

    fun playlist() {
        if (SimplePlayer.activePlaylist.name == KgListActivity.LIST_NAME) {
            startActivity(Intent(this, KgListActivity::class.java).apply {
                setPackage(application.packageName)
            })
            return
        }
        val intent = Intent(this, PlaylistActivity::class.java)
        intent.putExtra(
            PlaylistActivity.EXTRA_LIST_NAME,
            SimplePlayer.activePlaylist.name
        )
        startActivity(intent)
    }

    fun changeSongLikeState() {
        with (PlaylistManager) {
            val song = SimplePlayer.currentSong
            if (favoriteList.hasSong(song)) {
                removeSong(FAVORITE_LIST, song)
            } else {
                addSong(FAVORITE_LIST, song)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mHandler?.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 400)
        if (!artworkAnimator.isRunning && SimplePlayer.isPlaying) {
            artworkAnimator.start()
        }
    }

    override fun onMusicPlay() {
        if (!artworkAnimator.isStarted) {
            artworkAnimator.start()
        } else if (artworkAnimator.isPaused) {
            artworkAnimator.resume()
        }
    }

    override fun onMusicPause() {
        if (artworkAnimator.isStarted || artworkAnimator.isRunning) {
            artworkAnimator.pause()
        }
    }

    override fun onSongChanged(newSongId: Long) {
        updateInfo()
        artworkAnimator.cancel()
        artworkAnimator.start()
    }

    private fun updateInfo() {
        screen.setSong(SimplePlayer.currentSong)
        val data = ArtworkProvider.getArtworkDataForCoil(SimplePlayer.currentSong)
        if (data == null) {
            screen.setArtwork(defaultArtwork)
            screen.setMainColor(android.graphics.Color.WHITE)
            return
        }
        val request = ImageRequest.Builder(this)
            .data(data)
            .size(240.dps)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .listener(
                onSuccess = { _, result ->
                    ProgressHandler.handle {
                        val bitmap = result.drawable.toBitmap(
                            width = 240.dps,
                            height = 240.dps
                        )
                        Palette.from(bitmap).generate {
                            it?.let {
                                val color = it.getVibrantColor(android.graphics.Color.WHITE)
                                screen.setMainColor(color)
//                                val imageBitmap = bitmap.asImageBitmap()
                                screen.setArtwork(bitmap = bitmap)
                            }
                        }
                    }
                },
                onError = { _, _ ->
                    screen.setArtwork(defaultArtwork)
                    screen.setMainColor(android.graphics.Color.WHITE)
                }
            )
            .build()
        Coil.imageLoader(this).enqueue(request = request)
    }

    override fun onDestroy() {
        super.onDestroy()
        screen.setArtwork(defaultArtwork)
        mHandler?.removeCallbacksAndMessages(null)
        MusicEvent2.unregister(this)
        artworkAnimator.close()
    }

    private class UpdateHandler(looper: Looper, parent: PlayerContentNew) :
        SimpleHandler<PlayerContentNew>(looper, parent) {

        override fun handleMessage(msg: Message) {
            val parent = this.parent
            if (msg.what == MSG_UPDATE_PROGRESS && parent != null) {
                // 当 Activity 不处于 paused 且没有拖动进度条时，更新进度条
                if (!parent.isActivityPaused && !parent.isChangingByHand) {
                    parent.screen.setCurrent(SimplePlayer.current)
                }
                sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 400)
            }
            super.handleMessage(msg)
        }
    }
}