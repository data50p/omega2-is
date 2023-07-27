package com.femtioprocent.omega.lesson.canvas

class MsgItem {
    @JvmField
    var title: String
    @JvmField
    var text: String
    @JvmField
    var text2: String? = null
    @JvmField
    var image: String? = null
    @JvmField
    var image2: String? = null
    @JvmField
    var small_title: String? = null
    @JvmField
    var type: Char

    constructor(title: String, txt: String) {
	this.title = title
	text = txt
	type = '2'
    }

    constructor(type: Char, title: String, txt: String?, txt2: String?, image: String?, image2: String?, small_title: String?) {
	this.type = type
	this.title = title
	text = txt!!
	text2 = txt2
	this.image = image
	this.image2 = image2
	this.small_title = small_title
    }

    override fun toString(): String {
	return "MsgItem{" + type +
		title + ',' +
		text + ',' +
		text2 + ',' + "}"
    }
}
