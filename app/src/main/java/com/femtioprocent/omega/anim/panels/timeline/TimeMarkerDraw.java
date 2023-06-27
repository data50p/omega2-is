package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.anim.tool.timeline.TimeMarker;

import java.awt.*;

public class TimeMarkerDraw {
    static void draw(Graphics g, TimeMarker tm, int x, int y, int w) {
        switch (tm.type) {
            case TimeMarker.BEGIN:
                g.drawLine(x, y, x + 3, y - 5);
                g.drawLine(x, y, x + 3, y + 5);
                break;
            case TimeMarker.END:
                g.drawLine(x, y, x - 3, y - 5);
                g.drawLine(x, y, x - 3, y + 5);
                break;
            case TimeMarker.START:
                g.drawLine(x, y - 5, x, y + 5);
                g.drawLine(x, y - 5, x + 3, y - 5);
                g.drawLine(x, y + 5, x + 3, y + 5);
                break;
            case TimeMarker.STOP:
                g.drawLine(x, y - 5, x, y + 5);
                g.drawLine(x, y - 5, x - 3, y - 5);
                g.drawLine(x, y + 5, x - 3, y + 5);
                break;
            case TimeMarker.TSYNC:
                if (tm.duration == 0) {
                    g.drawLine(x - 3, y + 5, x, y - 5);
                    g.drawLine(x + 3, y + 5, x, y - 5);
                } else {
                    g.drawLine(x - 3, y + 5, x, y - 5);
                    g.drawLine(x + 3, y + 5, x, y - 5);
                    g.drawLine(x, y - 5, x + w, y - 5);
                    g.drawLine(x + w, y - 5, x + w, y);
                }
                break;
            case TimeMarker.TRIGGER:
                g.drawLine(x, y - 5, x, y + 5);
                g.drawLine(x - 3, y - 5, x + 3, y - 5);
                g.drawLine(x - 3, y + 5, x + 3, y + 5);
                break;
            case TimeMarker.TIMELINE:
                g.drawLine(x, y - 5, x, y + 5);
                g.drawLine(x - 3, y - 5, x + 3, y - 5);
                break;
            default:
                g.drawLine(x, y - 5, x, y + 5);
                break;
        }
    }
}

