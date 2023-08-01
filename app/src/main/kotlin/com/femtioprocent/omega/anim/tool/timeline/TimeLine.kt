package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.anim.panels.timeline.TimeLinePanel
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventFactory.createTriggerEvent
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventFactory.getSlot
import com.femtioprocent.omega.util.Num.grid
import com.femtioprocent.omega.xml.Element
import java.io.Serializable
import java.util.*

class TimeLine : Serializable {
    var offset = 0
    var duration = 0
	private set
    private var markers: MutableList<TimeMarker>
    var nid: Int
    var last_added_tm: TimeMarker? = null
    var lessonId = ""

    constructor(nid: Int) {
	this.nid = nid
	markers = LinkedList()
    }

    constructor(el: Element) {
	markers = LinkedList()
	val id = el.findAttr("lesson_id")
	if (id != null) lessonId = id
	nid = el.findAttr("nid")!!.toInt()
	offset = el.findAttr("offset")!!.toInt()
	duration = el.findAttr("duration")!!.toInt()
	for (i in 0..99) {
	    val me = el.findElement("TimeMarker", i) ?: break
	    val t = me.findAttr("type")
	    val w = me.findAttr("when")
	    val d = me.findAttr("duration")
	    val wl = w!!.toInt()
	    var dl = 0
	    if (d != null) dl = d.toInt()
	    addMarker(t!![0], wl, dl)
	    val teel = me.findElement("T_Event", 0)
	    if (teel != null) {
		val tm = last_added_tm
		for (ii in 0..999) {
		    val tee = teel.findElement("TriggerEvent", ii) ?: break
		    val te = createTriggerEvent(tee)
		    val slot = getSlot(te!!.cmd)
		    tm!!.setTriggerEvent(te, slot)
		}
	    }
	    if (t == "}") TimeLinePanel.playEnd = wl + offset // FIX
	}
    }

    constructor(nid: Int, offset: Int, duration: Int) : this(nid) {
	this.offset = offset
	this.duration = duration
	addMarker('[', 0)
	addMarker(']', duration)
    }

    constructor(nid: Int, src: TimeLine) {
	markers = LinkedList()
	this.nid = nid
	offset = src.offset
	duration = src.duration
	val it: Iterator<TimeMarker> = src.markers.iterator()
	while (it.hasNext()) {
	    val tm = it.next()
	    val new_tm = addMarker(
		    tm.type,
		    tm.`when` - offset,
		    tm.duration
	    )
	    if (tm.t_event != null) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "t_ev " + tm.t_event.length);
		for (i in tm.t_event!!.indices) {
		    val te = tm.t_event!![i]!!
		    val nte = createTriggerEvent(te.cmd)
		    if (nte != null) {
			val arg = te.argString
			if (nte.hasSelections()) {
			    (nte as TriggerEventSelections).setArgFromCmd(arg)
			} else nte.arg = arg
			nte.setOn(te.is_on)
		    }
		    //		    OmegaContext.sout_log.getLogger().info(":--: " + "new " + nte);
		    new_tm!!.setTriggerEvent(nte, i)
		}
	    }
	}
    }

    fun addMarker(type: Char, `when`: Int, duration: Int = 0): TimeMarker? {
	val tm = TimeMarker(this, type, offset + `when`, duration)
	markers.add(tm)
	reNumerateMarker()
	last_added_tm = tm
	return last_added_tm
    }

    fun addMarker(tltl: TimeLine?, `when`: Int): TimeMarker? {
	val tm = TimeMarker(this, tltl, offset + `when`)
	markers.add(tm)
	reNumerateMarker()
	last_added_tm = tm
	return last_added_tm
    }

    fun removeMarker(tm: TimeMarker) {
	markers.remove(tm)
    }

    fun reNumerateMarker() {
	val ma = markers.toTypedArray<TimeMarker>()
	Arrays.sort(ma) { tm1: TimeMarker, tm2: TimeMarker -> tm1.`when` - tm2.`when` }
	markers = ArrayList()
	for (i in ma.indices) ma[i].ord = i
	var ty = TimeMarker.TSYNC.code
	var cc = 0
	//   	for(int i = 0; i < ma.length; i++)
//   	    if ( ma[i].type == ty )
// 		ma[i].ord_same_type = cc++;
	ty = TimeMarker.TRIGGER.code
	cc = 0
	//   	for(int i = 0; i < ma.length; i++)
//   	    if ( ma[i].type == ty )
// 		ma[i].ord_same_type = cc++;
	val col: Collection<TimeMarker> = Arrays.asList(*ma)
	markers.addAll(col)
    }

    val timeMarker_TSyncSegments: DoubleArray
	get() {
	    val ta = getAllTimeMarkerType(TimeMarker.TSYNC)
	    var da: DoubleArray? = null
	    if (ta == null || ta.size == 0) {
		da = DoubleArray(2)
		da[0] = offset.toDouble()
		da[1] = (duration + offset).toDouble()
	    } else {
		da = DoubleArray(ta.size + 2)
		for (i in da.indices) {
		    if (i == 0) {
			da[i] = offset.toDouble()
		    } else if (i == da.size - 1) {
			da[i] = (duration + offset).toDouble()
		    } else {
			da[i] = ta[i - 1]!!.`when`.toDouble()
		    }
		}
	    }
	    return da
	}

    fun getAllTimeMarkerType(t: Char): Array<TimeMarker?> {
	var c = 0
	var it: Iterator<TimeMarker> = markers.iterator()
	while (it.hasNext()) {
	    val tm = it.next()
	    if (tm.type == t) c++
	}
	val ta = arrayOfNulls<TimeMarker>(c)
	c = 0
	it = markers.iterator()
	while (it.hasNext()) {
	    val tm = it.next()
	    if (tm.type == t) ta[c++] = tm
	}
	Arrays.sort(ta) { o1, o2 -> o1!!.`when` - o2!!.`when` }
	return ta
    }

    fun adjustSomeTimeMarkerRelative(d: Double) {
	for (tm in markers) {
	    if (tm.relativeAdjust()) {
		val dd = d * (tm.`when` - offset)
		tm.move(dd.toInt(), 1)
	    }
	}
    }

    fun adjustMove(d: Int): Int {
	return if (offset + d < 0) -(offset - 1) else d
    }

    fun move(d: Int) {
	move(d, 1)
    }

    fun move(d: Int, grid: Int) {
	offset += d
	offset = grid(offset, grid)
	for (tm in markers) {
	    if (tm.isMoveAble) tm.move(d, grid)
	}
    }

    fun moveSelectedTimeMarker(d: Int, grid: Int) {
	for (tm in markers) {
	    if (tm.selected) {
		if (tm.type == TimeMarker.STOP) size(d) else if (tm.type == TimeMarker.START) move(d) else if (tm.isMoveAble) tm.move(
			d,
			grid
		)
	    }
	}
    }

    val selectedTimeMarker: TimeMarker?
	get() {
	    for (tm in markers) {
		if (tm.selected) {
		    return tm
		}
	    }
	    return null
	}

    fun updateEndMarker(`when`: Int) {
	for (tm in markers) {
	    if (tm.type == TimeMarker.END) tm.`when` = `when`
	}
    }

    val endMarker: Int
	get() {
	    for (tm in markers) {
		if (tm.type == TimeMarker.END) return tm.`when`
	    }
	    return 2000
	}

    fun updateBeginMarker(`when`: Int) {
	for (tm in markers) {
	    if (tm.type == TimeMarker.BEGIN) tm.`when` = `when`
	}
    }

    fun size(d: Int) {
	duration += d
	if (duration <= 0) duration = 1
	for (tm in markers) {
	    if (tm.type == TimeMarker.STOP) tm.`when` = offset + duration
	}
    }

    fun getMarkersAbs(from: Int, to: Int): List<TimeMarker> {
	return getMarkersAbs(from, to, false)
    }

    fun getMarkersAbs(from: Int, to: Int, special: Boolean): List<TimeMarker> {
	val l: MutableList<TimeMarker> = ArrayList()
	for (tm in markers) {
	    if (tm.`when` > from && tm.`when` <= to) l.add(tm)
	}
	return l
    }

    fun getMarkersType(type: Char): List<TimeMarker> {
	val l: MutableList<TimeMarker> = ArrayList()
	for (tm in markers) {
	    if (tm.type == type) l.add(tm)
	}
	return l
    }

    fun getMarkerAtIndexType(ix: Int, type: Char): TimeMarker? {
	val it: Iterator<TimeMarker> = markers.iterator()
	var c = 0
	while (it.hasNext()) {
	    val tm = it.next()
	    if (tm.type == type) if (c == ix) return tm else c++
	}
	return null
    }

    fun getNearestTimeMarker(dt: Int): TimeMarker? {
	var dt_f = 9999999
	var tm_f: TimeMarker? = null
	for (tm in markers) {
	    val tdist = Math.abs(tm.`when` - dt)
	    if (tdist < dt_f) {
		dt_f = tdist
		tm_f = tm
	    }
	}
	return tm_f
    }

    fun setDeselectTimeMarker() {
	for (tm in markers) {
	    tm.selected = false
	}
    }

    fun activeNow(now: Int): Boolean {
	return now > offset && now < offset + duration
    }

    override fun toString(): String {
	return "TimeLine{" +
		"nid=" + nid +
		", markers.size()=" + markers.size +
		"}"
    }

    fun toStringDeep(): String {
	return "TimeLine{" +
		"" + markers +
		"}"
    }

    val element: Element
	get() {
	    val el = Element("TimeLine")
	    el.addAttr("lesson_id", "" + lessonId)
	    el.addAttr("offset", "" + offset)
	    el.addAttr("duration", "" + duration)
	    el.addAttr("nid", "" + nid)
	    for (tm in markers) {
		el.add(tm.element)
	    }
	    return el
	}

    fun fetchPlaySound(li: MutableList<String>) {
	for (tm in markers) {
	    if (li != null) {
		tm.fetchPlaySound(li)
	    }
	}
    }
}
