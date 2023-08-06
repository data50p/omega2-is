package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.value.Value
import com.femtioprocent.omega.value.Values
import java.awt.Color
import java.awt.Container
import java.awt.Graphics
import java.awt.GridBagLayout
import javax.swing.*
import javax.swing.text.Document

class TargetProperty internal constructor(owner: JFrame?) : Property_B(owner, t("Omega - Target Property")) {
    var guimap = HashMap<String, JComponent>()

    init {
	build(contentPane)
	pack()
	isVisible = true
    }

    fun refresh() {}
    fun build(con: Container) {
	con.layout = GridBagLayout()
	var jl: JLabel
	var tf: JTextField
	var cb: JComboBox<String>
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
	con.add(JLabel("Text").also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JTextField("Text", 20).also { tf = it }, gbcf.createL(X++, Y, 1))
	tf.document.addDocumentListener(mydocl)
	tf.isEnabled = false
	guimap["text"] = tf
	guimap["Ltext"] = jl
	Y++
	X = 0
	con.add(JLabel(t("Slot id")).also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JTextField("Text", 20).also { tf = it }, gbcf.createL(X++, Y, 1))
	tf.document.addDocumentListener(mydocl)
	guimap["tid"] = tf
	guimap["Ltid"] = jl
	Y++
	X = 0
	con.add(JLabel(t("Path id")).also { jl = it }, gbcf.createL(X++, Y, 1))
	con.add(JTextField("Text", 20).also { tf = it }, gbcf.createL(X++, Y, 1))
	tf.document.addDocumentListener(mydocl)
	guimap["lid"] = tf
	guimap["Llid"] = jl
	con.add(JComboBox<String>().also { cb = it }, gbcf.createL(X++, Y, 1))
	guimap["Slid"] = cb
	cb.addItem(t("(Select in list)"))
	cb.addItem(t("(Clear data)"))
	cb.addItemListener(myiteml)

// 	Y++;
// 	 X = 0;
// 	con.add(jl = new JLabel(T.t("Type")),                 gbcf.createL(X++, Y, 1));
// 	con.add(cb = new JComboBox(),     gbcf.createL(X++, Y, 1));
// //	con.add(tf = new JTextField("Text", 20),    gbcf.createL(X++, Y, 1));
// //	tf.getDocument().addDocumentListener(mydocl);
// 	cb.addItemListener(myiteml);
// 	cb.addItem("passive");
// 	cb.addItem("action");
// 	guimap.put("type", cb);
// 	guimap.put("Ltype", jl);
    }

    fun updValues(vs: Values) {
	run label@ {
	    val it = vs.hm.values.forEach { v ->
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "V " + v);
		if (v!!.id == "pathlist") {         // banor
		    val cb = guimap["Slid"] as JComboBox<String>?
		    val ss = v.str
		    if (ss != null) {
			val sa = split(ss, ",")
			cb!!.removeAllItems()
			cb.addItem(t("(Select in list)"))
			cb.addItem(t("(Clear data)"))
			for (i in sa.indices) cb.addItem("" + sa[i])
			//  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("+ " + sa[i]);
			cb.selectedIndex = 0
			pack()
		    } else {
			OmegaContext.sout_log.getLogger().info(":--: ss is null $v")
		    }
		} else {
		    var gui: Any? = guimap[v.id]
		    if (gui is JTextField) {
			gui.text = v.str
		    }
		    if (gui is JComboBox<*>) {
			gui.selectedItem = v.str
		    }
		    gui = guimap["L" + v.id]
		    if (gui is JLabel) {
			val jl = gui
			//		jl.setText(v.getId());
		    }
		}
	    }
	}
    }

    override fun updTrigger(doc: Document) {
	guimap.keys.forEach {key ->
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
	    var cbg: JComboBox<*>?
	    cbg = guimap["type"] as JComboBox<*>?
	    if (cb === cbg) {
		val s = cb!!.selectedItem as String
		//log  		OmegaContext.sout_log.getLogger().info(":--: " + "CB type " + cb);
		/*
                  if ( s.equals("action") )
  		    setLabel("Llid", "Path id");
  		if ( s.equals("actor") )
    		    setLabel("Llid", "Path id");
  		else
  		    setLabel("Llid", "Path id");
*/
//log		OmegaContext.sout_log.getLogger().info(":--: " + "=0= " + cb);
		fireValueChanged(Value("type", s))
	    }
	    cbg = guimap["Slid"] as JComboBox<*>?
	    if (cb === cbg) {
		val tf = guimap["lid"] as JTextField?
		updTF(tf!!, cbg)
	    }
	} catch (ex: ClassCastException) {
	    OmegaContext.sout_log.getLogger().info("ERR: CCE $ex")
	}
    }
}
