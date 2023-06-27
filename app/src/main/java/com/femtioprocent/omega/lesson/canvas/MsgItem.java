package com.femtioprocent.omega.lesson.canvas;

public class MsgItem {
    public String title;
    public String text;
    public String text2;
    public String image;
    public String image2;
    public String small_title;
    public char type;

    public MsgItem(String title, String txt) {
        this.title = title;
        this.text = txt;
        type = '2';
    }

    public MsgItem(char type, String title, String txt, String txt2, String image, String image2, String small_title) {
        this.type = type;
        this.title = title;
        this.text = txt;
        this.text2 = txt2;
        this.image = image;
        this.image2 = image2;
        this.small_title = small_title;
    }

    public String toString() {
        return "MsgItem{" + type +
                title + ',' +
                text + ',' +
                text2 + ',' + "}";
    }

}
