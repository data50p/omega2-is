package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChoosePupilImageFile : JFileChooser(File("media")) {
    init {
	val fi = ExtensionFileFilter(arrayOf("jpg"))
	fileFilter = fi
	approveButtonText = t("Select")
    }
}
