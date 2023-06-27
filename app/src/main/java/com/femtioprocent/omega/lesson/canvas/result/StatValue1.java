package com.femtioprocent.omega.lesson.canvas.result;

import java.text.DecimalFormat;


public class StatValue1 {
    double sum;
    int cnt;

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

    double getAvg() {
        return 100.0 * sum / cnt;
    }

    double getAvg_minus() {
        return 100.0 * sum / cnt;
    }

    double getAvgTot(int n) {
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

    String getAvg(String prfx, String suf) {
        DecimalFormat df = new DecimalFormat("##0.0#");
        String s = df.format(getAvg());
        return prfx + s + suf;
    }

    String getAvgTot(String prfx, String suf, int n) {
        DecimalFormat df = new DecimalFormat("##0.0#");
        String s = df.format(getAvgTot(n));
        return prfx + s + suf;
    }

    String getCnt(String prfx) {
        return prfx + getCnt();
    }
}
// class StatValue1 {
//     double sum;
//     int cnt;

//     int asInt(String s) {
// 	s = s.replace('-', ' ');
// 	s = s.replace('+', ' ');
// 	int a = Integer.parseInt(s.trim());
// 	return a;
//     }

//     void add(double v) {
// 	sum += v;
// 	cnt++;
//     }

//     void add(String s) {
// 	int v = asInt(s);
// 	add(v);
//     }

//     double getAvg() {
// 	return 100.0 * sum / cnt;
//     }
//     double getAvg_minus() {
// 	return 100.0 * sum / cnt;
//     }

//     double getAvgTot(int n) {
// 	return 100.0 * sum / n;
//     }

//     double getTotal() {
// 	return sum;
//     }

//     int getCnt() {
// 	return cnt;
//     }

//     String getTotal(String prfx) {
// 	return prfx + getTotal();
//     }

//     String getTotalInt(String prfx) {
// 	return prfx + (int)getTotal();
//     }

//     String getAvg(String prfx, String suf) {
// 	DecimalFormat df = new DecimalFormat("##0.0#");
// 	String s = df.format(getAvg());
// 	return prfx + s + suf;
//     }

//     String getAvgTot(String prfx, String suf, int n) {
// 	DecimalFormat df = new DecimalFormat("##0.0#");
// 	String s = df.format(getAvgTot(n));
// 	return prfx + s + suf;
//     }

//     String getCnt(String prfx) {
// 	return prfx + getCnt();
//     }
// }
