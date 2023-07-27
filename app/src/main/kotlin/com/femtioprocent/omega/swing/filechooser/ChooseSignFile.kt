package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseSignFile : JFileChooser(File(omegaAssets("media/sign"))) {
    init {
	val fi = ExtensionFileFilter()
	fi.addExtension("mpg")
	fileFilter = fi
    }
}
