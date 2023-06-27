package com.femtioprocent.omega.xml;

public class PCDATA extends Node {
    StringBuffer pcdata;

    public PCDATA() {
        pcdata = new StringBuffer();
    }

    public PCDATA(String s) {
        pcdata = new StringBuffer();
        add(s);
    }

    public void add(String s) {
        pcdata.append(s);
    }

    public void add(char[] ca, int offs, int len) {
        pcdata.append(ca, offs, len);
    }

    public String getString() {
        return pcdata.toString();
    }

    public void render(StringBuffer sbu, StringBuffer sbl) {
        sbu.append(pcdata);
    }
}

