package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaContext
import java.io.*
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.Files
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object SundryUtils {
    private var count = 0
    fun gcStat() {
	gcStat("")
    }

    @Synchronized
    fun gcStat(msg: String) {
	val ct = System.currentTimeMillis()
	count++
	val free1 = Runtime.getRuntime().freeMemory()
	System.gc()
	// System.gc();
	val free2 = Runtime.getRuntime().freeMemory()
	val ctg = System.currentTimeMillis()
	Log.getLogger().info(
		"count (max total free) used -> used [freed] gc: " + count + " (" + Runtime.getRuntime()
			.maxMemory() * 0.000001 + ' ' + Runtime.getRuntime()
			.totalMemory() * 0.000001 + ' ' + free2 * 0.000001 + ") " + (Runtime.getRuntime()
			.totalMemory() - free1) * 0.000001 + " -> " + ((Runtime.getRuntime().totalMemory() - free2)
			* 0.000001) + " [" + (free1 - free2) * 0.000001 + "] " + (ctg - ct) + " ms " + msg
	)
    }

    fun <T> asList(v: T): List<T?> {
	val l: MutableList<T?> = ArrayList<T?>()
	l.add(v)
	return l
    }

    fun <T> asList(vararg va: T): List<T?> {
	val l: MutableList<T?> = ArrayList<T?>()
	for (t in va) l.add(t)
	return l
    }

    fun formatDateDiff(d1: Date, d2: Date): String {
	return formatMilliTime(d1.time, d2.time, false, true)
    }

    fun formatMilliTime(l1: Long, l2: Long, suppressZero: Boolean, withMS: Boolean): String {
	var diff = l1 - l2
	var prefix = ""
	if (diff < 0) {
	    diff = -diff
	    prefix = "-"
	}
	val ms = (diff % 1000).toInt()
	diff /= 1000
	val sec = (diff % 60).toInt()
	diff /= 60
	val min = (diff % 60).toInt()
	diff /= 60
	val hour = (diff % 24).toInt()
	diff /= 24
	val day = diff.toInt()
	val mss = "00$ms"
	val l = mss.length
	val sb = StringBuilder()
	sb.append(prefix)
	if (!suppressZero || day != 0) {
	    sb.append(day)
	    sb.append("d")
	}
	if (!suppressZero || hour != 0) {
	    sb.append(hour)
	    sb.append("h")
	}
	if (!suppressZero || min != 0) {
	    sb.append(min)
	    sb.append("m")
	}
	sb.append(sec)
	sb.append("s")
	if (withMS) {
	    sb.append(mss.substring(l - 3))
	}
	return sb.toString()
    }

    fun trace(jsp_line: Int) {
	try {
	    throw Exception("for tracing")
	} catch (ex: Exception) {
	    val stack = ex.stackTrace
	    val i = 1
	    Log.getLogger().info(
		    "STACK: " + i + "java:" + stack[i].lineNumber + " = jsp+" + (stack[i].lineNumber - jsp_line) + " jsp:"
			    + jsp_line + " file: " + stack[i].fileName
	    )
	}
    }

    /**
     * split a list into smaller list of lists, each sublist having max size
     *
     * @param <T>
     * @param list
     * @param size
     * @return
    </T> */
    fun <T> splitList(list: List<T>, size: Int): List<List<T>> {
	val split: MutableList<List<T>> = ArrayList()
	var i = 0
	while (i < list.size) {
	    val from = i
	    var to = i + size
	    if (to > list.size) to = list.size
	    split.add(list.subList(from, to))
	    i += size
	}
	return split
    }

    /**
     * Extract all items in value list into one big. The order is arbitrary.
     *
     * @param <T>
     * @param <T2>
     * @param map
     * @return
    </T2></T> */
    fun <T, T2> extractAll(map: HashMap<T2, List<T>?>): List<T> {
	val list: MutableList<T> = ArrayList()
	for (l in map.values) list.addAll(l!!)
	return list
    }

    /**
     * Convert a list of pairs into a pair of list, join list content.
     *
     *
     * [Pair<List></List><La1>, List<La2>>, Pair<List></List><Lb1>, List<Lb2>>, ..., Pair<List></List><Ln1>, List<Ln2>>] -> Pair<List></List><La1></La1>, Lb1, Ln1>, List<La2></La2>, Lb2, Ln2>>
     *
     * @param <T>
     * @param col
     * @return
    </T></Ln2></Ln1></Lb2></Lb1></La2></La1> */
    fun <T> extractAllPair(col: Collection<Pair<List<T>?>>): Pair<List<T>?> {
	val fstList: MutableList<T> = ArrayList()
	val sndList: MutableList<T> = ArrayList()
	for (pl in col) {
	    fstList.addAll(pl.fst!!)
	    sndList.addAll(pl.snd!!)
	}
	return Pair<List<T>?>(fstList, sndList)
    }

    fun extractIntegers(s: String, re: String): Set<Int> {
	val set: MutableSet<Int> = HashSet()
	val sa = s.split(re.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
	for (s1 in sa) {
	    try {
		val a = s1.trim { it <= ' ' }.toInt()
		set.add(a)
	    } catch (ex: Exception) {
		Log.getLogger().info("Not an integer string value: $s1")
	    }
	}
	return set
    }

    /**
     * Convert 3 column table into hashmap of hasmaps
     *
     *
     * `
     * {hm-1-key, hm-2-key1, hm2-value1}
     * {hm-1-key, hm-2-key2, hm2-value2}
     * {hm-1-key, hm-2-key3, hm2-value3}
     * {hm-1-key2, hm-2-key1, hm2-value4}
     * {hm-1-key2, hm-2-key2, hm2-value5}
     * {hm-1-key2, hm-2-key3, hm2-value6}
     *
     *
     * ->
     *
     *
     * {hm-1-key -> {hm-2-keyn -> hm-value}}
     *
     *
    ` *
     *
     * @param tab
     * @return
     */
    fun populateMapMap(tab: Array<Array<String>>): HashMap<String, HashMap<String, String>> {
	val map = HashMap<String, HashMap<String, String>>()
	for (sa3 in tab) {
	    val key1 = sa3[0]
	    var m = map[key1]
	    if (m == null) {
		m = HashMap()
		map[key1] = m
	    }
	    m[sa3[1]] = sa3[2]
	}
	return map
    }

    fun <T1, T2> convertToSortedMap(map: Map<T1, T2>?): SortedMap<T1, T2>? {
	if (map == null) return null
	if (map is SortedMap<*, *>) return map as SortedMap<T1, T2>?
	val sm: SortedMap<T1, T2> = TreeMap()
	sm.putAll(map)
	return sm
    }

    fun whichMethodAndClass(): String {
	return whichMethod(1, true)
    }

    fun whichMethod(offs: Int = 1, withClass: Boolean = false): String {
	val stackTrace = Thread.currentThread().stackTrace
	return stackTrace[3 + offs].className + ':' + stackTrace[3 + offs].methodName
    }

    fun timeSetEven(date: Date?): Date {
	if (date == null) return timeSetEven(Date())
	val now = Calendar.getInstance()
	now.time = date
	now[Calendar.SECOND] = now[Calendar.SECOND] and 1.inv()
	return now.time
    }

    fun timeSetOdd(date: Date?): Date {
	if (date == null) return timeSetOdd(Date())
	val now = Calendar.getInstance()
	now.time = date
	now[Calendar.SECOND] = now[Calendar.SECOND] or 1
	return now.time
    }

    fun timeIsEven(ms: Long): Boolean {
	val seconds = ms / 1000
	return seconds and 1L == 0L
    }

    fun timeIsEven(date: Date?): Boolean {
	return if (date == null) false else timeIsEven(date.time)
    }

    fun timeIsOdd(ms: Long): Boolean {
	return !timeIsEven(ms)
    }

    fun timeIsOdd(date: Date): Boolean {
	return !timeIsEven(date.time)
    }

    fun currentTimeSetEven(): Date {
	return timeSetEven(Date())
    }

    fun currentTimeSetOdd(): Date {
	return timeSetOdd(Date())
    }

    val earlyDateConstant: Date?
	get() {
	    val pattern = "yyyy-MM-dd HH:mm:ss"
	    val sdf = SimpleDateFormat(pattern)
	    return try {
		sdf.parse("2010-01-01 00:00:00")
	    } catch (e: ParseException) {
		null
	    }
	}

    fun trim(sourceArr: Array<String?>) {
	for (i in sourceArr.indices) sourceArr[i] = trim(sourceArr[i])
    }

    private fun trim(s: String?): String? {
	return s?.trim { it <= ' ' }
    }

    fun empty(s: String?): Boolean {
	return s == null || s.length == 0
    }

    // simple crypt/decrypt
    const val oef62xc = "cvbnmQWERTY"
    const val nmdb = "RTYASDFGH"
    const val nmsovf = "VBNM01278"
    const val djlfb53 = "LZXfqwghjk"
    const val svo = "lz3nmUI"
    const val tetge5 = "ZXCVBNM01"
    const val xr35e = "qwertyuiopa"
    const val jbvg4 = "sdfghjklzx"
    const val opiwehfg = "iopaQWE"
    const val sklvnj34 = "tysOPu"
    const val a = "456xcvC"
    const val mv34 = "23456789"
    const val rete4 = "UIOPASDFGHJKL"
    const val q1 = "9derbJK"
    private fun mix(s: String, s1: String, s2: String): String {
	var s = s
	val sb = StringBuilder()
	for (ch in s.toCharArray()) {
	    val ix = s1.indexOf(ch)
	    if (ix > -1) sb.append(s2[ix]) else sb.append(ch)
	}
	s = sb.toString()
	return s
    }

    fun crypt(s: String): String {
	return mix(
		s,
		xr35e + jbvg4 + oef62xc + rete4 + tetge5 + mv34,
		sklvnj34 + opiwehfg + nmdb + a + nmsovf + q1 + djlfb53 + svo
	)
    }

    fun decrypt(s: String): String {
	return mix(
		s,
		sklvnj34 + opiwehfg + nmdb + a + nmsovf + q1 + djlfb53 + svo,
		xr35e + jbvg4 + oef62xc + rete4 + tetge5 + mv34
	)
    }

    fun convertToIntArr(csv: String?): IntArray? {
	if (csv == null || csv.length == 0) return null
	val sa = csv.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
	val ia = IntArray(sa.size)
	var ix = 0
	for (s in sa) {
	    val `val` = s.trim { it <= ' ' }.toInt()
	    ia[ix++] = `val`
	}
	return ia
    }

    /**
     * Return if the argument s is like a numeric sql id.
     *
     * @param s
     * @return
     */
    fun isValueLikeNumericId(s: String?): Boolean {
	return s != null && s.length > 0 && s != "0"
    }

    fun formatDisplayText(txt: String): String {
	return txt.replace("_", " ")
    }

    @Throws(IOException::class)
    fun copyFile(sourceFile: File?, destFile: File) {
	if (!destFile.exists()) {
	    destFile.createNewFile()
	}
	var source: FileChannel? = null
	var destination: FileChannel? = null
	try {
	    source = FileInputStream(sourceFile).channel
	    destination = FileOutputStream(destFile).channel
	    destination.transferFrom(source, 0, source.size())
	} finally {
	    source?.close()
	    destination?.close()
	}
    }

    fun m_sleep(a: Int) {
	var a = a
	try {
	    if (a <= 0) a = 10
	    Thread.sleep(a.toLong())
	} catch (e: InterruptedException) {
	}
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun createPrintWriter(fn: String?, append: Boolean = false): PrintWriter? {
	return createPrintWriterUTF8(fn, append)
    }

    fun createPrintWriter(os: OutputStream?): PrintWriter? {
	var pw: PrintWriter? = null
	pw = try {
	    PrintWriter(OutputStreamWriter(BufferedOutputStream(os), "utf-8"))
	} catch (e: UnsupportedEncodingException) {
	    e.printStackTrace()
	    return null
	}
	return pw
    }

    fun createPrintWriterUTF8(fn: String?, append: Boolean = false): PrintWriter? {
	return try {
	    PrintWriter(OutputStreamWriter(BufferedOutputStream(FileOutputStream(fn, append)), "UTF-8"))
	} catch (ex: IOException) {
	    ex.printStackTrace()
	    null
	}
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun pL(a: Int, w: Int, pad: Char = ' '): String {
	val s = "" + a
	val n = w - s.length
	var ss = ""
	for (i in 0 until n) ss += pad
	return ss + s
    }

    fun padLeft(s: String, len: Int, ch: Char): String {
	if (s.length < len) {
	    var ps = ""
	    for (i in 0 until len - s.length) ps += ch
	    return ps + s
	}
	return s
    }

    fun padRight(s: String, len: Int, ch: Char): String {
	if (s.length < len) {
	    var ps = ""
	    for (i in 0 until len - s.length) ps += ch
	    return s + ps
	}
	return s
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun ct(): Long {
	return System.currentTimeMillis()
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun split(str: String?, split: String?): Array<String?> {
	val t = StringTokenizer(str, split)
	val n = t.countTokens()
	var i = 0
	val arr = arrayOfNulls<String>(n)
	while (t.hasMoreTokens()) {
	    val word = t.nextToken()
	    arr[i++] = word
	}
	return arr
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun a2s(o: Any?, w: Int): String? {
	return arrToString(o, ",", w, ' ')
    }

    fun a2s(o: Any?): String? {
	return arrToString(o, ",", 0, ' ')
    }

    fun a2s(o: Any?, delim: String?): String? {
	return arrToString(o, delim, 0, ' ')
    }

    fun a2s(o: Any?, delim: String?, w: Int, pad: Char): String? {
	return arrToString(o, delim, w, pad)
    }

    fun arrToString(o: Any?, delim: String? = ",", w: Int = 0, pad: Char = ' '): String? {
	if (o == null) return "null"
	val s = StringBuffer()
	val cls: Class<*> = o.javaClass
	if (cls.isArray) {
	    val clsc = cls.componentType
	    if (!clsc.isPrimitive) {
		return if (clsc.isArray) {
		    val oa = o as Array<Any>
		    for (i in oa.indices) s.append(
			    (if (i == 0) "" else delim) +
				    "[" + arrToString(oa[i]) + "]"
		    )
		    s.toString()
		} else {
		    val oa = o as Array<Any>
		    for (i in oa.indices) s.append((if (i == 0) "" else delim) + oa[i])
		    s.toString()
		}
	    }
	    if (clsc.name == "int") {
		val ia = o as IntArray
		for (i in ia.indices) if (w == 0) s.append((if (i == 0) "" else delim) + ia[i]) else s.append(
			(if (i == 0) "" else delim) + pL(
				ia[i],
				w,
				pad
			)
		)
		return s.toString()
	    }
	    if (clsc.name == "char") {
		val ia = o as CharArray
		for (i in ia.indices) if (ia[i].code == 0) s.append((if (i == 0) "" else delim) + "^@") else s.append((if (i == 0) "" else delim) + ia[i])
		return s.toString()
	    }
	    if (clsc.name == "byte") {
		val ia = o as ByteArray
		for (i in ia.indices) s.append((if (i == 0) "" else delim) + ia[i])
		return s.toString()
	    }
	    if (clsc.name == "boolean") {
		val ba = o as BooleanArray
		for (i in ba.indices) s.append((if (i == 0) "" else delim) + ba[i])
		return s.toString()
	    }
	}
	return null
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
    fun castArray(o: Any?, to: Any): Any? {
	if (o == null) return null
	val cls: Class<*> = o.javaClass
	val tcls: Class<*> = to.javaClass
	if (cls.isArray) {
	    val clsc = cls.componentType
	    val tclsc = tcls.componentType
	    if (!tclsc.isPrimitive) {
		throw RuntimeException("cast obj[] -> obj'[] Not supported yet")
	    }
	    if (tclsc.name == "int") {
		return if (clsc.name == "java.lang.String") {
		    val l = java.lang.reflect.Array.getLength(o)
		    val ia = IntArray(l)
		    val sa = o as Array<String>
		    try {
			for (i in ia.indices) {
			    ia[i] = sa[i].toInt()
			}
			ia
		    } catch (ex: NumberFormatException) {
			null
		    }
		} else {
		    val l = java.lang.reflect.Array.getLength(o)
		    val ia = IntArray(l)
		    for (i in ia.indices) {
			ia[i] = java.lang.reflect.Array.getInt(o, i)
		    }
		    ia
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
	    } else if (tclsc.name == "double") {
		return if (clsc.name == "java.lang.String") {
		    val l = java.lang.reflect.Array.getLength(o)
		    val ia = DoubleArray(l)
		    val sa = o as Array<String>
		    for (i in ia.indices) {
			ia[i] = tD(sa[i])
		    }
		    ia
		} else {
		    val l = java.lang.reflect.Array.getLength(o)
		    val ia = DoubleArray(l)
		    for (i in ia.indices) {
			ia[i] = java.lang.reflect.Array.getDouble(o, i)
		    }
		    ia
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
	    } else if (tclsc.name == "char") {
		val l = java.lang.reflect.Array.getLength(o)
		val ia = CharArray(l)
		for (i in ia.indices) {
		    if (clsc.name == "byte") {
			ia[i] = Char(java.lang.reflect.Array.getByte(o, i).toUShort())
		    } else if (clsc.name == "short") {
			ia[i] = Char(java.lang.reflect.Array.getShort(o, i).toUShort())
		    } else if (clsc.name == "int") {
			ia[i] = java.lang.reflect.Array.getInt(o, i).toChar()
		    } else if (clsc.name == "long") {
			ia[i] = Char(java.lang.reflect.Array.getLong(o, i).toUShort())
		    } else if (clsc.name == "char") {
			ia[i] = java.lang.reflect.Array.getChar(o, i)
		    } else if (clsc.name == "double") {
			ia[i] = java.lang.reflect.Array.getDouble(o, i).toInt().toChar()
		    } else if (clsc.name == "float") {
			ia[i] = java.lang.reflect.Array.getFloat(o, i).toInt().toChar()
		    }
		}
		return ia
	    }
	}
	return null
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    var scrN = 5

    fun scrambleArr(a: Any?) {
	if (a == null) return
	val cls: Class<*> = a.javaClass
	if (cls.isArray) {
	    val clsc = cls.componentType
	    if (!clsc.isPrimitive) {
		val arr = a as Array<Any>
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	    if (clsc.name == "int") {
		val arr = a as IntArray
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	    if (clsc.name == "long") {
		val arr = a as LongArray
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	    if (clsc.name == "char") {
		val arr = a as CharArray
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	    if (clsc.name == "byte") {
		val arr = a as ByteArray
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	    if (clsc.name == "short") {
		val arr = a as ShortArray
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	    if (clsc.name == "double") {
		val arr = a as DoubleArray
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	    if (clsc.name == "float") {
		val arr = a as FloatArray
		for (j in 0 until scrN) for (i in arr.indices) {
		    val b = rand(arr.size)
		    val t = arr[i]
		    arr[i] = arr[b]
		    arr[b] = t
		}
	    }
	}
	return
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun tD(s: String?): Double {
	return try {
	    val dval = java.lang.Double.valueOf(s)
	    dval
	} catch (ex: Exception) {
	    0.0
	}
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun upTo(a: Int): IntArray {
	return fromTo(0, a)
    }

    fun fromTo(a: Int, b: Int): IntArray {
	val ia = IntArray(b - a)
	var ii = 0
	for (i in a until b) ia[ii++] = i
	return ia
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun swapia(ia: IntArray, a: Int, b: Int) {
	val c = ia[a]
	ia[a] = ia[b]
	ia[b] = c
    }

    fun getFileContent(fn: String?): String? {
	val f = File(OmegaContext.omegaAssets(fn))
	if (!f.exists() || !f.canRead()) return null
	try {
	    return String(Files.readAllBytes(f.toPath()), Charset.forName("UTF-8"))
	} catch (ex: IOException) {
	}
	return null
    }

    fun createUniq(max: Int): Uniq {
	return Uniq(max)
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private val randG = Random()

    fun rand(a: Int): Int {
	val r = 0x7fffffff and randG.nextInt()
	return r % a
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    fun flagAsMap(argv: Array<String>): HashMap<String, String> {
	val argl: MutableList<String?> = Arrays.asList(*argv)
	val flag: HashMap<String, String> = HashMap()
	for (i in argl.indices) {
	    val s = argl[i] as String
	    if (s.startsWith("-")) {
		val ss = s.substring(1)
		val ix = ss.indexOf('=')
		if (ix != -1) {
		    val sk = ss.substring(0, ix)
		    val sv = ss.substring(ix + 1)
		    if (sv.indexOf(',') == -1) flag[sk] = sv else {
			val sa = split(sv, ",")
			flag[sk] = sv
			flag["[SundryUtil;$sk"] = sa.toString()
		    }
		} else {
		    flag[ss] = ""
		}
	    }
	}
	return flag
    }

    fun argAsList(argv: Array<String>): List<String> {
	var argl: List<String> = LinkedList()
	for (av in argv) {
	    if (!av.startsWith("-")) {
		argl += av
	    }
	}
	return argl
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - -
    class Uniq(var max: Int) {
	var picked: IntArray
	var taken = 0

	init {
	    picked = upTo(max)
	    taken = 0
	}

	@get:Synchronized
	val next: Int
	    get() {
		var c = 0
		while (true) {
		    val ix = rand(max)
		    if (picked[ix] != -1) {
			val r = picked[ix]
			picked[ix] = -1
			taken++
			return r
		    } else {
			if (++c > 5) {
			    val ia = IntArray(max - taken)
			    var ix2 = 0
			    for (i in picked.indices) if (picked[i] != -1) ia[ix2++] = picked[i]
			    picked = ia
			    max = picked.size
			    taken = 0
			    //			OmegaContext.sout_log.getLogger().info("<" + max + ">");
			    return next
			}
		    }
		}
	    }

	@Synchronized
	fun asIntArray(): IntArray? {
	    if (taken != 0) {
		return null
	    }
	    val ia = IntArray(max)
	    for (i in ia.indices) ia[i] = next
	    return ia
	}
    }
}
