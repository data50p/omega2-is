package com.femtioprocent.omega.anim.panels.path

import com.femtioprocent.omega.anim.tool.path.Path
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.swing.properties.OmegaProperties
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Log
import java.awt.Color
import java.awt.Container
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class PathProperties(owner: JFrame?) : OmegaProperties(owner), ActionListener {
    var bound_pa: Path? = null
    var con: Container? = null
    var c = GBC_Factory()
    val pA: Path
	get() = obj as Path

    override fun refresh() {
	val pa = pA
	if (pa != null) {
	    if (buildProperties(pa)) {
		val jb = JButton(t("Close"))
		jb.actionCommand = "Close"
		jb.addActionListener(this)
		con!!.add(JPanel().add(jb))
	    }
	    pack()
	} else {
	    Log.getLogger().info(":--: " + "pa null")
	}
    }

    var c_pa_nid: JLabel? = null
    var c_pa_len: JLabel? = null
    var c_pa_imname: JTextField? = null

    init {
	title = "Properties"
    }

    private fun buildProperties(pa: Path): Boolean {
	var ret = false
	bound_pa = pa
	if (con == null) {
	    con = contentPane
	    updAll(pa)
	    con!!.setLayout(BoxLayout(con, BoxLayout.Y_AXIS))
	    con!!.add(JLabel(t("Path for timeline") + ' ' + pa.nid).also { c_pa_nid = it })
	    c_pa_nid!!.foreground = Color.black
	    con!!.add(JLabel(t("length") + " = " + pa.length).also { c_pa_len = it })
	    val pp = JPanel()
	    pp.layout = GridBagLayout()
	    var row = 0
	    pp.add(JLabel(t("Actor Image Name")), c.create(0, row))
	    pp.add(JTextField().also { c_pa_imname = it }, c.create(1, row))
	    val jb = JButton("...")
	    pp.add(jb, c.create(2, row))
	    row++
	    con!!.add(pp)
	    ret = true
	}
	c_pa_nid!!.text = t("Path for timeline") + ' ' + pa.nid
	c_pa_len!!.text = t("length = ") + pa.length
	c_pa_imname!!.text = t("--unknown--")
	return ret
    }

    fun updAll(pa: Path?) {}
    override fun actionPerformed(ev: ActionEvent) {
	if (ev.actionCommand == "Close") {
	    isVisible = false
	    return
	}
	if (bound_pa != null) {
	    if (ev.actionCommand == "comboBoxChanged") {
		updAll(bound_pa)
		return
	    } else {
		val o = ev.source
		if (o is JTextField) {
		    val fname = ev.actionCommand
		    updAll(bound_pa)
		    return
		}
	    }
	}
    }
}
