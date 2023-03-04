package com.simple.player.constant

object PreferencesData {



    /* 播放器的配置文件 */
    const val CONFIG_PLAYER = "player"
    /* 显示在设置界面的配置 */
    const val CONFIG_SETTINGS = "com.simple.player_preferences"
    /* 网页端设置 */
    const val CONFIG_WEB = "web"
    /* 播放歌曲的ID */
    const val PLAYER_SONG_ID = "song_id"
    /* 播放器的播放模式 */
    const val PLAYER_PLAY_MODE = "play_mode"
    /* 当前播放的播放列表 */
    const val PLAYER_PLAYLIST = "playlist"
    /* 记住上次播放进度，默认值为 0 */
    const val PLAYER_REMEMBER_PROGRESS = "remember_progress"
    /* 记住上次播放歌曲ID，默认值为 -1， 表示无记录 */
    const val PLAYER_REMEMBER_SONG_ID = "remember_song_id"
    /* 含有音乐文件的路径 */
    const val PLAYER_MUSIC_DIRECTORIES = "music_dirs"
    /* 最大歌曲大小，决定使用多大的 mask */
    const val PLAYER_MAX_SONG_SIZE = "max_song_size"
    /* 当其他媒体播放器播放时 */
    const val SETTINGS_OTHER_PLAYING = "other_playing"
    /* 当其他媒体播放器播放时，暂停播放 */
    const val SETTINGS_VALUE_OTHER_PLAYING_PAUSE = "pause"
    /* 当其他媒体播放器播放时，减小音量 */
    const val SETTINGS_VALUE_OTHER_PLAYING_LOWER = "vol_down"
    /* 当其他媒体播放器播放时，do nothing */
    const val SETTINGS_VALUE_OTHER_PLAYING_NONE = "none"
    /* 当其他媒体播放器播放时，减小的具体音量值 */
    const val SETTINGS_OTHER_PLAYING_VOLUME = "other_playing_volume"
    /* 是否开启音量键切歌 */
    const val SETTINGS_VOLUME_SHUFFLE = "volume_shuffle"
    /* 是否开启插入耳机后自动播放 */
    const val SETTINGS_HEADSET_AUTOPLAY = "headset_autoplay"
    /* 是否开启播放淡出淡入 */
    const val SETTINGS_PLAY_FADE = "play_fade"
    /* 是否开启二级音量 */
    const val SETTINGS_ENABLE_SECOND_VOLUME = "enable_second_volume"
    /* 二级音量的值 */
    const val SETTINGS_SECOND_VOLUME = "second_volume"
    /* 音乐播放源 */
    const val SETTINGS_MUSIC_SOURCE = "music_source"
    /* 音乐播放源，安卓系统的 MediaStore */
    const val SETTINGS_VALUE_MUSIC_SOURCE_MEDIA_STORE = "MediaStore"
    /* 音乐播放源，读取手机存储，需要相应的权限 */
    const val SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE = "ExternalStorage"
    /* 要扫描的音乐文件后缀名 */
    const val SETTINGS_ACCESS_EXTENSION = "access_extension"
    /* 扫描时排除的路径 */
    const val SETTINGS_EXCLUDE_PATH = "exclude_path"
    /* 是否显示锁屏界面 */
    const val SETTINGS_SHOW_LOCKSCREEN = "show_lockscreen"
    /* 是否主界面图片 */
    const val SETTINGS_SHOW_HEAD_IMAGE = "show_head_img"
    /* 底部播放栏默认样式 */
    const val SETTINGS_BUTTON_PLAYER_BAR = "botm_play_bar"
    /* 底部播放栏默认样式，简略 */
    const val SETTINGS_VALUE_BUTTON_PLAYER_BAR_SIMPLE = "simple"
    /* 底部播放栏默认样式，详细 */
    const val SETTINGS_VALUE_BUTTON_PLAYER_BAR_DETAIL = "detail"
    /* 使用新的播放列表界面 */
    const val SETTINGS_ENABLE_NEW_PLAYLIST = "enable_new_list"
    /* 额外扫描的路径 */
    const val SETTINGS_INCLUDE_PATH = "include_path"
    /* 主题颜色 */
    const val SETTINGS_THEME_COLOR = "theme_color"
    /* 网页背景类型 */
    const val WEB_BACKGROUND_TYPE = "background_type"
    /* 显示网页LOGO */
    const val WEB_SHOW_LOGO = "show_logo"
    /* 显示网页可视化 */
    const val WEB_SHOW_VISUALIZATION = "show_visualization"
    /* 可视化高度 */
    const val WEB_VISUALIZATION_HEIGHT = "visualization_height"
    /* 可视化平滑度 */
    const val WEB_VISUALIZATION_SMOOTH = "visualization_smooth"
    /* 可视化颜色 */
    const val WEB_VISUALIZATION_COLOR = "visualization_color"
    /* 显示网页背景中运动的白条 */
    const val WEB_WHITE_BAR = "white_bar"
    /* 背景根据鼠标移动 */
    const val WEB_BACKGROUND_MOTIVATION = "background_motivation"
    /* 网页 LOGO 跳动 */
    const val WEB_LOGO_BEAT = "logo_beat"
}