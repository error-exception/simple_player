package com.simple.player.adapter

import android.content.Context
import android.widget.ArrayAdapter
import com.simple.player.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import com.simple.player.model.Song
import android.graphics.Typeface
import android.view.View
import com.simple.player.playlist.Playlist
import com.simple.player.view.SimpleCheckBox
import java.util.ArrayList

class PlaylistAdapter(private var con: Context, private var list: Playlist) :
    ArrayAdapter<Song>(con, R.layout.player_list_adapter, list.list) {


    private var isSelectionState = false
    var isSelectAll = false
        private set
    private var playPosition = -1
    override fun getCount(): Int {
        return list.list.size
    }

    override fun getItem(p1: Int): Song {
        return list.list[p1]
    }

    override fun getItemId(p1: Int): Long {
        return 0
    }

    override fun getView(p1: Int, convertView: View?, p3: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder? = null
        if (convertView == null) {
            holder = ViewHolder()
            convertView =
                LayoutInflater.from(context).inflate(R.layout.playlist_adapter_normal, p3, false)
            with (holder) {
                title = convertView.findViewById(R.id.playlist_adapter_normal_title)
                artist = convertView.findViewById(R.id.playlist_adapter_normal_artist)
                check = convertView.findViewById(R.id.playlist_adapter_normal_check)
                position = convertView.findViewById(R.id.playlist_adapter_normal_position)
                check.isClickable = false
            }

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val song = getItem(p1)
        if (isSelectionState) {
            holder.title.setTextColor(-0x1000000)
            holder.artist.setTextColor(-0x7f7f80)
            holder.title.setTypeface(Typeface.DEFAULT)
        } else {
            holder.title.setTextColor(if (p1 == playPosition) -0x10000 else -0x1000000)
            holder.artist.setTextColor(if (p1 == playPosition) -0x10000 else -0x7f7f80)
            holder.position.setTextColor(if (p1 == playPosition) -0x10000 else -0x7f7f80)
            if (p1 == playPosition) holder.title.setTypeface(Typeface.DEFAULT_BOLD) else holder.title.setTypeface(
                Typeface.DEFAULT)
        }
        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.position.text = (p1 + 1).toString()
        if (isSelectionState) {
            holder.check.visibility = View.VISIBLE
            holder.check.isChecked = song.isChecked
            holder.position.visibility = View.GONE
        } else {
            holder.check.visibility = View.GONE
            holder.check.isChecked = false
            holder.position.visibility = View.VISIBLE
        }
        return convertView!!
    }

    inner class ViewHolder {
        lateinit var title: TextView
        lateinit var artist: TextView
        lateinit var position: TextView
        lateinit var check: SimpleCheckBox
    }

    fun select(position: Int) {
        if (!isSelectionState) return
        val a = getItem(position).isChecked
        getItem(position).isChecked = !a
        notifyDataSetChanged()
    }

    val selected: ArrayList<Song>
        get() {
            val a = ArrayList<Song>()
            for (song in list.list) {
                if (song.isChecked) {
                    a.add(song)
                }
            }
            return a
        }

    fun setPlaying(position: Int) {
        playPosition = position
        //getItem(position).isPlaying = true;
    }

    fun setSelectionState(g: Boolean) {
        isSelectionState = g
        if (!isSelectionState) {
            for (song in list.list) {
                song.isChecked = false
            }
            notifyDataSetChanged()
        }
    }

    fun isSelectionState(): Boolean {
        return isSelectionState
    }

    fun remove(song: Song) {
        list -= song
        notifyDataSetChanged()
    }

    fun selectAll() {
        for (p in list.list) {
            p.isChecked = true
        }
        isSelectAll = true
        notifyDataSetChanged()
    }

    fun cancelSelectAll() {
        for (p in list.list) {
            p.isChecked = false
        }
        isSelectAll = false
        notifyDataSetChanged()
    }

    override fun clear() {}
}