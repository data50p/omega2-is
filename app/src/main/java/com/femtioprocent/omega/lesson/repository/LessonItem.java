package com.femtioprocent.omega.lesson.repository;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;

import java.io.File;

public class LessonItem {
    String fname;
    String name;
    boolean is_dir = false;

    public LessonItem(String fname) {
        this.fname = fname;
        int ix = fname.lastIndexOf("/");
        name = fname.substring(ix + 1);

        String[] sa = Locator.scanDirDir(getLessonDirName());
        if (sa != null && sa.length > 0)
            is_dir = true;
    }

    public String getSelector() {
        return getOmegaSelectorFile();
    }

    public String getDefaultLessonFile() {
        return getOmegaLessonFile();
    }

    public String getLessonName() {
        return name;
    }

    public String getLessonFileName() {
        return fname;
    }

    public String getLessonFileNameBase() {
        int ix = fname.lastIndexOf("/");
        return fname.substring(0, ix);
    }

    public String getLessonImageFileName() {
        return getLessonImageFileName("");
    }

    public String getLessonImageFileName(String more) {
        return getLessonFileNameBase() + '/' + getLessonName() + "/" + "image" + more + ".png";
    }

    public String getLessonParentImage(String more) {
        return getLessonFileNameBase() + '/' + getLessonName() + "/../" + "image" + more + ".png";
    }

    public String getLessonDisplayName(String lessonLang) {
        return getLessonFileContent("display", lessonLang);
    }

    public String getLessonFileContent(String fName, String lessonLang) {
        String fn = getLessonFileNameBase() + '/' + getLessonName() +  "/" + fName + (lessonLang == null ? "" : "-" + lessonLang);
        if ( !OmegaContext.omegaAssetsExist(fn) )
            return lessonLang == null ? null : getLessonFileContent(fName, null);
        return SundryUtils.getFileContent(fn);
    }

    public String getLessonDirName() {
        return getLessonFileNameBase() + '/' + getLessonName();
    }

    public String getLessonShortName() {
        return getLessonName();
    }

    public String getLessonLongName() {
        return fname;
    }

    public String getDirName() {
        if (isDir())
            return name;
        else
            return null;
    }

//      public String getOmegaLessonFile() {
//  	String[] sa = Locator.scanDirLes(getLessonDirName());
//  	if ( sa != null ) {
//  	    OmegaContext.sout_log.getLogger().info(":--: " + "FOUND >>>>>>>> " + SundryUtils.a2s(sa));
//  	    if ( sa.length > 0 )
//  		return sa[0];
//  	}
//  	return "";
//      }

    public String getOmegaSelectorFile() {
        String[] sa = Locator.scanDirSel(getLessonDirName());
        if (sa != null) {
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "FOUND >>>>>>>> " + SundryUtils.a2s(sa));
            if (sa.length > 0)
                return sa[0];
        }
        return null;
    }

    public String getOmegaLessonFile() {
        String[] sa = Locator.scanDirLes(getLessonDirName());
        if (sa != null) {
            Log.getLogger().info("Scanned Lesson files: " + SundryUtils.a2s(sa));
            if (sa.length > 0)
                return sa[0];
        }
        return null;
    }

    public String toString() {
        return fname + ':' + name;
    }

    public boolean isDir() {
        return is_dir;
    }

    public boolean isStory() {
        String dn = getLessonDirName();
        File file = new File(dn + '/' + "story");
        return file.exists();
    }
}
