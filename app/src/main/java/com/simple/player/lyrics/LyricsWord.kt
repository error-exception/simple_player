package com.simple.player.lyrics

class LyricsWord {

    var startTime: Long = 0
    var duration: Long = 0
    var content: String = ""
    var offset: Long = 0

    override fun toString(): String {
        return "LyricsWord(startTime=$startTime, duration=$duration, content='$content', offset=$offset)"
    }

}