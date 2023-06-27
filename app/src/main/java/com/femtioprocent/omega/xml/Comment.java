package com.femtioprocent.omega.xml;

public class Comment extends Node {
    StringBuffer comment;

    public Comment() {
        comment = new StringBuffer();
    }

    public Comment(String s) {
        comment = new StringBuffer();
        add(s);
    }

    public void add(String s) {
        comment.append(s);
    }

    public void add(char[] ca, int offs, int len) {
        comment.append(ca, offs, len);
    }

    public String getString() {
        return comment.toString();
    }

    public void render(StringBuffer sbu, StringBuffer sbl) {
        sbu.append("<!-- ");
        sbu.append(comment);
        sbu.append("-->");
    }
}

