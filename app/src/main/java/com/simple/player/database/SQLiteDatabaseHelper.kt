package com.simple.player.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase

object SQLiteDatabaseHelper {

    const val TABLE_SONG = "song"

    const val TABLE_PLAYLISTS = "playlist"

    const val TABLE_SONG_IN_LIST = "song_in_list"

    const val TABLE_PLAY_HISTORY = "play_history"

    val database: SQLiteDatabase
        get() {
            return helper.writableDatabase
        }

    private lateinit var helper: SQLiteHelper

    fun forContext(context: Context) {
        helper = SQLiteHelper(context)
    }

    fun executeSQL(sql: String) {
        database.execSQL(sql)
    }

    fun close() {
        helper.close()
        SQLiteDatabase.releaseMemory()
    }

}