package com.femtioprocent.omega.lesson.settings

import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

open class SettingsDialog(title: String?) : JDialog(ApplContext.top_frame, title, true), ActionListener {
    var jpan = JPanel()
    private val gbcf = GBC_Factory()
    private var X = 0
    private var Y = 0
    fun addb(txt: String?, cmd: String?) {
	val b = JButton(txt)
	b.actionCommand = cmd
	b.addActionListener(this)
	jpan.add(b)
    }

    fun addtf(jp: JPanel, txt: String?): JTextField {
	val tf = JTextField(20)
	val l = JLabel(txt)
	l.labelFor = tf
	jp.add(l, gbcf.createL(X++, Y, 1))
	jp.add(tf, gbcf.createL(X++, Y, 1))
	X = 0
	Y++
	return tf
    }

    fun addcb(jp: JPanel, txt: String?, sa: Array<String?>): JComboBox<*> {
	val cb: JComboBox<String?> = JComboBox()
	val l = JLabel(txt)
	l.labelFor = cb
	jp.add(l, gbcf.createL(X++, Y, 1))
	jp.add(cb, gbcf.createL(X++, Y, 1))
	for (i in sa.indices) cb.addItem(sa[i])
	X = 0
	Y++
	return cb
    }

    fun addchb(jp: JPanel, txt: String?): JCheckBox {
	val chb = JCheckBox()
	val l = JLabel(txt)
	l.labelFor = chb
	jp.add(l, gbcf.createL(X++, Y, 1))
	jp.add(chb, gbcf.createL(X++, Y, 1))
	X = 0
	Y++
	return chb
    }

    fun populateCommon() {
	val c = contentPane
	addb(t("OK"), "OK")
	addb(t("Cancel"), "cancel")
	c.add(jpan, BorderLayout.SOUTH)
    }

    open fun save(): Boolean {
	return false
    }

    open fun load() {}
    override fun actionPerformed(e: ActionEvent) {
	val cmd = e.actionCommand
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + cmd);
	if ("OK" == cmd) {
	    if (save() == false) JOptionPane.showMessageDialog(
		ApplContext.top_frame,
		"Can't save"
	    )
	    isVisible = false
	}
	if ("cancel" == cmd) {
	    isVisible = false
	}
    }
}
