package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.t9n.T.Companion.t
import java.io.File
import javax.swing.JFileChooser

class ChooseMovieDir : JFileChooser(File("media")) {
    init {
	fileSelectionMode = DIRECTORIES_ONLY
	approveButtonText = t("Select Directory")
    }
}
