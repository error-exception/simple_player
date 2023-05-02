package com.simple.server

data class HttpRequestLine(
    val method: String,
    val url: HttpUrl,
    val version: String
)