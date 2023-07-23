package com.femtioprocent.omega

import com.sun.javafx.runtime.VersionInfo
import java.util.*

// DO NO CHANGE HERE
object OmegaVersion {
    @JvmStatic
    val omegaVersion: String
	get() = getOmegaVersion(null)

    fun getOmegaVersion(s: String?): String {
	return if (s != null) s + ' ' + "Ω, version: " + detailedVersion else "Ω, version: " + detailedVersion
    }

    val detailedVersion: String
	get() = version + "; build: " + buildDate
    @JvmStatic
    val version: String
	get() = OmegaVersion["Version"]
    val buildDate: String
	get() = OmegaVersion["Date"]
    @JvmStatic
    val versionBlurb: String
	get() = """
	 	$omegaVersion
	 	CWD: $cWD
	 	Version: java $javaVersion,   javafx ${VersionInfo.getRuntimeVersion()}
	 	Java Vendor: $javaVendor; OS name: ${System.getProperty("os.name").lowercase(Locale.getDefault())}
	 	java home: $javaHome
	 	""".trimIndent()

    operator fun get(item: String?): String {
	return when (item) {
	    "Version" -> "2.1.0"
	    "Date" -> "§§23-07-23_12:06:47 mango.local §§".replace("§".toRegex(), "")
	    else -> "?"
	}
    }

    @JvmStatic
    val cWD: String
	get() = System.getProperty("user.dir")
    @JvmStatic
    val javaHome: String
	get() = System.getProperty("java.home")
    @JvmStatic
    val javaVendor: String
	get() = System.getProperty("java.vendor")
    @JvmStatic
    val javaVersion: String
	get() = System.getProperty("java.version")
}
