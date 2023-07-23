package com.femtioprocent.omega.graphic.util

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext.Companion.getMediaFile
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.swing.OmegaSwingUtils
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.MilliTimer
import java.awt.Component
import java.awt.Image
import java.awt.MediaTracker
import java.awt.Toolkit
import java.io.File

object LoadImage {
    @JvmStatic
    fun loadAndWaitOrNull(comp: Component?, im_name: String, asNull: Boolean): Image? {
	val tk = Toolkit.getDefaultToolkit()
	var im: Image? = null
	val mt1 = MilliTimer()
	im = try {
	    val fn = getMediaFile(im_name)
	    val file = File(omegaAssets(fn))
	    if (file != null && file.canRead()) tk.createImage(fn) else if (asNull) null else tk.createImage(fn)
	} catch (ex: Exception) {
	    Log.getLogger().info("IMAGE3: Can't load image $im_name\n$ex")
	    return null
	}
	val mt = MediaTracker(comp)
	mt.addImage(im, 0)
	try {
	    mt.waitForID(0)
	} catch (e: InterruptedException) {
	}
	if (OmegaConfig.T) Log.getLogger().info("IMAGE4: " + " loaded file name " + im_name + ' ' + mt1.string + ' ' + im)
	return im
    }

    @JvmStatic
    fun loadAndWaitFromFile(comp: Component?, im_name: String?): Image? {
	val tk = Toolkit.getDefaultToolkit()
	var im: Image? = null
	im = try {
	    val aImname = omegaAssets(im_name)
	    Log.getLogger().info("load image: (A) $aImname")
	    tk.createImage(aImname)
	} catch (ex: Exception) {
	    return null
	}
	val mt = MediaTracker(comp)
	mt.addImage(im, 0)
	try {
	    mt.waitForID(0)
	} catch (e: InterruptedException) {
	    //	    im=null;
	}
	return im
    }

    @JvmStatic
    fun loadAndWaitFromResource(comp: Component?, im_name: String?): Image {
	return OmegaSwingUtils.getImage(im_name)
    }
}
