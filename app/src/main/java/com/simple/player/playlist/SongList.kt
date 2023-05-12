package com.simple.player.playlist

import android.util.LongSparseArray
import com.simple.player.model.Song

open class SongList(private val id: Long) {

    private val songList = ArrayList<Song>()
    private val songMap = LongSparseArray<Song>()

    val isTemple = id < 0

    var name: String = ""
    var description: String = ""

    fun getId(): Long = id

    fun addSong(song: Song) {
        songList.add(song)
        songMap.put(song.id, song)
    }

    fun getSong(songId: Long): Song? = songMap.get(songId)

    fun getSongAt(position: Int): Song {
        if (position < 0 || position >= songList.size) {
            throw IndexOutOfBoundsException()
        }
        return songList[position]
    }

    fun hasSong(id: Long): Boolean = getSong(id) != null

    fun removeBy(songId: Long) {
        val song = getSong(songId = songId) ?: return
        songList.remove(song)
        songMap.remove(songId)
    }

    fun removeAt(position: Int) {
        if (position < 0 || position >= songList.size) {
            throw IndexOutOfBoundsException()
        }
        val removedSong = songList.removeAt(position)
        songMap.remove(removedSong.id)
    }

    fun count(): Int = songList.size

    fun isEmpty(): Boolean = songList.isEmpty()

    fun isNotEmpty(): Boolean = songList.isNotEmpty()

    fun indexOf(song: Song): Int = songList.indexOf(song)

    fun indexOf(songId: Long): Int {
        val song = getSong(songId) ?: return -1
        return indexOf(song)
    }

    fun rawList(): List<Song> = songList

    fun first(): Song = getSongAt(0)

    fun last(): Song = getSongAt(count() - 1)

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is SongList) {
            return other.getId() == getId()
        }
        return super.equals(other)
    }

    fun clear() {
        songMap.clear()
        songList.clear()
    }

}