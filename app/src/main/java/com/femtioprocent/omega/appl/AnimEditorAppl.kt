package com.femtioprocent.omega.appl

import com.femtioprocent.omega.LicenseShowManager.showAndAccepted
import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.anim.appl.AnimEditor
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import java.awt.DefaultKeyboardFocusManager
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import javax.swing.UIManager

class AnimEditorAppl(verbose: Boolean) : OmegaAppl("Animator editor") {
    var ae: AnimEditor

    init {
	KeyboardFocusManager.setCurrentKeyboardFocusManager(object : DefaultKeyboardFocusManager() {
	    var last_state = '_'
	    var state = 'r'
	    var first_tr = false
	    var P = false
	    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
		val ch = e.keyChar
		val kc = e.keyCode
		if (e.id == KeyEvent.KEY_PRESSED) {
		    if (e.keyCode == KeyEvent.VK_F1) {
			AnimEditor.help!!.showManualAE()
		    }
		}
		return super.dispatchKeyEvent(e)
	    }
	})
	ae = AnimEditor(verbose)
    }

    companion object {
	@JvmStatic
	fun main(argv: Array<String>) {
	    val flag: HashMap<String, String> = flagAsMap(argv)
	    val argl = argAsList(argv)
	    OmegaContext.omega_lang = flag["omega_lang"]
	    Log.getLogger().info(":--: " + "param omega_lang is " + OmegaContext.omega_lang)
	    try {
		UIManager.setLookAndFeel("javax.swing.plaf.MetalLookAndFeel")
		// UIManager.getSystemLookAndFeelClassName());
	    } catch (e: Exception) {
	    }
	    var verbose = false
	    if (flag["v"] != null) verbose = true
	    if (flag["R"] != null) OmegaConfig.RUN_MODE = true
	    if (flag["T"] != null) OmegaConfig.T = true
	    var s: String? = flag["t"]

	    if (s != null) OmegaConfig.t_step = s!!.toInt()
	    //log	OmegaContext.sout_log.getLogger().info(":--: " + "" + OmegaConfig.t_step);
	    if (showAndAccepted()) {
		val e = AnimEditorAppl(verbose)
		m_sleep(3000)
		while (Splash.keep != null && Splash.keep!!) m_sleep(300)
		closeSplash()
	    } else {
		System.exit(1)
	    }

//log	OmegaContext.sout_log.getLogger().info(":--: " + "--------ok-------");
	}
    }
}