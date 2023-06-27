package com.femtioprocent.omega.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StackTrace {
    public static String trace() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        try {
            throw new Exception();
        } catch (Exception ex) {
            ex.printStackTrace(ps);
        }
        return bos.toString();
    }

    public static String trace1() {
        String s = trace();
        String[] sa = SundryUtils.split(s, "\n");
        return sa[3];
    }
}
