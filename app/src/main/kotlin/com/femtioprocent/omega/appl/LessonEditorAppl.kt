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
import kotlin.system.exitProcess

class LessonEditorAppl(fn: String?) : OmegaAppl("Lesson editor") {
    private var le: LessonEditor

    init {
	le = LessonEditor("Omega - Lesson Editor:", antiOmegaAssets(fn))
    }

    companion object {
	fun main() {
	    if (flag["help"] != null) {
		System.err.println("-help")
		System.err.println("-omega_assets=<assets name>")
		System.err.println("-omega_lang=<lang>")
		System.err.println("-T=<step>")
		System.err.println("-small")
		System.err.println("-")
		System.err.println("-")
		exitProcess(1)
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
	    val fn = if (argl.isNotEmpty()) argl[0] else null
	    if (showAndAccepted()) {
		LessonEditorAppl(fn)
	    } else {
		exitProcess(1)
	    }
	}

	@JvmStatic
	fun main(argv: Array<String>) {
	    flag = flagAsMap(argv)
	    argl = argAsList(argv)
	    main()
	}
    }
}
