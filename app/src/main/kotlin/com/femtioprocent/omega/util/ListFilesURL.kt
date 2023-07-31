package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaContext.Companion.getMediaFile
import com.femtioprocent.omega.util.SundryUtils.a2s
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import java.io.File

object ListFilesURL {
    fun getMediaList(n: String?): Array<String> {
	val name = getMediaFile(n!!)
	val dir = File(name)
	val names = dir.list()
	// 	OmegaContext.sout_log.getLogger().info(":--: " + "FILE dir[] name " + name + ' ' + names);
// 	OmegaContext.sout_log.getLogger().info(":--: " + "FILE dir[] names " + SundryUtils.a2s(names));
	val li: MutableSet<String?> = HashSet()
	for (i in names.indices) {
	    li.add(names[i])
	    if (names[i].endsWith(".mp3")) li.add(names[i].substring(0, names[i].length - 4) + ".wav")
	}

	//	OmegaContext.sout_log.getLogger().info(":--: " + "FILE dir[] " + dir + ' ' + SundryUtils.a2s(names));
//	return names;
	val ret = li.toTypedArray<String?>() as Array<String>
	return ret
    }
}
