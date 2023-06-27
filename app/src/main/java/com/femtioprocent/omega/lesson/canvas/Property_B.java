package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Value;
import com.femtioprocent.omega.value.ValuesListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class Property_B extends JDialog {
    EventListenerList lc_listeners;

    GBC_Factory gbcf = new GBC_Factory();

    Property_B(JFrame owner, String title) {
        super(owner, title);
        lc_listeners = new EventListenerList();
    }

    Property_B(JFrame owner, String title, boolean modal) {
        super(owner, title, modal);
        lc_listeners = new EventListenerList();
    }

    Property_B(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        lc_listeners = new EventListenerList();
    }

    public void addValuesListener(ValuesListener l) {
        lc_listeners.add(ValuesListener.class, l);
    }

    public void removeValuesListener(ValuesListener l) {
        lc_listeners.remove(ValuesListener.class, l);
    }

    void fireValueChanged(Value v) {
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "fireValueChanged " + v);
        Object[] lia = lc_listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((ValuesListener) lia[i + 1]).changed(v);
        }
    }

    class myDocumentListener implements DocumentListener {
        public void changedUpdate(DocumentEvent de) {
            Document doc = de.getDocument();
            updTrigger(doc);
        }

        public void insertUpdate(DocumentEvent de) {
            Document doc = de.getDocument();
            updTrigger(doc);
        }

        public void removeUpdate(DocumentEvent de) {
            Document doc = de.getDocument();
            updTrigger(doc);
        }
    }

    myDocumentListener mydocl = new myDocumentListener();

    class myItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent ie) {
            JComboBox cb = (JComboBox) ie.getItemSelectable();
            if (ie.getStateChange() == ItemEvent.SELECTED)
                updTrigger(cb);
        }
    }

    ;
    myItemListener myiteml = new myItemListener();

    void updTrigger(Document doc) {
    }

    void updTrigger(JComboBox cb) {
    }


    void updTF(JTextField tf, JComboBox cb) {
        if (false) {
            String s = (String) cb.getSelectedItem();
            if (s != null && s.length() > 0) {
                String[] sa = SundryUtils.split(s, " ");
                if (sa[0].equals("+")) {
                    String ss = tf.getText();
                    if (ss.length() > 0)
                        tf.setText(ss + ',' + sa[1]);
                    else
                        tf.setText(sa[1]);
                } else if (sa[0].equals("=")) {
                    tf.setText(sa[1]);
                }
            }
        } else {
            int ix = cb.getSelectedIndex();
            if (ix == 0)
                return;
            if (ix == 1) {
                tf.setText("");
                cb.setSelectedIndex(0);
                return;
            }
            String s = (String) cb.getSelectedItem();
            if (s != null && s.length() > 0) {
                String ss = tf.getText();
                if (ss.length() > 0)
                    tf.setText(ss + ',' + s);
                else
                    tf.setText(s);
                cb.setSelectedIndex(0);
            }
        }
    }
}
