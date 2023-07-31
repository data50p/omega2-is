package com.femtioprocent.omega.swing

import java.awt.Color
import javax.swing.JColorChooser

object ColorChooser {
    fun select(col: Color?): Color {
	return JColorChooser.showDialog(null, "Select color", col)
    }
}
