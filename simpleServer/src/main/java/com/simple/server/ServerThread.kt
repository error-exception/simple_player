package com.simple.server

import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.util.ContentTypeHelper
import com.simple.server.util.Resource
import com.simple.server.util.logger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.Charset

class ServerThread(private val server: SimpleHttpServer, var socket: Socket): Runnable {

    var tag = threadId++

    override fun run() {
        val request = Request(socket)
        if (!request.isHttpRequest) {
            Response(socket).responseWithEmptyBody(ResponseState.BAD_REQUEST)
            return
        }
        val isStatic = handleStaticResource(request)
        var success = false
        if (!isStatic) {
            for (httpServer in server.requestControllerList) {
                success = httpServer.callMethod(request.requestUrl!!.url, request.method!!, request, Response(socket))
                if (success) {
                    break
                }
            }
        }
        if (!success) {
            Response(socket).responseWithEmptyBody(ResponseState.NOT_FOUND)
        }
    }

    /**
     * 如果不是静态资源请求，返回 false， 否则处理该请求并返回 true
     */
    private fun handleStaticResource(request: Request): Boolean {
        if (request.isAjaxRequest()) {
            return false
        }
        val url =
            if (request.requestUrl!!.url == "/") {
                "/index.html"
            } else {
                URLDecoder.decode(request.requestUrl!!.url, SimpleHttpServerConfig.charset.toString())
            }
        val path = File(server.webResourcesRoot).absolutePath + url
        val targetFile = File(path)
        if (!targetFile.exists()) {
            return false
        }
        val response = Response(socket)
        val resource = Resource()
        resource.setResource(targetFile, MimeType(ContentTypeHelper.getContentTypeByURL(url, SimpleHttpServerConfig.charset.toString())))
        request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
        response.handleRequest(request, server)

        return true
    }
}
