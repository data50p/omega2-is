package com.femtioprocent.omega.lesson.canvas;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.lesson.repository.LessonItem;
import com.femtioprocent.omega.lesson.repository.Locator;
import com.femtioprocent.omega.swing.ScaledImageIcon;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Stack;

/*
--gapE---|=====|--gapB--|=========|---gapE---




 */

public class LessonMainCanvas extends BaseCanvas {
    static final float gapB = 5;
    static final float gapE = 5;
    static final float tgH = 8;
    private Font item_fo = new Font("Arial", Font.PLAIN, (int) (/*h*/20 * 0.8));
    private Font trgt_fo = new Font("Arial", Font.PLAIN, (int) (/*h*/20 * 0.8));
    private Font trgtA_fo = new Font("Arial", Font.PLAIN, (int) (/*h*/20 * 0.5));

    JLabel title;
    JLabel parent;
    public final ImageAreaJB lesson[] = new ImageAreaJB[10];

    Locator locator = new Locator();

    public boolean modeIsTest = false;

    public void setModeIsTest(boolean b) {
        modeIsTest = b;
        updLessons();
    }

    void updLessons() {
        for (int i = 0; i < lesson.length; i++) {
            if (lesson[i] != null) {
                LessonItem litm = (LessonItem) lesson[i].o;
                lesson[i].setEnabled(!(modeIsTest && litm.isStory()));
            }
        }
    }

    public LessonMainCanvas(LessonContext l_ctxt) {
        super(l_ctxt);
        focus_list = new CycleList(-1);
    }

    public String getPanelName() {
        return "main";
    }

    protected void resized() {
        for (int i = 0; i < lesson.length; i++) {
            if (lesson[i] != null) {
                remove(lesson[i]);
                lesson[i] = null;
            }
        }
        populate(false);
    }

    protected void resized2() {
        populate(true);
    }

    public void reload() {
        if (buttons[0] != null)
            populate(true);
    }

    String bs = null;
    static HashMap<String,Integer> bs_hm = new HashMap<>();

    public int setLessonBase(String bs) {
        this.bs = bs;
        resized2();
        Integer I = (Integer) bs_hm.get("" + bs);
        if (I == null) {
            return 0;
        }
        return 0;
    }

    public void addLessonBase(String bs, int ord) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "add " + this.bs + ' ' + bs);
        bs_hm.put("" + this.bs, ord);
//log	OmegaContext.sout_log.getLogger().info(":--: " + "<!add> LBhm: " + bs_hm);
        if (this.bs != null)
            this.bs = this.bs + '/' + bs;
        else
            this.bs = bs;
        resized2();
        requestFocus();
        mkButtons();
    }

    public void tellLessonBase(String bs, int ord) {
        bs_hm.put("" + this.bs, ord);
//log	OmegaContext.sout_log.getLogger().info(":--: " + "<!tell> LBhm: " + bs_hm);
    }

    public String getLessonBase() {
        return bs;
    }

    private boolean hasFile() {
        String lb = getLessonBase();
        File file;
        if (lb == null)
            file = new File(/*Locator.fbase + */ OmegaContext.omegaAssets("lesson-" + OmegaContext.getLessonLang() + "/active/story"));      // LESSON-DIR-A
        else
            file = new File(/*Locator.fbase + */ OmegaContext.omegaAssets("lesson-" + OmegaContext.getLessonLang() + "/active/" + lb + "/story")); // LESSON-DIR-A
        return file.exists();
    }

    boolean isStory() {
        return !modeIsTest && hasFile();
    }

    void mkButtons() {
        if (isStory())
            populateButtons(new String[]{"Finish", "", "", "", "Load Story"},
                    new String[]{"quit", "", "", "", "read_story"});
        else
            populateButtons(new String[]{"Finish", "", "", "", ""},
                    new String[]{"quit", "", "", "", ""});
        updLeftButton();
    }

    void populate(boolean requestF) {
        double h1 = 0.25;
        double h2 = 0.02;
        double h3 = 0.19;
        double hh = (1.0 - h1 - 2 * h2 - h3) / 2;

        int v0 = gY(h2);
        int v1 = gY(h1);
        int v2 = gY(h1 + hh + h2);
        int v3 = gY(h1 + hh + h2 + hh + h2);

        double l1 = 0.1;
        double l2 = 0.2;
        double l25 = 0.30;
        double l3 = 0.4;
        double l4 = 0.6;
        double l5 = 0.8;

        double yf1 = 0.2;
        double yf2 = 0.2 + 0.3;
        double yf3 = 0.2 + 0.6;

        int bw = gX(hh * 0.75);
        int bh = gY(hh * 0.9);
        int bh2 = gY(hh * 0.55);

        int fs = getCaH() / 48;
        fo = new Font("arial", Font.PLAIN, fs);

        if (title == null) {
            title = new JLabel(T.t(""));
            add(title);
        }
        if (parent == null) {
            parent = new JLabel(T.t(""));
            add(parent);
        }

        mkButtons();

//log	OmegaContext.sout_log.getLogger().info(":--: " + "base is " + bs);
        String[] lessons_name;
        if (bs == null)
            lessons_name = locator.getAllLessonsInDir();
        else
            lessons_name = locator.getAllLessonsInDir(bs);

        int w = getCaW() / 2;
        int h = getCaH() / 5;
        ImageIcon imic = ScaledImageIcon.createImageIcon(this,
                "toolbarButtonGraphics/omega/omega_title.png",
                w,
                h);
        if (imic == null)
            return;
        if (imic != null) {
            title.setSize(imic.getImage().getWidth(null), imic.getImage().getHeight(null));
            title.setLocation(gX(l25) - imic.getImage().getHeight(null), v0);
            title.setIcon(imic);
        }
        set(title);

        if (lesson != null && lessons_name != null) {

// 	    buttons[0].requestFocus();  // focus are lost when remove buttons

            for (int i = 0; i < lesson.length; i++) {
                if (lesson[i] != null) {
                    lesson[i].setVisible(false);
//---		    remove(lesson[i]);
//		    lesson[i] = null;
                }
                repaint();
            }
            int to = lessons_name.length;
            if (to >= 10)
                to = 10;

            for (int i = 0; i < to; i++) {
                LessonItem litm = new LessonItem(lessons_name[i]);
                if (i == 0) {
                    String imn = litm.getLessonParentImage("_parent");
                    ImageIcon imicp = ScaledImageIcon.createImageIcon(this,
                            imn,
                            w / 5,
                            h);
                    if (imicp == null) {
                        imn = litm.getLessonParentImage("_enter");
                        imicp = ScaledImageIcon.createImageIcon(this,
                                imn,
                                w / 5,
                                h);
                    }
                    if (imicp == null) {
                        imn = litm.getLessonParentImage("");
                        imicp = ScaledImageIcon.createImageIcon(this,
                                imn,
                                w / 5,
                                h);
                    }
                    OmegaContext.sout_log.getLogger().info(":--: " + "PARENT " + i + ' ' + imn + ' ' + imicp);
                    if (imicp != null) {
                        parent.setSize(imicp.getImage().getWidth(null), imicp.getImage().getHeight(null));
                        parent.setLocation(gX(l25) + w + 10, v0);
                        parent.setIcon(imicp);
                        set(parent);
                    } else {
                        parent.setIcon(null);
                    }

                }
                String dispName = litm.getLessonDisplayName(OmegaContext.getLessonLang());
//log		OmegaContext.sout_log.getLogger().info(":--: " + "recreate le " + i + ' ' + litm);
                ImageAreaJB l;
                if (lesson[i] == null) {
                    lesson[i] = new ImageAreaJB("",
                            i,
                            bw,
                            bh - 20,
                            dispName);
                    add(lesson[i]);
                }
                if (litm.isDir()) {
                    lesson[i].setNew(/*T.t("Lesson") + ' ' + */litm.getLessonShortName(),
                            litm.getLessonImageFileName(),
                            litm.getLessonImageFileName("_enter"),
                            litm,
                            dispName);
                } else {
                    lesson[i].setNew(/*T.t("Lesson") + ' ' + */litm.getLessonShortName(),
                            litm.getLessonImageFileName(),
                            litm.getLessonImageFileName("_enter"),
                            litm,
                            dispName);
                }
                lesson[i].setEnabled(!(modeIsTest && litm.isStory()));
            }
        }

        for (int i = 0; i < lesson.length; i++) {
            if (lesson[i] != null) {
                ImageAreaJB l = lesson[i];
                l.setSize(bw - 15, bh);
                l.setLocation(10 + (i % 5) * (bw + 3), i < 5 ? v1 : v2);
                setAlt(l);
            }
        }

        updLeftButton();

        requestFocusOrd(0);
    }

    void updLeftButton() {
        JButton b;
        b = buttons[0];
        if (bs == null) {
            b.setText(T.t("Logout"));
            setJBIcon(b, "toolbarButtonGraphics/omega/main_logout.png");
        } else {
            b.setText(T.t("Up level"));
            setJBIcon(b, "toolbarButtonGraphics/omega/main_uplevel.png");
        }
    }

    int current_ix = 0;
    int current_iy = 0;

    void gotoBox(int ix, int iy) {
    }

    public void gotoBoxRel(int dx, int dy) {
    }

    public void selectBox() {
    }

    public void populateGUI() {
    }

    public boolean ownKeyCode(int kc, boolean is_shift) {
        if (Lesson.skip_F)
            return true;
        if (OmegaConfig.isKeyESC(kc)) {
            om_msg_mgr.fire("button main:quit");
            return true;
        }

        if (OmegaConfig.isKeyNext(kc)) {
            if (is_shift)
                setPrevRed();
            else
                setNextRed();
            return true;
        }
        if (OmegaConfig.isKeySelect(kc)) {
            if (last_focus_ord >= 0 && last_focus_ord <= 9)
                lesson[last_focus_ord].doClick();
            else
                super.ownKeyCode(kc, is_shift);
            return true;
        }
        return super.ownKeyCode(kc, is_shift);
    }

    public void paintComponent(Graphics g) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "++++++++++ repaint LMC ");
        //SundryUtils.m_sleep(100);
        if (title == null)
            populate(false);

        Graphics2D g2 = (Graphics2D) g;

        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        drawBG(g2);

        g.setColor(Color.black);
        Font fo = g.getFont();
    }

    public void enter() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "LeMa-Enter");
        super.enter();

        Integer I = bs_hm.get("" + bs);

        setNoRed(0);
    }

    public void leave() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "LeMa-Leave");
        super.leave();
    }

    Stack red_stack = new Stack();

    public void setRedClear() {
        red_stack = new Stack();
        requestFocus();
        requestFocusOrd(0);
    }

    public int setRedPop() {
        if (red_stack.size() == 0) {
            requestFocus();
            return 0;
        }
        int ord = ((Integer) red_stack.pop()).intValue();
        requestFocus();
        requestFocusOrd(ord);
        return ord;
    }

    public void setRedPush(int ord) {
        red_stack.push(ord);
    }

    int last_focus_ord = 0;

    // 0 1 2 3 4
// 5 6 7 8 9
// 10     11
    public void requestFocusOrd(int ord) {
        for (int i = 0; i < 10; i++)
            if (lesson[i] != null)
                if (ord == i) {
                    lesson[ord].setBorder(BorderFactory.createLineBorder(new Color(242, 80, 80), 5));
                    lesson[ord].showEnter();
                } else if (i == last_focus_ord) {
                    lesson[i].setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 5));
                    lesson[i].showNormal();
                } else
                    ;
        last_focus_ord = ord;
    }

    public void setNextRed() {
        int how_many = howManyLessonButtons();
        int ord = last_focus_ord + 1;
        if (ord == how_many) {
            super.setRed(0); // BAD
            requestFocusOrd(10);
            return;
        }
        if (isStory() && ord == 11) {
            super.setRed(4); // BAD
            requestFocusOrd(11);
            return;
        }
        if (ord > 9 || lesson[ord] == null || !lesson[ord].isVisible()) {
            ord = 0;
        }
        setAllNoRed();
        requestFocusOrd(ord);
    }

    public void setRed(int ix) {
        int ord = ix;
        if (ord > 9 || lesson[ord] == null || !lesson[ord].isVisible()) {
            return;
        }
        setNoRed(0);
        requestFocusOrd(ord);
    }

    public void setPrevRed() {
        int ord = last_focus_ord - 1;
        if (ord < 0)
            for (int i = 9; i > 0; i--) {
                if (lesson[i] != null || lesson[i].isVisible()) {
                    ord = i;
                    break;
                }
            }
        setNoRed(0);
        requestFocusOrd(ord);
    }

    int howManyLessonButtons() {
        for (int i = 0; i < lesson.length; i++) {
            if (lesson[i] == null || !lesson[i].isVisible())
                return i;
        }
        return lesson.length;
    }
}
