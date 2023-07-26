package com.femtioprocent.omega.swing.properties

import java.awt.Dialog
import javax.swing.JDialog
import javax.swing.JFrame

open class OmegaProperties : JDialog {
    protected var obj: Any? = null

    constructor(owner: JFrame?) : super(owner)
    constructor(owner: JFrame?, title: String?) : super(owner, title)
    constructor(owner: JFrame?, title: String?, modal: Boolean) : super(owner, title, modal)
    constructor(owner: Dialog?) : super(owner)
    constructor(owner: Dialog?, title: String?) : super(owner, title)
    constructor(owner: Dialog?, title: String?, modal: Boolean) : super(owner, title, modal)

    fun setObject(obj: Any?) {
	this.obj = obj
	refresh()
    }

    open fun refresh() {}
}
