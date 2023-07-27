package com.femtioprocent.omega.xml

class PCDATA : Node {
    var pcdata: StringBuffer

    constructor() {
	pcdata = StringBuffer()
    }

    constructor(s: String?) {
	pcdata = StringBuffer()
	add(s)
    }

    fun add(s: String?) {
	pcdata.append(s)
    }

    fun add(ca: CharArray?, offs: Int, len: Int) {
	pcdata.append(ca, offs, len)
    }

    val string: String
	get() = pcdata.toString()

    override fun render(sbu: StringBuffer, sbl: StringBuffer?) {
	sbu.append(pcdata)
    }
}
