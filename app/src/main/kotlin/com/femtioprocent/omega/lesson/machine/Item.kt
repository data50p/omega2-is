package com.femtioprocent.omega.lesson.machine

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.empty
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.value.Values
import com.femtioprocent.omega.xml.Element

class Item {
    var it_ent: ItemEntry? = null
    var ord: Int
    var text: String? = null
	private set
    var text_Orig: String? = null
	private set
    var tTS: String? = null
	private set
    var tTS_Orig: String? = null
	private set
    private var dummytext_orig: String? = null
    var lid: String? = null
	private set
    var lid_Orig: String? = null
	private set
    var `var`: MutableList<String?>
    var sign: String? = null
    var sign_Orig: String? = null
	private set
    var sound: String? = null
    var sound_Orig: String? = null
	private set
    private var dummysound_orig: String? = null
    private var dummysign_orig: String? = null
    var dummyText: String? = null
	private set
    var dummySound: String? = null
	private set
    var dummySign: String? = null
	private set
    private var saved_dummytext: String? = null
    private var saved_dummysound: String? = null
    private var saved_dummysign: String? = null
    var isDummySpaceAllocated = false // if replace in self
	private set
    var dummy_extra = -1 // if extra dummy
    var isAction = false
    var action_type: String? = null
    var action_fname: String? = null
    var action_fname_orig: String? = null
    var dummy_slot = false

    constructor(txt: String?) {
	ord = 0
	`var` = ArrayList()
	text = txt
	text_Orig = java.lang.String(txt) as String
	tTS = ""
	tTS_Orig = ""
	lid = ""
	lid_Orig = ""
	sound = ""
	sound_Orig = ""
	sign = ""
	sign_Orig = ""
	dummyText = ""
	dummySound = ""
	dummySign = ""
	saved_dummytext = ""
	saved_dummysound = ""
	saved_dummysign = ""
	repeat(6) {
	    `var`.add("")
	}
	isAction = false
    }

    internal constructor(ord: Int, el: Element) {
	this.ord = ord
	`var` = ArrayList()
	load(el)
    }

    internal constructor(txt: String?, isAction_: Boolean) : this(txt) {
	isAction = true
	action_type = ""
	action_fname = ""
	action_fname_orig = action_fname
    }

    internal constructor(ord: Int, el: Element, isAction_: Boolean) : this(ord, el) {
	isAction = true
	action_type = el.findAttr("action-type")
	val action_fname_ = el.findAttr("action-fname")
	action_fname_orig = action_fname_
	action_fname = krull(action_fname_!!)
    }

    fun setDefaultAction() {
	setDefaultAction(false)
    }

    fun setDefaultAction(empty: Boolean) {
	if (action_fname_orig!!.length == 0 || empty) {
	    action_type = "omega_anim"
	    val action_fname_ = if (empty) "" else "anim/AnimTemplates/ActorActsWith"
	    action_fname_orig = action_fname_
	    action_fname = krull(action_fname_)
	}
    }

    private fun onlynumeric(s: String): Boolean {
	for (i in 0 until s.length) {
	    val ch = s[i]
	    if (ch < '0' || ch > '9') return false
	}
	return true
    }

    private fun isAlpha(ch: Char): Boolean {
	return Character.isLetter(ch)
	// 	return ch >= 'a' && ch <= 'z' ||
// 	    ch >= 'A' && ch <= 'Z';
    }

    // {nn} where nn is digit -> unicode
    // {-2} {+2:abc} -> variable subst
    // {lesson_name.path_lid.part:def} story chaining
    protected fun krull(s: String): String {
	try {
	    val ix = s.indexOf('{')
	    if (ix == -1 || ix + 1 >= s.length) return s
	    if (!isAlpha(s[ix + 1])) {     // xxx{yy}zzz
		val ix2 = s.indexOf('}')
		if (ix2 == -1) return s
		val s1 = s.substring(0, ix) // xxx
		val s2 = s.substring(ix, ix2 + 1) // {yy}
		val s3 = s.substring(ix2 + 1) // zzz
		return s1 + s2 + krull(s3)
	    }
	    val ix2 = s.indexOf('}')
	    if (ix2 == -1) return s
	    val s1 = s.substring(0, ix)
	    val s2 = s.substring(ix2 + 1)
	    val ks = s.substring(ix + 1, ix2)
	    val sa = split(ks, ":")
	    val story_hm = Lesson.story_hm
	    Log.getLogger().info("match $s from $story_hm")
	    val s_li = story_hm[sa[0]]
	    if (s_li!!.asString == null) {
		val ns = if (sa.size > 1) sa[1] else ""
		Log.getLogger().info("${s1 + ns + krull(s2)} -> $ks¶$s1¶$ns¶$s2")
		return s1 + ns + krull(s2)
	    }
	    s_li.asString?.let {
		val ns = s_li.asString
		Log.getLogger().info("-> $ks¶$s1¶$ns¶$s2")
		return s1 + ns + krull(s2)
	    }
	} catch (ex: Exception) {
	}
	return s
    }

    fun decode(raw_text: String?): String {
	if (raw_text == null) return ""
	val sb = StringBuffer()
	var i = 0
	while (i < raw_text.length) {
	    val ch = raw_text[i]
	    if (ch == '{') {
		var v = 0
		for (ii in 1..76) {
		    val ch2 = raw_text[i + ii]
		    if (ch2 == '}') {
			val kr_s = raw_text.substring(i + 1, i + ii)
			//log			OmegaContext.sout_log.getLogger().info(":--: " + "got text kr_s: " + kr_s);
			if (onlynumeric(kr_s)) {
			    sb.append(v.toChar())
			    i += ii
			    break
			} else {
			    sb.append("{$kr_s}")
			    i += ii
			    break
			}
		    }
		    v = v * 10 + (ch2.code - '0'.code)
		}
	    } else sb.append(ch)
	    i++
	}
	return sb.toString()
    }

    fun fel(el: Element, key: String?): String {
	return el.findAttr(key) ?: ""
    }

    private fun load(el: Element) {
	val raw_text = fel(el, "text")
	val text = decode(raw_text)
	val textid = fel(el, "textid")
	text_Orig = java.lang.String(text) as String
	val raw_dummytext = fel(el, "dummytext")
	val dummytext = decode(raw_dummytext)
	dummytext_orig = java.lang.String(dummytext) as String
	val raw_tts = fel(el, "tts")
	val tts = decode(raw_tts)
	tTS_Orig = java.lang.String(tts) as String
	var lid = el.findAttr("Lid")
	lid_Orig = lid
	if (lid == null) lid = ""
	val sound = fel(el, "sound")
	sound_Orig = sound
	val dummysound = fel(el, "dummysound")
	dummysound_orig = dummysound
	val sign = fel(el, "sign")
	sign_Orig = sign
	val dummysign = fel(el, "dummysign")
	dummysign_orig = dummysign
	this.sound = krull(sound)
	this.sign = krull(sign)
	this.text = krull(text)
	tTS = tts
	this.lid = krull(lid)
	dummyText = krull(dummytext)
	dummySound = krull(dummysound)
	dummySign = krull(dummysign)
	saved_dummytext = dummytext
	saved_dummysound = dummysound
	saved_dummysign = dummysign


//log	OmegaContext.sout_log.getLogger().info(":--: " + "NEW TXTXO " + text + ' ' + text_orig);
	`var`.add("")
	for (i in 1..5)
	    `var`.add(el.findAttr("var-$i") ?: "")
    }

    val defaultFilledText: String?
	get() {    // DUMMY?
	    var s = text
	    if (isDummySpaceAllocated) s = dummyText
	    if (s == null || s.length == 0) s = text
	    return getDefaultFilledText(s)
	}
    val defaultFilledTTS: String?
	get() {    // DUMMY?
	    var s = tTS
	    if (isDummySpaceAllocated) s = dummyText
	    if (s == null || s.length == 0) s = text
	    return getDefaultFilledTTS(s)
	}

    fun getDefaultFilledText(s: String?): String? {
	return try {
	    val ix = s!!.indexOf('{')
	    if (ix == -1) return s
	    val ix2 = s.indexOf('}')
	    val ix3 = s.indexOf(':')
	    if (ItemEntry.isPeTask(s) && Lesson.edit) {
		return s
	    }
	    if (ix3 != -1 && ix3 < ix2 && ix2 > 0) s.substring(0, ix) + s.substring(
		    ix3 + 1,
		    ix2
	    ) + getDefaultFilledText(s.substring(ix2 + 1)) else s.substring(0, ix) + getDefaultFilledText(
		    s.substring(
			    ix2 + 1
		    )
	    )
	} catch (ex: Exception) {
	    ""
	}
    }

    fun getDefaultFilledTTS(s: String?): String? {
	return try {
	    val ix = s!!.indexOf('{')
	    if (ix == -1) return s
	    val ix2 = s.indexOf('}')
	    val ix3 = s.indexOf(':')
	    if (ix3 != -1 && ix3 < ix2 && ix2 > 0) s.substring(0, ix) + s.substring(ix3 + 1, ix2) + getDefaultFilledTTS(
		    s.substring(ix2 + 1)
	    ) else s.substring(0, ix) + getDefaultFilledTTS(s.substring(ix2 + 1))
	} catch (ex: Exception) {
	    ""
	}
    }

    fun getVar(ix: Int): String? {
	return try {
	    `var`[ix]
	} catch (ex: IndexOutOfBoundsException) {
	    null
	}
    }

    fun setVar(ix: Int, s: String?) {
	try {
	    `var`[ix] = s
	} catch (ex: IndexOutOfBoundsException) {
	}
    }

    val entryTid: String
	get() = it_ent!!.tid!!

    fun getValues(orig: Boolean): Values {
	val vs = Values()
	vs.setStr("text", text_Orig)
	vs.setStr("tts", tTS_Orig)
	//	vs.setStr("text", text);
	vs.setStr("tid", it_ent!!.tid)
	vs.setStr("lid", lid_Orig)
	for (i in 0 until OmegaConfig.VAR_NUM) {
	    vs.setStr("v" + (i + 1), getVar(i + 1))
	}
	var s = "" + `var`
	s = s.substring(1, s.length - 1)
	vs.setStr("var", s)
	vs.setStr("sound", sound_Orig)
	vs.setStr("sign", sign_Orig)
	vs.setStr("ftype", "")
	vs.setStr("fname", "")
	vs.setStr("dummytext", dummytext_orig)
	vs.setStr("dummysound", dummysound_orig)
	vs.setStr("dummysign", dummysign_orig)
	if (isAction) {
	    vs.setStr("fname", action_fname_orig)
	    vs.setStr("ftype", action_type)
	}

//log	OmegaContext.sout_log.getLogger().info(":--: " + "Values is " + vs);
	return vs
    }

    fun encode(s: String?): String {
	if (s == null) return ""
	val sb = StringBuffer()
	for (i in 0 until s.length) {
	    val ch = s[i]
	    if (ch.code > 255) {
		sb.append("" + "{" + ch.code + "}")
	    } else {
		sb.append(ch)
	    }
	}
	return sb.toString()
    }

    val element: Element
	get() {
	    val el = Element("item")
	    el.addAttr("ord", "" + ord)
	    var t = encode(text_Orig)
	    el.addAttr("text", t)
	    t = encode(tTS_Orig)
	    el.addAttr("tts", t)
	    t = encode(dummytext_orig)
	    el.addAttr("dummytext", t)
	    el.addAttr("sound", sound_Orig)
	    el.addAttr("dummysound", dummysound_orig)
	    el.addAttr("sign", sign_Orig)
	    el.addAttr("dummysign", dummysign_orig)
	    if (lid != null && lid!!.length > 0) el.addAttr("Lid", lid_Orig)
	    for (i in 0 until OmegaConfig.VAR_NUM) el.addAttr("var-" + (i + 1), getVar(i + 1))
	    if (isAction) {
		el.addAttr("action-type", "" + action_type)
		el.addAttr("action-fname", action_fname_orig)
	    }
	    return el
	}
    val textD: String?
	get() =// DUMMY?
	    if (isDummySpaceAllocated && dummyText!!.length > 0) dummyText else text
    val tTSD: String?
	get() {              // DUMMY?
	    if (isDummySpaceAllocated && dummyText!!.length > 0) return dummyText
	    return if (empty(tTS)) text else tTS
	}
    val soundD: String?
	get() = if (isDummySpaceAllocated) dummySound else sound
    val signD: String?
	get() = if (isDummySpaceAllocated) dummySign else sign

    fun setText_Krull(s: String) {
	text_Orig = s
	text = krull(s)
    }

    fun setTTS_Krull(s: String) {
	tTS_Orig = s
	tTS = krull(s)
    }

    fun setDummyText_Krull(s: String?, saved: Boolean) {
	dummytext_orig = s
	dummyText = krull(s!!)
	if (saved) saved_dummytext = dummyText
    }

    fun setLid_Krull(s: String) {
	lid_Orig = s
	lid = krull(s)
    }

    fun setSign_Krull(s: String) {
	sign_Orig = s
	sign = krull(s)
    }

    fun setSound_Krull(s: String) {
	sound_Orig = s
	sound = krull(s)
    }

    fun setDummySound_Krull(s: String?, saved: Boolean) {
	dummysound_orig = s
	dummySound = krull(s!!)
	if (saved) saved_dummysound = dummySound
    }

    fun setDummySign_Krull(s: String?, saved: Boolean) {
	dummysign_orig = s
	dummySign = krull(s!!)
	if (saved) saved_dummysign = dummySign
    }

    fun allocateDummySpace(src_itm: Item) {
	if (src_itm.dummyText == null || src_itm.dummyText!!.length == 0) return
	setDummyText_Krull(src_itm.dummyText, false)
	setDummySound_Krull(src_itm.dummySound, false)
	setDummySign_Krull(src_itm.dummySign, false)
	isDummySpaceAllocated = true
    }

    fun setDummy(b: Boolean) {
	isDummySpaceAllocated = b
	//	OmegaContext.sout_log.getLogger().info(":--: " + "DUMMY dep_set to " + b + ' ' + dummytext + ' ' + text);
    }

    fun restoreSavedDummy() {
	dummyText = saved_dummytext
	dummySound = saved_dummysound
	dummySign = saved_dummysign
	if (dummy_extra != -1) text = "@_._"
	isDummySpaceAllocated = false
    }

    val actionText: String?
	get() {
	    if (action_fname == null || action_fname!!.length == 0 || action_type!!.length == 0) return null
	    val sa = split(action_fname, " ,")
	    var ss = ""
	    for (i in sa.indices) {
		if (i > 0) ss += ','
		val sx = sa[i] + '.' + action_type
		ss += sx
	    }
	    Log.getLogger().info(":--: return getActionText $ss")
	    return ss
	}

    fun setActionFile(fn: String) {
	val ix = fn.lastIndexOf('.')
	if (ix != -1) {
	    isAction = true
	    action_type = fn.substring(ix + 1)
	    action_fname_orig = fn.substring(0, ix)
	    action_fname = krull(action_fname_orig!!)
	}
    }

    fun setAction_Fname(s: String?, s2: String?) {
	if (s != null) {
	    isAction = true
	    action_type = s2
	    action_fname_orig = s
	    action_fname = krull(s)
	}
    }

    val actionFile: String?
	get() = actionText

    override fun toString(): String {
	try {
	    return "Item{" + ord +
		    ", isdummy=" + isDummySpaceAllocated +
		    ", text=" + text +
		    ", dummytext=" + dummyText +
		    ", text_orig=" + text_Orig +
		    ", sound=" + sound +
		    ", dummysound=" + dummySound +
		    ", sign=" + sign +
		    ", dummysign=" + dummySign +
		    ", Lid=" + lid +
		    ", var=" + `var` +
		    ", tid'=" + it_ent?.tid
	} catch (e: NullPointerException) {
	    Log.getLogger().info("While toString " + e)
	    e.printStackTrace()
	    return "Item{" + e + "}"
	}
    }
}
