package com.simple.player.ui.theme

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.simple.player.R
import com.simple.player.activity.HomeActivity

@Composable
fun RoundIconButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    iconSize: Dp = 24.dp,
    painter: Painter,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    onClick: () -> Unit
) {
    Surface (color = backgroundColor, shape = CircleShape) {
        IconButton(onClick = onClick, modifier = modifier) {
            Icon(modifier = Modifier.size(iconSize), painter = painter, contentDescription = "", tint = tint)
        }
    }
}
@Composable
fun RoundIconButton2(
    color: Color = Color.White,
    contentPadding: Dp = 0.dp,
    iconSize: Dp = 24.dp,
    painter: Painter,
    contentDescription: String,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
    onClick: () -> Unit
) {
    val padding = max(0.dp, contentPadding)
    val outSize = padding * 2 + max(iconSize, 0.dp)
    val radius = outSize / 2
    val i = remember {
        MutableInteractionSource()
    }
    Surface (
        color = color,
        shape = CircleShape,
        modifier = Modifier
            .size(outSize)
            .clickable(
                interactionSource = i,
                indication = rememberRipple(
                    bounded = true,
                    radius = radius
                ),
                onClick = onClick
            )
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .size(iconSize)
                .padding(contentPadding)
        )
    }
}



@Preview
@Composable
fun TestRoundIconButton() {
    CenterColumn (modifier = Modifier.fillMaxSize()) {
        val padding = 10.dp
        val iconSize = 24.dp
        val outSize = padding * 2 + iconSize
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_add_24),
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier
                .size(outSize)
                .clip(CircleShape)
                .padding(padding)
                .background(NRed)
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = rememberRipple(
                        bounded = false,
                        radius = 48.dp
                    ),
                    onClick = {}
                )
        )
    }

}

@Composable
inline fun CenterColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column (modifier = modifier, content = content, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally)
}

@Composable
inline fun CenterRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, content = content)
}

/**
 * RoundIcon 是一个有着圆形形状的图标，图标是尺寸固定的，contentPadding 和 iconSize 共同决定整个图标的大小
 */
@Composable
fun RoundIcon(
    painter: Painter,
    iconSize: Dp = 24.dp,
    contentPadding: Dp = 0.dp,
    contentDescription: String,
    color: Color = Color.White,
    tint: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
) {
    val padding = if (contentPadding < 0.dp) 0.dp else contentPadding
    val outSize = padding * 2 + if (iconSize < 0.dp) 24.dp else iconSize
    Surface(shape = CircleShape, color = color, modifier = Modifier.size(outSize)) {
        Icon(
            painter = painter,
            modifier = Modifier
                .size(iconSize)
                .padding(contentPadding),
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

@Composable
inline fun RowSpace(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
inline fun ColumnSpace(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun SimpleAsyncImage(
    data: () -> Any?,
    @DrawableRes error: Int,
    @Px imageSize: Int = -1,
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    Log.e(HomeActivity.TAG, "SimpleAsyncImage: ")
    val builder = ImageRequest.Builder(LocalContext.current)
        .data(data())
        .crossfade(false)
        .allowHardware(true)
        .allowRgb565(true)
    if (imageSize > 0) {
        builder.size(imageSize)
    }
    builder.error(error)
        .listener(
            onError = { _, result ->
                Log.e(HomeActivity.TAG, "SimpleAsyncImage: ${result.throwable}")
            }
        )
    Log.e(HomeActivity.TAG, "SimpleAsyncImage: loading")
    AsyncImage(
        model = builder.build(),
        contentDescription = contentDescription,
        modifier = modifier
    )
}