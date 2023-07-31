package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseImageFile : JFileChooser(File(omegaAssets("media"))) {
    init {
	val fi = ExtensionFileFilter(
	    arrayOf(
		"gif",
		"jpg",
		"jpeg",
		"png"
	    )
	)
	fileFilter = fi
	approveButtonText = t("Select")
    }
}
