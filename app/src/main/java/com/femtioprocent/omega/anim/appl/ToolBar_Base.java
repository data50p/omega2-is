package com.femtioprocent.omega.anim.appl;

import com.femtioprocent.omega.swing.ToolBar;

import java.util.HashMap;

public abstract class ToolBar_Base extends ToolBar {
    HashMap jb;

    ToolBar_Base(Object o) {
        super();
        jb = new HashMap();
        init(o);
    }

    ToolBar_Base(Object o, int orientation) {
        super(orientation);
        jb = new HashMap();
        init(o);
    }

    abstract protected void init(Object o);

    public void populate() {
        populate("default");
    }

    public void populate(String id) {
    }

    public void enable_path(int mask) {
    }
}
