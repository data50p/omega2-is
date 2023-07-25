package com.femtioprocent.omega.lesson.canvas

import java.util.*

interface LessonCanvasListener : EventListener {
    fun hitTarget(ix: Int, type: Char)
    fun hitItem(ix: Int, iy: Int, where: Int, type: Char)
}
