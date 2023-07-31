/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.femtioprocent.omega.lesson.managers.movie

import com.femtioprocent.omega.OmegaConfig.isLIU_Mode
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.getMediaFile
import com.femtioprocent.omega.OmegaContext.Companion.lessonLang
import com.femtioprocent.omega.lesson.EachWordMovie
import com.femtioprocent.omega.lesson.canvas.LessonCanvas
import com.femtioprocent.omega.lesson.machine.Item
import com.femtioprocent.omega.lesson.machine.Target
import com.femtioprocent.omega.lesson.managers.Manager
import com.femtioprocent.omega.media.video.VideoUtil.findSupportedFname
import java.awt.Component
import java.awt.Rectangle
import java.awt.Window
import java.io.File
import javax.swing.JComponent
import javax.swing.JRootPane

/**
 * @author lars
 */
class LiuMovieManager(var window: Window, le_canvas: LessonCanvas) : Manager("LiuMovieManager") {
    var eWmovie: EachWordMovie? = null
    var glp: Component? = null
    var le_canvas: LessonCanvas
    fun prepare(prefix: String, movieNameBase: String, init: Boolean): Boolean {
	if (!isLIU_Mode()) return false
	OmegaContext.sout_log.getLogger()
	    .info("LiuMovieManager:: prepare play movie: (~A) $prefix $movieNameBase $init")
	return if (init) prepare(prefix + movieNameBase) else prepareAgain(prefix + movieNameBase)
    }

    private fun prepare(movieFileName: String): Boolean {
	val cs = window.components
	val jrp = cs[0] as JRootPane
	val cop = jrp.contentPane
	glp = jrp.glassPane
	eWmovie = EachWordMovie()
	try {
	    val jcomp = glp as JComponent?
	    val canvas = eWmovie!!.prepare(movieFileName, jcomp)
	    if (canvas == null) {
		eWmovie = null
		return false
	    }
	    OmegaContext.sout_log.getLogger().info("LiuMovieManager: movie loaded")
	    return true
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: $ex")
	    ex.printStackTrace()
	}
	return false
    }

    private fun prepareAgain(movieFileName: String): Boolean {
	try {
	    if (eWmovie != null) {
		val jcomp = glp as JComponent?
		val canvas = eWmovie!!.prepare(movieFileName, jcomp)
		if (canvas == null) {
		    eWmovie = null
		    return false
		}
		OmegaContext.sout_log.getLogger().info("LiuMovieManager: movie loaded")
		return true
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: $ex")
	    ex.printStackTrace()
	}
	return false
    }

    enum class RepeatMode {
	CAN_REPEAT,
	NO_REPEAT,
	DO_REPEAT
    }

    init {
	repeat_mode = RepeatMode.CAN_REPEAT
	this.le_canvas = le_canvas
    }

    fun start(x: Int, y: Int, scale: Double): Rectangle? {
	try {
	    if (eWmovie != null) {
		eWmovie!!.perform(x, y, scale)
		var r = rectangle
		OmegaContext.sout_log.getLogger().info("rect: $r")
		Thread.sleep(500)
		r = rectangle
		OmegaContext.sout_log.getLogger().info("rect: $r")
		return r
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: $ex")
	    ex.printStackTrace()
	}
	return null
	//jrp.setContentPane(cop);
    }

    fun wait(x: Int, y: Int, scale: Double) {
	try {
	    if (eWmovie != null) {
		eWmovie!!.waitEnd()
		if (repeat_mode == RepeatMode.DO_REPEAT) {
		    repeat_mode = RepeatMode.NO_REPEAT
		    le_canvas.repaint()
		    eWmovie!!.reset()
		    eWmovie!!.perform(x, y, scale)
		    eWmovie!!.waitEnd()
		}
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: $ex")
	    ex.printStackTrace()
	}
	//jrp.setContentPane(cop);
    }

    fun cleanup() {
	if (eWmovie != null) {
	    eWmovie!!.dispose()
	    eWmovie = null
	}
    }

    val rectangle: Rectangle
	get() = eWmovie!!.movieRectangle

    fun getSignMovieFileName(sitm: Item, tg: Target, ix: Int): String? {
	var sfn = sitm.signD
	if (sfn != null && sfn.length > 0) {
	    sfn = tg.fillVarHere(ix, sfn)
	    if (mediaFileExist(sfn)) return sfn
	}
	sfn = ("sign-"
		+ lessonLang
		+ "/"
		+ sitm.textD
		+ ".mpg")
	OmegaContext.serr_log.getLogger().info("sign movie name: $sfn")
	return if (mediaFileExist(sfn)) sfn else null
    }

    private fun mediaFileExist(sfn: String?): Boolean {
	val smFn = findSupportedFname(getMediaFile(sfn!!)!!) ?: return false
	val f = File(smFn)
	val exist = f.exists() && f.canRead()
	OmegaContext.sout_log.getLogger().info("mediaFileExist: $sfn $exist")
	return exist
    }

    companion object {
	@JvmField
	var repeat_mode = RepeatMode.CAN_REPEAT
    }
}
