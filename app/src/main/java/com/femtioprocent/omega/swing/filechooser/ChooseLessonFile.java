package com.femtioprocent.omega.swing.filechooser;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.ExtensionFileFilter;

import javax.swing.*;
import java.io.File;

public class ChooseLessonFile extends JFileChooser {
    public static String ext = "omega_lesson";
    private static File lastFile;

    public ChooseLessonFile() {
            super(new File(OmegaContext.omegaAssets(".")));
            ExtensionFileFilter fi = new ExtensionFileFilter();
            fi.addExtension(ext);
            setFileFilter(fi);
    }

    public ChooseLessonFile(int dirStep) {
        super(new File(getDir(dirStep)));
        ExtensionFileFilter fi = new ExtensionFileFilter();
        fi.addExtension(ext);
        setFileFilter(fi);
    }

    private static String getDir(int dirStep) {
        OmegaContext.sout_log.getLogger().info("getDir " + dirStep + ' ' + lastFile);
        if ( dirStep == -1 )
            return OmegaContext.omegaAssets(".");
        if ( lastFile == null )
            return OmegaContext.omegaAssets(".");
        File f = lastFile;
        OmegaContext.sout_log.getLogger().info("getDir file " + f);
        while(dirStep-- > 0 ) {
            f = f.getParentFile();
            OmegaContext.sout_log.getLogger().info("getDir parent " + f);
        }
        OmegaContext.sout_log.getLogger().info("getDir return " + f.getPath());
        return f.getPath();
    }

    public void setLastFile(File lastFile) {
        OmegaContext.sout_log.getLogger().info("getDir setLastFile " + lastFile + " -> " + ChooseLessonFile.lastFile);
        ChooseLessonFile.lastFile = lastFile;
    }
}
