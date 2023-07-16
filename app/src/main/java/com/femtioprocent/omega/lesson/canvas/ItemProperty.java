package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.appl.LessonEditor;
import com.femtioprocent.omega.lesson.repository.Locator;
import com.femtioprocent.omega.swing.filechooser.ChooseActionMovieFile;
import com.femtioprocent.omega.swing.filechooser.ChooseAnimatorFile;
import com.femtioprocent.omega.swing.filechooser.ChooseAudioFile;
import com.femtioprocent.omega.swing.filechooser.ChooseSignFile;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Value;
import com.femtioprocent.omega.value.Values;
import org.hs.jfc.FormPanel;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class ItemProperty extends Property_B implements ActionListener {
    HashMap guimap = new HashMap();
    boolean skip_dirty = false;

    JTextField text_tf;
    JTextField fname_tf;

    ItemProperty(JFrame owner) {
        super(owner, T.t("Omega - Item property"));
        build(getContentPane());
        pack();
        setVisible(true);
    }

    String toURL(File file) {
        return Files.toURL(file);
    }

    String getFName() {
        String fn = null;
        try {
            String url_s = null;
            ChooseAnimatorFile choose_af = new ChooseAnimatorFile();
            int rv = choose_af.showDialog(this, T.t("Select"));
            if (rv == JFileChooser.APPROVE_OPTION) {
                File file = choose_af.getSelectedFile();
                url_s = toURL(file);
                String aFname = Files.mkRelFname(url_s);
                String fname = OmegaContext.antiOmegaAssets(aFname);
                OmegaContext.sout_log.getLogger().info("getFName: (~A) " + fname);
                return fname;
            }
        } catch (Exception ex) {
            OmegaContext.exc_log.getLogger().throwing(ItemProperty.class.getName(), "getFName", ex);
        }
        return null;
    }

    public void refresh() {
    }

    class JLabelL extends JLabel {
        JLabelL(String s) {
            super(s);
            setHorizontalTextPosition(SwingConstants.RIGHT);
        }
    }

    void build(Container con) {
        skip_dirty = true;
        int WW = 35;

        FormPanel fpan = new FormPanel(5, 5, 7, 15);

        //	con.setLayout(new GridBagLayout());
        JComboBox cb;
        JLabel jl;
        JTextField tf;
        JButton bt;

        int Y = 0;
        int X = 0;
        fpan.add(jl = new JLabelL(T.t("Text")), tf = new JTextField("Text", WW), Y, ++X);
        text_tf = tf;
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("Ltext", jl);
        guimap.put("text", tf);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("TTS")), tf = new JTextField("", WW), Y, ++X);
        text_tf = tf;
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("Ltts", jl);
        guimap.put("tts", tf);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("Sound")), tf = new JTextField("Sound File", WW - 5), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("Lsound", jl);
        guimap.put("sound", tf);
        fpan.add(jl = new JLabelL(T.t("")), bt = new JButton("..."), Y, X);
        guimap.put("Bsound", bt);
        bt.setActionCommand("sound_set");
        bt.addActionListener(this);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("Sign")), tf = new JTextField("Sign File", WW - 5), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("Lsign", jl);
        guimap.put("sign", tf);
        fpan.add(jl = new JLabelL(T.t("")), bt = new JButton("..."), Y, X);
        guimap.put("Bsign", bt);
        bt.setActionCommand("sign_set");
        bt.addActionListener(this);


        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("(Post Test Dummy Text):")), tf = new JTextField("", WW), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setBackground(new Color(220, 220, 220));
        guimap.put("Ldummytext", jl);
        guimap.put("dummytext", tf);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("(Post Test Dummy Sound):")), tf = new JTextField("", WW - 5), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setBackground(new Color(220, 220, 220));
        guimap.put("Ldummysound", jl);
        guimap.put("dummysound", tf);
        fpan.add(jl = new JLabelL(T.t("")), bt = new JButton("..."), Y, X);
        guimap.put("Bdummysound", bt);
        bt.setActionCommand("dummysound_set");
        bt.addActionListener(this);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("(Post Test Dummy Sign):")), tf = new JTextField("", WW - 5), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setBackground(new Color(220, 220, 220));
        guimap.put("Ldummysign", jl);
        guimap.put("dummysign", tf);
        fpan.add(jl = new JLabelL(T.t("")), bt = new JButton("..."), Y, X);
        guimap.put("Bdummysign", bt);
        bt.setActionCommand("dummysign_set");
        bt.addActionListener(this);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("Slot id list")), tf = new JTextField("", WW), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("Ltid", jl);
        guimap.put("tid", tf);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("Actor id")), tf = new JTextField("", WW - 8), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("Llid", jl);
        guimap.put("lid", tf);
        fpan.add(new JLabelL(""), cb = new JComboBox(), Y, X);
        guimap.put("actors", cb);
        cb.addItem("");
        cb.addItemListener(myiteml);

        Y++;
        X = 0;
        fpan.add(jl = new JLabelL(T.t("Action File")), tf = new JTextField("Action File", WW - 5), Y, ++X);
        fname_tf = tf;
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("fname", tf);
        guimap.put("Lfname", jl);
        fpan.add(new JLabelL(""), cb = new JComboBox(), Y, X);
        cb.addItem(T.t("(Select in list)"));
        cb.addItem(T.t("<Select file...>"));
        Locator loc = new Locator();
        String[] sa = loc.getAllActiveFiles(OmegaContext.omegaAssets("lesson-" + OmegaContext.getLessonLang() + "/active"), "omega_anim"); // LESSON-DIR-A
        sa = OmegaContext.antiOmegaAssets(sa);
        for (int i = 0; i < sa.length; i++)
            cb.addItem(sa[i]);
        cb.addItemListener(myiteml);
        guimap.put("fnamelist", cb);

        Y++;
        X = 0;
        JTextField tf2;
        fpan.add(jl = new JLabelL(T.t("Variables")), tf = new JTextField("V1", 10), Y, ++X);
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("v1", tf);
        fpan.add(tf = new JTextField("V2", 10), tf2 = new JTextField("V3", 10), Y, X);
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("v2", tf);
        tf2.getDocument().addDocumentListener(mydocl);
        guimap.put("v3", tf2);

        fpan.add(tf = new JTextField("", 10), tf2 = new JTextField("", 10), Y, X);
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("v4", tf);
        tf2.getDocument().addDocumentListener(mydocl);
        guimap.put("v5", tf2);
        guimap.put("LvN", jl);

        con.add(fpan);

// 	con.add(new JPanel() {
// 		public void paintComponent(Graphics g) {
// 		    super.paintComponent(g);
// 		    g.setColor(Color.blue);
// 		    g.fillRect(0, 0, 12, 12);
// 		    g.setColor(Color.magenta);
// 		    g.fillRect(12, 0, 12, 12);
// 		}
// 	    }, gbcf.createL(X++, Y, 1));

        skip_dirty = false;
    }

    // when text changes
    void updTrigger(Document doc) {
        Iterator it = guimap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object o = guimap.get(key);
            if (o instanceof JTextField) {
                JTextField tf = (JTextField) o;
                if (doc == tf.getDocument()) {
                    String txt = tf.getText();
                    OmegaContext.sout_log.getLogger().info(":--: " + "updTrigger: " + txt + ' ' + tf);
                    fireValueChanged(new Value(key, txt));
                    if (!skip_dirty)
                        LessonEditor.setDirty();
                }
            }
        }

    }

    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if ("sound_set".equals(cmd)) {
            ChooseAudioFile choose_f = new ChooseAudioFile();
            int rv = choose_f.showDialog(null, T.t("Load"));
            if (rv == JFileChooser.APPROVE_OPTION) {
                File file = choose_f.getSelectedFile();
                String url_s = Files.toURL(file);
// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
                String fn = Files.mkRelFnameAlt(url_s, "media");
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);

                JTextField tf = (JTextField) guimap.get("sound");
                tf.setText(fn);
                fireValueChanged(new Value("sound", fn));
                if (!skip_dirty)
                    LessonEditor.setDirty();
            }
        }
        if ("sign_set".equals(cmd)) {
            ChooseSignFile choose_f = new ChooseSignFile();
            int rv = choose_f.showDialog(null, T.t("Load"));
            if (rv == JFileChooser.APPROVE_OPTION) {
                File file = choose_f.getSelectedFile();
                String url_s = Files.toURL(file);
// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
                String fn = Files.mkRelFnameAlt(url_s, "media");
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);

                JTextField tf = (JTextField) guimap.get("sign");
                tf.setText(fn);
                fireValueChanged(new Value("sign", fn));
                if (!skip_dirty)
                    LessonEditor.setDirty();
            }
        }
        if ("dummysound_set".equals(cmd)) {
            ChooseAudioFile choose_f = new ChooseAudioFile();
            int rv = choose_f.showDialog(null, T.t("Load"));
            if (rv == JFileChooser.APPROVE_OPTION) {
                File file = choose_f.getSelectedFile();
                String url_s = Files.toURL(file);
// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
                String fn = Files.mkRelFnameAlt(url_s, "media");
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);

                JTextField tf = (JTextField) guimap.get("dummysound");
                tf.setText(fn);
                fireValueChanged(new Value("dummysound", fn));
                if (!skip_dirty)
                    LessonEditor.setDirty();
            }
        }
        if ("dummysign_set".equals(cmd)) {
            ChooseSignFile choose_f = new ChooseSignFile();
            int rv = choose_f.showDialog(null, T.t("Load"));
            if (rv == JFileChooser.APPROVE_OPTION) {
                File file = choose_f.getSelectedFile();
                String url_s = Files.toURL(file);
// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
                String fn = Files.mkRelFnameAlt(url_s, "media");
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);

                JTextField tf = (JTextField) guimap.get("dummysign");
                tf.setText(fn);
                fireValueChanged(new Value("dummysign", fn));
                if (!skip_dirty)
                    LessonEditor.setDirty();
            }
        }
    }

    void updTrigger(JComboBox cb) {
        try {
            JComboBox cbg;

//  	    cbg = (JComboBox)guimap.get("ftype");
//  	    if ( cb == cbg ) {
//  		String s = (String)cb.getSelectedItem();
//  	    }
            cbg = (JComboBox) guimap.get("actors");
            if (cb == cbg) {
                JTextField tf = (JTextField) guimap.get("lid");
                updTF(tf, cbg);
            }
            cbg = (JComboBox) guimap.get("fnamelist");
            if (cb == cbg) {
                String s = (String) cb.getSelectedItem();
                String fn;

                if (T.t("<Select file...>").equals(s)) {
                    fn = getFName();
                } else {
                    fn = s;
                }
                if (fn != null) {
                    int ix = fn.lastIndexOf('.');
                    if (ix != -1) {
                        String s1 = fn.substring(0, ix);
                        String s2 = fn.substring(ix + 1);
                        JTextField tf = (JTextField) guimap.get("fname");
                        tf.setText(s1);
//			JComboBox cb2 = (JComboBox)guimap.get("ftype");
//			cb2.setSelectedItem(s2);
                    }
                }
                cb.setSelectedIndex(0);
            }

        } catch (ClassCastException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "CCE " + ex);
        }
    }

    public synchronized void updValues(Values vs) {
        skip_dirty = true;
        long ct0 = SundryUtils.ct();

        Iterator it = vs.iterator();
        while (it.hasNext()) {
            Value v = (Value) it.next();

            if (v.getId().equals("actorlist")) {
                JComboBox cb = (JComboBox) guimap.get("actors");
                String sc = v.getStr();
                cb.removeAllItems();
                cb.addItem(T.t("(Select item in list)"));
                cb.addItem(T.t("(Clear data)"));
                if (sc != null) {
                    String[] sa = SundryUtils.split(sc, ",");
                    for (int i = 0; i < sa.length; i++)
                        cb.addItem("" + sa[i]);
//  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("+ " + sa[i]);
                    cb.setSelectedIndex(0);
                    pack();
//  		    String[] sa = SundryUtils.split(sc, ",");
//  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("= " + sa[i]);
//  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("+ " + sa[i]);
//  		    cb.setSelectedIndex(0);
//  		    pack();
                }
                continue;
            }

            Object gui = guimap.get(v.id);
            if (gui instanceof JTextField) {
//log		OmegaContext.sout_log.getLogger().info(":--: " + "VVV " + v);
                JTextField tf = (JTextField) gui;
                tf.setText(v.getStr());
            }
            if (gui instanceof JComboBox) {
                JComboBox cb = (JComboBox) gui;
                cb.setSelectedItem(v.getStr());
            }
            gui = guimap.get("L" + v.id);
            if (gui instanceof JLabel) {
                JLabel jl = (JLabel) gui;
//		jl.setText(v.getId());
            }
//  	    if ( v.getId().equals("ftype") ) {
//  //		JComboBox cb = (JComboBox)guimap.get("ftype");
//  		JComboBox cb2 = (JComboBox)guimap.get("fnamelist");
//  		JTextField tf = (JTextField)guimap.get("fname");
//  		String s = v.getStr();
//  		if ( s.length() >= 0 ) {
//  //		    cb.setEnabled(true);
//  		    cb2.setEnabled(true);
//  		    tf.setEnabled(true);
//  		} else {
//  //		    cb.setEnabled(false);
//  		    cb2.setEnabled(false);
//  		    tf.setEnabled(false);
//  		}
//  	    }
        }
        long ct1 = SundryUtils.ct();
//log	OmegaContext.sout_log.getLogger().info(":--: " + "iprop " + (ct1-ct0));
        skip_dirty = false;
    }
}
