package com.femtioprocent.omega

import com.femtioprocent.omega.util.Log
import java.io.File

object OmegaConfig {
    const val OMEGA_BUNDLE = "omega_bundle"
    const val OMEGA_BUNDLE_EXTENSION = "." + OMEGA_BUNDLE
    const val FRAME_WIDTH = 1600
    const val FRAME_HEIGHT = 1200
    private const val DEFAULT_FLATNESS = 1.5
    @JvmField
    var FLATNESS = DEFAULT_FLATNESS
    @JvmField
    var TIMELINES_N = 5
    @JvmField
    var CABARET_ACTOR_N = 12
    @JvmField
    var WINGS_N = 10
    @JvmField
    var T = !false
    @JvmField
    var RUN_MODE = false
    @JvmField
    var t_step = 10
    var key_next_1 = 9
    var key_next_2 = 9
    var key_select_1 = ' '.code
    var key_select_2 = '\r'.code
    var key_select_3 = '\n'.code
    var LIU_Mode = false
    @JvmStatic
    fun isLIU_Mode(): Boolean {
	Log.getLogger().info("is LIU_Mode: " + LIU_Mode)
	return LIU_Mode
    }

    var debug: Boolean? = null
    @JvmStatic
    fun isDebug(): Boolean {
	if (debug == null) debug = File("DEBUG").exists()
	return debug!!
    }

    var fullScreen = !false
    @JvmField
    var smaller = OmegaContext.isDeveloper
    const val VAR_NUM = 5
    @JvmField
    var tts = !true
    @JvmStatic
    fun isKeyNext(kc: Int): Boolean {
	return kc == key_next_1 ||
		kc == key_next_2
    }

    @JvmStatic
    fun isKeySelect(kc: Int): Boolean {
	return kc == key_select_1 || kc == key_select_2 || kc == key_select_3
    }

    @JvmStatic
    fun isKeyESC(kc: Int): Boolean {
	return kc == '\u001b'.code
    }

    @JvmStatic
    fun setNextKey() {
	key_next_2 = ' '.code
	key_select_1 = '\r'.code
    }

    @JvmStatic
    fun setSelectKey() {
	key_next_2 = 9
	key_select_1 = ' '.code
    }

    @JvmStatic
    fun FRAME_WIDTH(percent: Int): Int {
	return (percent / 100.0 * FRAME_WIDTH).toInt()
    }

    @JvmStatic
    fun FRAME_HEIGHT(percent: Int): Int {
	return (percent / 100.0 * FRAME_HEIGHT).toInt()
    }
}
