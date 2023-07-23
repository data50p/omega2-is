package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.anim.tool.timeline.MasterTimeLine;
import com.femtioprocent.omega.anim.tool.timeline.TimeLine;
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker;
import com.femtioprocent.omega.swing.Popup;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

public class TimeLinePanel extends JPanel {
    private static final int TL_H = 18;
    private static final int TL_M_DUR = 7;
    private static final int TL_M_SCAN = 14;
    private static final int HITM_NORMAL = 0;
    private static final int HITM_scan_object = 1;

    final MasterTimeLine mtl;
    private EventListenerList listeners;

    boolean lock = false;

    public int selected_tl = -1;

    int scale = 10;

    private int grid = 1;

    private Point press_p;

    private int tick_m = 0;
    private int hitMode = HITM_NORMAL;
    int tick_start = 0;

    public static int global_tick_stop = 5000;

    private int tick_h = 0;
    private int ltick_h = 0;

    private TimeMarkerProperties tmp = null;

    private boolean last_was_back = false;

    class Mouse extends MouseInputAdapter {
        public void mousePressed(final MouseEvent e) {
            Point pd = new Point(e.getX(), e.getY());
            press_p = pd;

            if (e.getY() < TL_M_SCAN) {
                hitMode = HITM_scan_object;
                int t_h = ((e.getX() - TLOFF) * scale) / 10;
                t_h *= 10;
                setTick(t_h);
                firePropertyChange();
                mtl.playAt(tick_h);
                repaint();
            } else {
                int old_selected_tl = selected_tl;

                deselectAllTimeLine();
                int tl_ix = hitTimeLine(pd);
                selected_tl = tl_ix;

                final TimeLine tl = mtl.getTimeLine(selected_tl);
                if (tl == null)
                    return;
                fireEvent("selectTL", tl);

                int mod = e.getModifiersEx();

                boolean pt = e.isPopupTrigger(); // macOS ?

                if (pt) {
                    if (e.isControlDown()) {
                        popup_ctrl_maction(e, tl);
                    } else {
                        popup_maction(e, tl);
                    }
                } else {
                    normal_maction(e, tl);
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            if (hitMode == HITM_scan_object) {
                int t_h = ((e.getX() - TLOFF) * scale) / 10;
                t_h *= 10;
                setTick(t_h);
                if (tick_h < mtl.getLastTimeTick())
                    last_was_back = true;
                firePropertyChange();
                if (tick_h > mtl.getLastTimeTick() && last_was_back) {
                    mtl.setLastTimeTick(0);
                    last_was_back = false;
                }
                mtl.playAt(tick_h);
                repaint();
            } else if (hitMode == HITM_NORMAL) {
                if (selected_tl != -1 && lock == false) {
                    TimeLine tl = mtl.getTimeLine(selected_tl);
                    if (tl != null) {
                        if (e.isShiftDown()) {
                            int dx = scale * (int) (e.getX() - press_p.getX());
                            dx = tl.adjustMove(dx);
                            AnimContext.ae.setDirty(true);
                            if (dx != 0) {
                                tl.move(dx);
                                press_p = new Point(e.getX(), e.getY());
                            }
                            firePropertyChange();
                            mtl.playAt(tick_h);
                            repaint();
                        } else if (e.isControlDown()) {
                            int dx = scale * (int) (e.getX() - press_p.getX());
                            if (dx != 0) {
                                TimeMarker tm = tl.getSelectedTimeMarker();
                                if (tmp != null)
                                    tmp.setObject(tm);

                                if (tm.type == TimeMarker.END) {
                                    global_tick_stop = e.getX() * scale;
                                    if (global_tick_stop < tick_start)
                                        global_tick_stop = tick_start;

                                    mtl.updateEndMarkers(global_tick_stop);
                                    AnimContext.ae.setDirty(true);
                                    repaint();
                                } else if (tm.type == TimeMarker.BEGIN) {
                                    if (false) {
                                        tick_start = (e.getX() - TLOFF) * scale;
                                        if (tick_start < 0)
                                            tick_start = 0;
                                        if (tick_start > global_tick_stop)
                                            tick_start = global_tick_stop;
                                        mtl.updateBeginMarkers(tick_start);
                                        AnimContext.ae.setDirty(true);
                                        repaint();
                                    }
                                } else if (tm.type == TimeMarker.STOP) {
                                    tl.adjustSomeTimeMarkerRelative((double) dx / (tm.when - tl.getOffset()));
                                    tl.moveSelectedTimeMarker(dx, grid);
                                    AnimContext.ae.setDirty(true);
                                } else {
                                    tl.moveSelectedTimeMarker(dx, grid);
                                    AnimContext.ae.setDirty(true);
                                }

                                press_p = new Point(e.getX(), e.getY());
                            }
                            repaint();
                        } else {
                        }
                    }
                }
            }
        }

        public void mouseMoved(MouseEvent e) {
            if (e.getY() > TL_M_SCAN) {
                tick_m = ((e.getX() - TLOFF) * scale) / 10;
                tick_m *= 10;
                firePropertyChange();
                repaint();
            }
        }

        public void mouseReleased(MouseEvent e) {
            Point pd = new Point(e.getX() - TLOFF, e.getY());

            int old_selected_tl = selected_tl;

            deselectAllTimeLine();
            int tl_ix = hitTimeLine(pd);
            selected_tl = tl_ix;

            final TimeLine tl = mtl.getTimeLine(selected_tl);
            if (tl == null)
                return;
            fireEvent("selectTL", tl);

            int mod = e.getModifiersEx();

            boolean pt = e.isPopupTrigger(); // macOS ?

            if (pt) {
                if (e.isControlDown()) {
                    popup_ctrl_maction(e, tl);
                } else {
                    popup_maction(e, tl);
                }
            } else {
                hitMode = HITM_NORMAL;
                firePropertyChange();
            }
        }
    }

    public TimeLinePanel(MasterTimeLine mtl) {
        this.mtl = mtl;
        setLayout(null);
        listeners = new EventListenerList();
        setBackground(low_blue);
        Mouse mouse = new Mouse();
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        updName();
    }

    JLabel[] name = new JLabel[4];

    public void updName() {
        if (name[0] == null)
            for (int i = 0; i < 4; i++) {
                name[i] = new JLabel(T.t("name"));
                name[i].setLocation(5, TL_M_SCAN + 4 + i * TL_H - 1);
                name[i].setForeground(Color.white);
                add(name[i]);
            }
    }

    public void popupProp() {
        try {
            int nid = 0;//id.charAt(1) - '0';
            JFrame owner = (JFrame) TimeLinePanel.this.getTopLevelAncestor();
            if (tmp == null)
                tmp = new TimeMarkerProperties(mtl.a_ctxt, owner);

            TimeLine tl = mtl.getTimeLine(nid);
            TimeMarker tm = tl.getNearestTimeMarker(0);

            tmp.setObject(tm);
            tmp.setVisible(true);
        } catch (NullPointerException ex) {
        }
    }

    void setSelectedTM(TimeMarker tm) {
        if (tm != null)
            tm.setSelected(true);
        if (tmp != null)
            tmp.setObject(tm);
    }

    void normal_maction(MouseEvent e, final TimeLine tl) {
//		    TimeLine tl = mtl.getTimeLine(selected_tl);
        if (tl != null) {
            TimeMarker tm = tl.getNearestTimeMarker((e.getX() - TLOFF) * scale);
            setSelectedTM(tm);
        }
        repaint();
        hitMode = HITM_NORMAL;
    }

    void popup_maction(final MouseEvent e, final TimeLine tl) {
        final String[] choice = {T.t("Marker properties"),
                T.t("Add timesync"),
                T.t("Add trigger"),
                "",
                T.t("Cancel")};

        com.femtioprocent.omega.swing.Popup pop = new Popup(TimeLinePanel.this);
        pop.popup(T.t("Marker"), choice, e.getX(), e.getY(), new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                int ix = -1;
                try {
                    ix = Integer.parseInt(ev.getActionCommand());
                } catch (Exception ex) {
                }
                if (ix >= 0 && ix < choice.length) {
                    switch (ix) {
                        case 0: {
                            JFrame owner = (JFrame) TimeLinePanel.this.getTopLevelAncestor();
                            if (tmp == null)
                                tmp = new TimeMarkerProperties(mtl.a_ctxt, owner);
                            TimeMarker tm = tl.getNearestTimeMarker((e.getX() - TLOFF) * scale);
                            setSelectedTM(tm);
                            tmp.setVisible(true);
                        }
                        break;

                        case 1:
                        case 2: {
                            char tmty = 0;
                            int duration = 0;
                            tmty = TimeMarker.TRIGGER;
                            if (ix == 1) {
                                tmty = TimeMarker.TSYNC;
                                duration = 0;
                            }
                            tl.addMarker(tmty, (e.getX() - TLOFF) * scale - tl.getOffset(), duration);
                            AnimContext.ae.setDirty(true);
                            fireEvent("addMarker", tl);
                            repaint();
                            hitMode = HITM_NORMAL;
                        }
                        break;
                    }
                }
            }
        });
    }

    void popup_ctrl_maction(MouseEvent e, final TimeLine tl) {
//TimeLine tl = mtl.getTimeLine(selected_tl);
        if (tl != null) {
            final TimeMarker tm = tl.getNearestTimeMarker((e.getX() - TLOFF) * scale);
            if (tm != null) {
                tm.setSelected(true);
                final String[] choice = {T.t("Marker properties"),
                        T.t("Marker delete"),
                        "",
                        T.t("Cancel")};

                Popup pop = new Popup(TimeLinePanel.this);
                pop.popup("Marker", choice, e.getX(), e.getY(), new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        // REWRITE THIS
                        int ix = -1;
                        try {
                            ix = Integer.parseInt(ev.getActionCommand());
                        } catch (Exception ex) {
                        }
                        switch (ix) {

                            case 0: {
                                JFrame owner = (JFrame) TimeLinePanel.this.getTopLevelAncestor();
                                if (tmp == null)
                                    tmp = new TimeMarkerProperties(mtl.a_ctxt, owner);
                                tmp.setObject(tm);
                                tmp.setVisible(true);
                            }
                            break;

                            case 1:
                                if (tm.canRemove()) {
                                    int tix = -1;
                                    if (tm.type == TimeMarker.TSYNC) {
                                        for (int i = 0; i < 1000; i++) {
                                            TimeMarker tmi =
                                                    tm.tl.getMarkerAtIndexType(i, TimeMarker.TSYNC);
                                            if (tmi == tm) {
                                                tix = i;
                                                break;
                                            }
                                        }
                                        if (tix != -1) {
                                            tm.setDeleteCandidate(true);
                                            repaint();
                                            if (JOptionPane.showConfirmDialog(mtl.a_ctxt.ae,
                                                    T.t("Delete selected red timesync?"),
                                                    "Omega",
                                                    JOptionPane.YES_NO_OPTION) == 0) {
                                                tm.tl.removeMarker(tm);
                                                AnimContext.ae.setDirty(true);
                                                fireEvent("delMarker", tix);
                                            } else
                                                tm.setDeleteCandidate(false);
                                            repaint();
                                        }
                                    } else if (tm.type == TimeMarker.TRIGGER) {
                                        for (int i = 0; i < 1000; i++) {
                                            TimeMarker tmi =
                                                    tm.tl.getMarkerAtIndexType(i, TimeMarker.TRIGGER);
                                            if (tmi == tm) {
                                                tix = i;
                                                break;
                                            }
                                        }
                                        if (tix != -1) {
                                            tm.setDeleteCandidate(true);
                                            repaint();
                                            if (JOptionPane.showConfirmDialog(mtl.a_ctxt.ae,
                                                    T.t("Delete selected red trigger?"),
                                                    "Omega",
                                                    JOptionPane.YES_NO_OPTION) == 0) {
                                                tm.tl.removeMarker(tm);
                                                AnimContext.ae.setDirty(true);

                                            } else
                                                tm.setDeleteCandidate(false);
                                            repaint();
                                        }
                                    }
                                    repaint();
                                }
                                break;
                        }
                    }
                });
            }
        }
        repaint();
        hitMode = HITM_NORMAL;
    }

    public int getPlayEnd() {
        return global_tick_stop;
    }

    public void setTick(int t) {
        ltick_h = tick_h;
        tick_h = t;
        if (tick_h < ltick_h)
            repaint();
        else
            repaint(TLOFF + ltick_h / scale - 5, 0, TLOFF + tick_h / scale + 5, 200);
    }

    public int getHitMS() {
        return tick_h;
    }

    public int getMouseHitMS() {
        return tick_m;
    }

    void firePropertyChange() {
        Object[] lia = listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((TimeLinePanelListener) lia[i + 1]).updateValues();
        }
    }

    void fireEvent(String evs, Object o) {
        Object[] lia = listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((TimeLinePanelListener) lia[i + 1]).event(evs, o);
        }
    }

    public void addTimeLinePanelListener(TimeLinePanelListener tll) {
        listeners.add(TimeLinePanelListener.class, tll);
    }

    void deselectAllTimeLine() {
        for (int i = 0; i < mtl.getMaxTimeLineIndex(); i++) {
            TimeLine tl = mtl.getTimeLine(i);
            if (tl != null)
                tl.setDeselectTimeMarker();
        }
    }

    private int hitTimeLine(Point p) {
        int y = (int) p.getY();
        if (y < TL_M_SCAN)
            return -1;
        return (y - TL_M_SCAN) / TL_H;
    }


    public void setLock(boolean b) {
        lock = b;
    }

    private void paintMasterTimeLine(Graphics g) {
        g.setColor(gray30);
        g.fillRect(0, 0, 2000, TL_M_SCAN);
        g.setColor(gray60);
        g.fillRect(tick_start / scale, 0, (global_tick_stop - tick_start) / scale, TL_M_DUR);

        g.setColor(gray45);
        if (100 / scale >= 5)
            for (double i = 1; i < 10000; i += 100.0 / scale)
                g.drawLine((int) (i + 0.5), 0, (int) (i + 0.5), TL_M_DUR);
        for (double i = 1; i < 10000; i += 500.0 / scale)
            g.drawLine((int) (i + 0.5), 0, (int) (i + 0.5), TL_M_DUR + 3);
        for (double i = 1; i < 10000; i += 1000.0 / scale)
            g.drawLine((int) (i + 0.5), 0, (int) (i + 0.5), TL_M_SCAN);
        g.setColor(gray80);
        if (100 / scale >= 5)
            for (double i = 0; i < 10000; i += 100.0 / scale)
                g.drawLine((int) (i + 0.5), 0, (int) (i + 0.5), TL_M_DUR);
        for (double i = 0; i < 10000; i += 500.0 / scale)
            g.drawLine((int) (i + 0.5), 0, (int) (i + 0.5), TL_M_DUR + 3);
        for (double i = 0; i < 10000; i += 1000.0 / scale)
            g.drawLine((int) (i + 0.5), 0, (int) (i + 0.5), TL_M_SCAN);
    }

    Color low_blue = new Color(80, 80, 130);
    Color gray80 = new Color(220, 220, 220);
    Color gray60 = new Color(180, 180, 180);
    Color gray50 = new Color(120, 120, 120);
    Color gray45 = new Color(110, 110, 110);
    Color gray40 = new Color(100, 100, 100);
    Color gray30 = new Color(80, 80, 80);

    Color outside_tl_color = new Color(100, 100, 100);

    Color selected_tl_timeline_color = Color.orange.brighter();
    Color light_selected_tl_timeline_color = selected_tl_timeline_color.brighter();

    Color normal_tl_timeline_color = Color.orange.darker();
    Color light_normal_tl_timeline_color = normal_tl_timeline_color.brighter();

    Color vertical_tl_mousecursor_color = new Color(100, 100, 100);
    Color vertical_tl_cursor_color = new Color(180, 80, 180);
    Color light_vertical_tl_cursor_color = vertical_tl_cursor_color.brighter();

    Color normal_marker_color = new Color(80, 180, 80);
    Color normal_TSync_marker_color = new Color(220, 80, 200);
    Color yellow = new Color(180, 180, 80);
    Color cyan = new Color(50, 180, 240);
    Color light_yellow = Color.yellow.brighter(); // new Color(220, 220, 110);


    int TLOFF = 80;

    private void drawTLBut(Graphics g, int y) {
        g.setColor(gray80);
        g.fillRect(-TLOFF + 3, y - 5, 10, 10);
    }

    private void paintTimeLine(Graphics g, MasterTimeLine mtl) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform at = null;

        try {
            g.setColor(gray30);
            g.fillRect(0, 0, TLOFF, 1000);

            at = g2.getTransform();
            at.translate(TLOFF, 0);
            g2.setTransform(at);

            paintMasterTimeLine(g);

            int t_x = tick_h / scale;
            int t_mx = tick_m / scale;
            int t_x_ = ltick_h / scale;

            g.setColor(vertical_tl_cursor_color);
            g.drawLine(t_x, 0, t_x, 200);
            g.setColor(vertical_tl_mousecursor_color);
            g.drawLine(t_mx, 0, t_mx, 200);

            for (int i = 0; i < mtl.getMaxTimeLineIndex(); i++) {
                int y = TL_M_SCAN + 10 + i * TL_H;
                g.setColor(outside_tl_color);
                g.drawLine(0, y, 2000, y);
                TimeLine tl = mtl.getTimeLine(i);
                if (tl == null)
                    continue;
                int start = (int) (tl.getOffset() / scale);
                int stop = (int) ((tl.getOffset() + tl.getDuration()) / scale);
                g.setColor(outside_tl_color);
                g.drawLine(0, y, start, y);
                if (i == selected_tl)
                    g.setColor(selected_tl_timeline_color);
                else
                    g.setColor(normal_tl_timeline_color);
                g.drawLine(start, y, stop, y);
                if (t_x > start) {
                    if (i == selected_tl)
                        g.setColor(light_selected_tl_timeline_color);
                    else
                        g.setColor(light_normal_tl_timeline_color);
                    g.drawLine(start, y, t_x, y);
                }
                if (t_x_ > start) {
                    g.setColor(light_vertical_tl_cursor_color);
                    g.drawLine(t_x_, y, t_x, y);
                }
                g.setColor(outside_tl_color);
                g.drawLine(stop, y, 2000, y);

                Iterator it = tl.getMarkersAbs(-1, 999999, true).iterator();
                while (it.hasNext()) {
                    TimeMarker tm = (TimeMarker) it.next();
                    int xx = (int) (tm.when / scale);
                    int dd = (int) (tm.duration / scale);
                    g.setColor(gray30);
                    TimeMarkerDraw.draw(g, tm, xx + 1, y + 1, dd);

                    Color mcol = normal_marker_color;
                    if (tm.type == TimeMarker.TSYNC)
                        mcol = normal_TSync_marker_color;
                    if (tm.isDeleteCandidate())
                        g.setColor(Color.red);
                    else if (tm.getSelected())
                        g.setColor(mcol.brighter().brighter());
                    else
                        g.setColor(mcol);
                    TimeMarkerDraw.draw(g, tm, xx, y, dd);
                }

                it = tl.getMarkersAbs(t_x_ * scale, t_x * scale).iterator();
                g.setColor(light_yellow);
                while (it.hasNext()) {
                    TimeMarker tm = (TimeMarker) it.next();
                    int xx = (int) (tm.when / scale);
                    int dd = (int) (tm.duration / scale);
                    TimeMarkerDraw.draw(g, tm, xx, y, dd);
                }

//		drawTLBut(g, y);
                at.translate(-TLOFF, 0);
                g2.setTransform(at);
                int xx = 10;
                String name = mtl.getTimeLine(i).getLessonId();
                if (name != null)
                    g2.drawString(name, xx, y + 3);
                at.translate(TLOFF, 0);
                g2.setTransform(at);
            }
        } finally {
            at.translate(-TLOFF, 0);
            g2.setTransform(at);
        }
    }

    public Dimension getMinimumSize() {
        return new Dimension(100, 120);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintTimeLine(g, mtl);
        updName();
    }
}
