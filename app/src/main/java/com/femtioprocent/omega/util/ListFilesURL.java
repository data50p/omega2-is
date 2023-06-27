package com.femtioprocent.omega.util;

import com.femtioprocent.omega.OmegaContext;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ListFilesURL {
    public static String[] getMediaList(String n) {
        String name = OmegaContext.getMediaFile(n);
        File dir = new File(name);
        String[] names = dir.list();
// 	OmegaContext.sout_log.getLogger().info("ERR: " + "FILE dir[] name " + name + ' ' + names);
// 	OmegaContext.sout_log.getLogger().info("ERR: " + "FILE dir[] names " + SundryUtils.a2s(names));
        Set li = new HashSet();
        for (int i = 0; i < names.length; i++) {
            li.add(names[i]);
            if (names[i].endsWith(".mp3"))
                li.add(names[i].substring(0, names[i].length() - 4) + ".wav");
        }
        String[] sa = (String[]) li.toArray(new String[li.size()]);

        //	OmegaContext.sout_log.getLogger().info("ERR: " + "FILE dir[] " + dir + ' ' + SundryUtils.a2s(names));
//	return names;

        return sa;
    }


    static public void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            long ct0 = SundryUtils.ct();
            OmegaContext.sout_log.getLogger().info("ERR: " + "" + SundryUtils.a2s(getMediaList("actor")));
            long ct1 = SundryUtils.ct();
            OmegaContext.sout_log.getLogger().info("ERR: " + "" + (ct1 - ct0));
            SundryUtils.m_sleep(200);
            System.gc();
        }
    }
}
