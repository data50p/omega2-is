package com.femtioprocent.omega.swing.filechooser

import com.femtioprocent.omega.util.ExtensionFileFilter
import java.io.File
import javax.swing.JFileChooser

//import omega.lesson.test.*;
class ChooseStoryFile(dir: String?) : JFileChooser(File(dir)) {
    init {
	val fi = ExtensionFileFilter()
	fi.addExtension(ext)
	fileFilter = fi
    }

    companion object {
	var ext = "omega_story_replay"
    }
}
