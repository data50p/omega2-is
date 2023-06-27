package com.femtioprocent.omega.lesson.settings;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.appl.OmegaStartManager;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.pupil.Pupil;
import com.femtioprocent.omega.swing.ScaledImageIcon;
import com.femtioprocent.omega.swing.filechooser.*;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
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
import java.io.IOException;
import java.util.*;

public class PupilSettingsDialog extends SettingsDialog {

    public Lesson lesson;
    Pupil pupil;
    JTextField pupil_name = new JTextField("               ");
    JSlider speed_slider;
    JSlider frequency_slider;
    JCheckBox show_sentence;
    JCheckBox show_sound_word;
    JCheckBox show_sign_word;
    JCheckBox show_sign_sentence;
    JCheckBox pingSentence, pingAnim, repeat_anim;
    JLabel image_label, image_wrong_label, movie_label, sign_movie_label;
    JComboBox lang_cb, theme_cb, space_cb;
    JCheckBox text_on, speech_on, image_on, image_wrong_on, movie_on, sign_movie_on;
    JTextField text_tf, speech_tf;
    JButton speech_set, image_wrong_set, image_set, movie_set, sign_movie_set;
    JButton color_change;
    JButton col_login, col_lesson, col_sent, col_words;
    HashMap jcomponent_hm = new HashMap();
    FormPanel cp_laf, cp_lang, cp_anim, cp_feedb, cp_admin;
    //    SecureButton secure_jb;
    JButton secure_jb;
    JButton secure_delete_jb;
    JLabel secure_warning;
    JLabel pupim_jl;
    JButton pupim_jb;
    public boolean was_deleted = false;
    boolean active = false;

    void updColors(String fname) {
	File file = new File(OmegaContext.omegaAssets(fname));
	if (file.canWrite()) {
	    col_lesson.setEnabled(true);
	    col_login.setEnabled(true);
	    col_sent.setEnabled(true);
	    col_words.setEnabled(true);
	} else {
	    col_lesson.setEnabled(false);
	    col_login.setEnabled(false);
	    col_sent.setEnabled(false);
	    col_words.setEnabled(false);
	}
    }

    ActionListener but_al = new ActionListener() {

	public void actionPerformed(ActionEvent ae) {
	    if (ae.getSource() == theme_cb) {
		String label = getSelectedColorFileLabel();
		String fname = getSelectedColorFile();
		if ("+".equals(label))
		    createTheme(fname);
		else
		    updColors(fname);
		return;
	    }

	    String cmd = ae.getActionCommand();

	    if ("selImage".equals(cmd)) {
		String url_s = selImage();
		if (url_s != null) {
		    String tfn = Files.mkRelativeCWD(url_s);
		    image_label.setText(tfn);
		    ImageIcon imc = createImageIcon(tfn, 100, 80);
		    image_label.setIcon(imc);
		    pack();
		    doLayout();
		}
	    }
	    if ("selImageWrong".equals(cmd)) {
		String url_s = selImage();
		if (url_s != null) {
		    String tfn = Files.mkRelativeCWD(url_s);
		    image_wrong_label.setText(tfn);
		    ImageIcon imc = createImageIcon(tfn, 100, 80);
		    image_wrong_label.setIcon(imc);
		    pack();
		    doLayout();
		}
	    }
	    if ("selMovie".equals(cmd)) {
		String url_s = selMovie();
		if (url_s != null) {
		    String tfn = Files.mkRelativeCWD(url_s);
		    String tfno = tfn;
		    movie_label.setText(tfn);
		    tfn = tfn.replaceFirst("\\.mpg", ".png");
		    ImageIcon imc = createImageIcon(tfn, 100, 80);
		    if (imc == null) {
			tfn = tfno + ".png";
			imc = createImageIcon(tfn, 100, 80);
			if (imc == null) {
			    String iim_s = "media/default/moviefeedback.png";
			    imc = createImageIcon(iim_s, 100, 80);
			}
		    }
		    movie_label.setIcon(imc);
		    pack();
		    doLayout();
		}
	    }
	    if ("selSignMovie".equals(cmd)) {
		String url_s = selMoviesDir();
		if (url_s != null) {
		    String tfn = Files.mkRelativeCWD(url_s);
		    File f = new File(tfn);
		    if (!f.exists()) {
			JOptionPane.showMessageDialog(AnimContext.top_frame,
				T.t("Invalid directory name."),
				"Omega",
				JOptionPane.WARNING_MESSAGE);
			return;
		    }
		    String tfno = tfn;
		    sign_movie_label.setText(tfn);
		    ImageIcon imc = createImageIcon(tfn + "moviesignfeedback.png", 100, 80);
		    if (imc == null) {
			String iim_s = "media/default/moviesignfeedback.png";
			imc = createImageIcon(iim_s, 100, 80);
			if (imc == null) {
			    iim_s = "media/default/moviefeedback.png";
			    imc = createImageIcon(iim_s, 100, 80);
			}
		    }
		    sign_movie_label.setIcon(imc);
		    pack();
		    doLayout();
		}
	    }
	    if ("selSpeech".equals(cmd)) {
		String url_s = selSpeech();
		if (url_s != null) {
		    String tfn = Files.mkRelativeCWD(url_s);
		    if (tfn.startsWith("media/")) {
			tfn = tfn.substring(6);
		    }
		    speech_tf.setText(tfn);
		}
	    }
	    if ("activate".equals(cmd)) {
		active = !active;
		if (active) {
		    showMore();
		} else {
		    showNoMore();
		}
	    }
	    if ("deletePupil".equals(cmd)) {
		deletePupil();
		showNoMore();
	    }
	    if ("change_color_lesson".equals(cmd)) {
		if (lesson != null) {
		    lesson.displayColor("main");
		}
	    }
	    if ("change_color_words".equals(cmd)) {
		if (lesson != null) {
		    lesson.displayColor("words");
		}
	    }
	    if ("change_color_login".equals(cmd)) {
		if (lesson != null) {
		    lesson.displayColor("pupil");
		}
	    }
	    if ("change_color_sent".equals(cmd)) {
		if (lesson != null) {
		    lesson.displayColor("sent");
		}
	    }
	    if ("pupim".equals(cmd)) {
		try {
		    ChoosePupilImageFile choose_pif = new ChoosePupilImageFile();
		    int rv = choose_pif.showDialog(PupilSettingsDialog.this, T.t("Select"));
		    if (rv == JFileChooser.APPROVE_OPTION) {
			File file = choose_pif.getSelectedFile();
			String fn = file.getName();
			int ix = fn.lastIndexOf('.');
			String ext = ".jpg";
			if (ix != -1) {
			    ext = file.getName().substring(ix);
			}
			String pims = "register/" + pupil.getName() + ".p/id" + ext;
			File pimf = new File(pims);
			Files.fileCopy(file, pimf);
			ImageIcon imc2 = createImageIcon(pims, 80, 60);
			pupim_jl.setIcon(imc2);
		    }
		} catch (Exception ex) {
		    OmegaContext.sout_log.getLogger().info("ERR: " + "ex " + ex);
		    ex.printStackTrace();
		}
	    }
	    if ("resetStarter".equals(cmd)) {
		OmegaStartManager.enableStarter();
	    }
	}
    };

    private void createTheme(String fname) {
	System.err.println("Create this " + fname);
	if (OmegaContext.omegaAssetsExist(fname))
	    return;
	File fnew = new File(OmegaContext.omegaAssets(fname));
	File fdef = new File(OmegaContext.omegaAssets("default.omega_colors"));
	try {
	    fnew.createNewFile();
	    SundryUtils.copyFile(fdef, fnew);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	MultiString ms = new MultiString(T.t(fname), new String[]{fname});
	theme_cb.removeAllItems();
	addColorItems();
	theme_cb.setSelectedItem(ms);
    }

    private void addColorItems() {
	Vector themes_v = getThemes();
	for (Object o : themes_v)
	    theme_cb.addItem(o);
    }

    ImageIcon createImageIcon(String fn, int max_w, int max_h) {
	return ScaledImageIcon.createImageIcon(this,
		fn,
		max_w,
		max_h);
    }

    void update_movie_on() {
// 	boolean b = ! movie_on.isSelected();
// 	text_on.setEnabled(b);
// 	speech_on.setEnabled(b);
// 	image_on.setEnabled(b);
// 	text_tf.setEnabled(b);
// 	speech_tf.setEnabled(b);
// 	image_label.setEnabled(b);
// 	speech_set.setEnabled(b);
// 	image_set.setEnabled(b);
// 	movie_label.setEnabled(!b);
// 	movie_set.setEnabled(!b);
    }

    void update_sign_movie_on() {
// 	boolean b = ! movie_on.isSelected();
// 	text_on.setEnabled(b);
// 	speech_on.setEnabled(b);
// 	image_on.setEnabled(b);
// 	text_tf.setEnabled(b);
// 	speech_tf.setEnabled(b);
// 	image_label.setEnabled(b);
// 	speech_set.setEnabled(b);
// 	image_set.setEnabled(b);
// 	movie_label.setEnabled(!b);
// 	movie_set.setEnabled(!b);
    }

    public PupilSettingsDialog(Lesson lesson) {
	super("Omega - " + T.t("Pupil Settings"));
	this.lesson = lesson;
	Container c = getContentPane();
	c.setLayout(new BorderLayout());

	JPanel pan = new JPanel() {

	    public Insets getInsets() {
		return new Insets(5, 5, 5, 5);
	    }
	};


	cp_anim = new FormPanel(5, 5, 7, 15);
	cp_feedb = new FormPanel(5, 5, 7, 10);
	cp_lang = new FormPanel(5, 5, 7, 15);
	cp_laf = new FormPanel(5, 5, 7, 15);
	cp_admin = new FormPanel(5, 5, 7, 15);
	JPanel cp_lang_panel = new JPanel();
	JPanel cp_laf_panel = new JPanel();
	JPanel cp_admin_panel = new JPanel();
	cp_lang_panel.add(cp_lang);
	cp_laf_panel.add(cp_laf);
	cp_admin_panel.add(cp_admin);

	JTabbedPane tabbed_pane = new JTabbedPane();
	tabbed_pane.addTab(T.t("Animation"), null, cp_anim, T.t("Settings for Animations"));
	tabbed_pane.addTab(T.t("Feedback"), null, cp_feedb, T.t("Settings for Feedback"));
	tabbed_pane.addTab(T.t("Language"), null, cp_lang_panel, T.t("Settings for Language"));
	tabbed_pane.addTab(T.t("Look&Feel"), null, cp_laf_panel, T.t("Settings for Look&Feel"));
	tabbed_pane.addTab(T.t("Admin"), null, cp_admin_panel, T.t("Some administrative settings"));

	pan.add(tabbed_pane, BorderLayout.CENTER);

	c.add(pan, BorderLayout.CENTER);


	// cp_anim
	speed_slider = new JSlider(0, 2);
	speed_slider.setSnapToTicks(true);
	speed_slider.setMajorTickSpacing(1);
	speed_slider.setSnapToTicks(true);
	speed_slider.setPaintLabels(true);
	speed_slider.setPaintTrack(true);
	if (true) {
	    Hashtable lt = new Hashtable();
	    lt.put(0, new JLabel(T.t("slow")));
	    lt.put(1, new JLabel(T.t("normal")));
	    lt.put(2, new JLabel(T.t("fast")));
	    speed_slider.setLabelTable(lt);
	}
	jcomponent_hm.put("speed", speed_slider);

	JCheckBox cb;

	int X = 0;
	int Y = 1;
	cp_anim.add(new JLabel(T.t("Speed")), speed_slider, Y, ++X);
	Y++;
	X = 0;

	if (true) {
	    pingSentence = new JCheckBox();
	    pingAnim = new JCheckBox();
	    repeat_anim = new JCheckBox();

	    cp_anim.add(new JLabel(T.t("Repeat animation twice")), repeat_anim, Y, ++X);
	    Y++;
	    X = 0;
	    cp_anim.add(new JLabel(T.t("Ping after complete sentence")), pingSentence, Y, ++X);
	    Y++;
	    X = 0;
	    cp_anim.add(new JLabel(T.t("Ping before/after animation")), pingAnim, Y, ++X);

	    jcomponent_hm.put("repeatanim", repeat_anim);
	    jcomponent_hm.put("pingSentence", pingSentence);
	    jcomponent_hm.put("pingAnim", pingAnim);
	}

	Y++;
	X = 0;
	if (true) {
	    cp_anim.add(new JLabel(T.t("Show Sentence")), show_sentence = new JCheckBox(), Y, ++X);
	    jcomponent_hm.put("showSentence", show_sentence);
	}

	Y++;
	X = 0;
	cp_anim.add(new JLabel(T.t("Sound after each word")), show_sound_word = new JCheckBox(), Y, ++X);
	jcomponent_hm.put("showSoundWord", show_sound_word);

	if (OmegaConfig.isLIU_Mode()) {
	    Y++;
	    X = 0;
	    cp_anim.add(new JLabel(T.t("Show Sign after each word")), show_sign_word = new JCheckBox(), Y, ++X);
	    jcomponent_hm.put("showSignWord", show_sign_word);
	    Y++;
	    X = 0;
	    cp_anim.add(new JLabel(T.t("Show Sign after sentence")), show_sign_sentence = new JCheckBox(), Y, ++X);
	    jcomponent_hm.put("showSignSentence", show_sign_sentence);
	}


	// -----------

	X = 0;
	Y = 1;

	JRadioButton rb;
	JTextField tf;

	JLabel jl0;
	JCheckBox chb;

	ActionListener item_sel_al = new ActionListener() {

	    public void actionPerformed(ActionEvent ae) {
		JCheckBox chb = (JCheckBox) ae.getSource();
		if (chb == movie_on) {
		    update_movie_on();
		    if (chb.getModel().isSelected()) {
			sign_movie_on.setSelected(false);
		    }
		}
		if (chb == sign_movie_on) {
		    update_sign_movie_on();
		    if (chb.getModel().isSelected()) {
			movie_on.setSelected(false);
		    }
		}
	    }
	};

	Y++;
	X = 0;
	cp_feedb.add(new JLabel(T.t("Item: Text")), chb = new JCheckBox(), Y, ++X);
	cp_feedb.add(tf = new JTextField("", 20), new JLabel(""), Y, ++X);
	chb.addActionListener(item_sel_al);
	text_on = chb;
	text_tf = tf;
	jcomponent_hm.put("text", tf);
	jcomponent_hm.put("text_on", chb);

	Y++;
	X = 0;
	if (true) {
	    JButton jb;
	    JRadioButton rb0, rb1;
	    cp_feedb.add(new JLabel(T.t("Item: Speech")), chb = new JCheckBox(), Y, ++X);
	    cp_feedb.add(tf = new JTextField("", 20), jb = new JButton(T.t("Set...")), Y, ++X);
	    chb.addActionListener(item_sel_al);
	    jb.addActionListener(but_al);
	    jb.setActionCommand("selSpeech");
	    jcomponent_hm.put("speech", tf);
	    jcomponent_hm.put("speech_on", chb);
	    speech_tf = tf;
	    speech_on = chb;
	    speech_set = jb;
	}

	if (true) {
	    Y++;
	    X = 0;
	    JLabel jl;
	    String im_s = "media/default/feedback.png";
	    JButton sjb;
	    cp_feedb.add(new JLabel(T.t("Item: Image, positive")), chb = new JCheckBox(), Y, ++X);
	    cp_feedb.add(jl = new JLabel(im_s), sjb = new JButton(T.t("Set...")), Y, ++X);
	    chb.addActionListener(item_sel_al);
	    sjb.addActionListener(but_al);
	    sjb.setActionCommand("selImage");
	    ImageIcon imc = createImageIcon(im_s, 100, 80);
	    jl.setIcon(imc);
	    image_label = jl;
	    image_on = chb;
	    image_set = sjb;
	    jcomponent_hm.put("image", image_label);
	    jcomponent_hm.put("image_on", chb);
	}

	if (true) {
	    Y++;
	    X = 0;
	    JLabel jl;
	    String im_s = "media/default/feedbackwrong.png";
	    JButton sjb;
	    cp_feedb.add(new JLabel(T.t("Item: Image, negative")), chb = new JCheckBox(), Y, ++X);
	    cp_feedb.add(jl = new JLabel(im_s), sjb = new JButton(T.t("Set...")), Y, ++X);
	    chb.addActionListener(item_sel_al);
	    sjb.addActionListener(but_al);
	    sjb.setActionCommand("selImageWrong");
	    ImageIcon imc = createImageIcon(im_s, 100, 80);
	    jl.setIcon(imc);
	    image_wrong_label = jl;
	    image_wrong_on = chb;
	    image_wrong_set = sjb;
	    jcomponent_hm.put("image_wrong", image_wrong_label);
	    jcomponent_hm.put("image_wrong_on", chb);
	}

	if (OmegaConfig.isLIU_Mode()) {
	    Y++;
	    X = 0;
	    JLabel jl;
	    String im_s = "media/default/signFeedbackRight";
	    JButton sjb;
	    cp_feedb.add(new JLabel(T.t("Item: Sign Movies, positive ")), chb = new JCheckBox(), Y, ++X);
	    cp_feedb.add(jl = new JLabel(im_s), sjb = new JButton(T.t("Set...")), Y, ++X);
	    chb.addActionListener(item_sel_al);
	    sjb.addActionListener(but_al);
	    sjb.setActionCommand("selSignMovie");
	    ImageIcon imc = createImageIcon(im_s, 100, 80);
	    jl.setIcon(imc);
	    sign_movie_label = jl;
	    sign_movie_on = chb;
	    sign_movie_set = sjb;
	    jcomponent_hm.put("sign_movie", sign_movie_label);
	    jcomponent_hm.put("sign_movie_on", chb);
	}

	if (true) {
	    JLabel jl;
	    JButton sjb;
	    Y++;
	    X = 0;
	    String im_s = "";
	    cp_feedb.add(new JLabel(T.t("Item: Movie")), chb = new JCheckBox(), Y, ++X);
	    cp_feedb.add(jl = new JLabel(im_s), sjb = new JButton(T.t("Set...")), Y, ++X);
	    chb.addActionListener(item_sel_al);
	    sjb.addActionListener(but_al);
	    sjb.setActionCommand("selMovie");
	    String iim_s = "media/default/moviefeedback.png";
	    ImageIcon imc = createImageIcon(iim_s, 100, 80);
	    jl.setIcon(imc);
	    movie_label = jl;
	    movie_on = chb;
	    movie_set = sjb;
	    jcomponent_hm.put("movie", movie_label);
	    jcomponent_hm.put("movie_on", chb);
	}

	Y++;
	X = 0;
	if (true) {
	    cp_feedb.add(new JLabel(T.t("Frequence")), frequency_slider = new JSlider(0, 3), Y, ++X);
	    frequency_slider.setSnapToTicks(true);
	    frequency_slider.setMajorTickSpacing(1);
	    frequency_slider.setSnapToTicks(true);
	    frequency_slider.setPaintLabels(true);
	    frequency_slider.setPaintTrack(true);
	    if (true) {
		Hashtable lt = new Hashtable();
		lt.put(0, new JLabel(T.t("none")));
		lt.put(1, new JLabel(T.t("seldom")));
		lt.put(2, new JLabel(T.t("often")));
		lt.put(3, new JLabel(T.t("always")));
		frequency_slider.setLabelTable(lt);
	    }
	    jcomponent_hm.put("frequence", frequency_slider);
	}

	// -----------

	X = 0;
	Y = 1;

	Vector lang_v = getLanguages(); // list of suffix to appemd to "lesson"
	cp_lang.add(new JLabel(T.t("Language")), lang_cb = new JComboBox(lang_v), Y, ++X);
	jcomponent_hm.put("languageSuffix", lang_cb);
	X = 0;
	Y++;
	Vector themes_v = getThemes();
	cp_laf.add(new JLabel(T.t("Color theme")), theme_cb = new JComboBox(themes_v), Y, ++X);
	theme_cb.addActionListener(but_al);
	jcomponent_hm.put("theme", theme_cb);

	JButton jb;
	X = 0;
	Y++;
	cp_laf.add(new JLabel(T.t("Change color")), jb = new JButton(T.t("Pupil Login")), Y, ++X);
	jb.setActionCommand("change_color_login");
	jb.addActionListener(but_al);
	col_login = jb;

	X = 0;
	Y++;
	cp_laf.add(new JLabel(""), jb = new JButton(T.t("Lesson select")), Y, ++X);
	jb.setActionCommand("change_color_lesson");
	jb.addActionListener(but_al);
	col_lesson = jb;

	X = 0;
	Y++;
	cp_laf.add(new JLabel(""), jb = new JButton(T.t("Story display")), Y, ++X);
	jb.setActionCommand("change_color_sent");
	jb.addActionListener(but_al);
	col_sent = jb;

	X = 0;
	Y++;
	cp_laf.add(new JLabel(""), jb = new JButton(T.t("Sentence build")), Y, ++X);
	jb.setActionCommand("change_color_words");
	jb.addActionListener(but_al);
	col_words = jb;

	X = 0;
	Y++;
	Vector space_v = getSpaceKeys();
	cp_laf.add(new JLabel(T.t("Space key is")), space_cb = new JComboBox(space_v), Y, ++X);
	jcomponent_hm.put("space_key", space_cb);

	// -----------

	X = 0;
	Y = 1;

	if (true) {
	    cp_admin.add(pupim_jl = new JLabel(T.t("Pupil Image")),
		    pupim_jb = new JButton(T.t("Set Pupil Image...")), Y, ++X);
	    String val = "register/" + "Guest" + ".p/id.png";
	    ImageIcon imc2 = createImageIcon(val, 80, 60);
	    pupim_jl.setIcon(imc2);
	    pupim_jb.addActionListener(but_al);
	    pupim_jb.setActionCommand("pupim");
	}
	X = 0;
	Y++;
//	cp_admin.add(new JLabel(T.t("")),  secure_jb = new SecureButton(this, T.t("Activate 'Delete pupil'")), Y, ++X);
	cp_admin.add(new JLabel(T.t("")), secure_jb = new JButton(T.t("Activate 'Delete Pupil'")), Y, ++X);
	secure_jb.setActionCommand("activate");
	secure_jb.addActionListener(but_al);

	X = 0;
	Y++;
	cp_admin.add(new JLabel(T.t("")),
		secure_warning = new JLabel("<html><h3>" + T.t("Warning!!!") + "</h3>"
			+ T.t("The pupil data will ramain in the file system.<br>")
			+ "</html>"),
		Y,
		++X);
	X = 0;
	Y++;
	cp_admin.add(new JLabel(T.t("")), secure_delete_jb = new JButton(T.t("Delete pupil")), Y, ++X);
	secure_delete_jb.setActionCommand("deletePupil");
	secure_delete_jb.addActionListener(but_al);

	X = 0;
	Y++;
	cp_admin.add(new JLabel(T.t("")), jb = new JButton(T.t("Reset Starter")), Y, ++X);
	jb.setActionCommand("resetStarter");
	jb.addActionListener(but_al);

	// -----------

	JPanel tpan = new JPanel();
	tpan.add(new JLabel(T.t("Pupil Name:")));
	tpan.add(pupil_name);
	pupil_name.setEditable(false);

	c.add(tpan, BorderLayout.NORTH);

	populateCommon();

	pack();
	doLayout();

	secure_delete_jb.setVisible(false);
	secure_warning.setVisible(false);
    }

    void add(Element el, String key, String val) {
	if (key == null || val == null) {
	    return;
	}
	Element el1 = new Element("value");
	el1.addAttr("key", key);
	el1.addAttr("val", val);
	el.add(el1);
    }

    void upd_jcomponent(String key, String val) {
	JComponent cmp = (JComponent) jcomponent_hm.get(key);

	if (cmp != null) {
	    if (cmp instanceof JSlider) {
		JSlider slider = (JSlider) cmp;
		int a = Integer.parseInt(val);
		slider.setValue(a);
		return;
	    }
	    if (cmp instanceof JTextField) {
		JTextField tf = (JTextField) cmp;
		tf.setText(val);
		return;
	    }
	    if (cmp instanceof JCheckBox) {
		JCheckBox cb = (JCheckBox) cmp;
		cb.setSelected("true".equals(val));
		return;
	    }
	    if (cmp instanceof JComboBox) {
		JComboBox cb = (JComboBox) cmp;
		if (cb == lang_cb) {
		    for (int i = 0; i < 100; i++) {
			MultiString ms = (MultiString) cb.getItemAt(i);
			if (ms.sa[0].equals(val)) {
			    cb.setSelectedIndex(i);
			    return;
			}
		    }
		}
		if (cb == theme_cb) {
		    for (int i = 0; i < 100; i++) {
			MultiString ms = (MultiString) cb.getItemAt(i);
			if (ms.sa[0].equals(val)) {
			    cb.setSelectedIndex(i);
			    updColors(val);
			    return;
			}
		    }
		}
		if (cb == space_cb) {
		    for (int i = 0; i < 10; i++) {
			MultiString ms = (MultiString) cb.getItemAt(i);
			if (ms.sa[0].equals(val)) {
			    cb.setSelectedIndex(i);
			    return;
			}
		    }
		}
		cb.setSelectedItem(val);
		return;
	    }
	    if (cmp instanceof JRadioButton) {
		JRadioButton rb = (JRadioButton) cmp;
		rb.setSelected("true".equals(val));
		return;
	    }
	    if (cmp instanceof JLabel) {
		JLabel jl = (JLabel) cmp;
		jl.setText(val);
		if (jl == image_label) {
		    ImageIcon imc = createImageIcon(val, 100, 80);
		    jl.setIcon(imc);
		    pack();
		    doLayout();
		}
		if (jl == image_wrong_label) {
		    ImageIcon imc = createImageIcon(val, 100, 80);
		    jl.setIcon(imc);
		    pack();
		    doLayout();
		}
		if (jl == movie_label) {
		    String valo = val;
		    val = val.replaceFirst("\\.mpg", ".png");
		    ImageIcon imc = createImageIcon(val, 100, 80);
		    if (imc == null) {
			val = valo + ".png";
			imc = createImageIcon(val, 100, 80);
			if (imc == null) {
			    imc = createImageIcon("media/default/moviefeedback.png", 100, 80);
			}
		    }
		    jl.setIcon(imc);
		    pack();
		    doLayout();
		}
	    }
	    return;
	}
    }

    Element getElements() {
	Element el = new Element("values");

	Iterator it = jcomponent_hm.keySet().iterator();
	while (it.hasNext()) {
	    String key = (String) it.next();
	    JComponent com = (JComponent) jcomponent_hm.get(key);
	    if (com == lang_cb) {
		String lang = ((MultiString) lang_cb.getSelectedItem()).sa[0];
		add(el, "languageSuffix", lang);
	    } else if (com == theme_cb) {
		String th = ((MultiString) theme_cb.getSelectedItem()).sa[0];
		add(el, "theme", th);
	    } else if (com == space_cb) {
		String th = ((MultiString) space_cb.getSelectedItem()).sa[0];
		add(el, "space_key", th);
	    } else {
		String val = getValue(com);
		add(el, key, val);
	    }
	}
	return el;
    }

    private String getValue(JComponent cmp) {
	if (cmp != null) {
	    if (cmp instanceof JSlider) {
		JSlider slider = (JSlider) cmp;
		return "" + slider.getValue();
	    }
	    if (cmp instanceof JTextField) {
		JTextField tf = (JTextField) cmp;
		return tf.getText();
	    }
	    if (cmp instanceof JCheckBox) {
		JCheckBox cb = (JCheckBox) cmp;
		return cb.isSelected() ? "true" : "false";
	    }
	    if (cmp instanceof JRadioButton) {
		JRadioButton rb = (JRadioButton) cmp;
		return rb.isSelected() ? "true" : "false";
	    }
	    if (cmp instanceof JLabel) {
		JLabel jl = (JLabel) cmp;
		return jl.getText();
	    }
	    return "?_";
	}
	return "?~";
    }

    String selImage() {
	ChooseImageFile choose_if = new ChooseImageFile();
	String url_s = null;
	int rv = choose_if.showDialog(this, T.t("Select"));
	if (rv == JFileChooser.APPROVE_OPTION) {
	    File file = choose_if.getSelectedFile();
	    return Files.toURL(file);
	}
	return null;
    }

    String selMovie() {
	ChooseMovieFile choose_mf = new ChooseMovieFile();
	String url_s = null;
	int rv = choose_mf.showDialog(this, T.t("Select"));
	if (rv == JFileChooser.APPROVE_OPTION) {
	    File file = choose_mf.getSelectedFile();
	    return Files.toURL(file);
	}
	return null;
    }

    String selMoviesDir() {
	ChooseMovieDir choose_md = new ChooseMovieDir();
	String url_s = null;
	int rv = choose_md.showDialog(this, T.t("Select Directory"));
	if (rv == JFileChooser.APPROVE_OPTION) {
	    File file = choose_md.getSelectedFile();
	    return Files.toURL(file);
	}
	return null;
    }

    String selSpeech() {
	ChooseSpeechFile choose_sf = new ChooseSpeechFile();
	String url_s = null;
	int rv = choose_sf.showDialog(this, T.t("Select"));
	if (rv == JFileChooser.APPROVE_OPTION) {
	    File file = choose_sf.getSelectedFile();
	    return Files.toURL(file);
	}
	return null;
    }

    // nb_NO_BN
    Vector getLanguages() {
	String inLang = T.lang;
	boolean bergen = false;
	int lang_bergen = -1;
	if ("nb_NO_BN".equals(T.lang)) {
	    inLang = "nb";
	    bergen = true;
	}

	Locale inlocale = new Locale(T.lang);
	File dot = new File(OmegaContext.omegaAssets("."));
	String[] scanned_lang = dot.list(new java.io.FilenameFilter() {

	    public boolean accept(File dir, String name) {
		if (name.startsWith("lesson-")) // LESSON-DIR
		{
		    return true;
		}
		return false;
	    }
	});
	Locale lA[] = new Locale[scanned_lang.length];
	for (int i = 0; i < lA.length; i++) {
	    String l = scanned_lang[i].substring(7);
	    if ("nb_NO_BN".equals(l)) {
		l = "nb";
		lang_bergen = i;
	    }
	    lA[i] = new Locale(l);
	}
	Vector v = new Vector();
	v.add(new MultiString(T.t("Default language"), new String[]{""}));

	for (int i = 0; i < lA.length; i++) {
	    String dn = lA[i].getDisplayName(inlocale);
	    String la = "" + lA[i].getLanguage();
	    if (dn.equals("no")) {
		dn = "bokmål";
		la = "nb";
	    }
	    if (dn.equals("nb")) {
		dn = "bokmål";
	    }
	    if (dn.equals("nn")) {
		dn = "nynorsk";
	    }
	    if (i == lang_bergen) {
		dn = "bokmål (bergen)";
		la = "nb_NO_BN";
	    }
	    OmegaContext.lesson_log.getLogger().info("Lang: DN LA " + dn + ' ' + la);
	    v.add(new MultiString(dn, new String[]{la}));
	}
	return v;
    }

    Vector getThemes() {
	File dot = new File(OmegaContext.omegaAssets("."));
	String[] scanned_themes = dot.list(new java.io.FilenameFilter() {

	    public boolean accept(File dir, String name) {
		if (name.endsWith(".omega_colors")) {
		    return true;
		}
		return false;
	    }
	});
	Vector v = new Vector();
	v.add(new MultiString(T.t("Default Theme"), new String[]{"default.omega_colors"}));

	for (int i = 0; i < scanned_themes.length; i++) {
	    v.add(new MultiString(scanned_themes[i], new String[]{scanned_themes[i]}));
	}
	int n = freeOption();
	if (n > 0)
	    v.add(new MultiString(T.t("+"), new String[]{"option" + n + ".omega_colors"}));
	return v;
    }

    private int freeOption() {
	for (int i = 1; i <= 9; i++) {
	    if (OmegaContext.omegaAssetsExist("option" + i + ".omega_colors"))
		continue;
	    return i;
	}
	return -1;
    }

    Vector getSpaceKeys() {
	Vector v = new Vector();
	v.add(new MultiString(T.t("select next"), new String[]{"next"}));
	v.add(new MultiString(T.t("activate selected"), new String[]{"select"}));
	return v;
    }

    String getPupilDir(Pupil pup) {
	if (pupil == null) {
	    return "register/Guest.p";
	}
	return "register/" + pup.getName() + ".p";
    }

    void loadDefault() {
	upd_jcomponent("text", "");
	upd_jcomponent("speech", "");
	upd_jcomponent("image", "media/default/feedback.png");
	upd_jcomponent("image_wrong", "media/default/feedbackwrong.png");
	upd_jcomponent("movie", "");
	upd_jcomponent("text_on", "true");
	upd_jcomponent("speech_on", "false");
	upd_jcomponent("image_on", "false");
	upd_jcomponent("image_wrong_on", "false");
	upd_jcomponent("movie_on", "false");
	upd_jcomponent("speed", "1");
	upd_jcomponent("showSentence", "true");
	upd_jcomponent("showSignWord", "true");
	upd_jcomponent("showSoundWord", "true");
	upd_jcomponent("showSignSentence", "true");
	upd_jcomponent("pingSentence", "true");
	upd_jcomponent("pingAnim", "false");
	upd_jcomponent("frequence", "3");
	upd_jcomponent("theme", "default.omega_colors");
	upd_jcomponent("space_key", "next");
	upd_jcomponent("sign_movie_on", "false");
	upd_jcomponent("sign_movie", "");
    }

    boolean save() {
        String dir = getPupilDir(pupil);
        if ( SundryUtils.empty(dir) ) {
            return false;
	}
	String fname = dir + "/pupil_settings.xml";
	Element pel = new Element("pupil_settings");
	Element el = getElements();
	pel.add(el);
	try (XML_PW xmlpw = new XML_PW(SundryUtils.createPrintWriterUTF8(fname), false) ) {
	    xmlpw.put(pel);
	    return true;
	} catch (Exception ex) {
	    ex.printStackTrace();
    	}
    	return false;
    }

    void load() {
	String fname = getPupilDir(pupil) + "/pupil_settings.xml";

	loadDefault();
	try {
	    Element el = SAX_node.parse(fname, false);
	    if (el == null) {
		loadDefault();
		return;
	    }
	    Element vel = el.findElement("values", 0);
	    for (int i = 0; i < 1000; i++) {
		Element el1 = vel.findElement("value", i);
		if (el1 == null) {
		    break;
		}
		String key = el1.findAttr("key");
		String val = el1.findAttr("val");
		upd_jcomponent(key, val);
	    }
	} catch (Exception ex) {
	    loadDefault();
	}
	update_movie_on();
	pack();
	doLayout();
    }

    public HashMap getParams() {
	String fname = getPupilDir(pupil) + "/pupil_settings.xml";
	try {
	    Element el = SAX_node.parse(fname, false);
	    if (el == null) {
		return null;
	    }
	    HashMap hm = new HashMap();

	    Element vel = el.findElement("values", 0);
	    for (int i = 0; vel != null && i < 1000; i++) {
		Element el1 = vel.findElement("value", i);
		if (el1 == null) {
		    break;
		}
		String key = el1.findAttr("key");
		String val = el1.findAttr("val");
		hm.put(key, val);
	    }
	    return hm;
	} catch (Exception ex) {
	}
	return null;
    }

    public void setPupil(Pupil pupil) {
	this.pupil = pupil;
	String pname = pupil.getName();
	String pnameL = pupil.getName();
	if (pname.equals("Guest")) {
	    pnameL = T.t("Guest");
	}
//	secure_jb.cb.setEnabled(! "Guest".equals(pupil.getName()));
	load();
	pupil_name.setText(pnameL);
	String val = "register/" + pname + ".p/id.png";
	ImageIcon imc2 = createImageIcon(val, 80, 60);
	pupim_jl.setIcon(imc2);
	pack();
    }

    private void deletePupil() {
	File file = new File("register/" + pupil.getName() + ".p");
	File file2 = new File("register/" + pupil.getName() + ".deleted");
	if (file2.exists()) {
	    File file3 = new File("register/" + pupil.getName() + ".deleted_" + SundryUtils.ct());
	    file2.renameTo(file3);
	}
	file.renameTo(file2);
	was_deleted = true;
    }

    void showMore() {
	secure_jb.setText(T.t("Deactivate"));
	secure_delete_jb.setVisible(true);
	secure_warning.setVisible(true);
	pack();
	doLayout();
    }

    void showNoMore() {
	secure_jb.setText(T.t("Activate 'Delete Pupil'"));
	secure_delete_jb.setVisible(false);
	secure_warning.setVisible(false);
	pack();
    }

    public String getSelectedColorFile() {
	String th = ((MultiString) theme_cb.getSelectedItem()).sa[0];
	return th;
    }

    public String getSelectedColorFileLabel() {
	String th = ((MultiString) theme_cb.getSelectedItem()).s;
	return th;
    }
}
