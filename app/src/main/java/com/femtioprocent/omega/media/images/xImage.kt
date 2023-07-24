package com.femtioprocent.omega.media.images

import com.femtioprocent.omega.graphic.util.LoadImage.loadAndWaitOrNull
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.ListFilesURL
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.pL
import java.awt.Component
import java.awt.Image
import javax.swing.JOptionPane

class xImage {
    var name: String? = null
    var dir: String?
    var base: String?
    var attr: String? = null

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
    var innerAnimIndex = -1
    var ext: String? = "gif"
    var peTaskNid = ""
    private val once = false
    private val onceDone = false
    var maxInnerAnimIndex = 1
    private val cache_attr: HashMap<String?, Boolean?> = HashMap()

    internal inner class Entry(var im: Image) {
	var time_stamp: Long
	var cnt = 0
	fun getIm_(): Image {
	    time_stamp = ct()
	    return im
	}

	var to = 60

	init {
	    time_stamp = ct()
	}

	val isOld: Boolean
	    get() = ct() > time_stamp + 1000 * to
    }

    constructor(name: String) {
	if (name.contains("{*")) {
	    this.name = mkPeTaskDef(name)
	    peTaskNid = mkPeTaskNid(name)
	} else {
	    this.name = name
	    peTaskNid = ""
	}
	val file = splitFile(this.name)
	dir = file[DIR]
	base = file[BASE]
	attr = null
	innerAnimIndex = -1
	ext = file[EXT]
	calcMaxSeq()
	//	OmegaContext.sout_log.getLogger().info(":--: " + "xImage created " + this);
    }

    private fun mkPeTaskNid(name: String): String {
	val sa = name.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
	return sa[0].replace("{*", "")
    }

    private fun mkPeTaskDef(name: String): String {
	val sa = name.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
	return sa[1].replace("}", "")
    }

    constructor(xim: xImage) {
	name = xim.name
	peTaskNid = xim.peTaskNid
	dir = xim.dir
	base = xim.base
	attr = xim.attr
	innerAnimIndex = xim.innerAnimIndex
	ext = xim.ext
	calcMaxSeq()
	//	OmegaContext.sout_log.getLogger().info(":--: " + "xImage cloned " + this);
    }

    fun removeOldEntry() {
	if (ct() < checkNow) return
	synchronized(cache_imf) {
	    val li = ArrayList<String?>()
	    var it: Iterator<String?>
	    it = cache_imf.keys.iterator()
	    while (it.hasNext()) {
		val k = it.next()
		if (cache_imf[k]!!.isOld) li.add(k)
	    }
	    it = li.iterator()
	    while (it.hasNext()) {
		val k = it.next()
		val ent = cache_imf[k]
		ent?.getIm_()?.flush()
		cache_imf.remove(k)
		Log.getLogger().info(":--: " + "%%%%%%%% remove from cache " + k + ", cnt:" + ent!!.cnt)
	    }
	}
	checkNow = ct() + 20 * 1000
    }

    fun getEntry(key: String?): Image? {
	val e = cache_imf[key] ?: return null
	removeOldEntry()
	e.cnt++

	//OmegaContext.sout_log.getLogger().info("IMAGE: " + "=== loaded from cache " + key + ", cnt:" + e.cnt);
	return e.getIm_()
    }

    fun putEntry(key: String?, im: Image?) {
	cache_imf[key] = Entry(im!!)
    }

    private fun hasInnerAnim(): Boolean {
	return maxInnerAnimIndex >= 0
    }

    fun setInnerAnimIndex(dt: Int, ix: Int): Boolean {
	//OmegaContext.sout_log.getLogger().info(":--: " + "ANIM setix " + ix + ' ' + seq + ' ' + max_seq + ' ' + ix_base0);
	if (hasInnerAnim()) {
	    if (innerAnimIndex == ix % (maxInnerAnimIndex + 1)) {
		//OmegaContext.serr_log.getLogger().info(":--: " + "ANIM setix same  " + ix + ' ' + seq + ' ' + max_seq + ' ' + dt);
		return false // it has not been changed
	    }
	    innerAnimIndex = ix
	    innerAnimIndex %= maxInnerAnimIndex + 1
	    //OmegaContext.serr_log.getLogger().info(":--: " + "ANIM setix true  " + ix + ' ' + seq + ' ' + max_seq + ' ' + dt);
	    return true
	}
	//OmegaContext.serr_log.getLogger().info(":--: " + "ANIM setix false " + ix + ' ' + seq + ' ' + max_seq + ' ' + dt);
	return false
    }

    fun calcMaxSeq(): Boolean {
	maxInnerAnimIndex = scanInnerAnimIndex()
	if (maxInnerAnimIndex == -1) {
	    innerAnimIndex = -1
	    return false
	}
	innerAnimIndex = 0
	return true
    }

    private fun isIn(s: String, sa: Array<String>): Boolean {
	for (i in sa.indices) if (s == sa[i]) return true
	return false
    }

    private fun isNumeric(s: String): Boolean {
	try {
	    val a = s.toInt()
	    return true
	} catch (ex: Exception) {
	}
	return false
    }

    // dir '/' base '-' attr '-' seq '.' ext
    private fun splitFile(fn: String?): Array<String?> {
	val sa = arrayOfNulls<String>(5)
	var ix = fn!!.lastIndexOf('.')
	var fnL: String? = null
	if (ix == -1) {
	    sa[EXT] = null
	    fnL = fn
	} else {
	    sa[EXT] = fn.substring(ix + 1)
	    fnL = fn.substring(0, ix)
	}
	val ix_sl = fnL!!.lastIndexOf('/')
	ix = fnL.lastIndexOf('-')
	var dash_ok = true
	if (ix != -1 && ix_sl != -1 && ix > ix_sl) dash_ok = false
	if (dash_ok == false || ix == -1) {
	    sa[SEQ] = null
	    //	    fnL = fnL;
	} else {
	    val maybee_seq = fnL.substring(ix + 1)
	    if (isNumeric(maybee_seq)) {
		sa[SEQ] = maybee_seq
		fnL = fnL.substring(0, ix)
	    } else {
		sa[SEQ] = null
		//		flL = fnL;
	    }
	}
	ix = fnL.lastIndexOf('-')
	dash_ok = true
	if (ix != -1 && ix_sl != -1 && ix < ix_sl) dash_ok = false
	if (dash_ok == false || ix == -1) {
	    sa[ATTR] = null
	    //	    fnL = fnL;
	} else {
	    val attr = fnL.substring(ix + 1)
	    sa[ATTR] = attr
	    fnL = fnL.substring(0, ix)
	}
	ix = fnL.lastIndexOf('/')
	if (ix == -1) {
	    sa[DIR] = null
	    sa[BASE] = fnL
	} else {
	    val base = fnL.substring(ix + 1)
	    sa[BASE] = base
	    sa[DIR] = fnL.substring(0, ix)
	}
	return sa
    }

    private fun scanDir(dir: String?): Array<String>? {
	val D = cache_dir[dir]
	if (D != null) return D
	var list: Array<String>? = null
	//	String[] list = (String[])list_hm.get(dir);
	if (list == null) {
	    try {
		list = ListFilesURL.getMediaList(dir)
	    } catch (ex: Exception) {
		Log.getLogger().info(":--: === $ex $dir")
		JOptionPane.showMessageDialog(
		    null, arrayOf(
			"""
		    	${t("Nu such directory")}!
		    	${t("File is")}: $dir
		    	""".trimIndent()
		    ),
		    t("Omega - Message"),
		    JOptionPane.INFORMATION_MESSAGE
		)
	    }
	    //	    list_hm.put(dir, list);
	}
	//	OmegaContext.sout_log.getLogger().info(":--: " + "scanDir -> " + SundryUtils.arrToString(list));
	cache_dir[dir] = list
	return list
    }

    fun setNoAttrib() {
	attrib = null
    }

    var attrib: String?
	get() = attr
	set(a) {
	    attr = if (scanAttrib(a)) a else null
	    calcMaxSeq()
	}

    fun getImage(comp: Component?): Image {
	val key = getFN(innerAnimIndex)
	var im: Image? = null
	synchronized(cache_imf) {
	    im = getEntry(key)
	    if (im != null) {
		return im!!
	    }
	    im = loadAndWaitOrNull(comp, key, attr != null)
	    if (im == null && attr != null) {
		attrib = null
		return getImage(comp)
	    }
	    putEntry(key, im)
	}
	return im!!
    }

    fun getBaseImage(comp: Component?): Image {
	val key = fNBase()
	var im: Image? = null
	synchronized(cache_imf) {
	    im = getEntry(key)
	    if (im != null) {
		return im!!
	    }
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "¤¤¤¤¤ loading " + key);
	    im = loadAndWaitOrNull(comp, key!!, false)
	    putEntry(key, im)
	}
	return im!!
    }

    var last_b: String? = null

    fun fNBase(): String {
	if (last_b == null) last_b = "$dir/$base.$ext"
	    return last_b!!
    }

    var last_seq = -2
    var last_fn_an: String? = null
    var last_fn: String? = null
    var last_attr: String? = null

    fun getFN(seq: Int): String {
	if (seq < 0) return fN_noSeq()!!
	return if (attr == null) {
	    if (last_seq != seq || last_fn_an == null) {
		last_fn_an = dir + '/' + base + '-' + pL(seq, 2, '0') + '.' + ext
		last_seq = seq
	    }
	    last_fn_an!!
	} else {
	    if (last_seq != seq || last_attr !== attr || last_fn == null) {
		last_fn = dir + '/' + base + '-' + attr + '-' + pL(seq, 2, '0') + '.' + ext
		last_seq = seq
		last_attr = attr
	    }
	    last_fn!!
	}
    }

    var last_ns_an: String? = null
    var last_ns: String? = null
    var last_an_attr: String? = null

    fun fN_noSeq(): String? {
	if (attr == null) {
	    if (last_ns_an == null) last_ns_an = "$dir/$base.$ext"
	    return last_ns_an
	} else {
	    if (last_an_attr !== attr || last_ns == null) {
		last_ns = "$dir/$base-$attr.$ext"
		last_an_attr = attr
	    }
	    return last_ns
	}
    }

    // ff-BB-00  ff-BB-01  ff-BB-02  ff-BB-03
    // ff-00     ff-01     ff-02
    // ff-aa-00  ff-aa-01
    private fun scanInnerAnimIndex(): Int {
	val key = fN_noSeq()
	val I = cache_seqLen[key] as Int?
	if (I != null) return I

//	OmegaContext.sout_log.getLogger().info(":--: " + "scanInner " + toString());
	var max = -1
	try {
	    val list = scanDir(dir)
	    for (i in list!!.indices) {
		val file = splitFile(list[i])
		//	    OmegaContext.sout_log.getLogger().info(":--: " + "try " + SundryUtils.arrToString(file));
		if (file[BASE] == base && (attr == null && file[ATTR] == null || attr != null && attr == file[ATTR])) {
		    if (file[SEQ] != null) {
			//		    OmegaContext.sout_log.getLogger().info(":--: " + "FOund seq " + SundryUtils.arrToString(file));
			val v = file[SEQ]!!.toInt()
			if (v > max) max = v
		    }
		}
	    }
	} catch (ex: Exception) {
	}
	//	OmegaContext.sout_log.getLogger().info(":--: " + "found max = " + max);
	cache_seqLen[key] = max
	return max
    }

    private fun scanAttrib(a: String?): Boolean {
//	OmegaContext.sout_log.getLogger().info(":--: " + "scanInner Attrib " + a + " ...");
	val key = fN_noSeq()
	if (cache_attr[key] != null) return true
	try {
	    val list = scanDir(dir)
	    for (i in list!!.indices) {
		val file = splitFile(list[i])
		if (file[BASE] == base && a != null && a == file[ATTR]) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "found attr " + SundryUtils.arrToString(file));
		    cache_attr[key] = java.lang.Boolean.valueOf(true)
		    return true
		}
	    }
	} catch (ex: Exception) {
	}
	return false
    }

    override fun toString(): String {
	return "xImage{" +
		dir + ':' +
		base + ':' +
		attr + ':' +
		innerAnimIndex + ':' +
		ext +
		"}"
    }

    companion object {
	const val DIR = 0
	const val BASE = 1
	const val ATTR = 2
	const val SEQ = 3
	const val EXT = 4
	var checkNow = ct() + 10 * 1000
	private var cache_dir = HashMap<String?, Array<String>?>()
	private val cache_imf: HashMap<String?, Entry?> = HashMap()
	private var cache_seqLen = HashMap<String?, Int?>()
	fun invalidateCache() {
	    cache_dir = HashMap()
	    cache_seqLen = HashMap()
	}

	fun removeAllEntry() {
	    synchronized(cache_imf) {
		val li: ArrayList<String?> = ArrayList()
		var it: Iterator<String?>
		it = cache_imf.keys.iterator()
		while (it.hasNext()) {
		    val k = it.next()
		    val e = cache_imf[k]
		    if (true) li.add(k)
		}
		it = li.iterator()
		while (it.hasNext()) {
		    val k = it.next()
		    val ent = cache_imf[k]
		    ent?.getIm_()?.flush()
		    cache_imf.remove(k)
		    Log.getLogger().info(":--: " + "%%%%%%%% remove from cache " + k + ", cnt:" + ent!!.cnt)
		}
	    }
	    checkNow = ct() + 20 * 1000
	}

	@JvmStatic
	fun main(args: Array<String>) {
	    val i = xImage("image.gif")
	    i.attrib = "a"
	}
    }
}
