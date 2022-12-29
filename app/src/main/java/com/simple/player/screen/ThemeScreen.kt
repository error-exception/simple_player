package com.simple.player.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.simple.player.R
import com.simple.player.ui.theme.DarkGreen
import com.simple.player.ui.theme.NRed
import com.simple.player.ui.theme.RowSpace

class ThemeScreen(private val activity: ComponentActivity) {

    private val colorList = mutableStateListOf(
        ColorItem(color = NRed, name = "网易红"),
        ColorItem(color = DarkGreen, name = "绿"),
        ColorItem(color = Color(0xFF8bc24b), name = "早苗绿"),
        ColorItem(color = Color(0xFF2095f4), name = "宝石蓝"),
        ColorItem(color = Color(0xFF9b27b0), name = "罗兰紫"),
        ColorItem(color = Color(0xFFf44435), name = "高能红"),
    )

    var onDefaultColorSelected: ((Color) -> Unit)? = null
    var onApplyCustomColor: ((Color) -> Unit)? = null

    @Composable
    fun ComposeContent() {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = { Text(text = "主题") },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                            contentDescription = "back"
                        )
                    }
                }
            )
            Content()
        }
    }

    @Composable
    private fun Content() {
        Column(modifier = Modifier
            .fillMaxWidth()
        ) {
            val navController = rememberNavController()
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.weight(1F),
                    onClick = {
                        navController.popBackStack()
                        navController.navigate("default")
                    }
                ) {
                    Text(text = "预设")
                }
//                TextButton(
//                    onClick = { /*TODO*/ },
//                    modifier = Modifier.weight(1F)
//                ) {
//                    Text(text = "头图")
//                }
                TextButton(
                    modifier = Modifier.weight(1F),
                    onClick = {
                        navController.popBackStack()
                        navController.navigate("custom")
                    }
                ) {
                    Text(text = "自定义")
                }
            }

            NavHost(navController = navController, startDestination = "default") {
                composable("default") {
                    DefaultTheme()
                }
                composable("custom") {
                    CustomTheme()
                }
            }
        }
    }

    @Composable
    private fun DefaultTheme() {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(colorList) { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDefaultColorSelected?.invoke(item.color) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RowSpace(width = 16.dp)
                    Box(modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(item.color)
                    )
                    Text(
                        text = item.name,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .weight(1F),
                        fontSize = 16.sp
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_check_24),
                        contentDescription = "check",
                        tint = MaterialTheme.colors.primary,
                    )
                    RowSpace(width = 16.dp)
                }
            }
        }
    }

    @Composable
    fun CustomTheme() {
        val controller = rememberColorPickerController()
        var hexColor by remember { mutableStateOf("") }

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AlphaTile(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                controller = controller,
                tileOddColor = Color.White,
                tileEvenColor = Color.LightGray,
            )
            HsvColorPicker(
                modifier = Modifier.size(160.dp),
                controller = controller,
                onColorChanged = {
                    hexColor = it.hexCode
                }
            )
            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                controller = controller
            )
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                controller = controller
            )
            TextField(value = hexColor, onValueChange = {})
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApplyCustomColor?.invoke(controller.selectedColor.value) }
            ) {
                Text(text = "确定")
            }
        }
    }

    data class ColorItem(
        val name: String,
        val color: Color
    )

}