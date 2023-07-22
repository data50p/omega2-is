package com.femtioprocent.omega;

import java.io.File;

public class OmegaConfig {
    public static final String OMEGA_BUNDLE = "omega_bundle";
    public static final String OMEGA_BUNDLE_EXTENSION = "." + OMEGA_BUNDLE;
    public static final int SMALL_WIDTH = 1600;
    public static final int SMALL_HEIGHT = 1200;
    private static final double DEFAULT_FLATNESS = 1.5;
    public static double FLATNESS = DEFAULT_FLATNESS;
    static public int TIMELINES_N = 5;
    static public int CABARET_ACTOR_N = 12;
    static public int WINGS_N = 10;
    static public boolean T = !false;
    static public boolean RUN_MODE = false;
    static public int t_step = 10;

    static public int key_next_1 = 9;
    static public int key_next_2 = 9;
    static public int key_select_1 = ' ';
    static public int key_select_2 = '\r';
    static public int key_select_3 = '\n';

    static public boolean LIU_Mode = false;

    static public boolean isLIU_Mode() {
        OmegaContext.sout_log.getLogger().info("is LIU_Mode: " + LIU_Mode);
        return LIU_Mode;
    }

    static Boolean debug = null;

    static public boolean isDebug() {
        if ( debug == null )
            debug = (new File("DEBUG")).exists();
        return debug;
    }

    public static boolean fullScreen = !false;
    public static boolean smaller = OmegaContext.isDeveloper();

    public static final int VAR_NUM = 5;
    public static boolean tts = !true;

    static public boolean isKeyNext(int kc) {
        return kc == key_next_1 ||
                kc == key_next_2;
    }

    static public boolean isKeySelect(int kc) {
        return
                kc == key_select_1 ||
                        kc == key_select_2 ||
                        kc == key_select_3;
    }

    static public boolean isKeyESC(int kc) {
        return
                kc == '\033';
    }

    public static void setNextKey() {
        key_next_2 = ' ';
        key_select_1 = '\r';
    }

    public static void setSelectKey() {
        key_next_2 = 9;
        key_select_1 = ' ';
    }
}
