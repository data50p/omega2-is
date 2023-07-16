package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class RegLocator {
    static String fbase = "register";  // this is not inside the omega_assets
    static String PUPIL_SUF = ".p";
    static String RESULT_SUF = ".omega_result";

    public RegLocator() {
    }

    String aRegisterFbase(String subPath) {
        return fbase + File.separatorChar + subPath;
    }

    String aRegisterFbase() {
        return fbase;
    }

    static String[] scanDir(String dir, FilenameFilter fnf) {
        OmegaContext.sout_log.getLogger().info(":--: " + "scan " + dir + ' ' + fnf);
        File df = new File(dir);
        File[] fa = df.listFiles(fnf);
        if (fa != null) {
            String[] sa = new String[fa.length];
            for (int i = 0; i < fa.length; i++)
                sa[i] = dir + File.separatorChar + fa[i].getName();
            Arrays.sort(sa);
            return sa;
        }
        return null;
    }

    String[] removeSuffix(String[] sa, String suf) {
        if (sa == null)
            return new String[0];
        String nsa[] = new String[sa.length];
        for (int i = 0; i < sa.length; i++)
            if (sa[i].endsWith(suf))
                nsa[i] = sa[i].substring(0, sa[i].length() - suf.length());
            else
                nsa[i] = sa[i];
        return nsa;
    }

    String[] removePrefix(String[] sa, String pre) {
        if (sa == null)
            return new String[0];
        String nsa[] = new String[sa.length];
        for (int i = 0; i < sa.length; i++)
            if (sa[i].startsWith(pre))
                nsa[i] = sa[i].substring(pre.length());
            else
                nsa[i] = sa[i];
        return nsa;
    }

    public String[] getAllPupilsName() {
        String sa[] = scanDir(aRegisterFbase(), new FilenameFilterExt(PUPIL_SUF));
        return removePrefix(removeSuffix(sa, PUPIL_SUF), aRegisterFbase() + File.separatorChar);
    }

    public String[] getAllResultsFName(String pupil, String[] with) {
        String sa[] = scanDir(aRegisterFbase() + File.separatorChar + pupil + PUPIL_SUF, new FilenameFilterExt(RESULT_SUF, with));
        return sa;
    }

    public String getFullFName(String pupil, String lesson_name) {
        String s = aRegisterFbase() + File.separatorChar +
                pupil + PUPIL_SUF + File.separatorChar +
            /*pupil + '-' +*/ lesson_name + RESULT_SUF;
        return s;
    }

    public String getDirPath(String pupil) {
        return aRegisterFbase() + File.separatorChar +
                pupil + PUPIL_SUF + File.separatorChar;
    }

    String getTestSuffix() {
        return RESULT_SUF;
    }

    public String mkResultsFName(String pupil, String name) {
        String s =
                aRegisterFbase() + File.separatorChar +
                        pupil + PUPIL_SUF + File.separatorChar +
                        name + RESULT_SUF;
        return s;
    }

    public void createPupilName(String name) {
        File f_old = new File(aRegisterFbase(name + ".deleted"));
        File f = new File(aRegisterFbase() + "/" + name + ".p");
        if (f.exists()) {
            JOptionPane.showMessageDialog(ApplContext.top_frame,
                    T.t("Pupil exist already"));
        }
        if (f_old.exists()) {
            JOptionPane.showMessageDialog(ApplContext.top_frame,
                    T.t("Pupil reinstalled"));
            f_old.renameTo(f);
        } else {
            f.mkdir();
            File ft = new File(aRegisterFbase(name + ".p/id.png"));
            File ff = new File(OmegaContext.getMediaFile("default/pupil.png"));
            Files.fileCopy(ff, ft);
            ft = new File(aRegisterFbase(name + ".p/pupil_settings.xml"));
            ff = new File(aRegisterFbase("Guest" + ".p/pupil_settings.xml"));
            Files.fileCopy(ff, ft);
        }
    }

    public static void main(String[] args) {
        RegLocator l = new RegLocator();
        String sa[] = l.getAllPupilsName();
        OmegaContext.sout_log.getLogger().info(":--: " + "" + SundryUtils.a2s(sa));
        sa = l.getAllResultsFName("Lars", new String[]{"test"});
        OmegaContext.sout_log.getLogger().info(":--: " + "" + SundryUtils.a2s(sa));
    }
}
