package com.simple.player.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simple.player.R

@Suppress("MemberVisibilityCanBePrivate")
class BottomSheetInputDialog(context: Context):
    BottomSheetDialog(context),
    View.OnClickListener
{

    private var titleView: TextView? = null
    private var editTextView: AppCompatEditText? = null
    private var positiveButton: AppCompatButton? = null
    private var negativeButton: AppCompatButton? = null

    private var onPositive: ((text: String) -> Unit)? = null
    private var onNegative: ((text: String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_sheet_input_dialog)

        titleView = findViewById(R.id.dialog_title)
        editTextView = findViewById(R.id.dialog_input)
        positiveButton = findViewById(R.id.dialog_ok)
        negativeButton = findViewById(R.id.dialog_cancel)

        positiveButton?.setOnClickListener(this)
        negativeButton?.setOnClickListener(this)
    }

    fun setDialogTitle(text: CharSequence) {
        titleView?.text = text
    }

    fun getEditText(): String {
        val editText = editTextView
        editText ?: return ""
        return editText.text.toString()
    }

    fun setHint(text: CharSequence) {
        editTextView?.hint = text
    }

    override fun onClick(v: View?) {
        v ?: return
        val text = getEditText()
        when (v.id) {
            R.id.dialog_ok -> onPositive?.invoke(text)
            R.id.dialog_cancel -> onNegative?.invoke(text)
        }
        dismiss()
    }

    companion object {

        fun showDialog(
            context: Context,
            title: CharSequence,
            hint: CharSequence = context.resources.getString(R.string.input_please),
            onPositive: ((text: String) -> Unit)? = null,
            onNegative: ((text: String) -> Unit)? = null
        ) {
            val dialog = BottomSheetInputDialog(context)
            dialog.let {
                it.create()
                it.setDialogTitle(title)
                it.setHint(hint)
                it.onPositive = onPositive
                it.onNegative = onNegative
                it.show()
            }
        }

    }

}