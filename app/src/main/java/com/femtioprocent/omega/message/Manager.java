package com.femtioprocent.omega.message;

import javax.swing.event.EventListenerList;

public class Manager {
    EventListenerList li = new EventListenerList();

    public void addListener(Listener l) {
        li.add(Listener.class, l);
    }

    public void removeListener(Listener l) {
        li.remove(Listener.class, l);
    }

    public void fire(String msg) {
        Object[] lia = li.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((Listener) lia[i + 1]).msg(msg);
        }

    }

    public String toString() {
        Object[] lia = li.getListenerList();
        return "Manager{" + lia.length + "}";
    }
}
