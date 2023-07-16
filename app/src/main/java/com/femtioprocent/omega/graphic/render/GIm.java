package com.femtioprocent.omega.graphic.render;

import com.femtioprocent.omega.anim.tool.timeline.TimeLine;
import com.femtioprocent.omega.media.images.xImage;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class GIm {
    protected final String fid;
    protected String delayed_id_attrib = null;
    protected int layer = 2;
    protected int reset_sequence = 0;
    protected AlphaComposite acomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            1.0f);

    protected com.femtioprocent.omega.graphic.render.Canvas ca;
    public xImage xim;
    protected Rectangle2D bounding_rect = new Rectangle2D.Double(0, 0, 0, 0);
    private Rectangle2D prev_bounding_rect = new Rectangle2D.Double(0, 0, 0, 0);
    private double maxW;
    private double maxH;
    private double maxWH;

    // Object geometrical state

    //    protected Point2D pos;
    protected double rot = 0;
    protected double scale = 1.0;
    public boolean mirror_x = false;
    public boolean mirror_y = false;
    public boolean prim_mirror_x = false;
    public boolean prim_mirror_y = false;

    protected GIm(com.femtioprocent.omega.graphic.render.Canvas ca, String fid) {
        this.ca = ca;
        this.fid = fid;
        xim = new xImage(fid);
        initIm();
    }

    protected GIm(com.femtioprocent.omega.graphic.render.Canvas ca, GIm gim) {
        this.ca = ca;
        this.fid = gim.fid;
        xim = new xImage(fid);
        initIm();
    }

    public void initIm() {
        Image ima = xim.getImage(ca);

        int w = ima.getWidth(null);
        int h = ima.getHeight(null);
        maxW = w; // ima.getWidth(null);
        maxH = h; // ima.getHeight(null);
        maxWH = Math.max(maxW, maxH);
//	prev_bounding_rect = new Rectangle2D.Double(0, 0, 0, 0);
//	pos = new Point2D.Double(11110, 11110);
    }

    public void setAttribName(String an) {
        if (an != null && an.length() == 0)
            an = null;
//	OmegaContext.sout_log.getLogger().info(":--: " + "ATTR dep_set " + an);
        xim.setAttrib(an);
        initIm();
    }

    public void commitAttribName() {
        if (delayed_id_attrib == null)
            return;
        if (delayed_id_attrib.equals("@@@ null @@@"))
            delayed_id_attrib = null;
        setAttribName(delayed_id_attrib);
        delayed_id_attrib = null;
    }

    public void setAttribNameUncommited(String an) {
        if (an == null)
            an = "@@@ null @@@";
//	OmegaContext.sout_log.getLogger().info(":--: " + "ATTR dep_set delayed " + an);
        delayed_id_attrib = an;
    }

    public void setMirror(boolean m_x, boolean m_y) {
        mirror_x = m_x;
        mirror_y = m_y;
    }

    public void setPrimMirror(boolean m_x, boolean m_y) {
        prim_mirror_x = m_x;
        prim_mirror_y = m_y;
    }

    public void setLayer(int a) {
        layer = a;
    }

    public void setVisibility(int percent) {
        if (percent == 0)
            acomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) (0.0));
        else
            acomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    (float) (percent / 100.0));
    }

    synchronized public void initPlay(Object o) {
        beginPlay();
    }

    synchronized public void beginPlay() {
        hide();
        layer = 2;
        setVisibility(100);
        mirror_x = mirror_y = false;
    }

    synchronized public void hide() {
        //pos.setLocation(-2000, -2000);
        AffineTransform at = new AffineTransform();
        at.translate(10000, 10000);
        render(at);
    }

    public String getFNBase() {
        return xim.getFNBase();
    }

    public String getPeTaskNid() {
        return xim.getPeTaskNid();
    }

//     public Image getImage() {
// 	return xim.getImage(ca);
//     }

    public Image getBaseImage() {
        return xim.getBaseImage(ca);
    }

    Image last_im = null;
    int last_w, last_h;

    public int getWidth() {
        Image im = xim.getImage(ca);
        if (last_im != im) {
            last_im = im;
            last_w = im.getWidth(null);
            last_h = im.getHeight(null);
        }
        return last_w;
    }

    public int getHeight() {
        getWidth();
        return last_h;
    }

    synchronized void restoreBackground() {
        ca.restoreImage(getPrevBoundingRect());
    }

    synchronized void render(AffineTransform at) {
//	OmegaContext.sout_log.getLogger().info(":--: " + "GIm.render " + this + ' ' + at);
        ca.drawImage(xim.getImage(ca), at, acomp);
    }

    void setPrevBoundingRect(Rectangle2D br) {
        prev_bounding_rect.setRect(br);
    }

    public Rectangle2D getPrevBoundingRect() {
        if (prev_bounding_rect == null) {
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "======================= pbr null ====");
            return bounding_rect;
        } else
            return prev_bounding_rect;
    }

    public AffineTransform getAffineTransformAtTime(int dt, TimeLine tl, Rectangle2D bounding_rect) {
        AffineTransform at = new AffineTransform();
        at.translate(dt / 10, dt / 10);
        return at;
    }

    public AffineTransform getAffineTransformAtTime(int dt, TimeLine[] tlA, Rectangle2D bounding_rect) {
//  	if ( nid < tlA.length) {
//  	    TimeLine tl = tlA[nid];
//  	    if ( tl != null ) {
//  		return getAffineTransformAtTime(dt, tl, bounding_rect);
//  	    }
//  	}
        return null;
    }
}

