package com.femtioprocent.omega.swing

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.util.Log.getLogger
import com.femtioprocent.omega.util.MilliTimer
import java.awt.Image
import java.awt.Toolkit
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon

/**
 * Created by lars on 2017-02-18.
 */
object OmegaSwingUtils {
    fun getImageIcon(path: String): ImageIcon? {
	return try {
	    val mt1 = MilliTimer()
	    val im = getImage(path)
	    val icon = ImageIcon(im)
	    OmegaContext.sout_log.getLogger().info("IMAGE7: " + "load image icon " + path + ' ' + mt1.string)
	    icon
	} catch (e: Exception) {
	    getLogger().warning("Can't find ImageIcon for $path")
	    null
	}
    }

    fun getImage(path: String): Image {
	return try {
	    val mt1 = MilliTimer()
	    val url = OmegaSwingUtils::class.java.classLoader.getResource(path)
	    val c = ImageIO.read(url)
	    OmegaContext.sout_log.getLogger().info("IMAGE8: " + "load image res " + path + ' ' + mt1.string)
	    c
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
