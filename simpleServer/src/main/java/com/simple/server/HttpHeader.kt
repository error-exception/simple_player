package com.simple.server

import com.simple.server.header.ContentDisposition
import com.simple.server.header.MimeType
import com.simple.server.header.Range

class HttpHeader {

    internal val hashMap = HashMap<String, String>()

    var requestLine: HttpRequestLine? = null
    var statusLine: HttpStatusLine? = null

    private var contentLength: Long = -1
    // TODO: send bad request response when occur exception
    fun getContentLength(): Long {
        if (contentLength > 0) {
            return contentLength
        }
        val value = hashMap["content-length"]
        value ?: return 0
        contentLength = value.toLong()
        return contentLength
    }

    fun setContentLength(length: Long) {
        contentLength = length
        set("content-length", length.toString())
    }

    fun setContentType(type: MimeType) {
        set("content-type", type.toString())
    }

    fun getContentType(): MimeType {
        val type = hashMap["content-type"] ?: "text/plain;charset=utf-8"
        return MimeType(type)
    }

    fun setContentRange(contentRange: String) {
        set("content-range", contentRange)
    }

    fun setConnection(connection: String) {
        set("connection", connection)
    }

    fun getConnection(): String? {
        return hashMap["connection"]
    }

//    private var isContentTypeAccessed = false
//    var contentType: MimeType = MimeType.MIME_TYPE_TEXT_HTML
//        get() {
//            if (isContentTypeAccessed) {
//                return field
//            }
//            isContentTypeAccessed = true
//            val value = hashMap["content-type"]
//            value ?: return field
//            field = MimeType(value)
//            return field
//        }
//        internal set

    private val contentDispositionState: MutablePair<ContentDisposition?, Boolean> = MutablePair(null, false)
    fun getContentDisposition(): ContentDisposition? {
        if (contentDispositionState.second) {
            return contentDispositionState.first
        }
        val value = hashMap["content-disposition"] ?: return null
        val contentDisposition = ContentDisposition.parseContentDisposition(value)
        contentDispositionState.first = contentDisposition
        return contentDisposition
    }

    fun setContentDisposition(value: String) {
        set("content-disposition", value)
    }

    fun setRange(range: String) {
        set("range", range)
    }

    fun getRange(length: Long): Range? {
        val value = hashMap["range"] ?: return null
        return Range.parseRange(length, value)
    }

    internal fun set(field: String, value: String) {
        hashMap[field] = value
    }

    fun get(name: String): String? {
        return hashMap[name]
    }

    override fun toString(): String {
        return hashMap.toString()
    }


}