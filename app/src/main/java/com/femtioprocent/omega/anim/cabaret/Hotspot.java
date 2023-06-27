package com.femtioprocent.omega.anim.cabaret;

public class Hotspot {
    public static final int HOTSPOT_N = 3;
    private double arr[] = new double[HOTSPOT_N * 2];

    public Hotspot() {
        for (int ih = 0; ih < HOTSPOT_N; ih++)
            set(ih, 0.5, 0.5);
    }

    public boolean set(int ix, double x, double y) {
        try {
            arr[ix * 2] = x;
            arr[ix * 2 + 1] = y;
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        return true;
    }

    public boolean isSeparate() {
        return true;
    }

    public double getX(int ix) {
        try {
            return arr[ix * 2];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public double getY(int ix) {
        try {
            return arr[ix * 2 + 1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public double getX(double f) {
        try {
            double a = arr[1 * 2];
            double b = arr[2 * 2];
            return a + f * (b - a);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public double getY(double f) {
        try {
            double a = arr[1 * 2 + 1];
            double b = arr[2 * 2 + 1];
            return a + f * (b - a);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
    }

    public double getX() {
        return getX(0);
    }

    public double getY() {
        return getY(0);
    }

    static private String[] type_s = {"rotate", "begin", "end"};

    public static String getType(int ix) {
        return type_s[ix];
    }

    public static String[] getAllTypes() {
        return type_s;
    }
}
