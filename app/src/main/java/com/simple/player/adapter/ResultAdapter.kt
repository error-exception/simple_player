package com.simple.player.adapter

import android.content.Context
import com.simple.player.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import com.simple.player.model.Song
import android.view.View
import com.simple.player.view.SimpleCheckBox
import android.widget.BaseAdapter
import java.util.ArrayList

class ResultAdapter(context: Context, values: List<Song>) : BaseAdapter() {

    private var mSelected: ArrayList<Song>?
    private val mContext = context
    private val mValues = values

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder? = null
        if (convertView == null) {
            holder = ViewHolder()
            convertView = LayoutInflater.from(mContext).inflate(R.layout.scan_music_result_item, parent, false)
            with (holder) {
                title = convertView.findViewById(R.id.scan_music_result_title)
                artist = convertView.findViewById(R.id.scan_music_result_artist)
                check = convertView.findViewById(R.id.scan_music_result_check)
            }
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val a = getItem(position)
        holder.title.text = a.title
        holder.artist.text = a.artist
        holder.check.isChecked = a.isChecked
        return convertView!!
    }

    inner class ViewHolder {
        lateinit var title: TextView
        lateinit var artist: TextView
        lateinit var check: SimpleCheckBox
    }

    val selected: ArrayList<Song>?
        get() {
            for (a in mValues) {
                if (a.isChecked) mSelected!!.add(a)
            }
            return mSelected
        }

    fun select(position: Int) {
        val a = getItem(position)
        a.isChecked = !a.isChecked
        notifyDataSetChanged()
    }

    fun clearSelected() {
        mSelected!!.clear()
        mSelected = null
    }

    override fun getCount(): Int {
        return mValues.size
    }

    override fun getItem(p1: Int): Song {
        return mValues[p1]
    }

    override fun getItemId(p1: Int): Long {
        return 0
    }

    init {
        mSelected = ArrayList()
    }
}