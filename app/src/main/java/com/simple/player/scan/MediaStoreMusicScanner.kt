package com.simple.player.scan

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.simple.player.Store

class MediaStoreMusicScanner: MusicScanner() {

    private var count = 0
    override val resultCount
        get() = count

    override fun scan() {
        val resolver: ContentResolver = Store.applicationContext.contentResolver
        val contentUri = MediaStore.Files.getContentUri("external")
        val cursor = resolver.query(contentUri, null, null, null, null)
        while (cursor!!.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val uri = ContentUris.withAppendedId(contentUri, id)
            val name =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME))
            if (name != null && name.lowercase().endsWith("mp3") && onEachMusic != null) {
                count++
                onEachMusic?.invoke(id, uri, cursor, name)
            }
        }
        cursor.close()
        onComplete?.invoke()
    }


}