package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.value.Value;
import com.femtioprocent.omega.value.Values;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

public class TestProperty extends Property_B {
    HashMap guimap = new HashMap();
    LessonContext l_ctxt;

    TestProperty(JFrame owner, LessonContext l_ctxt) {
        super(owner, T.t("Omega - Test Property"));
        this.l_ctxt = l_ctxt;
        build(getContentPane());
        pack();
        setVisible(true);
    }

    public void refresh() {
    }

    class myActionListener implements ActionListener {
        void set(String what, String value) {
            JTextField tf = (JTextField) guimap.get(what);
            if (value == null) {
                String def = tf.getText();
                value = l_ctxt.getLessonCanvas().askForOneTarget(TestProperty.this, def);
                if (value == null)
                    value = def;
            }
            tf.setText(value);
        }

        public void actionPerformed(ActionEvent ev) {
            String s = ev.getActionCommand();
            if (s.equals("setpret1")) {
                set("pret1", null);
            }
            if (s.equals("setpret2")) {
                set("pret2", null);
            }
            if (s.equals("setpostt1")) {
                set("postt1", null);
            }
            if (s.equals("setpostt2")) {
                set("postt2", null);
            }
            if (s.equals("setpret1_")) {
                set("pret1", l_ctxt.getTarget().getAllText());
            }
            if (s.equals("setpret2_")) {
                set("pret2", l_ctxt.getTarget().getAllText());
            }
            if (s.equals("setpostt1_")) {
                set("postt1", l_ctxt.getTarget().getAllText());
            }
            if (s.equals("setpostt2_")) {
                set("postt2", l_ctxt.getTarget().getAllText());
            }
        }
    }

    ;
    myActionListener myactl = new myActionListener();

    void build(Container con) {
        con.setLayout(new GridBagLayout());

        JLabel jl;
        JTextField tf;
        JComboBox cb;
        JCheckBox ch;
        JButton jb;

        int Y = 0;
        int X = 0;

        con.add(new JLabel(T.t("Parameter:   ")), gbcf.createL(X++, Y, 1));
        con.add(new JLabel(T.t("Value          ")), gbcf.createL(X++, Y, 1));
        con.add(new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.green);
                g.fillRect(0, 0, 12, 12);
            }
        }, gbcf.createL(X++, Y, 1));

        Y++;
        X = 0;
        con.add(jl = new JLabel(T.t("Pretest 1")), gbcf.createL(X++, Y, 1));
        con.add(tf = new JTextField("", 50), gbcf.createL(X++, Y, 1));
        con.add(jb = new JButton(T.t("Set shown")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpret1_");
        jb.addActionListener(myactl);
        con.add(jb = new JButton(T.t("Set from list")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpret1");
        jb.addActionListener(myactl);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setEnabled(false);
        guimap.put("pret1", tf);
        guimap.put("Lpret1", jl);

        Y++;
        X = 0;
        con.add(jl = new JLabel(T.t("Pretest 2")), gbcf.createL(X++, Y, 1));
        con.add(tf = new JTextField("", 50), gbcf.createL(X++, Y, 1));
        con.add(jb = new JButton(T.t("Set shown")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpret2_");
        jb.addActionListener(myactl);
        con.add(jb = new JButton(T.t("Set from list")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpret2");
        jb.addActionListener(myactl);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setEnabled(false);
        guimap.put("pret2", tf);
        guimap.put("Lpret2", jl);

        Y++;
        X = 0;
        con.add(jl = new JLabel(T.t("Posttest 1")), gbcf.createL(X++, Y, 1));
        con.add(tf = new JTextField("", 50), gbcf.createL(X++, Y, 1));
        con.add(jb = new JButton(T.t("Set shown")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpostt1_");
        jb.addActionListener(myactl);
        con.add(jb = new JButton(T.t("Set from list")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpostt1");
        jb.addActionListener(myactl);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setEnabled(false);
        guimap.put("postt1", tf);
        guimap.put("Lpostt1", jl);

        Y++;
        X = 0;
        con.add(jl = new JLabel(T.t("Posttest 2")), gbcf.createL(X++, Y, 1));
        con.add(tf = new JTextField("", 50), gbcf.createL(X++, Y, 1));
        con.add(jb = new JButton(T.t("Set shown")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpostt2");
        jb.addActionListener(myactl);
        con.add(jb = new JButton(T.t("Set from list")), gbcf.createL(X++, Y, 1));
        jb.setActionCommand("setpostt2_");
        jb.addActionListener(myactl);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setEnabled(false);
        guimap.put("postt2", tf);
        guimap.put("Lpostt2", jl);

        Y++;
        X = 0;
        con.add(jl = new JLabel(T.t("Settings")), gbcf.createL(X++, Y, 1));
        con.add(ch = new JCheckBox(T.t("Show word box")), gbcf.createL(X++, Y, 1));
        guimap.put("shwbx", ch);
        guimap.put("Lshwbx", jl);

        Y++;
        X = 0;
        con.add(jl = new JLabel(""), gbcf.createL(X++, Y, 1));
        con.add(ch = new JCheckBox(T.t("Hilite word box")), gbcf.createL(X++, Y, 1));
        guimap.put("hlwbx", ch);
        guimap.put("Lhlwbx", jl);
    }

    public void updValues(Values vs) {
        Iterator it = vs.iterator();
        while (it.hasNext()) {
            Value v = (Value) it.next();

//  	    if ( v.getId().equals("pathlist") ) {         // banor
//  		JComboBox cb = (JComboBox)guimap.get("Slid");
//  		String[] sa = SundryUtils.split(v.getStr(), ",");
//  		cb.removeAllItems();
//  		cb.addItem("");
//  		for(int i = 0; i < sa.length; i++)
//  		    cb.addItem("= " + sa[i]);
//  		for(int i = 0; i < sa.length; i++)
//  		    cb.addItem("+ " + sa[i]);
//  		cb.setSelectedIndex(0);
//  		pack();
//  		continue;
//  	    }

//  	    Object gui = guimap.get(v.id);
//  	    if ( gui instanceof JTextField ) {
//  		JTextField tf = (JTextField)gui;
//  		tf.setText(v.getStr());
//  	    }
//  	    if ( gui instanceof JComboBox ) {
//  		JComboBox cb = (JComboBox)gui;
//  		cb.setSelectedItem(v.getStr());
//  	    }
//  	    gui = guimap.get("L" + v.id);
//  	    if ( gui instanceof JLabel ) {
//  		JLabel jl = (JLabel)gui;
//  //		jl.setText(v.getId());
//  	    }
        }
    }

    void updTrigger(Document doc) {
        Iterator it = guimap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object o = guimap.get(key);
            if (o instanceof JTextField) {
                JTextField tf = (JTextField) o;
                if (doc == tf.getDocument()) {
                    String txt = tf.getText();
                    fireValueChanged(new Value(key, txt));
                }
            }
        }
    }

    void setLabel(String id, String txt) {
        Iterator it = guimap.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.equals(id)) {
                Object o = guimap.get(key);
                if (o instanceof JLabel) {
                    JLabel jl = (JLabel) o;
                    jl.setText(txt);
                }
            }
        }
    }

    void updTrigger(JComboBox cb) {
        try {
            JComboBox cbg;

//  	    cbg = (JComboBox)guimap.get("type");
//  	    if ( cb == cbg ) {
//  		String s = (String)cb.getSelectedItem();
//  		OmegaContext.sout_log.getLogger().info("ERR: " + "CB type " + cb);
//  		if ( s.equals("action") )
//  		    setLabel("Llid", "Path id");
//  		if ( s.equals("actor") )
//    		    setLabel("Llid", "Path id");
//  		else
//  		    setLabel("Llid", "-");
//  	    }

            cbg = (JComboBox) guimap.get("Slid");
            if (cb == cbg) {
                JTextField tf = (JTextField) guimap.get("lid");
                updTF(tf, cbg);
            }

        } catch (ClassCastException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "CCE " + ex);
        }
    }

    void updTrigger(JCheckBox ch) {
        try {
            JCheckBox chg;

            chg = (JCheckBox) guimap.get("Slid");
        } catch (ClassCastException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "CCE " + ex);
        }
    }

    public Element getElement() {
        Element el = new Element("test_prop");

        Element pel = new Element("test");
        pel.addAttr("kind", "pre");
        pel.addAttr("ord", "1");
        pel.addAttr("text", ((JTextField) (guimap.get("pret1"))).getText());
        el.add(pel);

        pel = new Element("test");
        pel.addAttr("kind", "pre");
        pel.addAttr("ord", "2");
        pel.addAttr("text", ((JTextField) (guimap.get("pret2"))).getText());
        el.add(pel);

        pel = new Element("test");
        pel.addAttr("kind", "post");
        pel.addAttr("ord", "1");
        pel.addAttr("text", ((JTextField) (guimap.get("postt1"))).getText());
        el.add(pel);

        pel = new Element("test");
        pel.addAttr("kind", "post");
        pel.addAttr("ord", "2");
        pel.addAttr("text", ((JTextField) (guimap.get("postt2"))).getText());
        el.add(pel);

        return el;
    }
}
