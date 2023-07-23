package com.femtioprocent.omega.util

class DelimitedStringBuilder @JvmOverloads constructor(var delim: String = " ") {
    var sb = StringBuilder()

    fun append(s: String?): StringBuilder {
	if (sb.length > 0) sb.append(delim)
	return sb.append(s)
    }

    fun append(ch: Char): StringBuilder {
	if (sb.length > 0) sb.append(delim)
	return sb.append(ch)
    }

    override fun toString(): String {
	return sb.toString()
    }
}
