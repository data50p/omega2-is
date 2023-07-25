package com.femtioprocent.omega.lesson.repository

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.lessonLang
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import java.io.File
import java.io.FilenameFilter
import java.util.*

class Locator {
    val allLessonsInDir: Array<String?>?
	get() = scanDir(lessonBase) //fbase);

    fun getAllLessonsInDir(more: String): Array<String?>? {
	return scanDir(lessonBase /*fbase*/ + '/' + more)
    }

    fun getAllActiveFiles(fbase: String, ext: String): Array<String?> {
	var fbase = fbase
	if (OmegaContext.DEMO) fbase = fbase.replace("active".toRegex(), "demo") // DIR
	val fnf_ext = FilenameFilterExt(ext)
	val dirs = scanDir(fbase)
	val sa = arrayOfNulls<String>(1000)
	var ix = 0

//	OmegaContext.sout_log.getLogger().info(":--: " + "FILES dirs " + SundryUtils.a2s(dirs));
	if (dirs != null) for (i in dirs.indices) {
	    val files = scanDir(dirs[i], fnf_ext)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "FILES " + SundryUtils.a2s(files));
	    if (files != null) {
		System.arraycopy(files, 0, sa, ix, files.size)
		ix += files.size
	    }
	}
	val ssa = arrayOfNulls<String>(ix)
	System.arraycopy(sa, 0, ssa, 0, ix)
	return ssa
    }

    val allSelectorInDir: Array<String?>?
	get() = scanDirSel(lessonBase /*fbase*/)

    companion object {
	var lang: String? = null
	val lessonBase: String?
	    get() = if (OmegaContext.DEMO) omegaAssets("lesson-" + lessonLang + "/demo") // LESSON-DIR-A
	    else omegaAssets("lesson-" + lessonLang + "/active") // LESSON-DIR-A
	var fnf_dir = FilenameFilter { dir, fname ->
	    val f = File(dir, fname)
	    if (f.isDirectory && f.name != ".svn") true else false
	}
	var fnf_se = FilenameFilter { dir, fname -> fname.endsWith(".omega_selector") }
	var fnf_le = FilenameFilter { dir, fname -> fname.endsWith(".omega_lesson") }

	fun scanDirLes(dir: String): Array<String?>? {
	    val df = File(dir)
	    val fa = df.listFiles(fnf_le)
	    //	OmegaContext.sout_log.getLogger().info(":--: " + "# " + SundryUtils.a2s(fa));
	    var N = 0
	    if (fa != null) {
		for (i in fa.indices) if (".svn" != fa[i].name) N++
	    }
	    if (fa != null) {
		val sa = arrayOfNulls<String>(N)
		var ii = 0
		for (i in fa.indices) if (".svn" != fa[i].name) sa[ii++] = dir + '/' + fa[i].name
		Arrays.sort(sa)
		return sa
	    }
	    return null
	}

	fun scanDirSel(dir: String?): Array<String?>? {
	    val df = File(dir)
	    val fa = df.listFiles(fnf_se)
	    //	OmegaContext.sout_log.getLogger().info(":--: " + "scan sel " + dir);
	    if (fa != null) {
		val sa = arrayOfNulls<String>(fa.size)
		for (i in fa.indices) sa[i] = dir + '/' + fa[i].name
		Arrays.sort(sa)
		return sa
	    }
	    return null
	}

	fun scanDirDir(dir: String): Array<String?>? {
	    val df = File(dir)
	    val fa = df.listFiles(fnf_dir)
	    //	OmegaContext.sout_log.getLogger().info(":--: " + "scan dirdir " + dir);
	    if (fa != null) {
		val sa = arrayOfNulls<String>(fa.size)
		for (i in fa.indices) sa[i] = dir + '/' + fa[i].name
		Arrays.sort(sa)
		return sa
	    }
	    return null
	}

	fun scanDir(dir: String?): Array<String?>? {
//	OmegaContext.sout_log.getLogger().info(":--: " + "SCAN in " + dir + ' ' + StackTrace.trace());
	    val df = File(dir)
	    val fa = df.listFiles(fnf_dir)
	    var N = 0
	    if (fa != null) {
		for (i in fa.indices) if (".svn" != fa[i].name) N++
	    }
	    if (fa != null) {
		val sa = arrayOfNulls<String>(N)
		var ii = 0
		for (i in fa.indices) if (".svn" != fa[i].name) sa[ii++] = dir + '/' + fa[i].name
		Arrays.sort(sa)
		return sa
	    }
	    return null
	}

	fun scanDirExt(dir: String, ext: String?): Array<String?>? {
	    val df = File(dir)
	    val fa = df.listFiles(fnf_le)
	    //	OmegaContext.sout_log.getLogger().info(":--: " + "scan sel " + dir);
	    if (fa != null) {
		val sa = arrayOfNulls<String>(fa.size)
		for (i in fa.indices) sa[i] = dir + '/' + fa[i].name
		Arrays.sort(sa)
		return sa
	    }
	    return null
	}

	fun scanDir(dir: String?, fnf: FilenameFilter?): Array<String?>? {
//	OmegaContext.sout_log.getLogger().info(":--: " + "scan " + dir + ' ' + fnf);
	    val df = File(dir)
	    val fa = df.listFiles(fnf)
	    if (fa != null) {
		val sa = arrayOfNulls<String>(fa.size)
		for (i in fa.indices) sa[i] = dir + '/' + fa[i].name
		Arrays.sort(sa)
		return sa
	    }
	    return null
	}
    }
}
