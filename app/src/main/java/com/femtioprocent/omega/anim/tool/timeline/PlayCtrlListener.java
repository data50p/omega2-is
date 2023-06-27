package com.femtioprocent.omega.anim.tool.timeline;

import java.util.EventListener;

public interface PlayCtrlListener extends EventListener {
    public void beginPlay(boolean dry);

    public boolean playAt(int t);

    public void endPlay();

    public void propertyChanged(String s);
}
