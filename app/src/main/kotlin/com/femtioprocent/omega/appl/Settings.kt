package com.femtioprocent.omega.appl

import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.PreferenceUtil
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class Settings internal constructor() {
    var settingsHashMap = HashMap<String, Any>()
    fun list() {
	Log.getLogger().info(":--: " + "list: " + settingsHashMap)
    }

    internal inner class MyActionListener : ActionListener {
	override fun actionPerformed(ae: ActionEvent) {
	    save()
	    System.exit(0)
	}
    }

    var f: JFrame? = null
    var pan: JPanel? = null

    init {
	Log.getLogger().info(":--: " + "" + flags)
	Log.getLogger().info(":--: " + "" + args)
	settingsHashMap["audio-cache"] = false
	settingsHashMap["audio-jmf"] = false
	settingsHashMap["audio-bufsize"] = "16"
	settingsHashMap["audio-write-ahead"] = "2"
	settingsHashMap["audio-silent"] = false
	settingsHashMap["audio-debug"] = false
	val pu = PreferenceUtil(Settings::class.java)
	val hm = pu.getObject("settings", HashMap<String, Any>()) as HashMap<String, Any>
	settingsHashMap.putAll(hm)
    }

    fun save() {
	val cA = pan!!.components
	for (i in cA.indices) {
	    val comp = cA[i]
	    if (comp is JTextField) {
		Log.getLogger().info(":--: " + "" + comp.text)
	    }
	    if (comp is JLabel) {
		val la = comp
		val tf = la.labelFor as JTextField
		val o = settingsHashMap[la.text]
		if (o is Boolean) {
		    var B = false
		    val s = tf.text
		    if ("true".equals(s, ignoreCase = true) ||
			    "t".equals(s, ignoreCase = true) ||
			    "ja".equals(s, ignoreCase = true) ||
			    "yes".equals(s, ignoreCase = true)
		    ) B = true
		    settingsHashMap[la.text] = B
		} else {
		    settingsHashMap[la.text] = tf.text
		}
	    }
	}
	val pu = PreferenceUtil(Settings::class.java)
	pu.save("settings", settingsHashMap)
    }

    fun gui() {
	f = JFrame("Omega - Guru Settings")
	pan = JPanel()
	pan!!.layout = GridLayout(0, 2)
	val c = f!!.contentPane
	c.layout = BorderLayout()
	c.add(pan, BorderLayout.CENTER)
	val exitB = JButton("Save & Exit")
	exitB.addActionListener(MyActionListener())
	c.add(exitB, BorderLayout.SOUTH)
	settingsHashMap.keys.forEach {key ->
	    val `val` = settingsHashMap[key].toString()
	    var la: JLabel
	    var tf: JTextField
	    pan!!.add(JLabel(key).also { la = it })
	    pan!!.add(JTextField(`val`).also { tf = it })
	    la.labelFor = tf
	}
	f!!.pack()
	f!!.isVisible = true
    }

    fun getBoolean(key: String): Boolean {
	return settingsHashMap[key] as Boolean
    }

    fun getString(key: String): String {
	return settingsHashMap[key].toString()
    }

    fun main() {
	if (flags!!["l"] != null) {
	    list()
	}
	if (true || flags!!["s"] != null) {
	    gui()
	}
    }

    companion object {
	var flags: HashMap<String, String>? = null
	var args: List<String>? = null
	var default_settings: Settings? = null

	fun getSettings(): Settings? {
	    if (default_settings == null) {
		default_settings = Settings()
		Log.getLogger().info(":--: " + "Settings created")
	    }
	    return default_settings
	}

	@JvmStatic
	fun main(argv: Array<String>) {
	    flags = flagAsMap(argv)
	    args = argAsList(argv)
	    val s = getSettings()
	    s!!.main()
	}
    }
}
