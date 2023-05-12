package com.simple.player.model

import com.simple.json.annota.JSONIgnore

class Song(songId: Long){

    lateinit var title: String
    lateinit var artist: String
    lateinit var type: String

    @JSONIgnore
    lateinit var uri: String
    var bitrate = 0
    val id = songId

    constructor() : this(-1)

    fun isTemple(): Boolean = id < 0

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
        return """{ "title":"$title", "artist":"$artist", "id":$id, "uri":$uri }"""
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + bitrate
        result = 31 * result + id.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }

}