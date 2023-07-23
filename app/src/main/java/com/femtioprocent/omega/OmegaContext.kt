package com.femtioprocent.omega

import com.femtioprocent.omega.subsystem.Subsystem
import com.femtioprocent.omega.t9n.T
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.empty
import java.awt.Color
import java.io.File
import java.util.*
import java.util.logging.Level

open class OmegaContext  //    public static String getEditorLessonLang() {
//	return lesson_lang_editor;
//    }
//
//    public static void setEditorLessonLang(String s) {
//        OmegaContext.lesson_log.getLogger().info("EditorLessonLang: old, new: " + lesson_lang_editor + ' ' + s);
//	lesson_lang_editor = s;
//    }
{
    class HelpStack {
	var stack = Stack<String>()
	fun push(s: String) {
	    stack.push(s)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "H push " + stack);
	}

	fun get(): String? {
	    return if (stack.empty()) null else stack.peek()
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "H get " + (String)stack.peek());
	}

	fun pop(s: String?) {
	    if (s == null) return
	    if (s.length == 0 || s == get()) if (!stack.empty()) stack.pop()
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "H pop " + stack);
	}
    }

    companion object {
	const val OMEGA_ASSETS_SUFFIX = ".omega_assets"
	const val defaultOmegaAssets = "default" + OMEGA_ASSETS_SUFFIX
	const val developerOmegaAssets = "developer" + OMEGA_ASSETS_SUFFIX
	private var currentOmegaAssets = getDefaultOmegaAssets()
	var lock = Any()
	var subsystems = HashMap<String, Subsystem?>()
	@JvmField
        var URL_BASE = "http://localhost:8089/"
	@JvmField
        var URL_BASE_AS_FILE = ""
	var logon = !false
	@JvmField
        var def_log = Log()
	@JvmField
        var sout_log = def_log
	@JvmField
        var serr_log = def_log
	@JvmField
        var exc_log = def_log
	@JvmField
        var story_log = def_log
	@JvmField
        var lesson_log = def_log
	var audio_log = def_log
	private var lesson_lang = T.lang
	private val lesson_lang_editor = T.lang
	@JvmField
        var SPEED = ""
	@JvmField
        var CACHE_FEW = false
	@JvmField
        var COLOR_WARP = Color(0xe5, 0xe5, 0xe5) // transfer color from anim panel to mpg panel
	@JvmField
        var COLOR_TEXT_WARP = Color(0, 0, 0) // transfer color from anim panel to mpg panel
	@JvmField
        var extern_help_browser = true
	@JvmField
        var variables: Map<*, *>? = null
	@JvmField
        var DEMO = false
	@JvmField
        var omega_lang: String? = null
	@JvmField
        var small: String? = null

	/**
	 * Get the full path for current omega assets
	 *
	 * @param path
	 * @return
	 */
        @JvmStatic
        fun omegaAssets(path: String?): String? {
	    if (path == null) {
		return null
	    }
	    val noAssets = path != null && path.contains("toolbarButtonGraphics") || path.startsWith("register/")
	    if (path != null && path.startsWith(currentOmegaAssets)) {
		//sout_log.getLogger().warning("currentOmegaAssets(): Already omega_assets: " + path);
		return if (noAssets) antiOmegaAssets(path) else path
	    }
	    if (noAssets) {
		Log.getLogger().warning("currentOmegaAssets(): noAssets omega_assets: $path")
		return path
	    }
	    if ("." == path) {
		Log.getLogger().warning("currentOmegaAssets(): .: " + currentOmegaAssets)
		return currentOmegaAssets
	    }
	    if (path.startsWith("/")) {
		Log.getLogger().warning("currentOmegaAssets(): /: " + currentOmegaAssets)
		return path
	    }
	    //sout_log.getLogger().warning("currentOmegaAssets():+: " + currentOmegaAssets + '/' + path);
	    return currentOmegaAssets + '/' + path
	}

	@JvmStatic
        fun antiOmegaAssets(afn: String?): String? {
	    if (afn == null || afn.length == 0) return afn
	    if (afn.startsWith(omegaAssets("")!!)) {
		return afn.substring(omegaAssets("")!!.length)
	    }
	    return if (afn.startsWith("./" + omegaAssets(""))) {
		afn.substring(("./" + omegaAssets("")).length)
	    } else afn
	}

	@JvmStatic
        fun antiOmegaAssets(afns: Array<String?>?): Array<String?>? {
	    if (afns == null || afns.size == 0) return afns
	    val asa = arrayOfNulls<String>(afns.size)
	    var ix = 0
	    for (s in afns) {
		asa[ix++] = antiOmegaAssets(s)
	    }
	    return asa
	}

	@JvmStatic
        fun omegaAssetsName(): String {
	    return currentOmegaAssets
	}

	/**
	 * Set the from now on choosen omega assets
	 *
	 * @param omega_assets_name null value restores default
	 */
	@JvmStatic
        @Throws(IllegalArgumentException::class)
	fun setOmegaAssets(omega_assets_name: String) {
	    var omega_assets_name = omega_assets_name
	    if (empty(omega_assets_name)) {
		currentOmegaAssets = getDefaultOmegaAssets()
		Log.getLogger().info("setOmegaAssets: " + currentOmegaAssets)
	    } else {
		if (!omega_assets_name.endsWith(OMEGA_ASSETS_SUFFIX)) omega_assets_name = omega_assets_name + OMEGA_ASSETS_SUFFIX
		if (File(omega_assets_name).exists()) {
		    Log.getLogger().info("setOmegaAssets: " + currentOmegaAssets + " -> " + omega_assets_name)
		    currentOmegaAssets = omega_assets_name
		    return
		}
		Log.getLogger().info("setOmegaAssets: unable to set omega assets, keep old! " + currentOmegaAssets)
	    }
	}

	private fun getDefaultOmegaAssets(): String {
	    return if (false && isDeveloper) developerOmegaAssets else defaultOmegaAssets
	}

	@JvmStatic
        fun getMediaFile(name: String): String? {
	    return omegaAssets("media/$name")
	}

	fun t9n(s: String?): String? {
	    if (s == null) return null
	    return if (s.startsWith("t9n/")) s else "t9n/$s"
	}

	@JvmStatic
        fun omegaAssetsExist(fn: String?): Boolean {
	    val of = omegaAssets(fn)
	    val f = File(of)
	    return f.exists() && f.canRead()
	}

	@JvmStatic
        fun media(): String {
	    return "media/"
	}

	@JvmField
        var HELP_STACK = HelpStack()
	@JvmStatic
        fun setLogon_(b: Boolean) {
	    var b = b
	    b = b or isDeveloper
	    Log.getLogger().level = if (b) Level.ALL else Level.OFF
	}

	@JvmStatic
        var lessonLang: String
	    get() = lesson_lang
	    set(s) {
		Log.getLogger().info("old, new: " + lesson_lang + ' ' + s)
		lesson_lang = s
	    }
	@JvmStatic
        val isDeveloper: Boolean
	    get() {
		val f = File("../.git")
		return !false && f.exists()
	    }

	@JvmStatic
        fun init(s: String, arg: Any?) {
	    synchronized(lock) {
		try {
		    if (subsystems[s] != null) return
		    val cl = Class.forName("com.femtioprocent.omega.subsystem.$s") as Class<Subsystem>
		    val ss = cl.getDeclaredConstructor().newInstance()
		    ss.init(arg)
		    subsystems.put(s, ss)
		} catch (ex: Exception) {
		    Log.getLogger().info("ERR: Can't start subsystem $s\n$ex")
		}
	    }
	}

	@JvmStatic
        fun getSubsystem(s: String): Subsystem? {
	    return subsystems[s]
	}

	@JvmStatic
        val isMacOS: Boolean
	    get() {
		val s = System.getProperty("os.name").lowercase(Locale.getDefault())
		return s.indexOf("mac") >= 0
	    }
	val isWINDOS: Boolean
	    get() {
		val s = System.getProperty("os.name").lowercase(Locale.getDefault())
		return s.indexOf("win") >= 0
	    }
	val isLinux: Boolean
	    get() {
		val s = System.getProperty("os.name").lowercase(Locale.getDefault())
		return s.indexOf("nux") >= 0
	    }
    }
}
