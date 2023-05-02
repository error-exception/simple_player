package com.simple.server

class HttpStatusLine(
    private val version: String,
    private val statusCode: String,
    private val message: String
) {

    override fun toString(): String {
        return "$version $statusCode $message"
    }

}