package com.simple.player.lyrics

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.simple.player.util.FileUtil
import java.io.File

object LyricsProvider {

    const val TAG = "LyricsProvider"

    private var lyricsType: LyricsType = LyricsType.NormalLyrics

    fun setSongUri(uri: Uri): Lrc? {
        if ("file" != uri.scheme) {
            return null
        }
        val songFile = uri.toFile()

        val parent = songFile.parentFile
        val list = parent?.listFiles()
        Log.e(TAG, "setSongUri: ${list?.contentToString()}")
        list ?: return null

        val songFileDotIndex = songFile.name.lastIndexOf('.')
        val songPureName = songFile.name.substring(0, songFileDotIndex)

        var lrcFile: File? = null

        for (file in list) {
            if (file.isDirectory) {
                continue
            }
            val name = file.name
            val dotIndex = name.lastIndexOf('.')
            if (dotIndex < 0) {
                continue
            }
            val pureName = name.substring(0, dotIndex)
            val extension = name.substring(dotIndex + 1)
            Log.e(TAG, "setSongUri: $dotIndex $pureName $extension")
            val isLyrics = when (extension) {
                "lrc" -> {
                    lyricsType = LyricsType.NormalLyrics
                    true
                }
                "krc" -> {
                    lyricsType = LyricsType.KugouLyrics
                    true
                }
                "lyric" -> {
                    lyricsType = LyricsType.NetesseLyrics
                    true
                }
                else -> false
            }
            if (pureName == songPureName && isLyrics) {
                lrcFile = file
            }
        }
        if (lrcFile != null) {
            Log.e(TAG, "setSongUri: parse lrc")
            return parseLyrics(lrcFile)
        }

        return null
    }

    private fun parseLyrics(path: File): Lrc? {
        val inputStream = FileUtil.openInputStream(file = path)
        inputStream ?: return null
        val result = when (lyricsType) {
            LyricsType.NetesseLyrics, LyricsType.NormalLyrics -> {
                Log.e(TAG, "parseLyrics: parse lrc")
                LrcParser().parse(inputStream = inputStream)
            }
            LyricsType.KugouLyrics -> {
                SimpleKrcParser().parse(input = inputStream)
            }
            else -> null
        }
        inputStream.close()
        return result
    }

    sealed class LyricsType {
        object NormalLyrics: LyricsType()

        object KugouLyrics: LyricsType()

        object NetesseLyrics: LyricsType()

        object NeteaseJsonLyrics: LyricsType()

        object Unknown: LyricsType()

    }

}