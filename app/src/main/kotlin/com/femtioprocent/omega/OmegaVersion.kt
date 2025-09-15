package com.femtioprocent.omega

import com.sun.javafx.runtime.VersionInfo
import java.util.*

// DO NO CHANGE HERE
object OmegaVersion {
    val theOmegaVersion: String
	get() = "Ω, version: " + detailedVersion
    val theLangVersion: String
	get() = "Versions: java $javaVersion,   kotlin: ${KotlinVersion.CURRENT},    javafx ${VersionInfo.getRuntimeVersion()}"
    val theVendorVersion: String
	get() = "Java Vendor: $javaVendor; OS name: ${System.getProperty("os.name").lowercase(Locale.getDefault())}"
    val theJavaHome: String
	get() = "Java home: $javaHome"
    val theCWD: String
	get() = "CWD: $cwd"

    val the_OmegaVersion: String
	get() = "" + detailedVersion
    val the_LangVersion: String
	get() = "java $javaVersion,   kotlin: ${KotlinVersion.CURRENT},    javafx ${VersionInfo.getRuntimeVersion()}"
    val the_VendorVersion: String
	get() = "$javaVendor; OS name: ${System.getProperty("os.name").lowercase(Locale.getDefault())}"
    val the_JavaHome: String
	get() = "$javaHome"
    val the_CWD: String
	get() = "$cwd"

    private val cwd: String
	get() = System.getProperty("user.dir")
    private val javaHome: String
	get() = System.getProperty("java.home")
    private val javaVendor: String
	get() = System.getProperty("java.vendor")
    private val javaVersion: String
	get() = System.getProperty("java.version")
    private val detailedVersion: String
	get() = version + "; build: " + buildDate
    private val version: String
	get() = OmegaVersion["Version"]
    private val buildDate: String
	get() = OmegaVersion["Date"]

    @JvmStatic
    val versionBlurb: String
	get() = """
	 	$theOmegaVersion
	 	$theCWD
	 	$theLangVersion
	 	$theVendorVersion
	 	$theJavaHome
	 	""".trimIndent()

    private operator fun get(item: String): String {
	return when (item) {
	    "Version" -> "2.1.1"
	    "Date" -> "§§25-09-15_14:09:21 mango.local§§".replace("§".toRegex(), "")
	    else -> "?"
	}
    }
}
