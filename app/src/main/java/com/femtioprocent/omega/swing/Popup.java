package com.femtioprocent.omega.swing;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionListener;

public class Popup implements PopupMenuListener {
    Component comp;
    JPopupMenu jpop;

    public Popup(Component comp) {
        this.comp = comp;
    }

    public void popup(String title, String[] sa, int x, int y, ActionListener al) {
        jpop = new JPopupMenu(title);
        jpop.addPopupMenuListener(this);
        for (int i = 0; i < sa.length; i++) {
            if ("".equals(sa[i])) {
                jpop.addSeparator();
            } else {
                JMenuItem mi = new JMenuItem(sa[i]);
                mi.setActionCommand("" + i);
                mi.addActionListener(al);
                jpop.add(mi);
            }
        }
        jpop.show(comp, x, y);
    }

    public void popupMenuCanceled(PopupMenuEvent ev) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
    }
}
