package com.femtioprocent.omega.adm.register.data;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.xml.Element;
import com.femtioprocent.omega.xml.SAX_node;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ResultTest extends Result {
    String type;
    String l_id;
    Date created;
    String pupil;
    public long session_length = 0;

    List<Entry> entries;

    Date first_perform_date;

    public ResultTest(String pupil, String l_id, String type) {
        this.pupil = pupil;
        this.l_id = l_id;
        this.type = type;
        created = new Date();

        entries = new ArrayList<>();

        first_perform_date = new Date();
    }

    public ResultTest(String pupil, String l_id, String type, String fname) {
        this.pupil = pupil;
        this.l_id = l_id;
        this.type = type;
        entries = new ArrayList<>();

        first_perform_date = new Date();

        load(fname);
    }

    void load(String fname) {
        Element el = SAX_node.parse(fname, false);
        if (el != null) {
            Element ent = el.findElement("entries", 0);
            if (ent != null) {
                String s = ent.findAttr("session_length");
                if (s != null)
                    session_length = Long.parseLong(s);
                for (int i = 0; ; i++) {
                    Element entry = ent.findElement("entry", i);
                    if (entry == null)
                        break;
                    Entry e = Entry.create(entry);
                    entries.add(e);
                }
            }
        }
    }

    public void add(Entry e) {
        e.ord = howManyTestEntries();
        entries.add(e);
    }

    public int howManyTestEntries() {
        return entries.size();
    }

    public Iterator getAllTestEntries() {
        return entries.iterator();
    }

    String getType() {
        return type;
    }

    String getLessonName() {
        return l_id + '-' + type;
    }

    Date getPerformDate() {
        return new Date();
    }

    Date getFirstPerformDate() {
        return first_perform_date;
    }

    String fixString(String s) {
        return s.replace(':', '_').replace(' ', '_');
    }

    public Entry getEntry(int ix) {
        return (Entry) entries.get(ix);
    }

    public int getEntrySize() {
        return entries.size();
    }

    public int getEntrySize(String type) {
        int c = 0;
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            if (e.type.equals(type))
                c++;
        }
        return c;
    }

    Element getElement() {
        Element el = new Element("result");
        el.addAttr("pupil", pupil);
        el.addAttr("type", getType());
        DateFormat df = DateFormat.getDateTimeInstance();
        el.addAttr("created_date", fixString("" + df.format(created)));

        Element eel = new Element("entries");
        eel.addAttr("session_length", "" + session_length);

        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            Element e1 = e.getElement();
            eel.add(e1);
        }
        el.add(eel);

        return el;
    }

    public void dump() {
        OmegaContext.sout_log.getLogger().info(":--: " + "sl-" + session_length);
        OmegaContext.sout_log.getLogger().info(":--: " + "ty-" + type);
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            OmegaContext.sout_log.getLogger().info(":--: " + "==  " + e);
        }
    }
}
