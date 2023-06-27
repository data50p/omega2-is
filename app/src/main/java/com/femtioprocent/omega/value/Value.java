package com.femtioprocent.omega.value;


public class Value {
    public String id;
    public Object val;

    public Value(String id) {
        this.id = id;
    }

    public Value(String id, int v) {
        this.id = id;
        int[] ia = new int[1];
        ia[0] = v;
        val = ia;
    }

    public Value(String id, String v) {
        this.id = id;
        val = v;
    }

    public Value(String id, Object v) {
        this.id = id;
        val = v;
    }

    public int getInt() {
        return ((int[]) val)[0];
    }

    public void setInt(int v) {
        val = new int[1];
        ((int[]) val)[0] = v;
    }

    public String getStr() {
        if (val instanceof int[])
            return "" + getInt();
        return (String) val;
    }

    public Object getObj() {
        return val;
    }

    public void setStr(String v) {
        val = v;
    }

    public void setObj(Object v) {
        val = v;
    }

    public String getId() {
        return id;
    }

    public String toString() {
        return "Value{" + id + ',' + val + "}";
    }
}
