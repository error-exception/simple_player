package com.simple.player.playlist

object TempleId {

    private var templeSongId = -1L
    private var templeListId = -1L

    fun nextTempleSongId(): Long = templeSongId--

    fun nextTempleListId(): Long = templeListId--

}