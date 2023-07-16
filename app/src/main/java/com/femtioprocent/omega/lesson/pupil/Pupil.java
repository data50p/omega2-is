package com.femtioprocent.omega.lesson.pupil;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.swing.ScaledImageIcon;

import java.awt.*;
import java.util.HashMap;

public class Pupil {
    String name;
    HashMap params;

    public Pupil(String name) {
        this.name = name;
        OmegaContext.def_log.getLogger().info("new Pupil " + name);
        OmegaContext.lesson_log.getLogger().info("created Pupil: " + name);
    }

    public String getName() {
        return name;
    }

    public String getTestId() {
        return "pre_1";
    }

    public void setParams(HashMap hm) {
        params = hm;
    }

    public int getParamInt(String key) {
        return 1;
    }


    public int getInt(String key, int def) {
        try {
            if (params == null)
                return def;
            String s = (String) params.get(key);
            if (s == null)
                return def;
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return def;
        }
    }

    public boolean getBool(String key, boolean def) {
        if (params == null)
            return def;
        String s = (String) params.get(key);
        if (s == null)
            return def;
        return
                s.startsWith("y") ||
                        s.startsWith("Y") ||
                        s.startsWith("j") ||
                        s.startsWith("J") ||
                        "true".equals(s) ||
                        "T".equals(s);
    }

    public String getString(String key, String def) {
        if (params == null)
            return def;
        String s = (String) params.get(key);

        OmegaContext.lesson_log.getLogger().info("param String, " + key + ' ' + def + ' ' + s);

        return s == null ? def : s;
    }

    public String getStringNo0(String key, String def) {
        if (params == null) {
            OmegaContext.lesson_log.getLogger().info("param NULL, " + key + ' ' + def);
            return def;
        }
        String s = (String) params.get(key);
        String ret = s == null || s.length() == 0 ? def : s;
        OmegaContext.sout_log.getLogger().info(":--: " + "Pupil -> " + ret + ' ' + (def));
        OmegaContext.lesson_log.getLogger().info("OK " + ret);

        return ret;
    }

    public int getSpeed(int val) {
        double[] dA = new double[]{0.6, 1.0, 1.5};
        if (params == null)
            return val;
        String s = (String) params.get("speed");
        if (s == null)
            s = "1";
        int ix = Integer.parseInt(s);
        double f = dA[ix];
        return (int) (val * f);
    }

    public Image getImage(Component comp) {
        if (params == null)
            return null;
        String s = (String) params.get("image");
        if (s == null)
            return null;
        return ScaledImageIcon.createImageIcon(comp,
                s,
                100,
                80).getImage();
    }

    public String getImageName() {
        if (params == null)
            return null;
        String s = (String) params.get("image");
        return s;
    }

    public String getImageNameWrongAnswer() {
        if (params == null)
            return null;
        String s = (String) params.get("image_wrong");
        return s;
    }

    public String toString() {
        return "Pupil:" + getName();// + ':' + params;
    }
}
