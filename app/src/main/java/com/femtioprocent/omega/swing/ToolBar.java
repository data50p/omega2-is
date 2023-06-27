package com.femtioprocent.omega.swing;

import javax.swing.*;
import java.util.Iterator;

public class ToolBar extends JToolBar {
    public ToolBar() {
    }

    public ToolBar(int orientation) {
        super(orientation);
    }

    public JComponent add(JComponent comp) {
        super.add(comp);
        comp.setAlignmentY(0.1f);
        comp.setAlignmentX(0.5f);
        return comp;
    }

    public JButton add(ToolAction ta) {
        JButton jb = super.add(ta);
        jb.setActionCommand(ta.getCommand());
        jb.setAlignmentY(0.1f);
        jb.setAlignmentX(0.5f);
        return jb;
    }

    public JButton[] add(ToolActionGroup tbg, ToolExecute texec) {
        JButton jba[] = new JButton[tbg.size()];
        Iterator it = tbg.iterator();
        int ix = 0;
        while (it.hasNext()) {
            ToolAction ta = (ToolAction) it.next();
            if (ta.getCommand() == null)
                addSeparator();
            else {
                ta.texec = texec;
                JButton jb = add(ta);
                jb.setActionCommand(ta.getCommand());
                jb.setAlignmentY(0.1f);
                jb.setAlignmentX(0.5f);
                jba[ix++] = jb;
            }
        }
        return jba;
    }
}
