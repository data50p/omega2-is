package com.femtioprocent.omega.adm.register.data

import java.io.File
import java.io.FilenameFilter

class FilenameFilterExt @JvmOverloads internal constructor(var ext: String, var with: Array<String>? = null) :
    FilenameFilter {
    override fun accept(dir: File, fname: String): Boolean {
	if (fname.endsWith(ext)) {
	    if (with == null) return true
	    for (i in with!!.indices) {
		if (fname.indexOf(with!![i]!!) != -1) return true
	    }
	    return false
	}
	return false
    }

    override fun toString(): String {
	return "FilenameFilterExt{$ext}"
    }
}
