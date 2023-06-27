package com.femtioprocent.omega.util;

/**
 * Utility to measure precious time in milli seconds
 */
public class MilliTimer {
    /**
     * Start time
     */
    private long ct0;

    /**
     * Create an instance and start the timer
     */
    public MilliTimer() {
        ct0 = System.nanoTime();
    }

    /**
     * Return the timeout value in ms and reset to timer.
     *
     * @return
     */
    public String getString() {
        return getString("", "");
    }

    /**
     * Return timeout value with an prefix and default suffix " ms"
     *
     * @param prefix
     * @return
     */
    public String getString(String prefix) {
        return getString(prefix, " ms");
    }

    /**
     * Return the timer value in ms and surround the value with prefix and suffix
     *
     * @param prefix
     * @param suffix
     * @return
     */
    public String getString(String prefix, String suffix) {
        long ct1 = System.nanoTime();
        try {
            double val = 0.000001 * (ct1 - ct0);
            return prefix + String.format("%.6f", val) + suffix;
        } finally {
            ct0 = ct1;
        }
    }

    /**
     * Get the timer value in ms
     *
     * @return
     */
    public double getValue() {
        long ct1 = System.nanoTime();
        double val = 0.000001 * (ct1 - ct0);
        ct0 = ct1;
        return val;
    }

    public double pollValue() {
        long ct1 = System.nanoTime();
        double val = 0.000001 * (ct1 - ct0);
        return val;
    }

    /**
     * Check if timer has expired
     *
     * @param expireValue
     * @return
     */
    public boolean isExpired(long expireValue) {
        return isExpired(expireValue, null);
    }

    /**
     * Check is timer is expired and dep_set status
     *
     * @param expireValue in ms
     * @param status      dep_set[0] to the status, do nothing if null
     * @return
     */
    public boolean isExpired(long expireValue, String[] status) {
        long ct1 = System.nanoTime();
        double val = 0.000001 * (ct1 - ct0);
        String f = SundryUtils.formatMilliTime((ct1 - ct0) / 1000000, 0, true, false);
        String e = SundryUtils.formatMilliTime(expireValue, 0, true, false);

        if (status != null)
            status[0] = "" + (val >= expireValue) + ' ' + f + ' ' + e;
        return val >= expireValue;
    }
}
