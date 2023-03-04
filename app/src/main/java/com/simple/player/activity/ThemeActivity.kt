package com.simple.player.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.Color
import com.simple.player.event.MusicEvent2
import com.simple.player.screen.ThemeScreen
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.mainColors
import com.simple.player.util.AppConfigure
import com.simple.player.util.ColorUtil

class ThemeActivity : BaseActivity2() {

    private val screen = ThemeScreen(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }
        screen.onDefaultColorSelected = {
            changeTheme(it)
        }
        screen.onApplyCustomColor = {
            changeTheme(it)
        }
    }

    private fun changeTheme(color: Color) {
        mainColors.value = mainColors.value.copy(
            primary = color,
            secondary = color
        )
        AppConfigure.Settings.themeColor = ColorUtil.toAndroidColorInt(color)
        MusicEvent2.fireOnThemeChanged(mainColors.value)
    }
}