package com.femtioprocent.omega.appl;

import com.femtioprocent.omega.util.PreferenceUtil;

import java.util.HashMap;

/**
 * Created by lars on 2017-03-27.
 */
public class OmegaStartManager {
    private static final String START_OBJECT = "start";
    private static final String SELECTION_ITEM = "selection";

    private static PreferenceUtil pu = new PreferenceUtil(OmegaStartManager.class);

    public static Integer fromAutoStart() {
	return null;
    }

    /**
     * Show start option dialog at next Omega start
     */
    public static void enableStarter() {
	HashMap start_object = getStartObject();
	start_object.put(SELECTION_ITEM, 0);
	putStartObject(start_object);
    }

    public static void nextStart(int selection) {
	HashMap start_object = getStartObject();
	start_object.put(SELECTION_ITEM, selection);
	putStartObject(start_object);
    }

    public static void savePref(int selection) {
	HashMap start_object = getStartObject();
	start_object.put(SELECTION_ITEM, selection);
	putStartObject(start_object);
    }

    private static HashMap getStartObject() {
	return (HashMap) pu.getObject(START_OBJECT, new HashMap());
    }

    private static void putStartObject(HashMap start_object) {
	pu.save(START_OBJECT, start_object);
    }

    public static int fromPU(String[] argv, Integer selection) {
	HashMap start_object = getStartObject();
	Integer setting_selection = (Integer) start_object.get(SELECTION_ITEM);
	setting_selection = 0; // I ignore the stored selection
	if (setting_selection != null && setting_selection > 0) {
	    selection = setting_selection;
	    return selection;
	} else {
	    Omega_IS ss = new Omega_IS();
	    ss.args = argv;
	    ss.pack();
	    ss.setVisible(true);
	    selection = ss.waitForSelection();
	    return selection;
	}
    }
}
