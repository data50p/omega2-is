package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.media.video.MpgPlayer.Companion.createMpgPlayer
import java.io.File
import java.io.IOException
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel

//import omega.lesson.test.*;
class FeedBackMovie : FeedBack {
    var signMode = false

    constructor() : super()
    constructor(signMode: Boolean) : super() {
	this.signMode = signMode
    }

    override fun prepare(rsrs: String?, canvas: JPanel?): JPanel? {
	var rsrs = rsrs
	var canvas = canvas
	OmegaContext.lesson_log.getLogger().info(": +++++++ prepare $rsrs")
	if (rsrs == null) {
	    return null
	}
	rsrs = random(rsrs)
	OmegaContext.lesson_log.getLogger().info(": +++++++ prepare random $rsrs")
	if (canvas == null) {
	    canvas = JPanel()
	    my_own = canvas
	}
	if (mp == null) {
	    OmegaContext.lesson_log.getLogger().info(": +++++++ prepare new  $rsrs")
	    mp = createMpgPlayer(rsrs!!, canvas)
	    this.canvas = canvas
	    my_own = null
	    vw = mp!!.vw
	    vh = mp!!.vh
	} else {
	}
	//	mp.setLocation(100, 100);
//	mp.setSize(5, 5);
	mp!!.visual!!.isVisible = false
	return canvas
    }

    @Deprecated("")
    fun prepareAlt(rsrs: String?, canvas: JComponent?): JComponent? {
	var rsrs = rsrs
	OmegaContext.lesson_log.getLogger().info(": +++++++ prepare $rsrs")
	if (rsrs == null) {
	    return null
	}
	rsrs = random(rsrs)
	OmegaContext.lesson_log.getLogger().info(": +++++++ prepare random $rsrs")
	if (canvas == null) return null
	if (mp == null) {
	    OmegaContext.lesson_log.getLogger().info(": +++++++ prepare new  $rsrs")
	    mp = createMpgPlayer(rsrs!!, canvas)
	    comp = canvas
	    my_own = null
	    vw = mp!!.vw
	    vh = mp!!.vh
	} else {
	}
	//	mp.setLocation(100, 100);
//	mp.setSize(5, 5);
	mp!!.visual!!.isVisible = false
	return canvas
    }

    fun random(rsrs: String): String? {
	var rsrs = rsrs
	var currentPath: String? = null
	currentPath = try {
	    File(".").canonicalPath
	} catch (e: IOException) {
	    throw RuntimeException(e)
	}
	OmegaContext.sout_log.getLogger().info("Current dir:$currentPath")
	val f = File(omegaAssets(rsrs))
	OmegaContext.lesson_log.getLogger().info(": " + "+++++++ random  " + f + ' ' + f.exists() + ' ' + f.isDirectory)
	if (f.exists() && f.isDirectory) {
	    return randomDir(rsrs)
	}
	//	return rsrs;
	var path = "."
	val ix = rsrs.lastIndexOf("/")
	if (ix != -1) path = rsrs.substring(0, ix)
	val mpg_file = File(rsrs)
	val mpg_dir = mpg_file.parentFile
	val other = mpg_dir.listFiles { f ->
	    val name = f.name
	    if (name.endsWith(".mpg")) true else false
	}
	fix++
	rsrs = path + "/" + other[fix % other.size].name
	OmegaContext.sout_log.getLogger().info(": " + "fb " + rsrs + " -> " + rsrs + ' ' + other.size)
	return rsrs
    }

    fun randomDir(rsrs: String): String? {
	var rsrs = rsrs
	val mpg_dir = File(omegaAssets(rsrs))
	val other = mpg_dir.listFiles { f ->
	    val name = f.name
	    if (name.endsWith(".mpg")) true else false
	}
	if (other.size == 0) return null
	val r = Random()
	var rr = r.nextInt(1000000000)
	if (rr < 0) rr = -rr
	rsrs = rsrs + "" + other[rr % other.size].name
	OmegaContext.sout_log.getLogger().info(": " + "fb " + rsrs + " -> " + rsrs + ' ' + other.size)
	return rsrs
    }

    override fun perform() {
	if (mp == null) return
	mp!!.reset()
	if (canvas != null) {
	    w = canvas!!.width
	    h = canvas!!.height
	    OmegaContext.sout_log.getLogger().info(": $vw $vh $w $h")
	    mp!!.setSize(w / 2, w * vh / (2 * vw))
	}
	if (comp != null) {
	    w = comp!!.width
	    h = comp!!.height
	    OmegaContext.sout_log.getLogger().info(": $vw $vh $w $h")
	    val ww = w / 5
	    mp!!.setSize(ww, (ww * (vh.toDouble() / vw)).toInt())
	}
	mp!!.visual!!.isVisible = true
	mp!!.start()
    }

    fun perform(x: Int, y: Int) {
	if (mp == null) return
	mp!!.reset()
	if (canvas != null) {
	    w = canvas!!.width
	    h = canvas!!.height
	    OmegaContext.sout_log.getLogger().info(": $vw $vh $w $h")
	    mp!!.setSize(w / 2, w * vh / (2 * vw))
	}
	if (comp != null) {
	    w = comp!!.width
	    h = comp!!.height
	    OmegaContext.sout_log.getLogger().info(": $vw $vh $w $h")
	    val ww = w / 5
	    mp!!.setSize(ww, (ww * (vh.toDouble() / vw)).toInt())
	}
	mp!!.setLocation(x, y)
	mp!!.visual!!.isVisible = true
	mp!!.start()
    }

    override fun waitEnd() {
	if (mp == null) return
	mp!!.wait4()
	mp!!.visual!!.isVisible = false
    }

    override fun dispose() {
	if (mp == null) return
	mp!!.dispose(canvas!!)
	if (my_own === canvas) {
	    my_own = null
	    canvas = my_own
	}
	mp = null
	OmegaContext.sout_log.getLogger().info(": " + "+++++ disposed")
    }

    companion object {
	var fix = 0
    }
}
