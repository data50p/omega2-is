package com.femtioprocent.omega.anim.panels.cabaret;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.appl.AnimEditor;
import com.femtioprocent.omega.anim.canvas.AnimCanvas;
import com.femtioprocent.omega.graphic.render.Wing;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WingsPanel extends JPanel {
    final int IMSIZE = 64;
    final int WING_N = OmegaConfig.WINGS_N;
    final int EMPTY = -1;

    public AnimEditor ae;
    public WingA wingA;
    WingsProperties prop = null;

    private Mouse m;
    private List cLiLi = new ArrayList();

    private int selected = -1;
    private boolean fld_state_id = false;

    int selected_wing_ix = -1;

    public class WingA {
        public class WingItem {
            Wing wing;
            int nid;
            int ord;

            WingItem(int ord) {
                this.ord = ord;
                nid = EMPTY;
            }
        }

        ;

        public WingItem[] arr;

        int selected_src_fld;
        int selected_dst_fld;

        WingA() {
            arr = new WingItem[WING_N];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new WingItem(i);
            }
            selected_src_fld = selected_dst_fld = EMPTY;
        }

        Image getIm(int ix) {
            try {
                return arr[ix].wing.im;
            } catch (NullPointerException ex) {
                return null;
            }
        }

        int findFree() {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].nid == EMPTY)
                    return i;
            }
            return -1;
        }

        int findOrd(int nid) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].nid == nid)
                    return i;
            }
            return -1;
        }
    }

    ;

    public WingsPanel(AnimEditor ae) {
        this.ae = ae;
        setLayout(new BorderLayout());

        wingA = new WingA();

        setMinimumSize(new Dimension(IMSIZE * WING_N, IMSIZE + 20));
        setMaximumSize(new Dimension(IMSIZE * WING_N, IMSIZE + 20));
        m = new Mouse();
    }

    void setPropTarget(int ixx) {
        if (prop != null)
            prop.setTarget(wingA.arr[ixx].wing, ixx);
        selected = ixx;
        wingA.selected_dst_fld = ixx;
        wingA.selected_src_fld = ixx;
        repaint();
    }

    public void addChangeListener(ChangeListener li) {
        cLiLi.add(li);
    }

    void fireStateChange() {
        Iterator it = cLiLi.iterator();
        while (it.hasNext()) {
            ChangeListener cli = (ChangeListener) it.next();
            cli.stateChanged(new ChangeEvent(this));
        }
    }

    class Mouse extends MouseInputAdapter {
        Point2D mpress_p;

        Mouse() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        private void fldState(MouseEvent e) {
            if (e.getY() > IMSIZE &&
                    e.getY() < IMSIZE + 20)
                fld_state_id = true;
            else
                fld_state_id = false;
        }

        public void mousePressed(MouseEvent e) {
            fldState(e);

            boolean pt = e.isPopupTrigger();

            int ixx = e.getX() / IMSIZE;

            if (ixx >= WING_N)
                return;

            AnimCanvas ca = ae.a_ctxt.anim_canvas;
            ca.traceWing(ixx, 0, 0, false);

            selected_wing_ix = ixx;

            if (pt) {
                popup(ixx);
                return;
            }

            setPropTarget(ixx);

            mpress_p = new Point2D.Double(e.getX(), e.getY());
            if (e.getY() < IMSIZE) {
            } else {
                wingA.selected_dst_fld = ixx;
                wingA.selected_src_fld = ixx;
            }
            repaint();
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            int ixx = e.getX() / IMSIZE;
            if (ixx >= WING_N)
                return;
            fldState(e);
            wingA.selected_dst_fld = ixx;
            if (prop != null)
                prop.cancelPos();
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            boolean pt = e.isPopupTrigger();

            fldState(e);

            if (pt) {
                int ixx = e.getX() / IMSIZE;
                if (ixx >= WING_N)
                    return;
                popup(ixx);
                return;
            }

            int ixx = e.getX() / IMSIZE;
            if (ixx >= WING_N)
                return;
            if (fld_state_id &&
                    wingA.selected_src_fld != EMPTY &&
                    wingA.selected_dst_fld != EMPTY &&
                    wingA.selected_src_fld != wingA.selected_dst_fld) {
                int nid1 = wingA.arr[wingA.selected_src_fld].nid;
                int nid2 = wingA.arr[wingA.selected_dst_fld].nid;
                wingA.arr[wingA.selected_src_fld].nid = nid2;
                wingA.arr[wingA.selected_dst_fld].nid = nid1;
                wingA.selected_dst_fld = wingA.selected_src_fld = EMPTY;
                fireStateChange();
            } else {
                wingA.selected_dst_fld = wingA.selected_src_fld = EMPTY;
            }
            repaint();
        }
    }

    public void popup(int ix) {
        if (prop == null) {
            JFrame owner = (JFrame) getTopLevelAncestor();
            prop = new WingsProperties(owner, this);
        }
        prop.setVisible(true);
//	prop_pa.setVisible(true);
        setPropTarget(ix);
    }

    public Wing getWing(int nid) {
        int ix = wingA.findOrd(nid);
        if (ix >= 0)
            return wingA.arr[ix].wing;
        return null;
    }

    public void removeWing(int ix) {
        removeAllWings();
        List li = ae.a_ctxt.anim_canvas.removeWing(ix);

        if (li != null) {
            int cnt = 0;
            for (Iterator it = li.iterator(); it.hasNext(); ) {
                Wing w = (Wing) it.next();
                setWing(w, cnt++);
            }
        }
        repaint();
    }

    public void removeAllWings() {
        wingA = new WingA();
        repaint();
    }

    public void replaceWing(int bound_wing_ixx) {
        ae.loadWing();
    }

    public int setWing(Wing wing, int nid) {
        int ix = wingA.findOrd(nid);
        if (ix >= 0) {
            wingA.arr[ix].wing = wing;
            repaint();
        } else {
            ix = wingA.findFree();
            if (ix >= 0) {
                wingA.arr[ix].wing = wing;
                wingA.arr[ix].nid = nid;
                repaint();
            } else {
                OmegaContext.sout_log.getLogger().info("ERR: " + "NO SLOT");
            }
        }
        setPropTarget(ix);
        return nid;
    }

    class MouseCa extends MouseInputAdapter {
        Point2D mpress_p;
        int ixx;
        Point2D p_p;

        MouseCa(int ixx) {
            this.ixx = ixx;
            AnimCanvas ca = ae.a_ctxt.anim_canvas;
            ca.traceWing(ixx, 0, 0, true);
        }

        public void mousePressed(MouseEvent e) {
            AnimCanvas ca = ae.a_ctxt.anim_canvas;
            Point p = ca.scaleEventPos(e);
            p_p = p; // new Point2D.Double(e.getX(), e.getY());
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            AnimCanvas ca = ae.a_ctxt.anim_canvas;
            Point p = ca.scaleEventPos(e);
            double dx, dy;
            ca.traceWing(ixx,
                    dx = p.getX() - p_p.getX(),
                    dy = p.getY() - p_p.getY(), true);
            if (prop != null)
                prop.updPos(wingA.arr[ixx].wing, (int) dx, (int) dy);
        }

        public void mouseReleased(MouseEvent e) {
            off();
            AnimCanvas ca = ae.a_ctxt.anim_canvas;
//	    ca.traceNoWing();
            Point p = ca.scaleEventPos(e);
            Point p_w = wingA.arr[ixx].wing.pos;
            Point p_n = new Point((int) (p_w.getX() + p.getX() - p_p.getX()),
                    (int) (p_w.getY() + p.getY() - p_p.getY()));
            wingA.arr[ixx].wing.pos = p_n;
            ae.a_ctxt.anim_canvas.resetBackground();
            ca.traceWing(ixx, 0, 0, false);
        }
    }

    MouseCa mca = null;

    void on(int ixx) {
        AnimCanvas ca = ae.a_ctxt.anim_canvas;
        mca = new MouseCa(ixx);
        ca.hook = mca;
    }

    void off() {
        AnimCanvas ca = ae.a_ctxt.anim_canvas;
        ca.hook = null;
        mca = null;
    }

    void draw(Graphics2D g2) {
        g2.setColor(new Color(110, 110, 110));
        g2.fillRect(0, 0, 2000, 1000);

        g2.setColor(new Color(35, 35, 110));
        g2.fillRect(0, 0, IMSIZE * WING_N, IMSIZE);

        for (int i = 0; i < wingA.arr.length; i++) {
            if (wingA.arr[i] != null) {
                AffineTransform at = new AffineTransform();

                int nid = wingA.arr[i].nid;
                try {
                    Image im = wingA.getIm(i);
                    if (im != null) {
                        int im_w = im.getWidth(null);
                        int im_h = im.getHeight(null);
                        double asp_x = im_w / (double) IMSIZE;
                        double asp_y = im_h / (double) IMSIZE;
                        double asp = asp_x < asp_y ? asp_y : asp_x;

                        at.translate(i * IMSIZE, 0);
                        double scale = 1.0 / asp;
                        at.scale(scale, scale);

                        g2.drawImage(im, at, null);

                        if (nid < OmegaConfig.TIMELINES_N) {

                            int ww = wingA.getIm(i).getWidth(null);
                            int hh = wingA.getIm(i).getHeight(null);
                            double fx, fy;
                            if (ww > hh) {
                                fx = 1.0;
                                fy = (double) hh / ww;
                            } else {
                                fy = 1.0;
                                fx = (double) ww / hh;
                            }
                        }
                        if (i == selected_wing_ix) {
                            int xx = i * IMSIZE;
                            g2.setColor(Color.yellow);
                            g2.drawRect(xx, 0, IMSIZE - 1, IMSIZE - 1);
                        }
                    }
                } catch (NullPointerException ex) {
                }

                // fld id

                Color idf = new Color(135, 135, 210);
                g2.setColor(idf);
                if (i == wingA.selected_src_fld)
                    g2.setColor(idf.darker());
                if (i == wingA.selected_dst_fld && fld_state_id)
                    g2.setColor(idf.brighter());

                g2.fillRect(i * IMSIZE, IMSIZE, IMSIZE, 20);

                g2.setColor(Color.white);


                if (nid >= 0 && nid < OmegaConfig.TIMELINES_N) {
                    Image im = wingA.getIm(i);

                    String ID = "";
                    g2.drawString("" + (nid + 1) + ": " + ID,
                            5 + i * IMSIZE,
                            IMSIZE + 16);
                }

                g2.setColor(new Color(35, 35, 110));
                g2.drawLine(i * IMSIZE, IMSIZE, i * IMSIZE, IMSIZE + 20);
            }
        }
        if (EMPTY != wingA.selected_src_fld &&
                EMPTY != wingA.selected_dst_fld)
            drawArrFld(g2, wingA.selected_src_fld, wingA.selected_dst_fld);
    }

    private void drawArrFld(Graphics2D g2, int src, int dst) {
        g2.setColor(Color.black);
        int h = IMSIZE + 10;
        if (src == dst) {
            g2.drawLine(src * IMSIZE, h,
                    (dst + 1) * IMSIZE, h);
            g2.drawLine(dst * IMSIZE, h,
                    dst * IMSIZE + 5, h - 5);
            g2.drawLine(dst * IMSIZE, h,
                    dst * IMSIZE + 5, h + 5);
            g2.drawLine((dst + 1) * IMSIZE, h,
                    (dst + 1) * IMSIZE - 5, h - 5);
            g2.drawLine((dst + 1) * IMSIZE, h,
                    (dst + 1) * IMSIZE - 5, h + 5);
        } else if (src < dst) {
            g2.drawLine(src * IMSIZE, h,
                    dst * IMSIZE, h);
            g2.drawLine(dst * IMSIZE, h,
                    dst * IMSIZE - 5, h - 5);
            g2.drawLine(dst * IMSIZE, h,
                    dst * IMSIZE - 5, h + 5);
        } else {
            g2.drawLine(src * IMSIZE, h,
                    dst * IMSIZE + 15, h);
            g2.drawLine(dst * IMSIZE + 15, h,
                    dst * IMSIZE + 15 + 5, h - 5);
            g2.drawLine(dst * IMSIZE + 15, h,
                    dst * IMSIZE + 15 + 5, h + 5);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(IMSIZE * WING_N, IMSIZE + 20);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        draw(g2);
    }

    public void setSelected(boolean b) {
        if (prop != null)
            prop.enableDelete(b);
    }
}
