package com.femtioprocent.omega.anim.appl

import com.femtioprocent.omega.swing.ToolBar

abstract class ToolBar_Base : ToolBar {
    internal constructor(o: Any?) : super() {
	init(o)
    }

    internal constructor(o: Any?, orientation: Int) : super(orientation) {
	init(o)
    }

    protected abstract fun init(o: Any?)

    fun populate(id: String? = "default") {
    }

    open fun enable_path(mask: Int) {}
}
