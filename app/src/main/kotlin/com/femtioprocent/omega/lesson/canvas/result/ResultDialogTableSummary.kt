package com.femtioprocent.omega.lesson.canvas.result

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.adm.register.data.CreateEntry
import com.femtioprocent.omega.adm.register.data.ResultTest
import com.femtioprocent.omega.adm.register.data.SelectEntry
import com.femtioprocent.omega.adm.register.data.TestEntry
import com.femtioprocent.omega.lesson.Lesson.RegisterProxy
import com.femtioprocent.omega.lesson.canvas.resultimport.StatValue
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

// has UTF-8
class ResultDialogTableSummary(var owner: Frame) : JDialog(owner, t("Omega - Results Summary"), true), ListSelectionListener, ActionListener, ChangeListener {
    var results_sp: JScrollPane? = null
    var lesson_name = JTextField("___")
    var pupil_name: JTextField? = null
    var stat: JTextField? = null
    var register: RegisterProxy? = null
    var pupilL: JLabel? = null
    var pupilTF: JTextField? = null
    var details: JButton? = null
    var table: JTable? = null
    var test_tb: JToggleButton? = null
    var create_tb: JToggleButton? = null
    var cur_ix = 0
    var CO_dat = 0
    var CO_l = 1
    var CO_t = 2
    var CO_rm = 3
    var CO_am = 3
    var CO_fm = 4
    var CO_prm = 5
    var CO_ro = 6
    var CO_ao = 6
    var CO_fo = 7
    var CO_pro = 8
    var CO_trm = 9
    var CO_tfm = 10
    var CO_sl = 11
    var CO_fn = 12
    var CO_MAX = 13
    var map_t = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    var map_c = intArrayOf(0, 1, 7, 2, 7, 7, 3, 7, 7, 4, 7, 5, 7)
    fun map(v: Int): Int {
	return if (tmod!!.mode == 'c') map_c[v] else map_t[v]
    }

    inner class Result_TableModel : AbstractTableModel() {
	var mode = 't'
	var data = Array<Array<String?>?>(0) { arrayOfNulls(0) }
	var hdn_t = arrayOf(t("Dat"),
		t("L#"),
		t("Lt"),
		t("CS"),
		t("WS"),
		t("%CS"),
		t("CW"),
		t("WW"),
		t("%CW"),
		t("TCS"),
		t("TWS"),
		t("SL")
	)
	var hdn_c = arrayOf(t("Dat"),
		t("L#"),  //
		t("NS"),  //fm
		//prm
		t("NW"),  //fo
		//pfo
		t("TS"),  //
		t("SL")
	)
	val hDN: Array<String>
	    get() = if (mode == 'c') hdn_c else hdn_t

	init {
	    data = Array(100) { arrayOfNulls(13) }
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
	    return (if (data[row]!![col] == null) "" else data[row]!![col])!!
	}

	override fun setValueAt(`val`: Any, row: Int, col: Int) {
	    data[row]!![col] = `val` as String
	    fireTableCellUpdated(row, col)
	}

	fun setData_(data: Array<Array<String?>?>) {
	    this.data = data
	    fireTableStructureChanged()
	    fireTableDataChanged()
	}
    }

    var tmod: Result_TableModel? = null
    val with: Array<String>
	get() = if (tmod!!.mode == 't') arrayOf("-test.", "-pre1.", "-post1.", "-pre2.", "-post2.") else arrayOf("-create.")

    override fun valueChanged(e: ListSelectionEvent) {
	val l = e.source as JList<*>
    }

    override fun stateChanged(ce: ChangeEvent) {
	val o = ce.source
	if (o is JCheckBox) {
	    val ac = o.actionCommand
	    val b = o.isSelected
	    return
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
    override fun actionPerformed(ae: ActionEvent) {
	if (ae.source === test_tb) {
	    tmod!!.mode = 't'
	    tmod!!.fireTableStructureChanged()
	    cur_ix = 0
	    upd_filter()
	    upd()
	    return
	}
	if (ae.source === create_tb) {
	    tmod!!.mode = 'c'
	    val column = table!!.columnModel.getColumn(1)
	    tmod!!.fireTableStructureChanged()
	    cur_ix = 0
	    upd_filter()
	    upd()
	    return
	}
	if (ae.source === details) {
	    val rdt = ResultDialogTableDetail(owner,
		    pupil_name!!.text,
		    lesson_name.text,
		    cur_ix,
		    tmod!!.mode,
		    register)
	    OmegaContext.HELP_STACK.push("result_detail")
	    rdt.isVisible = true
	    OmegaContext.HELP_STACK.pop("result_detail")
	}
	val cmd = ae.actionCommand
	if ("export" == cmd) {
	    export()
	}
	if ("close" == cmd) {
	    isVisible = false
	}
    }

    val allTestAsName: Array<String?>?
	get() = register!!.getAllTestsAsName(with)

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
	    X++
	    pan.add(JLabel(t("Export")), crBu(t("As file") + "...", "export"), Y, X)
	    X = 1
	    Y++
	    pan.add(JLabel(t("Select Type:")), JLabel(""), Y, X)
	    pan.add(JRadioButton(t("test")).also { test_tb = it },
		    JRadioButton(t("create")).also {
			create_tb = it
		    },
		    Y, X)
	    val bg = ButtonGroup()
	    bg.add(test_tb)
	    bg.add(create_tb)
	    test_tb!!.addActionListener(this@ResultDialogTableSummary)
	    create_tb!!.addActionListener(this@ResultDialogTableSummary)
	    // 	    test_tb.addChangeListener(ResultDialogTableSummary.this);
// 	    create_tb.addChangeListener(ResultDialogTableSummary.this);
	    test_tb!!.isSelected = true
	    X++
	    pan.add(JLabel(""), JButton(t("Details")).also { details = it }, Y, ++X)
	    details!!.actionCommand = "details"
	    details!!.addActionListener(this@ResultDialogTableSummary)
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
	    pan.layout = GridLayout(0, 4)
	    var jl: JLabel
	    pan.add(JLabel(Tt("Dat", "Date")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("L#", "Lesson id")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("Lt", "Lesson type")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("CS", "Correct Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("WS", "Wrong Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("NS", "No. of Sentences")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("%CS", "Correct Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("CW", "Correct Word")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("WW", "Wrong Word")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("NW", "No. of Words")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("%CW", "Correct Word")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("TCS", "Time Correct Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("TWS", "Time Wrong Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("TS", "Time Sentence")).also { jl = it })
	    jl.font = mf
	    pan.add(JLabel(Tt("SL", "Session Length")).also { jl = it })
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
		    cur_ix = ix
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
//	table.setPreferredSize(new Dimension(600, 300));
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
    private fun populateStat(stat: JPanel) {
//	stat.setLayout(new
	stat_l1 = JLabel(t("Average") + ':')
	stat_tf1 = JTextField("", 40)
	stat_tf1!!.isEditable = false
	stat.add(stat_l1)
	stat.add(stat_tf1)
	upd_stat()
    }

    fun getStringArray(saa: Array<Array<String?>?>, ix: Int): Array<String?> {
	val sa = arrayOfNulls<String>(saa.size)
	for (i in sa.indices) sa[i] = saa[i]!![ix]
	return sa
    }

    fun tD(s: String?): Double {
	return try {
	    s!!.replace(',', '.').toDouble()
	} catch (ex: Exception) {
	    0.0
	}
    }

    fun calcData(sv: StatValue, sa: Array<String?>) {
	for (i in sa.indices) {
	    if (sa[i] == null || sa[i]!!.length == 0) continue
	    sv.add(tD(sa[i]))
	}
    }

    private fun upd_stat() {
	try {
	    val data = tmod!!.data
	    if (data == null) {
		stat_tf1!!.text = ""
		return
	    }
	    val st_trm = StatValue()
	    val data_trm = getStringArray(data, map(CO_trm))
	    calcData(st_trm, data_trm)
	} catch (ex: Exception) {
	}
    }

    private fun upd_filter() {
	setTableData()
	upd_stat()
    }

    fun form(a: Int): String {
	val d = a / 1000.0
	val df = DecimalFormat("##0.0")
	return df.format(d)
    }

    fun asHMS(a: Double): String {
	var a = a
	val ms = (a % 1000).toInt()
	a /= 1000.0
	val s = (a % 60).toInt()
	a /= 60.0
	var m = a.toInt()
	if (m > 60) {
	    m = (a % 60).toInt()
	    a /= 60.0
	    val h = a.toInt()
	    return "" + h + "h " + m + "m " + s + "s"
	}
	return "" + m + "m " + s + "s"
    }

    fun asNoHMS(s: String): Int {  // 1h 12m 7s
	var s = s
	s = s.replace('m', ' ').replace('s', ' ').replace('h', ' ')
	val sa = split(s, " ")
	return if (sa.size == 2) sa[1]!!.toInt() + sa[0]!!.toInt() * 60 else sa[2]!!.toInt() + sa[1]!!.toInt() * 60 + sa[0]!!.toInt() * 60 * 60
    }

    var last = ""

    init {
	contentPane.layout = BorderLayout()
	populate()
	pack()
	setSize(700, 500)
	// make it bigger to accomodate scrollbar
//   	Dimension d = getSize();
//   	Dimension d2 = new Dimension(d.width+15, d.height);
//   	setSize(d2);
	upd_filter()
    }

    fun setTableData() {
	try {
	    if (register == null) return
	    val sa = register!!.getAllTestsAsName(with)
	    val data = Array<Array<String?>?>(sa!!.size) { arrayOfNulls(if (tmod!!.mode == 't') CO_MAX else 8) }
	    val dataLi: ArrayList<Array<String?>?> = ArrayList()
	    val statval_tfm = StatValue()
	    val statval_trm = StatValue()
	    val statval_pro = StatValue()
	    val statval_prm = StatValue()
	    val stat_sl = StatValue()
	    NEXT_LESSON@ for (i in sa!!.indices) {
		val testName = sa!![i]
		if (cur_ix >= sa!!.size) cur_ix = sa!!.size - 1
		if (cur_ix < 0) cur_ix = 0
		val pup = register!!.pupil.name
		val res_name = register!!.rl.getFullFName(pup, sa!![i]!!)
		val rt = ResultTest(pup, "", "", res_name)
		val n = rt.entrySize
		val j = 0
		val nn = 0
		if (n == 0) {
		    data[i]!![0] = "---"
		    dataLi.add(data[i])
		    continue
		}
		data[i]!![map(CO_fn)] = testName
		try {
		    val tname = split(testName, "-") // pupil-date_clock-TID-type.omegaresult
		    data[i]!![map(CO_dat)] = tname[1]!!.substring(0, 8)
		    data[i]!![map(CO_l)] = tname[2]
		    data[i]!![map(CO_t)] = tname[3]
		} catch (ex: Exception) {
		    data[i]!![0] = testName
		    data[i]!![map(CO_t)] = ""
		}
		data[i]!![map(CO_sl)] = "" + asHMS(1.0 * rt.session_length)
		if (tmod!!.mode == 't') {
		    if (data[i]!![map(CO_t)] == "test" || data[i]!![map(CO_t)] == "pre1" || data[i]!![map(CO_t)] == "pre2" || data[i]!![map(CO_t)] == "post1" || data[i]!![map(CO_t)] == "post2") dataLi.add(data[i]) else continue@NEXT_LESSON
		}
		if (tmod!!.mode == 'c') {
		    if (data[i]!![map(CO_t)] == "create") dataLi.add(data[i]) else continue@NEXT_LESSON
		}
		val nn_ix = 0
		val stat_data = getStringArray(data, 5)
		val correct_sent = StatValue()
		val correct_word = StatValue()
		val wrong_word = StatValue()
		val wrong_sent = StatValue()
		val wrong_sent_time = StatValue()
		val correct_sent_time = StatValue()
		var w_cnt = 0
		val i2_l = rt.howManyTestEntries()
		for (i2 in 0 until i2_l) {
		    val ent = rt.getEntry(i2)
		    //		    OmegaContext.sout_log.getLogger().info(":--: " + "ent " + ent);

		    /*
                    if ( "select".equals(ent.type) ) {
			if ( ent.extra.endsWith(":wrong") ) {
			    wrong_word_time.add(ent.when);
			}
		    }
		    */if (tmod!!.mode == 't' && "test" == ent.type) {
			try {
			    val tent = ent as TestEntry
			    val ccw = tent.cnt_correct_words ?: continue // s 1;w +3 -0
			    val s = ccw.replace(';', ' ')
			    val sat = split(s, " ")
			    if (sat.size > 3) {
				if (sat[1] == "1") {
				    correct_sent.add("1")
				    correct_sent_time.add("" + tent.duration)
				} else {
				    wrong_sent.add("1")
				    wrong_sent_time.add("" + tent.duration)
				}
				correct_word.add(sat[3]!!)
				wrong_word.add(sat[4]!!)
			    }
			} catch (ex: ArrayIndexOutOfBoundsException) {
			}
		    }
		    if (tmod!!.mode == 'c') {
			if ("create" == ent.type) {
			    try {
				val cent = ent as CreateEntry
				correct_sent_time.add("" + cent.duration)
				correct_sent.add("1")
			    } catch (ex: ArrayIndexOutOfBoundsException) {
			    }
			}
			if ("select" == ent.type) {
			    try {
				val sent = ent as SelectEntry
				if (sent.extra.startsWith("create")) w_cnt++
			    } catch (ex: ArrayIndexOutOfBoundsException) {
			    }
			}
		    }
		}
		if (tmod!!.mode == 'c') correct_word.add("" + w_cnt)
		val Ns = (correct_sent.total + wrong_sent.total).toInt()
		val Nw = (correct_word.total + wrong_word.total).toInt()
		if (tmod!!.mode == 't') {
		    data[i]!![map(CO_rm)] = correct_sent.getTotalInt("")
		    data[i]!![map(CO_fm)] = wrong_sent.getTotalInt("")
		    data[i]!![map(CO_prm)] = correct_sent.getAvgTot("", "", Ns)
		    data[i]!![map(CO_ro)] = correct_word.getTotalInt("")
		    data[i]!![map(CO_fo)] = wrong_word.getTotalInt("")
		    data[i]!![map(CO_pro)] = correct_word.getAvgTot("", "", Nw)
		    data[i]!![map(CO_tfm)] = wrong_sent_time.getAvg_1000("", "")
		    data[i]!![map(CO_trm)] = correct_sent_time.getAvg_1000("", "")
		} else {
		    data[i]!![map(CO_am)] = correct_sent.getTotalInt("")
		    data[i]!![map(CO_ao)] = correct_word.getTotalInt("")
		    data[i]!![map(CO_trm)] = correct_sent_time.getAvg_1000("", "")
		}
		if (wrong_sent_time.has()) statval_tfm.add(wrong_sent_time.avg1)
		if (correct_sent_time.has()) statval_trm.add(correct_sent_time.avg1)
		if (Nw > 0) statval_pro.add(correct_word.getAvg1(Nw))
		if (Ns > 0) statval_prm.add(correct_sent.getAvg1(Ns))
		if (rt.session_length > 0) stat_sl.add(rt.session_length.toDouble())
	    }
	    if (tmod!!.mode == 't') stat_tf1!!.text = t("%CS") + " " + statval_prm.getAvg("", "") + "   " +
		    t("%CW") + " " + statval_pro.getAvg("", "") + "   " +
		    t("TCS") + " " + statval_trm.getAvg_1000("", "") + "   " +
		    t("TWS") + " " + statval_tfm.getAvg_1000("", "") + "   " +
		    t("SL") + " " + asHMS(stat_sl.avg1) else stat_tf1!!.text = t("TS") + " " + statval_trm.getAvg_1000("", "") + "   " +
		    t("SL") + " " + stat_sl.getAvg_1000("", "")
	    tmod!!.setData_(dataLi.toTypedArray<Array<String?>?>())
	    table!!.doLayout()
	    val column = table!!.columnModel.getColumn(map(CO_dat))
	    column.preferredWidth = 100
	    //	    upd_stat();
	    //	    table.doLayout();
	} catch (ex: NullPointerException) {
	    tmod!!.setData_(Array(0) { arrayOfNulls(1) })
	    Log.getLogger().info("ERR: npe $ex")
	    ex.printStackTrace()
	}
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
	    pw!!.println("Pupil:," + register!!.pupil.name + ',' +
		    "Lesson Name:," + lesson_name.text
	    )
	    for (j in 0 until tmod!!.columnCount) {
		val col_name = tmod!!.getColumnName(j)
		pw.print((if (j == 0) "" else ",") + col_name)
	    }
	    pw.println("")
	    for (i in 0 until tmod!!.rowCount) {
		for (j in 0 until tmod!!.columnCount) {
		    val col_name = tmod!!.getColumnName(j)
		    val `val` = tmod!!.getValueAt(i, j) as String
		    if (j == map(CO_sl)) {
			val val2 = tmod!!.getValueAt(i, 1) as String
			if (val2.length == 0) pw.print(",") else pw.print((if (j == 0) "" else ",") + asNoHMS(`val`))
		    } else pw.print((if (j == 0) "" else ",") + `val`.replace(',', '.'))
		}
		pw.println("")
	    }
	    pw.close()
	} catch (ex: Exception) {
	    Log.getLogger().info("ERR: Can't export $fn $ex")
	    ex.printStackTrace()
	}
    }

    fun upd() {
	stat_tf1!!.text = "?"
	val sa = register!!.getAllTestsAsName(with)
	if (cur_ix < 0) cur_ix = 0
	if (cur_ix > sa!!.size - 1) cur_ix = sa!!.size - 1
	if (cur_ix > 0) setLessonName(sa!![cur_ix])
	setTableData()
	upd_stat()
    }

    fun set(register: RegisterProxy) {
	this.register = register
	pupil_name!!.text = register.pupil.name
	cur_ix = 0
	test_tb!!.doClick()
	upd()
    }

    fun setLessonName(ln: String?) {
	lesson_name.text = ln
	load(ln)
    }

    fun load(ln: String?) {}
}
