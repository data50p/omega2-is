package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.t9n.T;

import javax.swing.*;
import java.io.File;

public class ChooseMovieDir extends JFileChooser {
    public ChooseMovieDir() {
        super(new File("media"));
        setFileSelectionMode(DIRECTORIES_ONLY);
        setApproveButtonText(T.t("Select Directory"));
    }
}

