package com.femtioprocent.omega.anim.appl

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.swing.filechooser.ChooseAnimatorFile
import com.femtioprocent.omega.swing.filechooser.ChooseImageFile
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.createPrintWriterUTF8
import com.femtioprocent.omega.xml.Element
import com.femtioprocent.omega.xml.SAX_node
import com.femtioprocent.omega.xml.XML_PW
import java.awt.Component
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

class Anim_Repository {
    var name: String? = null
    fun clearName() {
	name = null
    }

    fun toURL(file: File?): String? {
	return Files.toURL(file!!)
    }

    fun getNameDlg(c: Component?, ask: Boolean, label: String?): String? {
	var fn = name
	try {
	    if (ask || fn == null) {
		var url_s: String? = null
		val choose_af = ChooseAnimatorFile()
		val rv = choose_af.showDialog(c, label)
		if (rv == JFileChooser.APPROVE_OPTION) {
		    val file = choose_af.selectedFile
		    url_s = toURL(file)
		    fn = if (url_s!!.startsWith("file:")) url_s.substring(5) else url_s
		    if (!fn.endsWith("." + ChooseAnimatorFile.ext)) fn = fn + "." + ChooseAnimatorFile.ext
		} else return null
	    }
	} catch (ex: Exception) {
	    Log.getLogger().throwing(this.javaClass.name, "getName", ex)
	    return null
	}
	if (fn.startsWith("/")) {
	    val file = File(fn)
	    val url_s = Files.toURL(file)
	    val fnr = Files.mkRelFnameAlt(url_s!!, omegaAssets(".")!!)
	    fn = fnr
	}
	return fn
    }

    fun save(a_ctxt: AnimContext?, fn: String?, ask: Boolean) {
	var fn = fn
	if (fn == null) {
	    JOptionPane.showMessageDialog(
		AnimContext.top_frame,
		"""
			${t("Invalid filename.")}
			${t("Current data NOT saved to file.")}
			""".trimIndent(),
		"Omega",
		JOptionPane.INFORMATION_MESSAGE
	    )
	    return
	}
	val el = Element("omega")
	el.addAttr("class", "Animation")
	el.addAttr("version", "0.1")
	a_ctxt!!.fillElement(el)
	val sbu = StringBuffer()
	val sbl = StringBuffer()
	el.render(sbu, sbl)

//log	OmegaContext.sout_log.getLogger().info(":--: " + "EEEEEE " + sbu + ' ' + sbl);
	var ppw = createPrintWriterUTF8("SAVED-omega_anim.dump")
	ppw!!.println(
	    """<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<!DOCTYPE omega >

$sbu $sbl"""
	)
	ppw.close()
	ppw = null
	if (sbu.length == 0 || sbl.length == 0) {
	    fn += "_DUMP"
	    JOptionPane.showMessageDialog(
		AnimContext.top_frame,
		"""
			Can't get data to save.
			Current data saved to file: $fn
			""".trimIndent(),
		"Omega",
		JOptionPane.INFORMATION_MESSAGE
	    )
	} else {
	    var err = false
	    try {
		XML_PW(createPrintWriterUTF8(omegaAssets("$fn.tmp")), false).use { xmlpw ->
		    xmlpw.put(el)
		    xmlpw.popAll()
		    xmlpw.flush()
		    if (xmlpw.pw.checkError()) {
			JOptionPane.showMessageDialog(
			    AnimContext.top_frame,
			    """${t("FATAL IO ERROR 1!")}
${t("Nothing saved")} ($fn)""",
			    "Omega",
			    JOptionPane.INFORMATION_MESSAGE
			)
			err = true
		    }
		}
	    } catch (ex: Exception) {
		ex.printStackTrace()
	    }
	    if (err == false) {
		var file: File? = File(omegaAssets(fn))
		val filet = File(omegaAssets("$fn.tmp"))
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "SAVED " + file + ' ' + filet);
		if (file!!.exists()) {
		    var filep: File? = File(omegaAssets("$fn.prev"))
		    if (filep!!.exists()) {
			var filepp: File? = File(omegaAssets("$fn.prevprev"))
			if (filepp!!.exists()) {
			    filepp.delete()
			}
			filep.renameTo(filepp)
			filepp = null
			System.gc()
		    }
		    file.renameTo(filep)
		    filep = null
		    System.gc()
		}
		filet.renameTo(file)
		file = null
		System.gc()
	    }
	    name = fn
	}
    }

    fun open(a_ctxt: AnimContext?, fn: String?): Element? {
	try {
//log	    OmegaContext.sout_log.getLogger().info(":--: " + "** PARSING " + fn);
	    //  	if ( el == null ) {
//  	    JOptionPane.showMessageDialog(null, // a_ctxt.
//  					  T.t("Can't open file ") + fn);
//  	}
	    return SAX_node.parse(fn, false)
	} catch (ex: Exception) {
	}
	return null
    }

    fun load(a_ctxt: AnimContext?, el: Element?): Element? {
	if (el == null) return null
	a_ctxt!!.anim_canvas!!.load(el)
	val mel = el.findElement("MTL", 0)
	a_ctxt.mtl!!.load(mel)
	return el
    }

    fun loadAct(a_ctxt: AnimContext, fn: String?) {
	val el = SAX_node.parse(fn, false)
	val f_el_an = el.findElement("Anim", 0)
	val fn_an = f_el_an.findAttr("ref")
	val el_an = SAX_node.parse("anim/$fn_an", false)
	a_ctxt.anim_canvas!!.load(el_an)
	val mel = el_an.findElement("MTL", 0)
	a_ctxt.mtl!!.load(mel)
    }

    fun getImageURL_Dlg(c: Component?): String? {
	var url_s: String? = null
	val choose_if = ChooseImageFile()
	val rv = choose_if.showDialog(c, t("Select"))
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_if.selectedFile
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "got file " + file);
	    url_s = Files.toURL(file)
	    return url_s
	}
	return null
    }
}
