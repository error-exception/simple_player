package com.simple.player.view.layout

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.simple.player.Util.dps
import com.simple.player.drawable.RoundBitmapDrawable2
import com.simple.player.drawable.RoundColorDrawable
import com.simple.player.util.BlurUtil
import com.simple.player.util.ColorUtil
import com.simple.player.util.StringUtil
import com.simple.player.view.ArtistView
import com.simple.player.view.DateTimeView
import com.simple.player.view.TitleView
import java.io.File

object ViewInitializer {

    fun initial(context: Context, viewName: String, propertyMap: HashMap<String, String>, parent: ViewGroup): View {
        val view = when (viewName) {
            "Title" -> TitleView(context)
            "Artist" -> ArtistView(context)
            "DateTime" -> DateTimeView(context)
            "Text" -> AppCompatTextView(context)
            "Image" -> AppCompatImageView(context)
            else -> View(context)
        }
        initCommonProperty(view, propertyMap, parent)
        if (view is AppCompatTextView) {
            initTextViewProperty(view, propertyMap)
        }
        if (view is DateTimeView) {
            initDateTimeViewProperty(view, propertyMap)
        }
        if (view is AppCompatImageView) {
            initImageViewProperty(view, propertyMap)
        }
        return view
    }

    private fun initImageViewProperty(
        imageView: AppCompatImageView,
        propertyMap: HashMap<String, String>
    ) {
        val src = StringUtil.toString(propertyMap["src"], "")
        val scaleType = StringUtil.toString(propertyMap["scaleType"], "center")
        val blur = 0

        var bitmap = Glide.with(imageView)
            .asBitmap()
            .load(File(src))
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .submit().get()
        bitmap = BlurUtil.blur(bitmap, blur)
        val roundBitmapDrawable2 =
            RoundBitmapDrawable2(bitmap, StringUtil.toFloat(propertyMap["radius"], 1f))
        imageView.setImageDrawable(roundBitmapDrawable2)
        imageView.scaleType = getScaleType(scaleType)
    }

    private fun initDateTimeViewProperty(
        dateTimeView: DateTimeView,
        propertyMap: HashMap<String, String>
    ) {
        val format = StringUtil.toString(propertyMap["format"], "yy-MM-dd HH:mm:ss")
        dateTimeView.setFormat(format)
    }

    private fun initTextViewProperty(
        textView: AppCompatTextView,
        propertyMap: HashMap<String, String>
    ) {
        val text = StringUtil.toString(propertyMap["text"], "")
        val textColor = StringUtil.toString(propertyMap["textColor"], "#black")
        val textStyle = StringUtil.toString(propertyMap["textStyle"], "normal")
        val textSize = StringUtil.toFloat(propertyMap["textSize"], 14f)
        with(textView) {
            this.text = text
            this.setTextColor(toColor(textColor))
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            this.setTypeface(null, when (textStyle) {
                "bold" -> Typeface.BOLD
                "italic" -> Typeface.ITALIC
                "bold-italic" -> Typeface.BOLD_ITALIC
                else -> Typeface.NORMAL
            })
        }
    }

    private fun toColor(colorText: String): Int {
        return if (ColorUtil.isColorString(colorText)) {
            Color.parseColor(colorText)
        } else {
            ColorUtil.getColorByName(colorText.substring(1))
        }
    }

    private fun initCommonProperty(view: View, propertyMap: HashMap<String, String>, parent: ViewGroup) {
        val width = StringUtil.toInteger(propertyMap["width"], -2).px
        val height = StringUtil.toInteger(propertyMap["height"], -2).px
        val marginLeft = StringUtil.toInteger(propertyMap["marginLeft"], 0).px
        val marginRight = StringUtil.toInteger(propertyMap["marginLeft"], 0).px
        val marginTop = StringUtil.toInteger(propertyMap["marginTop"], 0).px
        val marginBottom = StringUtil.toInteger(propertyMap["marginBottom"], 0).px
        val marginStart = StringUtil.toInteger(propertyMap["marginStart"], 0).px
        val marginEnd = StringUtil.toInteger(propertyMap["marginEnd"], 0).px
        val layoutGravity = StringUtil.toString(propertyMap["layoutGravity"], "top|left")
        val weight = StringUtil.toFloat(propertyMap["weight"], -1f)
        view.layoutParams = if (parent is LinearLayout) {
            LinearLayout.LayoutParams(width, height).apply {
                this.leftMargin = marginLeft
                this.rightMargin = marginRight
                this.topMargin = marginTop
                this.bottomMargin = marginBottom
                this.marginEnd = marginEnd
                this.marginStart = marginStart
                this.weight = weight
            }
        } else {
            FrameLayout.LayoutParams(width, height).apply {
                this.leftMargin = marginLeft
                this.rightMargin = marginRight
                this.topMargin = marginTop
                this.bottomMargin = marginBottom
                this.marginEnd = marginEnd
                this.marginStart = marginStart
                this.gravity = getGravity(layoutGravity)
            }
        }
        val background = StringUtil.toString(propertyMap["background"], "#transparent")
        val radius = StringUtil.toFloat(propertyMap["radius"], 1f)
        view.background = if (background.startsWith("#")) {
            val color = toColor(background)
            RoundColorDrawable(color, radius)
        } else {
            val file = File(background)
            if (file.exists()) {
                val bitmap = Glide.with(view)
                    .asBitmap()
                    .load(file)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .submit().get()
                RoundBitmapDrawable2(bitmap, radius)
            } else {
                RoundColorDrawable(Color.TRANSPARENT, radius)
            }
        }

        val gravity = StringUtil.toString(propertyMap["gravity"], "top|left")
        val targetGravity = getGravity(gravity)
        if (view is LinearLayout) {
            view.gravity = targetGravity
        } else if (view is TextView) {
            view.gravity = targetGravity
        }

        val scaleX = StringUtil.toFloat("scaleX", 1f)
        val scaleY = StringUtil.toFloat("scaleY", 1f)
        view.scaleX = scaleX
        view.scaleY = scaleY
    }

    private fun getScaleType(scaleType: String): ImageView.ScaleType {
        return when (scaleType) {
            "center" -> ImageView.ScaleType.CENTER
            "center-crop" -> ImageView.ScaleType.CENTER_CROP
            "center-inside" -> ImageView.ScaleType.CENTER_INSIDE
            "fit-center" -> ImageView.ScaleType.FIT_CENTER
            "fit-end" -> ImageView.ScaleType.FIT_END
            "fit-start" -> ImageView.ScaleType.FIT_START
            "fit-xy" -> ImageView.ScaleType.FIT_XY
            "matrix" -> ImageView.ScaleType.MATRIX
            else -> ImageView.ScaleType.CENTER
        }
    }

    private fun getGravity(gravityName: String): Int {
        val list = gravityName.split("|")
        var targetGravity = 0
        for (i in list.indices) {
            val name = list[i].trim()
            val value = getGravityValueByName(name)
            targetGravity = targetGravity or value
        }
        return targetGravity
    }

    private val Int.px: Int
        get() = this.dps

    private fun getGravityValueByName(gravityName: String): Int {
        return when (gravityName.lowercase()) {
            "left" -> Gravity.START
            "right" -> Gravity.END
            "top" -> Gravity.TOP
            "bottom" -> Gravity.BOTTOM
            "center" -> Gravity.CENTER
            "center-vertical" -> Gravity.CENTER_VERTICAL
            "center-horizontal" -> Gravity.CENTER_HORIZONTAL
            else -> Gravity.START
        }
    }

}