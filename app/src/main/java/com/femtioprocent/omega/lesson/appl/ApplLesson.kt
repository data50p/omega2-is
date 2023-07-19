package com.femtioprocent.omega.lesson.appl

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.help.HelpSystem
import com.femtioprocent.omega.servers.httpd.Server
import com.femtioprocent.omega.subsystem.Httpd
import java.awt.Window
import javax.swing.JFrame

open class ApplLesson internal constructor(title: String?, is_editor: Boolean) : JFrame(title) {
    private var is_editor: Boolean
    var httpd: Server

    init {
	this.is_editor = is_editor
	TOP_JFRAME = this
	OmegaContext.init("Httpd", null)
	val httpd_ = OmegaContext.getSubsystem("Httpd") as Httpd
	httpd = Httpd.httpd
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
	@JvmField
        var TOP_JFRAME: JFrame? = null
	@JvmField
        var help: HelpSystem? = null
	@JvmField
        var is_editor: Boolean? = null
	@JvmField
        var isMac = false
    }
}
