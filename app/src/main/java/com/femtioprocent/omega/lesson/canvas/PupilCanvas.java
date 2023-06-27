package com.femtioprocent.omega.lesson.canvas;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.register.data.RegLocator;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.lesson.pupil.Pupil;
import com.femtioprocent.omega.lesson.settings.PupilSettingsDialog;
import com.femtioprocent.omega.swing.ScaledImageIcon;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/*
--gapE---|=====|--gapB--|=========|---gapE---




 */

public class PupilCanvas extends BaseCanvas implements ListSelectionListener {
    public static final int BH_PUPIL = 1;
    public static final int BH_ADMINISTRATOR = 2;
    public static final int BH_DEFAULT = BH_ADMINISTRATOR;
    public static final int BH_OTHER = BH_PUPIL;

    static final float gapB = 5;
    static final float gapE = 5;
    static final float tgH = 8;
    private Font item_fo = null;

    JLabel ppic;
    JLabel welcome;

    JScrollPane names_sp;
    JList names;

    PupilCellRenderer pcr;

    ButtonGroup mode_gr = new ButtonGroup();

    String pname = "Guest";

    ImageIcon greeting_ic;

    public int behaviour = BH_PUPIL;

    class PupilCellRenderer extends JLabel implements ListCellRenderer {
        public PupilCellRenderer() {
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
            PupilItem pi = (PupilItem) value;
//		setIcon(pi.im_ic);

            setVerticalTextPosition(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(getFont1());
            if (value != null)
                setText(T.t(value.toString()));
            setBackground(isSelected ? getColor("bg_tx") : getColor("bg_frbg"));
            setForeground(isSelected ? getColor("bg_frbg") : getColor("bg_tx"));

            return this;
        }
    }

    ;

    class PupilItem {
        String name;
        String im_name;
        int w, h;
        ImageIcon im_ic;

        PupilItem(Component component, String name) {
            this.name = name;
            this.im_name = "register/" + name + ".p/" + "id.jpg";
            int w = 52;
            w = getCaH() / 7;
            int h = (int) (w * 1.3);

            im_ic = ScaledImageIcon.createImageIcon(component,
                    im_name,
                    w,
                    h);
            if (im_ic == null) {
                this.im_name = "register/" + "Guest" + ".p/" + "id.jpg";
                im_ic = ScaledImageIcon.createImageIcon(component,
                        im_name,
                        w,
                        h);
            }
        }

        public String toString() {
            return name;
        }
    }

    public boolean ownKeyCode(int kc, boolean is_shift) {
        if (Lesson.skip_F)
            return true;
        OmegaContext.def_log.getLogger().info("pupil own " + kc);
        if (OmegaConfig.isKeyNext(kc))
            if (is_shift)
                setPrevRed();
            else
                setNextRed();
        if (OmegaConfig.isKeySelect(kc)) {
            MyButton mb = focus_list.get();
            if (mb != null)
                mb.doClick();
        }
        if (kc == 40) {
            int ix = names.getSelectedIndex();
            ix++;
            int lix = names.getModel().getSize();
            OmegaContext.def_log.getLogger().info("++ " + lix + ' ' + ix);
            if (ix >= lix)
                ix = 0;
            names.setSelectedIndex(ix);
        }
        if (kc == 38) {
            int ix = names.getSelectedIndex();
            ix--;
            int lix = names.getModel().getSize();
            OmegaContext.def_log.getLogger().info("-- " + lix + ' ' + ix);
            if (ix < 0)
                ix = lix - 1;
            names.setSelectedIndex(ix);
        }

        return true;
    }

    Font getFont1() {
        if (item_fo == null)
            item_fo = new Font("Arial", Font.PLAIN, (int) (getCaH() / 24));
        return item_fo;
    }

    public PupilCanvas(LessonContext l_ctxt, String pname) {
        super(l_ctxt);
        focus_list = new CycleList(2);
        this.pname = pname;
        focus_list.set(1);
    }

    String getPanelName() {
        return "pupil";
    }

    public void setBehaviour(int b) {
        behaviour = b;
    }

    public void enter() {
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "ENTER pupil");
        super.enter();
        if (buttons[0] == null) {
            return;
        } else {
//	    login.setText("Login");
        }
    }

    public void leave() {
        super.leave();
    }

    public String getPupilName() {
        return pname;
    }

    protected void resized() {
        greeting_ic = null;
        item_fo = null;
        populate();
    }

    public void valueChanged(ListSelectionEvent e) {
        JList l = (JList) e.getSource();
        if (l == names) {
            PupilItem pi = (PupilItem) l.getSelectedValue();
            if (pi != null) {
                if (pi.name.equals("<New Pupil>") || pi.name.equals(T.t("<New Pupil>"))) {
                    String pn = null;
                    pn = JOptionPane.showInputDialog(ApplContext.top_frame,
                            T.t("What is the new pupils name?"));

                    if (pn != null && pn.length() > 0) {
                        RegLocator loc = new RegLocator();
                        loc.createPupilName(pn);
                        PupilSettingsDialog pupil_settings_dialog = new PupilSettingsDialog(Lesson.static_lesson);
                        pupil_settings_dialog.setPupil(new Pupil(pn));
                        pupil_settings_dialog.setVisible(true);
                        OmegaContext.def_log.getLogger().info("hidden +++++++++++++?");
                        pupil_settings_dialog = null;
                        mkList(pn);
                    }
                    return;
                }
                pname = pi.name;
                setPupil(pname);
                final String fpname = pname;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        l_ctxt.getLesson().setPupil(fpname);
                    }
                });
//		mkButtons();
            }
        }
    }

    public void changeBehaviour() {
        switch (behaviour) {
            case BH_PUPIL:
                behaviour = BH_ADMINISTRATOR;
                break;
            case BH_ADMINISTRATOR:
                behaviour = BH_PUPIL;
                break;
        }
        mkButtons();
        mkList();

        SundryUtils.m_sleep(200);
        repaint();
    }

    void mkButtons() {
        if (behaviour == BH_ADMINISTRATOR) {
            populateButtons(new String[]{"Quit", "Pupil", "", "Test", "Result"},
                    new String[]{"quit", "pupil", "", "test_t", "result"});
        } else if (behaviour == BH_PUPIL) {
            populateButtons(new String[]{"Quit", "", "Create sentence", "Test", ""},
                    new String[]{"quit", "", "create_p", "test_p", ""});
        }
    }

    public void mkList() {
        mkList(pname);
    }

    public void mkList(String selected_name) {
        OmegaContext.def_log.getLogger().info("________-" + selected_name);
        boolean new_pupil = behaviour == BH_ADMINISTRATOR;
        RegLocator loc = new RegLocator();
        String[] sa = loc.getAllPupilsName();
        PupilItem[] pA = new PupilItem[sa.length + (new_pupil ? 1 : 0)];
        PupilItem selected_pupil = null;
        for (int i = 0; i < pA.length - (new_pupil ? 1 : 0); i++) {
            String pn = sa[i];
            pA[i] = new PupilItem(this, pn);
            if (pn.equals(selected_name))
                selected_pupil = pA[i];
        }
        if (new_pupil)
            pA[pA.length - 1] = new PupilItem(this, T.t("<New Pupil>"));
        names.setListData(pA);
        if (selected_name != null && selected_pupil != null) {
            names.setSelectedValue(selected_pupil, true);
            OmegaContext.def_log.getLogger().info("selected value 1 " + names.getSelectedValue() + ' ' + selected_name);
        } else {
            names.setSelectedValue(T.t("Guest"), true);
        }
    }

    void populate() {
        double xW = 0.27;

        double list_start_sx = 0.1;
        double pupil_im_x = 0.5;
        double Top_H = 0.15;
        double Bot_H = 0.22;
        double Cen_H = 1.0 - Top_H - Bot_H;
        double Bot_Y = 1.0 - Bot_H;
        double list_pupim_top = Top_H + 0.05;

        int fs = getCaH() / 33;
        fo = new Font("arial", Font.PLAIN, fs);

        mkButtons();

        if (ppic == null) {
            ppic = new JLabel(T.t(""));
            add(ppic);
            ppic.setBorder(BorderFactory.createLineBorder(getColor("bg_fr"), 5));
            welcome = new JLabel(T.t("Welcome"));
            welcome.setFont(getFont1());
            Color color = getColor("bg_tx");
            OmegaContext.def_log.getLogger().info("color bg_tx " + color);
            welcome.setForeground(color);
            add(welcome);
        }

        if (pcr == null)
            pcr = new PupilCellRenderer();

        if (names != null) {
            ListSelectionModel lsm = names.getSelectionModel();
            int lix = lsm.getMaxSelectionIndex();
            if (lix != -1)
                lsm.removeIndexInterval(0, lix);
        } else {
            names = new JList();
            names_sp = new JScrollPane(names);
            add(names_sp);
            names.setVisibleRowCount(7);
            names.setCellRenderer(pcr);
            names.addListSelectionListener(this);
            names.setFocusTraversalKeysEnabled(false);
            names.setRequestFocusEnabled(false);
            names.setFocusable(false);
// 	    names.setForeground(getColor("bg_tx"));
            names.setBackground(getColor("bg_frbg"));
//	    names.addFocusListener(background_FA);
        }
        mkList();

        int h = getCaH() / 5;
        setPupil(pname);
        ppic.setLocation(gX(pupil_im_x), gY(list_pupim_top));
        set(ppic);
        welcome.setFont(getFont1());
        welcome.setLocation(gX(pupil_im_x), gY(list_pupim_top + 0.05) + pupil_image_h);
        welcome.setSize(gX(0.5), gY(0.13));

        JScrollPane sp = names_sp;
        JList l = names;
        sp.setSize(gX(0.45 - list_start_sx), gY(Cen_H - 0.15));
        sp.setLocation(gX(list_start_sx), gY(list_pupim_top));
        //l.setFont(fo);
        l.ensureIndexIsVisible(l.getSelectedIndex());
//	sp.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 5));
    }

    int pupil_image_h = 0;

    private void setPupil(String name) {
        int w = (int) (0.35 * getCaW());
        int h = (int) (0.35 * getCaH());

        ImageIcon imic = null;
        try {
            imic = ScaledImageIcon.createImageIcon(this,
                    "register/" + name + ".p/" + "id.jpg",
                    w,
                    h,
                    false);
            if (imic == null)
                imic = ScaledImageIcon.createImageIcon(this,
                        OmegaContext.omegaAssets("media/default/pupil.jpg"),
                        w,
                        h,
                        false);
        } catch (Exception _ex) {
        }

        if (imic != null) {
            ppic.setSize(imic.getImage().getWidth(null), imic.getImage().getHeight(null));
            ppic.setIcon(imic);
            pupil_image_h = imic.getImage().getHeight(null);
        } else {
// 	    if ( name != "Guest")
// 		setPupil("Guest");
        }

        String name2 = name;
        if (name.equals("Guest")) {
            name2 = T.t("Guest");
            welcome.setText(T.t("Welcome"));
        } else {
            welcome.setText(T.t("Welcome") + ' ' + name);
        }
        setSelectedPupil(name2);
        OmegaContext.def_log.getLogger().info("selected value 2 " + names.getSelectedValue() + ' ' + name + ' ' + name2);
    }

    private void setSelectedPupil(String name) {
        for (int i = 0; i < 1000; i++) {
            try {
                PupilItem pi = (PupilItem) names.getModel().getElementAt(i);
                if (pi.name.equals(name))
                    names.setSelectedIndex(i);
            } catch (Exception ex) {
            }
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

    public void paintComponent(Graphics g) {
        if (buttons[0] == null)
            populate();

        if (names != null)
            names.ensureIndexIsVisible(names.getSelectedIndex());

        Graphics2D g2 = (Graphics2D) g;

        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        drawBG(g2);

        if (greeting_ic == null) {
            greeting_ic = ScaledImageIcon.createImageIcon(this,
                    "media/default/pupil_greeting.png",
                    (int) gX(0.8),
                    (int) gY(0.1));
        }
        if (greeting_ic != null) {
            try {
                g2.drawImage(greeting_ic.getImage(), (int) gX(0.05), (int) gY(0.02), null);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }

        g.setColor(Color.black);
        Font fo = g.getFont();
    }

    public void reloadPIM() {
        updateDisp();
        setPupil(getPupilName());
    }

    public void updateDisp() {
        Color color = getColor("bg_tx");
        welcome.setForeground(color);
        names.setBackground(getColor("bg_frbg"));
        ppic.setBorder(BorderFactory.createLineBorder(getColor("bg_fr"), 5));
        names.setBorder(BorderFactory.createLineBorder(getColor("bg_fr"), 5));
    }

    public void setSettingsFromElement(Element el) {
        super.setSettingsFromElement(el);
        if (welcome != null) {
// 	    Color color = getColor("bg_tx");
// 	    welcome.setForeground(color);
            updateDisp();
        }
    }
}
