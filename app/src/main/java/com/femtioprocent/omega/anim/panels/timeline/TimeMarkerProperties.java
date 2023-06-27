package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.appl.AnimEditor;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker;
import com.femtioprocent.omega.anim.tool.timeline.TriggerEvent;
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventFactory;
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventSelections;
import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.swing.filechooser.ChooseAudioFile;
import com.femtioprocent.omega.swing.properties.OmegaProperties;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;

public class TimeMarkerProperties extends OmegaProperties implements ActionListener {
    public AnimContext a_ctxt;

    TimeMarker bound_tm = null;

    TriggerEventPanel[] event_panels;

    JLabel j_lb[] = new JLabel[7];

    JTextField lesson_id;

    private boolean skipDirty = false;

    public TimeMarkerProperties(AnimContext a_ctxt, JFrame owner) {
        super(owner);

        this.a_ctxt = a_ctxt;
        event_panels = new TriggerEventPanel[TriggerEventFactory.getSize()];
        setTitle("Omega - " + T.t("Marker Properties"));
        setSize(300, 200);
    }

    public TimeMarker getTM() {
        return (TimeMarker) obj;
    }

    private void setDirty() {
        if (skipDirty == false)
            AnimContext.ae.setDirty(true);
    }

    public void refresh() {
        skipDirty = true;
        TimeMarker tm = getTM();
        if (tm != null) {
            buildProperties(tm);
            pack();
        } else {
            OmegaContext.sout_log.getLogger().info("ERR: " + "tm null");
        }
        skipDirty = false;
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
            updTrigger(cb);
        }
    }

    ;
    myItemListener myiteml = new myItemListener();

    class OnOffItemEvent implements ItemListener {
        public void itemStateChanged(ItemEvent ie) {
            JCheckBox cb = (JCheckBox) ie.getItemSelectable();
            updTriggerOnOff(cb);
        }
    }

    ;
    OnOffItemEvent onoff_listener = new OnOffItemEvent();


    private HashMap hm_onoff = new HashMap();
    private HashMap hm_doc = new HashMap();
    private HashMap hm_cb = new HashMap();


    void initCache() {
        hm_onoff = new HashMap();
        hm_doc = new HashMap();
        hm_cb = new HashMap();
    }

    private void buildProperties(TimeMarker tm) {
        bound_tm = tm;

        if (j_lb[0] == null) {
            Container con = getContentPane();
            con.setLayout(new BorderLayout());

            JPanel top_pan = new JPanel();
            JPanel r_top_pan = new JPanel();
            JPanel l_top_pan = new JPanel();
            top_pan.add(l_top_pan);
            top_pan.add(r_top_pan);

            con.add(top_pan, BorderLayout.NORTH);

            r_top_pan.setLayout(new BoxLayout(r_top_pan, BoxLayout.Y_AXIS));

            int ix = 0;
            j_lb[ix] = new JLabel(T.t("TimeMarker for timeline") + ' ');
            j_lb[ix].setForeground(Color.black);
            j_lb[++ix] = new JLabel(T.t("Ordinal = "));
            j_lb[++ix] = new JLabel(T.t("Ordinal(type) = "));
            j_lb[++ix] = new JLabel(T.t("When = "));
            j_lb[++ix] = new JLabel(T.t("Type = "));
            j_lb[++ix] = new JLabel(T.t("Duration = "));

            for (int i = 0; i < ix; i++) {
                r_top_pan.add(j_lb[i]);
            }

            l_top_pan.add(new Label(T.t("Path ID:")));
            l_top_pan.add(lesson_id = new JTextField("", 10));
            Document doc2 = lesson_id.getDocument();
            doc2.addDocumentListener(mydocl);

            JPanel pp = new JPanel();
            pp.setLayout(new GridBagLayout());

            GBC_Factory c = new GBC_Factory();

            for (int i = -1; i < tm.t_event.length; i++) {
                if (i == -1) {
                    JLabel jlb;
                    pp.add(jlb = new JLabel(T.t("Event")), c.create(0, 0));
                    pp.add(jlb = new JLabel(T.t("Argument") + "               "),
                            c.create(1, 0));
                    pp.add(jlb = new JLabel(T.t("Note/Description")), c.create(2, 0));
                    pp.add(jlb = new JLabel(T.t("is On")), c.create(3, 0));
                } else {
                    TriggerEvent te = tm.t_event[i];
                    TriggerEventPanel tep = new TriggerEventPanel1(te);
                    tep.cb.addItemListener(onoff_listener);
                    if (tep.cell_edit instanceof JTextField) {
                        Document doc = ((JTextField) tep.cell_edit).getDocument();
                        doc.addDocumentListener(mydocl);
                        hm_doc.put(doc, i);
                    }
                    if (tep.cell_edit instanceof JComboBox) {
                        JComboBox cb = (JComboBox) tep.cell_edit;
                        String[] sel = te.getSelections_human();
                        for (int ii = 0; ii < sel.length; ii++) {
                            cb.addItem(sel[ii]);
                        }
                        cb.addItemListener(myiteml);
                        hm_cb.put(cb, i);
                    }
                    pp.add(tep.label, c.create(0, i + 1));
                    pp.add(tep.cell_edit, c.create(1, i + 1));
                    pp.add(tep.help, c.create(2, i + 1));
                    pp.add(tep.cb, c.create(3, i + 1));
                    JButton jb;
                    String[] ext_list;
                    if ((ext_list = te.getFiles()) != null) {
                        pp.add(jb = new JButton(T.t("Set...")), c.create(4, i + 1));
                        jb.setActionCommand("set_file");
                        jb.addActionListener(this);
                    }
                    hm_onoff.put(tep.cb, i);
                    event_panels[i] = tep;
                }
            }
            con.add(pp, BorderLayout.CENTER);

            JButton jb = new JButton(T.t("Close"));
            jb.setActionCommand("Close");
            jb.addActionListener(this);
            JPanel jpan = new JPanel();
            jpan.add(jb);
            con.add(jpan, BorderLayout.SOUTH);
        }
        if (true) {
            int ix = 0;
            j_lb[ix++].setText(T.t("TimeMarker for timeline ") + tm.tl.nid);
            j_lb[ix++].setText(T.t("Ordinal = ") + tm.ord);
            //	    j_lb[ix++].setText(T.t("Ordinal(type) = ") + tm.ord_same_type);
            j_lb[ix++].setText(T.t("When = ") + tm.when);
            j_lb[ix++].setText(T.t("Type = ") + tm.type + ", " + tm.typeString(tm.type));
            j_lb[ix++].setText(T.t("Duration = ") + tm.duration);

            lesson_id.setText(tm.tl.getLessonId());

            if ((tm.type == tm.TRIGGER ||
                    tm.type == tm.START ||
                    tm.type == tm.STOP ||
                    tm.type == tm.BEGIN ||
                    tm.type == tm.END
            ) && tm.t_event != null) {

                for (int i = -1; i < tm.t_event.length; i++) {
                    if (i == -1) {
                    } else {
                        TriggerEvent te = tm.t_event[i];
                        TriggerEventPanel tep = event_panels[i];
                        String val = te.getArgString_human();
                        if (te.hasSelections())
                            tep.setEC(val);
                        else
                            tep.setEC_TF(val);
                        tep.cb.setSelected(te.is_on);
                        tep.help.setEditable(false);
                    }
                }
            }
        }
    }

    public void updTriggerOnOff(JCheckBox cb) {
        setDirty();
        TimeMarker tm = bound_tm;

        if (cb != null) {
            Integer I = (Integer) hm_onoff.get(cb);
            if (I != null) {
                int i = I.intValue();
                tm.t_event[i].is_on = cb.isSelected();
            }
        }
        return;
    }

    public void updTrigger(Document doc) {
        setDirty();
        TimeMarker tm = bound_tm;

        if (tm == null)
            return;

        if (lesson_id.getDocument() == doc) {
            tm.tl.setLessonId(lesson_id.getText());
            AnimEditor ae = a_ctxt.ae;
            if (ae != null) {
                ae.tlp.repaint();
                ae.cabaret_panel.repaint();
            }
            return;
        }

        if (tm != null && tm.t_event != null)
            for (int i = 0; i < tm.t_event.length; i++) {
                TriggerEvent te = tm.t_event[i];
                if (te != null) {
                    if (!te.hasSelections()) {
                        Integer I = (Integer) hm_doc.get(doc);
                        if (I != null) {
                            if (I.intValue() == i) {
                                try {
                                    te.setArg(doc.getText(0, doc.getLength()));
                                } catch (BadLocationException ex) {
                                    OmegaContext.sout_log.getLogger().info("ERR: " + "" + ex);
                                }
                            }
                        }
                    }
                }
            }
    }

    public void updTrigger(JComboBox cb) {
        if (cb == null) {
            return;
        }

        setDirty();
        TimeMarker tm = bound_tm;
        if (tm != null && tm.t_event != null)
            for (int i = 0; i < tm.t_event.length; i++) {
                TriggerEvent te = tm.t_event[i];
                if (te != null) {
                    if (te.hasSelections()) {
                        TriggerEventSelections tes = (TriggerEventSelections) te;
                        Integer I = (Integer) hm_cb.get(cb);
                        if (I != null) {
                            if (I.intValue() == i) {
                                tes.setArgFromHuman((String) cb.getSelectedItem());
                            }
                        }
                    }
                }
            }
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("set_file")) {
            ChooseAudioFile choose_f = new ChooseAudioFile();

            String url_s = null;
            int rv = choose_f.showDialog(null, T.t("Load"));
            if (rv == JFileChooser.APPROVE_OPTION) {
                File file = choose_f.getSelectedFile();
                url_s = Files.toURL(file);
// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "FILE " + url_s);
                String fn = Files.mkRelFnameAlt(url_s, "media");
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "FILE " + fn);
                TriggerEvent te = bound_tm.findTEvent("PlaySound");
                te.setArg(fn);
                refresh();
            }
        }

        if (ev.getActionCommand().equals("Close")) {
            setVisible(false);
            return;
        }
    }
}

