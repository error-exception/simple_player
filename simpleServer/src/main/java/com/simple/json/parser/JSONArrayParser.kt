package com.simple.json.parser

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
    private val list = ArrayList<Any>()
    private var tmp: Any = ""

    fun parse(content: String, startIndex: Int): ArrayList<Any> {
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
                currentChar.isLetter() -> {
                    handleLabel()
                }
                currentChar == ',' || currentChar == ']' -> {
                    list.add(tmp)
                    if (currentChar == ']') return list
                }
                currentChar == '[' -> {
                    val parser = JSONArrayParser()
                    val list = parser.parse(content, index - 1)
                    index = parser.index
                    tmp = list
                }
                currentChar == '{' -> {
                    val parser = JSONObjectParser()
                    val map = parser.parse(content, index - 1)
                    index = parser.index
                    tmp = map
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
    }

    private fun handleLabel() {
        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && currentChar.isLetter())
        previousChar()
        tmp = s.toString()
    }

    private fun handleString() {
        nextChar()
        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && currentChar != '"')
        tmp = s.toString()
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