package com.simple.player.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simple.player.Util
import com.simple.player.scan.ScanConfigItem
import com.simple.player.ui.theme.windowBackgroundAlpha

@Composable
fun TextFieldWithButton(
    buttonText: String,
    label: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .background(windowBackgroundAlpha)
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
    ) {
        var focus by remember {
            mutableStateOf(false)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            maxLines = 1,
            label = if (label != null)
                ({ Text(text = label) })
            else
                null,
            modifier = Modifier.fillMaxWidth()
        )
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ){
            TextButton(
                onClick = onClick,
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
fun DeletableItemList(
    list: () -> SnapshotStateList<ScanConfigItem>,
    onDelete: (Int, ScanConfigItem) -> Unit,
    onEdit: (Int, ScanConfigItem) -> Unit,
    onSwitch: (Int, Boolean, ScanConfigItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(list()) { index, item ->
            DeletableItem(
                text = item.value,
                onEdit = { onEdit(index, item) },
                onDelete = { onDelete(index, item) },
                onSwitch = { onSwitch(index, it, item) },
                isOn = item.isValid
            )
        }

    }
}

@Composable
fun DeletableItem(
    text: MutableState<String>,
    isOn: MutableState<Boolean>,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onSwitch: (Boolean) -> Unit
) {
    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = windowBackgroundAlpha
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text.value,
                fontSize = 16.sp,
                color = Color.Black,
            )
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Black
                    )
                }
                IconButton(onClick = { onEdit(text.value) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Black
                    )
                }
                var checked by remember {
                    isOn
                }
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        onSwitch(it)
                    }
                )
            }

        }
    }
}