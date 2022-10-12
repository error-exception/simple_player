package com.simple.player.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.simple.player.MusicEvent
import com.simple.player.MusicEventHandler
import com.simple.player.Store
import com.simple.player.service.SimplePlayer
import com.simple.player.util.ProgressHandler

/**
 * 1. 显示图片主色
 * 2. 可设置圆角
 * 3. 可显示中间裁剪的图片
 */
class PaletteView: View, MusicEvent.OnSongChangedListener, LifecycleObserver {

    private var displaySongId: Long = SimplePlayer.currentSong.id
    private var newSongId: Long = SimplePlayer.currentSong.id
    private var normalDrawable: ShapeDrawable
    private var pressedDrawable: ShapeDrawable
    private var lifecycle: Lifecycle? = null

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        MusicEventHandler.register(this)
        val outRadius = floatArrayOf(8f, 8f, 8f, 8f, 8f, 8f, 8f, 8f)
        val shape = RoundRectShape(outRadius, null, null)
        normalDrawable = ShapeDrawable(shape).apply {
            paint.color = Color.WHITE
        }
        pressedDrawable = ShapeDrawable(shape).apply {
            paint.color = Color.parseColor("#bfbfbfbf")
        }
        val stateListDrawable = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
            addState(intArrayOf(android.R.attr.state_enabled), normalDrawable)
        }
        background = stateListDrawable
        if (context is LifecycleOwner) {
            val lifecycleOwner = context as LifecycleOwner
            lifecycle = lifecycleOwner.lifecycle
            lifecycle?.addObserver(this)
        }
    }

    fun setBitmap(bitmap: Bitmap) {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onActivityDestroyed() {
        lifecycle?.removeObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun applySongChange() {
        ProgressHandler.handle(handle = {

        }, after = {

        })
    }

    override fun onSongChanged(newSongId: Long) {
        this.newSongId = newSongId
        if (lifecycle != null && lifecycle!!.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            displaySongId = newSongId
            applySongChange()
        }
    }

}