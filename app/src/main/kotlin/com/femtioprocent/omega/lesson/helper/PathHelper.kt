package com.femtioprocent.omega.lesson.helper

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.adm.assets.TargetCombinations.TCItem
import com.femtioprocent.omega.anim.tool.path.Path
import com.femtioprocent.omega.anim.tool.path.Path.Companion.format_
import com.femtioprocent.omega.lesson.repository.Restore
import com.femtioprocent.omega.lesson.repository.Save
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.xml.Element

/**
 * Created by lars on 2017-05-21.
 */
class PathHelper(var dep_set: MutableSet<TCItem?>) {
    fun performStatus() {
	perform(false, null)
    }

    fun perform(doBackup: String?) {
	perform(true, doBackup)
    }

    fun perform(modify: Boolean, doBackup: String?) {
	for (tci in dep_set) {
	    try {
		val fname = tci!!.fn
		if (!fname.endsWith(".omega_anim")) {
		    Log.getLogger().info("Skip: wrong file name $fname")
		    continue
		}
		Log.getLogger().info("Fix path: $fname")
		val el = Restore.restore(fname)
		if (el == null) {
		    Log.getLogger().info("status: " + (if (modify) "upd" else "dry") + " error " + fname)
		    continue
		}
		val version = el.findAttr("version")
		val clazz = el.findAttr("class")
		if ("Animation" != clazz) {
		    Log.getLogger().info("Skip: class $clazz")
		    continue
		}
		if (false && "0.1" == version) {
		    Log.getLogger().info("Skip: version $version")
		    continue
		}
		Log.getLogger().info("Loaded: $el")
		val status = fixIt(el, modify)
		if (modify) {
		    el.addAttr("version", "0.1")
		    if (doBackup != null) Save.saveWithBackup(fname, doBackup, el) else Save.save(fname, el)
		    Log.getLogger().info("Saved: $el")
		}
		Log.getLogger()
			.info("status: " + (if (modify) "upd" else "dry") + ' ' + version + ' ' + status + ' ' + fname)
	    } catch (ex: Exception) {
		Log.getLogger().info("***Exception: $ex")
	    }
	}
    }

    private fun fixIt(el: Element, modify: Boolean): String {
	var cntTpath = 0
	var cntInfoAdded = 0
	var cntInfoExist = 0
	var cntHelpAdded = 0
	var cntHelpExist = 0
	val el_ac = el.findFirstElement("AnimCanvas")
	val el_ap = el_ac!!.findFirstElement("AllPath")
	for (i in 0..9) {
	    val el_tp = el.findElement("TPath", i) ?: continue
	    cntTpath++
	    Log.getLogger().info("fix TPath " + i + ": " + el_tp.findAttr("nid"))
	    for (j in 0..99) {
		val el_q = el.findElement("q", j) ?: break
		Log.getLogger().info("fix q " + i + ": " + el_q.findAttr("ord"))
	    }
	    val p = Path(el_tp)
	    val lenArr = p.lenA
	    val point2d = p.point2D
	    Log.getLogger().info("flatness " + i + ": " + OmegaConfig.FLATNESS)
	    Log.getLogger().info(
		    "len " + i + ": " + lenArr!!.size + ' ' + format_(
			    lenArr
		    )
	    )
	    Log.getLogger().info(
		    "seg " + i + ": " + point2d!!.size + ' ' + format_(
			    point2d
		    )
	    )
	    var el_i = el_tp.findElement("info", 0)
	    if (el_i == null) {
		el_i = Element("info")
		el_tp.add(el_i)
		cntInfoAdded++
	    } else {
		cntInfoExist++
	    }
	    el_i.subAttr("len")
	    el_i.subAttr("seg")
	    el_i.addAttr("flatness", "" + OmegaConfig.FLATNESS)
	    el_i.addAttr("size", "" + lenArr.size)
	    var el_h = el_tp.findElement("help", 0)
	    if (el_h == null) {
		el_h = Element("help")
		el_tp.add(el_h)
		cntHelpAdded++
	    } else {
		cntHelpExist++
	    }
	    el_h.addAttr("len", format_(lenArr))
	    el_h.addAttr("seg", format_(point2d))
	}
	return "n:$cntTpath +:$cntInfoAdded,$cntHelpAdded =:$cntInfoExist,$cntHelpExist"
    }
}
