package com.femtioprocent.omega.lesson.repository;

import com.femtioprocent.omega.OmegaContext;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;


public class Locator {
    static String lang = null;

    static String getLessonBase() {
        if (OmegaContext.DEMO)
            return OmegaContext.omegaAssets("lesson-" + OmegaContext.getLessonLang() + "/demo");    // LESSON-DIR-A
        else
            return OmegaContext.omegaAssets("lesson-" + OmegaContext.getLessonLang() + "/active");    // LESSON-DIR-A
    }

    static FilenameFilter fnf_dir = new FilenameFilter() {
        public boolean accept(File dir, String fname) {
            File f = new File(dir, fname);
            if (f.isDirectory() && !f.getName().equals(".svn"))
                return true;
            return false;
        }
    };

    static FilenameFilter fnf_se = new FilenameFilter() {
        public boolean accept(File dir, String fname) {
            if (fname.endsWith(".omega_selector"))
                return true;
            return false;
        }
    };

    public Locator() {
    }

    public static void setLang(String lang) {
// 	Locator.lang = lang;
// 	if ( lang != null )
// 	    fbase = "lesson-" + lang + "/active";
// 	else
// 	    fbase = "lesson-" + OmegaContext.getLessonLang() + "/active";
    }

    static FilenameFilter fnf_le = new FilenameFilter() {
        public boolean accept(File dir, String fname) {
            if (fname.endsWith(".omega_lesson"))
                return true;
            return false;
        }
    };

    static String[] scanDirLes(String dir) {
        File df = new File(dir);
        File[] fa = df.listFiles(fnf_le);
//	OmegaContext.sout_log.getLogger().info(":--: " + "# " + SundryUtils.a2s(fa));
        int N = 0;
        if (fa != null) {
            for (int i = 0; i < fa.length; i++)
                if (!".svn".equals(fa[i].getName()))
                    N++;
        }
        if (fa != null) {
            String[] sa = new String[N];
            int ii = 0;
            for (int i = 0; i < fa.length; i++)
                if (!".svn".equals(fa[i].getName()))
                    sa[ii++] = dir + '/' + fa[i].getName();
            Arrays.sort(sa);
            return sa;
        }
        return null;
    }

    static String[] scanDirSel(String dir) {
        File df = new File(dir);
        File[] fa = df.listFiles(fnf_se);
//	OmegaContext.sout_log.getLogger().info(":--: " + "scan sel " + dir);
        if (fa != null) {
            String[] sa = new String[fa.length];
            for (int i = 0; i < fa.length; i++)
                sa[i] = dir + '/' + fa[i].getName();
            Arrays.sort(sa);
            return sa;
        }
        return null;
    }

    static String[] scanDirDir(String dir) {
        File df = new File(dir);
        File[] fa = df.listFiles(fnf_dir);
//	OmegaContext.sout_log.getLogger().info(":--: " + "scan dirdir " + dir);
        if (fa != null) {
            String[] sa = new String[fa.length];
            for (int i = 0; i < fa.length; i++)
                sa[i] = dir + '/' + fa[i].getName();
            Arrays.sort(sa);
            return sa;
        }
        return null;
    }

    static String[] scanDir(String dir) {
//	OmegaContext.sout_log.getLogger().info(":--: " + "SCAN in " + dir + ' ' + StackTrace.trace());
        File df = new File(dir);
        File[] fa = df.listFiles(fnf_dir);
        int N = 0;
        if (fa != null) {
            for (int i = 0; i < fa.length; i++)
                if (!".svn".equals(fa[i].getName()))
                    N++;
        }
        if (fa != null) {
            String[] sa = new String[N];
            int ii = 0;
            for (int i = 0; i < fa.length; i++)
                if (!".svn".equals(fa[i].getName()))
                    sa[ii++] = dir + '/' + fa[i].getName();
            Arrays.sort(sa);
            return sa;
        }
        return null;
    }

    static String[] scanDirExt(String dir, String ext) {
        File df = new File(dir);
        File[] fa = df.listFiles(fnf_le);
//	OmegaContext.sout_log.getLogger().info(":--: " + "scan sel " + dir);
        if (fa != null) {
            String[] sa = new String[fa.length];
            for (int i = 0; i < fa.length; i++)
                sa[i] = dir + '/' + fa[i].getName();
            Arrays.sort(sa);
            return sa;
        }
        return null;
    }

    static String[] scanDir(String dir, FilenameFilter fnf) {
//	OmegaContext.sout_log.getLogger().info(":--: " + "scan " + dir + ' ' + fnf);
        File df = new File(dir);
        File[] fa = df.listFiles(fnf);
        if (fa != null) {
            String[] sa = new String[fa.length];
            for (int i = 0; i < fa.length; i++)
                sa[i] = dir + '/' + fa[i].getName();
            Arrays.sort(sa);
            return sa;
        }
        return null;
    }

    public String[] getAllLessonsInDir() {
        return scanDir(getLessonBase());//fbase);
    }

    public String[] getAllLessonsInDir(String more) {
        return scanDir(getLessonBase() /*fbase*/ + '/' + more);
    }

    public String[] getAllActiveFiles(String fbase, String ext) {
        if (OmegaContext.DEMO)
            fbase = fbase.replaceAll("active", "demo");               // DIR

        FilenameFilterExt fnf_ext = new FilenameFilterExt(ext);
        String[] dirs = scanDir(fbase);
        String[] sa = new String[1000];
        int ix = 0;

//	OmegaContext.sout_log.getLogger().info(":--: " + "FILES dirs " + SundryUtils.a2s(dirs));
        if (dirs != null)
            for (int i = 0; i < dirs.length; i++) {
                String[] files = scanDir(dirs[i], fnf_ext);
//	    OmegaContext.sout_log.getLogger().info(":--: " + "FILES " + SundryUtils.a2s(files));
                if (files != null) {
                    System.arraycopy(files, 0, sa, ix, files.length);
                    ix += files.length;
                }
            }
        String ssa[] = new String[ix];
        System.arraycopy(sa, 0, ssa, 0, ix);
        return ssa;
    }

    public String[] getAllSelectorInDir() {
        return scanDirSel(getLessonBase() /*fbase*/);
    }
}
