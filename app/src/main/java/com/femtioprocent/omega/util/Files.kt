package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.Log.getLogger
import com.femtioprocent.omega.util.SundryUtils.arrToString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.MalformedURLException

object Files {
    // urldir, name
    fun splitUrlString(url_s: String): Array<String?>? {
	val fi = File(omegaAssets("."))
	var cdu: String? = null
	try {
	    cdu = fi.toURI().toURL().toString()
	    cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ")
	} catch (ex: MalformedURLException) {
	    return null
	}
	cdu = cdu.substring(0, cdu.length)
	cdu += "media/"
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: try loading url from\n$url_s\n$cdu")
	val len_cd = cdu.length
	val sa = arrayOfNulls<String>(2)
	val name = url_s.substring(len_cd)
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: +++ $name")
	sa[0] = cdu
	sa[1] = name
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "" + arrToString(sa))
	return sa
    }

    @JvmStatic
    fun mkRelativeCWD(fn: String): String? {
	try {
	    val fi = File(".")
	    var cdu: String? = null
	    cdu = try {
		fi.toURI().toURL().toString()
	    } catch (ex: MalformedURLException) {
		return null
	    }
	    cdu = cdu!!.substring(0, cdu.length)
	    if (cdu.endsWith("./")) cdu = cdu.substring(0, cdu.length - 2) // -1 is to remove "./"
	    OmegaContext.sout_log.getLogger().info("floppy: mkRelativeCWD  $fn -> $cdu")
	    cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ")
	    OmegaContext.sout_log.getLogger().info("floppy: mkRelativeCWD' $fn -> $cdu")
	    val len_cd = cdu.length
	    val sa = arrayOfNulls<String>(2)
	    val name = fn.substring(len_cd)
	    if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: +++ $name")
	    sa[0] = cdu
	    sa[1] = name
	    if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "" + arrToString(sa))
	    return sa[1]
	} catch (ex: StringIndexOutOfBoundsException) {
	}
	return null
    }

    @JvmStatic
    fun mkRelFname1(url_s: String): String? {
	val fi = File(omegaAssets(".")) // ".");
	var cdu: String? = null
	try {
	    cdu = fi.toURI().toURL().toString()
	    cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ")
	} catch (ex: MalformedURLException) {
	    return null
	}
	//cdu = cdu.substring(0, cdu.length() - 1);
//log	OmegaContext.sout_log.getLogger().info(":--: " + "mkRelativeCWD\n" + url_s + '\n' + cdu);
	val len_cd = cdu.length
	val sa = arrayOfNulls<String>(2)
	val name = url_s.substring(len_cd)
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy +++ $url_s")
	sa[0] = cdu
	sa[1] = name
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy " + "=== " + arrToString(sa))
	return sa[1]
    }

    @JvmStatic
    fun mkRelFname(url_s: String): String? {
	val fi = File(omegaAssets(".")) // ".");
	var cdu: String? = null
	try {
	    cdu = fi.toURI().toURL().toString()
	    cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ")
	} catch (ex: MalformedURLException) {
	    return null
	}
	cdu = cdu.substring(0, cdu.length)
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "mkRelativeCWD\n" + url_s + '\n' + cdu);
	val len_cd = cdu.length
	val sa = arrayOfNulls<String>(2)
	val name = url_s.substring(len_cd)
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: +++ $name")
	sa[0] = cdu
	sa[1] = name
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy: " + "" + arrToString(sa))
	return sa[1]
    }

    @JvmStatic
    fun mkRelFnameAlt(url_s: String, prefix: String): String? {
	val fi = File(omegaAssets(prefix))
	var cdu: String? = null
	try {
	    cdu = fi.toURI().toURL().toString()
	    cdu = cdu.replace("%20", " ").replace("%20", " ").replace("%20", " ").replace("%20", " ")
	} catch (ex: MalformedURLException) {
	    return null
	}
	val len_cd = cdu.length
	val sa = arrayOfNulls<String>(2)
	val name = url_s.substring(len_cd)
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy +++ $url_s $prefix")
	sa[0] = cdu
	sa[1] = name
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info("floppy === " + arrToString(sa))
	return sa[1]
    }

    /*
WARNING   29/05 09:27:32.391     0 Files                   toURL                  URL matter:      C:\Users\Mats L\Documents\Omega-IS\default.omega_assets\lesson-sv\active\F-b_3-ord\F-b1\SpraySoapDry.omega_lesson
WARNING   29/05 09:27:32.391     0 Files                   toURL                      matter: new file:/C:/Users/Mats%20L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson
WARNING   29/05 09:27:32.391     0 Files                   toURL                      matter: old file:/C:/Users/Mats L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson
WARNING   29/05 09:27:32.391     0 Files                   toURL                      matter: alt file:/C:/Users/Mats L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson
INFO      29/05 09:27:32.391     0 Files                   mkRelativeCWD          :--: mkRelativeCWD file:/C:/Users/Mats L/Documents/Omega-IS/default.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson -> file:/C:/Users/Mats%20L/Documents/Omega-IS/
INFO      29/05 09:27:32.391     0 Files                   mkRelativeCWD          :--: +++ fault.omega_assets/lesson-sv/active/F-b_3-ord/F-b1/SpraySoapDry.omega_lesson

     */
    @JvmStatic
    fun toURL(file: File): String? {
	var url_s: String? = null
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "got file " + file);
	try {
	    val url = file.toURI().toURL()
	    val url0 = "file:" + slashify(file.absolutePath, file.isDirectory)
	    getLogger().warning("URL matter:      $file")
	    getLogger().warning("    matter: new $url")
	    getLogger().warning("    matter: alt $url0")
	    url_s = url0 //url2.toString();
	} catch (ex: Exception) {
	    OmegaContext.exc_log.getLogger().throwing(Files::class.java.name, "toURL", ex)
	}
	return url_s
    }

    private fun slashify(path: String, isDirectory: Boolean): String {
	var p = path
	if (File.separatorChar != '/') p = p.replace(File.separatorChar, '/')
	if (!p.startsWith("/")) p = "/$p"
	if (!p.endsWith("/") && isDirectory) p = "$p/"
	return p
    }

    @JvmStatic
    fun fileCopy(from: File?, to: File?) {
	try {
	    val fr = FileInputStream(from)
	    val fw = FileOutputStream(to)
	    val buf = ByteArray(10240)
	    while (true) {
		val n = fr.read(buf)
		if (n > 0) {
		    fw.write(buf, 0, n)
		} else {
		    break
		}
	    }
	    fr.close()
	    fw.close()
	} catch (edx: Exception) {
	}
    }
}
