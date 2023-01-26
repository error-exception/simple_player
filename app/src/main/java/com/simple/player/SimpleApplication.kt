package com.simple.player

import com.simple.player.util.FileUtil.getAssetInputStream
import com.simple.player.util.FileUtil.openOutputStream
import android.app.Application
import android.graphics.Typeface
import android.os.Environment
import com.simple.player.util.FileUtil
import android.util.Log
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.util.AppConfigure
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList

class SimpleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //System.loadLibrary("native-lib")
        SQLiteDatabaseHelper.forContext(this)
        Util.setContext(this)
        Store.iconTypeface = Typeface.createFromAsset(assets, "font/MaterialIcons-Regular.ttf")
        Store.applicationContext = this
        with (FileUtil) {
            mDataDirectory = getExternalFilesDir("")!!.absoluteFile
            mHeaderPicture =
                File(getExternalFilesDir("")!!.absoluteFile.toString() + "/header.jpg")
            mArtworkDirectory = getExternalFilesDir("artworks")!!
            mWebRoot = getExternalFilesDir("web")!!
            mListDirectory = getExternalFilesDir("list")!!
            mMaskFile = File(getExternalFilesDir("mask")!!.absolutePath + "/mask")
        }

//        if (FileUtil.mWebRoot.listFiles().isEmpty()) {
//            Log.e("application", "copy web res")
//            val path = Path()
//            path.append("web")
//            copyToData(path)
//        }
        AppConfigure.setContext(applicationContext)
    }

//    private fun copyToData(path: Path) {
//        val s = path.path
//        var list: Array<String?>? = arrayOfNulls(0)
//        try {
//            list = assets.list(s)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        for (i in list!!.indices) {
//            val s1 = list[i]
//            path.append(s1!!)
//            if (path.isDirectory) {
//                copyToData(path)
//            } else {
//                val t = path.path
//                val file = File(getExternalFilesDir(s).toString() + "/" + path.name)
//                Log.e("path", file.absolutePath)
//                copy(getAssetInputStream(t), openOutputStream(file.absolutePath))
//            }
//            path.toParent()
//        }
//    }

//    private fun copy(inputStream: InputStream?, outputStream: OutputStream?) {
//        try {
//            var len = 0
//            val j = ByteArray(1024)
//            while (inputStream!!.read(j).also { len = it } != -1) {
//                outputStream!!.write(j, 0, len)
//            }
//            outputStream!!.flush()
//            outputStream.close()
//            inputStream.close()
//        } catch (e: IOException) {
//        }
//    }
//
//    private class Path {
//        private val pathNames: ArrayList<String> = ArrayList()
//        fun append(path: String) {
//            pathNames.add(path)
//        }
//
//        val isDirectory: Boolean
//            get() = pathNames[pathNames.size - 1].lastIndexOf('.') == -1
//
//        fun toParent() {
//            pathNames.removeAt(pathNames.size - 1)
//        }
//
//        val path: String
//            get() {
//                val stringBuilder = StringBuilder()
//                for (i in pathNames.indices) {
//                    stringBuilder.append(pathNames[i])
//                    if (i != pathNames.size - 1) {
//                        stringBuilder.append('/')
//                    }
//                }
//                return stringBuilder.toString()
//            }
//        val name: String
//            get() = pathNames[pathNames.size - 1]
//
//    }
}