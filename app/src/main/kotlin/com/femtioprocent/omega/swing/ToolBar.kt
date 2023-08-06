package com.femtioprocent.omega.swing

import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JToolBar

open class ToolBar : JToolBar {
    constructor()
    constructor(orientation: Int) : super(orientation)

    fun add(comp: JComponent): JComponent {
	super.add(comp)
	comp.alignmentY = 0.1f
	comp.alignmentX = 0.5f
	return comp
    }

    fun add(ta: ToolAction): JButton {
	val jb = super.add(ta)
	jb.actionCommand = ta.command
	jb.alignmentY = 0.1f
	jb.alignmentX = 0.5f
	return jb
    }

    fun add(tbg: ToolActionGroup, texec: ToolExecute?): Array<JButton?> {
	val jba = arrayOfNulls<JButton>(tbg.size())
	var ix = 0
	val it = tbg.li.forEach {ta ->
	    if (ta!!.command == null) addSeparator() else {
		ta.texec = texec
		val jb = add(ta)
		jb.actionCommand = ta.command
		jb.alignmentY = 0.1f
		jb.alignmentX = 0.5f
		jba[ix++] = jb
	    }
	}
	return jba
    }
}
