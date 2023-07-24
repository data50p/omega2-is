package com.femtioprocent.omega.anim.tool.path

import com.femtioprocent.omega.graphic.render.Canvas
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.xml.Element
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.geom.Point2D
import javax.swing.JFrame
import javax.swing.event.MouseInputAdapter

class AllPath {
    var li: MutableList<Path?> = ArrayList()
    fun add(pa: Path?) {
	li.add(pa)
    }

    operator fun get(nid: Int): Path? {
	val it: Iterator<*> = li.iterator()
	while (it.hasNext()) {
	    val pa = it.next() as Path
	    if (pa.nid == nid) return pa
	}
	return null
    }

    fun removePath(nid: Int) {
	val pa = find(nid)
	li.remove(pa)
    }

    fun deselectAll(g2: Graphics2D?) {
	val it: Iterator<*> = li.iterator()
	while (it.hasNext()) {
	    val pa = it.next() as Path
	    pa.selected = false
	    if (g2 != null) pa.draw(g2)
	}
    }

    fun findNearest(p: Point2D?): Probe? {
	var nearest: Probe? = null
	val it: Iterator<*> = li.iterator()
	while (it.hasNext()) {
	    val pa = it.next() as Path
	    val n1 = pa.findNearest(p)
	    if (nearest == null) nearest = n1 else {
		if (nearest.dist > n1.dist) {
		    nearest = n1
		}
	    }
	}
	return nearest
    }

    fun findNearestMarker(p: Point2D?): Path.Mark? {
	val it: Iterator<*> = li.iterator()
	var fpm: Path.Mark? = null
	var dist = 9999999.0
	while (it.hasNext()) {
	    val pa = it.next() as Path
	    if (true || pa.isSelected()) {
		val fmk = pa.findNearestMarker(p)
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
	val it: Iterator<*> = li.iterator()
	while (it.hasNext()) {
	    val pa = it.next() as Path
	    if (pa.nid == nid) return pa
	}
	return null
    }

    fun findSelected(): Path? {
	val it: Iterator<*> = li.iterator()
	while (it.hasNext()) {
	    val pa = it.next() as Path
	    if (pa.isSelected()) return pa
	}
	return null
    }

    fun redraw(g2: Graphics2D?) {
	val it: Iterator<*> = li.iterator()
	while (it.hasNext()) {
	    val pa = it.next() as Path
	    pa.draw(g2!!)
	}
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
	    val it: Iterator<*> = li.iterator()
	    while (it.hasNext()) {
		val pa = it.next() as Path
		val pel = pa.element
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

    companion object {
	@JvmStatic
	fun main(argv: Array<String>) {
	    val flag: HashMap<String, String> = flagAsMap(argv)
	    val argl = argAsList(argv)
	    val f = JFrame("Path - test")
	    val c = f.contentPane
	    val ap = AllPath()
	    val ca = ap.P_Canvas()
	    if (flag["g"] != null);
	    c.add(ca)
	    f.pack()
	    f.setSize(870, 640)
	    f.isVisible = true
	    ca.setBackground("bg.jpg")
	    m_sleep(300)
	    val sp: Point2D = Point2D.Double(100.0, 100.0)
	    val ep: Point2D = Point2D.Double(200.0, 300.0)
	    val pa = Path(0, sp, ep)
	    ap.add(pa)
	    pa.draw((ca.graphics as Graphics2D))
	    pa.extendSegment(Point2D.Double(300.0, 100.0))
	    pa.draw((ca.graphics as Graphics2D))
	    val sp2: Point2D = Point2D.Double(300.0, 200.0)
	    val ep2: Point2D = Point2D.Double(400.0, 500.0)
	    val pa2 = Path(1, sp2, ep2)
	    ap.add(pa2)
	    pa2.draw((ca.graphics as Graphics2D))
	    pa2.extendSegment(Point2D.Double(350.0, 300.0))
	    pa2.draw((ca.graphics as Graphics2D))

//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(0.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(10.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(100.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(100.2));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(pa.getLength()));
	    val fpa = pa

	    class Mouse : MouseInputAdapter() {
		var press_p: Point2D? = null
		var selected_prb: Probe? = null

		init {
		    ca.addMouseListener(this)
		    ca.addMouseMotionListener(this)
		}

		override fun mousePressed(e: MouseEvent) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "m p " + e);
		    if (e.x < 20 && e.y < 20) {
			if (selected_prb != null) selected_prb!!.seg!!.path!!.removeSegment()
			ca.repaint()
			return
		    }
		    if (e.x < 20 && e.y < 40) {
			selected_prb!!.seg!!.path!!.splitSegment()
			ca.repaint()
			return
		    }
		    if (e.x < 20 && e.y < 60) {
			val npa = selected_prb!!.seg!!.path!!.createSegment()
			ap.add(npa)
			ca.repaint()
			return
		    }
		    if (e.x < 20 && e.y < 80) {
			selected_prb!!.seg!!.path!!.extendSegment(press_p!!)
			ca.repaint()
			return
		    }
		    press_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
		    ap.deselectAll(ca.graphics as Graphics2D)
		    val prb = ap.findNearest(press_p)
		    prb!!.seg!!.path!!.selected = true
		    prb.seg!!.selectedPoint = prb.sel
		    prb.seg!!.path!!.draw((ca.graphics as Graphics2D))
		    selected_prb = prb
		}

		override fun mouseMoved(e: MouseEvent) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "m m " + e);
		}

		override fun mouseDragged(e: MouseEvent) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "m d " + e);
		    val drag_p: Point2D = Point2D.Double(e.x.toDouble(), e.y.toDouble())
		    selected_prb!!.seg!!.moveto(selected_prb!!.sel, drag_p)
		    selected_prb!!.seg!!.path!!.rebuildGP()
		    ca.repaint() // ap.redraw((Graphics2D)fca.getGraphics());
		}
	    }

	    val m = Mouse()
	    m_sleep(200)
	    while (true) {
		m_sleep(200)
	    }
	}
    }
}
