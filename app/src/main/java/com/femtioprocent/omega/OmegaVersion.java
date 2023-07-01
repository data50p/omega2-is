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

    public static String getVersionBlurb() {
        String s = "CWD: " + OmegaVersion.getCWD() + "\n" +
        "Version: java " + OmegaVersion.getJavaVersion() + ",   javafx " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion() + "\n" +
        "Java Vendor: " + OmegaVersion.getJavaVendor() + "; OS name: " + System.getProperty("os.name").toLowerCase() + "\n" +
        "java home: " + OmegaVersion.getJavaHome();
        return s;
    }

    static public String get(String item) {
        switch (item) {
            case "Version":
                return "2.1.0";
            case "Date":
                return "§§23-07-01_13:47:13 hallon-omega§§".replaceAll("§", "");
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
