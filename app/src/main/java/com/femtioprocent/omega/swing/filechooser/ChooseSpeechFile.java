package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

public class ChooseSpeechFile extends JFileChooser {
    public ChooseSpeechFile() {
        super(new File("media"));
        ExtensionFileFilter fi = new ExtensionFileFilter(new String[]{"wav", "mp3"});
        setFileFilter(fi);
        setApproveButtonText(T.t("Select"));
    }
}


