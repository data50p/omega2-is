package com.femtioprocent.omega.lesson.canvas.result

import com.femtioprocent.omega.adm.register.data.CreateEntry
import com.femtioprocent.omega.adm.register.data.ResultTest
import com.femtioprocent.omega.adm.register.data.SelectEntry
import com.femtioprocent.omega.adm.register.data.TestEntry
import com.femtioprocent.omega.lesson.Lesson.RegisterProxy
import com.femtioprocent.omega.lesson.canvas.resultimport.StatValue1
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.swing.filechooser.ChooseExportFile
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.createPrintWriter
import com.femtioprocent.omega.util.SundryUtils.split
import org.hs.jfc.FormPanel
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.text.DecimalFormat
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableModel

class ResultDialogTableDetail(
	var owner: Frame,
	//     JLabel[] leftA = new JLabel[5];
	//     JTextField[] leftBA = new JTextField[5];
	var pupil_name_s: String,
	var lesson_file: String,
	cur_ix: Int,
	mode: Char,
	register: RegisterProxy?
) : JDialog(owner, t("Omega - Results Detail"), true), ListSelectionListener, ActionListener, ChangeListener {
    var results_sp: JScrollPane? = null
    var lesson_name: JTextField? = null
    var pupil_name: JTextField? = null
    var stat: JTextField? = null
    var register: RegisterProxy?
    var table: JTable? = null
    var cur_ix = 0
    var mode: Char
    var filter = booleanArrayOf(true, true, false)
    var CO_SEL = 0
    var CO_SENT = 1
    var CO_DUR = 2
    var CO_WORDID = 3
    var CO_CORR = 4
    var CO_RM = 5
    var CO_RO = 6
    var CO_FO = 7

    inner class Result_TableModel : AbstractTableModel() {
	var data: Array<Array<String?>>
	var hdn_c = arrayOf(
		t("Selection"),
		t("Sentence"),
		t("Time"),
		t("Type")
	)
	var hdn_t = arrayOf(
		t("Selection"),
		t("Written Sentence"),
		t("Time"),
		t("Type"),
		t("Correct Sentence"),
		t("NCS"),
		t("NCW"),
		t("NWW")
	)
	val hDN: Array<String>
	    get() = if (mode == 'c') hdn_c else hdn_t

	init {
	    data = arrayOf(arrayOf("", ""), arrayOf("", ""))
	}

	override fun getColumnCount(): Int {
	    return hDN.size
	}

	override fun getRowCount(): Int {
	    return data.size
	}

	override fun getColumnClass(c: Int): Class<*> {
	    return String::class.java
	}

	override fun getColumnName(c: Int): String {
	    return hDN[c]
	}

	override fun isCellEditable(r: Int, c: Int): Boolean {
	    return false
	}

	override fun getValueAt(row: Int, col: Int): Any {
	    return (if (data[row][col] == null) "" else data[row][col])!!
	}

	override fun setValueAt(`val`: Any, row: Int, col: Int) {
	    data[row][col] = `val` as String
	    fireTableCellUpdated(row, col)
	}

	fun setData_(data: Array<Array<String?>>) {
	    this.data = data
	    fireTableDataChanged()
	}
    }

    var tmod: Result_TableModel? = null
    val with: Array<String>
	get() = if (mode == 't') arrayOf("-test.", "-pre1.", "-post1.", "-pre2.", "-post2.") else arrayOf("-create.")

    override fun valueChanged(e: ListSelectionEvent) {
	val l = e.source as JList<*>
    }

    private fun _f(ix: Int, b: Boolean): Boolean {
	if (filter[ix] == b) return false
	filter[ix] = b
	return true
    }

    override fun stateChanged(ce: ChangeEvent) {
	val o = ce.source
	if (o is JCheckBox) {
	    val ac = o.actionCommand
	    val b = o.isSelected
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "AC " + ac);
	    var ns = false
	    if (ac == "word") ns = _f(F_W, b)
	    if (ns) {
		upd_filter()
	    }
	    return
	}
    }

    fun widthT() {
	var column = table!!.columnModel.getColumn(1)
	column.preferredWidth = 300
	column = table!!.columnModel.getColumn(4)
	column.preferredWidth = 300
	column = table!!.columnModel.getColumn(5)
	column.preferredWidth = 5
	tmod!!.fireTableStructureChanged()
    }

    fun widthC() {
	val column = table!!.columnModel.getColumn(2)
	column.preferredWidth = 5
	tmod!!.fireTableStructureChanged()
    }

    private fun setTableMode(ix: Int) {
	if (mode == 't') {
	    _f(F_T, true)
	    _f(F_C, false)
	    mode = 't'
	    val column = table!!.columnModel.getColumn(1)
	    tmod!!.fireTableStructureChanged()
	    cur_ix = ix
	    upd_filter()
	    upd()
	    return
	} else if (mode == 'c') {
	    _f(F_T, false)
	    _f(F_C, true)
	    mode = 'c'
	    val column = table!!.columnModel.getColumn(1)
	    tmod!!.fireTableStructureChanged()
	    cur_ix = ix
	    upd_filter()
	    upd()
	    return
	}
    }

    override fun actionPerformed(ae: ActionEvent) {
	val cmd = ae.actionCommand
	if ("export" == cmd) {
	    export()
	}
	if ("close" == cmd) {
	    isVisible = false
	}
    }

    fun crBu(txt: String?, cmd: String?): JButton {
	val b = JButton(txt)
	b.actionCommand = cmd
	b.addActionListener(this)
	return b
    }

    fun crCb(txt: String?, cmd: String?): JCheckBox {
	val cb = JCheckBox(txt)
	cb.addChangeListener(this)
	cb.actionCommand = cmd
	return cb
    }

    fun crTb(txt: String?, cmd: String?): JToggleButton {
	val tb = JToggleButton(txt)
	tb.addChangeListener(this)
	tb.actionCommand = cmd
	return tb
    }

    // Lesson: [..............]
    // prev   next   list   _
    internal inner class Navigator : JPanel() {
	init {
	    layout = BorderLayout()
	    val pan = FormPanel()
	    add(pan)
	    var X = 1
	    var Y = 0
	    pan.add(JLabel(t("Pupil:")), JTextField(15).also { pupil_name = it }, Y, X)
	    pupil_name!!.font = Font("dialog", Font.PLAIN, 16)
	    pupil_name!!.isEditable = false
	    pupil_name!!.text = pupil_name_s
	    X++
	    pan.add(JLabel(t("Export:")), crBu(t("As file") + "...", "export"), Y, X)
	    X = 1
	    Y++
	    var ct_tf: JTextField
	    pan.add(JLabel(t("Type:")), JTextField(if (mode == 't') "test" else "create").also { ct_tf = it }, Y, X)
	    ct_tf.isEditable = false
	    X = 1
	    Y++
	    pan.add(JLabel(t("Lesson:")), JTextField(23).also { lesson_name = it }, Y, X)
	    pan.add(JLabel(""), JTextField("0/0").also { stat = it }, Y, X)
	    lesson_name!!.font = Font("dialog", Font.PLAIN, 12)
	    lesson_name!!.isEditable = false
	}

	override fun getInsets(): Insets {
	    return Insets(5, 5, 5, 5)
	}
    }

    private fun Tt(s1: String, s2: String, s3: String, s4: String): String {
	return t(s1) + s2 + t(s3) + s4
    }

    private fun Tt(s1: String, s2: String): String {
	return Tt(s1, " = ", s2, "    ")
    }

    internal inner class Control : JPanel() {
	init {
	    val gbcf = GBC_Factory()
	    layout = GridBagLayout()
	    val mf = Font("sans", Font.PLAIN, 9)
	    val pan = JPanel()
	    pan.layout = GridLayout(0, 3)
	    var jl: JLabel
	    pan.add(JLabel(Tt("NCS", "No. Correct Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("NCW", "No. Correct Word")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("NWW", "No. Wrong Word")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("TS", "Time Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("TW", "Time Word")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("CS", "Correct Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("CW", "Correct Word")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("WW", "Wrong Word")).also { jl = it })
	    jl.font = mf
	    add(pan, gbcf.createL(0, 0, 1))
	    add(crBu(t("Close"), "close"), gbcf.createL(1, 0, 1))
	}

	override fun getInsets(): Insets {
	    return Insets(5, 5, 5, 5)
	}
    }

    inner class MyListSelectionModel : DefaultListSelectionModel(), ListSelectionListener {
	init {
	    addListSelectionListener(this)
	}

	override fun valueChanged(ev: ListSelectionEvent) {
	    try {
//	    if ( ev.getValueIsAdjusting() == false ) {
		val lselmod_ = ev.source as MyListSelectionModel
		val ix = lselmod_.minSelectionIndex
		val tmod = table!!.model as TableModel
		val imax = tmod.columnCount
		for (i in 0 until imax) {
		    val s = tmod.getValueAt(ix, i) as String
		    // 		    leftBA[i].setText(s);
		}
	    } catch (ex: Exception) {
	    }
	}
    }

    var lselmod: MyListSelectionModel = MyListSelectionModel()
    fun populate() {
	val pan = JPanel()
	pan.layout = FlowLayout()
	table = JTable(Result_TableModel().also { tmod = it })
	//	table.setAutoResizeMode(table.AUTO_RESIZE_ALL_COLUMNS);
	table!!.selectionModel = lselmod
	table!!.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
	//	table.setRowSelectionInterval(0, 0);
//	table.setPreferredSize(new Dimension(730, 300));
	results_sp = JScrollPane(table)
	val result_pan = JPanel()
	result_pan.layout = BorderLayout()
	result_pan.add(results_sp, BorderLayout.CENTER)
	val control: Control = Control()
	val stat = JPanel()
	populateStat(stat)
	result_pan.add(stat, BorderLayout.SOUTH)
	contentPane.add(Navigator(), BorderLayout.NORTH)
	contentPane.add(result_pan, BorderLayout.CENTER)
	contentPane.add(control, BorderLayout.SOUTH)
    }

    var stat_l1: JLabel? = null
    var stat_tf1: JTextField? = null
    var stat_l2: JLabel? = null
    var stat_tf2: JTextField? = null
    private fun populateStat(stat: JPanel) {
//	stat.setLayout(new
	stat_l1 = JLabel(t("Total:"))
	stat_tf1 = JTextField("", 40)
	stat_tf1!!.isEditable = false
	stat.add(stat_l1)
	stat.add(stat_tf1)
	stat.add(JLabel(t("Average time (s):")).also { stat_l2 = it })
	stat_tf2 = JTextField("", 16)
	stat_tf2!!.isEditable = false
	stat.add(stat_tf2)
	upd_stat()
    }

    fun getStringArray(saa: Array<Array<String?>>, ix: Int): Array<String?> {
	val sa = arrayOfNulls<String>(saa.size)
	for (i in sa.indices) sa[i] = saa[i][ix]
	return sa
    }

    fun tD(s: String?): Double {
	return try {
	    s!!.replace(',', '.').toDouble()
	} catch (ex: Exception) {
	    0.0
	}
    }

    private fun upd_stat() {
	stat_tf1!!.text = "..."
	try {
	    val data = tmod!!.data
	    if (data == null) {
		stat_tf1!!.text = ""
		return
	    }
	    val stat_data_rm = getStringArray(data, 5)
	    val stat_data_ro = getStringArray(data, 6)
	    val stat_data_fo = getStringArray(data, 7)
	    val correct_sent = StatValue1()
	    val correct_word = StatValue1()
	    val wrong_word = StatValue1()
	    for (i in stat_data_rm.indices) {
		if (stat_data_rm[i] == null || stat_data_rm[i]!!.length == 0) continue
		correct_sent.add(stat_data_rm[i]!!)
		correct_word.add(stat_data_ro[i]!!)
		wrong_word.add(stat_data_fo[i]!!)
	    }
	    var sum_word = 0.0
	    var sum_sent = 0.0
	    var sum_word_n = 0
	    var sum_sent_n = 0
	    val time_data = getStringArray(data, 2)
	    for (i in time_data.indices) {
		if (time_data[i] == null) continue
		val val2 = tmod!!.getValueAt(i, 1) as String
		if (val2.length == 0) {
		    sum_word += tD(time_data[i])
		    sum_word_n++
		} else {
		    sum_sent += tD(time_data[i])
		    sum_sent_n++
		}
	    }
	    val N = (correct_word.total + wrong_word.total).toInt()
	    //			     correct_sent.getAvg_100(" (", "%") +
	    stat_tf1!!.text = correct_sent.getTotalInt(t("CS") + " = ") +
		    correct_sent.getAvg(" (", "%") +  //			     correct_sent.getAvg_100(" (", "%") +
		    correct_word.getTotalInt(");    " + t("CW") + " = ") +
		    correct_word.getAvgTot(" (", "%", N) +
		    wrong_word.getTotalInt(")   " + t("WW") + " = ") +
		    wrong_word.getAvgTot(" (", "%", N) + ")"
	    val df = DecimalFormat("##0.0#")
	    var s_s = "?"
	    var s_w = "?"
	    if (sum_sent_n != 0) s_s = df.format(sum_sent / sum_sent_n)
	    if (sum_word_n != 0) s_w = df.format(sum_word / sum_word_n)
	    stat_tf2!!.text = t("TS") + " = " + s_s + "   " + t("TW") + " = " + s_w
	} catch (ex: Exception) {
	    stat_tf1!!.text = "?"
	    ex.printStackTrace()
	}
    }

    private fun upd_filter() {
	stat_tf1!!.text = "?"
	setTableData()
	val sa = tmod!!.hDN
	for (i in 0..4) {
	    var s = ""
	    if (sa.size > i) s = sa[i]
	    // 	    leftA[i].setText(s);
	}
	upd_stat()
    }

    fun form(a: Int): String {
	val d = a / 1000.0
	val df = DecimalFormat("##0.0")
	return df.format(d)
    }

    fun parseCCS(ccw: String?): Array<String?> {  // s +1; w +2 -3
	return try {
	    val sa = split(ccw, " ;+-")
	    val ss = sa[4]
	    sa
	} catch (ex: Exception) {
	    arrayOf("", "", "", "", "")
	}
    }

    var last = ""

    init {
	this.cur_ix = cur_ix
	this.mode = mode
	this.register = register
	contentPane.layout = BorderLayout()
	populate()
	pack()
	//	setSize(1000, 500);
//  	    // make it bigger to accomodate scrollbar
	val d = size
	val d2 = Dimension(d.width + 65, d.height)
	size = d2
	setTableMode(cur_ix)
	_f(F_W, true)
    }

    fun setTableData() {
	try {
	    val sa = register!!.getAllTestsAsName(with)
	    if (false && sa.size == 0) {
		val data = Array(1) { arrayOfNulls<String?>(8) }
		data[0][0] = "---"
		tmod!!.setData_(data)
		table!!.doLayout()
		return
	    }

// 	    if ( cur_ix >= sa.length )
// 		cur_ix = sa.length-1;
// 	    if ( cur_ix < 0 )
// 		cur_ix = 0;
	    val pup = register!!.pupil.name
	    val res_name = register!!.rl.getFullFName(pup, sa[cur_ix])
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "RESULT " + res_name);

// 	    if ( res_name.equals(last) )
// 		return;
// 	    last = res_name;
	    val rt = ResultTest(pup, "", "", res_name)
	    var pa_n = if (filter[F_T]) rt.getEntrySize("test") else 0
	    pa_n += if (filter[F_W]) rt.getEntrySize("select") else 0
	    pa_n += if (filter[F_C]) rt.getEntrySize("create") else 0
	    val n = rt.entrySize
	    val j = 0
	    var nn = 0
	    if (n == 0) {
		val data = Array(1) { arrayOfNulls<String>(8) }
		data[0][0] = "---"
		tmod!!.setData_(data)
		table!!.doLayout()
		return
	    }
	    for (i in 0 until n) {
		val ent = rt.getEntry(i)
		if (ent.type == "test" && filter[F_T]) {
		    nn++
		}
		if (ent.type == "select" && filter[F_W]) { // word
		    val sel = ent as SelectEntry
		    if (filter[F_T] && sel.extra.startsWith("test") ||
			    filter[F_C] && sel.extra.startsWith("create")
		    ) {
			nn++
		    }
		}
		if (ent.type == "create" && filter[F_C]) {
		    nn++
		}
	    }
	    val data = Array(nn) { arrayOfNulls<String>(8) }
	    var nn_ix = 0
	    for (i in 0 until n) {
		val ent = rt.getEntry(i)
		if (ent.type == "test" && filter[F_T]) {
		    val te = ent as TestEntry
		    val ccw = te.cnt_correct_words
		    val ccw_sa = parseCCS(ccw)
		    data[nn_ix][CO_CORR] = te.sentence
		    data[nn_ix][CO_SENT] = te.answer
		    data[nn_ix][CO_DUR] = "" + form(te.duration)
		    data[nn_ix][CO_WORDID] = "" + te.l_id_list
		    data[nn_ix][CO_RM] = "" + ccw_sa[1]
		    data[nn_ix][CO_RO] = "" + ccw_sa[3]
		    data[nn_ix][CO_FO] = "" + ccw_sa[4]
		    nn_ix++
		    //log		    OmegaContext.sout_log.getLogger().info(":--: " + "table + " + te);
		}
		if (ent.type == "select" && filter[F_W]) { // word
		    val sel = ent as SelectEntry
		    if (filter[F_T] && sel.extra.startsWith("test") ||
			    filter[F_C] && sel.extra.startsWith("create")
		    ) {
			if (filter[F_T] && sel.extra == "test:build:OK") {
			    data[nn_ix][CO_RO] = "1"
			    data[nn_ix][CO_FO] = "0"
			}
			if (filter[F_T] && sel.extra == "test:build:wrong") {
			    data[nn_ix][CO_RO] = "0"
			    data[nn_ix][CO_FO] = "1"
			}
			data[nn_ix][CO_SEL] = sel.word
			data[nn_ix][CO_DUR] = "" + form(sel.`when`)
			data[nn_ix][CO_WORDID] = "" + sel.l_id
			//log			OmegaContext.sout_log.getLogger().info(":--: " + "table + " + sel);
			nn_ix++
		    }
		}
		if (ent.type == "create" && filter[F_C]) {
		    val ce = ent as CreateEntry
		    //log		    OmegaContext.sout_log.getLogger().info(":--: " + "table + " + ce);
		    data[nn_ix][CO_SENT] = ce.sentence
		    data[nn_ix][CO_DUR] = "" + form(ce.duration)
		    data[nn_ix][CO_WORDID] = "" + ce.l_id_list
		    nn_ix++
		}
	    }
	    tmod!!.setData_(data)
	    if (mode == 't') {
		var column = table!!.columnModel.getColumn(1)
		column.preferredWidth = 300
		column = table!!.columnModel.getColumn(4)
		column.preferredWidth = 300
		column = table!!.columnModel.getColumn(5)
		column.preferredWidth = 33
		column = table!!.columnModel.getColumn(6)
		column.preferredWidth = 33
		column = table!!.columnModel.getColumn(7)
		column.preferredWidth = 33
	    } else if (mode == 'c') {
		val column = table!!.columnModel.getColumn(1)
		column.preferredWidth = 300
	    }
	    upd_stat()
	    //	    table.doLayout();
	} catch (ex: NullPointerException) {
	}
    }

    fun upd() {
	stat_tf1!!.text = "?"
	val sa = register!!.getAllTestsAsName(with)
	if (cur_ix < 0) cur_ix = 0
	if (cur_ix > sa.size - 1) cur_ix = sa.size - 1
	setLessonName(sa[cur_ix])
	setTableData()
	stat!!.text = "" + (cur_ix + 1) + " / " + sa.size
	upd_stat()
	//	pack();

//  	    // make it bigger to accomodate scrollbar
//  	Dimension d = getSize();
//  	Dimension d2 = new Dimension(d.width+15, d.height);
//  	setSize(d2);
    }

    fun export() {
	val choose_af = ChooseExportFile()
	var fn: String? = null
	val rv = choose_af.showDialog(this, t("Export"))
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_af.selectedFile
	    fn = file.absolutePath
	    if (!fn.endsWith("." + ChooseExportFile.ext)) fn = fn + "." + ChooseExportFile.ext
	} else return
	try {
	    val pw = createPrintWriter(fn)
	    pw!!.println(
		    "Pupil:," + register!!.pupil.name + ',' +
			    "Lesson Name:," + lesson_name!!.text
	    )
	    for (j in 0 until tmod!!.columnCount) {
		val col_name = tmod!!.getColumnName(j)
		if (col_name == t("Time (s)")) {
		    pw.print((if (j == 0) "" else ",") + col_name + " Sentence")
		    pw.print((if (j == 0) "" else ",") + col_name + " Word")
		} else pw.print((if (j == 0) "" else ",") + col_name)
	    }
	    pw.println("")
	    for (i in 0 until tmod!!.rowCount) {
		for (j in 0 until tmod!!.columnCount) {
		    val col_name = tmod!!.getColumnName(j)
		    val `val` = tmod!!.getValueAt(i, j) as String
		    if (col_name == t("Time (s)")) {
			val val2 = tmod!!.getValueAt(i, 1) as String
			if (val2.length == 0) pw.print(",")
			pw.print((if (j == 0) "" else ",") + `val`.replace(',', '.'))
			if (val2.length != 0) pw.print(",")
		    } else pw.print((if (j == 0) "" else ",") + `val`.replace(',', '.'))
		}
		pw.println("")
	    }
	    pw.close()
	} catch (ex: Exception) {
	    Log.getLogger().info("ERR: Can't export $fn")
	}
    }

    //     public void dep_set(Lesson.RegisterProxy register) {
    // 	this.register = register;
    // 	pupil_name.setText(register.pupil.getName());
    // 	cur_ix = 0;
    // 	//	test_tb.doClick();
    // 	upd();
    //     }
    fun setLessonName(ln: String?) {
	lesson_name!!.text = ln
	load(ln)
    }

    fun load(ln: String?) {}

    companion object {
	var F_W = 0
	var F_T = 1
	var F_C = 2
    }
}
