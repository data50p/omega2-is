package com.femtioprocent.omega.anim.panels.cabaret;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.anim.appl.AnimEditor;
import com.femtioprocent.omega.anim.cabaret.Actor;
import com.femtioprocent.omega.anim.cabaret.Cabaret;
import com.femtioprocent.omega.anim.cabaret.GImAE;
import com.femtioprocent.omega.anim.tool.timeline.TimeLine;

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

public class CabaretPanel extends JPanel {
    final int IMSIZE = 64;
    final int ACT_N = OmegaConfig.CABARET_ACTOR_N;
    final int EMPTY = -1;

    AnimEditor ae;
    CabaretProperties prop = null;

    private Mouse m;
    private List cLiLi = new ArrayList();

    private int selected = 0;
    private boolean fld_state_id = false;

    int selected_src_fld;
    int selected_dst_fld;

    Cabaret getCab() {
        return ae.a_ctxt.anim_canvas.cab;
    }

    public CabaretPanel(AnimEditor ae) {
        this.ae = ae;
        setLayout(new BorderLayout());

//	actA = new ActA();

        setMinimumSize(new Dimension(IMSIZE * ACT_N, IMSIZE + 20));
        setMaximumSize(new Dimension(IMSIZE * ACT_N, IMSIZE + 20));
        setPreferredSize(new Dimension(IMSIZE * ACT_N, IMSIZE + 20));
        m = new Mouse();
    }

    void replaceActor(int ixx) {
        if (ae != null)
            ae.replaceActor(ixx);
    }

    void deleteActor(int ixx) {
        if (ae != null)
            ae.deleteActor(ixx);
    }

    Actor getActor(int ix) {
        return getCab().getActor(ix);
    }

    GImAE getGImAE(int ix) {
        return getCab().actA.getGImAE(ix);
    }

    void setPropTarget(int ixx) {
        if (prop != null)
            prop.setTarget(getActor(ixx), ixx);
        if (prop != null) {
            int tl_nid = getCab().getTLnid(ixx);
            TimeLine tl = ae.a_ctxt.mtl.getTimeLine(tl_nid);
        }
        selected = ixx;
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

            if (ixx >= ACT_N)
                return;

            if (pt) {
                popup(ixx);
                return;
            }

            setPropTarget(ixx);

            mpress_p = new Point2D.Double(e.getX(), e.getY());
            if (e.getY() < IMSIZE) {
            } else {
                selected_dst_fld = ixx;
                selected_src_fld = ixx;
            }
            repaint();
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            int ixx = e.getX() / IMSIZE;
            if (ixx >= ACT_N)
                return;
            fldState(e);
            selected_dst_fld = ixx;
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            boolean pt = e.isPopupTrigger();

            fldState(e);

            if (pt) {
                int ixx = e.getX() / IMSIZE;
                if (ixx >= ACT_N)
                    return;
                popup(ixx);
                return;
            }

            int ixx = e.getX() / IMSIZE;
            if (ixx >= ACT_N)
                return;
            if (fld_state_id &&
                    selected_src_fld != EMPTY &&
                    selected_dst_fld != EMPTY &&
                    selected_src_fld != selected_dst_fld) {
                ae.a_ctxt.anim_canvas.hideActors();
                int tl_nid1 = getCab().actA.arr[selected_src_fld].tl_nid;
                int tl_nid2 = getCab().actA.arr[selected_dst_fld].tl_nid;
                getCab().actA.arr[selected_src_fld].tl_nid = tl_nid2;
                getCab().actA.arr[selected_dst_fld].tl_nid = tl_nid1;
                selected_dst_fld = selected_src_fld = EMPTY;
                fireStateChange();
            } else {
                selected_dst_fld = selected_src_fld = EMPTY;
            }
            repaint();
        }
    }

    public void resetCabaretOrder() {
        selected_dst_fld = selected_src_fld = EMPTY;
        getCab().newActA();
        fireStateChange();
    }

    public void popup(int ix) {
        if (prop == null) {
            JFrame owner = (JFrame) getTopLevelAncestor();
            prop = new CabaretProperties(owner, this);
//	    prop_pa = new CabaretPathProperties(owner, this);
        }
        prop.setVisible(true);
//	prop_pa.setVisible(true);
        setPropTarget(ix);
    }

    public Actor getActorInPanel(int tl_nid) {
        int ix = getCab().actA.findOrdTL(tl_nid);
        if (ix >= 0)
            return getActor(ix);
        return null;
    }

    public Actor getActorInPanelAbs(int ix) {
        return getActor(ix);
    }

    // NOFATAL FIX alloc tl_nid

    public int setActorInPanelAbs(Actor ac, int ix) {
        if (ix >= 0 && ix < getCab().actA.arr.length) {
            int tl_nid = getCab().getTLnid(ix);//actA.arr[ix].tl_nid;
            if (tl_nid == EMPTY) {
                int fix = getCab().actA.findOrdTL(ix);
                if (fix == -1) {
                    fix = getCab().actA.findFree();
                    if (fix < OmegaConfig.TIMELINES_N) {
                        ix = fix;
                        getCab().actA.arr[ix].tl_nid = ix;
                    }
                } else
                    ix = fix;
            }
            getCab().actA.arr[ix].ac = ac;
            repaint();
            setPropTarget(ix);

            return tl_nid;
        } else
            return -1;
    }

    void draw(Graphics2D g2) {
        g2.setColor(new Color(110, 110, 110));
        g2.fillRect(0, 0, 2000, 1000);
        g2.setColor(new Color(35, 35, 110));
        g2.fillRect(0, 0, IMSIZE * ACT_N, IMSIZE);

        for (int i = 0; i < getCab().actA.arr.length; i++) {
            if (getCab().actA.arr[i] != null) {
                AffineTransform at = new AffineTransform();

                int tl_nid = getCab().getTLnid(i); // actA.arr[i].tl_nid;
                try {
                    GImAE gimae = getGImAE(i);
                    if (gimae != null) {
                        Image im = gimae.getBaseImage();

                        int im_w = im.getWidth(null);
                        int im_h = im.getHeight(null);

                        double asp_x = im_w / (double) IMSIZE;
                        double asp_y = im_h / (double) IMSIZE;
                        double asp = asp_x < asp_y ? asp_y : asp_x;

                        at.translate(i * IMSIZE, 0);
                        double scale = 1.0 / asp;
                        at.scale(scale, scale);

                        g2.drawImage(im, at, null);

                        if (tl_nid < OmegaConfig.TIMELINES_N) {

                            int ww = getGImAE(i).imw;
                            int hh = getGImAE(i).imh;
                            double fx, fy;
                            if (ww > hh) {
                                fx = 1.0;
                                fy = (double) hh / ww;
                            } else {
                                fy = 1.0;
                                fx = (double) ww / hh;
                            }

                            if (selected == i) {
                                int xx = i * IMSIZE;

                                g2.setColor(Color.yellow);
                                g2.drawRect(xx, 0, IMSIZE - 1, IMSIZE - 1);
                            }
                        }
                    }
                } catch (NullPointerException ex) {
                }

                // fld id

                Color idf = new Color(135, 135, 210);
                g2.setColor(idf);
                if (i == selected_src_fld)
                    g2.setColor(idf.darker());
                if (i == selected_dst_fld && fld_state_id)
                    g2.setColor(idf.brighter());

                g2.fillRect(i * IMSIZE, IMSIZE, IMSIZE, 20);

                g2.setColor(Color.white);


                if (tl_nid >= 0 && tl_nid < OmegaConfig.TIMELINES_N) {
                    GImAE gimae = getGImAE(i);

                    // ae.ae_ctxt.mtl;
                    TimeLine tl = ae.a_ctxt.mtl.getTimeLine(tl_nid);
                    String ID = "";
                    if (tl != null)
                        ID = tl.getLessonId();

                    g2.drawString("" + (tl_nid + 1) + ": " + ID,
                            5 + i * IMSIZE,
                            IMSIZE + 16);
                }

                g2.setColor(new Color(35, 35, 110));
                g2.drawLine(i * IMSIZE, IMSIZE, i * IMSIZE, IMSIZE + 20);
            }
        }
        if (EMPTY != selected_src_fld &&
                EMPTY != selected_dst_fld)
            drawArrFld(g2, selected_src_fld, selected_dst_fld);
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
        return new Dimension(IMSIZE * ACT_N, IMSIZE + 20);
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
