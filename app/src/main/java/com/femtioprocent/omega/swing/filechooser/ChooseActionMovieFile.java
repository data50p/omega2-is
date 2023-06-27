package com.femtioprocent.omega.swing.filechooser;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

public class ChooseActionMovieFile extends JFileChooser {
    public ChooseActionMovieFile(boolean omega_anim) {
        super(new File(OmegaContext.omegaAssets("anim")));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        if ( omega_anim )
            fi.addExtension("omega_anim");
        fi.addExtension("mpeg");
        fi.addExtension("mpg");
        fi.addExtension("mov");
        fi.addExtension("avi");
        fi.addExtension("MPEG");
        fi.addExtension("MPG");
        fi.addExtension("MOV");
        fi.addExtension("AVI");
        fi.addExtension("mp4");
        fi.addExtension("mpv");
        fi.addExtension("MP4");
        fi.addExtension("MPV");
        setFileFilter(fi);
    }
}
