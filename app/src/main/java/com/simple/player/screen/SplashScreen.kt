package com.simple.player.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.window.layout.WindowMetricsCalculator
import com.simple.player.ApplicationLoader
import com.simple.player.LoadState
import com.simple.player.R
import com.simple.player.Store
import com.simple.player.ui.theme.NRed
import com.simple.player.util.ProgressHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Error
import java.lang.Exception

class SplashScreen(private val activity: ComponentActivity) {

    private val TAG: String = "SplashScreen"
    private val splashTips = mutableStateOf("")

    @Composable
    fun ComposeContent() {
        Splash()
    }

    @Composable
    private fun Splash() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_music_note_24),
                contentDescription = "software logo",
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colors.primary
            )
            SplashTips(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }

    @Composable
    private fun SplashTips(
        modifier: Modifier = Modifier,
    ) {
        Text(
            text = splashTips.value,
            modifier = modifier,
            color = Color.Gray,
            fontSize = 16.sp)
    }

    private var requestPermissionCallback: ((Boolean) -> Unit)? = null

    private val readExternalPermission = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        requestPermissionCallback?.invoke(it)
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private val manageStorage = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        requestPermissionCallback?.invoke(Environment.isExternalStorageManager())
    }

    private fun isPermissionOk(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission(callback: (Boolean) -> Unit) {
        requestPermissionCallback = callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manageStorage.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        } else {
            readExternalPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun startInit(onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (isPermissionOk()) {
            load(onSuccess, onFailure)
        } else {
            requestPermission {
                if (it) {
                    load(onSuccess, onFailure)
                } else {
                    onFailure()
                }
            }
        }

    }

    private var isLoaded = false

    private fun load(onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (isLoaded) {
            return
        }
        isLoaded = true
        Log.e(TAG, "load: Loading Method")
        var mark = false
        // 获取当前窗口尺寸，计算主界面图片地宽高
        val metrics =
            WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity = activity)
        val bounds = metrics.bounds
        Store.screenWidth = bounds.width()
        Store.screenHeight = bounds.height()
        ProgressHandler.handle(
            handle = {
                ApplicationLoader.load { state ->
                    when (state) {
                        LoadState.Scanning -> {
                            splashTips.value = "正在扫描"
                        }
                        LoadState.Loading -> {
                            splashTips.value = "正在加载"
                        }
                        LoadState.Finish -> {
                            splashTips.value = "加载完成"
                            mark = true
                        }
                        LoadState.Failed -> {
                            splashTips.value = "加载失败"
                            mark = false
                        }
                    }
                }
            },
            after = {
                if (mark) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
        )
    }
}
