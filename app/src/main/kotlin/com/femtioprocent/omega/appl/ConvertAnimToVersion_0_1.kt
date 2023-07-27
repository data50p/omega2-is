package com.femtioprocent.omega.appl

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.adm.assets.TargetCombinations.TCItem
import com.femtioprocent.omega.lesson.helper.PathHelper
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import java.io.File
import java.io.IOException

/**
 * Created by lars on 2017-05-22.
 */
class ConvertAnimToVersion_0_1 internal constructor(fn: File) {
    var baseDir: File? = null
    var theFile: File? = null

    init {
	if (fn.isDirectory) {
	    baseDir = fn
	} else {
	    theFile = fn
	}
	val flatnessF = flags!!["flatness"]
	if (flatnessF != null) {
	    val flatness = java.lang.Double.valueOf(flatnessF)
	    OmegaConfig.FLATNESS = flatness
	}
    }

    fun start() {
	val dep_set: MutableSet<TCItem?> = HashSet()
	if (baseDir != null) {
	    fill(dep_set)
	    keep(dep_set, ".omega_anim")
	} else {
	    val tci = createTCI(theFile)
	    dep_set.add(tci)
	}
	val ph = PathHelper(dep_set)
	val doBackup = flags!!["b"]
	if (flags!!["status"] != null) ph.performStatus() else ph.perform(doBackup)
    }

    private fun keep(dep_set: MutableSet<TCItem?>, s: String) {
	val it = dep_set.iterator()
	while (it.hasNext()) {
	    val next = it.next()
	    if (next!!.fn.endsWith(s)) continue
	    it.remove()
	}
    }

    private fun fill(dep_set: MutableSet<TCItem?>, dir: File? = baseDir) {
	val files = dir!!.listFiles()
	for (f in files) {
	    if (f.isDirectory) {
		fill(dep_set, f)
	    } else {
		if (f.name.endsWith(".omega_anim")) {
		    val tci = createTCI(f)
		    if (tci != null) dep_set.add(tci)
		} else Log.getLogger().info("Ignore " + f.absolutePath)
	    }
	}
    }

    private fun createTCI(f: File?): TCItem? {
	try {
	    Log.getLogger().info("convert " + f!!.canonicalPath)
	    return TCItem(f.canonicalPath)
	} catch (e: IOException) {
	    e.printStackTrace()
	}
	return null
    }

    companion object {
	var flags: HashMap<String, String>? = null
	var argl: List<String>? = null
	var flatness = OmegaConfig.FLATNESS
	@JvmStatic
	fun main(args: Array<String>) {
	    if (args.size == 0) {
		Log.getLogger().info("-b=<backupExt> -d=dir -status -flatness=value example.omega_anim,...")
		System.exit(99)
	    }
	    Log.getLogger().info("Started")
	    flags = flagAsMap(args)
	    argl = argAsList(args)
	    var baseDir: String? = null
	    val dir = flags!!.get("d")
	    if (dir != null) baseDir = dir
	    if (baseDir != null) {
		val c01 = ConvertAnimToVersion_0_1(File(baseDir))
		c01.start()
	    } else {
		for (fn in argl!!) {
		    val c01 = ConvertAnimToVersion_0_1(File(fn))
		    c01.start()
		}
	    }
	}
    }
}
