package com.simple.player.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.simple.player.util.StringUtils

fun Context.toast(any: Any) {
    Toast.makeText(this.applicationContext, StringUtils.toString(any, ""), Toast.LENGTH_LONG).show()
}

fun <T: Activity> Context.startActivity(clazz: Class<T>) {
    val intent = Intent(this, clazz)
    intent.setPackage(packageName)
    startActivity(intent)
}