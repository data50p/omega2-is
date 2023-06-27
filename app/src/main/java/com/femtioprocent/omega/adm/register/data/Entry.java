package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.xml.Element;

abstract public class Entry {
    public int ord;
    public String type;

    abstract Element getElement();

    static Entry create(Element entry) {
        String ty = entry.findAttr("type");
        Entry e = null;
        if ("select".equals(ty))
            e = new SelectEntry(entry);
        if ("test".equals(ty))
            e = new TestEntry(entry);
        if ("create".equals(ty))
            e = new CreateEntry(entry);
        return e;
    }

    public String toString() {
        return "Entry{}";
    }
}
