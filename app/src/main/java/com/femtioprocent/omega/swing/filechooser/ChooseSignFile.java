package com.femtioprocent.omega.swing.filechooser;


import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;


public class ChooseSignFile extends JFileChooser {
    public ChooseSignFile() {
        super(new File(OmegaContext.omegaAssets("media/sign")));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension("mpg");
        setFileFilter(fi);
    }
}
