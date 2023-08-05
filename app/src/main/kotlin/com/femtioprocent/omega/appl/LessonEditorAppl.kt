package com.femtioprocent.omega.appl

import com.femtioprocent.omega.LicenseShowManager.showAndAccepted
import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.antiOmegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.setOmegaAssets
import com.femtioprocent.omega.lesson.appl.LessonEditor
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import javax.swing.UIManager

class LessonEditorAppl(fn: String?) : OmegaAppl("Lesson editor") {
    var le: LessonEditor

    init {
	le = LessonEditor("Omega - Lesson Editor:", antiOmegaAssets(fn))
    }

    companion object {
	@JvmStatic
	fun main(argv: Array<String>) {
	    val flag: HashMap<String, String> = flagAsMap(argv)
	    val argl = argAsList(argv)
	    if (flag["help"] != null) {
		Log.getLogger().info("-help")
		Log.getLogger().info("-omega_assets=<assets name>")
		Log.getLogger().info("-omega_lang=<lang>")
		Log.getLogger().info("-T=<step>")
		Log.getLogger().info("-small")
		Log.getLogger().info("-")
		Log.getLogger().info("-")
		System.exit(1)
	    }
	    setOmegaAssets(flag["omega_assets"])
	    OmegaContext.omega_lang = flag["omega_lang"]
	    Log.getLogger().info(":--: " + "param omega_lang is " + OmegaContext.omega_lang)

	    OmegaContext.setWindowSize(flag)

	    try {
		UIManager.setLookAndFeel("javax.swing.plaf.MetalLookAndFeel")
		//	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch (e: Exception) {
	    }
	    val t_steps = flag["T"]
	    if (t_steps != null) OmegaConfig.t_step = t_steps.toInt()
	    val fn = if (argl.size > 0) argl[0] else null
	    if (showAndAccepted()) {
		val e = LessonEditorAppl(fn)
	    } else {
		System.exit(1)
	    }
	}
    }
}
