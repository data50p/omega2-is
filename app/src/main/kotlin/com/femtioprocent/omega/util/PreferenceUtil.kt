package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaContext
import java.io.*
import java.util.prefs.Preferences

class PreferenceUtil(var owner: Class<*>) {
    var ownername: String

    init {
	val s = owner.name
	ownername = s.substring(s.lastIndexOf('.') + 1)
    }

    /**
     * Save an Object to the user preference area. Use the supplied id.
     */
    fun save(subid: String, `object`: Any?) {
	val prefs = Preferences.userNodeForPackage(owner)
	try {
	    val bo = ByteArrayOutputStream()
	    val oo = ObjectOutputStream(bo)
	    oo.writeObject(`object`)
	    oo.close()
	    val ba = bo.toByteArray()
	    val baa = split(ba, 1000)
	    for (i in baa.indices) {
		prefs.putByteArray("$ownername:$subid-$i", baa[i])
	    }
	    prefs.putInt("$ownername:$subid-size", baa.size)
	} catch (ex: IOException) {
	}
    }

    private fun split(ba: ByteArray, size: Int): Array<ByteArray?> {
	val l = ba.size / size + if (ba.size % size == 0) 0 else 1
	val baa = arrayOfNulls<ByteArray>(l)
	return split(baa, ba, size, 0)
    }

    private fun split(baa: Array<ByteArray?>, ba: ByteArray, size: Int, n: Int): Array<ByteArray?> {
	val offs = n * size
	val rest = ba.size - offs
	val l = if (rest > size) size else rest
	val nba = ByteArray(l)
	System.arraycopy(ba, offs, nba, 0, l)
	baa[n] = nba
	if (rest > size) {
	    split(baa, ba, size, n + 1)
	}
	return baa
    }

    /**
     * Load a saved Object from the user preference area. Use the id to get it.
     * Return the default value if nothing found.
     */
    fun load(subid: String, def: Any?): Any? {
	val prefs = Preferences.userNodeForPackage(owner)
	try {
	    val size = prefs.getInt("$ownername:$subid-size", 100)
	    val ba = load(prefs, "$ownername:$subid", ByteArrayOutputStream(), 0, size)
	    if (ba != null) {
		val `is` = ByteArrayInputStream(ba)
		val oi = ObjectInputStream(`is`)
		return oi.readObject()
	    } else {
	    }
	} catch (ex: ClassNotFoundException) {
	} catch (ex: IOException) {
	}
	return def
    }

    private fun load(prefs: Preferences, key: String, bao: ByteArrayOutputStream, n: Int, max: Int): ByteArray {
	var n = n
	val ba = prefs.getByteArray("$key-$n", null) ?: return bao.toByteArray()
	bao.write(ba, 0, ba.size)
	n++
	return if (n == max) bao.toByteArray() else load(prefs, key, bao, n, max)
    }

    fun getObject(sub_id: String, def: Any): Any {
	val ret = load(sub_id, null)
	if (ret == null) {
	    save(sub_id, def)
	    return def
	}
	return ret
    }

    companion object {
	@JvmStatic
	fun main(args: Array<String>) {
	    val pu = PreferenceUtil(PreferenceUtil::class.java)
	    val hm = pu.getObject("test_obj", HashMap<Any?, Any?>()) as HashMap<String, String>
	    OmegaContext.sout_log.getLogger().info(":--: get $hm")
	    hm[args[0]] = args[1]
	    pu.save("test_obj", hm)
	    OmegaContext.sout_log.getLogger().info("ERR: saved $hm")
	}
    }
}
