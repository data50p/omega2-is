package com.femtioprocent.omega.lesson.settings;

import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsDialog extends JDialog implements ActionListener {
    JPanel jpan = new JPanel();
    private GBC_Factory gbcf = new GBC_Factory();
    private int X = 0;
    private int Y = 0;

    public SettingsDialog(String title) {
        super(ApplContext.top_frame, title, true);
    }

    void addb(String txt, String cmd) {
        JButton b = new JButton(txt);
        b.setActionCommand(cmd);
        b.addActionListener(this);
        jpan.add(b);
    }

    JTextField addtf(JPanel jp, String txt) {
        JTextField tf = new JTextField(20);
        JLabel l = new JLabel(txt);
        l.setLabelFor(tf);
        jp.add(l, gbcf.createL(X++, Y, 1));
        jp.add(tf, gbcf.createL(X++, Y, 1));
        X = 0;
        Y++;
        return tf;
    }

    JComboBox addcb(JPanel jp, String txt, String[] sa) {
        JComboBox cb = new JComboBox();
        JLabel l = new JLabel(txt);
        l.setLabelFor(cb);
        jp.add(l, gbcf.createL(X++, Y, 1));
        jp.add(cb, gbcf.createL(X++, Y, 1));
        for (int i = 0; i < sa.length; i++)
            cb.addItem(sa[i]);
        X = 0;
        Y++;
        return cb;
    }

    JCheckBox addchb(JPanel jp, String txt) {
        JCheckBox chb = new JCheckBox();
        JLabel l = new JLabel(txt);
        l.setLabelFor(chb);
        jp.add(l, gbcf.createL(X++, Y, 1));
        jp.add(chb, gbcf.createL(X++, Y, 1));
        X = 0;
        Y++;
        return chb;
    }

    void populateCommon() {
        Container c = getContentPane();

        addb(T.t("OK"), "OK");
        addb(T.t("Cancel"), "cancel");

        c.add(jpan, BorderLayout.SOUTH);
    }

    boolean save() {
        return false;
    }

    void load() {
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "" + cmd);
        if ("OK".equals(cmd)) {
            if ( save() == false )
                JOptionPane.showMessageDialog(ApplContext.top_frame,
                        "Can't save");
            setVisible(false);
        }
        if ("cancel".equals(cmd)) {
            setVisible(false);
        }
    }
}
