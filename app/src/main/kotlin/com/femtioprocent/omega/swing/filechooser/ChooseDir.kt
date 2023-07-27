package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

/**
 * Created by lars on 2017-02-19.
 */
class ChooseDir : JFileChooser(File(omegaAssets("."))) {
    init {
	val fi: FileFilter = object : FileFilter() {
	    override fun accept(f: File): Boolean {
		return f.isDirectory
	    }

	    override fun getDescription(): String {
		return "ChooseDir"
	    }
	}
	fileFilter = fi
	fileSelectionMode = DIRECTORIES_ONLY
    }
}
