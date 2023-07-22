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
    @JvmField
    var session_length: Long = 0
    var entries: MutableList<Entry>
    override var firstPerformDate: Date

    constructor(pupil: String, l_id: String, type: String) {
	this.pupil = pupil
	this.l_id = l_id
	this.type = type
	created = Date()
	entries = ArrayList()
	firstPerformDate = Date()
    }

    constructor(pupil: String, l_id: String, type: String, fname: String?) {
	this.pupil = pupil
	this.l_id = l_id
	this.type = type
	entries = ArrayList()
	firstPerformDate = Date()
	load(fname)
    }

    fun load(fname: String?) {
	val el = SAX_node.parse(fname, false)
	if (el != null) {
	    val ent = el.findElement("entries", 0)
	    if (ent != null) {
		val s = ent.findAttr("session_length")
		if (s != null) session_length = s.toLong()
		var i = 0
		while (true) {
		    val entry = ent.findElement("entry", i) ?: break
		    val e = Entry.create(entry)
		    e?.let { entries.add(it) }
		    i++
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

    val allTestEntries: Iterator<*>
	get() = entries.iterator()
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
	val it: Iterator<*> = entries.iterator()
	while (it.hasNext()) {
	    val e = it.next() as Entry
	    if (e.type == type) c++
	}
	return c
    }

    override var element: Element? = null
	get() {
	    val el = Element("result")
	    el.addAttr("pupil", pupil)
	    el.addAttr("type", type)
	    val df = DateFormat.getDateTimeInstance()
	    el.addAttr("created_date", fixString("" + df.format(created)))
	    val eel = Element("entries")
	    eel.addAttr("session_length", "" + session_length)
	    val it: Iterator<*> = entries.iterator()
	    while (it.hasNext()) {
		val e = it.next() as Entry
		val e1 = e.element
		eel.add(e1)
	    }
	    el.add(eel)
	    return el
	}

    fun dump() {
	Log.getLogger().info(":--: sl-$session_length")
	Log.getLogger().info(":--: ty-$type")
	val it: Iterator<*> = entries.iterator()
	while (it.hasNext()) {
	    val e = it.next() as Entry
	    Log.getLogger().info(":--: ==  $e")
	}
    }
}
