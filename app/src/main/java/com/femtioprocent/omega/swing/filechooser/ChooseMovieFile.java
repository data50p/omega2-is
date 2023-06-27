package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

public class ChooseMovieFile extends JFileChooser {
    public ChooseMovieFile() {
        super(new File("media"));
        ExtensionFileFilter fi = new ExtensionFileFilter(new String[]{"mpg",
                "mpeg",
                "mov",
                "avi"});
        setFileFilter(fi);
        setApproveButtonText(T.t("Select"));
    }
}

