package com.simple.player.playlist

import android.util.Log
import android.util.LongSparseArray
import androidx.core.util.containsKey
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
import kotlin.collections.ArrayList

object PlaylistManager :MusicEventListener {

    private const val TAG = "PlaylistManager"
    const val FAVORITE_LIST = "_favorite_"
    const val LOCAL_LIST = "_local_"

    const val LOCAL_LIST_ID = 2L
    const val FAVORITE_LIST_ID = 1L

//    private var listMap: HashMap<String, AbsPlaylist> = HashMap()
    private var listMap = LongSparseArray<SongList>()
//    lateinit var localPlaylist: Playlist
//        private set
//    lateinit var favoriteList: Playlist
//        private set

    private lateinit var localList: SongList
    private lateinit var favoriteList: SongList

    var hasInitialed = false
        private set

    /**
     * 可复用的列表对象
     */
    private var bufferList = ArrayList<SongList>()

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
                    this.uri = getString(getColumnIndexOrThrow(SongDao.PATH))
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
            val songList = SongList(id).apply {
                this.name = name
                this.description = description
            }
//            val playlist = Playlist(name)
//            playlist.id = id
//            playlist.description = description
            val idList = SongInListDao.queryAll(id)
            for (idElement in idList) {
//                playlist += songMap[idElement]
                songList.addSong(songMap[idElement])
            }
//            listMap[playlist.name] = playlist
            listMap.put(songList.getId(), songList)
            when (id) {
                LOCAL_LIST_ID -> localList = songList
                FAVORITE_LIST_ID -> favoriteList = songList
            }
        }
        hasInitialed = true
        MusicEvent2.fireOnPlaylistInitialized()
        return true
    }

    fun getLocalList(): SongList = localList

    fun getFavoriteList(): SongList = favoriteList

    fun create(name: String, desc: String = ""): SongList? {
        if (name == LOCAL_LIST || name == FAVORITE_LIST) {
            return null
        }
        val success = PlaylistDao.insertPlaylist(
            name = name,
            description = desc
        )
        if (!success) {
            Log.e(TAG, "create list filed: $name")
            return null
        }
//        val list = Playlist(name)
//        list.description = desc
//        list.id = PlaylistDao.queryIdByName(name = name)
//        listMap[name] = list
        val list = SongList(
            id = PlaylistDao.queryIdByName(name = name)
        ).apply {
            this.description = desc
            this.name = name
        }
        MusicEvent2.fireOnPlaylistCreated(list.getId())
        return list
    }

    private fun isInternalList(id: Long): Boolean =
        id == LOCAL_LIST_ID || id == FAVORITE_LIST_ID

    fun delete(listId: Long): Boolean {
        if (isInternalList(listId)) {
            return false
        }
        val list = listMap.get(listId) ?: return true
        val success = PlaylistDao.deletePlaylist(listId = listId)
        if (!success) {
            Log.i(TAG, "delete - the playlist not found in table playlist, name = ${list.name}")
            return false
        }
        SongInListDao.delete(listId)
        listMap.remove(listId)
        MusicEvent2.fireOnPlaylistDeleted(listId)
        return true
    }

//    fun delete(name: String): Boolean {
//        if (name == LOCAL_LIST || name == FAVORITE_LIST) {
//            return false
//        }
//        val list = listMap[name]
//        list ?: return true
//        val success = PlaylistDao.deletePlaylist(list.id)
//        if (!success) {
//            Log.i(TAG, "delete - the playlist not found in table playlist, name = $name")
//            return false
//        }
//        SongInListDao.delete(listId = list.id)
//        val isDone2 = listMap.remove(name)
//        if (isDone2 == null) {
//            Log.i(TAG, "delete - the playlist not found in map, name = $name")
//            return false
//        }
//        MusicEvent2.fireOnPlaylistDeleted(name)
//        return true
//    }

    fun rename(listId: Long, newName: String) {
        val list = listMap.get(listId) ?: return
        val oldName = list.name
        val success = PlaylistDao.updatePlaylist(
            listId = listId,
            columnName = PlaylistDao.NAME_CODE,
            newValue = newName
        )
        if (!success) {
            Log.i(TAG, "rename - the playlist not found in table playlist, name = ${oldName}")
            return
        }
        list.name = newName
        // 更改设置中的当前播放列表，否则可能会造成应用无法启动的情况
        if (listId == AppConfigure.Player.songListId) {
            AppConfigure.Player.songListId = listId
        }
        MusicEvent2.fireOnPlaylistRenamed(listId, newName)
    }

//    fun rename(oldName: String, newName: String) {
//        val list = getList(oldName) ?: return
//        list.name = newName
//        val success = PlaylistDao.updatePlaylist(
//            listId = list.id,
//            columnName = PlaylistDao.NAME_CODE,
//            newValue = newName
//        )
//        if (!success) {
//            Log.i(TAG, "rename - the playlist not found in table playlist, name = $oldName")
//            return
//        }
//        val isDone = listMap.remove(oldName) != null
//        if (!isDone) {
//            Log.i(TAG, "rename - the playlist not found in map, name = $oldName")
//            return
//        }
//        listMap[newName] = list
//        // 更改设置中的当前播放列表，否则可能会造成应用无法启动的情况
//        if (oldName == AppConfigure.Player.playlist) {
//            AppConfigure.Player.playlist = newName
//        }
//        MusicEvent2.fireOnPlaylistRenamed(oldName, newName)
//    }

    fun clear() {
        listMap.forEach { _, value ->
            value.clear()
        }
//        for (value in listMap.values) {
//            value.clear()
//        }
        listMap.clear()
        songMap.clear()
        MusicEvent2.unregister(this)
        hasInitialed = false
    }

    fun hasList(listId: Long): Boolean = listMap.containsKey(listId)

    fun hasList(listName: String): Boolean {
        var has = false
        listMap.forEach { _, value ->
            has = value.name == listName
        }
        return has
    }

//    fun hasList(listName: String?): Boolean {
//        return when (listName) {
//            LOCAL_LIST, FAVORITE_LIST -> true
//            else -> listMap.containsKey(listName)
//        }
//    }

//    /**
//     * 根据列表名称获取播放列表对象，包括本地列表和我喜欢列表，如果名字为 null 或找不到该名字的列表时返回 null
//     * @param name 播放列表的名称
//     * @return 名字对应播放列表对象
//     */
//    fun getList(name: String): AbsPlaylist? {
//        return listMap[name]
//    }
//

//    fun getList(id: Long): AbsPlaylist? {
//        for (entry in listMap) {
//            if (entry.value.id == id) {
//                return entry.value
//            }
//        }
//        return null
//    }

    fun getSongList(listId: Long): SongList? = listMap.get(listId)

//    /**
//     * 获取所有的自定义列表
//     * @return 所有的自定义列表对象构成的 ArrayList 对象
//     */
//    fun allCustomLists(): ArrayList<AbsPlaylist> {
//        val list = bufferList
//        list.clear()
//        val values = listMap.values
//        for (value in values) {
//            if (value.name != LOCAL_LIST && value.name != FAVORITE_LIST) {
//                list.add(value)
//            }
//        }
//        return list
//    }

    fun getAllExternalSongLists(): ArrayList<SongList> {
        val list = bufferList.apply { this.clear() }
        listMap.forEach { key, value ->
            if (isInternalList(key)) {
                return@forEach
            }
            list.add(value)
        }
        return list
    }

//    /**
//     * 获取所有的播放列表，包括自定义列表，本地列表和我喜欢的列表
//     * @return 所有列表构成的集合
//     */
//    fun allPlaylists(): MutableList<AbsPlaylist> {
//        val list = bufferList
//        list.clear()
//        for (value in listMap.values) {
//            list.add(value)
//        }
//        return list
//    }

    fun addSong(listId: Long, song: Song) {
        listMap[listId]?.let {
            it.addSong(song)
            SongInListDao.insert(
                listId = listId,
                songId = song.id
            )
            MusicEvent2.fireOnSongAddToList(song.id, listId)
        }
    }

//    fun addSong(listName: String, song: Song) {
//        val list = listMap[listName]
//        list ?: return
//        list += song
//        SongInListDao.insert(
//            listId = list.id,
//            songId = song.id
//        )
////        val contentValues = ContentValues().apply {
////            put("list_id", list.id)
////            put("song_id", song.id)
////        }
////        SQLiteDatabaseHelper.database.insertOrThrow("song_in_list", null, contentValues)
//        MusicEvent2.fireOnSongAddToList(song.id, listName)
//    }

    fun addSongs(listId: Long, songArray: Array<Song>) {
        listMap[listId]?.let {
            for (song in songArray) {
                val success = SongInListDao.insert(
                    listId = listId,
                    songId = song.id
                )
                if (!success) {
                    Log.e(TAG, "addSongs: add failed song id=${song.id}, title=${song.title}, artist=${song.artist}")
                }
                it.addSong(song)
            }
            MusicEvent2.fireOnSongsAddToList(LongArray(songArray.size) {i -> songArray[i].id }, listId)
        }
    }

//    fun addSongs(listName: String, songArray: Array<Song>) {
//        val list = listMap[listName]
//        if (list == null)
//            Log.e(TAG, "addSongs: playlist not found name=$listName")
//        list ?: return
////        val database = SQLiteDatabaseHelper.database
////        database.beginTransaction()
////        for (song in songArray) {
////            val contentValues = ContentValues()
////            with (contentValues) {
////                put("list_id", list.id)
////                put("song_id", song.id)
////            }
////            val id = SQLiteDatabaseHelper.database.insertOrThrow("song_in_list", null, contentValues)
////            if (id == -1L) {
////                Log.e(TAG, "addSongs: add failed song id=${song.id}, title=${song.title}, artist=${song.artist}")
////            }
////        }
////        database.setTransactionSuccessful()
////        database.endTransaction()
//        for (song in songArray) {
//            val success = SongInListDao.insert(
//                listId = list.id,
//                songId = song.id
//            )
//            if (!success) {
//                Log.e(TAG, "addSongs: add failed song id=${song.id}, title=${song.title}, artist=${song.artist}")
//            }
//        }
//        list += songArray
//        MusicEvent2.fireOnSongsAddToList(LongArray(songArray.size) {i -> songArray[i].id }, listName)
//    }

    fun removeSong(listId: Long, song: Song) {
        listMap[listId]?.let {
            it.removeBy(song.id)
            val success = SongInListDao.delete(listId = listId, songId = song.id)
            if (!success) {
                Log.i(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${listId}")
                return
            }
            MusicEvent2.fireOnSongRemovedFromList(song.id, listId)
        }
    }

//    fun removeSong(listName: String, song: Song) {
//        val list = listMap[listName]
//        list ?: return
//        list -= song
//        val success = SongInListDao.delete(listId = list.id, songId = song.id)
//        if (!success) {
//            Log.i(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${list.id}")
//            return
//        }
//        MusicEvent2.fireOnSongRemovedFromList(song.id, listName)
//    }

    fun removeSongs(listId: Long, songArray: Array<Song>) {
        listMap[listId]?.let {
            for (song in songArray) {
                val success = SongInListDao.delete(listId = listId, songId = song.id)
                if (!success) {
                    Log.i(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${listId}, list_name = ${it.name}")
                    return
                }
                it.removeBy(song.id)
            }
            MusicEvent2.fireOnSongsRemovedFromList(LongArray(songArray.size) {i -> songArray[i].id }, listId)
        }
    }

//    fun removeSongs(listName: String, songArray: Array<Song>) {
//        val list = listMap[listName]
//        list ?: return
//        for (song in songArray) {
//            val success = SongInListDao.delete(listId = list.id, songId = song.id)
//            if (!success) {
//                Log.i(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${list.id}, list_name = ${list.name}")
//                return
//            }
//        }
//        list -= songArray
//        MusicEvent2.fireOnSongsRemovedFromList(LongArray(songArray.size) {i -> songArray[i].id }, listName)
//    }

    fun getHistoryList(): AbsPlaylist {
        val list = PlayHistoryManager.queryHistory()
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
                PlayHistoryManager.addHistory(newSongId)
                MusicEvent2.fireOnHistoryChanged(newSongId)
            }
        }
    }

}