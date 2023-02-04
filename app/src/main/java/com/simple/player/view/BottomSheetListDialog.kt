package com.simple.player.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Icon
import android.graphics.drawable.VectorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simple.player.R
import com.simple.player.Util.dps
import com.simple.player.activity.BaseActivity2
import com.simple.player.activity.IconWithText

class BottomSheetListDialog(context: Context) : BottomSheetDialog(context) {

    private var recyclerView: RecyclerView? = null

    init {
        recyclerView = RecyclerView(context)
        recyclerView?.let {
            it.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            it.setBackgroundColor(Color.WHITE)
            val padding = 16.dps
            it.setPadding(0, padding, 0, padding)
        }
    }

    fun setAdapter(adapter: BottomSheetSimpleIconListAdapter) {
        recyclerView?.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
            adapter.setBottomSheetDialog(this)
            setContentView(it)
        }
    }

}

class BottomSheetSimpleIconListAdapter(private val dataList: List<IconWithText>):
    RecyclerView.Adapter<BottomSheetSimpleIconListAdapter.ViewHolder>(),
        View.OnClickListener
{

    var onItemClick: ((view: View, position: Int) -> Unit)? = null

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.dialog_list_item_icon)
        val text: TextView = view.findViewById(R.id.dialog_list_item_name)
        val parent = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dialog_list_icon_item, parent, false)
        return ViewHolder(view = view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        with(holder) {
            icon.setImageResource(data.first)
            icon.imageTintList = ColorStateList.valueOf(BaseActivity2.primaryColor)
            text.text = data.second
            parent.tag = position
            if (!parent.hasOnClickListeners()) {
                parent.setOnClickListener(this@BottomSheetSimpleIconListAdapter)
            }
        }
    }

    override fun onClick(v: View?) {
        v ?: return
        onItemClick?.invoke(v, v.tag as Int)
        dialog.dismiss()
    }

    private lateinit var dialog: BottomSheetListDialog

    internal fun setBottomSheetDialog(dialog: BottomSheetListDialog) {
        this.dialog = dialog
    }

}