package com.simple.player.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase

object SQLiteDatabaseHelper {

    const val TABLE_SONG = "song"

    const val TABLE_PLAYLISTS = "playlist"

    const val TABLE_SONG_IN_LIST = "song_in_list"

    const val TABLE_PLAY_HISTORY = "play_history"

    const val TABLE_SCAN_CONFIG = "scan_config"

    val database: SQLiteDatabase
        get() {
            return helper.writableDatabase
        }

    private lateinit var helper: SQLiteHelper

    fun forContext(context: Context) {
        helper = SQLiteHelper(context)
    }

    fun close() {
        helper.close()
        SQLiteDatabase.releaseMemory()
    }

}