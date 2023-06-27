package com.femtioprocent.omega.xml.dtd;

public class Attrib extends Item {
    String type;
    String key;
    String def;

    public Attrib(String name, String type, String key) {
        this(name, type, key, "");
    }

    public Attrib(String name, String type, String key, String def) {
        this.name = name;
        this.type = type;
        this.key = key;
        this.def = def;
    }

    public void render(StringBuffer sb) {
        sb.append("             " +
                name + ' ' +
                type + ' ' +
                key + ' ' +
                def + "\n");
    }
}
