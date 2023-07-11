package com.femtioprocent.omega.anim.canvas;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.appl.AnimEditor;
import com.femtioprocent.omega.anim.appl.AnimRuntime;
import com.femtioprocent.omega.anim.cabaret.Actor;
import com.femtioprocent.omega.anim.cabaret.Cabaret;
import com.femtioprocent.omega.anim.cabaret.GImAE;
import com.femtioprocent.omega.anim.cabaret.Hotspot;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.anim.panels.cabaret.CabaretPanel;
import com.femtioprocent.omega.anim.panels.path.PathProperties;
import com.femtioprocent.omega.anim.tool.path.AllPath;
import com.femtioprocent.omega.anim.tool.path.Path;
import com.femtioprocent.omega.anim.tool.path.Probe;
import com.femtioprocent.omega.anim.tool.timeline.TimeLine;
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker;
import com.femtioprocent.omega.graphic.render.Wing;
import com.femtioprocent.omega.swing.Popup;
import com.femtioprocent.omega.swing.ToolExecute;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.GenericEvent;
import com.femtioprocent.omega.util.GenericEventListener;
import com.femtioprocent.omega.util.GenericEventManager;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Stack;


public class AnimCanvas extends com.femtioprocent.omega.graphic.render.Canvas {
    public AllPath ap = new AllPath();
    Mouse m;
    Key key;
    Probe selected_prb = null;
    Path.Mark selected_mark = null;
    Point2D press_p = null;

    double sca = 1.0;
    double offs_x = 10000;
    double offs_y = 10000;
    double offs_w = 0;
    double offs_h = 0;

    public AnimContext a_ctxt;

    public GEL gel;
    GenericEventManager gem;

    AnimEditor ae;
    AnimRuntime arun;

    private int pa_offs = 10;

    String lesson_verb = "jagar";

    public Cabaret cab;

    Actor actA_animated[] = new Actor[OmegaConfig.TIMELINES_N];

    public Color background_color = new Color(30, 30, 90);

    public HashMap colors = new HashMap();

    public boolean hidden = false;

    public String getLessonVerb() {
        return lesson_verb;
    }

    public void setLessonVerb(String s) {
        lesson_verb = s;
    }

    public double getOffsX() {
        if (true || ae == null) // why?
            return offs_x;
        return 0;
    }

    public double getOffsY() {
        if (true || ae == null)
            return offs_y;
        return 0;
    }

    public Point scaleEventPos(MouseEvent ev) {
        Point p = new Point((int) ((ev.getX() - offs_x) / sca),
                (int) ((ev.getY() - offs_y) / sca));
        return p;
    }

    public void setHidden(boolean b) {
        hidden = b;
        repaint();
    }

    public MouseInputAdapter hook = null;

    class Mouse extends MouseInputAdapter {
        AnimCanvas anim_canvas;

        boolean was_shift = false;
        private final int M_TOOL_PATH = 0;
        private final int M_TOOL_IMAGE = 1;
        private final int M_TOOL_MARKER = 2;
        private final int MT_VOID = 0;
        private final int MT_EXTEND = 200;
        int m_tool = M_TOOL_PATH;
        int m_tool_sub = MT_VOID;
        Stack stack;

        Point2D mpress_p;

        Mouse(AnimCanvas anim_canvas) {
            this.anim_canvas = anim_canvas;
            addMouseListener(this);
            addMouseMotionListener(this);

            centerBackground();

            stack = new Stack();
        }

        void setM_Tool(int mt) {
            setM_Tool(mt, m_tool_sub);
       }

        void setM_Tool(int mt, int mts) {
            m_tool = mt;
            m_tool_sub = mts;
            updCursor();
        }

        public void mousePressed(MouseEvent e) {
            if (hook != null) {
                hook.mousePressed(e);
                return;
            }
//	OmegaContext.sout_log.getLogger().info("ERR: " + "m p " + e.getX() + ',' + e.getY());
            if (getVisibilityMode(HIDE_PATH)) {
                hideActors();
                repaint();
            }
            setVisibilityMode(SHOW_PATH);

            mpress_p = new Point2D.Double(e.getX(), e.getY());

            switch (m_tool) {
                case M_TOOL_IMAGE:
                case M_TOOL_PATH:
                case M_TOOL_MARKER:
                    switch (m_tool_sub) {
                        case MT_EXTEND:
                            if (selected_prb == null) {
                                break;
                            }

                            if (!e.isShiftDown() && was_shift) {
                                setM_Tool(m_tool, MT_VOID);
                                was_shift = false;
                            } else {

                                Point2D p_p = new Point2D.Double((e.getX() - offs_x) / sca, (e.getY() - offs_y) / sca);

                                selected_prb.seg.path.extendSegment(p_p);
                                // extendTimeLineAsWell();
                                repaint();

                                if (e.isShiftDown()) {
                                    setM_Tool(M_TOOL_MARKER, m.MT_EXTEND);
                                    stack = new Stack();
                                    was_shift = true;
                                } else {
                                    setM_Tool(m_tool, m.MT_VOID);
                                    was_shift = false;
                                }
                                Probe prb = ap.findNearest(p_p);
                                prb.seg.path.setSelected(true);
//  				if ( pa_prop != null )
//  				    pa_prop.setObject(prb.seg.path);
                                prb.seg.selectedPoint = prb.sel;
                                prb.seg.path.draw(getGraphics2D());

                                ae.selectTimeLine(prb.seg.path);
                            }
                            break;

                        default:
                        case 0:
                            press_p = new Point2D.Double((e.getX() - offs_x) / sca, (e.getY() - offs_y) / sca);

                            boolean pt = e.isPopupTrigger();

                            if (false) { // omega 2
                                popup_maction(e, Path.global_selected);
                            } else {
                                if (pt) { // omega 2
                                    popup_maction(e, Path.global_selected);
                                }
                                ap.deselectAll(getGraphics2D());
                                if (e.isControlDown()) {
                                    Path.Mark mk = ap.findNearestMarker(press_p);
                                    if (OmegaConfig.T)
                                        OmegaContext.sout_log.getLogger().info("ERR: " + "marker hit " + mk);
                                    if (mk != null) {
                                        Path pa = mk.pa;
                                        pa.setSelected(true);
//  					if ( pa_prop != null )
//  					    pa_prop.setObject(pa);
                                        pa.draw(getGraphics2D());
                                        ae.selectTimeLine(pa);
                                        selected_prb = null;
                                        selected_mark = mk;
                                        setM_Tool(M_TOOL_MARKER);
                                    }
                                } else {
                                    Probe prb = ap.findNearest(press_p);
                                    if (prb != null && prb.dist > 20) {
                                        prb = null;
                                        ae.toolbar_cmd.enable_path(0);
                                        ae.selectTimeLine();
                                    }
                                    if (prb != null) {
                                        prb.seg.path.setSelected(true);
//  					if ( pa_prop != null )
//  					    pa_prop.setObject(prb.seg.path);
                                        prb.seg.selectedPoint = prb.sel;
                                        prb.seg.path.draw(getGraphics2D());
                                        if (prb.seg == prb.seg.path.getSq(0) ||
                                                prb.seg == prb.seg.path.getSq(prb.seg.path.getSqN() - 1))
                                            ae.toolbar_cmd.enable_path(1);
                                        else
                                            ae.toolbar_cmd.enable_path(2);
                                        ae.selectTimeLine(prb.seg.path);
                                        setM_Tool(M_TOOL_PATH);
                                    } else {
                                        setM_Tool(M_TOOL_IMAGE);
                                    }
                                    selected_prb = prb;
                                }
                            }
                            break;
                    }
                    break;
            }
            if (pa_prop != null)
                pa_prop.setObject(Path.global_selected);
        }

        public void mouseMoved(MouseEvent e) {
            if (hook != null) {
                hook.mouseMoved(e);
                return;
            }
            if (e.isShiftDown())
                ;
            else if (e.isControlDown()) {
                Point2D mv_p = new Point2D.Double((e.getX() - offs_x) / sca, (e.getY() - offs_y) / sca);
                Path.Mark mk = ap.findNearestMarker(mv_p);
                if (mk != null) {
                    Path pa = mk.pa;
                    pa.draw(getGraphics2D());
                    setM_Tool(M_TOOL_MARKER);
                }
            } else {
                Point2D mv_p = new Point2D.Double((e.getX() - offs_x) / sca, (e.getY() - offs_y) / sca);
                Probe prb = ap.findNearest(mv_p);
                if (prb != null && prb.dist > 20) {
                    setM_Tool(M_TOOL_IMAGE);
                } else {
                    setM_Tool(M_TOOL_PATH);
                }
            }
            updCursor();
        }

        public void mouseDragged(MouseEvent e) {
            if (hook != null) {
                hook.mouseDragged(e);
                return;
            }
//	OmegaContext.sout_log.getLogger().info("ERR: " + "m d " + e.getX() + ',' + e.getY());
            switch (m_tool) {
                case M_TOOL_IMAGE:
                    Point2D drag2_p = new Point2D.Double(e.getX(), e.getY());
                    offs_w += drag2_p.getX() - mpress_p.getX();
                    offs_h += drag2_p.getY() - mpress_p.getY();
                    mpress_p = drag2_p;

                    centerBackground();
                    repaint();
                    break;
                case M_TOOL_MARKER:
                    Path.Mark mk = selected_mark;
                    if (mk != null) {
                        Point2D drag3_p = new Point2D.Double((e.getX() - offs_x) / sca, (e.getY() - offs_y) / sca);
                        double where = mk.pa.findNearestPoint(drag3_p);
                        mk.moveToPathPosition(where);
                        ae.setDirty(true);
                        repaint();
                    }
                    break;
                case M_TOOL_PATH:
                    switch (m_tool_sub) {
                        case MT_VOID:
                            Point2D drag_p = new Point2D.Double((e.getX() - offs_x) / sca, (e.getY() - offs_y) / sca);
                            if (e.isShiftDown()) {
                                if (selected_prb != null) {
                                    Point2D drag3_p = new Point2D.Double(e.getX(), e.getY());
                                    double dx = drag3_p.getX() - mpress_p.getX();
                                    double dy = drag3_p.getY() - mpress_p.getY();
                                    mpress_p = drag3_p;
                                    selected_prb.pa.moveAll(dx / sca, dy / sca);
                                    ae.setDirty(true);
                                }
                            } else {
                                if (selected_prb != null) {
                                    selected_prb.seg.moveto(selected_prb.sel, drag_p);
                                    selected_prb.seg.path.updateInternal();
                                    ae.setDirty(true);
                                }
                            }
                            repaint();
                            break;
                    }
                    break;
            }
            if (pa_prop != null)
                pa_prop.setObject(Path.global_selected);
        }

        public void mouseReleased(MouseEvent e) {
            if (hook != null) {
                hook.mouseReleased(e);
                return;
            }
        }

        public void updCursor() {
            switch (m_tool) {
                case M_TOOL_IMAGE:
                    if ( m_tool_sub == MT_EXTEND )
                        setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                    else
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    break;
                case M_TOOL_MARKER:
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    break;
                case M_TOOL_PATH:
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    break;
                default:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
            }
        }
    }

    class Mouse2 extends Mouse {
        Mouse2(AnimCanvas anim_canvas) {
            super(anim_canvas);
        }

        public void mousePressed(MouseEvent e) {
            if (big_button_text != null)
                big_button_text = null;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }
    }

    public PathProperties pa_prop;

    protected void processKeyEvent(KeyEvent ke) {
        super.processKeyEvent(ke);
        OmegaContext.sout_log.getLogger().info("ERR: " + "AnimCanvas:KEYEVENT " + ke);
        if (ke.getID() == ke.KEY_PRESSED) {
            if (ke.getKeyCode() == ke.VK_SPACE) {
                if (big_button_text != null)
                    big_button_text = null;
                OmegaContext.sout_log.getLogger().info("ERR: " + "SPACE");
            }
            if (ke.getKeyCode() == ke.VK_ENTER) {
                if (big_button_text != null)
                    big_button_text = null;
                OmegaContext.sout_log.getLogger().info("ERR: " + "ENTER");
            }
            if (ke.getKeyCode() == ke.VK_LEFT) {
                if (trigger_left) {
                    if (big_button_text != null)
                        big_button_text = null;
                    OmegaContext.sout_log.getLogger().info("ERR: " + "LEFT");
                }
                trigger_left = false;
            }
            if (ke.getKeyCode() == ke.VK_UP) {
                if (trigger_up)
                    OmegaContext.sout_log.getLogger().info("ERR: " + "UP");
                trigger_up = false;
            }
        }
    }

    void popup_maction(MouseEvent e, final Path pa) {
        final String[] choice = {"path Properties", "", "Cancel"};

        com.femtioprocent.omega.swing.Popup pop = new Popup(this);
        pop.popup("Marker", choice, e.getX(), e.getY(), new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (ev.getActionCommand().equals("0")) {
                    JFrame owner = (JFrame) getTopLevelAncestor();
                    if (pa_prop == null)
                        pa_prop = new PathProperties(owner);
                    pa_prop.setObject(pa);
                    pa_prop.setVisible(true);
                }
            }
        });
    }

    double getFit() {
        Image im = getImageBackground();
        if (im == null)
            return 1.0;

        int w = im.getWidth(null);
        int h = im.getHeight(null);

        int cw = getWidth();
        int ch = getHeight();

        double fx = (double) w / cw;
        double fy = (double) h / ch;

        return Math.max(fx, fy);
    }

    public void centerBackground() {
        Image im = getImageBackground();
//	OmegaContext.sout_log.getLogger().info("ERR: " + "centerBackground " + im);
        if (im == null)
            return;

        int w = im.getWidth(null);
        int h = im.getHeight(null);

        int cw = getWidth();
        int ch = getHeight();

        double ww = sca * w;
        double hh = sca * h;

//	OmegaContext.sout_log.getLogger().info("ERR: " + "CALC " + cw + ' ' + w + ' ' + ww);
//	OmegaContext.sout_log.getLogger().info("ERR: " + "CALC " + ch + ' ' + h + ' ' + hh);

        if (cw == 0 && ch == 0) {
            cw = (int) ww;
            ch = (int) hh;
        }

        offs_x = (int) ((cw - ww) / 2);
        offs_y = (int) ((ch - hh) / 2);

        offs_x += offs_w;
        offs_y += offs_h;
    }

    public void offCenterBackground() {
        Image im = getImageBackground();
//	OmegaContext.sout_log.getLogger().info("ERR: " + "offCenterBackground " + im);
        if (im == null)
            return;

        offs_x = 10000;
        offs_y = 10000;
        repaint();
    }

    public void createNewPath() {
        int nid = a_ctxt.mtl.getFreeTLIndex();
        if (nid == -1) {
            return;
        }
        Point2D p_p;

        Path pa = new Path(nid,
                p_p = new Point2D.Double(10.0, nid * 5 + 10.0),
                new Point2D.Double(300.0, nid * 5 + 10.0));
        ap.add(pa);

        ap.deselectAll(getGraphics2D());
        Probe prb = ap.findNearest(p_p);
        prb.seg.path.setSelected(true);
//  				if ( pa_prop != null )
//  				    pa_prop.setObject(prb.seg.path);
        prb.seg.selectedPoint = prb.sel;
        prb.seg.path.draw(getGraphics2D());

        ae.selectTimeLine(prb.seg.path);

        if (prb.seg == prb.seg.path.getSq(0) ||
                prb.seg == prb.seg.path.getSq(prb.seg.path.getSqN() - 1))
            ae.toolbar_cmd.enable_path(1);
        else
            ae.toolbar_cmd.enable_path(2);

        selected_prb = prb;

        repaint();
        int len = (int) pa.getLength();

        TimeLine tl = new TimeLine(nid, 200, 5 * len);
        tl.addMarker(TimeMarker.BEGIN, -tl.getOffset() + 1);
        tl.addMarker(TimeMarker.END, ae.tlp.getPlayEnd() - tl.getOffset());
        a_ctxt.mtl.addTimeLine(tl);
        ae.tlc.repaint();

        Actor act = a_ctxt.ae.cabaret_panel.getActorInPanel(nid);
        allgim.set(act.gimae, nid);

        ae.setDirty(true);
    }

    public boolean isCanvasNormal() {
        return
                offs_w == 0 &&
                        offs_h == 0 &&
//  	    offs_x == 0 &&
//  	    offs_y == 0 &&
                        sca == 1.0;
    }

    class GEL implements GenericEventListener, ToolExecute {

        public void execute(String id) {
            perform(id, 1.0);
        }

        public void genericEvent(GenericEvent gev, Object a) {
            double d = ((Double) a).doubleValue();
            perform(gev.id, d);
        }

        void perform(String cmd, double d) {
            if (cmd.equals("left")) {
                if (sca > 1.0)
                    offs_w -= d * 100;
                else
                    offs_w -= sca * d * 100;
                centerBackground();
                repaint();
            } else if (cmd.equals("right")) {
                if (sca > 1.0)
                    offs_w += d * 100;
                else
                    offs_w += sca * d * 100;
                centerBackground();
                repaint();
            } else if (cmd.equals("up")) {
                if (sca > 1.0)
                    offs_h -= d * 100;
                else
                    offs_h -= sca * d * 100;
                centerBackground();
                repaint();
            } else if (cmd.equals("down")) {
                if (sca > 1.0)
                    offs_h += d * 100;
                else
                    offs_h += sca * d * 100;
                centerBackground();
                repaint();
            } else if (cmd.equals("upper_left")) {
                offs_w = 0;
                offs_h = 0;
//		offs_x = offs_y = 0;
                sca = 1.0;
                centerBackground();
                repaint();
            } else if (cmd.equals("fit")) {
                offs_w = 0;
                offs_h = 0;
                sca = 1.0;
                double fd = getFit() * 1.1;
                sca /= d * fd;
                offs_w /= d * fd;
                offs_h /= d * fd;
                centerBackground();
                repaint();
            } else if (cmd.equals("bigger")) {
                sca *= d * 1.41;
                offs_w *= d * 1.41;
                offs_h *= d * 1.41;
                centerBackground();
                repaint();
            } else if (cmd.equals("smaller")) {
                sca /= d * 1.41;
                offs_w /= d * 1.41;
                offs_h /= d * 1.41;
                centerBackground();
                repaint();
            } else if (cmd.equals("path_tool")) {
                m.setM_Tool(m.M_TOOL_PATH);
                repaint();
            } else if (cmd.equals("im_tool")) {
                m.setM_Tool(m.M_TOOL_IMAGE);
                repaint();
            }

            if (cmd.equals("hideActor")) {
                repaint();
            }

            if (cmd.equals("select_path")) {
                m.setM_Tool(m.M_TOOL_PATH);
                repaint();
            } else if (cmd.equals("select_image")) {
                m.setM_Tool(m.M_TOOL_IMAGE);
                repaint();
            }


            if (cmd.equals("path_create")) {
                if (a_ctxt.mtl.getFreeTLIndex() == -1) {
                    JOptionPane.showMessageDialog(AnimCanvas.this,
                            T.t("Can't create path, max is " + OmegaConfig.TIMELINES_N),
                            "Omega",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    createNewPath();
                }
            } else if (cmd.equals("path_duplicate")) {
                if (a_ctxt.mtl.getFreeTLIndex() == -1) {
                    JOptionPane.showMessageDialog(AnimCanvas.this,
                            T.t("Can't create path, max is " + OmegaConfig.TIMELINES_N),
                            "Omega",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    m.setM_Tool(m.M_TOOL_IMAGE, m.MT_VOID);

                    if (selected_prb != null) {
                        int nid = a_ctxt.mtl.getFreeTLIndex();
                        if (nid == -1) {
                            return;
                        }
                        Path pa_src = selected_prb.seg.path;
                        Path pa_new = new Path(nid, pa_src, pa_offs, pa_offs);
                        pa_offs += 10;
                        pa_offs %= 50;
                        if (pa_offs == 0)
                            pa_offs = 10;

                        ap.add(pa_new);

                        repaint();

                        int src_nid = selected_prb.seg.path.nid;
//			OmegaContext.sout_log.getLogger().info("ERR: " + "src " + src_nid + ' ' + nid);
                        TimeLine tl_src = a_ctxt.mtl.getTimeLine(src_nid);
                        TimeLine tl = new TimeLine(nid, tl_src);
                        a_ctxt.mtl.addTimeLine(tl);
                        ae.tlc.repaint();
                        ae.setDirty(true);
                    }
                }
            } else if (cmd.equals("path_delete_all")) {
                m.setM_Tool(m.M_TOOL_IMAGE, m.MT_VOID);

                if (selected_prb != null) {
                    Path pa_src = selected_prb.seg.path;
                    int src_nid = pa_src.nid;

                    deleteAllNid(src_nid);
                    ae.setDirty(true);
                }
            } else if (cmd.equals("path_extend")) {
                if (selected_prb != null) {
                    m.setM_Tool(m.M_TOOL_PATH, m.MT_EXTEND);
                    m.stack = new Stack();
                    ae.setDirty(true);
                }
            } else if (cmd.equals("path_split")) {
                if (selected_prb != null) {
                    selected_prb.seg.path.splitSegment();
                    ae.setDirty(true);
                    repaint();
                }
            } else if (cmd.equals("path_delete")) {
                if (selected_prb != null) {
                    selected_prb.seg.path.removeSegment();
                    ae.setDirty(true);
                    repaint();
                }
            }
        }
    }

    public void setSelectedPath(int tl_nid, Path pa) {
        if (pa_prop != null)
            pa_prop.setObject(pa);
        pa.setSelected(true);
        Probe prb = ap.findNearest(pa.getFirstPoint());
        selected_prb = prb;
        repaint();
    }

    public AnimCanvas(AnimRuntime arun, AnimContext a_ctxt) {
        this.arun = arun;
        this.ae = null;
        this.a_ctxt = a_ctxt;
        a_ctxt.anim_canvas = this;
        cab = new Cabaret(a_ctxt);
        init();
    }

    public AnimCanvas(AnimEditor ae, AnimContext a_ctxt) {
        this.arun = null;
        this.ae = ae;
        this.a_ctxt = a_ctxt;
        a_ctxt.anim_canvas = this;
        setLayout(null);
        gel = new GEL();
        gem = new GenericEventManager();
        cab = new Cabaret(a_ctxt);
        init();
//	OmegaContext.sout_log.getLogger().info("ERR: " + "AnimCanvas(ae) created " + this);
    }

    void init() {
        initColors();

        addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "AnimCanvas:KeyAd " + e);
            }
        });
        if (ae == null) {
            m = new Mouse2(this);
            offs_w = 0;
            offs_h = 0;
            offs_x = 0;
            offs_y = 0;
            sca = 1.0;
            repaint();
        }
        if (ae != null) {
            m = new Mouse(this);
            key = new Key(this);
            gem.addGenericEventListener(gel);

            a_ctxt.ae.cabaret_panel.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ev) {
                    CabaretPanel cabp = (CabaretPanel) ev.getSource();
                    for (int i = 0; i < OmegaConfig.TIMELINES_N; i++) {
                        Actor act = cabp.getActorInPanel(i);
                        GImAE gim = null;
                        if (act != null)
                            gim = act.gimae;
                        allgim.set(gim, i);
                        // gim.nid = i;
                    }
                }
            });
            createDefaultActors();
        }
    }

    public ToolExecute getToolExecute() {
        return gel;
    }

    private void initBG() {
        setBackground("default/omega_splash.gif");
        centerBackground();
        repaint();
    }

    public void initPlay() {
        super.initPlay(ap);
        trigger_up = true;
        trigger_left = true;
    }

    public String[] getLessonId_Actors() {
        return cab.getLessonId();
    }

    GImAE findActorByLessonId(String s) {
        return cab.findActorByLessonId(s);
    }

    Actor findActorByNId(int nid) {
        return cab.getActor(nid);
    }

    int findTimeLineNidByLessonId(String s) {
        int nid = a_ctxt.mtl.getNid(s);
        return nid;
    }

    public void deleteAllNid(int nid) {
        if (JOptionPane.showConfirmDialog(a_ctxt.ae,
                T.t("Delete whole path and timeline no") + ' ' +
                        (nid + 1) + "?",
                "Omega",
                JOptionPane.YES_NO_OPTION) == 0) {
            allgim.set(null, nid);
            ap.removePath(nid);
            selected_prb = null;
            repaint();
            a_ctxt.mtl.removeTimeLine(nid);
            ae.tlc.repaint();
        }
    }

    public void bindAllNoActor() {
        for (int i = 0; i < OmegaConfig.TIMELINES_N; i++) {
            TimeLine tl = a_ctxt.mtl.getTimeLine(i);
            if (tl != null) {
                String lid = tl.getLessonId();
                if (lid != null && lid.length() > 0) {
                    bindNoActorOnTL(tl.nid);
                }
            }
        }
    }

    public boolean bindActor(String actor_lid, String timeline_lid) {
        GImAE gae = null;
        if (actor_lid != null)
            gae = findActorByLessonId(actor_lid);
        int tl_nid = findTimeLineNidByLessonId(timeline_lid);
        if (tl_nid != -1) {
            bindActorOnTL(tl_nid, gae);
            return true;
        }
        return false;
    }

    public void bindAllStatistActor() {
        for (int i = 0; i < OmegaConfig.TIMELINES_N; i++) {
            TimeLine tl = a_ctxt.mtl.getTimeLine(i);
            if (tl != null) {
                String lid = tl.getLessonId();
                if (lid != null && lid.length() == 0) {
                    Actor act = findActorByNId(i);
                    if (act != null)
                        bindActorOnTL(tl.nid, act.gimae);
//		    OmegaContext.sout_log.getLogger().info("ERR: " + "--- statist actor " + tl.nid + ' ' + act);
                }
            }
        }
    }

    public void deleteActor(int ix) {
        int nid;

        if (a_ctxt.ae != null)
            nid = a_ctxt.ae.cabaret_panel.setActorInPanelAbs(null, ix);
        else
            nid = ix;

        if (nid >= 0 && nid < OmegaConfig.TIMELINES_N) {
            allgim.set(null, nid);
        }
    }

    public Actor loadActor(int ix, String fn) {
        Actor act = cab.createActor(ix, fn, null);

        int nid;
        if (a_ctxt.ae != null)
            nid = a_ctxt.ae.cabaret_panel.setActorInPanelAbs(act, ix);
        else
            nid = ix;

        if (nid >= 0 && nid < OmegaConfig.TIMELINES_N)
            allgim.set(act.gimae, nid);

        return act;
    }

    public Actor bindActorOnTL(int tl_nid, GImAE gimae) {
        if (gimae == null)
            return null;
        if (tl_nid < OmegaConfig.TIMELINES_N) {
            GImAE gim = new GImAE(this, gimae, tl_nid); // make a ghost
//	    OmegaContext.sout_log.getLogger().info("ERR: " + "bound actor " + tl_nid + ' ' + gim);
            Actor act = new Actor(a_ctxt, gim);
            actA_animated[tl_nid] = act;
            allgim.set(gim, tl_nid);
            return act;
        }
        return null;
    }

    public Actor bindNoActorOnTL(int tl_nid) {
        if (tl_nid < OmegaConfig.TIMELINES_N) {
//	    OmegaContext.sout_log.getLogger().info("ERR: " + "bound no actor " + tl_nid);
            actA_animated[tl_nid] = null;
            allgim.set(null, tl_nid);
            return null;
        }
        return null;
    }

    public Actor getActor(int nid) {
        if (a_ctxt.ae != null)
            return a_ctxt.ae.cabaret_panel.getActorInPanel(nid);
        else
            return cab.getActor(nid);
    }

    public Actor getAnimatedActor(int nid) {
        Actor a = null;
        if (a_ctxt.ae != null)
            a = a_ctxt.ae.cabaret_panel.getActorInPanel(nid);
        else
            a = actA_animated[nid];
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "getAnimatedActor -> " + nid + ' ' + a + ' ' + SundryUtils.a2s(actA_animated));
        return a;
    }

    public Actor getAnimatedActor(String sid) {
        int nid = findTimeLineNidByLessonId(sid);
//log	OmegaContext.sout_log.getLogger().info("ERR: " + ">>>>>>>> getting animact " + sid + ' ' + nid);
        return getAnimatedActor(nid);
    }

    public void hideActors() {
        allgim.hideActors();
    }

    private void createDefaultActors() {
        if (a_ctxt.ae != null) {
            a_ctxt.ae.cabaret_panel.resetCabaretOrder();
            if (a_ctxt.ae != null) {
                for (int i = cab.actorNum() - 1; i >= 0; i--)
                    loadActor(i, "default/default_actor_" + i + ".gif");
            } else {
            }
        }
    }


    public static final int SHOW_PATH = 0x1011;
    public static final int HIDE_PATH = 0x1010;
    public static final int SHOW_ACTOR = 0x1021;
    public static final int HIDE_ACTOR = 0x1020;

    int visibilityMode = 0x30;

    public void setVisibilityMode(int cmd) {
        switch (cmd) {
            case SHOW_PATH:
                visibilityMode |= SHOW_PATH & 0xf0;
                break;
            case HIDE_PATH:
                visibilityMode &= ~(SHOW_PATH & 0xf0);
                break;
            case SHOW_ACTOR:
                visibilityMode |= SHOW_ACTOR & 0xf0;
                break;
            case HIDE_ACTOR:
                visibilityMode &= ~(SHOW_ACTOR & 0xf0);
                break;
        }
    }

    public boolean getVisibilityMode(int cmd) {
        if ((cmd & 1) == 1)
            return (visibilityMode & cmd & 0xf0) != 0;
        else
            return (visibilityMode & cmd & 0xf0) == 0;
    }

    public Graphics2D getGraphics2D() {
        Graphics2D g2 = (Graphics2D) getGraphics();
        AffineTransform at = g2.getTransform();
        if (m != null) {
            at.translate(offs_x, offs_y);
            at.scale(sca, sca);
        }
        g2.setTransform(at);
        return g2;
    }

    String big_button_text;
    boolean trigger_up = false;
    boolean trigger_left = false;

    public void setBigButtonText(String s) {
        if (s == null)
            return;
        if (s.length() == 0)
            big_button_text = null;
        else {
            big_button_text = s;
            trigger_up = true;
            trigger_left = true;
        }
        repaint();
    }

    static public class MsgItem2 {
        public String title;
        public String text;

        public MsgItem2(String title, String txt) {
            this.title = title;
            this.text = txt;
        }
    }

    int getCaW() {
        return getWidth();
    }

    int getCaH() {
        return getHeight();
    }


    int gX(double f) {
        return (int) (f * getCaW());
    }

    int gY(double f) {
        return (int) (f * getCaH());
    }

    Font getTitleFont() {
        int h = (int) (gX(0.024));
        return new Font("Arial", Font.PLAIN, h);
    }

    int getStringWidth(Graphics2D g2, Font fo, String s) {
        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = fo.getStringBounds(s, frc);
        return (int) r.getWidth();
    }

    private int limit(int a) {
        if (a > 255) a = 255;
        if (a < 0) a = 0;
        return a;
    }

    Color moreSaturate(Color col) {
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int gray = (r + g + b) / 3;
        r = (int) ((r - gray) * 1.3 + gray);
        g = (int) ((g - gray) * 1.3 + gray);
        b = (int) ((b - gray) * 1.3 + gray);
        return new Color(limit(r), limit(g), limit(b));
    }

    void initColors() {
        colors.put("bg_t", new Color(240, 220, 140));
        colors.put("bg_m", new Color(210, 180, 220));
        colors.put("bg_b", new Color(140, 220, 240));
        colors.put("bg_tx", new Color(0, 0, 0));
        colors.put("bg_fr", new Color(0, 0, 0));
        colors.put("bg_frbg", new Color(240, 220, 140));

        colors.put("sn_bg", new Color(240, 220, 140));
        colors.put("sn_hi", moreSaturate(new Color(240, 220, 140)));
        colors.put("sn_fr", new Color(0, 0, 0));
        colors.put("sn_tx", new Color(0, 0, 0));

        colors.put("bt_bg", new Color(0, 0, 0));
        colors.put("bt_hi", moreSaturate(new Color(240, 220, 140)));
        colors.put("bt_hs", new Color(255, 240, 180));
        colors.put("bt_fr", new Color(0, 0, 0));
        colors.put("bt_tx", new Color(0, 0, 0));
        colors.put("bt_fr_hi", new Color(0, 0, 0));
        colors.put("bt_tx_hi", new Color(0, 0, 0));
        colors.put("bt_fr_hs", new Color(0, 0, 0));
        colors.put("bt_tx_hs", new Color(0, 0, 0));
    }

    public Color getColor(String id, Color def) {
        Color col = (Color) colors.get(id);
        if (col == null)
            col = def;
        return col;
    }

    boolean show_msg;

    class MsgDialog {
        MsgItem2 msg_item;

        void show(MsgItem2 msg) {
            set(msg);
            while (show_msg)
                SundryUtils.m_sleep(200);
        }

        void set(MsgItem2 msg) {
            if (msg == null) {
                show_msg = false;
            } else {
                show_msg = true;
            }

            Graphics2D g2 = getGraphics2D();
            draw(g2);
            msg_item = msg;
            int w = gX(0.5);
            int h = gY(0.25);
            int x = gX(0.25);
            int y = gY(0.2);
            if (msg == null)
                repaint(x - 5, y - 5, w + 15, h + 15);
        }

        void draw(Graphics2D g2) {
            if (msg_item == null)
                return;

            int txtH = gY(0.037);
            Font fo = new Font("Arial", Font.PLAIN, txtH);
            int sw = getStringWidth(g2, fo, msg_item.text);

            long ct0 = SundryUtils.ct();
            while (sw > gX(0.94)) {
                txtH = (int) (txtH * 0.98);
                fo = new Font("Arial", Font.PLAIN, txtH);
                int o_sw = sw;
                sw = getStringWidth(g2, fo, msg_item.text);
                //		OmegaContext.sout_log.getLogger().info("ERR: " + "recalc sw " + o_sw + ' ' + sw + ' ' + txtH);
            }
            long ct1 = SundryUtils.ct();
            OmegaContext.sout_log.getLogger().info("ERR: " + "--> " + (ct1 - ct0));

            int w = sw + 10 + gX(0.03);
            int h = gY(0.06);
            int x = gX(0.5) - w / 2;
            int y = gY(0.88);
            int r = gX(0.02);
            Color col = getColor("sn_bg", new Color(0xe5, 0xe5, 0xe5));
            OmegaContext.COLOR_WARP = col;
            RoundRectangle2D fr = new RoundRectangle2D.Double(x, y, w, h, r, r);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.setColor(col);
            g2.fill(fr);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            if (false) {
                // titlebar
                int th = gY(0.026);
                g2.setColor(new Color(88, 88, 88));
                g2.setClip(fr);
                g2.fill(new Rectangle2D.Double(x, y, w, th));
            }


            g2.setClip(0, 0, 10000, 10000);//	    g2.setClip(fr);
            g2.setColor(getColor("sn_tx", Color.black));
            OmegaContext.COLOR_TEXT_WARP = getColor("sn_tx", Color.black);
            g2.setFont(fo);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.drawString(msg_item.text,
                    x + w / 2 - sw / 2,
                    y + h - (2 * txtH) / 5);

            g2.setColor(col);
            g2.setFont(getTitleFont());
            g2.drawString(msg_item.title, x + 1 * w / 10, (int) (y + gY(0.042)));

            Area a = new Area();
            a.add(new Area(new Rectangle2D.Double(0, 0, 10000, 10000)));
            a.subtract(new Area(fr));
            g2.setClip(a);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            g2.setColor(new Color(15, 15, 15));

            for (int i = 0; i < 7; i++) {
                RoundRectangle2D frs = new RoundRectangle2D.Double(x + 10 - i, y + 10 - i, w, h, r, r);
                g2.fill(frs);
            }

            BasicStroke stroke = new BasicStroke(getCaH() / 200f);
            g2.setStroke(stroke);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(getColor("sn_fr", new Color(15, 15, 15)));
            g2.setClip(0, 0, 10000, 10000);
            g2.draw(fr);

        }
    }

    MsgDialog msg_dlg = new MsgDialog();

    public void showMsg(MsgItem2 mi) {
        msg_dlg.set(mi);
    }

    public boolean isMsg() {
        return show_msg;
    }

    public void hideMsg() {
        msg_dlg.set(null);
    }


    public String waitBigButtonText(Runnable myra) {
        showMsg(new MsgItem2("", big_button_text));
        repaint();
        while (big_button_text != null) {
            SundryUtils.m_sleep(200);
            //	    SundryUtils.pe_("E");
            if ("up".equals(getEndCode())) {
                if (myra != null)
                    myra.run();
                myra = null;
            }
        }
        hideMsg();
        return getEndCode();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (HIDDEN) {
            g2.setColor(background_color);
            g2.fillRect(0, 0, 3000, 2000);
            return;
        }

        g2.setColor(background_color);
        g2.fillRect(0, 0, 3000, 2000);

        AffineTransform at0 = g2.getTransform();
        AffineTransform at = g2.getTransform();

//	OmegaContext.sout_log.getLogger().info("ERR: " + "trans " + offs_x + ' ' + offs_y);
        at.translate(offs_x, offs_y);
        at.scale(sca, sca);

        g2.setTransform(at);

        if (hidden)
            return;

        super.paintComponent(g);
        if (getVisibilityMode(SHOW_PATH)) {
            if (ae != null)
                ap.redraw(g2);
        }

        g2.setTransform(at0);
        if (false) if (big_button_text != null && big_button_text.length() > 0) {
            int ybot = getHeight() - 100;
            int h = 60;
            int x = 20;
            int w = getWidth() - x - x;
            g2.setColor(new Color(211, 211, 190));
            g2.fillRect(x, ybot, w, h);
            g2.setColor(new Color(0, 0, 0));
            g2.drawRect(x, ybot, w, h);
            g2.setFont(new Font("Arial", Font.PLAIN, (int) (24)));
            g2.drawString(big_button_text, x + 40, ybot + 25);
        }
//	g2.setTransform(at0);
        msg_dlg.draw(g2);
    }

    public void initNew() {
        ap = new AllPath();
        selected_prb = null;
        selected_mark = null;
        press_p = null;
        if (a_ctxt.ae != null) {
            initBG();
            createDefaultActors();
        }
        repaint();
    }

//      public void save(XML_PW xmlpw) {
//      Element mel = new Element("AnimCanvas");
//  	mel.add(super.getElement());
//  	xmlpw.push(mel);

//  	Element ael = ap.getElement();
//  	xmlpw.put(ael);

//  	Element verb_el = new Element("lesson");
//  	String verb = getLessonVerb();
//  	if ( verb != null && verb.length() > 0 ) {
//  	    verb_el.addAttr("verb", verb);
//  	    xmlpw.put(verb_el);
//  	}

//  	Element aacel = new Element("AllActors");
//  	for(int i = 0; i < cab.actorNum(); i++) {
//  	    Actor act = a_ctxt.ae.cabaret_panel.getActorInPanelAbs(i);
//  	    OmegaContext.sout_log.getLogger().info("ERR: " + "saving " + i + ' ' + act);
//  	    if ( act != null ) {
//  		Element acel = act.getElement();
//  		acel.addAttr("nid", "" + i);
//  		aacel.add(acel);
//  	    }
//  	}

//  	Element awel = new Element("AllWings");
//  	for(int i = 0; i < a_ctxt.ae.wings_panel.wingA.arr.length; i++) {
//  	    if ( a_ctxt.ae.wings_panel.getWing(i) != null ) {
//  		Element wel = a_ctxt.ae.wings_panel.getWing(i).getElement();
//  		wel.addAttr("nid", "" + i);
//  		awel.add(wel);
//  	    }
//  	}

//  	xmlpw.put(aacel);
//  	xmlpw.put(awel);
//  	xmlpw.pop();
//      }

    public void fillElement(Element el) {
        Element mel = new Element("AnimCanvas");
        mel.add(super.getElement());

        Element ael = ap.getElement();
        mel.add(ael);

        Element verb_el = new Element("lesson");
        String verb = getLessonVerb();
        if (verb != null && verb.length() > 0) {
            verb_el.addAttr("verb", verb);
            mel.add(verb_el);
        }

        Element aacel = new Element("AllActors");
        for (int i = 0; i < cab.actorNum(); i++) {
            Actor act = a_ctxt.ae.cabaret_panel.getActorInPanelAbs(i);
//	    OmegaContext.sout_log.getLogger().info("ERR: " + "saving " + i + ' ' + act);
            if (act != null) {
                Element acel = act.getElement();
                acel.addAttr("nid", "" + i);
                aacel.add(acel);
            }
        }

        Element awel = new Element("AllWings");
        for (int i = 0; i < a_ctxt.ae.wings_panel.wingA.arr.length; i++) {
            if (a_ctxt.ae.wings_panel.getWing(i) != null) {
                Element wel = a_ctxt.ae.wings_panel.getWing(i).getElement();
                wel.addAttr("nid", "" + i);
                awel.add(wel);
            }
        }

        mel.add(aacel);
        mel.add(awel);

        el.add(mel);
    }

    public void load(Element root) {
        if (root == null)
            return;

        Element eel = root.findElement("AnimCanvas", 0);
        if (eel == null)
            eel = root.findElement("EditCanvas", 0);
        if (eel != null) {
            super.load(eel);
        }
        ap.load(eel);

        Element lel = eel.findElement("lesson", 0);
        if (lel != null) {
            String verb = lel.findAttr("verb");
            setLessonVerb(verb);
        }

        Element aael = eel.findElement("AllActors", 0);
        if (aael != null) {
            createDefaultActors();
            for (int i = 0; i < OmegaConfig.CABARET_ACTOR_N; i++) {
                Element acel = aael.findElement("Actor", i);
                if (acel != null) {
                    int ix = Integer.parseInt(acel.findAttr("nid"));
                    String fn = acel.findAttr("name");
                    String id = acel.findAttr("lesson_id");
                    String var1 = acel.findAttr("var1");
                    if (var1 == null)
                        var1 = "";
                    String var2 = acel.findAttr("var2");
                    if (var2 == null)
                        var2 = "";
                    String var3 = acel.findAttr("var3");
                    if (var3 == null)
                        var3 = "";
                    String sc = acel.findAttr("prim_scale");
                    String mi = acel.findAttr("prim_mirror");
                    if (sc == null)
                        sc = "1.0";
                    if (mi == null)
                        mi = "0";

                    double[] da;

                    Actor act = loadActor(ix, fn);

//log		    OmegaContext.sout_log.getLogger().info("ERR: " + "!!!!!!!!!!! actor loaded " + act + ' ' + var1 + ' ' + var2 + ' ' + var3 + '.');
                    String hs = acel.findAttr("hotspot");
                    if (hs != null) {
                        act.gimae.setHotSpotIx(0, hs);
                    }

                    for (int ih = 0; ih < Hotspot.HOTSPOT_N; ih++) {
                        hs = acel.findAttr("hotspot_" + Hotspot.getType(ih));
                        if (hs == null) {
                            for (int ih2 = 1; ih2 < Hotspot.HOTSPOT_N; ih2++)
                                act.gimae.setHotSpotIxSame(ih2);
                            break;
                        }
                        act.gimae.setHotSpotIx(ih, hs);
                    }

                    if (id != null)
                        act.gimae.setLessonId(id);
                    act.gimae.setVariable(1, var1);
                    act.gimae.setVariable(2, var2);
                    act.gimae.setVariable(3, var3);
                    double scd = SundryUtils.tD(sc);
                    act.gimae.setPrimScale(scd);
                    act.gimae.setPrimMirror(Integer.parseInt(mi));
                }
            }
        }
        Element awel = eel.findElement("AllWings", 0);
        if (awel != null) {

            for (int i = 0; i < OmegaConfig.WINGS_N; i++) {
                Element wel = awel.findElement("Wing", i);
                if (wel != null) {
                    int ix = Integer.parseInt(wel.findAttr("nid"));
                    String fn = wel.findAttr("name");
                    String la = wel.findAttr("layer");
                    String mi = wel.findAttr("mirror");
                    if (mi == null)
                        mi = "0";
                    String sc = wel.findAttr("scale");
                    String po = wel.findAttr("position");
                    if (po == null)
                        po = "0.5 0.5";
                    if (la == null)
                        la = "1";
                    String[] sa = SundryUtils.split(po, " ,;");
                    double d1 = SundryUtils.tD(sa[0]);
                    double d2 = SundryUtils.tD(sa[1]);
                    double[] da = new double[]{d1, d2};

                    double sc_d = 1.0;
                    if (sc != null)
                        sc_d = SundryUtils.tD(sc);
                    Wing w = createWing(fn, (int) d1, (int) d2,
                            Integer.parseInt(la),
                            sc_d,
                            Integer.parseInt(mi));
                    int wing_nid = w.ord;

                    if (a_ctxt.ae != null)
                        a_ctxt.ae.wings_panel.setWing(w, wing_nid);
                    resetBackground();
                }
            }
        }
        repaint();
    }

    public String getEndCode() {
        if (trigger_left == false)
            return "left";
        if (trigger_up == false)
            return "up";
        return "normal";
    }

}
