package com.femtioprocent.omega.lesson.machine

import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.canvas.LessonCanvas
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.rand
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.xml.Element
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.util.*

class ItemEntry {
    var type: String? = null
    var tid: String? = null
    var items: MutableList<Item?>?
    var all_items: MutableList<Item?>? = null
    var ord = 0

    //    ItemEntry link_next;
    var Tr = false

    internal constructor() {
	items = ArrayList<Item?>()
	val item = Item("")
	item.ord = 0
	item.it_ent = this
	(items as ArrayList<Item?>).add(item)
	type = "passive"
	tid = ""
    }

    internal constructor(el: Element, dummy: Boolean, mix: Boolean) {
	items = ArrayList<Item?>()
	load(el, dummy, mix)
    }

    fun count(s: String?, ch: Char): Int {
	var a = 0
	for (i in 0 until s!!.length) if (s[i] == ch) a++
	return a
    }

    private fun load(el: Element, dummy: Boolean, mix: Boolean) {
	type = el.findAttr("type")
	tid = el.findAttr("Tid")
	val tid_len = count(tid, ',') + 1
	var cant_have_dummy = false
	for (i in 0..99) {
	    val it_el = el.findElement("item", i) ?: break
	    val txt = it_el.findAttr("text")
	    if (txt == null || txt.length == 0 || !Lesson.edit && isPeTask(txt)) continue
	    val item = Item(i, it_el, true)
	    if ("action" == type) {
		item.setDefaultAction()
	    }
	    if (item.dummyText == null || item.dummyText!!.length == 0) cant_have_dummy = true
	    item.it_ent = this
	    items!!.add(item)
	}
	if (dummy) {  // make an extra word place for dummy, one each for list of tid (s,o)
	    if ("action" != type) {
		for (i in 0 until tid_len) {
		    if (cant_have_dummy == false) {
			if (items!!.size < 8) {
			    val item = Item("@$ord.$i")
			    item.it_ent = this
			    item.dummy_extra = i // -1 no dummy
			    item.dummy_slot = true
			    items!!.add(item)
			} else {
			}
		    }
		}
	    }
	}
	if (mix) mixList()
	all_items = items
    }

    fun load(sa: MutableList<Array<String?>?>) {
	items = ArrayList<Item?>()
	for (i in sa.indices) {
	    val item = Item(sa[i]!![0], true)
	    item.setActionFile(sa[i]!![2]!!)
	    item.setLid_Krull(sa[i]!![1]!!)
	    item.it_ent = this
	    item.ord = i
	    item.setSound_Krull(sa[i]!![3]!!)
	    if (sa[i]!!.size > 4) {
		item.setVar(0, sa[i]!![4])
		item.setVar(1, sa[i]!![4])
		item.setVar(2, sa[i]!![4])
	    }
	    (items as ArrayList<Item?>).add(item)
	}
	all_items = items
    }

    fun setDummyExtra(src_itm: Item, ix: Int, free: ArrayList<Item>) {
	for (itm in items!!) {
	    if (itm!!.dummy_extra == ix) {  // OK, this is a free empty cell, I can use it
		itm.setText_Krull(src_itm.dummyText!!)
		itm.sound = src_itm.dummySound
		itm.sign = src_itm.dummySign
		if (Tr) Log.getLogger().info(":--: this is now dummy $itm")
		return
	    }
	}
	// no free empty speces, I must use another word
	if (free.size > 0 && "action" != type) {
	    val fix = rand(free.size)
	    val itm = free[fix]
	    itm.allocateDummySpace(src_itm)
	    if (Tr) Log.getLogger().info(":--: " + "this is now alloc dummy " + fix + ' ' + free.size + ' ' + itm)
	}
    }

    fun reOrdItem() {
	var a = 0
	for (itm in items!!) {
	    itm!!.ord = a++
	}
    }

    fun addEmptyItemAt(ix: Int) {
	addItemAt(ix, "")
    }

    @JvmOverloads
    fun addItemAt(ix: Int, txt: String? = "text") {
	val itm: Item = if (type == "action") {
	    val aitm = Item(txt, true)
	    //	    if ( txt.length() > 0 )
	    aitm.setDefaultAction()
	    aitm
	} else {
	    Item(txt, true)
	}
	itm.it_ent = this
	items!!.add(ix, itm)
	reOrdItem()
	all_items = items
    }

    fun resetItems() {
	//	OmegaContext.sout_log.getLogger().info(":--: " + "RESET old items " + items);
	items = all_items
	//	OmegaContext.sout_log.getLogger().info(":--: " + "RESET new items " + items);
    }

    fun getItemAt(ix: Int): Item? {
	return if (ix >= items!!.size) null else items!![ix]
    }

    fun howManyItems(): Int {
	return items!!.size
    }

    //     public int howManyLinkedItems() {
    // 	return howManyItems() + (hasNextLink() ? 0 : getNextLink().howManyLinkedItems());
    //     }
    //     public ItemEntry getNextLink() {
    // 	return link_next;
    //     }
    //     public boolean hasNextLink() {
    // 	return link_next != null;
    //     }
    fun maxStringWidth(): Int {
	var mx = 0
	for (itm in items!!) {
	    if (itm != null) {
		val txt = itm.defaultFilledText
		if (txt!!.length > mx) mx = txt.length
	    }
	}
	return mx * LessonCanvas.CH_W
    }

    fun getStringWidth(fo: Font, g2: Graphics2D, s: String?): Int {
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	val frc = g2.fontRenderContext
	val r = fo.getStringBounds(s, frc)
	return r.width.toInt()
    }

    fun maxStringWidth(fo: Font, g2: Graphics2D): Int {
	var mx = 0
	for (itm in items!!) {
	    val txt = itm!!.defaultFilledText
	    val gsw = getStringWidth(fo, g2, txt)
	    if (gsw > mx) mx = gsw
	}
	return mx * LessonCanvas.CH_W
    }

    fun sowDummy(current_correct_sentence: String?): Int {
	if (Tr) Log.getLogger().info(":--: sowD $current_correct_sentence")
	resetItems()
	if (current_correct_sentence == null) return 0
	mixList()
	var has_krull = current_correct_sentence.indexOf('{') != -1
	val free = ArrayList<Item>()
	for (itm in items!!) {
	    if (itm != null) {
		val extras = split(tid, ",")
		var free_1: ArrayList<Item>? = ArrayList()
		for (jj in extras.indices) {
		    val extra = if (has_krull) "{" + extras[jj] + '}' else ""
		    val s = itm.text + extra // current item + {tid}
		    if (Tr) Log.getLogger().info(":--: try locate $s")
		    if (current_correct_sentence.lowercase(Locale.getDefault())
				    .indexOf(s.lowercase(Locale.getDefault())) == -1
		    ) {
			if (!itm.isDummySpaceAllocated) {
			    free_1?.add(itm)
			    if (Tr) Log.getLogger().info(":--: free_1 added $itm")
			}
		    } else {
			if (Tr) Log.getLogger().info(":--: " + "free_1->null")
			free_1 = null
		    }
		}
		if (free_1 != null) {
		    free.addAll(free_1)
		}
	    }
	}
	if (Tr) Log.getLogger().info(":--: free is $free")
	val used: MutableSet<String> = HashSet<String>()
	for (itm in items!!) {
	    if (itm != null) {
		val extras = split(tid, ",")
		for (jj in extras.indices) {
		    val extra = if (has_krull) "{" + extras[jj] + '}' else ""
		    val s = itm.text + extra // current item + {tid}
		    if (Tr) Log.getLogger().info(":--: try locate' $s")
		    if (current_correct_sentence.lowercase(Locale.getDefault())
				    .indexOf(s.lowercase(Locale.getDefault())) == -1
		    ) {
			// this word not in correct sent
		    } else {  // we have this item as one of the correct
			if (Tr) Log.getLogger().info(":--: use?y $s $used")
			if (!used.contains(itm.text)) {
			    setDummyExtra(itm, jj, free)
			}
			used.add(itm.text!!)
		    }
		}
	    }
	}
	removeStaleDummyProxy()
	return 1
    }

    private fun removeStaleDummyProxy() {
	val n_items: MutableList<Item?> = ArrayList()
	for (itm in items!!) {
	    if (itm!!.text!![0] == '@') ; else n_items.add(itm)
	}
	if (items!!.size != n_items.size) items = n_items
	//	OmegaContext.sout_log.getLogger().info(":--: " + "stale " + items);
    }

    fun mixList() {
	items!!.shuffle()
	reOrdItem()
	Log.getLogger().info("mixList $items")
	if (Tr) Log.getLogger().info(":--: mixList $items")
    }

    fun removeDummy() {
	resetItems()
	items!!.forEach {itm ->
	    itm?.restoreSavedDummy()
	}
    }

    override fun toString(): String {
	return "ItemEntry{" + type +
		", Tid=" + tid +
		", items=" + items +
		"}"
    }

    companion object {
	fun isPeTask(txt: String): Boolean {
	    return txt.matches("[{]\\*[0-9]*:[}]".toRegex())
	}
    }
}
