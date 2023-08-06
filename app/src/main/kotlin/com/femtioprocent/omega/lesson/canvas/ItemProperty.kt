package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.antiOmegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.lessonLang
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.lesson.appl.LessonEditor.Companion.setDirty
import com.femtioprocent.omega.lesson.repository.Locator
import com.femtioprocent.omega.swing.filechooser.ChooseAnimatorFile
import com.femtioprocent.omega.swing.filechooser.ChooseAudioFile
import com.femtioprocent.omega.swing.filechooser.ChooseSignFile
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files
import com.femtioprocent.omega.util.Files.mkRelFname
import com.femtioprocent.omega.util.Files.mkRelFnameAlt
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.value.Value
import com.femtioprocent.omega.value.Values
import org.hs.jfc.FormPanel
import java.awt.Color
import java.awt.Container
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*
import javax.swing.text.Document

class ItemProperty internal constructor(owner: JFrame?) : Property_B(owner, t("Omega - Item property")),
	ActionListener {
    var guimap: HashMap<String?, JComponent?> = HashMap()
    var skip_dirty = false
    var text_tf: JTextField? = null
    var fname_tf: JTextField? = null

    init {
	build(contentPane)
	pack()
	isVisible = true
    }

    fun toURL(file: File?): String? {
	return Files.toURL(file!!)
    }

    val fName: String?
	get() {
	    val fn: String? = null
	    try {
		var url_s: String? = null
		val choose_af = ChooseAnimatorFile()
		val rv = choose_af.showDialog(this, t("Select"))
		if (rv == JFileChooser.APPROVE_OPTION) {
		    val file = choose_af.selectedFile
		    url_s = toURL(file)
		    val aFname = mkRelFname(url_s!!)
		    val fname = antiOmegaAssets(aFname)
		    OmegaContext.sout_log.getLogger().info("getFName: (~A) $fname")
		    return fname
		}
	    } catch (ex: Exception) {
		OmegaContext.exc_log.getLogger().throwing(ItemProperty::class.java.name, "getFName", ex)
	    }
	    return null
	}

    fun refresh() {}
    internal inner class JLabelL(s: String?) : JLabel(s) {
	init {
	    horizontalTextPosition = RIGHT
	}
    }

    fun build(con: Container) {
	skip_dirty = true
	val WW = 35
	val fpan = FormPanel(5, 5, 7, 15)

	//	con.setLayout(new GridBagLayout());
	var cb: JComboBox<String>
	var jl: JLabel?
	var tf: JTextField
	var bt: JButton
	var Y = 0
	var X = 0
	fpan.add(JLabelL(t("Text")).also { jl = it }, JTextField("Text", WW).also { tf = it }, Y, ++X)
	text_tf = tf
	tf.document.addDocumentListener(mydocl)
	guimap["Ltext"] = jl
	guimap["text"] = tf
	Y++
	X = 0
	fpan.add(JLabelL(t("TTS")).also { jl = it }, JTextField("", WW).also { tf = it }, Y, ++X)
	text_tf = tf
	tf.document.addDocumentListener(mydocl)
	guimap["Ltts"] = jl
	guimap["tts"] = tf
	Y++
	X = 0
	fpan.add(JLabelL(t("Sound")).also { jl = it }, JTextField("Sound File", WW - 5).also { tf = it }, Y, ++X)
	tf.document.addDocumentListener(mydocl)
	guimap["Lsound"] = jl
	guimap["sound"] = tf
	fpan.add(JLabelL(t("")).also { jl = it }, JButton("...").also { bt = it }, Y, X)
	guimap["Bsound"] = bt
	bt.actionCommand = "sound_set"
	bt.addActionListener(this)
	Y++
	X = 0
	fpan.add(JLabelL(t("Sign")).also { jl = it }, JTextField("Sign File", WW - 5).also { tf = it }, Y, ++X)
	tf.document.addDocumentListener(mydocl)
	guimap["Lsign"] = jl
	guimap["sign"] = tf
	fpan.add(JLabelL(t("")).also { jl = it }, JButton("...").also { bt = it }, Y, X)
	guimap["Bsign"] = bt
	bt.actionCommand = "sign_set"
	bt.addActionListener(this)
	Y++
	X = 0
	fpan.add(JLabelL(t("(Post Test Dummy Text):")).also { jl = it }, JTextField("", WW).also { tf = it }, Y, ++X)
	tf.document.addDocumentListener(mydocl)
	tf.background = Color(220, 220, 220)
	guimap["Ldummytext"] = jl
	guimap["dummytext"] = tf
	Y++
	X = 0
	fpan.add(
		JLabelL(t("(Post Test Dummy Sound):")).also { jl = it },
		JTextField("", WW - 5).also { tf = it },
		Y,
		++X
	)
	tf.document.addDocumentListener(mydocl)
	tf.background = Color(220, 220, 220)
	guimap["Ldummysound"] = jl
	guimap["dummysound"] = tf
	fpan.add(JLabelL(t("")).also { jl = it }, JButton("...").also { bt = it }, Y, X)
	guimap["Bdummysound"] = bt
	bt.actionCommand = "dummysound_set"
	bt.addActionListener(this)
	Y++
	X = 0
	fpan.add(
		JLabelL(t("(Post Test Dummy Sign):")).also { jl = it },
		JTextField("", WW - 5).also { tf = it },
		Y,
		++X
	)
	tf.document.addDocumentListener(mydocl)
	tf.background = Color(220, 220, 220)
	guimap["Ldummysign"] = jl
	guimap["dummysign"] = tf
	fpan.add(JLabelL(t("")).also { jl = it }, JButton("...").also { bt = it }, Y, X)
	guimap["Bdummysign"] = bt
	bt.actionCommand = "dummysign_set"
	bt.addActionListener(this)
	Y++
	X = 0
	fpan.add(JLabelL(t("Slot id list")).also { jl = it }, JTextField("", WW).also { tf = it }, Y, ++X)
	tf.document.addDocumentListener(mydocl)
	guimap["Ltid"] = jl
	guimap["tid"] = tf
	Y++
	X = 0
	fpan.add(JLabelL(t("Actor id")).also { jl = it }, JTextField("", WW - 8).also { tf = it }, Y, ++X)
	tf.document.addDocumentListener(mydocl)
	guimap["Llid"] = jl
	guimap["lid"] = tf
	fpan.add(JLabelL(""), JComboBox<String>().also { cb = it }, Y, X)
	guimap["actors"] = cb
	cb.addItem("")
	cb.addItemListener(myiteml)
	Y++
	X = 0
	fpan.add(JLabelL(t("Action File")).also { jl = it }, JTextField("Action File", WW - 5).also { tf = it }, Y, ++X)
	fname_tf = tf
	tf.document.addDocumentListener(mydocl)
	guimap["fname"] = tf
	guimap["Lfname"] = jl
	fpan.add(JLabelL(""), JComboBox<String>().also { cb = it }, Y, X)
	cb.addItem(t("(Select in list)"))
	cb.addItem(t("<Select file...>"))
	val loc = Locator()
	var sa: Array<String?>? =
		loc.getAllActiveFiles(omegaAssets("lesson-" + lessonLang + "/active")!!, "omega_anim") // LESSON-DIR-A
	sa = antiOmegaAssets(sa)
	for (i in sa!!.indices) cb.addItem(sa[i])
	cb.addItemListener(myiteml)
	guimap["fnamelist"] = cb
	Y++
	X = 0
	var tf2: JTextField
	fpan.add(JLabelL(t("Variables")).also { jl = it }, JTextField("V1", 10).also { tf = it }, Y, ++X)
	tf.document.addDocumentListener(mydocl)
	guimap["v1"] = tf
	fpan.add(JTextField("V2", 10).also { tf = it }, JTextField("V3", 10).also { tf2 = it }, Y, X)
	tf.document.addDocumentListener(mydocl)
	guimap["v2"] = tf
	tf2.document.addDocumentListener(mydocl)
	guimap["v3"] = tf2
	fpan.add(JTextField("", 10).also { tf = it }, JTextField("", 10).also { tf2 = it }, Y, X)
	tf.document.addDocumentListener(mydocl)
	guimap["v4"] = tf
	tf2.document.addDocumentListener(mydocl)
	guimap["v5"] = tf2
	guimap["LvN"] = jl
	con.add(fpan)

// 	con.add(new JPanel() {
// 		public void paintComponent(Graphics g) {
// 		    super.paintComponent(g);
// 		    g.setColor(Color.blue);
// 		    g.fillRect(0, 0, 12, 12);
// 		    g.setColor(Color.magenta);
// 		    g.fillRect(12, 0, 12, 12);
// 		}
// 	    }, gbcf.createL(X++, Y, 1));
	skip_dirty = false
    }

    // when text changes
    override fun updTrigger(doc: Document) {
	guimap.keys.forEach {key ->
	    val o: Any? = guimap[key]
	    if (o is JTextField) {
		val tf = o
		if (doc === tf.document) {
		    val txt = tf.text
		    OmegaContext.sout_log.getLogger().info(":--: updTrigger: $txt $tf")
		    fireValueChanged(Value(key!!, txt))
		    if (!skip_dirty) setDirty()
		}
	    }
	}
    }

    override fun actionPerformed(ae: ActionEvent) {
	val cmd = ae.actionCommand
	if ("sound_set" == cmd) {
	    val choose_f = ChooseAudioFile()
	    val rv = choose_f.showDialog(null, t("Load"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		val url_s = Files.toURL(file)
		// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
		val fn = mkRelFnameAlt(url_s!!, "media")
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);
		val tf = guimap["sound"] as JTextField?
		tf!!.text = fn
		fireValueChanged(Value("sound", fn))
		if (!skip_dirty) setDirty()
	    }
	}
	if ("sign_set" == cmd) {
	    val choose_f = ChooseSignFile()
	    val rv = choose_f.showDialog(null, t("Load"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		val url_s = Files.toURL(file)
		// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
		val fn = mkRelFnameAlt(url_s!!, "media")
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);
		val tf = guimap["sign"] as JTextField?
		tf!!.text = fn
		fireValueChanged(Value("sign", fn))
		if (!skip_dirty) setDirty()
	    }
	}
	if ("dummysound_set" == cmd) {
	    val choose_f = ChooseAudioFile()
	    val rv = choose_f.showDialog(null, t("Load"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		val url_s = Files.toURL(file)
		// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
		val fn = mkRelFnameAlt(url_s!!, "media")
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);
		val tf = guimap["dummysound"] as JTextField?
		tf!!.text = fn
		fireValueChanged(Value("dummysound", fn))
		if (!skip_dirty) setDirty()
	    }
	}
	if ("dummysign_set" == cmd) {
	    val choose_f = ChooseSignFile()
	    val rv = choose_f.showDialog(null, t("Load"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		val url_s = Files.toURL(file)
		// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
		val fn = mkRelFnameAlt(url_s!!, "media")
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);
		val tf = guimap["dummysign"] as JTextField?
		tf!!.text = fn
		fireValueChanged(Value("dummysign", fn))
		if (!skip_dirty) setDirty()
	    }
	}
    }

    override fun updTrigger(cb: JComboBox<*>?) {
	try {
	    var cbg: JComboBox<*>?

//  	    cbg = (JComboBox)guimap.get("ftype");
//  	    if ( cb == cbg ) {
//  		String s = (String)cb.getSelectedItem();
//  	    }
	    cbg = guimap["actors"] as JComboBox<*>?
	    if (cb === cbg) {
		val tf = guimap["lid"] as JTextField?
		updTF(tf!!, cbg)
	    }
	    cbg = guimap["fnamelist"] as JComboBox<*>?
	    if (cb === cbg) {
		val s = cb!!.selectedItem as String
		val fn: String?
		fn = if (t("<Select file...>") == s) {
		    fName
		} else {
		    s
		}
		if (fn != null) {
		    val ix = fn.lastIndexOf('.')
		    if (ix != -1) {
			val s1 = fn.substring(0, ix)
			val s2 = fn.substring(ix + 1)
			val tf = guimap["fname"] as JTextField?
			tf!!.text = s1
			//			JComboBox cb2 = (JComboBox)guimap.get("ftype");
//			cb2.setSelectedItem(s2);
		    }
		}
		cb.selectedIndex = 0
	    }
	} catch (ex: ClassCastException) {
	    OmegaContext.sout_log.getLogger().info("ERR: CCE $ex")
	}
    }

    @Synchronized
    fun updValues(vs: Values) {
	skip_dirty = true
	val ct0 = ct()
	vs.hm.values.forEach {v ->
	    if (v.id == "actorlist") {
		val cb = guimap["actors"] as JComboBox<String>?
		val sc = v.str
		cb!!.removeAllItems()
		cb.addItem(t("(Select item in list)"))
		cb.addItem(t("(Clear data)"))
		if (sc != null) {
		    val sa = split(sc, ",")
		    for (i in sa.indices) cb.addItem("" + sa[i])
		    //  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("+ " + sa[i]);
		    cb.selectedIndex = 0
		    pack()
		    //  		    String[] sa = SundryUtils.split(sc, ",");
//  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("= " + sa[i]);
//  		    for(int i = 0; i < sa.length; i++)
//  			cb.addItem("+ " + sa[i]);
//  		    cb.setSelectedIndex(0);
//  		    pack();
		}
	    }
	    var gui: Any? = guimap[v.id]
	    if (gui is JTextField) {
//log		OmegaContext.sout_log.getLogger().info(":--: " + "VVV " + v);
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
	    //  	    if ( v.getId().equals("ftype") ) {
//  //		JComboBox cb = (JComboBox)guimap.get("ftype");
//  		JComboBox cb2 = (JComboBox)guimap.get("fnamelist");
//  		JTextField tf = (JTextField)guimap.get("fname");
//  		String s = v.getStr();
//  		if ( s.length() >= 0 ) {
//  //		    cb.setEnabled(true);
//  		    cb2.setEnabled(true);
//  		    tf.setEnabled(true);
//  		} else {
//  //		    cb.setEnabled(false);
//  		    cb2.setEnabled(false);
//  		    tf.setEnabled(false);
//  		}
//  	    }
	}
	val ct1 = ct()
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "iprop " + (ct1-ct0));
	skip_dirty = false
    }
}
