package com.simple.player.screen

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.R
import com.simple.player.playlist.AbsPlaylist
import com.simple.player.ui.theme.ColumnSpace
import com.simple.player.ui.theme.RowSpace
import com.simple.player.ui.theme.windowBackgroundAlpha
import kotlinx.coroutines.delay

class PlaylistScreen(private val activity: ComponentActivity) {

    private val listName = mutableStateOf("播放列表")
    private val selectionMode = mutableStateOf(false)
    private val isSelectAll = mutableStateOf(false)
    private val selectMap = HashMap<Int, MutableState<Boolean>>()
    private val playPosition = mutableStateOf(0)
    private val list = mutableStateListOf<PlaylistItem>()

    var onItemClick: ((position: Int) -> Unit)? = null

    @Composable
    fun ComposeContent() {
        val listName by remember {
            mutableStateOf("播放列表")
        }
        val selectionMode by remember {
            selectionMode
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text(text = if (selectionMode) "全选" else listName) },
                navigationIcon = {
                    if (selectionMode) {
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
                }
            )
            Content()
        }
    }

    @Composable
    private fun Content() {
        Box(modifier = Modifier.fillMaxWidth()) {
            ListContent()
            Box(modifier = Modifier
                .padding(end = 16.dp, bottom = 16.dp)
                .align(Alignment.BottomEnd)) {
                FloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_my_location_24), contentDescription = "locate")
                }
            }

        }
    }

    @Composable
    private fun ListContent() {
        val state = rememberLazyListState()
        LaunchedEffect(key1 = Unit, block = {
            delay(500)
            state.scrollToItem(playPosition.value)
        })

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
            itemsIndexed(list) { index, item ->
                ListItem(position = index, item = item)
            }
            item {
                ColumnSpace(height = 8.dp)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ListItem(position: Int = 0, item: PlaylistItem) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = {
                        if (selectionMode.value) {
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
                        selectionMode.value = !selectionMode.value
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
            visible = selectionMode.value
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
            color = if (playPosition.value == position && !selectionMode.value) MaterialTheme.colors.primary else Color.Black,
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
                color = if (playPosition.value == position && !selectionMode.value) MaterialTheme.colors.primary else Color(0xFF808080),
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

    @Composable
    fun BottomMenu() {

    }

    fun setPlayingPosition(position: Int) {
        playPosition.value = position
    }

    fun setListSource(playlist: AbsPlaylist) {
        val songList = playlist.songList
        for (song in songList) {
            val item = PlaylistItem(
                title = song.title,
                artist = song.artist
            )
            list.add(item)
        }
    }

    fun openSelect() {
        selectionMode.value = true
    }

    fun cancelSelect() {
        selectionMode.value = false
    }

    data class PlaylistItem(
        val title: String,
        val artist: String
    )

}