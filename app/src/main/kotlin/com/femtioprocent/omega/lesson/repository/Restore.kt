package com.femtioprocent.omega.lesson.repository

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.util.Log.getLogger
import com.femtioprocent.omega.xml.Element
import com.femtioprocent.omega.xml.SAX_node

object Restore {
    @JvmStatic
    fun restore(fname: String): Element? {
	return try {
	    val aName: String?
	    aName = if (fname.startsWith(omegaAssets("")!!)) {
		getLogger().info("Already assets: $fname")
		fname
	    } else {
		omegaAssets(fname)
	    }
	    OmegaContext.sout_log.getLogger().info(":--: Restore from (~A) $fname -> (A) $aName")
	    SAX_node.parse(aName!!, false)
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: Exception! Restore.restore(): $ex")
	    null
	}
    }
}
