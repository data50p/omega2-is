package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseSpeechFile : JFileChooser(File("media")) {
    init {
	val fi = ExtensionFileFilter(arrayOf("wav", "mp3"))
	fileFilter = fi
	approveButtonText = t("Select")
    }
}
