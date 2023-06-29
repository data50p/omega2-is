package com.femtioprocent.omega;

// DO NO CHANGE HERE

import java.io.*;

public class OmegaVersion {
    static public String getOmegaVersion() {
        return getOmegaVersion(null);
    }

    static public String getOmegaVersion(String s) {
        if (s != null)
            return s + ' ' + "Ω, version: " + getDetailedVersion();
        else
            return "Ω, version: " + getDetailedVersion();
    }

    static public String getDetailedVersion() {
        String ver = getVersion() + "; build: " + getBuildDate();
        return ver;
    }

    static public String getVersion() {
        return get("Version");
    }

    static public String getBuildDate() {
        return get("Date");
    }

    static public String get(String item) {
        switch (item) {
            case "Version":
                return "2.1.0";
            case "Date":
                return "§§23-06-29_13:06:08§§".replaceAll("§", "");
            default:
                return "?";
        }
/*        try {
            InputStream ins = OmegaVersion.class.getClassLoader().getResourceAsStream("version");
            if (ins != null) {
                Reader r = new InputStreamReader(ins);
                BufferedReader br = new BufferedReader(r);
                for (; ; ) {
                    String s = br.readLine();
                    if (s.startsWith(item + ":"))
                        return s.substring((item + ":").length()).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item + ": ?";
 */
    }

    public static String getCWD() {
        return System.getProperty("user.dir");
    }

    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    public static String getJavaVendor() {
        return System.getProperty("java.vendor");
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
}

// DO NO CHANGE HERE
