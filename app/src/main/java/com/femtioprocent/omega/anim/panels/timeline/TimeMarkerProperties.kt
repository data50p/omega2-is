package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventFactory
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventSelections
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.swing.filechooser.ChooseAudioFile
import com.femtioprocent.omega.swing.properties.OmegaProperties
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files
import com.femtioprocent.omega.util.Log
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagLayout
import java.awt.Label
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.BadLocationException
import javax.swing.text.Document

class TimeMarkerProperties(var a_ctxt: AnimContext, owner: JFrame?) : OmegaProperties(owner), ActionListener {
    var bound_tm: TimeMarker? = null
    var event_panels: Array<TriggerEventPanel?>
    var j_lb = arrayOfNulls<JLabel>(7)
    var lesson_id: JTextField? = null
    private var skipDirty = false
    val tM: TimeMarker
	get() = obj as TimeMarker

    private fun setDirty() {
	if (skipDirty == false) AnimContext.ae!!.isDirty = true
    }

    override fun refresh() {
	skipDirty = true
	val tm = tM
	if (tm != null) {
	    buildProperties(tm)
	    pack()
	} else {
	    Log.getLogger().info(":--: " + "tm null")
	}
	skipDirty = false
    }

    inner class myDocumentListener : DocumentListener {
	override fun changedUpdate(de: DocumentEvent) {
	    val doc = de.document
	    updTrigger(doc)
	}

	override fun insertUpdate(de: DocumentEvent) {
	    val doc = de.document
	    updTrigger(doc)
	}

	override fun removeUpdate(de: DocumentEvent) {
	    val doc = de.document
	    updTrigger(doc)
	}
    }

    var mydocl: myDocumentListener = myDocumentListener()

    inner class myItemListener : ItemListener {
	override fun itemStateChanged(ie: ItemEvent) {
	    val cb = ie.itemSelectable as JComboBox<*>
	    updTrigger(cb)
	}
    }

    var myiteml: myItemListener = myItemListener()

    inner class OnOffItemEvent : ItemListener {
	override fun itemStateChanged(ie: ItemEvent) {
	    val cb = ie.itemSelectable as JCheckBox
	    updTriggerOnOff(cb)
	}
    }

    var onoff_listener = OnOffItemEvent()
    private var hm_onoff: HashMap<JCheckBox?, Int?> = HashMap()
    private var hm_doc: HashMap<Document?, Int?> = HashMap()
    private var hm_cb = HashMap<JComponent?, Int>()

    init {
	event_panels = arrayOfNulls(TriggerEventFactory.size)
	title = "Omega - " + t("Marker Properties")
	setSize(300, 200)
    }

    fun initCache() {
	hm_onoff = HashMap()
	hm_doc = HashMap()
	hm_cb = HashMap()
    }

    private fun buildProperties(tm: TimeMarker) {
	bound_tm = tm
	if (j_lb[0] == null) {
	    val con = contentPane
	    con.layout = BorderLayout()
	    val top_pan = JPanel()
	    val r_top_pan = JPanel()
	    val l_top_pan = JPanel()
	    top_pan.add(l_top_pan)
	    top_pan.add(r_top_pan)
	    con.add(top_pan, BorderLayout.NORTH)
	    r_top_pan.layout = BoxLayout(r_top_pan, BoxLayout.Y_AXIS)
	    var ix = 0
	    j_lb[ix] = JLabel(t("TimeMarker for timeline") + ' ')
	    j_lb[ix]!!.foreground = Color.black
	    j_lb[++ix] = JLabel(t("Ordinal = "))
	    j_lb[++ix] = JLabel(t("Ordinal(type) = "))
	    j_lb[++ix] = JLabel(t("When = "))
	    j_lb[++ix] = JLabel(t("Type = "))
	    j_lb[++ix] = JLabel(t("Duration = "))
	    for (i in 0 until ix) {
		r_top_pan.add(j_lb[i])
	    }
	    l_top_pan.add(Label(t("Path ID:")))
	    l_top_pan.add(JTextField("", 10).also { lesson_id = it })
	    val doc2 = lesson_id!!.document
	    doc2.addDocumentListener(mydocl)
	    val pp = JPanel()
	    pp.layout = GridBagLayout()
	    val c = GBC_Factory()
	    for (i in -1 until tm.t_event!!.size) {
		if (i == -1) {
		    var jlb: JLabel?
		    pp.add(JLabel(t("Event")).also { jlb = it }, c.create(0, 0))
		    pp.add(
			JLabel(t("Argument") + "               ").also { jlb = it },
			c.create(1, 0)
		    )
		    pp.add(JLabel(t("Note/Description")).also { jlb = it }, c.create(2, 0))
		    pp.add(JLabel(t("is On")).also { jlb = it }, c.create(3, 0))
		} else {
		    val te = tm.t_event!![i]!!
		    val tep: TriggerEventPanel = TriggerEventPanel1(te)
		    tep.cb!!.addItemListener(onoff_listener)
		    if (tep.cell_edit is JTextField) {
			val doc = (tep.cell_edit as JTextField?)!!.document
			doc.addDocumentListener(mydocl)
			hm_doc[doc] = i
		    }
		    if (tep.cell_edit is JComboBox<*>) {
			val cb = tep.cell_edit as JComboBox<String?>?
			val sel = te.selections_human
			for (ii in sel!!.indices) {
			    cb!!.addItem(sel[ii])
			}
			cb!!.addItemListener(myiteml)
			hm_cb[cb] = i
		    }
		    pp.add(tep.label, c.create(0, i + 1))
		    pp.add(tep.cell_edit, c.create(1, i + 1))
		    pp.add(tep.help, c.create(2, i + 1))
		    pp.add(tep.cb, c.create(3, i + 1))
		    var jb: JButton
		    var ext_list = te.files
		    if (ext_list != null) {
			pp.add(JButton(t("Set...")).also { jb = it }, c.create(4, i + 1))
			jb.actionCommand = "set_file"
			jb.addActionListener(this)
		    }
		    hm_onoff[tep.cb] = i
		    event_panels[i] = tep
		}
	    }
	    con.add(pp, BorderLayout.CENTER)
	    val jb = JButton(t("Close"))
	    jb.actionCommand = "Close"
	    jb.addActionListener(this)
	    val jpan = JPanel()
	    jpan.add(jb)
	    con.add(jpan, BorderLayout.SOUTH)
	}
	if (true) {
	    var ix = 0
	    j_lb[ix++]!!.text = t("TimeMarker for timeline ") + tm.tl.nid
	    j_lb[ix++]!!.text = t("Ordinal = ") + tm.ord
	    //	    j_lb[ix++].setText(T.t("Ordinal(type) = ") + tm.ord_same_type);
	    j_lb[ix++]!!.text = t("When = ") + tm.`when`
	    j_lb[ix++]!!.text = t("Type = ") + tm.type + ", " + tm.typeString(tm.type)
	    j_lb[ix++]!!.text = t("Duration = ") + tm.duration
	    lesson_id!!.text = tm.tl.lessonId
	    if ((tm.type == TimeMarker.TRIGGER || tm.type == TimeMarker.START || tm.type == TimeMarker.STOP || tm.type == TimeMarker.BEGIN || tm.type == TimeMarker.END) && tm.t_event != null) {
		for (i in -1 until tm.t_event!!.size) {
		    if (i == -1) {
		    } else {
			val te = tm.t_event!![i]!!
			val tep = event_panels[i]
			val `val` = te.argString_human
			if (te.hasSelections()) tep!!.setEC(`val`) else tep!!.setEC_TF(`val`)
			tep.cb!!.isSelected = te.is_on
			tep.help!!.isEditable = false
		    }
		}
	    }
	}
    }

    fun updTriggerOnOff(cb: JCheckBox?) {
	setDirty()
	val tm = bound_tm
	if (cb != null) {
	    val I = hm_onoff[cb] as Int
	    if (I != null) {
		tm!!.t_event!![I]!!.is_on = cb.isSelected
	    }
	}
	return
    }

    fun updTrigger(doc: Document) {
	setDirty()
	val tm = bound_tm ?: return
	if (lesson_id!!.document === doc) {
	    tm.tl.lessonId = lesson_id!!.text
	    val ae = AnimContext.ae
	    if (ae != null) {
		ae.tlp!!.repaint()
		ae.cabaret_panel!!.repaint()
	    }
	    return
	}
	if (tm != null && tm.t_event != null) for (i in tm.t_event!!.indices) {
	    val te = tm.t_event!![i]
	    if (te != null) {
		if (!te.hasSelections()) {
		    val I = hm_doc[doc]
		    if (I != null) {
			if (I == i) {
			    try {
				te.arg = doc.getText(0, doc.length)
			    } catch (ex: BadLocationException) {
				Log.getLogger().info("ERR: $ex")
			    }
			}
		    }
		}
	    }
	}
    }

    fun updTrigger(cb: JComboBox<*>?) {
	if (cb == null) {
	    return
	}
	setDirty()
	val tm = bound_tm
	if (tm != null && tm.t_event != null) for (i in tm.t_event!!.indices) {
	    val te = tm.t_event!![i]
	    if (te != null) {
		if (te.hasSelections()) {
		    val tes = te as TriggerEventSelections
		    val I = hm_cb[cb]
		    if (I != null) {
			if (I == i) {
			    tes.setArgFromHuman((cb.selectedItem as String))
			}
		    }
		}
	    }
	}
    }

    override fun actionPerformed(ev: ActionEvent) {
	if (ev.actionCommand == "set_file") {
	    val choose_f = ChooseAudioFile()
	    var url_s: String? = null
	    val rv = choose_f.showDialog(null, t("Load"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		url_s = Files.toURL(file)
		// 		if ( ! url_s.endsWith("." + ChooseAudioFile.ext) )
// 		    url_s = url_s + "." + ChooseAudioFile.ext;
//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + url_s);
		val fn = Files.mkRelFnameAlt(url_s!!, "media")
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "FILE " + fn);
		val te = bound_tm!!.findTEvent("PlaySound")
		te!!.arg = fn
		refresh()
	    }
	}
	if (ev.actionCommand == "Close") {
	    isVisible = false
	    return
	}
    }
}
