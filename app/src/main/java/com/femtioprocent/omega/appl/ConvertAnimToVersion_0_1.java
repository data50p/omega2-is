package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.assets.TargetCombinations;
import com.femtioprocent.omega.lesson.helper.PathHelper;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by lars on 2017-05-22.
 */
public class ConvertAnimToVersion_0_1 {
    static HashMap<String,String> flags;
    static List<String> argl;
    File baseDir = null;
    File theFile = null;

    static double flatness = OmegaConfig.FLATNESS;

    ConvertAnimToVersion_0_1(File fn) {
	if ( fn.isDirectory() ) {
	    this.baseDir = fn;
	} else {
	    this.theFile = fn;
	}
	String flatnessF = (String) flags.get("flatness");
	if (flatnessF != null) {
	    double flatness = Double.valueOf(flatnessF);
	    OmegaConfig.FLATNESS = flatness;
	}
    }

    public static void main(String[] args) {
        if ( args.length == 0 ) {
	    OmegaContext.serr_log.getLogger().info("-b=<backupExt> -d=dir -status -flatness=value example.omega_anim,...");
	    System.exit(99);
	}
	Log.getLogger().info("Started");
	flags = SundryUtils.flagAsMap(args);
	argl = SundryUtils.argAsList(args);
	String baseDir = null;

	String dir = flags.get("d");
	if ( dir != null )
	    baseDir = dir;
	if ( baseDir != null ) {
	    ConvertAnimToVersion_0_1 c01 = new ConvertAnimToVersion_0_1(new File(baseDir));
	    c01.start();
	} else {
	    for (String fn : argl ) {
		ConvertAnimToVersion_0_1 c01 = new ConvertAnimToVersion_0_1(new File(fn));
		c01.start();
	    }
	}
    }

    public void start() {
	Set<TargetCombinations.TCItem> dep_set = new HashSet<>();
	if ( baseDir != null ) {
	    fill(dep_set);
	    keep(dep_set, ".omega_anim");
	} else {
	    TargetCombinations.TCItem tci = createTCI(theFile);
	    dep_set.add(tci);
	}

	PathHelper ph = new PathHelper(dep_set);
	String doBackup = flags.get("b");
	if ( flags.get("status") != null )
	    ph.performStatus();
	else
	    ph.perform(doBackup);
    }

    private void keep(Set<TargetCombinations.TCItem> dep_set, String s) {
	Iterator<TargetCombinations.TCItem> it = dep_set.iterator();
	while (it.hasNext()) {
	    final TargetCombinations.TCItem next = it.next();
	    if ( next.fn.endsWith(s) )
	        continue;
	    it.remove();
	}
    }

    private void fill(Set<TargetCombinations.TCItem> dep_set) {
	fill(dep_set, baseDir);
    }

    private void fill(Set<TargetCombinations.TCItem> dep_set, File dir) {
	final File[] files = dir.listFiles();
	for(File f : files) {
	    if ( f.isDirectory() ) {
	        fill(dep_set, f);
	    } else {
	        if (f.getName().endsWith(".omega_anim") ) {
		    TargetCombinations.TCItem tci = createTCI(f);
		    if ( tci != null )
		        dep_set.add(tci);
		}
	        else
		    Log.getLogger().info("Ignore " + f.getAbsolutePath());
	    }
	}
    }

    private TargetCombinations.TCItem createTCI(File f) {
	try {
	    Log.getLogger().info("convert " + f.getCanonicalPath());
	    TargetCombinations.TCItem tci = new TargetCombinations.TCItem(f.getCanonicalPath());
	    return tci;
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }
}
