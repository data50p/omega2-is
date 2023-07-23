package com.femtioprocent.omega.anim.cabaret

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.anim.cabaret.Cabaret.ActA.Act
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.xml.Element

class Cabaret(var a_ctxt: AnimContext) {
    val ACT_N = OmegaConfig.CABARET_ACTOR_N
    val EMPTY = -1
    @JvmField
    var actA = ActA()

    inner class ActA internal constructor() {
	inner class Act internal constructor(var ord: Int) {
	    @JvmField
            var ac: Actor? = null
	    @JvmField
            var tl_nid: Int

	    init {
		tl_nid = EMPTY
	    }
	}

	@JvmField
        var arr: Array<Act?>

	init {
	    arr = arrayOfNulls(ACT_N)
	    for (i in arr.indices) {
		arr[i] = Act(i)
	    }
	}

	fun getGImAE(ix: Int): GImAE? {
	    return try {
		arr[ix]!!.ac!!.gimae
	    } catch (ex: NullPointerException) {
		null
	    }
	}

	fun getTLnid(ix: Int): Int {
	    return try {
		arr[ix]!!.tl_nid
	    } catch (ex: NullPointerException) {
		-1
	    }
	}

	fun findFree(): Int {
	    for (i in arr.indices) {
		if (arr[i]!!.tl_nid == EMPTY) return i
	    }
	    return -1
	}

	fun findOrdTL(tl_nid: Int): Int {
	    for (i in arr.indices) {
		if (arr[i]!!.tl_nid == tl_nid) return i
	    }
	    return -1
	}
    }

    fun newActA() {
	actA = ActA()
    }

    fun setActor(ix: Int, act: Actor?) {
	actA.arr[ix]!!.ac = act
    }

    fun getActor(ix: Int): Actor? {
	return getAct(ix)!!.ac
    }

    fun getAct(ix: Int): Act? {
	return actA.arr[ix]
    }

    fun getTLnid(ix: Int): Int {
	return actA.getTLnid(ix)
    }

    fun actorNum(): Int {
	return actA.arr.size
    }

    fun createActor(ix: Int, fn: String, hotspot: DoubleArray?): Actor {
	val gim = GImAE(a_ctxt.anim_canvas!!, fn, ix)
	val act = Actor(a_ctxt, gim)
	if (hotspot == null) act.gimae.setHotSpot(0.5, 0.5) else act.gimae.setHotSpot(hotspot[0], hotspot[1])
	setActor(ix, act)
	return act
    }

    val lessonId: Array<String>
	get() {
	    val li = ArrayList<String>()
	    for (i in actA.arr.indices) {
		val act = actA.arr[i]!!.ac
		if (act != null) {
		    val lid = act.gimae.lessonId
		    if (lid != null && !lid.startsWith("#") && lid.length > 0) li.add(lid)
		}
	    }
	    return li.toTypedArray<String>()
	}

    private fun reduce(s: String?, v: Int): String? {
	var v = v
	if (v == 0) return s
	val ix = s!!.lastIndexOf(':')
	return if (ix == -1) s else reduce(s.substring(0, ix), --v)
    }

    private fun match(s1: String, s2: String?): Boolean {
	return if (s1 == s2) true else false
    }

    fun findActorByLessonId(s1: String): GImAE? {  // reduce x:y:z to x:y while not match
	var s1 = s1
	for (r_val in 0..4) {
	    for (i in actA.arr.indices) {
		val act = actA.arr[i]
		if (act != null && act.ac != null) {
		    val s2 = act.ac!!.gimae.lessonId
		    val ss2 = reduce(s2, r_val)
		    if (match(s1, ss2)) {
			return act.ac!!.gimae
		    }
		}
	    }
	}
	var ix: Int
	if (s1.lastIndexOf(':').also { ix = it } != -1) {
	    s1 = s1.substring(0, ix)
	    return findActorByLessonId(s1)
	}
	return null
    }

    val element: Element
	get() {
	    val el = Element("Cabaret")
	    el.addAttr("a", "b")
	    return el
	}
}
