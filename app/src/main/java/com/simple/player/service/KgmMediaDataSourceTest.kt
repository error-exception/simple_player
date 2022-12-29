package com.simple.player.service

import android.media.MediaDataSource
import android.util.Log
import com.simple.player.decode.KgmDecoder
import com.simple.player.decode.KgmInputStream
import com.simple.player.util.FileUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.Arrays

class KgmMediaDataSourceTest(private val kgmFile: File): MediaDataSource() {

    private val TAG = "KgmMediaDataSourceTest"

    private var isKgm = false
    private var isVpr = false
    private var isPrepareToRead = false
    private var key: ByteArray? = null
    private var remainsLength = kgmFile.length()
    private var randomAccessFile: RandomAccessFile = RandomAccessFile(kgmFile, "r")

    init {
        val maxLength = 1024
        var buffer = ByteArray(maxLength)
        val magicHeaderLength: Int = randomAccessFile.read(buffer, 0, KgmDecoder.kgmHeader.size)
        if (magicHeaderLength < KgmDecoder.kgmHeader.size) {
            randomAccessFile.close()
            throw IOException("input file too small")
        }
        isKgm = KgmDecoder.isKgmFile(buffer)
        isVpr = KgmDecoder.isVprFile(buffer)
        if (!isKgm && !isVpr) {
            randomAccessFile.close()
            throw IOException("unsupported file")
        }
        var length: Int = randomAccessFile.read(buffer, 0, 4)
        if (length < 4) {
            throw IOException("the file is incomplete")
        }
        val headerLength: Long = KgmInputStream.calcHeaderSize(buffer, 0)
        remainsLength -= headerLength
        val remainsToReadLength = headerLength - length - magicHeaderLength
        if (remainsToReadLength > buffer.size) {
            buffer = ByteArray(remainsToReadLength.toInt())
        }
        length = randomAccessFile.read(buffer, 0, remainsToReadLength.toInt())
        if (length < remainsToReadLength) {
            throw IOException("the file is incomplete")
        }
        key = Arrays.copyOfRange(buffer, 8, 8 + (0x2c + 1 - 0x1c))
        val key = key
        key ?: throw IOException()
        key[key.size - 1] = 0
        if (KgmDecoder.mask == null) {
            KgmDecoder.initMask()
        }
        if (KgmDecoder.mask == null) {
            throw IOException("mask read failed")
        }
        isPrepareToRead = true
        Log.e(TAG, "init: prepared remains ${remainsLength}")
    }

    override fun close() {
        randomAccessFile.close()
    }

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        buffer ?: return -1
        if (!isPrepareToRead) return -1
        randomAccessFile.seek(position + 1024)
        var index = position.toInt()
        val length = randomAccessFile.read(buffer, offset, size)
        var i = offset
        while (i < size) {
            buffer[i] = if (isKgm)
                KgmInputStream.readKgm(index++, buffer[i].toInt(), key).toByte()
            else
                KgmInputStream.readVpr(index++, buffer[i].toInt(), key).toByte()
            i++
        }
        return length
    }

    override fun getSize(): Long {
        return kgmFile.length() - 1024
    }

}