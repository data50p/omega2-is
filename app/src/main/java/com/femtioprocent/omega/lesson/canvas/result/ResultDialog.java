package com.femtioprocent.omega.lesson.canvas.result;

import com.femtioprocent.omega.adm.register.data.*;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.swing.GBC_Factory;
import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResultDialog extends JDialog
        implements ListSelectionListener,
        ActionListener,
        ChangeListener {
    JScrollPane results_sp;
    JList results;
    JTextField lesson_name;
    JTextField pupil_name;
    JTextField stat;
    Lesson.RegisterProxy register;


    JToggleButton test_tb, create_tb;

    int cur_ix = 0;

    static int F_W = 0;
    static int F_T = 1;
    static int F_C = 2;
    boolean filter[] = new boolean[3];

    public void valueChanged(ListSelectionEvent e) {
        JList l = (JList) e.getSource();
    }

    public ResultDialog(java.awt.Frame owner) {
        super(owner, "Omega -Result", true);
        getContentPane().setLayout(new BorderLayout());
        populate();
        pack();
//  	    // make it bigger to accomodate scrollbar
//  	Dimension d = getSize();
//  	Dimension d2 = new Dimension(d.width+15, d.height);
//  	setSize(d2);
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
            if (ac.equals("test"))
                ns = _f(F_T, b);
            if (ac.equals("create"))
                ns = _f(F_C, b);
            if (ns) {
                upd_filter();
            }
            return;
        }
        if (o instanceof JToggleButton) {
            String ac = ((JToggleButton) o).getActionCommand();
            boolean b = ((JToggleButton) o).isSelected();
            if (ac.equals("test")) {
                if (b) {
                    if (create_tb.isSelected())
                        create_tb.setSelected(!b);
                } else {
                    if (create_tb.isSelected())
                        create_tb.setSelected(!b);
                }
                _f(F_T, b);
                upd_filter();
            }
            if (ac.equals("create")) {
                if (b) {
                    if (test_tb.isSelected())
                        test_tb.setSelected(!b);
                } else {
                    if (test_tb.isSelected())
                        test_tb.setSelected(!b);
                }
                _f(F_C, b);
                upd_filter();
            }
        }
    }

    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if ("next".equals(cmd)) {
            cur_ix++;
            upd();
        }
        if ("prev".equals(cmd)) {
            if (cur_ix > 0)
                cur_ix--;
            upd();
        }
        if ("list".equals(cmd)) {
        }
        if ("export".equals(cmd)) {
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
            GBC_Factory gbcf = new GBC_Factory();
            setLayout(new GridBagLayout());

            int Y = 0;
            add(new JLabel("Pupil:"), gbcf.createL(0, Y, 1));
            add(pupil_name = new JTextField(15), gbcf.createL(1, Y, 2));
            pupil_name.setFont(new Font("dialog", Font.PLAIN, 16));
            pupil_name.setEditable(false);

            Y++;
            add(new JLabel("Lesson:"), gbcf.createL(0, Y, 1));
            add(lesson_name = new JTextField(25), gbcf.createL(1, Y, 3));
            add(stat = new JTextField("0 / 0"), gbcf.createL(4, Y, 3));
            lesson_name.setFont(new Font("dialog", Font.PLAIN, 16));
            lesson_name.setEditable(false);

            Y++;
            add(new JLabel("Select file:"), gbcf.createL(0, Y, 1));
            add(crBu("Prev", "prev"), gbcf.createL(1, Y, 1));
            add(crBu("Next", "next"), gbcf.createL(2, Y, 1));
            add(crBu("List", "list"), gbcf.createL(3, Y, 1));
            add(crBu("Export", "export"), gbcf.createL(4, Y, 1));

            JComboBox cb;
            Y++;
            add(new JLabel(T.t("Filter:")), gbcf.createL(0, Y, 1));
            add(test_tb = new JToggleButton(T.t("test")));
            add(create_tb = new JToggleButton(T.t("create")));
            test_tb.addChangeListener(ResultDialog.this);
            create_tb.addChangeListener(ResultDialog.this);
            add(test_tb, gbcf.createL(1, Y, 1));
            add(create_tb, gbcf.createL(2, Y, 1));
// 	    add(cb = new JComboBox(),           gbcf.createL(1, Y, 1));
// 	    cb.addItem("test");
// 	    cb.addItem("create");

            add(crCb(T.t("word"), "word"), gbcf.createL(3, Y, 1));

            Y++;
            add(new JLabel("Select lessons:"), gbcf.createL(0, Y, 1));
            add(crBu("Prev", "lprev"), gbcf.createL(1, Y, 1));
            add(crBu("Next", "lnext"), gbcf.createL(2, Y, 1));
            add(crBu(" ", ""), gbcf.createL(3, Y, 1));
            add(crBu(" ", ""), gbcf.createL(4, Y, 1));
        }

        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }
    }

    class Control extends JPanel {
        Control() {
            GBC_Factory gbcf = new GBC_Factory();
            setLayout(new GridBagLayout());

            add(crBu("Close", "close"), gbcf.createL(0, 0, 1));
        }

        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }
    }

    class ResultCellRenderer extends JLabel implements ListCellRenderer {
        public ResultCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            RegisterPanel rp = (RegisterPanel) value;
            return rp;
        }
    }

    ;
    ResultCellRenderer rcr = new ResultCellRenderer();

    void populate() {

        getContentPane().add(new Navigator(), BorderLayout.NORTH);
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(0, 1));
        top.add(new Navigator());
        getContentPane().add(top, BorderLayout.NORTH);
        //getContentPane().add(new Navigator(), BorderLayout.NORTH);
        getContentPane().add(new Control(), BorderLayout.SOUTH);
        if (results == null) {
            results = new JList();
            results_sp = new JScrollPane(results);
            getContentPane().add(results_sp, BorderLayout.CENTER);
            results.setVisibleRowCount(7);
            results.setCellRenderer(rcr);

            results.addListSelectionListener(this);
        }
    }

    private void upd_filter() {
        setListData();
    }

    void setListData() {
        try {
            String[] sa = register.getAllTestsAsName(null);
            if (cur_ix >= sa.length)
                cur_ix = sa.length - 1;
            String pup = register.pupil.getName();
            String res_name = register.rl.getFullFName(pup, sa[cur_ix]);
//log	OmegaContext.sout_log.getLogger().info(":--: " + "RESULT " + res_name);

            ResultTest rt = new ResultTest(pup, "", "", res_name);

            int pa_n = filter[F_T] ? rt.getEntrySize("test") : 0;
            pa_n += filter[F_W] ? rt.getEntrySize("select") : 0;
            pa_n += filter[F_C] ? rt.getEntrySize("create") : 0;
            RegisterPanel[] pA = new RegisterPanel[pa_n];

            int n = rt.getEntrySize();
            int j = 0;
            if (pA.length == 0) {
                pA = new RegisterPanel[1];
                pA[0] = new RegisterPanel();
            } else {
                for (int i = 0; i < n; i++) {
                    Entry ent = rt.getEntry(i);
                    if (ent.type.equals("test") && filter[F_T]) {
                        TestRegisterPanel rp;
                        rp = new TestRegisterPanel();
                        pA[j++] = rp;
                        rp.set((TestEntry) ent);
                    }
                    if (ent.type.equals("select") && filter[F_W]) { // word
                        SelectEntry sel = (SelectEntry) ent;
                        if (filter[F_T] && sel.extra.startsWith("test") ||
                                filter[F_C] && sel.extra.startsWith("create")) {
                            SelectRegisterPanel rp;
                            rp = new SelectRegisterPanel();
                            pA[j++] = rp;
                            rp.set((SelectEntry) ent);
                        }
                    }
                    if (ent.type.equals("create") && filter[F_C]) {
                        CreateRegisterPanel rp;
                        rp = new CreateRegisterPanel();
                        pA[j++] = rp;
                        rp.set((CreateEntry) ent);
                    }
                }
            }
            ListModel lm = results.getModel();
            if (lm != null) {
                for (int i = 0; i < lm.getSize(); i++) {
                    RegisterPanel rp = (RegisterPanel) (lm.getElementAt(i));
                    rp.dispose();
                }
            }
            System.gc();
            results.setListData(pA);
        } catch (NullPointerException ex) {
        }
    }

    void upd() {
        String[] sa = register.getAllTestsAsName(null);
        if (cur_ix >= sa.length)
            cur_ix = sa.length - 1;
        setLessonName(sa[cur_ix]);
        setListData();
        stat.setText("" + (cur_ix + 1) + " / " + sa.length);
        pack();

//  	    // make it bigger to accomodate scrollbar
//  	Dimension d = getSize();
//  	Dimension d2 = new Dimension(d.width+15, d.height);
//  	setSize(d2);
    }

    public void set(Lesson.RegisterProxy register) {
        this.register = register;
        pupil_name.setText(register.pupil.getName());
        cur_ix = 0;
        upd();
    }

    public void setLessonName(String ln) {
        lesson_name.setText(ln);
        load(ln);
    }

    void load(String ln) {
    }
}
