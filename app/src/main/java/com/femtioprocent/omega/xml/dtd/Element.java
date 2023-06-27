package com.femtioprocent.omega.xml.dtd;

public class Element extends Item {
    AttList attl;
    String def;

    public Element(String name, String def) {
        this.name = name;
        this.def = def;
        attl = new AttList(name);
    }

    public Element(String name, Seq seq) {
        this.name = name;
        this.def = seq.toString();
        attl = new AttList(name);
    }

    public void addAttr(Attrib a) {
        attl.add(a);
    }

    public void addAttr(String name, String type, String key) {
        addAttr(new Attrib(name, type, key));
    }

    public void render(StringBuffer sb) {
        sb.append("<!ELEMENT " + name + " " + def + ">\n");
        attl.render(sb);
        sb.append("\n");
    }
}
