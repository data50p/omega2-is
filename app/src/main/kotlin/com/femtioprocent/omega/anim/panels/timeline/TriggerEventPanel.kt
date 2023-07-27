package com.femtioprocent.omega.anim.panels.timeline

import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

open class TriggerEventPanel : JPanel() {
    @JvmField
    var label: JTextField? = null
    var tf: JTextField? = null
    @JvmField
    var help: JTextField? = null
    @JvmField
    var cb: JCheckBox? = null
    @JvmField
    var cell_edit: JComponent? = null
    open fun setEC(o: Any?) {}
    open fun setEC_TF(s: String?) {}
    open fun setArg(s: String?) {}
}
