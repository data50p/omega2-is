package com.femtioprocent.omega.media.video

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssetsExist
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import java.awt.Component
import java.awt.Dimension
import java.net.MalformedURLException
import java.net.URL
import javax.swing.JComponent

class MpgPlayer(player: Any?, title: String?) {
    @JvmField
    var vw = 0
    @JvmField
    var vh = 0
    var origW = 0
    var origH = 0
    @JvmField
    var aspect = 1.0
    private val prefetch_done = false
    private var ready = false
    @JvmField
    var visual: Component? = null
    @JvmField
    var fxp: FxMoviePlayer? = null

    init {
	if (false) {
	    for (i in 0..99) if (prefetch_done == false) m_sleep(100)
	}
    }

    fun reset() {
	if (fxp != null) {
	    fxp!!.reset()
	}
	ready = false
    }

    fun start() {
	if (fxp != null) fxp!!.play()
    }

    fun stop() {
	if (fxp != null) fxp!!.player!!.stop()
    }

    fun wait4() {
	if (fxp != null) fxp!!.wait4done()
	//	if ( true ) {
//	    SundryUtils.m_sleep(4000);
//	    return;
//	}
//	while (ready == false)
//	    SundryUtils.m_sleep(200);
    }

    fun dispose(jcomp: JComponent) {
	if (fxp != null) fxp!!.dispose()
	fxp = null
	ready = true
	visual = null
	jcomp.removeAll()
    }

    val x: Int
	get() = visual!!.x
    val y: Int
	get() = visual!!.y
    val w: Int
	get() = visual!!.width //vw;
    val mediaH: Int
	get() = fxp!!.mediaH
    val mediaW: Int
	get() = fxp!!.mediaW
    val h: Int
	get() = visual!!.height //vh;

    fun setSize(w: Int, h: Int) {
	vw = w
	vh = h
	visual!!.size = Dimension(vw, vh)
	Log.getLogger().info("dep_set m size to: $w $h")
    }

    fun setLocation(x: Int, y: Int) {
	visual!!.setLocation(x, y)
	Log.getLogger().info("dep_set m loc at: $x $y")
    }

    companion object {
	@JvmStatic
	@JvmOverloads
	fun createMpgPlayer(fn: String, jcomp: JComponent?, winW: Int = 0, winH: Int = 0): MpgPlayer? {
	    var url: URL? = null
	    Log.getLogger().info("create mpgPlayer jcomp: $fn")
	    try {
		if (omegaAssetsExist(fn)) {
		    url = URL("file:$fn")
		    try {
			val fxp = FxMoviePlayer(winW, winH)
			val fxPanel = fxp.initGUI(jcomp, fn)
			val mp = MpgPlayer(null, "null")
			mp.visual = jcomp
			mp.fxp = fxp
			return mp
		    } catch (e: Exception) {
			e.printStackTrace()
			Log.getLogger().info("NoPlayerEx: $e")
		    }
		}
	    } catch (e: MalformedURLException) {
		Log.getLogger().info("ERR: MUE Error:$e")
	    } catch (e: Exception) {
		Log.getLogger().info("ERR: Exception:$e")
	    }
	    return null
	}
    }
}
