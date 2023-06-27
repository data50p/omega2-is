package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.LessonContext;
import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.lesson.appl.LessonEditor;
import com.femtioprocent.omega.swing.TableSorter;
import com.femtioprocent.omega.swing.filechooser.ChooseActionMovieFile;
import com.femtioprocent.omega.swing.filechooser.ChooseGenericFile;
import com.femtioprocent.omega.swing.filechooser.ChooseSignFileAlt;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Value;
import com.femtioprocent.omega.value.Values;
import com.femtioprocent.omega.xml.Element;
import org.hs.jfc.FormPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

public class SentenceProperty extends Property_B {
    static final int COL_SENT = 0;
    static final int COL_ACT = 1;
    static final int COL_SIGN = 2;
    static final int COL_TEST = 3;

    HashMap guimap = new HashMap();
    LessonContext l_ctxt;
    JFrame owner;

    JTable table;
    JButton set_act_b;
    JButton set_sgn_b;
    //SentencePropPanel sn_pan;

    JRadioButton rb_def, rb_act, rb_off;
    JRadioButton rb_defSign, rb_actSign;

    int[][] tmm;


    SentenceProperty(JFrame owner, LessonContext l_ctxt) {
        super(owner, T.t("Omega - Sentence Property"));
        this.owner = owner;
        this.l_ctxt = l_ctxt;
        build(getContentPane());
        pack();
        setVisible(true);
    }

    void destroy() {
    }

    public void refresh() {
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Dimension d2 = new Dimension((int) d.getWidth() + 100, (int) d.getHeight());
        return d2;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        return d;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return d;
    }


    class myActionListener implements ActionListener {
        void set(String what, String value) {
            JTextField tf = (JTextField) guimap.get(what);
            if (value == null) {
                String def = tf.getText();
                value = l_ctxt.getLessonCanvas().askForOneTarget(SentenceProperty.this, def);
                if (value == null)
                    value = def;
            }
            JTextField tf2 = (JTextField) guimap.get("sentence");
            tf2.setText(value);
        }

        public void actionPerformed(ActionEvent ev) {
            String s = ev.getActionCommand();
            if (s.equals("dep_set action file")) {
                LessonEditor.setDirty();
                TableModel tmod = (TableModel) table.getModel();
                int row = table.getSelectedRow();
                String ss = (String) tmod.getValueAt(row, COL_ACT);
                String fn = setActionField(ss);
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "NEW FILE " + fn);
                if (fn != null) {
                    tmod.setValueAt(fn, row, COL_ACT);
                }
            }
            if (s.equals("isDef")) {
                LessonEditor.setDirty();
                set_act_b.setEnabled(false);
                TableModel tmod = (TableModel) table.getModel();
                int row = table.getSelectedRow();
                String ss = (String) tmod.getValueAt(row, COL_ACT);
                String fn = "";//setActionField(ss);
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "NEW FILE " + fn);
                if (fn != null) {
                    tmod.setValueAt(fn, row, COL_ACT);
                }
            }
            if (s.equals("isSpec")) {
                LessonEditor.setDirty();
                set_act_b.setEnabled(true);
                TableModel tmod = (TableModel) table.getModel();
                int row = table.getSelectedRow();
                String ss = (String) tmod.getValueAt(row, COL_ACT);
                if (ss == null || ss.length() == 0) {
                    String fn = setActionField(ss);
                    //log		OmegaContext.sout_log.getLogger().info("ERR: " + "NEW FILE " + fn);
                    if (fn != null) {
                        tmod.setValueAt(fn, row, COL_ACT);
                    }
                }
            }
            if (s.equals("isOff")) {
                LessonEditor.setDirty();
                set_act_b.setEnabled(false);
                TableModel tmod = (TableModel) table.getModel();
                int row = table.getSelectedRow();
                String ss = (String) tmod.getValueAt(row, COL_ACT);
                String fn = "!off";
                if (fn != null) {
                    tmod.setValueAt(fn, row, COL_ACT);
                }
            }

            if (s.equals("dep_set sign file")) {
                LessonEditor.setDirty();
                TableModel tmod = (TableModel) table.getModel();
                int row = table.getSelectedRow();
                String ss = (String) tmod.getValueAt(row, COL_SIGN);
                String fn = setSignField(ss);
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "NEW FILE " + fn);
                if (fn != null) {
                    tmod.setValueAt(fn, row, COL_SIGN);
                }
            }
            if (s.equals("isDefSign")) {
                LessonEditor.setDirty();
                set_sgn_b.setEnabled(false);
                TableModel tmod = (TableModel) table.getModel();
                int row = table.getSelectedRow();
                String ss = (String) tmod.getValueAt(row, COL_SIGN);
                String fn = "";//setActionField(ss);
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "NEW FILE " + fn);
                if (fn != null) {
                    tmod.setValueAt(fn, row, COL_SIGN);
                }
            }
            if (s.equals("isSpecSign")) {
                LessonEditor.setDirty();
                set_sgn_b.setEnabled(true);
                TableModel tmod = (TableModel) table.getModel();
                int row = table.getSelectedRow();
                String ss = (String) tmod.getValueAt(row, COL_SIGN);
                if (ss == null || ss.length() == 0) {
                    String fn = setSignField(ss);
                    //log		OmegaContext.sout_log.getLogger().info("ERR: " + "NEW FILE " + fn);
                    if (fn != null) {
                        tmod.setValueAt(fn, row, COL_SIGN);
                    }
                }
            }
            if (s.equals("dump sent")) {
                ChooseGenericFile choose_f = new ChooseGenericFile();

                String url_s = null;
                int rv = choose_f.showDialog(ApplContext.top_frame, T.t("Save"));
                OmegaContext.sout_log.getLogger().info("ERR: " + "choose file -> " + rv);
                if (rv == JFileChooser.APPROVE_OPTION) {
                    File file = choose_f.getSelectedFile();
                    url_s = Files.toURL(file);
                    String tfn = Files.mkRelativeCWD(url_s);

                    PrintWriter pw = SundryUtils.createPrintWriter(tfn);
                    String sa[] = l_ctxt.getLessonCanvas().getAllTargetCombinations("; ", false);
                    for (int i = 0; i < sa.length; i++) {
                        pw.println(sa[i]);
                    }
                    pw.close();
                }
            }
            if (s.equals("close")) {
                setVisible(false);
            }
        }
    }

    ;
    myActionListener myactl = new myActionListener();

    // when item in table selected
    class MyListSelectionModel extends DefaultListSelectionModel implements ListSelectionListener {
        MyListSelectionModel() {
            addListSelectionListener(this);
        }

        public void valueChanged(ListSelectionEvent ev) {
//log	    OmegaContext.sout_log.getLogger().info("ERR: " + "" + ev);
            if (ev.getValueIsAdjusting() == false) {
                MyListSelectionModel lselmod_ = (MyListSelectionModel) ev.getSource();
                int ix = lselmod_.getMinSelectionIndex();
                TableModel tmod = (TableModel) table.getModel();
                String s = (String) tmod.getValueAt(ix, COL_SENT);
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "SEL " + lselmod_ + ' ' + ix + ' ' + s);
                JTextField tf2 = (JTextField) guimap.get("sentence");
                tf2.setText(s);

                s = (String) tmod.getValueAt(ix, COL_ACT);
                if (s.length() > 0) {
                    rb_act.setSelected(true);
                    set_act_b.setEnabled(true);
                } else {
                    rb_def.setSelected(true);
                    set_act_b.setEnabled(false);
                }
                s = (String) tmod.getValueAt(ix, COL_SIGN);
                if (s.length() > 0) {
                    rb_actSign.setSelected(true);
                    set_sgn_b.setEnabled(true);
                } else {
                    rb_defSign.setSelected(true);
                    set_sgn_b.setEnabled(false);
                }
            }
        }
    }

    ;
    MyListSelectionModel lselmod = new MyListSelectionModel();


    class CloseAction extends AbstractAction {
        CloseAction() {
            super(T.t("Close"));
        }

        public void actionPerformed(ActionEvent ev) {
            setVisible(false);
        }
    }

    ;

    void build(Container con) {
        FormPanel fpan = new FormPanel(5, 5, 7, 15);

        //	JPanel pan1 = new JPanel();
        con.setLayout(new BorderLayout());

        JLabel jl;
        JTextField tf;
        JComboBox cb;
        JCheckBox ch;
        JButton jb;

        int Y = 0;
        int X = 0;

// 	fpan.add(new JLabel(T.t("Parameter:   ")), gbcf.createL(X++, Y, 1));
// 	fpan.add(new JLabel(T.t("Value:          ")),  gbcf.createL(X++, Y, 1));

// 	Y++;
// 	X = 0;
        fpan.add(jl = new JLabel(T.t("Sentence")), tf = new JTextField("", 50), Y, ++X);


        guimap.put("sentence", tf);
        tf.getDocument().addDocumentListener(mydocl);
        tf.setEnabled(false);

        fpan.add(new JLabel(""), jb = new JButton(T.t("Save sentence list")), Y, ++X);
        jb.setActionCommand("dump sent");
        jb.addActionListener(myactl);


        Y++;
        X = 0;
        JRadioButton rb;
        fpan.add(new JLabel(T.t("Type:")), rb = rb_def = new JRadioButton(T.t("Default, as dep_set in word prop")), Y, ++X);

        ButtonGroup bgr = new ButtonGroup();
        bgr.add(rb);
        rb.setActionCommand("isDef");
        rb.addActionListener(myactl);

        Y++;
        X = 0;
        fpan.add(new JLabel(""), rb = rb_act = new JRadioButton(T.t("Specific")), Y, ++X);
        bgr.add(rb);
        rb.setActionCommand("isSpec");
        rb.addActionListener(myactl);

        JPanel jp2 = new JPanel();
        jp2.add(jb = new JButton(T.t("Set action file")));
        jp2.add(new JLabel(""));
        fpan.add(new JLabel(""), jp2, Y, X);
        set_act_b = jb;
        guimap.put("dep_set action file", jb);
        jb.setActionCommand("dep_set action file");
        jb.addActionListener(myactl);

        Y++;
        X = 0;
        fpan.add(new JLabel(""), rb = rb_off = new JRadioButton(T.t("Turn off")), Y, ++X);
        bgr.add(rb);
        rb.setActionCommand("isOff");
        rb.addActionListener(myactl);

        Y++;
        X = 0;
        JRadioButton rbS;
        fpan.add(new JLabel(T.t("Type:")), rbS = rb_defSign = new JRadioButton(T.t("Automatic, play each separate word")), Y, ++X);

        ButtonGroup bgrS = new ButtonGroup();
        bgrS.add(rbS);
        rbS.setActionCommand("isDefSign");
        rbS.addActionListener(myactl);

        Y++;
        X = 0;
        fpan.add(new JLabel(""), rbS = rb_actSign = new JRadioButton(T.t("Specific")), Y, ++X);
        bgrS.add(rbS);
        rbS.setActionCommand("isSpecSign");
        rbS.addActionListener(myactl);

        JPanel jp2S = new JPanel();
        jp2S.add(jb = new JButton(T.t("Set sign file")));
        jp2S.add(new JLabel(""));
        fpan.add(new JLabel(""), jp2S, Y, X);
        set_sgn_b = jb;
        guimap.put("dep_set sign file", jb);
        jb.setActionCommand("dep_set sign file");
        jb.addActionListener(myactl);

        Y++;
        X = 0;
        fpan.add(new JLabel(""), new JLabel(T.t("(Shift) Click on the table header to (reverse) sort")), Y, ++X);

        Y++;
        X = 0;

        String sa[] = l_ctxt.getLessonCanvas().getAllTargetCombinationsEx(" ", false, '{');
        tmm = l_ctxt.getLesson().getTestMatrix(sa);
        OmegaContext.sout_log.getLogger().info("ERR: " + "Got sa sent " + SundryUtils.a2s(sa));
        SenProp_TableModel tmod = new SenProp_TableModel(this, sa, tmm);

        TableSorter tsort = new TableSorter(tmod);

        table = new JTable(tsort);
        tsort.addMouseListenerToHeaderInTable(table);

        JScrollPane jscr = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn tcol = table.getColumnModel().getColumn(i);
            tcol.setPreferredWidth(i == 0 ? 410 :
                    i == 1 ? 180 :
                            i == 2 ? 180 :
                                    40);
        }
        try {
            table.setAutoResizeMode(table.AUTO_RESIZE_OFF);
            table.setSelectionModel(lselmod);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionInterval(0, 0);
            table.setPreferredScrollableViewportSize(new Dimension(830, 300));
        } catch (Exception ex) {
        }

        //	fpan.add(new JLabel(""), jscr, Y, ++X);

// 	JPanel c_pan = new JPanel();
// 	pan1.add(c_pan,  gbcf.createL(X++, Y, 5));

        con.add(fpan, BorderLayout.NORTH);
        con.add(jscr, BorderLayout.CENTER);
        JPanel jpa = new JPanel();
        jpa.add(jb = new JButton(new CloseAction()));
        con.add(jpa, BorderLayout.SOUTH);
    }

    String setActionField(String current) {
        ChooseActionMovieFile choose_f = new ChooseActionMovieFile(false);

        String url_s = null;
        int rv = choose_f.showDialog(ApplContext.top_frame, T.t("Select"));
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "choose file -> " + rv);
        if (rv == JFileChooser.APPROVE_OPTION) {
            File file = choose_f.getSelectedFile();
            url_s = Files.toURL(file);

            String tfn = Files.mkRelativeCWD(url_s);
            tfn = OmegaContext.antiOmegaAssets(tfn);
            return tfn;
        }
        return null;
    }

    String setSignField(String current) {
        ChooseSignFileAlt choose_f = new ChooseSignFileAlt();

        String url_s = null;
        int rv = choose_f.showDialog(ApplContext.top_frame, T.t("Select"));
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "choose file -> " + rv);
        if (rv == JFileChooser.APPROVE_OPTION) {
            File file = choose_f.getSelectedFile();
            url_s = Files.toURL(file);

            String tfn = Files.mkRelativeCWD(url_s);
            tfn = OmegaContext.antiOmegaAssets(tfn);
            return tfn;
        }
        return null;
    }

    public void updValues(Values vs) {
        Iterator it = vs.iterator();
        while (it.hasNext()) {
            Value v = (Value) it.next();
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
