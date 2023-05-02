package com.simple.server.util

import com.simple.server.ServerConfig
import com.simple.server.header.MimeType
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception

class Resource(
    private val inputStream: InputStream? = null,
    private val length: Long = -1,
    val mimeType: MimeType = MimeType.MIME_TYPE_TEXT_PLAIN
) {
    private val log = logger("Resource")

    fun getLength(): Long {
        if (length > 0) {
            return length
        }
        throw FileNotFoundException("no resource found!!")
    }

    fun getInputStream(): InputStream {
        val stream = inputStream
        stream ?: throw FileNotFoundException("no resource found!!")
        return stream
    }

    fun close() {
        try {
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {

        fun fromBytes(bytes: ByteArray, mimeType: MimeType): Resource = Resource(
            inputStream = ByteArrayInputStream(bytes),
            length = bytes.size.toLong(),
            mimeType = mimeType
        )

        fun fromFile(file: File, mimeType: MimeType): Resource = Resource(
            inputStream = FileInputStream(file),
            length = file.length(),
            mimeType = mimeType
        )

        fun fromString(s: String, mimeType: MimeType): Resource {
            val bytes = s.toByteArray(mimeType.charset ?: ServerConfig.charset)
            return fromBytes(bytes, mimeType)
        }
    }

}