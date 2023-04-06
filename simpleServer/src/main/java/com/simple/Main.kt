package com.simple

import com.simple.server.SimpleHttpServer


fun main() {
    simpleServer()
}

fun simpleServer() {
    val server = SimpleHttpServer(8888).apply {
        setWebResourcesRoot("C:\\Users\\HP\\Desktop\\reboot\\Develop\\HTML\\Simple Player Web")
        setDefaultCharset("utf-8")
        start()
    }
    readLine()
    server.close()
}
