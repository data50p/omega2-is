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
import kotlin.system.exitProcess

class LessonRuntimeAppl(fn_: String?, ask: Boolean, winSize: OmegaConfig.WinSize, run_mode: Char) : OmegaAppl("Lesson runtime") {
    private var le_rt: LessonRuntime
    private var ask: Boolean

    init {
	var fn = fn_
	this.ask = ask
	Log.getLogger().info("LessonRuntimeAppl...")
	if (ask) {
	    OmegaAppl.closeSplash()
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
		exitProcess(0)
	    }
	}
	le_rt = LessonRuntime(name, fn, winSize, run_mode)
    }

    companion object {
	private fun toURL(file: File): String {
	    return Files.toURL(file)!!
	}

	fun main() {

	    if ( flag["help"] != null ) {
		System.err.println("Args:")
		System.err.println("-help                This help text")
		System.err.println("-log                 Turn on logging")
		System.err.println("-small               Shows a small window size")
		System.err.println("-smaller             Shows a smaller window size")
		System.err.println("-smallest            Shows a the smallest window size")
		System.err.println("-omega_lang=<lang>   Set omega lang")
		System.err.println("-v                   Set verbose mode")
		System.err.println("-T                   Set a config setting to true")
		System.err.println("-R                   Set run mode to true")
		System.err.println("-t=<value>           Set step time in milli seconds")
		System.err.println("-T=<value>           Set step time in milli seconds for lesson editor")
		System.err.println("-omega_assets=<dir>  Set location of omega assets")
		System.err.println("-few                 Put only a few items in cache")
		System.err.println("-demo                Demo mode")
		System.err.println("-ask                 Ask for a lesson file [lesson runtime]")
		System.err.println("-pupil               Set run mode to pupil (default) [lesson runtime]")
		System.err.println("-admin -teacher      Set run mode to admin/teacher [lesson runtime]")
		System.exit(1)
	    }
	    Log.getLogger().info("started")

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
	    val fn = if (argl.isNotEmpty()) argl[0] else null
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
	    var ch = 'p'
	    if (b_p) {
		ch = 'p'
	    }
	    if (b_t) {
		ch = 'a'
	    }
	    if (b_a) {
		ch = 'a'
	    }
	    if (showAndAccepted()) {
		LessonRuntimeAppl(fn, ask, OmegaContext.winSize, ch)
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
