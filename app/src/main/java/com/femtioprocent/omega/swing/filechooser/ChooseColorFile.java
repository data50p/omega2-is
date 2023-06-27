package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

//import omega.lesson.test.*;

public class ChooseColorFile extends JFileChooser {
    public static String ext = "omega_colors";

    public ChooseColorFile() {
        super(new File(OmegaContext.omegaAssets(".")));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension(ext);
        setFileFilter(fi);
    }
}
