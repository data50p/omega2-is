package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.xml.Element

class TestEntry : Entry {
    var extra: String? = null
    @JvmField
    var sentence: String
    @JvmField
    var answer: String
    @JvmField
    var duration: Int
    @JvmField
    var cnt_correct_words: String
    @JvmField
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
	sentence = s
	s = e.findAttr("answer")
	answer = s
	s = e.findAttr("ord")
	ord = s.toInt()
	s = e.findAttr("duration")
	duration = s.toInt()
	s = e.findAttr("cnt_correct_words")
	cnt_correct_words = s // Integer.parseInt(s);
	s = e.findAttr("l_id_list")
	l_id_list = s
    }

    public override val element: Element
	get() {
	    val el = Element("entry")
	    el.addAttr("type", type)
	    el.addAttr("extra", extra)
	    el.addAttr("sentence", sentence)
	    el.addAttr("answer", answer)
	    el.addAttr("ord", "" + ord)
	    el.addAttr("duration", "" + duration)
	    el.addAttr("cnt_correct_words", "" + cnt_correct_words)
	    el.addAttr("l_id_list", "" + l_id_list)
	    return el
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
