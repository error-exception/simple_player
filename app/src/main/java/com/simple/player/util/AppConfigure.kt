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

        var accessExtension: String
            get() = settings.getString(PreferencesData.SETTINGS_ACCESS_EXTENSION, ".mp3,.flac,.ogg")!!
            set(value) = settings.edit().putString(PreferencesData.SETTINGS_ACCESS_EXTENSION, value).apply()

        var musicSource: String
            get() = settings.getString(PreferencesData.SETTINGS_MUSIC_SOURCE, "MediaStore")!!
            set(value) = settings.edit().putString(PreferencesData.SETTINGS_MUSIC_SOURCE, value).apply()

        var excludePath: String
            get() = settings.getString(PreferencesData.SETTINGS_EXCLUDE_PATH, "")!!
            set(value) = settings.edit().putString(PreferencesData.SETTINGS_EXCLUDE_PATH, value).apply()
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

    }

    object Web {
        var backgroundMove: Boolean
            get() = web.getBoolean(PreferencesData.WEB_BACKGROUND_MOTIVATION, true)
            set(value) = web.edit().putBoolean(PreferencesData.WEB_BACKGROUND_MOTIVATION, value).apply()

        var backgroundType: Int
            get() = web.getInt(PreferencesData.WEB_BACKGROUND_TYPE, 0)
            set(value) = web.edit().putInt(PreferencesData.WEB_BACKGROUND_TYPE, value).apply()

        var visualizeSmooth: Int
            get() = web.getInt(PreferencesData.WEB_VISUALIZATION_SMOOTH, 80)
            set(value) = web.edit().putInt(PreferencesData.WEB_VISUALIZATION_SMOOTH, value).apply()

        var visualizeHeight: Int
            get() = web.getInt(PreferencesData.WEB_VISUALIZATION_HEIGHT, 40)
            set(value) = web.edit().putInt(PreferencesData.WEB_VISUALIZATION_HEIGHT, value).apply()

        var visualizeColor: String?
            get() = web.getString(PreferencesData.WEB_VISUALIZATION_COLOR, "#0000ff")
            set(value) = web.edit().putString(PreferencesData.WEB_VISUALIZATION_COLOR, value).apply()

        var showVisualizer: Boolean
            get() = web.getBoolean(PreferencesData.WEB_SHOW_VISUALIZATION, true)
            set(value) = web.edit().putBoolean(PreferencesData.WEB_SHOW_VISUALIZATION, value).apply()

        var showLogo: Boolean
            get() = web.getBoolean(PreferencesData.WEB_SHOW_LOGO, true)
            set(value) = web.edit().putBoolean(PreferencesData.WEB_SHOW_LOGO, value).apply()

        var logoBeat: Boolean
            get() = web.getBoolean(PreferencesData.WEB_LOGO_BEAT, true)
            set(value) = web.edit().putBoolean(PreferencesData.WEB_LOGO_BEAT, value).apply()

        var showWhiteBar: Boolean
            get() = web.getBoolean(PreferencesData.WEB_WHITE_BAR, true)
            set(value) = web.edit().putBoolean(PreferencesData.WEB_WHITE_BAR, value).apply()
    }

}