package com.simple.player.playlist

import android.content.ContentValues
import android.util.Log
import android.util.LongSparseArray
import androidx.core.util.forEach
import com.simple.player.MusicEvent
import com.simple.player.MusicEventHandler
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.model.Song
import com.simple.player.database.SongDao
import com.simple.player.util.AppConfigure
import com.simple.player.util.ProgressHandler
import com.simple.player.util.StringUtil
import java.util.ArrayList
import java.util.HashMap

object PlaylistManager : MusicEvent.OnSongChangedListener{

    private const val TAG = "PlaylistManager"
    const val FAVORITE_LIST = "_favorite_"
    const val LOCAL_LIST = "_local_"

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

    fun load() {
        MusicEventHandler.register(this)
        val database = SQLiteDatabaseHelper.database
        val map = LongSparseArray<Song>()
        val songCursor = database.rawQuery("select * from song;", null)
        songCursor.moveToFirst()
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

        val playlistCursor = database.rawQuery("select * from playlist;", null)
        playlistCursor.moveToFirst()
        do {
            val listId = playlistCursor.getLong(playlistCursor.getColumnIndexOrThrow("id"))
            val nameCode = playlistCursor.getString(playlistCursor.getColumnIndexOrThrow("name_code"))
            val desc = playlistCursor.getString(playlistCursor.getColumnIndexOrThrow("description"))
            val playlist = Playlist(StringUtil.codeToString(nameCode))
            playlist.description = desc
            playlist.id = listId
            val playlistSongsCursor = database.rawQuery("select * from song_in_list where list_id = ?;", arrayOf(playlist.id.toString()))
            playlistSongsCursor.moveToFirst()
            if (playlist.name == LOCAL_LIST && playlistSongsCursor.count <= 0) {
                database.beginTransaction()
                map.forEach { key, _ ->
                    val contentValues = ContentValues().apply {
                        put("song_id", key)
                        put("list_id", playlist.id)
                    }
                    database.insertOrThrow("song_in_list", null, contentValues)
                    playlist += map.get(key)
                }
                database.setTransactionSuccessful()
                database.endTransaction()

            }
            if (playlistSongsCursor.count != 0) {
                do {
                    val id = playlistSongsCursor.getLong(playlistSongsCursor.getColumnIndexOrThrow("song_id"))
                    val song = map.get(id)
                    if (song != null) {
                        playlist += song
                    }
                } while (playlistSongsCursor.moveToNext())
            }
            playlistSongsCursor.close()
            listMap[playlist.name] = playlist
        } while (playlistCursor.moveToNext())
        playlistCursor.close()

        favoriteList = listMap[FAVORITE_LIST] as Playlist
        localPlaylist = listMap[LOCAL_LIST] as Playlist
        map.clear()
        hasInitialed = true
        MusicEventHandler.executeOnPlaylistInitialFinishedListener()
    }

    fun create(name: String, desc: String = ""): Playlist? {
        if (name == LOCAL_LIST || name == FAVORITE_LIST) {
            return null
        }
        val list = Playlist(name)
        list.description = desc
//        list.id = System.currentTimeMillis()
        val contentValues = ContentValues().apply {
//            put("id", list.id)
            put("name_code", StringUtil.stringToCode(list.name))
            put("description", list.description)
        }
        val code = SQLiteDatabaseHelper.database.insert("playlist", null, contentValues)
        if (code == -1L) {
            Log.e(TAG, "create list filed: ${list.name}")
            return null
        }
        listMap[name] = list
        MusicEventHandler.executeOnPlaylistCreatedListener(name)
        return list
    }

    fun delete(name: String): Boolean {
        if (name == LOCAL_LIST && name == FAVORITE_LIST) {
            return false
        }
        val list = listMap[name]
        list ?: return false
        val count = SQLiteDatabaseHelper.database.delete("playlist", "id = ?", arrayOf(list.id.toString()))
        if (count == 0) {
            Log.e(TAG, "delete - the playlist not found in table playlist, name = $name")
            return false
        }
        SQLiteDatabaseHelper.database.delete("song_in_list", "list_id = ?", arrayOf(list.id.toString()))
        val isDone2 = listMap.remove(name)
        if (isDone2 == null) {
            Log.e(TAG, "delete - the playlist not found in map, name = $name")
            return false
        }
        MusicEventHandler.executeOnPlaylistDeletedListener(name)
        return true
    }

    fun rename(oldName: String, newName: String) {
        val list = getList(oldName) ?: return
        list.name = newName
        val contentValues = ContentValues()
        contentValues.put("name_code", StringUtil.stringToCode(newName))
        val count = SQLiteDatabaseHelper.database.update("playlist", contentValues, "id = ?", arrayOf(list.id.toString()))
        if (count == 0) {
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
        MusicEventHandler.executeOnPlaylistRenamedListener(oldName, newName)
    }

    fun clear() {
        for (value in listMap.values) {
            value.clear()
        }
        listMap.clear()
        MusicEventHandler.unregister(this)
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
        val contentValues = ContentValues().apply {
            put("list_id", list.id)
            put("song_id", song.id)
        }
        SQLiteDatabaseHelper.database.insertOrThrow("song_in_list", null, contentValues)
        MusicEventHandler.executeOnSongAddToListListener(song.id, listName)
    }

    fun addSongs(listName: String, songArray: Array<Song>) {
        val list = listMap[listName]
        list ?: return
        val database = SQLiteDatabaseHelper.database
        database.beginTransaction()
        for (song in songArray) {
            val contentValues = ContentValues()
            with (contentValues) {
                put("list_id", list.id)
                put("song_id", song.id)
            }
            SQLiteDatabaseHelper.database.insertOrThrow("song_in_list", null, contentValues)
            MusicEventHandler.executeOnSongAddToListListener(song.id, listName)
        }
        database.setTransactionSuccessful()
        database.endTransaction()
        list += songArray
    }

    fun removeSong(listName: String, song: Song) {
        val list = listMap[listName]
        list ?: return
        list -= song
        val count = SQLiteDatabaseHelper.database.delete("song_in_list", "list_id = ? and song_id = ?", arrayOf(list.id.toString(), song.id.toString()))
        if (count == 0) {
            Log.e(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${list.id}")
            return
        }
        MusicEventHandler.executeOnSongRemovedFromListListener(song.id, listName)
    }

    fun removeSongs(listName: String, songArray: Array<Song>) {
        val list = listMap[listName]
        list ?: return
        val database = SQLiteDatabaseHelper.database
        database.beginTransaction()
        for (song in songArray) {
            val count = database.delete("song_in_list", "list_id = ? and song_id = ?", arrayOf(list.id.toString(), song.id.toString()))
            if (count == 0) {
                Log.e(TAG, "delete error in table song_in_list, song_id = ${song.id}, list_id = ${list.id}, list_name = ${list.name}")
                database.endTransaction()
                return
            }
            MusicEventHandler.executeOnSongRemovedFromListListener(song.id, listName)
        }
        database.setTransactionSuccessful()
        database.endTransaction()
        list -= songArray
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
        ProgressHandler.handle(handle = {
            HistoryListManager.addHistory(newSongId)
        }, after = {
            MusicEventHandler.executeOnHistoryChangedListener(newSongId)
        })
    }

}