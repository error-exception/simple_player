package com.simple.server.constant

object ResponseState {

    const val CONTINUE = 100
    const val SWITCHING_PROTOCOLS = 101
    const val OK = 200
    const val CREATED = 201
    const val ACCEPTED = 202
    const val NON_AUTHORITATIVE_INFORMATION = 203
    const val NO_CONTENT = 204
    const val RESET_CONTENT = 205
    const val PARTIAL_CONTENT = 206
    const val MULTIPLE_CHOICES = 300
    const val MOVE_PERMANENTLY = 301
    const val FOUND = 302
    const val SEE_OTHER = 303
    const val NOT_MODIFIED = 304
    const val USE_PROXY = 305
    const val UNUSED = 306
    const val TEMPORARY_REDIRECT = 307
    const val BAD_REQUEST = 400
    const val UNAUTHORIZED = 401
    const val PAYMENT_REQUIRED = 402
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404
    const val METHOD_NOT_ALLOWED = 405
    const val NOT_ACCEPTABLE = 406
    const val PROXY_AUTHENTICATION_REQUIRED = 407
    const val REQUEST_TIME_OUT = 408
    const val CONFLICT = 409
    const val GONE = 410
    const val LENGTH_REQUIRED = 411
    const val PRECONDITION_FAILED = 412
    const val REQUEST_ENTITY_TOO_LARGE = 413
    const val REQUEST_URI_TOO_LARGE = 414
    const val UNSUPPORTED_MEDIA_TYPE = 415
    const val REQUESTED_RANGE_NOT_SATISFIABLE = 416
    const val EXPECTATION_FAILED = 417
    const val INTERNAL_SERVER_ERROR = 500
    const val NOT_IMPLEMENTED = 501
    const val BAD_GATEWAY = 502
    const val SERVICE_UNAVAILABLE = 503
    const val GATEWAY_TIME_OUT = 504
    const val HTTP_VERSION_NOT_SUPPORTED = 505

    fun getStateInfo(stateCode: Int): String {
        return when (stateCode) {
            100 -> "Continue"
            101 -> "Switching Protocols"
            200 -> "OK"
            201 -> "Created"
            202 -> "Accepted"
            203 -> "Non-Authoritative Information"
            204 -> "No Content"
            205 -> "Reset Content"
            206 -> "Partial Content"
            300 -> "Multiple Choices"
            301 -> "Moved Permanently"
            302 -> "Found"
            303 -> "See Other"
            304 -> "Not Modified"
            305 -> "Use Proxy"
            306 -> "Unused"
            307 -> "Temporary Redirect"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            402 -> "Payment Required"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            406 -> "Not Acceptable"
            407 -> "Proxy Authentication Required"
            408 -> "Request Time-out"
            409 -> "Conflict"
            410 -> "Gone"
            411 -> "Length Required"
            412 -> "Precondition Failed"
            413 -> "Request Entity Too Large"
            414 -> "Request-URI Too Large"
            415 -> "Unsupported Media Type"
            416 -> "Requested range not satisfiable"
            417 -> "Expectation Failed"
            500 -> "Internal Server Error"
            501 -> "Not Implemented"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Time-out"
            505 -> "HTTP Version not supported"
            else -> throw RuntimeException("invalid response code $stateCode")
        }
    }

}