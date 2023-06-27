package com.femtioprocent.omega.anim.appl;

import com.femtioprocent.omega.swing.ToolExecute;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ToolBar_AnimEditor extends ToolBar_Base implements ActionListener {
    ToolExecute texec;

    ToolBar_AnimEditor(ToolExecute texec) {
        super(texec);
    }

    ToolBar_AnimEditor(ToolExecute texec, int orientation) {
        super(texec, orientation);
    }

    protected void init(Object o) {
        this.texec = (ToolExecute) o;
    }

    public void populateRest() {
//	addSeparator();

        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        JButton b;
        jp.add(b = new JButton(T.t("Actor")));
        b.setActionCommand("actor");
        b.addActionListener(this);

        jp.add(b = new JButton(T.t("Wings")));
        b.setActionCommand("wings");
        b.addActionListener(this);
        add(jp);

        jp.add(b = new JButton(T.t("Prop...")));
        b.setActionCommand("prop");
        b.addActionListener(this);
        add(jp);
    }

    private static boolean[][] msk_bool = {
            {true, false, false, false},
            {true, true, true, true},
            {true, false, true, true}
    };
    private static String s[] = {
            "path_create",
            "path_extend",
            "path_split",
            "path_delete"
    };

    public void enable_path(int mask) {
        boolean[] ba = msk_bool[mask];
        for (int i = 0; i < s.length; i++) {
            String ss = s[i];
        }
    }

    Container card_pan;
    CardLayout card;

    boolean who_act = true;

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("actor")) {
            card.show(card_pan, "actor");
            who_act = true;
            texec.execute("prop_act_show");
        } else if (ae.getActionCommand().equals("wings")) {
            card.show(card_pan, "wings");
            who_act = false;
            texec.execute("prop_wing_show");
        } else if (ae.getActionCommand().equals("prop")) {
            texec.execute(who_act ? "prop_act" : "prop_wing");
        }
    }
}
