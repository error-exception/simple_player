package com.simple.player.database

import androidx.annotation.IntRange
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.contentValuesOf
import com.simple.player.scan.ScanConfigItem

/**
 * extension name, include path, exclude path
 */
object ScanConfigDao {

    const val ID = "id"
    const val VALUE = "value"
    const val TYPE = "type"
    const val IS_VALID = "is_valid"

    const val TYPE_EXTENSION_NAME = 1
    const val TYPE_EXCLUDE_PATH = 2
    const val TYPE_INCLUDE_PATH = 3

    private val database = SQLiteDatabaseHelper.database

    fun insertItem(value: String, @IntRange(1, 3) type: Int): Boolean {
        return database.insert(
            SQLiteDatabaseHelper.TABLE_SCAN_CONFIG, null, contentValuesOf(
                VALUE to value,
                TYPE to type,
                IS_VALID to 1
            )
        ) > 0
    }

    fun deleteItem(value: String, @IntRange(1, 3) type: Int): Boolean {
        return database.delete(
            SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
            "value = ? and type = ?",
            arrayOf(value, type.toString())
        ) > 0
    }

    fun deleteItem(id: Int): Boolean {
        return database.delete(
            SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
            "id = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun updateItemValue(oldValue: String, @IntRange(1, 3) type: Int, newValue: String): Boolean {

        return database.update(
            SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
            contentValuesOf(VALUE to newValue),
            "value = ? and type = ?",
            arrayOf(oldValue, type.toString())
        ) > 0

    }

    fun updateItemValue(id: Int, newValue: String): Boolean {
        return database.update(
            SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
            contentValuesOf(VALUE to newValue),
            "id = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun queryValuesByType(@IntRange(1, 3) type: Int): List<ScanConfigItem> {
        val list = ArrayList<ScanConfigItem>()
        val cursor = database.rawQuery("select * from scan_config where type = ?;", arrayOf(type.toString()))
        cursor.moveToFirst()
        if (cursor.count <= 0) {
            cursor.close()
            return list
        }
        do {
            list += ScanConfigItem(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(ID)),
                value = mutableStateOf(cursor.getString(cursor.getColumnIndexOrThrow(VALUE))),
                type = cursor.getInt(cursor.getColumnIndexOrThrow(TYPE)),
                isValid = mutableStateOf(cursor.getInt(cursor.getColumnIndexOrThrow(IS_VALID)) == 1)
            )
        } while (cursor.moveToNext())
        return list
    }

    fun setValid(value: String, @IntRange(1, 3) type: Int, valid: Boolean): Boolean {
        return database.update(
            SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
            contentValuesOf(IS_VALID to if (valid) 1 else 0),
            "value = ? and type = ?",
            arrayOf(value, type.toString())
        ) > 0
    }

    fun setValid(id: Int, valid: Boolean): Boolean {
        return database.update(
            SQLiteDatabaseHelper.TABLE_SCAN_CONFIG,
            contentValuesOf(IS_VALID to if (valid) 1 else 0),
            "id = ?",
            arrayOf(id.toString())
        ) > 0
    }

}