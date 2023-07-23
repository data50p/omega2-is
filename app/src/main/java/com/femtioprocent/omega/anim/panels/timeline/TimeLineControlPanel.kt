package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.anim.tool.timeline.TimeLinePlayer
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

class TimeLineControlPanel(var tlp: TimeLinePanel) : JPanel(), ActionListener {
    var scale: UpDownTextField
    var speed: UpDownTextField
    var xxx_b: JCheckBox

    init {
	val p = JPanel()
	layout = BorderLayout()
	p.layout = GridLayout(0, 1)
	p.background = Color(180, 180, 180)
	scale = UpDownTextField(t("Scale"), "1")
	scale.addActionListener(this)
	p.add(scale)
	speed = UpDownTextField(t("Speed"), "1.0")
	speed.addActionListener(this)
	p.add(speed)
	val b = JButton(t("Prop..."))
	b.actionCommand = "prop"
	b.addActionListener(this)
	p.add(b)
	xxx_b = JCheckBox(t("lock"))
	//	p.add(xxx_b);
	xxx_b.addActionListener(this)
	add(p, BorderLayout.WEST)
    }

    override fun actionPerformed(ev: ActionEvent) {
	if ("prop" == ev.actionCommand) {
	    tlp.popupProp()
	    return
	}
	if (ev.source === scale.up) {
	    if (tlp.scale < 10) tlp.scale += 1 else tlp.scale += 10
	    scale.set("" + tlp.scale / 10.0)
	    tlp.repaint()
	}
	if (ev.source === scale.down) {
	    if (tlp.scale > 10) tlp.scale -= 10 else if (tlp.scale > 2) tlp.scale -= 1
	    scale.set("" + tlp.scale / 10.0)
	    tlp.repaint()
	}
	if (ev.source === xxx_b) {
	    tlp.lock = xxx_b.isSelected
	}
	if (ev.source === speed.down) {
	    if (!TimeLinePlayer.getDefaultTimeLinePlayer_().adjustSpeed(0.8)) {
		speed.down.isEnabled = false
		speed.up.isEnabled = true
	    } else {
		speed.down.isEnabled = true
		speed.up.isEnabled = true
	    }
	    speed.set("" + TimeLinePlayer.getDefaultTimeLinePlayer_().speed)
	}
	if (ev.source === speed.up) {
	    if (!TimeLinePlayer.getDefaultTimeLinePlayer_().adjustSpeed(1.0 / 0.8)) {
		speed.down.isEnabled = true
		speed.up.isEnabled = false
	    } else {
		speed.down.isEnabled = true
		speed.up.isEnabled = true
	    }
	    speed.set("" + TimeLinePlayer.getDefaultTimeLinePlayer_().speed)
	}
    }

    fun setLock(b: Boolean) {
	xxx_b.isSelected = b
	tlp.lock = xxx_b.isSelected
    }
}
