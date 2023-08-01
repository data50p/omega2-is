package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.adm.assets.TargetCombinations
import com.femtioprocent.omega.adm.assets.TargetCombinations.TCItem
import com.femtioprocent.omega.t9n.T.Companion.t
import javax.swing.table.AbstractTableModel

class OmAssProp_TableModel internal constructor(
	var sprop: OmegaAssetsProperty,
	var tc: TargetCombinations
) : AbstractTableModel() {
    var li_set0: MutableList<TCItem>
    var li_set: MutableList<TCItem>
    val TEST_MEM_OFFS = SentenceProperty.COL_TEST
    var hdn = arrayOf(
	    t("Source Files"),
	    t("Dependent Files"),
	    t("Exist"),
	    t("specified")
    )

    init {
	li_set = ArrayList()
	li_set.addAll(tc.dep_set)
	li_set0 = ArrayList()
	li_set0.addAll(tc.src_set)
    }

    override fun getColumnCount(): Int {
	return 4
    }

    override fun getRowCount(): Int {
	return Math.max(tc.dep_set.size, tc.src_set.size)
    }

    override fun getColumnClass(c: Int): Class<*> {
	return if (c == 0) String::class.java else if (c >= TEST_MEM_OFFS) Int::class.java else String::class.java
    }

    override fun getColumnName(c: Int): String {
	return hdn[c]
    }

    override fun isCellEditable(r: Int, c: Int): Boolean {
	return c >= TEST_MEM_OFFS
    }

    override fun getValueAt(row: Int, col: Int): Any {
	if (col == 0) {
	    var se = if (row < li_set0.size) li_set0[row].fn else ""
	    if (se == null) se = ""
	    return se
	}
	if (col == 1) {
	    var se = if (row < li_set.size) li_set[row].fn else ""
	    if (se == null) se = ""
	    return se
	}
	if (col == 2) {
	    var se = if (row < li_set.size) encode2Text(li_set[row].exist) else ""
	    if (se == null) se = ""
	    return se
	}
	if (col == 3) {
	    var se = if (row < li_set.size) li_set[row].formatOriginalExtention() else ""
	    if (se == null) se = ""
	    return se
	}
	return ""
    }

    private fun encode2Text(exist: Boolean?): String {
	return if (exist == null) "·" else if (exist) t("OK") else t("not found")
    }

    override fun setValueAt(`val`: Any, row: Int, col: Int) {
	OmegaContext.sout_log.getLogger().info(":--: SET VAL $`val`")
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

    fun update(targetCombinations: TargetCombinations) {
	tc = targetCombinations
	li_set = ArrayList()
	li_set.addAll(tc.dep_set)
	li_set0 = ArrayList()
	li_set0.addAll(tc.src_set)
	fireTableDataChanged()
    }
}
