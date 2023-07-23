package com.femtioprocent.omega.appl

import com.femtioprocent.omega.util.PreferenceUtil

/**
 * Created by lars on 2017-03-27.
 */
object OmegaStartManager {
    private const val START_OBJECT = "start"
    private const val SELECTION_ITEM = "selection"
    private val pu = PreferenceUtil(OmegaStartManager::class.java)
    @JvmStatic
    fun fromAutoStart(): Int? {
	return null
    }

    /**
     * Show start option dialog at next Omega start
     */
    @JvmStatic
    fun enableStarter() {
	val start_object = startObject
	start_object[SELECTION_ITEM] = 0
	putStartObject(start_object)
    }

    fun nextStart(selection: Int) {
	val start_object = startObject
	start_object[SELECTION_ITEM] = selection
	putStartObject(start_object)
    }

    fun savePref(selection: Int) {
	val start_object = startObject
	start_object[SELECTION_ITEM] = selection
	putStartObject(start_object)
    }

    private val startObject: HashMap<String, Int>
	private get() = pu.getObject(START_OBJECT, HashMap<String, Int>()) as HashMap<String, Int>

    private fun putStartObject(start_object: HashMap<String, Int>) {
	pu.save(START_OBJECT, start_object)
    }

    @JvmStatic
    fun fromPU(argv: Array<String?>?, selection: Int?): Int {
	var selection = selection
	val start_object = startObject
	var setting_selection = start_object[SELECTION_ITEM]
	setting_selection = 0 // I ignore the stored selection
	return if (setting_selection != null && setting_selection > 0) {
	    selection = setting_selection
	    selection
	} else {
	    val ss = Omega_IS()
	    ss.args = argv
	    ss.pack()
	    ss.isVisible = true
	    selection = ss.waitForSelection()
	    selection
	}
    }
}
