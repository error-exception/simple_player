package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Colors
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.ui.theme.mainColors
import com.simple.player.util.AppConfigure
import com.simple.player.util.ColorUtils

open class BaseActivity2: ComponentActivity(), MusicEventListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicEvent2.register(this)
        window.statusBarColor = primaryColor
    }

    override fun onThemeChanged(newColors: Colors) {
        primaryColor = ColorUtils.toAndroidColorInt(newColors.primary)
        window.statusBarColor = primaryColor
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicEvent2.unregister(this)
    }

    companion object {
        @JvmStatic
        var primaryColor: Int = AppConfigure.Settings.themeColor

        init {
            val themeColor = AppConfigure.Settings.themeColor
            val composeColor = ColorUtils.toComposeColor(themeColor)
            mainColors.value = mainColors.value.copy(
                primary = composeColor,
                secondary = composeColor
            )
        }
    }

}