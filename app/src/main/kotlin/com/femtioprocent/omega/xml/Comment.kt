package com.femtioprocent.omega.xml

class Comment : Node {
    var comment: StringBuffer

    constructor() {
	comment = StringBuffer()
    }

    constructor(s: String?) {
	comment = StringBuffer()
	add(s)
    }

    fun add(s: String?) {
	comment.append(s)
    }

    fun add(ca: CharArray?, offs: Int, len: Int) {
	comment.append(ca, offs, len)
    }

    val string: String
	get() = comment.toString()

    override fun render(sbu: StringBuffer, sbl: StringBuffer?) {
	sbu.append("<!-- ")
	sbu.append(comment)
	sbu.append("-->")
    }
}
