package com.femtioprocent.omega.graphic.render;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.tool.timeline.TimeLine;
import com.femtioprocent.omega.graphic.util.LoadImage;
import com.femtioprocent.omega.servers.httpd.Server;
import com.femtioprocent.omega.subsystem.Httpd;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Canvas extends JPanel implements java.awt.image.ImageObserver {
    private String im_name;
    protected Image bg;
    protected Image bg_wings;
    protected Image off_im;
    protected Image off_im_wings;
    protected Graphics2D off_g2;
    public Dimension im_size = new Dimension(0, 0);

    public AllGIm allgim;

    private static boolean gdbg = false;

    java.util.List<Wing> wings = new ArrayList<>();

    public boolean HIDDEN = false;

    public Canvas() {
        allgim = new AllGIm(this);
    }

    public Image getImageBackground() {
        return bg_wings;
    }

    public void resetBackground() {
        setBackground(im_name, wings);
    }

    public void setBackground(String im_name) {
        setBackground(im_name, null);
    }

    public void setBackground(String im_name, java.util.List<Wing> wings) {
        this.im_name = im_name;
        if (wings == null)
            wings = new ArrayList<>();
        this.wings = wings;

        String imn = im_name;
        bg = LoadImage.loadAndWaitOrNull(this, imn, false);
//	OmegaContext.sout_log.getLogger().info(":--: " + "bg " + bg + ' ' + imn);
        im_size = new Dimension(bg.getWidth(null), bg.getHeight(null));

        updWings();

        Server httpd = ((Httpd) (OmegaContext.getSubsystem("Httpd"))).httpd;
        httpd.getHashMap().put("lesson:background", imn);

        repaint();
    }

    public Wing createWing(String fn, int x, int y, int layer, double scale, int mirror) {
        Wing w = new Wing(this, fn, x, y, layer, wings.size());
        w.mirror = mirror;
        w.scale = scale;
        wings.add(w);
        return w;
    }

    public java.util.List<Wing> removeWing(int ix) {
        if (ix >= wings.size())
            return null;

        wings.remove(ix);
        resetBackground();
        return wings;
    }

    public void updWings() {
        if (bg != null)
            bg_wings = bg;
        bg_wings = createWithWings(bg_wings, im_size, wings);
        bg = null;

        off_im = createImage(im_size.width, im_size.height);
        if (off_im != null) {
            off_g2 = (Graphics2D) off_im.getGraphics();
            restoreImage(0, 0, im_size.width, im_size.height);
//	    repaint();
        }
    }

    Wing getWing(int ix) {
        return (Wing) wings.get(ix);
    }

    int trace_wing = -1;
    boolean trace_wing_drag = false;
    int trace_wing_dx = 0;
    int trace_wing_dy = 0;

    public void traceNoWing() {
        trace_wing = -1;
        trace_wing_drag = false;
        repaint();
    }

    public void traceWing(int ixx, double dx, double dy, boolean is_drag) {
        trace_wing = ixx;
        trace_wing_drag = is_drag;
        trace_wing_dx = (int) dx;
        trace_wing_dy = (int) dy;
        repaint();
    }

    void drawWing(Wing w) {
        drawWing(off_g2, w);
    }

    void drawWing(Graphics2D g2, Wing w) {
        AffineTransform at = new AffineTransform();
        at.translate(w.pos.getX(), w.pos.getY());
        double sc = w.scale;
        if (sc == 0)
            sc = 1.0;

        at.scale(w.scale, w.scale);
        switch (w.mirror) {
            case 1:
                at.translate(w.width, 0);
                at.scale(-1, 1);
                break;
            case 2:
                at.translate(0, w.height);
                at.scale(1, -1);
                break;
            case 3:
                at.translate(w.width, w.height);
                at.scale(-1, -1);
                break;
        }

        g2.drawImage(w.im, at, null);
    }

    Image createWithWings(Image bg, Dimension im_size, java.util.List<Wing> wings) {
        try {
            Image im = createImage(im_size.width, im_size.height);
//	OmegaContext.sout_log.getLogger().info(":--: " + "withW im is " + im + ' ' + im_size);
            if (im == null)
                return bg;

            Graphics gg = im.getGraphics();
            gg.drawImage(bg, 0, 0, null);
            if (wings != null) {
                for (int il = 0; il < 5; il++) {
                    for (int i = 0; i < wings.size(); i++) {
                        Wing wing = getWing(i);
                        if (wing != null && il == wing.layer) {
                            drawWing((Graphics2D) gg, wing);
                        }
                    }
                }
            }
            return im;
        } catch (IllegalArgumentException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "createWithWings: " + ex);
            return null;
        }
    }

    public Dimension getPreferredSize() {
        return im_size;
    }

    public synchronized void drawImage(Image im, AffineTransform at, AlphaComposite acomp) {
        if (acomp == null)
            acomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) (0.0));
        else
            off_g2.setComposite(acomp);
        off_g2.drawImage(im, at, null);
        off_g2.setComposite(AlphaComposite.SrcOver);
    }

    public double getOffsX() {
        return 0;
    }

    public double getOffsY() {
        return 0;
    }

    public synchronized void off_upd(int x, int y, int w, int h) {
        if (h < 0) {
            y += h;
            h = -h;
        }
        if (w < 0) {
            x += w;
            w = -w;
        }
        double offs_x = getOffsX();
        double offs_y = getOffsY();
        Graphics2D gg = (Graphics2D) getGraphics().create(x + (int) (offs_x),
                y + (int) (offs_y), w, h);
        gg.drawImage(off_im, -x, -y, this);  // Rxy
        gg.dispose();
    }

    synchronized void off_upd(Rectangle2D[] ra) {
        int a = SundryUtils.rand(255);
        for (int i = 0; i < ra.length; i++) {
            off_upd((int) ra[i].getX(),
                    (int) ra[i].getY(),
                    (int) ra[i].getWidth(),
                    (int) ra[i].getHeight());
            if (gdbg) {
                Graphics2D g2 = (Graphics2D) getGraphics();
                AffineTransform at = g2.getTransform();
                int offs_x = (int) getOffsX();
                int offs_y = (int) getOffsY();
                at.translate(offs_x, offs_y);
                g2.setTransform(at);
                g2.setColor(new Color(a, a, a));
                g2.drawRect((int) ra[i].getX(),
                        (int) ra[i].getY(),
                        (int) ra[i].getWidth(),
                        (int) ra[i].getHeight());
            }
        }
    }

    synchronized void restoreImage(int x, int y, int w, int h) {
        if (bg_wings == null)
            return;
        if (off_g2 != null) {
            Graphics gg = off_g2.create(x, y, w + 2, h + 2);
            gg.drawImage(bg_wings, -x, -y, null);
            gg.dispose();
        }
    }

    synchronized void restoreImage(Rectangle2D r) {
        restoreImage((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (off_im != null) {
            double offs_x = getOffsX();
            double offs_y = getOffsY();
            offs_y = offs_x = 0;
            g2.drawImage(off_im, (int) offs_x, (int) offs_y, null);
        }
        if (trace_wing != -1) {
            if (trace_wing < wings.size()) {
                if (trace_wing_drag)
                    g2.setColor(Color.red);
                else
                    g2.setColor(Color.yellow);
                g2.drawRect((int) getWing(trace_wing).pos.getX() + trace_wing_dx,
                        (int) getWing(trace_wing).pos.getY() + trace_wing_dy,
                        (int) (getWing(trace_wing).scale * getWing(trace_wing).im.getWidth(null)),
                        (int) (getWing(trace_wing).scale * getWing(trace_wing).im.getHeight(null)));
            }
        }
    }

    public void initPlay(Object o) {
        traceNoWing();
//	repaint();
        allgim.initPlay(o);
    }

    public void updateAtTime(int dt, TimeLine[] tlA) {
        allgim.updateAtTime(dt, tlA);
    }

    public Element getElement() {
        Element el = new Element("Canvas");
        Element bel = new Element("background");
        bel.addAttr("name", im_name);
        bel.addAttr("width", "" + bg_wings.getWidth(null));
        bel.addAttr("height", "" + bg_wings.getHeight(null));
        el.add(bel);
        return el;
    }


    public void load(Element el) {
        Element cel = el.findElement("Canvas", 0);
        if (cel != null) {
            Element eb = el.findElement("background", 0);
            if (eb != null) {
                String s = eb.findAttr("name");
                if (s != null) {
                    setBackground(s, new ArrayList<>());
//fix		    wings_panel.removeAllWings();
                }
            }
        }
    }
}
