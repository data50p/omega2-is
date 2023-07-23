package com.femtioprocent.omega.anim.panels.timeline

import java.util.*

internal interface TimeLinePanelListener : EventListener {
    fun updateValues()
    fun event(evs: String?, o: Any?)
}
