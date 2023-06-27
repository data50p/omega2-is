package com.femtioprocent.omega.xml;

import com.femtioprocent.omega.util.SundryUtils;

import java.io.*;
import java.util.Stack;

public class XML_PW implements AutoCloseable {
    public PrintWriter pw;
    Stack st;
    boolean first_done = false;
    boolean use_dtd = true;

    public XML_PW() {
	st = new Stack();
    }

    public XML_PW(boolean dtd) {
	st = new Stack();
	use_dtd = dtd;
    }

    public XML_PW(OutputStream os) {
	pw = SundryUtils.createPrintWriter(os);
	st = new Stack();
    }

    public XML_PW(PrintWriter pw) {
	this.pw = pw;
	st = new Stack();
    }

    public XML_PW(PrintWriter pw, boolean dtd) {
	this.pw = pw;
	st = new Stack();
	use_dtd = dtd;
    }

    void ensureDTD(String tag) {
	File f = new File(tag + ".dtd");
	if (f.exists())
	    return;
	try {
	    f.createNewFile();
	} catch (IOException ex) {
	}
    }

    void ensurePW(String tag) {
	if (pw == null) {
	    pw = SundryUtils.createPrintWriter(tag + ".xml");
	    try {
	        throw new Exception("trace");
	    } catch (Exception ex) {
		try {
		    try (PrintWriter pw = new PrintWriter(new File("logs/stack_trace"))) {
			ex.printStackTrace(pw);
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    StringBuffer dtd_sb;

    private boolean lastE = true;

    public void setDTD(String s) {
	dtd_sb = new StringBuffer();
	dtd_sb.append(s);
    }

    public void addDTD_E(String tag) {
	addDTD_E(tag, null, null);
    }

    public void addDTD_E(String tag, String def) {
	addDTD_E(tag, def, null);
    }

    public void addDTD_E(String tag, String def, String cmnt) {
	if (!lastE)
	    addDTD("\n");
	addDTD("	<!ELEMENT " +
		tag +
		' ' +
		(def == null ? "EMPTY" : def) +
		">" +
		(cmnt == null ? "\n" : ("  <!-- " + cmnt + " -->\n")));
	lastE = true;
    }

    public void addDTD_A(String tag, String attr) {
	addDTD_A(tag, attr, true, null);
    }

    public void addDTD_A(String tag, String attr, boolean req) {
	addDTD_A(tag, attr, req, null);
    }

    public void addDTD_A(String tag, String attr, boolean req, String cmnt) {
	addDTD("	<!ATTLIST " +
		tag + ' ' +
		attr + ' ' +
		"CDATA " +
		(req ? "#REQUIRED" : "#IMPLIED") +
		">" +
		(cmnt == null ? "\n" : "  <!-- " + cmnt + " -->\n"));
	lastE = false;
    }

    public void addDTD_A(String tag, String attr, String kind, boolean req, String cmnt) {
	addDTD("	<!ATTLIST " +
		tag + ' ' +
		attr + ' ' +
		kind + ' ' +
		(req ? "#REQUIRED" : "#IMPLIED") +
		">" +
		(cmnt == null ? "\n" : "  <!-- " + cmnt + " -->\n"));
	lastE = false;
    }

    public void addDTD_A(String tag, String attr, String[] val, String def) {
	addDTD_A(tag, attr, val, def, null);
    }

    public void addDTD_A(String tag, String attr, String[] val, String def, String cmnt) {
	addDTD("	<!ATTLIST " +
		tag + ' ' +
		attr + ' ' +
		" (");
	for (int i = 0; i < val.length; i++)
	    addDTD((i == 0 ? "" : " | ") + val[i]);
	addDTD(") \"" + def + "\">" +
		(cmnt == null ? "\n" : "  <!-- " + cmnt + " -->\n"));
	lastE = false;
    }

    public void addDTD(String s) {
	if (dtd_sb == null)
	    dtd_sb = new StringBuffer();
	dtd_sb.append(s);
    }

    String getDTD() {
	if (dtd_sb == null)
	    return null;
	return dtd_sb.toString();
    }

    void first(String tag) {
	if (first_done == false) {
	    if (pw == null)
		ensurePW(tag);
	    pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
	    String dtd = getDTD();
	    if (dtd == null)
		if (use_dtd)
		    pw.println("<!DOCTYPE " + tag + " SYSTEM \"" + tag + ".dtd\">");
		else
		    pw.println("<!DOCTYPE " + tag + " >");
	    else
		pw.println("<!DOCTYPE " + tag + "  [\n" + dtd + "]>");
	    pw.println("");
	    if (dtd == null && use_dtd)
		ensureDTD(tag);
	    first_done = true;
	}
    }

    public void push(Element el) {
	first(el.name);
	StringBuffer sbu = new StringBuffer();
	StringBuffer sbl = new StringBuffer();
	el.render(sbu, sbl);
	pw.print(sbu.toString());
	st.push(sbl);
	el.setRO();
    }

    public void put(Element el) {
	push(el);
	pop(el);
    }

    public boolean putDT(String tag) {
	if (first_done)
	    return false;
	first(tag);
	return true;
    }

    public void put(PCDATA pcdata) {
	pw.print(pcdata.getString());
    }

    public void put(String s) {
	pw.print(s);
    }

    public void pop() {
	pop(null);
    }

    public void popAll() {
	while (!st.isEmpty())
	    pop(null);
    }

    public void pop(Element el) {
	Object o = st.pop();
	if (o instanceof StringBuffer) {
	    StringBuffer sb = (StringBuffer) o;
	    pw.print(sb.toString());
	} else {
	    pw.println("<!-- FEL -->");
	}
    }

    public void flush() {
	pw.flush();
    }

    public void close() throws Exception {
	popAll();
	flush();
	pw.close();
    }

    public boolean checkError() {
	return pw.checkError();
    }

    static public void main(String[] args) {
	PrintWriter pw = new PrintWriter(System.out);
	try (XML_PW xmls = new XML_PW(pw)) {

	    Element xn = new Element("el_xn_test");

	    PCDATA pcd = new PCDATA();
	    pcd.add("hello world (xn)");
	    xn.add(pcd);

	    Element xn2 = new Element("empty_xn2_in_xn");
	    xn.add(xn2);

	    xn.add(xn2 = new Element("another_xn2_notempty"));
	    xn2.add(new PCDATA("first text"));

	    xn2 = new Element("emptyA");
	    xn2.addAttr("attr", "value");
	    xn2.addAttr("attr2", "value2");

	    xn.add(xn2 = new Element("notempty_xn2_again"));

	    xn2.addAttr("attr3", "value3");
	    xn2.addAttr("attr4", "value4");
	    xn2.add(new PCDATA("last text"));

	    xn2 = new Element("xn2separate");

	    xn2.addAttr("attr3", "value3");
	    xn2.addAttr("attr4", "value4");
	    xn2.add(new PCDATA("separate text"));


	    xmls.push(xn);
	    xmls.push(xn2);
	    xmls.pop();
	    xmls.push(xn2);
	    xmls.pop();
	    xmls.push(xn2);
	    xmls.pop();
	    xmls.push(xn2);
	    xmls.pop();
	    xmls.push(xn2);
	    xmls.pop();
	    xmls.pop(xn);

	    pw.flush();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}

