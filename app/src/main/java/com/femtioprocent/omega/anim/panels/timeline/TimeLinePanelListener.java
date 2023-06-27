package com.femtioprocent.omega.anim.panels.timeline;

interface TimeLinePanelListener extends java.util.EventListener {
    public void updateValues();

    public void event(String evs, Object o);
}
