package com.simple.player

import android.app.Application
import android.graphics.Typeface
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.util.AppConfigure
import com.simple.player.util.FileUtil
import java.io.File

class SimpleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //System.loadLibrary("native-lib")
        AppConfigure.setContext(applicationContext)
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