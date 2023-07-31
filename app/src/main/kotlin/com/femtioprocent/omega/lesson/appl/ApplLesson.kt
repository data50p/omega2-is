package com.femtioprocent.omega.lesson.appl

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.help.HelpSystem
import com.femtioprocent.omega.servers.httpd.Server
import com.femtioprocent.omega.subsystem.Httpd
import java.awt.Window
import javax.swing.JFrame

open class ApplLesson internal constructor(title: String?, is_editor: Boolean) : JFrame(title) {
    var httpd: Server

    init {
	TOP_JFRAME = this
	OmegaContext.init("Httpd", null)
	val httpd_ = OmegaContext.getSubsystem("Httpd") as Httpd
	httpd = Httpd.httpd!!
	if (OmegaConfig.fullScreen) {
	    try {
		val util = Class.forName("com.apple.eawt.FullScreenUtilities")
		val params = arrayOf<Class<*>>(Window::class.java, java.lang.Boolean.TYPE)
		val method = util.getMethod("setWindowCanFullScreen", *params)
		method.invoke(util, this, true)
		isMac = true
	    } catch (ex: Exception) {
		ex.printStackTrace()
	    }
	    //	getRootPane().putClientProperty("apple.awt.fullscreenable", Boolean.valueOf(true));
	    /*
	if ( FullScreenUtilities.class != null ) {
	    FullScreenUtilities.setWindowCanFullScreen(this, true);
	    Application.getApplication().requestToggleFullScreen(this);
	}
	*/
	}
	help = HelpSystem()
    }

    companion object {
	var TOP_JFRAME: JFrame? = null
	var help: HelpSystem? = null
	var is_editor: Boolean? = null
	var isMac = false
    }
}
