package com.simple.player.web

import android.util.Log
import com.simple.player.util.JSONUtil.listToJSON
import com.simple.player.util.FileUtil
import com.simple.player.playlist.PlaylistManager
import java.io.*
import java.net.Socket

class ServerThread(private val socket: Socket) : Thread() {

    private val ROOT = FileUtil.mWebRoot.absolutePath
    private var out: OutputStream = socket.getOutputStream()

    override fun run() {
        val request = RequestParser.parseRequest(socket)
        response(request)
        try {
            if (!socket.isClosed)
                socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        request.clear()
        System.gc()
    }

    private fun response(request: Request) {

        val url = request.requestUrl
        if (url == "/" || url.lastIndexOf('.') != -1) {
            handleFile(request)
        } else {
            doRequest(request)
        }

    }

    private fun doRequest(request: Request) {
        request.parameter
        when {
            request.isAjax() -> {
                handleAjaxRequest(request)
            }
            request.requestUrl.startsWith("/song") -> {
                val id = request.parameter["id"]!!.toLong()
                val song = PlaylistManager.localPlaylist[id]
                FileSender.sendFile(socket, song!!.path)
            }
            request.requestUrl == "/bgSrc" -> {
                val file = File("$ROOT/res")
                val list = file.listFiles { f ->
                    f.name.startsWith("background")
                }

                if (list.isNotEmpty()) {
                    FileSender.sendFile(socket, list[0].absolutePath)
                }
            }
            request.requestUrl == "/bgThumb" -> {
                val file = File("$ROOT/res")
                val list = file.listFiles { f ->
                    f.name.startsWith("bgThumb")
                }
                if (list != null) {
                    if (list.isNotEmpty()) {
                        FileSender.sendFile(socket, list[0].absolutePath)
                    }
                }
            }
        }
    }

    private fun handleFile(request: Request) {
        val url = request.requestUrl

        if (url == "/") {
            val file = File("$ROOT/index.html")
            if (!file.exists()) {
                FileSender.send404(socket)
                return
            }
            FileSender.sendFile(socket, "$ROOT/index.html")
        } else {
            val file = File("$ROOT/$url")
            if (!file.exists()) {
                FileSender.send404(socket)
                return
            }
            FileSender.sendFile(socket, "$ROOT/$url")
        }
    }

    private fun handleAjaxRequest(request: Request?) {
        val url = request?.requestUrl ?: return
        if (url == "/list") {
            val str = listToJSON(PlaylistManager.localPlaylist.songList)
            Log.e("", str)
            FileSender.sendText(socket, str, ContentType.APPLICATION_JSON_UTF8)
        }

    }

}