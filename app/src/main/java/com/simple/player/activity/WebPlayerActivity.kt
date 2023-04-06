package com.simple.player.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import com.simple.player.ui.theme.ComposeTestTheme

class WebPlayerActivity: BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Text(text = "正在开发当中")
            }
        }
    }

}