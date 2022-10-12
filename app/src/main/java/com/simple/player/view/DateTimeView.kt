package com.simple.player.view

import android.content.Context
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.simple.player.handler.SimpleHandler
import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.*

class DateTimeView: AppCompatTextView, Closeable, LifecycleObserver {

    companion object {

        const val MSG_UPDATE_DATETIME = 12

        class DateTimeHandler(parent: DateTimeView): SimpleHandler<DateTimeView>(Looper.getMainLooper(), parent) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == MSG_UPDATE_DATETIME) {
                    parent?.update()
                }
            }
        }
    }

    private val handler = DateTimeHandler(this)

    private lateinit var formatter: SimpleDateFormat

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        setFormat("yyyy-MM-dd HH:mm:ss")
        if (context is LifecycleOwner) {
            val c = context as LifecycleOwner
            c.lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        if (context is LifecycleOwner) {
            val c = context as LifecycleOwner
            c.lifecycle.removeObserver(this)
        }
    }

    private fun update() {
        val date = Date()
        text = formatter.format(date)
    }

    fun setFormat(format: String) {
        var v: String
        formatter = try {
            v = format
            SimpleDateFormat(format, Locale.CHINA)
        } catch (e: Exception) {
            v = "yy-MM-dd HH:mm:ss"
            SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.CHINA)
        }
        val delay = when {
            v.contains("s") -> 1000L
            v.contains("m") -> 10000L
            v.contains("S") -> 1L
            else -> 60 * 1000L
        }
        close()
        handler.sendEmptyMessageDelayed(MSG_UPDATE_DATETIME, delay)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    override fun close() {
        handler.removeCallbacksAndMessages(null)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        handler.sendEmptyMessage(MSG_UPDATE_DATETIME)
    }
}
