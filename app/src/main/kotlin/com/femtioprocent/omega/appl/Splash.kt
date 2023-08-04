package com.femtioprocent.omega.appl

import com.femtioprocent.omega.OmegaContext.Companion.media
import com.femtioprocent.omega.OmegaVersion.cwd
import com.femtioprocent.omega.OmegaVersion.theJjavaHome
import com.femtioprocent.omega.OmegaVersion.theLangVersion
import com.femtioprocent.omega.OmegaVersion.theOmegaVersion
import com.femtioprocent.omega.OmegaVersion.theVendorVersion
import com.femtioprocent.omega.graphic.util.LoadImage.loadAndWaitFromFile
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.MouseEvent
import javax.swing.JWindow
import javax.swing.event.MouseInputAdapter

class Splash internal constructor() : JWindow() {
    var im: Image? = null
    var off_x = 0
    var m: Mouse

    init {
	val d = Toolkit.getDefaultToolkit().screenSize
	val ww = 600
	off_x = (ww - 400) / 2
	val hh = 380 + 22 * 4
	setLocation((d.width - ww) / 2, (d.height - hh) / 2)
	setSize(ww, hh)
	if (im == null) im = loadAndWaitFromFile(this, media() + "default/omega_splash.gif")
	background = Color.black
	m = Mouse(this)
	isVisible = true
    }

    inner class Mouse(owner: JWindow) : MouseInputAdapter() {
	init {
	    owner.addMouseListener(this)
	    owner.addMouseMotionListener(this)
	}

	override fun mousePressed(e: MouseEvent) {
	    keep = if (keep == null) true else !keep!!
	}

	override fun mouseMoved(e: MouseEvent) {}
	override fun mouseDragged(e: MouseEvent) {}
    }

    override fun paint(g: Graphics) {
	g.drawImage(im, off_x, 10, null)
	g.color = Color.yellow
	var line = 0
	val yoff = 324
	g.drawString(theOmegaVersion, 5, yoff + 20 * line++)
	g.drawString("CWD: " + cwd, 5, yoff + 20 * line++)
	g.drawString(
	    theLangVersion,
	    5,
	    yoff + 20 * line++
	)
	g.drawString(
	    theVendorVersion,
	    5,
	    yoff + 20 * line++
	)
	g.drawString(
	    theJjavaHome,
	    5,
	    yoff + 20 * line++
	)
    }

    companion object {
	var keep: Boolean? = null
    }
}
