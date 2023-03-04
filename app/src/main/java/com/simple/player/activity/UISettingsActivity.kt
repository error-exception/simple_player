package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.constant.PreferencesData
import com.simple.player.event.MusicEvent2
import com.simple.player.ext.startActivity
import com.simple.player.screen.SettingsSingleChoice
import com.simple.player.screen.SettingsSwitch
import com.simple.player.screen.SettingsText
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.AppConfigure

class UISettingsActivity: BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTestTheme {
                Column {
                    TopAppBar(
                        title = { Text(text = "界面设置") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        }
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
                    title = "显示主界面图片",
                    description = "可能会减少社死？启用后点击图片区域来更改图片",
                    state = AppConfigure.Settings.showHeadImage,
                    onStateChanged = {
                        AppConfigure.Settings.showHeadImage = it
                        MusicEvent2.fireOnHeadImageVisibilityChanged(it)
                    }
                )
            }
            item {
                val list = remember {
                    mutableStateListOf(
                        "简略", "详细"
                    )
                }
                SettingsSingleChoice(
                    title = "底部默认播放栏样式",
                    description = "切换底部默认播放栏样式。还可通过长按底部播放栏样式来切换，但不改变默认样式",
                    list = list,
                    stateIndex = when (AppConfigure.Settings.bottomPlayerBar) {
                        PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_SIMPLE -> 0
                        PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_DETAIL -> 1
                        else -> 0
                    },
                    onSelected = { index, element ->
                        val style = when (index) {
                            0 -> PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_SIMPLE
                            1 -> PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_DETAIL
                            else -> PreferencesData.SETTINGS_VALUE_BUTTON_PLAYER_BAR_SIMPLE
                        }
                        AppConfigure.Settings.bottomPlayerBar = style
                        MusicEvent2.fireOnBottomPlayerBarStyleChanged(style)
                    }
                )
            }
            item {
                SettingsText(title = "主题") {
                    startActivity(ThemeActivity::class.java)
                }
            }
        }

    }
}