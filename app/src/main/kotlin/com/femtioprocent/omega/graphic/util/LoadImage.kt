package com.femtioprocent.omega.graphic.util

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
import kotlin.time.measureTime

object LoadImage {
    fun loadAndWaitOrNull(comp: Component?, im_name: String, asNull: Boolean): Image? {
	var im: Image? = null
	val theTime = measureTime {
	    val tk = Toolkit.getDefaultToolkit()
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
	}
	Log.getLogger().info("It took $theTime to load $im_name")
	return im
    }

    fun loadAndWaitFromFile(comp: Component?, im_name: String?): Image? {
	var im: Image? = null
	val theTime = measureTime {
	    val tk = Toolkit.getDefaultToolkit()
	    im = try {
		val aImname = omegaAssets(im_name)
		tk.createImage(aImname)
	    } catch (ex: Exception) {
		Log.getLogger().severe("Cannot load image $im_name/$ex")
		return null
	    }
	    val mt = MediaTracker(comp)
	    mt.addImage(im, 0)
	    try {
		mt.waitForID(0)
	    } catch (e: InterruptedException) {
		//	    im=null;
	    }
	}
	Log.getLogger().info("It took $theTime to load $im_name")
	return im
    }

    fun loadAndWaitFromResource(comp: Component?, im_name: String?): Image {
	return OmegaSwingUtils.getImage(im_name!!)
    }
}
