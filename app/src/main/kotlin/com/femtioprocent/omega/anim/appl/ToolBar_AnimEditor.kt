package com.femtioprocent.omega.anim.appl

import com.femtioprocent.omega.swing.ToolExecute
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.CardLayout
import java.awt.Container
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class ToolBar_AnimEditor : ToolBar_Base, ActionListener {
    var texec: ToolExecute? = null

    internal constructor(texec: ToolExecute?) : super(texec)
    internal constructor(texec: ToolExecute?, orientation: Int) : super(texec, orientation)

    override fun init(o: Any?) {
	texec = o as ToolExecute?
    }

    fun populateRest() {
//	addSeparator();
	val jp: JPanel = JPanel()
	jp.setLayout(BoxLayout(jp, BoxLayout.Y_AXIS))
	var b: JButton
	jp.add(JButton(t("Actor")).also({ b = it }))
	b.actionCommand = "actor"
	b.addActionListener(this)
	jp.add(JButton(t("Wings")).also({ b = it }))
	b.actionCommand = "wings"
	b.addActionListener(this)
	add(jp)
	jp.add(JButton(t("Prop...")).also({ b = it }))
	b.actionCommand = "prop"
	b.addActionListener(this)
	add(jp)
    }

    override fun enable_path(mask: Int) {
	val ba: BooleanArray = msk_bool.get(mask)
	for (i in s.indices) {
	    val ss: String = s.get(i)
	}
    }

    var card_pan: Container? = null
    var card: CardLayout? = null
    var who_act: Boolean = true
    override fun actionPerformed(ae: ActionEvent) {
	if ((ae.actionCommand == "actor")) {
	    card!!.show(card_pan, "actor")
	    who_act = true
	    texec!!.execute("prop_act_show")
	} else if ((ae.actionCommand == "wings")) {
	    card!!.show(card_pan, "wings")
	    who_act = false
	    texec!!.execute("prop_wing_show")
	} else if ((ae.actionCommand == "prop")) {
	    texec!!.execute(if (who_act) "prop_act" else "prop_wing")
	}
    }

    companion object {
	private val msk_bool: Array<BooleanArray> = arrayOf(
	    booleanArrayOf(true, false, false, false),
	    booleanArrayOf(true, true, true, true),
	    booleanArrayOf(true, false, true, true)
	)
	private val s: Array<String> = arrayOf(
	    "path_create",
	    "path_extend",
	    "path_split",
	    "path_delete"
	)
    }
}
