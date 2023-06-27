package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.xml.Element;

public class SelectEntry extends Entry {
    public String extra;
    public String word;
    public int when;
    public String l_id;

    public SelectEntry(String extra, String word, int when, String l_id) {
        this.extra = extra;
        this.word = word;
        this.when = when;
        this.l_id = l_id;
        type = "select";
    }

    public SelectEntry(Element e) {
//  	String s = e.findAttr("type");
//  	type = s;
        type = "select";
        String s = e.findAttr("extra");
        extra = s;
        s = e.findAttr("word");
        word = s;
        s = e.findAttr("ord");
        ord = Integer.parseInt(s);
        s = e.findAttr("when");
        when = Integer.parseInt(s);
        s = e.findAttr("l_id");
        l_id = s;
    }

    Element getElement() {
        Element el = new Element("entry");
        el.addAttr("type", "select");
        el.addAttr("extra", extra);
        el.addAttr("word", word);
        el.addAttr("ord", "" + ord);
        el.addAttr("when", "" + when);
        el.addAttr("l_id", "" + l_id);
        return el;
    }

    public String toString() {
        return "SelectEntry{" +
                "extra" + extra +
                ", word" + word +
                ", ord" + "" + ord +
                ", when" + "" + when +
                ", l_id" + "" + l_id +
                "}";
    }
}
