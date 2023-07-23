package com.femtioprocent.omega.anim.panels.timeline

import java.util.*

interface TimeLinePanelListener : EventListener {
    fun updateValues()
    fun event(evs: String?, o: Any?)
}
