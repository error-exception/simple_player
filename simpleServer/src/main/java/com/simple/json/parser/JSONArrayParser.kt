package com.simple.json.parser

import com.simple.json.JSONArray
import kotlin.collections.ArrayList

internal class JSONArrayParser {

    companion object {
        const val TAG = "JSONArrayParser"
    }

    var index: Int = 0
        private set
    private var currentChar: Char = ' '
    private lateinit var content: String
    private var isEnter = false
    private val list = JSONArray()
    private var tmp: Any = ""
    private var elementCount = 0

    fun parse(content: String, startIndex: Int): JSONArray {
        index = startIndex
        this.content = content
        while (nextChar()) {
            if (currentChar == '[' && !isEnter) {
                isEnter = true
                continue
            }
            if (!isEnter) {
                continue
            }
            when {
                currentChar == '"' -> {
                    handleString()
                }
                isNumber(currentChar) -> {
                    handleNumber()
                }
                currentChar in 'a'..'z' || currentChar in 'A'..'Z' -> {
                    handleLabel()
                }
                currentChar == ',' || currentChar == ']' -> {
                    if (elementCount > 0) {
                        list.add(tmp)
                    }
                    if (currentChar == ']') return list
                }
                currentChar == '[' -> {
                    val parser = JSONArrayParser()
                    val list = parser.parse(content, index - 1)
                    index = parser.index
                    tmp = list
                    elementCount++
                }
                currentChar == '{' -> {
                    val parser = JSONObjectParser()
                    val map = parser.parse(content, index - 1)
                    index = parser.index
                    tmp = map
                    elementCount++
                }
            }
        }
        return list
    }

    private fun isNumber(currentChar: Char): Boolean {
        return (currentChar in '0'..'9') || currentChar == '.' || currentChar == '-'
    }

    private fun handleNumber() {
        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && isNumber(currentChar))
        previousChar()
        tmp = s.toString()
        elementCount++
    }

    private fun handleLabel() {
        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && currentChar.isLetter())
        previousChar()
        tmp = s.toString()
        elementCount++
    }

    private fun handleString() {
        nextChar()
        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && currentChar != '"')
        tmp = s.toString()
        elementCount++
    }


    private fun nextChar(): Boolean {
        if (index >= content.length) {
            return false
        }
        currentChar = content[index++]
        return true
    }
    private fun previousChar(): Boolean {
        if (index > 0) {
            index--
            currentChar = content[index]
            return true
        }
        return false
    }
}