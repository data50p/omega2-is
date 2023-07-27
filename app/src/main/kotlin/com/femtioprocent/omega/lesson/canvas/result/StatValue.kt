package com.femtioprocent.omega.lesson.canvas.resultimport

import java.text.DecimalFormat

// has UTF-8
class StatValue {
    var total = 0.0
    var cnt = 0
    val NODATA = "·" //// UTF-8
    fun asInt(s: String): Int {
	var s = s
	s = s.replace('-', ' ')
	s = s.replace('+', ' ')
	return s.trim { it <= ' ' }.toInt()
    }

    fun add(v: Double) {
	total += v
	cnt++
    }

    fun add(s: String) {
	val v = asInt(s)
	add(v.toDouble())
    }

    val avg1: Double
	get() = if (cnt == 0) 0.0 else 1.0 * total / cnt

    fun getAvg1(n: Int): Double {
	return if (n == 0) 0.0 else 1.0 * total / n
    }

    val avg: Double
	get() = if (cnt == 0) 0.0 else 100.0 * total / cnt
    val avg_minus: Double
	get() = if (cnt == 0) 0.0 else 100.0 * total / cnt

    fun getAvgTot(n: Int): Double {
	return if (n == 0) 0.0 else 100.0 * total / n
    }

    fun getTotal(prfx: String): String {
	return prfx + total
    }

    fun getTotalInt(prfx: String): String {
	return prfx + total.toInt()
    }

    fun has(): Boolean {
	return cnt > 0
    }

    fun hasNot0(): Boolean {
	return has() && total > 0
    }

    fun getAvg_1000(prfx: String, suf: String): String {
	return if (cnt == 0) NODATA else try {
	    val df = DecimalFormat("##0.0")
	    val s = df.format(avg1 / 1000.0)
	    prfx + s + suf
	} catch (ex: NumberFormatException) {
	    "?"
	}
    }

    fun getAvg(prfx: String, suf: String): String {
	if (cnt == 0) return NODATA
	val df = DecimalFormat("##0.0")
	val s = df.format(avg)
	return prfx + s + suf
    }

    fun getAvgTot(prfx: String, suf: String, n: Int): String {
	if (n == 0) return NODATA
	val df = DecimalFormat("##0.0")
	val s = df.format(getAvgTot(n))
	return prfx + s + suf
    }

    fun getCnt(prfx: String): String {
	return prfx + cnt
    }
}
