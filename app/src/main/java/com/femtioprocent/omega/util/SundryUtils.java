package com.femtioprocent.omega.util;

import com.femtioprocent.omega.OmegaContext;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SundryUtils {
    private static int count = 0;

    public static void gcStat() {
        gcStat("");
    }

    public static synchronized void gcStat(String msg) {
        long ct = System.currentTimeMillis();
        count++;
        long free1 = Runtime.getRuntime().freeMemory();
        System.gc();
        // System.gc();
        long free2 = Runtime.getRuntime().freeMemory();
        long ctg = System.currentTimeMillis();
        OmegaContext.sout_log.getLogger().info
                ("count (max total free) used -> used [freed] gc: " + count + " (" + Runtime.getRuntime().maxMemory() * 0.000001 + ' '
                + Runtime.getRuntime().totalMemory() * 0.000001 + ' ' + free2 * 0.000001 + ") "
                + (Runtime.getRuntime().totalMemory() - free1) * 0.000001 + " -> " + (Runtime.getRuntime().totalMemory() - free2)
                * 0.000001 + " [" + (free1 - free2) * 0.000001 + "] " + (ctg - ct) + " ms " + msg);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> asList(T v) {
        List<T> l = new ArrayList();
        l.add(v);
        return l;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> asList(T... va) {
        List<T> l = new ArrayList();
        for (T t : va)
            l.add(t);
        return l;
    }

    public static String formatDateDiff(Date d1, Date d2) {
        return formatMilliTime(d1.getTime(), d2.getTime(), false, true);
    }

    public static String formatMilliTime(long l1, long l2, boolean suppressZero, boolean withMS) {
        long diff = l1 - l2;
        String prefix = "";
        if (diff < 0) {
            diff = -diff;
            prefix = "-";
        }
        int ms = (int) (diff % 1000);
        diff /= 1000;
        int sec = (int) (diff % 60);
        diff /= 60;
        int min = (int) (diff % 60);
        diff /= 60;
        int hour = (int) (diff % 24);
        diff /= 24;
        int day = (int) diff;
        String mss = "00" + ms;
        int l = mss.length();
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        if (!suppressZero || day != 0) {
            sb.append(day);
            sb.append("d");
        }
        if (!suppressZero || hour != 0) {
            sb.append(hour);
            sb.append("h");
        }
        if (!suppressZero || min != 0) {
            sb.append(min);
            sb.append("m");
        }
        sb.append(sec);
        sb.append("s");
        if (withMS) {
            sb.append(mss.substring(l - 3));
        }
        return sb.toString();
    }

    public static void trace(int jsp_line) {
        try {
            throw new Exception("for tracing");
        } catch (Exception ex) {
            StackTraceElement[] stack = ex.getStackTrace();
            int i = 1;
            OmegaContext.sout_log.getLogger().info("STACK: " + i + "java:" + (stack[i].getLineNumber()) + " = jsp+" + (stack[i].getLineNumber() - jsp_line) + " jsp:"
                    + jsp_line + " file: " + stack[i].getFileName());
        }
    }

    /**
     * split a list into smaller list of lists, each sublist having max size
     *
     * @param <T>
     * @param list
     * @param size
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> list, int size) {
        List<List<T>> split = new ArrayList<List<T>>();
        for (int i = 0; i < list.size(); i += size) {
            int from = i;
            int to = i + size;
            if (to > list.size())
                to = list.size();
            split.add(list.subList(from, to));
        }
        return split;
    }

    /**
     * Extract all items in value list into one big. The order is arbitrary.
     *
     * @param <T>
     * @param <T2>
     * @param map
     * @return
     */
    public static <T, T2> List<T> extractAll(HashMap<T2, List<T>> map) {
        List<T> list = new ArrayList<T>();
        for (List<T> l : map.values())
            list.addAll(l);
        return list;
    }

    /**
     * Convert a list of pairs into a pair of list, join list content.
     * <p>
     * [Pair<List<La1>, List<La2>>, Pair<List<Lb1>, List<Lb2>>, ..., Pair<List<Ln1>, List<Ln2>>] -> Pair<List<La1, Lb1, Ln1>, List<La2, Lb2, Ln2>>
     *
     * @param <T>
     * @param col
     * @return
     */
    public static <T> Pair<List<T>> extractAllPair(Collection<Pair<List<T>>> col) {
        List<T> fstList = new ArrayList<T>();
        List<T> sndList = new ArrayList<T>();
        for (Pair<List<T>> pl : col) {
            fstList.addAll(pl.fst);
            sndList.addAll(pl.snd);
        }
        return new Pair(fstList, sndList);
    }

    public static Set<Integer> extractIntegers(String s, String re) {
        Set<Integer> set = new HashSet<Integer>();
        String[] sa = s.split(re);
        for (String s1 : sa) {
            try {
                int a = Integer.parseInt(s1.trim());
                set.add(a);
            } catch (Exception ex) {
                OmegaContext.sout_log.getLogger().info("Not an integer string value: " + s1);
            }
        }
        return set;
    }

    /**
     * Convert 3 column table into hashmap of hasmaps
     * <p>
     * <code>
     * {hm-1-key, hm-2-key1, hm2-value1}
     * {hm-1-key, hm-2-key2, hm2-value2}
     * {hm-1-key, hm-2-key3, hm2-value3}
     * {hm-1-key2, hm-2-key1, hm2-value4}
     * {hm-1-key2, hm-2-key2, hm2-value5}
     * {hm-1-key2, hm-2-key3, hm2-value6}
     * <p>
     * ->
     * <p>
     * {hm-1-key -> {hm-2-keyn -> hm-value}}
     * <p>
     * </code>
     *
     * @param tab
     * @return
     */
    public static HashMap<String, HashMap<String, String>> populateMapMap(String[][] tab) {
        HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
        for (String[] sa3 : tab) {
            String key1 = sa3[0];
            HashMap<String, String> m = map.get(key1);
            if (m == null) {
                m = new HashMap<String, String>();
                map.put(key1, m);
            }
            m.put(sa3[1], sa3[2]);
        }
        return map;
    }

    public static <T1, T2> SortedMap<T1, T2> convertToSortedMap(Map<T1, T2> map) {
        if (map == null)
            return null;
        if (map instanceof SortedMap<?, ?>)
            return (SortedMap<T1, T2>) map;
        SortedMap<T1, T2> sm = new TreeMap<T1, T2>();
        sm.putAll(map);
        return sm;
    }

    public static String whichMethod() {
        return whichMethod(1, false);
    }

    public static String whichMethodAndClass() {
        return whichMethod(1, true);
    }

    public static String whichMethod(int offs) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String method = stackTrace[3 + offs].getMethodName();
        return method;
    }

    public static String whichMethod(int offs, boolean withClass) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String method = stackTrace[3 + offs].getClassName() + ':' + stackTrace[3 + offs].getMethodName();
        return method;
    }

    public static Date timeSetEven(Date date) {
        if (date == null)
            return timeSetEven(new Date());
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.SECOND, now.get(Calendar.SECOND) & ~1);
        return now.getTime();
    }

    public static Date timeSetOdd(Date date) {
        if (date == null)
            return timeSetOdd(new Date());
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.SECOND, now.get(Calendar.SECOND) | 1);
        return now.getTime();
    }

    public static boolean timeIsEven(long ms) {
        long seconds = ms / 1000;
        return (seconds & 1) == 0;
    }

    public static boolean timeIsEven(Date date) {
        if (date == null)
            return false;
        return timeIsEven(date.getTime());
    }

    public static boolean timeIsOdd(long ms) {
        return !timeIsEven(ms);
    }

    public static boolean timeIsOdd(Date date) {
        return !timeIsEven(date.getTime());
    }

    public static Date currentTimeSetEven() {
        return timeSetEven(new Date());
    }

    public static Date currentTimeSetOdd() {
        return timeSetOdd(new Date());
    }

    public static Date getEarlyDateConstant() {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse("2010-01-01 00:00:00");
        } catch (ParseException e) {
            return null;
        }
    }

    public static void trim(String[] sourceArr) {
        for (int i = 0; i < sourceArr.length; i++)
            sourceArr[i] = trim(sourceArr[i]);
    }

    private static String trim(String s) {
        if (s == null)
            return null;
        return s.trim();
    }

    public static boolean empty(String s) {
        return s == null || s.length() == 0;
    }

    // simple crypt/decrypt
    static final String oef62xc = "cvbnmQWERTY";
    static final String nmdb = "RTYASDFGH";
    static final String nmsovf = "VBNM01278";
    static final String djlfb53 = "LZXfqwghjk";
    static final String svo = "lz3nmUI";
    static final String tetge5 = "ZXCVBNM01";
    static final String xr35e = "qwertyuiopa";
    static final String jbvg4 = "sdfghjklzx";
    static final String opiwehfg = "iopaQWE";
    static final String sklvnj34 = "tysOPu";
    static final String a = "456xcvC";
    static final String mv34 = "23456789";
    static final String rete4 = "UIOPASDFGHJKL";
    static final String q1 = "9derbJK";

    private static String mix(String s, String s1, String s2) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            int ix = s1.indexOf(ch);
            if (ix > -1)
                sb.append(s2.charAt(ix));
            else
                sb.append(ch);
        }
        s = sb.toString();
        return s;
    }

    public static String crypt(String s) {
        return mix(s, xr35e + jbvg4 + oef62xc + rete4 + tetge5 + mv34, sklvnj34 + opiwehfg + nmdb + a + nmsovf + q1 + djlfb53 + svo);
    }

    public static String decrypt(String s) {
        return mix(s, sklvnj34 + opiwehfg + nmdb + a + nmsovf + q1 + djlfb53 + svo, xr35e + jbvg4 + oef62xc + rete4 + tetge5 + mv34);
    }

    public static int[] convertToIntArr(String csv) {
        if (csv == null || csv.length() == 0)
            return null;
        String[] sa = csv.split(",");
        int[] ia = new int[sa.length];
        int ix = 0;
        for (String s : sa) {
            int val = Integer.parseInt(s.trim());
            ia[ix++] = val;
        }
        return ia;
    }

    /**
     * Return if the argument s is like a numeric sql id.
     *
     * @param s
     * @return
     */
    public static boolean isValueLikeNumericId(String s) {
        return s != null && s.length() > 0 && !s.equals("0");
    }

    public static String formatDisplayText(String txt) {
        return txt.replace("_", " ");
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    public static void m_sleep(int a) {
        try {
            if (a <= 0)
                a = 10;
            Thread.sleep(a);
        } catch (InterruptedException e) {
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static PrintWriter createPrintWriter(String fn) {
        return createPrintWriter(fn, false);
    }

    public static PrintWriter createPrintWriter(String fn, boolean append) {
        return createPrintWriterUTF8(fn, append);
    }

    public static PrintWriter createPrintWriter(OutputStream os) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(os), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return pw;
    }

    public static PrintWriter createPrintWriterUTF8(String fn) {
        return createPrintWriterUTF8(fn, false);
    }

    public static PrintWriter createPrintWriterUTF8(String fn, boolean append) {
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fn, append)), "UTF-8"));
            return pw;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static String pL(int a, int w, char pad) {
        String s = "" + a;
        int n = w - s.length();
        String ss = "";
        for (int i = 0; i < n; i++)
            ss += pad;
        return ss + s;
    }

    public static String pL(int a, int w) {
        return pL(a, w, ' ');
    }

    public static String padLeft(String s, int len, char ch) {
        if (s.length() < len) {
            String ps = "";
            for (int i = 0; i < len - s.length(); i++)
                ps += ch;
            return ps + s;
        }
        return s;
    }

    public static String padRight(String s, int len, char ch) {
        if (s.length() < len) {
            String ps = "";
            for (int i = 0; i < len - s.length(); i++)
                ps += ch;
            return s + ps;
        }
        return s;
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static long ct() {
        return System.currentTimeMillis();
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static String[] split(String str, String split) {
        StringTokenizer t = new StringTokenizer(str, split);

        int n = t.countTokens();
        int i = 0;
        String[] arr = new String[n];

        for (; t.hasMoreTokens(); ) {
            String word = t.nextToken();
            arr[i++] = word;
        }
        return arr;
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static String a2s(Object o, int w) {
        return arrToString(o, ",", w, ' ');
    }

    public static String a2s(Object o) {
        return arrToString(o, ",", 0, ' ');
    }

    public static String a2s(Object o, String delim) {
        return arrToString(o, delim, 0, ' ');
    }

    public static String a2s(Object o, String delim, int w, char pad) {
        return arrToString(o, delim, w, pad);
    }

    public static String arrToString(Object o) {
        return arrToString(o, ",", 0, ' ');
    }

    public static String arrToString(Object o, String delim, int w, char pad) {
        if (o == null)
            return "null";
        StringBuffer s = new StringBuffer();
        Class cls = o.getClass();
        if (cls.isArray()) {
            Class clsc = cls.getComponentType();
            if (!clsc.isPrimitive()) {
                if (clsc.isArray()) {
                    Object[] oa = (Object[]) o;
                    for (int i = 0; i < oa.length; i++)
                        s.append((i == 0 ? "" : delim) +
                                "[" + arrToString(oa[i]) + "]");
                    return s.toString();
                } else {
                    Object[] oa = (Object[]) o;
                    for (int i = 0; i < oa.length; i++)
                        s.append((i == 0 ? "" : delim) + oa[i]);
                    return s.toString();
                }
            }
            if (clsc.getName().equals("int")) {
                int[] ia = (int[]) o;
                for (int i = 0; i < ia.length; i++)
                    if (w == 0)
                        s.append((i == 0 ? "" : delim) + ia[i]);
                    else
                        s.append((i == 0 ? "" : delim) + pL(ia[i], w, pad));
                return s.toString();
            }
            if (clsc.getName().equals("char")) {
                char[] ia = (char[]) o;
                for (int i = 0; i < ia.length; i++)
                    if (ia[i] == 0)
                        s.append((i == 0 ? "" : delim) + "^@");
                    else
                        s.append((i == 0 ? "" : delim) + ia[i]);
                return s.toString();
            }
            if (clsc.getName().equals("byte")) {
                byte[] ia = (byte[]) o;
                for (int i = 0; i < ia.length; i++)
                    s.append((i == 0 ? "" : delim) + ia[i]);
                return s.toString();
            }
            if (clsc.getName().equals("boolean")) {
                boolean[] ba = (boolean[]) o;
                for (int i = 0; i < ba.length; i++)
                    s.append((i == 0 ? "" : delim) + ba[i]);
                return s.toString();
            }
        }
        return null;
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

//      public static Object copyArr(Object o) {
//  	if ( o == null )
//  	    return null;
//  	Class cls = o.getClass();
//  	if ( cls.isArray() ) {
//  	    Class clsc = cls.getComponentType();
//  	    int len = java.lang.reflect.Array.getLength(o);
//  	    Object na = java.lang.reflect.Array.newInstance(clsc, len);
//  	    System.arraycopy(o, 0, na, 0, len);
//  	    return na;
//  	}
//  	return null;
//      }

    // use:        int[] ia = new int[10];
    //             int[] ia2 = (int[])ia.clone());
// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static Object castArray(Object o, Object to) {
        if (o == null)
            return null;
        Class cls = o.getClass();
        Class tcls = to.getClass();
        if (cls.isArray()) {
            Class clsc = cls.getComponentType();
            Class tclsc = tcls.getComponentType();
            if (!tclsc.isPrimitive()) {
                throw new
                        RuntimeException("cast obj[] -> obj'[] Not supported yet");
            }
            if (tclsc.getName().equals("int")) {
                if (clsc.getName().equals("java.lang.String")) {
                    int l = java.lang.reflect.Array.getLength(o);
                    int[] ia = new int[l];
                    String[] sa = (String[]) o;
                    try {
                        for (int i = 0; i < ia.length; i++) {
                            ia[i] = Integer.parseInt(sa[i]);
                        }
                        return ia;
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                } else {
                    int l = java.lang.reflect.Array.getLength(o);
                    int[] ia = new int[l];
                    for (int i = 0; i < ia.length; i++) {
                        ia[i] = java.lang.reflect.Array.getInt(o, i);
                    }
                    return ia;
                }
//  		if ( clsc.getName().equals("byte") ) {
//  			ia[i] = java.lang.reflect.Array.getByte(o, i);
//  		    } else if ( clsc.getName().equals("short") ) {
//  			ia[i] = java.lang.reflect.Array.getShort(o, i);
//  		    } else if ( clsc.getName().equals("int") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getInt(o, i);
//  		    } else if ( clsc.getName().equals("long") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getLong(o, i);
//  		    } else if ( clsc.getName().equals("char") ) {
//  			ia[i] = java.lang.reflect.Array.getChar(o, i);
//  		    } else if ( clsc.getName().equals("double") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getDouble(o, i);
//  		    } else if ( clsc.getName().equals("float") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getFloat(o, i);
//  		    }
//  		}
            } else if (tclsc.getName().equals("double")) {
                if (clsc.getName().equals("java.lang.String")) {
                    int l = java.lang.reflect.Array.getLength(o);
                    double[] ia = new double[l];
                    String[] sa = (String[]) o;
                    for (int i = 0; i < ia.length; i++) {
                        ia[i] = tD(sa[i]);
                    }
                    return ia;
                } else {
                    int l = java.lang.reflect.Array.getLength(o);
                    double[] ia = new double[l];
                    for (int i = 0; i < ia.length; i++) {
                        ia[i] = java.lang.reflect.Array.getDouble(o, i);
                    }
                    return ia;
                }
//  		if ( clsc.getName().equals("byte") ) {
//  			ia[i] = java.lang.reflect.Array.getByte(o, i);
//  		    } else if ( clsc.getName().equals("short") ) {
//  			ia[i] = java.lang.reflect.Array.getShort(o, i);
//  		    } else if ( clsc.getName().equals("int") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getInt(o, i);
//  		    } else if ( clsc.getName().equals("long") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getLong(o, i);
//  		    } else if ( clsc.getName().equals("char") ) {
//  			ia[i] = java.lang.reflect.Array.getChar(o, i);
//  		    } else if ( clsc.getName().equals("double") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getDouble(o, i);
//  		    } else if ( clsc.getName().equals("float") ) {
//  			ia[i] = (int)java.lang.reflect.Array.getFloat(o, i);
//  		    }
//  		}
            } else if (tclsc.getName().equals("char")) {
                int l = java.lang.reflect.Array.getLength(o);
                char[] ia = new char[l];
                for (int i = 0; i < ia.length; i++) {
                    if (clsc.getName().equals("byte")) {
                        ia[i] = (char) java.lang.reflect.Array.getByte(o, i);
                    } else if (clsc.getName().equals("short")) {
                        ia[i] = (char) java.lang.reflect.Array.getShort(o, i);
                    } else if (clsc.getName().equals("int")) {
                        ia[i] = (char) java.lang.reflect.Array.getInt(o, i);
                    } else if (clsc.getName().equals("long")) {
                        ia[i] = (char) java.lang.reflect.Array.getLong(o, i);
                    } else if (clsc.getName().equals("char")) {
                        ia[i] = (char) java.lang.reflect.Array.getChar(o, i);
                    } else if (clsc.getName().equals("double")) {
                        ia[i] = (char) java.lang.reflect.Array.getDouble(o, i);
                    } else if (clsc.getName().equals("float")) {
                        ia[i] = (char) java.lang.reflect.Array.getFloat(o, i);
                    }
                }
                return ia;
            }
        }
        return null;
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    static int scrN = 5;

    public static void scrambleArr(Object a) {
        if (a == null)
            return;
        Class cls = a.getClass();
        if (cls.isArray()) {
            Class clsc = cls.getComponentType();
            if (!clsc.isPrimitive()) {
                Object[] arr = (Object[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        Object t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
            if (clsc.getName().equals("int")) {
                int[] arr = (int[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        int t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
            if (clsc.getName().equals("long")) {
                long[] arr = (long[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        long t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
            if (clsc.getName().equals("char")) {
                char[] arr = (char[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        char t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
            if (clsc.getName().equals("byte")) {
                byte[] arr = (byte[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        byte t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
            if (clsc.getName().equals("short")) {
                short[] arr = (short[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        short t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
            if (clsc.getName().equals("double")) {
                double[] arr = (double[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        double t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
            if (clsc.getName().equals("float")) {
                float[] arr = (float[]) a;
                for (int j = 0; j < scrN; j++)
                    for (int i = 0; i < arr.length; i++) {
                        int b = SundryUtils.rand(arr.length);
                        float t = arr[i];
                        arr[i] = arr[b];
                        arr[b] = t;
                    }
            }
        }
        return;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static double tD(String s) {
        try {
            Double dval = Double.valueOf(s);
            double d = dval.doubleValue();
            return d;
        } catch (Exception ex) {
            return 0.0;
        }
    }


// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static int[] upTo(int a) {
        return fromTo(0, a);
    }

    public static int[] fromTo(int a, int b) {
        int ia[] = new int[b - a];
        int ii = 0;
        for (int i = a; i < b; i++)
            ia[ii++] = i;
        return ia;
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static void swapia(int[] ia, int a, int b) {
        int c = ia[a];
        ia[a] = ia[b];
        ia[b] = c;
    }

    public static String getFileContent(String fn) {
        File f = new File(OmegaContext.omegaAssets(fn));
        if ( !f.exists() || !f.canRead() )
            return null;
        try {
	    return new String(Files.readAllBytes(f.toPath()), Charset.forName("UTF-8"));
	} catch (IOException ex) {

	}
	return null;
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    static public class Uniq {
        int max;
        int[] picked;
        int taken = 0;

        public Uniq(int max) {
            this.max = max;
            picked = SundryUtils.upTo(max);
            taken = 0;
        }

        public synchronized int getNext() {
            int c = 0;
            for (; ; ) {
                int ix = SundryUtils.rand(max);
                if (picked[ix] != -1) {
                    int r = picked[ix];
                    picked[ix] = -1;
                    taken++;
                    return r;
                } else {
                    if (++c > 5) {
                        int[] ia = new int[max - taken];
                        int ix2 = 0;
                        for (int i = 0; i < picked.length; i++)
                            if (picked[i] != -1)
                                ia[ix2++] = picked[i];
                        picked = ia;
                        max = picked.length;
                        taken = 0;
//			OmegaContext.sout_log.getLogger().info("<" + max + ">");
                        return getNext();
                    }
                }
            }
        }

        public synchronized int[] asIntArray() {
            if (taken != 0) {
                return null;
            }
            int[] ia = new int[max];
            for (int i = 0; i < ia.length; i++)
                ia[i] = getNext();
            return ia;
        }

    }

    public static Uniq createUniq(int max) {
        return new SundryUtils.Uniq(max);
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private static Random randG = new Random();

    public static int rand(int a) {
        int r = 0x7fffffff & randG.nextInt();
        return r % a;
    }

// - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public static HashMap flagAsMap(String[] argv) {
        final List argl = Arrays.asList(argv);
        HashMap flag = new HashMap();

        for (int i = 0; i < argl.size(); i++) {
            String s = (String) argl.get(i);
            if (s.startsWith("-")) {
                String ss = s.substring(1);
                int ix = ss.indexOf('=');
                if (ix != -1) {
                    String sk = ss.substring(0, ix);
                    String sv = ss.substring(ix + 1);
                    if (sv.indexOf(',') == -1)
                        flag.put(sk, sv);
                    else {
                        String[] sa = split(sv, ",");
                        flag.put(sk, sv);
                        flag.put("[SundryUtil;" + sk, sa);
                    }
                } else {
                    flag.put(ss, "");
                }
            }
        }
        return flag;
    }

    public static List argAsList(String[] argv) {
        List argl = new LinkedList(Arrays.asList(argv));

        for (int i = 0; i < argl.size(); i++) {
            String s = (String) argl.get(i);
            if (s.startsWith("-")) {
                argl.remove(i);
                i--;
            }
        }
        return argl;
    }
}
