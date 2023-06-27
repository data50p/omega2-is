package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

public class ChooseImageFile extends JFileChooser {
    public ChooseImageFile() {
        super(new File(OmegaContext.omegaAssets("media")));
        ExtensionFileFilter fi = new ExtensionFileFilter(new String[]{"gif",
                "jpg",
                "jpeg",
                "png"});
        setFileFilter(fi);
        setApproveButtonText(T.t("Select"));
    }
}
