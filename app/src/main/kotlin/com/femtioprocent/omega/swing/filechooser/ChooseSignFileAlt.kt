package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseSignFileAlt : JFileChooser(File(omegaAssets("media"))) {
    init {
	val fi = ExtensionFileFilter()
	fi.addExtension("mp4")
	fileFilter = fi
    }
}
