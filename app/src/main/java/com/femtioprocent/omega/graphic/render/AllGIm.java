package com.femtioprocent.omega.graphic.render;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.cabaret.GImAE;
import com.femtioprocent.omega.anim.tool.timeline.TimeLine;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class AllGIm {
    GIm[] arr = new GIm[OmegaConfig.TIMELINES_N];
    Canvas ca;

    AllGIm(Canvas ca) {
        this.ca = ca;
    }

    public void set(GIm gim, int ix) {
        if (ix >= arr.length)
            return;

        hideActor(ix);
        arr[ix] = gim;
        if (gim != null)
            ((GImAE) (gim)).nid = ix;
    }

    public void remove(GIm gim) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == gim) {
                hideActor(i);
                arr[i] = null;
            }
    }

    public GIm get(int nid) {
        if (nid < arr.length)
            return arr[nid];
        return null;
    }

    public void initPlay(Object o) {
        hideActors();
        for (int i = 0; i < arr.length; i++) {
            GIm gim = get(i); // arr[i];
            if (gim != null) {
                gim.initPlay(o);
            }
        }
    }

    public void hideActors() {
        RectList rl = new RectList();

        for (int i = 0; i < arr.length; i++) {
            GIm gim = arr[i];
            if (gim != null) {
                gim.restoreBackground();
                rl.add(gim.getPrevBoundingRect());
                Rectangle2D bounding_rect = new Rectangle2D.Double(10000, 10000, 0, 0);
                gim.setPrevBoundingRect(bounding_rect);
            }
        }

        Rectangle2D[] r2da = (Rectangle2D[]) rl.rl.toArray(new Rectangle2D[0]);
        ca.off_upd(r2da);
    }

    public void hideActor(int ix) {
        RectList rl = new RectList();

        GIm gim = arr[ix];
        if (gim != null) {
            gim.restoreBackground();
            Rectangle2D bounding_rect = new Rectangle2D.Double(10000, 10000, 0, 0);
            gim.setPrevBoundingRect(bounding_rect);
            rl.add(gim.getPrevBoundingRect());
            Rectangle2D[] r2da = (Rectangle2D[]) rl.rl.toArray(new Rectangle2D[0]);
            ca.off_upd(r2da);
        }
    }

    public void updateAtTime(int dt, TimeLine[] tlA) {
        RectList rl = new RectList();
        GIm[] gA = arr;

        for (int ii = 0; ii < gA.length; ii++) {
            if (tlA[ii] == null)
                continue;
            GIm gim = get(ii);
            if (gim != null) {
                try {
                    gim.restoreBackground();
                    Rectangle2D br = new Rectangle2D.Double(0, 0, 0, 0);
                    br.setRect(gim.getPrevBoundingRect());
                    rl.add(br);
                    gim.commitAttribName();

                    int an_sp = (int) (1000 * ((GImAE) gim).anim_speed);
                    int td1 = dt - gim.reset_sequence;
                    if ( td1 < 0 )
                        td1 = 0;
                    int td2 = td1 / an_sp;
                    int tm = td2 % 1000;              // why?
                    if ( gim.xim.setInnerAnimIndex(dt, tm) )
                        gim.initIm();
                } catch (NullPointerException ex) {
                    OmegaContext.sout_log.getLogger().info("ERR: " + "---1 " + ii + ' ' + ex);
                    ex.printStackTrace();
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            for (int ii = 0; ii < gA.length; ii++) {
                if (tlA[ii] == null)
                    continue;
                GIm gim = get(ii); // gA[ii];
                if (gim != null) {
                    try {
                        if (gim.layer == i) {
                            Rectangle2D bounding_rect = new Rectangle2D.Double(0, 0, 0, 0);
                            AffineTransform at = gim.getAffineTransformAtTime(dt, tlA, bounding_rect);
                            if (at != null) {
//				gim.render(at);
                                ((GImAE) gim).render(at);
                                rl.add(bounding_rect);
                                gim.setPrevBoundingRect(bounding_rect);
                            } else {
                                bounding_rect = null;
                            }
                        }
                    } catch (NullPointerException ex) {
                        OmegaContext.sout_log.getLogger().info("ERR: " + "---2 " + ii + ' ' + ex);
                    }
                }
            }
            if (ca.wings != null)
                for (int iw = 0; iw < ca.wings.size(); iw++) {
                    if (ca.getWing(iw).layer == i)
                        ca.drawWing(ca.getWing(iw));
                }
        }

        Rectangle2D[] r2da = (Rectangle2D[]) rl.rl.toArray(new Rectangle2D[0]);
        ca.off_upd(r2da);
    }

    public Element getElement() {
        Element el = new Element("AllGIm");
        for (int i = 0; i < arr.length; i++) {
            Element ael = new Element("actor");
            el.add(ael);
        }
        return el;
    }
}
