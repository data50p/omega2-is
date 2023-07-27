package com.femtioprocent.omega.xml

import com.femtioprocent.omega.util.SundryUtils.createPrintWriter
import java.io.*
import java.util.*

class XML_PW : AutoCloseable {
    var pw: PrintWriter? = null
    var st: Stack<String>
    var first_done = false
    var use_dtd = true

    constructor() {
	st = Stack()
    }

    constructor(dtd: Boolean) {
	st = Stack()
	use_dtd = dtd
    }

    constructor(os: OutputStream?) {
	pw = createPrintWriter(os)
	st = Stack()
    }

    constructor(pw: PrintWriter?) {
	this.pw = pw
	st = Stack()
    }

    constructor(pw: PrintWriter?, dtd: Boolean) {
	this.pw = pw
	st = Stack()
	use_dtd = dtd
    }

    fun ensureDTD(tag: String?) {
	val f = File("$tag.dtd")
	if (f.exists()) return
	try {
	    f.createNewFile()
	} catch (ex: IOException) {
	}
    }

    fun ensurePW(tag: String?) {
	if (pw == null) {
	    pw = createPrintWriter("$tag.xml")
	    try {
		throw Exception("trace")
	    } catch (ex: Exception) {
		try {
		    PrintWriter(File("logs/stack_trace")).use { pw -> ex.printStackTrace(pw) }
		} catch (e: FileNotFoundException) {
		    e.printStackTrace()
		}
	    }
	}
    }

    var dtd_sb: StringBuffer? = null
    private var lastE = true
    @JvmOverloads
    fun addDTD_E(tag: String, def: String? = null, cmnt: String? = null) {
	if (!lastE) addDTD("\n")
	addDTD("	<!ELEMENT " +
		tag +
		' ' +
		(def ?: "EMPTY") +
		">" +
		if (cmnt == null) "\n" else "  <!-- $cmnt -->\n")
	lastE = true
    }

    @JvmOverloads
    fun addDTD_A(tag: String, attr: String, req: Boolean = true, cmnt: String? = null) {
	addDTD("	<!ATTLIST " +
		tag + ' ' +
		attr + ' ' +
		"CDATA " +
		(if (req) "#REQUIRED" else "#IMPLIED") +
		">" +
		if (cmnt == null) "\n" else "  <!-- $cmnt -->\n")
	lastE = false
    }

    fun addDTD_A(tag: String, attr: String, kind: String, req: Boolean, cmnt: String?) {
	addDTD("	<!ATTLIST " +
		tag + ' ' +
		attr + ' ' +
		kind + ' ' +
		(if (req) "#REQUIRED" else "#IMPLIED") +
		">" +
		if (cmnt == null) "\n" else "  <!-- $cmnt -->\n")
	lastE = false
    }

    @JvmOverloads
    fun addDTD_A(tag: String, attr: String, `val`: Array<String>, def: String, cmnt: String? = null) {
	addDTD("	<!ATTLIST " +
		tag + ' ' +
		attr + ' ' +
		" (")
	for (i in `val`.indices) addDTD((if (i == 0) "" else " | ") + `val`[i])
	addDTD(") \"" + def + "\">" +
		if (cmnt == null) "\n" else "  <!-- $cmnt -->\n")
	lastE = false
    }

    fun addDTD(s: String?) {
	if (dtd_sb == null) dtd_sb = StringBuffer()
	dtd_sb!!.append(s)
    }

    var dTD: String?
	get() = if (dtd_sb == null) null else dtd_sb.toString()
	set(s) {
	    dtd_sb = StringBuffer()
	    dtd_sb!!.append(s)
	}

    fun first(tag: String?) {
	if (first_done == false) {
	    if (pw == null) ensurePW(tag)
	    pw!!.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
	    val dtd = dTD
	    if (dtd == null) if (use_dtd) pw!!.println("<!DOCTYPE $tag SYSTEM \"$tag.dtd\">") else pw!!.println("<!DOCTYPE $tag >") else pw!!.println("<!DOCTYPE $tag  [\n$dtd]>")
	    pw!!.println("")
	    if (dtd == null && use_dtd) ensureDTD(tag)
	    first_done = true
	}
    }

    fun push(el: Element?) {
	first(el!!.name)
	val sbu = StringBuffer()
	val sbl = StringBuffer()
	el.render(sbu, sbl)
	pw!!.print(sbu.toString())
	st.push(sbl.toString())
	el.setRO()
    }

    fun put(el: Element?) {
	push(el)
	pop(el)
    }

    fun putDT(tag: String?): Boolean {
	if (first_done) return false
	first(tag)
	return true
    }

    fun put(pcdata: PCDATA) {
	pw!!.print(pcdata.string)
    }

    fun put(s: String?) {
	pw!!.print(s)
    }

    fun popAll() {
	while (!st.isEmpty()) pop(null)
    }

    @JvmOverloads
    fun pop(el: Element? = null) {
	val o = st.pop()
	if (o is String) { // was StringBuffer
	    pw!!.print(o.toString())
	} else {
	    pw!!.println("<!-- FEL -->")
	}
    }

    fun flush() {
	pw!!.flush()
    }

    @Throws(Exception::class)
    override fun close() {
	popAll()
	flush()
	pw!!.close()
    }

    fun checkError(): Boolean {
	return pw!!.checkError()
    }
}
