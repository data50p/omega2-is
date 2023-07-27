package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseLessonFile : JFileChooser {
    constructor() : super(File(omegaAssets("."))) {
	val fi = ExtensionFileFilter()
	fi.addExtension(ext)
	fileFilter = fi
    }

    constructor(dirStep: Int) : super(File(getDir(dirStep))) {
	val fi = ExtensionFileFilter()
	fi.addExtension(ext)
	fileFilter = fi
    }

    fun setLastFile(lastFile: File) {
	OmegaContext.sout_log.getLogger().info("getDir setLastFile " + lastFile + " -> " + Companion.lastFile)
	Companion.lastFile = lastFile
    }

    companion object {
	@JvmField
	var ext = "omega_lesson"
	private var lastFile: File? = null
	private fun getDir(dirStep: Int): String? {
	    var dirStep = dirStep
	    OmegaContext.sout_log.getLogger().info("getDir " + dirStep + ' ' + lastFile)
	    if (dirStep == -1) return omegaAssets(".")
	    if (lastFile == null) return omegaAssets(".")
	    var f = lastFile
	    OmegaContext.sout_log.getLogger().info("getDir file $f")
	    while (dirStep-- > 0) {
		f = f!!.parentFile
		OmegaContext.sout_log.getLogger().info("getDir parent $f")
	    }
	    OmegaContext.sout_log.getLogger().info("getDir return " + f!!.path)
	    return f.path
	}
    }
}
