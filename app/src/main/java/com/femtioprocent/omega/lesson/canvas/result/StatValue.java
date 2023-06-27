package com.femtioprocent.omega.lesson.canvas.result;

// has UTF-8


import java.text.DecimalFormat;

public class StatValue {
    double sum;
    int cnt;

    final String NODATA = "·";   //// UTF-8

    int asInt(String s) {
        s = s.replace('-', ' ');
        s = s.replace('+', ' ');
        int a = Integer.parseInt(s.trim());
        return a;
    }

    void add(double v) {
        sum += v;
        cnt++;
    }

    void add(String s) {
        int v = asInt(s);
        add(v);
    }

    double getAvg1() {
        if (cnt == 0)
            return 0;
        return 1.0 * sum / cnt;
    }

    double getAvg1(int n) {
        if (n == 0)
            return 0;
        return 1.0 * sum / n;
    }

    double getAvg() {
        if (cnt == 0)
            return 0;
        return 100.0 * sum / cnt;
    }

    double getAvg_minus() {
        if (cnt == 0)
            return 0;
        return 100.0 * sum / cnt;
    }

    double getAvgTot(int n) {
        if (n == 0)
            return 0.0;
        return 100.0 * sum / n;
    }

    double getTotal() {
        return sum;
    }

    int getCnt() {
        return cnt;
    }

    String getTotal(String prfx) {
        return prfx + getTotal();
    }

    String getTotalInt(String prfx) {
        return prfx + (int) getTotal();
    }

    boolean has() {
        return cnt > 0;
    }

    boolean hasNot0() {
        return has() && sum > 0;
    }

    String getAvg_1000(String prfx, String suf) {
        if (cnt == 0)
            return NODATA;
        try {
            DecimalFormat df = new DecimalFormat("##0.0");
            String s = df.format(getAvg1() / 1000.0);
            return prfx + s + suf;
        } catch (NumberFormatException ex) {
            return "?";
        }
    }

    String getAvg(String prfx, String suf) {
        if (cnt == 0)
            return NODATA;
        DecimalFormat df = new DecimalFormat("##0.0");
        String s = df.format(getAvg());
        return prfx + s + suf;
    }

    String getAvgTot(String prfx, String suf, int n) {
        if (n == 0)
            return NODATA;
        DecimalFormat df = new DecimalFormat("##0.0");
        String s = df.format(getAvgTot(n));
        return prfx + s + suf;
    }

    String getCnt(String prfx) {
        return prfx + getCnt();
    }
}
