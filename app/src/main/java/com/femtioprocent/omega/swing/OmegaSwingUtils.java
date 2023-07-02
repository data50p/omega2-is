package com.femtioprocent.omega.swing;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.MilliTimer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by lars on 2017-02-18.
 */
public class OmegaSwingUtils {
    public static ImageIcon getImageIcon(String path) {
        try {
            MilliTimer mt1 = new MilliTimer();
            Image im = getImage(path);
            ImageIcon icon = new ImageIcon(im);
            OmegaContext.sout_log.getLogger().info("IMAGE7: " + "load image icon " + path + ' ' + mt1.getString());
            return icon;
        } catch (Exception e) {
            Log.getLogger().warning("Can't find ImageIcon for " + path);
            return null;
        }
    }

    public static Image getImage(String path) {
        try {
            MilliTimer mt1 = new MilliTimer();
            URL url = OmegaSwingUtils.class.getClassLoader().getResource(path);
            BufferedImage c = ImageIO.read(url);
            OmegaContext.sout_log.getLogger().info("IMAGE8: " + "load image res " + path + ' ' + mt1.getString());
            return c;
        } catch (Exception e) {
            File cdir = new File(".");
            try {
                OmegaContext.sout_log.getLogger().info("File " + cdir.getCanonicalPath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Image image = Toolkit.getDefaultToolkit().createImage(path);
            OmegaContext.sout_log.getLogger().info("getImage() from resource did not work for: " + path + ' ' + image);
            return image;
        }
    }
}
