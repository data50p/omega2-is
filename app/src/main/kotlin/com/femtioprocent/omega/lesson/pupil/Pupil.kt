package com.femtioprocent.omega.lesson.pupil

import com.femtioprocent.omega.swing.ScaledImageIcon
import com.femtioprocent.omega.util.Log
import java.awt.Component
import java.awt.Image

class Pupil(var name: String) {
    var jparams: HashMap<String?,String?>? = null

    init {
	Log.getLogger().info("new Pupil $name")
	Log.getLogger().info("created Pupil: $name")
    }

    val testId: String
	get() = "pre_1"

    fun setParams(hm: java.util.HashMap<String?, String?>?) {
	jparams = hm
    }

    fun getParamInt(key: String?): Int {
	return 1
    }

    fun getInt(key: String?, def: Int): Int {
	return try {
	    if (jparams == null) return def
	    val s = jparams!![key] as String? ?: return def
	    s.toInt()
	} catch (ex: Exception) {
	    def
	}
    }

    fun getBool(key: String?, def: Boolean): Boolean {
	if (jparams == null) return def
	val s = jparams!![key] as String? ?: return def
	return s.startsWith("y") ||
		s.startsWith("Y") ||
		s.startsWith("j") ||
		s.startsWith("J") || "true" == s || "T" == s
    }

    fun getString(key: String, def: String?): String? {
	if (jparams == null) return def
	val s = jparams!![key] as String?
	Log.getLogger().info("param String, $key $def $s")
	return s ?: def
    }

    fun getStringNo0(key: String, def: String?): String? {
	if (jparams == null) {
	    Log.getLogger().info("param NULL, $key $def")
	    return def
	}
	val s = jparams!![key] as String?
	val ret = if (s == null || s.length == 0) def else s
	Log.getLogger().info(":--: Pupil -> $ret $def")
	Log.getLogger().info("OK $ret")
	return ret
    }

    fun getSpeed(`val`: Int): Int {
	val dA = doubleArrayOf(0.6, 1.0, 1.5)
	if (jparams == null) return `val`
	var s = jparams!!["speed"] as String?
	if (s == null) s = "1"
	val ix = s.toInt()
	val f = dA[ix]
	return (`val` * f).toInt()
    }

    fun getImage(comp: Component?): Image? {
	if (jparams == null) return null
	val s = jparams!!["image"] as String? ?: return null
	return ScaledImageIcon?.createImageIcon(
		comp,
		s,
		100,
		80
	)!!.image
    }

    val imageName: String?
	get() = if (jparams == null) null else jparams!!["image"] as String?
    val imageNameWrongAnswer: String?
	get() = if (jparams == null) null else jparams!!["image_wrong"] as String?

    override fun toString(): String {
	return "Pupil:" + name // + ':' + params;
    }
}
