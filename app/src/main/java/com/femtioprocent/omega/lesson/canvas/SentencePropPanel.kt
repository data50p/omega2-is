package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

@Deprecated("")
internal class SentencePropPanel private constructor(var sprop: SentenceProperty) : JPanel() {
    var guimap: HashMap<String, JComponent>
    var gbcf = GBC_Factory()

    internal inner class myActionListener : ActionListener {
	override fun actionPerformed(ev: ActionEvent) {
	    val s = ev.actionCommand
	    if (s == "dep_set action") {
	    }
	}
    }

    var myactl: myActionListener = myActionListener()

    init {
	guimap = sprop.guimap
	build()
    }

    fun build() {
	layout = GridBagLayout()
	var jl: JLabel
	var tf: JTextField
	var cb: JComboBox<*>
	var ch: JCheckBox
	var jb: JButton
	var Y = 0
	var X = 0
	var rb1: JRadioButton?
	var rb2: JRadioButton?
	val bgr = ButtonGroup()
	add(JRadioButton(t("Default, as dep_set in word prop")).also { rb1 = it }, gbcf.createL(X++, Y, 1))
	bgr.add(rb1)
	Y++
	X = 0
	add(JRadioButton(t("Specific")).also { rb2 = it }, gbcf.createL(X++, Y, 1))
	bgr.add(rb2)
	add(JButton(t("Set action file")).also { jb = it }, gbcf.createL(X++, Y, 1))
	guimap["dep_set action file"] = jb
	jb.actionCommand = "dep_set action file"
	jb.addActionListener(myactl)
    }
}
