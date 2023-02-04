package com.simple.player.adapter

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.player.R
import com.simple.player.activity.BaseActivity2
import com.simple.player.model.Song
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager



class PlaylistAdapter2(private val list: AbsPlaylist):
    RecyclerView.Adapter<PlaylistAdapter2.ViewHolder>(),
    View.OnClickListener, View.OnLongClickListener {

    private val selectMap = LongSparseArray<Boolean>()
    private var playPosition = -1

    var isSelectionState = false
        set(value) {
            field = value
            if (selectMap.isNotEmpty()) {
                selectMap.clear()
            }
            notifyItemsChange()
        }
    var isSelectAll = false
        private set

    var onItemClick: ((position: Int) -> Unit)? = null
    var onItemLongClick: ((position: Int) -> Boolean)? = null

    var primaryColor: Int = BaseActivity2.primaryColor
        set(value) {
            field = value
            notifyItemChanged(playPosition)
            ViewHolder.checkBoxColorStateList = ColorStateList(
                Array(2) {
                    if (it != 1) {
                        intArrayOf(android.R.attr.state_checked)
                    } else {
                        intArrayOf()
                    }
                },
                intArrayOf(value, 0xff808080.toInt())
            )
        }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.playlist_adapter_normal_title)
        val artist: TextView = view.findViewById(R.id.playlist_adapter_normal_artist)
        val position: TextView = view.findViewById(R.id.playlist_adapter_normal_position)
        val check: AppCompatCheckBox = view.findViewById(R.id.playlist_adapter_normal_check)
        val parent: View = view

        init {
            check.buttonTintList = checkBoxColorStateList
            check.isClickable = false
        }

        companion object {
            var checkBoxColorStateList = ColorStateList(
                Array(2) {
                    if (it != 1) {
                        intArrayOf(android.R.attr.state_checked)
                    } else {
                        intArrayOf()
                    }
                },
                intArrayOf(BaseActivity2.primaryColor, 0xff808080.toInt())
            )
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
            if (
                parent.getTag(R.integer.is_clicked) == null
                || !(parent.getTag(R.integer.is_clicked) as Boolean)
            ) {
                parent.setOnLongClickListener(this@PlaylistAdapter2)
                parent.setTag(R.integer.is_clicked, true)
            }
            if (isSelectionState) {
                updateSelectItem(song, holder)
            } else {
                updateItem(position, holder)
            }
            title.text = song.title
            artist.text = song.artist
            this.position.text = (position + 1).toString()
        }
    }

    override fun getItemCount(): Int {
        return list.count
    }

    fun select(position: Int) {
        if (!isSelectionState) return
        val songId = getItemId(position)
        var state = selectMap[songId]
        state = state == null || !state
        selectMap.put(songId, state)
        notifyItemChanged(position)
    }

    fun getItem(position: Int): Song {
        return list[position]!!
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    fun getSelectedSongList(): ArrayList<Song> {
        val selectedList = ArrayList<Song>()
        if (isSelectAll) {
            val songList = list.songList
            selectedList.addAll(songList)
        } else {
            selectMap.forEach { key, value ->
                val song = list[key]
                if (song == null || !value) {
                    return@forEach
                }
                selectedList.add(song)
            }
        }
        return selectedList
    }

    fun setPlayingPosition(lastPosition: Int, position: Int) {
        playPosition = position
        if (lastPosition >= 0)
            notifyItemChanged(lastPosition)
        else
            notifyItemsChange()
        notifyItemChanged(position)
    }

    fun remove(song: Song) {
        val position = list.position(song)
        PlaylistManager.removeSong(list.name, song)
        notifyItemRemoved(position)
    }

    fun remove(songArray: ArrayList<Song>) {
        for (song in songArray) {
            val position = list.position(song)
            notifyItemRemoved(position)
        }
        PlaylistManager.removeSongs(list.name, songArray.toTypedArray())
    }

    fun selectAll() {
        isSelectAll = true
        notifyItemsChange()
    }

    fun unselectAll() {
        isSelectAll = false
        notifyItemsChange()
    }

    override fun onClick(v: View?) {
        onItemClick?.invoke(if (v == null) -1 else v.tag as Int)
    }

    override fun onLongClick(v: View?): Boolean {
        return onItemLongClick?.invoke(if (v == null) -1 else v.tag as Int) ?: false
    }

    private fun View.gone() {
        visibility = View.GONE
    }

    private fun View.visible() {
        visibility = View.VISIBLE
    }

    private fun updateSelectItem(song: Song, holder: ViewHolder) {
        with(holder) {
            title.setTextColor(-0x1000000)
            artist.setTextColor(-0x7f7f80)
            title.typeface = Typeface.DEFAULT
            check.visible()
            check.isChecked = isSelectAll || selectMap[song.id] ?: false
            this.position.gone()
        }
    }

    private fun updateItem(position: Int, holder: ViewHolder) {
        with(holder) {
            val isPlayingPosition = position == playPosition;
            title.setTextColor(if (isPlayingPosition) primaryColor else -0x1000000)
            artist.setTextColor(if (isPlayingPosition) primaryColor else -0x7f7f80)
            this.position.setTextColor(if (isPlayingPosition) primaryColor else -0x7f7f80)
            title.typeface = if (isPlayingPosition)
                Typeface.DEFAULT_BOLD
            else
                Typeface.DEFAULT
            check.gone()
            check.isChecked = false
            this.position.visible()
        }
    }

    private var linearLayoutManager: LinearLayoutManager? = null

    fun setLinearLayoutManager(linearLayoutManager: LinearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager
    }

    private fun notifyItemsChange() {
        val manager = linearLayoutManager
        if (manager != null) {
            val firstPosition =
                manager.findFirstVisibleItemPosition()
            val lastPosition =
                manager.findLastVisibleItemPosition()
            notifyItemRangeChanged(firstPosition, lastPosition - firstPosition + 1)
        } else {
            notifyItemRangeChanged(0, itemCount)
        }
    }

    companion object {
        private const val TAG = "PlaylistAdapter2"
    }


}