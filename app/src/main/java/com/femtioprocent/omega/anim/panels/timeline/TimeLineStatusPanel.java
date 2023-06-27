package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TimeLineStatusPanel extends JPanel {
    TimeLinePanel tlp;
    JLabel pos_st;
    JLabel pos_m_st;
    JLabel info;
    JLabel status;

    class Mouse extends MouseInputAdapter {
        public void mousePressed(MouseEvent e) {
            Point pd = new Point(e.getX(), e.getY());
        }

        public void mouseDragged(MouseEvent e) {
            Point pd = new Point(e.getX(), e.getY());
        }
    }

    public TimeLineStatusPanel(TimeLinePanel tlp) {
        this.tlp = tlp;
        Mouse mouse = new Mouse();
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        JPanel p = new JPanel();
        setLayout(new BorderLayout());
        p.setLayout(new GridBagLayout());
        p.setLayout(new FlowLayout());
        p.setBackground(new Color(180, 180, 180));

        Insets ins = new Insets(0, 0, 0, 0);
        GridBagConstraints con = new GridBagConstraints(0, 0,
                1, 1,
                1, 1,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                ins,
                0, 0);

        info = new JLabel(T.t("Info:") + "                                       ");
        info.setBorder(new BevelBorder(BevelBorder.LOWERED));
        info.setForeground(Color.black);
        p.add(info, con);

        status = new JLabel(T.t("Status:") + "                                    ");
        status.setBorder(new BevelBorder(BevelBorder.LOWERED));
        status.setForeground(Color.black);
        con.gridx++;
        p.add(status, con);

        pos_st = new JLabel(T.t("Time Pos:") + " " + tlp.getHitMS());
        pos_st.setBorder(new BevelBorder(BevelBorder.LOWERED));
        pos_st.setForeground(Color.black);
        pos_st.setPreferredSize(new Dimension(130, 22));
        con.gridx++;
        p.add(pos_st, con);

        pos_m_st = new JLabel(T.t("Mouse Pos:") + " " + tlp.getHitMS());
        pos_m_st.setBorder(new BevelBorder(BevelBorder.LOWERED));
        pos_m_st.setForeground(Color.black);
        pos_m_st.setPreferredSize(new Dimension(180, 22));
        con.gridx++;
        p.add(pos_m_st, con);

        add(p, BorderLayout.EAST);

        tlp.addTimeLinePanelListener(new TimeLinePanelAdapter() {
            public void updateValues() {
                TimeLineStatusPanel.this.updateValues();
            }
        });
    }

    public void updateValues() {
        pos_st.setText("Time Pos: " + tlp.getHitMS());
        pos_m_st.setText(T.t("Mouse Pos:") + " " + tlp.getMouseHitMS() + ' ' +
                (tlp.getMouseHitMS() - tlp.getHitMS()));
        info.setText(T.t("Info:") + " " + '?');//tlp.th.speed);
    }

//      public Dimension getPreferredSize() {
//  	return new Dimension(1000, 30);
//      }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
    }
}
