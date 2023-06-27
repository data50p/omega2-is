package com.femtioprocent.omega.xml.dtd;

abstract class Item {
    String name;

    abstract void render(StringBuffer sb);

    public String getName() {
        return name;
    }
}
