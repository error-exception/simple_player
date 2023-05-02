package com.simple.server

import com.simple.server.request.Request

open class RequestController {

    private val map = HashMap<Pair<String /* url */, String /* method */>, RequestURLHandler>()

    protected lateinit var server: Server

    init {
        val methods = this.javaClass.declaredMethods
        for (method in methods) {
            val get = method.getAnnotation(GetMapping::class.java)
            val post = method.getAnnotation(PostMapping::class.java)
            if (get != null) {
                map[get.url to "GET"] = RequestURLHandler().apply {
                    requestController = this@RequestController
                    this.method = method
                    responseContentType = get.contentType
                }
            }
            if (post != null) {
                map[post.url to "POST"] = RequestURLHandler().apply {
                    requestController = this@RequestController
                    this.method = method
                    responseContentType = post.contentType
                }
            }
        }
    }

    internal fun setSimpleHttpServer(server: Server) {
        this.server = server
    }

    internal fun callMethod(request: Request, response: Response): Boolean {
        val method1 = map[request.getPath() to request.getMethod()]
        method1 ?: return false
        method1.call(request, response, server)
        return true
    }
}