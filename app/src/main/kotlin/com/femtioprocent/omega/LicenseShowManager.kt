package com.femtioprocent.omega

import com.femtioprocent.omega.util.PreferenceUtil

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * @author lars
 */
object LicenseShowManager {
    private const val licShow = "licShow"
    private const val yes = "yes"
    private const val no = "no"

    fun showAndAccepted(): Boolean {
	val pu = PreferenceUtil(LicenseShowManager::class.java)
	val answer = pu.getObject(licShow, no) as String
	if (true && (OmegaContext.isDeveloper || yes == answer)) return true
	val dialog = ShowLicense()
	dialog.pack()
	dialog.isVisible = true
	while (dialog.accepted == null) try {
	    Thread.sleep(200)
	} catch (e: InterruptedException) {
	    e.printStackTrace()
	}
	if (!dialog.accepted) System.exit(1)
	pu.save(licShow, yes)
	return true
    }
}
