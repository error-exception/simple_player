package com.simple.server

import java.net.ServerSocket
import java.nio.charset.Charset
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class SimpleHttpServer(val port: Int) {

    private val serverSocket = ServerSocket(port)
    internal var webResourcesRoot = ""
    internal val requestControllerList = ArrayList<RequestController>()
    private val executor: ThreadPoolExecutor
    var isRunning = false
        private set

    init {
        SimpleHttpServerConfig.port = port
        val processors = Runtime.getRuntime().availableProcessors()
        executor = ThreadPoolExecutor(
            processors,
            processors shl 2,
            10,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(processors shl 2),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }

    private val listenRunnable = object: Thread() {

        private var isRunning = true

        override fun run() {
            super.run()

            while (isRunning) {
                try {
                    val socket = serverSocket.accept()
                    executor.execute(ServerThread(this@SimpleHttpServer, socket))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun close() {
            serverSocket.close()
            isRunning = false
            interrupt()
        }

    }

    fun start() {
        listenRunnable.start()
        isRunning = true
    }

    fun close() {
        listenRunnable.close()
        executor.shutdown()
        isRunning = false
    }

    fun setWebResourcesRoot(root: String) {
        webResourcesRoot = root
        SimpleHttpServerConfig.resourceDirectory = root
    }

    fun setDefaultCharset(charset: String) {
        SimpleHttpServerConfig.charset = Charset.forName(charset)
    }

    fun registerHTTPServer(vararg requestControllers: RequestController) {
        for (httpServer in requestControllers) {
            requestControllerList += httpServer
        }
    }

}