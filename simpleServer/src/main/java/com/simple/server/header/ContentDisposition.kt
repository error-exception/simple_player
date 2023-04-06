package com.simple.server.header

import com.simple.server.util.tokenizer

class ContentDisposition {

    var name: String? = null
        private set
    var filename: String? = null
        private set

    companion object {

        private const val FILENAME = "filename="

        private const val NAME = "name="

        fun parseContentDisposition(value: String): ContentDisposition {
            val contentDisposition = ContentDisposition()
            val tokens = value.tokenizer(";")
            tokens.forEach {
                if (it.startsWith(FILENAME)) {
                    contentDisposition.filename = it.substring(FILENAME.length)
                } else if (it.startsWith(NAME)) {
                    contentDisposition.name = it.substring(NAME.length)
                }
            }
            return contentDisposition
        }

    }

}
