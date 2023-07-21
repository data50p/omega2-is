package com.femtioprocent.omega.swing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ToolActionGroup {
    List<ToolAction> li;

    public ToolActionGroup() {
        li = new ArrayList();
    }

    public void add(ToolAction ta) {
        li.add(ta);
    }

    public Iterator iterator() {
        return li.iterator();
    }

    public int size() {
        return li.size();
    }

    public ToolAction find(String cmd) {
        Iterator<ToolAction> it = li.iterator();
        while (it.hasNext()) {
            ToolAction ta = (ToolAction) it.next();
            if (ta.getCommand().equals(cmd))
                return ta;
        }
        return null;
    }
}
