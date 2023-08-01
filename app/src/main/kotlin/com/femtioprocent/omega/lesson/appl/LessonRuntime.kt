package com.femtioprocent.omega.lesson.appl

import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.util.Log
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JPanel

class LessonRuntime(title: String?, fn: String?, with_frame: Boolean, run_mode: Char) : ApplLesson(title, false) {
    init {
	val wi: Window
	val f: JFrame = this
	wi = f
	ApplContext.top_frame = this
	f.addWindowListener(object : WindowAdapter() {
	    override fun windowClosing(ev: WindowEvent) {
		maybeClose()
	    }
	})
	val le = Lesson(run_mode)
	le.runLessons(
		wi,
		f.contentPane as JPanel,
		fn,
		false,
		with_frame
	)
    }

    private fun maybeClose() {
	Log.getLogger().info(
		"""LessonRuntime want to close ${ApplContext.top_frame === this} ${ApplContext.top_frame}
$this"""
	)
	if (ApplContext.top_frame === this) System.exit(0)
    }
}
