package com.simple.player.event

import android.util.Log
import androidx.compose.material.Colors
import java.util.concurrent.CopyOnWriteArrayList

object MusicEvent2 {

    private const val TAG = "MusicEvent2"

    private val events = CopyOnWriteArrayList<MusicEventListener>()

    private inline fun loopList(onLoop: (MusicEventListener) -> Unit) {
        for (e in events) {
            onLoop(e)
        }
    }

    fun register(scope: MusicEventListener) {
        events += scope
        Log.d(TAG, "${scope.javaClass.simpleName} registered, has ${events.size}")

    }

    fun unregister(scope: MusicEventListener) {
        val isRemoved = events.remove(scope)
        Log.d(TAG, if (isRemoved) "${scope.javaClass.simpleName} removed, remain ${events.size}" else "${scope.javaClass.simpleName} removed failed, remain ${events.size}")
    }

    fun fireOnMusicPlay() {
        loopList {
            it.onMusicPlay()
        }
    }

    fun fireOnMusicPause() {
        loopList {
            it.onMusicPause()
        }
    }

    fun fireOnSongRemovedFromList(songId: Long, listId: Long) {
        loopList {
            it.onSongRemovedFromList(songId, listId)
        }
    }

    fun fireOnSongAddToList(songId: Long, listId: Long) {
        loopList {
            it.onSongAddToList(songId, listId)
        }
    }

    fun fireOnSongChanged(newSongId: Long) {
        loopList {
            it.onSongChanged(newSongId)
        }
    }

    fun fireOnPlayModeChanged(oldMode: Int, newMode: Int) {
        loopList {
            it.onPlayModeChanged(oldMode, newMode)
        }
    }

    fun fireOnPlaylistCreated(listId: Long) {
        loopList {
            it.onPlaylistCreated(listId)
        }
    }

    fun fireOnPlaylistRenamed(listId: Long, newName: String) {
        loopList {
            it.onPlaylistRenamed(listId, newName)
        }
    }

    fun fireOnPlaylistDeleted(listId: Long) {
        loopList {
            it.onPlaylistDeleted(listId)
        }
    }

    fun fireOnHistoryChanged(newSongId: Long) {
        loopList {
            it.onHistoryChanged(newSongId)
        }
    }

    fun fireOnHeadImageVisibilityChanged(visible: Boolean) {
        loopList {
            it.onHeadImageVisibilityChanged(visible)
        }
    }

    fun fireOnBottomPlayerBarStyleChanged(style: String) {
        loopList {
            it.onBottomPlayerBarStyleChanged(style)
        }
    }

    fun fireOnSongsAddToList(songIds: LongArray, listId: Long) {
        loopList {
            it.onSongsAddToList(songIds, listId)
        }
    }

    fun fireOnSongsRemovedFromList(songIds: LongArray, listId: Long) {
        loopList {
            it.onSongsRemovedFromList(songIds, listId)
        }
    }

    fun fireOnPlaylistInitialized() {
        loopList {
            it.onPlaylistInitialized()
        }
    }

    fun fireOnPlayingPlaylistChanged(listId: Long) {
        loopList {
            it.onPlayingPlaylistChanged(listId)
        }
    }

    fun fireOnThemeChanged(newColors: Colors) {
        loopList {
            it.onThemeChanged(newColors = newColors)
        }
    }

    fun fireOnPlayerInitialized() {
        loopList {
            it.onPlaylistInitialized()
        }
    }

    fun close() {
        events.clear()
    }

}

