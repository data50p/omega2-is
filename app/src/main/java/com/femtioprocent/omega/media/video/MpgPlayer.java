package com.femtioprocent.omega.media.video;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MpgPlayer {
    public int vw;
    public int vh;

    public int vw_orig;
    public int vh_orig;

    public double aspect = 1.0;

    private boolean prefetch_done = false;
    private boolean ready = false;

    public Component visual;
    public FxMoviePlayer fxp = null;

    public MpgPlayer(Object player, String title) {
        if (false) {
            for (int i = 0; i < 100; i++)
                if (prefetch_done == false)
                    SundryUtils.m_sleep(100);
        }
    }

    public void reset() {
        if ( fxp != null ) {
            fxp.reset();
        }
        ready = false;
    }

    public void start() {
        if (fxp != null)
            fxp.play();
    }

    public void stop() {
        if (fxp != null)
            fxp.player.stop();
    }

    public void wait4() {
        if (fxp != null)
            fxp.wait4done();
//	if ( true ) {
//	    SundryUtils.m_sleep(4000);
//	    return;
//	}
//	while (ready == false)
//	    SundryUtils.m_sleep(200);
    }

    public void dispose(JComponent jcomp) {
        if (fxp != null)
            fxp.dispose();
        fxp = null;
        ready = true;
        visual = null;
        jcomp.removeAll();
    }

    public int getX() {
        return visual.getX();
    }

    public int getY() {
        return visual.getY();
    }

    public int getW() {
        return visual.getWidth();//vw;
    }

    public int getMediaH() {
        return fxp.mediaH;
    }

    public int getMediaW() {
        return fxp.mediaW;
    }

    public int getH() {
        return visual.getHeight(); //vh;
    }

    public int getOrigW() {
        return vw_orig;
    }

    public int getOrigH() {
        return vh_orig;
    }

    public void setSize(int w, int h) {
        vw = w;
        vh = h;
        visual.setSize(new Dimension(vw, vh));
        Log.getLogger().info("dep_set m size to: " + w + ' ' + h);
    }

    public void setLocation(int x, int y) {
        visual.setLocation(x, y);
        Log.getLogger().info("dep_set m loc at: " + x + ' ' + y);
    }

    static public MpgPlayer createMpgPlayer(String fn, JComponent jcomp) {
        return createMpgPlayer(fn, jcomp, 0, 0);
    }

    static public MpgPlayer createMpgPlayer(String fn, JComponent jcomp, int winW, int winH) {
        URL url = null;

        OmegaContext.lesson_log.getLogger().info("create mpgPlayer jcomp: " + fn);

        try {
            if (OmegaContext.omegaAssetsExist(fn)) {
                url = new URL("file:" + fn);

                try {
                    FxMoviePlayer fxp = new FxMoviePlayer(winW, winH);
                    JFXPanel fxPanel = fxp.initGUI(jcomp, fn);
                    MpgPlayer mp = new MpgPlayer(null, "null");
                    mp.visual = jcomp;
                    mp.fxp = fxp;
                    return mp;
                } catch (Exception e) {
                    e.printStackTrace();
                    OmegaContext.lesson_log.getLogger().info("NoPlayerEx: " + e);
                }
            }
        } catch (MalformedURLException e) {
            OmegaContext.lesson_log.getLogger().info("ERR: " + "MUE Error:" + e);
        } catch (Exception e) {
            OmegaContext.lesson_log.getLogger().info("ERR: " + "Exception:" + e);
        }
        return null;
    }
}
