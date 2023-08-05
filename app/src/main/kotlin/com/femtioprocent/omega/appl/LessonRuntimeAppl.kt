package com.femtioprocent.omega.appl

import com.femtioprocent.omega.LicenseShowManager.showAndAccepted
import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.isDeveloper
import com.femtioprocent.omega.OmegaContext.Companion.setLogon_
import com.femtioprocent.omega.lesson.appl.LessonRuntime
import com.femtioprocent.omega.swing.filechooser.ChooseLessonFile
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

class LessonRuntimeAppl(fn: String?, ask: Boolean, winSize: OmegaConfig.WinSize, run_mode: Char) : OmegaAppl("Lesson runtime") {
    var le_rt: LessonRuntime
    var ask: Boolean

    init {
	var fn = fn
	this.ask = ask
	Log.getLogger().info("LessonRuntimeAppl...")
	if (ask) {
	    val choose_f = ChooseLessonFile()
	    var url_s: String? = null
	    val rv = choose_f.showDialog(null, t("Select"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		choose_f.setLastFile(file)
		url_s = toURL(file)
		fn = if (url_s.startsWith("file:")) {
		    url_s.substring(5)
		} else {
		    url_s
		}
		if (!fn.endsWith("." + ChooseLessonFile.ext)) {
		    fn = fn + "." + ChooseLessonFile.ext
		}
	    } else {
		System.exit(0)
	    }
	} else {
	}
	le_rt = LessonRuntime(name, fn, winSize, run_mode)
    }

    companion object {
	private fun toURL(file: File): String {
	    return Files.toURL(file)!!
	}

	var last_logged = ct()

	@JvmStatic
	fun main(argv: Array<String>) {
	    Log.getLogger().info("started")
	    val flag: HashMap<String, String> = flagAsMap(argv)
	    val argl = argAsList(argv)

	    OmegaContext.setWindowSize(flag)

	    OmegaContext.omega_lang = flag["omega_lang"]
	    Log.getLogger().info(":--: " + "param omega_lang is " + OmegaContext.omega_lang)
	    try {
		UIManager.setLookAndFeel("javax.swing.plaf.MetalLookAndFeel")
		//	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch (e: Exception) {
	    }
	    OmegaConfig.T = !false
	    val few = flag["few"] != null
	    if (few) {
		OmegaContext.CACHE_FEW = true
	    }
	    val demo = flag["demo"] != null
	    if (demo) {
		OmegaContext.DEMO = true
	    }
	    Log.getLogger().info(":--: " + "Omega demo: " + OmegaContext.DEMO)
	    val ask = flag["ask"] != null
	    val fn = if (argl.size > 0) argl[0] else null
	    //log	OmegaContext.sout_log.getLogger().info(":--: " + "start " + ask + ' ' + fn);
	    val t_steps = flag["T"]
	    if (t_steps != null) {
		OmegaConfig.t_step = t_steps.toInt()
	    }
	    val with_frame = flag["small"] != null
	    val logon = flag["log"] != null
	    setLogon_(isDeveloper || logon)
	    val b_p = flag["pupil"] != null
	    val b_a = flag["admin"] != null
	    val b_t = flag["teacher"] != null
	    var ch = '?'
	    if (b_p) {
		ch = 'p'
	    }
	    if (b_t) {
		ch = 't'
	    }
	    if (b_a) {
		ch = 'a'
	    }
	    if (showAndAccepted()) {
		val rt = LessonRuntimeAppl(fn, ask, OmegaContext.winSize, ch)
	    } else {
		System.exit(1)
	    }
	}
    }
}
