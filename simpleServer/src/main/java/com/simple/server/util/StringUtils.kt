package com.simple.server.util

import java.util.StringTokenizer

fun String.tokenizer(delim: String): Array<String> {
    val tokenizer = StringTokenizer(this, " \t\n\r\u000c${delim}")
    val count = tokenizer.countTokens()
    return Array(count) {
        tokenizer.nextToken()
    }
}