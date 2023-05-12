package com.simple.player.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomDrawer
import androidx.compose.material.BottomDrawerState
import androidx.compose.material.BottomDrawerValue
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberBottomDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.R
import com.simple.player.activity.NewPlaylistActivity
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.playlist.SongList
import com.simple.player.service.SimplePlayer
import com.simple.player.ui.theme.ColumnSpace
import com.simple.player.ui.theme.windowBackgroundAlpha
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log

class PlaylistScreen(private val activity: NewPlaylistActivity) {

    private val listName = mutableStateOf("播放列表")
//    private val selectionMode = mutableStateOf(false)
    private val isSelectAll = mutableStateOf(false)
    private val selectMap = HashMap<Int, MutableState<Boolean>>()
    private val playPosition = mutableStateOf(0)
    private val list = mutableStateListOf<PlaylistItem>()
    private var clickItemIndex = -1
    private var menuType = mutableStateOf(1)

    private val normalMenuList = listOf(
        R.drawable.ic_play_dark to "播放",
        R.drawable.baseline_remove_circle_24 to "移除",
        R.drawable.ic_baseline_favorite_24 to "添加至我喜欢",
        R.drawable.ic_baseline_add_24 to "添加至",
        R.drawable.ic_outline_info_24 to "详细信息"
    )

    private val multiSelectMenuList = listOf(
        R.drawable.baseline_remove_circle_24 to "移除",
        R.drawable.ic_baseline_favorite_24 to "添加至我喜欢",
        R.drawable.ic_baseline_add_24 to "添加至"
    )

    private val playlists = mutableStateListOf<Pair<Int, String>>()

    var onItemClick: ((position: Int) -> Unit)? = null
    var onNormalMenuItemClick: ((position: Int, listIndex: Int) -> Unit)? = null
    var onMultiSelectMenuItemClick: ((position: Int, listIndex: Int) -> Unit)? = null
    var onPlaylistSelectClick: ((position: Int, name: String) -> Unit)? = null

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ComposeContent() {
        var type by remember {
            menuType
        }
        val bottomDrawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
        LaunchedEffect(key1 = type) {
            if (type == 2) {
                bottomDrawerState.open()
            }
        }
        BottomDrawer(
            drawerShape = RoundedCornerShape(topEnd = 12.dp, topStart = 12.dp),
            drawerContent = {
                BottomMenu { bottomDrawerState }
            },
            gesturesEnabled = bottomDrawerState.isOpen,
            drawerState = bottomDrawerState,
            content = {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text(text = if (type == 0) "全选" else listName.value) },
                        navigationIcon = {
                            if (type == 0) {
                                RadioButton(
                                    selected = isSelectAll.value,
                                    onClick = { isSelectAll.value = !isSelectAll.value },
                                    colors = RadioButtonDefaults.colors(
                                        unselectedColor = Color.White,
                                        selectedColor = Color.White
                                    )
                                )
                            } else {
                                IconButton(onClick = { activity.finish() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                                        contentDescription = "back"
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    type = if (type == 0) 1 else 0
                                    if (type == 0) {
                                        for (mutableEntry in selectMap) {
                                            mutableEntry.value.value = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = if (type == 1) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_close_24),
                                    contentDescription = "select",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                    Content { bottomDrawerState }
                }

            }
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun Content(function: () -> BottomDrawerState) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val state = ListContent(function)
            val scope = rememberCoroutineScope()
            Box(modifier = Modifier
                .padding(end = 16.dp, bottom = 16.dp)
                .align(Alignment.BottomEnd)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                state.scrollToItem(0)
                            }
                        }
                    ) {
                        Icon(painter = painterResource(id = R.drawable.baseline_keyboard_arrow_up_24), contentDescription = "to top")
                    }
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                state.scrollToItem(playPosition.value)
                            }
                        }
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_baseline_my_location_24), contentDescription = "locate")
                    }
                }
            }

        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun ListContent(function: () -> BottomDrawerState): LazyListState {
        val state = rememberLazyListState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ColumnSpace(height = 8.dp)
                }
                items(count = list.size, key = { list[it].id }) { index ->
                    ListItem(position = index, item = list[index], function = function)
                }
                item {
                    ColumnSpace(height = 8.dp)
                }
            }
//            ScrollBar { state }
        }
        LaunchedEffect(key1 = Unit, block = {
            state.scrollToItem(playPosition.value)
        })
        return state
    }

    @Composable
    fun ScrollBar(state: () -> LazyListState) {
        // space 22
        // 194
        state().layoutInfo.visibleItemsInfo[0].offset
        val firstVisibleItemIndex = state().firstVisibleItemIndex
        val total = (firstVisibleItemIndex - 1) * if (state().layoutInfo.visibleItemsInfo.isNotEmpty()) state().layoutInfo.visibleItemsInfo[0].size else 56
        Text(text = total.toString())
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
    @Composable
    private fun ListItem(
        position: Int = 0,
        item: PlaylistItem,
        function: () -> BottomDrawerState
    ) {
        val scope = rememberCoroutineScope()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = {
                        if (menuType.value == 0) {
                            if (selectMap[position] == null) {
                                selectMap[position] = mutableStateOf(true)
                            } else {
                                selectMap[position]!!.value = !selectMap[position]!!.value
                            }
                        } else {
                            onItemClick?.invoke(position)
                        }
                    },
                    onLongClick = {
                        clickItemIndex = position
                        scope.launch {
                            val state = function()
                            if (state.isExpanded) {
                                state.close()
                            } else {
                                state.open()
                            }
                        }
                    }
                ),
            color = windowBackgroundAlpha,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListItemLeft(position = position)
                Column(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
//                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                ) {
                    SongTitle(title = item.title, position = position)
                    SongArtist(artist = item.artist, position = position)
                }
            }
        }

    }

    @Composable
    private fun ListItemLeft(position: Int) {
        val checked = remember {
            val c = selectMap[position]
            if (c == null) {
                selectMap[position] = mutableStateOf(false)
            }
            selectMap[position]
        }
        AnimatedVisibility(
            visible = menuType.value == 0
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 28.dp)
                    .padding(end = 16.dp),
            ) {
                Checkbox(checked = checked!!.value, onCheckedChange = {
                    checked.value = it
                    selectMap[position] = checked
                })
            }
        }

    }

    @Composable
    private fun SongTitle(position: Int = 0, modifier: Modifier = Modifier, title: String) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = if (playPosition.value == position && menuType.value == 1) MaterialTheme.colors.primary else Color.Black,
            maxLines = 1,
//            fontWeight = if (playPosition.value == position && !selectionMode.value) FontWeight.Bold else FontWeight.Normal
            fontWeight = FontWeight.Bold
        )
    }

    @Composable
    fun SongArtist(position: Int = 0, modifier: Modifier = Modifier, artist: String) {

//        AnimatedVisibility(visible = playing) {

            Text(
                text = artist,
                color = if (playPosition.value == position && menuType.value == 1) MaterialTheme.colors.primary else Color(0xFF808080),
                maxLines = 1,
                fontSize = 12.sp
            )
//        }
    }

    @Composable
    private fun PositionLabel(position: Int) {
        Text(
            text = position.toString(),
            color = if (playPosition.value == position) MaterialTheme.colors.primary else Color(0xFF808080),
            textAlign = TextAlign.Center
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun BottomMenu(function: () -> BottomDrawerState) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier
                .size(width = 80.dp, height = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray)
            )
            ColumnSpace(height = 16.dp)
            when (menuType.value) {
                0 -> MultiSelectMenu(function = function)
                1 -> NormalMenu(function = function)
                2 -> Playlists(function = function)
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun MultiSelectMenu(function: () -> BottomDrawerState) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((index, item) in multiSelectMenuList.withIndex()) {
                BottomMenuItem(item = item, position = index, function = function)
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun NormalMenu(function: () -> BottomDrawerState) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((index, item) in normalMenuList.withIndex()) {
                BottomMenuItem(item = item, position = index, function = function)
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun Playlists(function: () -> BottomDrawerState) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((index, item) in playlists.withIndex()) {
                BottomMenuItem(item = item, position = index, function = function)
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun BottomMenuItem(
        item: Pair<Int, String>,
        position: Int,
        function: () -> BottomDrawerState
    ) {
        val scope = rememberCoroutineScope()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    MainScope().launch {
                        val job = scope.launch {
                            function().close()
                        }
                        job.join()
                        when (menuType.value) {
                            0 -> {
                                onMultiSelectMenuItemClick?.invoke(position, clickItemIndex)
                            }

                            1 -> {
                                onNormalMenuItemClick?.invoke(position, clickItemIndex)
                            }

                            else -> {
                                onPlaylistSelectClick?.invoke(position, item.second)
                                menuType.value = 1
                            }
                        }
                    }

                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(painter = painterResource(id = item.first), contentDescription = "icon", tint = MaterialTheme.colors.primary)
                Text(
                    text = item.second,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }

    fun setPlayingPosition(position: Int) {
        playPosition.value = position
    }

    fun setListSource(playlist: SongList) {
        val songList = playlist.rawList()
        for (song in songList) {
            val item = PlaylistItem(
                title = song.title,
                artist = song.artist,
                id = song.id
            )
            list.add(item)
        }
    }

    fun openSelect() {
        menuType.value = 0
    }

    fun cancelSelect() {
        menuType.value = 1
    }

    fun remove(listIndex: Int) {
        list.removeAt(listIndex)
        playPosition.value = activity.playlist.indexOf(SimplePlayer.currentSong)
    }

    fun openPlaylistNameList(names: Array<String>) {
        playlists.clear()
        for (name in names) {
            playlists.add(
                R.drawable.ic_play_dark to name
            )
        }
        menuType.value = 2
    }

    data class PlaylistItem(
        val title: String,
        val artist: String,
        val id: Long
    )

    companion object {
        const val TAG = "PlaylistScreen"
    }

}