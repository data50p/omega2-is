package com.femtioprocent.omega.xml;

import com.femtioprocent.omega.util.Log;

public class XML {
    XML() {
    }

    void test() {
        Element xn = new Element("test");

        PCDATA pcd = new PCDATA();
        pcd.add("hello world");
        xn.add(pcd);

        Element xn2 = new Element("empty");
        xn.add(xn2);

        xn2 = new Element("pcdata", "PCDATA");
        xn.add(xn2);

        xn.add(xn2 = new Element("notempty"));
        xn2.add(new PCDATA("first text"));

        xn2 = new Element("emptyA");
        xn2.addAttr("attr", "value");
        xn2.addAttr("attr2", "value2");
        xn.add(xn2);
        xn.add(xn2 = new Element("notemptyA"));
        xn2.addAttr("attr3", "value3");
        xn2.addAttr("attr4", "value4");
        xn2.add(new PCDATA("last text"));

        StringBuffer sbu = new StringBuffer();
        StringBuffer sbl = new StringBuffer();
        xn.render(sbu, sbl);

        Log.getLogger().info(sbu.toString());
    }

    static public void main(String[] args) {
        XML xml = new XML();
        xml.test();
    }
}
