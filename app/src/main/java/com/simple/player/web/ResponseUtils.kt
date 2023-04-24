package com.simple.player.web

object ResponseUtils {

    fun ok(data: Any? = null): HashMap<String, Any?> {
        val map = HashMap<String, Any?>()
        map["code"] = 0
        map["message"] = "ok"
        map["data"] = data
        return map
    }

    fun responseEmpty(code: Int, message: String): HashMap<String, Any?> {
        val map = HashMap<String, Any?>()
        map["code"] = code
        map["message"] = message
        map["data"] = null
        return map
    }

}