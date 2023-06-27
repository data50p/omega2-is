package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.t9n.T;

import javax.swing.table.AbstractTableModel;


public class SenProp_TableModel extends AbstractTableModel {
    SentenceProperty sprop;

    String[] sa;
    int[][] test_member_map;

    final int TEST_MEM_OFFS = SentenceProperty.COL_TEST;

    String[] hdn = new String[]{T.t("Sentence"),
            T.t("Action File"),
            T.t("Sign movie"),
            T.t("Pre 1"),
            T.t("Pre 2"),
            T.t("Post 1"),
            T.t("Post 2")
//  					     T.t("<html>Test <b>Pre 1</B></html>"),
//  					     T.t("<html>Test <b>Pre 2</B></html>"),
//  					     T.t("<html>Test <b>Post 1</B></html>"),
//  					     T.t("<html>Test <b>Post 2</B></html>")
    };

    SenProp_TableModel(SentenceProperty sprop, String sa[], int[][] tmm) {
        this.sprop = sprop;
        this.sa = sa;
        test_member_map = tmm;
    }

    public int getColumnCount() {
        return 6 + 1;
    }

    public int getRowCount() {
        return sa.length;
    }

    public Class getColumnClass(int c) {
        return c == 0 ? String.class :
                c >= TEST_MEM_OFFS ? Integer.class :
                        String.class;
    }

    public String getColumnName(int c) {
        return hdn[c];
    }

    public boolean isCellEditable(int r, int c) {
        return c >= TEST_MEM_OFFS;
    }

    public Object getValueAt(int row, int col) {
        if (col == 0)
            return sa[row];

        if (col == 1) {
            String se = sa[row];
            if (se == null)
                se = "";
            String sent = se.replaceAll("\\{[a-z0-9]*?\\}", "");
            String s = sprop.l_ctxt.getLesson().action_specific.getAction(sent);
            if (s != null)
                return s;
            else
                return "";
        }

        if (col == 2) {
            String se = sa[row];
            if (se == null)
                se = "";
            String sent = se.replaceAll("\\{[a-z0-9]*?\\}", "");
            String s = sprop.l_ctxt.getLesson().action_specific.getSign(sent);
            if (s != null)
                return s;
            else
                return "";
        }

        if (col >= TEST_MEM_OFFS) {
            return test_member_map[row][col - TEST_MEM_OFFS];
        }

        return "";
    }

    public void setValueAt(Object val, int row, int col) {
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "SET VAL " + val);
        if (col == SentenceProperty.COL_ACT) {
            String sent = sa[row].replaceAll("\\{[a-z0-9]*?\\}", "");
            sprop.l_ctxt.getLesson().action_specific.setAction(sent, (String) val);
        }
        if (col == SentenceProperty.COL_SIGN) {
            String sent = sa[row].replaceAll("\\{[a-z0-9]*?\\}", "");
            sprop.l_ctxt.getLesson().action_specific.setSign(sent, (String) val);
        }
        if (col >= TEST_MEM_OFFS) {
            test_member_map[row][col - TEST_MEM_OFFS] = ((Integer) val).intValue();
            sprop.l_ctxt.getLesson().setTestMatrix(sa, test_member_map);
        }
        sprop.repaint();
    }
}

