package com.femtioprocent.omega.lesson.appl;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class LessonRuntime extends ApplLesson {

    public LessonRuntime(String title, String fn, boolean with_frame, char run_mode) {
        super(title, false);

        Window wi;
        JFrame f = this;
        wi = f;

        ApplContext.top_frame = this;

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                maybeClose();
            }
        });

        Lesson le = new Lesson(run_mode);
        le.runLessons(wi,
                (JPanel) (f.getContentPane()),
                fn,
                false,
                with_frame);
    }

    private void maybeClose() {
        OmegaContext.serr_log.getLogger().info("LessonRuntime want to close " + (ApplContext.top_frame == this) + ' ' + ApplContext.top_frame + '\n'+ this);
        if ( ApplContext.top_frame == this )
	    System.exit(0);
    }
}
