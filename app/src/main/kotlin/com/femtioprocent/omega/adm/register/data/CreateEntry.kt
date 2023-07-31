package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.xml.Element

class CreateEntry : Entry {
    var sentence: String
    var duration: Int
    var l_id_list: String

    constructor(sentence: String, duration: Int, l_id_list: String) {
	this.sentence = sentence
	this.duration = duration
	this.l_id_list = l_id_list
	type = "create"
    }

    constructor(e: Element) {
//  	String s = e.findAttr("type");
//  	type = s;
	type = "create"
	sentence = e.findAttr("sentence")!!
	ord = e.findAttr("ord")!!.toInt()
	duration = e.findAttr("duration")!!.toInt()
	l_id_list = e.findAttr("l_id_list")!!
    }

    override val element: Element
	get() {
	    val el = Element("entry")
	    el.addAttr("type", "create")
	    el.addAttr("sentence", sentence)
	    el.addAttr("ord", "" + ord)
	    el.addAttr("duration", "" + duration)
	    el.addAttr("l_id_list", "" + l_id_list)
	    return el
	}

    override fun toString(): String {
	return "CreateEntry{, sentence: $sentence, ord: $ord, duration: $duration, l_id_list: $l_id_list}"
    }
}
