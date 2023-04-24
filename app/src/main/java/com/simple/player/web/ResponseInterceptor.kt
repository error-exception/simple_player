package com.simple.player.web

import com.simple.server.Interceptor

class ResponseInterceptor: Interceptor {

    override fun afterController(result: Any?): Any? {
        return result
    }

}