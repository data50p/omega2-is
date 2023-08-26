package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.xml.Element

class TestEntry : Entry {
    var extra: String? = null
    var sentence: String
    var answer: String
    var duration: Int
    var cnt_correct_words: String
    var l_id_list: String

    constructor(
	    extra: String?,
	    sentence: String,
	    answer: String,
	    duration: Int,
	    cnt_correct_words: String,
	    l_id_list: String
    ) {
	this.sentence = sentence
	this.answer = answer
	this.duration = duration
	this.cnt_correct_words = cnt_correct_words
	this.l_id_list = l_id_list
	type = "test"
    }

    constructor(e: Element) {
	type = "test"
	//  	String s = e.findAttr("type");
//  	type = s;
	var s = e.findAttr("extra")
	extra = s
	s = e.findAttr("sentence")
	sentence = s!!
	s = e.findAttr("answer")
	answer = s!!
	s = e.findAttr("ord")
	ord = s!!.toInt()
	s = e.findAttr("duration")
	duration = s!!.toInt()
	s = e.findAttr("cnt_correct_words")
	cnt_correct_words = s!! // Integer.parseInt(s);
	s = e.findAttr("l_id_list")
	l_id_list = s!!
    }

    override val element: Element
	get() {
	    return Element("entry").also {
		it.addAttr("type", type)
		it.addAttr("extra", extra)
		it.addAttr("sentence", sentence)
		it.addAttr("answer", answer)
		it.addAttr("ord", "" + ord)
		it.addAttr("duration", "" + duration)
		it.addAttr("cnt_correct_words", "" + cnt_correct_words)
		it.addAttr("l_id_list", "" + l_id_list)
	    }
	}

    override fun toString(): String {
	return "TestEntry{" +
		"extra" + extra +
		", sentence" + sentence +
		", answer" + answer +
		", ord" + "" + ord +
		", duration" + "" + duration +
		", cnt_correct_words" + "" + cnt_correct_words +
		", l_id_list" + "" + l_id_list +
		"}"
    }
}
