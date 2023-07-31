package com.femtioprocent.omega.media.audio

import com.femtioprocent.omega.OmegaContext.Companion.isMacOS
import com.femtioprocent.omega.media.audio.impl.TTS_mac

/**
 * Created by lars on 2017-07-09.
 */
object TTS {
    private var tts_mac: TTS_mac? = null

    fun say(lang: String?, s: String?, wait: Boolean): Boolean {
	if (tts_mac == null && isMacOS) {
	    tts_mac = TTS_mac()
	}
	return if (tts_mac != null) {
	    tts_mac!!.say(lang!!, s!!, wait)
	} else false
    }
}
