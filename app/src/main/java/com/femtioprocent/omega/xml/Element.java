package com.femtioprocent.omega.xml;

import java.util.*;

public class Element extends Node implements Cloneable {
    String name;
    SortedMap attr;
    List<Node> content;
    boolean hasSubElement = false;
    int nx_ix;
    boolean ro;

    public Element(String name) {
        this.name = name;
        attr = new TreeMap();
        content = new ArrayList();
    }

    public Element(String name, Node xn) {
        this(name);
        add(xn);
    }

    public Element(String name, String s) {
        this(name);
        add(s);
    }

    public void setRO() {
        ro = true;
    }

    // merge >1 PCNODE

    public void add(Node xn) {
        if (ro)
            throw new RuntimeException("Element readonly");

        if (xn instanceof Element)
            hasSubElement = true;
        content.add(xn);
    }

    public void add(String s) {
        if (ro)
            throw new RuntimeException("Element readonly");

        content.add(new PCDATA(s));
    }

    public void addAttr(String key, String val) {
        if (ro)
            throw new RuntimeException("Element readonly");

        attr.put(key, val);
    }

    public void subAttr(String key) {
        if (ro)
            throw new RuntimeException("Element readonly");

        attr.remove(key);
    }

    public synchronized void addAttr_nx(String key, String val) {
        if (ro)
            throw new RuntimeException("Element readonly");

        attr.put("n" + nx_ix++, key + '=' + val);
    }

    public synchronized void addAttr_nx(Hashtable ht) {
        if (ro)
            throw new RuntimeException("Element readonly");

        Iterator it = ht.keySet().iterator();
        while (it.hasNext()) {
            String k = (String) it.next();
            String v = (String) ht.get(k);
            attr.put("n" + nx_ix++, k + '=' + v);
        }
    }

    public List find(String nm) {
        List li = new ArrayList();
        Iterator it = content.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            if (n instanceof Element) {
                Element e = (Element) n;
                if (e.name.equals(nm)) {
                    li.add(e);
                } else {
                    List li1 = e.find(nm);
                    if (li1.size() > 0)
                        li.addAll(li1);
                }
            }
        }
        return li;
    }

    private boolean isIn(String s, String[] sa) {
        for (int i = 0; i < sa.length; i++)
            if (s.equals(sa[i]))
                return true;
        return false;
    }

    public Element remove(String nm) { // <Element>
        if (name.equals(nm))
            return null;

        List nv = new ArrayList();
        Iterator it = content.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            if (n instanceof Element) {
                Element e = (Element) n;
                if (e.name.equals(nm)) {
                    ;
                } else {
                    e = e.remove(nm);
                    nv.add(e);
                }
            } else {
                nv.add(n);
            }
        }
        try {
            Element cel = (Element) clone();
            cel.content = nv;
            return cel;
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }

    public Element remove(String[] names) {
        if (isIn(name, names))
            return null;

        List nv = new ArrayList();
        Iterator it = content.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            if (n instanceof Element) {
                Element e = (Element) n;
                if (isIn(e.name, names)) {
                    ;
                } else {
                    e = e.remove(names);
                    nv.add(e);
                }
            } else {
                nv.add(n);
            }
        }
        try {
            Element cel = (Element) clone();
            cel.content = nv;
            return cel;
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }


//      public void sortByAttrib(final String attrib) {
//  	Object oao[] = content.toArray();
//  	Object oa[] = (Object[])oao.clone();
//  	Arrays.sort(oa, new Comparator() {
//  	    public int compare(Object o1, Object o2) {
//  		Element e1 = (Element)o1;
//  		Element e2 = (Element)o2;
//  		String s1;
//  		String s2;
//  		s1 = (String)e1.attr.get(attrib);
//  		s2 = (String)e2.attr.get(attrib);
//  		return s1.compareTo(s2);
//  	    }
//  	});
//  	content = Arrays.asList(oa);
//      }

//      public void sortByContent() {
//  	Object oao[] = content.toArray();
//  	Object oa[] = (Object[])oao.clone();
//  	Arrays.sort(oa, new Comparator() {
//  		// optimize by putting SB in hashmap
//  	    public int compare(Object o1, Object o2) {
//  		Node e1 = (Node)o1;
//  		Node e2 = (Node)o2;
//  		String s1;
//  		String s2;
//  		StringBuffer sb1 = new StringBuffer();
//  		StringBuffer sb2 = new StringBuffer();
//  		e1.render(sb1);
//  		e2.render(sb2);
//  		s1 = sb1.toString();
//  		s2 = sb2.toString();
//  		return s1.compareTo(s2);
//  	    }
//  	});
//  	content = Arrays.asList(oa);
//      }

    public Element findFirstElement(String nm) {
        return findElement(nm, 0);
    }

    public Element findElement(String nm, int ix) {
        Iterator it = content.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            if (n instanceof Element) {
                Element e = (Element) n;
                if (e.name.equals(nm)) {
                    if (ix == 0)
                        return e;
                    else
                        ix--;
                } else {
                    Element el1 = e.findElement(nm, ix);
                    if (el1 != null)
                        return el1;
                }
            }
        }
        return null;
    }

    public Element findElement2(String nm) { // nm = name/name2[nix]/name3[@attr]
        return null;
    }

    public String findAttr(String an) {
        return (String) attr.get(an);
    }

    public String findPCDATA() {
        Iterator it = content.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            if (n instanceof PCDATA) {
                PCDATA e = (PCDATA) n;
                return e.pcdata.toString();
            }
        }
        return null;
    }

    String encode(String s) {
        if (s == null)
            return "";
        if (s.indexOf('<') == -1 && s.indexOf('&') == -1 && s.indexOf('"') == -1)
            return s;
        StringBuffer sb = new StringBuffer();
        int l = s.length();
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            if (ch == '&')
                sb.append("&#x26;");
            else if (ch == '<')
                sb.append("&#x3C;");
            else if (ch == '"')
                sb.append("&#x22;");
            else
                sb.append(ch);
        }
        return sb.toString();
    }

    boolean lastWasStartElement = false;

    public void render(StringBuffer sbu, StringBuffer sbl) {
        try {
            sbu.append("<" + name);
            if (!attr.isEmpty()) {
                Iterator it = attr.keySet().iterator();
                while (it.hasNext()) {
                    String s = (String) it.next();
                    sbu.append(" ");
                    sbu.append(s);
                    sbu.append("=\"");
                    sbu.append(encode((String) attr.get(s)));
                    sbu.append("\"");
                }
            }
            if (sbl == null && content.isEmpty())
                sbu.append("/>\n");
            else {
                sbu.append(">");

                if (hasSubElement)
                    sbu.append("\n");

                Iterator it = content.iterator();
                while (it.hasNext()) {
                    Node xn = (Node) it.next();
                    xn.render(sbu);
                }

                if (sbl == null)
                    sbu.append("</" + name + ">\n");
                else
                    sbl.append("</" + name + ">\n");
            }
        } catch (NullPointerException ex) {
        }
    }

    public String toString() {
        return "Element{" + name + "}";
    }

}
