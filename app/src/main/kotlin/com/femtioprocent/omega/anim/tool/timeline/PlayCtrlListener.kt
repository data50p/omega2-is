package com.femtioprocent.omega.anim.tool.timeline

import java.util.*

interface PlayCtrlListener : EventListener {
    fun beginPlay(dry: Boolean)
    fun playAt(t: Int): Boolean
    fun endPlay()
    fun propertyChanged(s: String?)
}
