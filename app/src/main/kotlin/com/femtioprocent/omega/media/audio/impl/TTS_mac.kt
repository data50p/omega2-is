package com.femtioprocent.omega.media.audio.impl

import com.femtioprocent.omega.util.Log

/**
 * Created by lars on 2017-07-09.
 *
 *
 * On DOS
 *
 * PowerShell -Command "Add-Type –AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('hello');
 *
 */
class TTS_mac {
    private val voice_lang = arrayOf(
	    arrayOf("Alex", "en_US"),
	    arrayOf("Alice", "it_IT"),
	    arrayOf("Alva", "sv_SE"),
	    arrayOf("Amelie", "fr_CA"),
	    arrayOf("Anna", "de_DE"),
	    arrayOf("Carmit", "he_IL"),
	    arrayOf("Damayanti", "id_ID"),
	    arrayOf("Daniel", "en_GB"),
	    arrayOf("Diego", "es_AR"),
	    arrayOf("Ellen", "nl_BE"),
	    arrayOf("Fiona", "en-scotland"),
	    arrayOf("Fred", "en_US"),
	    arrayOf("Ioana", "ro_RO"),
	    arrayOf("Joana", "pt_PT"),
	    arrayOf("Jorge", "es_ES"),
	    arrayOf("Juan", "es_MX"),
	    arrayOf("Kanya", "th_TH"),
	    arrayOf("Karen", "en_AU"),
	    arrayOf("Kyoko", "ja_JP"),
	    arrayOf("Laura", "sk_SK"),
	    arrayOf("Lekha", "hi_IN"),
	    arrayOf("Luca", "it_IT"),
	    arrayOf("Luciana", "pt_BR"),
	    arrayOf("Maged", "ar_SA"),
	    arrayOf("Mariska", "hu_HU"),
	    arrayOf("Mei-Jia", "zh_TW"),
	    arrayOf("Melina", "el_GR"),
	    arrayOf("Milena", "ru_RU"),
	    arrayOf("Moira", "en_IE"),
	    arrayOf("Monica", "es_ES"),
	    arrayOf("Nora", "nb_NO"),
	    arrayOf("Paulina", "es_MX"),
	    arrayOf("Samantha", "en_US"),
	    arrayOf("Sara", "da_DK"),
	    arrayOf("Satu", "fi_FI"),
	    arrayOf("Sin-ji", "zh_HK"),
	    arrayOf("Tessa", "en_ZA"),
	    arrayOf("Thomas", "fr_FR"),
	    arrayOf("Ting-Ting", "zh_CN"),
	    arrayOf("Veena", "en_IN"),
	    arrayOf("Victoria", "en_US"),
	    arrayOf("Xander", "nl_NL"),
	    arrayOf("Yelda", "tr_TR"),
	    arrayOf("Yuna", "ko_KR"),
	    arrayOf("Yuri", "ru_RU"),
	    arrayOf("Zosia", "pl_PL"),
	    arrayOf("Zuzana", "cs_CZ")
    )
    var map = HashMap<String, String?>()
    fun say(lang: String, s: String, wait: Boolean): Boolean {
	if (map.size == 0) {
	    for (sa in voice_lang) {
		map[sa[1]] = sa[0]
		val ix = sa[1].indexOf("_")
		if (ix != -1) if (map[sa[1].substring(0, ix)] == null) map[sa[1].substring(0, ix)] = sa[0]
	    }
	}
	val v = map[lang]
	val pb: ProcessBuilder
	pb = if (v == null) ProcessBuilder("say", s) else ProcessBuilder("say", "-v", v, s)
	return try {
	    Log.getLogger().info("TTS: $v $lang $s")
	    val p = pb.start()
	    if (wait) p.waitFor()
	    true
	} catch (e: InterruptedException) {
	    e.printStackTrace()
	    true
	} catch (e: Exception) {
	    e.printStackTrace()
	    false
	}
    }
}
