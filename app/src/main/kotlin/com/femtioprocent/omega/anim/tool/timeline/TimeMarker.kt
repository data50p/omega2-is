package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.anim.tool.timeline.TriggerEventFactory.allAsStringA
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventFactory.size
import com.femtioprocent.omega.util.Num.grid
import com.femtioprocent.omega.xml.Element
import java.io.Serializable

class TimeMarker : Serializable {
    var tl: TimeLine
    var type: Char
    var `when`: Int
    var duration = 0
    var tltl: TimeLine? = null
    var selected = false
    var t_event: Array<TriggerEvent?>? = null
    var isDeleteCandidate = false
    var ord = 0

    internal constructor(tl: TimeLine, type: Char, `when`: Int) {
	this.tl = tl
	this.type = type
	this.`when` = `when`
	init()
    }

    internal constructor(tl: TimeLine, type: Char, `when`: Int, duration: Int) {
	this.tl = tl
	this.type = type
	this.`when` = `when`
	this.duration = duration
	init()
    }

    internal constructor(tl: TimeLine, tltl: TimeLine?, `when`: Int) {
	this.tl = tl
	type = TIMELINE
	this.`when` = `when`
	this.tltl = tltl
	init()
    }

    private fun init() {
	if (type == TRIGGER || type == START || type == STOP || type == BEGIN || type == END) {
	    t_event = arrayOfNulls(size)
	    val sa = allAsStringA
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "Trigger " + SundryUtils.arrToString(sa));
	    for (i in sa.indices) {
		t_event!![i] = TriggerEventFactory[i]
	    }
	}
    }

    val isMoveAble: Boolean
	get() = !(type == BEGIN || type == END)

    fun move(d: Int, grid: Int = 1) {
	`when` += d
	`when` = grid(`when`, grid)
	if (type == TIMELINE) tltl!!.move(d, grid)
	tl.reNumerateMarker()
    }

    fun canRemove(): Boolean {
	return type == TSYNC || type == TRIGGER
    }

    fun relativeAdjust(): Boolean {
	return type == TSYNC || type == TRIGGER
    }

    fun typeString(type: Char): String {
	for (i in typeString.indices) if (typeString[i][0] == type) return typeString[i].substring(1)
	return "--noname--"
    }

    fun setTriggerEvent(te: TriggerEvent?, ix: Int) {
	try {
	    t_event!![ix] = te
	} catch (ex: Exception) {
	}
    }

    fun doAllAction(tea: TriggerEventAction, dry: Boolean) {
	if (t_event != null) for (i in t_event!!.indices) if (t_event!![i] != null) if (t_event!![i]!!.is_on) if (t_event!![i]!!.cmd.length > 0) tea.doAction(
	    t_event!![i],
	    this,
	    dry
	)
    }

    override fun toString(): String {
	return "TimeMarker{" +
		"tl=" + tl +
		", type=" + (if (type == TIMELINE) tltl.toString() else java.lang.String("" + type) as String) +
		", when=" + `when` +
		", duration=" + duration +
		"}"
    }

    val element: Element
	get() {
	    val el = Element("TimeMarker")
	    el.addAttr("type", "" + type)
	    el.addAttr("when", "" + (`when` - tl.offset))
	    el.addAttr("duration", "" + duration)
	    if (t_event != null) {
		val tel = Element("T_Event")
		for (i in t_event!!.indices) {
		    if (t_event!![i] != null) {
			val teel = t_event!![i]!!.element
			tel.add(teel)
		    }
		}
		el.add(tel)
	    }
	    return el
	}

    fun fetchPlaySound(li: MutableList<String>) {
	if (t_event != null) for (i in t_event!!.indices) {
	    if (t_event!![i] != null) {
		if ("PlaySound" == t_event!![i]!!.cmd) {
		    if (t_event!![i]!!.is_on) {
			val arg = t_event!![i]!!.argString
			if (arg != null && arg.length > 0) li.add(arg)
		    }
		}
	    }
	}
    }

    fun findTEvent(id: String): TriggerEvent? {
	if (t_event != null) for (i in t_event!!.indices) {
	    if (t_event!![i] != null) {
		if (id == t_event!![i]!!.cmd) {
		    return t_event!![i]
		}
	    }
	}
	return null
    }

    companion object {
	const val BEGIN = '{'
	const val END = '}'
	const val START = '['
	const val STOP = ']'
	const val TSYNC = '^'
	const val TRIGGER = 't'
	const val TIMELINE = 'T'
	val typeString = arrayOf(
	    "{Begin",
	    "}End",
	    "[Start",
	    "]Stop",
	    "^TimeSync",
	    "tTrigger",
	    "TTimeLine"
	)

	var comparator: Comparator<TimeMarker>? = null
	    get() {
		if (field == null) field = object : Comparator<TimeMarker> {
		    override fun compare(o1: TimeMarker?, o2: TimeMarker?): Int {
			val ta = o1 as TimeMarker
			val tb = o2 as TimeMarker
			val d = ta.`when` - tb.`when`
			return if (d < 0) -1 else if (d > 0) 1 else 0
		    }
		}
		return field
	    }
	    private set
    }
}
