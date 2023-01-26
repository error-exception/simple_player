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

    fun fireOnSongRemovedFromList(songId: Long, listName: String) {
        loopList {
            it.onSongRemovedFromList(songId, listName)
        }
    }

    fun fireOnSongAddToList(songId: Long, listName: String) {
        loopList {
            it.onSongAddToList(songId, listName)
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

    fun fireOnPlaylistCreated(listName: String) {
        loopList {
            it.onPlaylistCreated(listName)
        }
    }

    fun fireOnPlaylistRenamed(oldName: String, newName: String) {
        loopList {
            it.onPlaylistRenamed(oldName, newName)
        }
    }

    fun fireOnPlaylistDeleted(listName: String) {
        loopList {
            it.onPlaylistDeleted(listName)
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

    fun fireOnSongsAddToList(songIds: LongArray, listName: String) {
        loopList {
            it.onSongsAddToList(songIds, listName)
        }
    }

    fun fireOnSongsRemovedFromList(songIds: LongArray, listName: String) {
        loopList {
            fireOnSongsRemovedFromList(songIds, listName)
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

