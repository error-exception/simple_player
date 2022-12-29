package com.simple.player.service

import android.media.MediaDataSource
import com.simple.player.decode.NCMDecoderTest
import com.simple.player.util.AESUtils
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.experimental.xor

class NCMMediaDataSource(private val ncmFile: File): MediaDataSource() {

    private val randomAccessFile = RandomAccessFile(ncmFile, "r")
    private var audioLength = 0L
    private val key: ByteArray
    private val sizeBytes = ByteArray(4)
    private val box: IntArray
    private val audioOffset: Long

    init {
        randomAccessFile.seek(0)
        val headerBytes = ByteArray(NCMDecoderTest.magicHeader.size + 2)
        randomAccessFile.read(headerBytes)
        if (!NCMDecoderTest.isNCMFile(headerBytes)) {
            randomAccessFile.close()
            throw IOException("not a ncm file")
        }
        key = readKey()
        box = NCMDecoderTest.buildKeyBox(key)
        randomAccessFile.read(sizeBytes)
        val metaLength = NCMDecoderTest.calcSize(sizeBytes, 0)
        randomAccessFile.skipBytes(metaLength.toInt() + 5 + 4)
        randomAccessFile.read(sizeBytes)
        val coverLength = NCMDecoderTest.calcSize(sizeBytes, 0)
        randomAccessFile.skipBytes(coverLength.toInt())
        audioOffset = randomAccessFile.filePointer
        audioLength = ncmFile.length() - audioOffset

    }

    override fun close() {
        randomAccessFile.close()
    }

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        buffer ?: return -1
        randomAccessFile.seek(audioOffset + position)
        val length = randomAccessFile.read(buffer, offset, size)
        var i = offset
        while (i < size) {
            buffer[i] = NCMDecoderTest.decryptByte((position + i).toInt(), box, buffer[i])
            i++
        }
        return length
    }

    override fun getSize(): Long {
        return audioLength
    }

    private fun readKey(): ByteArray {
        randomAccessFile.read(sizeBytes)
        val keyLength = NCMDecoderTest.calcSize(sizeBytes, 0).toInt()
        val keyBytes = ByteArray(keyLength)
        randomAccessFile.read(keyBytes)
        for (i in 0 until keyLength) {
            keyBytes[i] = (keyBytes[i] xor 0x64)
        }
        val decryptedKey = AESUtils.decrypt(keyBytes, NCMDecoderTest.keyCore)
        return decryptedKey.copyOfRange(17, decryptedKey.size)
    }
}