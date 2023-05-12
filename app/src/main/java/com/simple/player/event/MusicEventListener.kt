package com.simple.player.event

import androidx.compose.material.Colors

interface MusicEventListener {

    fun onMusicPlay() {}

    fun onMusicPause() {}

    fun onSongRemovedFromList(songId: Long, listId: Long) {}

    fun onSongAddToList(songId: Long, listId: Long) {}

    fun onSongChanged(newSongId: Long) {}

    fun onPlayModeChanged(oldMode: Int, newMode: Int) {}

    fun onPlaylistCreated(listId: Long) {}

    fun onPlaylistRenamed(listId: Long, newName: String) {}

    fun onPlaylistDeleted(listId: Long) {}

    fun onHistoryChanged(newSongId: Long) {}

    fun onHeadImageVisibilityChanged(visible: Boolean) {}

    fun onBottomPlayerBarStyleChanged(style: String) {}

    fun onSongsAddToList(songIds: LongArray, listId: Long) {}

    fun onSongsRemovedFromList(songIds: LongArray, listId: Long) {}

    fun onPlaylistInitialized() {}

    fun onPlayingPlaylistChanged(listId: Long) {}

    fun onThemeChanged(newColors: Colors) {}

    fun onPlayerInitialized() {}

}