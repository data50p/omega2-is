package com.femtioprocent.omega.anim.tool.path

import java.awt.geom.Point2D

class Probe {
    @JvmField
    var seg: Segment_Q? = null
    @JvmField
    var pa: Path? = null
    var p: Point2D? = null
    @JvmField
    var sel = 0
    @JvmField
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
