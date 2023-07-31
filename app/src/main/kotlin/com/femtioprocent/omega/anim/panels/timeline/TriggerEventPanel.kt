package com.femtioprocent.omega.anim.panels.timeline

import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

open class TriggerEventPanel : JPanel() {
    var label: JTextField? = null
    var tf: JTextField? = null
    var help: JTextField? = null
    var cb: JCheckBox? = null
    var cell_edit: JComponent? = null
    open fun setEC(o: Any?) {}
    open fun setEC_TF(s: String?) {}
    open fun setArg(s: String?) {}
}
