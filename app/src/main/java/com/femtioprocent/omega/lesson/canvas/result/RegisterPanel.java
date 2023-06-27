package com.femtioprocent.omega.lesson.canvas.result;

import com.femtioprocent.omega.swing.GBC_Factory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class RegisterPanel extends JPanel {
    RegisterPanel() {
        setLayout(new GridBagLayout());
        populate();
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    GBC_Factory gbcf = new GBC_Factory();

    void populate() {
    }

    public Insets getInsets() {
        return new Insets(5, 5, 5, 5);
    }

    public void dispose() {
        removeAll();
    }
}
