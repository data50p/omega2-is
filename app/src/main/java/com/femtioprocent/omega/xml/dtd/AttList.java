package com.femtioprocent.omega.xml.dtd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AttList extends Item {
    List l;

    public AttList(String name) {
        this.name = name;
        l = new ArrayList();
    }

    void add(Attrib a) {
        l.add(a);
    }

    public void render(StringBuffer sb) {
        if (l.size() > 0) {
            sb.append("<!ATTLIST " + name + "\n");
            Iterator it = l.iterator();
            while (it.hasNext()) {
                Attrib a = (Attrib) it.next();
                a.render(sb);
            }
            sb.append(">\n");
        }
    }
}
