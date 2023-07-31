package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.anim.tool.timeline.TimeMarker
import java.awt.Graphics

object TimeMarkerDraw {
    fun draw(g: Graphics, tm: TimeMarker, x: Int, y: Int, w: Int) {
	when (tm.type) {
	    TimeMarker.BEGIN -> {
		g.drawLine(x, y, x + 3, y - 5)
		g.drawLine(x, y, x + 3, y + 5)
	    }

	    TimeMarker.END -> {
		g.drawLine(x, y, x - 3, y - 5)
		g.drawLine(x, y, x - 3, y + 5)
	    }

	    TimeMarker.START -> {
		g.drawLine(x, y - 5, x, y + 5)
		g.drawLine(x, y - 5, x + 3, y - 5)
		g.drawLine(x, y + 5, x + 3, y + 5)
	    }

	    TimeMarker.STOP -> {
		g.drawLine(x, y - 5, x, y + 5)
		g.drawLine(x, y - 5, x - 3, y - 5)
		g.drawLine(x, y + 5, x - 3, y + 5)
	    }

	    TimeMarker.TSYNC -> if (tm.duration == 0) {
		g.drawLine(x - 3, y + 5, x, y - 5)
		g.drawLine(x + 3, y + 5, x, y - 5)
	    } else {
		g.drawLine(x - 3, y + 5, x, y - 5)
		g.drawLine(x + 3, y + 5, x, y - 5)
		g.drawLine(x, y - 5, x + w, y - 5)
		g.drawLine(x + w, y - 5, x + w, y)
	    }

	    TimeMarker.TRIGGER -> {
		g.drawLine(x, y - 5, x, y + 5)
		g.drawLine(x - 3, y - 5, x + 3, y - 5)
		g.drawLine(x - 3, y + 5, x + 3, y + 5)
	    }

	    TimeMarker.TIMELINE -> {
		g.drawLine(x, y - 5, x, y + 5)
		g.drawLine(x - 3, y - 5, x + 3, y - 5)
	    }

	    else -> g.drawLine(x, y - 5, x, y + 5)
	}
    }
}
