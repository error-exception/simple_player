package com.simple.player.util

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import java.util.HashMap

/*
    此代码来自CSDN,用于设置字体
*/
object FontPlacer {
    fun setDefaultFont(context: Context, staticTypefaceField: String?, fontPath: String?) {
        val typeface = Typeface.createFromAsset(context.assets, fontPath)
        replaceFont(staticTypefaceField, typeface)
    }

    internal fun replaceFont(staticTypefaceField: String?, typeFace: Typeface) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val newMap = HashMap<String?, Typeface>()
            newMap[staticTypefaceField] = typeFace
            try {
                val staticField = Typeface::class.java.getDeclaredField("sSystemFontMap")
                staticField.isAccessible = true
                staticField[null] = newMap
            } catch (f: NoSuchFieldException) {
            } catch (f: IllegalAccessException) {
            }
        } else {
            try {
                val staticField = Typeface::class.java.getDeclaredField(staticTypefaceField)
                staticField.isAccessible = true
                staticField[null] = typeFace
            } catch (f: NoSuchFieldException) {
            } catch (f: IllegalAccessException) {
            }
        }
    }
}