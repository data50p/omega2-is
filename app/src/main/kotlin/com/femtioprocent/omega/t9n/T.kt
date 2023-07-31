package com.femtioprocent.omega.t9n

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils
import java.beans.XMLDecoder
import java.beans.XMLEncoder
import java.io.*
import java.util.*

class T {
    private fun before_(s: String): String {
	val ix = s.indexOf('_')
	return if (ix == -1) s else s.substring(0, ix)
    }

    class MyThread : Thread() {
	override fun run() {
	    SundryUtils.m_sleep(5000)
	    putXML(hm_new, "T_new_" + lang_country)
	    mythread = null
	}
    }

    init {
	var la = OmegaContext.omega_lang
	Log.getLogger().info("T lang p is from omega_lang $la")
	if (la == null) {
	    la = System.getProperty("lang")
	    Log.getLogger().info("T lang p is from -Domega_lang $la")
	}
	Log.getLogger().info("omega gui lang is now $la")
	val loc: Locale
	if (la != null) {
	    lang = before_(la)
	    lang_country = la
	} else {
	    loc = Locale.getDefault()
	    Log.getLogger().info(":--: locale $loc")
	    lang = loc.language
	    lang_country = loc.language + '_' + loc.country
	    if ("no" == lang) {
		Log.getLogger().info("no -> nb $loc")
		lang = "nb"
		lang_country = "nb_NO"
	    }
	}
	Log.getLogger().info("FINALLY omega lang " + OmegaContext.omega_lang)
	Log.getLogger().info("FINALLY lang " + lang)
	Log.getLogger().info("FINALLY lang_country " + lang_country)
    }

    companion object {
	var lang = "en"
	var hm: HashMap<String, String>? = null
	var hm_new: HashMap<String, String>? = null
	var lang_country = "en_US"
	var tt = T()
	private fun fopen(fn: String, fn2: String?, who: IntArray): FileInputStream? {
	    who[0] = -1
	    var `in`: FileInputStream? = null
	    try {
		`in` = FileInputStream(fn)
		Log.getLogger().info(":--: T file is $fn")
		who[0] = 0
	    } catch (ex: FileNotFoundException) {
		try {
		    if (fn2 != null) {
			`in` = FileInputStream(fn2)
			Log.getLogger().info(":--: T file is $fn2")
			who[0] = 1
		    }
		} catch (ex2: FileNotFoundException) {
		    return null
		}
	    }
	    return `in`
	}

	fun fillFrom(fn: String, fn2: String, hm_: HashMap<String, String>?): Int {
	    val who = IntArray(1)
	    try {
		var `in`: FileInputStream? = OmegaContext.t9n(fn)?.let { fopen(it, fn2, who) } ?: return -1
		var ir = InputStreamReader(`in`)
		var br = BufferedReader(ir)
		var enc = br.readLine()
		if ("utf-8" == enc) Log.getLogger().info(":--: T_ enc is $fn $fn2 $enc") else enc = null
		`in` = fopen(fn, fn2, who)
		ir = if (enc == null) InputStreamReader(`in`) else InputStreamReader(`in`, enc)
		br = BufferedReader(ir)
		br.readLine()
		try {
		    var cnt = 0
		    while (true) {
			val s = br.readLine()
			if (s == null) {
			    Log.getLogger().info(":--: " + "rL: null")
			    break
			}
			val sa = s.split("[\\]\\[]+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			if (cnt < 10) Log.getLogger().info(":--: " + "got T_ " + sa.size + ' ' + SundryUtils.a2s(sa))
			if (sa.size == 3) hm_!![sa[1]] = sa[2] else Log.getLogger()
			    .info(":--: " + "t9n.T.t strange " + SundryUtils.a2s(sa))
			cnt++
		    }
		    br.close()
		} catch (ex: IOException) {
		}
	    } catch (ex: IOException) {
		Log.getLogger().info("ERR: T_ $ex")
	    }
	    return who[0]
	}

	fun fillFromXML(fn: String, fn2: String?, hm_: HashMap<String, String>?): Int {
	    try {
		val who = IntArray(1)
		val `in` =
		    OmegaContext.t9n("$fn.xml")?.let { fopen(it, OmegaContext.t9n("$fn2.xml"), who) } ?: return -1
		val ir = InputStreamReader(`in`)
		val d = XMLDecoder(BufferedInputStream(`in`))
		val result_ = d.readObject()
		d.close()
		if (result_ !is HashMap<*, *>) {
		    return -1
		}
		Log.getLogger().info(":--: " + "T_xml " + result_.size)
		hm_!!.putAll(result_ as Map<out String, String>)
		return who[0]
	    } catch (ex: Exception) {
		Log.getLogger().info("ERR: T_ $ex")
	    }
	    return -1
	}

	@Synchronized
	fun init() {
	    if (hm == null) {
		hm = HashMap<String, String>()
		val whox = fillFromXML("T_" + lang_country, "T_" + lang, hm)
		if (whox == -1) {
		    val who = fillFrom("T_" + lang_country, "T_" + lang, hm)
		    if (whox == -1 && who != -1) putXML(hm, if (who == 0) "T_" + lang_country else "T_" + lang)
		}
	    }
	}

	private fun putEncoding() {
	    val f = File(OmegaContext.t9n("T_new_" + lang_country))
	    if (f.exists() && f.length() > 0) return
	    val pw = SundryUtils.createPrintWriter(OmegaContext.t9n("T_new"))
	    pw!!.println("utf-8")
	    pw.close()
	}

	@Synchronized
	private fun putXML(hm: HashMap<*, *>?, fn: String) {
	    try {
		val e = XMLEncoder(BufferedOutputStream(FileOutputStream(OmegaContext.t9n("$fn.xml"))))
		e.writeObject(hm)
		e.close()
	    } catch (ex: Exception) {
		Log.getLogger().info("ERR: $ex")
	    }
	}

	var mythread: MyThread? = null

	fun t(s: String): String {
	    init()
	    if (hm == null) return s
	    if (s.length == 0) return s
	    var ss = hm!![s]
	    if (ss == null) {
		if (hm_new == null) {
		    hm_new = HashMap<String, String>()
		    fillFromXML("T_new_" + lang_country, null, hm_new)
		}
		ss = hm_new!![s]
		if (ss == null) {
		    ss = s
		    hm!![s] = ss
		    hm_new!![s] = ss
		    if (mythread == null) {
			mythread = MyThread()
			mythread!!.start()
		    }
		    // 		PrintWriter pw = SundryUtils.createPrintWriter("T_new", true);
// 		pw.println("[" + s + "][" + s + "]");
// 		pw.flush();
// 		pw.close();
//log		OmegaContext.sout_log.getLogger().info(":--: " + "T.t " + s);
		}
	    } else {
		//	    OmegaContext.sout_log.getLogger().info(":--: " + "Tt " + s + ' ' + ss);
	    }
	    return ss
	}
    }
}
