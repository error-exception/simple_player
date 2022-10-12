package com.simple.player.adapter

import android.content.Context
import android.widget.ArrayAdapter
import com.simple.player.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View

class SimpleListAdapter<T>(context: Context, values: Array<T>) : ArrayAdapter<Any?>(
    context, R.layout.simple_list_item, values) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder? = null
        if (convertView == null) {
            holder = ViewHolder()
            convertView =
                LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false)
            holder.name = convertView.findViewById(R.id.simple_list_item_name)
            convertView.tag = holder
        } else {
            holder = convertView.tag as SimpleListAdapter<T>.ViewHolder
        }
        holder.name.text = getItem(position).toString()
        return convertView!!
    }

    inner class ViewHolder {
        lateinit var name: TextView
    }
}