package com.femtioprocent.omega.util;

import java.util.HashMap;

public class GenericEvent {
    public String grp;
    public String id;
    public HashMap hm;

    public GenericEvent(String grp, String id, HashMap hm) {
        this.grp = grp;
        this.id = id;
        this.hm = hm;
    }
}
