package com.femtioprocent.omega.xml

import com.femtioprocent.omega.util.Log.getLogger

class XML internal constructor() {
    fun test() {
	val xn = Element("test")
	val pcd = PCDATA()
	pcd.add("hello world")
	xn.add(pcd)
	var xn2 = Element("empty")
	xn.add(xn2)
	xn2 = Element("pcdata", "PCDATA")
	xn.add(xn2)
	xn.add(Element("notempty").also { xn2 = it })
	xn2.add(PCDATA("first text"))
	xn2 = Element("emptyA")
	xn2.addAttr("attr", "value")
	xn2.addAttr("attr2", "value2")
	xn.add(xn2)
	xn.add(Element("notemptyA").also { xn2 = it })
	xn2.addAttr("attr3", "value3")
	xn2.addAttr("attr4", "value4")
	xn2.add(PCDATA("last text"))
	val sbu = StringBuffer()
	val sbl = StringBuffer()
	xn.render(sbu, sbl)
	getLogger().info(sbu.toString())
    }

    companion object {
	@JvmStatic
	fun main(args: Array<String>) {
	    val xml = XML()
	    xml.test()
	}
    }
}
