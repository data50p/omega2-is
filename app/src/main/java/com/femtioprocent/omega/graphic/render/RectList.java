package com.femtioprocent.omega.graphic.render;

import com.femtioprocent.omega.util.SundryUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RectList {
    List rl;
    Rectangle2D nr;

    RectList() {
        rl = new ArrayList();
        nr = new Rectangle2D.Double();
    }

    final static double mi(double a, double b) {
        return a < b ? a : b;
    }

    final static double ma(double a, double b) {
        return a > b ? a : b;
    }

    void add(Rectangle2D r) {
        List nrl = new ArrayList();

        Iterator it = rl.iterator();
        while (it.hasNext()) {
            Rectangle2D r1 = (Rectangle2D) it.next();
            intersect(r, r1, nr);

            if (r1.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight())) {
                return;
            }
            if (!nr.isEmpty()) {
                Rectangle rr = new Rectangle();
                Rectangle2D.union(r, r1, rr);
                r = rr;
            } else {
                nrl.add(r1);
            }
        }
        nrl.add((Rectangle2D) r.clone());

        rl = nrl;
    }

    public static void main(String[] argv) {
        HashMap flag = SundryUtils.flagAsMap(argv);
        java.util.List argl = SundryUtils.argAsList(argv);

        RectList rl = new RectList();
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
        rl.add(new Rectangle2D.Double(150, 150, 100, 100));

        rl = new RectList();
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
        rl.add(new Rectangle2D.Double(50, 50, 100, 100));

        rl = new RectList();
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
        rl.add(new Rectangle2D.Double(150, 150, 10, 10));

        rl = new RectList();
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
        rl.add(new Rectangle2D.Double(150, 50, 100, 100));

        rl = new RectList();
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
        rl.add(new Rectangle2D.Double(50, 150, 100, 100));

        rl = new RectList();
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
        rl.add(new Rectangle2D.Double(300, 300, 100, 100));

        rl = new RectList();
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
        rl.add(new Rectangle2D.Double(150, 300, 100, 100));

        rl = new RectList();
        rl.add(new Rectangle2D.Double(150, 300, 100, 100));
        rl.add(new Rectangle2D.Double(100, 100, 100, 100));
    }

    public static void intersect(Rectangle2D src1,
                                 Rectangle2D src2,
                                 Rectangle2D dest) {
        double x1 = Math.max(src1.getMinX(), src2.getMinX());
        double y1 = Math.max(src1.getMinY(), src2.getMinY());
        double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
        dest.setFrame(x1, y1, x2 - x1, y2 - y1);
    }

    Iterator iterator() {
        return rl.iterator();
    }
}

