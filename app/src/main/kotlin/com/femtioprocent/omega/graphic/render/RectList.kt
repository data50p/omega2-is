package com.femtioprocent.omega.graphic.render

import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import java.awt.Rectangle
import java.awt.geom.Rectangle2D

class RectList internal constructor() {
    @JvmField
    var rl: List<Rectangle2D>
    var nr: Rectangle2D

    init {
	rl = ArrayList()
	nr = Rectangle2D.Double()
    }

    fun add(r: Rectangle2D) {
	var r = r
	val nrl: MutableList<Rectangle2D> = ArrayList()
	val it = rl.iterator()
	while (it.hasNext()) {
	    val r1 = it.next()
	    intersect(r, r1, nr)
	    if (r1.contains(r.x, r.y, r.width, r.height)) {
		return
	    }
	    if (!nr.isEmpty) {
		val rr = Rectangle()
		Rectangle2D.union(r, r1, rr)
		r = rr
	    } else {
		nrl.add(r1)
	    }
	}
	nrl.add(r.clone() as Rectangle2D)
	rl = nrl
    }

    operator fun iterator(): Iterator<*> {
	return rl.iterator()
    }

    companion object {
	fun mi(a: Double, b: Double): Double {
	    return if (a < b) a else b
	}

	fun ma(a: Double, b: Double): Double {
	    return if (a > b) a else b
	}

	@JvmStatic
	fun main(argv: Array<String>) {
	    val flag = flagAsMap(argv)
	    val argl = argAsList(argv)
	    var rl = RectList()
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(150.0, 150.0, 100.0, 100.0))
	    rl = RectList()
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(50.0, 50.0, 100.0, 100.0))
	    rl = RectList()
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(150.0, 150.0, 10.0, 10.0))
	    rl = RectList()
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(150.0, 50.0, 100.0, 100.0))
	    rl = RectList()
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(50.0, 150.0, 100.0, 100.0))
	    rl = RectList()
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(300.0, 300.0, 100.0, 100.0))
	    rl = RectList()
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(150.0, 300.0, 100.0, 100.0))
	    rl = RectList()
	    rl.add(Rectangle2D.Double(150.0, 300.0, 100.0, 100.0))
	    rl.add(Rectangle2D.Double(100.0, 100.0, 100.0, 100.0))
	}

	fun intersect(
	    src1: Rectangle2D,
	    src2: Rectangle2D,
	    dest: Rectangle2D
	) {
	    val x1 = Math.max(src1.minX, src2.minX)
	    val y1 = Math.max(src1.minY, src2.minY)
	    val x2 = Math.min(src1.maxX, src2.maxX)
	    val y2 = Math.min(src1.maxY, src2.maxY)
	    dest.setFrame(x1, y1, x2 - x1, y2 - y1)
	}
    }
}