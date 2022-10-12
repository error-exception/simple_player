package com.simple.player.database

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.StaleDataException


object SongDao {

    const val ID = "id"
    const val TITLE = "title"
    const val ARTIST = "artist"
    const val TYPE = "type"
    const val BITRATE = "bitrate"
    const val PATH = "path"

    private var database = SQLiteDatabaseHelper.database

    fun insertSong(values: ContentValues) {
        database.insertOrThrow("song", null, values)
    }

    fun deleteSong(id: Long) {
        database.delete("song", "id = ?", arrayOf(id.toString()))
    }

    fun deleteAll() {
        database.delete("song", null, null)
    }

    operator fun get(name: String, id: Long): String {
        var result = "-1"
        val c = database.rawQuery("SELECT * FROM song WHERE id = ?", arrayOf(id.toString()))
        if (c.moveToFirst()) {
            result = c.getString(c.getColumnIndexOrThrow(name))
        }
        c.close()
        return result
    }

    fun update(values: ContentValues) {
        database.update("song", values, null, null)
    }

    fun update(name: String, newValues: String, id: Long) {
        database.execSQL("UPDATE song SET $name = ? WHERE id = ?;", arrayOf(newValues, id.toString()))
    }
}
