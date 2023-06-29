package com.femtioprocent.omega.media.audio;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.media.audio.impl.FxPlayer;
import com.femtioprocent.omega.media.audio.impl.JPlayer;
import com.femtioprocent.omega.util.ListFilesURL;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;

import java.io.File;
import java.util.HashMap;

public class APlayer {
    static HashMap dir_cache = new HashMap();

    String id;
    public String nname;

    static boolean T = true;

    static boolean alwaysFxPlayer = false;

    JPlayer jplayer;
    FxPlayer fxplayer;

    private APlayer() {
    }


    private APlayer(String nname, String id) {
        this.id = id;
        this.nname = nname;
    }

    static public APlayer createAPlayer(String name, String attr, String id) {
        APlayer apl = load_(null, name, attr, id);
        if (apl == null && attr != null)
            apl = load_(null, name, null, id);
        if (apl == null)
            return new APlayer();
        return apl;
    }

    static public APlayer createAPlayer(String lang, String name, String attr, String id) {
        APlayer apl = load_(lang, name, attr, id);
        if (apl == null && attr != null)
            apl = load_(lang, name, null, id);
        if (apl == null)
            return new APlayer();
        return apl;
    }

    private static String splice(String base, String attr) {
        if (attr != null && attr.length() > 0 && attr.charAt(0) == '-')
            attr = attr.substring(1);
        int ix = base.lastIndexOf('.');
        if (ix == -1)
            return base + '-' + attr;
        else
            return base.substring(0, ix) + '-' + attr + base.substring(ix);
    }

    static boolean isIn(String s, String sa[]) {
        if (sa == null)
            return false;
        for (int i = 0; i < sa.length; i++)
            if (s.equals(sa[i]))
                return true;
        return false;
    }

    public boolean isLoaded() {
        return nname != null;
    }

    static private APlayer load_(String lang, String name, String attr, String id) {
        if (name == null)
            return new APlayer();

        long ct0 = SundryUtils.ct();

        APlayer apl = null;
        try {
            int ix = name.lastIndexOf('/');
            String dir;
            String base;

            if (ix == -1) {
                dir = ".";
                base = name;
            } else {
                dir = name.substring(0, ix);
                base = name.substring(ix + 1);
            }

            OmegaContext.sout_log.getLogger().info("ERR: " + "audio: dir " + dir);
            OmegaContext.sout_log.getLogger().info("ERR: " + "audio: base " + base);


            String[] list = (String[]) dir_cache.get(dir);
            if (list == null) {
                try {
                    long ct0a = System.nanoTime();
                    list = ListFilesURL.getMediaList(dir);
                    long ct1a = System.nanoTime();
                    dir_cache.put(dir, list);
                    OmegaContext.sout_log.getLogger().info("ERR: " + "===== " + dir + ' ' + (ct1a - ct0a));
                } catch (Exception ex) {
                    OmegaContext.sout_log.getLogger().info("ERR: " + "Can't get file list " + lang + ' ' + name + ' ' + attr + ' ' + id);
                }
            }

            String nname = null;
            boolean isTL = id != null && id.startsWith("TL");

            if (attr != null && attr.length() > 0) {
                String nnname = splice(base, attr + (isTL ? OmegaContext.SPEED : ""));
                if (isIn(nnname, list))
                    nname = nnname;
                else {
                    nnname = splice(base, attr);
                    if (isIn(nnname, list))
                        nname = nnname;
                }
            }
            if (nname == null && isIn(isTL ? splice(base, OmegaContext.SPEED) : base, list))
                nname = isTL ? splice(base, OmegaContext.SPEED) : base;
            if (nname == null && isIn(base, list))
                nname = base;

            if (nname == null)
                return null;

            nname = dir + '/' + nname;

            String lang_id = "";
            String ffname = OmegaContext.getMediaFile(nname);
            if (ffname.indexOf("words-") != -1) {
                String alname = ffname.replaceAll("words\\-[a-zA-Z]*", "words-" + lang);
                File fffile = new File(alname);

                if (fffile.exists()) {
                    ffname = alname;
                    lang_id = ":" + lang;
                }
                OmegaContext.sout_log.getLogger().info("ERR: " + "NAME IS " + ffname);
            }


            String url_name = "file:" + ffname;

            try {
                apl = new APlayer(nname, id);
                ffname = maybeeTheMp3(ffname);
                if (ffname.endsWith(".mp3") || alwaysFxPlayer) {
                    apl.fxplayer = new FxPlayer(ffname);
                    OmegaContext.sout_log.getLogger().info("ERR: " + "FxPlayer created: " + nname + ' ' + apl.fxplayer);
                } else {
                    apl.jplayer = new JPlayer(ffname);
                    OmegaContext.sout_log.getLogger().info("ERR: " + "JPlayer created: " + nname + ' ' + apl.jplayer);
                }
            } catch (Exception ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "" + ex);
                ex.printStackTrace();
                return null;
            }
            return apl;
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "" + ex);
            ex.printStackTrace();
        } finally {
        }
        return null;
    }

    private static String maybeeTheMp3(String fn) {
        String fnMp3 = fn.replaceAll("\\.wav$", ".mp3");
        File fileMp3 = new File(fnMp3);
        File file = new File(fn);
        return fileMp3.exists() && !file.exists() ? fnMp3 : fn;
    }


    public void play() {
        if (fxplayer != null) {
            fxplayer.play(false);
            return;
        }
        if (jplayer != null) {
            jplayer.play();
            return;
        }
    }

    public void playWait() {
        try {
            if (fxplayer != null) {
                fxplayer.play(true);
                return;
            }
            if (jplayer != null) {
                jplayer.play();
                jplayer.waitAudio();
                return;
            }
        } catch (Exception ex) {
            Log.getLogger().info("while playWait audio " + fxplayer + ' ' + ex);
            ex.printStackTrace();
        }
    }

    public void stop() {
    }

    public void close() {
    }
}
