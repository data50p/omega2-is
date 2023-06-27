package com.femtioprocent.omega.swing;

import com.femtioprocent.omega.graphic.util.LoadImage;
import com.femtioprocent.omega.util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ScaledImageIcon {
    public static ImageIcon createImageIcon(Component comp, String fn, int max_w, int max_h) {
        return createImageIcon(comp, fn, max_w, max_h, true);
    }

    public static ImageIcon createImageIcon(Component comp, String fn, int max_w, int max_h, boolean no_bigger) {
        Image im;
        if (fn.startsWith("toolbarButtonGraphics/"))
            im = LoadImage.loadAndWaitFromResource(comp, fn);
        else
            im = LoadImage.loadAndWaitFromFile(comp, fn);
        Log.getLogger().config("Create ImageIcon from: " + fn + ' ' + im);
        if (im == null)
            return null;

        int ww = max_w;
        int hh = max_h;
        int imw = im.getWidth(null);
        int imh = im.getHeight(null);

        if (imw == -1 || imh == -1)
            return null;

        if (ww == 0)
            ww = 20;
        if (hh == 0)
            hh = 20;

        if (no_bigger) {
            if (imw < max_w)
                ww = imw;
            if (imh < max_h)
                hh = imh;
        }
        double fw = (double) imw / ww;
        double fh = (double) imh / hh;
        double f = fw;
        if (fw < fh)
            f = fh;
        BufferedImage imd = null;

        try {
            imd = new BufferedImage((int) (imw / f), (int) (imh / f),
                    BufferedImage.TYPE_INT_ARGB);
            // createImage((int)(imw / f), (int)(imh / f));
        } catch (IllegalArgumentException _ex) {
        }
        if (imd == null)
            return null;
        Graphics2D g2 = (Graphics2D) imd.getGraphics();
        AffineTransform at = new AffineTransform();
        at.scale(1.0 / f, 1.0 / f);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D fr = new Rectangle2D.Double(0, 0, 2000, 2000);
        g2.fill(fr);
        g2.setComposite(AlphaComposite.SrcOver);
        g2.drawImage(im, at, null);
        return new ImageIcon(imd);
    }
}
