package com.simple.server

data class HttpProtocol(
    val httpHeader: HttpHeader,
    val httpBody: HttpBody
)