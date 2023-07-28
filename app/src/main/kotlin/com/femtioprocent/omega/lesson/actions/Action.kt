package com.femtioprocent.omega.lesson.actions

import com.femtioprocent.omega.xml.Element
import java.awt.Window
import javax.swing.JPanel

interface Action {
    val canvas: JPanel?
    val pathList: String?
    val actorList: String?
    val hm: HashMap<String?, Any?>

    fun prefetch(action_s: String?): Element?
    fun perform(window: Window,
		action_s: String?,
		actA: Array<String?>,
		pathA: Array<String?>,
		ord: Int,
		hook: Runnable?)
    fun show()
    fun clearScreen()
    fun clean()
}
