package com.simple.player.service

import android.media.MediaDataSource
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.rememberDismissState
import com.simple.player.decode.KgmInputStream
import com.simple.player.util.FileUtil
import java.io.File
import java.io.IOException
import java.lang.Exception

class KgmMediaDataSource(private val kgmFile: File): MediaDataSource() {

    private var kgmInputStream: KgmInputStream? = null
    private val byteCache = ArrayList<Byte>(1024 * 1024 * 10)

    init {
        val openInputStream = FileUtil.openInputStream(kgmFile)
        openInputStream ?: throw IOException()
        kgmInputStream = KgmInputStream(openInputStream)
    }

    override fun close() {
        kgmInputStream?.close()
        byteCache.clear()
    }

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        try {
            if (buffer == null) {
                return -1
            }
            val inputStream = kgmInputStream
            inputStream ?: return -1
            val requirePosition = position.toInt()
            val cachedPosition = byteCache.size - 1
            if (requirePosition + size <= byteCache.size) {
                // 在缓存区中
                for (index in 0 until size) {
                    buffer[offset + index] = byteCache[requirePosition + index]
                }
                return size
            } else if (requirePosition - cachedPosition > 1) {
                // 在缓存区外
                val needToCache = requirePosition - cachedPosition - 1
                val temp = ByteArray(needToCache)
                var length = inputStream.read(temp)
                addToCache(temp, 0, length)
                if (length < needToCache) {
                    return -1
                }
                length = inputStream.read(buffer, offset, size)
                addToCache(buffer, offset, length)
                return length
            } else if (requirePosition <= cachedPosition && requirePosition + size > byteCache.size) {
                // 部分在缓存区
                var cacheRead = 0
                for (i in requirePosition until byteCache.size) {
                    buffer[(i - requirePosition) + offset] = byteCache[i]
                    cacheRead++
                }
                val newOffset = offset + cacheRead
                val newSize = size - cacheRead
                val length = inputStream.read(buffer, newOffset, newSize)
                addToCache(buffer, newOffset, length)
                return length
            } else {
                // 紧接着缓存区
                val length = inputStream.read(buffer, offset, size)
                addToCache(buffer, offset, length)
                return length
            }
        } catch (e: Exception) {
            return -1
        }

    }

    override fun getSize(): Long {
        return kgmFile.length()
    }

    private fun addToCache(bytes: ByteArray, offset: Int, size: Int) {
        for (i in 0 until size) {
            byteCache.add(bytes[i + offset])
        }
    }

}