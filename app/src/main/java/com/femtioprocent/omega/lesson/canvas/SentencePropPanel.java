package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

@Deprecated
class SentencePropPanel extends JPanel {
    SentenceProperty sprop;
    HashMap<String,JComponent> guimap;

    GBC_Factory gbcf = new GBC_Factory();

    private SentencePropPanel(SentenceProperty sprop) {
        this.sprop = sprop;
        guimap = sprop.guimap;
        build();
    }

    class myActionListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            String s = ev.getActionCommand();
            if (s.equals("dep_set action")) {

            }
        }
    }

    ;
    myActionListener myactl = new myActionListener();

    void build() {
        setLayout(new GridBagLayout());

        JLabel jl;
        JTextField tf;
        JComboBox cb;
        JCheckBox ch;
        JButton jb;

        int Y = 0;
        int X = 0;

        JRadioButton rb1, rb2;
        ButtonGroup bgr = new ButtonGroup();
        add(rb1 = new JRadioButton(T.t("Default, as dep_set in word prop")), gbcf.createL(X++, Y, 1));
        bgr.add(rb1);

        Y++;
        X = 0;
        add(rb2 = new JRadioButton(T.t("Specific")), gbcf.createL(X++, Y, 1));
        bgr.add(rb2);
        add(jb = new JButton(T.t("Set action file")), gbcf.createL(X++, Y, 1));
        guimap.put("dep_set action file", jb);
        jb.setActionCommand("dep_set action file");
        jb.addActionListener(myactl);
    }
}
