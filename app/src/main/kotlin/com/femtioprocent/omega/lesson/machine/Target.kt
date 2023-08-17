package com.femtioprocent.omega.lesson.machine

import com.femtioprocent.omega.OmegaContext.Companion.antiOmegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.lessonLang
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssetsExist
import com.femtioprocent.omega.adm.assets.TargetCombinations
import com.femtioprocent.omega.adm.assets.TargetCombinations.TCItem
import com.femtioprocent.omega.anim.appl.Anim_Repository
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.Lesson.SentenceList
import com.femtioprocent.omega.lesson.managers.movie.LiuMovieManager
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.a2s
import com.femtioprocent.omega.util.SundryUtils.empty
import com.femtioprocent.omega.util.SundryUtils.rand
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.value.Values
import com.femtioprocent.omega.xml.Element
import java.awt.Font
import java.awt.Graphics2D
import java.io.File

// has UTF-8
class Target {
    var saved_sa: MutableList<Array<String?>?>? = null
    var saved_saD: MutableList<Array<String?>?>? = null
    var machine: Machine? = null
    var t_items: MutableList<T_Item?>? = null
    var items: Items? = null
    var story_next: String? = null
    var iam_composite = false
    var iam_dummy = false
    var Tr = false

    inner class T_Item internal constructor(
	    var ord: Int,
	    var type: String?,
	    var tid: String?, //fillVarHere(ord, lid);
	    var lID4TgOrNull_KeepVar: String?
    ) {
	var item: Item? = null
	val filledText: String?
	    get() = if (item == null) "               " else fillVarHere(ord, item!!.textD)
	val filledTTS: String?
	    get() = if (item == null) "               " else fillVarHere(ord, item!!.tTSD)
	val filledActionText: String?
	    get() = if (item == null) "               " else fillVarHere(ord, item!!.actionFile)
	val lIDText: String?
	    get() = if (item == null) "" else fillVarHere(ord, item!!.lid)
	val lIDOrNull: String?
	    get() = if (item == null) null else fillVarHere(ord, item!!.lid)
	val textOrNull: String?
	    get() = if (item == null) null else fillVarHere(ord, item!!.text)
	val textVarsOrNull: String?
	    get() = if (item == null) null else fillVarHere(ord, item!!.text) +
		    "§_" + item!!.getVar(1) +  ////   se Lesson.java
		    "§_" + item!!.getVar(2) +  //// UTF-8
		    "§_" + item!!.getVar(3) +  //// UTF-8
		    "§_" + fillVarHere(ord, item!!.sound)
	val lID4TgOrNull: String?
	    get() = fillVarHere(ord, lID4TgOrNull_KeepVar)

	fun clearText() {
	    item = null
	}

	val element: Element
	    get() {
		val el = Element("t-item")
		el.addAttr("ord", "" + (ord + 0))
		el.addAttr("type", "" + type)
		el.addAttr("Tid", "" + tid)
		if (lID4TgOrNull_KeepVar != null) el.addAttr("Lid", "" + lID4TgOrNull_KeepVar)
		return el
	    }

	override fun toString(): String {
	    return "T_Item{" + ord + ", type=" + type + ", tid=" + tid + ", item=" + item + ", lid=" + lID4TgOrNull_KeepVar + "}"
	}

	fun lID4TgOrNull_KeepVar_(): String? {
	    return lID4TgOrNull_KeepVar
	}

	fun lID4TgOrNull_KeepVar_set(str: String?) {
	    lID4TgOrNull_KeepVar = str
	}
    }

    constructor() {
	init()
    }

    constructor(machine: Machine?) {
	this.machine = machine
	init()
    }

    fun init() {
	items = Items()
	t_items = ArrayList()
    }

    @Throws(Exception::class)
    fun loadCompositeFromEl(
	    el: Element?,
	    test_txt: String,
	    story_hm: HashMap<String?, SentenceList?>?,
	    dummy: Boolean,
	    mix: Boolean
    ) {
	iam_composite = true
	iam_dummy = dummy
	init()
	if (el == null) throw Exception("No data to load from")
	var tg2: Target? = Target()
	tg2!!.loadFromEl(el, "", story_hm, false, mix) // can't have dummy extra slot
	val ty = "action"
	val tid = "X"
	val test_index = tg2.getAllTargetCombinationsIndexes(test_txt)
	var lid = a2s(tg2.all_Lid_Target_KeepVar)
	val lid_orig = lid
	if (lid!!.contains("++")) lid = lid.replace("++", "=")
	if (lid.contains("+")) lid = lid.replace("+", "=")
	val titm = T_Item(0, ty, tid, lid) // make one slot
	t_items!!.add(titm)
	val sa = tg2.getAllTargetCombinationsAndMore(false) // with no dummy
	val saD = tg2.getAllTargetCombinationsAndMore(true) // with own dummy random 1/4
	tg2 = null
	saved_sa = sa
	saved_saD = saD
	var five = 5
	if (sa.size < 5) five = sa.size
	sa.shuffle()
	saD.shuffle()

	// first find the correct sentence
	var riktig: Array<String?>? = null
	for (i in sa.indices) if (sa[i]!![0] == test_txt) riktig = sa[i]
	if (riktig == null) throw Exception("Can't find sentence $test_txt")

	// second, create 5 maybee dummy slot
	val sa5: MutableList<Array<String?>?> = ArrayList(five)
	repeat(five) { sa5.add(null) }
	for (i in sa5.indices)  // fem blandade med dummy eller vanliga
	    sa5[i] = if (dummy) saD[i] else sa[i]
	var finns = false
	for (i in sa5.indices) if (sa5[i]!![0] == test_txt) finns = true // test finns redan
	if (finns == false) // put  test
	    sa5[rand(5)] = riktig
	items = Items(sa5, this)
	story_next = null
	val story_el = el.findElement("story", 0)
	if (story_el != null) {
	    val ell = story_el.findElement("link", 1)
	    if (ell != null) {
		val next = ell.findAttr("next")
		story_next = next
	    }
	}
    }

    @Throws(Exception::class)
    fun loadFromEl(
	    el: Element?,
	    test_txt: String?,
	    story_hm: HashMap<String?, SentenceList?>?,
	    dummy: Boolean,
	    mix: Boolean
    ) {
	iam_composite = false
	iam_dummy = dummy
	init()
	if (el == null) throw Exception("No data to load from")
	val ta_el = el.findElement("target", 0) ?: throw Exception("Data corrupt")
	for (i in 0..99) {
	    val ti_el = ta_el.findElement("t-item", i) ?: break
	    var ty = ti_el.findAttr("type")
	    if ("actor" == ty) ty = "passive"
	    val tid = ti_el.findAttr("Tid")
	    val lid = ti_el.findAttr("Lid")
	    //log		OmegaContext.sout_log.getLogger().info(":--: " + "tg_load add " + ty + ' ' + tid + ' ' + lid);
	    t_items!!.add(T_Item(i, ty, tid, lid))
	}
	val its_el = el.findElement("items", 0)
	items = Items(its_el!!, this, story_hm, dummy, mix)
	story_next = null
	val story_el = el.findElement("story", 0)
	//log	    OmegaContext.sout_log.getLogger().info(":--: " + "FSt " + story_el);
	if (story_el != null) {
	    val ell = story_el.findElement("link", 0)
	    //log		OmegaContext.sout_log.getLogger().info(":--: " + "FSt story/link " + ell);
	    val next = ell!!.findAttr("next")
	    if (next != null) {
		Log.getLogger().info(":--: FSt story/link[next] $next")
		story_next = next
	    }
	}
    }

    @Throws(Exception::class)
    fun reloadComposite(test_txt: String) {
	if (!iam_composite) return
	if (saved_sa == null) return
	val sa = saved_sa!!
	val saD = saved_saD!!
	var five = 5
	if (sa.size < 5) five = sa.size
	sa.shuffle() //scrambleArr(sa)
	saD.shuffle() //scrambleArr(saD)
	var riktig: Array<String?>? = null
	for (i in sa.indices) {
	    if (sa[i]!![0] == test_txt) riktig = sa[i]!!
	}
	if (riktig == null) throw Exception("Can't find sentence $test_txt")
	val sa5: MutableList<Array<String?>?> = ArrayList()
	repeat(five) { sa5.add(null) }
	for (i in sa5.indices)  // fem blandade med dummy
	    sa5[i] = if (iam_dummy) saD[i] else sa[i]
	var finns = false
	for (i in sa5.indices) if (sa5[i]!![0] == test_txt) finns = true // test finns redan
	if (finns == false) // put test
	    sa5[rand(five)] = riktig
	items = Items(sa5, this)
    }

    val targetElement: Element
	get() {
	    val el = Element("target")
	    t_items!!.forEach { titm ->
		val tel = titm!!.element
		el.add(tel)
	    }
	    return el
	}
    val itemsElement: Element
	get() = items!!.element

    fun matchTid2(ent_tid: String?, tg_tid: String?): Boolean { // ent_tid is a comma list
	if (ent_tid == null) return false
	val sa = split(ent_tid, ",")
	for (i in sa.indices) if (sa[i] == tg_tid) {
	    return true
	}
	return false
    }

    fun whatTargetMatchTid(ent_tid: String?): Int { // mask
	var a = 0
	var c = 0
	t_items!!.forEach { titm ->
	    if (matchTid2(ent_tid, titm!!.tid)) a = a or (1 shl c)
	    c++
	}
	return a
    }

    private fun findItemEntryMatchTidAll(tid: String?): Array<ItemEntry> {
	return items!!.getItemEntryTidAll(tid)
    }

    fun findItemEntryMatchTid(tid: String?): ItemEntry? {
	return items!!.getItemEntryTid(tid)
    }

    fun findItemEntryVirtualListMatchTid(tid: String?): ItemEntryVirtualList {
	return items!!.getItemEntryVirtualList(tid)
    }

    fun isTidInTarget(ent_tid: String?): Boolean {
	for (titm in t_items!!) {
	    if (matchTid2(ent_tid, titm!!.tid)) return true
	}
	return false
    }

    fun findNextFreeT_ItemIx(box_itm: Item, replace: Boolean, where: Int): Int {
	var same = 0
	run {
	    for (titm in t_items!!) {
		if (matchTid2(box_itm.it_ent!!.tid, titm!!.tid)) same++
	    }
	}
	if (same == 0) return -1
	val d = where / 100.0
	var skip = (d * same).toInt()
	t_items!!.forEach { titm ->
	    if (matchTid2(box_itm.it_ent!!.tid, titm!!.tid)) if (titm.item == null || replace && skip-- == 0) {
		return titm.ord
	    }
	}
	return -1
    }

    fun findNextFreeT_ItemIx(): Int {
	t_items!!.forEach { titm ->
	    if (titm!!.item == null) return titm.ord
	}
	return -1
    }

    fun findEntryIxMatchTargetIx(tg_ix: Int): Int {
	val t_itm = getT_Item(tg_ix)
	val tg_tid = t_itm!!.tid
	val it_ent = findItemEntryMatchTid(tg_tid)
	return it_ent!!.ord
    }

    fun findEntryIxMatchTargetIxAll(tg_ix: Int): IntArray {
	val t_itm = getT_Item(tg_ix)
	val tg_tid = t_itm!!.tid
	val it_ent = findItemEntryMatchTidAll(tg_tid)
	//	OmegaContext.sout_log.getLogger().info(":--: " + "match " + tg_tid + " " + SundryUtils.a2s(it_ent));
	val len = it_ent.size
	val ia = IntArray(len)
	for (i in 0 until len) ia[i] = it_ent[i].ord
	return ia
    }

    fun get_howManyT_Items(): Int {
	return t_items!!.size
    }

    fun getT_Item(ix: Int): T_Item? {
	try {
	    return t_items!![ix]
	} catch (ex: IndexOutOfBoundsException) {
	}
	return null
    }

    fun addT_Item(ix: Int) {
	if (t_items!!.size < 6) {
	    val i = t_items!!.size
	    val ty = "passive"
	    val tid = "p$i"
	    val lid = ""
	    t_items!!.add(ix, T_Item(i, ty, tid, lid))
	    reOrdT_Items()
	}
    }

    fun delT_Item(ix: Int) {
	if (t_items!!.size > 0) {
	    t_items!!.removeAt(ix)
	    reOrdT_Items()
	}
    }

    fun reOrdT_Items() {
	var o = 0
	t_items!!.forEach { titm ->
	    titm!!.ord = o++
	}
    }

    fun addItemEntry(ix: Int, iy: Int) {
	if (items!!.item_entry_list.size < 6) {
	    items!!.add(ix)
	}
    }

    fun delItemEntry(ix: Int, iy: Int) {
	if (items!!.item_entry_list.size >= ix) {
	    items!!.remove(ix)
	}
    }

    fun addItem(ix: Int, iy: Int) {
	val it_ent = items!!.getItemEntryAt(ix)
	it_ent!!.addItemAt(iy)
    }

    fun addEmptyItem(ix: Int, iy: Int) {
	val it_ent = items!!.getItemEntryAt(ix)
	it_ent!!.addEmptyItemAt(iy)
    }

    operator fun set(ix: Int, it: Item?) {
//	item_list.dep_set(ix, it);
    }

    val isTargetFilled: Boolean
	get() {
	    t_items!!.forEach { titm ->
		if (titm!!.item == null) return false
	    }
	    return true
	}

    fun apply(max: Int, ord: Int, txt: String?): String {
	val sa = split(txt, "{}")
	val sb = StringBuffer()
	for (i in sa.indices) {
	    val s = sa[i]
	    var a = 0
	    var aa = 0
	    var isVar = false
	    for (j in 0 until s.length) {
		isVar = if (s.length > 1 && s[j] == '-') {
		    a--
		    aa++
		    true
		} else if (s[j] == '+') {
		    a++
		    aa++
		    true
		} else if (s[j] == '=') {
		    aa++
		    true
		} else {
		    break
		}
	    }
	    if (isVar == false) { // a == 0 )
		if (sa[i].contains("*")) {
		    val ss = sa[i].replace("\\*[0-9]+:?".toRegex(), "")
		    sa[i] = ss
		}
		sb.append(sa[i])
	    } else {
		var def = ""
		var var_ix = -1
		try {
		    var_ix = s[aa].code - '0'.code
		    var ix = 0
		    def = ""
		    if (s.indexOf(':').also { ix = it } != -1) {
			def = s.substring(ix + 1)
		    }
		} catch (ex: StringIndexOutOfBoundsException) {
		    System.err.println("" + ex)
		}

		var var_val: String?

		try {
		    var_val = t_items!![ord + a]!!.item!!.getVar(var_ix) // the H4 bug, item is null
		    if (var_val == null) var_val = def
		    if (var_val.contains("{")) {
			if (max > 0) {
			    val var_val2 = apply(max - 1, ord + a, var_val)
			    //OmegaContext.sout_log.getLogger().info("apply: " + max + ',' + ord + ',' + txt + " -> " + var_val + " -> " + var_val2);
			    var_val = var_val2
			}
		    } else {
			//OmegaContext.sout_log.getLogger().info("apply: " + max + ',' + ord + ',' + txt + " -> " + var_val);
		    }
		} catch (ex: StringIndexOutOfBoundsException) {
		    var_val = def
		} catch (ex: IndexOutOfBoundsException) {
		    var_val = def
		} catch (ex: NullPointerException) {
		    var_val = def
		}
		sb.append(var_val)
	    }
	}
	return sb.toString()
    }

    private fun hasVar(s: String?): Boolean {
	return if (s == null) false else s.indexOf('{') != -1
    }

    fun fillVarHere(ix: Int, s: String?): String? {
	return if (s != null && hasVar(s)) return apply(4, ix, s) else s
    }

    private val actionIx: Int
	private get() {
	    t_items!!.forEachIndexed { index, titm -> if (titm!!.type == "action") return index }
	    return -1
	}

    //     private Item getActionItem() {
    // 	int ix = getActionIx();
    // 	if ( ix >= 0 )
    // 	    return (Item)getT_Item(ix).item;
    // 	return null;
    //     }
    // what:
    // 0 first, default
    // 99 all ' ' separated
    fun getActionFileName(what: Int): String? {
	if (what == 0) {
	    val ix = actionIx
	    return if (ix >= 0) {
		val ss = fillActionLid(ix, getT_Item(ix)!!.item)
			?: return null
		val sa = split(ss, ",")
		sa[0]
	    } else null
	} else if (what == 99) {
	    val ix = actionIx
	    return if (ix >= 0) {
		fillActionLid(ix, getT_Item(ix)!!.item)
	    } else null
	} else if (what == 999) { // get all from all
	    val sb = StringBuffer()
	    for (i in 0 until get_howManyT_Items()) {
		if (i >= 0) {
		    val ss = fillActionLid(i, getT_Item(i)!!.item)
		    if (ss != null && ss.length > 0) {
			if (sb.length > 0) sb.append(",")
			sb.append(ss)
		    }
		}
	    }
	    return sb.toString()
	}
	return getActionFileName(0)
    }

    fun fillActionLid(ix: Int, it: Item?): String? {
	if (it == null) return null
	val txt = it.actionText
	return fillVarHere(ix, txt)
    }

    fun setItem(ix: Int, item: Item?) {
	t_items!![ix]!!.item = item
    }

    fun howManyItemBoxes(): Int {
	return items!!.howManyItemEntries()
    }

    fun getItemAt(ix: Int, iy: Int): Item? {
	val it_ent = items!!.getItemEntryAt(ix) ?: return null
	return it_ent.getItemAt(iy)
    }

    fun releaseT_ItemAt(ix: Int) {
	t_items!![ix]!!.clearText()
    }

    fun releaseAllT_Items() {
	t_items!!.forEach { titm -> titm!!.clearText() }
	Log.getLogger().info(":--: " + "target released")
    }

    fun pickNextItem(): Item? {
	return null
    }

    fun pickItemAtEx(ix: Int, iy: Int, tg_ix: Int): Item? { // pick at ix,iy -> target[tg_ix]
	val ret = pickItemAt(ix, iy, tg_ix, false, true)
	if (Tr) Log.getLogger().info(":--: pick  $ret")
	return ret
    }

    @JvmOverloads
    fun pickItemAt(ix: Int, iy: Int, tg_ix: Int, any_id: Boolean = false, next_col: Boolean = false): Item? {
	var ix = ix
	var iy = iy
	var ret: Item? = null
	try {
	    if (iy >= 8) { // ZZ
		if (next_col) {
		    iy -= 8
		    ix++
		} else return null
	    }
	    val tg_tid = t_items!![tg_ix]!!.tid
	    //   OmegaContext.sout_log.getLogger().info(":--: " + "pick it tg_tid " + tg_tid + ' ' + ix + ' ' + iy);
	    //ItemEntry it_ent = items.getItemEntryTid(tg_tid);
	    val it_ent_ix = items!!.getItemEntryAt(ix)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "tid.. " + it_ent_ix.tid);
	    if (it_ent_ix == null) {
		ret = null
		return ret
	    }
	    val item = it_ent_ix.getItemAt(iy)
	    val ent_tid = it_ent_ix.tid
	    return if (any_id || matchTid2(ent_tid, tg_tid)) {
		setItem(tg_ix, item)
		ret = item
		ret
	    } else {
		ret = null
		ret
	    }
	} catch (ex: Exception) {
	    ex.printStackTrace()
	} finally {
//	    if ( ret == null )
// 		OmegaContext.sout_log.getLogger().info(":--: " + "pickItem -> NULL " + ix + ' ' + iy);
// 	    OmegaContext.sout_log.getLogger().info(":--: " + "pickItem -> " + ix + ' ' + iy + ' ' + ret.getText());
	}
	return ret
    }

    @JvmOverloads
    fun pickItemAtDummy(dummy: Boolean, ix: Int, iy: Int, tg_ix: Int, any_id: Boolean = false): Item? {
	try {
	    val tg_tid = t_items!![tg_ix]!!.tid

	    //ItemEntry it_ent = items.getItemEntryTid(tg_tid);
	    val it_ent_ix = items!!.getItemEntryAt(ix) ?: return null
	    val item = it_ent_ix.getItemAt(iy)
	    item!!.setDummy(if (dummy) rand(100) < 25 else false)
	    val ent_tid = it_ent_ix.tid
	    return if (any_id || matchTid2(ent_tid, tg_tid)) {
		setItem(tg_ix, item)
		item
	    } else {
		null
	    }
	} catch (ex: Exception) {
	    ex.printStackTrace()
	}
	return null
    }

    fun sowDummy(current_correct_sentence: String?): Int {
	return items!!.sowDummy(current_correct_sentence)
    }

    fun removeDummy() {
	items!!.removeDummy()
    }

    val maxItemsInAnyBox: Int
	get() {
	    var a = 0
	    for (i in 0 until howManyItemBoxes()) {
		val ite = items!!.getItemEntryAt(i)
		val n = ite!!.howManyItems()
		if (n > a) a = n
	    }
	    return a
	}

    fun getMaxItemsInBox(ix: Int): Int {
	for (i in 0 until howManyItemBoxes()) {
	    val ite = items!!.getItemEntryAt(i)
	    if (i == ix) return ite!!.howManyItems()
	}
	return 0
    }

    fun getMaxWidthInBox(ix: Int): Int {
	val ite = items!!.getItemEntryAt(ix)
	return ite!!.maxStringWidth()
    }

    fun getMaxWidthInBox(ix: Int, fo: Font?, g2: Graphics2D?): Int {
	val ite = items!!.getItemEntryAt(ix)
	return ite!!.maxStringWidth(fo!!, g2!!)
    }

    fun getMaxWidthUptoBox(ix: Int): Int {
	var a = 0
	for (i in 0 until ix) a += getMaxWidthInBox(i)
	return a
    }

    val maxWidthSumAllBox: Int
	get() {
	    var a = 0
	    val to = items!!.howManyItemEntries()
	    for (i in 0 until to) {
		val ite = items!!.getItemEntryAt(i)
		a += ite!!.maxStringWidth()
	    }
	    return a
	}

    fun getMaxWidthSumAllBox(fo: Font?, g2: Graphics2D?): Int {
	var a = 0
	val to = items!!.howManyItemEntries()
	for (i in 0 until to) {
	    val ite = items!!.getItemEntryAt(i)
	    a += ite!!.maxStringWidth(fo!!, g2!!)
	}
	return a
    }

    fun Upper1(s: String?): String? {
	if (s != null && s.length >= 1) {
	    val ss = s.substring(1)
	    val ch = s[0]
	    return "" + ch.uppercaseChar() + ss
	}
	return s
    }

    val allText: String
	get() {
	    val s = getTextUpto(t_items!!.size, 1)
	    if (Tr) Log.getLogger().info(":--: getAllText $s.")
	    return s
	}
    val allTTS: String
	get() {
	    val s = getTTSUpto(t_items!!.size, 1)
	    if (Tr) Log.getLogger().info(":--: getAllTTS $s.")
	    return s
	}

    fun getAllSignMovies(lmm: LiuMovieManager): ArrayList<String> {
	val list = ArrayList<String>()
	for (i in t_items!!.indices) {
	    val t_Item = getT_Item(i)
	    val smfName = lmm.getSignMovieFileName(t_Item!!.item!!, this, i)
	    if (smfName != null) {
		list.add(smfName)
	    } else {
		list.add("")
	    }
	}
	return list
    }

    fun getAllText(sep: String?): String {
	return getTextUpto(t_items!!.size, sep)
    }

    fun getAllText(sep: String?, delim: Char): String {
	return getTextUpto(t_items!!.size, sep, delim)
    }

    fun getAllText(space: Int): String {
	return getTextUpto(t_items!!.size, space)
    }

    fun getTextAt(ix: Int): String? {  // can be dummy text
	val titm = getT_Item(ix) ?: return ""
	return titm.filledText
    }

    fun getTTSAt(ix: Int): String? {  // can be dummy text
	val titm = getT_Item(ix) ?: return ""
	return titm.filledTTS
    }

    fun getTidAt(ix: Int): String? {
	val titm = getT_Item(ix) ?: return "X"
	return titm.tid
    }

    fun getTextUpto(ix: Int, space: Int): String {
	val sp = "                                                     ".substring(0, space)
	val sb = StringBuffer()
	for (i in 0 until ix) {
	    var txt = getTextAt(i)
	    if (sb.length > 0 && txt!!.length > 0) {
		sb.append(sp)
	    }
	    if (i == 0) txt = Upper1(txt)
	    sb.append(txt)
	}
	return sb.toString()
    }

    fun getTTSUpto(ix: Int, space: Int): String {
	val sp = "                                                     ".substring(0, space)
	val sb = StringBuffer()
	for (i in 0 until ix) {
	    var txt = getTTSAt(i)
	    if (sb.length > 0 && txt!!.length > 0) {
		sb.append(sp)
	    }
	    if (i == 0) txt = Upper1(txt)
	    sb.append(txt)
	}
	return sb.toString()
    }

    fun getTextUpto(ix: Int, sep: String?): String {
	val sb = StringBuffer()
	for (i in 0 until ix) {
	    var txt = getTextAt(i)
	    if (i == 0) txt = Upper1(txt)
	    if (i > 0) sb.append(sep)
	    sb.append(txt)
	}
	return sb.toString()
    }

    fun getTextUpto(ix: Int, sep: String?, delim: Char): String {
	var delim2 = ""
	if (delim == '{') delim2 = "}"
	if (delim == '(') delim2 = ")"
	if (delim == '[') delim2 = "]"
	if (delim == '<') delim2 = ">"
	val sb = StringBuffer()
	for (i in 0 until ix) {
	    var txt = getTextAt(i)
	    if (i == 0) txt = Upper1(txt)
	    if (i > 0) sb.append(sep)
	    sb.append(txt)
	    sb.append("" + delim + getTidAt(i) + delim2)
	}
	return sb.toString()
    }

    val allLessonBothArg: Array<String?>
	get() {
	    val li: MutableList<String?> = ArrayList()
	    t_items!!.forEach { titm ->
		val s = titm!!.lIDText
		if (s != null && s.length > 0) li.add(s + ';' + titm.lID4TgOrNull_KeepVar)
	    }
	    return li.toTypedArray<String?>()
	}

    fun addSA(li: MutableList<String>, sa: Array<String>) {
	for (i in sa.indices) li.add(sa[i])
    }

    val all_Lid_Target: Array<String>
	get() {  // banor,banor...
	    val li: MutableList<String> = ArrayList()
	    t_items!!.forEach { titm ->
		val s = titm!!.lID4TgOrNull
		if (s != null && s.length > 0) {
		    val sa = split(s, ",")
		    addSA(li, sa)
		}
	    }
	    return li.toTypedArray<String>()
	}
    val all_Lid_Target_KeepVar: Array<String>
	get() {  // banor,banor...
	    val li: MutableList<String> = ArrayList()
	    t_items!!.forEach { titm ->
		val s = titm!!.lID4TgOrNull_KeepVar
		if (s != null && s.length > 0) {
		    val sa = split(s, ",")
		    addSA(li, sa)
		}
	    }
	    return li.toTypedArray<String>()
	}
    val allSounds: Array<Pair<Int,String?>>
	get() {
	    val li: MutableList<Pair<Int,String?>> = ArrayList()
	    var ix = 0
	    t_items!!.forEach { titm ->
		if (titm != null && titm.item != null) {
		    var s = titm.item!!.soundD
		    s = fillVarHere(ix, s)
		    li.add(Pair(ix, s))
		}
		ix++
	    }

//log	OmegaContext.sout_log.getLogger().info(":--: " + "[] get sounds " + SundryUtils.a2s(sa));
	    return li.toTypedArray<Pair<Int,String?>>()
	}

    fun getSoundsAt(x: Int, y: Int): Array<String?> {
	val li: MutableList<String?> = ArrayList()
	var ix = 0
	t_items!!.forEach { titm ->
	    if (titm != null && titm.item != null) {
		var s = titm.item!!.soundD
		s = fillVarHere(ix, s)
		li.add(s)
	    }
	    ix++
	}

//log	OmegaContext.sout_log.getLogger().info(":--: " + "[] get sounds " + SundryUtils.a2s(sa));
	return li.toTypedArray<String?>()
    }

    val all_Lid_Item: Array<String>
	get() {  // actor,actor...
	    val li: MutableList<String> = ArrayList()
	    t_items!!.forEach { titm ->
		if (titm != null) {
		    val s = titm.lIDOrNull
		    if (s != null && s.length > 0) {
			val sa = split(s, ",")
			addSA(li, sa)
		    }
		}
	    }
	    return li.toTypedArray<String>()
	}
    val all_Tid_Item: String
	get() {  // ordgrupps id
	    val sb = StringBuffer()
	    t_items!!.forEach { titm ->
		if (titm != null) {
		    val s = titm.tid
		    if (sb.length > 0) sb.append(";")
		    sb.append(s)
		}
	    }
	    return sb.toString()
	}
    val all_Text_Item: Array<String>
	get() {  // actor,actor...
	    val li: MutableList<String> = ArrayList()
	    t_items!!.forEach { titm ->
		if (titm != null) {
		    val sx = titm.textOrNull
		    val s = titm.lIDOrNull
		    if (s != null && s.length > 0) {
			val sa = split(sx, ",")
			addSA(li, sa)
		    }
		}
	    }
	    return li.toTypedArray<String>()
	}
    val all_Sound_Item: String?
	get() {
	    var s: String? = ""
	    var ix = 0
	    t_items!!.forEach { titm ->
		var snd = titm!!.item!!.sound
		snd = fillVarHere(ix, snd) // WHY-SundryUtil
		if (s!!.length != 0) s += ','
		s += snd
		ix++
	    }
	    return s
	}
    val all_Sound_Items: List<String?>
	get() {
	    val li: MutableList<String?> = ArrayList()
	    var ix = 0
	    t_items!!.forEach { titm ->
		var snd = titm!!.item!!.sound
		snd = fillVarHere(ix, snd) // WHY-SundryUtil
		if (!empty(snd)) fillHavingComma(li, snd)
		var sndD = titm.item!!.dummySound
		sndD = fillVarHere(ix, sndD) // WHY-SundryUtil
		if (!empty(sndD)) li.add(sndD)
		ix++
	    }
	    return li
	}

    private fun fillHavingComma(li: MutableList<String?>, snd: String?) {
	if (!snd!!.contains(",")) {
	    li.add(snd)
	    return
	}
	val sa = split(snd, ",")
	for (s in sa) {
	    li.add(s)
	}
    }

    val all_TextVars_Item: Array<String>
	get() {  // actor,actor...
	    val li: MutableList<String> = ArrayList()
	    t_items!!.forEach { titm ->
		if (titm != null) {
		    val sx = titm.textVarsOrNull // text:v1:v2:v3:sound : is paragraph_
		    //log		OmegaContext.sout_log.getLogger().info(":--: " + "tvvv = " + sx);
		    val s = titm.lIDOrNull
		    if (s != null && s.length > 0) {
			val sa = split(sx, ",")
			addSA(li, sa)
		    }
		}
	    }
	    return li.toTypedArray<String>()
	}

    fun putAll_TextVars_Item(hm: HashMap<String?, String?>) {
	t_items!!.forEach { titm ->
	    if (titm != null) {
		val item = titm.item
		var s = item!!.getVar(1)
		hm[titm.tid + ':' + "1"] = s
		s = item.getVar(2)
		hm[titm.tid + ':' + "2"] = s
		s = item.getVar(3)
		hm[titm.tid + ':' + "3"] = s
		s = item.getVar(4)
		hm[titm.tid + ':' + "4"] = s
		s = item.getVar(5)
		hm[titm.tid + ':' + "5"] = s
	    }
	}
    }

    @get:Deprecated("")
    val actionFromTarget: String?
	get() {
	    t_items!!.forEach { titm ->
		if (titm != null) {
		    if (titm.item != null && titm.item!!.isAction) return titm.item!!.actionFile
		}
	    }
	    return ""
	}
    val lessonArgLength: Int
	get() {
	    val sa = allLessonBothArg
	    return sa.size
	}

    fun getLessonArg(ix: Int): String? {
	return try {
	    val sa = allLessonBothArg
	    sa[ix]
	} catch (ex: ArrayIndexOutOfBoundsException) {
	    null
	}
    }

    fun getTargetValues(ix: Int): Values {
	val tit = getT_Item(ix)
	val vs = Values()
	if (tit != null) {
	    if (tit.item != null) vs.setStr("text", tit.item!!.defaultFilledText) // text);
	    else vs.setStr("text", "")
	    vs.setStr("tid", tit.tid)
	    vs.setStr("lid", tit.lID4TgOrNull_KeepVar)
	    vs.setStr("type", tit.type)
	}
	return vs
    }

    fun putTargetValues(ix: Int, vs: Values) {
	val tit = getT_Item(ix)
	tit!!.tid = vs.getStr("tid")
	tit.lID4TgOrNull_KeepVar = vs.getStr("lid")
	tit.type = vs.getStr("type")
    }

    @Deprecated("Not used any more")
    fun getAllTargetCombinations_old(sep: String?): Array<String?> {
	val li: MutableList<String?> = ArrayList()
	val tg2 = this
	val Tn = tg2.get_howManyT_Items()
	if (Tn > 0) {
	    val tid0 = tg2.getT_Item(0)!!.tid
	    val it_ent0 = tg2.findItemEntryMatchTid(tid0)
	    val ie_n0 = it_ent0!!.howManyItems()
	    for (i0 in 0 until ie_n0) {
		tg2.pickItemAt(it_ent0.ord, i0, 0)
		if (Tn > 1) {
		    val tid1 = tg2.getT_Item(1)!!.tid
		    val it_ent1 = tg2.findItemEntryMatchTid(tid1)
		    val ie_n1 = it_ent1!!.howManyItems()
		    for (i1 in 0 until ie_n1) {
			tg2.pickItemAt(it_ent1.ord, i1, 1)
			if (Tn > 2) {
			    val tid2 = tg2.getT_Item(2)!!.tid
			    val it_ent2 = tg2.findItemEntryMatchTid(tid2)
			    val ie_n2 = it_ent2!!.howManyItems()
			    for (i2 in 0 until ie_n2) {
				tg2.pickItemAt(it_ent2.ord, i2, 2)
				if (Tn > 3) {
				    val tid3 = tg2.getT_Item(3)!!.tid
				    val it_ent3 = tg2.findItemEntryMatchTid(tid3)
				    val ie_n3 = it_ent3!!.howManyItems()
				    for (i3 in 0 until ie_n3) {
					tg2.pickItemAt(it_ent3.ord, i3, 3)
					if (Tn > 4) {
					    val tid4 = tg2.getT_Item(4)!!.tid
					    val it_ent4 = tg2.findItemEntryMatchTid(tid4)
					    val ie_n4 = it_ent4!!.howManyItems()
					    for (i4 in 0 until ie_n4) {
						tg2.pickItemAt(it_ent4.ord, i4, 4)
						if (Tn > 5) {
						    val tid5 = tg2.getT_Item(5)!!.tid
						    val it_ent5 = tg2.findItemEntryMatchTid(tid5)
						    val ie_n5 = it_ent5!!.howManyItems()
						    for (i5 in 0 until ie_n5) {
							tg2.pickItemAt(it_ent5.ord, i5, 5)
							li.add(tg2.getAllText(sep))
						    }
						} else {
						    li.add(tg2.getAllText(sep))
						}
					    }
					} else {
					    li.add(tg2.getAllText(sep))
					}
				    }
				} else {
				    li.add(tg2.getAllText(sep))
				}
			    }
			} else {
			    li.add(tg2.getAllText(sep))
			}
		    }
		} else {
		    li.add(tg2.getAllText(sep))
		}
	    }
	} else {
	    li.add(tg2.getAllText(sep))
	}
	return li.toTypedArray<String?>()
    }

    fun getAllTargetCombinations(sep: String?): Array<String?> {
	val li: MutableList<String?> = ArrayList()
	val tg2 = this
	val Tn = tg2.get_howManyT_Items()
	if (Tn > 0) {
	    val tid0 = tg2.getT_Item(0)!!.tid
	    val it_ent0 = tg2.findItemEntryVirtualListMatchTid(tid0)
	    val ie_n0 = it_ent0.howManyItems()
	    for (i0 in 0 until ie_n0) {
		val itm0 = it_ent0.getItemAt(i0)
		tg2.pickItemAt(itm0!!.it_ent!!.ord, itm0.ord, 0)
		if (Tn > 1) {
		    val tid1 = tg2.getT_Item(1)!!.tid
		    val it_ent1 = tg2.findItemEntryVirtualListMatchTid(tid1)
		    val ie_n1 = it_ent1.howManyItems()
		    for (i1 in 0 until ie_n1) {
			val itm1 = it_ent1.getItemAt(i1)
			tg2.pickItemAt(itm1!!.it_ent!!.ord, itm1.ord, 1)
			if (Tn > 2) {
			    val tid2 = tg2.getT_Item(2)!!.tid
			    val it_ent2 = tg2.findItemEntryVirtualListMatchTid(tid2)
			    val ie_n2 = it_ent2.howManyItems()
			    for (i2 in 0 until ie_n2) {
				val itm2 = it_ent2.getItemAt(i2)
				tg2.pickItemAt(itm2!!.it_ent!!.ord, itm2.ord, 2)
				if (Tn > 3) {
				    val tid3 = tg2.getT_Item(3)!!.tid
				    val it_ent3 = tg2.findItemEntryVirtualListMatchTid(tid3)
				    val ie_n3 = it_ent3.howManyItems()
				    for (i3 in 0 until ie_n3) {
					val itm3 = it_ent3.getItemAt(i3)
					tg2.pickItemAt(itm3!!.it_ent!!.ord, itm3.ord, 3)
					if (Tn > 4) {
					    val tid4 = tg2.getT_Item(4)!!.tid
					    val it_ent4 = tg2.findItemEntryVirtualListMatchTid(tid4)
					    val ie_n4 = it_ent4.howManyItems()
					    for (i4 in 0 until ie_n4) {
						val itm4 = it_ent4.getItemAt(i4)
						tg2.pickItemAt(itm4!!.it_ent!!.ord, itm4.ord, 4)
						if (Tn > 5) {
						    val tid5 = tg2.getT_Item(5)!!.tid
						    val it_ent5 = tg2.findItemEntryVirtualListMatchTid(tid5)
						    val ie_n5 = it_ent5.howManyItems()
						    for (i5 in 0 until ie_n5) {
							val itm5 = it_ent5.getItemAt(i5)
							tg2.pickItemAt(itm5!!.it_ent!!.ord, itm5.ord, 5)
							li.add(tg2.getAllText(sep))
						    }
						} else {
						    li.add(tg2.getAllText(sep))
						}
					    }
					} else {
					    li.add(tg2.getAllText(sep))
					}
				    }
				} else {
				    li.add(tg2.getAllText(sep))
				}
			    }
			} else {
			    li.add(tg2.getAllText(sep))
			}
		    }
		} else {
		    li.add(tg2.getAllText(sep))
		}
	    }
	} else {
	    li.add(tg2.getAllText(sep))
	}
	return li.toTypedArray<String?>()
    }

    fun getAllTargetCombinationsEx(sep: String?, delim: Char): Array<String?> {
	val li: MutableList<String?> = ArrayList()
	val tg2 = this
	val Tn = tg2.get_howManyT_Items()
	if (Tn > 0) {
	    val tid0 = tg2.getT_Item(0)!!.tid
	    val it_ent0 = tg2.findItemEntryVirtualListMatchTid(tid0)
	    val ie_n0 = it_ent0.howManyItems()
	    for (i0 in 0 until ie_n0) {
		val itm0 = it_ent0.getItemAt(i0)
		tg2.pickItemAt(itm0!!.it_ent!!.ord, itm0.ord, 0)
		if (Tn > 1) {
		    val tid1 = tg2.getT_Item(1)!!.tid
		    val it_ent1 = tg2.findItemEntryVirtualListMatchTid(tid1)
		    val ie_n1 = it_ent1.howManyItems()
		    for (i1 in 0 until ie_n1) {
			val itm1 = it_ent1.getItemAt(i1)
			tg2.pickItemAt(itm1!!.it_ent!!.ord, itm1.ord, 1)
			if (Tn > 2) {
			    val tid2 = tg2.getT_Item(2)!!.tid
			    val it_ent2 = tg2.findItemEntryVirtualListMatchTid(tid2)
			    val ie_n2 = it_ent2.howManyItems()
			    for (i2 in 0 until ie_n2) {
				val itm2 = it_ent2.getItemAt(i2)
				tg2.pickItemAt(itm2!!.it_ent!!.ord, itm2.ord, 2)
				if (Tn > 3) {
				    val tid3 = tg2.getT_Item(3)!!.tid
				    val it_ent3 = tg2.findItemEntryVirtualListMatchTid(tid3)
				    val ie_n3 = it_ent3.howManyItems()
				    for (i3 in 0 until ie_n3) {
					val itm3 = it_ent3.getItemAt(i3)
					tg2.pickItemAt(itm3!!.it_ent!!.ord, itm3.ord, 3)
					if (Tn > 4) {
					    val tid4 = tg2.getT_Item(4)!!.tid
					    val it_ent4 = tg2.findItemEntryVirtualListMatchTid(tid4)
					    val ie_n4 = it_ent4.howManyItems()
					    for (i4 in 0 until ie_n4) {
						val itm4 = it_ent4.getItemAt(i4)
						tg2.pickItemAt(itm4!!.it_ent!!.ord, itm4.ord, 4)
						if (Tn > 5) {
						    val tid5 = tg2.getT_Item(5)!!.tid
						    val it_ent5 = tg2.findItemEntryVirtualListMatchTid(tid5)
						    val ie_n5 = it_ent5.howManyItems()
						    for (i5 in 0 until ie_n5) {
							val itm5 = it_ent5.getItemAt(i5)
							tg2.pickItemAt(itm5!!.it_ent!!.ord, itm5.ord, 5)
							li.add(tg2.getAllText(sep, delim))
						    }
						} else {
						    li.add(tg2.getAllText(sep, delim))
						}
					    }
					} else {
					    li.add(tg2.getAllText(sep, delim))
					}
				    }
				} else {
				    li.add(tg2.getAllText(sep, delim))
				}
			    }
			} else {
			    li.add(tg2.getAllText(sep, delim))
			}
		    }
		} else {
		    li.add(tg2.getAllText(sep, delim))
		}
	    }
	} else {
	    li.add(tg2.getAllText(sep, delim))
	}
	return li.toTypedArray<String?>()
    }

    fun getAllTargetCombinationsEx2(lesson: Lesson): TargetCombinations {
	val tc = TargetCombinations()

	// add lesson icons
	val lln = lesson.loadedFName!!
	val ix = lln.lastIndexOf("/")
	if (ix != -1) {
	    val llnBase = lln.substring(0, ix)
	    addLessonIcon(tc, "$llnBase/image.png")
	    addLessonIcon(tc, "$llnBase/image_enter.png")
	}
	val media = lesson.action_specific!!.media
	for (s in media) tc.dep_set.add(TCItem(s))
	val tg2 = this
	val Tn = tg2.get_howManyT_Items()
	val set: Set<String> = HashSet()
	if (Tn > 0) {
	    val tid0 = tg2.getT_Item(0)!!.tid
	    val it_ent0 = tg2.findItemEntryVirtualListMatchTid(tid0)
	    val ie_n0 = it_ent0.howManyItems()
	    for (i0 in 0 until ie_n0) {
		val itm0 = it_ent0.getItemAt(i0)
		tg2.pickItemAt(itm0!!.it_ent!!.ord, itm0.ord, 0)
		if (Tn > 1) {
		    val tid1 = tg2.getT_Item(1)!!.tid
		    val it_ent1 = tg2.findItemEntryVirtualListMatchTid(tid1)
		    val ie_n1 = it_ent1.howManyItems()
		    for (i1 in 0 until ie_n1) {
			val itm1 = it_ent1.getItemAt(i1)
			tg2.pickItemAt(itm1!!.it_ent!!.ord, itm1.ord, 1)
			if (Tn > 2) {
			    val tid2 = tg2.getT_Item(2)!!.tid
			    val it_ent2 = tg2.findItemEntryVirtualListMatchTid(tid2)
			    val ie_n2 = it_ent2.howManyItems()
			    for (i2 in 0 until ie_n2) {
				val itm2 = it_ent2.getItemAt(i2)
				tg2.pickItemAt(itm2!!.it_ent!!.ord, itm2.ord, 2)
				if (Tn > 3) {
				    val tid3 = tg2.getT_Item(3)!!.tid
				    val it_ent3 = tg2.findItemEntryVirtualListMatchTid(tid3)
				    val ie_n3 = it_ent3.howManyItems()
				    for (i3 in 0 until ie_n3) {
					val itm3 = it_ent3.getItemAt(i3)
					tg2.pickItemAt(itm3!!.it_ent!!.ord, itm3.ord, 3)
					if (Tn > 4) {
					    val tid4 = tg2.getT_Item(4)!!.tid
					    val it_ent4 = tg2.findItemEntryVirtualListMatchTid(tid4)
					    val ie_n4 = it_ent4.howManyItems()
					    for (i4 in 0 until ie_n4) {
						val itm4 = it_ent4.getItemAt(i4)
						tg2.pickItemAt(itm4!!.it_ent!!.ord, itm4.ord, 4)
						if (Tn > 5) {
						    val tid5 = tg2.getT_Item(5)!!.tid
						    val it_ent5 = tg2.findItemEntryVirtualListMatchTid(tid5)
						    val ie_n5 = it_ent5.howManyItems()
						    for (i5 in 0 until ie_n5) {
							val itm5 = it_ent5.getItemAt(i5)
							tg2.pickItemAt(itm5!!.it_ent!!.ord, itm5.ord, 5)
							tg2.update(tc)
						    }
						} else {
						    tg2.update(tc)
						}
					    }
					} else {
					    tg2.update(tc)
					}
				    }
				} else {
				    tg2.update(tc)
				}
			    }
			} else {
			    tg2.update(tc)
			}
		    }
		} else {
		    tg2.update(tc)
		}
	    }
	} else {
	    tg2.update(tc)
	}
	return tc
    }

    private fun addLessonIcon(tc: TargetCombinations, fn: String) {
	if (omegaAssetsExist(fn)) tc.dep_set.add(TCItem(fn))
    }

    private fun update(tc: TargetCombinations) {
	val nTarget = Target()
	tc.tg_set.add(nTarget) // not populated yet
	val sound_list = all_Sound_Items
	for (s in sound_list) {
	    val fn = "media" + File.separator + s
	    val l = expandVariants(fn)
	    var oneExist = false
	    for (f in l) {
		if (omegaAssetsExist(f.fn)) {
		    tc.dep_set.add(f)
		    oneExist = true
		} else {
		    //tc.dep_set.add(new TargetCombinations.TCItem(f, false));
		}
	    }
	    if (!oneExist) {
		tc.dep_set.add(TCItem(fn))
	    }
	}
	/*
        Element asel = .findElement("action_specific", 0);
        if (asel != null) {
            for (int i = 0; i < 1000; i++) {
                Element eb = asel.findElement("value", i);
                if (eb != null) {
                    String val = eb.findAttr("val");
                    if (!SundryUtils.empty(val)) {
                        tc.dep_set.add(OmegaContext.media() + val);
                    }
                }
            }
        }
*/for (o in t_items!!) {
	    val titm = o
	    if (titm!!.item != null && !empty(titm.item!!.action_fname)) {
		val af_alt = titm.item!!.actionFile
		val af = titm.filledActionText
		if (empty(af)) continue
		val ar = Anim_Repository()
		val anim_el_root = ar.open(null, omegaAssets(af)) ?: continue
		Log.getLogger().info(anim_el_root.toString())
		val cel = anim_el_root.findElement("Canvas", 0)
		if (cel != null) {
		    val eb = anim_el_root.findElement("background", 0)
		    if (eb != null) {
			val s = eb.findAttr("name")
			if (s != null) {
			    val fn = "media" + File.separator + s
			    tc.dep_set.add(TCItem(fn))
			}
		    }
		}
		val ael = anim_el_root.findElement("AllActors", 0)
		if (ael != null) {
		    for (i in 0..9) {
			val eb = anim_el_root.findElement("Actor", i)
			if (eb != null) {
			    val s = eb.findAttr("name")
			    if (s != null) {
				val ms = "media" + File.separator + s
				tc.dep_set.add(TCItem(ms))
				val aiL = attributedImages(ms)
				for (ai in aiL) tc.dep_set.add(ai)
			    }
			}
		    }
		}
		for (ti in 0..99) {
		    val mel = anim_el_root.findElement("TimeMarker", ti)
		    if (mel != null) {
			for (ei in 0..99) {
			    val tel = mel.findElement("T_Event", ei)
			    if (tel != null) {
				for (i in 0..9) {
				    val eb = tel.findElement("TriggerEvent", i)
				    if (eb != null) {
					val cmd = eb.findAttr("cmd")
					if (cmd != null && cmd == "PlaySound") {
					    val sf_ = eb.findAttr("arg")
					    if (!empty(sf_)) {
						val sf = fillVarHere(titm.ord, sf_)
						val fn = "media" + File.separator + sf
						val l = expandVariants(fn)
						var oneExist = false
						for (f in l) {
						    if (omegaAssetsExist(f.fn)) {
							tc.dep_set.add(f)
							oneExist = true
						    } else {
							//tc.dep_set.add(new TargetCombinations.TCItem(f, false));
						    }
						}
						if (!oneExist) {
						    tc.dep_set.add(TCItem(fn))
						}
					    }
					}
				    }
				}
			    }
			}
		    }
		}
		tc.dep_set.add(TCItem(af!!))
	    }
	}
    }

    private fun expandVariants(fn: String): List<TCItem> {
	val l: MutableList<TCItem> = ArrayList()
	l.add(TCItem(fn))
	var alt = fn.replace("\\.wav$".toRegex(), ".mp3")
	l.add(TCItem(alt, "wav"))
	alt = fn.replace("\\.mpg$".toRegex(), ".mp4")
	l.add(TCItem(alt, "mpg"))
	alt = fn.replace("\\.avi$".toRegex(), ".mp4")
	l.add(TCItem(alt, "avi"))
	alt = fn.replace("\\.mov$".toRegex(), ".mp4")
	l.add(TCItem(alt, "mov"))
	return l
    }

    private fun attributedImages(ms: String): List<TCItem> {
	val li: MutableList<TCItem> = ArrayList()
	val fName = omegaAssets(ms)
	val fBase = File(fName)
	val fn = fBase.name
	val ix = fn.lastIndexOf('.')
	if (ix == -1) return li
	val fnNEx = fn.substring(0, ix) + "-"
	val dir = fBase.parentFile
	val files = dir.listFiles()
	if (files != null) {
	    for (f in files) {
		if (f.name.startsWith(fnNEx)) {
		    val fn2 = f.path
		    li.add(TCItem(antiOmegaAssets(fn2)!!))
		}
	    }
	}
	return li
    }

    fun gDta(tg2: Target): Array<String?> {
	val sndA = tg2.allSounds
	val snd = a2s(sndA)
	return arrayOf(
		tg2.allText,
		a2s(tg2.all_Lid_Item),
		tg2.getActionFileName(99),  // all
		snd,
		a2s(tg2.all_Lid_Target)
	)
    }

    private fun getAllTargetCombinationsAndMore(dummy: Boolean): MutableList<Array<String?>?> {
	val li: MutableList<Array<String?>?> = ArrayList()
	val tg2 = this
	val Tn = tg2.get_howManyT_Items()
	if (Tn > 0) {
	    val tid0 = tg2.getT_Item(0)!!.tid
	    val it_ent0 = tg2.findItemEntryMatchTid(tid0)
	    val ie_n0 = it_ent0!!.howManyItems()
	    for (i0 in 0 until ie_n0) {
		tg2.pickItemAtDummy(dummy, it_ent0.ord, i0, 0)
		if (Tn > 1) {
		    val tid1 = tg2.getT_Item(1)!!.tid
		    val it_ent1 = tg2.findItemEntryMatchTid(tid1)
		    val ie_n1 = it_ent1!!.howManyItems()
		    for (i1 in 0 until ie_n1) {
			tg2.pickItemAtDummy(dummy, it_ent1.ord, i1, 1)
			if (Tn > 2) {
			    val tid2 = tg2.getT_Item(2)!!.tid
			    val it_ent2 = tg2.findItemEntryMatchTid(tid2)
			    val ie_n2 = it_ent2!!.howManyItems()
			    for (i2 in 0 until ie_n2) {
				tg2.pickItemAtDummy(dummy, it_ent2.ord, i2, 2)
				if (Tn > 3) {
				    val tid3 = tg2.getT_Item(3)!!.tid
				    val it_ent3 = tg2.findItemEntryMatchTid(tid3)
				    val ie_n3 = it_ent3!!.howManyItems()
				    for (i3 in 0 until ie_n3) {
					tg2.pickItemAtDummy(dummy, it_ent3.ord, i3, 3)
					if (Tn > 4) {
					    val tid4 = tg2.getT_Item(4)!!.tid
					    val it_ent4 = tg2.findItemEntryMatchTid(tid4)
					    val ie_n4 = it_ent4!!.howManyItems()
					    for (i4 in 0 until ie_n4) {
						tg2.pickItemAtDummy(dummy, it_ent4.ord, i4, 4)
						if (Tn > 5) {
						    val tid5 = tg2.getT_Item(5)!!.tid
						    val it_ent5 = tg2.findItemEntryMatchTid(tid5)
						    val ie_n5 = it_ent5!!.howManyItems()
						    for (i5 in 0 until ie_n5) {
							tg2.pickItemAtDummy(dummy, it_ent5.ord, i5, 5)
							li.add(gDta(tg2))
						    }
						} else {
						    li.add(gDta(tg2))
						}
					    }
					} else {
					    li.add(gDta(tg2))
					}
				    }
				} else {
				    li.add(gDta(tg2))
				}
			    }
			} else {
			    li.add(gDta(tg2))
			}
		    }
		} else {
		    li.add(gDta(tg2))
		}
	    }
	} else {
	    li.add(gDta(tg2))
	}
	return li
    }

    fun getAllTargetCombinationsIndexes(txt: String): Array<IntArray> {
	val li: List<Array<String>?> = ArrayList()
	val tg2 = this
	val Tn = tg2.get_howManyT_Items()
	//ZZ
	if (Tn > 0) {
	    val tid0 = tg2.getT_Item(0)!!.tid
	    val it_ent0 = tg2.findItemEntryMatchTidAll(tid0)
	    val ie_n0 = A_howManyItems(it_ent0)
	    for (i0 in 0 until ie_n0) {
		tg2.pickItemAtEx(A_getX(it_ent0, i0), A_getY(it_ent0, i0), 0)
		if (Tn > 1) {
		    val tid1 = tg2.getT_Item(1)!!.tid
		    val it_ent1 = tg2.findItemEntryMatchTidAll(tid1)
		    val ie_n1 = A_howManyItems(it_ent1)
		    for (i1 in 0 until ie_n1) {
			tg2.pickItemAtEx(A_getX(it_ent1, i1), A_getY(it_ent1, i1), 1)
			if (Tn > 2) {
			    val tid2 = tg2.getT_Item(2)!!.tid
			    val it_ent2 = tg2.findItemEntryMatchTidAll(tid2)
			    val ie_n2 = A_howManyItems(it_ent2)
			    for (i2 in 0 until ie_n2) {
				tg2.pickItemAtEx(A_getX(it_ent2, i2), A_getY(it_ent2, i2), 2)
				if (Tn > 3) {
				    val tid3 = tg2.getT_Item(3)!!.tid
				    val it_ent3 = tg2.findItemEntryMatchTidAll(tid3)
				    val ie_n3 = A_howManyItems(it_ent3)
				    for (i3 in 0 until ie_n3) {
					tg2.pickItemAtEx(A_getX(it_ent3, i3), A_getY(it_ent3, i3), 3)
					if (Tn > 4) {
					    val tid4 = tg2.getT_Item(4)!!.tid
					    val it_ent4 = tg2.findItemEntryMatchTidAll(tid4)
					    val ie_n4 = A_howManyItems(it_ent4)
					    for (i4 in 0 until ie_n4) {
						tg2.pickItemAtEx(A_getX(it_ent4, i4), A_getY(it_ent4, i4), 4)
						if (Tn > 5) {
						    val tid5 = tg2.getT_Item(5)!!.tid
						    val it_ent5 = tg2.findItemEntryMatchTidAll(tid5)
						    val ie_n5 = A_howManyItems(it_ent5)
						    for (i5 in 0 until ie_n5) {
							tg2.pickItemAtEx(A_getX(it_ent5, i5), A_getY(it_ent5, i5), 5)
							val s = tg2.allText
							if (eq(txt, s)) return arrayOf(
								intArrayOf(
									i0,
									A_getX(it_ent0, i0),
									A_getY(it_ent0, i0)
								),
								intArrayOf(i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)),
								intArrayOf(i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2)),
								intArrayOf(i3, A_getX(it_ent3, i3), A_getY(it_ent3, i3)),
								intArrayOf(i4, A_getX(it_ent4, i4), A_getY(it_ent4, i4)),
								intArrayOf(i5, A_getX(it_ent5, i5), A_getY(it_ent5, i5))
							)
						    }
						} else {
						    val s = tg2.allText
						    if (eq(txt, s)) return arrayOf(
							    intArrayOf(
								    i0,
								    A_getX(it_ent0, i0),
								    A_getY(it_ent0, i0)
							    ),
							    intArrayOf(i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)),
							    intArrayOf(i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2)),
							    intArrayOf(i3, A_getX(it_ent3, i3), A_getY(it_ent3, i3)),
							    intArrayOf(i4, A_getX(it_ent4, i4), A_getY(it_ent4, i4))
						    )
						}
					    }
					} else {
					    val s = tg2.allText
					    if (eq(txt, s)) return arrayOf(
						    intArrayOf(
							    i0,
							    A_getX(it_ent0, i0),
							    A_getY(it_ent0, i0)
						    ),
						    intArrayOf(i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)),
						    intArrayOf(i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2)),
						    intArrayOf(i3, A_getX(it_ent3, i3), A_getY(it_ent3, i3))
					    )
					}
				    }
				} else {
				    val s = tg2.allText
				    if (eq(txt, s)) return arrayOf(
					    intArrayOf(
						    i0,
						    A_getX(it_ent0, i0),
						    A_getY(it_ent0, i0)
					    ),
					    intArrayOf(i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)),
					    intArrayOf(i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2))
				    )
				}
			    }
			} else {
			    val s = tg2.allText
			    if (eq(txt, s)) return arrayOf(
				    intArrayOf(i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)),
				    intArrayOf(i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1))
			    )
			}
		    }
		} else {
		    val s = tg2.allText
		    if (eq(txt, s)) return arrayOf(intArrayOf(i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)))
		}
	    }
	} else {
	    val s = tg2.allText
	    if (eq(txt, s)) return Array(0) { IntArray(0) }
	}
	return Array(0) { IntArray(0) }
    }

    private fun eq(s1: String, s2: String): Boolean {
	if (Tr) Log.getLogger().info("ERR: " + "eq ." + s1 + '.' + s2 + '.' + s1.equals(s2, ignoreCase = true))
	return s1.equals(s2, ignoreCase = true)
    }

    private fun A_howManyItems(it_entA: Array<ItemEntry>): Int {
	var n = 0
	if (Tr) Log.getLogger().info("ERR: " + "howMany( " + a2s(it_entA))
	for (i in it_entA.indices) {
	    if (Tr) Log.getLogger().info("ERR: " + "howMany " + i + ' ' + it_entA[i].howManyItems())
	    n += it_entA[i].howManyItems()
	}
	if (Tr) Log.getLogger().info("ERR: howMany) $n")
	return n
    }

    private fun A_getX(it_entA: Array<ItemEntry>, `in`: Int): Int {
	var `in` = `in`
	if (Tr) Log.getLogger().info("ERR: getX( $`in`")
	for (i in it_entA.indices) {
	    if (`in` < it_entA[i].howManyItems()) {
		if (Tr) Log.getLogger().info("ERR: " + "getX) " + it_entA[i].howManyItems() + ' ' + i)
		return it_entA[0].ord + i
	    } else {
		`in` -= it_entA[i].howManyItems()
		if (Tr) Log.getLogger().info("ERR: " + "getX) - " + it_entA[i].howManyItems() + ' ' + i + ' ' + `in`)
	    }
	}
	if (Tr) Log.getLogger().info("ERR: " + "getX) -1")
	return -1
    }

    private fun A_getY(it_entA: Array<ItemEntry>, `in`: Int): Int {
	var `in` = `in`
	if (Tr) Log.getLogger().info("ERR: getY( $`in`")
	for (i in it_entA.indices) {
	    if (`in` < it_entA[i].howManyItems()) {
		if (Tr) Log.getLogger().info("ERR: " + "getY) " + it_entA[i].howManyItems() + ' ' + i + ' ' + `in`)
		return `in`
	    } else {
		`in` -= it_entA[i].howManyItems()
		if (Tr) Log.getLogger().info("ERR: " + "getX) - " + it_entA[i].howManyItems() + ' ' + i + ' ' + `in`)
	    }
	}
	if (Tr) Log.getLogger().info("ERR: " + "getY) -1")
	return -1
    }

    val storyNext: String?
	get() {
	    var s = story_next ?: return null
	    s = s.replace(
		    "lesson-[a-zA-Z]*/active".toRegex(),
		    omegaAssets("lesson-" + lessonLang + "/active")!!
	    ) // LESSON-DIR-A
	    return s
	}

    override fun toString(): String {
	return "Target{t_item=$t_items, items=$items}"
    }
}
