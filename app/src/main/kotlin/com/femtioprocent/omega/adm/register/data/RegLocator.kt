package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files
import com.femtioprocent.omega.util.Log
import java.io.File
import java.io.FilenameFilter
import javax.swing.JOptionPane

class RegLocator {
    fun aRegisterFbase(subPath: String): String {
	return fbase + File.separatorChar + subPath
    }

    fun aRegisterFbase(): String {
	return fbase
    }

    fun removeSuffix(sa: Array<String>?, suf: String): Array<String> {
	if (sa == null) return Array(0) { "" }
	return sa.map { if (it.endsWith(suf)) it.substring(0, it.length - suf.length) else it }.toTypedArray()
    }

    fun removePrefix(sa: Array<String>?, pre: String): Array<String> {
	if (sa == null) return Array(0) { "" }
	return sa.map { if (it.startsWith(pre)) it.substring(pre.length) else it }.toTypedArray()
    }

    val allPupilsName: Array<String>
	get() {
	    val sa = scanDir(aRegisterFbase(), FilenameFilterExt(PUPIL_SUF))
	    return removePrefix(removeSuffix(sa, PUPIL_SUF), aRegisterFbase() + File.separatorChar)
	}

    fun getAllResultsFName(pupil: String, with: Array<String>): Array<String>? {
	return scanDir(aRegisterFbase() + File.separatorChar + pupil + PUPIL_SUF, FilenameFilterExt(testSuffix, with))
    }

    fun getFullFName(pupil: String, lesson_name: String): String {
	return aRegisterFbase() + File.separatorChar +
		pupil + PUPIL_SUF + File.separatorChar +  /*pupil + '-' +*/
		lesson_name + testSuffix
    }

    fun getDirPath(pupil: String): String {
	return aRegisterFbase() + File.separatorChar +
		pupil + PUPIL_SUF + File.separatorChar
    }

    fun mkResultsFName(pupil: String, name: String): String {
	return aRegisterFbase() + File.separatorChar +
		pupil + PUPIL_SUF + File.separatorChar +
		name + testSuffix
    }

    fun createPupilName(name: String) {
	val f_old = File(aRegisterFbase("$name.deleted"))
	val f = File(aRegisterFbase() + "/" + name + ".p")
	if (f.exists()) {
	    JOptionPane.showMessageDialog(
		    ApplContext.top_frame,
		    t("Pupil exist already")
	    )
	}
	if (f_old.exists()) {
	    JOptionPane.showMessageDialog(
		    ApplContext.top_frame,
		    t("Pupil reinstalled")
	    )
	    f_old.renameTo(f)
	} else {
	    f.mkdir()
	    var ft = File(aRegisterFbase("$name.p/id.png"))
	    var ff = File(OmegaContext.getMediaFile("default/pupil.png"))
	    Files.fileCopy(ff, ft)
	    ft = File(aRegisterFbase("$name.p/pupil_settings.xml"))
	    ff = File(aRegisterFbase("Guest" + ".p/pupil_settings.xml"))
	    Files.fileCopy(ff, ft)
	}
    }

    companion object {
	val fbase = "register" // this is not inside the omega_assets
	var PUPIL_SUF = ".p"
	val testSuffix = ".omega_result"

	fun scanDir(dir: String, fnf: FilenameFilter): Array<String>? {
	    Log.getLogger().info(":--: scan $dir $fnf")
	    val df = File(dir)
	    val fa = df.listFiles(fnf) ?: return null
	    return fa.map { f -> dir + File.separatorChar + f.name }.sorted().toTypedArray()
	}
    }
}
