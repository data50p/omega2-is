package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

class ChooseActionMovieFile(omega_anim: Boolean) : JFileChooser(File(omegaAssets("anim"))) {
    init {
	val fi = ExtensionFileFilter()
	if (omega_anim) fi.addExtension("omega_anim")
	fi.addExtension("mpeg")
	fi.addExtension("mpg")
	fi.addExtension("mov")
	fi.addExtension("avi")
	fi.addExtension("MPEG")
	fi.addExtension("MPG")
	fi.addExtension("MOV")
	fi.addExtension("AVI")
	fi.addExtension("mp4")
	fi.addExtension("mpv")
	fi.addExtension("MP4")
	fi.addExtension("MPV")
	fileFilter = fi
    }
}
