package com.femtioprocent.omega.xml.dtd;

import com.femtioprocent.omega.OmegaContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Dtd extends Item {
    List elem; // Item

    public Dtd() {
        elem = new ArrayList();
    }

    public void add(Element el) {
        elem.add(el);
    }

    public void render(StringBuffer sb) {
        sb.append("<?xml " + "encoding=\"US-ASCII\"" + "?>\n\n");
        Iterator it = elem.iterator();
        while (it.hasNext()) {
            Element el = (Element) it.next();
            el.render(sb);
        }
    }


    public static void main(String[] args) {
        Dtd d = new Dtd();

        Element apa = new Element("apa", "ANY");
        d.add(apa);

        Element el;
        d.add(el = new Element("elem", "(#PCDATA | " + apa.getName() + ")"));
        el.addAttr(new Attrib("attr", "CDATA", "#REQUIRED"));

        StringBuffer sb = new StringBuffer();
        d.render(sb);
        OmegaContext.sout_log.getLogger().info("" + sb.toString());
    }
}
