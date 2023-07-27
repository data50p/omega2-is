package com.femtioprocent.omega.anim.panels.timeline

import java.io.File
import java.io.FilenameFilter

class FilenameFilterExt @JvmOverloads internal constructor(var ext: String, var dir_only: Boolean = false) :
    FilenameFilter {
    override fun accept(dir: File, fname: String): Boolean {
	return if (fname.endsWith(ext)) true else false
    }

    override fun toString(): String {
	return "FilenameFilterExt{$ext}"
    }
}
