package com.femtioprocent.omega.lesson.canvas;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.assets.TargetCombinations;
import com.femtioprocent.omega.t9n.T;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;


public class OmAssProp_TableModel extends AbstractTableModel {
    OmegaAssetsProperty sprop;

    TargetCombinations tc;
    List<TargetCombinations.TCItem> li_set0;
    List<TargetCombinations.TCItem> li_set;

    int[][] test_member_map;

    final int TEST_MEM_OFFS = SentenceProperty.COL_TEST;

    String[] hdn = new String[]{
            T.t("Source Files"),
            T.t("Dependent Files"),
            T.t("Exist"),
            T.t("specified")
    };

    OmAssProp_TableModel(OmegaAssetsProperty sprop, TargetCombinations tc, int[][] tmm) {
        this.sprop = sprop;
        this.tc = tc;
        li_set = new ArrayList<TargetCombinations.TCItem>();
        li_set.addAll(tc.dep_set);
        li_set0 = new ArrayList<TargetCombinations.TCItem>();
        li_set0.addAll(tc.src_set);
        test_member_map = tmm;
    }

    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return Math.max(tc.dep_set.size(), tc.src_set.size());
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
        if (col == 0) {
            String se = row < li_set0.size() ? li_set0.get(row).fn : "";
            if (se == null)
                se = "";
            return se;
        }

        if (col == 1) {
            String se = row < li_set.size() ? li_set.get(row).fn : "";
            if (se == null)
                se = "";
            return se;
        }

        if (col == 2) {
            String se = row < li_set.size() ? encode2Text(li_set.get(row).exist) : "";
            if (se == null)
                se = "";
            return se;
        }

        if (col == 3) {
            String se = row < li_set.size() ? li_set.get(row).formatOriginalExtention() : "";
            if (se == null)
                se = "";
            return se;
        }

        return "";
    }

    private String encode2Text(Boolean exist) {
        return exist == null ? "·" : exist ? T.t("OK") : T.t("not found");
    }

    public void setValueAt(Object val, int row, int col) {
        OmegaContext.sout_log.getLogger().info("ERR: " + "SET VAL " + val);
//	if (col == SentenceProperty.COL_ACT) {
//	    String sent = sa[row].replaceAll("\\{[a-z0-9]*?\\}", "");
//	    sprop.l_ctxt.getLesson().action_specific.setAction(sent, (String) val);
//	}
//	if (col == SentenceProperty.COL_SIGN) {
//	    String sent = sa[row].replaceAll("\\{[a-z0-9]*?\\}", "");
//	    sprop.l_ctxt.getLesson().action_specific.setSign(sent, (String) val);
//	}
//	if (col >= TEST_MEM_OFFS) {
//	    test_member_map[row][col - TEST_MEM_OFFS] = ((Integer) val).intValue();
//	    sprop.l_ctxt.getLesson().setTestMatrix(sa, test_member_map);
//	}
//	sprop.repaint();
    }

    public void update(TargetCombinations targetCombinations) {
        tc = targetCombinations;
        li_set = new ArrayList<TargetCombinations.TCItem>();
        li_set.addAll(tc.dep_set);
        li_set0 = new ArrayList<TargetCombinations.TCItem>();
        li_set0.addAll(tc.src_set);
        fireTableDataChanged();
    }
}

