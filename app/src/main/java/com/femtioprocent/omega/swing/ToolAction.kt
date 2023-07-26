package com.femtioprocent.omega.swing

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Icon

class ToolAction : AbstractAction {
    var command: String?
    @JvmField
    var texec: ToolExecute? = null

    constructor(text: String?, cmd: String?, texec: ToolExecute?) : super(text) {
	command = cmd
	this.texec = texec
    }

    constructor(text: String?, icons: String?, cmd: String?, texec: ToolExecute?) : super(
	text,
	OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/" + icons + "24.gif")
    ) {
	command = cmd
	this.texec = texec
    }

    constructor(text: String?, icons: String, cmd: String?, texec: ToolExecute?, _b: Boolean) : super(
	text,
	OmegaSwingUtils.getImageIcon("toolbarButtonGraphics/" + icons + "24.png")
    ) {
	command = cmd
	this.texec = texec
    }

    constructor() {
	command = null
    }

    override fun actionPerformed(ae: ActionEvent) {
	if (texec != null) texec!!.execute(command)
    }

    val icon: Icon
	get() = getValue(SMALL_ICON) as Icon
}
