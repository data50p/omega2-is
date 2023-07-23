package com.femtioprocent.omega.lesson.canvas.resultimport

import com.femtioprocent.omega.swing.GBC_Factory
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.border.BevelBorder


open class RegisterPanel internal constructor() : JPanel() {
    var gbcf = GBC_Factory()

    init {
	layout = GridBagLayout()
	populate()
	border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
    }

    open fun populate() {}
    override fun getInsets(): Insets {
	return Insets(5, 5, 5, 5)
    }

    fun dispose() {
	removeAll()
    }
}
