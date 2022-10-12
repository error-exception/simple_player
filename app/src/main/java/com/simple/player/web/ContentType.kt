package com.simple.player.web

import java.lang.StringBuilder

object ContentType {
    fun getContentType(type: String, charset: String?): String {
        val s = StringBuilder(type)
        if (charset != null) {
            s.append(';').append(' ').append(charset)
        }
        return s.toString()
    }

    const val TEXT_HTML = "text/html"

    const val TEXT_HTML_UTF8 = "text/html; charset=utf-8"

    const val TEXT_JAVASCRIPT = "text/javascript"

    const val TEXT_JAVASCRIPT_UTF8 = "text/javascript; charset=utf-8"

    const val TEXT_CSS = "text/css"

    const val TEXT_CSS_UTF8 = "text/css; charset=utf-8"

    const val IMAGE_X_ICON = "image/x-icon"

    const val IMAGE_PNG = "image/png"
    const val IMAGE_JPG = "image/jpeg"
    const val IMAGE_SVG_XML = "image/svg+xml"

    const val AUDIO_MPEG = "audio/mp3"

    const val APPLICATION_JSON = "application/json"

    const val APPLICATION_JSON_UTF8 = "application/json; charset=utf-8"

    const val APPLICATION_BINARY = "application/octet-stream"

    fun isBinary(contentType: String): Boolean {
        if (contentType.startsWith("text/")) {
            return false
        }
        if (contentType.startsWith(APPLICATION_JSON)) {
            return false
        }
        return true
    }
}