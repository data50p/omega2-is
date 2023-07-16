package com.femtioprocent.omega;

// DO NO CHANGE HERE

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
        return getVersion() + "; build: " + getBuildDate();
    }

    static public String getVersion() {
        return get("Version");
    }

    static public String getBuildDate() {
        return get("Date");
    }

    public static String getVersionBlurb() {
        return getOmegaVersion() + "\n"
                + "CWD: " + OmegaVersion.getCWD() + "\n"
                + "Version: java " + OmegaVersion.getJavaVersion() + ",   javafx " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion() + "\n"
                + "Java Vendor: " + OmegaVersion.getJavaVendor() + "; OS name: " + System.getProperty("os.name").toLowerCase() + "\n"
                + "java home: " + OmegaVersion.getJavaHome();
    }

    static public String get(String item) {
        return switch (item) {
            case "Version" -> "2.1.0";
            case "Date" -> "§§23-07-16_04:17:54 mango.local §§".replaceAll("§", "");
            default -> "?";
        };
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
