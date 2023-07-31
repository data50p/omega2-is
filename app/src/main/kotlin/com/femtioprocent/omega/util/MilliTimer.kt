package com.femtioprocent.omega.util

/**
 * Utility to measure precious time in milli seconds
 */
class MilliTimer {
    /**
     * Start time
     */
    private var ct0: Long

    /**
     * Create an instance and start the timer
     */
    init {
	ct0 = System.nanoTime()
    }

    val string: String
	/**
	 * Return the timeout value in ms and reset to timer.
	 *
	 * @return
	 */
	get() = getString("", "")

    /**
     * Return timeout value with an prefix and default suffix " ms"
     *
     * @param prefix
     * @return
     */
    fun getString(prefix: String): String {
	return getString(prefix, " ms")
    }

    /**
     * Return the timer value in ms and surround the value with prefix and suffix
     *
     * @param prefix
     * @param suffix
     * @return
     */
    fun getString(prefix: String, suffix: String): String {
	val ct1 = System.nanoTime()
	return try {
	    val `val` = 0.000001 * (ct1 - ct0)
	    prefix + String.format("%.6f", `val`) + suffix
	} finally {
	    ct0 = ct1
	}
    }

    val value: Double
	/**
	 * Get the timer value in ms
	 *
	 * @return
	 */
	get() {
	    val ct1 = System.nanoTime()
	    val `val` = 0.000001 * (ct1 - ct0)
	    ct0 = ct1
	    return `val`
	}

    fun pollValue(): Double {
	val ct1 = System.nanoTime()
	return 0.000001 * (ct1 - ct0)
    }

    /**
     * Check if timer has expired
     *
     * @param expireValue
     * @return
     */
    fun isExpired(expireValue: Long): Boolean {
	return isExpired(expireValue, null)
    }

    /**
     * Check is timer is expired and dep_set status
     *
     * @param expireValue in ms
     * @param status      dep_set[0] to the status, do nothing if null
     * @return
     */
    fun isExpired(expireValue: Long, status: Array<String?>?): Boolean {
	val ct1 = System.nanoTime()
	val `val` = 0.000001 * (ct1 - ct0)
	val f = SundryUtils.formatMilliTime((ct1 - ct0) / 1000000, 0, true, false)
	val e = SundryUtils.formatMilliTime(expireValue, 0, true, false)
	if (status != null) status[0] = "" + (`val` >= expireValue) + ' ' + f + ' ' + e
	return `val` >= expireValue
    }
}
