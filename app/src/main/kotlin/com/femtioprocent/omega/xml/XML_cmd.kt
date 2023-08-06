package com.femtioprocent.omega.xml

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import com.femtioprocent.omega.util.SundryUtils.split
import java.io.IOException
import java.util.*

// file cmd args...
// extract tag
object XML_cmd {
    @Throws(IOException::class)
    @JvmStatic
    fun main(argv: Array<String>) {
	val flag: HashMap<String, String> = flagAsMap(argv)
	val argl = argAsList(argv)
	if (argl.size == 0 || argl[0] == "help") {
	    OmegaContext.sout_log.getLogger().info("xmlfile extract element")
	    OmegaContext.sout_log.getLogger().info("xmlfile remove  element[,element2]*")
	    OmegaContext.sout_log.getLogger().info("xmlfile sort    element")
	    OmegaContext.sout_log.getLogger().info("xmlfile sort    element attr")
	    return
	}
	OmegaContext.sout_log.getLogger().info(":--: argl $argl $flag")
	val cmd = argl[1]
	var validating = true
	if (flag["n"] != null) validating = false
	val el: Element? = SAX_node.Companion.parse(argl[0], validating)
	if (cmd == "extract") {
	    val tag = argl[2]
	    val li = el!!.find(tag)
	    val eel = Element("extract")
	    eel.addAttr("found_tag", tag)
	    eel.addAttr("found_items", "" + li.size)
	    try {
		XML_PW(System.out).use { xmlpw ->
		    xmlpw.push(eel)
		    xmlpw.put("\n")
		    li.forEach { xmlpw.put(it) }
		    xmlpw.pop()
		}
	    } catch (ex: Exception) {
		ex.printStackTrace()
	    }
	}
	if (cmd == "insert") {
	}
	if (cmd == "remove") {
	    val tag = argl[2]
	    val tags = split(tag, ",")
	    val rel = el!!.remove(tags)
	    val eel = Element("remove")
	    eel.addAttr("removed_tag", tag)
	    try {
		XML_PW(System.out).use { xmlpw ->
		    xmlpw.push(eel)
		    xmlpw.put("\n")
		    xmlpw.put(rel)
		    xmlpw.pop()
		}
	    } catch (ex: Exception) {
		ex.printStackTrace()
	    }
	}

	// sort findElement sortElement
	// sort findElement sortElement sortAttrib
	if (cmd == "--sort") {
	}

	// sort findElement sortAttr
	if (cmd == "sort") {
	    val tag = argl[2]
	    var a: String? = null
	    if (argl.size > 3) a = argl[3]
	    val fa = a
	    val li = el!!.find(tag)
	    val oa = li.toTypedArray<Element?>()
	    Arrays.sort<Element?>(oa) { o1, o2 ->
		val e1 = o1 as Element
		val e2 = o2 as Element
		val s1: String?
		val s2: String?
		if (fa != null) {
		    s1 = e1.attr[fa]
		    s2 = e2.attr[fa]
		} else {
		    val sb1 = StringBuffer()
		    val sb2 = StringBuffer()
		    e1.render(sb1)
		    e2.render(sb2)
		    s1 = sb1.toString()
		    s2 = sb2.toString()
		}
		s1!!.compareTo(s2!!)
		//  		    int i1 = Integer.parseInt(s1);
//  		    int i2 = Integer.parseInt(s2);
//  		    return i1 - i2; // s1.compareTo(s2);
	    }
	    val sli = Arrays.asList(*oa)
	    val eel = Element("sort")
	    eel.addAttr("sorted_tag", tag)
	    eel.addAttr("sorted_attr", a)
	    eel.addAttr("sorted_items", "" + sli.size)
	    try {
		XML_PW(System.out).use { xmlpw ->
		    xmlpw.push(eel)
		    xmlpw.put("\n")
		    sli.forEach { xmlpw.put(it) }
		    xmlpw.pop()
		}
	    } catch (ex: Exception) {
		ex.printStackTrace()
	    }
	}
    }
}
