package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simple.player.BuildConfig
import com.simple.player.R
import com.simple.player.ext.startActivity
import com.simple.player.screen.SettingsText
import com.simple.player.ui.theme.ComposeTestTheme

class NewSettingsActivity: BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Test()
            }
        }
    }

    @Preview
    @Composable
    fun Test() {
        Surface(color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text("设置")
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "")
                        }
                    }
                )
                SettingsContent()
            }
        }
    }

    @Composable
    fun SettingsContent() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsText(title = "播放器") {
                startActivity(PlayerSettingsActivity::class.java)
            }
            SettingsText(title = "扫描设置") {
                startActivity(ScanMusicSettingsActivity::class.java)
            }
            SettingsText(title = "界面设置") {
                startActivity(UISettingsActivity::class.java)
            }
            SettingsText(title = "网页播放器") {
                startActivity(WebSettingsActivity::class.java)
            }
            SettingsText(title = "更新日志") {
                startActivity(ChangeLogActivity::class.java)
            }
            SettingsText(
                title = "关于",
                description = BuildConfig.VERSION_NAME,
                more = false
            )
        }
    }

}