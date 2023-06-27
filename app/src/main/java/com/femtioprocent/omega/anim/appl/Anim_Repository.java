package com.femtioprocent.omega.anim.appl;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.swing.filechooser.ChooseAnimatorFile;
import com.femtioprocent.omega.swing.filechooser.ChooseImageFile;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;
import com.femtioprocent.omega.xml.SAX_node;
import com.femtioprocent.omega.xml.XML_PW;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;


public class Anim_Repository {
    private String saved_file = null;

    public Anim_Repository() {
    }

    void clearName() {
	saved_file = null;
    }

    String getName() {
	return saved_file;
    }

    void setName(String fn) {
	saved_file = fn;
    }


    String toURL(File file) {
	return Files.toURL(file);
    }

    public String getNameDlg(Component c, boolean ask, String label) {
	String fn = getName();
	try {
	    if (ask || fn == null) {
		String url_s = null;
		ChooseAnimatorFile choose_af = new ChooseAnimatorFile();
		int rv = choose_af.showDialog(c, label);
		if (rv == JFileChooser.APPROVE_OPTION) {
		    File file = choose_af.getSelectedFile();
		    url_s = toURL(file);
		    if (url_s.startsWith("file:"))
			fn = url_s.substring(5);
		    else
			fn = url_s;
		    if (!fn.endsWith("." + ChooseAnimatorFile.ext))
			fn = fn + "." + ChooseAnimatorFile.ext;
		} else
		    return null;
	    }
	} catch (Exception ex) {
	    OmegaContext.exc_log.getLogger().throwing(this.getClass().getName(), "getName", ex);
	    return null;
	}
	if (fn.startsWith("/")) {
	    File file = new File(fn);
	    String url_s = Files.toURL(file);
	    String fnr = Files.mkRelFnameAlt(url_s, OmegaContext.omegaAssets("."));
	    fn = fnr;
	}
	return fn;
    }

    void save(AnimContext a_ctxt, String fn, boolean ask) {
	if (fn == null) {
	    JOptionPane.showMessageDialog(AnimContext.top_frame,
		    T.t("Invalid filename.") + "\n" +
			    T.t("Current data NOT saved to file."),
		    "Omega",
		    JOptionPane.INFORMATION_MESSAGE);
	    return;
	}

	Element el = new Element("omega");
	el.addAttr("class", "Animation");
	el.addAttr("version", "0.1");

	a_ctxt.fillElement(el);

	StringBuffer sbu = new StringBuffer();
	StringBuffer sbl = new StringBuffer();
	el.render(sbu, sbl);

//log	OmegaContext.sout_log.getLogger().info("ERR: " + "EEEEEE " + sbu + ' ' + sbl);

	PrintWriter ppw = SundryUtils.createPrintWriterUTF8("SAVED-omega_anim.dump");
	ppw.println("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n" +
		"<!DOCTYPE omega >\n\n" +
		sbu + ' ' + sbl);
	ppw.close();
	ppw = null;

	if (sbu.length() == 0 || sbl.length() == 0) {
	    fn += "_DUMP";
	    JOptionPane.showMessageDialog(AnimContext.top_frame,
		    "Can't get data to save.\n" +
			    "Current data saved to file: " + fn,
		    "Omega",
		    JOptionPane.INFORMATION_MESSAGE);
	} else {
	    boolean err = false;

	    try (XML_PW xmlpw = new XML_PW(SundryUtils.createPrintWriterUTF8(OmegaContext.omegaAssets(fn + ".tmp")), false)) {
		xmlpw.put(el);
		xmlpw.popAll();
		xmlpw.flush();
		if (xmlpw.pw.checkError()) {
		    JOptionPane.showMessageDialog(AnimContext.top_frame,
			    T.t("FATAL IO ERROR 1!") + "\n" +
				    T.t("Nothing saved") + " (" + fn +
				    ")",
			    "Omega",
			    JOptionPane.INFORMATION_MESSAGE);
		    err = true;
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }

	    if (err == false) {
		File file = new File(OmegaContext.omegaAssets(fn));
		File filet = new File(OmegaContext.omegaAssets(fn + ".tmp"));
//log		OmegaContext.sout_log.getLogger().info("ERR: " + "SAVED " + file + ' ' + filet);
		if (file.exists()) {
		    File filep = new File(OmegaContext.omegaAssets(fn + ".prev"));
		    if (filep.exists()) {
			File filepp = new File(OmegaContext.omegaAssets(fn + ".prevprev"));
			if (filepp.exists()) {
			    filepp.delete();
			}
			filep.renameTo(filepp);
			filepp = null;
			System.gc();
		    }
		    file.renameTo(filep);
		    filep = null;
		    System.gc();
		}
		filet.renameTo(file);
		file = null;
		System.gc();
	    }
	    saved_file = fn;
	}
    }

    public Element open(AnimContext a_ctxt, String fn) {
	try {
//log	    OmegaContext.sout_log.getLogger().info("ERR: " + "** PARSING " + fn);
	    Element el = SAX_node.parse(fn, false);
//  	if ( el == null ) {
//  	    JOptionPane.showMessageDialog(null, // a_ctxt.
//  					  T.t("Can't open file ") + fn);
//  	}
	    return el;
	} catch (Exception ex) {
	}
	return null;
    }

    public Element load(AnimContext a_ctxt, Element el) {
	if (el == null)
	    return null;

	a_ctxt.anim_canvas.load(el);

	Element mel = el.findElement("MTL", 0);
	a_ctxt.mtl.load(mel);

	return el;
    }

    void loadAct(AnimContext a_ctxt, String fn) {
	Element el = SAX_node.parse(fn, false);

	Element f_el_an = el.findElement("Anim", 0);
	String fn_an = f_el_an.findAttr("ref");
	Element el_an = SAX_node.parse("anim/" + fn_an, false);

	a_ctxt.anim_canvas.load(el_an);

	Element mel = el_an.findElement("MTL", 0);
	a_ctxt.mtl.load(mel);
    }

    public String getImageURL_Dlg(Component c) {
	String url_s = null;
	ChooseImageFile choose_if = new ChooseImageFile();
	int rv = choose_if.showDialog(c, T.t("Select"));
	if (rv == JFileChooser.APPROVE_OPTION) {
	    File file = choose_if.getSelectedFile();
//log	    OmegaContext.sout_log.getLogger().info("ERR: " + "got file " + file);
	    url_s = Files.toURL(file);
	    return url_s;
	}
	return null;
    }
}

