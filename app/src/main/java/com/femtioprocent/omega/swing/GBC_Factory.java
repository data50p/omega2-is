package com.femtioprocent.omega.swing;

import java.awt.*;

public class GBC_Factory {
    int a = 2;

    public GBC_Factory() {
    }

    public GridBagConstraints create(int x, int y) {
        return new GridBagConstraints(x, y, 1, 1,
                0, 0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(a, a, a, a),
                0, 0);
    }

    public GridBagConstraints create(int x, int y, int w) {
        return new GridBagConstraints(x, y, w, 1,
                0, 0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(a, a, a, a),
                0, 0);
    }

    public GridBagConstraints createL(int x, int y, int w) {
        return new GridBagConstraints(x, y, w, 1,
                0, 0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(a, a, a, a),
                0, 0);
    }

    public GridBagConstraints createR(int x, int y) {
        return new GridBagConstraints(x, y, GridBagConstraints.REMAINDER, 1,
                0, 0,
                GridBagConstraints.EAST,
                GridBagConstraints.HORIZONTAL,
                new Insets(a, a, a, a),
                0, 0);
    }

    public GridBagConstraints createR2(int x, int y) {
        return new GridBagConstraints(x, y, GridBagConstraints.REMAINDER, 1,
                0, 0,
                GridBagConstraints.EAST,
                0,
                new Insets(a, a, a, a),
                0, 0);
    }
}
