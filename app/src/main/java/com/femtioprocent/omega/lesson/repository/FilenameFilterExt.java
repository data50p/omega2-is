package com.femtioprocent.omega.lesson.repository;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterExt implements FilenameFilter {
    String ext;

    FilenameFilterExt(String ext) {
        this.ext = ext;
    }

    public boolean accept(File dir, String fname) {
        if (fname.endsWith(ext))
            return true;
        return false;
    }
}
