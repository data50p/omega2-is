package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.xml.Element;

public class CreateEntry extends Entry {
    public String sentence;
    public int duration;
    public String l_id_list;

    public CreateEntry(String sentence, int duration, String l_id_list) {
        this.sentence = sentence;
        this.duration = duration;
        this.l_id_list = l_id_list;
        type = "create";
    }

    public CreateEntry(Element e) {
//  	String s = e.findAttr("type");
//  	type = s;
        type = "create";
        String s = e.findAttr("sentence");
        sentence = s;
        s = e.findAttr("ord");
        ord = Integer.parseInt(s);
        s = e.findAttr("duration");
        duration = Integer.parseInt(s);
        s = e.findAttr("l_id_list");
        l_id_list = s;
    }

    Element getElement() {
        Element el = new Element("entry");
        el.addAttr("type", "create");
        el.addAttr("sentence", sentence);
        el.addAttr("ord", "" + ord);
        el.addAttr("duration", "" + duration);
        el.addAttr("l_id_list", "" + l_id_list);
        return el;
    }

    public String toString() {
        return "CreateEntry{" +
                ", sentence" + sentence +
                ", ord" + "" + ord +
                ", duration" + "" + duration +
                ", l_id_list" + "" + l_id_list +
                "}";
    }
}
