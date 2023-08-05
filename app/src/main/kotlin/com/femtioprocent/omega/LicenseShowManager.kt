package com.femtioprocent.omega

import com.femtioprocent.omega.util.PreferenceUtil
import java.util.concurrent.Semaphore

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

    val semaphore = Semaphore(1)

    fun showAndAccepted(): Boolean {
	val pu = PreferenceUtil(LicenseShowManager::class.java)
	val answer = pu.getObject(licShow, no) as String
	if (true && (OmegaContext.isDeveloper || yes == answer)) return true
	val dialog = ShowLicense(semaphore)
	dialog.pack()
	dialog.isVisible = true
	semaphore.acquire()
	if (!dialog.accepted) System.exit(1)
	pu.save(licShow, yes)
	return true
    }
}
