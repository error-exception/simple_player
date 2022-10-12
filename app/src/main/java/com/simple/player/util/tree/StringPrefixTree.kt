package com.simple.player.util.tree

import kotlin.collections.HashMap

class StringPrefixTree {

    private val root = Node(' ')

    var size = 0
        private set

    fun add(s: String) {
        var current = root
        for (ch in s) {
            var childNode = current.children[ch]
            if (childNode == null) {
                childNode = Node(ch)
                current.children[ch] = childNode
            }
            current = childNode
        }
        size++
    }

    fun contains(other: String): Boolean {
        var current = root
        for (ch in other) {
            val childNode = current.children[ch] ?: return false
            current = childNode
        }
        return true
    }

    fun remove(s: String): Boolean {
        var b = true
        if (!contains(s)) {
            return false
        }
        TODO("not impl")
        return false
    }

    fun clear() {
        clear(root.children)
        root.children.clear()
        size = 0
    }

    private fun clear(map: HashMap<Char, Node>) {
        if (map.isEmpty()) {
            return
        }
        for (entry in map) {
            clear(entry.value.children)
            entry.value.children.clear()
        }
    }

    override fun toString(): String {
        return root.toString()
    }

    class Node(val element: Char) {
        val children = HashMap<Char, Node>()

        override operator fun equals(other: Any?): Boolean {
            other ?: return false
            if (other is Node) {
                return other.element == element
            }
            return false
        }

        override fun hashCode(): Int {
            var result = element.hashCode()
            result = 31 * result + children.hashCode()
            return result
        }

        override fun toString(): String {
            return "{children: $children}"
        }
    }
}