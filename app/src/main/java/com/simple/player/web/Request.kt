package com.simple.player.web

import java.util.HashMap

class Request {

    lateinit var httpVersion: String
    lateinit var requestUrl: String
    lateinit var method: String

    val header: HashMap<String, String?> = HashMap()
    val parameter = HashMap<String, String>()

    init {
        val index = requestUrl.lastIndexOf("?")
        if (index != -1) {
            val parameterSequence = requestUrl.substring(index + 1)
            val parameters = parameterSequence.split("&")
            for (parameter in parameters) {
                val tmp = parameter.split("=")
                this.parameter[tmp[0]] = tmp[1]
            }
        }
    }

    fun isAjax(): Boolean {
        return header["X-Requested-With"] == "XMLHttpRequest"
    }

    fun clear() {
        header.clear()
        parameter.clear()
    }


}