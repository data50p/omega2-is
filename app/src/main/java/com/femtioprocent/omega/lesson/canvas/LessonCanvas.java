package com.femtioprocent.omega.lesson.canvas;
/*

 låt TAB enbart gå i den kolumn (-er) som nästa menings ord pekar ut.
 välj sedan nästa kolumn

 TAB skall fokusera

 kvittera med TAB Enter
 */

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.assets.TargetCombinations;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.lesson.actions.AnimAction;
import com.femtioprocent.omega.lesson.machine.Item;
import com.femtioprocent.omega.lesson.machine.ItemEntry;
import com.femtioprocent.omega.lesson.machine.Target;
import com.femtioprocent.omega.lesson.managers.movie.LiuMovieManager;
import com.femtioprocent.omega.message.Manager;
import com.femtioprocent.omega.swing.GetOption;
import com.femtioprocent.omega.swing.ScaledImageIcon;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Num;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/*
 --gapE---|=====|--gapB--|=========|---gapE---




 */
public class LessonCanvas extends BaseCanvas {

    public Manager om_msg_mgr = new Manager();
    static float gapB = 5;
    static final float gapE = 5;
    static final float tgH = 8;
    public LessonEditorPanel lep;
    //    public Target tg;
    private int target_standout_msk = 0; // mask
    private boolean mark_more = false;
    public boolean mouse_mark_target = true;
    public boolean box_mark_target = true;
    boolean show_wordbox = true;
    private Box active_item_action_box = null;
    private Box active_item_box = null;
    int active_titem_ix = -1;
    static final int SELECTED = 0;   // this only when mouseover
    static final int ACTIVATED = 1;   // clicked with mouse, blue/red frame
    static final int MARKED = 2;   // whole bureau when mouseover
    static final int BUSY = 3;   // clicked
    static final int BOX_MAXSTATE = 4;
    public boolean edit;
    ShapeList tgAddBoxes;
    ShapeList tgDelBoxes;
    ShapeList addBoxes;
    ShapeList delBoxes;
    //    EventListenerList lc_listeners;
    private Font item_fo = new Font("Arial", Font.PLAIN, (int) (/*h*/20 * 0.7));
    private Font item_fo2 = new Font("Arial", Font.PLAIN, (int) (/*h*/20 * 0.35));
    private Font trgt_fo = new Font("Arial", Font.PLAIN, (int) (/*h*/20 * 0.8));
    private Font trgtA_fo = new Font("Arial", Font.PLAIN, (int) (/*h*/20 * 0.4));
    boolean show_msg = false;
    String lesson_name = "-noname-";
    String lesson_link_next = null;
    boolean lesson_is_first = false;
    boolean last_nav_kbd = false;
    // 0 en svart
    // 1 en röd
    // 2 två röd svart
    // 3 två svart röd
    int quit_state = 0;
    boolean quit_disabled = false;
    ComponentAdapter cmp_li = new ComponentAdapter() {
        public void componentResized(ComponentEvent ev) {
            trgt_fo = null;
            trgtA_fo = null;
            title_fo = null;
            render();
        }
    };

    void actionQuit() {
        OmegaContext.sout_log.getLogger().info(":--: " + "enter quitAction");
        MsgItem msgitem = l_ctxt.getLesson().getResultSummary_MsgItem();
        OmegaContext.sout_log.getLogger().info(":--: " + "enter quitAction");
        if (msgitem != null) {
            showMsg(msgitem);
        }
        OmegaContext.sout_log.getLogger().info(":--: " + "exited quitAction");
        hideMsg();
        fireExit(1);
        SundryUtils.m_sleep(300);
    }

    public LessonCanvas(LessonContext l_ctxt) {
        super(l_ctxt);
        OmegaContext.lesson_log.getLogger().info("XXX");

//	focus_list = new CycleList(-1);
        requestFocus();
        setLayout(null);
        addComponentListener(cmp_li);
        lc_listeners = new EventListenerList();

    }

    public boolean skip_keycode = false;

    public boolean ownKeyCode(int kc, boolean is_shift) {
        if (edit) {
            return true;
        }
        OmegaContext.sout_log.getLogger().info(":--: " + "ownKey " + kc);
        if (OmegaConfig.isKeyNext(kc)) {
            if (isMsg()) {
                hideMsg();
                return true;
            }

            if (skip_keycode) {
                return false;
            }

            if (quit_state == 2) {
                setQuitState("kN", 3);
            } else if (quit_state == 3) {
                setQuitState("kS", 2);
            } else {
                if (is_shift) {
                    gotoPrevBox();
                } else {
                    gotoNextBox();
                }
                last_nav_kbd = true;
            }
            return false;
        }
        if (OmegaConfig.isKeyESC(kc)) {
            if (!quit_disabled) {
                setQuitState("Esc", 0);
                hideMsg();
                fireExit(2);
                //		om_msg_mgr.fire("exit create");
                return false;
            }
        }
        if (kc == 'r') {
            if (LiuMovieManager.repeat_mode == LiuMovieManager.RepeatMode.CAN_REPEAT) {
                LiuMovieManager.repeat_mode = LiuMovieManager.RepeatMode.DO_REPEAT;
                repaint();
            }
        }
        if (OmegaConfig.isKeySelect(kc)) {
            if (anim_action_patch != null) {
                anim_action_patch.rt.a_ctxt.anim_canvas.setBigButtonText("");
            } else {
                if (isMsg()) {
                    hideMsg();
                    return true;
                }
                if (skip_keycode) {
                    return false;
                }
                if (quit_state == 1) {
                    setQuitState("kS", 2);
                } else if (quit_state == 2) {
                    setQuitState("kS", 1);
                } else if (quit_state == 3) {
                    setQuitState("kS", 0);
                    hideMsg();
                    fireExit(3);
                    //		    om_msg_mgr.fire("exit create");
                } else {
                    if (isMsg()) {
                        hideMsg();
                    } else {
                        if (skip_keycode) {
                            return false;
                        }
                        selectBox(false, SundryUtils.ct());
                    }
                }
            }
            return false;
        }
        return true;
    }

    //     public void addLessonCanvasListener(LessonCanvasListener l) {
// 	lc_listeners.add(LessonCanvasListener.class, l);
//     }
//     public void removeLessonCanvasListener(LessonCanvasListener l) {
// 	lc_listeners.remove(LessonCanvasListener.class, l);
//     }
    void fireLessonEditorHitTarget(int ix, char type) {
        l_ctxt.getLesson().hitTarget(ix, type);
// 	Object[] lia= lc_listeners.getListenerList();
// 	for(int i = 0; i < lia.length; i += 2) {
// 	    ((LessonCanvasListener)lia[i+1]).hitTarget(ix, type);
// 	}
    }

    synchronized void fireLessonEditorHitItem(int ix, int iy, int where, char type) {
        l_ctxt.getLesson().hitItem(ix, iy, where, type);

// 	Object[] lia= lc_listeners.getListenerList();
// 	for(int i = 0; i < lia.length; i += 2) {
// 	    ((LessonCanvasListener)lia[i+1]).hitItem(ix, iy, where, type);
// 	}
    }

    class BoxState {

        void deselectAllBut(Box bx0, int state) {
            if (getAllBox() == null) {
                return;
            }

            synchronized (allbox_sy) {
                Iterator it = getAllBox().all.values().iterator();
                while (it.hasNext()) {
                    Box bx = (Box) it.next();
                    if (bx.state[state] && bx != bx0) {
                        bx.setState(state, false);
                    }
                }
            }
        }

        void selectCol(int col, int state) {
            if (getAllBox() == null) {
                return;
            }

            synchronized (allbox_sy) {
                Iterator it = getAllBox().all.values().iterator();
                while (it.hasNext()) {
                    Box bx = (Box) it.next();
                    if (bx.o_x == col) {
                        bx.setState(state, true);
                    } else {
                        bx.setState(state, false);
                    }
                }
            }
        }

        void setState(Box bx, int state, boolean b) {
            if (bx == null) {
                deselectAllBut(bx, state);
            }
            if (state == MARKED) {
                if (bx != null) {
                    selectCol(bx.o_x, state);
                }
            } else {
                deselectAllBut(bx, state);
                if (bx != null) {
                    bx.setState(state, b);
                }
            }
        }
    }

    ;

    public class ShapeList {

        static class ShapeItem {
            String id;
            Shape shp;
            Color col;

            public ShapeItem(String id, Shape shp, Color col) {
                this.id = id;
                this.shp = shp;
                this.col = col;
            }
        }

        java.util.List<ShapeItem> li;

        ShapeList() {
            li = new ArrayList<>();
        }

        void add(String id, Shape shp, Color col) {
            li.add(new ShapeItem(id, shp, col));
        }

        void draw(Graphics2D g2) {
            Iterator<ShapeItem> it = li.iterator();
            while (it.hasNext()) {
                ShapeItem sh = it.next();
                g2.setColor(sh.col);
                g2.fill(sh.shp);
            }
        }
    }

    public class Box {

        ItemEntry itm_ent;
        public int o_x, o_y;
        int x, y, w, h;
        RoundRectangle2D r;
        boolean[] state = new boolean[BOX_MAXSTATE];
        Stroke stroke;
        public int where;
        private boolean marked;
        public long when_hit;

        Box(ItemEntry itm_ent, int x, int y, int w, int h, int o_x, int o_y) {
            this.itm_ent = itm_ent;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.o_x = o_x;
            this.o_y = o_y;
            r = new RoundRectangle2D.Double(x, y, w, h, getCaH() / 48, getCaW() / 64);
            stroke = new BasicStroke(getCaH() / 300f);
        }

        void noNewBox(ItemEntry itm_ent, int x, int y, int w, int h, int o_x, int o_y) {
            this.itm_ent = itm_ent;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.o_x = o_x;
            this.o_y = o_y;
            r = new RoundRectangle2D.Double(x, y, w, h, getCaH() / 48, getCaW() / 64);
            stroke = new BasicStroke(getCaH() / 300f);
        }

        public Item getItem() {
            return itm_ent.getItemAt(o_y);
        }

        int hitBox(int xx, int yy) {
            if (xx >= x
                    && xx <= x + w
                    && yy >= y
                    && yy <= y + h) {
                return (int) ((((double) (xx - x)) / w) * 100);
            }
            return -1;
        }

        int last_h;
        int last_w;

        void draw(Graphics2D g2, boolean ink) {
            String s = "";
            Item itm = getItem();
            String colorModifier = "";
            if (itm != null) {
                s = itm.getDefaultFilledText();
                colorModifier = itm.getIt_ent().tid;
            }

            g2.setStroke(stroke);
            if (s.length() > 0) {
                if (state[SELECTED]) {
                    g2.setColor(getColor("bt_hs", colorModifier));
                    if (ink) {
                        g2.fill(r);
                    }
                } else if (state[MARKED]) {
                    g2.setColor(getColor("bt_hi", colorModifier));
                    if (ink) {
                        g2.fill(r);
                    }
                } else {
                    g2.setColor(getColor("bt_bg", colorModifier));
                    if (ink) {
                        g2.fill(r);
                    }
                }
            }

            Color col = getColor("bt_fr");
            Color co = col;

            if (edit) {
                if (this == active_item_action_box) {
                    col = Color.red;
                }
                if (this == active_item_box) {
                    col = Color.blue;
                }
                if (this == active_item_box && this == active_item_action_box) {
                    col = Color.magenta;
                }
                if (itm.getDefaultFilledText().length() == 0) {
                    col = moreGray(col);
                }
            }

            if (col == co) {
                if (state[SELECTED]) {
                    g2.setColor(getColor("bt_fr_hs"));
                } else if (state[MARKED]) {
                    g2.setColor(getColor("bt_fr_hi"));
                } else {
                    g2.setColor(getColor("bt_fr"));
                }
            } else {
                g2.setColor(col);
            }

            if (ink) {
                g2.draw(r);
            }

            if (state[SELECTED]) {
                g2.setColor(getColor("bt_tx_hs"));
            } else if (state[MARKED]) {
                g2.setColor(getColor("bt_tx_hi"));
            } else {
                g2.setColor(getColor("bt_tx"));
            }

            String ss = s.length() == 0 ? " " : s;
            if (getItemFont() == null || last_h != h || last_w != w) {
                setItemFont(new Font("Arial",
                        Font.PLAIN,
                        getSize((double) getCaW() / getCaH(), h)));
                setItemFont2(new Font("Arial",
                        Font.PLAIN,
                        getSize((double) getCaW() / (2 * getCaH()), h)));
            }
            last_h = h;
            last_w = w;


            int xQ = (int) (getCaW() * gapE / 100.0);
            int WW = getCaW() - xQ - xQ;
            int gB = (int) (WW * gapB / 100.0);

            int sw = getStringWidth(getItemFont(), ss) + (int) (gB / 2);
            if (ss.length() > 1)
                ;//OmegaContext.sout_log.getLogger().info(":--: " + "calc:   box:w=" + w + " string:w=" + sw + " string=" + ss + ' ' + ss.length());

            double d = wantRP;
            if (sw > w - gB / 2) {
                d = (double) sw / w;
                ;//OmegaContext.sout_log.getLogger().info(":--: " + "this wRP " + d + " sw=" + sw + "  w = " + w + "   w-gB/2=" + (w-gB/2));
                if (d > wantRP) {
                    wantRP = d;
                }
            }

            if (wantRP >= 0.95 && wantRP <= 1.005) {
                if (ss.length() > 1)
                    ;//OmegaContext.sout_log.getLogger().info(":--: " + "wantRP t' OK   " + wantRP + ' ' + d + ' ' + ss);
                g2.setFont(getItemFont());
                if (ink) {
                    g2.drawString(ss, (int) (x + 0.01 * getCaW()), y + (int) (h * 0.75));
                }
            } else if (ss.length() > 1)
                ;//OmegaContext.sout_log.getLogger().info(":--: " + "wantRP t' ELS  " + wantRP + ' ' + d + ' ' + ss);


            if (edit) {
                int msk = getTarget().whatTargetMatchTid(itm.getEntryTid());
                int how_many = Num.howManyBits(msk);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(new Color(111, 111, 111));
                if (how_many > 1) {
                    int dw = w / how_many;
                    for (int i = 1; i < how_many; i++) {
                        if (ink) {
                            g2.drawLine(x + dw * i, y,
                                    x + dw * i, y + h);
                        }
                    }
                }
            }
        }

        void draw(Graphics2D g2) {
            try {
                draw(g2, true);
            } catch (Exception ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "Box:draw(): " + ex);
            }
        }

        void drawNull(Graphics2D g2) {
            try {
                draw(g2, false);
            } catch (Exception ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "Box:draw(): " + ex);
            }
// 	    String s = "";
// 	    Item itm = getItem();
// 	    if ( itm != null )
// 		s = itm.getDefaultFilledText();

// 	    g2.setStroke(stroke);

// 	    if ( getItemFont() == null )
// 		last_h = h + 1;

// 	    String ss = s.length() == 0 ? " " : s;
// 	    if ( last_h != h || last_w != w ) {
// 		setItemFont(new Font("Arial",
// 				     Font.PLAIN,
// 				     getSize((double)getCaW() / getCaH(), h)));
// 		setItemFont2(new Font("Arial",
// 				     Font.PLAIN,
// 				     getSize((double)getCaW() / (2*getCaH()), h)));
// 	    }
// 	    last_h = h;
// 	    last_w = w;

// 	    int sw = getStringWidth(getItemFont(), ss) + 10;

// 	    if ( ss.length() >= 25 )
// 		if ( gapB != 1 ) {
// 		    gapB = 1;
// 		    wantRP = 0.95;
// 		    reCreateBoxesKeep();
// 		}

// 	    if ( sw > w ) {
// 		double d = (double)sw / w;
// 		if ( d > wantRP ) {
// 		    wantRP = d;
// 		}
// 	    }
        }

        void drawFrameOnly(Graphics2D g2) {
            Item itm = getItem();
            g2.setStroke(stroke);
            g2.setColor(getColor("bt_fr"));
            if (edit) {
                Color col = getColor("bt_fr");
                if (this == active_item_action_box) {
                    col = Color.red;
                }
                if (this == active_item_box) {
                    col = Color.blue;
                }
                if (this == active_item_box && this == active_item_action_box) {
                    col = Color.magenta;
                }
                if (itm.getDefaultFilledText().length() == 0) {
                    col = moreGray(col);
                }
                g2.setColor(col);
            }
            g2.draw(r);
        }

        void setState(int state_val, boolean b) {
            if (state[state_val] == b) {
                return;
            }
            state[state_val] = b;
            if (isVisible()) {
                repaintBox();
            }
        }

        void repaintBox() {
            LessonCanvas.this.repaint(x - 4, y - 4, w + 8, h + 8);
        }

        public String toString() {
            return "Box{" + o_x + ", "
                    + o_y + ", "
                    + x + ", "
                    + y + ", "
                    + w + ", "
                    + h + ", "
                    + getItem()
                    + "}";
        }
    }
    // ------------------------ Box end -----------------

    class AllBox {

        HashMap<String,Box> all;

        AllBox() {
            all = new HashMap();
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "AllBox CREATED " + new Date());
        }

        Rectangle2D getBound(int ix, int iy) {
            Iterator it = all.values().iterator();
            while (it.hasNext()) {
                Box bx = (Box) it.next();
                if (bx.o_x == ix
                        && bx.o_y == iy) {
                    return new Rectangle2D.Double(bx.r.getX(),
                            bx.r.getY(),
                            bx.r.getWidth(),
                            bx.r.getHeight());
                }
            }
            return null;
        }

        Box getBox(int ix, int iy) {
            Iterator it = all.values().iterator();
            while (it.hasNext()) {
                Box bx = (Box) it.next();
                if (bx.o_x == ix
                        && bx.o_y == iy) {
                    return bx;
                }
            }
            return null;
        }

        int getLastIx() {
            int max = -1;
            Iterator it = all.values().iterator();
            while (it.hasNext()) {
                Box bx = (Box) it.next();
                if (bx.o_x > max) {
                    max = bx.o_x;
                }
            }
            return max;
        }

        int getLastIy(int ix) {
            int max = -1;
            Iterator it = all.values().iterator();
            while (it.hasNext()) {
                Box bx = (Box) it.next();
                if (bx.o_x == ix && bx.o_y > max) {
                    max = bx.o_y;
                }
            }
            return max;
        }

        void clearAll() {
//	    all.removeAll();
        }

        void mark_All() {
            Iterator it = all.values().iterator();
            while (it.hasNext()) {
                Box bx = (Box) it.next();
                bx.marked = true;
            }
        }

        void delAllMarked() {
            Iterator it = all.values().iterator();
            while (it.hasNext()) {
                Box bx = (Box) it.next();
                if (bx.marked) {
                    all.remove("" + bx.o_x + ':' + bx.o_y);
                    delAllMarked();
                    return;
                }
            }
        }
    }

    ;

    public String getLessonName() {
        if (lep == null) {
            return lesson_name;
        } else {
            return lep.getLessonName();
        }
    }

    public String getLessonLinkNext() {
        if (lep == null) {
            return lesson_link_next;
        } else {
            return lep.getLessonLinkNext();
        }
    }

    public boolean getLessonIsFirst() {
        if (lep == null) {
            return lesson_is_first;
        } else {
            return lep.getLessonIsFirst();
        }
    }

    private AllBox all_Box;
    Object allbox_sy = new Object();

    AllBox getAllBox() {
        return all_Box;
    }

    void setAllBox(AllBox ab) {
        all_Box = ab;
    }

    class Mouse extends MouseInputAdapter {

        LessonCanvas l_canvas;
        Point2D mpress_p;
        int NORM = 0;
        int MSG = 1;
        int mode = NORM;

        Mouse(LessonCanvas l_canvas) {
            this.l_canvas = l_canvas;
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        Box last_hbx = null;

        public void mousePressed(MouseEvent e) {
            if (Lesson.mistNoMouse) {
                return;
            }

            mpress_p = new Point2D.Double(e.getX(), e.getY());

            if (mode == NORM) {
                int ix;
                if ((ix = hitTarget((int) e.getX(), (int) e.getY(), 'p')) != -1) {
                    if (e.isPopupTrigger()) {
                        if (lep != null) {
                            lep.popupTargetProp();
                        }
                    }
                    if (mouse_mark_target) {
                        target_standout_msk = 1 << ix;
                        mark_more = false;
                    }
                    repaintTarget();
                    fireLessonEditorHitTarget(ix, 'p');
                    active_titem_ix = ix;
                    setQuitState("mP", 0);
                    return;
                }

                if (true) {
                    int[] ia = new int[1];
                    Box hbx = hitBox((int) e.getX(), (int) e.getY(), 'p', ia);
                    if (hbx != null) {
                        if (e.isPopupTrigger()) {
                            if (lep != null) {
                                lep.popupItemProp();
                            }
                        }
                        hbx.where = ia[0];
                        if (hbx != null) {
                            selectBox(hbx, true, e.getWhen());
                            last_nav_kbd = false;
                        }
                    }
                }

                if (edit) {
                    Iterator it = addBoxes.li.iterator();
                    while (it.hasNext()) {
                        Object[] oa = (Object[]) it.next();
                        String id = (String) oa[0];
                        Shape shp = (Shape) oa[1];
                        if (shp.contains(mpress_p)) {
                            if (id.length() > 0) {
                                addItemEntry(Integer.parseInt(id));
                            }
                        }
                    }

                    it = delBoxes.li.iterator();
                    while (it.hasNext()) {
                        Object[] oa = (Object[]) it.next();
                        String id = (String) oa[0];
                        Shape shp = (Shape) oa[1];
                        if (shp.contains(mpress_p)) {
                            if (id.length() > 0) {
                                delItemEntry(Integer.parseInt(id));
                            }
                        }
                    }

                    it = tgAddBoxes.li.iterator();
                    while (it.hasNext()) {
                        Object[] oa = (Object[]) it.next();
                        String id = (String) oa[0];
                        Shape shp = (Shape) oa[1];
                        if (shp.contains(mpress_p)) {
                            if (id.length() > 0) {
                                if (getTarget().get_howManyT_Items() < 6) {
                                    addTarget(Integer.parseInt(id));
                                }
                            }
                        }
                    }

                    it = tgDelBoxes.li.iterator();
                    while (it.hasNext()) {
                        Object[] oa = (Object[]) it.next();
                        String id = (String) oa[0];
                        Shape shp = (Shape) oa[1];
                        if (shp.contains(mpress_p)) {
                            if (id.length() > 0) {
                                delTarget(Integer.parseInt(id));
                            }
                        }
                    }
                }
            } else { // mode == MSG
                hideMsg();
            }
        }

        int last_ix = -1;

        public void mouseMoved(MouseEvent e) {
            if (false && Lesson.mistNoMouse) {
                return;
            }
            if (last_nav_kbd) {
                return;
            }

            int ix;
            if ((ix = hitTarget((int) e.getX(), (int) e.getY(), 'p')) != -1) {
                if (mouse_mark_target && edit) {
                    target_standout_msk = 1 << ix;
                    mark_more = false;
                }
                repaintTarget();
                if (last_ix != ix) {
                    fireLessonEditorHitTarget(ix, 'm');
                    last_ix = ix;
                }
                return;
            }

            int[] ia = new int[1];
            Box hbx = hitBox((int) e.getX(), (int) e.getY(), 'm', ia);
            if (hbx != null) {
            }
            if (hbx != null && hbx != last_hbx) {
                int msk = l_canvas.getTarget().whatTargetMatchTid(hbx.getItem().getEntryTid());
                if (box_mark_target) {
                    target_standout_msk = msk;
                    mark_more = false;
                }
                enterBox(hbx);
                repaintTarget();
                last_hbx = hbx;
            }

            if (hitQuitButton((int) e.getX(), (int) e.getY())
                    || hitExtraQuitButton((int) e.getX(), (int) e.getY())) {
                if (hitQuitButton((int) e.getX(), (int) e.getY())) {
                    if (quit_state == 0) {
                        setQuitState("mM", 1);
                    } else if (quit_state == 3) {
                        setQuitState("mM", 2);
                    } else {
                    }
                } else {
                }

                if (hitExtraQuitButton((int) e.getX(), (int) e.getY())) {
                    if (quit_state == 2) {
                        setQuitState("mMx", 3);
                    }
                } else {
// 		    if ( quit_state == 2 || quit_state == 3 )
// 			setQuitState("mM", 1);
                }
            } else {
            }
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            if (false && Lesson.mistNoMouse) {
                return;
            }
            mpress_p = new Point2D.Double(e.getX(), e.getY());

            OmegaContext.sout_log.getLogger().info(":--: " + "release " + mode);

            if (mode == NORM && !Lesson.mistNoMouse) {
                int ix;
                if ((ix = hitTarget((int) e.getX(), (int) e.getY(), 'p')) != -1) {
                    if (e.isPopupTrigger()) {
                        if (lep != null) {
                            lep.popupTargetProp();
                        }
                    }
                }
                if (!Lesson.mistNoMouse) {
                    int[] ia = new int[1];
                    Box hbx = hitBox((int) e.getX(), (int) e.getY(), 'p', ia);
                    if (hbx != null && e.isPopupTrigger()) {
                        if (lep != null) {
                            lep.popupItemProp();
                        }
                    }
                }
            }

            if (!Lesson.mistNoMouse) {
                if (hitQuitButton((int) e.getX(), (int) e.getY())) {
                    if (quit_state == 1) {
                        setQuitState("mQ", 2);
                    } else if (quit_state == 2) {
                        setQuitState("mQ", 1);
                    } else if (quit_state == 3) {
                        setQuitState("mQ", 1);
                    } else {
                        setQuitState("mQ", 1);
                    }
                } else {
                    if (hitExtraQuitButton((int) e.getX(), (int) e.getY())) {
                        if (quit_state == 2 || quit_state == 3) {
                            setQuitState("mP", 0);
                            hideMsg();
                            fireExit(5);
                            //om_msg_mgr.fire("exit create");
                        }
                    }
                }
            }
            OmegaContext.sout_log.getLogger().info(":--: " + "" + isMsg());

            if (isMsg()) {
                hideMsg();
            }
        }
    }

    void fireExit(int where) {
        OmegaContext.sout_log.getLogger().info(":--: " + "fireExit " + where);
        om_msg_mgr.fire("show_result");
    }

    public void fireRealExit() {
        OmegaContext.sout_log.getLogger().info(":--: " + "fireRealExit");
        om_msg_mgr.fire("exit create");
    }

    public void setNextMarkTarget() {
        target_standout_msk <<= 1;
        mark_more = false;
    }

    public void setMarkTarget(int ix) {
        setMarkTarget(ix, false);
    }

    public void setMarkTarget(int ix, boolean mark_more) {
        this.mark_more = mark_more;
        if (ix == -1) {
            target_standout_msk = 0;
        } else {
            target_standout_msk = 1 << ix;
        }
        mouse_mark_target = false;
        box_mark_target = false;
        repaintTarget();
    }

    public void setMarkTargetNo() {
        target_standout_msk = 0;
        mark_more = false;
        mouse_mark_target = true;
        box_mark_target = true;
        repaintTarget();
    }

    public void setMarkTargetAll() {
        target_standout_msk = 0xff;
        mark_more = false;
        mouse_mark_target = true;
        box_mark_target = true;
        repaintTarget();
    }

    Mouse m = new Mouse(this);
    Box last_bx;

    synchronized void enterBox(Box bx) {
        if (bx == null) {
            return;
        }
        if (last_bx != bx) {
            fireLessonEditorHitItem(bx.o_x, bx.o_y, 1, 'm');
            last_bx = bx;
        }
    }

    Box last_selected_box = null;

    void selectBox(Box bx, boolean with_mouse, long when_hit) {
        if (bx == null) {
            return;
        }

        setQuitState("selB", 0);

        skip_keycode = true;

        last_selected_box = bx;

// 	if ( with_mouse == false ) {
// 	    if ( mouse_mark_target )
// 		target_standout_msk = 1 << ix;
// 	}

        box_state.setState(bx, BUSY, true);
        box_state.setState(bx, SELECTED, true);
        bx.repaintBox();

        if ( useThisText(bx.getItem().getText()) ) {
            bx.when_hit = when_hit;
            l_ctxt.getLesson().sendMsg("hBox" + (with_mouse ? "M" : "K"), bx);
        }

        if (bx.getItem() != null && bx.getItem().isAction()) {
            active_item_action_box = bx;
            //repaint();
        }
        if (bx.getItem() != null && bx.getItem() instanceof Item) {
            active_item_box = bx;
            //repaint();
        }

        fireLessonEditorHitItem(bx.o_x, bx.o_y, 1, 'p');
    }

    private boolean useThisText(String text) {
        if (text.length() == 0)
            return false;
        if ( text.matches("[{]\\*[0-9]*:[}]")) {
            return false;
        }
        return true;
    }

    public void ready() {
        box_state.setState(null, BUSY, false);
    }

    int current_ix = 0;
    int current_iy = 0;

    void gotoBox(int ix, int iy) {
        Box bx = getAllBox().getBox(ix, iy);
        if (bx != null) {
            enterBox(bx);
        }
    }

    void gotoBoxNoEnter(int ix, int iy) {
        Box bx = getAllBox().getBox(ix, iy);
    }

    public void gotoNextBox() {
        gotoNextSmartBox();
    }

    private void gotoPrevBox() {
        setQuitState("prevB", 2);
    }

    private void gotoQuit() {
        setQuitState("goQ", 1);
    }

    private void gotoCorrectBox() {
        gotoNextSmartBox();
    }

    int last_bix = -1;
    int last_bixAix = -1;

    private void gotoNextSmartBox() {
        try {
            int nix = getTarget().findNextFreeT_ItemIx();
            int bixA[] = getTarget().findEntryIxMatchTargetIxAll(nix);

            if (last_bixAix == -1 || last_bixAix >= bixA.length) {
                last_bixAix = 0;
            }

            int bix = bixA[last_bixAix];

            if (mouse_mark_target) {
                target_standout_msk = 1 << bix;
                mark_more = false;
                repaintTarget();
            }
            int niy = 0;
            if (bix == last_bix) {
                niy = current_iy + 1;
            }
            last_bix = bix;
            Box bx = getAllBox().getBox(bix, niy);
            if (bx != null) {
                setQuitState("z1", 0);
                current_iy = niy;
                current_ix = bix;
                int[] where = new int[1];
                hitBox((int) (bx.r.getX()),
                        (int) (bx.r.getY()),
                        'm',
                        where);
                enterBox(bx);
            } else {
                setQuitState("z1", 0);
                current_iy = 0;

                last_bixAix++;
                if (last_bixAix >= bixA.length) {
                    last_bixAix = 0;
                }
                bix = bixA[last_bixAix];
                current_ix = bix;
                bx = getAllBox().getBox(bix, current_iy);

                int[] where = new int[1];
                hitBox((int) (bx.r.getX()),
                        (int) (bx.r.getY()),
                        'm',
                        where);
                enterBox(bx);
                last_bix = bix;
            }
        } catch (Exception ex) {
        }
    }

    private void gotoSameBox() {
        try {
            Box bx;
            int nix = current_ix;
            int niy = current_iy;
            bx = getAllBox().getBox(nix, niy);
            if (bx == null) {
                nix++;
                ;
                niy = 0;
                bx = getAllBox().getBox(nix, niy);
                if (bx == null) {
                    nix = 0;
                    niy = 0;
                    setQuitState("z2", 0);
                    bx = null; // getAllBox().getBox(nix, niy);
                }
            }
            if (bx != null) {
                setQuitState("z3", 0);
//log		OmegaContext.sout_log.getLogger().info(":--: " + "move " + current_ix + ' ' + current_iy + ' ' + nix + ' ' + niy);
                current_iy = niy;
                current_ix = nix;
                int[] where = new int[1];
                hitBox((int) (bx.r.getX()),
                        (int) (bx.r.getY()),
                        'm',
                        where);
                enterBox(bx);
            }
        } catch (NullPointerException ex) {
        }
    }

    public void gotoBoxRel(int dx, int dy) {
        Box bx = getAllBox().getBox(current_ix + dx, current_iy + dy);
        if (bx == null) {
            return;
        }
        current_ix += dx;
        current_iy += dy;
        int[] where = new int[1];
        hitBox((int) (bx.r.getX()),
                (int) (bx.r.getY()),
                'm',
                where);
        enterBox(bx);
    }

    public void selectBox(boolean with_mouse, long when_hit) {
        try {
            Box bx = getAllBox().getBox(current_ix, current_iy);
            int[] where = new int[1];
            hitBox((int) (bx.r.getX()),
                    (int) (bx.r.getY()),
                    'p',
                    where);
            selectBox(bx, with_mouse, when_hit);
        } catch (NullPointerException ex) {
        }
    }

    public int sowDummy(String current_correct_sentence) {
        return getTarget().sowDummy(current_correct_sentence);
    }

    public void removeDummy() {
        getTarget().removeDummy();
    }

    public Target getTarget() {
        return l_ctxt.getTarget();
    }

    public String[] getAllTargetCombinations() {
        return getAllTargetCombinations(" ", false);
    }

    public String[] getAllTargetCombinationsEx(String sep, boolean dummy, char delim) {
        try {
            Target tg2 = new Target();
            HashMap<String, Lesson.SentenceList> story_hm = Lesson.story_hm;
            tg2.loadFromEl(l_ctxt.getLesson().getElement(), "", story_hm, dummy, false); // FIX nomix?

            String[] sa = tg2.getAllTargetCombinationsEx(sep, delim);

            return sa;
        } catch (Exception ex) {
            return new String[0];
        }
    }

    public TargetCombinations getAllTargetCombinationsEx2(boolean dummy) {
        try {
            Target tg2 = new Target();
            HashMap<String, Lesson.SentenceList> story_hm = Lesson.story_hm;
            tg2.loadFromEl(l_ctxt.getLesson().getElement(), "", story_hm, dummy, false); // FIX nomix?
            return tg2.getAllTargetCombinationsEx2(l_ctxt.getLesson());
        } catch (Exception ex) {
            return new TargetCombinations();
        }
    }

    @Deprecated
    public TargetCombinations getAllTargetCombinationsEx2(File omega_lesson, boolean dummy) {
        try {
            Target tg2 = new Target();
            HashMap<String, Lesson.SentenceList> story_hm = Lesson.story_hm;
            tg2.loadFromEl(l_ctxt.getLesson().getElement(), "", story_hm, dummy, false); // FIX nomix?
            return tg2.getAllTargetCombinationsEx2(l_ctxt.getLesson());
        } catch (Exception ex) {
            return new TargetCombinations();
        }
    }

    public String[] getAllTargetCombinations(String sep, boolean dummy) {
        try {
            Target tg2 = new Target();
            HashMap<String, Lesson.SentenceList> story_hm = Lesson.story_hm;
            tg2.loadFromEl(l_ctxt.getLesson().getElement(), "", story_hm, dummy, false); // FIX nomix?

            String[] sa = tg2.getAllTargetCombinations(sep);

            return sa;
        } catch (Exception ex) {
            return new String[0];
        }
    }

    String askForOneTarget(Component owner) {
        return askForOneTarget(owner, null);
    }

    String askForOneTarget(Component owner, String def) {
        String[] sa = getAllTargetCombinations();
        if (def == null) {
            def = sa[0];
        }
        String value = (String) JOptionPane.showInputDialog(owner,
                T.t("Select a sentence"),
                "Omega - Option",
                JOptionPane.QUESTION_MESSAGE,
                null,
                sa,
                def);
        return value;
    }

    public void addTarget(int where) {
        String[] sa = {
                T.t("Cancel"),
                T.t("Add new target")
        };
        int sel = GetOption.getOption(T.t("Adding the target area"),
                sa);
        if (sel > 0) {
            getTarget().addT_Item(where);
            reCreateBoxesKeep();
            repaint();
        }
    }

    public void delTarget(int where) {
        String[] sa = {
                T.t("Cancel"),
                T.t("Delete target")
        };
        int sel = GetOption.getOption(T.t("Deleting the target area"),
                sa);
        if (sel > 0) {
            getTarget().delT_Item(where);
            reCreateBoxesKeep();
            repaint();
        }
    }

    void addItemEntry(int ix) {
        String[] sa = {
                T.t("Cancel"),
                T.t("Add new column")
        };
        int sel = GetOption.getOption(T.t("Add a new column"),
                sa);
        if (sel > 0) {
            getTarget().addItemEntry(ix, 0);
            getTarget().addItem(ix, 0);
            reCreateBoxesKeep();

            if (active_item_box != null) {
                int cix = active_item_box.o_x;
                int ciy = active_item_box.o_y;
                if (cix >= ix) {
                    active_item_box = getAllBox().getBox(cix + 1, ciy);
                }
            }
            if (active_item_action_box != null) {
                int cix = active_item_action_box.o_x;
                int ciy = active_item_action_box.o_y;
                if (cix >= ix) {
                    active_item_action_box = getAllBox().getBox(cix + 1, ciy);
                }
            }
            repaint();
        }
    }

    public void delItemEntry(int ix) {
        String[] sa = {
                T.t("Cancel"),
                T.t("Delete column")
        };
        int sel = GetOption.getOption(T.t("Delete selected column"),
                sa);
        if (sel > 0) {
            getTarget().delItemEntry(ix, 0);
            reCreateBoxes();
            repaint();
        }
    }

    boolean showWordbox() {
        return show_wordbox;
    }

    private void reCreateBoxes() {
        active_item_action_box = null;
        active_item_box = null;
        reCreateBoxesKeep();
    }

    void reCreateBoxesKeep() {
        createBoxes();
        repaint();
    }

    int[] rndA = null;

    void createBoxes() {
        int x = (int) (getCaW() * gapE / 100.0);
        int y = (int) (getCaH() * (2 * gapE) / 100.0) + (int) (getCaH() * tgH / 100.0) * 2 + 25;
        int WW = getCaW() - x - x;
        int HH = (int) (getCaH() * (100 - tgH - tgH) / 100.0) - y;

        int gB = (int) (WW * gapB / 100.0);

        int nx = getTarget().howManyItemBoxes();
        int ny = edit ? 8 : getTarget().getMaxItemsInAnyBox();


        Font fo = new Font("Arial", Font.PLAIN, 12);
// 	Lambda la = new Lambda() {
// 		public void eval(Object oa, Object ob) {
// 		    String s = (String)oa);
// 		}
// 	    };
// 	int sw = getStringWidth(item_fot, msg_item.text);

        Graphics2D g2 = (Graphics2D) getGraphics();
        int max_bxw = getTarget().getMaxWidthSumAllBox(fo, g2);

        synchronized (allbox_sy) {
//  	    if ( allbox != null )
//  		allbox.clearAll();

            if (getAllBox() == null) {
                setAllBox(new AllBox());
            }

            getAllBox().mark_All();

            tgDelBoxes = new ShapeList();
            tgAddBoxes = new ShapeList();
            addBoxes = new ShapeList();
            delBoxes = new ShapeList();

            int hh = getHeight() / 15;
            int hhh = hh / 5;

            int XX = 0;
            for (int xi = 0; xi < nx; xi++) {
                int bx_w = (int) (getTarget().getMaxWidthInBox(xi, fo, g2));
                double ratio = (double) (bx_w + 1 * gB * 0.5) / (0.5 * nx * gB + max_bxw);

                int sww = (int) ((WW - (gB * (nx - 1))) * ratio);
                int hm_bx = getTarget().getMaxItemsInBox(xi);

                //OmegaContext.sout_log.getLogger().info(":--: " + "ratio " + xi + ' ' + ratio + ' ' + bx_w + ' ' + max_bxw + ' ' + sww + ' ' + gB);

                if (hm_bx > 0 && rndA == null) {
                    rndA = SundryUtils.createUniq(hm_bx).asIntArray();
                }
                for (int yi = 0; yi < ny; yi++) {
                    int yi_rnd;

                    if (false && !edit && rndA != null && yi < rndA.length) // FIX NOFATAL
                    {
                        yi_rnd = rndA[yi];
                    } else {
                        yi_rnd = yi;
                    }

                    Item itm = getTarget().getItemAt(xi, yi_rnd);
                    int xx = XX;

                    Box bx = null;

                    int o_x = xi;
                    int o_y = yi_rnd;

                    Box obx = getAllBox().getBox(o_x, o_y);
                    if (itm != null) {
                        if (obx == null) {
                            bx = new Box(itm.getIt_ent(), x + xx, y + yi * (hh + hhh), sww, hh, o_x, o_y);
                        } else {
                            bx = obx;
                            bx.marked = false;
                            bx.noNewBox(itm.getIt_ent(), x + xx, y + yi * (hh + hhh), sww, hh, o_x, o_y);
                        }
                    } else if (edit) { // itm == null
                        getTarget().addEmptyItem(xi, yi);
                        itm = getTarget().getItemAt(xi, yi);
                        if (obx == null) {
                            bx = new Box(itm.getIt_ent(), x + xx, y + yi * (hh + hhh), sww, hh, o_x, o_y);
                        } else {
                            bx = obx;
                            bx.noNewBox(itm.getIt_ent(), x + xx, y + yi * (hh + hhh), sww, hh, o_x, o_y);
                        }
                    } else
                        ;

                    if (bx != null) {
                        if (active_item_box != null) {
                            if (active_item_box.o_x == bx.o_x
                                    && active_item_box.o_y == bx.o_y) {
                                active_item_box = bx;
                            }
                        }
                        if (active_item_action_box != null) {
                            if (active_item_action_box.o_x == bx.o_x
                                    && active_item_action_box.o_y == bx.o_y) {
                                active_item_action_box = bx;
                            }
                        }
                        getAllBox().all.put("" + xi + ':' + yi, bx);
                    }
                }
                XX += sww + gB;
            }
            getAllBox().delAllMarked();
        } // sync allbox

        createAddDelBoxes();

        repaint();
    }

    public static int CH_W = 1;

    public void render(boolean all, boolean reset) {
        gapB = 5;
        if (reset) {
            box_state.setState(null, ACTIVATED, false);
            setAllBox(new AllBox());
        }
        if (all) {
//  	    active_item_action_box = null;
//  	    active_item_box = null;
            createBoxes();
            repaint();
        } else {
            repaint(getTargetRectangle());
        }
    }

    public void render() {
        if (getTarget() == null) {
            return;
        }

        createBoxes();
        repaint();
        return;
    }

    public void renderTg() {
        if (getTarget() == null) {
            return;
        }

        createAddDelBoxesTgOnly();
        repaintTarget();
        return;
    }

    public void resetNav() {
        last_bixAix = 0;
    }

    public void initNewLesson() {
        setQuitState("z4", 0);
        rndA = null;
        box_state.setState(null, ACTIVATED, false);
        box_state.setState(null, SELECTED, false);
        box_state.setState(null, BUSY, false);
        resetItemFont();
        last_bixAix = 0;
    }

    public void eraseHilitedBox() {
        gotoBox(0, 0);
        box_state.setState(null, ACTIVATED, false);
        box_state.setState(null, SELECTED, false);
        box_state.setState(null, BUSY, false);
        box_state.setState(null, MARKED, false);
    }

    public void startAction() {
        box_state.setState(null, ACTIVATED, false);
        box_state.setState(null, SELECTED, false);
        box_state.setState(null, BUSY, false);
//	show_action.setVisible(true);
        om_msg_mgr.fire("action");
        gotoBox(0, 0);
    }

    AnimAction anim_action_patch;

    public String waitReplyAction(AnimAction anim_action,
                                  String text,
                                  boolean show,
                                  Runnable myra) {
        String end_code_s = null;
        anim_action_patch = anim_action;
        if (show) {
            anim_action.rt.a_ctxt.anim_canvas.setBigButtonText(text);
            end_code_s = anim_action.rt.a_ctxt.anim_canvas.waitBigButtonText(myra);
            OmegaContext.sout_log.getLogger().info(":--: " + "LessonCanvas: end_ " + end_code_s);

            if (end_code_s.equals("normal")) {
                anim_action.rt.a_ctxt.anim_canvas.setBigButtonText("");
            }
        }
        anim_action_patch = null;
        return end_code_s;
    }

    public void endAction() {
    }

    public void endLastAction() {
//	show_action.setVisible(false);
        init();
    }

    public void initAction() {
//	show_action.setVisible(false);
    }

    public void removeHilitedBox() {
        eraseHilitedBox();
        last_bix = -1;
        repaint();
    }

    public void init() {
        getTarget().releaseAllT_Items();
        resetItemFont();
        repaintTarget();
    }

    void resetItemFont() {
        item_fo = null;
        item_fo2 = null;
        trgt_fo = null;
        trgtA_fo = null;
    }

    Font getItemFont() {
        return item_fo;
    }

    void setItemFont(Font fo) {
        item_fo = fo;
    }

    void setItemFont2(Font fo) {
        item_fo2 = fo;
    }

    void setTargetFont() {
        int h = (int) (1.5 * getCaH() * tgH / 100.0);
        trgt_fo = new Font("Arial", Font.PLAIN, getSize((double) getCaW() / getCaH(), h));
    }

    void setTargetFont(double f) {
        trgt_fo = new Font("Arial", Font.PLAIN, (int) f);
    }

    Font getTargetFont() {
        if (trgt_fo == null) {
            setTargetFont();
        }
        return trgt_fo;
    }

    void setTargetFontAlt(double f) {
        int h = (int) (getCaH() * tgH / 100.0);
        trgtA_fo = new Font("Arial",
                Font.PLAIN,
                (int) (f * getSize((double) getCaW() / getCaH(),
                        (int) (h * 0.65))));
    }

    Font getTargetFontAltS() {
        int h = (int) (getCaH() * tgH / 100.0);
        return new Font("Arial",
                Font.PLAIN,
                (int) (0.8 * getSize((double) getCaW() / getCaH(),
                        (int) (h * 0.65))));
    }

    Font getTargetFontAlt() {
        if (trgtA_fo == null) {
            setTargetFontAlt(1.0);
        }
        return trgtA_fo;
    }

    int getSize(double asp, int h) {
        if (asp == 0) {
            return (int) (h * 0.65);
        }
        int hh = (int) (h * 0.55);

        if (asp < 1.0) {
            hh *= asp;
        }

//	OmegaContext.sout_log.getLogger().info(":--: " + "      >>>>>>>>     " + asp + ' ' + hh);
        return hh;
    }

    // --
    Font title_fo;

    void setTitleFont() {
        int h = (int) (gX(0.024));
        title_fo = new Font("Arial", Font.PLAIN, h);
    }

    Font getTitleFont() {
        if (title_fo == null) {
            setTitleFont();
        }
        return title_fo;
    }
    // -- //

    Box getBox(Item itm) {
        Iterator it = getAllBox().all.values().iterator();
        while (it.hasNext()) {
            Box bx = (Box) it.next();
            if (bx.getItem() == itm) {
                return bx;
            }
        }
        return null;
    }

    public void repaint(Target.T_Item tit) {
        repaintTarget();
    }

    public void repaint(Item itm) {
        Box bx = getBox(itm);
        if (bx != null) {
            bx.repaintBox();
        }
    }

    void repaintTarget() {
        Rectangle r = getTargetRectangle();
        r.y -= 80;
        r.height += 80;
        repaint(r);
    }

    public Rectangle getTargetRectangle(int ix) {
        String s_left = getTarget().getTextUpto(ix, edit ? 3 : 1);
        if (s_left.length() > 0) {
            s_left += edit ? "   " : " ";
        }
        String s = getTarget().getTextAt(ix);
        int w_left = getStringWidth(getTargetFont(), s_left);
        int w_s = getStringWidth(getTargetFont(), s);
        Rectangle r = getTargetRectangle();
        return new Rectangle(r.x + (edit ? 30 : 10) + w_left,
                r.y + 2,
                w_s,
                r.height - 4);
    }

    public Rectangle getTargetRectangle() {
        int x = (int) (getCaW() * gapE / 100.0);
        int y = (int) (getCaH() * (2 * gapE) / 100.0);
        int w = getCaW() - x - x;
        int h = (int) (getCaH() * tgH / 100.0);
        return new Rectangle(x, y, w, h);
    }

    public Shape getTargetShape() {
        return getTargetRectangleMoreR(0);
    }

    Rectangle getTargetRectangleMore() {
        return getTargetRectangleMore(0.0);
    }

    Rectangle getTargetRectangleMore(double insets) {
        double x = insets + getCaW() * (gapE / 2.0) / 100.0;
        double y = insets + getCaH() * (1 * gapE) / 100.0;
        double w = getCaW() - x - x - insets * 2.0;
        double h = 2 * getCaH() * tgH / 100.0 - insets * 2.0;
        return new Rectangle((int) x, (int) y, (int) w, (int) h);
    }

    Shape getTargetRectangleMoreR(double insets) {
        double x = insets + getCaW() * (gapE / 2.0) / 100.0;
        double y = insets + getCaH() * (1.2 * gapE) / 100.0;
        double w = getCaW() - x - x;
        double h = 1.7 * getCaH() * tgH / 100.0 - insets * 2.0;
        return new RoundRectangle2D.Double(x, y, w, h, h, h);
    }

    public void populateGUI() {
    }

    void drawTarget(Graphics2D g2) {
        Target tg = getTarget();

        Rectangle rra = getTargetRectangle();
        Shape rram = getTargetRectangleMoreR(0.0);
        double baseline = rra.y + rra.height * 0.7;

        if (tg != null) {

            if (!edit) {
                g2.setColor(markTarget(getColor("sn_fr")));
                g2.fill(rram);

                g2.setColor(markTarget(getColor("sn_bg")));
                Shape rram2 = getTargetRectangleMoreR(3.0);
                g2.fill(rram2);
            }

            if (edit) {
                g2.setColor(getColor("sn_bg"));
                g2.fill(rra);

                if (target_standout_msk != 0) {
                    for (int i = 0; i < 0xff; i++) {
                        if (((1 << i) & target_standout_msk) != 0) {
                            Rectangle rr = getTargetRectangle(i);
                            Color col = moreSaturate(moreSaturate(getColor("sn_hi")));
                            if (mark_more) {
                                col = moreSaturate(moreSaturate(col));
                            }
                            g2.setColor(col);
                            g2.fill(rr);
                        }
                    }
                }
                for (int i = 0; i < getTarget().get_howManyT_Items(); i++) {
                    Rectangle rr = getTargetRectangle(i);
                    g2.setColor(new Color(111, 111, 111));
                    if (active_titem_ix == i) {
                        g2.setColor(Color.green);
                    }
                    g2.draw(rr);
                }

                g2.setStroke(new BasicStroke(getCaH() / 300f));
                g2.setColor(getColor("sn_fr"));
                g2.draw(rra);
            }

            if (showWordbox()) {
                if (target_standout_msk != 0) {
                    for (int i = 0; i < 6; i++) {
                        if (((1 << i) & target_standout_msk) != 0) {
                            Rectangle rr = getTargetRectangle(i);
                            int hh = 5;
                            if (mark_more) {
                                hh = (int) (rram.getBounds().getHeight() * 0.7);
                            }
                            Rectangle rr2 = new Rectangle(rr.x, rr.y + rr.height - hh, rr.width, hh);
                            Color col = getColor("sn_hi");
                            if (mark_more) {
                                col = moreSaturate(moreSaturate(col));
                            }
                            g2.setColor(col);
                            g2.fill(rr2);
                            g2.setColor(getColor("sn_hs"));
                            g2.draw(rr2);
                        } else {
                            Rectangle rr = getTargetRectangle(i);
                            Rectangle rr2 = new Rectangle(rr.x, rr.y + rr.height - 5, rr.width, 5);
//  			    g2.setColor(getColor("sn_hi"));
//  			    g2.fill(rr2);
                            g2.setColor(getColor("sn_hs"));
                            g2.draw(rr2);
                        }
                    }
                }
            }
        }

        Rectangle r = rra;

        //--

        if (!true) {
            Area a = new Area();
            double x = (double) r.getX();
            double y = (double) r.getY();
            double w = (double) r.getWidth();
            double h = (double) r.getHeight();

            RoundRectangle2D fr = new RoundRectangle2D.Double(x, y, w, h, 5, 5);

            a.add(new Area(new Rectangle2D.Double(0, 0, 10000, 10000)));
            a.subtract(new Area(fr));
            g2.setClip(a);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            g2.setColor(new Color(15, 15, 15));

            for (int i = 7; i <= 7; i++) {
                RoundRectangle2D frs = new RoundRectangle2D.Double(x + 10 - i, y + 10 - i, w, h, 5, 5);
                g2.fill(frs);
            }
            g2.setClip(new Area(new Rectangle2D.Double(0, 0, 10000, 10000)));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        //--

        if (edit && active_titem_ix >= 0) {
            Rectangle rr = getTargetRectangle(active_titem_ix);
            g2.setColor(Color.green);
            g2.setStroke(new BasicStroke(getCaH() / 500f));
            g2.draw(rr);
        }

        String s = tg == null ? "-" : getTarget().getAllText(edit ? 3 : 1);
        if (!true) {
            g2.setColor(getColor("sn_tx"));
        } else {
            g2.setColor(getColor("sn_tx"));
        }
        Font fo = getTargetFont();
        g2.setFont(fo);

        int sw = getStringWidth(getTargetFont(), s) + 20;
        if (sw > r.width) {
            double ff = r.width / (double) sw;
            setTargetFont(getTargetFont().getSize() * ff);
            repaint();
        } else {
            g2.drawString(s, r.x + (edit ? 30 : 10), (int) (r.y + r.height * 0.7));
        }
    }

    void drawTargetNull(Graphics2D g2) {
        Target tg = getTarget();

        Rectangle rra = getTargetRectangle();

        String s = tg == null ? "-" : getTarget().getAllText(edit ? 3 : 1);
        Font fo = getTargetFont();
        g2.setFont(fo);
        Rectangle r = rra;

        int sw = getStringWidth(getTargetFont(), s) + 20;
        if (sw > r.width) {
            double ff = r.width / (double) sw;
            setTargetFont(getTargetFont().getSize() * ff);
            repaint();
        }
    }

    BoxState box_state = new BoxState();

    Box hitBox(int x, int y, char type, int[] where) {
        if (getAllBox() != null) {
            Iterator it = getAllBox().all.values().iterator();
            while (it.hasNext()) {
                Box bx = (Box) it.next();
                int bhit;
                if ((bhit = bx.hitBox(x, y)) >= 0) {
                    where[0] = bhit;
                    if (type == 'm') {
                        box_state.setState(bx, SELECTED, true);
                        box_state.setState(bx, MARKED, true);
                    } else if (type == 'p') {
                        box_state.setState(bx, ACTIVATED, true);
                        box_state.setState(bx, SELECTED, true);
                        box_state.setState(bx, MARKED, true);
                    }
                    return bx;
                }
            }
        }
        box_state.setState(null, SELECTED, false);
        box_state.setState(null, BUSY, false);
        return null;
    }

    int hitTarget(int hx, int hy, char type) {
        if (getTarget() != null) {
            for (int i = 0; i < getTarget().get_howManyT_Items(); i++) {
                Rectangle r = getTargetRectangle(i);
                if (r.contains(hx, hy)) {
                    return i;
                }
            }
        }
        return -1;
    }

    void addTgAddBox(ShapeList sbl, Double[] da, double y) {
        double d = getCaW() / 60;
        Color col = new Color(150, 150, 150);
        for (int i = 0; i < da.length; i += 2) {
            RoundRectangle2D r = new RoundRectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - d / 2 + 0.5,
                    y + 0.5,
                    d + 0.5,
                    d + 0.5,
                    d / 4 + 0.5,
                    d / 4 + 0.5);
            Rectangle2D rs = new Rectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - 0.35 * d + 0.5,
                    y + 0.4 * d + 0.5,
                    0.7 * d + 0.5,
                    0.2 * d + 0.5);
            Rectangle2D rt = new Rectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - 0.1 * d + 0.5,
                    y + 0.15 * d + 0.5,
                    0.2 * d + 0.5,
                    0.7 * d + 0.5);
            sbl.add("" + i / 2, r, col);
            Area a = new Area(rs);
            a.add(new Area(rt));
            if (da.length < 7 * 2) {
                sbl.add("", a, Color.white);
            }
        }
    }

    void addTgDelBox(ShapeList sbl, Double[] da, double y) {
        double d = getCaW() / 60;
        Color col = new Color(150, 150, 150);
        for (int i = 0; i < da.length; i += 2) {
            RoundRectangle2D r = new RoundRectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - d / 2 + 0.5,
                    y + 0.5,
                    d + 0.5,
                    d + 0.5,
                    d / 4 + 0.5,
                    d / 4 + 0.5);
            Rectangle2D rs = new Rectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - 0.35 * d + 0.5,
                    y + 0.4 * d + 0.5,
                    0.7 * d + 0.5,
                    0.2 * d + 0.5);
//  	    Rectangle2D rt = new Rectangle2D.Double((da[i].doubleValue() + da[i+1].doubleValue()) / 2 - 0.1 * d + 0.5,
//  						    y + 0.15 * d + 0.5,
//  						    0.2 * d + 0.5,
//  						    0.7 * d + 0.5);
            sbl.add("" + i / 2, r, col);
            Area a = new Area(rs);
//	    a.add(new Area(rt));
            sbl.add("", a, Color.white);
        }
    }

    void addAddBox(ShapeList sbl, Double[] da, double y) {
        double d = getCaW() / 60;
        Color col = new Color(150, 150, 150);
        for (int i = 0; i < da.length; i += 2) {
            RoundRectangle2D r = new RoundRectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - d / 2 + 0.5,
                    y + 0.5,
                    d + 0.5,
                    d + 0.5,
                    d / 4 + 0.5,
                    d / 4 + 0.5);
            Rectangle2D rs = new Rectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - 0.35 * d + 0.5,
                    y + 0.4 * d + 0.5,
                    0.7 * d + 0.5,
                    0.2 * d + 0.5);
            Rectangle2D rt = new Rectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - 0.1 * d + 0.5,
                    y + 0.15 * d + 0.5,
                    0.2 * d + 0.5,
                    0.7 * d + 0.5);
            sbl.add("" + i / 2, r, col);
            Area a = new Area(rs);
            a.add(new Area(rt));
            sbl.add("", a, Color.white);
        }
    }

    void addDelBox(ShapeList sbl, Double[] da, Double[] day, boolean ba[]) {
        double d = getCaW() / 60;
        Color col = new Color(150, 150, 150);
        for (int i = 0; i < da.length; i += 2) {
            RoundRectangle2D r = new RoundRectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - d / 2 + 0.5,
                    day[i / 2].doubleValue() + 0.5,
                    d + 0.5,
                    d + 0.5,
                    d / 4 + 0.5,
                    d / 4 + 0.5);
            Rectangle2D rs = new Rectangle2D.Double((da[i].doubleValue() + da[i + 1].doubleValue()) / 2 - 0.35 * d + 0.5,
                    day[i / 2].doubleValue() + d * 0.4 + 0.5,
                    0.7 * d + 0.5,
                    0.2 * d + 0.5);
            if (ba[i / 2]) {
                sbl.add("" + i / 2, r, col);
                sbl.add("", rs, Color.white);
            }
        }
    }

    void createAddDelBoxes() {
        createAddBoxes();
        createDelBoxes();
        createTgAddBoxes();
        createTgDelBoxes();
    }

    void createAddDelBoxesTgOnly() {
        createTgAddBoxes();
        createTgDelBoxes();
    }

    void createAddBoxes() {
        ShapeList naddBoxes = new ShapeList();

        java.util.List<Double> li = new ArrayList();
        li.add(Double.valueOf(0.0));

        double y = 0;
        for (int i = 0; i < 20; i++) {
            Box bx = getAllBox().getBox(i, 0);
            if (bx == null) {
                continue;
            }
            Rectangle2D rr = getAllBox().getBound(i, 0);
            li.add(Double.valueOf(rr.getX()));
            li.add(Double.valueOf(rr.getX() + rr.getWidth()));
            y = rr.getY();
        }
        li.add(Double.valueOf(getCaW()));

        Double[] da = (Double[]) li.toArray(new Double[0]);
        Box bx = getAllBox().getBox(0, 0);
        if (bx != null) {
            addAddBox(naddBoxes, da, bx.r.getY());
        }

        addBoxes = naddBoxes;
    }

    void createTgAddBoxes() {
        ShapeList naddBoxes = new ShapeList();

        java.util.List<Double> li = new ArrayList();
        Rectangle2D r0 = getTargetRectangle();
        li.add(Double.valueOf(r0.getX()));

        double d = 0.0;

        for (int i = 0; i < getTarget().get_howManyT_Items(); i++) {
            Rectangle rr = getTargetRectangle(i);
            li.add(Double.valueOf(rr.getX()));
            li.add(Double.valueOf(d = rr.getX() + rr.getWidth()));
        }
        li.add(Double.valueOf(d + 20));
        Double[] da = (Double[]) li.toArray(new Double[0]);
        addTgAddBox(naddBoxes, da, r0.getY() + 4);

        tgAddBoxes = naddBoxes;
    }

    void createTgDelBoxes() {
        ShapeList naddBoxes = new ShapeList();

        java.util.List<Double> li = new ArrayList();
        Rectangle2D r0 = getTargetRectangle();

        double d = 0.0;

        for (int i = 0; i < getTarget().get_howManyT_Items(); i++) {
            Rectangle rr = getTargetRectangle(i);
            li.add(Double.valueOf(rr.getX()));
            li.add(Double.valueOf(d = rr.getX() + rr.getWidth()));
        }
        Double[] da = (Double[]) li.toArray(new Double[0]);
        double dh = getCaW() / 60;
        addTgDelBox(naddBoxes, da, r0.getY() + /*r0.getHeight() + */ -dh - 4);

        tgDelBoxes = naddBoxes;
    }

    void createDelBoxes() {
        ShapeList ndelBoxes = new ShapeList();

        boolean[] ba = new boolean[20];
        java.util.List<Double> li = new ArrayList();
        java.util.List<Double> liy = new ArrayList();

        boolean hasmore1 = true;
        Box bx = getAllBox().getBox(1, 0);
        if (bx == null) {
            hasmore1 = false;
        }

        for (int ix = 0; ix < 20; ix++) {
            for (int iy = 20; iy >= 0; iy--) {
                bx = getAllBox().getBox(ix, iy);
                if (bx == null) {
                    continue;
                }
                if (ix > 0) {
                    hasmore1 = true;
                }
                Rectangle2D rr = getAllBox().getBound(ix, iy);
                if (rr == null) {
                    continue;
                }
                Item itm = getTarget().getItemAt(ix, iy);
                if (hasmore1) {
                    ba[ix] = true;
                }
                li.add(Double.valueOf(rr.getX()));
                li.add(Double.valueOf(rr.getX() + rr.getWidth()));
                liy.add(Double.valueOf(rr.getY() + rr.getHeight() + 5));
                break;
            }
        }

        Double[] da = (Double[]) li.toArray(new Double[0]);
        Double[] day = (Double[]) liy.toArray(new Double[0]);
        bx = getAllBox().getBox(0, 0);

        if (bx != null) {
            addDelBox(ndelBoxes, da, day, ba);
        }

        delBoxes = ndelBoxes;
    }

    double wantRP = 1.0;

    void drawBoxes(Graphics2D g2) {
        synchronized (allbox_sy) {
            if (getAllBox() != null) {
                wantRP = 1.0;
                int o_x = 0;
                Iterator it = getAllBox().all.values().iterator();
                while (it.hasNext()) {
                    Box bx = (Box) it.next();
                    if (bx.o_x > o_x) {
                        o_x = bx.o_x;
                    }
                    bx.drawNull(g2);
                }
                //OmegaContext.sout_log.getLogger().info(":--: " + "wantRP is' " + wantRP);

                if (wantRP >= 1.005 || wantRP <= 0.95) {
                    int fs = item_fo.getSize();
                    setItemFont(new Font("Arial", Font.PLAIN, (int) (fs / (1.01 * wantRP))));
                    setItemFont2(new Font("Arial", Font.PLAIN, (int) (fs / (2 * (1.01 * wantRP)))));
                    repaint();
                    return;
                }

                it = getAllBox().all.values().iterator();
                while (it.hasNext()) {
                    Box bx = (Box) it.next();
                    if (bx.o_x > o_x) {
                        o_x = bx.o_x;
                    }
                    bx.draw(g2);
                }
                if (edit) {
                    addBoxes.draw(g2);
                    delBoxes.draw(g2);
                    tgAddBoxes.draw(g2);
                    tgDelBoxes.draw(g2);
                }
            }
        }
    }

    void drawArrows(Graphics2D g2) {
        if (!edit) {
            return;
        }
        BasicStroke stroke = new BasicStroke(1f);
        g2.setStroke(stroke);
        g2.setColor(new Color(111, 111, 111));

        if (getAllBox() != null) {
            synchronized (allbox_sy) {
                Iterator it = getAllBox().all.values().iterator();
                while (it.hasNext()) {
                    Box bx = (Box) it.next();
                    if (bx.o_y == 0) {
                        Item itm = bx.getItem();
                        if (itm == null) {
                            continue;
                        }

                        int msk = getTarget().whatTargetMatchTid(itm.getEntryTid());
                        int how_many = Num.howManyBits(msk);
                        int msk_cnt = 0;
                        for (int i = 0; i < getTarget().get_howManyT_Items(); i++) {
                            if (((1 << i) & msk) != 0) {
                                Target.T_Item tit = getTarget().getT_Item(i);
                                Rectangle tgr = getTargetRectangle(i);

                                int msk_cnt_w = bx.w / how_many;

                                Font fo = getTargetFontAlt();
                                g2.setFont(fo);
                                g2.setColor(Color.black);
                                g2.drawString(tit.tid, tgr.x + 15, tgr.y - 8);   // ordtyp
                                int txtH = getCaH() / 30;
                                if (tit.lid != null) {
                                    String ls = tit.getLID4TgOrNull();         // banid
                                    if (ls != null) {
                                        g2.drawString(ls, tgr.x + 15, tgr.y - 8 - txtH);
                                    }
                                    String lt = tit.getLIDText();             // aktörsid
                                    if (lt != null) {
                                        g2.drawString(lt, tgr.x + 15, tgr.y - 8 - txtH - txtH);
                                    }
                                }
                                if (i == 0) {
                                    Font fos = getTargetFontAltS();
                                    g2.setFont(fos);
                                    g2.drawString(T.t("Label:"), 5, tgr.y - 8);   // ordtyp
                                    g2.drawString(T.t("Path id:"), 5, tgr.y - 8 - txtH);
                                    g2.drawString(T.t("Actor id:"), 5, tgr.y - 8 - txtH - txtH);
                                }

                                g2.setColor(new Color(111, 111, 111));

                                int x1 = bx.x + msk_cnt_w * msk_cnt + msk_cnt_w / 2;
                                int y1 = bx.y - 3;
                                int x2 = tgr.x + tgr.width / 2;
                                int y2 = tgr.y + tgr.height + 3;
                                g2.drawLine(x1, y1, x2, y2);
                                msk_cnt++;
                            }
                        }
                    }
                }
            }
        }
    }

    void drawQuitButton(Graphics2D g2) {
        if (!edit) {
            if (quit_disabled) {
                return;
            }
            double d = getCaW() / 60;
            double xx = 0.05 * getCaW();
            double yy = (1.0 - 0.02 - 0.04) * getCaH();
            yy -= 20;
            double ww = 0.1 * getCaW();
            double hh = (0.04) * getCaH();
            RoundRectangle2D r = new RoundRectangle2D.Double(xx,
                    yy,
                    ww,
                    hh,
                    d / 4 + 0.5,
                    d / 4 + 0.5);
            g2.setColor(getColor("bt_bg"));
            g2.fill(r);
            g2.setColor(getColor("bt_fr"));
            if (quit_state == 1 || quit_state == 2) {
                g2.setColor(new Color(222, 44, 44));
            }
            g2.draw(r);
            double xx2 = 0.055 * getCaW();
            double yy2 = (1.0 - 0.02 - 0.01) * getCaH();
            yy2 -= 20;
            Font fo = getTargetFontAlt();
            g2.setFont(fo);
            g2.setColor(getColor("bt_tx"));
            if (quit_state == 2 || quit_state == 3) {
                g2.drawString(T.t("Cancel"), (int) xx2, (int) yy2);
            } else {
                g2.drawString(T.t("Quit"), (int) xx2, (int) yy2);
            }
        }
    }

    void drawExtraQuitButton(Graphics2D g2) {
        if (quit_state == 2 || quit_state == 3) {
            if (!edit) {
                if (quit_disabled) {
                    return;
                }
                double d = getCaW() / 60;
                double xx = 0.05 * getCaW();
                double yy = (1.0 - 0.02 - 0.04) * getCaH();
                yy -= 20;
                double ww = 0.1 * getCaW();
                double hh = (0.04) * getCaH();
                xx += ww + hh / 2;
                RoundRectangle2D r = new RoundRectangle2D.Double(xx,
                        yy,
                        ww,
                        hh,
                        d / 4 + 0.5,
                        d / 4 + 0.5);
                g2.setColor(getColor("bt_bg"));
                g2.fill(r);
                g2.setColor(getColor("bt_fr"));
                if (quit_state == 3) {
                    g2.setColor(new Color(222, 44, 44));
                }
                g2.draw(r);
                double xx2 = 0.055 * getCaW();
                xx2 += ww + hh / 2;
                double yy2 = (1.0 - 0.02 - 0.01) * getCaH();
                yy2 -= 20;
                Font fo = getTargetFontAlt();
                g2.setFont(fo);
                g2.setColor(getColor("bt_tx"));
                g2.drawString(T.t("Quit"), (int) xx2, (int) yy2);
            }
        }
    }

    boolean hitQuitButton(int x, int y) {
        if (!edit) {
            double d = getCaW() / 60;
            double xx = 0.05 * getCaW();
            double yy = (1.0 - 0.02 - 0.04) * getCaH();
            yy -= 20;
            double ww = 0.1 * getCaW();
            double hh = (0.04) * getCaH();
            if (x > xx && x < xx + ww
                    && y > yy && y < yy + hh) {
                return true;
            }
        }
        return false;
    }

    boolean hitExtraQuitButton(int x, int y) {
        if (!edit) {
            double d = getCaW() / 60;
            double xx = 0.05 * getCaW();
            double yy = (1.0 - 0.02 - 0.04) * getCaH();
            yy -= 20;
            double ww = 0.1 * getCaW();
            double hh = (0.04) * getCaH();
            xx += ww + hh / 2;
            if (x > xx && x < xx + ww
                    && y > yy && y < yy + hh) {
                return true;
            }
        }
        return false;
    }

    private void repaintQuitButton() {
        if (!edit) {
            double d = getCaW() / 60;
            double xx = 0.05 * getCaW();
            double yy = (1.0 - 0.02 - 0.04) * getCaH();
            yy -= 20;
            double ww = 0.1 * getCaW();
            double hh = (0.04) * getCaH();
            ww *= 3;
            repaint((int) xx - 5, (int) yy - 5, (int) ww + 10, (int) hh + 30);
        }
    }

    class MsgDialog {

        MsgItem msg_item;
        String cont_image_fn = "media/default/continue.png";

        void show(MsgItem msg) {
            set(msg);
            long ct0 = SundryUtils.ct();
            while (show_msg) {
                SundryUtils.m_sleep(200);
                if ((SundryUtils.ct() - ct0) > 1000 * 30) {
                    return;
                }
            }
        }

        void showNoWait(MsgItem msg) {
            set(msg);
        }

        void set(MsgItem msg) {
            if (msg == null) {
                m.mode = m.NORM;
                show_msg = false;
                OmegaContext.sout_log.getLogger().info(":--: " + "MSGITEM null");
            } else {
                m.mode = m.MSG;
                show_msg = true;
                OmegaContext.sout_log.getLogger().info(":--: " + "MSGITEM " + msg);
            }

            msg_item = msg;
            int w = gX(0.6);
            int h = gY(0.35);
            int x = gX(0.2);
            int y = gY(0.2);
            repaint(x - 5, y - 5, w + 15, h + 15);
        }

        private int f(int row, int tot, int rows) {
            return row * tot / rows;
        }

        void draw(Graphics2D g2) {
            int w = gX(0.5);
            int h = gY(0.35);
            int th = gY(0.039);
            int x = gX(0.25);
            int y = gY(0.2);
            int r = gX(0.02);
            if (msg_item.type == 'W' || msg_item.type == 'S') {
                w = gX(0.6);
                x = gX(0.2);
            }

            RoundRectangle2D fr = new RoundRectangle2D.Double(x, y, w, h, r, r);

            g2.setColor(getColor("bg_frbg"));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.fill(fr);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // titlebar
            g2.setColor(new Color(88, 88, 88));
            g2.setClip(fr);
            g2.fill(new Rectangle2D.Double(x, y, w, th));

            BasicStroke stroke = new BasicStroke(getCaH() / 200f);
            g2.setStroke(stroke);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(getColor("bg_fr"));
            g2.setClip(0, 0, 10000, 10000);
            g2.draw(fr);


            g2.setClip(0, 0, 10000, 10000);//	    g2.setClip(fr);
            g2.setColor(Color.black);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            g2.setColor(getColor("bg_tx"));

            if (msg_item.type == '2') {
                int yy = 0;
                int f_f = 30;//item_fo.getSize();
                Font item_fot = new Font("Arial", Font.PLAIN, f_f);
                for (; ; ) {
                    item_fot = new Font("Arial", Font.PLAIN, f_f);
                    int sw = getStringWidth(item_fot, msg_item.text);
                    if (sw > (w - 20)) {
                        f_f = (int) (0.9 * f_f);
                    } else {
                        g2.setFont(item_fot);
                        break;
                    }
                }
                g2.setFont(item_fot);
                g2.drawString(msg_item.text,
                        x + f(4, w, 50),
                        y + f(6, h, 10));
            } else if (msg_item.type == 'W') {
                int yy = 0;
                int f_f = 30;//item_fo.getSize();
                Font item_fot = new Font("Arial", Font.PLAIN, f_f);
                for (; ; ) {
                    item_fot = new Font("Arial", Font.PLAIN, f_f);
                    int sw = getStringWidth(item_fot, msg_item.text);
                    if (sw > (w - 20)) {
                        f_f = (int) (0.9 * f_f);
                    } else {
                        g2.setFont(item_fot);
                        break;
                    }
                }
                g2.setFont(item_fot);
                g2.drawString(msg_item.small_title,
                        x + f(12, w, 50),
                        y + f(3, h, 10));
                g2.drawString(msg_item.text,
                        x + f(4, w, 50),
                        y + f(6, h, 10));
            } else if (msg_item.type == 'S') {
                int f_f = 30;//item_fo.getSize();
                Font item_fot = new Font("Arial", Font.PLAIN, f_f);
                for (; ; ) {
                    item_fot = new Font("Arial", Font.PLAIN, f_f);
                    int sw = getStringWidth(item_fot, msg_item.text);
                    if (sw > (w - 20)) {
                        f_f = (int) (0.9 * f_f);
                    } else {
                        g2.setFont(item_fot);
                        break;
                    }
                }

                g2.setFont(item_fot);
                g2.drawString(msg_item.text,
                        x + f(12, w, 50),
                        y + f(3, h, 10));
                g2.drawString(msg_item.text2,
                        x + f(12, w, 50),
                        y + f(6, h, 10));
            } else if (msg_item.type == 'R') {
                int f_f = 30;//item_fo.getSize();
                Font item_fot = new Font("Arial", Font.PLAIN, f_f);
                for (; ; ) {
                    item_fot = new Font("Arial", Font.PLAIN, f_f);
                    int sw = getStringWidth(item_fot, msg_item.text);
                    if (sw > (w - 20)) {
                        f_f = (int) (0.9 * f_f);
                    } else {
                        g2.setFont(item_fot);
                        break;
                    }
                }

                g2.setFont(item_fot);
                g2.drawString(msg_item.text,
                        x + f(2, w, 5),
                        y + f(17, h, 30));
            }

            g2.setColor(getColor("bg_tx"));
            g2.setFont(getTitleFont());
            g2.drawString(msg_item.title, x + 1 * w / 10, (int) (y + gY(0.03)));

            int HH = (int) (h * 0.35);
            if (msg_item.image != null) {
                int hh = (int) (h * 0.35);
                int ww = (4 * hh) / 3;
                try {
                    Image img = ScaledImageIcon.createImageIcon(LessonCanvas.this,
                            msg_item.image,
                            ww,
                            hh).getImage();
                    HH = img.getHeight(null);
                    g2.drawImage(img, x + 3, y + th + 3, null);
                } catch (Exception ex) {
                }
            }
            if (msg_item.image2 != null) {
                int hh = (int) (h * 0.35);
                int ww = (4 * hh) / 3;
                try {
                    Image img = ScaledImageIcon.createImageIcon(LessonCanvas.this,
                            msg_item.image2,
                            ww,
                            hh).getImage();
                    g2.drawImage(img, x + 3, y + th + 3 + HH + 5, null);
                } catch (Exception ex) {
                }
            }
            if (cont_image_fn != null) {
                int hh = (int) (h * 0.25);
                int ww = hh * 4;
                try {
                    Image img = ScaledImageIcon.createImageIcon(LessonCanvas.this,
                            cont_image_fn,
                            ww,
                            hh).getImage();
                    int imw = img.getWidth(null);
                    g2.drawImage(img, x + w - imw - 3, y + h - hh - 3, null);
                } catch (Exception ex) {
                }
            }

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
        }
    }

    MsgDialog msg_dlg = new MsgDialog();

    public void showMsg(MsgItem mi) {
        msg_dlg.show(mi);
    }

    public void showMsgNoWait(MsgItem mi) {
        msg_dlg.showNoWait(mi);
        SundryUtils.m_sleep(3000);
    }

    public boolean isMsg() {
        return show_msg;
    }

    public void hideMsg() {
        msg_dlg.set(null);
    }

    Rectangle signMovieRectangle = null;

    public void setSignMovieRectangle(Rectangle r) {
        signMovieRectangle = r;
    }

    static private int mist_mode = 0;
    Shape mist_blueSky = null;
    Color mistBgCol;
    int mistAlpha;

    public void setMist(int mode, Shape blueSky, Color bgCol, int alpha) {
        this.mist_mode = mode;
        this.mist_blueSky = blueSky;
        this.mistBgCol = bgCol;
        this.mistAlpha = alpha;
        repaint();
    }

    public void paintComponent(Graphics g) {
        long ct0 = SundryUtils.ct();
        Graphics2D g2 = (Graphics2D) g;

        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        drawBG(g2);

        g.setColor(Color.black);
        Font fo = g.getFont();

        drawTargetNull((Graphics2D) g);
        drawTarget((Graphics2D) g);
        drawBoxes((Graphics2D) g);
        drawArrows((Graphics2D) g);

        if (show_msg) {
            try {
                msg_dlg.draw(g2);
            } catch (Exception ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "Can't show msg");
            }
        }

        drawQuitButton(g2);
        drawExtraQuitButton(g2);

        if (mist_mode > 0) {
            if (signMovieRectangle != null)
                ;//g2.draw(signMovieRectangle);
            drawMist(g2, LiuMovieManager.repeat_mode == LiuMovieManager.RepeatMode.DO_REPEAT ? 2 : 1, mist_blueSky, mistBgCol, mistAlpha, signMovieRectangle);
        }

        long ct1 = SundryUtils.ct();
    }

    public Element getElement() {
        Element el = new Element("lesson_canvas");
        el.addAttr("show_wordbox", "" + show_wordbox);
        fillElement(el);
        return el;
    }

    public void setFrom(Element el, boolean dummy) {
        resetItemFont();
        if (lep != null) {
            lep.destroyAllPopups();
        }

        Element lel = el.findElement("lesson_canvas", 0);
        if (lel != null) {
            String s = lel.findAttr("show_wordbox");
            if (s != null && s.equals("true")) {
                show_wordbox = true;
            } else {
                show_wordbox = false;
            }
        } else {
            show_wordbox = true;
        }

        lesson_name = "-noname-";
//log	OmegaContext.sout_log.getLogger().info(":--: " + "=-= dep_set lesson_name ");
        Element lesson_el = el.findElement("lesson", 0);
        if (lesson_el != null) {
            String nm = lesson_el.findAttr("name");
            if (nm != null) {
                lesson_name = nm;
            }
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "=-= lesson_name is " + lesson_name);
        }

        lesson_link_next = null;
        Element lesson2_el = el.findElement("story", 0);
        if (lesson2_el != null) {
            Element lel2 = el.findElement("link", 0);
            if (lel2 != null) {
                String nm = lel2.findAttr("next");
                if (nm != null) {
                    lesson_link_next = nm;
                }
            }
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "=-= lesson_link_next is " + lesson_link_next);
        }

        lesson_is_first = false;
        Element lesson3_el = el.findElement("story", 0);
        if (lesson3_el != null) {
            String nm = lesson3_el.findAttr("isfirst");
            if (nm != null) {
                if (nm.equalsIgnoreCase("yes") || nm.equals("1")) {
                    lesson_is_first = true;
                }
            }
        }

        if (lep != null) {
            lep.setLessonName(lesson_name);
            lep.setLessonLinkNext(lesson_link_next);
            lep.setLessonIsFirst(lesson_is_first);
        }
    }

    public void disposeOldLesson() {
        setAllBox(null);
        repaint();
    }

    public void enter() {
        super.enter();
        if (all_Box == null) {
            repaint();
        }
        if (false && l_ctxt.getLesson().isTestMode()) {
            gotoNextSmartBox();
        } else {
            gotoQuit();
        }
    }

    public void leave() {
        super.leave();
    }

    void setQuitState(String s, int val) {
        quit_state = val;
        repaintQuitButton();
    }

    public void resetHboxFocus() {
        eraseHilitedBox();
        gotoBox(0, 0);
// 	Box bx = getAllBox().getBox(0, 0);
// 	if ( bx != null ) {
// 	    box_state.setState(bx, BUSY, false);
// 	    box_state.setState(bx, MARKED, false);
// 	    box_state.setState(bx, SELECTED, false);
// 	    bx.repaintBox();
// 	}
        gotoBox(0, 0);
    }

    public void enableQuitButton(boolean b) {
        quit_disabled = !b;
        repaintQuitButton();
    }
}
