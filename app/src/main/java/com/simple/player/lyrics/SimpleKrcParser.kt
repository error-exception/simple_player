package com.simple.player.lyrics

import com.simple.player.decode.KRCDecoder
import java.io.InputStream

class SimpleKrcParser {

    private var content = ""
    private var index = 0
    private var currentChar = ' '
    private val lrc = Lrc()

    fun parse(input: InputStream): Lrc {
        content = KRCDecoder.INSTANCE.decode(input)
        while (nextChar()) {
            if (currentChar == '[') {
                if (!nextChar())
                    break
                if (isAlpha()) {
                    handleMetaDataTag()
                } else {
                    handleLyricsLine()
                }
            }
        }
        return lrc
    }

    private fun handleLyricsLine() {
        val builder = StringBuilder(8)
        do {
            builder.append(currentChar)
        } while (nextChar() && currentChar != ',')
        val startTime = builder.toString().toLong(10)
        builder.clear()
        while (nextChar() && currentChar != ']') {
            builder.append(currentChar)
        }
        val duration = builder.toString().toLong(10)
        builder.clear()
        var skip = false
        while (nextChar() && currentChar != '[') {
            if (currentChar == '<') {
                skip = true
                continue
            }
            if (currentChar == '>') {
                skip = false
                continue
            }
            if (skip) {
                continue
            }
            builder.append(currentChar)
        }
        previousChar()
        val word = LyricsWord()
        word.startTime = startTime
        word.duration = duration
        word.content = builder.trim().toString()
        lrc.addLine(word = word)
    }

    private fun handleMetaDataTag() {
        val builder = StringBuilder(5)
        do {
            builder.append(currentChar)
        } while (nextChar() && currentChar != ':')
        val tag = builder.toString()
        builder.clear()
        while (nextChar() && currentChar != ']') {
            builder.append(currentChar)
        }
        val value = builder.toString()
        when (tag) {
            "id" -> lrc.id = value
            "ar" -> lrc.artist = value
            "ti" -> lrc.title = value
            "al" -> lrc.album = value
            "by" -> lrc.by = value
            "total" -> lrc.total = if (value.isEmpty()) 0L else value.toLong()
        }
    }

    private fun isAlpha(): Boolean = currentChar in 'a'..'z' || currentChar in 'A'..'Z'

    private fun nextChar(): Boolean {
        if (index >= content.length) {
            return false
        }
        currentChar = content[index++]
        return true;
    }

    private fun previousChar(): Boolean {
        if (index < 0) {
            return false
        }
        currentChar = content[--index]
        return true
    }

}