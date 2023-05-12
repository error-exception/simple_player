package com.simple.player.playlist

import androidx.core.content.contentValuesOf
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.model.Song
import kotlin.collections.ArrayList

internal object PlayHistoryManager {

    private const val TAG = "HistoryListManager"

    private val database = SQLiteDatabaseHelper.database

    private const val HISTORY_MAX = 200

    fun queryHistory(): ArrayList<Pair<Song, Long>> {
        val cursor = database.rawQuery("SELECT * FROM play_history;", null)
        if (cursor.count == 0) {
            cursor.close()
            return ArrayList()
        }
        cursor.moveToFirst()
        val list = ArrayList<Pair<Song, Long>>(cursor.count)
        do {
            val id = cursor.getLong(0)
            val playtime = cursor.getLong(1)
            val song = PlaylistManager.getLocalList().getSong(id)
            //TODO: 解决当歌曲移除时，还残留历史记录的问题
            if (song != null) {
                list.add(Pair(song, playtime))
            }
        } while (cursor.moveToNext())
        cursor.close()
        list.sortByDescending { pair -> pair.second }
        return list
    }

    fun addHistory(id: Long) {
        updateOrInsertPlayDatetime(id)
    }

    fun clearHistory() {
        database.delete("play_history", null, null)
    }

    private fun updateOrInsertPlayDatetime(id: Long) {
        val time = System.currentTimeMillis()
        val targetValue = Pair("play_time", time)
        val contentValues = contentValuesOf(targetValue)
        val count = database.update(
            SQLiteDatabaseHelper.TABLE_PLAY_HISTORY,
            contentValues,
            "id = ? and play_time < ?",
            arrayOf(id.toString(), time.toString()))

        if (count <= 0) {
            val cursor = database.rawQuery("select * from play_history;", null)
            if (cursor.count >= HISTORY_MAX) {
                database.execSQL("""
                    delete from play_history where play_time <= (select min(play_time) from play_history);
                """.trimIndent())
            }
            cursor.close()
            contentValues.put("id", id)
            database.insertOrThrow(SQLiteDatabaseHelper.TABLE_PLAY_HISTORY, null, contentValues)
        }
    }

}