package com.simple

import com.simple.server.GetMapping
import com.simple.server.Param
import com.simple.server.RequestController
import com.simple.server.Server
import java.nio.charset.StandardCharsets

fun main() {

    val controller = HelloController()
    val server = Server(8888)
    server.addControllers(controller)
    server.setDefaultCharset("utf-8")
    server.start()
    readLine()

}

class HelloController: RequestController() {

    @GetMapping("/hello")
    fun hello(): String {
        return "<h1>Hello</h1>"
    }

    @GetMapping("/music")
    @Param(["id", "vip"])
    fun getMusic(id: Long, vip: Boolean): String {
        return "<h1>Get Music Width Id=${id}, isVip=${vip}</h1>"
    }
}