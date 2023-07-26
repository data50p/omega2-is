package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.t9n.T.Companion.t
import javax.swing.table.AbstractTableModel

class SenProp_TableModel internal constructor(
    var sprop: SentenceProperty,
    var sa: Array<String?>,
    var test_member_map: Array<IntArray>
) : AbstractTableModel() {
    val TEST_MEM_OFFS = SentenceProperty.COL_TEST
    var hdn = arrayOf(
	t("Sentence"),
	t("Action File"),
	t("Sign movie"),
	t("Pre 1"),
	t("Pre 2"),
	t("Post 1"),
	t("Post 2") //  					     T.t("<html>Test <b>Pre 1</B></html>"),
	//  					     T.t("<html>Test <b>Pre 2</B></html>"),
	//  					     T.t("<html>Test <b>Post 1</B></html>"),
	//  					     T.t("<html>Test <b>Post 2</B></html>")
    )

    override fun getColumnCount(): Int {
	return 6 + 1
    }

    override fun getRowCount(): Int {
	return sa.size
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
	if (col == 0) return sa[row]!!
	if (col == 1) {
	    var se = sa[row]
	    if (se == null) se = ""
	    val sent = se.replace("\\{[a-z0-9]*?\\}".toRegex(), "")
	    val s = sprop.l_ctxt.lesson.action_specific!!.getAction(sent)
	    return s ?: ""
	}
	if (col == 2) {
	    var se = sa[row]
	    if (se == null) se = ""
	    val sent = se.replace("\\{[a-z0-9]*?\\}".toRegex(), "")
	    val s = sprop.l_ctxt.lesson.action_specific!!.getSign(sent)
	    return s ?: ""
	}
	return if (col >= TEST_MEM_OFFS) {
	    test_member_map[row][col - TEST_MEM_OFFS]
	} else ""
    }

    override fun setValueAt(`val`: Any, row: Int, col: Int) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "SET VAL " + val);
	if (col == SentenceProperty.COL_ACT) {
	    val sent = sa[row]!!.replace("\\{[a-z0-9]*?\\}".toRegex(), "")
	    sprop.l_ctxt.lesson.action_specific!!.setAction(sent, `val` as String)
	}
	if (col == SentenceProperty.COL_SIGN) {
	    val sent = sa[row]!!.replace("\\{[a-z0-9]*?\\}".toRegex(), "")
	    sprop.l_ctxt.lesson.action_specific!!.setSign(sent, `val` as String)
	}
	if (col >= TEST_MEM_OFFS) {
	    test_member_map[row][col - TEST_MEM_OFFS] = `val` as Int
	    sprop.l_ctxt.lesson.setTestMatrix(sa, test_member_map)
	}
	sprop.repaint()
    }
}
