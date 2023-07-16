package com.femtioprocent.omega.lesson.machine;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.canvas.LessonCanvas;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class ItemEntry {
    public String type;
    public String tid;
    List items;
    List all_items;
    public int ord;
    //    ItemEntry link_next;

    boolean Tr = false;

    ItemEntry() {
        items = new ArrayList();
        Item item = new Item("");
        item.ord = 0;
        item.it_ent = this;
        items.add(item);
        type = "passive";
        tid = "";
    }

    ItemEntry(Element el, boolean dummy, boolean mix) {
        items = new ArrayList();
        load(el, dummy, mix);
    }

    int count(String s, char ch) {
        int a = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == ch)
                a++;
        return a;
    }

    private void load(Element el, boolean dummy, boolean mix) {
        type = el.findAttr("type");
        tid = el.findAttr("Tid");
        int tid_len = count(tid, ',') + 1;
        int last_i = 0;
        boolean cant_have_dummy = false;
        for (int i = 0; i < 100; i++) {
            Element it_el = el.findElement("item", i);
            if (it_el == null)
                break;
            String txt = it_el.findAttr("text");
            if (txt == null || txt.length() == 0 || (!Lesson.edit && isPeTask(txt)) )
                continue;
            Item item = new Item(i, it_el, true);
            if ("action".equals(type)) {
                item.setDefaultAction();
            }
            if (item.getDummyText() == null || item.getDummyText().length() == 0)
                cant_have_dummy = true;
            item.it_ent = this;
            items.add(item);
            last_i = i + 1;
        }
        if (dummy) {  // make an extra word place for dummy, one each for list of tid (s,o)
            if (!"action".equals(type)) {
                for (int i = 0; i < tid_len; i++) {
                    if (cant_have_dummy == false) {
                        if (items.size() < 8) {
                            Item item = new Item("@" + ord + '.' + i);
                            item.it_ent = this;
                            item.dummy_extra = i;  // -1 no dummy
                            item.dummy_slot = true;
                            items.add(item);
                        } else {
                        }
                    }
                }
            }
        }
        if (mix)
            mixList();
        all_items = items;
    }

    public static boolean isPeTask(String txt) {
        return txt.matches("[{]\\*[0-9]*:[}]");
    }

    void load(String sa[][]) {
        items = new ArrayList();
        for (int i = 0; i < sa.length; i++) {
            Item item = new Item(sa[i][0], true);
            item.setActionFile(sa[i][2]);
            item.setLid_Krull(sa[i][1]);
            item.it_ent = this;
            item.ord = i;
            item.setSound_Krull(sa[i][3]);
            if (sa[i].length > 4) {
                item.setVar(0, sa[i][4]);
                item.setVar(1, sa[i][4]);
                item.setVar(2, sa[i][4]);
            }
            items.add(item);
        }
        all_items = items;
    }

    public void setDummyExtra(Item src_itm, int ix, ArrayList free) {
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item itm = (Item) it.next();
            if (itm.dummy_extra == ix) {  // OK, this is a free empty cell, I can use it
                itm.setText_Krull(src_itm.getDummyText());
                itm.sound = src_itm.getDummySound();
                itm.sign = src_itm.getDummySign();
                if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "this is now dummy " + itm);
                return;
            }
        }
        // no free empty speces, I must use another word
        if (free.size() > 0 && !"action".equals(type)) {
            int fix = SundryUtils.rand(free.size());
            Item itm = (Item) free.get(fix);
            itm.allocateDummySpace(src_itm);
            if (Tr)
                OmegaContext.sout_log.getLogger().info(":--: " + "this is now alloc dummy " + fix + ' ' + free.size() + ' ' + itm);
        }
    }

    void reOrdItem() {
        int a = 0;
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item itm = (Item) it.next();
            itm.ord = a++;
        }
    }

    void addItemAt(int ix) {
        addItemAt(ix, "text");
    }

    void addEmptyItemAt(int ix) {
        addItemAt(ix, "");
    }

    void addItemAt(int ix, String txt) {
        Item itm;
        if (type.equals("action")) {
            Item aitm = new Item(txt, true);
            //	    if ( txt.length() > 0 )
            aitm.setDefaultAction();
            itm = aitm;
        } else {
            itm = new Item(txt, true);
        }
        itm.it_ent = this;
        items.add(ix, itm);
        reOrdItem();
        all_items = items;
    }

    public void resetItems() {
        //	OmegaContext.sout_log.getLogger().info(":--: " + "RESET old items " + items);
        items = all_items;
        //	OmegaContext.sout_log.getLogger().info(":--: " + "RESET new items " + items);
    }

    public Item getItemAt(int ix) {
        if (ix >= items.size())
            return null;
        return (Item) items.get(ix);
    }

    public int howManyItems() {
        return items.size();
    }

//     public int howManyLinkedItems() {
// 	return howManyItems() + (hasNextLink() ? 0 : getNextLink().howManyLinkedItems());
//     }

//     public ItemEntry getNextLink() {
// 	return link_next;
//     }

//     public boolean hasNextLink() {
// 	return link_next != null;
//     }

    int maxStringWidth() {
        int mx = 0;
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item itm = (Item) it.next();
            if (itm != null) {
                String txt = itm.getDefaultFilledText();
                if (txt.length() > mx)
                    mx = txt.length();
            }
        }
        return mx * LessonCanvas.CH_W;
    }

    int getStringWidth(Font fo, Graphics2D g2, String s) {
        RenderingHints rh = g2.getRenderingHints();
        rh.put(rh.KEY_ANTIALIASING, rh.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        FontRenderContext frc = g2.getFontRenderContext();
        Rectangle2D r = fo.getStringBounds(s, frc);
        return (int) r.getWidth();
    }

    int maxStringWidth(Font fo, Graphics2D g2) {
        int mx = 0;
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item itm = (Item) it.next();
            if (itm != null) {
                String txt = itm.getDefaultFilledText();
                int gsw = getStringWidth(fo, g2, txt);
                if (gsw > mx)
                    mx = gsw;
            }
        }
        return mx * LessonCanvas.CH_W;
    }

    public int sowDummy(String current_correct_sentence) {
        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "sowD " + current_correct_sentence);

        resetItems();
        if (current_correct_sentence == null)
            return 0;
        mixList();
        Iterator it = items.iterator();
        boolean has_krull = false;
        if (current_correct_sentence.indexOf('{') != -1)
            has_krull = true;

        ArrayList free = new ArrayList();

        while (it.hasNext()) {
            Item itm = (Item) it.next();
            if (itm != null) {
                String extras[] = SundryUtils.split(tid, ",");
                ArrayList free_1 = new ArrayList();
                for (int jj = 0; jj < extras.length; jj++) {
                    String extra = has_krull ? "{" + extras[jj] + '}' : "";
                    String s = itm.getText() + extra; // current item + {tid}
                    if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "try locate " + s);
                    if (current_correct_sentence.toLowerCase().indexOf(s.toLowerCase()) == -1) {
                        if (!itm.isDummySpaceAllocated()) {
                            if (free_1 != null)
                                free_1.add(itm);
                            if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "free_1 added " + itm);
                        }
                    } else {
                        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "free_1->null");
                        free_1 = null;
                    }
                }
                if (free_1 != null) {
                    free.addAll(free_1);
                }
            }
        }
        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "free is " + free);

        Set used = new HashSet();
        it = items.iterator();
        while (it.hasNext()) {
            Item itm = (Item) it.next();
            if (itm != null) {
                String extras[] = SundryUtils.split(tid, ",");
                for (int jj = 0; jj < extras.length; jj++) {
                    String extra = has_krull ? "{" + extras[jj] + '}' : "";
                    String s = itm.getText() + extra; // current item + {tid}
                    if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "try locate' " + s);
                    if (current_correct_sentence.toLowerCase().indexOf(s.toLowerCase()) == -1) {
                        // this word not in correct sent
                    } else {  // we have this item as one of the correct
                        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "use?y " + s + ' ' + used);
                        if (!used.contains(itm.getText())) {
                            setDummyExtra(itm, jj, free);
                        }
                        used.add(itm.getText());
                    }
                }
            }
        }
        removeStaleDummyProxy();
        return 1;
    }

    private void removeStaleDummyProxy() {
        List n_items = new ArrayList();
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item itm = (Item) it.next();
            if (itm.getText().charAt(0) == '@')
                ;
            else
                n_items.add(itm);
        }
        if (items.size() != n_items.size())
            items = n_items;
        //	OmegaContext.sout_log.getLogger().info(":--: " + "stale " + items);
    }

    public void mixList() {
        Item[] iA = (Item[]) items.toArray(new Item[0]);
        SundryUtils.scrambleArr(iA);
        items = new ArrayList(Arrays.asList(iA));
        reOrdItem();
        OmegaContext.def_log.getLogger().info("mixList " + items);
        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "mixList " + items);
    }

    void removeDummy() {
        resetItems();
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Item itm = (Item) it.next();
            if (itm != null) {
                itm.restoreSavedDummy();
            }
        }
    }

    public String toString() {
        return "ItemEntry{" + type +
                ", Tid=" + tid +
                ", items=" + items +
                "}";
    }
}
