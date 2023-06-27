package com.femtioprocent.omega.media.video;

import java.io.File;

/**
 * Created by lars on 2017-02-07.
 */
public class VideoUtil {

    /**
     * Try with supported movie types
     *
     * @param fname
     * @return
     */
    public static boolean exist(String fname) {
        return findSupportedFname(fname) != null;
    }

    /**
     * Find the first found supported existing movie file name from the intended.
     *
     * @param fname
     * @return
     */
    public static String findSupportedFname(String fname) {
        String altFname = fname.replaceAll("\\.[mM][pP][gG]$", ".mp4").replaceAll("\\.[mM][oO][vV]$", ".mp4").replaceAll("\\.[mM][pP][eE][gG]$", ".mp4").replaceAll("\\.[aA][vV][iI]$", ".mp4");
        System.err.println("alt file name " + altFname);
        if (fileExist(altFname))
            return altFname;

        File f = new File(altFname);
        String parent = f.getParentFile().getPath();
        String fn = f.getName();
        File altFile = new File(parent, fn.toLowerCase());
        if (fileExist(altFile))
            return altFile.getPath();
        return null;
    }

    private static boolean fileExist(String fname) {
        return fileExist(new File(fname));
    }

    private static boolean fileExist(File file) {
        return file.exists() && file.canRead();
    }
}
