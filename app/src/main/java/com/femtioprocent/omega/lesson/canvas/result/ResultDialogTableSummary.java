package com.femtioprocent.omega.lesson.canvas.result;

// has UTF-8


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
import java.util.ArrayList;


public class ResultDialogTableSummary extends JDialog
        implements ListSelectionListener,
        ActionListener,
        ChangeListener {
    java.awt.Frame owner;

    JScrollPane results_sp;

    JTextField lesson_name = new JTextField("___");
    JTextField pupil_name;
    JTextField stat;
    Lesson.RegisterProxy register;

    JLabel pupilL;
    JTextField pupilTF;

    JButton details;

    JTable table;

    JToggleButton test_tb, create_tb;

    int cur_ix = 0;

    int CO_dat = 0;
    int CO_l = 1;
    int CO_t = 2;
    int CO_rm = 3;
    int CO_am = 3;
    int CO_fm = 4;
    int CO_prm = 5;
    int CO_ro = 6;
    int CO_ao = 6;
    int CO_fo = 7;
    int CO_pro = 8;
    int CO_trm = 9;
    int CO_tfm = 10;
    int CO_sl = 11;
    int CO_fn = 12;
    int CO_MAX = 13;

    int[] map_t = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    int[] map_c = {0, 1, 7, 2, 7, 7, 3, 7, 7, 4, 7, 5, 7};

    int map(int v) {
        return tmod.mode == 'c' ? map_c[v] : map_t[v];
    }

    class Result_TableModel extends AbstractTableModel {
        char mode = 't';

        String[][] data = new String[0][0];

        String[] hdn_t = new String[]{T.t("Dat"),
                T.t("L#"),
                T.t("Lt"),
                T.t("CS"),
                T.t("WS"),
                T.t("%CS"),
                T.t("CW"),
                T.t("WW"),
                T.t("%CW"),
                T.t("TCS"),
                T.t("TWS"),
                T.t("SL")
        };
        String[] hdn_c = new String[]{T.t("Dat"),
                T.t("L#"),
                //
                T.t("NS"),
                //fm
                //prm
                T.t("NW"),
                //fo
                //pfo
                T.t("TS"),
                //
                T.t("SL")
        };

        String[] getHDN() {
            if (mode == 'c')
                return hdn_c;
            return hdn_t;
        }

        Result_TableModel() {
            data = new String[100][13];
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
            fireTableStructureChanged();
            fireTableDataChanged();
        }
    }

    Result_TableModel tmod;

    String[] getWith() {
        if (tmod.mode == 't')
            return new String[]{"-test.", "-pre1.", "-post1.", "-pre2.", "-post2."};
        return new String[]{"-create."};
    }

    public void valueChanged(ListSelectionEvent e) {
        JList l = (JList) e.getSource();
    }

    public ResultDialogTableSummary(java.awt.Frame owner) {
        super(owner, T.t("Omega - Results Summary"), true);
        this.owner = owner;
        getContentPane().setLayout(new BorderLayout());
        populate();
        pack();
        setSize(700, 500);
        // make it bigger to accomodate scrollbar
//   	Dimension d = getSize();
//   	Dimension d2 = new Dimension(d.width+15, d.height);
//   	setSize(d2);
        upd_filter();
    }

    public void stateChanged(ChangeEvent ce) {
        Object o = ce.getSource();
        if (o instanceof JCheckBox) {
            String ac = ((JCheckBox) o).getActionCommand();
            boolean b = ((JCheckBox) o).isSelected();
            return;
        }
    }

//      void widthT() {
//  	TableColumn column = table.getColumnModel().getColumn(1);
//  	column.setPreferredWidth(300);
//  	column = table.getColumnModel().getColumn(4);
//  	column.setPreferredWidth(300);
//  	column = table.getColumnModel().getColumn(5);
//  	column.setPreferredWidth(5);
//  	tmod.fireTableStructureChanged();
//     }

//     void widthC() {
// 	TableColumn column = table.getColumnModel().getColumn(2);
// 	column.setPreferredWidth(5);
// 	tmod.fireTableStructureChanged();
//     }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == test_tb) {
            tmod.mode = 't';
            tmod.fireTableStructureChanged();
            cur_ix = 0;
            upd_filter();
            upd();
            return;
        }
        if (ae.getSource() == create_tb) {
            tmod.mode = 'c';
            TableColumn column = table.getColumnModel().getColumn(1);
            tmod.fireTableStructureChanged();
            cur_ix = 0;
            upd_filter();
            upd();
            return;
        }
        if (ae.getSource() == details) {
            ResultDialogTableDetail rdt = new ResultDialogTableDetail(owner,
                    pupil_name.getText(),
                    lesson_name.getText(),
                    cur_ix,
                    tmod.mode,
                    register);
            OmegaContext.HELP_STACK.push("result_detail");
            rdt.setVisible(true);
            OmegaContext.HELP_STACK.pop("result_detail");
        }

        String cmd = ae.getActionCommand();
        if ("export".equals(cmd)) {
            export();
        }

        if ("close".equals(cmd)) {
            setVisible(false);
        }
    }


    String[] getAllTestAsName() {
        String[] sa = register.getAllTestsAsName(getWith());
        return sa;
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
            X++;
            pan.add(new JLabel(T.t("Export")), crBu(T.t("As file") + "...", "export"), Y, X);


            X = 1;
            Y++;
            pan.add(new JLabel(T.t("Select Type:")), new JLabel(""), Y, X);
            pan.add(test_tb = new JRadioButton(T.t("test")),
                    create_tb = new JRadioButton(T.t("create")),
                    Y, X);
            ButtonGroup bg = new ButtonGroup();
            bg.add(test_tb);
            bg.add(create_tb);
            test_tb.addActionListener(ResultDialogTableSummary.this);
            create_tb.addActionListener(ResultDialogTableSummary.this);
// 	    test_tb.addChangeListener(ResultDialogTableSummary.this);
// 	    create_tb.addChangeListener(ResultDialogTableSummary.this);
            test_tb.setSelected(true);

            X++;
            pan.add(new JLabel(""), details = new JButton(T.t("Details")), Y, ++X);
            details.setActionCommand("details");
            details.addActionListener(ResultDialogTableSummary.this);
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
            pan.setLayout(new GridLayout(0, 4));
            JLabel jl;
            pan.add(jl = new JLabel(Tt("Dat", "Date")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("L#", "Lesson id")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("Lt", "Lesson type")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("CS", "Correct Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("WS", "Wrong Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("NS", "No. of Sentences")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("%CS", "Correct Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("CW", "Correct Word")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("WW", "Wrong Word")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("NW", "No. of Words")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("%CW", "Correct Word")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("TCS", "Time Correct Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("TWS", "Time Wrong Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("TS", "Time Sentence")));
            jl.setFont(mf);
            pan.add(jl = new JLabel(Tt("SL", "Session Length")));
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
                    cur_ix = ix;
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
//	table.setPreferredSize(new Dimension(600, 300));

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

    private void populateStat(JPanel stat) {
//	stat.setLayout(new
        stat_l1 = new JLabel(T.t("Average") + ':');
        stat_tf1 = new JTextField("", 40);
        stat_tf1.setEditable(false);
        stat.add(stat_l1);
        stat.add(stat_tf1);
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

    void calcData(StatValue sv, String[] sa) {
        for (int i = 0; i < sa.length; i++) {
            if (sa[i] == null || sa[i].length() == 0)
                continue;
            sv.add(tD(sa[i]));
        }
    }

    private void upd_stat() {
        try {
            String[][] data = tmod.data;
            if (data == null) {
                stat_tf1.setText("");
                return;
            }

            StatValue st_trm = new StatValue();

            String[] data_trm = getStringArray(data, map(CO_trm));

            calcData(st_trm, data_trm);

        } catch (Exception ex) {
        }
    }

    private void upd_filter() {
        setTableData();
        upd_stat();
    }

    String form(int a) {
        double d = a / 1000.0;
        DecimalFormat df = new DecimalFormat("##0.0");
        String s = df.format(d);
        return s;
    }

    String asHMS(double a) {
        int ms = (int) (a % 1000);
        a /= 1000;
        int s = (int) (a % 60);
        a /= 60;
        int m = (int) (a);
        if (m > 60) {
            m = (int) (a % 60);
            a /= 60;
            int h = (int) (a);
            return "" + h + "h " + m + "m " + s + "s";
        }
        return "" + m + "m " + s + "s";
    }

    int asNoHMS(String s) {  // 1h 12m 7s
        s = s.replace('m', ' ').replace('s', ' ').replace('h', ' ');
        String[] sa = SundryUtils.split(s, " ");
        if (sa.length == 2)
            return Integer.parseInt(sa[1]) + Integer.parseInt(sa[0]) * 60;
        return Integer.parseInt(sa[2]) + Integer.parseInt(sa[1]) * 60 + Integer.parseInt(sa[0]) * 60 * 60;
    }

    String last = "";

    void setTableData() {
        try {
            if (register == null)
                return;
            String[] sa = register.getAllTestsAsName(getWith());

            String[][] data = new String[sa.length][tmod.mode == 't' ? CO_MAX : 8];

            ArrayList dataLi = new ArrayList();

            StatValue statval_tfm = new StatValue();
            StatValue statval_trm = new StatValue();
            StatValue statval_pro = new StatValue();
            StatValue statval_prm = new StatValue();
            StatValue stat_sl = new StatValue();

            NEXT_LESSON:
            for (int i = 0; i < sa.length; i++) {
                String testName = sa[i];

                if (cur_ix >= sa.length)
                    cur_ix = sa.length - 1;
                if (cur_ix < 0)
                    cur_ix = 0;

                String pup = register.pupil.getName();
                String res_name = register.rl.getFullFName(pup, sa[i]);

                ResultTest rt = new ResultTest(pup, "", "", res_name);

                int n = rt.getEntrySize();
                int j = 0;

                int nn = 0;

                if (n == 0) {
                    data[i][0] = "---";
                    dataLi.add(data[i]);
                    continue;
                }
                data[i][map(CO_fn)] = testName;
                try {
                    String[] tname = SundryUtils.split(testName, "-"); // pupil-date_clock-TID-type.omegaresult
                    data[i][map(CO_dat)] = tname[1].substring(0, 8);
                    data[i][map(CO_l)] = tname[2];
                    data[i][map(CO_t)] = tname[3];
                } catch (Exception ex) {
                    data[i][0] = testName;
                    data[i][map(CO_t)] = "";
                }

                data[i][map(CO_sl)] = "" + asHMS(1.0 * rt.session_length);

                if (tmod.mode == 't') {
                    if (data[i][map(CO_t)].equals("test") ||
                            data[i][map(CO_t)].equals("pre1") ||
                            data[i][map(CO_t)].equals("pre2") ||
                            data[i][map(CO_t)].equals("post1") ||
                            data[i][map(CO_t)].equals("post2"))
                        dataLi.add(data[i]);
                    else
                        continue NEXT_LESSON;
                }
                if (tmod.mode == 'c') {
                    if (data[i][map(CO_t)].equals("create"))
                        dataLi.add(data[i]);
                    else
                        continue NEXT_LESSON;
                }
                int nn_ix = 0;

                String[] stat_data = getStringArray(data, 5);

                StatValue correct_sent = new StatValue();
                StatValue correct_word = new StatValue();
                StatValue wrong_word = new StatValue();
                StatValue wrong_sent = new StatValue();
                StatValue wrong_sent_time = new StatValue();
                StatValue correct_sent_time = new StatValue();
                int w_cnt = 0;

                int i2_l = rt.howManyTestEntries();
                for (int i2 = 0; i2 < i2_l; i2++) {
                    Entry ent = rt.getEntry(i2);
                    //		    OmegaContext.sout_log.getLogger().info("ERR: " + "ent " + ent);

		    /*
                    if ( "select".equals(ent.type) ) {
			if ( ent.extra.endsWith(":wrong") ) {
			    wrong_word_time.add(ent.when);
			}
		    }
		    */

                    if (tmod.mode == 't' && "test".equals(ent.type)) {
                        try {
                            TestEntry tent = (TestEntry) ent;
                            String ccw = tent.cnt_correct_words; // s 1;w +3 -0

                            if (ccw == null)
                                continue;
                            String s = ccw.replace(';', ' ');
                            String sat[] = SundryUtils.split(s, " ");
                            if (sat.length > 3) {
                                if (sat[1].equals("1")) {
                                    correct_sent.add("1");
                                    correct_sent_time.add("" + tent.duration);
                                } else {
                                    wrong_sent.add("1");
                                    wrong_sent_time.add("" + tent.duration);
                                }
                                correct_word.add(sat[3]);
                                wrong_word.add(sat[4]);
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    }
                    if (tmod.mode == 'c') {
                        if ("create".equals(ent.type)) {
                            try {
                                CreateEntry cent = (CreateEntry) ent;
                                correct_sent_time.add("" + cent.duration);
                                correct_sent.add("1");
                            } catch (ArrayIndexOutOfBoundsException ex) {
                            }
                        }
                        if ("select".equals(ent.type)) {
                            try {
                                SelectEntry sent = (SelectEntry) ent;
                                if (sent.extra.startsWith("create"))
                                    w_cnt++;
                            } catch (ArrayIndexOutOfBoundsException ex) {
                            }
                        }
                    }
                }

                if (tmod.mode == 'c')
                    correct_word.add("" + w_cnt);

                int Ns = (int) (correct_sent.getTotal() + wrong_sent.getTotal());
                int Nw = (int) (correct_word.getTotal() + wrong_word.getTotal());

                if (tmod.mode == 't') {
                    data[i][map(CO_rm)] = correct_sent.getTotalInt("");
                    data[i][map(CO_fm)] = wrong_sent.getTotalInt("");
                    data[i][map(CO_prm)] = correct_sent.getAvgTot("", "", Ns);

                    data[i][map(CO_ro)] = correct_word.getTotalInt("");
                    data[i][map(CO_fo)] = wrong_word.getTotalInt("");
                    data[i][map(CO_pro)] = correct_word.getAvgTot("", "", Nw);

                    data[i][map(CO_tfm)] = wrong_sent_time.getAvg_1000("", "");
                    data[i][map(CO_trm)] = correct_sent_time.getAvg_1000("", "");
                } else {
                    data[i][map(CO_am)] = correct_sent.getTotalInt("");
                    data[i][map(CO_ao)] = correct_word.getTotalInt("");
                    data[i][map(CO_trm)] = correct_sent_time.getAvg_1000("", "");
                }

                if (wrong_sent_time.has())
                    statval_tfm.add(wrong_sent_time.getAvg1());
                if (correct_sent_time.has())
                    statval_trm.add(correct_sent_time.getAvg1());

                if (Nw > 0)
                    statval_pro.add(correct_word.getAvg1(Nw));
                if (Ns > 0)
                    statval_prm.add(correct_sent.getAvg1(Ns));

                if (rt.session_length > 0)
                    stat_sl.add(rt.session_length);
            }

            if (tmod.mode == 't')
                stat_tf1.setText(T.t("%CS") + " " + statval_prm.getAvg("", "") + "   " +
                        T.t("%CW") + " " + statval_pro.getAvg("", "") + "   " +
                        T.t("TCS") + " " + statval_trm.getAvg_1000("", "") + "   " +
                        T.t("TWS") + " " + statval_tfm.getAvg_1000("", "") + "   " +
                        T.t("SL") + " " + asHMS(stat_sl.getAvg1())
                );
            else
                stat_tf1.setText(T.t("TS") + " " + statval_trm.getAvg_1000("", "") + "   " +
                        T.t("SL") + " " + stat_sl.getAvg_1000("", "")
                );


            String[][] data2 = (String[][]) dataLi.toArray(new String[0][]);

            tmod.setData(data2);

            table.doLayout();
            TableColumn column = table.getColumnModel().getColumn(map(CO_dat));
            column.setPreferredWidth(100);
            //	    upd_stat();
            //	    table.doLayout();
        } catch (NullPointerException ex) {
            tmod.setData(new String[0][1]);
            OmegaContext.sout_log.getLogger().info("ERR: " + "npe " + ex);
            ex.printStackTrace();
        }
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
                pw.print((j == 0 ? "" : ",") + col_name);
            }
            pw.println("");
            for (int i = 0; i < tmod.getRowCount(); i++) {
                for (int j = 0; j < tmod.getColumnCount(); j++) {
                    String col_name = tmod.getColumnName(j);
                    String val = (String) tmod.getValueAt(i, j);
                    if (j == map(CO_sl)) {
                        String val2 = (String) tmod.getValueAt(i, 1);
                        if (val2.length() == 0)
                            pw.print(",");
                        else
                            pw.print((j == 0 ? "" : ",") + asNoHMS(val));
                    } else
                        pw.print((j == 0 ? "" : ",") + val.replace(',', '.'));
                }
                pw.println("");
            }
            pw.close();
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Can't export " + fn + ' ' + ex);
            ex.printStackTrace();
        }
    }

    void upd() {
        stat_tf1.setText("?");
        String[] sa = register.getAllTestsAsName(getWith());
        if (cur_ix < 0)
            cur_ix = 0;
        if (cur_ix > sa.length - 1)
            cur_ix = sa.length - 1;
        if (cur_ix > 0)
            setLessonName(sa[cur_ix]);
        setTableData();
        upd_stat();
    }

    public void set(Lesson.RegisterProxy register) {
        this.register = register;
        pupil_name.setText(register.pupil.getName());
        cur_ix = 0;
        test_tb.doClick();
        upd();
    }

    public void setLessonName(String ln) {
        lesson_name.setText(ln);
        load(ln);
    }

    void load(String ln) {
    }
}
