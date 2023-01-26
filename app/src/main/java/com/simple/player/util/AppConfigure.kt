package com.simple.player.util

import android.content.Context
import android.content.SharedPreferences
import com.simple.player.constant.PreferencesData
import com.simple.player.playlist.PlaylistManager

object AppConfigure {

    private lateinit var player: SharedPreferences
    private lateinit var settings: SharedPreferences
    private lateinit var web: SharedPreferences

    fun setContext(context: Context) {
        player = context.getSharedPreferences(PreferencesData.CONFIG_PLAYER, Context.MODE_PRIVATE)
        settings = context.getSharedPreferences(PreferencesData.CONFIG_SETTINGS, Context.MODE_PRIVATE)
        web = context.getSharedPreferences(PreferencesData.CONFIG_WEB, Context.MODE_PRIVATE)
    }

    object Settings {

        private var comfortableVolumes: IntArray? = null
        private val defaultExcludePath = mutableSetOf(
            "${FileUtil.defaultPath}/Android"
        )
        private val defaultAccessExtension = mutableSetOf(
            "mp3", "flac"
        )

        var isSecondVolumeOn: Boolean
            get() = settings.getBoolean(PreferencesData.SETTINGS_ENABLE_SECOND_VOLUME, false)
            set(value) = settings.edit().putBoolean(PreferencesData.SETTINGS_ENABLE_SECOND_VOLUME, value).apply()

        var secondVolume: Int
            get() = settings.getInt(PreferencesData.SETTINGS_SECOND_VOLUME, 100)
            set(value) = settings.edit().putInt(PreferencesData.SETTINGS_SECOND_VOLUME, value).apply()

        var playFade: Boolean
            get() = settings.getBoolean(PreferencesData.SETTINGS_PLAY_FADE, false)
            set(value) = settings.edit().putBoolean(PreferencesData.SETTINGS_PLAY_FADE, value).apply()

        var headsetAutoPlay: Boolean
            get() = settings.getBoolean(PreferencesData.SETTINGS_HEADSET_AUTOPLAY, false)
            set(value) = settings.edit().putBoolean(PreferencesData.SETTINGS_HEADSET_AUTOPLAY, value).apply()

        var volumeShuffle: Boolean
            get() = settings.getBoolean(PreferencesData.SETTINGS_VOLUME_SHUFFLE, false)
            set(value) = settings.edit().putBoolean(PreferencesData.SETTINGS_VOLUME_SHUFFLE, value).apply()

        var otherPlaying: String
            get() = settings.getString(PreferencesData.SETTINGS_OTHER_PLAYING, PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_PAUSE)!!
            set(value) = settings.edit().putString(PreferencesData.SETTINGS_OTHER_PLAYING, value).apply()

        var accessExtension: MutableSet<String>
            get() = settings.getStringSet(PreferencesData.SETTINGS_ACCESS_EXTENSION, defaultAccessExtension)!!
            set(value) = settings.edit().putStringSet(PreferencesData.SETTINGS_ACCESS_EXTENSION, value).apply()

        var musicSource: String
            get() = settings.getString(PreferencesData.SETTINGS_MUSIC_SOURCE, "MediaStore")!!
            set(value) = settings.edit().putString(PreferencesData.SETTINGS_MUSIC_SOURCE, value).apply()

        var excludePath: MutableSet<String>
            get() = settings.getStringSet(PreferencesData.SETTINGS_EXCLUDE_PATH, defaultExcludePath)!!
            set(value) = settings.edit().putStringSet(PreferencesData.SETTINGS_EXCLUDE_PATH, value).apply()

        var includePath: MutableSet<String>
            get() = settings.getStringSet(PreferencesData.SETTINGS_INCLUDE_PATH, emptySet<String>())!!
            set(value) = settings.edit().putStringSet(PreferencesData.SETTINGS_INCLUDE_PATH, value).apply()

        var comfortableVolume: IntArray
            get() {
                return intArrayOf(1)
            }
            set(value) = TODO()

        var showLockScreen: Boolean
            get() = settings.getBoolean(PreferencesData.SETTINGS_SHOW_LOCKSCREEN, false)
            set(value) = settings.edit().putBoolean(PreferencesData.SETTINGS_SHOW_LOCKSCREEN, value).apply()

        var showHeadImage: Boolean
            get() = settings.getBoolean(PreferencesData.SETTINGS_SHOW_HEAD_IMAGE, false)
            set(value) = settings.edit().putBoolean(PreferencesData.SETTINGS_SHOW_HEAD_IMAGE, value).apply()

        var bottomPlayerBar: String
            get() = settings.getString(PreferencesData.SETTINGS_BUTTON_PLAYER_BAR, PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_SIMPLE)!!
            set(value) = settings.edit().putString(PreferencesData.SETTINGS_BUTTON_PLAYER_BAR, value).apply()

        var enableNewPlaylist: Boolean
            get() = settings.getBoolean(PreferencesData.SETTINGS_ENABLE_NEW_PLAYLIST, false)
            set(value) = settings.edit().putBoolean(PreferencesData.SETTINGS_ENABLE_NEW_PLAYLIST, value).apply()

    }

    object Player {

        var songId: Long
            get() = player.getLong(PreferencesData.PLAYER_SONG_ID, -1L)
            set(value) = player.edit().putLong(PreferencesData.PLAYER_SONG_ID, value).apply()

        var playMode: Int
            get() = player.getInt(PreferencesData.PLAYER_PLAY_MODE, 2)
            set(value) = player.edit().putInt(PreferencesData.PLAYER_PLAY_MODE, value).apply()

        var playlist: String
            get() = player.getString(PreferencesData.PLAYER_PLAYLIST, PlaylistManager.LOCAL_LIST)!!
            set(value) = player.edit().putString(PreferencesData.PLAYER_PLAYLIST, value).apply()

        var rememberProgress: Long
            get() = player.getLong(PreferencesData.PLAYER_REMEMBER_PROGRESS, 0L)
            set(value) = player.edit().putLong(PreferencesData.PLAYER_REMEMBER_PROGRESS, value).apply()

        var rememberId: Long
            get() = player.getLong(PreferencesData.PLAYER_REMEMBER_SONG_ID, -1L)
            set(value) = player.edit().putLong(PreferencesData.PLAYER_REMEMBER_SONG_ID, value).apply()

        var musicDirectories: MutableSet<String>
            get() = player.getStringSet(PreferencesData.PLAYER_MUSIC_DIRECTORIES, emptySet())!!
            set(value) = player.edit().putStringSet(PreferencesData.PLAYER_MUSIC_DIRECTORIES, value).apply()

        var maxSongSize: Long
            get() = player.getLong(PreferencesData.PLAYER_MAX_SONG_SIZE, 1024 * 1024 * 100)!!
            set(value) = player.edit().putLong(PreferencesData.PLAYER_MAX_SONG_SIZE, value).apply()
    }
}