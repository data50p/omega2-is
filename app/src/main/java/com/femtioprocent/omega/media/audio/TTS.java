package com.femtioprocent.omega.media.audio;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.media.audio.impl.TTS_mac;

/**
 * Created by lars on 2017-07-09.
 */
public class TTS {
    static private TTS_mac tts_mac = null;

    static public boolean say(String lang, String s, boolean wait) {
        if ( tts_mac == null && OmegaContext.isMacOS() ) {
            tts_mac = new TTS_mac();
	}
	if ( tts_mac != null ) {
	    return tts_mac.say(lang, s, wait);
	}
	return false;
    }
}
