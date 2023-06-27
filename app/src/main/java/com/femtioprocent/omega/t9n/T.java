package com.femtioprocent.omega.t9n;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.SundryUtils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;

public class T {
    static HashMap hm;
    static HashMap hm_new;
    public static String lang = "en";
    static String lang_country = "en_US";
    static T tt = new T();

    public T() {
        String la = OmegaContext.omega_lang;
        OmegaContext.lesson_log.getLogger().info("T lang p is from omega_lang " + la);
        if (la == null) {
            la = System.getProperty("lang");
            OmegaContext.lesson_log.getLogger().info("T lang p is from -Domega_lang " + la);
        }

        OmegaContext.lesson_log.getLogger().info("omega gui lang is now " + la);
        Locale loc;
        if (la != null) {
            lang = before_(la);
            lang_country = la;
        } else {
            loc = Locale.getDefault();
            OmegaContext.sout_log.getLogger().info("ERR: " + "locale " + loc);
            lang = loc.getLanguage();
            lang_country = loc.getLanguage() + '_' + loc.getCountry();
            if ("no".equals(lang)) {
                OmegaContext.lesson_log.getLogger().info("no -> nb " + loc);
                lang = "nb";
                lang_country = "nb_NO";
            }
        }

        OmegaContext.lesson_log.getLogger().info("FINALLY omega lang " + OmegaContext.omega_lang);
        OmegaContext.lesson_log.getLogger().info("FINALLY lang " + lang);
        OmegaContext.lesson_log.getLogger().info("FINALLY lang_country " + lang_country);
    }

    private String before_(String s) {
        int ix = s.indexOf('_');
        if (ix == -1)
            return s;
        return s.substring(0, ix);
    }

    private static FileInputStream fopen(String fn, String fn2, int[] who) {
        who[0] = -1;
        FileInputStream in = null;
        try {
            in = new FileInputStream(fn);
            OmegaContext.sout_log.getLogger().info("ERR: " + "T file is " + fn);
            who[0] = 0;
        } catch (FileNotFoundException ex) {
            try {
                if (fn2 != null) {
                    in = new FileInputStream(fn2);
                    OmegaContext.sout_log.getLogger().info("ERR: " + "T file is " + fn2);
                    who[0] = 1;
                }
            } catch (FileNotFoundException ex2) {
                return null;
            }
        }
        return in;
    }

    static int fillFrom(String fn, String fn2, HashMap hm_) {
        int[] who = new int[1];

        try {
            FileInputStream in = fopen(OmegaContext.t9n(fn), fn2, who);
            if (in == null)
                return -1;
            InputStreamReader ir = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(ir);

            String enc = br.readLine();
            if ("utf-8".equals(enc))
                OmegaContext.sout_log.getLogger().info("ERR: " + "T_ enc is " + fn + ' ' + fn2 + ' ' + enc);
            else
                enc = null;

            in = fopen(fn, fn2, who);
            if (enc == null)
                ir = new InputStreamReader(in);
            else
                ir = new InputStreamReader(in, enc);
            br = new BufferedReader(ir);

            br.readLine();

            try {
                int cnt = 0;
                for (; ; ) {
                    String s = br.readLine();
                    if (s == null) {
                        OmegaContext.sout_log.getLogger().info("ERR: " + "rL: null");
                        break;
                    }
                    String sa[] = s.split("[\\]\\[]+");
                    if (cnt < 10)
                        OmegaContext.sout_log.getLogger().info("ERR: " + "got T_ " + sa.length + ' ' + SundryUtils.a2s(sa));
                    if (sa.length == 3)
                        hm_.put(sa[1], sa[2]);
                    else
                        OmegaContext.sout_log.getLogger().info("ERR: " + "t9n.T.t strange " + SundryUtils.a2s(sa));
                    cnt++;
                }
                br.close();
            } catch (IOException ex) {
            }
        } catch (IOException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "T_ " + ex);
        }
        return who[0];
    }

    static int fillFromXML(String fn, String fn2, HashMap hm_) {
        try {
            int[] who = new int[1];

            FileInputStream in = fopen(OmegaContext.t9n(fn + ".xml"), OmegaContext.t9n(fn2 + ".xml"), who);
            if (in == null)
                return -1;
            InputStreamReader ir = new InputStreamReader(in);

            XMLDecoder d = new XMLDecoder(new BufferedInputStream(in));
            Object result = d.readObject();
            d.close();
            if (!(result instanceof HashMap)) {
                return -1;
            }
            OmegaContext.sout_log.getLogger().info("ERR: " + "T_xml " + ((HashMap) result).size());

            hm_.putAll((HashMap) result);
            return who[0];
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "T_ " + ex);
        }
        return -1;
    }

    static synchronized void init() {
        if (hm == null) {
            hm = new HashMap();
            int whox = fillFromXML("T_" + lang_country, "T_" + lang, hm);
            if (whox == -1) {
                int who = fillFrom("T_" + lang_country, "T_" + lang, hm);
                if (whox == -1 && who != -1)
                    putXML(hm, who == 0 ? "T_" + lang_country : "T_" + lang);
            }
        }
    }

    static private void putEncoding() {
        File f = new File(OmegaContext.t9n("T_new_" + lang_country));
        if (f.exists() && f.length() > 0)
            return;
        PrintWriter pw = SundryUtils.createPrintWriter(OmegaContext.t9n("T_new"));
        pw.println("utf-8");
        pw.close();
    }

    synchronized static private void putXML(HashMap hm, String fn) {
        try {
            XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(OmegaContext.t9n(fn + ".xml"))));
            e.writeObject(hm);
            e.close();
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "" + ex);
        }
    }

    static class MyThread extends Thread {
        public void run() {
            SundryUtils.m_sleep(5000);
            putXML(hm_new, "T_new_" + lang_country);
            mythread = null;
        }
    }

    static MyThread mythread = null;

    public static String t(String s) {
        init();
        if (hm == null)
            return s;

        if (s.length() == 0)
            return s;

        String ss = (String) hm.get(s);
        if (ss == null) {
            if (hm_new == null) {
                hm_new = new HashMap();
                fillFromXML("T_new_" + lang_country, null, hm_new);
            }
            ss = (String) hm_new.get(s);
            if (ss == null) {
                ss = s;
                hm.put(s, ss);
                hm_new.put(s, ss);
                if (mythread == null) {
                    mythread = new MyThread();
                    mythread.start();
                }
// 		PrintWriter pw = SundryUtils.createPrintWriter("T_new", true);
// 		pw.println("[" + s + "][" + s + "]");
// 		pw.flush();
// 		pw.close();
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "T.t " + s);
            }
        } else {
            //	    OmegaContext.sout_log.getLogger().info("ERR: " + "Tt " + s + ' ' + ss);
        }
        return ss;
    }
}
