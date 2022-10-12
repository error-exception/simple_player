package com.simple.player

import android.content.ComponentName
import android.os.Binder
import android.os.IBinder
import com.simple.player.activity.MusicInfo

/**
 * 该类完成所有与音乐有关事件的添加、移除和调用
 */
object MusicEventHandler {

    private const val SIZE = 6

    private val onPauseListenerList = ArrayList<MusicEvent.OnMusicPauseListener>(SIZE)
    private val onPlayListenerList = ArrayList<MusicEvent.OnMusicPlayListener>(SIZE)
    private val onSongRemovedFromListListenerList = ArrayList<MusicEvent.OnSongRemovedFromListListener>(SIZE)
    private val onSongAddToListListenerList = ArrayList<MusicEvent.OnSongAddToListListener>(SIZE)
    private val onSongChangedListenerList = ArrayList<MusicEvent.OnSongChangedListener>(SIZE)
    private val onPlayModeChangedListenerListener = ArrayList<MusicEvent.OnPlayModeChangedListener>(SIZE)
    private val onPlaylistRenamedListenerList = ArrayList<MusicEvent.OnPlaylistRenamedListener>(SIZE)
    private val onPlaylistCreatedListenerList = ArrayList<MusicEvent.OnPlaylistCreatedListener>(SIZE)
    private val onPlaylistDeletedListenerList = ArrayList<MusicEvent.OnPlaylistDeletedListener>(SIZE)
    private val onHistoryChangedListenerList = ArrayList<MusicEvent.OnHistoryChangedListener>(SIZE)
    private val onPlaylistInitialFinishedListenerList = ArrayList<MusicEvent.OnPlaylistInitialFinishedListener>(SIZE)

    fun register(any: Any) {
        if (any is MusicEvent.OnMusicPauseListener) {
            onPauseListenerList.add(any)
        }
        if (any is MusicEvent.OnMusicPlayListener) {
            onPlayListenerList.add(any)
        }
        if (any is MusicEvent.OnSongRemovedFromListListener) {
            onSongRemovedFromListListenerList.add(any)
        }
        if (any is MusicEvent.OnSongAddToListListener) {
            onSongAddToListListenerList.add(any)
        }
        if (any is MusicEvent.OnSongChangedListener) {
            onSongChangedListenerList.add(any)
        }
        if (any is MusicEvent.OnPlayModeChangedListener) {
            onPlayModeChangedListenerListener.add(any)
        }
        if (any is MusicEvent.OnPlaylistRenamedListener) {
            onPlaylistRenamedListenerList.add(any)
        }
        if (any is MusicEvent.OnPlaylistCreatedListener) {
            onPlaylistCreatedListenerList.add(any)
        }
        if (any is MusicEvent.OnPlaylistDeletedListener) {
            onPlaylistDeletedListenerList.add(any)
        }
        if (any is MusicEvent.OnHistoryChangedListener) {
            onHistoryChangedListenerList.add(any)
        }
        if (any is MusicEvent.OnPlaylistInitialFinishedListener) {
            onPlaylistInitialFinishedListenerList.add(any)
        }
    }

    fun unregister(any: Any) {
        if (any is MusicEvent.OnMusicPauseListener) {
            onPauseListenerList.remove(any)
        }
        if (any is MusicEvent.OnMusicPlayListener) {
            onPlayListenerList.remove(any)
        }
        if (any is MusicEvent.OnSongRemovedFromListListener) {
            onSongRemovedFromListListenerList.remove(any)
        }
        if (any is MusicEvent.OnSongAddToListListener) {
            onSongAddToListListenerList.remove(any)
        }
        if (any is MusicEvent.OnSongChangedListener) {
            onSongChangedListenerList.remove(any)
        }
        if (any is MusicEvent.OnPlayModeChangedListener) {
            onPlayModeChangedListenerListener.remove(any)
        }
        if (any is MusicEvent.OnPlaylistRenamedListener) {
            onPlaylistRenamedListenerList.remove(any)
        }
        if (any is MusicEvent.OnPlaylistCreatedListener) {
            onPlaylistCreatedListenerList.remove(any)
        }
        if (any is MusicEvent.OnPlaylistDeletedListener) {
            onPlaylistDeletedListenerList.remove(any)
        }
        if (any is MusicEvent.OnHistoryChangedListener) {
            onHistoryChangedListenerList.remove(any)
        }
        if (any is MusicEvent.OnPlaylistInitialFinishedListener) {
            onPlaylistInitialFinishedListenerList.remove(any)
        }
    }

    fun executeOnPlaylistInitialFinishedListener() {
        for (listener in onPlaylistInitialFinishedListenerList) {
            listener.onPlaylistInitialFinished()
        }
    }

    fun executeOnPauseListener() {
        for (listener in onPauseListenerList) {
            listener.onMusicPause()
        }
    }

    fun executeOnPlayListener() {
        for (listener in onPlayListenerList) {
            listener.onMusicPlay()
        }
    }

    fun executeOnSongRemovedFromListListener(songId: Long, listName: String) {
        for (listener in onSongRemovedFromListListenerList) {
            listener.onSongRemovedFromList(songId, listName)
        }
    }

    fun executeOnSongAddToListListener(songId: Long, listName: String) {
        for (listener in onSongAddToListListenerList) {
            listener.onSongAddToList(songId, listName)
        }
    }

    fun executeOnSongChangedListener(songId: Long) {
        for (listener in onSongChangedListenerList) {
            listener.onSongChanged(songId)
        }
    }

    fun executeOnPlayModeChangedListener(oldMode: Int, newMode: Int) {
        for (listener in onPlayModeChangedListenerListener) {
            listener.onPlayModeChanged(oldMode, newMode)
        }
    }

    fun executeOnPlaylistRenamedListener(oldName: String, newName: String) {
        for (listener in onPlaylistRenamedListenerList) {
            listener.onPlaylistRenamed(oldName, newName)
        }
    }

    fun executeOnPlaylistCreatedListener(listName: String) {
        for (listener in onPlaylistCreatedListenerList) {
            listener.onPlaylistCreated(listName)
        }
    }

    fun executeOnPlaylistDeletedListener(listName: String) {
        for (listener in onPlaylistDeletedListenerList) {
            listener.onPlaylistDeleted(listName)
        }
    }

    fun executeOnHistoryChangedListener(newSongId: Long) {
        for (listener in onHistoryChangedListenerList) {
            listener.onHistoryChangedListener(newSongId)
        }
    }



}