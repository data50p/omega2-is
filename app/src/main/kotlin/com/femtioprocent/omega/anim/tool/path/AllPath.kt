package com.femtioprocent.omega.anim.tool.path

import com.femtioprocent.omega.graphic.render.Canvas
import com.femtioprocent.omega.xml.Element
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.geom.Point2D

class AllPath {
    var li: MutableList<Path?> = ArrayList()
    fun add(pa: Path?) {
	li.add(pa)
    }

    operator fun get(nid: Int): Path? {
	li.forEach { pa -> if (pa!!.nid == nid) return pa }
	return null
    }

    fun removePath(nid: Int) {
	val pa = find(nid)
	li.remove(pa)
    }

    fun deselectAll(g2: Graphics2D?) {
	li.forEach { pa ->
	    pa!!.selected = false
	    if (g2 != null) pa.draw(g2)
	}
    }

    fun findNearest(p: Point2D?): Probe? {
	var nearest: Probe? = null
	li.forEach { pa ->
	    val n1 = pa!!.findNearest(p)
	    if (nearest == null) nearest = n1 else {
		if (nearest!!.dist > n1.dist) {
		    nearest = n1
		}
	    }
	}
	return nearest
    }

    fun findNearestMarker(p: Point2D?): Path.Mark? {
	var fpm: Path.Mark? = null
	var dist = 9999999.0
	li.forEach { pa ->
	    if (true || pa!!.isSelected()) {
		val fmk = pa!!.findNearestMarker(p)
		if (fmk != null) {
		    val fdist = pa.distMarker(fmk, p)
		    if (fdist < dist) {
			dist = fdist
			fpm = fmk
		    }
		}
	    }
	}
	return fpm
    }

    fun find(nid: Int): Path? {
	li.forEach { pa -> if (pa!!.nid == nid) return pa }
	return null
    }

    fun findSelected(): Path? {
	li.forEach { pa -> if (pa!!.isSelected()) return pa }
	return null
    }

    fun redraw(g2: Graphics2D?) {
	li.forEach { pa -> pa!!.draw(g2!!) }
    }

    internal inner class P_Canvas : Canvas() {
	fun redrawControl(g2: Graphics2D) {
	    g2.color = Color.blue
	    for (i in 0..3) g2.drawRect(0, i * 20, 20, 20)
	}

	override fun paintComponent(g: Graphics) {
	    super.paintComponent(g)
	    val g2 = g as Graphics2D
	    redraw(g2)
	    redrawControl(g2)
	}
    }

    val element: Element
	get() {
	    val el = Element("AllPath")
	    li.forEach {pa ->
		val pel = pa!!.element
		el.add(pel)
	    }
	    return el
	}

    fun load(el: Element) {
	li = ArrayList()
	for (i in 0..99) {
	    try {
		val eel = el.findElement("TPath", i) ?: break
		val pa = Path(eel)
		li.add(pa)
	    } catch (ex: Exception) {
	    }
	}
    }
}
