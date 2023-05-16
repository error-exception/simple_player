package com.simple.server

import com.simple.server.util.logger
import java.io.InputStream
import java.lang.RuntimeException
object HttpParser {

    private var isEndOfLine = false
    private var isEndOfHead = false
    private var lineNumber = 0
    private var inputStream: InputStream? = null
    private var currentChar = ' '
    private var httpHeader = HttpHeader()
    private var log = logger("HttpParser")

    fun parse(inputStream: InputStream): HttpProtocol {
        this.inputStream = inputStream
        reset()
        val validChar = skipBlank()
        parseHeader(validChar)
        val httpBody = parseBody()
        log("parse finish!!")
        return HttpProtocol(
            httpHeader = httpHeader,
            httpBody = httpBody
        )
    }

    private fun skipBlank(): Char {
        val stream = inputStream
        stream ?: throw RuntimeException("stream is null")
        var ch = stream.read().toChar()
        while (ch == '\r' || ch == '\n' || ch == ' ') {
            ch = stream.read().toChar()
        }
        return ch
    }

    private fun parseHeader(validChar: Char) {
        var flag = false
        while (true) {
            if (flag) {
                readChar()
            } else {
                currentChar = validChar
                flag = true
            }
            when (currentChar) {
                '\n' -> {
                    if (isEndOfHead) {
                        break
                    }
                    if (isEndOfLine) {
                        continue
                    }
                }
                else -> {
                    if (isEndOfHead) {
                        // to read last '\n'
                        continue
                    }
                    isEndOfLine = false
                    if (lineNumber == 0) {
                        log("parse request line")
                        parseRequestLine()
                    } else {
                        parseHttpField()
                    }
                    lineNumber++
                }
            }
        }

    }

    private fun parseRequestLine() {
        var i = 0
        val method = StringBuilder()
        val url = StringBuilder()
        val httpVersion = StringBuilder()
        while (!isEndOfLine) {
            if (currentChar == ' ') {
                i++
            } else {
                when (i) {
                    0 -> method.append(currentChar)
                    1 -> url.append(currentChar)
                    2 -> httpVersion.append(currentChar)
                }
            }
            readChar()
        }
        httpHeader.requestLine = HttpRequestLine(
            method = method.toString(),
            url = HttpUrlParser.parse(url.toString()),
            version = httpVersion.toString()
        )
        log("request url $url")
    }

    private fun parseHttpField() {
        var i = 0
        val key = StringBuilder()
        val value = StringBuilder()
        var isColon = false
        while (!isEndOfLine) {
            if (currentChar == ':' && i != 0 && !isColon) {
                isColon = true
            } else {
                if (!isColon) {
                    key.append(currentChar.lowercaseChar())
                } else {
                    value.append(currentChar)
                }
            }
            i++
            readChar()
        }
        httpHeader.set(
            field = key.trim().toString(),
            value = value.trim().toString()
        )
        log("header -> {${key.trim()}} {${value.trim()}}")
    }

    private fun parseBody(): HttpBody {
        val stream = inputStream
        stream ?: throw RuntimeException("stream is null")
        return HttpBodyParser.parse(httpHeader, stream)
    }

    private fun readChar() {
        val stream = inputStream
        stream ?: throw RuntimeException("stream is null")
        val ch = stream.read().toChar()
        readState(ch)
        currentChar = ch
    }

    private fun readState(ch: Char) {
        if (ch == '\r') {
            if (isEndOfLine) {
                isEndOfHead = true
            } else {
                isEndOfLine = true
            }
        }
    }

    private fun reset() {
        isEndOfHead = false
        isEndOfLine = false
        lineNumber = 0
        httpHeader = HttpHeader()
    }
}