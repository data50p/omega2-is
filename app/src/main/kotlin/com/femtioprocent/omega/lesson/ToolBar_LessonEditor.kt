package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.swing.ToolBar
import com.femtioprocent.omega.swing.ToolExecute
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class ToolBar_LessonEditor : ToolBar, ActionListener {
    var texec: ToolExecute

    constructor(texec: ToolExecute) {
	this.texec = texec
    }

    constructor(texec: ToolExecute, orientation: Int) : super(orientation) {
	this.texec = texec
    }

    fun populateRest() {}
    override fun actionPerformed(ae: ActionEvent) {}
}
