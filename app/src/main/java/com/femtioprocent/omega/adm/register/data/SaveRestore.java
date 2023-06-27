package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;
import com.femtioprocent.omega.xml.XML_PW;

public class SaveRestore {
    public boolean save(String fname, Result rslt) {
        Element el = rslt.getElement();
        OmegaContext.sout_log.getLogger().info("ERR: " + "saving SaveRestore el " + el);
        try (XML_PW xmlpw = new XML_PW(SundryUtils.createPrintWriterUTF8(fname), false)) {
            xmlpw.put(el);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public Result restore(String fname) {
        return null;
    }
}
