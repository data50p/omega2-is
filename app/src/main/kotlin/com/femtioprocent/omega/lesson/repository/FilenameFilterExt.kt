package com.femtioprocent.omega.lesson.repository

import java.io.File
import java.io.FilenameFilter

class FilenameFilterExt internal constructor(var ext: String) : FilenameFilter {
    override fun accept(dir: File, fname: String): Boolean {
	return fname.endsWith(ext)
    }
}
