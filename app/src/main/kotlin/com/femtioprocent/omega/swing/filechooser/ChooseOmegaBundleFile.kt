package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseOmegaBundleFile : JFileChooser(File(omegaAssets(".."))) {
    init {
	val fi = ExtensionFileFilter()
	fi.addExtension(OmegaConfig.OMEGA_BUNDLE)
	fileFilter = fi
    }
}
