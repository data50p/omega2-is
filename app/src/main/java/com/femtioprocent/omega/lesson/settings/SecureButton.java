package com.femtioprocent.omega.lesson.settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SecureButton extends JButton {
    JCheckBox cb;
    JLabel jl;
    PupilSettingsDialog psd;

    ActionListener item_sel_al = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            JComponent jc = (JComponent) ae.getSource();
            if (jc == cb) {
                SecureButton.this.repaint();
            }
            if (jc == SecureButton.this) {
                if (cb.isSelected()) {
// 			was_deleted = true;
// 			hide();
                    psd.showMore();
                }
            }
        }
    };

    class Mouse extends MouseAdapter {
        public void mouseExited(MouseEvent e) {
            cb.setSelected(false);
//	    psd.showNoMore();
            SecureButton.this.repaint();
        }
    }

    Mouse m = new Mouse();

    SecureButton(PupilSettingsDialog psd, String text) {
        super("");
        this.psd = psd;
        jl = new JLabel("        " + text);
        jl.setForeground(new Color(0, 0, 40));
        cb = new JCheckBox();
        cb.addActionListener(item_sel_al);
        add(cb);
        add(jl);
        addActionListener(item_sel_al);
        addMouseListener(m);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!cb.isSelected()) {
            g.setColor(new Color(233, 243, 243));
            g.fillRect(0, 0, 233, 233);
            for (int i = -33; i < 233; i += 12) {
                g.setColor(new Color(233, 53, 53));
                for (int ii = 0; ii < 6; ii++) {
                    g.drawLine(i + ii + 33, 0, i + ii, 33);
                }
            }
        }
    }
}
