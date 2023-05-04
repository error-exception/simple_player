package com.simple.server

import com.simple.server.constant.ResponseState
import com.simple.server.request.Request
import com.simple.server.util.logger
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.RuntimeException
import java.net.Socket
import java.net.SocketTimeoutException

class Connection(socket: Socket) {

    private val inputStream = socket.getInputStream()
    private var request: Request? = null
    private var response: Response
    private val log = logger("Connection")

    init {
        socket.soTimeout = 5000
        response = Response(socket)
        try {
            handleRequest()
        } catch (e: SocketTimeoutException) {
            log("timeout close socket")
            socket.close()
        } catch (e: NumberFormatException) {
            response.responseWithEmptyBody(ResponseState.BAD_REQUEST)
        } catch (e: Exception) {
            log("has exception close socket")
            socket.close()
            e.printStackTrace()
        }
    }

    private fun handleRequest() {
        val http = HttpParser.parse(inputStream)
        request = Request(httpHeader = http.httpHeader, requestBody = http.httpBody)
    }

    fun getRequest(): Request = request ?: throw RuntimeException("request is null")

    fun getResponse(): Response = response

    fun close() {
        TODO("当一个请求到达最大执行时间时，关闭连接，中断线程")
    }

}