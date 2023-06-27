package com.femtioprocent.omega.lesson.canvas;

public interface LessonCanvasListener extends java.util.EventListener {
    void hitTarget(int ix, char type);

    void hitItem(int ix, int iy, int where, char type);
}
