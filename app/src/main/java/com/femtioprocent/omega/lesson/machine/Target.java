package com.femtioprocent.omega.lesson.machine;

// has UTF-8

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.assets.TargetCombinations;
import com.femtioprocent.omega.anim.appl.Anim_Repository;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.canvas.LessonCanvas;
import com.femtioprocent.omega.lesson.managers.movie.LiuMovieManager;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Values;
import com.femtioprocent.omega.xml.Element;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class Target {
    public String[][] saved_sa;
    public String[][] saved_saD;

    public Machine machine;

    public List<T_Item> t_items;
    Items items;

    String story_next;

    public LessonCanvas.Box[] hBox = new LessonCanvas.Box[1];

    public boolean iam_composite = false;
    public boolean iam_dummy = false;

    boolean Tr = false;

    public class T_Item {
        //	Target tg;
        int ord;
        public String type;
        public String tid;
        public Item item;
        public String lid;

        //	T_Item(Target tg, int ord, String type, String tid, String lid) {
        T_Item(int ord, String type, String tid, String lid) {
//	    this.tg = tg;
            this.ord = ord;
            this.type = type;
            this.tid = tid;
            this.lid = lid;
        }

        Item getItem() {
            return item;
        }

        String getTid() {
            return tid;
        }

        String getFilledText() {
            if (item == null)
                return "               ";
            else
                return fillVarHere(ord, item.getTextD());
        }

        String getFilledTTS() {
            if (item == null)
                return "               ";
            else
                return fillVarHere(ord, item.getTTSD());
        }

        String getFilledActionText() {
            if (item == null)
                return "               ";
            else
                return fillVarHere(ord, item.getActionFile());
        }

        public String getLIDText() {
            if (item == null)
                return "";
            else
                return fillVarHere(ord, item.getLid());
        }

        String getLIDOrNull() {
            if (item == null)
                return null;
            else
                return fillVarHere(ord, item.getLid());
        }

        String getTextOrNull() {
            if (item == null)
                return null;
            else
                return fillVarHere(ord, item.getText());
        }

        public String getTextVarsOrNull() {
            if (item == null)
                return null;
            return fillVarHere(ord, item.getText()) +
                    "§_" + item.getVar(1) +                              ////   se Lesson.java
                    "§_" + item.getVar(2) +                              //// UTF-8
                    "§_" + item.getVar(3) +                              //// UTF-8
                    "§_" + fillVarHere(ord, item.getSound());  // AUDIO                              //// UTF-8
        }

        public String getLID4TgOrNull() {
            return fillVarHere(ord, lid);
        }

        public String getLID4TgOrNull_KeepVar() {
            return lid;//fillVarHere(ord, lid);
        }

        void clearText() {
            item = null;
        }

        public Element getElement() {
            Element el = new Element("t-item");
            el.addAttr("ord", "" + (ord + 0));
            el.addAttr("type", "" + type);
            el.addAttr("Tid", "" + tid);
            if (lid != null)
                el.addAttr("Lid", "" + lid);
            return el;
        }

        public String toString() {
            return "T_Item{" + ord + ", type=" + type + ", tid=" + tid + ", item=" + item + ", lid=" + lid + "}";
        }
    }

    public Target() {
        init();
    }

    public Target(Machine machine) {
        this.machine = machine;
        init();
    }

    public void init() {
        items = new Items();
        t_items = new ArrayList();
    }

    public void loadCompositeFromEl(Element el,
                                    String test_txt,
                                    HashMap<String, Lesson.SentenceList> story_hm,
                                    final boolean dummy,
                                    final boolean mix) throws Exception {
        iam_composite = true;
        iam_dummy = dummy;
        init();
        if (el == null)
            throw new Exception("No data to load from");

        Target tg2 = new Target();
        tg2.loadFromEl(el, "", story_hm, false, mix); // can't have dummy extra slot

        String ty = "action";
        String tid = "X";

        int[][] test_index = tg2.getAllTargetCombinationsIndexes(test_txt);

        String lid = SundryUtils.a2s(tg2.getAll_Lid_Target_KeepVar());
        String lid_orig = lid;

        if (lid.contains("++"))
            lid = lid.replace("++", "=");
        if (lid.contains("+"))
            lid = lid.replace("+", "=");

        T_Item titm = new T_Item(0, ty, tid, lid); // make one slot
        t_items.add(titm);

        String[][] sa = tg2.getAllTargetCombinationsAndMore(false); // with no dummy
        String[][] saD = tg2.getAllTargetCombinationsAndMore(true); // with own dummy random 1/4

        tg2 = null;

        saved_sa = sa;
        saved_saD = saD;

        int five = 5;
        if (sa.length < 5)
            five = sa.length;
        SundryUtils.scrambleArr(sa);
        SundryUtils.scrambleArr(saD);

        // first find the correct sentence
        String[] riktig = null;
        for (int i = 0; i < sa.length; i++)
            if (sa[i][0].equals(test_txt))
                riktig = sa[i];
        if (riktig == null)
            throw new Exception("Can't find sentence " + test_txt);

        // second, create 5 maybee dummy slot
        String[][] sa5 = new String[five][];
        for (int i = 0; i < sa5.length; i++) // fem blandade med dummy eller vanliga
            sa5[i] = dummy ? saD[i] : sa[i];

        boolean finns = false;
        for (int i = 0; i < sa5.length; i++)
            if (sa5[i][0].equals(test_txt))
                finns = true;                   // test finns redan
        if (finns == false)                   // put  test
            sa5[SundryUtils.rand(5)] = riktig;

        items = new Items(sa5, this);

        story_next = null;
        Element story_el = el.findElement("story", 0);
        if (story_el != null) {
            Element ell = story_el.findElement("link", 1);
            if (ell != null) {
                String next = ell.findAttr("next");
                story_next = next;
            }
        }
    }

    public void loadFromEl(Element el,
                           String test_txt,
                           HashMap<String, Lesson.SentenceList> story_hm,
                           boolean dummy,
                           boolean mix) throws Exception {
        iam_composite = false;
        iam_dummy = dummy;
        init();
        if (el == null)
            throw new Exception("No data to load from");

        Element ta_el = el.findElement("target", 0);
        if (ta_el == null)
            throw new Exception("Data corrupt");

        for (int i = 0; i < 100; i++) {
            Element ti_el = ta_el.findElement("t-item", i);
            if (ti_el == null)
                break;
            String ty = ti_el.findAttr("type");
            if ("actor".equals(ty))
                ty = "passive";
            String tid = ti_el.findAttr("Tid");
            String lid = ti_el.findAttr("Lid");
            //log		OmegaContext.sout_log.getLogger().info(":--: " + "tg_load add " + ty + ' ' + tid + ' ' + lid);
            t_items.add(new T_Item(i, ty, tid, lid));
        }

        Element its_el = el.findElement("items", 0);
        items = new Items(its_el, this, story_hm, dummy, mix);

        story_next = null;
        Element story_el = el.findElement("story", 0);
        //log	    OmegaContext.sout_log.getLogger().info(":--: " + "FSt " + story_el);
        if (story_el != null) {
            Element ell = story_el.findElement("link", 0);
            //log		OmegaContext.sout_log.getLogger().info(":--: " + "FSt story/link " + ell);
            String next = ell.findAttr("next");
            if ( next != null ) {
                OmegaContext.sout_log.getLogger().info(":--: " + "FSt story/link[next] " + next);
                story_next = next;
            }
        }
    }

    public void reloadComposite(String test_txt) throws Exception {
        if (!iam_composite)
            return;
        if (saved_sa == null)
            return;


        String[][] sa = saved_sa;
        String[][] saD = saved_saD;

        int five = 5;
        if (sa.length < 5)
            five = sa.length;
        SundryUtils.scrambleArr(sa);
        SundryUtils.scrambleArr(saD);

        String[] riktig = null;
        for (int i = 0; i < sa.length; i++) {
            if (sa[i][0].equals(test_txt))
                riktig = sa[i];
        }
        if (riktig == null)
            throw new Exception("Can't find sentence " + test_txt);

        String[][] sa5 = new String[five][];
        for (int i = 0; i < sa5.length; i++) // fem blandade med dummy
            sa5[i] = iam_dummy ? saD[i] : sa[i];

        boolean finns = false;
        for (int i = 0; i < sa5.length; i++)
            if (sa5[i][0].equals(test_txt))
                finns = true;                   // test finns redan
        if (finns == false)                   // put test
            sa5[SundryUtils.rand(5)] = riktig;

        items = new Items(sa5, this);
    }


    public Element getTargetElement() {
        Element el = new Element("target");
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            Element tel = titm.getElement();
            el.add(tel);
        }
        return el;
    }

    public Element getItemsElement() {
        return items.getElement();
    }

    boolean matchTid2(String ent_tid, String tg_tid) { // ent_tid is a comma list
        if (ent_tid == null)
            return false;
        String sa[] = SundryUtils.split(ent_tid, ",");
        for (int i = 0; i < sa.length; i++)
            if (sa[i].equals(tg_tid)) {
                return true;
            }
        return false;
    }

    public int whatTargetMatchTid(String ent_tid) { // mask
        int a = 0;
        int c = 0;

        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (matchTid2(ent_tid, titm.tid))
                a |= (1 << c);
            c++;
        }

        return a;
    }


    private ItemEntry[] findItemEntryMatchTidAll(String tid) {
        return items.getItemEntryTidAll(tid);
    }

    public ItemEntry findItemEntryMatchTid(String tid) {
        return items.getItemEntryTid(tid);
    }

    public ItemEntryVirtualList findItemEntryVirtualListMatchTid(String tid) {
        return items.getItemEntryVirtualList(tid);
    }

    boolean isTidInTarget(String ent_tid) {
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (matchTid2(ent_tid, titm.tid))
                return true;
        }
        return false;
    }

    public int findNextFreeT_ItemIx(Item box_itm, boolean replace, int where) {
        int same = 0;
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (matchTid2(box_itm.getIt_ent().tid, titm.tid))
                same++;
        }
        if (same == 0)
            return -1;
        double d = where / 100.0;
        int skip = (int) (d * same);
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (matchTid2(box_itm.getIt_ent().tid, titm.tid))
                if (titm.item == null || (replace && skip-- == 0)) {
                    return titm.ord;
                }
        }
        return -1;
    }

    public int findNextFreeT_ItemIx() {
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm.item == null)
                return titm.ord;
        }
        return -1;
    }

    public int findEntryIxMatchTargetIx(int tg_ix) {
        T_Item t_itm = getT_Item(tg_ix);
        String tg_tid = t_itm.tid;
        ItemEntry it_ent = findItemEntryMatchTid(tg_tid);
        return it_ent.ord;
    }

    public int[] findEntryIxMatchTargetIxAll(int tg_ix) {
        T_Item t_itm = getT_Item(tg_ix);
        String tg_tid = t_itm.tid;
        ItemEntry[] it_ent = findItemEntryMatchTidAll(tg_tid);
        //	OmegaContext.sout_log.getLogger().info(":--: " + "match " + tg_tid + " " + SundryUtils.a2s(it_ent));
        int len = it_ent.length;
        int[] ia = new int[len];
        for (int i = 0; i < len; i++)
            ia[i] = it_ent[i].ord;
        return ia;
    }

    public int get_howManyT_Items() {
        return t_items.size();
    }

    public T_Item getT_Item(int ix) {
        try {
            return (T_Item) t_items.get(ix);
        } catch (IndexOutOfBoundsException ex) {
        }
        return null;
    }

    public void addT_Item(int ix) {
        if (t_items.size() < 6) {
            int i = t_items.size();
            String ty = "passive";
            String tid = "p" + i;
            String lid = "";
            t_items.add(ix, new Target.T_Item(i, ty, tid, lid));
            reOrdT_Items();
        }
    }

    public void delT_Item(int ix) {
        if (t_items.size() > 0) {
            t_items.remove(ix);
            reOrdT_Items();
        }
    }

    public void reOrdT_Items() {
        int o = 0;
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            titm.ord = o++;
        }
    }

    public void addItemEntry(int ix, int iy) {
        if (items.item_entry_list.size() < 6) {
            items.add(ix);
        }
    }

    public void delItemEntry(int ix, int iy) {
        if (items.item_entry_list.size() >= ix) {
            items.remove(ix);
        }
    }

    public void addItem(int ix, int iy) {
        ItemEntry it_ent = items.getItemEntryAt(ix);
        it_ent.addItemAt(iy);
    }

    public void addEmptyItem(int ix, int iy) {
        ItemEntry it_ent = items.getItemEntryAt(ix);
        it_ent.addEmptyItemAt(iy);
    }

    void set(int ix, Item it) {
//	item_list.dep_set(ix, it);
    }

    public boolean isTargetFilled() {
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm.item == null)
                return false;
        }
        return true;
    }

    String apply(int max, int ord, String txt) {
        String[] sa = SundryUtils.split(txt, "{}");
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sa.length; i++) {
            String s = sa[i];

            int a = 0;
            int aa = 0;
            boolean isVar = false;
            for (int j = 0; j < s.length(); j++) {
                if (s.charAt(j) == '-') {
                    a--;
                    aa++;
                    isVar = true;
                } else if (s.charAt(j) == '+') {
                    a++;
                    aa++;
                    isVar = true;
                } else if (s.charAt(j) == '=') {
                    aa++;
                    isVar = true;
                } else {
                    break;
                }
            }

            if (isVar == false) { // a == 0 )
                if ( sa[i].contains("*") ) {
                    String ss = sa[i].replaceAll("\\*[0-9]+:?", "");
                    sa[i] = ss;
                }
                sb.append(sa[i]);
            } else {
                int var_ix = s.charAt(aa) - '0';
                int ix = 0;
                String def = "";
                if ((ix = s.indexOf(':')) != -1) {
                    def = s.substring(ix + 1);
                }
                String var_val;
                try {
                    T_Item t_it = (T_Item) t_items.get(ord + a);
                    var_val = t_it.item.getVar(var_ix);               // the H4 bug, item is null
                    if (var_val == null)
                        var_val = def;
                    if (var_val.contains("{")) {
                        if ( max > 0 ) {
                            String var_val2 = apply(max - 1, ord + a, var_val);
                            //OmegaContext.sout_log.getLogger().info("apply: " + max + ',' + ord + ',' + txt + " -> " + var_val + " -> " + var_val2);
                            var_val = var_val2;
                        }
                    } else {
                        //OmegaContext.sout_log.getLogger().info("apply: " + max + ',' + ord + ',' + txt + " -> " + var_val);
                    }
                } catch (IndexOutOfBoundsException ex) {
                    var_val = def;
                } catch (NullPointerException ex) {
                    var_val = def;
                }
                sb.append(var_val);
            }
        }
        return sb.toString();
    }

    private boolean hasVar(String s) {
        if (s == null)
            return false;
        return s.indexOf('{') != -1;
    }

    public String fillVarHere(int ix, String s) {
// 	SundryUtils.pe_("fillV " + ix + ' ' + s );
        if (s == null)
            return null;

        if (hasVar(s))
            s = apply(4, ix, s);
//	OmegaContext.sout_log.getLogger().info(":--: " + " " + s );
        return s;
    }

    private int getActionIx() {
        int c = 0;
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm.type.equals("action"))
                return c;
            c++;
        }
        return -1;
    }

//     private Item getActionItem() {
// 	int ix = getActionIx();
// 	if ( ix >= 0 )
// 	    return (Item)getT_Item(ix).item;
// 	return null;
//     }

    // what:
    // 0 first, default
    // 99 all ' ' separated
    public String getActionFileName(int what) {
        if (what == 0) {
            int ix = getActionIx();
            if (ix >= 0) {
                String ss = fillActionLid(ix, (Item) getT_Item(ix).item);
                if (ss == null)
                    return null;
                String[] sa = SundryUtils.split(ss, ",");
                return sa[0];
            } else
                return null;
        } else if (what == 99) {
            int ix = getActionIx();
            if (ix >= 0) {
                String ss = fillActionLid(ix, (Item) getT_Item(ix).item);
                return ss;
            } else
                return null;
        } else if (what == 999) { // get all from all
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < get_howManyT_Items(); i++) {
                int ix = i;
                if (ix >= 0) {
                    String ss = fillActionLid(ix, (Item) getT_Item(ix).item);
                    if (ss != null && ss.length() > 0) {
                        if (sb.length() > 0)
                            sb.append(",");
                        sb.append(ss);
                    }
                }
            }
            return sb.toString();
        }
        return getActionFileName(0);
    }

    String fillActionLid(int ix, Item it) {
        if (it == null)
            return null;
        String txt = it.getActionText();
        return fillVarHere(ix, txt);
    }

    public void setItem(int ix, Item item) {
        T_Item t_i = (T_Item) t_items.get(ix);
        t_i.item = item;
    }

    public int howManyItemBoxes() {
        return items.howManyItemEntries();
    }

    public Item getItemAt(int ix, int iy) {
        ItemEntry it_ent = items.getItemEntryAt(ix);
        if (it_ent == null)
            return null;
        Item item = it_ent.getItemAt(iy);
        return item;
    }

    public void releaseT_ItemAt(int ix) {
        T_Item t_itm = (T_Item) t_items.get(ix);
        t_itm.clearText();
    }

    public void releaseAllT_Items() {
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            titm.clearText();
        }
        OmegaContext.sout_log.getLogger().info(":--: " + "target released");
    }

    public Item pickNextItem() {
        return null;
    }

    public Item pickItemAtEx(int ix, int iy, int tg_ix) { // pick at ix,iy -> target[tg_ix]
        Item ret = pickItemAt(ix, iy, tg_ix, false, true);
        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "pick  " + ret);
        return ret;
    }

    public Item pickItemAt(int ix, int iy, int tg_ix) {
        return pickItemAt(ix, iy, tg_ix, false, false);
    }

    public Item pickItemAtDummy(boolean dummy, int ix, int iy, int tg_ix) {
        return pickItemAtDummy(dummy, ix, iy, tg_ix, false);
    }

    public Item pickItemAt(int ix, int iy, int tg_ix, boolean any_id) {
        return pickItemAt(ix, iy, tg_ix, any_id, false);
    }

    public Item pickItemAt(int ix, int iy, int tg_ix, boolean any_id, boolean next_col) {
        Item ret = null;

        try {
            if (iy >= 8) { // ZZ
                if (next_col) {
                    iy -= 8;
                    ix++;
                } else
                    return null;
            }
            T_Item t_itm = (T_Item) t_items.get(tg_ix);
            String tg_tid = t_itm.tid;
            //   OmegaContext.sout_log.getLogger().info(":--: " + "pick it tg_tid " + tg_tid + ' ' + ix + ' ' + iy);
            //ItemEntry it_ent = items.getItemEntryTid(tg_tid);
            ItemEntry it_ent_ix = items.getItemEntryAt(ix);
            //	    OmegaContext.sout_log.getLogger().info(":--: " + "tid.. " + it_ent_ix.tid);

            if (it_ent_ix == null) {
                ret = null;
                return ret;
            }
            Item item = it_ent_ix.getItemAt(iy);
            String ent_tid = it_ent_ix.tid;
            if (any_id || matchTid2(ent_tid, tg_tid)) {
                setItem(tg_ix, item);
                ret = item;
                return ret;
            } else {
                ret = null;
                return ret;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//	    if ( ret == null )
// 		OmegaContext.sout_log.getLogger().info(":--: " + "pickItem -> NULL " + ix + ' ' + iy);
// 	    OmegaContext.sout_log.getLogger().info(":--: " + "pickItem -> " + ix + ' ' + iy + ' ' + ret.getText());
        }
        return ret;
    }

    public Item pickItemAtDummy(boolean dummy, int ix, int iy, int tg_ix, boolean any_id) {
        try {
            T_Item t_itm = (T_Item) t_items.get(tg_ix);
            String tg_tid = t_itm.tid;

            //ItemEntry it_ent = items.getItemEntryTid(tg_tid);
            ItemEntry it_ent_ix = items.getItemEntryAt(ix);

            if (it_ent_ix == null) {
                return null;
            }
            Item item = it_ent_ix.getItemAt(iy);
            item.setDummy(dummy ? SundryUtils.rand(100) < 25 : false);

            String ent_tid = it_ent_ix.tid;
            if (any_id || matchTid2(ent_tid, tg_tid)) {
                setItem(tg_ix, item);
                return item;
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int sowDummy(String current_correct_sentence) {
        return items.sowDummy(current_correct_sentence);
    }

    public void removeDummy() {
        items.removeDummy();
    }

    public int getMaxItemsInAnyBox() {
        int a = 0;
        for (int i = 0; i < howManyItemBoxes(); i++) {
            ItemEntry ite = items.getItemEntryAt(i);
            int n = ite.howManyItems();
            if (n > a)
                a = n;
        }
        return a;
    }

    public int getMaxItemsInBox(int ix) {
        for (int i = 0; i < howManyItemBoxes(); i++) {
            ItemEntry ite = items.getItemEntryAt(i);
            if (i == ix)
                return ite.howManyItems();
        }
        return 0;
    }

    public int getMaxWidthInBox(int ix) {
        ItemEntry ite = items.getItemEntryAt(ix);
        return ite.maxStringWidth();
    }

    public int getMaxWidthInBox(int ix, Font fo, Graphics2D g2) {
        ItemEntry ite = items.getItemEntryAt(ix);
        return ite.maxStringWidth(fo, g2);
    }

    public int getMaxWidthUptoBox(int ix) {
        int a = 0;
        for (int i = 0; i < ix; i++)
            a += getMaxWidthInBox(i);
        return a;
    }

    public int getMaxWidthSumAllBox() {
        int a = 0;
        int to = items.howManyItemEntries();
        for (int i = 0; i < to; i++) {
            ItemEntry ite = items.getItemEntryAt(i);
            a += ite.maxStringWidth();
        }
        return a;
    }

    public int getMaxWidthSumAllBox(Font fo, Graphics2D g2) {
        int a = 0;
        int to = items.howManyItemEntries();
        for (int i = 0; i < to; i++) {
            ItemEntry ite = items.getItemEntryAt(i);
            a += ite.maxStringWidth(fo, g2);
        }
        return a;
    }

    String Upper1(String s) {
        if (s != null && s.length() >= 1) {
            String ss = s.substring(1);
            char ch = s.charAt(0);
            return "" + Character.toUpperCase(ch) + ss;
        }
        return s;
    }

    public String getAllText() {
        String s = getTextUpto(t_items.size(), 1);
        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "getAllText " + s + '.');
        return s;
    }

    public String getAllTTS() {
        String s = getTTSUpto(t_items.size(), 1);
        if (Tr) OmegaContext.sout_log.getLogger().info(":--: " + "getAllTTS " + s + '.');
        return s;
    }

    public ArrayList<String> getAllSignMovies(LiuMovieManager lmm) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < t_items.size(); i++) {
            final T_Item t_Item = getT_Item(i);
            String smfName = lmm.getSignMovieFileName(t_Item.item, this, i);
            if (smfName != null) {
                list.add(smfName);
            } else {
                list.add("");
            }
        }
        return list;
    }

    public String getAllText(String sep) {
        String s = getTextUpto(t_items.size(), sep);
        return s;
    }

    public String getAllText(String sep, char delim) {
        String s = getTextUpto(t_items.size(), sep, delim);
        return s;
    }

    public String getAllText(int space) {
        return getTextUpto(t_items.size(), space);
    }

    public String getTextAt(int ix) {  // can be dummy text
        T_Item titm = getT_Item(ix);
        if (titm == null)
            return "";
        String txt = titm.getFilledText();
        return txt;
    }

    public String getTTSAt(int ix) {  // can be dummy text
        T_Item titm = getT_Item(ix);
        if (titm == null)
            return "";
        String txt = titm.getFilledTTS();
        return txt;
    }

    public String getTidAt(int ix) {
        T_Item titm = getT_Item(ix);
        if (titm == null)
            return "X";
        return titm.tid;
    }

    public String getTextUpto(int ix, int space) {
        String sp = "                                                     ".substring(0, space);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ix; i++) {
            String txt = getTextAt(i);
            if (sb.length() > 0 && txt.length() > 0) {
                sb.append(sp);
            }
            if (i == 0)
                txt = Upper1(txt);
            sb.append(txt);
        }
        return sb.toString();
    }

    public String getTTSUpto(int ix, int space) {
        String sp = "                                                     ".substring(0, space);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ix; i++) {
            String txt = getTTSAt(i);
            if (sb.length() > 0 && txt.length() > 0) {
                sb.append(sp);
            }
            if (i == 0)
                txt = Upper1(txt);
            sb.append(txt);
        }
        return sb.toString();
    }

    public String getTextUpto(int ix, String sep) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ix; i++) {
            String txt = getTextAt(i);
            if (i == 0)
                txt = Upper1(txt);
            if (i > 0)
                sb.append(sep);
            sb.append(txt);
        }
        return sb.toString();
    }

    public String getTextUpto(int ix, String sep, char delim) {
        String delim2 = "";
        if (delim == '{')
            delim2 = "}";
        if (delim == '(')
            delim2 = ")";
        if (delim == '[')
            delim2 = "]";
        if (delim == '<')
            delim2 = ">";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ix; i++) {
            String txt = getTextAt(i);
            if (i == 0)
                txt = Upper1(txt);
            if (i > 0)
                sb.append(sep);
            sb.append(txt);
            sb.append("" + delim + getTidAt(i) + delim2);
        }
        return sb.toString();
    }

    public String[] getAllLessonBothArg() {
        List<String> li = new ArrayList();

        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            String s = titm.getLIDText();
            if (s != null && s.length() > 0)
                li.add(s + ';' + titm.lid);
        }
        return (String[]) li.toArray(new String[0]);
    }

    void addSA(List<String> li, String[] sa) {
        for (int i = 0; i < sa.length; i++)
            li.add(sa[i]);
    }

    public String[] getAll_Lid_Target() {  // banor,banor...
        List<String> li = new ArrayList();

        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            String s = titm.getLID4TgOrNull();
            if (s != null && s.length() > 0) {
                String[] sa = SundryUtils.split(s, ",");
                addSA(li, sa);
            }
        }
        return (String[]) li.toArray(new String[0]);
    }

    public String[] getAll_Lid_Target_KeepVar() {  // banor,banor...
        List<String> li = new ArrayList();

        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            String s = titm.getLID4TgOrNull_KeepVar();
            if (s != null && s.length() > 0) {
                String[] sa = SundryUtils.split(s, ",");
                addSA(li, sa);
            }
        }
        return (String[]) li.toArray(new String[0]);
    }

    public String[] getAllSounds() {
        List<String> li = new ArrayList();

        int ix = 0;
        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null && titm.item != null) {
                String s = titm.item.getSoundD();
                s = fillVarHere(ix, s);
                li.add(s);
            }
            ix++;
        }
        String[] sa = (String[]) li.toArray(new String[0]);

//log	OmegaContext.sout_log.getLogger().info(":--: " + "[] get sounds " + SundryUtils.a2s(sa));
        return sa;
    }

    public String[] getSoundsAt(int x, int y) {
        List<String> li = new ArrayList();

        int ix = 0;
        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null && titm.item != null) {
                String s = titm.item.getSoundD();
                s = fillVarHere(ix, s);
                li.add(s);
            }
            ix++;
        }
        String[] sa = (String[]) li.toArray(new String[0]);

//log	OmegaContext.sout_log.getLogger().info(":--: " + "[] get sounds " + SundryUtils.a2s(sa));
        return sa;
    }

    public String[] getAll_Lid_Item() {  // actor,actor...
        List<String> li = new ArrayList();

        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null) {
                String s = titm.getLIDOrNull();
                if (s != null && s.length() > 0) {
                    String[] sa = SundryUtils.split(s, ",");
                    addSA(li, sa);
                }
            }
        }
        return (String[]) li.toArray(new String[0]);
    }

    public String getAll_Tid_Item() {  // ordgrupps id
        StringBuffer sb = new StringBuffer();

        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null) {
                String s = titm.tid;
                if (sb.length() > 0)
                    sb.append(";");
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public String[] getAll_Text_Item() {  // actor,actor...
        List<String> li = new ArrayList();

        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null) {
                String sx = titm.getTextOrNull();
                String s = titm.getLIDOrNull();
                if (s != null && s.length() > 0) {
                    String[] sa = SundryUtils.split(sx, ",");
                    addSA(li, sa);
                }
            }
        }
        return (String[]) li.toArray(new String[0]);
    }

    public String getAll_Sound_Item() {
        String s = "";
        int ix = 0;
        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            String snd = titm.item.getSound();
            snd = fillVarHere(ix, snd);          // WHY-SundryUtil
            if (s.length() != 0)
                s += ',';
            s += snd;
            ix++;
        }
        return s;
    }

    public List<String> getAll_Sound_Items() {
        List<String> li = new ArrayList<>();
        int ix = 0;
        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            String snd = titm.item.getSound();
            snd = fillVarHere(ix, snd);          // WHY-SundryUtil
            if (!SundryUtils.empty(snd))
                fillHavingComma(li, snd);
            String sndD = titm.item.getDummySound();
            sndD = fillVarHere(ix, sndD);          // WHY-SundryUtil
            if (!SundryUtils.empty(sndD))
                li.add(sndD);
            ix++;
        }
        return li;
    }

    private void fillHavingComma(List<String> li, String snd) {
        if (!snd.contains(",")) {
            li.add(snd);
            return;
        }
        String[] sa = SundryUtils.split(snd, ",");
        for (String s : sa) {
            li.add(s);
        }
    }

    public String[] getAll_TextVars_Item() {  // actor,actor...
        List<String> li = new ArrayList();

        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null) {
                String sx = titm.getTextVarsOrNull(); // text:v1:v2:v3:sound : is paragraph_
//log		OmegaContext.sout_log.getLogger().info(":--: " + "tvvv = " + sx);
                String s = titm.getLIDOrNull();
                if (s != null && s.length() > 0) {
                    String[] sa = SundryUtils.split(sx, ",");
                    addSA(li, sa);
                }
            }
        }
        return (String[]) li.toArray(new String[0]);
    }

    public void putAll_TextVars_Item(HashMap hm) {
        for (Iterator<T_Item> it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null) {
                Item item = titm.getItem();
                String s = item.getVar(1);
                hm.put(titm.getTid() + ':' + "1", s);
                s = item.getVar(2);
                hm.put(titm.getTid() + ':' + "2", s);
                s = item.getVar(3);
                hm.put(titm.getTid() + ':' + "3", s);
                s = item.getVar(4);
                hm.put(titm.getTid() + ':' + "4", s);
                s = item.getVar(5);
                hm.put(titm.getTid() + ':' + "5", s);
            }
        }
    }

    @Deprecated
    public String getActionFromTarget() {
        for (Iterator it = t_items.iterator(); it.hasNext(); ) {
            T_Item titm = (T_Item) it.next();
            if (titm != null) {
                if (titm.item != null && titm.item.isAction())
                    return ((Item) (titm.item)).getActionFile();
            }
        }
        return "";
    }

    public int getLessonArgLength() {
        String[] sa = getAllLessonBothArg();
        return sa.length;
    }

    public String getLessonArg(int ix) {
        try {
            String[] sa = getAllLessonBothArg();
            return sa[ix];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    public Values getTargetValues(int ix) {
        T_Item tit = getT_Item(ix);
        Values vs = new Values();
        if (tit != null) {
            if (tit.item != null)
                vs.setStr("text", tit.item.getDefaultFilledText()); // text);
            else
                vs.setStr("text", "");
            vs.setStr("tid", tit.tid);
            vs.setStr("lid", tit.lid);
            vs.setStr("type", tit.type);
        }

        return vs;
    }

    public void putTargetValues(int ix, Values vs) {
        T_Item tit = getT_Item(ix);
        tit.tid = vs.getStr("tid");
        tit.lid = vs.getStr("lid");
        tit.type = vs.getStr("type");
    }

    public String[] getAllTargetCombinations_old(String sep) {
        List<String> li = new ArrayList();

        Target tg2 = this;

        int Tn = tg2.get_howManyT_Items();
        if (Tn > 0) {
            String tid0 = tg2.getT_Item(0).tid;
            ItemEntry it_ent0 = tg2.findItemEntryMatchTid(tid0);
            int ie_n0 = it_ent0.howManyItems();
            for (int i0 = 0; i0 < ie_n0; i0++) {
                tg2.pickItemAt(it_ent0.ord, i0, 0);
                if (Tn > 1) {
                    String tid1 = tg2.getT_Item(1).tid;
                    ItemEntry it_ent1 = tg2.findItemEntryMatchTid(tid1);
                    int ie_n1 = it_ent1.howManyItems();
                    for (int i1 = 0; i1 < ie_n1; i1++) {
                        tg2.pickItemAt(it_ent1.ord, i1, 1);
                        if (Tn > 2) {
                            String tid2 = tg2.getT_Item(2).tid;
                            ItemEntry it_ent2 = tg2.findItemEntryMatchTid(tid2);
                            int ie_n2 = it_ent2.howManyItems();
                            for (int i2 = 0; i2 < ie_n2; i2++) {
                                tg2.pickItemAt(it_ent2.ord, i2, 2);
                                if (Tn > 3) {
                                    String tid3 = tg2.getT_Item(3).tid;
                                    ItemEntry it_ent3 = tg2.findItemEntryMatchTid(tid3);
                                    int ie_n3 = it_ent3.howManyItems();
                                    for (int i3 = 0; i3 < ie_n3; i3++) {
                                        tg2.pickItemAt(it_ent3.ord, i3, 3);
                                        if (Tn > 4) {
                                            String tid4 = tg2.getT_Item(4).tid;
                                            ItemEntry it_ent4 = tg2.findItemEntryMatchTid(tid4);
                                            int ie_n4 = it_ent4.howManyItems();
                                            for (int i4 = 0; i4 < ie_n4; i4++) {
                                                tg2.pickItemAt(it_ent4.ord, i4, 4);
                                                if (Tn > 5) {
                                                    String tid5 = tg2.getT_Item(5).tid;
                                                    ItemEntry it_ent5 = tg2.findItemEntryMatchTid(tid5);
                                                    int ie_n5 = it_ent5.howManyItems();
                                                    for (int i5 = 0; i5 < ie_n5; i5++) {
                                                        tg2.pickItemAt(it_ent5.ord, i5, 5);
                                                        li.add(tg2.getAllText(sep));
                                                    }
                                                } else {
                                                    li.add(tg2.getAllText(sep));
                                                }
                                            }
                                        } else {
                                            li.add(tg2.getAllText(sep));
                                        }
                                    }
                                } else {
                                    li.add(tg2.getAllText(sep));
                                }
                            }
                        } else {
                            li.add(tg2.getAllText(sep));
                        }
                    }
                } else {
                    li.add(tg2.getAllText(sep));
                }
            }
        } else {
            li.add(tg2.getAllText(sep));
        }
        return (String[]) li.toArray(new String[0]);
    }

    public String[] getAllTargetCombinations(String sep) {
        List<String> li = new ArrayList();

        Target tg2 = this;

        int Tn = tg2.get_howManyT_Items();

        if (Tn > 0) {
            String tid0 = tg2.getT_Item(0).tid;
            ItemEntryVirtualList it_ent0 = tg2.findItemEntryVirtualListMatchTid(tid0);
            int ie_n0 = it_ent0.howManyItems();
            for (int i0 = 0; i0 < ie_n0; i0++) {
                Item itm0 = it_ent0.getItemAt(i0);
                tg2.pickItemAt(itm0.getIt_ent().ord, itm0.getOrd(), 0);
                if (Tn > 1) {
                    String tid1 = tg2.getT_Item(1).tid;
                    ItemEntryVirtualList it_ent1 = tg2.findItemEntryVirtualListMatchTid(tid1);
                    int ie_n1 = it_ent1.howManyItems();
                    for (int i1 = 0; i1 < ie_n1; i1++) {
                        Item itm1 = it_ent1.getItemAt(i1);
                        tg2.pickItemAt(itm1.getIt_ent().ord, itm1.getOrd(), 1);
                        if (Tn > 2) {
                            String tid2 = tg2.getT_Item(2).tid;
                            ItemEntryVirtualList it_ent2 = tg2.findItemEntryVirtualListMatchTid(tid2);
                            int ie_n2 = it_ent2.howManyItems();
                            for (int i2 = 0; i2 < ie_n2; i2++) {
                                Item itm2 = it_ent2.getItemAt(i2);
                                tg2.pickItemAt(itm2.getIt_ent().ord, itm2.getOrd(), 2);
                                if (Tn > 3) {
                                    String tid3 = tg2.getT_Item(3).tid;
                                    ItemEntryVirtualList it_ent3 = tg2.findItemEntryVirtualListMatchTid(tid3);
                                    int ie_n3 = it_ent3.howManyItems();
                                    for (int i3 = 0; i3 < ie_n3; i3++) {
                                        Item itm3 = it_ent3.getItemAt(i3);
                                        tg2.pickItemAt(itm3.getIt_ent().ord, itm3.getOrd(), 3);
                                        if (Tn > 4) {
                                            String tid4 = tg2.getT_Item(4).tid;
                                            ItemEntryVirtualList it_ent4 = tg2.findItemEntryVirtualListMatchTid(tid4);
                                            int ie_n4 = it_ent4.howManyItems();
                                            for (int i4 = 0; i4 < ie_n4; i4++) {
                                                Item itm4 = it_ent4.getItemAt(i4);
                                                tg2.pickItemAt(itm4.getIt_ent().ord, itm4.getOrd(), 4);
                                                if (Tn > 5) {
                                                    String tid5 = tg2.getT_Item(5).tid;
                                                    ItemEntryVirtualList it_ent5 = tg2.findItemEntryVirtualListMatchTid(tid5);
                                                    int ie_n5 = it_ent5.howManyItems();
                                                    for (int i5 = 0; i5 < ie_n5; i5++) {
                                                        Item itm5 = it_ent5.getItemAt(i5);
                                                        tg2.pickItemAt(itm5.getIt_ent().ord, itm5.getOrd(), 5);
                                                        li.add(tg2.getAllText(sep));
                                                    }
                                                } else {
                                                    li.add(tg2.getAllText(sep));
                                                }
                                            }
                                        } else {
                                            li.add(tg2.getAllText(sep));
                                        }
                                    }
                                } else {
                                    li.add(tg2.getAllText(sep));
                                }
                            }
                        } else {
                            li.add(tg2.getAllText(sep));
                        }
                    }
                } else {
                    li.add(tg2.getAllText(sep));
                }
            }
        } else {
            li.add(tg2.getAllText(sep));
        }
        return (String[]) li.toArray(new String[0]);
    }

    public String[] getAllTargetCombinationsEx(String sep, char delim) {
        List<String> li = new ArrayList();

        Target tg2 = this;

        int Tn = tg2.get_howManyT_Items();

        if (Tn > 0) {
            String tid0 = tg2.getT_Item(0).tid;
            ItemEntryVirtualList it_ent0 = tg2.findItemEntryVirtualListMatchTid(tid0);
            int ie_n0 = it_ent0.howManyItems();
            for (int i0 = 0; i0 < ie_n0; i0++) {
                Item itm0 = it_ent0.getItemAt(i0);
                tg2.pickItemAt(itm0.getIt_ent().ord, itm0.getOrd(), 0);
                if (Tn > 1) {
                    String tid1 = tg2.getT_Item(1).tid;
                    ItemEntryVirtualList it_ent1 = tg2.findItemEntryVirtualListMatchTid(tid1);
                    int ie_n1 = it_ent1.howManyItems();
                    for (int i1 = 0; i1 < ie_n1; i1++) {
                        Item itm1 = it_ent1.getItemAt(i1);
                        tg2.pickItemAt(itm1.getIt_ent().ord, itm1.getOrd(), 1);
                        if (Tn > 2) {
                            String tid2 = tg2.getT_Item(2).tid;
                            ItemEntryVirtualList it_ent2 = tg2.findItemEntryVirtualListMatchTid(tid2);
                            int ie_n2 = it_ent2.howManyItems();
                            for (int i2 = 0; i2 < ie_n2; i2++) {
                                Item itm2 = it_ent2.getItemAt(i2);
                                tg2.pickItemAt(itm2.getIt_ent().ord, itm2.getOrd(), 2);
                                if (Tn > 3) {
                                    String tid3 = tg2.getT_Item(3).tid;
                                    ItemEntryVirtualList it_ent3 = tg2.findItemEntryVirtualListMatchTid(tid3);
                                    int ie_n3 = it_ent3.howManyItems();
                                    for (int i3 = 0; i3 < ie_n3; i3++) {
                                        Item itm3 = it_ent3.getItemAt(i3);
                                        tg2.pickItemAt(itm3.getIt_ent().ord, itm3.getOrd(), 3);
                                        if (Tn > 4) {
                                            String tid4 = tg2.getT_Item(4).tid;
                                            ItemEntryVirtualList it_ent4 = tg2.findItemEntryVirtualListMatchTid(tid4);
                                            int ie_n4 = it_ent4.howManyItems();
                                            for (int i4 = 0; i4 < ie_n4; i4++) {
                                                Item itm4 = it_ent4.getItemAt(i4);
                                                tg2.pickItemAt(itm4.getIt_ent().ord, itm4.getOrd(), 4);
                                                if (Tn > 5) {
                                                    String tid5 = tg2.getT_Item(5).tid;
                                                    ItemEntryVirtualList it_ent5 = tg2.findItemEntryVirtualListMatchTid(tid5);
                                                    int ie_n5 = it_ent5.howManyItems();
                                                    for (int i5 = 0; i5 < ie_n5; i5++) {
                                                        Item itm5 = it_ent5.getItemAt(i5);
                                                        tg2.pickItemAt(itm5.getIt_ent().ord, itm5.getOrd(), 5);
                                                        li.add(tg2.getAllText(sep, delim));
                                                    }
                                                } else {
                                                    li.add(tg2.getAllText(sep, delim));
                                                }
                                            }
                                        } else {
                                            li.add(tg2.getAllText(sep, delim));
                                        }
                                    }
                                } else {
                                    li.add(tg2.getAllText(sep, delim));
                                }
                            }
                        } else {
                            li.add(tg2.getAllText(sep, delim));
                        }
                    }
                } else {
                    li.add(tg2.getAllText(sep, delim));
                }
            }
        } else {
            li.add(tg2.getAllText(sep, delim));
        }
        return (String[]) li.toArray(new String[0]);
    }

    public TargetCombinations getAllTargetCombinationsEx2(Lesson lesson) {
        TargetCombinations tc = new TargetCombinations();

        // add lesson icons
        String lln = lesson.getLoadedFName();
        int ix = lln.lastIndexOf("/");
        if ( ix != -1 ) {
            String llnBase = lln.substring(0, ix);
            addLessonIcon(tc, llnBase + "/image.png");
            addLessonIcon(tc, llnBase + "/image_enter.png");
        }
        List<String> media = lesson.action_specific.getMedia();
        for (String s : media)
            tc.getDep_set().add(new TargetCombinations.TCItem(s));

        Target tg2 = this;

        int Tn = tg2.get_howManyT_Items();

        Set<String> set = new HashSet<String>();

        if (Tn > 0) {
            String tid0 = tg2.getT_Item(0).tid;
            ItemEntryVirtualList it_ent0 = tg2.findItemEntryVirtualListMatchTid(tid0);
            int ie_n0 = it_ent0.howManyItems();
            for (int i0 = 0; i0 < ie_n0; i0++) {
                Item itm0 = it_ent0.getItemAt(i0);
                tg2.pickItemAt(itm0.getIt_ent().ord, itm0.getOrd(), 0);
                if (Tn > 1) {
                    String tid1 = tg2.getT_Item(1).tid;
                    ItemEntryVirtualList it_ent1 = tg2.findItemEntryVirtualListMatchTid(tid1);
                    int ie_n1 = it_ent1.howManyItems();
                    for (int i1 = 0; i1 < ie_n1; i1++) {
                        Item itm1 = it_ent1.getItemAt(i1);
                        tg2.pickItemAt(itm1.getIt_ent().ord, itm1.getOrd(), 1);
                        if (Tn > 2) {
                            String tid2 = tg2.getT_Item(2).tid;
                            ItemEntryVirtualList it_ent2 = tg2.findItemEntryVirtualListMatchTid(tid2);
                            int ie_n2 = it_ent2.howManyItems();
                            for (int i2 = 0; i2 < ie_n2; i2++) {
                                Item itm2 = it_ent2.getItemAt(i2);
                                tg2.pickItemAt(itm2.getIt_ent().ord, itm2.getOrd(), 2);
                                if (Tn > 3) {
                                    String tid3 = tg2.getT_Item(3).tid;
                                    ItemEntryVirtualList it_ent3 = tg2.findItemEntryVirtualListMatchTid(tid3);
                                    int ie_n3 = it_ent3.howManyItems();
                                    for (int i3 = 0; i3 < ie_n3; i3++) {
                                        Item itm3 = it_ent3.getItemAt(i3);
                                        tg2.pickItemAt(itm3.getIt_ent().ord, itm3.getOrd(), 3);
                                        if (Tn > 4) {
                                            String tid4 = tg2.getT_Item(4).tid;
                                            ItemEntryVirtualList it_ent4 = tg2.findItemEntryVirtualListMatchTid(tid4);
                                            int ie_n4 = it_ent4.howManyItems();
                                            for (int i4 = 0; i4 < ie_n4; i4++) {
                                                Item itm4 = it_ent4.getItemAt(i4);
                                                tg2.pickItemAt(itm4.getIt_ent().ord, itm4.getOrd(), 4);
                                                if (Tn > 5) {
                                                    String tid5 = tg2.getT_Item(5).tid;
                                                    ItemEntryVirtualList it_ent5 = tg2.findItemEntryVirtualListMatchTid(tid5);
                                                    int ie_n5 = it_ent5.howManyItems();
                                                    for (int i5 = 0; i5 < ie_n5; i5++) {
                                                        Item itm5 = it_ent5.getItemAt(i5);
                                                        tg2.pickItemAt(itm5.getIt_ent().ord, itm5.getOrd(), 5);
                                                        tg2.update(tc);
                                                    }
                                                } else {
                                                    tg2.update(tc);
                                                }
                                            }
                                        } else {
                                            tg2.update(tc);
                                        }
                                    }
                                } else {
                                    tg2.update(tc);
                                }
                            }
                        } else {
                            tg2.update(tc);
                        }
                    }
                } else {
                    tg2.update(tc);
                }
            }
        } else {
            tg2.update(tc);
        }

        return tc;
    }

    private void addLessonIcon(TargetCombinations tc, String fn) {
        if (OmegaContext.omegaAssetsExist(fn))
            tc.getDep_set().add(new TargetCombinations.TCItem(fn));
    }

    private void update(TargetCombinations tc) {
        Target nTarget = new Target();
        tc.getTg_set().add(nTarget);     // not populated yet

        List<String> sound_list = getAll_Sound_Items();
        for (String s : sound_list) {
            String fn = "media" + File.separator + s;
            List<TargetCombinations.TCItem> l = expandVariants(fn);
            boolean oneExist = false;
            for (TargetCombinations.TCItem f : l) {
                if (OmegaContext.omegaAssetsExist(f.getFn())) {
                    tc.getDep_set().add(f);
                    oneExist = true;
                } else {
                    ;//tc.dep_set.add(new TargetCombinations.TCItem(f, false));
                }
            }
            if (!oneExist) {
                tc.getDep_set().add(new TargetCombinations.TCItem(fn));
            }
        }
/*
        Element asel = .findElement("action_specific", 0);
        if (asel != null) {
            for (int i = 0; i < 1000; i++) {
                Element eb = asel.findElement("value", i);
                if (eb != null) {
                    String val = eb.findAttr("val");
                    if (!SundryUtils.empty(val)) {
                        tc.dep_set.add(OmegaContext.media() + val);
                    }
                }
            }
        }
*/

        for (Object o : t_items) {
            T_Item titm = (T_Item) o;
            if (titm.item != null && !SundryUtils.empty(titm.item.getAction_fname())) {
                String af_alt = (titm.item).getActionFile();
                String af = titm.getFilledActionText();
                if (SundryUtils.empty(af))
                    continue;
                ;
                Anim_Repository ar = new Anim_Repository();
                Element anim_el_root = ar.open(null, OmegaContext.omegaAssets(af));
                if (anim_el_root == null)
                    continue;
                ;
                Log.getLogger().info(anim_el_root.toString());

                Element cel = anim_el_root.findElement("Canvas", 0);
                if (cel != null) {
                    Element eb = anim_el_root.findElement("background", 0);
                    if (eb != null) {
                        String s = eb.findAttr("name");
                        if (s != null) {
                            String fn = "media" + File.separator + s;
                            tc.getDep_set().add(new TargetCombinations.TCItem(fn));
                        }
                    }
                }

                Element ael = anim_el_root.findElement("AllActors", 0);
                if (ael != null) {
                    for (int i = 0; i < 10; i++) {
                        Element eb = anim_el_root.findElement("Actor", i);
                        if (eb != null) {
                            String s = eb.findAttr("name");
                            if (s != null) {
                                String ms = "media" + File.separator + s;
                                tc.getDep_set().add(new TargetCombinations.TCItem(ms));
                                List<TargetCombinations.TCItem> aiL = attributedImages(ms);
                                for (TargetCombinations.TCItem ai : aiL)
                                    tc.getDep_set().add(ai);
                            }
                        }
                    }
                }

                for (int ti = 0; ti < 100; ti++) {
                    Element mel = anim_el_root.findElement("TimeMarker", ti);
                    if (mel != null) {
                        for (int ei = 0; ei < 100; ei++) {
                            Element tel = mel.findElement("T_Event", ei);
                            if (tel != null) {
                                for (int i = 0; i < 10; i++) {
                                    Element eb = tel.findElement("TriggerEvent", i);
                                    if (eb != null) {
                                        String cmd = eb.findAttr("cmd");
                                        if (cmd != null && cmd.equals("PlaySound")) {
                                            String sf_ = eb.findAttr("arg");
                                            if (!SundryUtils.empty(sf_)) {
                                                String sf = fillVarHere(titm.ord, sf_);
                                                String fn = "media" + File.separator + sf;
                                                List<TargetCombinations.TCItem> l = expandVariants(fn);
                                                boolean oneExist = false;
                                                for (TargetCombinations.TCItem f : l) {
                                                    if (OmegaContext.omegaAssetsExist(f.getFn())) {
                                                        tc.getDep_set().add(f);
                                                        oneExist = true;
                                                    } else {
                                                        ;//tc.dep_set.add(new TargetCombinations.TCItem(f, false));
                                                    }
                                                }
                                                if (!oneExist) {
                                                    tc.getDep_set().add(new TargetCombinations.TCItem(fn));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                tc.getDep_set().add(new TargetCombinations.TCItem(af));
            }
        }
    }

    private List<TargetCombinations.TCItem> expandVariants(String fn) {
        List<TargetCombinations.TCItem> l = new ArrayList<>();
        l.add(new TargetCombinations.TCItem(fn));
        String alt = fn.replaceAll("\\.wav$", ".mp3");
        l.add(new TargetCombinations.TCItem(alt, "wav"));
        alt = fn.replaceAll("\\.mpg$", ".mp4");
        l.add(new TargetCombinations.TCItem(alt, "mpg"));
        alt = fn.replaceAll("\\.avi$", ".mp4");
        l.add(new TargetCombinations.TCItem(alt, "avi"));
        alt = fn.replaceAll("\\.mov$", ".mp4");
        l.add(new TargetCombinations.TCItem(alt, "mov"));
        return l;
    }

    private List<TargetCombinations.TCItem> attributedImages(String ms) {
        List<TargetCombinations.TCItem> li = new ArrayList<>();
        String fName = OmegaContext.omegaAssets(ms);
        File fBase = new File(fName);
        String fn = fBase.getName();
        int ix = fn.lastIndexOf('.');
        if (ix == -1)
            return li;
        String fnNEx = fn.substring(0, ix) + "-";
        File dir = fBase.getParentFile();
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.getName().startsWith(fnNEx)) {
                String fn2 = f.getPath();
                li.add(new TargetCombinations.TCItem(OmegaContext.antiOmegaAssets(fn2)));
            }
        }
        return li;
    }

    String[] gDta(Target tg2) {
        String[] sndA = tg2.getAllSounds();
        String snd = SundryUtils.a2s(sndA);
        return new String[]{tg2.getAllText(),
                SundryUtils.a2s(tg2.getAll_Lid_Item()),
                tg2.getActionFileName(99),         // all
                snd,
                SundryUtils.a2s(tg2.getAll_Lid_Target())

        };
    }

    private String[][] getAllTargetCombinationsAndMore(boolean dummy) {
        List<String[]> li = new ArrayList();

        Target tg2 = this;

        int Tn = tg2.get_howManyT_Items();

        if (Tn > 0) {
            String tid0 = tg2.getT_Item(0).tid;
            ItemEntry it_ent0 = tg2.findItemEntryMatchTid(tid0);
            int ie_n0 = it_ent0.howManyItems();
            for (int i0 = 0; i0 < ie_n0; i0++) {
                tg2.pickItemAtDummy(dummy, it_ent0.ord, i0, 0);
                if (Tn > 1) {
                    String tid1 = tg2.getT_Item(1).tid;
                    ItemEntry it_ent1 = tg2.findItemEntryMatchTid(tid1);
                    int ie_n1 = it_ent1.howManyItems();
                    for (int i1 = 0; i1 < ie_n1; i1++) {
                        tg2.pickItemAtDummy(dummy, it_ent1.ord, i1, 1);
                        if (Tn > 2) {
                            String tid2 = tg2.getT_Item(2).tid;
                            ItemEntry it_ent2 = tg2.findItemEntryMatchTid(tid2);
                            int ie_n2 = it_ent2.howManyItems();
                            for (int i2 = 0; i2 < ie_n2; i2++) {
                                tg2.pickItemAtDummy(dummy, it_ent2.ord, i2, 2);
                                if (Tn > 3) {
                                    String tid3 = tg2.getT_Item(3).tid;
                                    ItemEntry it_ent3 = tg2.findItemEntryMatchTid(tid3);
                                    int ie_n3 = it_ent3.howManyItems();
                                    for (int i3 = 0; i3 < ie_n3; i3++) {
                                        tg2.pickItemAtDummy(dummy, it_ent3.ord, i3, 3);
                                        if (Tn > 4) {
                                            String tid4 = tg2.getT_Item(4).tid;
                                            ItemEntry it_ent4 = tg2.findItemEntryMatchTid(tid4);
                                            int ie_n4 = it_ent4.howManyItems();
                                            for (int i4 = 0; i4 < ie_n4; i4++) {
                                                tg2.pickItemAtDummy(dummy, it_ent4.ord, i4, 4);
                                                if (Tn > 5) {
                                                    String tid5 = tg2.getT_Item(5).tid;
                                                    ItemEntry it_ent5 = tg2.findItemEntryMatchTid(tid5);
                                                    int ie_n5 = it_ent5.howManyItems();
                                                    for (int i5 = 0; i5 < ie_n5; i5++) {
                                                        tg2.pickItemAtDummy(dummy, it_ent5.ord, i5, 5);
                                                        li.add(gDta(tg2));
                                                    }
                                                } else {
                                                    li.add(gDta(tg2));
                                                }
                                            }
                                        } else {
                                            li.add(gDta(tg2));
                                        }
                                    }
                                } else {
                                    li.add(gDta(tg2));
                                }
                            }
                        } else {
                            li.add(gDta(tg2));
                        }
                    }
                } else {
                    li.add(gDta(tg2));
                }
            }
        } else {
            li.add(gDta(tg2));
        }
        return (String[][]) li.toArray(new String[0][0]);
    }

    public int[][] getAllTargetCombinationsIndexes(String txt) {
        List<String[]> li = new ArrayList();

        Target tg2 = this;

        int Tn = tg2.get_howManyT_Items();
//ZZ

        if (Tn > 0) {
            String tid0 = tg2.getT_Item(0).tid;
            ItemEntry[] it_ent0 = tg2.findItemEntryMatchTidAll(tid0);
            int ie_n0 = A_howManyItems(it_ent0);
            for (int i0 = 0; i0 < ie_n0; i0++) {
                tg2.pickItemAtEx(A_getX(it_ent0, i0), A_getY(it_ent0, i0), 0);
                if (Tn > 1) {
                    String tid1 = tg2.getT_Item(1).tid;
                    ItemEntry[] it_ent1 = tg2.findItemEntryMatchTidAll(tid1);
                    int ie_n1 = A_howManyItems(it_ent1);
                    for (int i1 = 0; i1 < ie_n1; i1++) {
                        tg2.pickItemAtEx(A_getX(it_ent1, i1), A_getY(it_ent1, i1), 1);
                        if (Tn > 2) {
                            String tid2 = tg2.getT_Item(2).tid;
                            ItemEntry[] it_ent2 = tg2.findItemEntryMatchTidAll(tid2);
                            int ie_n2 = A_howManyItems(it_ent2);
                            for (int i2 = 0; i2 < ie_n2; i2++) {
                                tg2.pickItemAtEx(A_getX(it_ent2, i2), A_getY(it_ent2, i2), 2);
                                if (Tn > 3) {
                                    String tid3 = tg2.getT_Item(3).tid;
                                    ItemEntry[] it_ent3 = tg2.findItemEntryMatchTidAll(tid3);
                                    int ie_n3 = A_howManyItems(it_ent3);
                                    for (int i3 = 0; i3 < ie_n3; i3++) {
                                        tg2.pickItemAtEx(A_getX(it_ent3, i3), A_getY(it_ent3, i3), 3);
                                        if (Tn > 4) {
                                            String tid4 = tg2.getT_Item(4).tid;
                                            ItemEntry[] it_ent4 = tg2.findItemEntryMatchTidAll(tid4);
                                            int ie_n4 = A_howManyItems(it_ent4);
                                            for (int i4 = 0; i4 < ie_n4; i4++) {
                                                tg2.pickItemAtEx(A_getX(it_ent4, i4), A_getY(it_ent4, i4), 4);
                                                if (Tn > 5) {
                                                    String tid5 = tg2.getT_Item(5).tid;
                                                    ItemEntry[] it_ent5 = tg2.findItemEntryMatchTidAll(tid5);
                                                    int ie_n5 = A_howManyItems(it_ent5);
                                                    for (int i5 = 0; i5 < ie_n5; i5++) {
                                                        tg2.pickItemAtEx(A_getX(it_ent5, i5), A_getY(it_ent5, i5), 5);
                                                        String s = tg2.getAllText();
                                                        if (eq(txt, s))
                                                            return new int[][]{
                                                                    new int[]{i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)},
                                                                    new int[]{i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)},
                                                                    new int[]{i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2)},
                                                                    new int[]{i3, A_getX(it_ent3, i3), A_getY(it_ent3, i3)},
                                                                    new int[]{i4, A_getX(it_ent4, i4), A_getY(it_ent4, i4)},
                                                                    new int[]{i5, A_getX(it_ent5, i5), A_getY(it_ent5, i5)}
                                                            };
                                                    }
                                                } else {
                                                    String s = tg2.getAllText();
                                                    if (eq(txt, s))
                                                        return new int[][]{
                                                                new int[]{i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)},
                                                                new int[]{i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)},
                                                                new int[]{i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2)},
                                                                new int[]{i3, A_getX(it_ent3, i3), A_getY(it_ent3, i3)},
                                                                new int[]{i4, A_getX(it_ent4, i4), A_getY(it_ent4, i4)}
                                                        };
                                                }
                                            }
                                        } else {
                                            String s = tg2.getAllText();
                                            if (eq(txt, s))
                                                return new int[][]{
                                                        new int[]{i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)},
                                                        new int[]{i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)},
                                                        new int[]{i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2)},
                                                        new int[]{i3, A_getX(it_ent3, i3), A_getY(it_ent3, i3)}
                                                };
                                        }
                                    }
                                } else {
                                    String s = tg2.getAllText();
                                    if (eq(txt, s))
                                        return new int[][]{
                                                new int[]{i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)},
                                                new int[]{i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)},
                                                new int[]{i2, A_getX(it_ent2, i2), A_getY(it_ent2, i2)}
                                        };
                                }
                            }
                        } else {
                            String s = tg2.getAllText();
                            if (eq(txt, s))
                                return new int[][]{
                                        new int[]{i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)},
                                        new int[]{i1, A_getX(it_ent1, i1), A_getY(it_ent1, i1)}
                                };
                        }
                    }
                } else {
                    String s = tg2.getAllText();
                    if (eq(txt, s))
                        return new int[][]{new int[]{i0, A_getX(it_ent0, i0), A_getY(it_ent0, i0)}};
                }
            }
        } else {
            String s = tg2.getAllText();
            if (eq(txt, s))
                return new int[0][0];
        }
        return new int[0][0];
    }

    private boolean eq(String s1, String s2) {
        if (Tr)
            OmegaContext.sout_log.getLogger().info("ERR: " + "eq ." + s1 + '.' + s2 + '.' + s1.equalsIgnoreCase(s2));
        return s1.equalsIgnoreCase(s2);
    }

    private int A_howManyItems(ItemEntry[] it_entA) {
        int n = 0;
        if (Tr) OmegaContext.sout_log.getLogger().info("ERR: " + "howMany( " + SundryUtils.a2s(it_entA));
        for (int i = 0; i < it_entA.length; i++) {
            if (Tr) OmegaContext.sout_log.getLogger().info("ERR: " + "howMany " + i + ' ' + it_entA[i].howManyItems());
            n += it_entA[i].howManyItems();
        }
        if (Tr) OmegaContext.sout_log.getLogger().info("ERR: " + "howMany) " + n);
        return n;
    }

    private int A_getX(ItemEntry[] it_entA, int in) {
        if (Tr) OmegaContext.sout_log.getLogger().info("ERR: " + "getX( " + in);
        for (int i = 0; i < it_entA.length; i++) {
            if (in < it_entA[i].howManyItems()) {
                if (Tr)
                    OmegaContext.sout_log.getLogger().info("ERR: " + "getX) " + it_entA[i].howManyItems() + ' ' + i);
                return it_entA[0].ord + i;
            } else {
                in -= it_entA[i].howManyItems();
                if (Tr)
                    OmegaContext.sout_log.getLogger().info("ERR: " + "getX) - " + it_entA[i].howManyItems() + ' ' + i + ' ' + in);
            }
        }
        if (Tr) OmegaContext.sout_log.getLogger().info("ERR: " + "getX) -1");
        return -1;
    }

    private int A_getY(ItemEntry[] it_entA, int in) {
        if (Tr) OmegaContext.sout_log.getLogger().info("ERR: " + "getY( " + in);
        for (int i = 0; i < it_entA.length; i++) {
            if (in < it_entA[i].howManyItems()) {
                if (Tr)
                    OmegaContext.sout_log.getLogger().info("ERR: " + "getY) " + it_entA[i].howManyItems() + ' ' + i + ' ' + in);
                return in;
            } else {
                in -= it_entA[i].howManyItems();
                if (Tr)
                    OmegaContext.sout_log.getLogger().info("ERR: " + "getX) - " + it_entA[i].howManyItems() + ' ' + i + ' ' + in);
            }
        }
        if (Tr) OmegaContext.sout_log.getLogger().info("ERR: " + "getY) -1");
        return -1;
    }

    public String getStoryNext() {
        String s = story_next;
        if (s == null)
            return null;
        s = s.replaceAll("lesson-[a-zA-Z]*/active", OmegaContext.omegaAssets("lesson-" + OmegaContext.getLessonLang() + "/active"));   // LESSON-DIR-A
        return s;
    }

    public String toString() {
        return "Target{t_item=" + t_items + ", items=" + items + "}";
    }
}
