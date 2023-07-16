package com.femtioprocent.omega.lesson.canvas.result;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.register.data.*;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.swing.filechooser.ChooseExportFile;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.SundryUtils;
import org.hs.jfc.FormPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;


public class ResultDialogTableDetail extends JDialog
        implements ListSelectionListener,
        ActionListener,
        ChangeListener {
    java.awt.Frame owner;

    JScrollPane results_sp;

    JTextField lesson_name;
    JTextField pupil_name;
    JTextField stat;
    Lesson.RegisterProxy register;

    String lesson_file;

    JTable table;

    int cur_ix = 0;

    char mode;

//     JLabel[] leftA = new JLabel[5];
//     JTextField[] leftBA = new JTextField[5];

    String pupil_name_s;

    static int F_W = 0;
    static int F_T = 1;
    static int F_C = 2;
    boolean filter[] = new boolean[]{true, true, false};

    int CO_SEL = 0;
    int CO_SENT = 1;
    int CO_DUR = 2;
    int CO_WORDID = 3;
    int CO_CORR = 4;
    int CO_RM = 5;
    int CO_RO = 6;
    int CO_FO = 7;


    class Result_TableModel extends AbstractTableModel {
        String[][] data = new String[0][0];

        String[] hdn_c = new String[]{T.t("Selection"),
                T.t("Sentence"),
                T.t("Time"),
                T.t("Type")
        };

        String[] hdn_t = new String[]{T.t("Selection"),
                T.t("Written Sentence"),
                T.t("Time"),
                T.t("Type"),
                T.t("Correct Sentence"),
                T.t("NCS"),
                T.t("NCW"),
                T.t("NWW")
        };

        String[] getHDN() {
            if (mode == 'c')
                return hdn_c;
            return hdn_t;
        }

        Result_TableModel() {
            data = new String[10][8];
        }

        public int getColumnCount() {
            return getHDN().length;
        }

        public int getRowCount() {
            return data.length;
        }

        public Class getColumnClass(int c) {
            return String.class;
        }

        public String getColumnName(int c) {
            return getHDN()[c];
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public Object getValueAt(int row, int col) {
            return data[row][col] == null ? "" : data[row][col];
        }

        public void setValueAt(Object val, int row, int col) {
            data[row][col] = (String) val;
            fireTableCellUpdated(row, col);
        }

        void setData(String[][] data) {
            this.data = data;
            fireTableDataChanged();
        }
    }

    Result_TableModel tmod;

    String[] getWith() {
        if (mode == 't')
            return new String[]{"-test.", "-pre1.", "-post1.", "-pre2.", "-post2."};
        return new String[]{"-create."};
    }

    public void valueChanged(ListSelectionEvent e) {
        JList l = (JList) e.getSource();
    }

    public ResultDialogTableDetail(java.awt.Frame owner,
                                   String pupil_name_s,
                                   String lesson_file,
                                   int cur_ix,
                                   char mode,
                                   Lesson.RegisterProxy register) {
        super(owner, T.t("Omega - Results Detail"), true);
        this.owner = owner;
        this.pupil_name_s = pupil_name_s;
        this.lesson_file = lesson_file;
        this.cur_ix = cur_ix;
        this.mode = mode;
        this.register = register;
        getContentPane().setLayout(new BorderLayout());
        populate();
        pack();
//	setSize(1000, 500);
//  	    // make it bigger to accomodate scrollbar
        Dimension d = getSize();
        Dimension d2 = new Dimension(d.width + 65, d.height);
        setSize(d2);
        setTableMode(cur_ix);
        _f(F_W, true);
    }

    private boolean _f(int ix, boolean b) {
        if (filter[ix] == b)
            return false;
        filter[ix] = b;
        return true;
    }

    public void stateChanged(ChangeEvent ce) {
        Object o = ce.getSource();
        if (o instanceof JCheckBox) {
            String ac = ((JCheckBox) o).getActionCommand();
            boolean b = ((JCheckBox) o).isSelected();
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "AC " + ac);
            boolean ns = false;
            if (ac.equals("word"))
                ns = _f(F_W, b);
            if (ns) {
                upd_filter();
            }
            return;
        }
    }

    void widthT() {
        TableColumn column = table.getColumnModel().getColumn(1);
        column.setPreferredWidth(300);
        column = table.getColumnModel().getColumn(4);
        column.setPreferredWidth(300);
        column = table.getColumnModel().getColumn(5);
        column.setPreferredWidth(5);
        tmod.fireTableStructureChanged();
    }

    void widthC() {
        TableColumn column = table.getColumnModel().getColumn(2);
        column.setPreferredWidth(5);
        tmod.fireTableStructureChanged();
    }

    private void setTableMode(int ix) {
        if (mode == 't') {
            _f(F_T, true);
            _f(F_C, false);
            mode = 't';
            TableColumn column = table.getColumnModel().getColumn(1);
            tmod.fireTableStructureChanged();
            cur_ix = ix;
            upd_filter();
            upd();
            return;
        } else if (mode == 'c') {
            _f(F_T, false);
            _f(F_C, true);
            mode = 'c';
            TableColumn column = table.getColumnModel().getColumn(1);
            tmod.fireTableStructureChanged();
            cur_ix = ix;
            upd_filter();
            upd();
            return;
        }
    }

    public void actionPerformed(ActionEvent ae) {

        String cmd = ae.getActionCommand();
        if ("export".equals(cmd)) {
            export();
        }
        if ("close".equals(cmd)) {
            setVisible(false);
        }
    }

    JButton crBu(String txt, String cmd) {
        JButton b = new JButton(txt);
        b.setActionCommand(cmd);
        b.addActionListener(this);
        return b;
    }

    JCheckBox crCb(String txt, String cmd) {
        JCheckBox cb = new JCheckBox(txt);
        cb.addChangeListener(this);
        cb.setActionCommand(cmd);
        return cb;
    }

    JToggleButton crTb(String txt, String cmd) {
        JToggleButton tb = new JToggleButton(txt);
        tb.addChangeListener(this);
        tb.setActionCommand(cmd);
        return tb;
    }

    // Lesson: [..............]
    // prev   next   list   _
    class Navigator extends JPanel {
        Navigator() {
            setLayout(new BorderLayout());

            FormPanel pan = new FormPanel();
            add(pan);

            int X = 1, Y = 0;
            pan.add(new JLabel(T.t("Pupil:")), pupil_name = new JTextField(15), Y, X);
            pupil_name.setFont(new Font("dialog", Font.PLAIN, 16));
            pupil_name.setEditable(false);
            pupil_name.setText(pupil_name_s);
            X++;
            pan.add(new JLabel(T.t("Export:")), crBu(T.t("As file") + "...", "export"), Y, X);

            X = 1;
            Y++;
            JTextField ct_tf;
            pan.add(new JLabel(T.t("Type:")), ct_tf = new JTextField(mode == 't' ? "test" : "create"), Y, X);
            ct_tf.setEditable(false);

            X = 1;
            Y++;
            pan.add(new JLabel(T.t("Lesson:")), lesson_name = new JTextField(23), Y, X);
            pan.add(new JLabel(""), stat = new JTextField("0/0"), Y, X);
            lesson_name.setFont(new Font("dialog", Font.PLAIN, 12));
            lesson_name.setEditable(false);

        }

        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }
    }

    private String Tt(String s1, String s2, String s3, String s4) {
        return T.t(s1) + s2 + T.t(s3) + s4;
    }

    private String Tt(String s1, String s2) {
        return Tt(s1, " = ", s2, "    ");
    }

    class Control extends JPanel {
        Control() {
            GBC_Factory gbcf = new GBC_Factory();
            setLayout(new GridBagLayout());

            Font mf = new Font("sans", Font.PLAIN, 9);
            JPanel pan = new JPanel();
            pan.setLayout(new GridLayout(0, 3));
            JLabel jl;
            pan.add(jl = new JLabel(Tt("NCS", "No. Correct Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("NCW", "No. Correct Word")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("NWW", "No. Wrong Word")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("TS", "Time Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("TW", "Time Word")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("CS", "Correct Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("CW", "Correct Word")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("WW", "Wrong Word")));
            jl.setFont(mf);
            add(pan, gbcf.createL(0, 0, 1));
            add(crBu(T.t("Close"), "close"), gbcf.createL(1, 0, 1));
        }

        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }
    }

    class MyListSelectionModel extends DefaultListSelectionModel implements ListSelectionListener {
        MyListSelectionModel() {
            addListSelectionListener(this);
        }

        public void valueChanged(ListSelectionEvent ev) {
            try {
//	    if ( ev.getValueIsAdjusting() == false ) {
                MyListSelectionModel lselmod_ = (MyListSelectionModel) ev.getSource();
                int ix = lselmod_.getMinSelectionIndex();
                TableModel tmod = (TableModel) table.getModel();
                int imax = tmod.getColumnCount();
                for (int i = 0; i < imax; i++) {
                    String s = (String) tmod.getValueAt(ix, i);
// 		    leftBA[i].setText(s);
                }
            } catch (Exception ex) {
            }
        }
    }

    ;
    MyListSelectionModel lselmod = new MyListSelectionModel();

    void populate() {

        JPanel pan = new JPanel();
        pan.setLayout(new FlowLayout());

        table = new JTable(tmod = new Result_TableModel());
//	table.setAutoResizeMode(table.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionModel(lselmod);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//	table.setRowSelectionInterval(0, 0);
//	table.setPreferredSize(new Dimension(730, 300));

        results_sp = new JScrollPane(table);

        JPanel result_pan = new JPanel();
        result_pan.setLayout(new BorderLayout());
        result_pan.add(results_sp, BorderLayout.CENTER);

        Control control = new Control();

        JPanel stat = new JPanel();
        populateStat(stat);
        result_pan.add(stat, BorderLayout.SOUTH);

        getContentPane().add(new Navigator(), BorderLayout.NORTH);
        getContentPane().add(result_pan, BorderLayout.CENTER);
        getContentPane().add(control, BorderLayout.SOUTH);
    }

    JLabel stat_l1;
    JTextField stat_tf1;
    JLabel stat_l2;
    JTextField stat_tf2;

    private void populateStat(JPanel stat) {
//	stat.setLayout(new
        stat_l1 = new JLabel(T.t("Total:"));
        stat_tf1 = new JTextField("", 40);
        stat_tf1.setEditable(false);
        stat.add(stat_l1);
        stat.add(stat_tf1);

        stat.add(stat_l2 = new JLabel(T.t("Average time (s):")));
        stat_tf2 = new JTextField("", 16);
        stat_tf2.setEditable(false);
        stat.add(stat_tf2);
        upd_stat();
    }

    String[] getStringArray(String[][] saa, int ix) {
        String[] sa = new String[saa.length];
        for (int i = 0; i < sa.length; i++)
            sa[i] = saa[i][ix];
        return sa;
    }

    double tD(String s) {
        try {
            double a = Double.parseDouble(s.replace(',', '.'));
            return a;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private void upd_stat() {
        stat_tf1.setText("...");
        try {
            String[][] data = tmod.data;
            if (data == null) {
                stat_tf1.setText("");
                return;
            }
            String[] stat_data_rm = getStringArray(data, 5);
            String[] stat_data_ro = getStringArray(data, 6);
            String[] stat_data_fo = getStringArray(data, 7);

            StatValue1 correct_sent = new StatValue1();
            StatValue1 correct_word = new StatValue1();
            StatValue1 wrong_word = new StatValue1();

            for (int i = 0; i < stat_data_rm.length; i++) {
                if (stat_data_rm[i] == null || stat_data_rm[i].length() == 0)
                    continue;
                correct_sent.add(stat_data_rm[i]);
                correct_word.add(stat_data_ro[i]);
                wrong_word.add(stat_data_fo[i]);
            }

            double sum_word = 0.0;
            double sum_sent = 0.0;
            int sum_word_n = 0;
            int sum_sent_n = 0;

            String[] time_data = getStringArray(data, 2);

            for (int i = 0; i < time_data.length; i++) {
                if (time_data[i] == null)
                    continue;
                String val2 = (String) tmod.getValueAt(i, 1);
                if (val2.length() == 0) {
                    sum_word += tD(time_data[i]);
                    sum_word_n++;
                } else {
                    sum_sent += tD(time_data[i]);
                    sum_sent_n++;
                }
            }

            int N = (int) (correct_word.getTotal() + wrong_word.getTotal());
            stat_tf1.setText(correct_sent.getTotalInt(T.t("CS") + " = ") +
                    correct_sent.getAvg(" (", "%") +
//			     correct_sent.getAvg_100(" (", "%") +
                    correct_word.getTotalInt(");    " + T.t("CW") + " = ") +
                    correct_word.getAvgTot(" (", "%", N) +
                    wrong_word.getTotalInt(")   " + T.t("WW") + " = ") +
                    wrong_word.getAvgTot(" (", "%", N) +
                    ")");
            DecimalFormat df = new DecimalFormat("##0.0#");
            String s_s = "?";
            String s_w = "?";
            if (sum_sent_n != 0)
                s_s = df.format(sum_sent / sum_sent_n);
            if (sum_word_n != 0)
                s_w = df.format(sum_word / sum_word_n);
            stat_tf2.setText(T.t("TS") + " = " + s_s + "   " + T.t("TW") + " = " + s_w);
        } catch (Exception ex) {
            stat_tf1.setText("?");
            ex.printStackTrace();
        }
    }

    private void upd_filter() {
        stat_tf1.setText("?");
        setTableData();
        String[] sa = tmod.getHDN();
        for (int i = 0; i < 5; i++) {
            String s = "";
            if (sa.length > i)
                s = sa[i];
// 	    leftA[i].setText(s);
        }
        upd_stat();
    }

    String form(int a) {
        double d = a / 1000.0;
        DecimalFormat df = new DecimalFormat("##0.0");
        String s = df.format(d);
        return s;
    }

    String[] parseCCS(String ccw) {  // s +1; w +2 -3
        try {
            String[] sa = SundryUtils.split(ccw, " ;+-");
            String ss = sa[4];
            return sa;
        } catch (Exception ex) {
            return new String[]{"", "", "", "", ""};
        }
    }

    String last = "";

    void setTableData() {
        try {
            String[] sa = register.getAllTestsAsName(getWith());
            if (false && sa.length == 0) {
                String[][] data = new String[1][8];
                data[0][0] = "---";
                tmod.setData(data);
                table.doLayout();
                return;
            }

// 	    if ( cur_ix >= sa.length )
// 		cur_ix = sa.length-1;
// 	    if ( cur_ix < 0 )
// 		cur_ix = 0;
            String pup = register.pupil.getName();
            String res_name = register.rl.getFullFName(pup, sa[cur_ix]);
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "RESULT " + res_name);

// 	    if ( res_name.equals(last) )
// 		return;
// 	    last = res_name;

            ResultTest rt = new ResultTest(pup, "", "", res_name);

            int pa_n = filter[F_T] ? rt.getEntrySize("test") : 0;
            pa_n += filter[F_W] ? rt.getEntrySize("select") : 0;
            pa_n += filter[F_C] ? rt.getEntrySize("create") : 0;

            int n = rt.getEntrySize();
            int j = 0;

            int nn = 0;

            if (n == 0) {
                String[][] data = new String[1][8];
                data[0][0] = "---";
                tmod.setData(data);
                table.doLayout();
                return;
            }

            for (int i = 0; i < n; i++) {
                Entry ent = rt.getEntry(i);
                if (ent.type.equals("test") && filter[F_T]) {
                    nn++;
                }
                if (ent.type.equals("select") && filter[F_W]) { // word
                    SelectEntry sel = (SelectEntry) ent;
                    if (filter[F_T] && sel.extra.startsWith("test") ||
                            filter[F_C] && sel.extra.startsWith("create")) {
                        nn++;
                    }
                }
                if (ent.type.equals("create") && filter[F_C]) {
                    nn++;
                }
            }

            String[][] data = new String[nn][8];

            int nn_ix = 0;

            for (int i = 0; i < n; i++) {
                Entry ent = rt.getEntry(i);
                if (ent.type.equals("test") && filter[F_T]) {
                    TestEntry te = (TestEntry) ent;

                    String ccw = te.cnt_correct_words;
                    String[] ccw_sa = parseCCS(ccw);

                    data[nn_ix][CO_CORR] = te.sentence;
                    data[nn_ix][CO_SENT] = te.answer;
                    data[nn_ix][CO_DUR] = "" + form(te.duration);
                    data[nn_ix][CO_WORDID] = "" + te.l_id_list;
                    data[nn_ix][CO_RM] = "" + ccw_sa[1];
                    data[nn_ix][CO_RO] = "" + ccw_sa[3];
                    data[nn_ix][CO_FO] = "" + ccw_sa[4];
                    nn_ix++;
//log		    OmegaContext.sout_log.getLogger().info(":--: " + "table + " + te);
                }
                if (ent.type.equals("select") && filter[F_W]) { // word
                    SelectEntry sel = (SelectEntry) ent;
                    if (filter[F_T] && sel.extra.startsWith("test") ||
                            filter[F_C] && sel.extra.startsWith("create")) {
                        if (filter[F_T] && sel.extra.equals("test:build:OK")) {
                            data[nn_ix][CO_RO] = "1";
                            data[nn_ix][CO_FO] = "0";
                        }
                        if (filter[F_T] && sel.extra.equals("test:build:wrong")) {
                            data[nn_ix][CO_RO] = "0";
                            data[nn_ix][CO_FO] = "1";
                        }
                        data[nn_ix][CO_SEL] = sel.word;
                        data[nn_ix][CO_DUR] = "" + form(sel.when);
                        data[nn_ix][CO_WORDID] = "" + sel.l_id;
//log			OmegaContext.sout_log.getLogger().info(":--: " + "table + " + sel);
                        nn_ix++;
                    }
                }
                if (ent.type.equals("create") && filter[F_C]) {
                    CreateEntry ce = (CreateEntry) ent;
//log		    OmegaContext.sout_log.getLogger().info(":--: " + "table + " + ce);
                    data[nn_ix][CO_SENT] = ce.sentence;
                    data[nn_ix][CO_DUR] = "" + form(ce.duration);
                    data[nn_ix][CO_WORDID] = "" + ce.l_id_list;
                    nn_ix++;
                }
            }
            tmod.setData(data);
            if (mode == 't') {
                TableColumn column = table.getColumnModel().getColumn(1);
                column.setPreferredWidth(300);
                column = table.getColumnModel().getColumn(4);
                column.setPreferredWidth(300);
                column = table.getColumnModel().getColumn(5);
                column.setPreferredWidth(33);
                column = table.getColumnModel().getColumn(6);
                column.setPreferredWidth(33);
                column = table.getColumnModel().getColumn(7);
                column.setPreferredWidth(33);
            } else if (mode == 'c') {
                TableColumn column = table.getColumnModel().getColumn(1);
                column.setPreferredWidth(300);
            }
            upd_stat();
//	    table.doLayout();
        } catch (NullPointerException ex) {
        }
    }

    void upd() {
        stat_tf1.setText("?");
        String[] sa = register.getAllTestsAsName(getWith());
        if (cur_ix < 0)
            cur_ix = 0;
        if (cur_ix > sa.length - 1)
            cur_ix = sa.length - 1;
        setLessonName(sa[cur_ix]);
        setTableData();
        stat.setText("" + (cur_ix + 1) + " / " + sa.length);
        upd_stat();
//	pack();

//  	    // make it bigger to accomodate scrollbar
//  	Dimension d = getSize();
//  	Dimension d2 = new Dimension(d.width+15, d.height);
//  	setSize(d2);
    }

    void export() {
        ChooseExportFile choose_af = new ChooseExportFile();

        String fn = null;
        int rv = choose_af.showDialog(this, T.t("Export"));
        if (rv == JFileChooser.APPROVE_OPTION) {
            File file = choose_af.getSelectedFile();
            fn = file.getAbsolutePath();
            if (!fn.endsWith("." + ChooseExportFile.ext))
                fn = fn + "." + ChooseExportFile.ext;
        } else
            return;

        try {
            PrintWriter pw = SundryUtils.createPrintWriter(fn);

            pw.println("Pupil:," + register.pupil.getName() + ',' +
                    "Lesson Name:," + lesson_name.getText()
            );
            for (int j = 0; j < tmod.getColumnCount(); j++) {
                String col_name = tmod.getColumnName(j);
                if (col_name.equals(T.t("Time (s)"))) {
                    pw.print((j == 0 ? "" : ",") + col_name + " Sentence");
                    pw.print((j == 0 ? "" : ",") + col_name + " Word");
                } else
                    pw.print((j == 0 ? "" : ",") + col_name);
            }
            pw.println("");
            for (int i = 0; i < tmod.getRowCount(); i++) {
                for (int j = 0; j < tmod.getColumnCount(); j++) {
                    String col_name = tmod.getColumnName(j);
                    String val = (String) tmod.getValueAt(i, j);
                    if (col_name.equals(T.t("Time (s)"))) {
                        String val2 = (String) tmod.getValueAt(i, 1);
                        if (val2.length() == 0)
                            pw.print(",");
                        pw.print((j == 0 ? "" : ",") + val.replace(',', '.'));
                        if (val2.length() != 0)
                            pw.print(",");
                    } else
                        pw.print((j == 0 ? "" : ",") + val.replace(',', '.'));
                }
                pw.println("");
            }
            pw.close();
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Can't export " + fn);
        }
    }

//     public void dep_set(Lesson.RegisterProxy register) {
// 	this.register = register;
// 	pupil_name.setText(register.pupil.getName());
// 	cur_ix = 0;
// 	//	test_tb.doClick();
// 	upd();
//     }

    public void setLessonName(String ln) {
        lesson_name.setText(ln);
        load(ln);
    }

    void load(String ln) {
    }
}
