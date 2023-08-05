package com.femtioprocent.omega.util

import com.femtioprocent.omega.util.SundryUtils.split
import java.io.ByteArrayOutputStream
import java.io.PrintStream

object StackTrace {
    fun trace(): String {
	val bos = ByteArrayOutputStream()
	val ps = PrintStream(bos)
	try {
	    throw Exception()
	} catch (ex: Exception) {
	    ex.printStackTrace(ps)
	}
	return bos.toString()
    }

    fun trace1(): String {
	val s = trace()
	val sa = split(s, "\n")
	return sa[3]
    }
}
