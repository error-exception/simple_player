package com.simple.player.model

class Song(songId: Long){

    lateinit var title: String
    lateinit var artist: String
    lateinit var type: String
    var bitrate = 0
    var isChecked = false
    val id = songId
    lateinit var path: String
    var isPlaying = false

    constructor() : this(-1)

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other === this) {
            return true
        }
        if (other is Song) {
            return this.id == other.id
        }
        return false
    }

    override fun toString(): String {
        return """{ "title":"$title", "artist":"$artist", "id":$id, "uri":$path }"""
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + bitrate
        result = 31 * result + isChecked.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + isPlaying.hashCode()
        return result
    }

}