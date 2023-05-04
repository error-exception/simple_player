package com.simple.server

import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.util.Resource
import com.simple.server.util.StreamUtils
import com.simple.server.request.Request
import com.simple.server.util.logger
import java.io.FileNotFoundException
import java.io.PrintStream
import java.lang.IllegalArgumentException
import java.net.Socket

/**
 * 对于简单的响应，在使用时，只需给出响应体，该类会自动计算出响应体的大小，默认情况下，在响应时，会自动向浏览器或客户端请求断开连接
 */
class Response(private val socket: Socket) {

    private var header: HttpHeader = HttpHeader()
    private val outputStream = PrintStream(socket.getOutputStream())
    private val log = logger("Response")
    var hasResponded = false
        internal set

    var responseCode = 200

    init {
        with(header) {
            setContentLength(0)
            setContentType(MimeType.MIME_TYPE_TEXT_PLAIN)
        }
    }

    /**
     * 写入响应头信息，并加上分割行
     */
    private fun writeHeader(header: HttpHeader) {
        outputStream.apply {
            write("HTTP/1.1 $responseCode ${ResponseState.getStateInfo(responseCode)}\r\n".toByteArray())
            val list = header.hashMap
            for (pair in list) {
                write("${pair.key}: ${pair.value}\r\n".toByteArray())
            }
            write("\r\n".toByteArray())
        }
    }

    fun handleRequest(request: Request) {
        val resource = request.getAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE) as Resource?
        if (resource == null) {
            responseWithEmptyBody(ResponseState.NOT_FOUND)
            return
        }
        val requestHeader = request.httpHeader
        val range = requestHeader.getRange(resource.getLength())
        if (range != null && ServerConfig.enablePartial) {
            try {
                responseCode = ResponseState.PARTIAL_CONTENT
                val contentLength = range.end - range.start + 1
                header.apply {
                    setContentRange("bytes ${range.start}-${range.end}/${resource.getLength()}")
                    setContentType(resource.mimeType)
                    setContentLength(contentLength)
                    setConnection("Close")
                }
                hasResponded = true
                StreamUtils.copyToRange(resource, getBody(), range.start, contentLength)
                closeSocket()
            } catch (e: FileNotFoundException) {
                responseWithEmptyBody(ResponseState.NOT_FOUND)
            } catch (e: IllegalArgumentException) {
                responseWithEmptyBody(ResponseState.BAD_REQUEST)
            }
        } else {
            responseCode = ResponseState.OK
            header.apply {
                setContentType(resource.mimeType)
                setContentLength(resource.getLength())
                setConnection("Close")
            }

            try {
                hasResponded = true
                StreamUtils.copy(resource, getBody())
                closeSocket()
            } catch (e: FileNotFoundException) {
                responseWithEmptyBody(ResponseState.NOT_FOUND)
            }
        }
    }

    fun responseWithEmptyBody(stateCode: Int) {
        hasResponded = true
        responseCode = stateCode
        header.setConnection("Close")
        writeHeader(header)
        outputStream.flush()
        closeSocket()

    }

    fun getBody(): PrintStream {
        writeHeader(header)
        return outputStream
    }

    private fun closeSocket() {
        log("responsed close socket")
        if (socket.isConnected) {
            socket.shutdownOutput()
        }
        if (!socket.isClosed) {
            socket.close()
        }
    }
}