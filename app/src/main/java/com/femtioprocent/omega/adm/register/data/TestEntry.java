package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.xml.Element;

public class TestEntry extends Entry {
    public String extra;
    public String sentence;
    public String answer;
    public int duration;
    public String cnt_correct_words;
    public String l_id_list;

    public TestEntry(String extra,
                     String sentence,
                     String answer,
                     int duration,
                     String cnt_correct_words,
                     String l_id_list) {
        this.sentence = sentence;
        this.answer = answer;
        this.duration = duration;
        this.cnt_correct_words = cnt_correct_words;
        this.l_id_list = l_id_list;
        type = "test";
    }

    public TestEntry(Element e) {
        type = "test";
//  	String s = e.findAttr("type");
//  	type = s;
        String s = e.findAttr("extra");
        extra = s;
        s = e.findAttr("sentence");
        sentence = s;
        s = e.findAttr("answer");
        answer = s;
        s = e.findAttr("ord");
        ord = Integer.parseInt(s);
        s = e.findAttr("duration");
        duration = Integer.parseInt(s);
        s = e.findAttr("cnt_correct_words");
        cnt_correct_words = s;// Integer.parseInt(s);
        s = e.findAttr("l_id_list");
        l_id_list = s;
    }

    Element getElement() {
        Element el = new Element("entry");
        el.addAttr("type", type);
        el.addAttr("extra", extra);
        el.addAttr("sentence", sentence);
        el.addAttr("answer", answer);
        el.addAttr("ord", "" + ord);
        el.addAttr("duration", "" + duration);
        el.addAttr("cnt_correct_words", "" + cnt_correct_words);
        el.addAttr("l_id_list", "" + l_id_list);
        return el;
    }

    public String toString() {
        return "TestEntry{" +
                "extra" + extra +
                ", sentence" + sentence +
                ", answer" + answer +
                ", ord" + "" + ord +
                ", duration" + "" + duration +
                ", cnt_correct_words" + "" + cnt_correct_words +
                ", l_id_list" + "" + l_id_list +
                "}";
    }
}
