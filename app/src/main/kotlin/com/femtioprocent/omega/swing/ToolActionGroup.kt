package com.femtioprocent.omega.swing

class ToolActionGroup {
    var li: MutableList<ToolAction?>

    init {
	li = ArrayList()
    }

    fun add(ta: ToolAction?) {
	li.add(ta)
    }

    fun size(): Int {
	return li.size
    }

    fun find(cmd: String): ToolAction? {
	li.forEach {ta -> if (ta!!.command == cmd) return ta }
	return null
    }
}
