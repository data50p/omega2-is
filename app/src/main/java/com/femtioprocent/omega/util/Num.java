package com.femtioprocent.omega.util;

public class Num {
    public static int grid(int val, int grid) {
        val /= grid;
        return val * grid;
    }

    public static int howManyBits(int a) {
        int c = 0;
        for (int i = 0; i < 32; i++)
            if (((1 << i) & a) != 0)
                c++;
        return c;
    }
}
