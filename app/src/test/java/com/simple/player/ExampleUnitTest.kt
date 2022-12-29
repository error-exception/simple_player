package com.simple.player

import androidx.compose.runtime.produceState
import com.simple.player.decode.KgmDecoder
import com.simple.player.decode.KgmInputStream
import com.simple.player.decode.NCMDecoder
import com.simple.player.decode.UCDecoder
import com.simple.player.lyrics.KRCDecoder
import com.simple.player.lyrics.Lrc
import com.simple.player.lyrics.LrcParser
import com.simple.player.util.FileUtil
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        ncmTest()
    }

    fun ncmTest() {
        val inFile = File("C:\\Users\\HP\\Desktop\\许嵩 - 千百度.ncm")
        val data = NCMDecoder.decode(FileUtil.openInputStream(inFile))
        val outFile = File("C:\\Users\\HP\\Desktop\\test_1.mp3")
        FileUtil.writeBytes(outFile, data = data);
    }

    fun ucTest() {
        val inFile = File("C:\\Users\\HP\\AppData\\Local\\Netease\\CloudMusic\\Cache\\Cache\\561712-320-b19dafba706c4fd7770f12d3890dccf5.uc")
        val data = UCDecoder.decode(FileUtil.openInputStream(inFile))
        val outFile = File("C:\\Users\\HP\\Desktop\\test.mp3")
        FileUtil.writeBytes(outFile, data = data);
    }

    fun kgmTest() {
        val inputStream = FileUtil.openInputStream("C:\\Users\\HP\\Desktop\\e783ae46a4210d35e02a42eff36b1088-2-407526009_HQ.kge")
        if (inputStream == null) {
            println("read failed")
            return
        }
        val data = KgmDecoder.decode(inputStream)
//        val data = FileUtil.readBytes(KgmInputStream(inputStream))
        if (data == null) {
            println("decode failed")
            return
        }
        FileUtil.writeBytes(File("C:\\Users\\HP\\Desktop\\e783ae46a4210d35e02a42eff36b1088-2-407526009_HQ.mp3"), data)
    }

    fun krcTest() {
        val file = File("C:\\Users\\HP\\Desktop\\茶鸣拾贰律 - 青芽-9a0dac6c737b6a3e46c0b3a08806a3d1.krc")
        if (!file.exists()) {
            println("not found")
            return
        }
        val inputStream = FileUtil.openInputStream(file)
        if (inputStream == null) {
            println("error")
            return
        }
        val content = KRCDecoder.INSTANCE.decode(inputStream)
        println(content)

    }
}