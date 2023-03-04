package com.simple.player.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.simple.player.util.StringUtil

fun Context.toast(any: Any) {
    Toast.makeText(this.applicationContext, StringUtil.toString(any, ""), Toast.LENGTH_LONG).show()
}

fun <T: Activity> Context.startActivity(clazz: Class<T>) {
    val intent = Intent(this, clazz)
    intent.setPackage(packageName)
    startActivity(intent)
}