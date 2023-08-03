package com.femtioprocent.omega.xml

import java.util.*

class Element(var name: String) : Node(), Cloneable {
    var attr: SortedMap<String, String>
    var content: MutableList<Node?>
    var hasSubElement = false
    var nx_ix = 0
    var ro = false

    constructor(name: String, xn: Node?) : this(name) {
	add(xn)
    }

    constructor(name: String, s: String?) : this(name) {
	add(s)
    }

    fun setRO() {
	ro = true
    }

    // merge >1 PCNODE
    fun add(xn: Node?) {
	if (ro) throw RuntimeException("Element readonly")
	if (xn is Element) hasSubElement = true
	content.add(xn)
    }

    fun add(s: String?) {
	if (ro) throw RuntimeException("Element readonly")
	content.add(PCDATA(s))
    }

    fun addAttr(key: String?, `val`: String?) {
	if (ro) throw RuntimeException("Element readonly")
	attr[key] = `val`
    }

    fun subAttr(key: String?) {
	if (ro) throw RuntimeException("Element readonly")
	attr.remove(key)
    }

    @Synchronized
    fun addAttr_nx(key: String, `val`: String) {
	if (ro) throw RuntimeException("Element readonly")
	attr["n" + nx_ix++] = "$key=$`val`"
    }

    @Synchronized
    fun addAttr_nx(ht: Hashtable<*, *>) {
	if (ro) throw RuntimeException("Element readonly")
	val it: Iterator<*> = ht.keys.iterator()
	while (it.hasNext()) {
	    val k = it.next() as String
	    val v = ht[k] as String?
	    attr["n" + nx_ix++] = "$k=$v"
	}
    }

    fun find(nm: String): List<Element> {
	val li: MutableList<Element> = ArrayList()
	val it: Iterator<Node?> = content.iterator()
	while (it.hasNext()) {
	    val n = it.next()
	    if (n is Element) {
		val e = n
		if (e.name == nm) {
		    li.add(e)
		} else {
		    val li1 = e.find(nm)
		    if (li1.size > 0) li.addAll(li1)
		}
	    }
	}
	return li
    }

    private fun isIn(s: String, sa: Array<String>): Boolean {
	for (i in sa.indices) if (s == sa[i]) return true
	return false
    }

    fun remove(nm: String): Element? { // <Element>
	if (name == nm) return null
	val nv: MutableList<Node?> = ArrayList()
	val it: Iterator<Node?> = content.iterator()
	while (it.hasNext()) {
	    val n = it.next()
	    if (n is Element) {
		var e = n as Element?
		if (e!!.name == nm) {
		} else {
		    e = e.remove(nm)
		    nv.add(e)
		}
	    } else {
		nv.add(n)
	    }
	}
	try {
	    val cel = clone() as Element
	    cel.content = nv
	    return cel
	} catch (ex: CloneNotSupportedException) {
	}
	return null
    }

    fun remove(names: Array<String>): Element? {
	if (isIn(name, names)) return null
	val nv: MutableList<Node?> = ArrayList()
	val it: Iterator<Node?> = content.iterator()
	while (it.hasNext()) {
	    val n = it.next()
	    if (n is Element) {
		var e = n as Element?
		if (isIn(e!!.name, names)) {
		} else {
		    e = e.remove(names)
		    nv.add(e)
		}
	    } else {
		nv.add(n)
	    }
	}
	try {
	    val cel = clone() as Element
	    cel.content = nv
	    return cel
	} catch (ex: CloneNotSupportedException) {
	}
	return null
    }

    //      public void sortByAttrib(final String attrib) {
    //  	Object oao[] = content.toArray();
    //  	Object oa[] = (Object[])oao.clone();
    //  	Arrays.sort(oa, new Comparator() {
    //  	    public int compare(Object o1, Object o2) {
    //  		Element e1 = (Element)o1;
    //  		Element e2 = (Element)o2;
    //  		String s1;
    //  		String s2;
    //  		s1 = (String)e1.attr.get(attrib);
    //  		s2 = (String)e2.attr.get(attrib);
    //  		return s1.compareTo(s2);
    //  	    }
    //  	});
    //  	content = Arrays.asList(oa);
    //      }
    //      public void sortByContent() {
    //  	Object oao[] = content.toArray();
    //  	Object oa[] = (Object[])oao.clone();
    //  	Arrays.sort(oa, new Comparator() {
    //  		// optimize by putting SB in hashmap
    //  	    public int compare(Object o1, Object o2) {
    //  		Node e1 = (Node)o1;
    //  		Node e2 = (Node)o2;
    //  		String s1;
    //  		String s2;
    //  		StringBuffer sb1 = new StringBuffer();
    //  		StringBuffer sb2 = new StringBuffer();
    //  		e1.render(sb1);
    //  		e2.render(sb2);
    //  		s1 = sb1.toString();
    //  		s2 = sb2.toString();
    //  		return s1.compareTo(s2);
    //  	    }
    //  	});
    //  	content = Arrays.asList(oa);
    //      }
    fun findFirstElement(nm: String): Element? {
	return findElement(nm, 0)
    }

    fun findElement(nm: String, ix: Int): Element? {
	var ix = ix
	val it: Iterator<Node?> = content.iterator()
	while (it.hasNext()) {
	    val n = it.next()
	    if (n is Element) {
		val e = n
		if (e.name == nm) {
		    if (ix == 0) return e else ix--
		} else {
		    val el1 = e.findElement(nm, ix)
		    if (el1 != null) return el1
		}
	    }
	}
	return null
    }

    fun findElement2(nm: String?): Element? { // nm = name/name2[nix]/name3[@attr]
	return null
    }

    fun findAttr(an: String?): String? {
	return attr[an]
    }

    fun findPCDATA(): String? {
	val it: Iterator<Node?> = content.iterator()
	while (it.hasNext()) {
	    val n = it.next()
	    if (n is PCDATA) {
		return n.pcdata.toString()
	    }
	}
	return null
    }

    fun encode(s: String?): String {
	if (s == null) return ""
	if (s.indexOf('<') == -1 && s.indexOf('&') == -1 && s.indexOf('"') == -1) return s
	val sb = StringBuffer()
	val l = s.length
	for (i in 0 until l) {
	    val ch = s[i]
	    if (ch == '&') sb.append("&#x26;") else if (ch == '<') sb.append("&#x3C;") else if (ch == '"') sb.append("&#x22;") else sb.append(
		    ch
	    )
	}
	return sb.toString()
    }

    var lastWasStartElement = false

    init {
	attr = TreeMap()
	content = ArrayList()
    }

    override fun render(sbu: StringBuffer, sbl: StringBuffer?) {
	try {
	    sbu.append("<$name")
	    if (!attr.isEmpty()) {
		val it: Iterator<*> = attr.keys.iterator()
		while (it.hasNext()) {
		    val s = it.next() as String
		    sbu.append(" ")
		    sbu.append(s)
		    sbu.append("=\"")
		    sbu.append(encode(attr[s]))
		    sbu.append("\"")
		}
	    }
	    if (sbl == null && content.isEmpty()) sbu.append("/>\n") else {
		sbu.append(">")
		if (hasSubElement) sbu.append("\n")
		val it: Iterator<*> = content.iterator()
		while (it.hasNext()) {
		    val xn = it.next() as Node
		    xn.render(sbu)
		}
		if (sbl == null) sbu.append("</$name>\n") else sbl.append("</$name>\n")
	    }
	} catch (ex: NullPointerException) {
	}
    }

    override fun toString(): String {
	return "Element{$name}"
    }
}
