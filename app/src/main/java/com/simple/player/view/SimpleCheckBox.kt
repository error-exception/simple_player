package com.simple.player.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.simple.player.constant.IconCode

class SimpleCheckBox : IconButton {

    private var mIsChecked = false
    private var mClickable = false
    private var mListener: OnCheckedChangeListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attr: AttributeSet?) : super(context, attr) {
        init()
    }

    constructor(context: Context?, attr: AttributeSet?, p: Int) : super(context, attr, p) {
        init()
    }

    private fun init() {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mClickable) return super.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            isChecked = !mIsChecked
            if (mListener != null) {
                mListener!!.onCheckedChange(this, mIsChecked)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun setClickable(clickable: Boolean) {
        mClickable = clickable
        super.setClickable(clickable)
    }

    var isChecked: Boolean
        get() = mIsChecked
        set(check) {
            mIsChecked = check
            icon = if (mIsChecked) IconCode.ICON_CHECK_CIRCLE else IconCode.ICON_RADIO_BUTTON_UNCHECKED
        }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        mListener = listener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChange(p1: SimpleCheckBox?, p2: Boolean)
    }
}