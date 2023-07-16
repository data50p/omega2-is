package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.LicenseShowManager;
import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.appl.AnimEditor;
import com.femtioprocent.omega.appl.OmegaAppl;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class AnimEditorAppl extends OmegaAppl {
    public AnimEditor ae;

    public AnimEditorAppl(boolean verbose) {
        super("Animator editor");

        KeyboardFocusManager.setCurrentKeyboardFocusManager(new DefaultKeyboardFocusManager() {
            char last_state = '_';
            char state = 'r';
            boolean first_tr = false;

            boolean P = false;

            public boolean dispatchKeyEvent(KeyEvent e) {
                char ch = e.getKeyChar();
                int kc = e.getKeyCode();

                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == KeyEvent.VK_F1) {
                        AnimEditor.help.showManualAE();
                    }

                }
                return super.dispatchKeyEvent(e);
            }
        });

        ae = new AnimEditor(verbose);
    }

    public static void main(String[] argv) {
        HashMap flag = SundryUtils.flagAsMap(argv);
        java.util.List argl = SundryUtils.argAsList(argv);

        OmegaContext.omega_lang = (String) flag.get("omega_lang");
        OmegaContext.sout_log.getLogger().info(":--: " + "param omega_lang is " + OmegaContext.omega_lang);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.MetalLookAndFeel");
            // UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        boolean verbose = false;

        if (flag.get("v") != null)
            verbose = true;
        if (flag.get("R") != null)
            OmegaConfig.RUN_MODE = true;
        if (flag.get("T") != null)
            OmegaConfig.T = true;

        String s = null;
        if ((s = (String) flag.get("t")) != null)
            OmegaConfig.t_step = Integer.parseInt(s);
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + OmegaConfig.t_step);

        if (LicenseShowManager.showAndAccepted()) {
            AnimEditorAppl e = new AnimEditorAppl(verbose);
            SundryUtils.m_sleep(3000);
            while ( Splash.keep != null && Splash.keep )
                SundryUtils.m_sleep(300);
            OmegaAppl.closeSplash();
        } else {
            System.exit(1);
        }

//log	OmegaContext.sout_log.getLogger().info(":--: " + "--------ok-------");
    }
}
