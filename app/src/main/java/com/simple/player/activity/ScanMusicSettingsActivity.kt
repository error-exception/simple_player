package com.simple.player.activity

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.ext.startActivity
import com.simple.player.screen.SettingsText
import com.simple.player.ui.theme.ComposeTestTheme

class ScanMusicSettingsActivity: BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeTestTheme {
                Column {
                    TopAppBar(
                        title = { Text(text = "扫描设置") },
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
                SettingsText(
                    title = "音乐拓展名",
                    description = "只扫描指定的拓展名"
                ) {
                    startActivity(ExtensionItemActivity::class.java)
                }
            }
            item {
                SettingsText(
                    title = "排除路径",
                    description = "扫描时会忽略该路径下的所有文件，可以在一定程度上提高扫描速度"
                ) {
                    startActivity(ExcludePathActivity::class.java)
                }
            }
            item {
                SettingsText(
                    title = "包含路径",
                    description = "扫描时会扫面该路径下的所有文件，建议用于在被排除的路径中，需要扫描的路径"
                ) {
                    startActivity(IncludePathActivity::class.java)
                }
            }
        }
    }

}