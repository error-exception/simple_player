package com.simple.player.activity

import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import com.simple.player.R
import com.simple.player.database.IdPathDao
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.database.SongDao
import com.simple.player.ext.toast
import com.simple.player.json.JSON
import com.simple.player.util.FileUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DeveloperActivity: BaseActivity2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.developer)

        findViewById<AppCompatButton>(R.id.clear_id_path).setOnClickListener {
            IdPathDao.clear()
            toast("完成")
        }

        findViewById<AppCompatButton>(R.id.export_id_path).setOnClickListener {
            val database = SQLiteDatabaseHelper.database
            val cursor = database.rawQuery("select * from id_path;", null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                toast("无数据")
                return@setOnClickListener
            }
            val list = ArrayList<HashMap<String, Any>>()
            do {
                val map = HashMap<String, Any>()
                map["id"] = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                map["uri"] = cursor.getString(cursor.getColumnIndexOrThrow("uri"))
                map["valid"] = cursor.getInt(cursor.getColumnIndexOrThrow("valid"))
                list.add(map)
            } while (cursor.moveToNext())
            cursor.close()
            val json = JSON.stringify(list)
            val format = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA)
            val filename = "${format.format(Date())}_id_path.json"
            FileUtil.writeTextUTF8(File(FileUtil.mDataDirectory.absolutePath + "/${filename}"), json)
            toast("OK")
        }

        findViewById<AppCompatButton>(R.id.export_song).setOnClickListener {
            val database = SQLiteDatabaseHelper.database
            val cursor = database.rawQuery("select * from song;", null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                toast("无数据")
                return@setOnClickListener
            }
            val list = ArrayList<HashMap<String, Any>>()
            do {
                val map = HashMap<String, Any>()
                map[SongDao.ID] = cursor.getLong(cursor.getColumnIndexOrThrow(SongDao.ID))
                map[SongDao.TITLE] = cursor.getString(cursor.getColumnIndexOrThrow(SongDao.TITLE))
                map[SongDao.ARTIST] = cursor.getString(cursor.getColumnIndexOrThrow(SongDao.ARTIST))
                map[SongDao.BITRATE] = cursor.getString(cursor.getColumnIndexOrThrow(SongDao.BITRATE))
                map[SongDao.TYPE] = cursor.getString(cursor.getColumnIndexOrThrow(SongDao.TYPE))
                map[SongDao.PATH] = cursor.getString(cursor.getColumnIndexOrThrow(SongDao.PATH))
                list.add(map)
            } while (cursor.moveToNext())
            cursor.close()
            val json = JSON.stringify(list)
            val format = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA)
            val filename = "${format.format(Date())}_song.json"
            FileUtil.writeTextUTF8(File(FileUtil.mDataDirectory.absolutePath + "/${filename}"), json)
            toast("OK")
        }
    }

}