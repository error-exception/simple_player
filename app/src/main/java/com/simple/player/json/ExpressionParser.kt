package com.simple.player.json

internal class ExpressionParser {

    private val list = ArrayList<String>()
    private lateinit var content: String
    private var index = 0
    private var currentChar = ' '

    fun parse(expression: String): List<String> {
        content = expression
        while (nextChar()) {
            when {
                currentChar.isLetter() -> {
                    handleLabel()

                }
                isNumber(currentChar) -> {
                    handleNumber()
                }
            }
        }
        return list
    }

    private fun isNumber(currentChar: Char): Boolean {
        return (currentChar in '0'..'9')
    }

    private fun handleNumber() {
        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && isNumber(currentChar))
        list.add(s.toString())
    }

    private fun handleLabel() {
        val s = StringBuilder()
        do {
            s.append(currentChar)
        } while (nextChar() && (currentChar.isLetter() || currentChar == '_'))
        list.add(s.toString())

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