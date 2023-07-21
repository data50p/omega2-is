package com.femtioprocent.omega.lesson.canvas;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.swing.ScaledImageIcon;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

public class SentenceCanvas extends BaseCanvas {
    private Font item_fo = null;
    private Font item2_fo = null;

    boolean read_mode = false;

    JScrollPane story_list_sp;
    public JList story_list;
    int story_list_x = gX(0.5 - 0.25);
    int story_list_y = gY(0.07);
    int story_list_w = gX(0.5);
    int story_list_h = gY(0.7);

    ComponentAdapter cmp_li = new ComponentAdapter() {
        public void componentResized(ComponentEvent ev) {
            item_fo = null;
            item2_fo = null;
            populate();
        }
    };

    void actionQuit() {
        hideMsg();
        om_msg_mgr.fire("exit create");
    }

    String getPanelName() {
        return "sent";
    }

    public SentenceCanvas(LessonContext l_ctxt) {
        super(l_ctxt);
        focus_list = new CycleList(6);
        requestFocus();
        setLayout(null);
        addComponentListener(cmp_li);
    }

    public boolean ownKeyCode(int kc, boolean is_shift) {
        if (Lesson.skip_F)
            return true;
        if (ignore_press) {
            if (OmegaConfig.isKeySelect(kc)) {
            }
            return true;
        }
        OmegaContext.sout_log.getLogger().info(":--: " + "pupil own " + kc);
        if (OmegaConfig.isKeyNext(kc))
            if (is_shift)
                setMyPrevRed();
            else
                setMyNextRed();
        if (OmegaConfig.isKeySelect(kc))
            if (story_list_sp.isVisible()) {
                enableStoryList(false);
                focus_list.set(1);
            } else {
                MyButton mb = focus_list.get();
                if (mb != null)
                    mb.doClick();
            }
        if (OmegaConfig.isKeyESC(kc)) {
            hideMsg();
            om_msg_mgr.fire("exit create");
            return false;
        }

        return true;
    }

    public String waitDone() {
        while (story_list_sp.isVisible())
            SundryUtils.m_sleep(100);
        setRed(1);
        String filename = (String) story_list.getSelectedValue();
        return filename;
    }

    Font getFont1() {
        if (item2_fo == null)
            item2_fo = new Font("Arial", Font.PLAIN, (int) (getCaH() / 48));
        return item2_fo;
    }

    class Mouse extends MouseInputAdapter {
        SentenceCanvas l_canvas;
        Point2D mpress_p;

        Mouse(SentenceCanvas l_canvas) {
            this.l_canvas = l_canvas;
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void mousePressed(MouseEvent e) {
            mpress_p = new Point2D.Double(e.getX(), e.getY());
            Lesson.cnt_hit_keyOrButton++;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
            mpress_p = new Point2D.Double(e.getX(), e.getY());

// 	    if ( hitQuitButton((int)e.getX(), (int)e.getY()) ) {
// 		om_msg_mgr.fire("exit create");
// 	    }
        }
    }

    Mouse m = new Mouse(this);

    public void init() {
    }

    String[] list_data = new String[0];

    void setMyNextRed() {
        if (story_list_sp.isVisible()) {
            int ix = story_list.getSelectedIndex();
// 	    ListSelectionModel lsm = story_list.getSelectionModel();
            int lix = list_data.length;
            ix++;
            if (ix >= lix)
                ix = 0;
            story_list.setSelectedIndex(ix);
            setAllNoRed();
            story_list.ensureIndexIsVisible(story_list.getSelectedIndex());
        } else
            setNextRed();
    }

    void setMyPrevRed() {
        if (story_list_sp.isVisible()) {
            int ix = story_list.getSelectedIndex();
// 	    ListSelectionModel lsm = story_list.getSelectionModel();
            int lix = list_data.length;
            ix--;
            if (ix < 0)
                ix = lix - 1;
            story_list.setSelectedIndex(ix);
            setAllNoRed();
            story_list.ensureIndexIsVisible(story_list.getSelectedIndex());
        } else
            setPrevRed();
    }

    public void setListData(String[] files) {
        list_data = files;
        int ix = story_list.getSelectedIndex();
        story_list.setListData(files);
        if (ix == -1)
            story_list.setSelectedIndex(0);
        setAllNoRed();
    }

    int selected_index = -1;

    ListSelectionListener lsl = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            JList l = (JList) e.getSource();
            if (l == story_list) {
                String filename = (String) l.getSelectedValue();
                if (e.getValueIsAdjusting())
                    selected_index = story_list.getSelectedIndex();
                OmegaContext.sout_log.getLogger().info(":--: " + "Selected " + filename + ' ' + e);
            }
        }
    };

    void populate() {
        if (story_list == null) {
            story_list = new JList();
            story_list_sp = new JScrollPane(story_list);
            add(story_list_sp);
            if (cell_renderer == null)
                cell_renderer = new CellRenderer();
//	    story_list.setVisibleRowCount(7);
            story_list.setCellRenderer(cell_renderer);
            story_list_sp.setSize(story_list_w, story_list_h);
            story_list_sp.setLocation(story_list_x, story_list_y);
            story_list.setBackground(getColor("bg_frbg"));
            story_list.addListSelectionListener(lsl);
            story_list.setFocusTraversalKeysEnabled(false);
            story_list.setRequestFocusEnabled(false);
            story_list.setFocusable(false);
            story_list_sp.setVisible(false);

            MouseListener mouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int index = story_list.locationToIndex(e.getPoint());
                    int ix = story_list.getSelectedIndex();
                    if (0 == selected_index)
                        story_list_sp.setVisible(false);
                    selected_index = 0;
                    OmegaContext.sout_log.getLogger().info(":--: " + "Clicked on Item " + index + ' ' + ix);
                }
            };
            story_list.addMouseListener(mouseListener);
        }
        mkButtons();
    }

    void mkButtons() {
        if (read_mode)
            populateButtons(new String[]{"Quit", "Read and Listen", "Replay", "Printer...§Print§Select Printer", "Select"},
                    new String[]{"quit", "read", "replay", "print§print_print§print_select", "select"});
        else
            populateButtons(new String[]{"Quit", "Read and Listen", "Replay", "Printer...§Print§Select Printer", "Save"},
                    new String[]{"quit", "read", "replay", "print§print_print§print_select", "save"});
    }

    public void enableStoryList(boolean on) {
        story_list_x = gX(0.5 - 0.25);
        story_list_y = gY(0.07);
        story_list_w = gX(0.5);
        story_list_h = gY(0.7);

        story_list_sp.setSize(story_list_w, story_list_h);
        story_list_sp.setLocation(story_list_x, story_list_y);
        story_list.setBackground(getColor("bg_frbg"));
        story_list_sp.setVisible(on);

        repaint();
    }

    Font getItemFont() {
        if (item_fo == null)
            item_fo = new Font("Arial", Font.PLAIN, (int) gY(0.05));
        return item_fo;
    }

    void setItemFont(Font fo) {
        item_fo = fo;
    }

    int getSize(double asp, int h) {
        if (asp == 0)
            return (int) (h * 0.65);
        int hh = (int) (h * 0.55);

        if (asp < 1.0)
            hh *= asp;

        return hh;
    }

    // --

    Font title_fo;

    void setTitleFont() {
        int h = (int) (gX(0.024));
        title_fo = new Font("Arial", Font.PLAIN, h);
    }

    Font getTitleFont() {
        if (title_fo == null)
            setTitleFont();
        return title_fo;
    }
    // -- //

    public void populateGUI() {
    }

    boolean show_msg;

    class MsgDialog {
        ArrayList<String> li;
        int cnt_show = 0;
        ImageIcon imic, imic_done;

        MsgDialog() {
        }

        void set(ArrayList<String> li) {
            this.li = li;
            cnt_show = 0;
            repaint();
        }

        int[] getBounding() {
            int WW = 0;
            int HH = 0;
            if (li == null)
                return new int[]{500, 350};

            Iterator<String> it = li.iterator();
            while (it.hasNext()) {
                String sent = (String) it.next();
                int sh = getStringHeight(getItemFont(), sent);
                int sw = getStringWidth(getItemFont(), sent);
                HH += sh + 5;
                WW = sw > WW ? sw : WW;
            }
            return new int[]{WW, HH};
        }

        void draw(Graphics2D g2, int[] bounding) {
            if (li == null || li.size() == 0)
                return;

            if (gX(0.02) > 0 && gY(0.02) > 0) {
                imic = ScaledImageIcon.createImageIcon(SentenceCanvas.this,
                        "media/default/story-listen-continue.png",
                        gX(0.035),
                        gY(0.035),
                        false);
                imic_done = ScaledImageIcon.createImageIcon(SentenceCanvas.this,
                        "media/default/story-listen-done.png",
                        gX(0.035),
                        gY(0.035),
                        false);
            }

            int txw = gX(0.85);
            int txh = gY(0.70);
            int w = gX(0.9);
            int h = gY(0.75);
            int th = gY(0.042);
            int x = gX(0.05);
            int y = gY(0.05);
            int r = gX(0.02);

            while (bounding[0] > txw - 10 || bounding[1] > txh - 15) {
                int size = getItemFont().getSize();
                setItemFont(new Font("Arial", Font.PLAIN, (int) (size * 0.9)));
                bounding = getBounding();
            }

            Color col = getColor("bg_frbg"); // new Color(0xe5, 0xe5, 0xe5);
            RoundRectangle2D fr = new RoundRectangle2D.Double(x, y, w, h, r, r);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.setColor(col);
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
            g2.setFont(getItemFont());
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));


            g2.setColor(getColor("bg_tx"));
            int hh = th;
            int sh = getStringHeight(getItemFont(), "ABC");
            int row = sh + 2;
            Iterator it = li.iterator();
            int cnt = 0;
            int gap = 0;
            while (it.hasNext()) {
                String sent = (String) it.next();
                if (cnt < cnt_show)
                    g2.drawString(sent,
                            x + w / 20,
                            y + hh + row);
//		sh = getStringHeight(getItemFont(), sent);
                row += sh + gap;
                cnt++;
            }
            int cnt_max = li.size() + 1;
            if (imic != null && imic_done != null)
                if (cnt_show >= cnt_max)
                    g2.drawImage(imic_done.getImage(),
                            x + gX(0.015),
                            y + gY(0.013) + (sh + gap) * cnt_max,
                            null);
                else
                    g2.drawImage(imic.getImage(),
                            x + gX(0.015),
                            y + gY(0.013) + (sh + gap) * cnt_show,
                            null);

            g2.setColor(col);
            g2.setFont(getTitleFont());
            g2.drawString("", x + 1 * w / 10, (int) (y + gY(0.03)));

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

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    MsgDialog msg_dlg = new MsgDialog();

    class CellRenderer extends JLabel implements ListCellRenderer {
        public CellRenderer() {
            setOpaque(true);
        }

        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            String val = (String) value;

            setVerticalTextPosition(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(getFont1());
            if (value != null)
                setText(val);
            setBackground(isSelected ? getColor("bg_tx") : getColor("bg_frbg"));
            setForeground(isSelected ? getColor("bg_frbg") : getColor("bg_tx"));

            return this;
        }
    }

    ;

    CellRenderer cell_renderer;

    void drawList(Graphics2D g2) {
        if (story_list_sp.isVisible()) {
            OmegaContext.sout_log.getLogger().info(":--: " + "story is visible");
            story_list_x = gX(0.5 - 0.25);
            story_list_y = gY(0.07);
            story_list_w = gX(0.5);
            story_list_h = gY(0.7);

            int th = gY(0.042);
            int x = story_list_x - 10;
            int y = story_list_y - 10 - th;
            int w = story_list_w + 20;
            int h = story_list_h + 20 + th;
            int r = 15;

            Color col = getColor("bg_frbg"); // new Color(0xe5, 0xe5, 0xe5);
            RoundRectangle2D fr = new RoundRectangle2D.Double(x, y, w, h, r, r);
            Rectangle story_r = story_list_sp.getBounds();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.setColor(col);

            Area a = new Area();
            a.add(new Area(new Rectangle2D.Double(0, 0, 10000, 10000)));
            a.subtract(new Area(story_r));
            g2.setClip(a);
            g2.fill(fr);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // titlebar
// 	    g2.setColor(new Color(88, 88, 88));
// //	    g2.setClip(fr);
// 	    g2.fill(new Rectangle2D.Double(x, y, w, th));

            BasicStroke stroke = new BasicStroke(getCaH() / 200f);
            g2.setStroke(stroke);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(getColor("bg_fr"));
//	    g2.setClip(0, 0, 10000, 10000);
            g2.draw(fr);

            a = new Area();
            a.add(new Area(new Rectangle2D.Double(0, 0, 10000, 10000)));
            a.subtract(new Area(fr));
            g2.setClip(a);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            g2.setColor(new Color(15, 15, 15));

            for (int i = 0; i < 7; i++) {
                RoundRectangle2D frs = new RoundRectangle2D.Double(x + 10 - i, y + 10 - i, w, h, r, r);
                g2.fill(frs);
            }

            g2.setClip(0, 0, 10000, 10000);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    public void showMsg(ArrayList<String> sentences) {
        item_fo = null;
        msg_dlg.set(sentences);
    }

    public void showMsgMore() {
        msg_dlg.cnt_show++;
        repaint();
    }

    public void hideMsg() {
        msg_dlg.set(null);
    }

    public void paintComponent(Graphics g) {
        if (buttons[0] == null)
            populate();

        long ct0 = SundryUtils.ct();
        Graphics2D g2 = (Graphics2D) g;

        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        drawBG(g2);

        g.setColor(Color.black);
        Font fo = g.getFont();

        int bounding[] = msg_dlg.getBounding();
        msg_dlg.draw(g2, bounding);

        drawList(g2);

        long ct1 = SundryUtils.ct();
    }

    public void enter() {
        super.enter();
        setRed(6);
    }

    public void leave() {
        super.leave();
    }

    public void setStoryData(Lesson.PlayDataList playDataList) {
        ArrayList<Lesson.PlayData> story_list = playDataList.arr;
        Lesson.PlayData pd = null;
    }

    ListenListener getListenListener() {
        return null;
    }

    public void setRead(boolean b) {
        if (read_mode != b) {
            read_mode = b;
            if (buttons[0] != null) { // else swing crash
                mkButtons();
            }
        }
    }
}
