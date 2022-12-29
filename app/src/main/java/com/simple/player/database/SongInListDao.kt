package com.simple.player.database

import androidx.core.content.contentValuesOf

object SongInListDao {

    const val LIST_ID = "list_id"
    const val SONG_ID = "song_id"
    const val TABLE_NAME = "song_in_list"

    fun insert(listId: Long, songId: Long): Boolean {
        return SQLiteDatabaseHelper.database.insert(
            TABLE_NAME,
            null,
            contentValuesOf(
                LIST_ID to listId,
                SONG_ID to songId
            )
        ) != -1L
    }

    fun delete(listId: Long, songId: Long): Boolean {
        return SQLiteDatabaseHelper.database.delete(
            TABLE_NAME,
            "list_id = ? and song_id = ?",
            arrayOf(listId.toString(), songId.toString())
        ) != 0
    }

    fun delete(listId: Long): Boolean {
        return SQLiteDatabaseHelper.database.delete(
            TABLE_NAME,
            "list_id = ?",
            arrayOf(listId.toString())
        ) != 0
    }

    fun queryAll(listId: Long): List<Long> {
        val cursor = SQLiteDatabaseHelper.database.rawQuery(
            "select song_id from song_in_list where list_id = ?;",
            arrayOf(listId.toString())
        )
        cursor.moveToFirst()
        if (cursor.count <= 0) {
            cursor.close()
            return emptyList()
        }
        val list = ArrayList<Long>()
        do {
            val id = cursor.getLong(
                cursor.getColumnIndexOrThrow(SONG_ID)
            )
            list += id
        } while (cursor.moveToNext())
        cursor.close()
        return list
    }

    fun has(listId: Long, songId: Long): Boolean {
        val cursor = SQLiteDatabaseHelper.database.rawQuery(
            "select * from song_in_list where list_id = ? and song_id = ?;",
            arrayOf(listId.toString(), songId.toString())
        )
        cursor.moveToFirst()
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    fun has(listId: Long): Boolean {
        val cursor = SQLiteDatabaseHelper.database.rawQuery(
            "select * from song_in_list where list_id = ?;",
            arrayOf(listId.toString())
        )
        cursor.moveToFirst()
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

}