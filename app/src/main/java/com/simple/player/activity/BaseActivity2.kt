package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.Colors
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.ui.theme.mainColors
import com.simple.player.util.ColorUtil

open class BaseActivity2: ComponentActivity(), MusicEventListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicEvent2.register(this)
        window.statusBarColor = primaryColor
    }

    override fun onThemeChanged(newColors: Colors) {
        primaryColor = ColorUtil.toAndroidColor(newColors.primary).toArgb()
        window.statusBarColor = primaryColor
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicEvent2.unregister(this)
    }

    companion object {
        @JvmStatic
        var primaryColor: Int = ColorUtil.toAndroidColor(mainColors.value.primary).toArgb()
    }

}