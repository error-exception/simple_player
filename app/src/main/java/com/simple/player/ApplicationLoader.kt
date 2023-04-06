package com.simple.player

import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.memory.MemoryCache
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.database.SongDao
import com.simple.player.playlist.PlaylistManager
import com.simple.player.scan.MediaStoreMusicScanner
import com.simple.player.scan.MusicScannerProvider
import com.simple.player.util.FileUtil
import com.simple.player.util.MusicUtils
import com.simple.player.util.StringUtils

sealed class LoadState {
    object Loading: LoadState()
    object Scanning: LoadState()
    object Finish: LoadState()
    object Failed: LoadState()
}

object ApplicationLoader {

    const val TAG = "ApplicationLoader"

    fun load(loadListener: (LoadState) -> Unit) {

        loadListener(LoadState.Loading)
        val hasSong = PlaylistManager.load()
        if (!hasSong) {
            loadListener(LoadState.Scanning)
            scanMusic()
            PlaylistManager.load()
        }
//        initCoil()
        loadListener(LoadState.Finish)
    }

    private fun scanMusic() {
        val database = SQLiteDatabaseHelper.database
        database.beginTransaction()
        val contentValues = ContentValues()
        val scanner = MusicScannerProvider.getScanner()
        Log.e(TAG, "loadAppData: ")
        scanner.onEach { id, uri, cursor, name ->
            val title = MusicUtils.getTitle(name)
            val artist = MusicUtils.getArtist(name)
            val type = if (scanner is MediaStoreMusicScanner) {
                StringUtils.toString(cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)))
            } else {
                FileUtil.getFileType(name)
            }
            val bitrate = 250
            contentValues.put(SongDao.ID, id)
            contentValues.put(SongDao.TITLE, title)
            contentValues.put(SongDao.ARTIST, artist)
            contentValues.put(SongDao.PATH, uri.toString())
            contentValues.put(SongDao.BITRATE, bitrate)
            contentValues.put(SongDao.TYPE, type)
            database.insertOrThrow("song", null, contentValues)
            contentValues.clear()
        }
        scanner.onComplete {
            database.setTransactionSuccessful()
            database.endTransaction()
        }
        scanner.scan()
    }

    private fun initCoil() {
        val memoryCache = MemoryCache.Builder(Store.applicationContext)
            .maxSizeBytes(0)
            .weakReferencesEnabled(true)
            .build()
        val imageLoader = ImageLoader.Builder(Store.applicationContext)
//            .memoryCache(memoryCache)
            .allowHardware(true)
            .allowRgb565(true)
            .build()
        Coil.setImageLoader(imageLoader)
    }
}
