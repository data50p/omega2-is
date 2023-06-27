package com.femtioprocent.omega.lesson.repository;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;
import com.femtioprocent.omega.xml.XML_PW;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Save {
    public static void saveWithBackup(String fname, String extension, Element el) {
        save(fname, el, extension);
    }

    public static void save(String fname, Element el) {
	save(fname, el, null);
    }

    private static void save(String fname, Element el, String extension) {
	List stories = el.find("story");
	if (stories != null && stories.size() > 0) {
	    Element sel = (Element) stories.get(0);
	    String isfirst = sel.findAttr("isfirst");
	    if (isfirst != null) {
		if (fname.contains("active")) {
		    addStoryFileIndicator(fname);
		}
	    }
	}

	if ( extension != null )
	    doBackup(fname, extension);
	try (XML_PW xmlpw = new XML_PW(SundryUtils.createPrintWriterUTF8(fname), false)) {
	    xmlpw.put(el);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static void doBackup(String fname, String extension) {
	File buFile = new File(fname + extension);
	File theFile = new File(fname);

	if ( theFile.exists() && buFile.exists() )
	    buFile.delete();
	if ( theFile.exists() )
	    theFile.renameTo(buFile);
    }

    private static void addStoryFileIndicator(String fname) {
        Log.getLogger().fine("ADD story file");
        File file = new File(OmegaContext.omegaAssets(fname));
        File dir = file.getParentFile().getParentFile();
        File storyFile = new File(dir, "story");
        if ( ! storyFile.exists() ) {
	    try {
		boolean b = storyFile.createNewFile();
		Log.getLogger().info("ADDED story file " + storyFile + ' ' + b);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
