package com.simple.player.screen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.simple.player.R
import com.simple.player.activity.HomeActivity
import com.simple.player.model.Song
import com.simple.player.ui.theme.*
import com.simple.player.util.ArtworkProvider

@Composable
fun HomeClickableCard(
    modifier: Modifier = Modifier,
    item: HomeActivity.Item,
    onClick: (() -> Unit)? = null
) {
    val m = modifier
        .clip(shape = RoundedCornerShape(12.dp))
        .clickable { onClick?.invoke() }
    Card (modifier = m, shape = RoundedCornerShape(12.dp), elevation = 0.dp, backgroundColor = windowBackgroundAlpha) {
        Row (
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                painter = painterResource(id = item.icon),
                contentDescription = "",
                tint = MaterialTheme.colors.primary
            )
            Text(
                text = item.text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun HomeDrawerSongInfo(songState: () -> MutableState<Song>) {
    val song = remember {
        songState()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(NRed),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(ArtworkProvider.getArtworkDataForCoil(song.value))
                .crossfade(false)
                .allowHardware(true)
                .allowRgb565(true)
                .error(R.drawable.nc_player_content_default_work_art)
                .build(),
            contentDescription = "",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape),
        )
        Text(
            text = song.value.title,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)
        )
    }
}


@Composable
fun HomeDrawerListItem(item: HomeActivity.Item) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .clickable(onClick = item.click ?: {}),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowSpace(width = 16.dp)
        RoundIcon(
            painter = painterResource(id = item.icon),
            contentPadding = 8.dp,
            iconSize = 22.dp,
            contentDescription = "",
            color = NRed,
            tint = Color.White
        )
        Text(text = item.text, modifier = Modifier.padding(start = 16.dp, end = 16.dp), fontSize = 16.sp)
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun CustomPlaylistItem(
    item: HomeContentScreen.PlayListItem,
    onClick: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    val isPlaying = remember {
        item.isPlaying
    }
    var isShow by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(windowBackgroundAlpha)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    isShow = !isShow
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        val imageData by remember {
            item.artwork
        }
        SimpleAsyncImage(
            data = { imageData },
            error = R.drawable.nc_player_content_default_work_art,
            contentDescription = "playlist cover",
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
        )

        AnimatedContent(
            targetState = isShow,
            modifier = Modifier.weight(1F)
        ) { target ->
            if (!target) {
                CenterRow(modifier = Modifier.fillMaxSize()) {

                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .padding(start = 16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.name.value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying.value) MaterialTheme.colors.primary else Color.Black
                        )
                        ColumnSpace(height = 4.dp)
                        Text(
                            text = "共 ${item.songCount.value} 首",
                            fontSize = 12.sp,
                            color = if (isPlaying.value) MaterialTheme.colors.primary else Color(0x80808080)
                        )
                    }
                }
            } else {
                CustomListMenu(
                    visible = true,
                    onItemClick = {
                        if (it != 3)
                            onItemClick(it)
                        isShow = !isShow
                    }
                )
            }
        }


    }
}

@Composable
fun CustomListMenu(
    visible: Boolean,
    onItemClick: (Int) -> Unit = {}
) {
    AnimatedVisibility(visible = visible) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ){
            RoundIconButton2(
                painter = painterResource(id = R.drawable.ic_play_dark),
                contentDescription = "",
                contentPadding = 8.dp,
                iconSize = 24.dp,
                tint = Color.Black,
                color = Color.White
            ) {
                onItemClick(0)
            }
            RoundIconButton2(
                painter = painterResource(id = R.drawable.ic_baseline_edit_24),
                contentDescription = "",
                contentPadding = 8.dp,
                iconSize = 24.dp,
                tint = Color.Black,
                color = Color.White
            ) {
                onItemClick(1)
            }
            RoundIconButton2(
                painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                contentDescription = "",
                contentPadding = 8.dp,
                iconSize = 24.dp,
                tint = Color.Black,
                color = Color.White
            ) {
                onItemClick(2)
            }
            RoundIconButton2(
                painter = painterResource(id = R.drawable.ic_baseline_close_24),
                contentDescription = "",
                contentPadding = 8.dp,
                iconSize = 24.dp,
                tint = Color.Black,
                color = Color.White
            ) {
                onItemClick(3)
            }
        }
    }


}

@Composable
fun CustomPlaylistControl(
    expandClick: () -> Unit,
    addClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = expandClick),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24), contentDescription = "")
        Text(
            text = "已创建的播放列表",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .weight(1F)
        )
        IconButton(
            modifier = Modifier.size(36.dp),
            onClick = addClick
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create New Playlist")
        }
    }
}