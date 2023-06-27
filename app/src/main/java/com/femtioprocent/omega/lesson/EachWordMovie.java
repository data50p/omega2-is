package com.femtioprocent.omega.lesson;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.media.video.MpgPlayer;
import com.femtioprocent.omega.media.video.VideoUtil;
import com.femtioprocent.omega.util.Log;

import javax.swing.*;
import java.awt.*;

//import omega.lesson.test.*;

public class EachWordMovie {
    public MpgPlayer mp;
    JComponent jcomp;

    int w, h;
    int vw_orig, vh_orig;

    public EachWordMovie() {
        super();
    }

    public JComponent prepare(String fName, JComponent jcomp) {
        if (jcomp == null || fName == null) {
            return null;
        }
        fName = OmegaContext.omegaAssets(fName);
        fName = VideoUtil.findSupportedFname(fName);
        if (fName == null)
            return null;

        if (mp == null) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "+++++++ prepare new: " + fName);
            mp = MpgPlayer.createMpgPlayer(fName, jcomp, jcomp.getWidth(), jcomp.getHeight());
            if (mp == null)
                return null;
            this.jcomp = jcomp;
            vw_orig = mp.getOrigW();
            vh_orig = mp.getOrigH();
        } else {
            OmegaContext.sout_log.getLogger().info("ERR: " + "+++++++ prepare again: " + fName);
            mp.setSize(1, 1);
            mp.setLocation(10, 10);
            mp.visual.setVisible(false);
        }
        return jcomp;
    }

    public void perform() {
        if (mp == null)
            return;
        mp.reset();

        if (jcomp != null) {
            w = jcomp.getWidth();
            h = jcomp.getHeight();
            OmegaContext.sout_log.getLogger().info("ERR: " + "" + vw_orig + ' ' + vh_orig + ' ' + w + ' ' + h);
            int ww = w / 5;
            mp.setSize(ww, (int) (ww * ((double) vh_orig / vw_orig)));
        }
        mp.visual.setVisible(true);
        mp.start();
    }

    public void perform(int x, int y, double scale) {
        if (mp == null)
            return;
        mp.reset();

        w = jcomp.getWidth();
        h = jcomp.getHeight();
        int ww = (int) (w / scale);
        mp.setSize(ww, (int) (ww / mp.aspect));

        int xx = x - ww / 2;
        if (xx + ww > w) {
            xx = w - ww - 1;
        }
        if (xx < 1) {
            xx = 1;
        }
        mp.setLocation(xx, y);
        mp.visual.setVisible(true);

        OmegaContext.sout_log.getLogger().info("ERR: " + "+++++++ perform movie: " + vw_orig + ' ' + vh_orig + ' ' + w + ' ' + h + ' ' + ww);

        mp.start();
    }

    public Rectangle getMovieRectangle() {
        Rectangle r = new Rectangle(mp.getX(), mp.getY(), mp.getW(), mp.getH());
        Log.getLogger().info("Movie rect = " + r);
        return r;
    }

    public void waitEnd() {
        if (mp == null)
            return;
        mp.wait4();
        mp.visual.setVisible(false);
    }

    public void dispose() {
        if (mp == null)
            return;
        mp.dispose(jcomp);
        jcomp = null;
        mp = null;
        OmegaContext.sout_log.getLogger().info("ERR: " + "+++++ disposed");
    }

    public void reset() {
        mp.reset();
    }
}
