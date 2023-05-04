package com.simple.server

import com.simple.server.util.logger
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class Server(val port: Int) {

    private val serverSocket = ServerSocket(port)
    internal val requestControllerList = ArrayList<RequestController>()
    internal var interceptor: Interceptor? = null
    private val executor: ThreadPoolExecutor
    private val log = logger("Server")
    var isRunning = false
        private set

    init {
        ServerConfig.port = port
        val processors = Runtime.getRuntime().availableProcessors()
        executor = ThreadPoolExecutor(
            processors,
            processors shl 2,
            10,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(1024),
            ThreadPoolExecutor.CallerRunsPolicy()
        )
    }

    private val listenRunnable = object: Thread() {

        private var isRunning = true

        override fun run() {
            super.run()

            while (isRunning) {
                var socket: Socket? = null
                try {
                    socket = serverSocket.accept()
                    log("accept: ${socket.inetAddress}")
                    val connection = Connection(socket)
//                    executor.execute(
                    Thread(ServerThread(this@Server, connection)).start()
//                    )
                    socket = null
                } catch (e: Exception) {
                    socket?.let {
                        if (!it.isClosed) {
                            it.close()
                        }
                    }
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
        ServerConfig.resourceDirectory = root
    }

    fun setDefaultCharset(charset: String) {
        ServerConfig.charset = Charset.forName(charset)
    }

    fun addControllers(vararg requestControllers: RequestController) {
        for (httpServer in requestControllers) {
            requestControllerList += httpServer
            httpServer.setSimpleHttpServer(this)
        }
    }

    fun registerInterceptor(interceptor: Interceptor) {
        this.interceptor = interceptor
    }

}