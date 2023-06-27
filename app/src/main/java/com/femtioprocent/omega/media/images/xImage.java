package com.femtioprocent.omega.media.images;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.graphic.util.LoadImage;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.ListFilesURL;
import com.femtioprocent.omega.util.SundryUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class xImage {
    final static int DIR = 0;
    final static int BASE = 1;
    final static int ATTR = 2;
    final static int SEQ = 3;
    final static int EXT = 4;

    String name;

    String dir;
    String base;
    String attr = null;
    int seq = -1;
    String ext = "gif";
    String petask_nid = "";

    private boolean once = false;
    private boolean onceDone = false;

    int max_seq = 1;

    static long checkNow = SundryUtils.ct() + 10 * 1000;

    static private HashMap cache_dir = new HashMap();
    static private HashMap cache_imf = new HashMap();
    static private HashMap cache_seqLen = new HashMap();
    private HashMap cache_attr = new HashMap();

    class Entry {
        long time_stamp;
        Image im;
        public int cnt;

        Entry(Image im) {
            this.im = im;
            time_stamp = SundryUtils.ct();
        }

        Image getIm() {
            time_stamp = SundryUtils.ct();
            return im;
        }

        int to = 60;

        boolean isOld() {
            return SundryUtils.ct() > time_stamp + 1000 * to;
        }
    }

    ;

    public xImage(String name) {
        if ( name.contains("{*") ) {
            this.name = mkPeTaskDef(name);
            this.petask_nid = mkPeTaskNid(name);

        } else {
            this.name = name;
            this.petask_nid = "";
        }
        String[] file = splitFile(this.name);
        dir = file[DIR];
        base = file[BASE];
        attr = null;
        seq = -1;
        ext = file[EXT];
        calcMaxSeq();
        //	OmegaContext.sout_log.getLogger().info("ERR: " + "xImage created " + this);
    }

    private String mkPeTaskNid(String name) {
        String sa[] = name.split(":");
        return sa[0].replace("{*", "");
    }

    private String mkPeTaskDef(String name) {
        String sa[] = name.split(":");
        return sa[1].replace("}", "");
    }

    public void setPeTaskNid(String petnid) {
        this.petask_nid = petnid;
    }


    public xImage(xImage xim) {
        this.name = xim.name;
        this.petask_nid = xim.petask_nid;
        this.dir = xim.dir;
        this.base = xim.base;
        this.attr = xim.attr;
        this.seq = xim.seq;
        this.ext = xim.ext;
        calcMaxSeq();
        //	OmegaContext.sout_log.getLogger().info("ERR: " + "xImage cloned " + this);
    }

    public static void invalidateCache() {
        cache_dir = new HashMap();
        cache_seqLen = new HashMap();
    }

    void removeOldEntry() {
        if (SundryUtils.ct() < checkNow)
            return;

        synchronized (cache_imf) {
            ArrayList li = new ArrayList();
            Iterator it;
            it = cache_imf.keySet().iterator();
            while (it.hasNext()) {
                String k = (String) it.next();
                Entry e = (Entry) cache_imf.get(k);
                if (e.isOld())
                    li.add(k);
            }
            it = li.iterator();
            while (it.hasNext()) {
                String k = (String) it.next();
                Entry ent = (Entry) cache_imf.get(k);
                if ( ent != null ) {
                    ent.im.flush();
                }
                cache_imf.remove(k);
                OmegaContext.sout_log.getLogger().info("ERR: " + "%%%%%%%% remove from cache " + k + ", cnt:" + ent.cnt);
            }
        }

        checkNow = SundryUtils.ct() + 20 * 1000;
    }

    public static void removeAllEntry() {
        synchronized (cache_imf) {
            ArrayList li = new ArrayList();
            Iterator it;
            it = cache_imf.keySet().iterator();
            while (it.hasNext()) {
                String k = (String) it.next();
                Entry e = (Entry) cache_imf.get(k);
                if (true)
                    li.add(k);
            }
            it = li.iterator();
            while (it.hasNext()) {
                String k = (String) it.next();
                Entry ent = (Entry) cache_imf.get(k);
                if ( ent != null ) {
                    ent.im.flush();
                }
                cache_imf.remove(k);
                OmegaContext.sout_log.getLogger().info("ERR: " + "%%%%%%%% remove from cache " + k + ", cnt:" + ent.cnt);
            }
        }

        checkNow = SundryUtils.ct() + 20 * 1000;
    }

    Image getEntry(String key) {
        Entry e = (Entry) cache_imf.get(key);
        if (e == null)
            return null;

        removeOldEntry();

        e.cnt++;

        //OmegaContext.sout_log.getLogger().info("IMAGE: " + "=== loaded from cache " + key + ", cnt:" + e.cnt);
        return e.getIm();
    }

    void putEntry(String key, Image im) {
        cache_imf.put(key, new Entry(im));
    }

    private boolean hasInnerAnim() {
        return max_seq >= 0;
    }

    public boolean setInnerAnimIndex(int dt, int ix) {
        //OmegaContext.sout_log.getLogger().info("ERR: " + "ANIM setix " + ix + ' ' + seq + ' ' + max_seq + ' ' + ix_base0);
        if (hasInnerAnim()) {
            if (seq == (ix % (max_seq + 1))) {
                //System.err.println("ERR: " + "ANIM setix same  " + ix + ' ' + seq + ' ' + max_seq + ' ' + dt);
                return false; // it has not been changed
            }
            seq = ix;
            seq %= (max_seq + 1);
            //System.err.println("ERR: " + "ANIM setix true  " + ix + ' ' + seq + ' ' + max_seq + ' ' + dt);
            return true;
        }
        //System.err.println("ERR: " + "ANIM setix false " + ix + ' ' + seq + ' ' + max_seq + ' ' + dt);
        return false;
    }

//      public void incInnerAnimIndex() {
//  	if (hasInnerAnim() && onceDone == false ) {
//  	    seq++;
//  	    if ( seq == max_seq + 1 && once ) {
//  		seq = 0;
//  		onceDone = true;
//  	    }
//  	    seq %= max_seq;
//  	}
//      }

    public int getInnerAnimIndex() {
        return seq;
    }

    public int getMaxInnerAnimIndex() {
        return max_seq;
    }

    boolean calcMaxSeq() {
        max_seq = scanInnerAnimIndex();
        if (max_seq == -1) {
            seq = -1;
            return false;
        }
        seq = 0;
        return true;
    }

    private boolean isIn(String s, String sa[]) {
        for (int i = 0; i < sa.length; i++)
            if (s.equals(sa[i]))
                return true;
        return false;
    }


    private boolean isNumeric(String s) {
        try {
            int a = Integer.parseInt(s);
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    // dir '/' base '-' attr '-' seq '.' ext

    private String[] splitFile(String fn) {
        String[] sa = new String[5];

        int ix = fn.lastIndexOf('.');

        String fnL = null;

        if (ix == -1) {
            sa[EXT] = null;
            fnL = fn;
        } else {
            sa[EXT] = fn.substring(ix + 1);
            fnL = fn.substring(0, ix);
        }

        int ix_sl = fnL.lastIndexOf('/');
        ix = fnL.lastIndexOf('-');

        boolean dash_ok = true;
        if (ix != -1 && ix_sl != -1 && ix > ix_sl)
            dash_ok = false;

        if (dash_ok == false || ix == -1) {
            sa[SEQ] = null;
//	    fnL = fnL;
        } else {
            String maybee_seq = fnL.substring(ix + 1);
            if (isNumeric(maybee_seq)) {
                sa[SEQ] = maybee_seq;
                fnL = fnL.substring(0, ix);
            } else {
                sa[SEQ] = null;
//		flL = fnL;
            }
        }

        ix = fnL.lastIndexOf('-');
        dash_ok = true;
        if (ix != -1 && ix_sl != -1 && ix < ix_sl)
            dash_ok = false;

        if (dash_ok == false || ix == -1) {
            sa[ATTR] = null;
//	    fnL = fnL;
        } else {
            String attr = fnL.substring(ix + 1);
            sa[ATTR] = attr;
            fnL = fnL.substring(0, ix);
        }

        ix = fnL.lastIndexOf('/');

        if (ix == -1) {
            sa[DIR] = null;
            sa[BASE] = fnL;
        } else {
            String base = fnL.substring(ix + 1);
            sa[BASE] = base;
            sa[DIR] = fnL.substring(0, ix);
        }

        return sa;
    }

    private String[] scanDir(String dir) {
        String[] D = (String[]) cache_dir.get(dir);
        if (D != null)
            return D;

        String[] list = null;
//	String[] list = (String[])list_hm.get(dir);
        if (list == null) {
            try {
                list = ListFilesURL.getMediaList(dir);
            } catch (Exception ex) {
                OmegaContext.sout_log.getLogger().info("ERR: " + "=== " + ex + ' ' + dir);
                JOptionPane.showMessageDialog(null,
                        new String[]{
                                T.t("Nu such directory") + "!\n" +
                                        T.t("File is") + ": " + dir
                        },
                        T.t("Omega - Message"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
//	    list_hm.put(dir, list);
        }
//	OmegaContext.sout_log.getLogger().info("ERR: " + "scanDir -> " + SundryUtils.arrToString(list));
        cache_dir.put(dir, list);
        return list;
    }

    public void setAttrib(String a) {
        if (scanAttrib(a))
            attr = a;
        else
            attr = null;
        calcMaxSeq();
    }

    public void setNoAttrib() {
        setAttrib(null);
    }

    public String getAttrib() {
        return attr;
    }

    public Image getImage(Component comp) {
        String key = getFN(seq);
        Image im = null;
        synchronized (cache_imf) {
            im = getEntry(key);
            if (im != null) {
                return im;
            }
            im = LoadImage.loadAndWaitOrNull(comp, key, attr != null);
            if (im == null && attr != null) {
                setAttrib(null);
                return getImage(comp);
            }
            putEntry(key, im);
        }
        return im;
    }

    public Image getBaseImage(Component comp) {
        String key = getFNBase();
        Image im = null;
        synchronized (cache_imf) {
            im = getEntry(key);
            if (im != null) {
                return im;
            }
//	    OmegaContext.sout_log.getLogger().info("ERR: " + "¤¤¤¤¤ loading " + key);
            im = LoadImage.loadAndWaitOrNull(comp, key, false);
            putEntry(key, im);
        }
        return im;
    }

    String last_b;

    public String getFNBase() {
        if (last_b == null)
            last_b = dir + '/' + base + '.' + ext;
        return last_b;
    }

    public String getPeTaskNid() {
        return petask_nid;
    }

    int last_seq = -2;

    String last_fn_an, last_fn, last_attr;

    public String getFN(int seq) {
        if (seq < 0)
            return getFN_noSeq();

        if (attr == null) {
            if (last_seq != seq || last_fn_an == null) {
                last_fn_an = dir + '/' + base + '-' + SundryUtils.pL(seq, 2, '0') + '.' + ext;
                last_seq = seq;
            }
            return last_fn_an;
        } else {
            if (last_seq != seq || last_attr != attr || last_fn == null) {
                last_fn = dir + '/' + base + '-' + attr + '-' + SundryUtils.pL(seq, 2, '0') + '.' + ext;
                last_seq = seq;
                last_attr = attr;
            }
            return last_fn;
        }
    }

    String last_ns_an, last_ns, last_an_attr;

    public String getFN_noSeq() {
        if (attr == null) {
            if (last_ns_an == null)
                last_ns_an = dir + '/' + base + '.' + ext;
            return last_ns_an;
        } else {
            if (last_an_attr != attr || last_ns == null) {
                last_ns = dir + '/' + base + '-' + attr + '.' + ext;
                last_an_attr = attr;
            }
            return last_ns;
        }
    }

// ff-BB-00  ff-BB-01  ff-BB-02  ff-BB-03
// ff-00     ff-01     ff-02
// ff-aa-00  ff-aa-01

    private int scanInnerAnimIndex() {
        String key = getFN_noSeq();
        Integer I = (Integer) cache_seqLen.get(key);
        if (I != null)
            return I.intValue();

//	OmegaContext.sout_log.getLogger().info("ERR: " + "scanInner " + toString());
        int max = -1;

        try {
            String[] list = scanDir(dir);
            for (int i = 0; i < list.length; i++) {
                String[] file = splitFile(list[i]);
                //	    OmegaContext.sout_log.getLogger().info("ERR: " + "try " + SundryUtils.arrToString(file));
                if (file[BASE].equals(base) && ((attr == null && file[ATTR] == null) ||
                        (attr != null && attr.equals(file[ATTR])))) {
                    if (file[SEQ] != null) {
                        //		    OmegaContext.sout_log.getLogger().info("ERR: " + "FOund seq " + SundryUtils.arrToString(file));
                        int v = Integer.parseInt(file[SEQ]);
                        if (v > max)
                            max = v;
                    }
                }
            }
        } catch (Exception ex) {
        }
//	OmegaContext.sout_log.getLogger().info("ERR: " + "found max = " + max);

        cache_seqLen.put(key, max);

        return max;
    }

    private boolean scanAttrib(String a) {
//	OmegaContext.sout_log.getLogger().info("ERR: " + "scanInner Attrib " + a + " ...");

        String key = getFN_noSeq();
        Boolean B = (Boolean) cache_attr.get(key);
        if (B != null)
            return true;

        try {
            String[] list = scanDir(dir);
            for (int i = 0; i < list.length; i++) {
                String[] file = splitFile(list[i]);
                if (file[BASE].equals(base) && a != null && a.equals(file[ATTR])) {
//		OmegaContext.sout_log.getLogger().info("ERR: " + "found attr " + SundryUtils.arrToString(file));
                    cache_attr.put(key, Boolean.valueOf(true));
                    return true;
                }
            }
        } catch (Exception ex) {
        }
        return false;
    }

    public String toString() {
        return "xImage{" +
                dir + ':' +
                base + ':' +
                attr + ':' +
                seq + ':' +
                ext +
                "}";
    }

    public static void main(String[] args) {
        xImage i = new xImage("image.gif");
        i.setAttrib("a");
    }
}
