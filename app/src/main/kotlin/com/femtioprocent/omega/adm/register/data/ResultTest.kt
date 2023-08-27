package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.xml.Element
import com.femtioprocent.omega.xml.SAX_node
import java.text.DateFormat
import java.util.*

class ResultTest : Result {
    override var type: String
    var l_id: String
    var created: Date? = null
    var pupil: String
    var session_length: Long = 0
    var entries: MutableList<Entry>

    constructor(pupil: String, l_id: String, type: String) {
	this.pupil = pupil
	this.l_id = l_id
	this.type = type
	created = Date()
	entries = ArrayList()
    }

    constructor(pupil: String, l_id: String, type: String, fname: String) {
	this.pupil = pupil
	this.l_id = l_id
	this.type = type
	entries = ArrayList()
	load(fname)
    }

    fun load(fname: String) {
	val el = SAX_node.parse(fname, false)
	if (el != null) {
	    val ent = el.findElement("entries", 0)
	    if (ent != null) {
		val s = ent.findAttr("session_length")
		if (s != null) session_length = s.toLong()
		run loop@{
		    generateSequence(0) { it + 1 }.forEach {
			val entry = ent.findElement("entry", it) ?: return@loop
			val e = Entry.create(entry)
			e?.let { entries.add(it) }
		    }
		}
	    }
	}
    }

    fun add(e: Entry) {
	e.ord = howManyTestEntries()
	entries.add(e)
    }

    fun howManyTestEntries(): Int {
	return entries.size
    }

    override val lessonName: String
	get() = "$l_id-$type"
    override val performDate: Date
	get() = Date()

    fun fixString(s: String): String {
	return s.replace(':', '_').replace(' ', '_')
    }

    fun getEntry(ix: Int): Entry {
	return entries[ix]
    }

    val entrySize: Int
	get() = entries.size

    fun getEntrySize(type: String): Int {
	var c = 0
	entries.forEach {e -> if (e.type == type) c++ }
	return c
    }

    override var element: Element? = null
	get() {
	    return Element("result").also { el ->
		el.addAttr("pupil", pupil)
		el.addAttr("type", type)
		val df = DateFormat.getDateTimeInstance()
		el.addAttr("created_date", fixString("" + df.format(created)))
		val eel = Element("entries")
		eel.addAttr("session_length", "" + session_length)
		entries.forEach { e -> eel.add(e.element) }
		el.add(eel)
	    }
	}

    fun dump() {
	Log.getLogger().info(":--: sl-$session_length")
	Log.getLogger().info(":--: ty-$type")
	entries.forEach {e -> Log.getLogger().info(":--: ==  $e") }
    }
}
