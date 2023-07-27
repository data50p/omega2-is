package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.createPrintWriterUTF8
import com.femtioprocent.omega.xml.XML_PW

class SaveRestore {
    fun save(fname: String?, rslt: Result): Boolean {
	val el = rslt.element
	Log.getLogger().info(":--: saving SaveRestore el $el")
	try {
	    XML_PW(createPrintWriterUTF8(fname), false).use { xmlpw -> xmlpw.put(el) }
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    return false
	}
	return true
    }

    fun restore(fname: String?): Result? {
	return null
    }
}
