package com.simple.player.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.simple.player.R
import com.simple.player.ext.toast
import com.simple.player.screen.SettingsTextField
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.AppConfigure
import java.io.File

class WebSettingsActivity: BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopAppBar(
                        title = { Text(text = "网页播放器设置") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                                    contentDescription = "back"
                                )
                            }
                        }
                    )
                    Content()
                }
            }
        }
    }

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsTextField(
                title = "服务器端口",
                stateText = AppConfigure.Web.port.toString(),
                description = "如果端口被占用，可以重新设置端口号已解决此问题，建议大于默认的端口号，小于 65535",
                onApplyText = {
                    val newPort = it.toIntOrNull()
                    if (newPort == null) {
                        toast("必须是纯数字")
                        return@SettingsTextField
                    }
                    if (newPort <= 0 || newPort >= 65535) {
                        toast("端口号错误")
                        return@SettingsTextField
                    }
                    AppConfigure.Web.port = newPort
                }
            )
            SettingsTextField(
                title = "网页资源路径",
                stateText = AppConfigure.Web.resourceRoot,
                description = "浏览器访问时所需加载的网页以及其他资源，为空则使用软件内部资源",
                onApplyText = {
                    val resDir = File(it)
                    if (it.isEmpty()) {
                        AppConfigure.Web.resourceRoot = it
                        return@SettingsTextField
                    }
                    if (!resDir.exists()) {
                        toast("路径不存在")
                        return@SettingsTextField
                    }
                    if (resDir.isFile) {
                        toast("应当是一个目录")
                        return@SettingsTextField
                    }
                    AppConfigure.Web.resourceRoot = it
                }
            )
            SettingsTextField(
                title = "背景资源路径",
                stateText = AppConfigure.Web.backgroundPath,
                description = "当路径为空时，使用网页资源中的背景",
                onApplyText = {
                    val backgroundDir = File(it)
                    if (it.isEmpty()) {
                        AppConfigure.Web.backgroundPath = it
                        return@SettingsTextField
                    }
                    if (!backgroundDir.exists()) {
                        toast("路径不存在")
                        return@SettingsTextField
                    }
                    if (backgroundDir.isFile) {
                        toast("应当是一个目录")
                        return@SettingsTextField
                    }
                    AppConfigure.Web.backgroundPath = it
                }
            )
        }
    }

}