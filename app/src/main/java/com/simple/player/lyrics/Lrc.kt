package com.simple.player.lyrics

class Lrc {

    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var offset: Long = 0
    var by: String? = null
    var id: String? = null
    var total: Long = 0

    var lrcLineList = ArrayList<LyricsWord>()

    fun addLine(time: Long, content: String) {
        lrcLineList += LyricsWord().apply {
            startTime = time
            this.content = content
            this.offset = this@Lrc.offset
        }
    }

    fun addLine(word: LyricsWord) {
        lrcLineList += word.apply {
            offset = this@Lrc.offset
        }
    }

    override fun toString(): String {
        return "Lrc(title=$title, artist=$artist, album=$album, offset=$offset, by=$by, lrcLineList=$lrcLineList)"
    }

}