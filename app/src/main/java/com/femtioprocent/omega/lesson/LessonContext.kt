package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.lesson.canvas.LessonCanvas
import com.femtioprocent.omega.lesson.machine.Target

class LessonContext internal constructor(var lesson: Lesson) {

    val lessonCanvas: LessonCanvas
	get() = Lesson.le_canvas!!
    val target: Target?
	get() = lesson.machine.target
}
