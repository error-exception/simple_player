package com.simple.player.web

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.Socket

object RequestParser {

    fun parseRequest(socket: Socket): Request {
        val request = Request()
        try {
            val input = socket.getInputStream()
            val reader = BufferedReader(InputStreamReader(input))
            val line = reader.readLine()
            line ?: return request
            val split = line.split(" ")
            request.apply {
                method = split[0]
                requestUrl = split[1]
                httpVersion = split[2]
            }
            while (true) {
                val string = reader.readLine()
                string ?: break
                if (!string.contains(":")) {
                    break
                }
                val tmp = string.split(":")
                request.header[tmp[0].trim()] = tmp[1].trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return request
    }
}