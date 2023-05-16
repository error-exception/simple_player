package com.simple.player.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.R
import com.simple.player.Util
import com.simple.player.Util.copyText
import com.simple.player.ext.startActivity
import com.simple.player.ext.toast
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.RoundIconButton2
import com.simple.player.ui.theme.windowBackgroundAlpha
import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import com.simple.player.util.SimplePath
import com.simple.player.web.ResponseInterceptor
import com.simple.player.web.controller.ImageController
import com.simple.player.web.controller.MusicController
import com.simple.server.Server
import java.io.File

class WebPlayerActivity: BaseActivity2() {

    private val serverRunningState = mutableStateOf(false)
    private var ipList = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopAppBar(
                        title = {
                            Text(text = "网页播放器")
                        },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { startActivity(WebSettingsActivity::class.java) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_settings_24),
                                    contentDescription = "web settings",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                    Content()
                }
            }
        }

        serverRunningState.value = server?.isRunning ?: false
        if (serverRunningState.value) {
            ipList.clear()
            val ips = Util.getInetAddress()
            for (ip in ips) {
                ipList.add(ip)
            }
        }
        loadWebResource()
    }

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val runningState = remember {
                serverRunningState
            }
            Surface(
                color = windowBackgroundAlpha,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        modifier = Modifier.weight(1F),
                        text = if (runningState.value) "正在运行" else "已关闭",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    RoundIconButton2(
                        painter = painterResource(id = R.drawable.ic_baseline_web_24),
                        contentDescription = "",
                        tint = Color.White,
                        contentPadding = 16.dp,
                        color = if (runningState.value) MaterialTheme.colors.primary else Color.Gray,
                        iconSize = 48.dp,
                        onClick = {
                            if (server == null || needInitServer) {
                                initServer()
                                needInitServer = false
                            }
                            // 初始化服务器可能出现问题
                            server ?: return@RoundIconButton2
                            if (!server!!.isRunning) {
                                server!!.start()
                                Log.e("WebPlayerActivity", "start server")
                                serverRunningState.value = true
                            } else {
                                server!!.close()
                                serverRunningState.value = false
                                Log.e("WebPlayerActivity", "close server")
                                needInitServer = true
                            }
                        }
                    )
                }
            }
            if (serverRunningState.value) {
                IPList()
            }
        }
    }

    @Composable
    fun IPList() {
        Surface(
            color = windowBackgroundAlpha,
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "访问以下地址之一",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                item {
                    Text(
                        text = "请选择 192.168 开头的地址",
                        color = Color.Gray
                    )
                }
                item {
                    Column {
                        for (ip in ipList) {
                            Text(
                                modifier = Modifier
                                    .clickable {
                                        copyText("http://$ip:${AppConfigure.Web.port}/index.html")
                                        toast("已复制到剪贴板")
                                    },
                                text = "http://$ip:${AppConfigure.Web.port}/index.html"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initServer() {

        val port = AppConfigure.Web.port
        try {
            server = Server(port = port)
            server?.apply {
                val customRes = AppConfigure.Web.resourceRoot
                if (customRes.isNotEmpty()) {
                    setWebResourcesRoot(customRes)
                } else {
                    setWebResourcesRoot(FileUtil.mWebRoot.absolutePath)
                }
                setDefaultCharset("UTF-8")
                registerInterceptor(ResponseInterceptor())
                addControllers(
                    MusicController(),
                    ImageController()
                )
            }
            ipList.clear()
            ipList.addAll(Util.getInetAddress())
        } catch (e: Exception) {
            toast("启动服务器失败，请检查端口号或稍后再试")
            server?.close()
            server = null
            needInitServer = true
        }
    }

    private fun loadWebResource(): Boolean {
        val hasDir = FileUtil.mWebRoot.exists()
        val index = File(FileUtil.mWebRoot.absolutePath + "/index.html")
        val children = FileUtil.mWebRoot.list()
        if (hasDir && children != null && children.isNotEmpty() && index.exists()) {
            return true
        }
        val list = assets.list("web")
        Log.e("WebPlayerActivity", "loadWebResource: ${list.contentToString()}")
        val path = SimplePath("web")
        list ?: return false
        path.append("")
        list.forEach {
            path.replaceLast(it)
            copy(path)
        }
        return true
    }

    fun copy(path: SimplePath) {
        val pathString = path.pathString()
        if (path.hasExtension()) {
            val outputPath = File(getExternalFilesDir("")!!.absolutePath + "/" + pathString)
            val parent = outputPath.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }
            val outputStream = FileUtil.openOutputStream(outputPath)
            outputStream ?: return
            val inputStream = assets.open(pathString)
            FileUtil.copy(inputStream, outputStream, autoClose = true)
        } else {
            val list = assets.list(pathString)
            Log.e("WebPlayerActivity", "loadWebResource: ${list.contentToString()}")

            list ?: return
            path.append("")
            list.forEach {
                path.replaceLast(it)
                copy(path)
            }
            path.toParent()
        }
    }

    companion object {
        var server: Server? = null
        var needInitServer = false
    }

}