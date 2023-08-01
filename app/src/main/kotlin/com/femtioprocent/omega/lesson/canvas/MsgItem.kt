package com.femtioprocent.omega.lesson.canvas

data class MsgItem(val title: String, val txt: String) {
    var text: String
    var text2: String? = null
    var image: String? = null
    var image2: String? = null
    var small_title: String? = null
    var type: Char

    init {
	text = txt
	type = '2'
    }

    constructor(
	    type: Char,
	    title: String,
	    txt: String?,
	    txt2: String?,
	    image: String?,
	    image2: String?,
	    small_title: String?
    ) : this(title, txt!!) {
	this.type = type
	text2 = txt2
	this.image = image
	this.image2 = image2
	this.small_title = small_title
    }
}
