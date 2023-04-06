package com.simple.player.json.parser

import java.util.*
import kotlin.collections.HashMap

internal class JSONObjectParser {

    companion object {
        const val TAG = "JSONObjectParser"
    }

    var index: Int = 0
        private set
    private var currentChar: Char = ' '
    private lateinit var content: String
    private val tmpStack = Stack<Any>()
    private val result = HashMap<String, Any>()
    private var isEnter = false

    fun parse(content: String, startIndex: Int): HashMap<String, Any> {
        index = startIndex
        this.content = content
        while (nextChar()) {
            if (currentChar == '{' && !isEnter) {
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
                currentChar == ',' || currentChar == '}' -> {
                    if (tmpStack.isNotEmpty()) {
                        val value = tmpStack.pop()
                        val key = tmpStack.pop()
                        result[key.toString()] = value
                    }
                    if (currentChar == '}') return result
                }
                currentChar == '[' -> {
                    val parser = JSONArrayParser()
                    val list = parser.parse(content, index - 1)
                    index = parser.index
                    tmpStack.push(list)
                }
                currentChar == '{' -> {
                    val parser = JSONObjectParser()
                    val map = parser.parse(content, index - 1)
                    index = parser.index
                    tmpStack.push(map)
                }
            }
        }
        return result
    }

    private fun isNumber(currentChar: Char): Boolean {
        return (currentChar in '0'..'9') || currentChar == '.' || currentChar == '-'
    }

    private fun handleNumber() {
        val s = StringBuilder()
        do {

            s.append(currentChar)
        } while (nextChar() && isNumber(currentChar))
        tmpStack.push(s.toString())
        previousChar()
    }

    private fun handleLabel() {

        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && currentChar.isLetter())
        tmpStack.push(s.toString())
        previousChar()
    }

    private fun handleString() {
        val s = StringBuilder()
        var isEscapeChar = false
        while (nextChar() && (isEscapeChar || currentChar != '"')) {
            if (currentChar == '\\') {
                isEscapeChar = true
                continue
            }
            if (isEscapeChar) {
                isEscapeChar = false
                s.append(when (currentChar) {
                    'n' -> '\n'
                    't' -> '\t'
                    'r' -> '\r'
                    'b' -> '\b'
                    else -> currentChar
                })
            } else {
                s.append(currentChar)
            }
        }
        tmpStack.push(s.toString())
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