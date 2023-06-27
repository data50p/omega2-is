package com.femtioprocent.omega.anim.tool.timeline;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TimeLinePlayer implements ActionListener {
    final int NULL = 0;
    final int INIT_PLAY = 1;
    final int PLAY = 2;
    final int STOP_REQ = 3;
    final int STOPPED = 4;

    private EventListenerList playctrl_listeners;

    int tick_start;
    long ct0;
    double speed = 1.0;

    boolean MODE = OmegaConfig.RUN_MODE;

    int state = NULL;

    javax.swing.Timer timer;

    Object mb = new Object();

    boolean is_running;

    Runnable after;


    public TimeLinePlayer() {
        playctrl_listeners = new EventListenerList();
        timer = new javax.swing.Timer(OmegaConfig.t_step, this);
        timer.setCoalesce(true);
        self = this;
    }

    private static TimeLinePlayer self = null;

    public static TimeLinePlayer getDefaultTimeLinePlayer() {
        return self;
    }

    public void addPlayCtrlListener(PlayCtrlListener pcl) {
        playctrl_listeners.add(PlayCtrlListener.class, pcl);
    }

    private void callPlay_beginPlay(boolean dry) {
        Object[] lia = playctrl_listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((PlayCtrlListener) lia[i + 1]).beginPlay(dry);
        }
    }

    int cnt = 0;
    long last = 0;

    private boolean callPlay_playAt(int t) {
        long ct0 = System.currentTimeMillis();
        long a = ct0 - last;
//	Log.getLogger().info("P " + a + ' ' + t);
        if (last > 0 && a > 300)
            System.err.println("playAt: long time " + a + ' ' + t);
        last = ct0;
        boolean b = false;
        Object[] lia = playctrl_listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            b |= ((PlayCtrlListener) lia[i + 1]).playAt(t);
        }
        return b;
    }

    private void callPlay_endPlay() {
        Object[] lia = playctrl_listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((PlayCtrlListener) lia[i + 1]).endPlay();
        }
    }

    void firePropertyChange(String s) {
        Object[] lia = playctrl_listeners.getListenerList();
        for (int i = 0; i < lia.length; i += 2) {
            ((PlayCtrlListener) lia[i + 1]).propertyChanged(s);
        }
    }

    public void dry_play(Runnable after, int clicks) {
        this.after = after;
        state = INIT_PLAY;
        is_running = true;
        callPlay_beginPlay(true);
        state = PLAY;
        for (int i = 0; i < 30000; i += clicks)
            if (callPlay_playAt(i))
                break;
        callPlay_endPlay();
        state = STOP_REQ;
        if (after != null)
            after.run();
        after = null;
        is_running = false;
        state = STOPPED;
    }

    public boolean play(Runnable after) {
        this.after = after;
        state = INIT_PLAY;
        is_running = true;
        ct0 = SundryUtils.ct();
        timer.start();
        return true;
    }

    public boolean isRunning() {
        return is_running;
    }

    public void waitWhileRunning() {
        for (; ; ) {
            synchronized (mb) {
                try {
                    if (is_running == false)
                        return;
                    mb.wait();
                } catch (Exception ex) {
                }
            }
        }
    }

    public void stop() {
        state = STOP_REQ;
    }

    public boolean pause() {
        return false;
    }

    public void normalize() {
        int a = (int) (speed * 10 + 0.5);
        speed = a / 10.0;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean adjustSpeed(double factor) {
        if (factor > 1.0 && speed < 2.0) {
            speed += 0.1;
            normalize();
            firePropertyChange("speed");
            return true;
        }
        if (factor < 1.0 && speed > 0.5) {
            speed -= 0.1;
            normalize();
            firePropertyChange("speed");
            return true;
        }
        return false;
    }


    public void actionPerformed(ActionEvent ae) {
        if (state == INIT_PLAY) {
            callPlay_beginPlay(false);
            state = PLAY;
        }
        if (state == STOP_REQ) {
            callPlay_endPlay();
            state = STOPPED;
            is_running = false;
            synchronized (mb) {
                try {
                    mb.notify();
                } catch (Exception ex) {
                }
            }
            timer.stop();
            if (after != null)
                after.run();
            after = null;
        }
        long delta = SundryUtils.ct() - ct0;
        delta *= speed;
        if (callPlay_playAt((int) delta)) {
            return;
        }
    }
}


