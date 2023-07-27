package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseAnimatorFile : JFileChooser(File(omegaAssets("anim"))) {
    init {
	val fi = ExtensionFileFilter()
	fi.addExtension(ext)
	fileFilter = fi
    }

    companion object {
	var ext = "omega_anim"
    }
}
