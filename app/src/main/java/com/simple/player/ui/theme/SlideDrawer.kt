package com.simple.player.ui.theme

import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class SlideDrawerValue {
    Open,
    Close
}

private val slideAnimateSpec = TweenSpec<Float>(durationMillis = 220)

@OptIn(ExperimentalMaterialApi::class)
class SlideDrawerState(
    initialValue: SlideDrawerValue,
    confirmStateChange: (SlideDrawerValue) -> Boolean = { true }
) {
    val swipeableState = SwipeableState(
        initialValue = initialValue,
        animationSpec = slideAnimateSpec,
        confirmStateChange = confirmStateChange
    )

    val isOpen: Boolean
        get() = currentValue == SlideDrawerValue.Open

    val isClose: Boolean
        get() = currentValue == SlideDrawerValue.Close

    val currentValue: SlideDrawerValue
        get() {
            return swipeableState.currentValue
        }

    suspend fun open() {
        swipeableState.animateTo(SlideDrawerValue.Open, slideAnimateSpec)
    }

    suspend fun close() {
        swipeableState.animateTo(SlideDrawerValue.Close, slideAnimateSpec)
    }

    companion object {

        fun Saver(confirmStateChange: (SlideDrawerValue) -> Boolean) =
            Saver<SlideDrawerState, SlideDrawerValue>(
                save = { it.currentValue },
                restore = { SlideDrawerState(it, confirmStateChange) }
            )

    }
}

@Composable
fun rememberSlideDrawerState(
    initialValue: SlideDrawerValue,
    confirmStateChange: (SlideDrawerValue) -> Boolean = { true }
): SlideDrawerState {
    return rememberSaveable(saver = SlideDrawerState.Saver(confirmStateChange)) {
        SlideDrawerState(initialValue, confirmStateChange)
    }
}

private fun calculateFraction(a: Float, b: Float, pos: Float) =
    ((pos - a) / (b - a)).coerceIn(0f, 1f)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SlideDrawer(
    drawerContent: @Composable () -> Unit,
    drawerState: SlideDrawerState = rememberSlideDrawerState(initialValue = SlideDrawerValue.Close),
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val slideDrawerConstraints = constraints
        val minValue = slideDrawerConstraints.maxHeight.toFloat()
        val maxValue = 0f
        val anchor = mapOf(minValue to SlideDrawerValue.Close, maxValue to SlideDrawerValue.Open)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .swipeable(
                    state = drawerState.swipeableState,
                    anchors = anchor,
                    thresholds = { _, _ ->
                        FractionalThreshold(0.5F)
                    },
                    orientation = Orientation.Vertical,
                    velocityThreshold = 400.dp,
                    resistance = null
                )
        ) {
            Box(modifier = Modifier.fillMaxSize()) { content() }
            Scrim(
                fraction = { calculateFraction(minValue, maxValue, drawerState.swipeableState.offset.value) },
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.32f)
            )
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, drawerState.swipeableState.offset.value.roundToInt()) }
            ) {
               Column(modifier = Modifier.fillMaxSize()) {
                   drawerContent()
               }
            }
        }
    }
}
@Composable
private fun Scrim(
    fraction: () -> Float,
    color: Color
) {

    Canvas(
        Modifier
            .fillMaxSize()
    ) {
        drawRect(color, alpha = fraction())
    }
}
