package com.simple.player.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.node.modifierElementOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.simple.player.screen.SettingsItem
import com.simple.player.screen.SettingsText

@Composable
fun Alert(
    title: String = "Title",
    message: String = "message",
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null
) {
    var dismiss by remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(visible = !dismiss) {
        Dialog(
            onDismissRequest = { dismiss = !dismiss },
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 16.sp
                )
                Text(
                    text = message,
                    fontSize = 15.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        modifier = Modifier
                            .background(windowBackgroundAlpha)
                            .clip(RoundedCornerShape(8.dp))
                        ,
                        onClick = { /*TODO*/ }
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    TextButton(onClick = { /*TODO*/ }) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialogButton() {
//    Surface(
//        color = windowBackgroundAlpha,
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(8.dp))
//            .clickable(onClick = onClick)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            content = content
//        )
//    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TestDialog() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Alert()
    }
}