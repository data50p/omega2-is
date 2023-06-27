package com.femtioprocent.omega.adm.people;

public abstract class People {
    public String null_name;
    public String name;

    public void setName(String name) {
        if (name == null)
            this.name = null_name;
        else
            this.name = name;
    }

    public String toString() {
        return "People{" + name + "}";
    }
}
