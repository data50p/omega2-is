package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Value;
import com.femtioprocent.omega.value.Values;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

public class TargetProperty extends Property_B {
    HashMap guimap = new HashMap();

    TargetProperty(JFrame owner) {
        super(owner, T.t("Omega - Target Property"));
        build(getContentPane());
        pack();
        setVisible(true);
    }

    public void refresh() {
    }

    void build(Container con) {
        con.setLayout(new GridBagLayout());

        JLabel jl;
        JTextField tf;
        JComboBox cb;

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
        con.add(jl = new JLabel("Text"), gbcf.createL(X++, Y, 1));
        con.add(tf = new JTextField("Text", 20), gbcf.createL(X++, Y, 1));
        tf.getDocument().addDocumentListener(mydocl);
        tf.setEnabled(false);
        guimap.put("text", tf);
        guimap.put("Ltext", jl);

        Y++;
        X = 0;
        con.add(jl = new JLabel(T.t("Slot id")), gbcf.createL(X++, Y, 1));
        con.add(tf = new JTextField("Text", 20), gbcf.createL(X++, Y, 1));
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("tid", tf);
        guimap.put("Ltid", jl);

        Y++;
        X = 0;
        con.add(jl = new JLabel(T.t("Path id")), gbcf.createL(X++, Y, 1));
        con.add(tf = new JTextField("Text", 20), gbcf.createL(X++, Y, 1));
        tf.getDocument().addDocumentListener(mydocl);
        guimap.put("lid", tf);
        guimap.put("Llid", jl);

        con.add(cb = new JComboBox(), gbcf.createL(X++, Y, 1));
        guimap.put("Slid", cb);
        cb.addItem(T.t("(Select in list)"));
        cb.addItem(T.t("(Clear data)"));
        cb.addItemListener(myiteml);

// 	Y++;
// 	 X = 0;
// 	con.add(jl = new JLabel(T.t("Type")),                 gbcf.createL(X++, Y, 1));
// 	con.add(cb = new JComboBox(),     gbcf.createL(X++, Y, 1));
// //	con.add(tf = new JTextField("Text", 20),    gbcf.createL(X++, Y, 1));
// //	tf.getDocument().addDocumentListener(mydocl);
// 	cb.addItemListener(myiteml);
// 	cb.addItem("passive");
// 	cb.addItem("action");
// 	guimap.put("type", cb);
// 	guimap.put("Ltype", jl);
    }

    public void updValues(Values vs) {
        Iterator it = vs.iterator();
        while (it.hasNext()) {
            Value v = (Value) it.next();

//log	    OmegaContext.sout_log.getLogger().info(":--: " + "V " + v);

            if (v.getId().equals("pathlist")) {         // banor
                JComboBox cb = (JComboBox) guimap.get("Slid");
                String ss = v.getStr();
                if (ss != null) {
                    String[] sa = SundryUtils.split(ss, ",");
                    cb.removeAllItems();
                    cb.addItem(T.t("(Select in list)"));
                    cb.addItem(T.t("(Clear data)"));
                    for (int i = 0; i < sa.length; i++)
                        cb.addItem("" + sa[i]);
//  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("+ " + sa[i]);
                    cb.setSelectedIndex(0);
                    pack();
                    continue;
                } else
                    OmegaContext.sout_log.getLogger().info(":--: " + "ss is null " + v);
            }

            Object gui = guimap.get(v.id);
            if (gui instanceof JTextField) {
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

            cbg = (JComboBox) guimap.get("type");
            if (cb == cbg) {
                String s = (String) cb.getSelectedItem();
//log  		OmegaContext.sout_log.getLogger().info(":--: " + "CB type " + cb);
/*
                  if ( s.equals("action") )
  		    setLabel("Llid", "Path id");
  		if ( s.equals("actor") )
    		    setLabel("Llid", "Path id");
  		else
  		    setLabel("Llid", "Path id");
*/
//log		OmegaContext.sout_log.getLogger().info(":--: " + "=0= " + cb);
                fireValueChanged(new Value("type", s));
            }

            cbg = (JComboBox) guimap.get("Slid");
            if (cb == cbg) {
                JTextField tf = (JTextField) guimap.get("lid");
                updTF(tf, cbg);
            }

        } catch (ClassCastException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "CCE " + ex);
        }
    }
}
