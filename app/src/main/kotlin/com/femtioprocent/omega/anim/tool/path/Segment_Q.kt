package com.femtioprocent.omega.anim.tool.path

import com.femtioprocent.omega.util.Log
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.*

class Segment_Q {
    var nid: Int
    var path: Path? = null
    var q: QuadCurve2D
    var p1: Point2D
    var p2: Point2D
    var pc: Point2D
    var selectedPoint = -1
    var showHandle = true

    constructor(nid: Int, sp: Point2D, cp: Point2D, ep: Point2D) {
	p1 = sp
	pc = cp
	p2 = ep
	q = QuadCurve2D.Float()
	q.setCurve(p1, pc, p2)
	this.nid = nid
    }

    constructor(nid: Int, q: QuadCurve2D) {
	this.q = q
	p1 = q.p1
	pc = q.ctrlPt
	p2 = q.p2
	this.nid = nid
    }

    fun adjust(p: Point2D) {
	p1 = p
	q.setCurve(p1, pc, p2)
    }

    fun toString(q: QuadCurve2D): String {
	return "{" + q.p1 + ',' + q.ctrlPt + ',' + q.p2 + "}"
    }

    fun split(): Segment_Q {
	val q1: QuadCurve2D = QuadCurve2D.Double()
	q.subdivide(q, q1)
	p1 = q.p1
	pc = q.ctrlPt
	p2 = q.p2
	return Segment_Q(0, q1)
    }

    fun moveto(who: Int, p: Point2D) {
	when (who) {
	    SEL_START -> p1.setLocation(p.x, p.y)
	    SEL_END -> p2.setLocation(p.x, p.y)
	    SEL_CTRL -> pc.setLocation(p.x, p.y)
	    else -> Log.getLogger().info(":--: active_nr $who")
	}
	q.setCurve(p1, pc, p2)
    }

    fun getPoint(sel: Int): Point2D? {
	return when (sel) {
	    SEL_START -> p1
	    SEL_END -> p2
	    SEL_CTRL -> pc
	    else -> null
	}
    }

    fun setPoint(sel: Int, p: Point2D) {
	when (sel) {
	    SEL_START -> p1 = p
	    SEL_END -> p2 = p
	    SEL_CTRL -> pc = p
	    else -> return
	}
	q.setCurve(p1, pc, p2)
    }

    fun moveAllBy(p: Point2D) {
	p1.setLocation(
		p1.x + p.x,
		p1.y + p.y
	)
	p2.setLocation(
		p2.x + p.x,
		p2.y + p.y
	)
	pc.setLocation(
		pc.x + p.x,
		pc.y + p.y
	)
	q.setCurve(p1, pc, p2)
    }

    fun moveAllBy(dx: Double, dy: Double) {
	p1.setLocation(p1.x + dx, p1.y + dy)
	p2.setLocation(p2.x + dx, p2.y + dy)
	pc.setLocation(pc.x + dx, pc.y + dy)
	q.setCurve(p1, pc, p2)
    }

    fun addMe(gp: GeneralPath) {
	gp.append(q, true)
    }

    fun findNearest(p: Point2D?): Probe {
	val prb = Probe()
	prb.dist = 2000000000.0
	for (i in 0 until SEL_N) {
	    val pp = getPoint(i)
	    val d = pp!!.distance(p)
	    if (d < prb.dist) {
		prb.dist = d
		prb.sel = i
		prb.p = pp
		prb.seg = this
	    }
	}
	return prb
    }

    fun drawSmallBox(g2: Graphics2D, p: Point2D?, w: Int) {
	g2.draw(
		Rectangle2D.Double(
			p!!.x - w / 2.0,
			p.y - w / 2.0,
			w.toDouble(), w.toDouble()
		)
	)
    }

    fun drawSmallCross(g2: Graphics2D, p: Point2D?, w: Int) {
	g2.draw(
		Line2D.Double(
			p!!.x - w / 2.0,
			p.y - w / 2.0,
			p.x + w / 2.0,
			p.y + w / 2.0
		)
	)
	g2.draw(
		Line2D.Double(
			p.x + w / 2.0,
			p.y - w / 2.0,
			p.x - w / 2.0,
			p.y + w / 2.0
		)
	)
    }

    fun fillSmallBox(g2: Graphics2D, p: Point2D?, w: Int) {
	g2.fill(
		Rectangle2D.Double(
			p!!.x - w / 2.0,
			p.y - w / 2.0,
			w.toDouble(), w.toDouble()
		)
	)
    }

    fun drawConnector(g2: Graphics2D) {
	g2.draw(Line2D.Double(p1, pc))
	g2.draw(Line2D.Double(pc, p2))
    }

    fun draw(g2: Graphics2D) {
	g2.draw(q)
	if (showHandle) {
	    for (i in 0 until SEL_N) {
		val p = getPoint(i)
		g2.color = if (i == selectedPoint) Color.red else Color.green
		if (i == SEL_CTRL) drawSmallCross(g2, p, 5) else drawSmallBox(g2, p, 5)
	    }
	    if (this === path!!.getSegment(0)) {
		g2.color = if (SEL_START == selectedPoint) Color.red else Color.green
		fillSmallBox(g2, getPoint(SEL_START), 5)
	    }
	}
    }

    override fun toString(): String {
	return "Segment_Q{" +
		nid +
		"}"
    }

    companion object {
	const val SEL_START = 0
	const val SEL_END = 1
	const val SEL_CTRL = 2
	const val SEL_N = 3
    }
}
