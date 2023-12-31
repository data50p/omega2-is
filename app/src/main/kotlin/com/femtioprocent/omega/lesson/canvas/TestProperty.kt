package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.value.Value
import com.femtioprocent.omega.value.Values
import com.femtioprocent.omega.xml.Element
import java.awt.Color
import java.awt.Container
import java.awt.Graphics
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.text.Document

class TestProperty internal constructor(owner: JFrame?, var l_ctxt: LessonContext) :
	Property_B(owner, t("Omega - Test Property")) {
    var guimap: HashMap<String?, JComponent?> = HashMap()
    fun refresh() {}
    inner class myActionListener : ActionListener {
	operator fun set(what: String?, value: String?) {
	    var value = value
	    val tf = guimap[what] as JTextField?
	    if (value == null) {
		val def = tf!!.text
		value = l_ctxt.lessonCanvas.askForOneTarget(this@TestProperty, def)
		if (value == null) value = def
	    }
	    tf!!.text = value
	}

	override fun actionPerformed(ev: ActionEvent) {
	    val s = ev.actionCommand
	    if (s == "setpret1") {
		set("pret1", null)
	    }
	    if (s == "setpret2") {
		set("pret2", null)
	    }
	    if (s == "setpostt1") {
		set("postt1", null)
	    }
	    if (s == "setpostt2") {
		set("postt2", null)
	    }
	    if (s == "setpret1_") {
		set("pret1", l_ctxt.target!!.allText)
	    }
	    if (s == "setpret2_") {
		set("pret2", l_ctxt.target!!.allText)
	    }
	    if (s == "setpostt1_") {
		set("postt1", l_ctxt.target!!.allText)
	    }
	    if (s == "setpostt2_") {
		set("postt2", l_ctxt.target!!.allText)
	    }
	}
    }

    var myactl: myActionListener = myActionListener()

    init {
	build(contentPane)
	pack()
	isVisible = true
    }

    fun build(con: Container) {
	con.layout = GridBagLayout()
	var jl: JLabel?
	var tf: JTextField
	var cb: JComboBox<*>
	var ch: JCheckBox?
	var jb: JButton
	var Y = 0
	var X = 0

	con.add(JLabel(t("Parameter:   ")), gbcf.createL(X++, Y, 1))
	con.add(JLabel(t("Value          ")), gbcf.createL(X++, Y, 1))
	con.add(object : JPanel() {
	    public override fun paintComponent(g: Graphics) {
		super.paintComponent(g)
		g.color = Color.green
		g.fillRect(0, 0, 12, 12)
	    }
	}, gbcf.createL(X++, Y, 1))

	Y++
	X = 0
	con.add(JLabel(t("Pretest 1")).also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JTextField("", 50).also {
	    it.document.addDocumentListener(mydocl)
	    it.isEnabled = false
	    guimap["pret1"] = it
	}, gbcf.createL(X++, Y, 1))

	con.add(JButton(t("Set shown")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpret1_"
	jb.addActionListener(myactl)

	con.add(JButton(t("Set from list")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpret1"
	jb.addActionListener(myactl)
	guimap["Lpret1"] = jl

	Y++
	X = 0
	con.add(JLabel(t("Pretest 2")).also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JTextField("", 50).also { tf = it }, gbcf.createL(X++, Y, 1))
	con.add(JButton(t("Set shown")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpret2_"
	jb.addActionListener(myactl)
	con.add(JButton(t("Set from list")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpret2"
	jb.addActionListener(myactl)
	tf.document.addDocumentListener(mydocl)
	tf.isEnabled = false
	guimap["pret2"] = tf
	guimap["Lpret2"] = jl
	Y++
	X = 0
	con.add(JLabel(t("Posttest 1")).also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JTextField("", 50).also { tf = it }, gbcf.createL(X++, Y, 1))
	con.add(JButton(t("Set shown")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpostt1_"
	jb.addActionListener(myactl)
	con.add(JButton(t("Set from list")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpostt1"
	jb.addActionListener(myactl)
	tf.document.addDocumentListener(mydocl)
	tf.isEnabled = false
	guimap["postt1"] = tf
	guimap["Lpostt1"] = jl
	Y++
	X = 0
	con.add(JLabel(t("Posttest 2")).also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JTextField("", 50).also { tf = it }, gbcf.createL(X++, Y, 1))
	con.add(JButton(t("Set shown")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpostt2"
	jb.addActionListener(myactl)
	con.add(JButton(t("Set from list")).also { jb = it }, gbcf.createL(X++, Y, 1))
	jb.actionCommand = "setpostt2_"
	jb.addActionListener(myactl)
	tf.document.addDocumentListener(mydocl)
	tf.isEnabled = false
	guimap["postt2"] = tf
	guimap["Lpostt2"] = jl
	Y++
	X = 0
	con.add(JLabel(t("Settings")).also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JCheckBox(t("Show word box")).also { ch = it }, gbcf.createL(X++, Y, 1))
	guimap["shwbx"] = ch
	guimap["Lshwbx"] = jl
	Y++
	X = 0
	con.add(JLabel("").also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JCheckBox(t("Hilite word box")).also { ch = it }, gbcf.createL(X++, Y, 1))
	guimap["hlwbx"] = ch
	guimap["Lhlwbx"] = jl
    }

    fun updValues(vs: Values) {
	val it = vs.hm.values.forEach { v ->

//  	    if ( v.getId().equals("pathlist") ) {         // banor
//  		JComboBox cb = (JComboBox)guimap.get("Slid");
//  		String[] sa = SundryUtils.split(v.getStr(), ",");
//  		cb.removeAllItems();
//  		cb.addItem("");
//  		for(int i = 0; i < sa.length; i++)
//  		    cb.addItem("= " + sa[i]);
//  		for(int i = 0; i < sa.length; i++)
//  		    cb.addItem("+ " + sa[i]);
//  		cb.setSelectedIndex(0);
//  		pack();
//  		continue;
//  	    }

//  	    Object gui = guimap.get(v.id);
//  	    if ( gui instanceof JTextField ) {
//  		JTextField tf = (JTextField)gui;
//  		tf.setText(v.getStr());
//  	    }
//  	    if ( gui instanceof JComboBox ) {
//  		JComboBox cb = (JComboBox)gui;
//  		cb.setSelectedItem(v.getStr());
//  	    }
//  	    gui = guimap.get("L" + v.id);
//  	    if ( gui instanceof JLabel ) {
//  		JLabel jl = (JLabel)gui;
//  //		jl.setText(v.getId());
//  	    }
	}
    }

    override fun updTrigger(doc: Document) {
	guimap.keys.forEach {key ->
	    val o = guimap[key]
	    if (o is JTextField) {
		val tf = o
		if (doc === tf.document) {
		    val txt = tf.text
		    fireValueChanged(Value(key!!, txt))
		}
	    }
	}
    }

    fun setLabel(id: String, txt: String?) {
	guimap.keys.forEach {key ->
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
		updTF(tf!!, cbg)
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
	    return Element("test_prop").also {
		it.add( Element("test").also {
		    it.addAttr("kind", "pre")
		    it.addAttr("ord", "1")
		    it.addAttr("text", (guimap["pret1"] as JTextField?)!!.text)
		})
		it.add( Element("test").also {
		    it.addAttr("kind", "pre")
		    it.addAttr("ord", "2")
		    it.addAttr("text", (guimap["pret2"] as JTextField?)!!.text)
		})
		it.add( Element("test").also {
		    it.addAttr("kind", "post")
		    it.addAttr("ord", "1")
		    it.addAttr("text", (guimap["postt1"] as JTextField?)!!.text)
		})
		it.add( Element("test").also {
		    it.addAttr("kind", "post")
		    it.addAttr("ord", "2")
		    it.addAttr("text", (guimap["postt2"] as JTextField?)!!.text)
		})
	    }
	}
}
