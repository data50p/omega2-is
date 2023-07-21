package com.femtioprocent.omega.lesson.canvas;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.message.Manager;
import com.femtioprocent.omega.swing.ScaledImageIcon;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BaseCanvas extends JPanel {
    public Manager om_msg_mgr = new Manager();
    EventListenerList lc_listeners;
    LessonContext l_ctxt;

    public HashMap<String,Color> colors = new HashMap();

    Font fo;


    boolean ignore_press = false;

    ComponentAdapter cmp_li = new ComponentAdapter() {
        public void componentResized(ComponentEvent ev) {
            resized();
        }
    };

    ActionListener act_li = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            String ac = ae.getActionCommand();
            OmegaContext.def_log.getLogger().info("Fire... " + ac);
            if (!ignore_press) {
                MyButton mb = (MyButton) ae.getSource();
                MyButton ob = focus_list.get();
                if (ob != null) {
                    ob.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                    ob.repaint();
                }
                mb.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 120), 5));
                om_msg_mgr.fire("button " + ac);
                mb.setBorder(BorderFactory.createLineBorder(new Color(242, 80, 80), 5));
            }
            OmegaContext.def_log.getLogger().info("Fired " + ac);
        }
    };

    class FrameState {
        String states = "A";
        int ix = 0;

        FrameState() {
            setStates("A", 0);
        }

        void next() {
            ix++;
            if (ix >= states.length())
                ix = 0;
        }

        void prev() {
            ix--;
            if (ix < 0)
                ix = states.length() - 1;
        }

        char get() {
            return states.charAt(ix);
        }

        void set(char ch) {
            for (int i = 0; i < states.length(); i++)
                if (states.charAt(i) == ch) {
                    ix = i;
                    return;
                }
        }

        void setStates(String ss, int n) {
            char ch = get();
            for (int i = 0; i < n; i++)
                ss += "" + i;
            set(ch);
        }
    }

    FrameState frame_state = new FrameState();

    public class ImageArea extends JLabel {
        int ord;
        String im_n, im_enter_n;
        ImageIcon im_ic, im_enter_ic;
        int w, h;
        public Object o;

        ImageArea(String txt, int ord, String im_n, String im_enter_n, int w, int h, Object o) {
            super(txt);
            this.w = w;
            this.h = h;
            this.ord = ord;
            this.im_n = im_n;
            this.im_enter_n = im_enter_n;
            this.o = o;
            setForeground(Color.black);
        }

        class Mouse extends MouseInputAdapter {
            Point2D mpress_p;

            Mouse() {
                addMouseListener(this);
                addMouseMotionListener(this);
            }

            public void mousePressed(MouseEvent e) {
                mpress_p = new Point2D.Double(e.getX(), e.getY());
            }

            public void mouseMoved(MouseEvent e) {
            }

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                om_msg_mgr.fire("imarea " + getPanelName() + ":lesson_" + ord);
            }

            public void mouseEntered(MouseEvent e) {
                showEnter();
            }

            public void mouseExited(MouseEvent e) {
                showNormal();
            }
        }

        Mouse m = new Mouse();

        void setNewImage(String fn, String fn_e) {
            im_enter_n = fn_e;
            im_n = fn;
            im_enter_ic = null;
            im_ic = null;
            showEnter();
        }

        void showNormal() {
            if (im_ic == null)
                im_ic = createImageIcon(im_n, w, h);
            setIcon(im_ic);
            setVerticalTextPosition(SwingConstants.BOTTOM);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            repaint();
        }

        void showEnter() {
            if (im_enter_ic == null)
                im_enter_ic = createImageIcon(im_enter_n, w, h);
            setIcon(im_enter_ic);
            repaint();
        }

        ImageIcon createImageIcon(String fn, int max_w, int max_h) {
            return ScaledImageIcon.createImageIcon(BaseCanvas.this,
                    fn,
                    max_w,
                    max_h);
        }

        public void setText(String s) {
            super.setText(s);
        }

        public void paintComponent(Graphics g) {
            if (im_ic == null)
                showNormal();
            super.paintComponent(g);
        }
    }

    public class ImageAreaJB extends JButton implements ActionListener {
        int ord;
        String im_n, im_enter_n;
        ImageIcon im_ic, im_enter_ic;
        int w, h;
        public Object o;

        ImageAreaJB(String txt, int ord, int w, int h, String dispName) {
            super(dispName != null ? dispName : txt);
            this.w = w;
            this.h = h;
            this.ord = ord;
            this.im_n = null;
            this.im_enter_n = null;
            setForeground(Color.black);
            addActionListener(this);
//	    addFocusListener(background_FA);
            setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 5));
            setFocusTraversalKeysEnabled(false);
            setRequestFocusEnabled(false);
            setFocusable(false);
        }

        void setNew(String txt, String im_n, String im_enter_n, Object o, String dispName) {
            if ( dispName != null ) {
                setText(dispName);
            } else {
                setText(SundryUtils.formatDisplayText(txt));
            }
            this.im_n = im_n;
            this.im_enter_n = im_enter_n;
            this.o = o;
            im_ic = im_enter_ic = null;
            setForeground(Color.black);
            setVisible(true);
            repaint();
        }

        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }

        public void actionPerformed(ActionEvent ae) {
            ImageAreaJB ima = (ImageAreaJB) (ae.getSource());
            om_msg_mgr.fire("imarea " + getPanelName() + ":lesson_" + ima.ord);
        }

        class Mouse extends MouseInputAdapter {
            Point2D mpress_p;

            Mouse() {
                addMouseListener(this);
                addMouseMotionListener(this);
            }

            public void mousePressed(MouseEvent e) {
                mpress_p = new Point2D.Double(e.getX(), e.getY());
                setRedOtherOffIMJB();
            }

            public void mouseMoved(MouseEvent e) {
            }

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
                //om_msg_mgr.fire("imarea " + getPanelName() + ":lesson_" + ord);
            }

            public void mouseEntered(MouseEvent e) {
                showEnter();
            }

            public void mouseExited(MouseEvent e) {
                showNormal();
            }
        }

        Mouse m = new Mouse();

        void setNewImage(String fn, String fn_e) {
            im_enter_n = fn_e;
            im_n = fn;
            im_enter_ic = null;
            im_ic = null;
            showEnter();
        }

        void showNormal() {
            if (im_n != null) {
                im_ic = createImageIcon(im_n, w, h - 13);
                setIcon(im_ic);
            }
            setVerticalTextPosition(SwingConstants.BOTTOM);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            repaint();
        }

        void showEnter() {
            if (im_enter_n != null) {
                if (im_enter_ic == null)
                    im_enter_ic = createImageIcon(im_enter_n, w, h - 13);
                setIcon(im_enter_ic);
            }
            repaint();
        }

        ImageIcon createImageIcon(String fn, int max_w, int max_h) {
            return ScaledImageIcon.createImageIcon(BaseCanvas.this,
                    fn,
                    max_w,
                    max_h);
        }

        public void setText(String s) {
            super.setText(SundryUtils.formatDisplayText(s));
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            RenderingHints rh = g2.getRenderingHints();
            rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
            g2.setRenderingHints(rh);

            if (im_ic == null) {
                showNormal();
            }
            super.paintComponent(g);
        }

        public void setNoRedIMJB() {
            setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 5));
        }

        public void setRedIMJB() {
            setBorder(BorderFactory.createLineBorder(new Color(242, 80, 80), 5));
        }

        public void setRedOtherOffIMJB() {
            if (BaseCanvas.this instanceof LessonMainCanvas)
                ((LessonMainCanvas) BaseCanvas.this).setRed(ord);
        }
    }

    public BaseCanvas(LessonContext l_ctxt) {
        initColors();
        this.l_ctxt = l_ctxt;
        setLayout(null);
        addComponentListener(cmp_li);
        lc_listeners = new EventListenerList();
        enableEvents(Event.KEY_PRESS);
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

    String getPanelName() {
        return "base";
    }

    protected void resized() {
    }

    int gX(double f) {
        return (int) (f * getCaW());
    }

    int gY(double f) {
        return (int) (f * getCaH());
    }

    void set(JButton b) {
        b.setFont(fo);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    void set(JLabel b) {
        b.setFont(fo);
        b.setVerticalTextPosition(SwingConstants.TOP);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    void setAlt(JLabel b) {
        b.setFont(fo);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setHorizontalAlignment(SwingConstants.CENTER);
    }

    void setAlt(JButton b) {
        b.setFont(fo);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setHorizontalAlignment(SwingConstants.CENTER);
    }

    class MouseB extends MouseInputAdapter {
        MouseB() {
        }

        public void mousePressed(MouseEvent e) {
            if (!buttons_enabled)
                return;

            JButton jb = (JButton) e.getSource();
            int ord = ((Integer) jb.getClientProperty("ord")).intValue();
            for (int i = 0; i < 5; i++)
                if (i == ord) {
                    setRedBut(i);
                } else {
                    setNoRedBut(i);
                }
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    MouseB mouseb = new MouseB();

    void adda(JButton b, String cmd) {
        b.addActionListener(act_li);
        b.setActionCommand(getPanelName() + ':' + cmd);
        b.addMouseListener(mouseb);
        b.addMouseMotionListener(mouseb);
//	b.addFocusListener(background_FA_Alt);
        add(b);
    }

    void setJBIcon(JButton b, String fn) {
        try {
            int w = (int) (b.getWidth() * 0.75);
            int h = (int) (b.getHeight() * 0.75);
            if (w == 0)
                w = 20;
            if (h == 0)
                h = 20;
            ImageIcon imic = ScaledImageIcon.createImageIcon(b,
                    fn,
                    w,
                    h);
            b.setIcon(imic);
            setJBROIcon(b, fn.replaceAll("\\.png", "-over.png"));
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "ImageIco size 0,0");
        }
    }

    private void setJBROIcon(JButton b, String fn) {
        int w = (int) (b.getWidth() * 0.75);
        int h = (int) (b.getHeight() * 0.75);
        ImageIcon imic = ScaledImageIcon.createImageIcon(b,
                fn,
                w,
                h);
        if (imic != null) {
            b.setRolloverIcon(imic);
            b.setRolloverEnabled(true);
        }
    }

    Image getImage(String fn) {
        Image image = Toolkit.getDefaultToolkit().getImage(OmegaContext.omegaAssets(fn));
        return image;
    }

    class CycleList {
        int want_first = 0;
        int ix;
        ArrayList<MyButton> li;

        CycleList(int want_first) {
            this.want_first = want_first;
            li = new ArrayList();
            ix = -1;
        }

        void reset() {
            ix = -1;
            li = new ArrayList();
        }

        void add(MyButton b, int grp) {
            li.add(b);
// 	    if ( ix == -1 )
// 		ix = 0;
        }

        void next() {
            if (ix == -1)
                ix = want_first;
            else
                ix++;
            if (ix >= li.size())
                ix = 0;
            MyButton mb = get();
            Boolean B = (Boolean) mb.getClientProperty("skipred");
            if (!mb.isVisible() || B != null && B.booleanValue())
                next();
        }

        void prev() {
// 	    if ( ix == -1 )
// 		return;
            ix--;
            if (ix < 0)
                ix = li.size() - 1;
            MyButton mb = get();
            Boolean B = (Boolean) mb.getClientProperty("skipred");
            if (!mb.isVisible() || B != null && B.booleanValue())
                prev();
        }

        void set(int ix) {
            this.ix = ix;
        }

        MyButton get() {
            if (ix == -1)
                return null;
            return (MyButton) li.get(ix);
        }

        int getIx() {
            return ix;
        }

        boolean isPopup() {
            return ix == 4 || ix == 5;
        }

        void dump() {
        }
    }

    CycleList focus_list;

    class MyButton extends JButton {
        MyButton[] popup = null;

        MyButton(String txt) {
            super(txt);
        }

        void remove() {
            if (popup == null)
                return;
            for (int i = 0; i < popup.length; i++) {
                remove(popup[i]);
            }
            popup = null;
        }

        void setBound(int x, int y, int w, int h) {
            if (popup == null)
                return;
            for (int i = 0; i < popup.length; i++) {
                popup[i].setLocation(x, y - i * h - h);
                popup[i].setSize(w, h);
            }
        }

        private void setPopupVisible(boolean b) {
            if (popup != null)
                for (int i = 0; i < popup.length; i++) {
                    if (b == false)
                        popup[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                    popup[i].setVisible(b);
                }
        }

        void toggleVisible() {
            if (popup != null && popup[0].isVisible())
                setPopupVisible(false);
            else
                setPopupVisible(true);
        }

        void popupVisible(boolean b) {
            if (popup != null)
                setPopupVisible(b);
        }

        void add_fucus_list(CycleList cl, int grp) {
            if (popup != null)
                for (int i = 0; i < popup.length; i++) {
                    cl.add(popup[i], grp);
                }
        }

        public String toString() {
            return getText();
        }

        public void paintComponent(Graphics g) {
            Icon ic = getIcon();
            if (ic == null) {
                String id = getPanelName();
                String s = "toolbarButtonGraphics/omega/" + getActionCommand() + ".png";
                s = s.replaceAll("\\:", "_");
                setJBIcon(this, s);
            }
            super.paintComponent(g);
        }
    }

    MyButton buttons[] = new MyButton[5];
    boolean buttons_enabled = true;


    public void togglePopup(int ix) {
        buttons[ix].toggleVisible();
    }

    public void hidePopup(int ix) {
        buttons[ix].popupVisible(false);
        if (focus_list.isPopup())
            setRedBut(ix);
    }

    public void setBusy(boolean b) {
        MyButton mb = focus_list.get();
        if (mb != null) {
            if (b)
                mb.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 120), 5));
            else
                mb.setBorder(BorderFactory.createLineBorder(new Color(242, 80, 80), 5));
        }
    }

    public void buttonsEnable(boolean b) {
        buttons_enabled = b;

        if (b == false) {
            setAllNoRed();
        } else {
            setRedBut(1);
        }
    }

    void eraseAllOldButtons() {
        Component[] comp = getComponents();
        for (int i = 0; i < comp.length; i++)
            if (comp[i] instanceof MyButton)
                remove((MyButton) (comp[i]));
        comp = null;
        buttons = new MyButton[5];
    }

    void populateButtons(String[] text, String[] cmd) {
        eraseAllOldButtons();
        focus_list.reset();

        String id = getPanelName();
        double h1 = 0.25;
        double h2 = 0.02;
        double h3 = 0.19;
        double hh = (1.0 - h1 - 2 * h2 - h3) / 2;

        int v0 = gY(h2);
        int v1 = gY(h1);
        int v2 = gY(h1 + hh + h2);
        int v3 = gY(h1 + hh + h2 + hh + h2);

        double l1 = 0.005;
        double l2 = 0.2;
        double l25 = 0.30;
        double l3 = 0.4;
        double l4 = 0.6;
        double l5 = 0.8;

        double lA[] = {l1, l2, l3, l4, l5};

        int bw = gX(hh * 0.75);
        int bw1 = gX(hh * 0.73);
        int bh = gY(hh * 0.9);
        int bh2 = gY(hh * 0.55);

        int fs = getCaH() / 48;
        fo = new Font("arial", Font.PLAIN, fs);

        MyButton b;
        for (int i = 0; i < 5; i++) {
            if (buttons[i] == null) {
                String[] textA = SundryUtils.split(text[i], "§");
                String[] cmdA = SundryUtils.split(cmd[i], "§");
                if (cmdA.length > 1) {
                    b = buttons[i] = new MyButton(T.t(textA[0]));
                    b.setRequestFocusEnabled(false);
                    b.setFocusTraversalKeysEnabled(false);
                    b.setFocusable(false);
                    b.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                    b.putClientProperty("ord", i);
                    adda(b, cmdA[0]);
                    if (cmdA[0].equals(""))
                        b.setVisible(false);
                    MyButton[] bA = new MyButton[cmdA.length - 1];
                    for (int ii = 1; ii < cmdA.length; ii++) {
                        b = bA[ii - 1] = new MyButton(T.t(textA[ii]));
                        b.setRequestFocusEnabled(false);
                        b.setFocusTraversalKeysEnabled(false);
                        b.setFocusable(false);
                        b.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                        b.putClientProperty("ord", i);
                        adda(b, cmdA[ii]);
                        if (ii == 2)
                            b.putClientProperty("skipred", Boolean.valueOf(true));
//			setJBIcon(b, "toolbarButtonGraphics/omega/" + id + "_" + cmdA[ii] + ".png");
                        set(b);
                        b.setVisible(false);
                    }
                    buttons[i].popup = bA;
                } else {
                    b = buttons[i] = new MyButton(T.t(text[i]));
                    b.setRequestFocusEnabled(false);
                    b.setFocusTraversalKeysEnabled(false);
                    b.setFocusable(false);
                    b.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                    b.putClientProperty("ord", i);
                    adda(b, cmd[i]);
                    if (cmd[i].equals(""))
                        b.setVisible(false);
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            b = buttons[i];
            b.setSize(i == 0 ? bw1 : bw, bh2);
            b.setLocation(gX(lA[i]), v3);
            b.setBound(gX(lA[i]), v3, i == 0 ? bw1 : bw, bh2);
            setJBIcon(b, "toolbarButtonGraphics/omega/" + id + "_" + cmd[i] + ".png");
            set(b);
            focus_list.add(b, i);
            b.add_fucus_list(focus_list, i);
        }

        focus_list.dump();
    }

    void populate() {
    }

    public void addLessonCanvasListener(LessonCanvasListener l) {
        lc_listeners.add(LessonCanvasListener.class, l);
    }

    public void removeLessonCanvasListener(LessonCanvasListener l) {
        lc_listeners.remove(LessonCanvasListener.class, l);
    }

    public void setColor(String id, Color col) {
        colors.put(id, col);
        repaint(100);
    }

    public void setColor(String id, HashMap<String, Color> col) {
        colors.put(id, col);
        repaint(100);
    }

    public Color getColor(String id) {
        Color col = (Color) colors.get(id);
        if (col == null)
            col = Color.black;
        return col;
    }

    public Color getColor(String id, String mod) {
        if (SundryUtils.empty(mod))
            return getColor(id);


        String idMod = id.replace("hi", "bg").replace("hs", "bg");
        HashMap<String, Color> cols = (HashMap<String, Color>) colors.get(":" + idMod);
        if (cols != null) {
            Color col = cols.get(mod);
            if (col != null) {
                switch (id) {
                    case "bt_hi":
                        col = col.brighter();
                        break;
                    case "bt_hs":
                        col = moreGray(moreGray(col.brighter()));
                }
                return col;
            }
        }

        Color col = (Color) colors.get(id);
        if (col == null)
            col = Color.black;
        return col;
    }

    boolean navigate_gotoRel(int dx, int dy) {
        return false;
    }

    boolean navigate_goto(int x, int y) {
        return false;
    }

    Color moreGray(Color col) {
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int gray = (r + g + b) / 3;
        gray += (255 - gray) / 2;
        r = (int) (((r - gray) * 0.3 + gray) * 1.0);
        g = (int) (((g - gray) * 0.3 + gray) * 1.0);
        b = (int) (((b - gray) * 0.3 + gray) * 1.0);
        return new Color(r, g, b);
    }

    Color lessSaturate(Color col) {
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int gray = (r + g + b) / 3;
        r = (int) ((r - gray) * 0.7 + gray);
        g = (int) ((g - gray) * 0.7 + gray);
        b = (int) ((b - gray) * 0.7 + gray);
        return new Color(r, g, b);
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

    Color invert(Color col) {
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int gray = (r + g + b) / 3;
        r = (int) ((-r + gray) * 1.0 + gray);
        g = (int) ((-g + gray) * 1.0 + gray);
        b = (int) ((-b + gray) * 1.0 + gray);
        return new Color(limit(r), limit(g), limit(b));
    }

    Color left(Color col) {
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int rr = g;
        int gg = b;
        int bb = r;
        return new Color(limit(rr), limit(gg), limit(bb));
    }

    Color right(Color col) {
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int rr = b;
        int gg = r;
        int bb = g;
        return new Color(limit(rr), limit(gg), limit(bb));
    }

    Color diag(Color col) {
        int r = col.getRed();
        int g = col.getGreen();
        int b = col.getBlue();
        int rr = b + g;
        int gg = r + b;
        int bb = g + r;
        return new Color(limit(rr / 2), limit(gg / 2), limit(bb / 2));
    }

    Color markTarget(Color col) {
        return moreSaturate(col);
    }

    Color markItem(Color col) {
        return moreSaturate(col);
    }

    Color markSelectedItem(Color col) {
        return moreSaturate(moreSaturate(col));
    }

    int getStringWidth(Font fo, String s) {
        Graphics2D g2 = (Graphics2D) getGraphics();

        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = fo.getStringBounds(s, frc);
        return (int) r.getWidth();
    }

    int getStringHeight(Font fo, String s) {
        Graphics2D g2 = (Graphics2D) getGraphics();

        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = fo.getStringBounds(s, frc);
        return (int) r.getHeight();
    }

    int getCaW() {
        int r = getWidth();
        return r < 0 ? 0 : r;
    }

    int getCaH() {
        int r = getHeight();
        return r < 0 ? 0 : r;
    }

    public boolean ownKeyCode(int kc, boolean is_shift) {
        if (Lesson.skip_F)
            return true;
        if (ignore_press)
            return true;
        if (OmegaConfig.isKeyNext(kc))
            if (buttons_enabled)
                if (is_shift)
                    setPrevRed();
                else
                    setNextRed();
        if (OmegaConfig.isKeySelect(kc)) {
            MyButton b = focus_list.get();
            if (b != null) {
                if (buttons_enabled)
                    b.doClick();
            }
        }
        return true;
    }

    Color _bg_;

    public void enter() {
    }

    public void leave() {
    }

    public void populateGUI() {
    }

    void drawBG(Graphics2D g2) {
        int hh = (int) (0.23 * getCaH());
        GradientPaint pa = new GradientPaint(0.0f, 0.0f, getColor("bg_t"),
                0.0f, (float) hh, getColor("bg_m"));
        g2.setPaint(pa);
        g2.fill(new Rectangle(0, 0, getCaW(), hh));

        pa = new GradientPaint(0.0f, (float) hh, getColor("bg_m"),
                0.0f, (float) getCaH(), getColor("bg_b"));
        g2.setPaint(pa);
        g2.fill(new Rectangle(0, hh - 1, getCaW(), getCaH() - hh + 1));
    }


    void drawMist(Graphics2D g2, int marker, Shape blueSky, Color bgColor, int alpha, Rectangle mRect) {
        g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (255 * alpha) / 100));

        Rectangle all = new Rectangle(0, 0, getCaW(), getCaH());

        Path2D p = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        p.append(all, false);
        p.append(blueSky, false);
        g2.clip(p);

        g2.fill(new Rectangle(0, 0, getCaW(), getCaH()));

        if (marker == 2) {
//	    g2.setColor(new Color(200, 0, 0, 255));
//	    g2.fill(new Arc2D.Double((int)(blueSky.getBounds().getX() + blueSky.getBounds().getWidth()),
//		    (int)blueSky.getBounds().getY(),
//		    10, 10, 0, 360, 0));
            g2.setColor(new Color(200, 0, 165, 255));
            g2.fill(new Arc2D.Double((int) (mRect.getX() + mRect.getWidth()) + 5,
                    (int) mRect.getY() + 20,
                    10, 10, 0, 360, 0));
        }

    }

    public void paintComponent(Graphics g) {
        populate();
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        drawBG(g2);
    }

    public void fillElement(Element el) {
        Iterator it = colors.keySet().iterator();
        while (it.hasNext()) {
            String k = (String) it.next();
            if (colors.get(k) instanceof Color) {
                Color col = (Color) colors.get(k);
                el.addAttr("color_" + k, "#" + Integer.toHexString(0xffffff & col.getRGB()));
            } else {
            }
        }
    }

    public void fillSettingsElement(Element el) {
        Iterator it = colors.keySet().iterator();
        while (it.hasNext()) {
            String k = (String) it.next();
            if (colors.get(k) instanceof Color) {
                Color col = (Color) colors.get(k);
                el.addAttr("color_" + k, "#" + Integer.toHexString(0xffffff & col.getRGB()));
            } else {
            }
        }
    }

    public void setSettingsFromElement(Element el) {
        try {
            if (el != null) {
                initColors();

                HashMap<String,Object> newColors = new HashMap<>();

                for(Object o : colors.keySet()) {
                    //OmegaContext.serr_log.getLogger().info("o is " + o);
                    String k = (String)o;
/*
                    if (!(colors.get(k) instanceof Color))
                        continue;
*/

                    try {
                        if ( ! (colors.get(k) instanceof Color) )
                            continue;
                        
                        Color col = (Color) colors.get(k);
                        String c = el.findAttr("color_" + k);

                        if (c != null) {
//			OmegaContext.sout_log.getLogger().info(":--: " + "col " + k + ' ' + col + ' ' + c);
                            if (c.charAt(0) == '#') {
                                int rgb;
                                if (c.length() == 9)
                                    rgb = Integer.parseInt(c.substring(3), 16);
                                else
                                    rgb = Integer.parseInt(c.substring(1), 16);
                                newColors.put(k, new Color(rgb));
                            }
                        }

                        c = el.findAttr("colorTid_" + k);
                        // s#123456,v#123456,o#123456
                        if (c != null) {
//			OmegaContext.sout_log.getLogger().info(":--: " + "col " + k + ' ' + col + ' ' + c);
                            HashMap<String, Color> cols = new HashMap<>();
                            String[] ca = c.split(",");
                            for (String c1 : ca) {
                                String[] ca2 = c1.split("#");
                                if (ca2.length == 2) {
                                    int rgb;
                                    if (ca2[1].length() == 9)
                                        rgb = Integer.parseInt(ca2[1].substring(3), 16);
                                    else
                                        rgb = Integer.parseInt(ca2[1].substring(0), 16);
                                    cols.put(ca2[0], new Color(rgb));
                                }
                            }
                            newColors.put(":" + k, cols);
                        }
                    } catch (ClassCastException ex) {
                        ex.printStackTrace();
                    }
                }
                colors.putAll(newColors);
                repaint();
            } else {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Lesson.global_skipF(true);
            JOptionPane.showMessageDialog(ApplContext.top_frame,
                    "Can't create from file\n" + ex);
            Lesson.global_skipF(false);
            ex.printStackTrace();
        }
    }

    void setPrevRed() {
        if (!buttons_enabled)
            return;

        MyButton ob = focus_list.get();
        focus_list.prev();
        MyButton nb = focus_list.get();

        setCurrentRed();
        return;
    }

    void setNextRed() {
        if (!buttons_enabled)
            return;

        MyButton ob = focus_list.get();
        focus_list.next();
        MyButton nb = focus_list.get();

        setCurrentRed();
        return;
    }

    void setCurrentRed() {
        MyButton mbcur = focus_list.get();
        Iterator it = focus_list.li.iterator();
        while (it.hasNext()) {
            MyButton mb = (MyButton) it.next();
            if (mb != mbcur)
                mb.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            else
                mb.setBorder(BorderFactory.createLineBorder(new Color(242, 80, 80), 5));
        }
    }

    void setRed(int ix) {
        setRedBut(ix);
    }

    void setNoRed(int ix) {
        setNoRedBut(ix);
    }

    void setAllNoRed() {
        for (int i = 0; i < 5; i++)
            setNoRedBut(i);
    }

    void setRedBut(int ix) {
        if (!buttons_enabled)
            return;

        try {
            MyButton ob = focus_list.get();
            focus_list.set(ix);
            MyButton nb = focus_list.get();

            if (ob != null) {
                ob.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                ob.repaint();
            }
            if (nb != null) {
                nb.setBorder(BorderFactory.createLineBorder(new Color(242, 80, 80), 5));
                nb.repaint();
            }
        } catch (Exception ex) {
        }
    }

    void setNoRedBut(int ix) {
        MyButton ob = focus_list.get();
        if (ob != null) {
            ob.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            ob.repaint();
        }
        focus_list.set(ix);
    }

    public void ignorePress(boolean b) {
        ignore_press = b;
    }

    public void updateDisp() {
    }
}
