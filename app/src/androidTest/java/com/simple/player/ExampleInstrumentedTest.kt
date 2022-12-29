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
        val krcDir = File("/storage/emulated/0/kugou/lyrics/")
        val cacheDir = File("/storage/emulated/0/Android/data/com.kugou.android.lite/files/kugou/down_c/default")
        Log.e("a", Arrays.toString(krcDir.list()))
//        println(Arrays.toString(cacheDir.list()))




        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.simple.player", appContext.packageName)
    }
}