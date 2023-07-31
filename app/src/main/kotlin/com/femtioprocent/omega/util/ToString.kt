package com.femtioprocent.omega.util

import com.femtioprocent.omega.util.Log.getLogger
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * This class makes it easy to create a String representation of an instance with all known members value. Convenient to have this in the toString method.
 *
 *
 * Format: -> <ClassName>{<variable_1>=<value_1>, <variable_2>=<value_2>, <variable_N>=<value_N>}<br></br>
 *
 *
 * Use regular pattern such as '-<RE>' to romeve variables and '+<RE>' to add variables in the output. Use regular pattern "" to supress class name
 *
 *
 * toString(obj, "-.*Foo.*")<br></br> -> ToString{nominalInt=123, nominalString=nominal value}<br></br> toString(obj, "-.*Foo.*", "+.*Str.*, "=foobar")<br></br> -> ToString{nominalInt=123,
 * nominalString=nominal value, nominalFooString=nominal foo value, foobar}<br></br> <br></br> toString(obj, "")<br></br> -> nominalInt=123, nominalString=nominal value, nominalFooInt=12345,
 * nominalFooString=nominal foo value<br></br> <br></br>
 *
 *
 * public String toString() {<br></br> &nbsp;&nbsp;&nbsp;&nbsp;return ToString.toString(this); }
 *
 * @author lars
</RE></RE></value_N></variable_N></value_2></variable_2></value_1></variable_1></ClassName> */
class ToString {
    /**
     * Used as a demo for ToString
     */
    var nominalInt = 123

    /**
     * Used as a demo for ToString
     */
    var nominalString = "nominal value"

    /**
     * Used as a demo for ToString
     */
    var nominal12345Int = 12345

    /**
     * Used as a demo for ToString
     */
    var nominalFooString = "nominal foo value"

    /**
     * return "ToString{nominalInt=123, nominalString=nominal value}"
     *
     * @return
     */
    override fun toString(): String {
	return toString(this)
    }

    companion object {
	/**
	 * Create a string consisting of the class base name and a list of all members and its value.
	 *
	 * @param obj
	 * @param specs
	 * @return
	 */
	fun toString(obj: Any?): String {
	    return toString(obj, "")
	}

	fun toString(obj: Any?, vararg specs: String): String {
	    var noClassName = false
	    if (obj == null) {
		return "=null="
	    }
	    if (obj.javaClass == String::class.java) {
		return obj.toString()
	    } else if (obj.javaClass == Int::class.javaPrimitiveType) {
		return obj.toString()
	    } else if (obj.javaClass == Int::class.java) {
		return obj.toString()
	    } else if (obj.javaClass == Long::class.javaPrimitiveType) {
		return obj.toString()
	    } else if (obj.javaClass == Long::class.java) {
		return obj.toString()
	    } else if (obj.javaClass == Double::class.javaPrimitiveType) {
		return obj.toString()
	    } else if (obj.javaClass == Double::class.java) {
		return obj.toString()
	    } else if (obj.javaClass == GregorianCalendar::class.java) {
		val gc = obj as GregorianCalendar
		return "" + gc.time
	    }
	    val str = arrayOfNulls<String>(specs.size)
	    val pat = arrayOfNulls<Pattern>(specs.size)
	    val type = CharArray(specs.size)
	    for (i in pat.indices) {
		if (specs[i].length == 0) {
		    noClassName = true
		    // pat[i] = null;
		} else {
		    val typ = specs[i][0]
		    var ix = 0
		    if (typ == '=') {
			type[i] = '='
			pat[i] = null
			str[i] = specs[i].substring(1)
			continue
		    }
		    if (typ == '-' || typ == '+') {
			ix = 1
		    }
		    pat[i] = Pattern.compile(specs[i].substring(ix))
		    if (ix == 1) {
			type[i] = specs[i][0]
		    }
		}
	    }
	    val tc: Class<*> = obj.javaClass
	    val sb = StringBuilder()
	    if (!noClassName) {
		sb.append(last(tc.name) + '{')
	    }
	    val fA = tc.declaredFields
	    var first = true
	    FIELD@ for (f in fA) {
		try {
		    val name = f.name
		    var Bp: Boolean? = null
		    var bp = false
		    var i = 0
		    for (p in pat) {
			if (p != null) {
			    val m = p.matcher(name)
			    if (m.matches()) {
				if (type[i] == '+') {
				    Bp = true
				} else if (type[i] == '-') {
				    Bp = false
				} else if (type[i] == '=') {
				    continue@FIELD
				} else {
				    bp = true
				}
			    }
			} else {
			}
			i++
		    }
		    if (Bp == null && !bp || Bp != null && Bp) {
			if (!first) {
			    sb.append(", ")
			}
			val b = f.isAccessible
			f.isAccessible = true
			var v = "" // obj.getClass().getName();
			try {
			    val oo = f[obj]
			    v = if (oo == null) {
				"<null>"
			    } else {
// toString for a GregorianCalendar is huge
				if (oo is GregorianCalendar) {
				    "" + oo.time
				} else {
				    oo.toString()
				}
			    }
			} catch (ex: Exception) {
			    getLogger().info("" + ex)
			    v = ex.message!!
			}
			sb.append("" + f.name + '=' + v)
			if (!b) {
			    f.isAccessible = b
			}
			first = false
		    }
		} catch (ex: IllegalArgumentException) {
		    Logger.getLogger(ToString::class.java.name).log(Level.SEVERE, null, ex)
		} finally {
		}
	    }
	    var i = 0
	    for (p in pat) {
		if (p == null && type[i] == '=') {
		    sb.append(", " + str[i])
		}
		i++
	    }
	    if (!noClassName) {
		sb.append("}")
	    }
	    return sb.toString()
	}

	private fun last(s: String): String {
	    val ix = s.lastIndexOf('.')
	    return if (ix == -1) {
		s
	    } else s.substring(ix + 1)
	}
    }
}