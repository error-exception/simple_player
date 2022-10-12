package com.simple.player.adapter

import android.content.Context
import android.widget.ArrayAdapter
import com.simple.player.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import com.simple.player.model.IconWithText
import com.simple.player.view.Icon

class DialogListAdapter(context: Context, values: Array<IconWithText>) : ArrayAdapter<IconWithText?>(
    context, R.layout.dialog_list_icon_item, values) {
    override fun getView(position: Int, convertView1: View?, parent: ViewGroup): View {
        var convertView = convertView1
        var holder: ViewHolder? = null
        if (convertView == null) {
            holder = ViewHolder()
            convertView =
                LayoutInflater.from(context).inflate(R.layout.dialog_list_icon_item, parent, false)
            holder.icon = convertView.findViewById<View>(R.id.dialog_list_item_icon) as Icon
            holder.name = convertView.findViewById<View>(R.id.dialog_list_item_name) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val iconWithText = getItem(position)!!
        holder.icon.icon = iconWithText.icon
        holder.name.text = iconWithText.text
        return convertView!!
    }

    inner class ViewHolder {
        lateinit var icon: Icon
        lateinit var name: TextView
    }
}