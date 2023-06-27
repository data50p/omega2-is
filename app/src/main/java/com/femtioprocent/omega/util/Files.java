package com.femtioprocent.omega.util;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Files {

    // urldir, name

    static public String[] splitUrlString(String url_s) {
        File fi = new File(OmegaContext.omegaAssets("."));
        String cdu = null;
        try {
            cdu = fi.toURI().toURL().toString();
            cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ");
        } catch (MalformedURLException ex) {
            return null;
        }
        cdu = cdu.substring(0, cdu.length());
        cdu += "media/";
        if (OmegaConfig.T)
            OmegaContext.sout_log.getLogger().info("floppy: " + "try loading url from\n" + url_s + '\n' + cdu);
        int len_cd = cdu.length();

        String[] sa = new String[2];
        String name = url_s.substring(len_cd);
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "+++ " + name);
        sa[0] = cdu;
        sa[1] = name;
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "" + SundryUtils.arrToString(sa));
        return sa;
    }

    static public String mkRelativeCWD(String fn) {
        try {
            File fi = new File(".");
            String cdu = null;
            try {
                cdu = fi.toURI().toURL().toString();
            } catch (MalformedURLException ex) {
                return null;
            }
            cdu = cdu.substring(0, cdu.length());
            if ( cdu.endsWith("./") )
                cdu = cdu.substring(0, cdu.length() - 2); // -1 is to remove "./"
            OmegaContext.sout_log.getLogger().info("floppy: " + "mkRelativeCWD  " + fn + " -> " + cdu);
            cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ");
            OmegaContext.sout_log.getLogger().info("floppy: " + "mkRelativeCWD' " + fn + " -> " + cdu);
            int len_cd = cdu.length();

            String[] sa = new String[2];
            String name = fn.substring(len_cd);
            if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "+++ " + name);
            sa[0] = cdu;
            sa[1] = name;
            if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "" + SundryUtils.arrToString(sa));
            return sa[1];
        } catch (StringIndexOutOfBoundsException ex) {
        }
        return null;
    }

    static public String mkRelFname1(String url_s) {
        File fi = new File(OmegaContext.omegaAssets("."));// ".");
        String cdu = null;
        try {
            cdu = fi.toURI().toURL().toString();
            cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ");
        } catch (MalformedURLException ex) {
            return null;
        }
        //cdu = cdu.substring(0, cdu.length() - 1);
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "mkRelativeCWD\n" + url_s + '\n' + cdu);
        int len_cd = cdu.length();

        String[] sa = new String[2];
        String name = url_s.substring(len_cd);
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy " + "+++ " + url_s);
        sa[0] = cdu;
        sa[1] = name;
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy " + "=== " + SundryUtils.arrToString(sa));
        return sa[1];
    }

    static public String mkRelFname(String url_s) {
        File fi = new File(OmegaContext.omegaAssets("."));// ".");
        String cdu = null;
        try {
            cdu = fi.toURI().toURL().toString();
            cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ");
        } catch (MalformedURLException ex) {
            return null;
        }
        cdu = cdu.substring(0, cdu.length());
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "mkRelativeCWD\n" + url_s + '\n' + cdu);
        int len_cd = cdu.length();

        String[] sa = new String[2];
        String name = url_s.substring(len_cd);
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "+++ " + name);
        sa[0] = cdu;
        sa[1] = name;
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "" + SundryUtils.arrToString(sa));
        return sa[1];
    }

    static public String mkRelFnameAlt(String url_s, String prefix) {
        File fi = new File(OmegaContext.omegaAssets(prefix));
        String cdu = null;
        try {
            cdu = fi.toURI().toURL().toString();
            cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ");
        } catch (MalformedURLException ex) {
            return null;
        }
        int len_cd = cdu.length();

        String[] sa = new String[2];
        String name = url_s.substring(len_cd);
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy +++ " + url_s + ' ' + prefix);
        sa[0] = cdu;
        sa[1] = name;
        if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy === " + SundryUtils.arrToString(sa));
        return sa[1];
    }

    /*
WARNING   29/05 09:27:32.391     0 Files                   toURL                  URL matter:      C:\Users\Mats L\Documents\Omega-IS\default.omega_assets\lesson-sv\active\F-b_3-ord\F-b1\SpraySoapDry.omega_lesson
WARNING   29/05 09:27:32.391     0 Files                   toURL                      matter: new file:/C:/Users/Mats%20L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson
WARNING   29/05 09:27:32.391     0 Files                   toURL                      matter: old file:/C:/Users/Mats L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson
WARNING   29/05 09:27:32.391     0 Files                   toURL                      matter: alt file:/C:/Users/Mats L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson
INFO      29/05 09:27:32.391     0 Files                   mkRelativeCWD          ERR: mkRelativeCWD file:/C:/Users/Mats L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson -> file:/C:/Users/Mats%20L/Documents/Omega-IS/
INFO      29/05 09:27:32.391     0 Files                   mkRelativeCWD          ERR: +++ fault.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson

     */
    public static String toURL(File file) {
        String url_s = null;
//log	OmegaContext.sout_log.getLogger().info("ERR: " + "got file " + file);
        try {
            URL url = file.toURI().toURL();
            URL url2 = file.toURL();
            String url0 = "file:" + slashify(file.getAbsolutePath(), file.isDirectory());
            Log.getLogger().warning("URL matter:      " + file);
            Log.getLogger().warning("    matter: new " + url);
            Log.getLogger().warning("    matter: old " + url2);
            Log.getLogger().warning("    matter: alt " + url0);
            url_s = url0;//url2.toString();
        } catch (Exception ex) {
            OmegaContext.exc_log.getLogger().throwing(Files.class.getName(), "toURL", ex);
        }
        return url_s;
    }

    private static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (File.separatorChar != '/')
            p = p.replace(File.separatorChar, '/');
        if (!p.startsWith("/"))
            p = "/" + p;
        if (!p.endsWith("/") && isDirectory)
            p = p + "/";
        return p;
    }

    public static void fileCopy(File from, File to) {
        try {
            FileInputStream fr = new FileInputStream(from);
            FileOutputStream fw = new FileOutputStream(to);
            byte[] buf = new byte[10240];

            for (; ; ) {
                int n = fr.read(buf);
                if (n > 0) {
                    fw.write(buf, 0, n);
                } else {
                    break;
                }
            }
            fr.close();
            fw.close();
        } catch (Exception edx) {
        }
    }
}
