package com.femtioprocent.omega;

import com.femtioprocent.omega.subsystem.Subsystem;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;


public class OmegaContext {
    public static final String OMEGA_ASSETS_SUFFIX = ".omega_assets";
    public static final String defaultOmegaAssets = "default" + OMEGA_ASSETS_SUFFIX;
    public static final String developerOmegaAssets = "developer" + OMEGA_ASSETS_SUFFIX;

    private static String currentOmegaAssets = getDefaultOmegaAssets();

    static Object lock = new Object();
    static HashMap<String, Subsystem> subsystems = new HashMap<String, Subsystem>();
    public static String URL_BASE = "http://localhost:8089/";
    public static String URL_BASE_AS_FILE = "";

    public static boolean logon = !false;

    public static Log def_log = new Log();
    public static Log sout_log = def_log;
    public static Log exc_log = def_log;
    public static Log story_log = def_log;
    public static Log lesson_log = def_log;
    public static Log audio_log = def_log;

    private static String lesson_lang = T.lang;
    private static String lesson_lang_editor = T.lang;

    public static String SPEED = "";

    public static boolean CACHE_FEW = false;

    public static Color COLOR_WARP = new Color(0xe5, 0xe5, 0xe5); // transfer color from anim panel to mpg panel
    public static Color COLOR_TEXT_WARP = new Color(0, 0, 0); // transfer color from anim panel to mpg panel

    public static boolean extern_help_browser = true;

    public static Map variables = null;

    public static boolean DEMO = false;
    public static String omega_lang = null;
    public static String small = null;

    /**
     * Get the full path for current omega assets
     *
     * @param path
     * @return
     */
    public static String omegaAssets(String path) {
        if (path == null) {
            return null;
        }

        boolean noAssets = path != null && path.contains("toolbarButtonGraphics") || path.startsWith("register/");

        if (path != null && path.startsWith(currentOmegaAssets)) {
            //sout_log.getLogger().warning("currentOmegaAssets(): Already omega_assets: " + path);
            if (noAssets)
                return antiOmegaAssets(path);
            return path;
        }
        if (noAssets) {
            sout_log.getLogger().warning("currentOmegaAssets(): noAssets omega_assets: " + path);
            return path;
        }
        if (".".equals(path)) {
            sout_log.getLogger().warning("currentOmegaAssets(): .: " + currentOmegaAssets);
            return currentOmegaAssets;
        }
        if (path.startsWith("/")) {
            sout_log.getLogger().warning("currentOmegaAssets(): /: " + currentOmegaAssets);
            return path;
        }
        //sout_log.getLogger().warning("currentOmegaAssets():+: " + currentOmegaAssets + '/' + path);
        return currentOmegaAssets + '/' + path;
    }

    public static String antiOmegaAssets(String afn) {
        if (afn == null || afn.length() == 0)
            return afn;
        if (afn.startsWith(omegaAssets(""))) {
            return afn.substring(omegaAssets("").length());
        }
        if (afn.startsWith("./" + omegaAssets(""))) {
            return afn.substring(("./" + omegaAssets("")).length());
        }
        return afn;
    }

    public static String[] antiOmegaAssets(String[] afns) {
        if (afns == null || afns.length == 0)
            return afns;

        String[] asa = new String[afns.length];
        int ix = 0;
        for (String s : afns) {
            asa[ix++] = antiOmegaAssets(s);
        }
        return asa;
    }

    public static String omegaAssetsName() {
        return currentOmegaAssets;
    }

    /**
     * Set the from now on choosen omega assets
     *
     * @param omega_assets_name null value restores default
     */
    public static void setOmegaAssets(String omega_assets_name) throws IllegalArgumentException {
        if (SundryUtils.empty(omega_assets_name)) {
            currentOmegaAssets = getDefaultOmegaAssets();
            OmegaContext.sout_log.getLogger().info("setOmegaAssets: " + currentOmegaAssets);
        } else {
            if (!omega_assets_name.endsWith(OMEGA_ASSETS_SUFFIX))
                omega_assets_name = omega_assets_name + OMEGA_ASSETS_SUFFIX;
            if ((new File(omega_assets_name)).exists()) {
                OmegaContext.sout_log.getLogger().info("setOmegaAssets: " + currentOmegaAssets + " -> " + omega_assets_name);
                currentOmegaAssets = omega_assets_name;
                return;
            }
            OmegaContext.sout_log.getLogger().info("setOmegaAssets: unable to set omega assets, keep old! " + currentOmegaAssets);
        }
    }

    private static String getDefaultOmegaAssets() {
        return false && isDeveloper() ? developerOmegaAssets : defaultOmegaAssets;
    }

    public static String getMediaFile(String name) {
        return omegaAssets("media/" + name);
    }

    public static String t9n(String s) {
        if (s == null)
            return null;
        if (s.startsWith("t9n/"))
            return s;
        return "t9n/" + s;
    }

    public static boolean omegaAssetsExist(String fn) {
        String of = omegaAssets(fn);
        File f = new File(of);
        return f.exists() && f.canRead();
    }

    public static String media() {
        return "media/";
    }

    public static class HelpStack {
        Stack<String> stack = new Stack<String>();


        public void push(String s) {
            stack.push(s);
            //	    OmegaContext.sout_log.getLogger().info("ERR: " + "H push " + stack);
        }

        public String get() {
            if (stack.empty())
                return null;
            //	    OmegaContext.sout_log.getLogger().info("ERR: " + "H get " + (String)stack.peek());
            return stack.peek();
        }

        public void pop(String s) {
            if (s == null)
                return;
            if (s.length() == 0 || s.equals(get()))
                if (!stack.empty())
                    stack.pop();
            //	    OmegaContext.sout_log.getLogger().info("ERR: " + "H pop " + stack);
        }
    }

    ;
    static public HelpStack HELP_STACK = new HelpStack();

    public static void setLogon(boolean b) {
        b |= OmegaContext.isDeveloper();
        Log.getLogger().setLevel(b ? Level.ALL : Level.OFF);
    }

    public static String getLessonLang() {
        return lesson_lang;
    }

    public static void setLessonLang(String s) {
        OmegaContext.lesson_log.getLogger().info("old, new: " + lesson_lang + ' ' + s);
        lesson_lang = s;
    }

    public static boolean isDeveloper() {
        File f = new File("../.git");
        return !false && f.exists();
    }
//    public static String getEditorLessonLang() {
//	return lesson_lang_editor;
//    }
//
//    public static void setEditorLessonLang(String s) {
//        OmegaContext.lesson_log.getLogger().info("EditorLessonLang: old, new: " + lesson_lang_editor + ' ' + s);
//	lesson_lang_editor = s;
//    }

    public OmegaContext() {
    }

    public static void init(String s, Object arg) {
        synchronized (lock) {
            try {
                if (subsystems.get(s) != null)
                    return;
                Class<Subsystem> cl = (Class<Subsystem>) Class.forName("com.femtioprocent.omega.subsystem." + s);
                Subsystem ss = cl.getDeclaredConstructor().newInstance();
                ss.init(arg);
                subsystems.put(s, ss);
            } catch (Exception ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "Can't start subsystem " + s + '\n' + ex);
            }
        }
    }

    static public Subsystem getSubsystem(String s) {
        return subsystems.get(s);
    }

    public static boolean isMacOS() {
        String s = System.getProperty("os.name").toLowerCase();
        return  s.indexOf("mac") >= 0;
    }

    public static boolean isWINDOS() {
        String s = System.getProperty("os.name").toLowerCase();
        return  s.indexOf("win") >= 0;
    }

    public static boolean isLinux() {
        String s = System.getProperty("os.name").toLowerCase();
        return  s.indexOf("nux") >= 0;
    }
}

