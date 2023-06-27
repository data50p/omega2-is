package com.femtioprocent.omega.lesson;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.media.video.MpgPlayer;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Random;

//import omega.lesson.test.*;

public class FeedBackMovie extends FeedBack {
    boolean signMode = false;

    public FeedBackMovie() {
        super();
    }

    public FeedBackMovie(boolean signMode) {
        super();
        this.signMode = signMode;
    }

    public JPanel prepare(String rsrs, JPanel canvas) {
        OmegaContext.lesson_log.getLogger().info(": " + "+++++++ prepare " + rsrs);
        if (rsrs == null) {
            return null;
        }
        rsrs = random(rsrs);
        OmegaContext.lesson_log.getLogger().info(": " + "+++++++ prepare random " + rsrs);
        if (canvas == null)
            my_own = canvas = new JPanel();
        if (mp == null) {
            OmegaContext.lesson_log.getLogger().info(": " + "+++++++ prepare new  " + rsrs);
            mp = MpgPlayer.createMpgPlayer(rsrs, canvas);
            this.canvas = canvas;
            my_own = null;
            vw = mp.vw;
            vh = mp.vh;
        } else {
        }
//	mp.setLocation(100, 100);
//	mp.setSize(5, 5);
        mp.visual.setVisible(false);
        return canvas;
    }

    @Deprecated
    public JComponent prepareAlt(String rsrs, JComponent canvas) {
        OmegaContext.lesson_log.getLogger().info(": " + "+++++++ prepare " + rsrs);
        if (rsrs == null) {
            return null;
        }
        rsrs = random(rsrs);
        OmegaContext.lesson_log.getLogger().info(": " + "+++++++ prepare random " + rsrs);
        if (canvas == null)
            return null;
        if (mp == null) {
            OmegaContext.lesson_log.getLogger().info(": " + "+++++++ prepare new  " + rsrs);
            mp = MpgPlayer.createMpgPlayer(rsrs, canvas);
            this.comp = canvas;
            my_own = null;
            vw = mp.vw;
            vh = mp.vh;
        } else {
        }
//	mp.setLocation(100, 100);
//	mp.setSize(5, 5);
        mp.visual.setVisible(false);
        return canvas;
    }

    static int fix = 0;

    String random(String rsrs) {
        String currentPath = null;
        try {
            currentPath = new File(".").getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Current dir:" + currentPath);

        File f = new File(OmegaContext.omegaAssets(rsrs));
        OmegaContext.lesson_log.getLogger().info(": " + "+++++++ random  " + f + ' ' + f.exists() + ' ' + f.isDirectory());
        if (f.exists() && f.isDirectory()) {
            return randomDir(rsrs);
        }
//	return rsrs;
        String path = ".";
        int ix = rsrs.lastIndexOf("/");
        if (ix != -1)
            path = rsrs.substring(0, ix);
        File mpg_file = new File(rsrs);
        File mpg_dir = mpg_file.getParentFile();
        File[] other = mpg_dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                String name = f.getName();
                if (name.endsWith(".mpg"))
                    return true;
                return false;
            }
        });
        fix++;
        rsrs = path + "/" + other[fix % other.length].getName();
        OmegaContext.sout_log.getLogger().info(": " + "fb " + rsrs + " -> " + rsrs + ' ' + other.length);
        return rsrs;
    }

    String randomDir(String rsrs) {
        File mpg_dir = new File(OmegaContext.omegaAssets(rsrs));
        File[] other = mpg_dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
                String name = f.getName();
                if (name.endsWith(".mpg"))
                    return true;
                return false;
            }
        });
        if (other.length == 0)
            return null;

        Random r = new Random();
        int rr = r.nextInt(1000000000);
        if (rr < 0)
            rr = -rr;
        rsrs = rsrs + "" + other[rr % other.length].getName();
        OmegaContext.sout_log.getLogger().info(": " + "fb " + rsrs + " -> " + rsrs + ' ' + other.length);
        return rsrs;
    }

    public void perform() {
        if (mp == null)
            return;
        mp.reset();
        if (canvas != null) {
            w = canvas.getWidth();
            h = canvas.getHeight();
            OmegaContext.sout_log.getLogger().info(": " + "" + vw + ' ' + vh + ' ' + w + ' ' + h);
            mp.setSize(w / 2, w * vh / (2 * vw));
        }
        if (comp != null) {
            w = comp.getWidth();
            h = comp.getHeight();
            OmegaContext.sout_log.getLogger().info(": " + "" + vw + ' ' + vh + ' ' + w + ' ' + h);
            int ww = w / 5;
            mp.setSize(ww, (int) (ww * ((double) vh / vw)));
        }
        mp.visual.setVisible(true);
        mp.start();
    }

    public void perform(int x, int y) {
        if (mp == null)
            return;
        mp.reset();
        if (canvas != null) {
            w = canvas.getWidth();
            h = canvas.getHeight();
            OmegaContext.sout_log.getLogger().info(": " + "" + vw + ' ' + vh + ' ' + w + ' ' + h);
            mp.setSize(w / 2, w * vh / (2 * vw));
        }
        if (comp != null) {
            w = comp.getWidth();
            h = comp.getHeight();
            OmegaContext.sout_log.getLogger().info(": " + "" + vw + ' ' + vh + ' ' + w + ' ' + h);
            int ww = w / 5;
            mp.setSize(ww, (int) (ww * ((double) vh / vw)));
        }
        mp.setLocation(x, y);
        mp.visual.setVisible(true);
        mp.start();
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
        mp.dispose(canvas);
        if (my_own == canvas)
            canvas = my_own = null;
        mp = null;
        OmegaContext.sout_log.getLogger().info(": " + "+++++ disposed");
    }
}
