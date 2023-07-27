package com.femtioprocent.omega.swing

import java.awt.Component
import java.awt.event.ActionListener
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener

class Popup(var comp: Component) : PopupMenuListener {
    var jpop: JPopupMenu? = null
    fun popup(title: String?, sa: Array<String>, x: Int, y: Int, al: ActionListener?) {
	jpop = JPopupMenu(title)
	jpop!!.addPopupMenuListener(this)
	for (i in sa.indices) {
	    if ("" == sa[i]) {
		jpop!!.addSeparator()
	    } else {
		val mi = JMenuItem(sa[i])
		mi.actionCommand = "" + i
		mi.addActionListener(al)
		jpop!!.add(mi)
	    }
	}
	jpop!!.show(comp, x, y)
    }

    override fun popupMenuCanceled(ev: PopupMenuEvent) {}
    override fun popupMenuWillBecomeInvisible(ev: PopupMenuEvent) {}
    override fun popupMenuWillBecomeVisible(ev: PopupMenuEvent) {}
}
