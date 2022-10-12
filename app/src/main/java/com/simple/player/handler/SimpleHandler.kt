package com.simple.player.handler

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import java.lang.ref.WeakReference

open class SimpleHandler<T>(@NonNull looper: Looper, parent: T): Handler(looper) {

    private var parentWeak: WeakReference<T> = WeakReference<T>(parent)

    var parent: T? = parentWeak.get()

}