package com.simple.server.header


class Range(private val length: Long) {

    var start: Long = 0
    var end: Long = -1

    fun setRange(start: Long, end: Long = -1) {
        this.start = start
        this.end = if (end < 0) length - 1 else end
    }

    override fun toString(): String {
        return "bytes=${start}-${end}"
    }

    companion object {

        private const val RANGE_PREFIX = "bytes="

        fun parseRange(length: Long, rangeString: String): Range? {
            if (rangeString.startsWith(RANGE_PREFIX)) {
                val range = Range(length)
                val s = rangeString.substring(RANGE_PREFIX.length)
                val hyphenIndex = s.indexOf('-')
                val start = s.substring(0, hyphenIndex).toLong()
                val end = s.substring(hyphenIndex + 1).run {
                    if (isEmpty()) {
                        return@run "-1"
                    }
                    return@run this
                }.toLong()
                println("range: start=$start, end=$end")
                range.setRange(start, end)
                return range
            } else {
                return null
            }
        }

    }

}