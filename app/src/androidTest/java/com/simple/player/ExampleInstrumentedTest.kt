package com.simple.player

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Arrays

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    val TAG = javaClass.simpleName

    @Test
    fun useAppContext() {
        val s = "YWJj".toByteArray()
        println(android.util.Base64.decode(s, android.util.Base64.DEFAULT).toUTFString())
        val encoded = java.util.Base64.getDecoder().decode(s)
        println(encoded.toUTFString())



        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.simple.player", appContext.packageName)
    }

    fun ByteArray.toUTFString(): String {
        return String(this, 0, this.size, StandardCharsets.UTF_8)
    }
}