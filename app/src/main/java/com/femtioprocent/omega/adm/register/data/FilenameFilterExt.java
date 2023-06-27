package com.femtioprocent.omega.adm.register.data;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterExt implements FilenameFilter {
    String ext;
    String with[];

    FilenameFilterExt(String ext) {
        this(ext, null);
    }

    FilenameFilterExt(String ext, String[] with) {
        this.ext = ext;
        this.with = with;
    }

    public boolean accept(File dir, String fname) {
        if (fname.endsWith(ext)) {
            if (with == null)
                return true;
            for (int i = 0; i < with.length; i++) {
                if (fname.indexOf(with[i]) != -1)
                    return true;
            }
            return false;
        }
        return false;
    }

    public String toString() {
        return "FilenameFilterExt{" + ext + "}";
    }
}
