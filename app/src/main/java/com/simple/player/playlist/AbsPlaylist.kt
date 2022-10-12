package com.simple.player.playlist

import com.simple.player.model.Song

abstract class AbsPlaylist{

    var name: String = ""
        internal set

    var description: String = ""

    var id: Long = 0L
        internal set

    abstract fun clear(): AbsPlaylist

    abstract fun hasSong(id: Long): Boolean

    abstract fun position(song: Song): Int

    abstract val songList: MutableList<Song>

    abstract val count: Int

    // 重载 [] 运算符，获取歌曲
    abstract operator fun get(position: Int): Song?

    abstract operator fun get(id: Long): Song?

    // 重载 += 运算符，添加
    internal abstract operator fun plusAssign(song: Song)

    internal abstract operator fun plusAssign(id: Long)

    internal abstract operator fun plusAssign(song: Array<Song>)

    // 重载 -= 运算符，移除歌曲
    internal abstract operator fun minusAssign(song: Song)

    internal abstract operator fun minusAssign(position: Int)

    internal abstract operator fun minusAssign(id: Long)

    internal abstract operator fun minusAssign(song: Array<Song>)

}