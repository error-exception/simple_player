package com.simple.player.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.ext.startActivity
import com.simple.player.screen.SettingsSwitch
import com.simple.player.screen.SettingsText
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.NRed
import com.simple.player.util.AppConfigure

class ExperimentalActivity: BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTestTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(text = "实验性功能") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        }
                    )
                    Text(
                        text = "这些功能仅为前瞻体验，在正式加入前，本软件不会完整使用这些功能！",
                        color = NRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 32.dp)
                    )
                    EntranceList()
                }
            }
        }
    }

    @Composable
    fun EntranceList() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsText(title = "酷狗3D丽音测试") {
                startActivity(KGPrettySoundActivity::class.java)
            }
            SettingsText(title = "KRC解密") {
                startActivity(KrcDecodeActivity::class.java)
            }
            SettingsText(title = "歌词测试") {
                startActivity(LyricsActivity::class.java)
            }
            SettingsSwitch(
                title = "新播放列表测试",
                state = AppConfigure.Settings.enableNewPlaylist,
                description = "只是用来测试滑动的流畅性"
            ) {
                AppConfigure.Settings.enableNewPlaylist = it
            }
        }

    }

}