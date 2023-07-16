package com.femtioprocent.omega.lesson.helper;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.assets.TargetCombinations;
import com.femtioprocent.omega.anim.tool.path.Path;
import com.femtioprocent.omega.lesson.repository.Restore;
import com.femtioprocent.omega.lesson.repository.Save;
import com.femtioprocent.omega.util.DelimitedStringBuilder;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.xml.Element;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by lars on 2017-05-21.
 */
public class PathHelper {
    Set<TargetCombinations.TCItem> dep_set;

    public PathHelper(Set<TargetCombinations.TCItem> dep_set) {
        this.dep_set = dep_set;
    }

    public void performStatus() {
        perform(false, null);
    }

    public void perform(String doBackup) {
	perform(true, doBackup);
    }

    public void perform(boolean modify, String doBackup) {
	for(TargetCombinations.TCItem tci : dep_set) {
	    try {
		String fname = tci.fn;
		if (!fname.endsWith(".omega_anim")) {
		    Log.getLogger().info("Skip: wrong file name " + fname);
		    continue;
		}
		Log.getLogger().info("Fix path: " + fname);
		Element el = Restore.restore(fname);
		if ( el == null ) {
		    OmegaContext.sout_log.getLogger().info("status: " + (modify ? "upd" : "dry") + " error " + fname);
		    continue;
		}
		String version = el.findAttr("version");
		String clazz = el.findAttr("class");
		if (!"Animation".equals(clazz)) {
		    Log.getLogger().info("Skip: class " + clazz);
		    continue;
		}
		if (false && "0.1".equals(version)) {
		    Log.getLogger().info("Skip: version " + version);
		    continue;
		}
		Log.getLogger().info("Loaded: " + el);
		String status = fixIt(el, modify);
		if ( modify ) {
		    el.addAttr("version", "0.1");
		    if ( doBackup != null )
			Save.saveWithBackup(fname, doBackup, el);
		    else
			Save.save(fname, el);
		    Log.getLogger().info("Saved: " + el);
		}
		OmegaContext.sout_log.getLogger().info("status: " + (modify ? "upd" : "dry") + ' ' + version + ' ' + status + ' ' + fname);
	    } catch (Exception ex) {
		Log.getLogger().info("***Exception: " + ex);
	    }
	}
    }

    private String fixIt(Element el, boolean modify) {
	int cntTpath = 0;
	int cntInfoAdded = 0;
	int cntInfoExist = 0;
	int cntHelpAdded = 0;
	int cntHelpExist = 0;

	Element el_ac = el.findFirstElement("AnimCanvas");
	Element el_ap = el_ac.findFirstElement("AllPath");
	for (int i = 0; i < 10; i++) {
	    Element el_tp = el.findElement("TPath", i);
	    if ( el_tp == null )
	        continue;
	    cntTpath++;
	    Log.getLogger().info("fix TPath " + i + ": " + el_tp.findAttr("nid"));
	    for (int j = 0; j < 100; j++) {
		Element el_q = el.findElement("q", j);
		if ( el_q == null )
		    break;
		Log.getLogger().info("fix q " + i + ": " + el_q.findAttr("ord"));
	    }
	    Path p = new Path(el_tp);
	    double[] lenArr = p.getLenA();
	    Point2D[] point2d = p.getPoint2D();
	    Log.getLogger().info("flatness " + i + ": " + OmegaConfig.FLATNESS);
	    Log.getLogger().info("len " + i + ": " + lenArr.length + ' ' + Path.format(lenArr));
	    Log.getLogger().info("seg " + i + ": " + point2d.length + ' ' + Path.format(point2d));

	    Element el_i = el_tp.findElement("info", 0);
	    if ( el_i == null ) {
		el_i = new Element("info");
		el_tp.add(el_i);
		cntInfoAdded++;
	    } else {
		cntInfoExist++;
	    }
	    el_i.subAttr("len");
	    el_i.subAttr("seg");
	    el_i.addAttr("flatness", "" + OmegaConfig.FLATNESS);
	    el_i.addAttr("size", "" + lenArr.length);

	    Element el_h = el_tp.findElement("help", 0);
	    if ( el_h == null ) {
		el_h = new Element("help");
		el_tp.add(el_h);
		cntHelpAdded++;
	    } else {
	        cntHelpExist++;
	    }
	    el_h.addAttr("len", Path.format(lenArr));
	    el_h.addAttr("seg", Path.format(point2d));
	}
	return "n:" + cntTpath + " +:" + cntInfoAdded + "," + cntHelpAdded + " =:" + cntInfoExist + "," + cntHelpExist;
    }
}
