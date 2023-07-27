package com.femtioprocent.omega.lesson.settings

import com.femtioprocent.omega.OmegaConfig.isLIU_Mode
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.swing.ColorChooser
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.createPrintWriterUTF8
import com.femtioprocent.omega.xml.Element
import com.femtioprocent.omega.xml.SAX_node
import com.femtioprocent.omega.xml.XML_PW
import org.hs.jfc.FormPanel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.FilenameFilter
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JSlider

class OmegaSettingsDialog : SettingsDialog("Omega - Settings") {
    @JvmField
    var feedback_movie_background: ColorButton? = null
    @JvmField
    var action_movie_background: ColorButton? = null
    @JvmField
    var anim_background: ColorButton? = null
    @JvmField
    var signWord_background: ColorButton? = null
    @JvmField
    var signSentence_background: ColorButton? = null
    var color_theme: JComboBox<*>? = null
    var color_theme_main: JButton? = null
    var color_theme_pupil: JButton? = null
    var color_theme_words: JButton? = null
    var color_theme_sent: JButton? = null
    @JvmField
    var signWord_alpha: JSlider? = null
    @JvmField
    var signSentence_alpha: JSlider? = null
    @JvmField
    var signMovieWord_scale: JSlider? = null
    @JvmField
    var signMovieSentence_scale: JSlider? = null
    private val fname = "omega_settings.xml"
    @JvmField
    var lesson: Lesson? = null

    inner class ColorButton internal constructor(@JvmField var color: Color) : JButton(), ActionListener {
	init {
	    upd()
	    addActionListener(this)
	    text = t("Change Color...")
	}

	override fun actionPerformed(ev: ActionEvent) {
	    val nc = ColorChooser.select(color)
	    if (nc != null) {
		color = nc
		upd()
	    }
	}

	fun upd() {
	    background = color
	    val r = color.red
	    val g = color.green
	    val b = color.blue
	    val c = Color(0x80 xor r, 0x80 xor g, 0x80 xor b)
	    foreground = c
	}
    }

    public override fun save(): Boolean {
	val el = Element("omega-settings")
	val fel = Element("feedback_movie_background")
	fel.addAttr("color", "" + feedback_movie_background!!.color.rgb)
	val ael = Element("action_movie_background")
	ael.addAttr("color", "" + action_movie_background!!.color.rgb)
	val anel = Element("anim_background")
	anel.addAttr("color", "" + anim_background!!.color.rgb)
	el.add(fel)
	el.add(ael)
	el.add(anel)
	if (isLIU_Mode()) {
	    val swel = Element("signWord_background")
	    swel.addAttr("color", "" + signWord_background!!.color.rgb)
	    val ssel = Element("signSentence_background")
	    ssel.addAttr("color", "" + signSentence_background!!.color.rgb)
	    val ssalel = Element("signSentence_alpha")
	    ssalel.addAttr("value", "" + signSentence_alpha!!.value)
	    val swalel = Element("signWord_alpha")
	    swalel.addAttr("value", "" + signWord_alpha!!.value)
	    val smwel = Element("signMovieWord_scale")
	    smwel.addAttr("value", "" + signMovieWord_scale!!.value)
	    val smsel = Element("signMovieSentence_scale")
	    smsel.addAttr("value", "" + signMovieSentence_scale!!.value)
	    el.add(swel)
	    el.add(swalel)
	    el.add(ssel)
	    el.add(ssalel)
	    el.add(smwel)
	    el.add(smsel)
	}
	try {
	    XML_PW(createPrintWriterUTF8(fname), false).use { xmlpw -> xmlpw.put(el) }
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    return false
	}
	return true
    }

    public override fun load() {
	val el = loadElement()
	if (el != null) {
	    val fel = el.findElement("feedback_movie_background", 0)
	    val ael = el.findElement("action_movie_background", 0)
	    val anel = el.findElement("anim_background", 0)
	    val swel = el.findElement("signWord_background", 0)
	    val ssel = el.findElement("signSentence_background", 0)
	    val swalel = el.findElement("signWord_alpha", 0)
	    val ssalel = el.findElement("signSentence_alpha", 0)
	    val smwel = el.findElement("signMovieWord_scale", 0)
	    val smsel = el.findElement("signMovieSentence_scale", 0)
	    if (fel != null) {
		var coli = 256 * 256 * 40 + 256 * 40 + 100
		try {
		    val cols = fel.findAttr("color")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		var nc = Color(coli)
		feedback_movie_background!!.color = nc
		feedback_movie_background!!.upd()
		coli = 256 * 256 * 40 + 256 * 40 + 100
		try {
		    val cols = ael.findAttr("color")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		nc = Color(coli)
		action_movie_background!!.color = nc
		action_movie_background!!.upd()
		coli = 256 * 256 * 30 + 256 * 30 + 80
		try {
		    val cols = anel.findAttr("color")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		nc = Color(coli)
		anim_background!!.color = nc
		anim_background!!.upd()
		coli = 256 * 256 * 30 + 256 * 30 + 80
		try {
		    val cols = swel.findAttr("color")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		nc = Color(coli)
		if (signWord_background != null) {
		    signWord_background!!.color = nc
		    signWord_background!!.upd()
		}
		coli = 256 * 256 * 30 + 256 * 30 + 80
		try {
		    val cols = ssel.findAttr("color")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		nc = Color(coli)
		if (signSentence_background != null) {
		    signSentence_background!!.color = nc
		    signSentence_background!!.upd()
		}
		coli = 65
		try {
		    val cols = swalel.findAttr("value")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		if (signWord_alpha != null) signWord_alpha!!.value = coli
		coli = 92
		try {
		    val cols = ssalel.findAttr("value")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		if (signWord_alpha != null) signSentence_alpha!!.value = coli
		coli = 20
		try {
		    val cols = smwel.findAttr("value")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		if (signMovieWord_scale != null) signMovieWord_scale!!.value = coli
		coli = 40
		try {
		    val cols = smsel.findAttr("value")
		    coli = cols.toInt()
		} catch (ex: NullPointerException) {
		} catch (ex: NumberFormatException) {
		}
		if (signMovieSentence_scale != null) signMovieSentence_scale!!.value = coli
	    }
	}
    }

    fun loadElement(): Element? {
	return try {
	    SAX_node.parse(omegaAssets(fname), false)
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: Exception! Restore.restore(): $ex")
	    null
	}
    }

    inner class MyActionListener : ActionListener {
	override fun actionPerformed(ev: ActionEvent) {
	    val jb = ev.source as JButton
	    if (jb === color_theme_main) {
		if (lesson != null) {
		    lesson!!.displayColor("main")
		}
	    }
	    if (jb === color_theme_pupil) {
		if (lesson != null) {
		    lesson!!.displayColor("pupil")
		}
	    }
	    if (jb === color_theme_words) {
		if (lesson != null) {
		    lesson!!.displayColor("words")
		}
	    }
	    if (jb === color_theme_sent) {
		if (lesson != null) {
		    lesson!!.displayColor("sent")
		}
	    }
	}
    }

    var my_al: MyActionListener = MyActionListener()

    init {
	val c = contentPane
	c.layout = BorderLayout()
	val top = FormPanel()
	var X = 0
	var Y = 0
	top.add(JLabel(t("Action Movie Background")),
		ColorButton(Color(40, 40, 100)).also {
		    action_movie_background = it
		}, Y, ++X)
	X = 0
	Y++
	top.add(JLabel(t("Feedback Movie Background")),
		ColorButton(Color(80, 80, 120)).also {
		    feedback_movie_background = it
		}, Y, ++X)
	X = 0
	Y++
	top.add(JLabel(t("Animation Background")),
		ColorButton(Color(30, 30, 80)).also {
		    anim_background = it
		}, Y, ++X)
	if (isLIU_Mode()) {
	    X = 0
	    Y++
	    top.add(JLabel(t("Sign Word Background")),
		    ColorButton(Color(30, 30, 80)).also {
			signWord_background = it
		    }, Y, ++X)
	    X = 0
	    Y++
	    top.add(JLabel(t("Sign Word Transparence")),
		    JSlider(0, 100).also {
			signWord_alpha = it
		    }, Y, ++X)
	    signWord_alpha!!.majorTickSpacing = 20
	    signWord_alpha!!.minorTickSpacing = 5
	    signWord_alpha!!.paintTicks = true
	    signWord_alpha!!.paintLabels = true
	    X = 0
	    Y++
	    top.add(JLabel(t("Sign Word Movie Scale")),
		    JSlider(0, 100).also {
			signMovieWord_scale = it
		    }, Y, ++X)
	    signMovieWord_scale!!.majorTickSpacing = 20
	    signMovieWord_scale!!.minorTickSpacing = 5
	    signMovieWord_scale!!.paintTicks = true
	    signMovieWord_scale!!.paintLabels = true
	    X = 0
	    Y++
	    top.add(JLabel(t("Sign Sentence Background")),
		    ColorButton(Color(30, 30, 80)).also {
			signSentence_background = it
		    }, Y, ++X)
	    X = 0
	    Y++
	    top.add(JLabel(t("Sign Sentence Transparence")),
		    JSlider(0, 100).also {
			signSentence_alpha = it
		    }, Y, ++X)
	    signSentence_alpha!!.majorTickSpacing = 20
	    signSentence_alpha!!.minorTickSpacing = 5
	    signSentence_alpha!!.paintTicks = true
	    signSentence_alpha!!.paintLabels = true
	    X = 0
	    Y++
	    top.add(JLabel(t("Sign Sentence Movie Scale")),
		    JSlider(0, 100).also {
			signMovieSentence_scale = it
		    }, Y, ++X)
	    signMovieSentence_scale!!.majorTickSpacing = 20
	    signMovieSentence_scale!!.minorTickSpacing = 5
	    signMovieSentence_scale!!.paintTicks = true
	    signMovieSentence_scale!!.paintLabels = true
	}
	c.add(top, BorderLayout.CENTER)
	populateCommon()
	load()
	pack()
    }

    companion object {
	var fnf = FilenameFilter { dir, fname ->
	    if (fname.endsWith(".omega_colors")) {
		true
	    } else false
	}
    }
}
