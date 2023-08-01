package com.femtioprocent.omega.media.video

import com.femtioprocent.omega.util.Log
import java.io.File
import java.util.*

/**
 * Created by lars on 2017-02-07.
 */
object VideoUtil {
    /**
     * Try with supported movie types
     *
     * @param fname
     * @return
     */
    fun exist(fname: String): Boolean {
	return findSupportedFname(fname) != null
    }

    /**
     * Find the first found supported existing movie file name from the intended.
     *
     * @param fname
     * @return
     */
    fun findSupportedFname(fname: String): String? {
	val altFname = fname
		.replace("\\.[mM][pP][gG]$".toRegex(), ".mp4")
		.replace("\\.[mM][oO][vV]$".toRegex(), ".mp4")
		.replace("\\.[mM][pP][eE][gG]$".toRegex(), ".mp4")
		.replace("\\.[aA][vV][iI]$".toRegex(), ".mp4")
	Log.getLogger().info("alt file name: $fname -> $altFname")
	if (fileExist(altFname)) return altFname
	val f = File(altFname)
	val parent = f.parentFile.path
	val fn = f.name
	val altFile = File(parent, fn.lowercase(Locale.getDefault()))
	return if (fileExist(altFile)) altFile.path else null
    }

    private fun fileExist(fname: String): Boolean {
	return fileExist(File(fname))
    }

    private fun fileExist(file: File): Boolean {
	return file.exists() && file.canRead()
    }
}
