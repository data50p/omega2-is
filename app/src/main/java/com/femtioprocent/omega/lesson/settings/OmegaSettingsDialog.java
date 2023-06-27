package com.femtioprocent.omega.lesson.settings;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.swing.ColorChooser;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;
import com.femtioprocent.omega.xml.SAX_node;
import com.femtioprocent.omega.xml.XML_PW;
import org.hs.jfc.FormPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

public class OmegaSettingsDialog extends SettingsDialog {

    public ColorButton feedback_movie_background;
    public ColorButton action_movie_background;
    public ColorButton anim_background;
    public ColorButton signWord_background;
    public ColorButton signSentence_background;
    public JComboBox color_theme;
    public JButton color_theme_main;
    public JButton color_theme_pupil;
    public JButton color_theme_words;
    public JButton color_theme_sent;
    public JSlider signWord_alpha;
    public JSlider signSentence_alpha;
    public JSlider signMovieWord_scale;
    public JSlider signMovieSentence_scale;
    private String fname = "omega_settings.xml";
    public Lesson lesson;

    public class ColorButton extends JButton implements ActionListener {

        public Color color;

        ColorButton(Color color) {
            this.color = color;
            upd();
            addActionListener(this);
            setText(T.t("Change Color..."));
        }

        public void actionPerformed(ActionEvent ev) {
            Color nc = ColorChooser.select(color);
            if (nc != null) {
                color = nc;
                upd();
            }
        }

        void upd() {
            setBackground(color);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            Color c = new Color(0x80 ^ r, 0x80 ^ g, 0x80 ^ b);
            setForeground(c);
        }
    }

    boolean save() {
        Element el = new Element("omega-settings");
        Element fel = new Element("feedback_movie_background");
        fel.addAttr("color", "" + feedback_movie_background.color.getRGB());
        Element ael = new Element("action_movie_background");
        ael.addAttr("color", "" + action_movie_background.color.getRGB());
        Element anel = new Element("anim_background");
        anel.addAttr("color", "" + anim_background.color.getRGB());
        el.add(fel);
        el.add(ael);
        el.add(anel);
        if (OmegaConfig.isLIU_Mode()) {
            Element swel = new Element("signWord_background");
            swel.addAttr("color", "" + signWord_background.color.getRGB());
            Element ssel = new Element("signSentence_background");
            ssel.addAttr("color", "" + signSentence_background.color.getRGB());
            Element ssalel = new Element("signSentence_alpha");
            ssalel.addAttr("value", "" + signSentence_alpha.getValue());
            Element swalel = new Element("signWord_alpha");
            swalel.addAttr("value", "" + signWord_alpha.getValue());
            Element smwel = new Element("signMovieWord_scale");
            smwel.addAttr("value", "" + signMovieWord_scale.getValue());
            Element smsel = new Element("signMovieSentence_scale");
            smsel.addAttr("value", "" + signMovieSentence_scale.getValue());
            el.add(swel);
            el.add(swalel);
            el.add(ssel);
            el.add(ssalel);
            el.add(smwel);
            el.add(smsel);
        }
        try (XML_PW xmlpw = new XML_PW(SundryUtils.createPrintWriterUTF8(fname), false)) {
            xmlpw.put(el);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    void load() {
        Element el = loadElement();
        if (el != null) {
            Element fel = el.findElement("feedback_movie_background", 0);
            Element ael = el.findElement("action_movie_background", 0);
            Element anel = el.findElement("anim_background", 0);
            Element swel = el.findElement("signWord_background", 0);
            Element ssel = el.findElement("signSentence_background", 0);
            Element swalel = el.findElement("signWord_alpha", 0);
            Element ssalel = el.findElement("signSentence_alpha", 0);
            Element smwel = el.findElement("signMovieWord_scale", 0);
            Element smsel = el.findElement("signMovieSentence_scale", 0);

            if (fel != null) {
                int coli = 256 * 256 * 40 + 256 * 40 + 100;
                try {
                    String cols = fel.findAttr("color");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                Color nc = new Color(coli);
                feedback_movie_background.color = nc;
                feedback_movie_background.upd();
                ;

                coli = 256 * 256 * 40 + 256 * 40 + 100;
                try {
                    String cols = ael.findAttr("color");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                nc = new Color(coli);
                action_movie_background.color = nc;
                action_movie_background.upd();

                coli = 256 * 256 * 30 + 256 * 30 + 80;
                try {
                    String cols = anel.findAttr("color");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                nc = new Color(coli);
                anim_background.color = nc;
                anim_background.upd();

                coli = 256 * 256 * 30 + 256 * 30 + 80;
                try {
                    String cols = swel.findAttr("color");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                nc = new Color(coli);
                if (signWord_background != null) {
                    signWord_background.color = nc;
                    signWord_background.upd();
                }
                coli = 256 * 256 * 30 + 256 * 30 + 80;
                try {
                    String cols = ssel.findAttr("color");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                nc = new Color(coli);
                if (signSentence_background != null) {
                    signSentence_background.color = nc;
                    signSentence_background.upd();
                }

                coli = 65;
                try {
                    String cols = swalel.findAttr("value");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                if (signWord_alpha != null)
                    signWord_alpha.setValue(coli);

                coli = 92;
                try {
                    String cols = ssalel.findAttr("value");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                if (signWord_alpha != null)
                    signSentence_alpha.setValue(coli);

                coli = 20;
                try {
                    String cols = smwel.findAttr("value");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                if (signMovieWord_scale != null)
                    signMovieWord_scale.setValue(coli);

                coli = 40;
                try {
                    String cols = smsel.findAttr("value");
                    coli = Integer.parseInt(cols);
                } catch (NullPointerException ex) {
                } catch (NumberFormatException ex) {
                }
                if (signMovieSentence_scale != null)
                    signMovieSentence_scale.setValue(coli);
            }
        }
    }

    static FilenameFilter fnf = new FilenameFilter() {

        public boolean accept(File dir, String fname) {
            if (fname.endsWith(".omega_colors")) {
                return true;
            }
            return false;
        }
    };

    Element loadElement() {
        try {
            Element el = SAX_node.parse(OmegaContext.omegaAssets(fname), false);
            return el;
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Exception! Restore.restore(): " + ex);
            return null;
        }
    }

    class MyActionListener implements ActionListener {

        public void actionPerformed(ActionEvent ev) {
            JButton jb = (JButton) ev.getSource();
            if (jb == color_theme_main) {
                if (lesson != null) {
                    lesson.displayColor("main");
                }
            }
            if (jb == color_theme_pupil) {
                if (lesson != null) {
                    lesson.displayColor("pupil");
                }
            }
            if (jb == color_theme_words) {
                if (lesson != null) {
                    lesson.displayColor("words");
                }
            }
            if (jb == color_theme_sent) {
                if (lesson != null) {
                    lesson.displayColor("sent");
                }
            }
        }
    }

    MyActionListener my_al = new MyActionListener();

    public OmegaSettingsDialog() {
        super("Omega - Settings");

        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        FormPanel top = new FormPanel();
        int X = 0;
        int Y = 0;
        top.add(new JLabel(T.t("Action Movie Background")),
                action_movie_background = new ColorButton(new Color(40, 40, 100)), Y, ++X);
        X = 0;
        Y++;
        top.add(new JLabel(T.t("Feedback Movie Background")),
                feedback_movie_background = new ColorButton(new Color(80, 80, 120)), Y, ++X);

        X = 0;
        Y++;
        top.add(new JLabel(T.t("Animation Background")),
                anim_background = new ColorButton(new Color(30, 30, 80)), Y, ++X);

        if (OmegaConfig.isLIU_Mode()) {
            X = 0;
            Y++;
            top.add(new JLabel(T.t("Sign Word Background")),
                    signWord_background = new ColorButton(new Color(30, 30, 80)), Y, ++X);

            X = 0;
            Y++;
            top.add(new JLabel(T.t("Sign Word Transparence")),
                    signWord_alpha = new JSlider(0, 100), Y, ++X);
            signWord_alpha.setMajorTickSpacing(20);
            signWord_alpha.setMinorTickSpacing(5);
            signWord_alpha.setPaintTicks(true);
            signWord_alpha.setPaintLabels(true);

            X = 0;
            Y++;
            top.add(new JLabel(T.t("Sign Word Movie Scale")),
                    signMovieWord_scale = new JSlider(0, 100), Y, ++X);
            signMovieWord_scale.setMajorTickSpacing(20);
            signMovieWord_scale.setMinorTickSpacing(5);
            signMovieWord_scale.setPaintTicks(true);
            signMovieWord_scale.setPaintLabels(true);


            X = 0;
            Y++;
            top.add(new JLabel(T.t("Sign Sentence Background")),
                    signSentence_background = new ColorButton(new Color(30, 30, 80)), Y, ++X);

            X = 0;
            Y++;
            top.add(new JLabel(T.t("Sign Sentence Transparence")),
                    signSentence_alpha = new JSlider(0, 100), Y, ++X);
            signSentence_alpha.setMajorTickSpacing(20);
            signSentence_alpha.setMinorTickSpacing(5);
            signSentence_alpha.setPaintTicks(true);
            signSentence_alpha.setPaintLabels(true);

            X = 0;
            Y++;
            top.add(new JLabel(T.t("Sign Sentence Movie Scale")),
                    signMovieSentence_scale = new JSlider(0, 100), Y, ++X);
            signMovieSentence_scale.setMajorTickSpacing(20);
            signMovieSentence_scale.setMinorTickSpacing(5);
            signMovieSentence_scale.setPaintTicks(true);
            signMovieSentence_scale.setPaintLabels(true);
        }

        c.add(top, BorderLayout.CENTER);

        populateCommon();

        load();

        pack();
    }
}
