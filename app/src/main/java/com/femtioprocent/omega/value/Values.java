package com.femtioprocent.omega.value;

import java.util.HashMap;
import java.util.Iterator;

public class Values {
    HashMap<String,Value> hm;

    public Values() {
        hm = new HashMap<>();
    }

    public Iterator iterator() {
        return hm.values().iterator();
    }

    Value getValue(String id) {
        Value v = (Value) hm.get(id);
        if (v == null) {
            v = new Value(id);
            hm.put(id, v);
        }
        return v;
    }

    public int getInt(String id) {
        Value v = getValue(id);
        return v.getInt();
    }

    public void setInt(String id, int a) {
        Value v = getValue(id);
        v.setInt(a);
    }

    public String getStr(String id) {
        Value v = getValue(id);
        return v.getStr();
    }

    public void setStr(String id, String s) {
        Value v = getValue(id);
        v.setStr(s);
    }

    public Object getObj(String id) {
        Value v = getValue(id);
        return v.getObj();
    }

    public void setObj(String id, Object o) {
        Value v = getValue(id);
        v.setObj(o);
    }

    public String toString() {
        return "Value{" + hm + "}";
    }
}
