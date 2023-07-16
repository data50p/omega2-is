package com.femtioprocent.omega.lesson.repository;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.xml.Element;
import com.femtioprocent.omega.xml.SAX_node;

public class Restore {
    public static Element restore(String fname) {
        try {
            String aName;
            if (fname.startsWith(OmegaContext.omegaAssets(""))) {
                Log.getLogger().info("Already assets: " + fname);
                aName = fname;
            } else {
                aName = OmegaContext.omegaAssets(fname);
            }
            OmegaContext.sout_log.getLogger().info(":--: " + "Restore from (~A) " + fname + " -> (A) " + aName);
            Element el = SAX_node.parse(aName, false);
            return el;
        } catch (Exception ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "Exception! Restore.restore(): " + ex);
            return null;
        }
    }
}
