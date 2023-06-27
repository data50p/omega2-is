package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;


public class ChooseAnimatorFile extends JFileChooser {
    public static String ext = "omega_anim";

    public ChooseAnimatorFile() {
        super(new File(OmegaContext.omegaAssets("anim")));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension(ext);
        setFileFilter(fi);
    }
}

