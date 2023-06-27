package com.femtioprocent.omega.xml.dtd;

public class SeqItem {
    String id;
    char cnt;

    public SeqItem(String id) {
        this(id, ' ');
    }

    public SeqItem(String id, char cnt) {
        this.id = id;
        this.cnt = cnt;
    }

    public SeqItem(Item itm) {
        this(itm.getName());
    }

    public SeqItem(Item itm, char cnt) {
        this(itm.getName(), cnt);
    }

    public String toString() {
        return "" + id + cnt;
    }
}
