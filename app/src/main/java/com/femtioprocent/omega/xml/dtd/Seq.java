package com.femtioprocent.omega.xml.dtd;

import com.femtioprocent.omega.OmegaContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Seq extends SeqItem {
    List l;
    char type;

    public Seq(char type) {
        this(type, ' ');
    }

    public Seq(char type, char cnt) {
        super("(", cnt);
        this.type = type;
        l = new ArrayList();
    }

    public void add(SeqItem si) {
        l.add(si);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = l.iterator();
        while (it.hasNext()) {
            SeqItem si = (SeqItem) it.next();
            sb.append(si.toString());
            if (it.hasNext())
                sb.append(type);
        }
        return "(" + sb.toString() + ")" + cnt;
    }

    public static void main(String[] args) {
        Seq s = new Seq(',', '*');

        s.add(new SeqItem("meta", '?'));
        s.add(new SeqItem("PC"));
        s.add(new SeqItem("f5_drag", '*'));

        OmegaContext.sout_log.getLogger().info("" + s.toString());
    }
}
