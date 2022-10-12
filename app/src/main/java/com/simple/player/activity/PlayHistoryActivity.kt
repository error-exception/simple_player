package com.simple.player.activity

import android.content.DialogInterface
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.MusicEvent
import com.simple.player.MusicEventHandler
import com.simple.player.R
import com.simple.player.Util
import com.simple.player.model.Song
import com.simple.player.playlist.HistoryListManager
import com.simple.player.playlist.PlaylistManager
import com.simple.player.ui.theme.ComposeTestTheme
import com.simple.player.ui.theme.Gary
import com.simple.player.ui.theme.windowBackground
import com.simple.player.util.DialogUtil
import com.simple.player.util.ProgressHandler

class PlayHistoryActivity: AppCompatActivity(), MusicEvent.OnHistoryChangedListener {

    private var isListLoaded = false
//    private lateinit var recyclerView: RecyclerView

    private var backIcon = mutableStateOf(R.drawable.ic_baseline_arrow_back_24)
    private var optionIcon = mutableStateOf(R.drawable.ic_baseline_delete_24)
    private var title = mutableStateOf("播放历史")
    private var list = mutableStateListOf<Song>(*(ArrayList<Song>().toTypedArray()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicEventHandler.register(this)
        updateHistoryList()
        setContent {
            ComposeTestTheme {
                Column (modifier = Modifier
                    .fillMaxSize()
                    .background(windowBackground)) {
                    Toolbar(
                        title = title,
                        backIcon = backIcon,
                        optionIcon = optionIcon,
                        backClick = { finish() },
                        optionClick = clearHistory
                    )
                    HistoryList(list)

                }
            }
        }
//        setContentView(R.layout.play_list)
//        optionIcon = IconCode.ICON_DELETE

//        recyclerView = findViewById(R.id.play_list_fragment_list)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        val fastScrollbarThumb = ResourcesCompat.getDrawable(resources, R.drawable.fast_scrollbar_thumb, null)
//        val verticalTrackDrawable: Drawable = ColorDrawable(Color.TRANSPARENT)
//        val horizontalThumbDrawable = fastScrollbarThumb as StateListDrawable
//        val horizontalTrackDrawable: Drawable = ColorDrawable(Color.TRANSPARENT)
//        recyclerView.addItemDecoration(
//            FastScroller2(
//                recyclerView, fastScrollbarThumb, verticalTrackDrawable,
//                horizontalThumbDrawable, horizontalTrackDrawable,
//                resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
//                resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
//                resources.getDimensionPixelOffset(R.dimen.fastscroll_margin)
//            )
//        )
//        findViewById<View>(R.id.play_list_fragment_play_position).visibility = View.GONE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !isListLoaded) {
            updateHistoryList()
            isListLoaded = true
        }
    }

    private val clearHistory: () -> Unit = {
        DialogUtil.confirm(this, "提示", "是否清空播放记录？", positive = { dialogInterface: DialogInterface, i: Int ->
            ProgressHandler.handle(
                before = {
                    Util.showProgressDialog(this@PlayHistoryActivity, 12, "正在清除...")
                },
                handle = {
                    HistoryListManager.clearHistoryList()
                },
                after = {
                    Util.closeProgressDialog(12)
                    updateHistoryList()
                }
            )
        }, negative = null)
    }

    // 添加历史记录是异步的，建议用此回调来更新历史记录
    override fun onHistoryChangedListener(newSongId: Long) {
        updateHistoryList()
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicEventHandler.unregister(this)
    }

    private fun updateHistoryList() {
        val playlist = PlaylistManager.getHistoryList()
//        recyclerView.adapter = PlaylistAdapter2(playlist)
        list.clear()
        for (song in playlist.songList) {
            list += song
        }
    }
}

@Composable
fun HistoryList(historyList: SnapshotStateList<Song>) {
    LazyColumn {
        itemsIndexed(historyList) { i: Int, song: Song ->
            Row (modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .background(Color.White)
                .clickable(true) {}) {
                Row(modifier = Modifier.size(56.dp, 64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = "${i + 1}", color = Gary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column (modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp), verticalArrangement = Arrangement.Center) {
                    Text(text = song.title, color = Color.Black, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = song.artist, color = Gary, fontSize = 12.sp)
                }
            }
        }
    }
}
