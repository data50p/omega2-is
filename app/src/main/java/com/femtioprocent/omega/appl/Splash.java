package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.OmegaVersion;
import com.femtioprocent.omega.anim.tool.path.Path;
import com.femtioprocent.omega.anim.tool.path.Probe;
import com.femtioprocent.omega.graphic.util.LoadImage;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class Splash extends JWindow {
    Image im;

    int off_x = 0;

    Mouse m;

    public static Boolean keep = null;


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
        m = new Mouse(this);
        setVisible(true);
    }

    class Mouse extends MouseInputAdapter {

        Mouse(JWindow owner) {
            owner.addMouseListener(this);
            owner.addMouseMotionListener(this);
        }

        public void mousePressed(MouseEvent e) {
            keep = keep == null ? true : !keep;
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }
    }

    public void paint(Graphics g) {
        g.drawImage(im, off_x, 10, null);
        g.setColor(Color.yellow);
        int line = 0;
        int yoff = 324;
        g.drawString(OmegaVersion.getOmegaVersion(), 5, yoff + 20 * line++);
        g.drawString("CWD: " + OmegaVersion.getCWD(), 5, yoff + 20 * line++);
        g.drawString("Version: java " + OmegaVersion.getJavaVersion() + ",   javafx " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion(), 5, yoff + 20 * line++);
        g.drawString("Java Vendor: " + OmegaVersion.getJavaVendor() + "; OS name: " + System.getProperty("os.name").toLowerCase(), 5, yoff + 20 * line++);
        g.drawString("java home: " + OmegaVersion.getJavaHome(), 5, yoff + 20 * line++);
    }
}
