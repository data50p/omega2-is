package com.femtioprocent.omega.swing.filechooser;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;


public class ChooseAudioFile extends JFileChooser {
    public ChooseAudioFile() {
        super(new File(OmegaContext.omegaAssets("media/audio")));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension("mp3");
        fi.addExtension("wav");
        setFileFilter(fi);
    }
}
