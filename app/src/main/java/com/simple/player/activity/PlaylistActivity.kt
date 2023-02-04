package com.simple.player.activity


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.simple.player.*
import com.simple.player.Util.toast
import com.simple.player.adapter.PlaylistAdapter2
import com.simple.player.constant.IconCode
import com.simple.player.event.MusicEvent2
import com.simple.player.event.MusicEventListener
import com.simple.player.model.Song
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.PlaylistManager
import com.simple.player.service.SimplePlayer
import com.simple.player.util.DialogUtil
import com.simple.player.view.BottomSheetConfirmDialog
import com.simple.player.view.BottomSheetListDialog
import com.simple.player.view.BottomSheetSimpleIconListAdapter
import com.simple.player.view.FastScroller2

class PlaylistActivity : BaseActivity(),
//    PlaylistAdapter2.OnItemLongClickListener,
//    PlaylistAdapter2.OnItemClickListener,
    MusicEventListener {

    private val TAG = "PlaylistActivity"

    private lateinit var mList: RecyclerView
    private lateinit var mPlaylist: AbsPlaylist
    private lateinit var mAdapter: PlaylistAdapter2
    private val player = SimplePlayer

    private var position = 0

    companion object {
        const val EXTRA_LIST_NAME = "list_name"
        val MULTI_SELECT_MENU_IN_FAVORITE = arrayListOf(
            IconWithText(R.drawable.baseline_remove_circle_24, "移除"),
            IconWithText(R.drawable.ic_baseline_add_24, "添加至")
        )
        val MULTI_SELECT_MENU = arrayListOf(
            IconWithText(R.drawable.baseline_remove_circle_24, "移除"),
            IconWithText(R.drawable.ic_baseline_favorite_24, "添加到我喜欢"),
            IconWithText(R.drawable.ic_baseline_add_24, "添加至")
        )
        val MENU = arrayListOf(
            IconWithText(R.drawable.ic_play_dark, "播放"),
            IconWithText(R.drawable.baseline_remove_circle_24, "移除"),
            IconWithText(R.drawable.ic_outline_info_24, "信息"),
            IconWithText(R.drawable.ic_baseline_check_24, "多选"),
            IconWithText(R.drawable.ic_baseline_favorite_24, "添加到我喜欢"),
            IconWithText(R.drawable.ic_baseline_add_24, "添加至"),
        )

        val MENU_IN_FAVORITE = ArrayList<IconWithText>().apply {
            addAll(MENU)
            removeAt(4)
        }

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
        setListListener()
        if (player.activePlaylist == mPlaylist) {
            mAdapter.setPlayingPosition(lastPosition = -1, position)
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
        fastScrollbarThumb.setTint(BaseActivity2.primaryColor)
        mList.addItemDecoration(
            FastScroller2(
                mList, fastScrollbarThumb, verticalTrackDrawable,
                horizontalThumbDrawable, horizontalTrackDrawable,
                resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
                resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
                resources.getDimensionPixelOffset(R.dimen.fastscroll_margin)
            )
        )
        mAdapter = PlaylistAdapter2(mPlaylist).apply {
            setLinearLayoutManager(linearLayoutManager)
        }

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
        findViewById<FloatingActionButton>(R.id.play_list_fragment_to_top).setOnClickListener {
            mList.scrollToPosition(0)
        }
        initTheme()
    }

    override fun onSongChanged(newSongId: Long) {
        if (mAdapter.isSelectionState) {
            isNewSong = true
            return
        }
        if (mPlaylist.id != SimplePlayer.activePlaylist.id) {
            return
        }
        val newPosition = mPlaylist.position(mPlaylist[newSongId]!!)
        mAdapter.setPlayingPosition(position, newPosition)
        position = newPosition
    }

    private fun setListListener() {
        mAdapter.onItemClick = {
            val s = mPlaylist[it]!!
            if (mAdapter.isSelectionState) {
                mAdapter.select(it)
            } else {
                if (s == player.currentSong) {
                    player.startOrPause(false)
                } else {
                    player.activePlaylist = mPlaylist
                    player.loadMusicOrStart(s, isNoFade = true)
                    mAdapter.setPlayingPosition(lastPosition = position, position = it)
                    this.position = it
                }
            }
        }
        mAdapter.onItemLongClick = onItemLongClick@{
            if (mAdapter.isSelectionState) {
                handleMultiChecks()
                return@onItemLongClick true
            }
            val list = if (mPlaylist.name == PlaylistManager.FAVORITE_LIST) {
                MENU_IN_FAVORITE
            } else {
                MENU
            }
            createDialogAndShow(list) { _, index ->
                handleOption(position = it, item = list[index])
            }
            return@onItemLongClick true
        }
    }

    override fun onOptionPressed() {
        cancelSelection()
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
        val items = if (mPlaylist.name == PlaylistManager.FAVORITE_LIST) {
            MULTI_SELECT_MENU_IN_FAVORITE
        } else {
            MULTI_SELECT_MENU
        }
        createDialogAndShow(items) { _, index ->
            val selectedList = mAdapter.getSelectedSongList()
            when (items[index].second) {
                "移除" -> {
                    if (selectedList.isEmpty()) {
                        toast("无已选择的歌曲")
                    } else {
                        showRemoveDialog(selectedList = selectedList)
                    }
                }
                "添加至" -> {
                    if (selectedList.isEmpty()) {
                        toast("无已选择的歌曲")
                    } else {
                        addToList(selectedList.toTypedArray())
                    }
                }
                "添加到我喜欢" -> {
                    if (selectedList.isEmpty()) {
                        toast("无已选择的歌曲")
                    } else {
                        PlaylistManager.addSongs(
                            PlaylistManager.FAVORITE_LIST,
                            selectedList.toTypedArray()
                        )
                        toast("已添加")
                        onOptionPressed()
                    }
                }
            }

        }
    }

    private fun cancelSelection() {
        mAdapter.isSelectionState = false
        optionIcon = null
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
            mAdapter.setPlayingPosition(lastPosition = -1, position)
        }
    }

    private fun showRemoveDialog(selectedList: ArrayList<Song>) {
        BottomSheetConfirmDialog.showDialog(
            context = this,
            title = "提示",
            message = "将要移除已选的 ${selectedList.size} 首歌曲",
            onPositive = {
                if (selectedList.size == 1) {
                    mAdapter.remove(selectedList[0])
                } else {
                    mAdapter.remove(selectedList)
                }
                onOptionPressed()
                toast("已移除")
            }
        )
    }

    private fun handleOption(position: Int, item: IconWithText) {
        val song: Song = mAdapter.getItem(position)
        when (item.second) {
            "播放" -> {
                if (song == player.currentSong) {
                    player.startOrPause(isNoFade = true)
                } else {
                    player.activePlaylist = mPlaylist
                    player.loadMusicOrStart(song, isNoFade = true)
                    mAdapter.setPlayingPosition(lastPosition = this.position, position = position)
                    this.position = position
                }
            }
            "移除" -> {
                PlaylistManager.removeSong(mPlaylist.name, song)
                //重新定位当前歌曲
                val pos = mPlaylist.position(player.currentSong)
                mAdapter.setPlayingPosition(-1, pos)
                toast("已移除")
            }
            "信息" -> {
                val info = Intent(this, MusicInfo2::class.java)
                info.putExtra(MusicInfo2.EXTRA_MUSIC_ID, song.id)
                startActivity(info)
            }
            "添加至" -> {
                addToList(arrayOf(song))
            }
            "多选" -> {
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
        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount)
    }

    private fun createDialogAndShow(
        dataList: List<Pair<Int, String>>,
        onItemClick: ((view: View, position: Int) -> Unit)
    ) {
        val adapter = BottomSheetSimpleIconListAdapter(dataList = dataList)
        adapter.onItemClick = onItemClick
        val dialog = BottomSheetListDialog(this)
        dialog.setAdapter(adapter = adapter)
        dialog.show()
    }

    private fun initTheme() {
        val primaryColor = BaseActivity2.primaryColor
        window.statusBarColor = primaryColor
        toolbar.setBackgroundColor(primaryColor)
        mAdapter.primaryColor = primaryColor
        findViewById<FloatingActionButton>(R.id.play_list_fragment_play_position).apply {
            backgroundTintList = ColorStateList.valueOf(primaryColor)
            setColorFilter(Color.WHITE)
        }
        findViewById<FloatingActionButton>(R.id.play_list_fragment_to_top).apply {
            backgroundTintList = ColorStateList.valueOf(primaryColor)
            setColorFilter(Color.WHITE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearItemView()

        MusicEvent2.unregister(this)
        System.gc()
    }

    override fun onBackPressed() {
        if (mAdapter.isSelectionState) {
            cancelSelection()
        } else {
            super.onBackPressed()
        }
    }
}

typealias IconWithText = Pair<Int, String>