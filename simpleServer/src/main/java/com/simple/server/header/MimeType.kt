package com.simple.server.header

import com.simple.server.util.tokenizer
import java.nio.charset.Charset

class MimeType(contentType: String) {

    lateinit var genericType: String
    lateinit var subType: String
    var charset: Charset? = null
    var boundary: String? = null

    init {
        val tokens = contentType.tokenizer(";")
        for (token in tokens) {
            if (token.contains('/')) {
                val list = token.split("/")
                genericType = list[0].lowercase()
                subType = list[1].lowercase()
            } else if (token.startsWith(PARAM_CHARSET)) {
                charset = Charset.forName(token.substring(PARAM_CHARSET.length))
            } else if (token.startsWith(PARAM_BOUNDARY)) {
                boundary = token.substring(PARAM_BOUNDARY.length)
            }
        }
    }

    fun isText(): Boolean {
        return genericType == "text"
    }

    fun isSameType(mimeType: MimeType): Boolean {
        return mimeType.genericType == genericType && mimeType.subType == subType
    }

    fun toSimpleString(): String = "$genericType/$subType"

    override fun toString(): String {
        return if (charset != null) {
            "$genericType/$subType;charset=${charset.toString()}"
        } else {
            "$genericType/$subType"
        }
    }

    companion object {
        const val PARAM_CHARSET = "charset="
        const val PARAM_BOUNDARY = "boundary="

        val MIME_TYPE_MULTIPART_FORM_DATA = MimeType("multipart/form-data")
        val MIME_TYPE_APPLICATION_JSON = MimeType("application/json")
        val MIME_TYPE_APPLICATION_X_WWW_FORM_URLENCODED = MimeType("application/x-www-form-urlencoded")
        val MIME_TYPE_TEXT_HTML = MimeType("text/html")
        val MIME_TYPE_TEXT_PLAIN = MimeType("text/plain")
    }

}