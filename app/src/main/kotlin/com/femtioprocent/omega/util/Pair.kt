package com.femtioprocent.omega.util

/**
 * Created by lars on 2017-01-01.
 */
class Pair<T>(val fst: T, val snd: T) {
    override fun toString(): String {
	return "[$fst,$snd]"
    }
}
