package com.femtioprocent.omega.lesson.canvas.resultimport

import java.text.DecimalFormat


class StatValue1 {
    var total = 0.0
    var cnt = 0
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

    val avg: Double
	get() = 100.0 * total / cnt
    val avg_minus: Double
	get() = 100.0 * total / cnt

    fun getAvgTot(n: Int): Double {
	return 100.0 * total / n
    }

    fun getTotal(prfx: String): String {
	return prfx + total
    }

    fun getTotalInt(prfx: String): String {
	return prfx + total.toInt()
    }

    fun getAvg(prfx: String, suf: String): String {
	val df = DecimalFormat("##0.0#")
	val s = df.format(avg)
	return prfx + s + suf
    }

    fun getAvgTot(prfx: String, suf: String, n: Int): String {
	val df = DecimalFormat("##0.0#")
	val s = df.format(getAvgTot(n))
	return prfx + s + suf
    }

    fun getCnt(prfx: String): String {
	return prfx + cnt
    }
}
