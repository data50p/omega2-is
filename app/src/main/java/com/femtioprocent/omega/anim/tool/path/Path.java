package com.femtioprocent.omega.anim.tool.path;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.graphic.render.Canvas;
import com.femtioprocent.omega.util.DelimitedStringBuilder;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class Path {
    boolean selected;
    public int nid;

    public static Path global_selected = null;

    public class Mark {
        public Path pa;
        public int id;
        public int ord;
        public double where;
        public char type;

        public Mark(int id, char type, double where) {
            this.id = id;
            this.type = type;
            this.where = where;
            pa = Path.this;
        }

        public void moveToPathPosition(double a) {
            where = a;
            pa.reNumerateMarker();
        }

        public Point2D getPos() {
            return pa.getPointAt(where);
        }

        public String toString() {
            return "Path.Mark{" +
                    "ord=" + ord +
                    ", id=" + id +
                    ", type=" + type +
                    ", where=" + where +
                    ", pa=" + pa +
                    "}";
        }

        public Element getElement() {
            Element em = new Element("mark");
            em.addAttr("id", "" + id);
            em.addAttr("type", "" + type);
            em.addAttr("where", "" + where);
            return em;
        }
    }

    List marker;  // Mark

    GeneralPath gp;
    List seg_l;

    double[] lenA;
    Point2D[] pointA;
    double len = 0;

    public Path(Element el) {       // TPath
        seg_l = new ArrayList();
        marker = new ArrayList();
        String nidp_s = el.findAttr("nid");
        int nidp_i = Integer.parseInt(nidp_s);
        nid = nidp_i;
        for (int i = 0; i < 1000; i++) {
            Element eq = el.findElement("q", i);
            if (eq == null)
                break;
// <q nid="0" p1="146.0,169.0" p2="206.0,139.0" pc="176.0,154.0"/>
            String ord_s = eq.findAttr("ord");
            String p1_s = eq.findAttr("p1");
            String p2_s = eq.findAttr("p2");
            String pc_s = eq.findAttr("pc");
            int ord_i = Integer.parseInt(ord_s);
            Point2D p1 = decode2D(p1_s);
            Point2D p2 = decode2D(p2_s);
            Point2D pc = decode2D(pc_s);

            Segment_Q sq = new Segment_Q(seg_l.size(), p1, pc, p2);
            // sq.ord = ord_i;
            sq.path = this;
            seg_l.add(sq);
        }
        for (int i = 0; i < 1000; i++) {
            Element em = el.findElement("mark", i);
            if (em == null)
                break;
            String id = em.findAttr("id");
            String type = em.findAttr("type");
            String where = em.findAttr("where");
            int iid = Integer.parseInt(id);
            double wd = tD(where);
            addMarker(iid, type.charAt(0), wd);
        }
        rebuildGP();
    }

    public Path(int nid, Point2D sp, Point2D ep) {
        this.nid = nid;
        seg_l = new ArrayList();
        marker = new ArrayList();
        double dx = ep.getX() - sp.getX();
        double dy = ep.getY() - sp.getY();
        Segment_Q sq = new Segment_Q(seg_l.size(),
                sp,
                new Point2D.Double(sp.getX() + dx / 2,
                        sp.getY() + dy / 2),
                ep);
        sq.path = this;
        seg_l.add(sq);
        rebuildGP();
    }

    Point2D move(Point2D p, int offx, int offy) {
        return new Point2D.Double(p.getX() + offx,
                p.getY() + offy);
    }

    public Path(int nid, Path pa_src, int offx, int offy) {
        this.nid = nid;
        seg_l = new ArrayList();
        marker = new ArrayList();

        Iterator it = pa_src.seg_l.iterator();
        while (it.hasNext()) {
            Segment_Q sq = (Segment_Q) it.next();
            Segment_Q sq_c = new Segment_Q(nid,
                    move(sq.p1, offx, offy),
                    move(sq.pc, offx, offy),
                    move(sq.p2, offx, offy));

            sq_c.path = this;
            seg_l.add(sq_c);
        }
        it = pa_src.marker.iterator();
        while (it.hasNext()) {
            Mark mk = (Mark) it.next();
            addMarker(mk.id, mk.type, mk.where);
        }
        rebuildGP();
    }

    Point2D decode2D(String s) {
        String[] sa = SundryUtils.split(s, ",");
        float a = Float.parseFloat(sa[0]);
        float b = Float.parseFloat(sa[1]);
        return new Point2D.Float(a, b);
    }

    public void moveAll(double dx, double dy) {
        Iterator it = seg_l.iterator();
        while (it.hasNext()) {
            Segment_Q sq = (Segment_Q) it.next();
            sq.moveAllBy(dx, dy);
        }
        rebuildGP();
    }

    public void extendSegment(Point2D np) {
        Point2D lp = getLastPoint();
        double dx = np.getX() - lp.getX();
        double dy = np.getY() - lp.getY();
        Segment_Q sq = new Segment_Q(seg_l.size(),
                lp,
                new Point2D.Double(lp.getX() + dx / 2, lp.getY() + dy / 2),
                np);
        sq.path = this;
        seg_l.add(sq);
        rebuildGP();
    }

    public Path createSegment() {
        Point2D sp = new Point2D.Double(100, 100);
        Point2D ep = new Point2D.Double(200, 300);
        Path npa = new Path(0, sp, ep);
        return npa;
    }

    public void splitSegment() {
        List nseg_l = new ArrayList();

        boolean b = false;

        Iterator it = seg_l.iterator();
        while (it.hasNext()) {
            Segment_Q sq = (Segment_Q) it.next();
            if (sq.selectedPoint >= 0) {
                Segment_Q nsq = sq.split();
                nsq.path = this;
                nseg_l.add(sq);
                nseg_l.add(nsq);
                b = true;
            } else {
                nseg_l.add(sq);
            }
        }
        if (b) {
            seg_l = nseg_l;
            rebuildGP();
        }
    }

    public void removeSegment() {
        List nseg_l = new ArrayList();

        boolean b = false;

        Iterator it = seg_l.iterator();
        while (it.hasNext()) {
            Segment_Q sq = (Segment_Q) it.next();
            if (sq.selectedPoint >= 0) {
                b = true;
            } else {
                nseg_l.add(sq);
            }
        }
        if (b && nseg_l.size() > 0) {
            seg_l = nseg_l;
            rebuildGP();
        }
    }

    Segment_Q getSegment(int ix) {
        return (Segment_Q) seg_l.get(ix);
    }

    public static double tD(String s) {
        try {
            Double dval = Double.valueOf(s);
            double d = dval.doubleValue();
            return d;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    int howManyTSync() {
        return marker.size();
    }

    public void addMarker(int id, char type, double where) {
        Mark mk;
        marker.add(mk = new Mark(id, type, where));
        mk.ord = marker.size() - 1;
        reNumerateMarker();
            /*
	Mark[] ma = (Mark[])(marker.toArray(new Mark[0]));
	Arrays.sort(ma, new Comparator() {
	    public int compare(Object o1, Object o2) {
		Mark tm1 = (Mark)o1;
		Mark tm2 = (Mark)o2;
		return (int)(tm1.where - tm2.where);
	    }
	});	
	marker = new ArrayList();
	for(int i = 0; i < ma.length; i++)
	    ma[i].ord = i;
	Collection col = Arrays.asList(ma);
	marker.addAll(col);
	    */
    }

    public void delMarker(int ix) {
        marker.remove(ix);
        reNumerateMarker();
    }

    void reNumerateMarker() {
        Mark[] ma = (Mark[]) (marker.toArray(new Mark[0]));
        Arrays.sort(ma, new Comparator() {
            public int compare(Object o1, Object o2) {
                Mark tm1 = (Mark) o1;
                Mark tm2 = (Mark) o2;
                return (int) (tm1.where - tm2.where);
            }
        });
        marker = new ArrayList();
        for (int i = 0; i < ma.length; i++)
            ma[i].ord = i;
        Collection col = Arrays.asList(ma);
        marker.addAll(col);
    }

    public void updateInternal() {
        rebuildGP();
    }

    void rebuildGP() {
        GeneralPath ngp = new GeneralPath();
        for (int i = 0; i < seg_l.size(); i++) {
            if (i != 0)
                getSq(i).adjust(ngp.getCurrentPoint());
            getSq(i).addMe(ngp);
        }
        gp = ngp;
        rebuildLength();
    }

    Shape getShape() {
        return gp;
    }

    public Segment_Q getSq(int ix) {
        return (Segment_Q) seg_l.get(ix);
    }

    public int getSqN() {
        return seg_l.size();
    }

    Point2D getLastPoint() {
        return gp.getCurrentPoint();
    }

    public Point2D getFirstPoint() {
        return getPointAt(0.0);
    }

    Object[] getPathCoordinates(Shape shape) {
        double[] fa = new double[6];
        int cnt = 0;
        PathIterator pi = shape.getPathIterator(null, OmegaConfig.FLATNESS);
        LOOP:
        while (!pi.isDone()) {
            int a = pi.currentSegment(fa);
            switch (a) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    cnt++;
                    break;
                default:
                    OmegaContext.sout_log.getLogger().info("ERR: " + "pi ?");
            }
            pi.next();
        }
        pi = shape.getPathIterator(null, OmegaConfig.FLATNESS);
        Point2D[] pa = new Point2D[cnt];
        double[] lena = new double[cnt];
        double le = 0;
        double x = 0, y = 0, xx = 0, yy = 0;
        int cnt2 = 0;
        LOOP2:
        while (!pi.isDone()) {
            int a = pi.currentSegment(fa);
            switch (a) {
                case PathIterator.SEG_MOVETO:
                    pa[cnt2] = new Point2D.Double(fa[0], fa[1]);
                    lena[cnt2] = le;
                    x = fa[0];
                    y = fa[1];
                    break;
                case PathIterator.SEG_LINETO:
                    xx = fa[0];
                    yy = fa[1];
                    le += Point2D.distance(xx, yy, x, y);  // (int)Math.sqrt(xxx*xxx+yyy*yyy);
                    pa[cnt2] = new Point2D.Double(fa[0], fa[1]);
                    lena[cnt2] = le;
                    x = xx;
                    y = yy;
                    break;
                default:
                    OmegaContext.sout_log.getLogger().info("ERR: " + "pi ?");
            }
            cnt2++;
            pi.next();
        }
        return new Object[]{lena, pa};
    }

    private double rebuildLength() {
        Shape shape = getShape();
        if (shape == null) {
            pointA = null;
            lenA = null;
            return 0;
        }

        Object[] oa = getPathCoordinates(shape);
        lenA = (double[]) oa[0];
        pointA = (Point2D[]) oa[1];
        len = lenA[lenA.length - 1];

        return len;
    }

    public double[] getPathLength_TSyncSegments() {
        double[] da = new double[howManyTSync() + 2];
        if (da.length == 2) {
            da[0] = 0;
            da[1] = getLength();
        } else {
            da[0] = 0;
            for (int i = 0; i < da.length; i++)
                if (i == 0)
                    da[i] = 0;
                else if (i == 1)
                    da[i] = ((Mark) marker.get(i - 1)).where;
                else if (i == da.length - 1)
                    da[i] = getLength();
                else
                    da[i] = ((Mark) marker.get(i - 1)).where;
        }
        return da;
    }

    public Point2D getPointAt(double l) {
        return getPointAt(l, true);
    }

    public Point2D getPointAt(double l, boolean hide_after) {
        int ix = Arrays.binarySearch(lenA, l);
        if (ix < 0)
            ix = -ix - 1;
        if (ix >= lenA.length)
            ix = lenA.length - 1;

        int i = ix;
// 	for(int i = 0; i < lenA.length; i++) {
// 	    if ( lenA[i] >= l ) {
//		OmegaContext.sout_log.getLogger().info("ERR: " + "bin " + ix + ' ' + i);

        double prev = 0;
        if (i != 0)
            prev = lenA[i - 1];
        // return pointA[i];

        double df = l - prev;
        if (df < 0.001) {
            return pointA[i];
        }
        double d_1 = lenA[i] - prev;
        double frac = df / d_1;
        Point2D p_1 = i == 0 ? pointA[i] : pointA[i - 1];
        Point2D p = pointA[i];
        Point2D ip = new Point2D.Double(p_1.getX() + (p.getX() - p_1.getX()) * frac,
                p_1.getY() + (p.getY() - p_1.getY()) * frac);
        return ip;
// 	    }
// 	}
        //return hide_after ? new Point2D.Double(-11100.0, -11100.0) : pointA[pointA.length-1];
    }

    public Point2D getPointAtPercent(double percent) {
        return getPointAt(percent * getLength());
    }

    public void setSelected(boolean b) {
        selected = b;
        if (selected) {
            global_selected = this;
        }
        Iterator it = seg_l.iterator();
        while (it.hasNext()) {
            Segment_Q sq = (Segment_Q) it.next();
            sq.selectedPoint = -1;
        }
    }

    public boolean isSelected() {
        return selected;
    }

    Probe findNearest(Point2D p) {
        Probe nearest = new Probe();
        Iterator it = seg_l.iterator();
        while (it.hasNext()) {
            Segment_Q sq = (Segment_Q) it.next();
            Probe n = sq.findNearest(p);
            if (n.dist < nearest.dist) {
                nearest = n;
                nearest.pa = this;
            }
        }
        return nearest;
    }

    public Mark findNearestMarker(Point2D p) {
        Mark mk = null;
        double dist = 0.0;
        Iterator it = marker.iterator();
        while (it.hasNext()) {
            Mark mk2 = (Mark) it.next();
            double d = getPointAt(mk2.where).distance(p);
            if (mk == null || d < dist) {
                dist = d;
                mk = mk2;
            }
        }
        return mk;
    }

    public double distMarker(Mark mk, Point2D p) {
        double d = getPointAt(mk.where).distance(p);
        return d;
    }

    public Mark getMarker(int m_ord) {
        if (m_ord < 0)
            return null;
        if (m_ord >= marker.size())
            return null;
        return (Mark) marker.get(m_ord);
    }

    public double findNearestPoint(Point2D p) {
        double dist = 99999999;
        double fp = 0.0;
        for (int i = 0; i <= getLength(); i++) {
            Point2D pp = getPointAt(i);
            if (pp != null) {
                double d = pp.distance(p);
                if (d < dist) {
                    dist = d;
                    fp = i;
                }
            }
        }
        return findNearestPointScale(p, fp);
    }

    double findNearestPointScale(Point2D p, double here) {
        double scale = 0.01;
        double dist = 99999999;
        double fp = here;
        for (int i = -200; i <= 200; i++) {
            Point2D pp = getPointAt(here + i * scale);
            if (pp != null) {
                double d = pp.distance(p);
                if (d < dist) {
                    dist = d;
                    fp = here + i * scale;
                }
            }
        }
        return fp;
    }

    public double getLength() {
        if (len == 0)
            rebuildLength();
        return len;
    }

    public void draw(Graphics2D g2) {
        Segment_Q selected_sq = null;

        if (true /*active*/) {
            Color col = selected ? Color.orange : Color.orange.darker();

            Iterator it = seg_l.iterator();
            while (it.hasNext()) {
                Segment_Q sq = (Segment_Q) it.next();
                if (sq.selectedPoint >= 0)
                    selected_sq = sq;
                else {
                    g2.setColor(col);
                    sq.draw(g2);
                }
            }
            if (selected_sq != null) {
                g2.setColor(Color.gray);
                selected_sq.drawConnector(g2);
                g2.setColor(Color.orange.brighter());
                selected_sq.draw(g2);
            }
        } else {
            g2.setColor(Color.green);
            g2.draw(getShape());
        }
        drawMarker(g2);
    }

    private void drawSmallBox(Graphics2D g2, Point2D p, int w) {
        g2.draw(new Rectangle2D.Double(p.getX() - w / 2.0,
                p.getY() - w / 2.0,
                w, w));
    }

    private void drawMarker(Graphics2D g2) {
        Iterator it = marker.iterator();
        while (it.hasNext()) {
            Mark mk = (Mark) it.next();
            Point2D pp = getPointAt(mk.where);
            if (pp != null) {
                g2.setColor(Color.magenta.brighter());
                drawSmallBox(g2, pp, 5);
            }
        }
    }

    private String encodePoint(Point2D p) {
        return "" + p.getX() + ',' + p.getY();
    }

    public Element element() {
        Element el = new Element("TPath");
        for (int i = 0; i < seg_l.size(); i++) {
            Segment_Q qq = getSq(i);
            Element eq = new Element("q");
            eq.addAttr("ord", "" + i);
            eq.addAttr("p1", "" + encodePoint(qq.p1));
            eq.addAttr("p2", "" + encodePoint(qq.p2));
            eq.addAttr("pc", "" + encodePoint(qq.pc));
            el.add(eq);
        }
        return el;
    }

    public Element getElement() {
        Element el = new Element("TPath");
        el.addAttr("nid", "" + nid);
        for (int i = 0; i < seg_l.size(); i++) {
            Segment_Q qq = getSq(i);
            Element eq = new Element("q");
            eq.addAttr("ord", "" + i);
            eq.addAttr("p1", "" + encodePoint(qq.p1));
            eq.addAttr("p2", "" + encodePoint(qq.p2));
            eq.addAttr("pc", "" + encodePoint(qq.pc));
            el.add(eq);
        }
        double[] lenArr = getLenA();
        Point2D[] point2d = getPoint2D();
        Element help = new Element("help");
        help.addAttr("len", format(lenArr));
        help.addAttr("seg", format(point2d));
        el.add(help);
        Element info = new Element("info");
        info.addAttr("flatness", "" + OmegaConfig.FLATNESS);
        info.addAttr("size", "" + point2d.length);
        el.add(info);

        for (int i = 0; i < marker.size(); i++) {
            Mark mk = (Mark) marker.get(i);
            el.add(mk.getElement());
        }
        return el;
    }


    public static String format(Point2D[] point2d) {
        DelimitedStringBuilder sb = new DelimitedStringBuilder(";");
        for(Point2D p : point2d) {
            sb.append("" + p.getX() + "," + p.getY());
        }
        return sb.toString();
    }

    public static String format(double[] lenArr) {
        DelimitedStringBuilder sb = new DelimitedStringBuilder(";");
        for(double d : lenArr) {
            sb.append("" + d);
        }
        return sb.toString();
    }


    public static void main(String[] argv) {
        HashMap flag = SundryUtils.flagAsMap(argv);
        java.util.List argl = SundryUtils.argAsList(argv);

        JFrame f = new JFrame("Path - test");
        Container c = f.getContentPane();

        com.femtioprocent.omega.graphic.render.Canvas ca = new Canvas();

        if (flag.get("g") != null)
            ;//Canvas.gdbg = true;

        c.add(ca);

        f.pack();

        f.setSize(870, 640);

        f.setVisible(true);
        //ca.setBackground("developer.omega_assets/media/background/Barn1.jpg");

        SundryUtils.m_sleep(300);


        Point2D sp = new Point2D.Double(100, 100);
        Point2D ep = new Point2D.Double(200, 300);
        Path pa = new Path(1, sp, ep);
        pa.selected = true;
        pa.draw((Graphics2D) ca.getGraphics());
        pa.extendSegment(new Point2D.Double(300, 100));
        pa.draw((Graphics2D) ca.getGraphics());

        Point2D sp2 = new Point2D.Double(300, 200);
        Point2D ep2 = new Point2D.Double(400, 500);
        Path pa2 = new Path(2, sp2, ep2);
        pa2.draw((Graphics2D) ca.getGraphics());
        pa2.extendSegment(new Point2D.Double(350, 300));
        pa2.draw((Graphics2D) ca.getGraphics());

//log	OmegaContext.sout_log.getLogger().info("ERR: " + "" + pa.getPointAt(0.0));
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "" + pa.getPointAt(10.0));
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "" + pa.getPointAt(100.0));
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "" + pa.getPointAt(100.2));
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "" + pa.getPointAt(pa.getLength()));
    }

    public double[] getLenA() {
        return lenA;
    }

    public Point2D[] getPoint2D() {
        return pointA;
    }
}
