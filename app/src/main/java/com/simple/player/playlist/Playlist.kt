package com.simple.player.playlist

import android.util.LongSparseArray
import androidx.core.util.containsKey
import com.simple.player.model.Song

class Playlist(listName: String): AbsPlaylist() {

    var list: ArrayList<Song> = ArrayList()
        internal set

    private val map: LongSparseArray<Song> = LongSparseArray<Song>()

    init {
        name = listName
    }

    override operator fun plusAssign(song: Song) {
        if (!map.containsKey(song.id)) {
            map.put(song.id, song)
            list.add(song)
        }
    }

    override operator fun plusAssign(id: Long) {
        this += PlaylistManager.localPlaylist[id]!!
    }

    override operator fun plusAssign(song: Array<Song>) {
        for (son in song) {
            this += son
        }
    }

    override operator fun minusAssign(song: Song) {
        list.remove(song)
        map.remove(song.id)
    }

    override operator fun minusAssign(position: Int) {
        val song = list.removeAt(position)
        map.remove(song.id)
    }

    override operator fun minusAssign(id: Long) {
        val song = map.get(id) ?: return
        this -= song
    }

    override operator fun minusAssign(song: Array<Song>) {
        for (song1 in song) {
            list.remove(song1)
            map.remove(song1.id)
        }
    }

    override fun hasSong(id: Long): Boolean = map.containsKey(id)

    fun hasSong(song: Song?): Boolean = if (song == null) false else map.containsKey(song.id)

    override fun equals(other: Any?): Boolean {
        if (other is Playlist) {
            return this.name == other.name
        }
        return false
    }

    override fun clear(): Playlist {
        list.clear()
        map.clear()
        return this
    }

    override operator fun get(id: Long): Song? {
        return map.get(id)
    }

    override operator fun get(position: Int): Song = list[position]

    override fun position(song: Song): Int = list.indexOf(song)

    override fun hashCode(): Int {
        var result = list.hashCode()
        result = 31 * result + map.hashCode()
        result = 31 * result + songList.hashCode()
        result = 31 * result + count
        return result
    }

    override val songList: MutableList<Song>
        get() = list

    override val count: Int
        get() = list.size
}