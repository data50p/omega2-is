package com.femtioprocent.omega.anim.panels.timeline;

import java.io.File;
import java.io.FilenameFilter;


public class FilenameFilterExt implements FilenameFilter {
    String ext;
    boolean dir_only;

    FilenameFilterExt(String ext) {
        this(ext, false);
    }

    FilenameFilterExt(String ext, boolean dir_only) {
        this.dir_only = dir_only;
        this.ext = ext;
    }

    public boolean accept(File dir, String fname) {
        if (fname.endsWith(ext))
            return true;
        return false;
    }

    public String toString() {
        return "FilenameFilterExt{" + ext + "}";
    }
}
