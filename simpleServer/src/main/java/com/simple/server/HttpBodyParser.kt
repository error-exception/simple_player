package com.simple.server

import java.io.ByteArrayOutputStream
import java.io.InputStream

//TODO: handle multipart/form-data
object HttpBodyParser {

    fun parse(httpHeader: HttpHeader, inputStream: InputStream): HttpBody {
        return parseBinaryBody(httpHeader, inputStream)
    }

    private fun parseBinaryBody(httpHeader: HttpHeader, inputStream: InputStream): HttpBody {
        val contentLength = httpHeader.getContentLength()
        val bytesContainer = ByteArrayOutputStream()
        for (i in 0 until contentLength) {
            bytesContainer.write(inputStream.read())
        }
        val body = HttpBody()
        body.byteData = bytesContainer.toByteArray()
        bytesContainer.close()
        return body
    }

}