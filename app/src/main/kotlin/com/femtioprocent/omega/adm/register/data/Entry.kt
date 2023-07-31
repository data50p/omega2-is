package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.xml.Element

abstract class Entry {
    var ord = 0

    @JvmField
    var type: String? = null
    abstract val element: Element
    override fun toString(): String {
	return "Entry{}"
    }

    companion object {
	fun create(entry: Element): Entry? {
	    val ty = entry.findAttr("type")
	    var e: Entry? = null
	    if ("select" == ty) e = SelectEntry(entry)
	    if ("test" == ty) e = TestEntry(entry)
	    if ("create" == ty) e = CreateEntry(entry)
	    return e
	}
    }
}
