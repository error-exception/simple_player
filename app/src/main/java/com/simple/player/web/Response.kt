package com.simple.player.web

import java.io.PrintStream
import java.net.Socket

class Response {

    private val map = HashMap<String, String>()
    private var body = Any()

    fun addHeader(name: String, value: String) {
        map[name] = value
    }

    fun setContentType(contentType: String) {
        map["Content-Type"] = contentType
    }

    /**
     * accept: readable content
     */
    fun setResponseBody(text: String) {
        body = text
    }

    fun setResponseBody(byteArray: ByteArray) {
        body = byteArray
    }

    fun response(socket: Socket) {
        PrintStream(socket.getOutputStream()).apply {
            for (entry in map) {
                println("${entry.key}: ${entry.value}")
            }
            println()
            if (body is String) {
                println(body)
            }
            if (body is ByteArray) {
                val a = body as ByteArray
                write(a, 0, a.size)
            }
            flush()
            close()
        }
    }

}