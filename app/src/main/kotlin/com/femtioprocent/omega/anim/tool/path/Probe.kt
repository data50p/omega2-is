package com.femtioprocent.omega.anim.tool.path

import java.awt.geom.Point2D

class Probe {
    var seg: Segment_Q? = null
    var pa: Path? = null
    var p: Point2D? = null
    var sel = 0
    var dist = 999999999.99
    override fun toString(): String {
	return "Probe" +
		"{seg=" + seg +
		", p=" + p +
		", pa=" + pa +
		", sel=" + sel +
		", dist=" + dist +
		"}"
    }
}
