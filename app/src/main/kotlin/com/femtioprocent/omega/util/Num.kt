package com.femtioprocent.omega.util

object Num {
    fun grid(value: Int, grid: Int): Int {
	var value = value
	value /= grid
	return value * grid
    }

    fun howManyBits(a: Int): Int {
	var c = 0
	for (i in 0..31) if (1 shl i and a != 0) c++
	return c
    }
}
