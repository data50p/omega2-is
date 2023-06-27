package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.anim.tool.timeline.TimeLinePlayer;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TimeLineControlPanel extends JPanel implements ActionListener {
    TimeLinePanel tlp;
    UpDownTextField scale;
    UpDownTextField speed;
    JCheckBox xxx_b;

    public TimeLineControlPanel(TimeLinePanel tlp) {
        this.tlp = tlp;

        JPanel p = new JPanel();
        setLayout(new BorderLayout());
        p.setLayout(new GridLayout(0, 1));

        p.setBackground(new Color(180, 180, 180));

        scale = new UpDownTextField(T.t("Scale"), "1");
        scale.addActionListener(this);
        p.add(scale);

        speed = new UpDownTextField(T.t("Speed"), "1.0");
        speed.addActionListener(this);
        p.add(speed);


        JButton b = new JButton(T.t("Prop..."));
        b.setActionCommand("prop");
        b.addActionListener(this);
        p.add(b);

        xxx_b = new JCheckBox(T.t("lock"));
//	p.add(xxx_b);
        xxx_b.addActionListener(this);

        add(p, BorderLayout.WEST);
    }

    public void actionPerformed(ActionEvent ev) {
        if ("prop".equals(ev.getActionCommand())) {
            tlp.popupProp();
            return;
        }
        if (ev.getSource() == scale.up) {
            if (tlp.scale < 10)
                tlp.scale += 1;
            else
                tlp.scale += 10;
            scale.set("" + (tlp.scale / 10.0));
            tlp.repaint();
        }
        if (ev.getSource() == scale.down) {
            if (tlp.scale > 10)
                tlp.scale -= 10;
            else if (tlp.scale > 2)
                tlp.scale -= 1;
            scale.set("" + (tlp.scale / 10.0));
            tlp.repaint();
        }
        if (ev.getSource() == xxx_b) {
            tlp.setLock(xxx_b.isSelected());
        }
        if (ev.getSource() == speed.down) {
            if (!TimeLinePlayer.getDefaultTimeLinePlayer().adjustSpeed(0.8)) {
                speed.down.setEnabled(false);
                speed.up.setEnabled(true);
            } else {
                speed.down.setEnabled(true);
                speed.up.setEnabled(true);
            }
            speed.set("" + TimeLinePlayer.getDefaultTimeLinePlayer().getSpeed());
        }
        if (ev.getSource() == speed.up) {
            if (!TimeLinePlayer.getDefaultTimeLinePlayer().adjustSpeed(1.0 / 0.8)) {
                speed.down.setEnabled(true);
                speed.up.setEnabled(false);
            } else {
                speed.down.setEnabled(true);
                speed.up.setEnabled(true);
            }
            speed.set("" + TimeLinePlayer.getDefaultTimeLinePlayer().getSpeed());
        }
    }

    public void setLock(boolean b) {
        xxx_b.setSelected(b);
        tlp.setLock(xxx_b.isSelected());
    }
}
