package com.femtioprocent.omega.lesson.repository

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.Log.getLogger
import com.femtioprocent.omega.util.SundryUtils.createPrintWriterUTF8
import com.femtioprocent.omega.xml.Element
import com.femtioprocent.omega.xml.XML_PW
import java.io.File
import java.io.IOException

object Save {
    fun saveWithBackup(fname: String, extension: String?, el: Element) {
	save(fname, el, extension)
    }

    @JvmStatic
    fun save(fname: String, el: Element) {
	save(fname, el, null)
    }

    private fun save(fname: String, el: Element, extension: String?) {
	val stories = el.find("story")
	if (stories != null && stories.size > 0) {
	    val sel = stories[0]
	    val isfirst = sel.findAttr("isfirst")
	    if (isfirst != null) {
		if (fname.contains("active")) {
		    addStoryFileIndicator(fname)
		}
	    }
	}
	if (extension != null) doBackup(fname, extension)
	try {
	    XML_PW(createPrintWriterUTF8(fname), false).use { xmlpw -> xmlpw.put(el) }
	} catch (e: Exception) {
	    e.printStackTrace()
	}
    }

    private fun doBackup(fname: String, extension: String) {
	val buFile = File(fname + extension)
	val theFile = File(fname)
	if (theFile.exists() && buFile.exists()) buFile.delete()
	if (theFile.exists()) theFile.renameTo(buFile)
    }

    private fun addStoryFileIndicator(fname: String) {
	getLogger().fine("ADD story file")
	val file = File(omegaAssets(fname))
	val dir = file.parentFile.parentFile
	val storyFile = File(dir, "story")
	if (!storyFile.exists()) {
	    try {
		val b = storyFile.createNewFile()
		getLogger().info("ADDED story file $storyFile $b")
	    } catch (e: IOException) {
		e.printStackTrace()
	    }
	}
    }
}
