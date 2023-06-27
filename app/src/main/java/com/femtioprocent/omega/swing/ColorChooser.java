package com.femtioprocent.omega.swing;

import javax.swing.*;
import java.awt.*;

public class ColorChooser {
    public ColorChooser() {
    }

    public static Color select(Color col) {
        return JColorChooser.showDialog(null, "Select color", col);
    }
}
