package com.simple.player.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.ColorUtils
import androidx.core.view.setPadding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simple.player.R
import com.simple.player.Util.dps
import com.simple.player.activity.BaseActivity2

class BottomSheetConfirmDialog(context: Context):
    BottomSheetDialog(context),
    View.OnClickListener
{

    private var titleView: TextView? = null
    private var messageView: TextView? = null
    private var positiveButton: AppCompatButton? = null
    private var negativeButton: AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_sheet_confirm_dialog)

        titleView = findViewById(R.id.dialog_title)
        messageView = findViewById(R.id.dialog_message)
        positiveButton = findViewById(R.id.dialog_ok)
        negativeButton = findViewById(R.id.dialog_cancel)
//        var primaryColor = BaseActivity2.primaryColor
//        val newAlpha = (255 * 0.38F).toInt()
//        primaryColor = ((primaryColor and 0xffffff) or (newAlpha shl 24))
//        val colorStateList = ColorStateList.valueOf(primaryColor)
//
//        positiveButton?.let {
//            it.backgroundTintList = colorStateList
//        }
//        negativeButton?.let {
//            it.backgroundTintList = colorStateList
//        }
    }

    fun setDialogTitle(text: CharSequence) {
        titleView?.text = text
    }

    fun setDialogMessage(text: CharSequence) {
        messageView?.text = text
    }

    var onPositive: (() -> Unit)? = null
        set(value) {
            positiveButton?.setOnClickListener(this)
            field = value
        }

    var onNegative: (() -> Unit)? = null
        set(value) {
            negativeButton?.setOnClickListener(this)
            field = value
        }

    override fun onClick(v: View?) {
        Log.e(TAG, "onClick: ")
        v ?: return
        when (v.id) {
            R.id.dialog_cancel -> onNegative?.invoke()
            R.id.dialog_ok -> onPositive?.invoke()
        }
        dismiss()
    }

    companion object {

        private const val TAG = "BottomSheetConfirmDialog"

        fun showDialog(
            context: Context,
            title: CharSequence,
            message: CharSequence,
            onPositive: (() -> Unit)? = null,
            onNegative: (() -> Unit)? = null
        ) {
            val dialog = BottomSheetConfirmDialog(context)
            dialog.let {
                dialog.create()
                it.setDialogTitle(text = title)
                it.setDialogMessage(text = message)
                it.onPositive = onPositive
                it.onNegative = onNegative
                dialog.show()
            }
        }

    }
}