package com.simple.player.view.layout

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.simple.player.Util
import com.simple.player.util.StringUtil
import com.simple.player.view.ArtistView
import com.simple.player.view.DateTimeView
import com.simple.player.view.TitleView
import java.util.*
import kotlin.collections.HashMap

class LayoutParser(private val context: Context) {

    private lateinit var content: String
    private var currentChar = ' '
    var index = 0
    private val stack = Stack<String>()
    private lateinit var viewGroup: ViewGroup
    private val map = HashMap<String, String>()
    private var viewName = ""

    fun parse(content: String, index: Int = 0, parent: ViewGroup): View {
        this.content = content
        this.index = index
        this.viewGroup = parent
        while (nextChar()) {
            when {
                isAlpha(currentChar) -> {
                    handleLabel()
                }
                currentChar == '=' -> {}
                currentChar == '{' -> {
                    val parser = LayoutParser(context)
                    val view = parser.parse(viewName, this.index - 1, viewGroup)
                    this.index = parser.index - 1
                    addToParent(view)
                }
                currentChar == '}' -> {
                    return viewGroup
                }
                currentChar == '(' -> {
                    viewName = stack.pop()
                }
                currentChar == '"' -> {
                    handleString()
                }
                currentChar == ',' -> {
                    val value = stack.pop()
                    val key = stack.pop()
                    map[key] = value
                }
                currentChar == ')' -> {
                    val value = stack.pop()
                    val key = stack.pop()
                    map[key] = value
                    val view = ViewInitializer.initial(context, viewName, map, parent)
                    addToParent(view)
                }
            }
        }
        return viewGroup
    }

    private fun addToParent(view: View) {
        if (viewGroup is LinearLayout) {
            val linearLayout = viewGroup as LinearLayout
            linearLayout.addView(view)
        } else if (viewGroup is FrameLayout) {
            val frameLayout = viewGroup as FrameLayout
            frameLayout.addView(view)
        }
    }

    private fun handleString() {
        val builder = StringBuilder()
        while (nextChar() && currentChar != '"') {
            builder.append(currentChar)
        }
        stack.push(builder.toString())
    }

    private fun handleLabel() {
        val builder = StringBuilder()
        do {
            builder.append(currentChar)
        } while (nextChar() && isAlpha(currentChar))
        val s = builder.toString()
        stack.push(s)
        previousChar()
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z'
    }

    private fun nextChar(): Boolean {
        if (index == content.length) {
            return false
        }
        currentChar = content[index++]
        return true
    }

    private fun previousChar(){
        currentChar = content[--index]
    }

}