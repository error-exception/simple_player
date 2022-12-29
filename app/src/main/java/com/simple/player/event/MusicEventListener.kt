package com.simple.player.event

import androidx.compose.material.Colors

interface MusicEventListener {

    fun onMusicPlay() {}

    fun onMusicPause() {}

    fun onSongRemovedFromList(songId: Long, listName: String) {}

    fun onSongAddToList(songId: Long, listName: String) {}

    fun onSongChanged(newSongId: Long) {}

    fun onPlayModeChanged(oldMode: Int, newMode: Int) {}

    fun onPlaylistCreated(listName: String) {}

    fun onPlaylistRenamed(oldName: String, newName: String) {}

    fun onPlaylistDeleted(listName: String) {}

    fun onHistoryChanged(newSongId: Long) {}

    fun onHeadImageVisibilityChanged(visible: Boolean) {}

    fun onBottomPlayerBarStyleChanged(style: String) {}

    fun onSongsAddToList(songIds: LongArray, listName: String) {}

    fun onSongsRemovedFromList(songIds: LongArray, listName: String) {}

    fun onPlaylistInitialized() {}

    fun onPlayingPlaylistChanged(listId: Long) {}

    fun onThemeChanged(newColors: Colors) {}

}