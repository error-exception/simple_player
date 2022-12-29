package com.simple.player.util

import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.material.rememberBottomSheetState
import com.simple.player.model.Song
import com.simple.player.Util
import com.simple.player.playlist.PlaylistManager
import java.lang.Error
import java.lang.Exception

object MusicUtil {

    val MP3_TAGGER_HEADER = byteArrayOf(
        0x00, 0x6D, 0x70, 0x33, 0x20,
        0x74, 0x61, 0x67, 0x67, 0x65,
        0x72, 0x00
    )

    fun getArtist(filename: String): String {
        return try {
            when {
                filename.contains(" - ") -> {
                    filename.substring(0, filename.indexOf(" - "))
                }
                filename.contains("-") -> {
                    filename.substring(0, filename.indexOf("-"))
                }
                else -> {
                    "未知艺术家"
                }
            }
        } catch (e: Exception) {
            "命名不规范"
        }
    }

    fun getTitle(filename: String): String {
        val endIndex = filename.lastIndexOf(".")
        val startIndex = when {
            " - " in filename -> filename.lastIndexOf(" - ") + 3
            "-" in filename -> filename.lastIndexOf("-") + 1
            else -> 0
        }
        return filename.substring(startIndex, endIndex)
    }

    private fun getArtworkBytes(song: Song): ByteArray? {
        var mmr: MediaMetadataRetriever? = null
        return try {
            mmr = MediaMetadataRetriever()
            val r = Util.mContext.contentResolver?.openFileDescriptor(Uri.parse(song.path), "r")
            mmr.setDataSource(r?.fileDescriptor)
            val data: ByteArray = mmr.embeddedPicture ?: return null
            fixArtwork(data) ?: data
//            data
        } catch (e: Exception) {
            null
        } catch (e: Error) {
            null
        } finally {
            mmr?.release()
            mmr = null
        }
    }

    fun getArtworkBytes(songId: Long): ByteArray? {
        val song: Song? = PlaylistManager.localPlaylist[songId]
        return if (song != null) getArtworkBytes(song) else null
    }

    private val artworkUri = Uri.parse("content://media/external/audio/albumart")

    fun getArtworkUri(songId: Long): Uri? {
        val contentResolver = Util.mContext.contentResolver
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.ALBUM_ID),
            "${MediaStore.Audio.Media._ID} = ?",
            arrayOf(songId.toString()),
            null
        )
        cursor ?: return null
        if (cursor.count <= 0) {
            cursor.close()
            return null
        }
        cursor.moveToFirst()
        val albumId = cursor.getLong(0)
        cursor.close()
        return ContentUris.withAppendedId(artworkUri, albumId)
    }

//    fun getArtworkBitmap(activity: Activity, songId: Long, rect: Rect): Bitmap? {
//        val uri = getArtworkUri(songId)
//        uri ?: return null
//
//        val asBitmap = Glide.with(activity).asBitmap()
//        return asBitmap.load(uri).submit(rect.width(), rect.height()).get()
//    }

    /**
     * 主要解决MP3tagger的问题，类似补丁
     */
    private fun fixArtwork(data: ByteArray): ByteArray? {
        if (data.size <= MP3_TAGGER_HEADER.size) {
            return null
        }
        for (index in MP3_TAGGER_HEADER.indices) {
            if (data[index] != MP3_TAGGER_HEADER[index]) {
                return data
            }
        }
        return data.sliceArray((MP3_TAGGER_HEADER.size) until data.size)
    }

    fun getBitrate(path: String): Int {
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            val r = Util.mContext.contentResolver?.openFileDescriptor(Uri.parse(path), "r")
            mediaMetadataRetriever.setDataSource(r?.fileDescriptor)
            val bitrate = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            mediaMetadataRetriever.release()
            if (bitrate == null) {
                return 0
            }
            return bitrate.toInt()
        } catch (_: Exception) {
            return 0
        } catch (_: Error) {
            return 0
        }
    }
}