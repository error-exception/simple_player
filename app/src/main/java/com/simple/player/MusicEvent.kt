package com.simple.player

import android.content.ComponentName
import android.os.IBinder

class MusicEvent {
    // 播放暂停事件
    interface OnMusicPauseListener {

        fun onMusicPause()

    }
    // 开始播放事件
    interface OnMusicPlayListener {

        fun onMusicPlay()

    }
    // 列表删除歌曲事件
    interface OnSongRemovedFromListListener {

        fun onSongRemovedFromList(songId: Long, listName: String)

    }
    // 列表添加歌曲事件
    interface OnSongAddToListListener {

        fun onSongAddToList(songId: Long, listName: String)

    }
    // 歌曲切换事件
    interface OnSongChangedListener {

        fun onSongChanged(newSongId: Long)

    }
    // 播放模式切换事件
    interface OnPlayModeChangedListener {

        fun onPlayModeChanged(oldMode: Int, newMode: Int)

    }
    // 歌曲完成播放事件
    interface OnSongCompleteListener
    // 歌曲播放出错事件
    interface OnPlayErrorListener {

        fun onPlayError(songId: Long, code: Int)

    }
    // 列表创建事件
    interface OnPlaylistCreatedListener {

        fun onPlaylistCreated(listName: String)

    }
    // 列表重命名事件
    interface OnPlaylistRenamedListener {

        fun onPlaylistRenamed(oldName: String, newName: String)

    }
    // 列表删除事件
    interface OnPlaylistDeletedListener {

        fun onPlaylistDeleted(listName: String)

    }
    // 历史播放改变事件
    interface OnHistoryChangedListener {

        fun onHistoryChangedListener(newSongId: Long)

    }
    // 列表封面改变事件
    interface OnPlaylistCoverChangedListener
    // 歌曲扫描事件
    interface OnSongScanningListener {

        fun onSongScanning()

    }
    // 歌曲重命名事件
    interface OnSongRenamedListener {

        fun onSongRenamed(oldName: String, newName: String)

    }

    // 播放列表初始化完毕事件
    interface OnPlaylistInitialFinishedListener {

        fun onPlaylistInitialFinished()

    }
}