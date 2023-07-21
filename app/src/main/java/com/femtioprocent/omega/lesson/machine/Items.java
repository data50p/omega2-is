package com.femtioprocent.omega.lesson.machine;

import com.femtioprocent.omega.xml.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Items {
    List item_entry_list;

    Items() {
        item_entry_list = new ArrayList();
    }

    Items(Element el, Target tg, HashMap story_hm, boolean dummy, boolean mix) {
        item_entry_list = new ArrayList();

        for (int i = 0; i < 100; i++) {
            Element il_el = el.findElement("item-entry", i);
            if (il_el == null)
                break;
            ItemEntry item_entry = new ItemEntry(il_el, dummy, mix);
            item_entry.ord = i;

            if (tg.isTidInTarget(item_entry.tid))
                item_entry_list.add(item_entry);
        }
    }

    Items(String[][] sa, Target tg) {      // text, lid,lid... 4 random target list
        item_entry_list = new ArrayList();

        ItemEntry item_entry = new ItemEntry();
        item_entry.tid = "X";
        item_entry.ord = 0;
        item_entry.load(sa);
        item_entry_list.add(item_entry);
    }

    Items(String[][] sa, Target tg, boolean dummy) {      // text, lid,lid... 4 random target list
        item_entry_list = new ArrayList();

        ItemEntry item_entry = new ItemEntry();
        item_entry.tid = "X";
        item_entry.ord = 0;
        item_entry.load(sa);
        item_entry_list.add(item_entry);
    }

    ItemEntry getItemEntryTid(String tg_tid) {
        for (Iterator it = item_entry_list.iterator(); it.hasNext(); ) {
            ItemEntry it_ent = (ItemEntry) it.next();
            if (it_ent.tid.equals(tg_tid))
                return it_ent;
            if (tg_tid.length() == 1 && it_ent.tid.indexOf(tg_tid.charAt(0)) != -1)
                return it_ent;
        }
        return null;
    }

    ItemEntry[] getItemEntryTidAll(String tg_tid) { // ie v
        String tg_choise = "," + tg_tid + ",";

        ArrayList<ItemEntry> li = new ArrayList<>();

        for (Iterator it = item_entry_list.iterator(); it.hasNext(); ) {
            ItemEntry it_ent = (ItemEntry) it.next();
            String item_choise = "," + it_ent.tid + ",";
            if (item_choise.indexOf(tg_choise) != -1)               // ie it_ent.tid s,v  -> match
                li.add(it_ent);
        }
        return (ItemEntry[]) li.toArray(new ItemEntry[li.size()]);
    }

    int howManyItemEntries() {
        return item_entry_list.size();
    }

    ItemEntryVirtualList getItemEntryVirtualList(String tid) {
        String tid2 = "," + tid + ",";
        ItemEntryVirtualList ievli = new ItemEntryVirtualList();
        for (Iterator it = item_entry_list.iterator(); it.hasNext(); ) {
            ItemEntry it_ent = (ItemEntry) it.next();
            String tid3 = "," + it_ent.tid + ",";
            if (tid3.indexOf(tid2) != -1)  // tid=s   it_ent.tid=s,o  true
                ievli.items.addAll(it_ent.items);
        }
        return ievli;
    }

    ItemEntry getItemEntryAt(int ix) {
        try {
            return (ItemEntry) item_entry_list.get(ix);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public void add(int ix) {
        ItemEntry item_entry = new ItemEntry();
        item_entry_list.add(ix, item_entry);
        reOrd();
    }

    void reOrd() {
        int ORD = 0;
        for (Iterator it = item_entry_list.iterator(); it.hasNext(); ) {
            ItemEntry it_ent = (ItemEntry) it.next();
            it_ent.ord = ORD++;
        }
    }

    public void remove(int ix) {
        item_entry_list.remove(ix);
    }

    public int sowDummy(String current_correct_sentence) {
        int c = 0;
        for (Iterator it = item_entry_list.iterator(); it.hasNext(); ) {
            ItemEntry it_ent = (ItemEntry) it.next();
            c += it_ent.sowDummy(current_correct_sentence);
        }
        return c;
    }

    void removeDummy() {
        for (Iterator it = item_entry_list.iterator(); it.hasNext(); ) {
            ItemEntry it_ent = (ItemEntry) it.next();
            it_ent.removeDummy();
        }
    }

    public Element getElement() {
        Element el = new Element("items");

        int a = 0;

        for (Iterator it = item_entry_list.iterator(); it.hasNext(); ) {
            ItemEntry it_ent = (ItemEntry) it.next();
            Element iel = new Element("item-entry");
            iel.addAttr("ord", "" + a++);
            iel.addAttr("type", it_ent.type);
            iel.addAttr("Tid", it_ent.tid);
            for (int ix = 0; ix < it_ent.howManyItems(); ix++) {
                Item itm = it_ent.getItemAt(ix);
                Element itel = itm.getElement();
                iel.add(itel);
            }
            el.add(iel);
        }
        return el;
    }
}
