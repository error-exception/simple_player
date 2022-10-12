package com.simple.player.adapter

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simple.player.R
import com.simple.player.model.Song
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.view.SimpleCheckBox
import java.util.ArrayList


class PlaylistAdapter2(private val list: AbsPlaylist):
    RecyclerView.Adapter<PlaylistAdapter2.ViewHolder>(),
    View.OnClickListener, View.OnLongClickListener {

    var isSelectionState = false
        set(value) {
            field = value
            if (!value) {
                for (song in list.songList) {
                    song.isChecked = false
                }
                notifyItemRangeChanged(0, itemCount)
            }

        }

    var isSelectAll = false
        private set
    private var playPosition = -1

    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.playlist_adapter_normal_title)
        val artist: TextView = view.findViewById(R.id.playlist_adapter_normal_artist)
        val position: TextView = view.findViewById(R.id.playlist_adapter_normal_position)
        val check: SimpleCheckBox = view.findViewById(R.id.playlist_adapter_normal_check)
        val parent: View = view

        init {
            check.isClickable = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_adapter_normal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list.songList
        val song = data[position]
        with (holder) {
            parent.tag = position
            if (!parent.hasOnClickListeners()) {
                parent.setOnClickListener(this@PlaylistAdapter2)
            }
            parent.setOnLongClickListener(this@PlaylistAdapter2)
            if (isSelectionState) {
                title.setTextColor(-0x1000000)
                artist.setTextColor(-0x7f7f80)
                title.typeface = Typeface.DEFAULT
            } else {
                title.setTextColor(if (position == playPosition) -0x10000 else -0x1000000)
                artist.setTextColor(if (position== playPosition) -0x10000 else -0x7f7f80)
                this.position.setTextColor(if (position == playPosition) -0x10000 else -0x7f7f80)
                title.typeface = if (position == playPosition) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            }
            title.text = song.title
            artist.text = song.artist
            this.position.text = (position + 1).toString()
            if (isSelectionState) {
                check.visibility = View.VISIBLE
                check.isChecked = song.isChecked
                this.position.visibility = View.GONE
            } else {
                check.visibility = View.GONE
                check.isChecked = false
                this.position.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.count
    }

    fun select(position: Int) {
        if (!isSelectionState) return
        val a = getItem(position).isChecked
        getItem(position).isChecked = !a
        notifyItemChanged(position)
    }

    fun getItem(position: Int): Song {
        return list[position]!!
    }


    val selectedSongList: ArrayList<Song>
        get() {
            val a = ArrayList<Song>()
            for (song in list.songList) {
                if (song.isChecked) {
                    a.add(song)
                }
            }
            return a
        }

    fun setPlayingPosition(position: Int) {
        playPosition = position
        notifyItemChanged(position)
    }

    fun remove(song: Song) {
        val position = list.position(song)
        PlaylistManager.removeSong(list.name, song)
        notifyItemRemoved(position)
    }

    fun remove(songArray: Array<Song>) {
        with (PlaylistManager) {
            for (song in songArray) {
                val position = list.position(song)
                notifyItemRemoved(position)
            }
            removeSongs(list.name, songArray)
        }
    }

    fun selectAll() {
        for (p in list.songList) {
            p.isChecked = true
        }
        isSelectAll = true
        notifyItemRangeChanged(0, itemCount)
    }

    fun unselectAll() {
        for (p in list.songList) {
            p.isChecked = false
        }
        isSelectAll = false
        notifyItemRangeChanged(0, itemCount)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View?, position: Int): Boolean
    }

    override fun onClick(v: View?) {
        onItemClickListener?.onItemClick(v, if (v == null) -1 else v.tag as Int)
    }

    override fun onLongClick(v: View?): Boolean {
        return if (onItemLongClickListener == null) {
            false
        } else {
            onItemLongClickListener!!.onItemLongClick(v, if (v == null) -1 else v.tag as Int)
        }
    }

}