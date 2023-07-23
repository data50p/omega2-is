package com.femtioprocent.omega.lesson.canvas.result

import com.femtioprocent.omega.adm.register.data.CreateEntry
import com.femtioprocent.omega.adm.register.data.ResultTest
import com.femtioprocent.omega.adm.register.data.SelectEntry
import com.femtioprocent.omega.adm.register.data.TestEntry
import com.femtioprocent.omega.lesson.Lesson.RegisterProxy
import com.femtioprocent.omega.lesson.canvas.resultimport.RegisterPanel
import com.femtioprocent.omega.lesson.canvas.resultimport.SelectRegisterPanel
import com.femtioprocent.omega.lesson.canvas.resultimport.TestRegisterPanel
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class ResultDialog(owner: Frame?) : JDialog(owner, "Omega -Result", true), ListSelectionListener, ActionListener, ChangeListener {
    var results_sp: JScrollPane? = null
    var results: JList<RegisterPanel>? = null
    var lesson_name: JTextField? = null
    var pupil_name: JTextField? = null
    var stat: JTextField? = null
    var register: RegisterProxy? = null
    var test_tb: JToggleButton? = null
    var create_tb: JToggleButton? = null
    var cur_ix = 0
    var filter = BooleanArray(3)
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
	    if (ac == "test") ns = _f(F_T, b)
	    if (ac == "create") ns = _f(F_C, b)
	    if (ns) {
		upd_filter()
	    }
	    return
	}
	if (o is JToggleButton) {
	    val ac = o.actionCommand
	    val b = o.isSelected
	    if (ac == "test") {
		if (b) {
		    if (create_tb!!.isSelected) create_tb!!.isSelected = !b
		} else {
		    if (create_tb!!.isSelected) create_tb!!.isSelected = !b
		}
		_f(F_T, b)
		upd_filter()
	    }
	    if (ac == "create") {
		if (b) {
		    if (test_tb!!.isSelected) test_tb!!.isSelected = !b
		} else {
		    if (test_tb!!.isSelected) test_tb!!.isSelected = !b
		}
		_f(F_C, b)
		upd_filter()
	    }
	}
    }

    override fun actionPerformed(ae: ActionEvent) {
	val cmd = ae.actionCommand
	if ("next" == cmd) {
	    cur_ix++
	    upd()
	}
	if ("prev" == cmd) {
	    if (cur_ix > 0) cur_ix--
	    upd()
	}
	if ("list" == cmd) {
	}
	if ("export" == cmd) {
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
	    val gbcf = GBC_Factory()
	    layout = GridBagLayout()
	    var Y = 0
	    add(JLabel("Pupil:"), gbcf.createL(0, Y, 1))
	    add(JTextField(15).also { pupil_name = it }, gbcf.createL(1, Y, 2))
	    pupil_name!!.font = Font("dialog", Font.PLAIN, 16)
	    pupil_name!!.isEditable = false
	    Y++
	    add(JLabel("Lesson:"), gbcf.createL(0, Y, 1))
	    add(JTextField(25).also { lesson_name = it }, gbcf.createL(1, Y, 3))
	    add(JTextField("0 / 0").also { stat = it }, gbcf.createL(4, Y, 3))
	    lesson_name!!.font = Font("dialog", Font.PLAIN, 16)
	    lesson_name!!.isEditable = false
	    Y++
	    add(JLabel("Select file:"), gbcf.createL(0, Y, 1))
	    add(crBu("Prev", "prev"), gbcf.createL(1, Y, 1))
	    add(crBu("Next", "next"), gbcf.createL(2, Y, 1))
	    add(crBu("List", "list"), gbcf.createL(3, Y, 1))
	    add(crBu("Export", "export"), gbcf.createL(4, Y, 1))
	    var cb: JComboBox<*>
	    Y++
	    add(JLabel(t("Filter:")), gbcf.createL(0, Y, 1))
	    add(JToggleButton(t("test")).also { test_tb = it })
	    add(JToggleButton(t("create")).also { create_tb = it })
	    test_tb!!.addChangeListener(this@ResultDialog)
	    create_tb!!.addChangeListener(this@ResultDialog)
	    add(test_tb, gbcf.createL(1, Y, 1))
	    add(create_tb, gbcf.createL(2, Y, 1))
	    // 	    add(cb = new JComboBox(),           gbcf.createL(1, Y, 1));
// 	    cb.addItem("test");
// 	    cb.addItem("create");
	    add(crCb(t("word"), "word"), gbcf.createL(3, Y, 1))
	    Y++
	    add(JLabel("Select lessons:"), gbcf.createL(0, Y, 1))
	    add(crBu("Prev", "lprev"), gbcf.createL(1, Y, 1))
	    add(crBu("Next", "lnext"), gbcf.createL(2, Y, 1))
	    add(crBu(" ", ""), gbcf.createL(3, Y, 1))
	    add(crBu(" ", ""), gbcf.createL(4, Y, 1))
	}

	override fun getInsets(): Insets {
	    return Insets(5, 5, 5, 5)
	}
    }

    internal inner class Control : JPanel() {
	init {
	    val gbcf = GBC_Factory()
	    layout = GridBagLayout()
	    add(crBu("Close", "close"), gbcf.createL(0, 0, 1))
	}

	override fun getInsets(): Insets {
	    return Insets(5, 5, 5, 5)
	}
    }

    inner class ResultCellRenderer : JLabel(), ListCellRenderer<Any?> {
	init {
	    isOpaque = true
	}

	override fun getListCellRendererComponent(list: JList<out Any?>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component? {
	    return value as RegisterPanel
	}
    }

    var rcr = ResultCellRenderer()

    init {
	contentPane.layout = BorderLayout()
	populate()
	pack()
	//  	    // make it bigger to accomodate scrollbar
//  	Dimension d = getSize();
//  	Dimension d2 = new Dimension(d.width+15, d.height);
//  	setSize(d2);
    }

    fun populate() {
	contentPane.add(Navigator(), BorderLayout.NORTH)
	val top = JPanel()
	top.layout = GridLayout(0, 1)
	top.add(Navigator())
	contentPane.add(top, BorderLayout.NORTH)
	//getContentPane().add(new Navigator(), BorderLayout.NORTH);
	contentPane.add(Control(), BorderLayout.SOUTH)
	if (results == null) {
	    results = JList<RegisterPanel>()
	    results_sp = JScrollPane(results)
	    contentPane.add(results_sp, BorderLayout.CENTER)
	    (results as JList<Any?>).setVisibleRowCount(7)
	    (results as JList<Any?>).setCellRenderer(rcr)
	    (results as JList<Any?>).addListSelectionListener(this)
	}
    }

    private fun upd_filter() {
	setListData()
    }

    fun setListData() {
	try {
	    val sa = register!!.getAllTestsAsName(null)
	    if (cur_ix >= sa.size) cur_ix = sa.size - 1
	    val pup = register!!.pupil.name
	    val res_name = register!!.rl.getFullFName(pup, sa[cur_ix])
	    //log	OmegaContext.sout_log.getLogger().info(":--: " + "RESULT " + res_name);
	    val rt = ResultTest(pup, "", "", res_name)
	    var pa_n = if (filter[F_T]) rt.getEntrySize("test") else 0
	    pa_n += if (filter[F_W]) rt.getEntrySize("select") else 0
	    pa_n += if (filter[F_C]) rt.getEntrySize("create") else 0
	    var pA = arrayOfNulls<RegisterPanel>(pa_n)
	    val n = rt.entrySize
	    var j = 0
	    if (pA.size == 0) {
		pA = arrayOfNulls(1)
		pA[0] = RegisterPanel()
	    } else {
		for (i in 0 until n) {
		    val ent = rt.getEntry(i)
		    if (ent.type == "test" && filter[F_T]) {
			var rp: TestRegisterPanel
			rp = TestRegisterPanel()
			pA[j++] = rp
			rp.set(ent as TestEntry)
		    }
		    if (ent.type == "select" && filter[F_W]) { // word
			val sel = ent as SelectEntry
			if (filter[F_T] && sel.extra.startsWith("test") ||
				filter[F_C] && sel.extra.startsWith("create")) {
			    var rp: SelectRegisterPanel
			    rp = SelectRegisterPanel()
			    pA[j++] = rp
			    rp.set(ent)
			}
		    }
		    if (ent.type == "create" && filter[F_C]) {
			var rp: CreateRegisterPanel
			rp = CreateRegisterPanel()
			pA[j++] = rp
			rp.set(ent as CreateEntry)
		    }
		}
	    }
	    val lm = results!!.model
	    if (lm != null) {
		for (i in 0 until lm.size) {
		    val rp = lm.getElementAt(i) as RegisterPanel
		    rp.dispose()
		}
	    }
	    System.gc()
	    results!!.setListData(pA)
	} catch (ex: NullPointerException) {
	}
    }

    fun upd() {
	val sa = register!!.getAllTestsAsName(null)
	if (cur_ix >= sa.size) cur_ix = sa.size - 1
	setLessonName(sa[cur_ix])
	setListData()
	stat!!.text = "" + (cur_ix + 1) + " / " + sa.size
	pack()

//  	    // make it bigger to accomodate scrollbar
//  	Dimension d = getSize();
//  	Dimension d2 = new Dimension(d.width+15, d.height);
//  	setSize(d2);
    }

    fun set(register: RegisterProxy) {
	this.register = register
	pupil_name!!.text = register.pupil.name
	cur_ix = 0
	upd()
    }

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
