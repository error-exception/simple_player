package com.simple.player.database

import android.content.ContentValues

object IdPathDao {

    private val database = SQLiteDatabaseHelper.database

    fun queryIdByPath(uri: String): Long {
        val cursor = database.rawQuery("select id from id_path where uri = ?", arrayOf(uri))
        var id = -1L
        if (cursor.count > 0) {
            cursor.moveToFirst()
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        return id
    }

    fun queryUriById(id: Long): String {
        val cursor = database.rawQuery("select uri from id_path where id = ?", arrayOf(id.toString()))
        var uri = ""
        if (cursor.count != 0) {
            cursor.moveToFirst()
            uri = cursor.getString(0)
        }
        cursor.close()
        return uri
    }

    fun addUri(path: String): Long {
        val id = queryIdByPath(path)
        if (id != -1L) {
            return id
        }
        database.insert("id_path", null, ContentValues().apply {
            put("uri", path)
            put("valid", 1)
        })
        return queryIdByPath(path)
    }

    fun addUri(id: Long, path: String) {

    }

    /**
     * 更新状态
     */
    fun updateData() {
        TODO("not impl")
    }

    fun clear() {
        database.delete("id_path", null, null)
    }

}