package com.simple

import com.simple.server.GetMapping
import com.simple.server.Param
import com.simple.server.Request
import com.simple.server.RequestController
import com.simple.server.SimpleHttpServer
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.pathString

fun main() {

}

class MusicController: RequestController() {

    @GetMapping("/music")
    @Param(["id", ":request"])
    fun music(id: String, request: Request): String {
        println(id)
        println(request)
        return "Get music id=$id"
    }

}

fun simpleServer() {
    val server = SimpleHttpServer(8888).apply {
        setWebResourcesRoot("C:\\Users\\HP\\Desktop\\reboot\\Develop\\HTML\\Simple Player Web")
        setDefaultCharset("utf-8")
        registerHTTPServer(MusicController())
        start()
    }
    readLine()
    server.close()
}
