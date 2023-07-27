package com.femtioprocent.omega.lesson.machine

class ItemEntryVirtualList internal constructor() {
    var type: String? = null
    var tid: String? = null
    var items: MutableList<Item?>

    //    public int ord;
    init {
	items = ArrayList()
    }

    fun count(s: String, ch: Char): Int {
	var a = 0
	for (i in 0 until s.length) if (s[i] == ch) a++
	return a
    }

    fun addMore(it_ent: ItemEntry?) {}
    fun getItemAt(ix: Int): Item? {
	return if (ix >= items.size) null else items[ix]
    }

    fun howManyItems(): Int {
	return items.size
    }

    override fun toString(): String {
	return "ItemEntryList{" + type +
		", Tid=" + tid +
		", items=" + items +
		"}"
    }
}
