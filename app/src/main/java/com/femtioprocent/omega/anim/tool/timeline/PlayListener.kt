package com.femtioprocent.omega.anim.tool.timeline

import java.util.*

interface PlayListener : EventListener {
    fun actionAtTime(tlA: Array<TimeLine?>?, t: Int, attr: Int, dry: Boolean)
    fun actionMarkerAtTime(tm: TimeMarker?, t: Int, dry: Boolean)

    companion object {
	const val FIRST = 1
	const val LAST = 2
    }
}
