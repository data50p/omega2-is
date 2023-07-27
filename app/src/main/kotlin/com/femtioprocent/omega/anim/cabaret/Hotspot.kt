package com.femtioprocent.omega.anim.cabaret

class Hotspot {
    private val arr = DoubleArray(HOTSPOT_N * 2)
    operator fun set(ix: Int, x: Double, y: Double): Boolean {
	try {
	    arr[ix * 2] = x
	    arr[ix * 2 + 1] = y
	} catch (ex: ArrayIndexOutOfBoundsException) {
	    return false
	}
	return true
    }

    val isSeparate: Boolean
	get() = true

    fun getX(ix: Int): Double {
	return try {
	    arr[ix * 2]
	} catch (ex: ArrayIndexOutOfBoundsException) {
	    0.0
	}
    }

    fun getY(ix: Int): Double {
	return try {
	    arr[ix * 2 + 1]
	} catch (ex: ArrayIndexOutOfBoundsException) {
	    0.0
	}
    }

    fun getX(f: Double): Double {
	return try {
	    val a = arr[1 * 2]
	    val b = arr[2 * 2]
	    a + f * (b - a)
	} catch (ex: ArrayIndexOutOfBoundsException) {
	    0.0
	}
    }

    fun getY(f: Double): Double {
	return try {
	    val a = arr[1 * 2 + 1]
	    val b = arr[2 * 2 + 1]
	    a + f * (b - a)
	} catch (ex: ArrayIndexOutOfBoundsException) {
	    0.0
	}
    }

    val x: Double
	get() = getX(0)
    val y: Double
	get() = getY(0)

    init {
	for (ih in 0 until HOTSPOT_N) set(ih, 0.5, 0.5)
    }

    companion object {
	const val HOTSPOT_N = 3
	val allTypes = arrayOf("rotate", "begin", "end")
	@JvmStatic
        fun getType(ix: Int): String {
	    return allTypes[ix]
	}
    }
}
