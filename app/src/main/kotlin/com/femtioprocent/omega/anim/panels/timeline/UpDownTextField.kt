package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.swing.OmegaSwingUtils
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class UpDownTextField internal constructor(label: String?, def: String?) : JPanel() {
    var l: JLabel
    var tf: JTextField
    var up: JButton
    var down: JButton
    private val def = 0
    private val min = 0
    private val max = 0

    init {
	l = JLabel(label)
	tf = JTextField(def, 4)
	tf.isEditable = false
	up = JButton(OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/navigation/Up16.gif"))
	down = JButton(OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/navigation/Down16.gif"))
	up.preferredSize = Dimension(16, 12)
	down.preferredSize = Dimension(16, 12)
	val pl = JPanel()
	val pr = JPanel()
	pr.layout = GridLayout(0, 1)
	pr.add(up)
	pr.add(down)
	pl.add(l)
	pl.add(tf)
	add(pl)
	add(pr)
    }

    fun set(s: String?) {
	tf.text = s
    }

    fun addActionListener(al: ActionListener?) {
	up.addActionListener(al)
	down.addActionListener(al)
    }
}
