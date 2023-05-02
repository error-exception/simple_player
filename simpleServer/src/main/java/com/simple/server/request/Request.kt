package com.simple.server.request

import com.simple.server.HttpBody
import com.simple.server.HttpHeader
import com.simple.server.HttpRequestLine
import com.simple.server.HttpUrl
import java.lang.RuntimeException

class Request(val httpHeader: HttpHeader, val requestBody: HttpBody) {

    private val requestLine: HttpRequestLine = httpHeader.requestLine ?: throw RuntimeException("request line is null")

    private val attributes = HashMap<String, Any>()

    fun getMethod(): String {
        return requestLine.method
    }

    fun setAttribute(name: String, value: Any) {
        attributes[name] = value
    }

    fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    fun isAjaxRequest(): Boolean {
        return httpHeader.get("X-Requested-With") == "XMLHttpRequest"
    }

    fun getPath(): String {
        return requestLine.url.path
    }

    fun getHttpUrl(): HttpUrl {
        return requestLine.url
    }

}