package com.simple.player.web

import android.util.Log
import java.io.IOException
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket

object MainSocket {
    private val TAG = "socket"
    private var serverSocket: ServerSocket? = null
    private lateinit var socketThread: SocketThread
    var isStart = false
        private set

    fun start() {
        Log.e(TAG, "start")
        try {
            serverSocket = ServerSocket(8080)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        socketThread = SocketThread()
        socketThread.start()
        isStart = true
    }

    fun close() {
        Log.e(TAG, "close")
        socketThread.interrupt()
        try {
            if (serverSocket != null) {
                if (!serverSocket!!.isClosed) {
                    serverSocket!!.close()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isStart = false
    }

    private class SocketThread : Thread() {
        override fun run() {
            super.run()
            try {
                var socket: Socket
                while (true) {
                    socket = serverSocket!!.accept()
                    val thread = ServerThread(socket)
                    thread.start()
                    val hostAddress = socket.localAddress.hostAddress
                    Log.e(TAG, "host $hostAddress")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}