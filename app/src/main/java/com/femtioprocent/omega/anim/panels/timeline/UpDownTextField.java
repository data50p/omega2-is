package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.swing.OmegaSwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class UpDownTextField extends JPanel {
    JLabel l;
    JTextField tf;
    JButton up, down;

    private int def, min, max;

    UpDownTextField(String label, String def) {
        l = new JLabel(label);
        tf = new JTextField(def, 4);
        tf.setEditable(false);
        up = new JButton(OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/navigation/Up16.gif"));
        down = new JButton(OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/navigation/Down16.gif"));
        up.setPreferredSize(new Dimension(16, 12));
        down.setPreferredSize(new Dimension(16, 12));
        JPanel pl = new JPanel();
        JPanel pr = new JPanel();
        pr.setLayout(new GridLayout(0, 1));
        pr.add(up);
        pr.add(down);
        pl.add(l);
        pl.add(tf);
        add(pl);
        add(pr);
    }

    void set(String s) {
        tf.setText(s);
    }


    void addActionListener(ActionListener al) {
        up.addActionListener(al);
        down.addActionListener(al);
    }
}
