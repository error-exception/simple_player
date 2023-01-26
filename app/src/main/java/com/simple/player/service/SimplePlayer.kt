package com.simple.player.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toFile
import com.simple.player.Store
import com.simple.player.Util
import com.simple.player.activity.KgListActivity
import com.simple.player.constant.PreferencesData
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.model.MutablePair
import com.simple.player.model.Song
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.util.AppConfigure
import java.io.Closeable
import java.lang.Exception

object SimplePlayer: Closeable, AudioManager.OnAudioFocusChangeListener, MusicEventListener {

    const val TAG = "SimplePlayer"

    const val PLAY_MODE_REPEAT = 0
    const val PLAY_MODE_RANDOM = 1
    const val PLAY_MODE_NONE = 2

    private val player = MediaPlayer()
    private var currentIndex = 0
    private var prepareLock = MutablePair(first = true, second = false)
    private var musicFader = MusicFadeAnimation2(this)
    private val audioManager = Util.mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    /**
     * 当播放器至少播放一次时，为 true
     */
    var hasBeenStarted = false
        private set

    lateinit var currentSong: Song
        private set

    lateinit var audioAttributes: AudioAttributes
        private set

    /**
     * 当播放列表有歌曲，且 SimplePlayer 已初始化完毕时，为 true
     */
    var isPlayerAvailable = false
        private set

    var activePlaylist: AbsPlaylist = PlaylistManager.localPlaylist
        set(value) {
            if (value.name != field.name) {
                field = value
                if (KgListActivity.LIST_NAME != value.name) {
                    AppConfigure.Player.playlist = activePlaylist.name
                }
                MusicEvent2.fireOnPlayingPlaylistChanged(value.id)
            }
        }

    var fadeDuration: Long = 1000L
        set(value) {
            field = value
            // TODO: set fade duration in SettingsActivity
        }

    @Deprecated(level = DeprecationLevel.WARNING, message = "未来将在 Song 对象上或 favoriteList 上实现同操作的方法")
    var isCurrentSongLike = false
        get() = PlaylistManager.favoriteList.hasSong(currentSong)
        private set

    var playMode = PLAY_MODE_NONE
        get() = AppConfigure.Player.playMode
        set (value) {
            val old = AppConfigure.Player.playMode
            if (value != old) {
                AppConfigure.Player.playMode = value
                field = value
//                MusicEventHandler.executeOnPlayModeChangedListener(old, value)
                MusicEvent2.fireOnPlayModeChanged(old, value)
            }
        }

    var volume = 0F
        set (value) {
            field = value
            setVolume(value, value)
        }

    val current: Int
        get() = if (isPlayerAvailable) player.currentPosition else 0

    val duration: Int
        get() = if (isPlayerAvailable) player.duration else 0

    val isPlaying: Boolean
        get() = if (isPlayerAvailable) player.isPlaying else false

    val playerId: Int
        get() = player.audioSessionId

    /**
     * 该方法仅为启动 SimplePlayer
     */
    fun launch() {
        if (PlaylistManager.hasInitialed && PlaylistManager.localPlaylist.count != 0) {
            initPlayer()
        }
    }

    override fun onPlaylistDeleted(listName: String) {
        if (activePlaylist.name == listName) {
            activePlaylist = PlaylistManager.localPlaylist
        }
    }

    override fun onPlaylistInitialized() {
        if (PlaylistManager.localPlaylist.count == 0) {
            return
        }
        initPlayer()
    }

    override fun onMusicPause() {
        Store.state.playState.value = false
    }

    override fun onMusicPlay() {
        Store.state.playState.value = true
    }

    override fun onSongChanged(newSongId: Long) {
        with(Store.state) {
            isCurrentSongLike.value = SimplePlayer.isCurrentSongLike
            songTitle.value = currentSong.title
            songArtist.value = currentSong.artist
            songUriString.value = currentSong.path
            playState.value = isPlaying
            duration.value = this@SimplePlayer.duration
        }
    }

    override fun onPlayModeChanged(oldMode: Int, newMode: Int) {
        Store.state.currentPlayMode.value = newMode
    }

    override fun onSongAddToList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == currentSong.id) {
                Store.state.isCurrentSongLike.value = true
            }
        }
    }

    override fun onSongRemovedFromList(songId: Long, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            if (songId == currentSong.id) {
                Store.state.isCurrentSongLike.value = false
            }
        }
    }

    override fun onSongsAddToList(songIds: LongArray, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            for (songId in songIds) {
                if (songId == currentSong.id) {
                    Store.state.isCurrentSongLike.value = true
                    break
                }
            }
        }
    }

    override fun onSongsRemovedFromList(songIds: LongArray, listName: String) {
        if (listName == PlaylistManager.FAVORITE_LIST) {
            for (songId in songIds) {
                if (songId == currentSong.id) {
                    Store.state.isCurrentSongLike.value = false
                    break
                }
            }
        }
    }

    private fun initPlayer() {
        if (isPlayerAvailable) {
            return
        }
        Log.d(TAG, "initPlayer: ")
        with(player) {
            audioAttributes = AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            }
            setAudioAttributes(audioAttributes)
            setOnCompletionListener {
                when (playMode) {
                    PLAY_MODE_REPEAT -> start(isNoFade = true)
                    else -> playNext()
                }
            }
            setOnErrorListener { _, what, _ ->
                if (what == -38) {
                    playNext()
                    Util.toast("播放出现错误，已切换为下一首")
                }
                true
            }
            setOnPreparedListener {
                synchronized(prepareLock) {
                    if (prepareLock.first) {
                        Log.e(TAG, "prepared next")
                        if (activePlaylist.name != KgListActivity.LIST_NAME) {
                            AppConfigure.Player.songId = currentSong.id
                        }
                        start(isNoFade = prepareLock.second, isListener = false)
//                        MusicEventHandler.executeOnSongChangedListener(currentSong.id)
                        MusicEvent2.fireOnSongChanged(currentSong.id)
                    }
                }
            }
        }
        musicFader.onStart {
            player.start()
//            MusicEventHandler.executeOnPlayListener()
            MusicEvent2.fireOnMusicPlay()
        }
        musicFader.onEnd {
            player.pause()
//            MusicEventHandler.executeOnPauseListener()
            MusicEvent2.fireOnMusicPause()
        }
        // 获取确切的播放列表
        var playlist = PlaylistManager.getList(AppConfigure.Player.playlist)
        if (playlist == null) {
            playlist = PlaylistManager.localPlaylist
        }
        if (playlist.count == 0) {
            playlist = PlaylistManager.localPlaylist
        }
        activePlaylist = playlist
        // 获取当前确切的歌曲
        var song = activePlaylist[AppConfigure.Player.songId]
        currentIndex = if (song == null) {
            song = activePlaylist[0]
            0
        } else {
            activePlaylist.position(song)
        }
        currentSong = song!!
        initAudioFocus()
        isPlayerAvailable = true
        loadMusicOrStart(currentSong, isStart = false)
        if (AppConfigure.Player.rememberId == song.id) {
            seekTo(AppConfigure.Player.rememberProgress.toInt())
        }
        volume = 1F
        with(Store.state) {
            isCurrentSongLike.value = SimplePlayer.isCurrentSongLike
            songTitle.value = currentSong.title
            songArtist.value = currentSong.artist
            songUriString.value = currentSong.path
            playState.value = isPlaying
            duration.value = this@SimplePlayer.duration
        }

        MusicEvent2.register(this)
        MusicEvent2.fireOnPlaylistInitialized()
    }

    /**
     * 在调用前，确保 audioAttributes 已初始化
     */
    private fun initAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(audioAttributes)
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(this@SimplePlayer)
                build()
            }
        }
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (!isPlayerAvailable) {
            return
        }
        if (AppConfigure.Settings.isSecondVolumeOn) {
            val volume = AppConfigure.Settings.secondVolume / 100F
            val leftVol = volume * leftVolume
            val rightVol = volume * rightVolume
            player.setVolume(leftVol, rightVol)
        } else {
            player.setVolume(leftVolume, rightVolume)
        }
    }

    fun nextPlayMode() {
        playMode = (playMode + 1) % 3
    }

    fun start(isNoFade: Boolean = false, isListener: Boolean = true) {
        if (!isPlayerAvailable || isPlaying) {
            return
        }
        hasBeenStarted = true
        if (!requestAudioFocus()) {
            return
        }
        if (AppConfigure.Settings.playFade && !isNoFade) {
            musicFader.fadeIn(fadeDuration)
        } else {
            if (volume <= 0.001F) {
                volume = 1F
            }
            player.start()
            if (isListener) {
//                MusicEventHandler.executeOnPlayListener()
                MusicEvent2.fireOnMusicPlay()
            }
        }
    }

    fun pause(isNoFade: Boolean = false, isListener: Boolean = true) {
        if (!isPlayerAvailable || !player.isPlaying) {
            return
        }
        if (AppConfigure.Settings.playFade && !isNoFade) {
            musicFader.fadeOut(fadeDuration)
        } else {
            player.pause()
            if (isListener) {
//                MusicEventHandler.executeOnPauseListener()
                MusicEvent2.fireOnMusicPause()
            }
        }
    }

    fun startOrPause(isNoFade: Boolean = false) {
        if (!isPlayerAvailable) {
            return
        }
        if (isPlaying) {
            pause(isNoFade = isNoFade)
        } else {
            start(isNoFade = isNoFade)
        }
    }

    fun seekTo(time: Int) {
        if (isPlayerAvailable) {
            player.seekTo(time)
        }
    }

    fun playNext() {
        if (!isPlayerAvailable) {
            return
        }
        val song = getNextSong(playMode = playMode)
        song ?: return
        if (AppConfigure.Settings.playFade) {
            volume = 1F
        }
        loadMusicOrStart(song, isStart = true,isNoFade = true)
    }

    fun playPrevious() {
        if (!isPlayerAvailable) {
            return
        }
        val song = getPreviousSong(playMode = playMode)
        song ?: return
        if (AppConfigure.Settings.playFade) {
            volume = 1F
        }
        loadMusicOrStart(song, isNoFade = true)
    }

    fun loadMusicOrStart(song: Song, isStart: Boolean = true, isNoFade: Boolean = false) {
        if (!isPlayerAvailable) {
            return
        }
        currentSong = song
        currentIndex = activePlaylist.position(song)
        try {
            player.reset()
            setDataSource(song)
            synchronized(prepareLock) {
                prepareLock.first = isStart
                prepareLock.second = isNoFade
                if (isStart) {
                    player.prepareAsync()
                } else {
                    player.prepare()
                    if (activePlaylist.name != KgListActivity.LIST_NAME) {
                        AppConfigure.Player.songId = song.id
                    }
                    MusicEvent2.fireOnSongChanged(song.id)
                }
            }
        } catch (_: Exception) {}
    }

    private fun setDataSource(song: Song) {
        val uri = Uri.parse(song.path)
        if (AppConfigure.Settings.musicSource == PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_MEDIA_STORE) {
            player.setDataSource(Util.mContext, uri)
            return
        }
        val mediaDataSource: MediaDataSource? = try {
            when (song.type) {
                "kge", "kgm" -> KgmMediaDataSourceTest(uri.toFile())
                "uc", "uc!" -> UCMediaDataSource(uri.toFile())
                "ncm" -> NCMMediaDataSource(uri.toFile())
                else -> null
            }
        } catch (e: Exception) {
            null
        }
        if (mediaDataSource != null) {
            player.setDataSource(mediaDataSource)
        } else {
            player.setDataSource(Util.mContext, uri)
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        } else {
            audioManager.abandonAudioFocus(this)
        }
    }

    private fun getNextSong(playMode: Int = PLAY_MODE_NONE): Song? {
        if (!isPlayerAvailable) {
            return null
        }
        val totalCount = activePlaylist.count
        if (playMode == PLAY_MODE_RANDOM) {
            currentIndex = (Math.random() * totalCount).toInt()
            return activePlaylist[currentIndex]
        }
        currentIndex = (currentIndex + 1) % totalCount
        return activePlaylist[currentIndex]
    }

    private fun getPreviousSong(playMode: Int = PLAY_MODE_NONE): Song? {
        if (!isPlayerAvailable) {
            return null
        }
        val totalCount = activePlaylist.count
        if (playMode == PLAY_MODE_RANDOM) {
            currentIndex = (Math.random() * totalCount).toInt()
            return activePlaylist[currentIndex]
        }
        currentIndex--
        if (currentIndex < 0) {
            currentIndex = totalCount - 1
        }
        return activePlaylist[currentIndex]
    }

    private var audioFocusDelayed = false
    private var resumeOnFocusGain = false
    private var hasBeenStartedBeforeRequest = false
    private fun requestAudioFocus(): Boolean {
        if (AppConfigure.Settings.otherPlaying == PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_NONE) {
            return true
        }

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(audioFocusRequest)
        } else {
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return when (result) {
            AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                false
            }
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                true
            }
            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                audioFocusDelayed = true
                false
            }
            else -> false
        }
    }

    private var resumeIfPaused = false
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.e("", "AUDIOFOCUS_GAIN")
                if (resumeOnFocusGain || audioFocusDelayed) {
                    Log.e("", "$hasBeenStartedBeforeRequest")
                    resumeOnFocusGain = false
                    audioFocusDelayed = false
                    if (hasBeenStartedBeforeRequest) {
                        start()
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.e("", "AUDIOFOCUS_LOSS")
                resumeOnFocusGain = false
                audioFocusDelayed = false
                if (AppConfigure.Settings.otherPlaying == PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_PAUSE) {
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.e("", "AUDIOFOCUS_LOSS_TRANSIENT")
                resumeOnFocusGain = true
                audioFocusDelayed = false
                hasBeenStartedBeforeRequest = isPlaying
                if (AppConfigure.Settings.otherPlaying == PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_PAUSE) {
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                resumeOnFocusGain = true
                audioFocusDelayed = false
                Log.e("", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
            }
            else -> {
                Log.e(TAG, "$focusChange")
            }
        }
    }

    override fun close() {
        if (isPlayerAvailable) {
            player.stop()
            player.reset()
            player.release()
            abandonAudioFocus()
            isPlayerAvailable = false
        }
        MusicEvent2.unregister(this)
    }

}