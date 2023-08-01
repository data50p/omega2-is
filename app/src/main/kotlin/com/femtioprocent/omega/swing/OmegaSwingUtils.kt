package com.femtioprocent.omega.swing

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.util.Log.getLogger
import java.awt.Image
import java.awt.Toolkit
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import kotlin.time.measureTimedValue

/**
 * Created by lars on 2017-02-18.
 */
object OmegaSwingUtils {
    fun getImageIcon(path: String): ImageIcon? {
	return try {
	    val im = getImage(path)
	    ImageIcon(im)
	} catch (e: Exception) {
	    getLogger().warning("Can't find ImageIcon for $path")
	    null
	}
    }

    fun getImage(path: String): Image {
	return try {
	    val theTime = measureTimedValue<Image?> {
		val url = OmegaSwingUtils::class.java.classLoader.getResource(path)
		ImageIO.read(url)
	    }
	    theTime.duration
	    OmegaContext.sout_log.getLogger().info("load image res $path in ${theTime.duration}: ${theTime.value}")
	    return theTime.value!!
	} catch (e: Exception) {
	    val cdir = File(".")
	    try {
		OmegaContext.sout_log.getLogger().info("File " + cdir.canonicalPath)
	    } catch (ex: IOException) {
		throw RuntimeException(ex)
	    }
	    val image = Toolkit.getDefaultToolkit().createImage(path)
	    OmegaContext.sout_log.getLogger().info("getImage() from resource did not work for: $path $image")
	    image
	}
    }
}
