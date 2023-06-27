package com.femtioprocent.omega.util;

public class DelimitedStringBuilder {
    StringBuilder sb = new StringBuilder();
    String delim;

    public DelimitedStringBuilder() {
        this(" ");
    }

    public DelimitedStringBuilder(String delim) {
        this.delim = delim;
    }

    public StringBuilder append(String s) {
        if (sb.length() > 0)
            sb.append(delim);
        return sb.append(s);
    }

    public StringBuilder append(char ch) {
        if (sb.length() > 0)
            sb.append(delim);
        return sb.append(ch);
    }

    public String toString() {
        return sb.toString();
    }
}

