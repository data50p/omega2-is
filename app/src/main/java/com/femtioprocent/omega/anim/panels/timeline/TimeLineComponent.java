package com.femtioprocent.omega.anim.panels.timeline;

import com.femtioprocent.omega.anim.tool.timeline.PlayCtrlListener;

import javax.swing.*;
import java.awt.*;

public class TimeLineComponent extends JPanel implements PlayCtrlListener {
    TimeLinePanel tlp;
    TimeLineStatusPanel tlsp;
    TimeLineControlPanel tlcp;

    public TimeLineComponent(TimeLinePanel tlp) {
        this.tlp = tlp;
        tlsp = new TimeLineStatusPanel(tlp);
        tlcp = new TimeLineControlPanel(tlp);
        setLayout(new BorderLayout());
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(tlp, BorderLayout.CENTER);
        p.add(tlsp, BorderLayout.SOUTH);
        add(p, BorderLayout.CENTER);
        add(tlcp, BorderLayout.WEST);
    }

    // --------- interface PlayCtrlListener

    public void beginPlay(boolean dry) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera begin");
    }

    public boolean playAt(int lt, int t) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera " + lt + ' ' + t);
        return false;
    }

    public boolean playAt(int t) {
        tlsp.updateValues();
        tlp.setTick(t);
        return false;
    }

    public void endPlay() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera end");
    }

    public void propertyChanged(String s) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera prop " + s);
    }
} 
