package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

public class ChooseOmegaBundleFile extends JFileChooser {
    public ChooseOmegaBundleFile() {
        super(new File(OmegaContext.omegaAssets("..")));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension(OmegaConfig.OMEGA_BUNDLE);
        setFileFilter(fi);
    }
}

