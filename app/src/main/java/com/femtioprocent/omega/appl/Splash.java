package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.OmegaVersion;
import com.femtioprocent.omega.graphic.util.LoadImage;

import javax.swing.*;
import java.awt.*;

public class Splash extends JWindow {
    Image im;

    int off_x = 0;

    Splash() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int ww = 600;
        off_x = (ww - 400) / 2;
        int hh = 380 + 22 * 4;
        setLocation((d.width - ww) / 2, (d.height - hh) / 2);
        setSize(ww, hh);
        if (im == null)
            im = LoadImage.loadAndWaitFromFile(this, OmegaContext.media() + "default/omega_splash.gif");
        setBackground(Color.black);
        setVisible(true);
    }

    public void paint(Graphics g) {
        g.drawImage(im, off_x, 10, null);
        g.setColor(Color.yellow);
        int line = 0;
        int yoff = 324;
        g.drawString(OmegaVersion.getOmegaVersion(), 5, yoff + 20 * line++);
        g.drawString(OmegaVersion.getCWD(), 5, yoff + 20 * line++);
        g.drawString("Java version " + OmegaVersion.getJavaVersion(), 5, yoff + 20 * line++);
        g.drawString(OmegaVersion.getJavaVendor() + "; " + System.getProperty("os.name").toLowerCase(), 5, yoff + 20 * line++);
        g.drawString(OmegaVersion.getJavaHome(), 5, yoff + 20 * line++);
    }
}
