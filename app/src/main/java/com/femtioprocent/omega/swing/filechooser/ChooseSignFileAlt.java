package com.femtioprocent.omega.swing.filechooser;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;


public class ChooseSignFileAlt extends JFileChooser {
    public ChooseSignFileAlt() {
        super(new File(OmegaContext.omegaAssets("media")));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension("mp4");
        setFileFilter(fi);
    }
}
