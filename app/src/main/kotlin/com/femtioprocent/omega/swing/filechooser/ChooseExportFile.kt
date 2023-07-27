package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseExportFile : JFileChooser(File(".")) {
    init {
	val fi = ExtensionFileFilter()
	fi.addExtension(ext)
	fileFilter = fi
    }

    companion object {
	var ext = "omega_export"
    }
}
