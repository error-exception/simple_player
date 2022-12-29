package com.simple.player.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.Util
import com.simple.player.constant.PreferencesData
import com.simple.player.database.IdPathDao
import com.simple.player.screen.SettingsSingleChoice
import com.simple.player.screen.SettingsSwitch
import com.simple.player.screen.SettingsSwitchAndProgress
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.AppConfigure
import com.simple.player.util.ArtworkProvider
import com.simple.player.util.ProgressHandler

class PlayerSettingsActivity: BaseActivity2() {

    private val TAG: String = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopAppBar(
                        title = {
                            Text(text = "播放器设置")
                        },
                        navigationIcon = { ->
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        },
                    )
                    SettingsContent()
                }
            }
        }
    }

    @Composable
    fun SettingsContent() {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SettingsSwitch(
                    title = "音量键切歌",
                    description = resources.getString(R.string.volume_shuffle_summary),
                    state = AppConfigure.Settings.volumeShuffle
                ) {
                    AppConfigure.Settings.volumeShuffle = it
                }
            }
            item {
                SettingsSwitch(
                    title = "播放淡出淡入",
                    state = AppConfigure.Settings.playFade
                ) {
                    AppConfigure.Settings.playFade = it
                }
            }
            item {
                SettingsSwitch(
                    title = "插入耳机自动播放",
                    state = AppConfigure.Settings.headsetAutoPlay
                ) {
                    AppConfigure.Settings.headsetAutoPlay = it
                }
            }
            item {
                SettingsSingleChoice(
                    title = "媒体播放冲突时",
                    description = "当其他软件播放媒体时，本软件要采取的行为",
                    list = mutableStateListOf(
                        "无", "暂停", "减小音量"
                    ),
                    stateIndex = when (AppConfigure.Settings.otherPlaying) {
                        PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_NONE -> 0
                        PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_PAUSE -> 1
                        else -> 2
                    }
                ) { index, _ ->
                    AppConfigure.Settings.otherPlaying = when (index) {
                        0 -> PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_NONE
                        1 -> PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_PAUSE
                        2 -> PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_LOWER
                        else -> PreferencesData.SETTINGS_VALUE_OTHER_PLAYING_NONE
                    }
                }
            }
            item {
                SettingsSingleChoice(
                    title = "切换音乐源",
                    description = "切换后，将自动开始扫描",
                    list = mutableStateListOf(
                        "MediaStore", "外置存储"
                    ),
                    stateIndex = when (AppConfigure.Settings.musicSource) {
                        PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_MEDIA_STORE -> 0
                        PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE -> 1
                        else -> 0
                    },
                    onSelected = rescanMusic
                )
            }
            item {
                SettingsSwitch(
                    title = "显示音乐锁屏",
                    description = "锁屏界面啥都没有",
                    state = AppConfigure.Settings.showLockScreen,
                    onStateChanged = {
                        AppConfigure.Settings.showLockScreen = it
                    }
                )
            }
            item {
                SettingsSwitchAndProgress(
                    title = "二级音量",
                    switchState = AppConfigure.Settings.isSecondVolumeOn,
                    progressState = AppConfigure.Settings.secondVolume.toFloat() / 100F,
                    onStateChanged = {
                        AppConfigure.Settings.isSecondVolumeOn = it
                        SimplePlayer.volume = 1F
                    },
                    onProgress = {
                        AppConfigure.Settings.secondVolume = (100F * it).toInt()
                        SimplePlayer.volume = 1F
                    }
                )
            }
        }
    }

    private val rescanMusic: (index: Int, item: String) -> Unit = { index, item ->
        val oldSource = AppConfigure.Settings.musicSource
        val newSource = when (index) {
            0 -> PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_MEDIA_STORE
            1 -> PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_EXTERNAL_STORAGE
            else -> PreferencesData.SETTINGS_VALUE_MUSIC_SOURCE_MEDIA_STORE
        }
        if (oldSource != newSource) {
            AppConfigure.Settings.musicSource = newSource
            ProgressHandler.handle(
                before = {
                    Util.showProgressDialog(this, 12, "准备中......")
                },
                handle = {
                    IdPathDao.clear()
                    ArtworkProvider.clearArtworkCache(this@PlayerSettingsActivity)
                },
                after = {
                    Util.closeProgressDialog(12)
                    startActivity(Intent(this@PlayerSettingsActivity, ScanMusicActivity::class.java).apply {
                        putExtra(ScanMusicActivity.EXTRA_SCAN_IMMEDIATELY, true)
                        putExtra(ScanMusicActivity.EXTRA_AUTO_ADD, true)
                    })
                }
            )
        }
    }

}