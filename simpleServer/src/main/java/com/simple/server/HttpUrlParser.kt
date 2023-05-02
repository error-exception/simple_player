package com.simple.server

object HttpUrlParser {

    private var content = ""
    private var currentChar = ' '
    private var index = 0

    fun parse(url: String): HttpUrl {
        reset()
        content = url
        val httpUrl = HttpUrl()
        while (nextChar()) {
            if (httpUrl.path.isEmpty()) {
                val sb = StringBuilder()
                do {
                    sb.append(currentChar)
                } while (nextChar() && currentChar != '?')
                httpUrl.path = sb.toString()
            } else {
                var isValue = false
                val key = StringBuilder()
                val value = StringBuilder()
                do {
                    if (currentChar == '=') {
                        isValue = true
                        continue
                    }
                    if (isValue) {
                        value.append(currentChar)
                    } else {
                        key.append(currentChar)
                    }
                } while (nextChar() && currentChar != '&')
                httpUrl.queryMap[key.toString()] = value.toString()
            }
        }
        return httpUrl
    }

    private fun reset() {
        currentChar = ' '
        content = ""
        index = 0
    }

    private fun nextChar(): Boolean {
        if (index >= content.length) {
            return false
        }
        currentChar = content[index++]
        return true

    }

}