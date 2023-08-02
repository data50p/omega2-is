package com.femtioprocent.omega.graphic.render

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.anim.cabaret.GImAE
import com.femtioprocent.omega.anim.tool.timeline.TimeLine
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.xml.Element
import java.awt.geom.Rectangle2D

class AllGIm internal constructor(var ca: Canvas) {
    var arr = arrayOfNulls<GIm>(OmegaConfig.TIMELINES_N)

    operator fun get(nid: Int): GIm? {
	return if (nid < arr.size) arr[nid] else null
    }

    operator fun set(gim: GIm?, ix: Int) {
	if (ix >= arr.size) return
	hideActor(ix)
	arr[ix] = gim
	if (gim != null) (gim as GImAE).nid = ix
    }

    fun remove(gim: GIm) {
	for (i in arr.indices) if (arr[i] === gim) {
	    hideActor(i)
	    arr[i] = null
	}
    }

    fun initPlay(o: Any?) {
	hideActors()
	for (i in arr.indices) {
	    val gim = get(i) // arr[i];
	    gim?.initPlay(o)
	}
    }

    fun hideActors() {
	val rl = RectList()
	for (i in arr.indices) {
	    val gim = arr[i]
	    if (gim != null) {
		gim.restoreBackground()
		rl.add(gim.prevBoundingRect)
		val bounding_rect: Rectangle2D = Rectangle2D.Double(10000.0, 10000.0, 0.0, 0.0)
		gim.prevBoundingRect = bounding_rect
	    }
	}
	ca.off_upd(rl.rl.toTypedArray<Rectangle2D>())
    }

    fun hideActor(ix: Int) {
	val rl = RectList()
	val gim = arr[ix]
	if (gim != null) {
	    gim.restoreBackground()
	    val bounding_rect: Rectangle2D = Rectangle2D.Double(10000.0, 10000.0, 0.0, 0.0)
	    gim.prevBoundingRect = bounding_rect
	    rl.add(gim.prevBoundingRect)
	    ca.off_upd(rl.rl.toTypedArray<Rectangle2D>())
	}
    }

    fun updateAtTime(dt: Int, tlA: Array<TimeLine?>) {
	val rl = RectList()
	val gA = arr
	for (ii in gA.indices) {
	    if (tlA[ii] == null) continue
	    val gim = get(ii)
	    if (gim != null) {
		try {
		    gim.restoreBackground()
		    val br: Rectangle2D = Rectangle2D.Double(0.0, 0.0, 0.0, 0.0)
		    br.setRect(gim.prevBoundingRect)
		    rl.add(br)
		    gim.commitAttribName()
		    val an_sp = (1000 * (gim as GImAE).anim_speed).toInt()
		    var td1 = dt - gim.reset_sequence
		    if (td1 < 0) td1 = 0
		    val td2 = td1 / an_sp
		    val tm = td2 % 1000 // why?
		    if (gim.xim.setInnerAnimIndex(dt, tm)) gim.initIm()
		} catch (ex: NullPointerException) {
		    Log.getLogger().info("ERR: ---1 $ii $ex")
		    ex.printStackTrace()
		}
	    }
	}
	for (i in 0..4) {
	    for (ii in gA.indices) {
		if (tlA[ii] == null) continue
		val gim = get(ii) as GImAE// gA[ii];
		if (gim != null) {
		    try {
			if (gim.layer == i) {
			    var bounding_rect: Rectangle2D = Rectangle2D.Double(0.0, 0.0, 0.0, 0.0)
			    val at = gim.getAffineTransformAtTime(dt, tlA as Array<TimeLine>, bounding_rect)
			    if (at != null) {
//				gim.render(at);
				gim.render(at)
				rl.add(bounding_rect)
				gim.prevBoundingRect = bounding_rect
			    } else {
				bounding_rect = Rectangle2D.Double(0.0, 0.0, 0.0, 0.0)
			    }
			}
		    } catch (ex: NullPointerException) {
			Log.getLogger().info("ERR: ---2 $ii $ex")
		    }
		}
	    }
	    if (ca.wings != null) for (iw in ca.wings.indices) {
		if (ca.getWing(iw).layer == i) ca.drawWing(ca.getWing(iw))
	    }
	}
	ca.off_upd(rl.rl.toTypedArray<Rectangle2D>())
    }

    val element: Element
	get() {
	    val el = Element("AllGIm")
	    for (i in arr.indices) {
		val ael = Element("actor")
		el.add(ael)
	    }
	    return el
	}
}
