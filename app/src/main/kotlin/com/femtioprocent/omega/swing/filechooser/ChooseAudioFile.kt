package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseAudioFile : JFileChooser(File(omegaAssets("media/audio"))) {
    init {
	val fi = ExtensionFileFilter()
	fi.addExtension("mp3")
	fi.addExtension("wav")
	fileFilter = fi
    }
}
