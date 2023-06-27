package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

//import omega.lesson.test.*;

public class ChooseStoryFile extends JFileChooser {
    static String ext = "omega_story_replay";

    public ChooseStoryFile(String dir) {
        super(new File(dir));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension(ext);
        setFileFilter(fi);
    }
}
