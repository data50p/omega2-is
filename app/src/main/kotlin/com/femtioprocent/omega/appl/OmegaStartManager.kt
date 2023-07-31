package com.femtioprocent.omega.appl

import com.femtioprocent.omega.util.PreferenceUtil

/**
 * Created by lars on 2017-03-27.
 */
object OmegaStartManager {
    private const val START_OBJECT = "start"
    private const val SELECTION_ITEM = "selection"

    /**
     * Show start option dialog at next Omega start
     */
    fun enableStarter() {
    }

    @JvmStatic
    fun askForIt(argv: Array<String?>?): Int {
	val ss = Omega_IS()
	ss.args = argv
	ss.pack()
	ss.isVisible = true
	val selection = ss.waitForSelection()
	return selection
    }
}
