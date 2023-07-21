package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.LicenseShowManager;
import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.appl.LessonEditor;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import java.util.HashMap;

public class LessonEditorAppl extends OmegaAppl {
    LessonEditor le;

    public LessonEditorAppl(String fn) {
        super("Lesson editor");
        le = new LessonEditor("Omega - Lesson Editor:", OmegaContext.antiOmegaAssets(fn));
    }

    public static void main(String[] argv) {
        HashMap<String,String> flag = SundryUtils.flagAsMap(argv);
        java.util.List<String> argl = SundryUtils.argAsList(argv);

        if (flag.get("help") != null) {
            OmegaContext.serr_log.getLogger().info("-help");
            OmegaContext.serr_log.getLogger().info("-omega_assets=<assets name>");
            OmegaContext.serr_log.getLogger().info("-omega_lang=<lang>");
            OmegaContext.serr_log.getLogger().info("-T=<step>");
            OmegaContext.serr_log.getLogger().info("-small");
            OmegaContext.serr_log.getLogger().info("-");
            OmegaContext.serr_log.getLogger().info("-");
            System.exit(1);
        }

        OmegaContext.setOmegaAssets((String) flag.get("omega_assets"));

        OmegaContext.omega_lang = (String) flag.get("omega_lang");
        OmegaContext.sout_log.getLogger().info(":--: " + "param omega_lang is " + OmegaContext.omega_lang);
        OmegaContext.small = (String) flag.get("small");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.MetalLookAndFeel");
//	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        String t_steps = (String) flag.get("T");
        if (t_steps != null)
            OmegaConfig.t_step = Integer.parseInt(t_steps);

        String fn = argl.size() > 0 ? (String) argl.get(0) : null;

        if (LicenseShowManager.showAndAccepted()) {
            LessonEditorAppl e = new LessonEditorAppl(fn);
        } else {
            System.exit(1);
        }
    }
}
