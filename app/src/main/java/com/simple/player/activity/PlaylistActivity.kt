package com.simple.player.activity


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simple.player.*
import com.simple.player.Util.toast
import com.simple.player.adapter.DialogListAdapter
import com.simple.player.adapter.PlaylistAdapter2
import com.simple.player.constant.IconCode
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.model.IconWithText
import com.simple.player.model.Song
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import com.simple.player.service.SimpleService
import com.simple.player.util.DialogUtil
import com.simple.player.view.FastScroller2

class PlaylistActivity : BaseActivity(), PlaylistAdapter2.OnItemLongClickListener,
    PlaylistAdapter2.OnItemClickListener,
    MusicEventListener {

    private val TAG = "PlaylistActivity"

    private lateinit var mList: RecyclerView
    private lateinit var mPlaylist: AbsPlaylist
    private lateinit var mAdapter: PlaylistAdapter2
    private val player = SimplePlayer

    private var position = 0

    companion object {
        const val EXTRA_LIST_NAME = "list_name"
        val MULTI_SELECT_MENU_IN_FAVORITE = arrayOf("移除", "添加至")
        val MULTI_SELECT_MENU = arrayOf("移除", "添加到我喜欢", "添加至")
        private val MENU_ITEMS = arrayListOf(
            IconWithText(IconCode.ICON_PLAY_ARROW, "播放"),
            IconWithText(IconCode.ICON_REMOVE, "移除"),
            IconWithText(IconCode.ICON_INFO_OUTLINE, "信息"),
            IconWithText(IconCode.ICON_CHECK, "多选"),
            IconWithText(IconCode.ICON_FAVORITE, "添加到我喜欢"),
            IconWithText(IconCode.ICON_ADD, "添加至"),
        )

        private val MENU = MENU_ITEMS.toTypedArray()

        private val MENU_IN_FAVORITE = ArrayList(MENU_ITEMS).apply {
            remove(MENU_ITEMS[4])
        }.toTypedArray()

    }

    //在进入多选状态时，标记歌曲是已经结束并进入下一曲，以便于后续重新定位歌曲位置
    private var isNewSong = false
    private var isInitialed = false
    private fun initBase() {
        if (isInitialed) {
            return
        }
        position = mPlaylist.position(player.currentSong)
        mList.adapter = mAdapter
        mAdapter.setOnItemClickListener(this)
        mAdapter.setOnItemLongClickListener(this)

        if (player.activePlaylist == mPlaylist) {
            mAdapter.setPlayingPosition(position)
            mList.scrollToPosition(if (position - 3 < 0) 0 else position - 3)
        }
        MusicEvent2.register(this)
        isInitialed = true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            initBase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val name = intent.getStringExtra(EXTRA_LIST_NAME)
        mPlaylist = PlaylistManager.getList(name!!)!!

        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_list)

        val titleName = when (mPlaylist.name) {
            PlaylistManager.LOCAL_LIST -> "播放列表"
            PlaylistManager.FAVORITE_LIST -> "我喜欢"
            else -> mPlaylist.name
        }
        actionTitle = titleName

        mList = findViewById(R.id.play_list_fragment_list)
        val linearLayoutManager = LinearLayoutManager(this)
        mList.layoutManager = linearLayoutManager
        mList.itemAnimator?.changeDuration = 0L
        val fastScrollbarThumb = ResourcesCompat.getDrawable(resources, R.drawable.fast_scrollbar_thumb, null)
        val verticalTrackDrawable = ColorDrawable(Color.TRANSPARENT)
        val horizontalThumbDrawable = fastScrollbarThumb as StateListDrawable
        val horizontalTrackDrawable = ColorDrawable(Color.TRANSPARENT)
        mList.addItemDecoration(
            FastScroller2(
                mList, fastScrollbarThumb, verticalTrackDrawable,
                horizontalThumbDrawable, horizontalTrackDrawable,
                resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
                resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
                resources.getDimensionPixelOffset(R.dimen.fastscroll_margin)
            )
        )
        mAdapter = PlaylistAdapter2(mPlaylist)

        findViewById<View>(R.id.play_list_fragment_play_position).setOnClickListener {
            val first = linearLayoutManager.findFirstVisibleItemPosition()
            val last = linearLayoutManager.findLastVisibleItemPosition()
            if (position in first..last) {
                return@setOnClickListener
            }
            if (position < first && position < last) {
                mList.smoothScrollToPosition(if (position - 3 < 0) 0 else position - 3)
            } else {
                mList.smoothScrollToPosition(if (position - 3 < 0) 0 else position + 4)
            }
        }
    }

    override fun onSongChanged(newSongId: Long) {
        if (mAdapter.isSelectionState) {
            isNewSong = true
            return
        }
        if (mPlaylist.id != SimplePlayer.activePlaylist.id) {
            return
        }
        clearItemView()
        position = mPlaylist.position(mPlaylist[newSongId]!!)
        mAdapter.setPlayingPosition(position)
    }

    override fun onOptionPressed() {
        cancelSelection()
        backIcon = R.drawable.ic_baseline_arrow_back_24
        actionTitle = when (mPlaylist.name) {
            PlaylistManager.LOCAL_LIST -> "播放列表"
            PlaylistManager.FAVORITE_LIST -> "我喜欢"
            else -> mPlaylist.name
        }
        // 重新定位歌曲
        if (isNewSong) {
            isNewSong = false
            val position = mPlaylist.position(player.currentSong)
            mAdapter.setPlayingPosition(position)
        }
    }

    override fun onActionBarBackPressed() {
        if (mAdapter.isSelectionState) {
            if (mAdapter.isSelectAll) {
                mAdapter.unselectAll()
                backIcon = R.drawable.ic_baseline_radio_button_unchecked_24
                actionTitle = "全选"
            } else {
                mAdapter.selectAll()
                backIcon = R.drawable.ic_baseline_check_circle_24
                actionTitle = "全不选"
            }
            return
        }
        super.onActionBarBackPressed()
    }

    private fun handleMultiChecks() {
        val items: Array<String> = if (mPlaylist.name == PlaylistManager.FAVORITE_LIST) {
            MULTI_SELECT_MENU_IN_FAVORITE
        } else {
            MULTI_SELECT_MENU
        }
        DialogUtil.simpleList(this, items) { _, p2 ->
            when (items[p2]) {
                "移除" -> {
                    if (mAdapter.selectedSongList.size == 0) {
                        toast("无已选择的歌曲")
                        return@simpleList
                    }
                    showRemoveDialog()
                }
                "添加至" -> {
                    if (mAdapter.selectedSongList.size == 0) {
                        toast("无已选择的歌曲")
                        return@simpleList
                    }
                    val selectedList: ArrayList<Song> = mAdapter.selectedSongList
                    addToList(selectedList.toTypedArray())
                }
                "添加到我喜欢" -> {
                    if (mAdapter.selectedSongList.size == 0) {
                        toast("无已选择的歌曲")
                        return@simpleList
                    }
                    val selectedList: ArrayList<Song> = mAdapter.selectedSongList
                    with (PlaylistManager) {
                        addSongs(FAVORITE_LIST, selectedList.toTypedArray())
                    }
                    toast("已添加")
                    onOptionPressed()
                }
            }
        }
    }

    private fun cancelSelection() {
        mAdapter.isSelectionState = false
        mAdapter.selectedSongList.clear()
        optionIcon = null
    }

    private fun showRemoveDialog() {
        DialogUtil.confirm(this,
            "提示",
            "将要移除已选的 ${mAdapter.selectedSongList.size} 首歌曲",
            null
        ) { _, _ ->
            val data = mAdapter.selectedSongList
            mAdapter.remove(data.toTypedArray())
            onOptionPressed()
            toast("已移除")
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        val s = mPlaylist[position]!!
        if (mAdapter.isSelectionState) {
            mAdapter.select(position)
            return
        }
        if (s == player.currentSong) {
            player.startOrPause(false)
        } else {
            player.activePlaylist = mPlaylist
            player.loadMusicOrStart(s, isNoFade = true)
            mAdapter.setPlayingPosition(position)
            this.position = position
        }
    }

    override fun onItemLongClick(view: View?, position: Int): Boolean {
        if (mAdapter.isSelectionState) {
            handleMultiChecks()
            return true
        }
        val arr: Array<IconWithText> =
            if (mPlaylist.name == PlaylistManager.FAVORITE_LIST) {
                MENU_IN_FAVORITE
            } else {
                MENU
            }
        val adapter = DialogListAdapter(this, arr)
        DialogUtil.list(this, adapter) { p11, p21 -> handleOption(position, arr[p21]) }
        return true
    }

    private fun handleOption(position: Int, item: IconWithText) {
        val song: Song = mAdapter.getItem(position)
        when (item.text) {
            "播放" -> {
                if (song == player.currentSong) {
                    with (player) {
                        if (isPlaying)
                            pause()
                        else
                            start(true)
                    }
                } else {
                    player.activePlaylist = mPlaylist
                    player.loadMusicOrStart(song, isNoFade = true)
                    mAdapter.setPlayingPosition(position)
                    this.position = position
                }
                clearItemView()
            }
            "移除" -> {
                PlaylistManager.removeSong(mPlaylist.name, song)
                clearItemView()
                //重新定位当前歌曲
                val pos = mPlaylist.position(player.currentSong)
                mAdapter.setPlayingPosition(pos)
                toast("已移除")
            }
            "信息" -> {
                val info = Intent(this, MusicInfo2::class.java)
                info.putExtra(MusicInfo2.EXTRA_MUSIC_ID, song.id)
                clearItemView()
                startActivity(info)
            }
            "添加至" -> {
                addToList(arrayOf(song))
            }
            "多选" -> {
                clearItemView()
                mAdapter.isSelectionState = true
                optionIcon = R.drawable.ic_baseline_close_24
                backIcon = R.drawable.ic_baseline_radio_button_unchecked_24
                actionTitle = "全选"
            }
            "添加到我喜欢" -> {
                with(PlaylistManager.favoriteList) {
                    if (hasSong(song)) {
                        toast("该歌曲已存在")
                        return@with
                    }
                    PlaylistManager.addSong(PlaylistManager.FAVORITE_LIST, song)
                    toast("已添加")
                }
            }
        }
    }

    private fun addToList(song: Array<Song>) {
        val playlist = PlaylistManager.allCustomLists()
        val names = Array(playlist.size) { index ->
            playlist[index].name
        }
        DialogUtil.simpleList(this, names) { _, p2 ->
            val name = names[p2]
            PlaylistManager.addSongs(name, song)
            toast("添加完成")
            if (mAdapter.isSelectionState) {
                onOptionPressed()
            }
        }
    }

    private fun clearItemView() {
        for (song in mPlaylist.songList) {
            song.isChecked = false
            song.isPlaying = false
        }
        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearItemView()

        MusicEvent2.unregister(this)
        System.gc()
    }
}