package com.femtioprocent.omega.anim.tool.timeline;

public interface PlayListener extends java.util.EventListener {
    static final int FIRST = 1;
    static final int LAST = 2;

    public void actionAtTime(TimeLine[] tlA, int t, int attr, boolean dry);

    public void actionMarkerAtTime(TimeMarker tm, int t, boolean dry);
}
