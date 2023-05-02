package com.simple.player.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import com.simple.player.screen.WebPlayerScreen
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.util.FileUtil
import com.simple.player.util.SimplePath
import com.simple.player.web.ResponseInterceptor
import com.simple.player.web.controller.MusicController
import com.simple.server.Server
import java.io.File

class WebPlayerActivity: BaseActivity2() {

    private val screen = WebPlayerScreen(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                screen.ComposeContent()
            }
        }

        screen.serverRunningState.value = server?.isRunning ?: false

        screen.onSwitchClick = {
            if (server == null || needInitServer) {
                initServer()
                needInitServer = false
            }
            if (!server!!.isRunning) {
                server!!.start()
                Log.e("WebPlayerActivity", "start server")
                screen.serverRunningState.value = true
            } else {
                server!!.close()
                screen.serverRunningState.value = false
                Log.e("WebPlayerActivity", "close server")
                needInitServer = true
            }
        }

        loadWebResource()
    }

    private fun initServer() {
        server = Server(port = 8888).apply {
            setWebResourcesRoot(FileUtil.mWebRoot.absolutePath)
            setDefaultCharset("UTF-8")
            registerInterceptor(ResponseInterceptor())
            addControllers(MusicController())
        }
    }

    private fun loadWebResource(): Boolean {
        val hasDir = FileUtil.mWebRoot.exists()
        val children = FileUtil.mWebRoot.list()
        if (hasDir && children != null && children.isNotEmpty()) {
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