package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.antiOmegaAssets
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.lesson.appl.LessonEditor.Companion.setDirty
import com.femtioprocent.omega.swing.TableSorter
import com.femtioprocent.omega.swing.filechooser.ChooseActionMovieFile
import com.femtioprocent.omega.swing.filechooser.ChooseGenericFile
import com.femtioprocent.omega.swing.filechooser.ChooseSignFileAlt
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files.mkRelativeCWD
import com.femtioprocent.omega.util.Files.toURL
import com.femtioprocent.omega.util.SundryUtils.a2s
import com.femtioprocent.omega.util.SundryUtils.createPrintWriter
import com.femtioprocent.omega.value.Value
import com.femtioprocent.omega.value.Values
import com.femtioprocent.omega.xml.Element
import org.hs.jfc.FormPanel
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.table.TableModel
import javax.swing.text.Document

class SentenceProperty internal constructor(var owner: JFrame, @JvmField var l_ctxt: LessonContext) :
    Property_B(owner, t("Omega - Sentence Property")) {
    @JvmField
    var guimap = HashMap<String, JComponent>()
    var table: JTable? = null
    var set_act_b: JButton? = null
    var set_sgn_b: JButton? = null

    //SentencePropPanel sn_pan;
    var rb_def: JRadioButton? = null
    var rb_act: JRadioButton? = null
    var rb_off: JRadioButton? = null
    var rb_defSign: JRadioButton? = null
    var rb_actSign: JRadioButton? = null
    var tmm: Array<IntArray>? = null
    fun destroy() {}
    fun refresh() {}
    override fun getPreferredSize(): Dimension {
	val d = super.getPreferredSize()
	return Dimension(d.getWidth().toInt() + 100, d.getHeight().toInt())
    }

    inner class myActionListener : ActionListener {
	operator fun set(what: String, value: String?) {
	    var value = value
	    val tf = guimap[what] as JTextField?
	    if (value == null) {
		val def = tf!!.text
		value = l_ctxt.lessonCanvas.askForOneTarget(this@SentenceProperty, def)
		if (value == null) value = def
	    }
	    val tf2 = guimap["sentence"] as JTextField?
	    tf2!!.text = value
	}

	override fun actionPerformed(ev: ActionEvent) {
	    val s = ev.actionCommand
	    if (s == "dep_set action file") {
		setDirty()
		val tmod = table!!.model as TableModel
		val row = table!!.selectedRow
		val ss = tmod.getValueAt(row, COL_ACT) as String
		val fn = setActionField(ss)
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "NEW FILE " + fn);
		if (fn != null) {
		    tmod.setValueAt(fn, row, COL_ACT)
		}
	    }
	    if (s == "isDef") {
		setDirty()
		set_act_b!!.isEnabled = false
		val tmod = table!!.model as TableModel
		val row = table!!.selectedRow
		val ss = tmod.getValueAt(row, COL_ACT) as String
		val fn = "" //setActionField(ss);
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "NEW FILE " + fn);
		if (fn != null) {
		    tmod.setValueAt(fn, row, COL_ACT)
		}
	    }
	    if (s == "isSpec") {
		setDirty()
		set_act_b!!.isEnabled = true
		val tmod = table!!.model as TableModel
		val row = table!!.selectedRow
		val ss = tmod.getValueAt(row, COL_ACT) as String
		if (ss == null || ss.length == 0) {
		    val fn = setActionField(ss)
		    //log		OmegaContext.sout_log.getLogger().info(":--: " + "NEW FILE " + fn);
		    if (fn != null) {
			tmod.setValueAt(fn, row, COL_ACT)
		    }
		}
	    }
	    if (s == "isOff") {
		setDirty()
		set_act_b!!.isEnabled = false
		val tmod = table!!.model as TableModel
		val row = table!!.selectedRow
		val ss = tmod.getValueAt(row, COL_ACT) as String
		val fn = "!off"
		if (fn != null) {
		    tmod.setValueAt(fn, row, COL_ACT)
		}
	    }
	    if (s == "dep_set sign file") {
		setDirty()
		val tmod = table!!.model as TableModel
		val row = table!!.selectedRow
		val ss = tmod.getValueAt(row, COL_SIGN) as String
		val fn = setSignField(ss)
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "NEW FILE " + fn);
		if (fn != null) {
		    tmod.setValueAt(fn, row, COL_SIGN)
		}
	    }
	    if (s == "isDefSign") {
		setDirty()
		set_sgn_b!!.isEnabled = false
		val tmod = table!!.model as TableModel
		val row = table!!.selectedRow
		val ss = tmod.getValueAt(row, COL_SIGN) as String
		val fn = "" //setActionField(ss);
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "NEW FILE " + fn);
		if (fn != null) {
		    tmod.setValueAt(fn, row, COL_SIGN)
		}
	    }
	    if (s == "isSpecSign") {
		setDirty()
		set_sgn_b!!.isEnabled = true
		val tmod = table!!.model as TableModel
		val row = table!!.selectedRow
		val ss = tmod.getValueAt(row, COL_SIGN) as String
		if (ss == null || ss.length == 0) {
		    val fn = setSignField(ss)
		    //log		OmegaContext.sout_log.getLogger().info(":--: " + "NEW FILE " + fn);
		    if (fn != null) {
			tmod.setValueAt(fn, row, COL_SIGN)
		    }
		}
	    }
	    if (s == "dump sent") {
		val choose_f = ChooseGenericFile()
		var url_s: String? = null
		val rv = choose_f.showDialog(ApplContext.top_frame, t("Save"))
		OmegaContext.sout_log.getLogger().info(":--: choose file -> $rv")
		if (rv == JFileChooser.APPROVE_OPTION) {
		    val file = choose_f.selectedFile
		    url_s = toURL(file)
		    val tfn = mkRelativeCWD(url_s!!)
		    val pw = createPrintWriter(tfn)
		    val sa = l_ctxt.lessonCanvas.getAllTargetCombinations("; ", false)
		    for (i in sa.indices) {
			pw!!.println(sa[i])
		    }
		    pw!!.close()
		}
	    }
	    if (s == "close") {
		isVisible = false
	    }
	}
    }

    var myactl: myActionListener = myActionListener()

    // when item in table selected
    inner class MyListSelectionModel : DefaultListSelectionModel(), ListSelectionListener {
	init {
	    addListSelectionListener(this)
	}

	override fun valueChanged(ev: ListSelectionEvent) {
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "" + ev);
	    if (ev.valueIsAdjusting == false) {
		val lselmod_ = ev.source as MyListSelectionModel
		val ix = lselmod_.minSelectionIndex
		val tmod = table!!.model as TableModel
		var s = tmod.getValueAt(ix, COL_SENT) as String
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "SEL " + lselmod_ + ' ' + ix + ' ' + s);
		val tf2 = guimap["sentence"] as JTextField?
		tf2!!.text = s
		s = tmod.getValueAt(ix, COL_ACT) as String
		if (s.length > 0) {
		    rb_act!!.isSelected = true
		    set_act_b!!.isEnabled = true
		} else {
		    rb_def!!.isSelected = true
		    set_act_b!!.isEnabled = false
		}
		s = tmod.getValueAt(ix, COL_SIGN) as String
		if (s.length > 0) {
		    rb_actSign!!.isSelected = true
		    set_sgn_b!!.isEnabled = true
		} else {
		    rb_defSign!!.isSelected = true
		    set_sgn_b!!.isEnabled = false
		}
	    }
	}
    }

    var lselmod: MyListSelectionModel = MyListSelectionModel()

    init {
	build(contentPane)
	pack()
	isVisible = true
    }

    internal inner class CloseAction : AbstractAction(t("Close")) {
	override fun actionPerformed(ev: ActionEvent) {
	    isVisible = false
	}
    }

    fun build(con: Container) {
	val fpan = FormPanel(5, 5, 7, 15)

	//	JPanel pan1 = new JPanel();
	con.layout = BorderLayout()
	var jl: JLabel?
	var tf: JTextField
	var cb: JComboBox<*>
	var ch: JCheckBox
	var jb: JButton
	var Y = 0
	var X = 0

// 	fpan.add(new JLabel(T.t("Parameter:   ")), gbcf.createL(X++, Y, 1));
// 	fpan.add(new JLabel(T.t("Value:          ")),  gbcf.createL(X++, Y, 1));

// 	Y++;
// 	X = 0;
	fpan.add(JLabel(t("Sentence")).also { jl = it }, JTextField("", 50).also { tf = it }, Y, ++X)
	guimap["sentence"] = tf
	tf.document.addDocumentListener(mydocl)
	tf.isEnabled = false
	fpan.add(JLabel(""), JButton(t("Save sentence list")).also { jb = it }, Y, ++X)
	jb.actionCommand = "dump sent"
	jb.addActionListener(myactl)
	Y++
	X = 0
	var rb: JRadioButton
	fpan.add(JLabel(t("Type:")), JRadioButton(t("Default, as dep_set in word prop")).also { rb_def = it }
	    .also { rb = it }, Y, ++X)
	val bgr = ButtonGroup()
	bgr.add(rb)
	rb.actionCommand = "isDef"
	rb.addActionListener(myactl)
	Y++
	X = 0
	fpan.add(JLabel(""), JRadioButton(t("Specific")).also { rb_act = it }.also { rb = it }, Y, ++X)
	bgr.add(rb)
	rb.actionCommand = "isSpec"
	rb.addActionListener(myactl)
	val jp2 = JPanel()
	jp2.add(JButton(t("Set action file")).also { jb = it })
	jp2.add(JLabel(""))
	fpan.add(JLabel(""), jp2, Y, X)
	set_act_b = jb
	guimap["dep_set action file"] = jb
	jb.actionCommand = "dep_set action file"
	jb.addActionListener(myactl)
	Y++
	X = 0
	fpan.add(JLabel(""), JRadioButton(t("Turn off")).also { rb_off = it }.also { rb = it }, Y, ++X)
	bgr.add(rb)
	rb.actionCommand = "isOff"
	rb.addActionListener(myactl)
	Y++
	X = 0
	var rbS: JRadioButton
	fpan.add(JLabel(t("Type:")), JRadioButton(t("Automatic, play each separate word")).also { rb_defSign = it }
	    .also { rbS = it }, Y, ++X)
	val bgrS = ButtonGroup()
	bgrS.add(rbS)
	rbS.actionCommand = "isDefSign"
	rbS.addActionListener(myactl)
	Y++
	X = 0
	fpan.add(JLabel(""), JRadioButton(t("Specific")).also { rb_actSign = it }.also { rbS = it }, Y, ++X)
	bgrS.add(rbS)
	rbS.actionCommand = "isSpecSign"
	rbS.addActionListener(myactl)
	val jp2S = JPanel()
	jp2S.add(JButton(t("Set sign file")).also { jb = it })
	jp2S.add(JLabel(""))
	fpan.add(JLabel(""), jp2S, Y, X)
	set_sgn_b = jb
	guimap["dep_set sign file"] = jb
	jb.actionCommand = "dep_set sign file"
	jb.addActionListener(myactl)
	Y++
	X = 0
	fpan.add(JLabel(""), JLabel(t("(Shift) Click on the table header to (reverse) sort")), Y, ++X)
	Y++
	X = 0
	val sa = l_ctxt.lessonCanvas.getAllTargetCombinationsEx(" ", false, '{')
	tmm = l_ctxt.lesson.getTestMatrix(sa)
	OmegaContext.sout_log.getLogger().info(":--: " + "Got sa sent " + a2s(sa))
	val tmod = SenProp_TableModel(this, sa, tmm!!)
	val tsort = TableSorter(tmod)
	table = JTable(tsort)
	tsort.addMouseListenerToHeaderInTable(table!!)
	val jscr = JScrollPane(
	    table,
	    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
	)
	for (i in 0 until table!!.columnModel.columnCount) {
	    val tcol = table!!.columnModel.getColumn(i)
	    tcol.preferredWidth = if (i == 0) 410 else if (i == 1) 180 else if (i == 2) 180 else 40
	}
	try {
	    table!!.autoResizeMode = JTable.AUTO_RESIZE_OFF
	    table!!.selectionModel = lselmod
	    table!!.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
	    table!!.setRowSelectionInterval(0, 0)
	    table!!.preferredScrollableViewportSize = Dimension(830, 300)
	} catch (ex: Exception) {
	}

	//	fpan.add(new JLabel(""), jscr, Y, ++X);

// 	JPanel c_pan = new JPanel();
// 	pan1.add(c_pan,  gbcf.createL(X++, Y, 5));
	con.add(fpan, BorderLayout.NORTH)
	con.add(jscr, BorderLayout.CENTER)
	val jpa = JPanel()
	jpa.add(JButton(CloseAction()).also { jb = it })
	con.add(jpa, BorderLayout.SOUTH)
    }

    fun setActionField(current: String?): String? {
	val choose_f = ChooseActionMovieFile(false)
	var url_s: String? = null
	val rv = choose_f.showDialog(ApplContext.top_frame, t("Select"))
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "choose file -> " + rv);
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_f.selectedFile
	    url_s = toURL(file)
	    var tfn = mkRelativeCWD(url_s!!)
	    tfn = antiOmegaAssets(tfn)
	    return tfn
	}
	return null
    }

    fun setSignField(current: String?): String? {
	val choose_f = ChooseSignFileAlt()
	var url_s: String? = null
	val rv = choose_f.showDialog(ApplContext.top_frame, t("Select"))
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "choose file -> " + rv);
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_f.selectedFile
	    url_s = toURL(file)
	    var tfn = mkRelativeCWD(url_s!!)
	    tfn = antiOmegaAssets(tfn)
	    return tfn
	}
	return null
    }

    fun updValues(vs: Values) {
	val it = vs.iterator()
	while (it.hasNext()) {
	    val v = it.next() as Value
	}
    }

    override fun updTrigger(doc: Document) {
	val it: Iterator<*> = guimap.keys.iterator()
	while (it.hasNext()) {
	    val key = it.next() as String
	    val o: Any? = guimap[key]
	    if (o is JTextField) {
		val tf = o
		if (doc === tf.document) {
		    val txt = tf.text
		    fireValueChanged(Value(key, txt))
		}
	    }
	}
    }

    fun setLabel(id: String, txt: String?) {
	val it: Iterator<*> = guimap.keys.iterator()
	while (it.hasNext()) {
	    val key = it.next() as String
	    if (key == id) {
		val o: Any? = guimap[key]
		if (o is JLabel) {
		    o.text = txt
		}
	    }
	}
    }

    override fun updTrigger(cb: JComboBox<*>?) {
	try {
	    val cbg: JComboBox<*>?

//  	    cbg = (JComboBox)guimap.get("type");
//  	    if ( cb == cbg ) {
//  		String s = (String)cb.getSelectedItem();
//  		OmegaContext.sout_log.getLogger().info(":--: " + "CB type " + cb);
//  		if ( s.equals("action") )
//  		    setLabel("Llid", "Path id");
//  		if ( s.equals("actor") )
//    		    setLabel("Llid", "Path id");
//  		else
//  		    setLabel("Llid", "-");
//  	    }
	    cbg = guimap["Slid"] as JComboBox<*>?
	    if (cb === cbg) {
		val tf = guimap["lid"] as JTextField?
		updTF(tf!!, cbg!!)
	    }
	} catch (ex: ClassCastException) {
	    OmegaContext.sout_log.getLogger().info("ERR: CCE $ex")
	}
    }

    fun updTrigger(ch: JCheckBox?) {
	try {
	    val chg: JCheckBox?
	    chg = guimap["Slid"] as JCheckBox?
	} catch (ex: ClassCastException) {
	    OmegaContext.sout_log.getLogger().info("ERR: CCE $ex")
	}
    }

    val element: Element
	get() {
	    val el = Element("test_prop")
	    var pel = Element("test")
	    pel.addAttr("kind", "pre")
	    pel.addAttr("ord", "1")
	    pel.addAttr("text", (guimap["pret1"] as JTextField?)!!.text)
	    el.add(pel)
	    pel = Element("test")
	    pel.addAttr("kind", "pre")
	    pel.addAttr("ord", "2")
	    pel.addAttr("text", (guimap["pret2"] as JTextField?)!!.text)
	    el.add(pel)
	    pel = Element("test")
	    pel.addAttr("kind", "post")
	    pel.addAttr("ord", "1")
	    pel.addAttr("text", (guimap["postt1"] as JTextField?)!!.text)
	    el.add(pel)
	    pel = Element("test")
	    pel.addAttr("kind", "post")
	    pel.addAttr("ord", "2")
	    pel.addAttr("text", (guimap["postt2"] as JTextField?)!!.text)
	    el.add(pel)
	    return el
	}

    companion object {
	const val COL_SENT = 0
	const val COL_ACT = 1
	const val COL_SIGN = 2
	const val COL_TEST = 3
    }
}
