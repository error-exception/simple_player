package com.simple.player.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.simple.player.playlist.PlaylistManager
import com.simple.player.util.StringUtil

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, "mainPlayer.db", null, 9) {
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
            execSQL("""insert into playlist values (1, "${StringUtil.stringToCode(PlaylistManager.FAVORITE_LIST)}", "")""")
            execSQL("""insert into playlist values (2, "${StringUtil.stringToCode(PlaylistManager.LOCAL_LIST)}", "")""")
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
//            execSQL("""
//                create table config(
//                    _key text not null,
//                    _value text default ''
//                );
//            """.trimIndent())
        }
    }
    override fun onUpgrade(p1: SQLiteDatabase, p2: Int, p3: Int) {
//        p1.execSQL("""
//                create table config(
//                    _key text not null,
//                    _value text default ''
//                );
//            """.trimIndent())
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