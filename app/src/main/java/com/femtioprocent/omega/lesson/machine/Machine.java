package com.femtioprocent.omega.lesson.machine;

import com.femtioprocent.omega.lesson.LessonContext;

public class Machine {
    Target tg;
    LessonContext l_ctxt;

    public Machine(LessonContext l_ctxt) {
        this.l_ctxt = l_ctxt;
    }

    public Target getTarget() {
        return tg;
    }

    public void setTarget(Target tg) {
        this.tg = tg;
    }
}
