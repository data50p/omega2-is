package com.femtioprocent.omega.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenericEventManager {
    List<GenericEventListener> li = new ArrayList<>();

    public void fireGenericEvent(GenericEvent ge, Object a) {
        Iterator<GenericEventListener> it = li.iterator();
        while (it.hasNext()) {
            GenericEventListener gel = (GenericEventListener) it.next();
            if (true /*gel.grp.equals(ge.grp) */)
                gel.genericEvent(ge, a);
        }
    }

    public void addGenericEventListener(GenericEventListener gel) {
        li.add(gel);
    }
}
