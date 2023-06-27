package com.femtioprocent.omega.anim.tool.path;

import com.femtioprocent.omega.OmegaContext;

import java.awt.*;
import java.awt.geom.*;

public class Segment_Q {
    static final int SEL_START = 0;
    static final int SEL_END = 1;
    static final int SEL_CTRL = 2;
    static final int SEL_N = 3;

    int nid;

    public Path path;
    QuadCurve2D q;
    Point2D p1, p2, pc;

    public int selectedPoint = -1;
    boolean showHandle = true;

    public Segment_Q(int nid, Point2D sp, Point2D cp, Point2D ep) {
        p1 = sp;
        pc = cp;
        p2 = ep;
        q = new QuadCurve2D.Float();
        q.setCurve(p1, pc, p2);
        this.nid = nid;
    }

    public Segment_Q(int nid, QuadCurve2D q) {
        this.q = q;
        p1 = q.getP1();
        pc = q.getCtrlPt();
        p2 = q.getP2();
        this.nid = nid;
    }

    public void adjust(Point2D p) {
        p1 = p;
        q.setCurve(p1, pc, p2);
    }

    String toString(QuadCurve2D q) {
        return "{" + q.getP1() + ',' + q.getCtrlPt() + ',' + q.getP2() + "}";
    }

    public Segment_Q split() {
        QuadCurve2D q1 = new QuadCurve2D.Double();
        q.subdivide(q, q1);
        p1 = q.getP1();
        pc = q.getCtrlPt();
        p2 = q.getP2();
        return new Segment_Q(0, q1);
    }

    public void moveto(int who, Point2D p) {
        switch (who) {
            case SEL_START:
                p1.setLocation(p.getX(), p.getY());
                break;
            case SEL_END:
                p2.setLocation(p.getX(), p.getY());
                break;
            case SEL_CTRL:
                pc.setLocation(p.getX(), p.getY());
                break;
            default:
                OmegaContext.sout_log.getLogger().info("ERR: " + "active_nr " + who);
        }
        q.setCurve(p1, pc, p2);
    }

    public Point2D getPoint(int sel) {
        switch (sel) {
            case SEL_START:
                return p1;
            case SEL_END:
                return p2;
            case SEL_CTRL:
                return pc;
            default:
                return null;
        }
    }

    public void setPoint(int sel, Point2D p) {
        switch (sel) {
            case SEL_START:
                p1 = p;
                break;
            case SEL_END:
                p2 = p;
                break;
            case SEL_CTRL:
                pc = p;
                break;
            default:
                return;
        }
        q.setCurve(p1, pc, p2);
    }

    public void moveAllBy(Point2D p) {
        p1.setLocation(p1.getX() + p.getX(),
                p1.getY() + p.getY());
        p2.setLocation(p2.getX() + p.getX(),
                p2.getY() + p.getY());
        pc.setLocation(pc.getX() + p.getX(),
                pc.getY() + p.getY());
        q.setCurve(p1, pc, p2);
    }

    public void moveAllBy(double dx, double dy) {
        p1.setLocation(p1.getX() + dx, p1.getY() + dy);
        p2.setLocation(p2.getX() + dx, p2.getY() + dy);
        pc.setLocation(pc.getX() + dx, pc.getY() + dy);
        q.setCurve(p1, pc, p2);
    }

    public void addMe(GeneralPath gp) {
        gp.append(q, true);
    }

    Probe findNearest(Point2D p) {
        Probe prb = new Probe();
        prb.dist = 2000000000;

        for (int i = 0; i < SEL_N; i++) {
            Point2D pp = getPoint(i);
            double d = pp.distance(p);
            if (d < prb.dist) {
                prb.dist = d;
                prb.sel = i;
                prb.p = pp;
                prb.seg = this;
            }
        }
        return prb;
    }

    void drawSmallBox(Graphics2D g2, Point2D p, int w) {
        g2.draw(new Rectangle2D.Double(p.getX() - w / 2.0,
                p.getY() - w / 2.0,
                w, w));
    }

    void drawSmallCross(Graphics2D g2, Point2D p, int w) {
        g2.draw(new Line2D.Double(p.getX() - w / 2.0,
                p.getY() - w / 2.0,
                p.getX() + w / 2.0,
                p.getY() + w / 2.0));
        g2.draw(new Line2D.Double(p.getX() + w / 2.0,
                p.getY() - w / 2.0,
                p.getX() - w / 2.0,
                p.getY() + w / 2.0));
    }

    void fillSmallBox(Graphics2D g2, Point2D p, int w) {
        g2.fill(new Rectangle2D.Double(p.getX() - w / 2.0,
                p.getY() - w / 2.0,
                w, w));
    }

    public void drawConnector(Graphics2D g2) {
        g2.draw(new Line2D.Double(p1, pc));
        g2.draw(new Line2D.Double(pc, p2));
    }

    public void draw(Graphics2D g2) {
        g2.draw(q);
        if (showHandle) {
            for (int i = 0; i < SEL_N; i++) {
                Point2D p = getPoint(i);
                g2.setColor(i == selectedPoint ?
                        Color.red :
                        Color.green);
                if (i == SEL_CTRL)
                    drawSmallCross(g2, p, 5);
                else
                    drawSmallBox(g2, p, 5);
            }
            if (this == path.getSegment(0)) {
                g2.setColor(SEL_START == selectedPoint ?
                        Color.red :
                        Color.green);
                fillSmallBox(g2, getPoint(SEL_START), 5);
            }
        }
    }

    public String toString() {
        return "Segment_Q{" +
                nid +
                "}";
    }
}

