package com.femtioprocent.omega.anim.appl;

import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class LessonEmu extends JPanel {
    Element el;

    class A {
        String text;
        String type;
        Rectangle r;
        boolean hasActor;

        A(String tx, String ty, boolean has_act) {
            text = tx;
            type = ty;
            hasActor = has_act;
        }

        void setText(String tx) {
            text = tx;
        }
    }

    class R {
        String all;
        A[] a;
        int ix;

        R() {
            a = new A[]{
                    new A("        ", "s", true),
                    new A("        ", "v", false),
                    new A("        ", "o", true)};
        }

        int size() {
            return a.length;
        }

        boolean isEmpty(int ix) {
            if (a[ix].text.equals("        "))
                return true;
            return false;
        }

    }

    R rslt;
    Rectangle rslt_r;

    A[] subjobj = new A[0];
    String typeA[] = {"so", "v"};

    Verb verb = new Verb("verb");
    Rectangle verb_r;

    Font fo;
    Mouse m;


    LessonEmu() {
        fo = new Font("Sans Serif", Font.PLAIN, 22);
        m = new Mouse();
    }

    class Mouse extends MouseInputAdapter {
        boolean was_shift = false;
        Point mpress_p;

        Mouse() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void mousePressed(MouseEvent e) {
            mpress_p = new Point((int) e.getX(), (int) e.getY());
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "" + mpress_p);
            for (int i = 0; i < subjobj.length; i++)
                if (subjobj[i].r.contains(mpress_p)) {
                    String s = subjobj[i].text;
                    if (addNext(s, typeA[0]))
                        synchronized (mb_r) {
                            mb_r.notify();
                        }
                }
            if (verb_r.contains(mpress_p)) {
                String s = verb.getBase();
                if (addNext(s, typeA[1]))
                    synchronized (mb_r) {
                        mb_r.notify();
                    }
            }
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }
    }

    Object mb_r = new Object();

    void waitForResult() {
        synchronized (mb_r) {
            try {
                mb_r.wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    void reset() {
        rslt = new R();
        repaint();
    }

    void attach(String verb_s, Element el) {
        if (verb_s.indexOf('$') == -1) {
            verb_s = "$1 " + verb_s + " $2";
        }
        verb = new Verb(verb_s);
//log	OmegaContext.sout_log.getLogger().info(":--: " + "VERB " + verb);
        ArrayList li = new ArrayList();

        this.el = el;
        Element aact = el.findElement("AllActors", 0);
        for (int i = 0; i < 10; i++) {
            Element act = aact.findElement("Actor", i);
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "++ found " + act);
            if (act != null) {
                String id = act.findAttr("lesson_id");
                if (id != null)
                    li.add(id);
            }
        }

        String[] sa = (String[]) li.toArray(new String[0]);
        subjobj = new A[sa.length];

        for (int i = 0; i < sa.length; i++) {
            subjobj[i] = new A(sa[i], "s", true);
        }

        reset();
    }

    String get(R r) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < r.size(); i++) {
            if (i > 0 && !r.isEmpty(i))
                sb.append(" ");
            sb.append(r.a[i].text);
        }
        return sb.toString();
    }

    boolean isFull() {
        for (int i = 0; i < rslt.size(); i++)
            if (rslt.isEmpty(i))
                return false;
        return true;
    }

    boolean match(String collection, String test) {
        return collection.indexOf(test) != -1;
    }

    boolean addNext(String s, String src_t) {
        for (int i = 0; i < rslt.size(); i++) {
            if (rslt.isEmpty(i))
                if (match(src_t, rslt.a[i].type)) {
                    rslt.a[i].setText(s);
                    repaint();
                    return isFull();
                }
        }
        return false;
    }

    String[] getAllBoundActorId() {
        if (rslt == null)
            return new String[0];

        ArrayList li = new ArrayList();

        for (int i = 0; i < rslt.size(); i++)
            if (rslt.a[i].hasActor)
                li.add(rslt.a[i].text);

        return (String[]) li.toArray(new String[0]);
    }

    void drawRect(Graphics g, Rectangle r, String s) {
        g.drawRect((int) r.getX(),
                (int) r.getY(),
                (int) r.getWidth(),
                (int) r.getHeight());
        g.drawString(s,
                (int) r.getX() + 20,
                (int) r.getY() + 28);
    }

    boolean initR_done = false;

    void initR() {
        if (initR_done)
            return;

        int W = getWidth();
//log	OmegaContext.sout_log.getLogger().info(":--: " + "W is " + W);
        int H = getWidth();
//log	OmegaContext.sout_log.getLogger().info(":--: " + "H is " + H);

        int wc = W / 3;
        int wg = (W - 2 * wc) / 3;
        int wd = W - 2 * wg;

        int hc = 38;
        int hg1 = 52;
        int hg = 14;
        int hgs = 12;
        int hd = 42;

        int hC = hg + hd + hg + hg + hg + hg;

        verb_r = new Rectangle(wg + wc + wg,
                hg1 + 0 * (hgs + hc) + hC,
                wc,
                hc);

        rslt_r = new Rectangle(wg, hg1 + hg, wd, hd);

        for (int i = 0; i < subjobj.length; i++) {
            subjobj[i].r = new Rectangle(wg,
                    hg1 + i * (hgs + hc) + hC,
                    wc,
                    hc);
        }

        initR_done = true;
    }

    public void paintComponent(Graphics g) {
        g.setColor(new Color(220, 220, 180));
        g.fillRect(0, 0, 19000, 19000);

        g.setColor(new Color(0, 0, 0));

        initR();

        g.setFont(fo);

        if (rslt != null)
            drawRect(g, rslt_r, get(rslt));


        for (int i = 0; i < subjobj.length; i++) {
            drawRect(g, subjobj[i].r, subjobj[i].text);
        }

        for (int i = 0; i < 1; i++) {
            drawRect(g, verb_r, verb.getBase());
        }
    }
}
