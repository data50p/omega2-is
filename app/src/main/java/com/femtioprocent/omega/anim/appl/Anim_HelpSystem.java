package com.femtioprocent.omega.anim.appl;

import com.femtioprocent.omega.help.HelpSystem;
import com.femtioprocent.omega.swing.HtmlFrame;

public class Anim_HelpSystem extends HelpSystem {
    HtmlFrame html_fr;

    void showManualL() {
        show(mkFileName("lesson_manual"), 800, 600);
    }

    public void showManualAE() {
        show(mkFileName("editor_manual"), 800, 600);
    }
}
