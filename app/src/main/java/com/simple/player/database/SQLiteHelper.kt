package com.simple.player.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import com.simple.player.playlist.PlaylistManager
import com.simple.player.util.AppConfigure
import com.simple.player.util.StringUtils

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, "mainPlayer.db", null, 11) {
    override fun onCreate(p1: SQLiteDatabase) {
        with(p1) {
            execSQL("""
                CREATE TABLE song(
                    id bigint PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    artist TEXT NOT NULL,
                    type TEXT NOT NULL,
                    bitrate TEXT NOT NULL,
                    path TEXT NOT NULL);
            """.trimIndent())
            execSQL("""create table playlist(
                id integer primary key autoincrement not null,
                name_code text not null,
                description text
                );"""
            )
            execSQL("""insert into playlist values (1, "${StringUtils.stringToCode(PlaylistManager.FAVORITE_LIST)}", "")""")
            execSQL("""insert into playlist values (2, "${StringUtils.stringToCode(PlaylistManager.LOCAL_LIST)}", "")""")
            execSQL("""create table song_in_list( 
                list_id int not null,
                song_id int not null
            );""")
            execSQL("""
                create table play_history(
                    id bigint not null,
                    play_time bigint not null
                );
            """.trimIndent())
            execSQL("""
                create table id_path(
                    id integer primary key autoincrement not null,
                    uri text not null,
                    valid int default 1
                );
            """.trimIndent())
            execSQL("""
                create table scan_config(
                    id integer primary key autoincrement not null,
                    value text not null,
                    type int not null, --1. extension name 2. exclude path 3. include path
                    is_valid int not null --1. valid 0. invalid 
                );
            """.trimIndent())
            execSQL("""insert into scan_config (value, type, is_valid) values ("mp3", 1, 1)""")
            execSQL("""insert into scan_config (value, type, is_valid) values ("flac", 1, 1)""")
            execSQL("""insert into scan_config (value, type, is_valid) values ("/storage/emulated/0/Android", 2, 1)""")
//            execSQL("""
//                create table config(
//                    _key text not null,
//                    _value text default ''
//                );
//            """.trimIndent())
        }
    }
    override fun onUpgrade(p1: SQLiteDatabase, p2: Int, p3: Int) {
        p1.execSQL("""
                create table scan_config(
                    id integer primary key autoincrement not null,
                    value text not null,
                    type int not null, --1. extension name 2. exclude path 3. include path
                    is_valid int not null --1. valid 0. invalid 
                );
            """.trimIndent())
        val accessExtension = AppConfigure.Settings.accessExtension
        val excludePath = AppConfigure.Settings.excludePath
        val includePath = AppConfigure.Settings.includePath
        for (extension in accessExtension) {
            p1.insert(
                SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
                null,
                contentValuesOf(
                    ScanConfigDao.VALUE to extension,
                    ScanConfigDao.TYPE to ScanConfigDao.TYPE_EXTENSION_NAME,
                    ScanConfigDao.IS_VALID to 1
                )
            )
        }
        for (path in excludePath) {
            p1.insert(
                SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
                null,
                contentValuesOf(
                    ScanConfigDao.VALUE to path,
                    ScanConfigDao.TYPE to ScanConfigDao.TYPE_EXCLUDE_PATH,
                    ScanConfigDao.IS_VALID to 1
                )
            )
        }
        for (path in includePath) {
            p1.insert(
                SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
                null,
                contentValuesOf(
                    ScanConfigDao.VALUE to path,
                    ScanConfigDao.TYPE to ScanConfigDao.TYPE_INCLUDE_PATH,
                    ScanConfigDao.IS_VALID to 1
                )
            )
        }
    }

    /**
     * TODO: song info
     * main_color: for artwork
     * artwork_type:
     *     1 内嵌
     *     2 相邻
     *     3 指定目录
     *     4 无专辑
     * artwork_cache:
     *     1 有
     *     0 没有
     * artwork_src: 专辑图片 uri
     *
     */
}