package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.simple.player.R
import com.simple.player.Util
import com.simple.player.event.MusicEvent2
import com.simple.player.screen.ThemeScreen
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.DarkGreen
import com.simple.player.ui.theme.NRed
import com.simple.player.ui.theme.mainColors
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
        MusicEvent2.fireOnThemeChanged(mainColors.value)
    }
}