package com.femtioprocent.omega.lesson.machine

import com.femtioprocent.omega.lesson.Lesson.SentenceList
import com.femtioprocent.omega.xml.Element

class Items {
    var item_entry_list: MutableList<ItemEntry>

    internal constructor() {
	item_entry_list = ArrayList()
    }

    internal constructor(el: Element, tg: Target, story_hm: HashMap<String?, SentenceList?>?, dummy: Boolean, mix: Boolean) {
	item_entry_list = ArrayList()
	for (i in 0..99) {
	    val il_el = el.findElement("item-entry", i) ?: break
	    val item_entry = ItemEntry(il_el, dummy, mix)
	    item_entry.ord = i
	    if (tg.isTidInTarget(item_entry.tid)) item_entry_list.add(item_entry)
	}
    }

    internal constructor(sa: MutableList<Array<String?>?>, tg: Target?) {      // text, lid,lid... 4 random target list
	item_entry_list = ArrayList()
	val item_entry = ItemEntry()
	item_entry.tid = "X"
	item_entry.ord = 0
	item_entry.load(sa)
	item_entry_list.add(item_entry)
    }

    internal constructor(sa: MutableList<Array<String?>?>, tg: Target?, dummy: Boolean) {      // text, lid,lid... 4 random target list
	item_entry_list = ArrayList()
	val item_entry = ItemEntry()
	item_entry.tid = "X"
	item_entry.ord = 0
	item_entry.load(sa)
	item_entry_list.add(item_entry)
    }

    fun getItemEntryTid(tg_tid: String?): ItemEntry? {
	for(it_ent in item_entry_list) {
	    if (it_ent!!.tid == tg_tid) return it_ent
	    if (tg_tid!!.length == 1 && it_ent.tid!!.indexOf(tg_tid[0]) != -1) return it_ent
	}
	return null
    }

    fun getItemEntryTidAll(tg_tid: String?): Array<ItemEntry> { // ie v
	val tg_choise = ",$tg_tid,"
	val li = ArrayList<ItemEntry>()
	item_entry_list.forEach { it_ent ->
	    val item_choise = "," + it_ent.tid + ","
	    if (item_choise.indexOf(tg_choise) != -1) // ie it_ent.tid s,v  -> match
		li.add(it_ent)
	}
	return li.toTypedArray<ItemEntry>()
    }

    fun howManyItemEntries(): Int {
	return item_entry_list.size
    }

    fun getItemEntryVirtualList(tid: String?): ItemEntryVirtualList {
	val tid2 = ",$tid,"
	val ievli = ItemEntryVirtualList()
	item_entry_list.forEach {
	    val tid3 = "," + it.tid + ","
	    if (tid3.indexOf(tid2) != -1) // tid=s   it_ent.tid=s,o  true
		ievli.items.addAll(it.items!!)
	}
	return ievli
    }

    fun getItemEntryAt(ix: Int): ItemEntry? {
	return try {
	    item_entry_list[ix]
	} catch (ex: IndexOutOfBoundsException) {
	    null
	}
    }

    fun add(ix: Int) {
	val item_entry = ItemEntry()
	item_entry_list.add(ix, item_entry)
	reOrd()
    }

    fun reOrd() {
	item_entry_list.forEachIndexed {index,it_ent -> it_ent.ord = index}
    }

    fun remove(ix: Int) {
	item_entry_list.removeAt(ix)
    }

    fun sowDummy(current_correct_sentence: String?): Int {
	return item_entry_list.fold(0) { c, it_ent -> c + it_ent.sowDummy(current_correct_sentence) }
//	return item_entry_list.map { it_ent -> it_ent.sowDummy(current_correct_sentence) }.reduce {a, b -> a + b}
    }

    fun removeDummy() {
	item_entry_list.forEach {it.removeDummy()}
    }

    val element: Element
	get() {
	    val el = Element("items")
	    var a = 0
	    for(it_ent in item_entry_list) {
		val iel = Element("item-entry")
		iel.addAttr("ord", "" + a++)
		iel.addAttr("type", it_ent.type)
		iel.addAttr("Tid", it_ent.tid)
		for (ix in 0 until it_ent.howManyItems()) {
		    val itm = it_ent.getItemAt(ix)
		    val itel = itm!!.element
		    iel.add(itel)
		}
		el.add(iel)
	    }
	    return el
	}
}
