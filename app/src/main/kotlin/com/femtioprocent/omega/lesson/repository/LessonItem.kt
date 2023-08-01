package com.femtioprocent.omega.lesson.repository

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssetsExist
import com.femtioprocent.omega.util.Log.getLogger
import com.femtioprocent.omega.util.SundryUtils.a2s
import com.femtioprocent.omega.util.SundryUtils.getFileContent
import java.io.File

class LessonItem(var lessonLongName: String) {
    var lessonName: String
    var isDir = false

    init {
	val ix = lessonLongName.lastIndexOf("/")
	lessonName = lessonLongName.substring(ix + 1)
	val sa: Array<String?>? = Locator.Companion.scanDirDir(lessonDirName)
	if (sa != null && sa.size > 0) isDir = true
    }

    val selector: String?
	get() = omegaSelectorFile
    val defaultLessonFile: String?
	get() = omegaLessonFile
    val lessonFileNameBase: String
	get() {
	    val ix = lessonLongName.lastIndexOf("/")
	    return lessonLongName.substring(0, ix)
	}
    val lessonImageFileName: String
	get() = getLessonImageFileName("")

    fun getLessonImageFileName(more: String): String {
	return lessonFileNameBase + '/' + lessonName + "/" + "image" + more + ".png"
    }

    fun getLessonParentImage(more: String): String {
	return lessonFileNameBase + '/' + lessonName + "/../" + "image" + more + ".png"
    }

    fun getLessonDisplayName(lessonLang: String?): String? {
	return getLessonFileContent("display", lessonLang)
    }

    fun getLessonFileContent(fName: String, lessonLang: String?): String? {
	val fn = lessonFileNameBase + '/' + lessonName + "/" + fName + if (lessonLang == null) "" else "-$lessonLang"
	return if (!omegaAssetsExist(fn)) if (lessonLang == null) null else getLessonFileContent(
		fName,
		null
	) else getFileContent(fn)
    }

    val lessonDirName: String
	get() = lessonFileNameBase + '/' + lessonName
    val lessonShortName: String
	get() = lessonName
    val dirName: String?
	get() = if (isDir) lessonName else null
    val omegaSelectorFile: String?
	//      public String getOmegaLessonFile() {
	get() {
	    val sa: Array<String?>? = Locator.Companion.scanDirSel(lessonDirName)
	    if (sa != null) {
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "FOUND >>>>>>>> " + SundryUtils.a2s(sa));
		if (sa.size > 0) return sa[0]
	    }
	    return null
	}
    val omegaLessonFile: String?
	get() {
	    val sa: Array<String?>? = Locator.Companion.scanDirLes(lessonDirName)
	    if (sa != null) {
		getLogger().info("Scanned Lesson files: " + a2s(sa))
		if (sa.size > 0) return sa[0]
	    }
	    return null
	}

    override fun toString(): String {
	return lessonLongName + ':' + lessonName
    }

    val isStory: Boolean
	get() {
	    val dn = lessonDirName
	    val file = File("$dn/story")
	    return file.exists()
	}
}
