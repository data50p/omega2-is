package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker.Companion.comparator
import com.femtioprocent.omega.xml.Element
import java.beans.PropertyChangeSupport
import java.util.*
import javax.swing.event.EventListenerList

class MasterTimeLine(var a_ctxt: AnimContext) : PlayCtrlListener {
    private val pr_ch: PropertyChangeSupport
    private var timelines: Array<TimeLine?>
    private val play_listeners: EventListenerList
    private val edit_listeners: EventListenerList
    private var dry = false
    fun initNew() {
	timelines = arrayOfNulls(maxTimeLineIndex)
    }

    val maxDuration: Int
	get() {
	    var max = 0
	    for (tl in timelines) {
		if (tl != null) {
		    val d = tl.offset + tl.duration
		    if (d > max) max = d
		}
	    }
	    return max
	}

    fun getTimeLine(nid: Int): TimeLine? {
	return if (nid < 0 || nid >= timelines.size) null else timelines[nid]
    }

    fun addTimeLine(tl: TimeLine) {
	timelines[tl.nid] = tl
	pr_ch.firePropertyChange("timelines", null, timelines)
    }

    fun removeTimeLine(nid: Int) {
	val tl = timelines[nid]
	timelines[nid] = null
    }

    val freeTLIndex: Int
	get() {
	    for (i in timelines.indices) if (timelines[i] == null) {
		return i
	    }
	    return -1
	}

    private fun getMarkers(type: Char): List<TimeMarker> {
	val l: MutableList<TimeMarker> = ArrayList()
	for (tl in timelines) {
	    if (tl == null) continue
	    l.addAll(tl.getMarkersType(type))
	}
	val tla = l.toTypedArray<TimeMarker>()
	Arrays.sort(tla, comparator)
	return Arrays.asList(*tla)
    }

    private fun getMarkersAbs(from: Int, to: Int): List<TimeMarker> {
	val l: MutableList<TimeMarker> = ArrayList()
	for (tl in timelines) {
	    if (tl == null) continue
	    l.addAll(tl.getMarkersAbs(from, to))
	}
	val tla = l.toTypedArray<TimeMarker>()
	Arrays.sort(tla, comparator)
	return Arrays.asList(*tla)
    }

    //      private List getAllTimeLinesAt(int now) {
    //  	List l = new ArrayList();
    //  	for(int i = 0; i < timelines.length; i++) {
    //  	    TimeLine tl = timelines[i];
    //  	    if ( tl == null )
    //  		;
    //  	    l.add(tl);
    //  	}
    //  	return l;
    //      }
    fun addPlayListener(l: PlayListener) {
	play_listeners.add(PlayListener::class.java, l)
    }

    fun removePlayListener(l: PlayListener) {
	play_listeners.remove(PlayListener::class.java, l)
    }

    fun addEditListener(l: EditListener) {
	edit_listeners.add(EditListener::class.java, l)
    }

    fun removeEditListener(l: EditListener) {
	edit_listeners.remove(EditListener::class.java, l)
    }

    fun fireEventMarkerAtTime(from: Int, to: Int) {
	val l = getMarkersAbs(from, to)
	l.forEach {tm ->
	    val lia = play_listeners.listenerList
	    var i = 0
	    while (i < lia.size) {
		(lia[i + 1] as PlayListener).actionMarkerAtTime(tm, to, dry)
		i += 2
	    }
	}
    }

    fun fireEventAtTime(t: Int) {
//	List l = getAllTimeLinesAt(t);
	val tlA = timelines // (TimeLine[])l.toArray(new TimeLine[0]);
	val lia = play_listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as PlayListener).actionAtTime(tlA, t, 0, dry)
	    i += 2
	}
    }

    fun updateEndMarkers(`when`: Int) {
	for (tl in timelines) {
	    if (tl == null) continue
	    tl.updateEndMarker(`when`)
	}
	stop_time = `when`
    }

    fun updateBeginMarkers(`when`: Int) {
	for (tl in timelines) {
	    if (tl == null) continue
	    tl.updateBeginMarker(`when`)
	}
    }

    fun playBegin(dry: Boolean) {
	this.dry = dry
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "PLAY BEGIN");
    }

    fun playEnd(`when`: Int) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "PLAY END");
	dry = false
    }

    fun playAt2(last: Int, now: Int) {
	fireEventMarkerAtTime(last, now)
	fireEventAtTime(now)
    }

    fun fillElement(el: Element) {
	el.add( Element("MTL").also {
	    timelines.filter { it != null }.forEach { tl -> it.add(tl!!.element) }
	})
    }

    //      public void save(XML_PW xmlpw) {
    //  	Element mel = new Element("MTL");
    //  	xmlpw.push(mel);
    //  	for(int i = 0; i < timelines.length; i++) {
    //  	    TimeLine tl = timelines[i];
    //  	    if ( tl == null )
    //  		continue;
    //  	    Element el = tl.getElement();
    //  	    xmlpw.put(el);
    //  	}
    //  	xmlpw.pop();
    //      }
    fun load(el: Element) { // MTL
	timelines = arrayOfNulls(maxTimeLineIndex)
	for (i in timelines.indices) {
	    try {
		val tel = el.findElement("TimeLine", i) ?: break
		val tl = TimeLine(tel)
		updateEndMarkers(tl.endMarker)
		addTimeLine(tl)
	    } catch (ex: Exception) {
	    }
	}
    }

    val lessonId_TimeLines: Array<String>
	get() {
	    val li = ArrayList<String>()
	    for (tl in timelines) {
		if (tl != null) {
		    val lid = tl.lessonId
		    if (lid != null && lid.length > 0) li.add(lid)
		}
	    }
	    return li.toTypedArray<String>()
	}

    fun getNid(lesson_id: String): Int {
	for (i in timelines.indices) {
	    val tl = timelines[i]
	    if (tl != null) {
		val lid = tl.lessonId
		if (lid != null) if (lid == lesson_id) return i
	    }
	}
	return -1
    }

    // --------- interface PlayCtrlListener
    var stop_time = 5000

    // --------
    var lastTimeTick = 0

    init {
	pr_ch = PropertyChangeSupport(this)
	timelines = arrayOfNulls(maxTimeLineIndex)
	play_listeners = EventListenerList()
	edit_listeners = EventListenerList()
    }

    override fun beginPlay(dry: Boolean) {
	lastTimeTick = 0
	playBegin(dry)
    }

    fun playAt(lt: Int, t: Int): Boolean {
	playAt2(lt, t)
	return t >= stop_time
    }

    override fun playAt(t: Int): Boolean {
	playAt2(lastTimeTick, t)
	lastTimeTick = t
	return t >= stop_time
    }

    override fun endPlay() {
	playEnd(lastTimeTick)
    }

    override fun propertyChanged(s: String?) {}

    fun setStopTime(st: Int) {
	stop_time = st
    }

    fun getFirst2Path_LessonId(verb: String): String {
	return try {
	    var ss = ""
	    var cnt = 0
	    for (i in 0..3) {
		val s = getTimeLine(i)!!.lessonId
		if (s.length > 0) {
		    cnt++
		    ss += if (cnt == 1) "$$s $verb" else {
			return "$ss $$s"
		    }
		}
	    }
	    verb
	} catch (ex: NullPointerException) {
	    verb
	}
    }

    fun fetchPlaySound(b: BooleanArray): List<String> {
	val li: MutableList<String> = ArrayList()
	for (i in timelines.indices) {
	    val tl = timelines[i]
	    if (tl != null && b[i]) tl.fetchPlaySound(li)
	}
	return li
    }

    companion object {
	val maxTimeLineIndex: Int
	    get() = OmegaConfig.TIMELINES_N
    }
}
