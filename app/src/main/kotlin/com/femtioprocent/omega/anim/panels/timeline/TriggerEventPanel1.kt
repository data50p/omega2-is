package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.anim.tool.timeline.TriggerEvent
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JTextField

class TriggerEventPanel1 internal constructor(te: TriggerEvent) : TriggerEventPanel() {
    init {
	label = JTextField(te.cmdLabel)
	label!!.isEditable = false
	cell_edit = if (te.hasSelections()) JComboBox<Any?>() else JTextField()
	help = JTextField(te.help)
	cb = JCheckBox()
    }

    override fun setEC(o: Any?) {
	(cell_edit as JComboBox<*>).selectedItem = o
    }

    override fun setEC_TF(s: String?) {
	(cell_edit as JTextField).text = s
    }

    override fun setArg(s: String?) {
	tf!!.text = s
    }
}
