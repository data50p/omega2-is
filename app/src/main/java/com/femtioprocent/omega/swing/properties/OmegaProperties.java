package com.femtioprocent.omega.swing.properties;

import javax.swing.*;
import java.awt.*;

public class OmegaProperties extends JDialog {
    protected Object obj;

    public OmegaProperties(JFrame owner) {
        super(owner);
    }

    public OmegaProperties(JFrame owner, String title) {
        super(owner, title);
    }

    public OmegaProperties(JFrame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public OmegaProperties(Dialog owner) {
        super(owner);
    }

    public OmegaProperties(Dialog owner, String title) {
        super(owner, title);
    }

    public OmegaProperties(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public void setObject(Object obj) {
        this.obj = obj;
        refresh();
    }

    public void refresh() {
    }
}
