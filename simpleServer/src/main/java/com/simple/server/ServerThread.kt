package com.simple.server

import com.simple.server.constant.AttributeConstant
import com.simple.server.constant.ResponseState
import com.simple.server.header.MimeType
import com.simple.server.util.ContentTypeHelper
import com.simple.server.util.Resource
import com.simple.server.request.Request
import java.io.File
import java.net.Socket
import java.net.URLDecoder

class ServerThread(private val server: Server, private val connection: Connection): Runnable {

    var tag = threadId++

    override fun run() {
        val request = connection.getRequest()
        val response = connection.getResponse()
//        if (!request.isHttpRequest) {
//            connection.getResponse().responseWithEmptyBody(ResponseState.BAD_REQUEST)
//            return
//        }
        val isStatic = handleStaticResource(request, response)
        if (!isStatic) {
            var success = false
            for (httpServer in server.requestControllerList) {
                success = httpServer.callMethod(request, response)
                if (success) {
                    break
                }
            }
            if (!success) {
                response.responseWithEmptyBody(ResponseState.NOT_FOUND)
            }
        }
    }

    /**
     * 如果不是静态资源请求，返回 false， 否则处理该请求并返回 true
     */
    private fun handleStaticResource(request: Request, response: Response): Boolean {
        if (request.isAjaxRequest()) {
            return false
        }
        val url =
            if (request.getPath() == "/") {
                "/index.html"
            } else {
                URLDecoder.decode(request.getPath(), ServerConfig.charset.toString())
            }
        val path = File(ServerConfig.resourceDirectory).absolutePath + url
        val targetFile = File(path)
        if (!targetFile.exists() || targetFile.isDirectory) {
            return false
        }
        val resource = Resource.fromFile(
            file = targetFile,
            mimeType = MimeType(ContentTypeHelper.getContentTypeByURL(url), ServerConfig.charset)
        )
        request.setAttribute(AttributeConstant.ATTR_REQUEST_RESOURCE, resource)
        response.handleRequest(request)

        return true
    }
}
