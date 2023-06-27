package com.femtioprocent.omega.media.audio.impl;

//åäö

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.appl.Settings;
import com.femtioprocent.omega.util.Log;

import javax.sound.sampled.*;
import java.io.File;

public class JPlayer implements LineListener {
    String fn;
    AudioInputStream ais;
    AudioFormat aformat;
    SourceDataLine sdataline;
    Object lock = new Object();

    boolean started = false;
    boolean eom = false;
    boolean opened = false;

    boolean done = false;

    static int wah = 2;
    static boolean silent = !true;
    static boolean o = !false;
    static int N = 4096 * 16;

    String realy_name;


    private static void s_pe(String s) {
        if (o)
            System.err.println(s);
    }

    private static void s_pe_(String s) {
        if (o)
            System.err.print(s);
    }

    static {
        String bs = Settings.getSettings().getString("audio-bufsize");
        if (bs != null) {
            int bs_i = 1024 * Integer.parseInt(bs);
            N = bs_i;
        }

        bs = Settings.getSettings().getString("audio-write-ahead");
        if (bs != null) {
            wah = Integer.parseInt(bs);
        }

        if (Settings.getSettings().getBoolean("audio-silent"))
            silent = true;
        else
            silent = false;

        if (Settings.getSettings().getBoolean("audio-debug"))
            o = true;
        else
            o = false;
        o = true;

        OmegaContext.sout_log.getLogger().info("STATIC: " + "" + Settings.getSettings().getSettingsHashMap());
        Log.getLogger().info("STATIC: " + "" + Settings.getSettings().getSettingsHashMap());
    }

    public JPlayer(String fn) {
        OmegaContext.sout_log.getLogger().info("INIT: " + "JPlayer: fn = " + fn);

        this.fn = fn;
        realy_name = fn;
        try {
            File file = new File(fn);
            ais = AudioSystem.getAudioInputStream(file);
            aformat = ais.getFormat();
            sdataline = getSourceDataLine(aformat);
            sdataline.addLineListener(this);

            OmegaContext.sout_log.getLogger().info("<init>: " + "JPlayer0: " + ais + ' ' + aformat + ' ' + sdataline);
        } catch (Exception ex) {
            ais = null;
            aformat = null;
            sdataline = null;
            done = true;
            OmegaContext.sout_log.getLogger().info("ERR: " + "JPlayer1: " + ex);
        }
    }

    public static SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        return (SourceDataLine) AudioSystem.getLine(info);
    }

    Object w_w = new Object();

    public void waitAudio() {
        synchronized (lock) {
            try {
                while (!done)
                    lock.wait(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    static final byte[] silent_buf = new byte[4096 * 4];

    public void play() {
        try {
            Thread th = new Thread(new Runnable() {
                public void run() {
                    try {
                        //Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 3);

                        sdataline.open(aformat);
                        waitOpen();
                        sdataline.start();

                        byte[] ba = new byte[N];
                        for (int i = 0; ; i++) {
                            int n = ais.read(ba);
                            if (n < 0)
                                break;
                            sdataline.write(ba, 0, n);
                            if (i < wah)
                                s_pe_("{w}");
                            else
                                s_pe_("{W}");

                            if (wah > 0 && i == wah) {
                                s_pe("-w-");
                                waitStart();
                                s_pe("-W-");
                            }
                        }
                        s_pe(">d>");
                        sdataline.drain();
                        done = true;
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                        if (silent) {
                            sdataline.write(silent_buf, 0, silent_buf.length);
                            s_pe(">D>");
                            sdataline.drain();
                        }

                        ais.close();
                        ais = null;

                        s_pe(">SundryUtil>");
                        sdataline.stop();
                        s_pe(">C>");
                        sdataline.close();
                    } catch (Exception ex) {
                        OmegaContext.sout_log.getLogger().info("ERR: " + "JPlayer2: " + ex);
                        ex.printStackTrace();
                    } finally {
                        //			    sdataline.removeLineListener(JPlayer.this);
                        sdataline = null;
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                }
            });
            th.start();
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "JPlayer3: " + ex);
        }
    }

    private void waitOpen() {
        synchronized (this) {
            while (!opened) {
                try {
                    wait(5000);
                    return;
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private void waitStart() {
        synchronized (this) {
            if (!started) {
                while (!started) {
                    try {
                        wait(5000);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }
    }

    private void waitEOM() {
        synchronized (this) {
            while (!eom) {
                try {
                    wait(5000);
                    return;
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    public synchronized void update(LineEvent le) {
        LineEvent.Type t = le.getType();

        if (t == LineEvent.Type.OPEN) {
            opened = true;
            s_pe("+O+");
        } else if (t == LineEvent.Type.START) {
            started = true;
            s_pe("+s+");
        } else if (t == LineEvent.Type.CLOSE) {
            eom = true;
            s_pe("+C+");
        } else if (t == LineEvent.Type.STOP) {
            s_pe("+SundryUtil+");
        }

        notifyAll();
    }
}
