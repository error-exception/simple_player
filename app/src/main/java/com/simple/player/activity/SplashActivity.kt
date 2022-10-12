package com.simple.player.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simple.player.database.SQLiteDatabaseHelper
import com.simple.player.database.SongDao
import com.simple.player.Util
import com.simple.player.ext.checkPermission
import com.simple.player.playlist.PlaylistManager
import com.simple.player.scan.MediaStoreMusicScanner
import com.simple.player.scan.MusicScannerProvider
import com.simple.player.ui.theme.NRed
import com.simple.player.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var isFocus = false
    private var isPermissionOk = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestFullscreen()
        setContent {
            Column (modifier = Modifier.background(NRed).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Simple Player", fontSize = 36.sp, color = Color.White)
            }
        }


        if (!permissionCheck()) {
            isPermissionOk = false
            requestPermission()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !isFocus && isPermissionOk) {
            isFocus = true
            initData()
        }
    }

    override fun onBackPressed() {}

    private fun requestFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (!isTaskRoot) {
            val i = intent
            val action = i.action
            if (i.hasCategory(Intent.CATEGORY_LAUNCHER) && !TextUtils.isEmpty(action) && action == Intent.ACTION_MAIN) {
                finish()
                return
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val metric = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metric)
            Util.width = metric.widthPixels
            Util.height = metric.heightPixels
        } else {
            val bounds = windowManager.currentWindowMetrics.bounds
            Util.width = bounds.width()
            Util.height = bounds.height()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView)?.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            this.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        }
    }

    private fun initData() {
        ProgressHandler.handle(handle = {
            val database = SQLiteDatabaseHelper.database
            val cursor = database.rawQuery("select * from song;", null)
            if (cursor.count > 0) {
                PlaylistManager.load()
                cursor.close()
                val intent = Intent(this@SplashActivity, HomeActivity::class.java).apply {
                    setPackage(packageName)
                }
                startActivity(intent)
                finish()
                cursor.close()
                return@handle
            }
            if (!cursor.isClosed)
                cursor.close()
            database.beginTransaction()
            val contentValues = ContentValues()
            val scanner = MusicScannerProvider.getScanner()
            scanner.onEach { id, uri, cursor, name ->
                val title: String = MusicUtil.getTitle(name)
                val artist: String = MusicUtil.getArtist(name)
                val type: String = if (scanner is MediaStoreMusicScanner) {
                    StringUtil.toString(cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)))
                } else {
                    FileUtil.getFileType(name)
                }
                val bitrate = 250
                contentValues.put(SongDao.ID, id)
                contentValues.put(SongDao.TITLE, title)
                contentValues.put(SongDao.ARTIST, artist)
                contentValues.put(SongDao.PATH, uri.toString())
                contentValues.put(SongDao.BITRATE, bitrate)
                contentValues.put(SongDao.TYPE, type)
                database.insertOrThrow("song", null, contentValues)
                contentValues.clear()
            }
            scanner.onComplete {
                database.setTransactionSuccessful()
                database.endTransaction()
                PlaylistManager.load()
            }
            scanner.scan()
        }, after = {
            this@SplashActivity.finish()
            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
        })
    }

    private val permission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        isPermissionOk = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (it[Manifest.permission.MANAGE_EXTERNAL_STORAGE]!!) {
                DialogUtil.alert(applicationContext, message = "没有该权限，将导致程序的部分功能无法工作")
                return@registerForActivityResult
            }
        } else {
            if (it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!) {
                DialogUtil.alert(this, message = "没有该权限，将导致程序的部分功能无法工作")
                return@registerForActivityResult
            }
        }
        initData()
    }

    private fun permissionCheck(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (checkPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
                return true
            }
        } else {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                return true
            }
        }

        return false
    }

    private fun requestPermission() {
        val list = ArrayList<String>()
        list += if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        permission.launch(list.toTypedArray())
    }

}