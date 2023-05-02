package com.simple.server.header
//
//import com.simple.server.Request
//import com.simple.server.Response
//import java.nio.charset.Charset
//
//class HttpHeader {
//
//    var request: Request? = null
//    var response: Response? = null
//    private val headers = HashMap<String, String>()
//
//    constructor(request: Request) {
//        this.request = request
//    }
//
//    constructor(response: Response) {
//        this.response = response
//    }
//
//    constructor()
//
//
//    fun set(headerName: String, headerValue: String) {
//        headers[headerName] = headerValue
//    }
//
//    private fun get(headerName: String): String? {
//        var value = headers[headerName]
//        if (value == null)
//            value = headers[headerName.lowercase()]
//        return value
//    }
//
//    fun has(headerName: String): Boolean {
//        return headers[headerName] != null
//    }
//
//    private fun getIfNull(headerName: String, defaultValue: String): String {
//        var value = headers[headerName]
//        if (value == null) {
//            value = headers[headerName.lowercase()]
//        }
//        if (value == null) {
//            headers[headerName] = defaultValue
//            value = defaultValue
//        }
//        return value
//    }
//
//    fun getXRequestedWith(): String? {
//        return get(X_REQUESTED_WITH)
//    }
//
//    fun remove(headerName: String) {
//        val values = headers[headerName]
//        values ?: return
//        headers.remove(headerName)
//    }
//
//    fun setRange(range: String) {
//        set(RANGE, range)
//    }
//
//    fun setContentRange(contentRange: String) {
//        set(CONTENT_RANGE, contentRange)
//    }
//
//    fun setContentLength(length: Long) {
//        set(CONTENT_LENGTH, length.toString())
//    }
//
//    fun getRange(): Range? {
//        println("HttpHeader: ${headers}")
//        var range = get(RANGE)
//        if (range == null)
//            range = get(RANGE.lowercase())
//        range ?: return null
//        request ?: return null
//        return Range.parseRange(request!!, range)
//    }
//
//    fun getContentType(): MimeType {
//        val contentType = getIfNull(CONTENT_TYPE, "text/plain;charset=utf-8")
//        return MimeType(contentType)
//    }
//
//    fun getContentLength(): Long {
//        return getIfNull(CONTENT_LENGTH, "0").toLong()
//    }
//
//    fun getHeaderList(): List<Pair<String, String>> {
//        return headers.toList()
//    }
//
//    fun setContentType(mimeType: MimeType) {
//        set(CONTENT_TYPE, mimeType.toString())
//    }
//
//    fun setConnection(value: String) {
//        set(CONNECTION, value)
//    }
//
//    fun getConnection(): String? {
//        return get(CONNECTION)
//    }
//
//    fun setContentDisposition(value: String) {
//        set(CONTENT_DISPOSITION, value)
//    }
//
//    fun getContentDisposition(): ContentDisposition? {
//        val contentDisposition = get(CONTENT_DISPOSITION)
//        contentDisposition ?: return null
//        return ContentDisposition.parseContentDisposition(contentDisposition)
//    }
//
//    companion object {
//
//        const val ACCEPT = "Accept"
//
//        const val ACCEPT_CHARSET = "Accept-Charset"
//
//        const val ACCEPT_ENCODING = "Accept-Encoding"
//
//        const val ACCEPT_LANGUAGE = "Accept-Language"
//
//        const val ACCEPT_PATCH = "Accept-Patch"
//
//        const val ACCEPT_RANGES = "Accept-Ranges"
//
//        const val ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials"
//
//        const val ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers"
//
//        const val ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods"
//
//        const val ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin"
//
//        const val ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers"
//
//        const val ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age"
//
//        const val ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers"
//
//        const val ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method"
//
//        const val AGE = "Age"
//
//        const val ALLOW = "Allow"
//
//        const val AUTHORIZATION = "Authorization"
//
//        const val CACHE_CONTROL = "Cache-Control"
//
//        const val CONNECTION = "Connection"
//
//        const val CONTENT_ENCODING = "Content-Encoding"
//
//        const val CONTENT_DISPOSITION = "Content-Disposition"
//
//        const val CONTENT_LANGUAGE = "Content-Language"
//
//        const val CONTENT_LENGTH = "Content-Length"
//
//        const val CONTENT_LOCATION = "Content-Location"
//
//        const val CONTENT_RANGE = "Content-Range"
//
//        const val CONTENT_TYPE = "Content-Type"
//
//        const val COOKIE = "Cookie"
//
//        const val DATE = "Date"
//
//        const val ETAG = "ETag"
//
//        const val EXPECT = "Expect"
//
//        const val EXPIRES = "Expires"
//
//        const val FROM = "From"
//
//        const val HOST = "Host"
//
//        const val IF_MATCH = "If-Match"
//
//        const val IF_MODIFIED_SINCE = "If-Modified-Since"
//
//        const val IF_NONE_MATCH = "If-None-Match"
//
//        const val IF_RANGE = "If-Range"
//
//        const val IF_UNMODIFIED_SINCE = "If-Unmodified-Since"
//
//        const val LAST_MODIFIED = "Last-Modified"
//
//        const val LINK = "Link"
//
//        const val LOCATION = "Location"
//
//        const val MAX_FORWARDS = "Max-Forwards"
//
//        const val ORIGIN = "Origin"
//
//        const val PRAGMA = "Pragma"
//
//        const val PROXY_AUTHENTICATE = "Proxy-Authenticate"
//
//        const val PROXY_AUTHORIZATION = "Proxy-Authorization"
//
//        const val RANGE = "Range"
//
//        const val REFERER = "Referer"
//
//        const val RETRY_AFTER = "Retry-After"
//
//        const val SERVER = "Server"
//
//        const val SET_COOKIE = "Set-Cookie"
//
//        const val SET_COOKIE2 = "Set-Cookie2"
//
//        const val TE = "TE"
//
//        const val TRAILER = "Trailer"
//
//        const val TRANSFER_ENCODING = "Transfer-Encoding"
//
//        const val UPGRADE = "Upgrade"
//
//        const val USER_AGENT = "User-Agent"
//
//        const val VARY = "Vary"
//
//        const val VIA = "Via"
//
//        const val WARNING = "Warning"
//
//        const val WWW_AUTHENTICATE = "WWW-Authenticate"
//
//        const val X_REQUESTED_WITH = "X-Requested-With"
//
//
//    }
//
//}