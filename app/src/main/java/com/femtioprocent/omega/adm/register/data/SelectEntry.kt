package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.xml.Element

class SelectEntry : Entry {
    @JvmField
    var extra: String
    @JvmField
    var word: String
    @JvmField
    var `when`: Int
    @JvmField
    var l_id: String

    constructor(extra: String, word: String, `when`: Int, l_id: String) {
	this.extra = extra
	this.word = word
	this.`when` = `when`
	this.l_id = l_id
	type = "select"
    }

    constructor(e: Element) {
//  	String s = e.findAttr("type");
//  	type = s;
	type = "select"
	var s = e.findAttr("extra")
	extra = s
	s = e.findAttr("word")
	word = s
	s = e.findAttr("ord")
	ord = s.toInt()
	s = e.findAttr("when")
	`when` = s.toInt()
	s = e.findAttr("l_id")
	l_id = s
    }

    override val element: Element
	get() {
	    val el = Element("entry")
	    el.addAttr("type", "select")
	    el.addAttr("extra", extra)
	    el.addAttr("ord", "" + ord)
	    el.addAttr("word", "" + word)
	    el.addAttr("when", "" + `when`)
	    el.addAttr("l_id", "" + l_id)
	    return el
	}

    override fun toString(): String {
	return "SelectEntry{" +
		"extra" + extra +
		", word" + word +
		", ord" + "" + ord +
		", when" + "" + `when` +
		", l_id" + "" + l_id +
		"}"
    }
}
