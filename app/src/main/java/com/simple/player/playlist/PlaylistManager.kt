package com.simple.player.playlist

import android.util.Log
import android.util.LongSparseArray
import androidx.core.util.forEach
import com.simple.player.database.PlaylistDao
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.model.Song
import com.simple.player.database.SongDao
import com.simple.player.database.SongInListDao
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.util.AppConfigure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import java.util.HashMap

object PlaylistManager :MusicEventListener {

    private const val TAG = "PlaylistManager"
    const val FAVORITE_LIST = "_favorite_"
    const val LOCAL_LIST = "_local_"

    const val LOCAL_LIST_ID = 2L
    const val FAVORITE_LIST_ID = 1L

    private var listMap: HashMap<String, AbsPlaylist> = HashMap()
    lateinit var localPlaylist: Playlist
        private set
    lateinit var favoriteList: Playlist
        private set

    var hasInitialed = false
        private set

    /**
     * 可复用的列表对象
     */
    private var bufferList = ArrayList<AbsPlaylist>()

    private val songMap = LongSparseArray<Song>()

    init {
        MusicEvent2.register(this)
    }

    fun load(): Boolean {
        if (hasInitialed) {
            return true
        }
        songMap.clear()
        val database = SQLiteDatabaseHelper.database
        val map = songMap
        val songCursor = database.rawQuery("select * from song;", null)
        songCursor.moveToFirst()
        if (songCursor.count <= 0) {
            songCursor.close()
            return false
        }
        do {
            with (songCursor) {
                val id = getLong(getColumnIndexOrThrow(SongDao.ID))
                val song = Song(id).apply {
                    this.path = getString(getColumnIndexOrThrow(SongDao.PATH))
                    this.title = getString(getColumnIndexOrThrow(SongDao.TITLE))
                    this.bitrate = getInt(getColumnIndexOrThrow(SongDao.BITRATE))
                    this.type  = getString(getColumnIndexOrThrow(SongDao.TYPE))
                    this.artist = getString(getColumnIndexOrThrow(SongDao.ARTIST))
                }
                map.put(id, song)
            }
        } while (songCursor.moveToNext())
        songCursor.close()
        if (!SongInListDao.has(LOCAL_LIST_ID)) {
            songMap.forEach { key, _ ->
                SongInListDao.insert(LOCAL_LIST_ID, key)
            }
        }
        PlaylistDao.queryAll { id, name, description ->
            val playlist = Playlist(name)
            playlist.id = id
            playlist.description = description
            val idList = SongInListDao.queryAll(id)
            for (idElement in idList) {
                playlist += songMap[idElement]
            }
            listMap[playlist.name] = playlist
            when (id) {
                LOCAL_LIST_ID -> localPlaylist = playlist
                FAVORITE_LIST_ID -> favoriteList = playlist
            }
        }
        hasInitialed = true
        MusicEvent2.fireOnPlaylistInitialized()
        return true
    }

    fun create(name: String, desc: String = ""): Playlist? {
        if (name == LOCAL_LIST || name == FAVORITE_LIST) {
            return null
        }
        val success = PlaylistDao.insertPlaylist(
            name = name,
            description = desc
        )
        if (success) {
            Log.e(TAG, "create list filed: $name")
            return null
        }
        val list = Playlist(name)
        list.description = desc
        list.id = PlaylistDao.queryIdByName(name = name)
        listMap[name] = list
        MusicEvent2.fireOnPlaylistCreated(name)
        return list
    }

    fun delete(name: String): Boolean {
        if (name == LOCAL_LIST && name == FAVORITE_LIST) {
            return false
        }
        val list = listMap[name]
        list ?: return true
        val success = PlaylistDao.deletePlaylist(list.id)
        if (!success) {
            Log.e(TAG, "delete - the playlist not found in table playlist, name = $name")
            return false
        }
        SongInListDao.delete(listId = list.id)
        val isDone2 = listMap.remove(name)
        if (isDone2 == null) {
            Log.e(TAG, "delete - the playlist not found in map, name = $name")
            return false
        }
        MusicEvent2.fireOnPlaylistDeleted(name)
        return true
    }

    fun rename(oldName: String, newName: String) {
        val list = getList(oldName) ?: return
        list.name = newName
        val success = PlaylistDao.updatePlaylist(
            listId = list.id,
            columnName = PlaylistDao.NAME_CODE,
            newValue = newName
        )
        if (!success) {
            Log.e(TAG, "rename - the playlist not found in table playlist, name = $oldName")
            return
        }
        val isDone = listMap.remove(oldName) != null
        if (!isDone) {
            Log.e(TAG, "rename - the playlist not found in map, name = $oldName")
            return
        }
        listMap[newName] = list
        // 更改设置中的当前播放列表，否则可能会造成应用无法启动的情况
        if (oldName == AppConfigure.Player.playlist) {
            AppConfigure.Player.playlist = newName
        }
        MusicEvent2.fireOnPlaylistRenamed(oldName, newName)
    }

    fun clear() {
        for (value in listMap.values) {
            value.clear()
        }
        listMap.clear()
        songMap.clear()
        MusicEvent2.unregister(this)
        hasInitialed = false
    }

    fun hasList(listName: String?): Boolean {
        return when (listName) {
            LOCAL_LIST, FAVORITE_LIST -> true
            else -> listMap.containsKey(listName)
        }
    }

    /**
     * 根据列表名称获取播放列表对象，包括本地列表和我喜欢列表，如果名字为 null 或找不到该名字的列表时返回 null
     * @param name 播放列表的名称
     * @return 名字对应播放列表对象
     */
    fun getList(name: String): AbsPlaylist? {
        return listMap[name]
    }

    fun getList(id: Long): AbsPlaylist? {
        for (entry in listMap) {
            if (entry.value.id == id) {
                return entry.value
            }
        }
        return null
    }

    /**
     * 获取所有的自定义列表
     * @return 所有的自定义列表对象构成的 ArrayList 对象
     */
    fun allCustomLists(): ArrayList<AbsPlaylist> {
        val list = bufferList
        list.clear()
        val values = listMap.values
        for (value in values) {
            if (value.name != LOCAL_LIST && value.name != FAVORITE_LIST) {
                list.add(value)
            }
        }
        return list
    }

    /**
     * 获取所有的播放列表，包括自定义列表，本地列表和我喜欢的列表
     * @return 所有列表构成的集合
     */
    fun allPlaylists(): MutableList<AbsPlaylist> {
        val list = bufferList
        list.clear()
        for (value in listMap.values) {
            list.add(value)
        }
        return list
    }

    fun addSong(listName: String, song: Song) {
        val list = listMap[listName]
        list ?: return
        list += song
        SongInListDao.insert(
            listId = list.id,
            songId = song.id
        )
//        val contentValues = ContentValues().apply {
//            put("list_id", list.id)
//            put("song_id", song.id)
//        }
//        SQLiteDatabaseHelper.database.insertOrThrow("song_in_list", null, contentValues)
        MusicEvent2.fireOnSongAddToList(song.id, listName)
    }

    fun addSongs(listName: String, songArray: Array<Song>) {
        val list = listMap[listName]
        if (list == null)
            Log.e(TAG, "addSongs: playlist not found name=$listName")
        list ?: return
//        val database = SQLiteDatabaseHelper.database
//        database.beginTransaction()
//        for (song in songArray) {
//            val contentValues = ContentValues()
//            with (contentValues) {
//                put("list_id", list.id)
//                put("song_id", song.id)
//            }
//            val id = SQLiteDatabaseHelper.database.insertOrThrow("song_in_list", null, contentValues)
//            if (id == -1L) {
//                Log.e(TAG, "addSongs: add failed song id=${song.id}, title=${song.title}, artist=${song.artist}")
//            }
//        }
//        database.setTransactionSuccessful()
//        database.endTransaction()
        for (song in songArray) {
            val success = SongInListDao.insert(
                listId = list.id,
                songId = song.id
            )
            if (!success) {
                Log.e(TAG, "addSongs: add failed song id=${song.id}, title=${song.title}, artist=${song.artist}")
            }
        }
        list += songArray
        MusicEvent2.fireOnSongsAddToList(LongArray(songArray.size) {i -> songArray[i].id }, listName)
    }

    fun removeSong(listName: String, song: Song) {
        val list = listMap[listName]
        list ?: return
        list -= song
        val success = SongInListDao.delete(listId = list.id, songId = song.id)
        if (!success) {
            Log.e(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${list.id}")
            return
        }
        MusicEvent2.fireOnSongRemovedFromList(song.id, listName)
    }

    fun removeSongs(listName: String, songArray: Array<Song>) {
        val list = listMap[listName]
        list ?: return
        for (song in songArray) {
            val success = SongInListDao.delete(listId = list.id, songId = song.id)
            if (!success) {
                Log.e(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${list.id}, list_name = ${list.name}")
                return
            }
        }
        list -= songArray
        MusicEvent2.fireOnSongsRemovedFromList(LongArray(songArray.size) {i -> songArray[i].id }, listName)
    }

    fun getHistoryList(): AbsPlaylist {
        val list = HistoryListManager.queryHistoryList()
        val playlist = Playlist("_history_")
        for (pair in list) {
            playlist += pair.first
        }
        list.clear()
        return playlist
    }

    override fun onSongChanged(newSongId: Long) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                HistoryListManager.addHistory(newSongId)
                MusicEvent2.fireOnHistoryChanged(newSongId)
            }
        }
    }

}