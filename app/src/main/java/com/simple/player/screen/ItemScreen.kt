package com.simple.player.screen

import androidx.compose.foundation.background
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
fun <T> DeletableItemList(
    list: () -> SnapshotStateList<T>,
    onDelete: (Int, T) -> Unit,
    onEdit: (Int, T) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(list()) { index, item ->
            DeletableItem(item.toString(), onEdit = { onEdit(index, item) }) {
                onDelete(index, item)
            }
        }

    }
}

@Composable
fun DeletableItem(
    text: String,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit
) {
    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = windowBackgroundAlpha
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 8.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { onEdit(text) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.Black
                )
            }
        }
    }
}