package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.media.video.MpgPlayer
import com.femtioprocent.omega.media.video.MpgPlayer.Companion.createMpgPlayer
import com.femtioprocent.omega.media.video.VideoUtil.findSupportedFname
import com.femtioprocent.omega.util.Log.getLogger
import java.awt.Rectangle
import javax.swing.JComponent

//import omega.lesson.test.*;
class EachWordMovie {
    var mp: MpgPlayer? = null
    var jcomp: JComponent? = null
    var w = 0
    var h = 0
    var vw_orig = 0
    var vh_orig = 0
    fun prepare(fName: String?, jcomp: JComponent?): JComponent? {
	var fName = fName
	if (jcomp == null || fName == null) {
	    return null
	}
	fName = omegaAssets(fName)
	fName = findSupportedFname(fName!!)
	if (fName == null) return null
	if (mp == null) {
	    OmegaContext.sout_log.getLogger().info(":--: +++++++ prepare new: $fName")
	    mp = createMpgPlayer(fName, jcomp, jcomp.width, jcomp.height)
	    if (mp == null) return null
	    this.jcomp = jcomp
	    vw_orig = mp!!.origW
	    vh_orig = mp!!.origH
	} else {
	    OmegaContext.sout_log.getLogger().info(":--: +++++++ prepare again: $fName")
	    mp!!.setSize(1, 1)
	    mp!!.setLocation(10, 10)
	    mp!!.visual!!.isVisible = false
	}
	return jcomp
    }

    fun perform() {
	if (mp == null) return
	mp!!.reset()
	if (jcomp != null) {
	    w = jcomp!!.width
	    h = jcomp!!.height
	    OmegaContext.sout_log.getLogger().info(":--: $vw_orig $vh_orig $w $h")
	    val ww = w / 5
	    mp!!.setSize(ww, (ww * (vh_orig.toDouble() / vw_orig)).toInt())
	}
	mp!!.visual!!.isVisible = true
	mp!!.start()
    }

    fun perform(x: Int, y: Int, scale: Double) {
	if (mp == null) return
	mp!!.reset()
	w = jcomp!!.width
	h = jcomp!!.height
	val ww = (w / scale).toInt()
	mp!!.setSize(ww, (ww / mp!!.aspect).toInt())
	var xx = x - ww / 2
	if (xx + ww > w) {
	    xx = w - ww - 1
	}
	if (xx < 1) {
	    xx = 1
	}
	mp!!.setLocation(xx, y)
	mp!!.visual!!.isVisible = true
	OmegaContext.sout_log.getLogger().info(":--: +++++++ perform movie: $vw_orig $vh_orig $w $h $ww")
	mp!!.start()
    }

    val movieRectangle: Rectangle
	get() {
	    val r = Rectangle(mp!!.x, mp!!.y, mp!!.w, mp!!.h)
	    getLogger().info("Movie rect = $r")
	    return r
	}

    fun waitEnd() {
	if (mp == null) return
	mp!!.wait4()
	mp!!.visual!!.isVisible = false
    }

    fun dispose() {
	if (mp == null) return
	mp!!.dispose(jcomp!!)
	jcomp = null
	mp = null
	OmegaContext.sout_log.getLogger().info(":--: " + "+++++ disposed")
    }

    fun reset() {
	mp!!.reset()
    }
}
