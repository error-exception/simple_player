package com.simple.player.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.simple.player.model.MutablePair
import com.simple.player.model.Song
import com.simple.player.util.tree.StringPrefixTree
import java.io.File

object ArtworkProvider {

    private const val TAG = "ArtworkProvider"

    private val artworkCacheDirectory: File = FileUtil.mArtworkDirectory

    private val imageFormats = StringPrefixTree().apply {
        add("jpg")
        add("png")
        add("jpeg")
    }

    /**
     * 缓存当前专辑 URI
     */
    private val currentState = MutablePair<Long, Uri?>(0, null)

    fun getArtworkUri(song: Song): Uri? {
        if (song.id == currentState.first) {
            Log.e(TAG, "getArtworkUri: mem-cache=${currentState.second}-----${song.id}")
            return currentState.second
        }
        currentState.first = song.id
        val uri = getArtworkUriFromCache(song.id)
        if (uri != null) {
            currentState.second = uri
            Log.e(TAG, "getArtworkUri: cache=${currentState.second}")
            return currentState.second
        }
        val target = File(artworkCacheDirectory.absolutePath + "/${song.id}")
        if (AppConfigure.Settings.musicSource == "MediaStore") {
            val data = MusicUtils.getArtworkBytes(song.id)
            if (data == null) {
                currentState.second = null
                return null
            }
            FileUtil.writeBytes(target, data)
            currentState.second = Uri.fromFile(target)
        } else {
            val file = getImageFileInSameDirectory(song)
            if (file != null) {
                FileUtil.copy(from = file, to = target)
                currentState.second = Uri.fromFile(file)
            } else {
                val data = MusicUtils.getArtworkBytes(song.id)
                if (data == null) {
                    currentState.second = null
                    return null
                }
                FileUtil.writeBytes(target, data)
                currentState.second = Uri.fromFile(target)
            }
        }
        Log.e(TAG, "getArtworkUri: ${currentState.second}")
        return currentState.second
    }

    /**
     * 对 Coil 特殊对待，coil 的 compose api 并没有对以 file 协议的非 ascii 的 uri 做转化处理
     */
    fun getArtworkDataForCoil(song: Song): Any? {
        val uri = getArtworkUri(song)
        return try {
            uri?.toFile()
        } catch (_: Exception) {
            uri
        }
    }

    /**
     * 运行在线程中
     */
    fun clearArtworkCache(context: Context): Boolean {
        var isEmpty = true
        val files: Array<File>? = FileUtil.mArtworkDirectory.listFiles()
        files ?: return true
        for (file in files) {
            file.delete()
            isEmpty = false
        }
//        Glide.get(context).clearDiskCache()
        return isEmpty
    }

    private fun getImageFileInSameDirectory(song: Song): File? {
        val songFile = Uri.parse(song.uri).toFile()
        val directory = songFile.parentFile!!
        val list = directory.listFiles { _, name ->
            if (!isImageFile(name)) {
                return@listFiles false
            }
            val filename = songFile.name
            val nameWithNoExt = filename.substring(0, filename.lastIndexOf('.'))
            name.startsWith(nameWithNoExt)
        }
        return if (list != null && list.isNotEmpty()) list[0] else null
    }
    private fun getArtworkUriFromCache(id: Long): Uri? {
        val cache = artworkCacheDirectory.absolutePath + "/" + id
        val cacheFile = File(cache)
        if (cacheFile.exists()) {
            return Uri.fromFile(cacheFile)
        }
        return null
    }

    private fun isImageFile(filename: String): Boolean {
        val i = filename.lastIndexOf('.')
        if (i == -1) {
            return false
        }
        val extension = filename.substring(i + 1)
        return imageFormats.contains(extension)
    }
}