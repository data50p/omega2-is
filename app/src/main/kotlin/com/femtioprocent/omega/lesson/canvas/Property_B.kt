package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.value.Value
import com.femtioprocent.omega.value.ValuesListener
import java.awt.Dialog
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.EventListenerList
import javax.swing.text.Document

open class Property_B : JDialog {
    var lc_listeners: EventListenerList
    var gbcf = GBC_Factory()

    internal constructor(owner: JFrame?, title: String?) : super(owner, title) {
	lc_listeners = EventListenerList()
    }

    internal constructor(owner: JFrame?, title: String?, modal: Boolean) : super(owner, title, modal) {
	lc_listeners = EventListenerList()
    }

    internal constructor(owner: Dialog?, title: String?, modal: Boolean) : super(owner, title, modal) {
	lc_listeners = EventListenerList()
    }

    fun addValuesListener(l: ValuesListener) {
	lc_listeners.add(ValuesListener::class.java, l)
    }

    fun removeValuesListener(l: ValuesListener) {
	lc_listeners.remove(ValuesListener::class.java, l)
    }

    fun fireValueChanged(v: Value?) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "fireValueChanged " + v);
	val lia = lc_listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as ValuesListener).changed(v)
	    i += 2
	}
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
	    if (ie.stateChange == ItemEvent.SELECTED) updTrigger(cb)
	}
    }

    var myiteml: myItemListener = myItemListener()
    open fun updTrigger(doc: Document) {}
    open fun updTrigger(cb: JComboBox<*>?) {}
    fun updTF(tf: JTextField, cb: JComboBox<*>?) {
	if (false) {
	    val s = cb!!.selectedItem as String
	    if (s != null && s.length > 0) {
		val sa = split(s, " ")
		if (sa[0] == "+") {
		    val ss = tf.text
		    if (ss.length > 0) tf.text = ss + ',' + sa[1] else tf.text = sa[1]
		} else if (sa[0] == "=") {
		    tf.text = sa[1]
		}
	    }
	} else {
	    val ix = cb!!.selectedIndex
	    if (ix == 0) return
	    if (ix == 1) {
		tf.text = ""
		cb.selectedIndex = 0
		return
	    }
	    val s = cb.selectedItem as String
	    if (s != null && s.length > 0) {
		val ss = tf.text
		if (ss.length > 0) tf.text = "$ss,$s" else tf.text = s
		cb.selectedIndex = 0
	    }
	}
    }
}
