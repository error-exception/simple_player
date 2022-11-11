package com.simple.player.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simple.player.R
import com.simple.player.ui.theme.ColumnSpace
import com.simple.player.ui.theme.ComposeTestTheme
import kotlinx.coroutines.coroutineScope

class NewSettingsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTestTheme {
                Test()
            }
        }
    }

    @Preview
    @Composable
    fun Test() {
        Surface(color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text("设置")
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24), contentDescription = "")
                        }
                    }
                )
                SettingsContent()
            }
        }
    }

    @Composable
    fun SettingsContent() {
        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {

            var isClick by remember {
                mutableStateOf(false)
            }
            val clickTransition = updateTransition(targetState = isClick, label = null)
            val scale by clickTransition.animateFloat(
                targetValueByState = { isClick ->
                    if (isClick) 0.8F else 1F
                }, label = ""
            )
            val color by clickTransition.animateColor(
                targetValueByState = {
                    if (it) Color.Gray else Color(0xEDEDEDED)
                },
                label = ""
            )
            Card(
                shape = RoundedCornerShape(12.dp),
                backgroundColor = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .scale(scale)
                    .clickable(enabled = true) {
//                        isClick = !isClick
                    }
                    .pointerInput(Unit) { ->
                        awaitPointerEventScope {
                            val pointer = awaitPointerEvent(pass = PointerEventPass.Initial)
                            Log.e("", "tach")

                        }
                        detectTapGestures(
                            onPress = {
                                isClick = true
                            }
                        )
                    }
                    ,
                elevation = 0.dp
            ) {
                Column (modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                ) {
                    Text(text = "关于")
                }
                Dialog(onDismissRequest = { /*TODO*/ }) {
                    Column (modifier = Modifier
                        .width(400.dp)
                        .background(Color.White)
                        .clip(RoundedCornerShape(2.dp))
                        .padding(16.dp)
                    ) {
                        var value by remember {
                            mutableStateOf("")
                        }
                        Text(text = "Title", fontWeight = FontWeight.Bold)
                        ColumnSpace(height = 8.dp)
                        BasicTextField(value = value, onValueChange = {
                            value = it
                        }, modifier = Modifier.fillMaxWidth().height(50.dp)
                        )
                        ColumnSpace(height = 16.dp)
                        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { /*TODO*/ }) {
                                Text(text = "OK")
                            }
                        }
                    }
                }
            }
        }

    }

}