package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;


public class ChoosePupilImageFile extends JFileChooser {
    public ChoosePupilImageFile() {
        super(new File("media"));
        ExtensionFileFilter fi = new ExtensionFileFilter(new String[]{"jpg"});
        setFileFilter(fi);
        setApproveButtonText(T.t("Select"));
    }
}
