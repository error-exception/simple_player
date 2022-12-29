package com.simple.player.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.BaseAdapter
import android.widget.EditText
import com.simple.player.R
import com.simple.player.Util.dps
import com.simple.player.adapter.SimpleListAdapter

object DialogUtil {

    private const val THEME = android.R.style.Theme_Material_Light_Dialog
    //提示对话框
    fun alert(context: Context?, title: String = "提示", message: String?, handler: (() -> Unit)? = null) {
        var bool = false
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            handler?.invoke()
            bool = true
        }
        builder.setOnDismissListener {
            if (!bool) {
                handler?.invoke()
            }
        }
        builder.show()
    }

    fun alert(context: Context?, titleId: Int = R.string.tips, messageId: Int, handler: (() -> Unit)? = null) {
        var bool = false
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(titleId)
        builder.setMessage(messageId)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            handler?.invoke()
            bool = true
        }
        builder.setOnDismissListener {
            if (!bool) {
                handler?.invoke()
            }
        }
        builder.show()
    }
    //简单列表对话框
    fun <T> simpleList(
        context: Context,
        arr: Array<T>,
        itemClickListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context, THEME)
        val adapter: SimpleListAdapter<T> = SimpleListAdapter(context, arr)
        builder.setAdapter(adapter, itemClickListener)
        builder.show()
    }


    fun <T> simpleList(
        context: Context,
        title: String,
        arr: Array<T>,
        itemClickListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(title)
        val adapter: SimpleListAdapter<T> = SimpleListAdapter(context, arr)
        builder.setAdapter(adapter, itemClickListener)
        builder.show()
    }


    fun <T> simpleList(
        context: Context,
        titleId: Int,
        arr: Array<T>,
        itemClickListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(titleId)
        val adapter: SimpleListAdapter<T> = SimpleListAdapter(context, arr)
        builder.setAdapter(adapter, itemClickListener)
        builder.show()
    }

    fun list(
        context: Context?,
        adapter: BaseAdapter?,
        itemClickListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context, THEME)
        builder.setAdapter(adapter, itemClickListener)
        builder.show()
    }

    fun list(
        context: Context?,
        title: String?,
        adapter: BaseAdapter?,
        itemClickListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(title)
        builder.setAdapter(adapter, itemClickListener)
        builder.show()
    }

    fun list(
        context: Context?,
        titleId: Int,
        adapter: BaseAdapter?,
        itemClickListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(titleId)
        builder.setAdapter(adapter, itemClickListener)
        builder.show()
    }
    //确认对话框
    fun confirm(
        context: Context?,
        title: String?,
        message: String?,
        negative: DialogInterface.OnClickListener? = null,
        positive: DialogInterface.OnClickListener? = null
    ): Boolean {
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setNegativeButton(R.string.no, negative)
        builder.setPositiveButton(R.string.yes, positive)
        builder.show()
        return false
    }

    //确认对话框
    fun confirm(
        context: Context?,
        titleId: Int,
        messageId: Int,
        negative: DialogInterface.OnClickListener?,
        positive: DialogInterface.OnClickListener?
    ): Boolean {
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(titleId)
        builder.setMessage(messageId)
        builder.setNegativeButton(R.string.no, negative)
        builder.setPositiveButton(R.string.yes, positive)
        builder.show()
        return false
    }


    //输入对话框
    fun input(context: Context?, title: String?, hint: String?, listener: OnInputClickListener?) {
        val editText = EditText(context)
        editText.hint = hint
        editText.setTextColor(R.color.black)
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(title)
        builder.setView(editText)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.ok
        ) { dialogInterface: DialogInterface?, i: Int ->
            listener?.onInputClickListener(
                dialogInterface,
                i,
                editText.getText().toString()
            )
        }
        builder.show()
    }

    //输入对话框
    fun input(context: Context?, titleId: Int, hintId: Int, listener: OnInputClickListener?) {
        val editText = EditText(context)
        editText.setHint(hintId)
        editText.setTextColor(R.color.black)
        val builder = AlertDialog.Builder(context, THEME)
        builder.setTitle(titleId)
        builder.setView(editText)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.ok
        ) { dialogInterface: DialogInterface?, i: Int ->
            listener?.onInputClickListener(dialogInterface,
                i,
                editText.text.toString())
        }
        builder.show()
    }


    fun interface OnInputClickListener {
        fun onInputClickListener(dialogInterface: DialogInterface?, i: Int, content: String?)
    }
}