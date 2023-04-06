package com.simple.server.util

fun logger(tag: String): (message: String) -> Unit {
    return {
        println("%-30s %s".format(tag, it))
    }
}