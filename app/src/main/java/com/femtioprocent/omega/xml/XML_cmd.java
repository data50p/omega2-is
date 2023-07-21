package com.femtioprocent.omega.xml;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.util.SundryUtils;

import java.io.IOException;
import java.util.*;

// file cmd args...

// extract tag

public class XML_cmd {
    public static void main(String argv[]) throws IOException {
	HashMap<String,String> flag = SundryUtils.flagAsMap(argv);
	List<String> argl = SundryUtils.argAsList(argv);

	if (argl.size() == 0 || ((String) argl.get(0)).equals("help")) {
	    OmegaContext.sout_log.getLogger().info("xmlfile extract element");
	    OmegaContext.sout_log.getLogger().info("xmlfile remove  element[,element2]*");
	    OmegaContext.sout_log.getLogger().info("xmlfile sort    element");
	    OmegaContext.sout_log.getLogger().info("xmlfile sort    element attr");
	    return;
	}

	OmegaContext.sout_log.getLogger().info(":--: " + "argl " + argl + ' ' + flag);

	String file = (String) argl.get(0);
	String cmd = (String) argl.get(1);

	boolean validating = true;
	if (flag.get("n") != null)
	    validating = false;

	Element el = SAX_node.parse(file, validating);

	if (cmd.equals("extract")) {
	    String tag = (String) argl.get(2);

	    List<Element> li = el.find(tag);

	    Element eel = new Element("extract");
	    eel.addAttr("found_tag", tag);
	    eel.addAttr("found_items", "" + li.size());

	    try (XML_PW xmlpw = new XML_PW(System.out)) {
		xmlpw.push(eel);
		xmlpw.put("\n");

		Iterator<Element> it = li.iterator();
		while (it.hasNext()) {
		    Element ell = (Element) it.next();
		    xmlpw.put(ell);
		}

		xmlpw.pop();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	if (cmd.equals("insert")) {
	}

	if (cmd.equals("remove")) {
	    String tag = (String) argl.get(2);
	    String[] tags = SundryUtils.split(tag, ",");

	    Element rel = el.remove(tags);

	    Element eel = new Element("remove");
	    eel.addAttr("removed_tag", tag);

	    try (XML_PW xmlpw = new XML_PW(System.out)) {

		xmlpw.push(eel);
		xmlpw.put("\n");

		xmlpw.put(rel);

		xmlpw.pop();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	// sort findElement sortElement
	// sort findElement sortElement sortAttrib
	if (cmd.equals("--sort")) {
	}

	// sort findElement sortAttr
	if (cmd.equals("sort")) {
	    String tag = (String) argl.get(2);
	    String a = null;
	    if (argl.size() > 3)
		a = (String) argl.get(3);
	    final String fa = a;
	    List<Element> li = el.find(tag);

	    Element oa[] = li.toArray(new Element[0]);
	    Arrays.sort(oa, new Comparator<Element>() {
		public int compare(Element o1, Element o2) {
		    Element e1 = (Element) o1;
		    Element e2 = (Element) o2;
		    String s1;
		    String s2;
		    if (fa != null) {
			s1 = (String) e1.attr.get(fa);
			s2 = (String) e2.attr.get(fa);
		    } else {
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			e1.render(sb1);
			e2.render(sb2);
			s1 = sb1.toString();
			s2 = sb2.toString();
		    }
		    return s1.compareTo(s2);
//  		    int i1 = Integer.parseInt(s1);
//  		    int i2 = Integer.parseInt(s2);
//  		    return i1 - i2; // s1.compareTo(s2);
		}
	    });
	    List<Element> sli = Arrays.asList(oa);

	    Element eel = new Element("sort");
	    eel.addAttr("sorted_tag", tag);
	    eel.addAttr("sorted_attr", a);
	    eel.addAttr("sorted_items", "" + sli.size());

	    try (XML_PW xmlpw = new XML_PW(System.out)) {
		xmlpw.push(eel);
		xmlpw.put("\n");

		Iterator<Element> it = sli.iterator();
		while (it.hasNext()) {
		    Element ell = (Element) it.next();
		    xmlpw.put(ell);
		}

		xmlpw.pop();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }
}
