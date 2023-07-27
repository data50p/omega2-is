package com.femtioprocent.omega.media.audio

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.getMediaFile
import com.femtioprocent.omega.media.audio.impl.FxPlayer
import com.femtioprocent.omega.media.audio.impl.JPlayer
import com.femtioprocent.omega.util.ListFilesURL
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.ct
import java.io.File

class APlayer {
    var id: String? = null
    var nname: String? = null
    var jplayer: JPlayer? = null
    var fxplayer: FxPlayer? = null

    private constructor()
    private constructor(nname: String, id: String?) {
	this.id = id
	this.nname = nname
    }

    fun isLoaded(): Boolean {
	return nname != null
    }

    fun play() {
	if (fxplayer != null) {
	    fxplayer!!.play(false)
	    return
	}
	if (jplayer != null) {
	    jplayer!!.play()
	    return
	}
    }

    fun playWait() {
	try {
	    if (fxplayer != null) {
		fxplayer!!.play(true)
		return
	    }
	    if (jplayer != null) {
		jplayer!!.play()
		jplayer!!.waitAudio()
		return
	    }
	} catch (ex: Exception) {
	    Log.getLogger().info("while playWait audio $fxplayer $ex")
	    ex.printStackTrace()
	}
    }

    fun stop() {}
    fun close() {}

    companion object {
	var dir_cache: HashMap<String?, Array<String>?> = HashMap()
	var T = true
	var alwaysFxPlayer = false
	@JvmStatic
	fun createAPlayer(name: String?, attr: String?, id: String?): APlayer {
	    var apl = load_(null, name, attr, id)
	    if (apl == null && attr != null) apl = load_(null, name, null, id)
	    return apl ?: APlayer()
	}

	@JvmStatic
	fun createAPlayer(lang: String?, name: String?, attr: String?, id: String?): APlayer {
	    var apl = load_(lang, name, attr, id)
	    if (apl == null && attr != null) apl = load_(lang, name, null, id)
	    return apl ?: APlayer()
	}

	private fun splice(base: String, attr: String): String {
	    var attr: String? = attr
	    if (attr != null && attr.length > 0 && attr[0] == '-') attr = attr.substring(1)
	    val ix = base.lastIndexOf('.')
	    return if (ix == -1) "$base-$attr" else base.substring(0, ix) + '-' + attr + base.substring(ix)
	}

	fun isIn(s: String, sa: Array<String>?): Boolean {
	    if (sa == null) return false
	    for (i in sa.indices) if (s == sa[i]) return true
	    return false
	}

	private fun load_(lang: String?, name: String?, attr: String?, id: String?): APlayer? {
	    if (name == null) return APlayer()
	    val ct0 = ct()
	    var apl: APlayer? = null
	    try {
		val ix = name.lastIndexOf('/')
		val dir: String
		val base: String
		if (ix == -1) {
		    dir = "."
		    base = name
		} else {
		    dir = name.substring(0, ix)
		    base = name.substring(ix + 1)
		}
		Log.getLogger().info(":--: audio: dir $dir")
		Log.getLogger().info(":--: audio: base $base")
		var list = dir_cache[dir]
		if (list == null) {
		    try {
			val ct0a = System.nanoTime()
			list = ListFilesURL.getMediaList(dir)
			val ct1a = System.nanoTime()
			dir_cache[dir] = list
			Log.getLogger().info(":--: " + "===== " + dir + ' ' + (ct1a - ct0a) + ' ' + list)
		    } catch (ex: Exception) {
			Log.getLogger().info("ERR: Can't get file list $lang $name $attr $id")
		    }
		}
		var nname: String? = null
		val isTL = id != null && id.startsWith("TL")
		if (attr != null && attr.length > 0) {
		    var nnname = splice(base, attr + if (isTL) OmegaContext.SPEED else "")
		    if (isIn(nnname, list)) nname = nnname else {
			nnname = splice(base, attr)
			if (isIn(nnname, list)) nname = nnname
		    }
		}
		if (nname == null && isIn(if (isTL) splice(base, OmegaContext.SPEED) else base, list)) nname =
		    if (isTL) splice(base, OmegaContext.SPEED) else base
		if (nname == null && isIn(base, list)) nname = base
		if (nname == null) return null
		nname = "$dir/$nname"
		var lang_id = ""
		var ffname = getMediaFile(nname)
		if (ffname!!.indexOf("words-") != -1) {
		    val alname = ffname.replace("words\\-[a-zA-Z]*".toRegex(), "words-$lang")
		    val fffile = File(alname)
		    if (fffile.exists()) {
			ffname = alname
			lang_id = ":$lang"
		    }
		    Log.getLogger().info(":--: NAME IS $ffname")
		}
		val url_name = "file:$ffname"
		try {
		    apl = APlayer(nname, id)
		    ffname = maybeeTheMp3(ffname)
		    if (ffname.endsWith(".mp3") || alwaysFxPlayer) {
			apl.fxplayer = FxPlayer(ffname)
			Log.getLogger().info(":--: " + "FxPlayer created: " + nname + ' ' + apl.fxplayer)
		    } else {
			apl.jplayer = JPlayer(ffname)
			Log.getLogger().info(":--: " + "JPlayer created: " + nname + ' ' + apl.jplayer)
		    }
		} catch (ex: Exception) {
		    Log.getLogger().info("ERR: $ex")
		    ex.printStackTrace()
		    return null
		}
		return apl
	    } catch (ex: Exception) {
		Log.getLogger().info("ERR: $ex")
		ex.printStackTrace()
	    } finally {
	    }
	    return null
	}

	private fun maybeeTheMp3(fn: String?): String {
	    val fnMp3 = fn!!.replace("\\.wav$".toRegex(), ".mp3")
	    val fileMp3 = File(fnMp3)
	    val file = File(fn)
	    return if (fileMp3.exists() && !file.exists()) fnMp3 else fn
	}
    }
}
