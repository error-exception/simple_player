package com.simple.player.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.ui.theme.RowSpace
import com.simple.player.ui.theme.windowBackgroundAlpha


@Composable
inline fun SettingsItem(
    noinline onClick: () -> Unit,
    crossinline content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = windowBackgroundAlpha,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun BaseSettingsItem(
    title: String,
    description: String? = null,
    expandState: () -> MutableState<Boolean> = { mutableStateOf(false) },
    fixed: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    var state by remember {
        expandState()
    }
    SettingsItem(onClick = {
        if (!fixed)
            state = !state
        onClick?.invoke()
    }) {
        Column (Modifier.fillMaxWidth()){
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1F)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, end = 16.dp)
                        )
                    }
                }
                if (content != null) {
                    RowSpace(width = 16.dp)
                    content()
                }

            }
            if (extraContent != null) {
                AnimatedVisibility(visible = state) {
                    extraContent()
                }
            }
        }

    }
}

@Composable
fun SettingsText(
    title: String,
    description: String? = null,
    more: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    BaseSettingsItem(
        title = title,
        description = description,
        onClick = onClick,
        content = {
            if (more) {
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "");
            }
        }
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String? = null,
    state: Boolean = false,
    onStateChanged: (Boolean) -> Unit = {}
) {
    var checked by remember {
        mutableStateOf(state)
    }
    BaseSettingsItem(
        title = title,
        description = description,
        onClick = {
            checked = !checked
            onStateChanged(checked)
        },
        content = {
            Switch(checked = checked, onCheckedChange = { isChecked ->
                checked = isChecked
                onStateChanged(isChecked)
            })
        }
    )
}

@Composable
fun SettingsSwitchAndProgress(
    title: String,
    description: String? = null,
    switchState: Boolean = false,
    progressState: Float = 0F,
    onStateChanged: ((Boolean) -> Unit)? = null,
    onProgress: ((Float) -> Unit)? = null
) {
    var checked by remember {
        mutableStateOf(switchState)
    }
    val expandState = remember {
        mutableStateOf(switchState)
    }
    BaseSettingsItem(
        title = title,
        fixed = true,
        description = description,
        expandState = { expandState },
        content = {
            Switch(checked = checked, onCheckedChange = { isChecked ->
                checked = isChecked
                expandState.value = isChecked
                onStateChanged?.invoke(isChecked)
            })
        },
        extraContent = {
            var progress by remember {
                mutableStateOf(progressState)
            }
            Slider(
                value = progress,
                onValueChange = {
                    progress = it
                    onProgress?.invoke(it)
                }
            )
        }
    )
}

@Composable
fun <T> SettingsSingleChoice(
    title: String,
    description: String? = null,
    stateIndex: Int = 0,
    list: SnapshotStateList<T> = mutableStateListOf<T>(),
    onSelected: ((index: Int, element: T) -> Unit)? = null
) {
    val state = remember {
        mutableStateOf(false)
    }
    var selected by remember {
        mutableStateOf(stateIndex)
    }
    BaseSettingsItem(
        title = title,
        description = description,
        expandState = { state },
        extraContent = {
            Column (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (index in list.indices) {
                    val item = list[index]
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = index
                                onSelected?.invoke(index, item)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.toString(),
                            modifier = Modifier.weight(1F),
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        RadioButton(
                            selected = selected == index,
                            onClick = {
                                selected = index
                                onSelected?.invoke(index, item)
                            }
                        )
                    }
                }
                Row (
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    TextButton(onClick = { state.value = !state.value }) {
                        Text(text = "CLOSE")
                    }
                }
            }
        }
    )
}

@Composable
fun SettingsTextField(
    title: String,
    description: String? = null,
    stateText: String,
    onApplyText: ((String) -> Unit)? = null
) {
    val state = remember {
        mutableStateOf(false)
    }
    var text by remember {
        mutableStateOf(stateText)
    }
    BaseSettingsItem(
        title = title,
        description = description,
        expandState = { state },
        extraContent = {
            Column (
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 16.sp
                    )
                )
                Row (
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    TextButton(
                        onClick = {
                            state.value = !state.value
                            onApplyText?.invoke(text)
                        }
                    ) {
                        Text(text = "OK")
                    }
                }
            }
        }
    )
}

@Composable
fun SettingsProgress(
    title: String,
    description: String? = null,
    stateValue: Float = 0F,
    onProgress: ((Float) -> Unit)? = null
) {
    val state = remember {
        mutableStateOf(false)
    }
    BaseSettingsItem(
        title = title,
        description = description,
        expandState = { state },
        extraContent = {
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var progress by remember {
                    mutableStateOf(stateValue)
                }
                Slider(value = progress, onValueChange = {
                    progress = it
                    onProgress?.invoke(it)
                })
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            state.value = !state.value
                        }
                    ) {
                        Text(text = "OK")
                    }
                }
            }
        }
    )
}