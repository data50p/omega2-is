package com.femtioprocent.omega.anim.tool.path;

import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AllPath {
    List li = new ArrayList();

    public AllPath() {
    }

    public void add(Path pa) {
        li.add(pa);
    }

    public Path get(int nid) {
        Iterator it = li.iterator();
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            if (pa.nid == nid)
                return pa;
        }
        return null;
    }

    public void removePath(int nid) {
        Path pa = find(nid);
        li.remove(pa);
    }

    public void deselectAll(Graphics2D g2) {
        Iterator it = li.iterator();
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            pa.setSelected(false);
            if (g2 != null)
                pa.draw(g2);
        }
    }

    public Probe findNearest(Point2D p) {
        Probe nearest = null;
        Iterator it = li.iterator();
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            Probe n1 = pa.findNearest(p);
            if (nearest == null)
                nearest = n1;
            else {
                if (nearest.dist > n1.dist) {
                    nearest = n1;
                }
            }
        }
        return nearest;
    }

    public Path.Mark findNearestMarker(Point2D p) {
        Iterator it = li.iterator();
        Path.Mark fpm = null;
        double dist = 9999999;
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            if (true || pa.isSelected()) {
                Path.Mark fmk = pa.findNearestMarker(p);
                if (fmk != null) {
                    double fdist = pa.distMarker(fmk, p);
                    if (fdist < dist) {
                        dist = fdist;
                        fpm = fmk;
                    }
                }
            }
        }
        return fpm;
    }

    public Path find(int nid) {
        Iterator it = li.iterator();
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            if (pa.nid == nid)
                return pa;
        }
        return null;
    }

    public Path findSelected() {
        Iterator it = li.iterator();
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            if (pa.isSelected())
                return pa;
        }
        return null;
    }

    public void redraw(Graphics2D g2) {
        Iterator it = li.iterator();
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            pa.draw(g2);
        }
    }

    class P_Canvas extends com.femtioprocent.omega.graphic.render.Canvas {
        void redrawControl(Graphics2D g2) {
            g2.setColor(Color.blue);
            for (int i = 0; i < 4; i++)
                g2.drawRect(0, i * 20, 20, 20);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            redraw(g2);

            redrawControl(g2);
        }
    }

    public Element getElement() {
        Element el = new Element("AllPath");

        Iterator it = li.iterator();
        while (it.hasNext()) {
            Path pa = (Path) it.next();
            Element pel = pa.getElement();
            el.add(pel);
        }
        return el;
    }


    public void load(Element el) {
        li = new ArrayList();
        for (int i = 0; i < 100; i++) {
            try {
                Element eel = el.findElement("TPath", i);
                if (eel == null)
                    break;
                Path pa = new Path(eel);
                li.add(pa);
            } catch (Exception ex) {
            }
        }
    }

    public static void main(String[] argv) {
        HashMap flag = SundryUtils.flagAsMap(argv);
        java.util.List argl = SundryUtils.argAsList(argv);

        JFrame f = new JFrame("Path - test");
        Container c = f.getContentPane();

        final AllPath ap = new AllPath();

        P_Canvas ca = ap.new P_Canvas();

        if (flag.get("g") != null)
            ;//Canvas.gdbg = true;

        c.add(ca);

        f.pack();

        f.setSize(870, 640);

        f.setVisible(true);
        ca.setBackground("bg.jpg");

        SundryUtils.m_sleep(300);

        Point2D sp = new Point2D.Double(100, 100);
        Point2D ep = new Point2D.Double(200, 300);
        Path pa = new Path(0, sp, ep);

        ap.add(pa);

        pa.draw((Graphics2D) ca.getGraphics());
        pa.extendSegment(new Point2D.Double(300, 100));
        pa.draw((Graphics2D) ca.getGraphics());

        Point2D sp2 = new Point2D.Double(300, 200);
        Point2D ep2 = new Point2D.Double(400, 500);
        Path pa2 = new Path(1, sp2, ep2);
        ap.add(pa2);

        pa2.draw((Graphics2D) ca.getGraphics());
        pa2.extendSegment(new Point2D.Double(350, 300));
        pa2.draw((Graphics2D) ca.getGraphics());

//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(0.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(10.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(100.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(100.2));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(pa.getLength()));

        final P_Canvas fca = ca;

        final Path fpa = pa;

        class Mouse extends MouseInputAdapter {
            Point2D press_p = null;
            Probe selected_prb = null;

            Mouse() {
                fca.addMouseListener(this);
                fca.addMouseMotionListener(this);
            }

            public void mousePressed(MouseEvent e) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "m p " + e);
                if (e.getX() < 20 && e.getY() < 20) {
                    if (selected_prb != null)
                        selected_prb.seg.path.removeSegment();
                    fca.repaint();
                    return;
                }
                if (e.getX() < 20 && e.getY() < 40) {
                    selected_prb.seg.path.splitSegment();
                    fca.repaint();
                    return;
                }
                if (e.getX() < 20 && e.getY() < 60) {
                    Path npa = selected_prb.seg.path.createSegment();
                    ap.add(npa);
                    fca.repaint();
                    return;
                }
                if (e.getX() < 20 && e.getY() < 80) {
                    selected_prb.seg.path.extendSegment(press_p);
                    fca.repaint();
                    return;
                }

                press_p = new Point2D.Double(e.getX(), e.getY());

                ap.deselectAll((Graphics2D) fca.getGraphics());
                Probe prb = ap.findNearest(press_p);
                prb.seg.path.setSelected(true);
                prb.seg.selectedPoint = prb.sel;
                prb.seg.path.draw((Graphics2D) fca.getGraphics());

                selected_prb = prb;
            }

            public void mouseMoved(MouseEvent e) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "m m " + e);
            }

            public void mouseDragged(MouseEvent e) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "m d " + e);
                Point2D drag_p = new Point2D.Double(e.getX(), e.getY());
                selected_prb.seg.moveto(selected_prb.sel, drag_p);
                selected_prb.seg.path.rebuildGP();
                fca.repaint(); // ap.redraw((Graphics2D)fca.getGraphics());
            }
        }

        Mouse m = new Mouse();
        SundryUtils.m_sleep(200);

        for (; ; ) {
            SundryUtils.m_sleep(200);
        }
    }
}
