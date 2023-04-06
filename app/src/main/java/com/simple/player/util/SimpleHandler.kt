package com.simple.player.util

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference

open class SimpleHandler<T>(looper: Looper, parent: T): Handler(looper) {

    private var parentWeak: WeakReference<T> = WeakReference<T>(parent)

    var parent: T? = parentWeak.get()

}