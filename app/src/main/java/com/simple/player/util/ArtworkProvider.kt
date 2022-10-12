package com.simple.player.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.simple.player.model.Song
import com.simple.player.util.tree.StringPrefixTree
import java.io.File

object ArtworkProvider {

    private val artworkCacheDirectory: File = FileUtil.mArtworkDirectory

    private lateinit var cacheArtworkFile: File
    private val emptyByteArray = ByteArray(0)
    private val imageFormats = StringPrefixTree().apply {
        add("jpg")
        add("png")
        add("jpeg")
    }

    @Deprecated(level = DeprecationLevel.WARNING, message = "use getArtworkUri(song: Song)")
    fun getArtworkBytes(song: Song): ByteArray {
        if (AppConfigure.Settings.musicSource == "MediaStore") {
            return getArtworkFromUri(song)
        }
        if (AppConfigure.Settings.musicSource == "ExternalStorage") {
            return getArtworkFromFile(song)
        }
        return emptyByteArray
    }

    fun getArtworkUri(song: Song): Uri? {
        if (hasArtworkCache(song)) {
            return Uri.fromFile(cacheArtworkFile)
        } else {
            val target = File(artworkCacheDirectory.absolutePath + "/${song.id}.png")
            if (AppConfigure.Settings.musicSource == "MediaStore") {
                val data = MusicUtil.getArtworkBytes(song.id)
                data ?: return null
                FileUtil.writeBytes(target, data)
                return Uri.fromFile(target)
            } else {
                val file = getImageFileInSameDirectory(song)
                if (file == null) {
                    val data = MusicUtil.getArtworkBytes(song.id)
                    data ?: return null
                    FileUtil.writeBytes(target, data)
                    return Uri.fromFile(target)
                }
                return Uri.fromFile(file)
            }
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
        Glide.get(context).clearDiskCache()
        return isEmpty
    }

    private fun getArtworkFromUri(song: Song): ByteArray {
        if (hasArtworkCache(song)) {
            return getArtworkBytesFromCache()
        } else {
            val data = MusicUtil.getArtworkBytes(song.id) ?: return emptyByteArray
            saveToArtworkCache(song, data)
        }
        return FileUtil.readBytes(song.path)
    }

    private fun getArtworkBytesFromCache(): ByteArray {
        return FileUtil.readBytes(cacheArtworkFile)
    }

    private fun getArtworkFromFile(song: Song): ByteArray {
        return if (hasArtworkCache(song)) {
            getArtworkBytesFromCache()
        } else {
            var data = MusicUtil.getArtworkBytes(song.id) ?: emptyByteArray
            if (data.isEmpty()) {
                val imageFile = getImageFileInSameDirectory(song) ?: return emptyByteArray
                data = FileUtil.readBytes(imageFile)
            }
            saveToArtworkCache(song, data)
            data
        }
    }

    private fun getImageFileInSameDirectory(song: Song): File? {
        val songFile = Uri.parse(song.path).toFile()
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

    private fun saveToArtworkCache(song: Song, data: ByteArray) {
        val target = artworkCacheDirectory.absolutePath + "/${song.id}.png"
        FileUtil.writeBytes(File(target), data)
    }

    private fun hasArtworkCache(song: Song): Boolean {
        val list = artworkCacheDirectory.listFiles { _, name ->
            val list = name.split(".")
            if (isImageFile(name)) {
                StringUtil.toLong(list[0], -1) == song.id
            } else {
                false
            }
        }
        return if (list.isNullOrEmpty()) {
            false
        } else {
            cacheArtworkFile = list[0]
            true
        }
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