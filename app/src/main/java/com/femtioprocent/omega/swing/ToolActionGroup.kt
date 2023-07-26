package com.femtioprocent.omega.swing

class ToolActionGroup {
    var li: MutableList<ToolAction?>

    init {
	li = ArrayList()
    }

    fun add(ta: ToolAction?) {
	li.add(ta)
    }

    operator fun iterator(): Iterator<*> {
	return li.iterator()
    }

    fun size(): Int {
	return li.size
    }

    fun find(cmd: String): ToolAction? {
	val it: Iterator<ToolAction?> = li.iterator()
	while (it.hasNext()) {
	    val ta = it.next()
	    if (ta!!.command == cmd) return ta
	}
	return null
    }
}
