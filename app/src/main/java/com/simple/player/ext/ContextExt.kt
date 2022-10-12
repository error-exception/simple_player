package com.simple.player.ext

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.simple.player.util.StringUtil

fun Context.toast(any: Any) {
    Toast.makeText(this.applicationContext, StringUtil.toString(any, ""), Toast.LENGTH_LONG).show()
}

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
