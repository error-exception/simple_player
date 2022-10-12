package com.simple.player.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.simple.player.R
import com.simple.player.model.IconWithText
import com.simple.player.view.Icon

class OptionalAdapter(context: Context, values: Array<IconWithText>) :
    ArrayAdapter<IconWithText>(context, R.layout.optional_list_item, values) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val v = inflater.inflate(R.layout.optional_list_item, parent, false)
        val icon: Icon = v.findViewById(R.id.optional_list_item_icon)
        val name: TextView = v.findViewById(R.id.optional_list_item_name)
        val item = getItem(position)!!
        icon.icon = item.icon
        name.text = item.text
        return v
    }
}