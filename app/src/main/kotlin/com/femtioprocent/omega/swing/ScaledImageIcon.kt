package com.femtioprocent.omega.swing

import com.femtioprocent.omega.graphic.util.LoadImage.loadAndWaitFromFile
import com.femtioprocent.omega.graphic.util.LoadImage.loadAndWaitFromResource
import com.femtioprocent.omega.util.Log.getLogger
import java.awt.AlphaComposite
import java.awt.Component
import java.awt.Graphics2D
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

object ScaledImageIcon {
    fun createImageIcon(comp: Component?, fn: String, max_w: Int, max_h: Int, no_bigger: Boolean = true): ImageIcon? {
	val im: Image?
	im = if (fn.startsWith("toolbarButtonGraphics/")) loadAndWaitFromResource(
		comp,
		fn
	) else loadAndWaitFromFile(comp, fn)
	getLogger().info("Create ImageIcon from: $fn $im")
	if (im == null) return null
	var ww = max_w
	var hh = max_h
	val imw = im.getWidth(null)
	val imh = im.getHeight(null)
	if (imw == -1 || imh == -1) return null
	if (ww == 0) ww = 20
	if (hh == 0) hh = 20
	if (no_bigger) {
	    if (imw < max_w) ww = imw
	    if (imh < max_h) hh = imh
	}
	val fw = imw.toDouble() / ww
	val fh = imh.toDouble() / hh
	var f = fw
	if (fw < fh) f = fh
	var imd: BufferedImage? = null
	try {
	    imd = BufferedImage(
		    (imw / f).toInt(), (imh / f).toInt(),
		    BufferedImage.TYPE_INT_ARGB
	    )
	    // createImage((int)(imw / f), (int)(imh / f));
	} catch (_ex: IllegalArgumentException) {
	}
	if (imd == null) return null
	val g2 = imd.graphics as Graphics2D
	val at = AffineTransform()
	at.scale(1.0 / f, 1.0 / f)
	g2.composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f)
	val fr: Rectangle2D = Rectangle2D.Double(0.0, 0.0, 2000.0, 2000.0)
	g2.fill(fr)
	g2.composite = AlphaComposite.SrcOver
	g2.drawImage(im, at, null)
	return ImageIcon(imd)
    }
}
