package com.femtioprocent.omega.anim.appl;

import javax.swing.*;

;

public class Main {
    public Main() {
        JFrame f = new JFrame();
        f.pack();
        f.setVisible(true);
    }

    //Main method
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.MetalLookAndFeel");
// UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        Main m = new Main();
    }
} 
