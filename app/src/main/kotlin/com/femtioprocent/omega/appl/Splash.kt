package com.femtioprocent.omega.appl

import com.femtioprocent.omega.OmegaContext.Companion.media
import com.femtioprocent.omega.OmegaVersion.cWD
import com.femtioprocent.omega.OmegaVersion.javaHome
import com.femtioprocent.omega.OmegaVersion.javaVendor
import com.femtioprocent.omega.OmegaVersion.javaVersion
import com.femtioprocent.omega.OmegaVersion.omegaVersion
import com.femtioprocent.omega.graphic.util.LoadImage.loadAndWaitFromFile
import com.sun.javafx.runtime.VersionInfo
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.MouseEvent
import java.util.*
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
	g.drawString(omegaVersion, 5, yoff + 20 * line++)
	g.drawString("CWD: " + cWD, 5, yoff + 20 * line++)
	g.drawString(
		"Version: java " + javaVersion + ",   javafx " + VersionInfo.getRuntimeVersion(),
		5,
		yoff + 20 * line++
	)
	g.drawString(
		"Java Vendor: " + javaVendor + "; OS name: " + System.getProperty("os.name").lowercase(Locale.getDefault()),
		5,
		yoff + 20 * line++
	)
	g.drawString("java home: " + javaHome, 5, yoff + 20 * line++)
    }

    companion object {
	var keep: Boolean? = null
    }
}
