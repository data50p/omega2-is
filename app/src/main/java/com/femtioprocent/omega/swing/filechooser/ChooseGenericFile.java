package com.femtioprocent.omega.swing.filechooser;


import com.femtioprocent.omega.OmegaContext;

import javax.swing.*;
import java.io.File;


public class ChooseGenericFile extends JFileChooser {
    public ChooseGenericFile() {
        this(false);
    }

    ChooseGenericFile(boolean runtime) {
        super(new File(OmegaContext.omegaAssets(runtime ? ".." : ".")));
    }
}


