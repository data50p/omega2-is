package com.femtioprocent.omega.media.audio.impl;

//åäö

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.MilliTimer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.swing.*;
import java.io.File;

public class FxPlayer {
    boolean done = false;
    MediaPlayer mediaPlayer = null;
    Object lock = new Object();

    final String realy_name;
    final String fname;

    public FxPlayer(String fn) {
        realy_name = fn;

        if (fn.endsWith(".wav")) {
            String fn3 = fn.replaceAll("\\.wav", ".mp3");
            File file3 = new File(fn3);
            File file2 = new File(fn);
            if (file3.exists() && !file2.exists()) {
                fname = fn3;
                OmegaContext.sout_log.getLogger().info(": " + "FxPlayer: fn -> " + fname + " (" + realy_name + ")");
            } else {
                fname = fn;
                OmegaContext.sout_log.getLogger().info(": " + "FxPlayer: fn => " + fname);
            }
        } else {
            fname = fn;
            OmegaContext.sout_log.getLogger().info(": " + "FxPlayer: fn => " + fname);
        }
    }

    public void play(boolean wait) {
        playFX(fname);
        if (wait) {
            synchronized (lock) {
                try {
                    while (!done)
                        lock.wait(200);
                } catch (InterruptedException ex) {
                }
                Log.getLogger().info("fxPlayed waited ... notified done");
            }
        }
    }

    void playFX(final String fn) {
        Log.getLogger().info("Enter playFX " + fn);
        doOnce();
        Platform.runLater(() -> {
            MilliTimer mt = new MilliTimer();
            File f = new File(fn);
            String bip = null;
            bip = f.toURI().toString();
            Log.getLogger().info("fxPrepare " + bip + ' ' + mt.getString());
            Media hit = new Media(bip);
            mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.setOnEndOfMedia(() -> {
                synchronized (lock) {
                    mediaPlayer.dispose();
                    Log.getLogger().info("fxPlayed eof" + ' ' + mt.getString());
                    done = true;
                    lock.notifyAll();
                }
            });
            Log.getLogger().info("fxPlay..." + ' ' + mt.getString());
            mediaPlayer.play();
        });
        Log.getLogger().info("Leave playFX " + fn);
    }

    private static boolean once = false;

    private void doOnce() {
        if (once)
            return;
        initFxFramework();
        once = true;
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }
    }

    static JFXPanel z = null;

    private synchronized static void initFxFramework() {
        if (z == null) {
            SwingUtilities.invokeLater(() -> {
                z = new JFXPanel(); // this will prepare JavaFX toolkit and environment
                Platform.setImplicitExit(false);
            });
        }
    }

    public String toString() {
        return "" + realy_name;
    }
}
