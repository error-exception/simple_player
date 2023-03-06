package com.simple.player.lyrics

import android.util.Log
import com.simple.player.util.FileUtil
import java.io.InputStream

class LrcParser {

    private var lrcContent = ""
    private var currentChar = ' '
    private var index = 0
    private lateinit var lrc: Lrc

    fun parse(inputStream: InputStream): Lrc? {
        lrcContent = FileUtil.readTextUTF8(inputStream)
        if (lrcContent.isEmpty() || lrcContent.isBlank()) {
            return null
        }
        lrc = Lrc()
        while (nextChar()) {
            if (currentChar == '[') {
                nextChar()
                if (currentChar in '0'..'9') {
                    previousChar()
                    val time = handleTimeTag()
                    val content = handleString()
                    if (content.isNotBlank()) {
                        lrc.addLine(time = time, content = content)
                    }
                } else {
                    previousChar()
                    handleMetaDataTag()
                }
            }
        }
        return lrc
    }

    private fun handleString(): String {
        val builder = StringBuilder()
        while (nextChar() && currentChar != '[') {
            builder.append(currentChar)
        }
        previousChar()
        return builder.trim('\n', '\r', ' ', '\t').toString()
    }

    private fun handleMetaDataTag() {

        val builder = StringBuilder(4)
        while (nextChar() && currentChar != ':') {
            builder.append(currentChar)
        }
        val attr = builder.toString()
        builder.clear()
        while (nextChar() && currentChar != ']') {
            builder.append(currentChar)
        }
        val value = builder.toString()
        println("$attr: $value")
        when (attr) {
            "ti" -> lrc.title = value
            "ar" -> lrc.artist = value
            "al" -> lrc.album = value
            "by" -> lrc.by = value
            "offset" -> lrc.offset = value.toLong(10)
        }

    }

    private fun handleTimeTag(): Long {
        var minute = 0
        var seconds = 0
        var milliseconds = 0
        var i = 0
        val builder = StringBuilder(2)
        while (nextChar() && currentChar != ':') {
            builder.append(currentChar)
        }
        minute = builder.toString().toInt(10)
        builder.clear()
        while (nextChar() && currentChar != '.') {
            builder.append(currentChar)
        }
        seconds = builder.toString().toInt(10)
        builder.clear()
        while (nextChar() && currentChar != ']') {
            builder.append(currentChar)
        }
        milliseconds = builder.toString().toInt(10)
//        Log.e("LrcParser", "handleTimeTag: $minute:$seconds.$milliseconds = ")
        return (milliseconds + seconds * 1000 + minute * 60 * 1000).toLong()
    }

    private fun nextChar(): Boolean {
        if (index >= lrcContent.length) {
            return false
        }
        currentChar = lrcContent[index++]
        return true
    }

    private fun previousChar(): Boolean {
        if (index < 0) {
            return false
        }
        currentChar = lrcContent[--index]
        return true
    }

}