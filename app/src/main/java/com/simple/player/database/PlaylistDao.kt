package com.simple.player.database

import androidx.core.content.contentValuesOf
import androidx.core.database.getStringOrNull
import com.simple.player.util.StringUtils

object PlaylistDao {

    const val ID = "id"
    const val NAME_CODE = "name_code"
    const val DESCRIPTION = "description"
    const val TABLE_NAME = "playlist"

    fun insertPlaylist(name: String, description: String = ""): Boolean {
        val contentValues = contentValuesOf(
            NAME_CODE to StringUtils.stringToCode(name),
            DESCRIPTION to description
        )
        val code = SQLiteDatabaseHelper.database.insert(TABLE_NAME, null, contentValues)
        return code != -1L
    }

    fun queryPlaylist(listId: Long): PlaylistDto? {
        val database = SQLiteDatabaseHelper.database
        val cursor =
            database.rawQuery("select * from playlist where id = ?;", arrayOf(listId.toString()))
        cursor.moveToFirst()
        if (cursor.count <= 0) {
            cursor.close()
            return null
        }
        val idIndex = cursor.getColumnIndexOrThrow(ID)
        val nameIndex = cursor.getColumnIndexOrThrow(NAME_CODE)
        val descriptionIndex = cursor.getColumnIndexOrThrow(DESCRIPTION)

        val id = cursor.getLong(idIndex)
        val name = StringUtils.codeToString(cursor.getString(nameIndex))
        val description = cursor.getStringOrNull(descriptionIndex)
        cursor.close()

        return PlaylistDto(
            id = id,
            name = name,
            description = description
        )
    }

    fun updatePlaylist(listId: Long, columnName: String, newValue: String): Boolean {
        if (columnName != NAME_CODE && columnName != DESCRIPTION) {
            return false
        }
        val contentValues = contentValuesOf(
            columnName to if (columnName == NAME_CODE) StringUtils.stringToCode(newValue) else newValue
        )
        return SQLiteDatabaseHelper.database.update(
            TABLE_NAME,
            contentValues,
            "id = ?",
            arrayOf(listId.toString())
        ) != 0
    }

    fun deletePlaylist(listId: Long): Boolean {
        return SQLiteDatabaseHelper.database.delete(TABLE_NAME, "id = ?", arrayOf(listId.toString())) != 0
    }

    /**
     * 查询所有记录，若不存在记录，则返回 false
     */
    fun queryAll(callback: (id: Long, nameCode: String, description: String) -> Unit): Boolean {
        val database = SQLiteDatabaseHelper.database
        val cursor =
            database.rawQuery("select * from playlist;", null)
        cursor.moveToFirst()
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        do {
            val idIndex = cursor.getColumnIndexOrThrow(ID)
            val nameIndex = cursor.getColumnIndexOrThrow(NAME_CODE)
            val descriptionIndex = cursor.getColumnIndexOrThrow(DESCRIPTION)

            val id = cursor.getLong(idIndex)
            val name = StringUtils.codeToString(cursor.getString(nameIndex))
            val description = cursor.getString(descriptionIndex)
            callback(id, name, description)
        } while (cursor.moveToNext())
        cursor.close()
        return true
    }

    fun queryIdByName(name: String): Long {
        val database = SQLiteDatabaseHelper.database
        val cursor =
            database.rawQuery(
                "select id from playlist where name_code = ?;",
                arrayOf(StringUtils.stringToCode(name))
            )
        cursor.moveToFirst()
        if (cursor.count <= 0) {
            cursor.close()
            return -1L
        }
        val idIndex = cursor.getColumnIndexOrThrow(ID)
        val id = cursor.getLong(idIndex)
        cursor.close()
        return id
    }
}

data class PlaylistDto(
    val id: Long,
    val name: String,
    val description: String?
)