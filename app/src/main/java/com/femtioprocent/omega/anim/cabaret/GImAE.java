package com.femtioprocent.omega.anim.cabaret;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.canvas.AnimCanvas;
import com.femtioprocent.omega.anim.tool.path.AllPath;
import com.femtioprocent.omega.anim.tool.path.Path;
import com.femtioprocent.omega.anim.tool.timeline.TimeLine;
import com.femtioprocent.omega.graphic.render.GIm;
import com.femtioprocent.omega.util.SundryUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class GImAE extends GIm {
    // a + b * dt
    private double rot_a = 0;
    private double rot_b = 0;
    private double rot_t;
    private double rot_lim = 10001;

    private double scale_a = 1.0;
    private double scale_b = 0.0;
    private double scale_t;
    private double scale_lim = 1.0;

    public double anim_speed = 0.2;

    //    private boolean use_alt_hotspot = false;
//      public double hotspot_fx = 0.5;
//      public double hotspot_fy = 0.5;
//      public double hotspotAlt_fx = 0.5;
//      public double hotspotAlt_fy = 0.5;
    public Hotspot hotspot = new Hotspot();

    double prim_scale = 1.0;
    int prim_mirror = 0;

    private String lesson_id;

    private boolean can_eat;
    private boolean can_bee_eaten;

    AllPath apa;

    public int nid;
    public int imw;
    public int imh;

    private static GImAE mask;
    private static Image current_mask_image;

    private BufferedImage buffered_image = null;

    private AffineTransform at = new AffineTransform();

    private String variable[] = new String[]{"", "", ""};

    public GImAE(AnimCanvas ca, String id, int nid) {
        super(ca, id);
        this.apa = ca.ap;
        this.nid = nid;
        imw = getWidth();
        imh = getHeight();
        setHotSpotIx(0, 0.5, 0.5);
    }

    public GImAE(AnimCanvas ca, GImAE gimae, int nid) {
        super(ca, (GIm) gimae);
        this.apa = gimae.apa;
        this.nid = nid;
        for (int ih = 0; ih < Hotspot.HOTSPOT_N; ih++)
            setHotSpotIx(ih, gimae.hotspot.getX(ih), gimae.hotspot.getY(ih));
        imw = getWidth();
        imh = getHeight();
        setPrimMirror(gimae.prim_mirror);
        setPrimScale(gimae.prim_scale);
        setAnimSpeed(gimae.anim_speed);
        for (int i = 0; i < 4; i++)
            setVariable(i, gimae.getVariable(i));
        lesson_id = gimae.lesson_id;
    }

    public void setVariable(int ix, String s) {
        ix--;
        if (ix >= 0 && ix < 3)
            variable[ix] = s;
    }

    public String getVariable(int ix) {
        if (ix == 0)
            return getLessonId();
        ix--;
        if (ix >= 0 && ix < 3)
            return variable[ix];
        return "";
    }

    public void setRotation(double val, double stop_val, int when) {
        if (val == 0) {
            if (stop_val > 10001)
                stop_val = 0;
            rot_a = stop_val;
            rot_b = 0;
            rot_t = when;
            rot_lim = 10001;
            return;
        }
        rot_a = rotAt(when);
        rot_b = val;
        rot_t = when;

        if (stop_val >= 19999.0) {
            rot_lim = stop_val;
            if (rot_b < 0)
                rot_lim *= -1;
        } else {
            double rot_0_1 = ((rot_a + 10000 *
                    (3.141592653589793238 * 2)) % (3.141592653589793238 * 2));
            double rot_0_1_2 = ((stop_val + 10000 *
                    (3.141592653589793238 * 2)) % (3.141592653589793238 * 2));
            double rot_togo = rot_0_1_2 - rot_0_1;
            if (rot_b > 0) {
                if (rot_togo >= 0)
                    rot_lim = rot_a + rot_togo;
                else
                    rot_lim = rot_a + rot_togo + 3.141592653589793238 * 2;
            } else {
                if (rot_togo <= 0)
                    rot_lim = rot_a + rot_togo;
                else
                    rot_lim = rot_a + rot_togo - 3.141592653589793238 * 2;
            }
        }
    }

    public void setScale(double val, double stop_val, int when) {
        scale_a = scaleAt(when);
        scale_b = val;
        scale_t = when;
        scale_lim = stop_val;
    }

    public void setAnimSpeed(double a) {
        anim_speed = a;
    }

    synchronized public void initPlay(Object o) {
        super.initPlay(o);
        rot_a = 0;
        rot_b = 0;
        scale_a = 1.0;
        scale_b = 0;
        scale_t = 0;
        rot_t = 0;
        scale_lim = 1.0;
        rot_lim = 10001;
        setAnimSpeed(0.2);
        setAttribName(null);
        apa = (AllPath) o;
    }

    synchronized public void beginPlay() {
        super.beginPlay();
        mask = null;
        current_mask_image = null;
        buffered_image = null;
        setDinner(false, false);
//	use_alt_hotspot = false;
    }

    double scaleAt(int dt) {
        double scale = scale_a + scale_b * (dt - scale_t);
        if (scale_b < 0) {
            if (scale < scale_lim) {
                scale_b = 0;
                scale_a = scale_lim;
                scale = scale_lim;
            }
        } else {
            if (scale > scale_lim) {
                scale_b = 0;
                scale_a = scale_lim;
                scale = scale_lim;
            }
        }
        return scale;
    }

    double rotAt(int dt) {
        double rot = rot_a + rot_b * (dt - rot_t);
        if (rot_lim > 10000 || rot_lim < -10000)
            return rot;
        if (rot_b < 0) {
            if (rot <= rot_lim) {
                rot_b = 0;
                rot_a = rot_lim;
                rot = rot_lim;
            }
        } else {
            if (rot >= rot_lim) {
                rot_b = 0;
                rot_a = rot_lim;
                rot = rot_lim;
            }
        }

        return rot;
    }

    public void setHotSpot(double fx, double fy) {
        hotspot.set(0, fx, fy);
    }

    public void setHotSpotIx(int ix, double fx, double fy) {
        hotspot.set(ix, fx, fy);
    }

    public void setHotSpotIx(int ix, String s) {
        String[] sa = SundryUtils.split(s, " ,;");
        double x = SundryUtils.tD(sa[0]);
        double y = SundryUtils.tD(sa[1]);
        hotspot.set(ix, x, y);
    }

    public void setHotSpotIxSame(int ix) {
        if (ix > 0)
            hotspot.set(ix, hotspot.getX(0), hotspot.getY(0));
    }

    private double fix2(double a) {
        int i = (int) (a * 100 + 0.5);
        return ((double) i) / 100;
    }

    public String getHotSpotAsString(int ix) {
        if (ix == 0)
            return "" + fix2(hotspot.getX()) + ' ' + fix2(hotspot.getY());
        else
            return "" + fix2(hotspot.getX(1)) + ' ' + fix2(hotspot.getY(1)) + " ->"
                    + " " + fix2(hotspot.getX(2)) + ' ' + fix2(hotspot.getY(2)) + "";
    }

    public void setDinner(boolean can_eat, boolean can_bee_eaten) {
        this.can_eat = can_eat;
        this.can_bee_eaten = can_bee_eaten;
    }

    public void setOption(int arg) {
    }

    public void setResetSequence(String arg, int when, int beginning) {
        if ( arg == null || arg.equals("") ) {
            reset_sequence = when;
        } else if ( arg.equals("{") ) {
            reset_sequence = 0;
        } else if ( arg.equals("[") ) {
            reset_sequence = beginning;
        }
    }

//      public void setAlternative(int arg) {
//  	if ( (arg & 1) == 1 )
//  	    use_alt_hotspot = true;
//  	else
//  	    use_alt_hotspot = false;
//  	OmegaContext.sout_log.getLogger().info("ERR: " + "alt hs " + use_alt_hotspot);
//      }

    public void setPrimScale(double a) {
        prim_scale = a;
    }

    public double getPrimScale() {
        return prim_scale;
    }

    public void setPrimMirror(int a) {
        prim_mirror = a;
        switch (a) {
            case 0:
                setPrimMirror(false, false);
                break;
            case 1:
                setPrimMirror(true, false);
                break;
            case 2:
                setPrimMirror(false, true);
                break;
            case 3:
                setPrimMirror(true, true);
                break;
        }
    }

    public int getPrimMirror() {
        return prim_mirror;
    }

    public String getLessonId() {
        if (lesson_id != null)
            return lesson_id;
        return "#" + nid;
    }

    public String getLessonIdAlt() {
        if (lesson_id != null)
            return lesson_id;
        return "";
    }

    public void setLessonId(String lid) {
        this.lesson_id = lid;
    }

    AffineTransform cur_at;

    public AffineTransform getAffineTransform() {
        return cur_at;
    }

    public AffineTransform getAffineTransformAtTime(int dt,
                                                    TimeLine tl,
                                                    Rectangle2D bounding_rect) {
        try {
            rot = rotAt(dt);
            scale = scaleAt(dt);
            scale *= prim_scale;

            Path pa = apa.get(nid);

            int len = (int) pa.getLength();
            int dur = (int) tl.getDuration();
            int offs = (int) tl.getOffset();

            if (pa != null) {

                double da[] = pa.getPathLength_TSyncSegments();
                double dat[] = tl.getTimeMarker_TSyncSegments();

                double where = 0.0;

                int ii = -1;
                for (int i = 0; i < da.length; i++) {
                    if (dat[i] < dt)
                        ii = i;
                }
                Point2D po;

                if (ii == -1) {
                    po = pa.getPointAt(0);
                    //OmegaContext.sout_log.getLogger().info("ERR: " + "po1: " + po);
                } else if (ii == da.length - 1) {
                    po = pa.getPointAt(da[ii]);
                    //OmegaContext.sout_log.getLogger().info("ERR: " + "po2: " + po);
                    if (len == 0)
                        where = da[ii] / 1;
                    else
                        where = da[ii] / len;
                    //OmegaContext.sout_log.getLogger().info("ERR: " + "wh2: " + where + ' ' + da[ii] + ' ' + len);
                } else {
                    int len2 = (int) da[ii];
                    double difft = dt - dat[ii];
                    double fact = 0.1;
                    if ((dat[ii + 1] - dat[ii]) != 0)
                        fact = difft / (dat[ii + 1] - dat[ii]);
                    double dlen = da[ii + 1] - da[ii];
                    len2 += fact * dlen;
                    //OmegaContext.sout_log.getLogger().info("ERR: " + "da3: " + da[ii+1] + ' ' + da[ii] + ' ' + ii);
                    //OmegaContext.sout_log.getLogger().info("ERR: " + "f3: " + fact + ' ' + dlen);
                    po = pa.getPointAt(len2);
                    //OmegaContext.sout_log.getLogger().info("ERR: " + "po3: " + po);
                    if (len == 0)
                        where = (double) len2 / 1;
                    else
                        where = (double) len2 / len;
                    //OmegaContext.sout_log.getLogger().info("ERR: " + "wh3: " + where + ' ' + len2 + ' ' + len);
                }

                //OmegaContext.sout_log.getLogger().info("ERR: " + "-------- where " + where);

                double hotsp_x = hotspot.getX(where);
                if (prim_mirror_x ^ mirror_x) {
                    hotsp_x = 1.0 - hotsp_x;
                }

                double hotsp_y = hotspot.getY(where);
                if (prim_mirror_y ^ mirror_y) {
                    hotsp_y = 1.0 - hotsp_y;
                }

                imw = getWidth();
                imh = getHeight();

                at.setToIdentity();
                at.translate(scale * -hotsp_x * imw, scale * -hotsp_y * imh);
                at.translate(po.getX(), po.getY());

                at.rotate(rot,
                        scale * hotspot.getX(0) * imw,
                        scale * hotspot.getY(0) * imh);

                if (prim_mirror_x ^ mirror_x) {
                    at.translate(scale * imw, 0);
                    at.scale(-1, 1);
                }
                if (prim_mirror_y ^ mirror_y) {
                    at.translate(0, scale * imh);
                    at.scale(1, -1);
                }

                at.scale(scale, scale);

                Rectangle2D rT = new Rectangle2D.Double(0, 0,
                        imw, imh);
                Shape sh = at.createTransformedShape(rT);
                if (bounding_rect != null) {
                    bounding_rect.setRect(sh.getBounds2D());
//		    bounding_rect.setRect(expand(sh.getBounds2D()));
                    this.bounding_rect.setRect(bounding_rect);
                }
                cur_at = at;
                return at;
            }
        } catch (NullPointerException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "GImAE.getAffineTransformAtTime(): " + ex);
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "GImAE.getAffineTransformAtTime(): " + ex);
            ex.printStackTrace();
            return null;
        }
        return null;
    }

    Rectangle2D expand(Rectangle2D r) {
        return new Rectangle2D.Double(r.getX(),
                r.getY(),
                r.getWidth() + 2,
                r.getHeight() + 2);
    }

    public AffineTransform getAffineTransformAtTime(int dt,
                                                    TimeLine[] tlA,
                                                    Rectangle2D bounding_rect) {
        if (nid < tlA.length) {
            TimeLine tl = tlA[nid];
            if (tl != null) {
                return getAffineTransformAtTime(dt, tl, bounding_rect);
            }
        }
        return null;
    }

    public synchronized void render(AffineTransform at) {
        if (acomp == null)
            return;

        if ((mask == null || xim.getImage(ca) != current_mask_image) && can_eat) {
            mask = this;
            mask.buffered_image = new BufferedImage(getWidth(),
                    getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D gb = (Graphics2D) mask.buffered_image.getGraphics();
            AffineTransform at2 = new AffineTransform();
            gb.drawImage(current_mask_image = xim.getImage(ca), at2, null);
        }
        if (buffered_image == null && can_bee_eaten) {
            buffered_image = new BufferedImage(getWidth(),
                    getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D gb = (Graphics2D) buffered_image.getGraphics();
            gb.drawImage(xim.getImage(ca), 0, 0, null);
        }
        if (mask != null && mask != this && can_bee_eaten) {
            try {
                AffineTransform iat = getAffineTransform().createInverse();
                iat.concatenate(mask.getAffineTransform());
                Graphics2D gb = (Graphics2D) buffered_image.getGraphics();
                gb.setComposite(AlphaComposite.DstOut);
                gb.drawImage(mask.buffered_image, iat, null);
            } catch (NoninvertibleTransformException ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "AFFTRAINV " + ex);
            }
        }
        if (buffered_image != null)
            ca.drawImage(buffered_image, at, acomp);
        else
            ca.drawImage(xim.getImage(ca), at, acomp);
    }

    public String toString() {
        return "GImAE{fid=" + fid +
                ",lesson_id=" + lesson_id +
                ",var=" + SundryUtils.a2s(variable) +
                ",nid=" + nid +
                ",im=" + xim.getImage(null) +
                ",layer=" + layer +
                ",reset_sequence=" + reset_sequence +
                ",prim_scale=" + prim_scale +
                ",prim_mirror=" + prim_mirror +
                '}';
    }
}
