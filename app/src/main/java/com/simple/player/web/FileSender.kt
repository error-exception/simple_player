package com.simple.player.web

import android.net.Uri
import com.simple.player.util.FileUtil
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import java.lang.Exception
import java.net.Socket

object FileSender {

    private val contentTypeMap = mapOf(
        Pair("jpeg", ContentType.IMAGE_JPG),
        Pair("jpg", ContentType.IMAGE_JPG),
        Pair("png", ContentType.IMAGE_PNG),
        Pair("mp3", ContentType.AUDIO_MPEG),
        Pair("html", ContentType.TEXT_HTML_UTF8),
        Pair("json", ContentType.APPLICATION_JSON_UTF8),
        Pair("css", ContentType.TEXT_CSS_UTF8),
        Pair("js", ContentType.TEXT_JAVASCRIPT_UTF8),
    )

//    fun sendImage(socket: Socket, imagePath: String?) {
//        try {
//            val inputStream = FileUtil.openInputStream(imagePath)
//            val type = FileUtil.getFileType(imagePath!!).lowercase()
//            val mimeType = contentTypeMap[type]
//            sendFile(socket, inputStream, mimeType, null, true)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendImage(socket: Socket, uri: Uri?) {
//        try {
//            val inputStream = getInputStream(uri)
//            val mimeType = getMimeType(uri!!)
//            sendFile(socket, inputStream, mimeType, null, true)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendMP3(socket: Socket, mp3Path: String?) {
//        try {
//            val inputStream = getInputStream(mp3Path)
//            sendFile(socket, inputStream, ContentType.Audio.MPEG, null, true)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendMP3(socket: Socket, uri: Uri?) {
//        try {
//            val inputStream = getInputStream(uri)
//            sendFile(socket, inputStream, ContentType.Audio.MPEG, null, true)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendText(socket: Socket, path: String?) {
//        try {
//            val type = getFileType(path!!).toLowerCase()
//            val charset = "utf-8"
//            val inputStream = getInputStream(path)
//            var mimeType: String? = null
//            if (type == "html") {
//                mimeType = ContentType.Text.HTML
//            } else if (type == "js") {
//                mimeType = ContentType.Text.JAVASCRIPT
//            } else if (type == "css") {
//                mimeType = ContentType.Text.CSS
//            }
//            sendFile(socket, inputStream, mimeType, charset, false)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendText(socket: Socket, uri: Uri?) {
//        try {
//            val inputStream = getInputStream(uri)
//            val mimeType = getMimeType(uri!!)
//            sendFile(socket, inputStream, mimeType, "utf-8", false)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendBinary(socket: Socket, path: String?) {
//        try {
//            val inputStream = getInputStream(path)
//            sendFile(socket, inputStream, ContentType.Application.BIN, null, true)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendBinary(socket: Socket, uri: Uri?) {
//        try {
//            val inputStream = getInputStream(uri)
//            sendFile(socket, getInputStream(uri), ContentType.Application.BIN, null, true)
//            inputStream!!.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendMessage(socket: Socket, msg: String) {
//        try {
//            val ps = PrintStream(socket.getOutputStream())
//            ps.println("HTTP/1.1 200 OK")
//            ps.println("Content-Type: text/html; charset=gbk")
//            ps.println("Content-Length: " + msg.toByteArray().size)
//            ps.println()
//            ps.println(msg)
//            ps.flush()
//            ps.close()
//            socket.shutdownOutput()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendJSONText(socket: Socket, jsonText: String?) {
//        try {
//            val ps = PrintStream(socket.getOutputStream())
//            ps.println("HTTP/1.1 200 OK")
//            ps.println("Content-Type: application/json; charset=utf-8")
//            ps.println("Content-Length: " + jsonText!!.toByteArray().size)
//            ps.println()
//            ps.println(jsonText)
//            ps.flush()
//            ps.close()
//            socket.shutdownOutput()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendJSON(socket: Socket, path: String?) {
//        sendJSONText(socket, read(path))
//    }
//
//    fun sendJSON(socket: Socket, uri: Uri?) {
//        sendJSONText(socket, read(uri))
//    }

//    fun sendFile(
//        socket: Socket,
//        inputStream: InputStream?,
//        mineType: String?,
//        charset: String?,
//        isBinary: Boolean
//    ) {
//        if (isBinary) {
//            sendFileAsBinary(socket,
//                toBytes(inputStream),
//                ContentType.getContentType(ContentType.Application.BIN, null))
//        } else {
//            sendFileAsText(socket, read(inputStream), mineType!!, charset)
//        }
//    }

    fun sendFile(socket: Socket, path: String) {
        try {
            PrintStream(socket.getOutputStream()).apply {
                val extension = getExtension(path)
                val contentType = if (extension == null) {
                    ContentType.APPLICATION_BINARY
                } else {
                    val s = contentTypeMap[extension.lowercase()]
                    s ?: ContentType.APPLICATION_BINARY
                }
                println("HTTP/1.1 200 OK")
                println("Content-Type: $contentType")
                if (ContentType.isBinary(contentType)) {
                    val content = FileUtil.readTextUTF8(path)
                    println("Content-Length: ${content.toByteArray().size}")
                    println()
                    println(content)
                } else {
                    val data = FileUtil.readBytes(path)
                    println("Content-Length: ${data.size}")
                    println()
                    write(data, 0, data.size)
                }
                flush()
                close()
            }
        } catch (e: Exception) {}
    }

    fun sendFile(socket: Socket, uri: Uri) {
        try {
            PrintStream(socket.getOutputStream()).apply {
                val mimeType = FileUtil.getMimeType(uri)
                val contentType = "${mimeType}; charset=utf-8"
                println("HTTP/1.1 200 OK")
                println("Content-Type: $contentType")
                if (ContentType.isBinary(contentType)) {
                    val content = FileUtil.readTextUTF8(uri)
                    println("Content-Length: ${content.toByteArray().size}")
                    println()
                    println(content)
                } else {
                    val data = FileUtil.readBytes(uri)
                    println("Content-Length: ${data.size}")
                    println()
                    write(data, 0, data.size)
                }
                flush()
                close()
            }
        } catch (e: Exception) {}
    }

    fun sendText(socket: Socket, content: String, contentType: String) {
        try {
            PrintStream(socket.getOutputStream()).apply {
                println("HTTP/1.1 200 OK")
                println("Content-Type: $contentType")
                println("Content-Length: ${content.toByteArray().size}")
                println()
                println(content)
                flush()
                close()
            }
        } catch (e: Exception) {}
    }

    fun send404(socket: Socket) {
        val content = "<h1 >File Not Found..</h1>"
        try {
            PrintStream(socket.getOutputStream()).apply {
                println("HTTP/1.1 404 file not found")
                println("Content-Type: ${ContentType.TEXT_HTML_UTF8}")
                println("Content-Length: ${content.toByteArray().size}")
                println()
                println(content)
                flush()
                close()
            }
        } catch (e: Exception) {}
    }

    private fun getExtension(filePath: String): String? {
        val index = filePath.lastIndexOf('.')
        if (index != -1) {
            return null
        }
        return filePath.substring(index + 1)
    }

//    private fun sendFileAsText(
//        socket: Socket,
//        content: String?,
//        mineType: String,
//        charset: String?
//    ) {
//        try {
//            val ps = PrintStream(socket.getOutputStream())
//            val contentType = ContentType.getContentType(mineType, charset)
//            ps.println("HTTP/1.1 200 OK")
//            ps.println("Content-Type: $contentType")
//            ps.println("Content-Length: " + content!!.toByteArray().size)
//            ps.println()
//            ps.println(content)
//            ps.flush()
//            ps.close()
//            socket.shutdownOutput()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun sendFileAsBinary(socket: Socket, data: ByteArray?, mineType: String?) {
//        try {
//            val ps = PrintStream(socket.getOutputStream())
//            val contentType = ContentType.getContentType(mineType!!, null)
//            ps.println("HTTP/1.1 200 OK")
//            ps.println("Content-Type: $contentType")
//            ps.println("Content-Length: " + data!!.size)
//            ps.println()
//            ps.write(data, 0, data.size)
//            ps.flush()
//            ps.close()
//            socket.shutdownOutput()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
}