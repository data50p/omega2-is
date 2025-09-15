package com.femtioprocent.omega.lesson.settings

import com.femtioprocent.omega.OmegaConfig.isLIU_Mode
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssetsExist
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.appl.OmegaAppl
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.pupil.Pupil
import com.femtioprocent.omega.swing.ScaledImageIcon
import com.femtioprocent.omega.swing.filechooser.*
import com.femtioprocent.omega.t9n.T
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files.fileCopy
import com.femtioprocent.omega.util.Files.mkRelativeCWD
import com.femtioprocent.omega.util.Files.toURL
import com.femtioprocent.omega.util.SundryUtils.copyFile
import com.femtioprocent.omega.util.SundryUtils.createPrintWriterUTF8
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.empty
import com.femtioprocent.omega.xml.Element
import com.femtioprocent.omega.xml.SAX_node
import com.femtioprocent.omega.xml.XML_PW
import org.hs.jfc.FormPanel
import java.awt.BorderLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.io.IOException
import java.util.*
import javax.swing.*

class PupilSettingsDialog(var lesson: Lesson?) : SettingsDialog("Omega - " + t("Pupil Settings")) {
    var pupil_: Pupil? = null
    var pupil_name = JTextField("               ")
    var speed_slider: JSlider
    var frequency_slider: JSlider? = null
    var show_sentence: JCheckBox? = null
    var show_sound_word: JCheckBox? = null
    var show_sign_word: JCheckBox? = null
    var show_sign_sentence: JCheckBox? = null
    var pingSentence: JCheckBox? = null
    var pingAnim: JCheckBox? = null
    var repeat_anim: JCheckBox? = null
    var image_label: JLabel? = null
    var image_wrong_label: JLabel? = null
    var movie_label: JLabel? = null
    var sign_movie_label: JLabel? = null
    var lang_cb: JComboBox<MultiString?>? = null
    var theme_cb: JComboBox<MultiString>? = null
    var space_cb: JComboBox<MultiString?>? = null
    var text_on: JCheckBox
    var speech_on: JCheckBox? = null
    var image_on: JCheckBox? = null
    var image_wrong_on: JCheckBox? = null
    var movie_on: JCheckBox? = null
    var sign_movie_on: JCheckBox? = null
    var text_tf: JTextField
    var speech_tf: JTextField? = null
    var speech_set: JButton? = null
    var image_wrong_set: JButton? = null
    var image_set: JButton? = null
    var movie_set: JButton? = null
    var sign_movie_set: JButton? = null
    var color_change: JButton? = null
    var col_login: JButton
    var col_lesson: JButton
    var col_sent: JButton
    var col_words: JButton
    var jcomponent_hm: HashMap<String?, JComponent?> = HashMap()
    var cp_laf: FormPanel
    var cp_lang: FormPanel
    var cp_anim: FormPanel
    var cp_feedb: FormPanel
    var cp_admin: FormPanel

    //    SecureButton secure_jb;
    var secure_jb: JButton? = null
    var secure_delete_jb: JButton? = null
    var secure_warning: JLabel? = null
    var pupim_jl: JLabel? = null
    var pupim_jb: JButton? = null
    var was_deleted = false
    var active = false
    fun updColors(fname: String?) {
	val file = File(omegaAssets(fname))
	if (file.canWrite()) {
	    col_lesson.isEnabled = true
	    col_login.isEnabled = true
	    col_sent.isEnabled = true
	    col_words.isEnabled = true
	} else {
	    col_lesson.isEnabled = false
	    col_login.isEnabled = false
	    col_sent.isEnabled = false
	    col_words.isEnabled = false
	}
    }

    var but_al: ActionListener = object : ActionListener {
	override fun actionPerformed(ae: ActionEvent) {
	    if (ae.source === theme_cb) {
		val label = selectedColorFileLabel
		val fname = selectedColorFile
		if ("+" == label) createTheme(fname) else updColors(fname)
		return
	    }
	    val cmd = ae.actionCommand
	    when (cmd) {
		"selImage" -> {
		    val url_s = selImage()
		    if (url_s != null) {
			val tfn = mkRelativeCWD(url_s)
			image_label!!.text = tfn
			val imc = createImageIcon(tfn, 100, 80)
			image_label!!.icon = imc
			pack()
			doLayout()
		    }
		}

		"selImageWrong" -> {
		    val url_s = selImage()
		    if (url_s != null) {
			val tfn = mkRelativeCWD(url_s)
			image_wrong_label!!.text = tfn
			val imc = createImageIcon(tfn, 100, 80)
			image_wrong_label!!.icon = imc
			pack()
			doLayout()
		    }
		}

		"selMovie" -> {
		    val url_s = selMovie()
		    if (url_s != null) {
			var tfn = mkRelativeCWD(url_s)
			val tfno = tfn
			movie_label!!.text = tfn
			tfn = tfn!!.replaceFirst("\\.mpg".toRegex(), ".png")
			var imc = createImageIcon(tfn, 100, 80)
			if (imc == null) {
			    tfn = "$tfno.png"
			    imc = createImageIcon(tfn, 100, 80)
			    if (imc == null) {
				val iim_s = "media/default/moviefeedback.png"
				imc = createImageIcon(iim_s, 100, 80)
			    }
			}
			movie_label!!.icon = imc
			pack()
			doLayout()
		    }
		}

		"selSignMovie" -> {
		    val url_s = selMoviesDir()
		    if (url_s != null) {
			val tfn = mkRelativeCWD(url_s)
			val f = File(tfn)
			if (!f.exists()) {
			    JOptionPane.showMessageDialog(
				AnimContext.top_frame,
				t("Invalid directory name."),
				"Omega",
				JOptionPane.WARNING_MESSAGE
			    )
			    return
			}
			val tfno = tfn
			sign_movie_label!!.text = tfn
			var imc = createImageIcon(tfn + "moviesignfeedback.png", 100, 80)
			if (imc == null) {
			    var iim_s = "media/default/moviesignfeedback.png"
			    imc = createImageIcon(iim_s, 100, 80)
			    if (imc == null) {
				iim_s = "media/default/moviefeedback.png"
				imc = createImageIcon(iim_s, 100, 80)
			    }
			}
			sign_movie_label!!.icon = imc
			pack()
			doLayout()
		    }
		}

		"selSpeech" -> {
		    val url_s = selSpeech()
		    if (url_s != null) {
			var tfn = mkRelativeCWD(url_s)
			if (tfn!!.startsWith("media/")) {
			    tfn = tfn.substring(6)
			}
			speech_tf!!.text = tfn
		    }
		}

		"activate" -> {
		    active = !active
		    if (active) {
			showMore()
		    } else {
			showNoMore()
		    }
		}

		"deletePupil" -> {
		    deletePupil()
		    showNoMore()
		}

		"change_color_lesson" -> {
		    if (lesson != null) {
			lesson!!.displayColor("main")
		    }
		}

		"change_color_words" -> {
		    if (lesson != null) {
			lesson!!.displayColor("words")
		    }
		}

		"change_color_login" -> {
		    if (lesson != null) {
			lesson!!.displayColor("pupil")
		    }
		}

		"change_color_sent" -> {
		    if (lesson != null) {
			lesson!!.displayColor("sent")
		    }
		}

		"pupim" -> {
		    try {
			val choose_pif = ChoosePupilImageFile()
			val rv = choose_pif.showDialog(this@PupilSettingsDialog, t("Select"))
			if (rv == JFileChooser.APPROVE_OPTION) {
			    val file = choose_pif.selectedFile
			    val fn = file.name
			    val ix = fn.lastIndexOf('.')
			    var ext = ".jpg"
			    if (ix != -1) {
				ext = file.name.substring(ix)
			    }
			    val pims = "register/" + pupil_!!.name + ".p/id" + ext
			    val pimf = File(pims)
			    fileCopy(file, pimf)
			    val imc2 = createImageIcon(pims, 80, 60)
			    pupim_jl!!.icon = imc2
			}
		    } catch (ex: Exception) {
			OmegaContext.sout_log.getLogger().info("ERR: ex $ex")
			ex.printStackTrace()
		    }
		}
	    }
	}
    }

    private fun createTheme(fname: String?) {
	OmegaContext.serr_log.getLogger().info("Create this $fname")
	if (omegaAssetsExist(fname)) return
	val fnew = File(omegaAssets(fname))
	val fdef = File(omegaAssets("default.omega_colors"))
	try {
	    fnew.createNewFile()
	    copyFile(fdef, fnew)
	} catch (e: IOException) {
	    e.printStackTrace()
	}
	val ms = MultiString(t(fname!!), arrayOf<String?>(fname))
	theme_cb!!.removeAllItems()
	addColorItems()
	theme_cb!!.selectedItem = ms
    }

    private fun addColorItems() {
	val themes_v = themes
	for (o in themes_v) theme_cb!!.addItem(o)
    }

    fun createImageIcon(fn: String?, max_w: Int, max_h: Int): ImageIcon? {
	return ScaledImageIcon.createImageIcon(
	    this,
	    fn!!,
	    max_w,
	    max_h
	)
    }

    fun update_movie_on() {
// 	boolean b = ! movie_on.isSelected();
// 	text_on.setEnabled(b);
// 	speech_on.setEnabled(b);
// 	image_on.setEnabled(b);
// 	text_tf.setEnabled(b);
// 	speech_tf.setEnabled(b);
// 	image_label.setEnabled(b);
// 	speech_set.setEnabled(b);
// 	image_set.setEnabled(b);
// 	movie_label.setEnabled(!b);
// 	movie_set.setEnabled(!b);
    }

    fun update_sign_movie_on() {
// 	boolean b = ! movie_on.isSelected();
// 	text_on.setEnabled(b);
// 	speech_on.setEnabled(b);
// 	image_on.setEnabled(b);
// 	text_tf.setEnabled(b);
// 	speech_tf.setEnabled(b);
// 	image_label.setEnabled(b);
// 	speech_set.setEnabled(b);
// 	image_set.setEnabled(b);
// 	movie_label.setEnabled(!b);
// 	movie_set.setEnabled(!b);
    }

    init {
	val c = contentPane
	c.layout = BorderLayout()
	val pan: JPanel = object : JPanel() {
	    override fun getInsets(): Insets {
		return Insets(5, 5, 5, 5)
	    }
	}
	cp_anim = FormPanel(5, 5, 7, 15)
	cp_feedb = FormPanel(5, 5, 7, 10)
	cp_lang = FormPanel(5, 5, 7, 15)
	cp_laf = FormPanel(5, 5, 7, 15)
	cp_admin = FormPanel(5, 5, 7, 15)
	val cp_lang_panel = JPanel()
	val cp_laf_panel = JPanel()
	val cp_admin_panel = JPanel()
	cp_lang_panel.add(cp_lang)
	cp_laf_panel.add(cp_laf)
	cp_admin_panel.add(cp_admin)
	val tabbed_pane = JTabbedPane()
	tabbed_pane.addTab(t("Animation"), null, cp_anim, t("Settings for Animations"))
	tabbed_pane.addTab(t("Feedback"), null, cp_feedb, t("Settings for Feedback"))
	tabbed_pane.addTab(t("Language"), null, cp_lang_panel, t("Settings for Language"))
	tabbed_pane.addTab(t("Look&Feel"), null, cp_laf_panel, t("Settings for Look&Feel"))
	tabbed_pane.addTab(t("Admin"), null, cp_admin_panel, t("Some administrative settings"))
	pan.add(tabbed_pane, BorderLayout.CENTER)
	c.add(pan, BorderLayout.CENTER)


	// cp_anim
	speed_slider = JSlider(0, 2)
	speed_slider.snapToTicks = true
	speed_slider.majorTickSpacing = 1
	speed_slider.snapToTicks = true
	speed_slider.paintLabels = true
	speed_slider.paintTrack = true
	if (true) {
	    val lt: Hashtable<Int, JComponent> = Hashtable()
	    lt[0] = JLabel(t("slow"))
	    lt[1] = JLabel(t("normal"))
	    lt[2] = JLabel(t("fast"))
	    speed_slider.labelTable = lt
	}
	jcomponent_hm["speed"] = speed_slider
	var cb: JCheckBox
	var X = 0
	var Y = 1
	cp_anim.add(JLabel(t("Speed")), speed_slider, Y, ++X)
	Y++
	X = 0
	if (true) {
	    pingSentence = JCheckBox()
	    pingAnim = JCheckBox()
	    repeat_anim = JCheckBox()
	    cp_anim.add(JLabel(t("Repeat animation twice")), repeat_anim, Y, ++X)
	    Y++
	    X = 0
	    cp_anim.add(JLabel(t("Ping after complete sentence")), pingSentence, Y, ++X)
	    Y++
	    X = 0
	    cp_anim.add(JLabel(t("Ping before/after animation")), pingAnim, Y, ++X)
	    jcomponent_hm["repeatanim"] = repeat_anim
	    jcomponent_hm["pingSentence"] = pingSentence
	    jcomponent_hm["pingAnim"] = pingAnim
	}
	Y++
	X = 0
	if (true) {
	    cp_anim.add(JLabel(t("Show Sentence")), JCheckBox().also { show_sentence = it }, Y, ++X)
	    jcomponent_hm["showSentence"] = show_sentence
	}
	Y++
	X = 0
	cp_anim.add(JLabel(t("Sound after each word")), JCheckBox().also { show_sound_word = it }, Y, ++X)
	jcomponent_hm["showSoundWord"] = show_sound_word
	if (isLIU_Mode()) {
	    Y++
	    X = 0
	    cp_anim.add(JLabel(t("Show Sign after each word")), JCheckBox().also { show_sign_word = it }, Y, ++X)
	    jcomponent_hm["showSignWord"] = show_sign_word
	    Y++
	    X = 0
	    cp_anim.add(JLabel(t("Show Sign after sentence")), JCheckBox().also { show_sign_sentence = it }, Y, ++X)
	    jcomponent_hm["showSignSentence"] = show_sign_sentence
	}


	// -----------
	X = 0
	Y = 1
	var rb: JRadioButton
	var tf: JTextField
	var jl0: JLabel
	var chb: JCheckBox
	val item_sel_al = ActionListener { ae ->
	    val chb = ae.source as JCheckBox
	    if (chb === movie_on) {
		update_movie_on()
		if (chb.model.isSelected) {
		    sign_movie_on!!.isSelected = false
		}
	    }
	    if (chb === sign_movie_on) {
		update_sign_movie_on()
		if (chb.model.isSelected) {
		    movie_on!!.isSelected = false
		}
	    }
	}
	Y++
	X = 0
	cp_feedb.add(JLabel(t("Item: Text")), JCheckBox().also { chb = it }, Y, ++X)
	cp_feedb.add(JTextField("", 20).also { tf = it }, JLabel(""), Y, ++X)
	chb.addActionListener(item_sel_al)
	text_on = chb
	text_tf = tf
	jcomponent_hm["text"] = tf
	jcomponent_hm["text_on"] = chb
	Y++
	X = 0
	if (true) {
	    var jb: JButton
	    var rb0: JRadioButton
	    var rb1: JRadioButton
	    cp_feedb.add(JLabel(t("Item: Speech")), JCheckBox().also { chb = it }, Y, ++X)
	    cp_feedb.add(JTextField("", 20).also { tf = it }, JButton(t("Set...")).also { jb = it }, Y, ++X)
	    chb.addActionListener(item_sel_al)
	    jb.addActionListener(but_al)
	    jb.actionCommand = "selSpeech"
	    jcomponent_hm["speech"] = tf
	    jcomponent_hm["speech_on"] = chb
	    speech_tf = tf
	    speech_on = chb
	    speech_set = jb
	}
	if (true) {
	    Y++
	    X = 0
	    var jl: JLabel
	    val im_s = "media/default/feedback.png"
	    var sjb: JButton
	    cp_feedb.add(JLabel(t("Item: Image, positive")), JCheckBox().also { chb = it }, Y, ++X)
	    cp_feedb.add(JLabel(im_s).also { jl = it }, JButton(t("Set...")).also { sjb = it }, Y, ++X)
	    chb.addActionListener(item_sel_al)
	    sjb.addActionListener(but_al)
	    sjb.actionCommand = "selImage"
	    val imc = createImageIcon(im_s, 100, 80)
	    jl.icon = imc
	    image_label = jl
	    image_on = chb
	    image_set = sjb
	    jcomponent_hm["image"] = image_label
	    jcomponent_hm["image_on"] = chb
	}
	if (true) {
	    Y++
	    X = 0
	    var jl: JLabel
	    val im_s = "media/default/feedbackwrong.png"
	    var sjb: JButton
	    cp_feedb.add(JLabel(t("Item: Image, negative")), JCheckBox().also { chb = it }, Y, ++X)
	    cp_feedb.add(JLabel(im_s).also { jl = it }, JButton(t("Set...")).also { sjb = it }, Y, ++X)
	    chb.addActionListener(item_sel_al)
	    sjb.addActionListener(but_al)
	    sjb.actionCommand = "selImageWrong"
	    val imc = createImageIcon(im_s, 100, 80)
	    jl.icon = imc
	    image_wrong_label = jl
	    image_wrong_on = chb
	    image_wrong_set = sjb
	    jcomponent_hm["image_wrong"] = image_wrong_label
	    jcomponent_hm["image_wrong_on"] = chb
	}
	if (isLIU_Mode()) {
	    Y++
	    X = 0
	    var jl: JLabel
	    val im_s = "media/default/signFeedbackRight"
	    var sjb: JButton
	    cp_feedb.add(JLabel(t("Item: Sign Movies, positive ")), JCheckBox().also { chb = it }, Y, ++X)
	    cp_feedb.add(JLabel(im_s).also { jl = it }, JButton(t("Set...")).also { sjb = it }, Y, ++X)
	    chb.addActionListener(item_sel_al)
	    sjb.addActionListener(but_al)
	    sjb.actionCommand = "selSignMovie"
	    val imc = createImageIcon(im_s, 100, 80)
	    jl.icon = imc
	    sign_movie_label = jl
	    sign_movie_on = chb
	    sign_movie_set = sjb
	    jcomponent_hm["sign_movie"] = sign_movie_label
	    jcomponent_hm["sign_movie_on"] = chb
	}
	if (true) {
	    var jl: JLabel
	    var sjb: JButton
	    Y++
	    X = 0
	    val im_s = ""
	    cp_feedb.add(JLabel(t("Item: Movie")), JCheckBox().also { chb = it }, Y, ++X)
	    cp_feedb.add(JLabel(im_s).also { jl = it }, JButton(t("Set...")).also { sjb = it }, Y, ++X)
	    chb.addActionListener(item_sel_al)
	    sjb.addActionListener(but_al)
	    sjb.actionCommand = "selMovie"
	    val iim_s = "media/default/moviefeedback.png"
	    val imc = createImageIcon(iim_s, 100, 80)
	    jl.icon = imc
	    movie_label = jl
	    movie_on = chb
	    movie_set = sjb
	    jcomponent_hm["movie"] = movie_label
	    jcomponent_hm["movie_on"] = chb
	}
	Y++
	X = 0
	if (true) {
	    cp_feedb.add(JLabel(t("Frequence")), JSlider(0, 3).also { frequency_slider = it }, Y, ++X)
	    frequency_slider!!.snapToTicks = true
	    frequency_slider!!.majorTickSpacing = 1
	    frequency_slider!!.snapToTicks = true
	    frequency_slider!!.paintLabels = true
	    frequency_slider!!.paintTrack = true
	    if (true) {
		val lt: Hashtable<Int, JComponent> = Hashtable()
		lt[0] = JLabel(t("none"))
		lt[1] = JLabel(t("seldom"))
		lt[2] = JLabel(t("often"))
		lt[3] = JLabel(t("always"))
		frequency_slider!!.labelTable = lt
	    }
	    jcomponent_hm["frequence"] = frequency_slider
	}

	// -----------
	X = 0
	Y = 1
	val lang_v = languages // list of suffix to appemd to "lesson"
	cp_lang.add(JLabel(t("Language")), JComboBox<MultiString?>(lang_v).also { lang_cb = it }, Y, ++X)
	jcomponent_hm["languageSuffix"] = lang_cb
	X = 0
	Y++
	val themes_v = themes
	cp_laf.add(JLabel(t("Color theme")), JComboBox<MultiString>(themes_v).also { theme_cb = it }, Y, ++X)
	theme_cb!!.addActionListener(but_al)
	jcomponent_hm["theme"] = theme_cb
	var jb: JButton
	X = 0
	Y++
	cp_laf.add(JLabel(t("Change color")), JButton(t("Pupil Login")).also { jb = it }, Y, ++X)
	jb.actionCommand = "change_color_login"
	jb.addActionListener(but_al)
	col_login = jb
	X = 0
	Y++
	cp_laf.add(JLabel(""), JButton(t("Lesson select")).also { jb = it }, Y, ++X)
	jb.actionCommand = "change_color_lesson"
	jb.addActionListener(but_al)
	col_lesson = jb
	X = 0
	Y++
	cp_laf.add(JLabel(""), JButton(t("Story display")).also { jb = it }, Y, ++X)
	jb.actionCommand = "change_color_sent"
	jb.addActionListener(but_al)
	col_sent = jb
	X = 0
	Y++
	cp_laf.add(JLabel(""), JButton(t("Sentence build")).also { jb = it }, Y, ++X)
	jb.actionCommand = "change_color_words"
	jb.addActionListener(but_al)
	col_words = jb
	X = 0
	Y++
	val space_v = spaceKeys
	cp_laf.add(JLabel(t("Space key is")), JComboBox(space_v).also { space_cb = it }, Y, ++X)
	jcomponent_hm["space_key"] = space_cb

	// -----------
	X = 0
	Y = 1
	if (true) {
	    cp_admin.add(
		JLabel(t("Pupil Image")).also { pupim_jl = it },
		JButton(t("Set Pupil Image...")).also {
		    pupim_jb = it
		}, Y, ++X
	    )
	    val `val` = "register/" + "Guest" + ".p/id.png"
	    val imc2 = createImageIcon(`val`, 80, 60)
	    pupim_jl!!.icon = imc2
	    pupim_jb!!.addActionListener(but_al)
	    pupim_jb!!.actionCommand = "pupim"
	}
	X = 0
	Y++
	//	cp_admin.add(new JLabel(T.t("")),  secure_jb = new SecureButton(this, T.t("Activate 'Delete pupil'")), Y, ++X);
	cp_admin.add(JLabel(t("")), JButton(t("Activate 'Delete Pupil'")).also { secure_jb = it }, Y, ++X)
	secure_jb!!.actionCommand = "activate"
	secure_jb!!.addActionListener(but_al)
	X = 0
	Y++
	cp_admin.add(
	    JLabel(t("")),
	    JLabel(
		"<html><h3>" + t("Warning!!!") + "</h3>"
			+ t("The pupil data will ramain in the file system.<br>")
			+ "</html>"
	    ).also {
		secure_warning = it
	    },
	    Y,
	    ++X
	)
	X = 0
	Y++
	cp_admin.add(JLabel(t("")), JButton(t("Delete pupil")).also { secure_delete_jb = it }, Y, ++X)
	secure_delete_jb!!.actionCommand = "deletePupil"
	secure_delete_jb!!.addActionListener(but_al)
	X = 0
	Y++
	cp_admin.add(JLabel(t("")), JButton(t("Reset Starter")).also { jb = it }, Y, ++X)
	jb.actionCommand = "resetStarter"
	jb.addActionListener(but_al)

	// -----------
	val tpan = JPanel()
	tpan.add(JLabel(t("Pupil Name:")))
	tpan.add(pupil_name)
	pupil_name.isEditable = false
	c.add(tpan, BorderLayout.NORTH)
	populateCommon()
	pack()
	doLayout()
	secure_delete_jb!!.isVisible = false
	secure_warning!!.isVisible = false
    }

    fun add(el: Element, key: String?, `val`: String?) {
	if (key == null || `val` == null) {
	    return
	}
	el.add(Element("value").also {
	    it.addAttr("key", key)
	    it.addAttr("val", `val`)
	})
    }

    fun upd_jcomponent(key: String?, `val`: String) {
	var `val` = `val`
	val cmp = jcomponent_hm[key]
	if (cmp != null) {
	    if (cmp is JSlider) {
		val a = `val`.toInt()
		cmp.value = a
		return
	    }
	    if (cmp is JTextField) {
		cmp.text = `val`
		return
	    }
	    if (cmp is JCheckBox) {
		cmp.isSelected = "true" == `val`
		return
	    }
	    if (cmp is JComboBox<*>) {
		val cb = cmp
		if (cb === lang_cb) {
		    for (i in 0..99) {
			val ms = cb.getItemAt(i) as MultiString
			if (ms.sa[0] == `val`) {
			    cb.selectedIndex = i
			    return
			}
		    }
		}
		if (cb === theme_cb) {
		    for (i in 0..99) {
			val ms = cb.getItemAt(i) as MultiString
			if (ms.sa[0] == `val`) {
			    cb.selectedIndex = i
			    updColors(`val`)
			    return
			}
		    }
		}
		if (cb === space_cb) {
		    for (i in 0..9) {
			val ms = cb.getItemAt(i) as MultiString
			if (ms.sa[0] == `val`) {
			    cb.selectedIndex = i
			    return
			}
		    }
		}
		cb.selectedItem = `val`
		return
	    }
	    if (cmp is JRadioButton) {
		cmp.isSelected = "true" == `val`
		return
	    }
	    if (cmp is JLabel) {
		val jl = cmp
		jl.text = `val`
		if (jl === image_label) {
		    val imc = createImageIcon(`val`, 100, 80)
		    jl.icon = imc
		    pack()
		    doLayout()
		}
		if (jl === image_wrong_label) {
		    val imc = createImageIcon(`val`, 100, 80)
		    jl.icon = imc
		    pack()
		    doLayout()
		}
		if (jl === movie_label) {
		    val valo = `val`
		    `val` = `val`.replaceFirst("\\.mpg".toRegex(), ".png")
		    var imc = createImageIcon(`val`, 100, 80)
		    if (imc == null) {
			`val` = "$valo.png"
			imc = createImageIcon(`val`, 100, 80)
			if (imc == null) {
			    imc = createImageIcon("media/default/moviefeedback.png", 100, 80)
			}
		    }
		    jl.icon = imc
		    pack()
		    doLayout()
		}
	    }
	    return
	}
    }

    val elements: Element
	get() {
	    return Element("values").also {
		jcomponent_hm.keys.forEach { key ->
		    val com = jcomponent_hm[key]
		    if (com === lang_cb) {
			val lang = (lang_cb!!.selectedItem as MultiString).sa[0]
			add(it, "languageSuffix", lang)
		    } else if (com === theme_cb) {
			val th = (theme_cb!!.selectedItem as MultiString).sa[0]
			add(it, "theme", th)
		    } else if (com === space_cb) {
			val th = (space_cb!!.selectedItem as MultiString).sa[0]
			add(it, "space_key", th)
		    } else {
			val `val` = getValue(com)
			add(it, key, `val`)
		    }
		}
	    }
	}

    private fun getValue(cmp: JComponent?): String {
	if (cmp != null) {
	    if (cmp is JSlider) {
		return "" + cmp.value
	    }
	    if (cmp is JTextField) {
		return cmp.text
	    }
	    if (cmp is JCheckBox) {
		return if (cmp.isSelected) "true" else "false"
	    }
	    if (cmp is JRadioButton) {
		return if (cmp.isSelected) "true" else "false"
	    }
	    if (cmp is JLabel) {
		return cmp.text
	    }
	    return "?_"
	}
	return "?~"
    }

    fun selImage(): String? {
	val choose_if = ChooseImageFile()
	val url_s: String? = null
	val rv = choose_if.showDialog(this, t("Select"))
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_if.selectedFile
	    return toURL(file)
	}
	return null
    }

    fun selMovie(): String? {
	val choose_mf = ChooseMovieFile()
	val url_s: String? = null
	val rv = choose_mf.showDialog(this, t("Select"))
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_mf.selectedFile
	    return toURL(file)
	}
	return null
    }

    fun selMoviesDir(): String? {
	val choose_md = ChooseMovieDir()
	val url_s: String? = null
	val rv = choose_md.showDialog(this, t("Select Directory"))
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_md.selectedFile
	    return toURL(file)
	}
	return null
    }

    fun selSpeech(): String? {
	val choose_sf = ChooseSpeechFile()
	val url_s: String? = null
	val rv = choose_sf.showDialog(this, t("Select"))
	if (rv == JFileChooser.APPROVE_OPTION) {
	    val file = choose_sf.selectedFile
	    return toURL(file)
	}
	return null
    }

    val languages: Vector<MultiString?>
	// nb_NO_BN
	get() {
	    var inLang = T.lang
	    var bergen = false
	    var lang_bergen = -1
	    if ("nb_NO_BN" == T.lang) {
		inLang = "nb"
		bergen = true
	    }
	    val inlocale = Locale(T.lang)
	    val dot = File(omegaAssets("."))
	    val scanned_lang = dot.list { dir, name ->
		name.startsWith("lesson-")
	    }
	    if (scanned_lang == null) {
		OmegaAppl.closeSplash()
		JOptionPane.showMessageDialog(
		    null, // a_ctxt.
		    T.t("Can't find default.omega_assets")
		)
		System.exit(88)
	    }
	    val lA = arrayOfNulls<Locale>(scanned_lang.size)
	    for (i in lA.indices) {
		var l = scanned_lang[i].substring(7)
		if ("nb_NO_BN" == l) {
		    l = "nb"
		    lang_bergen = i
		}
		lA[i] = Locale(l)
	    }

	    val v: Vector<MultiString?> = Vector()
	    v.add(MultiString(t("Default language"), arrayOf("")))
	    for (i in lA.indices) {
		var dn = lA[i]!!.getDisplayName(inlocale)
		var la = "" + lA[i]!!.language
		if (dn == "no") {
		    dn = "bokmål"
		    la = "nb"
		}
		if (dn == "nb") {
		    dn = "bokmål"
		}
		if (dn == "nn") {
		    dn = "nynorsk"
		}
		if (i == lang_bergen) {
		    dn = "bokmål (bergen)"
		    la = "nb_NO_BN"
		}
		OmegaContext.lesson_log.getLogger().info("Lang: DN LA $dn $la")
		v.add(MultiString(dn, arrayOf<String?>(la)))
	    }
	    return v
	}

    val themes: Vector<MultiString>
	get() {
	    val dot = File(omegaAssets("."))
	    val scanned_themes = dot.list { dir, name ->
		name.endsWith(".omega_colors")
	    }
	    val v: Vector<MultiString> = Vector()
	    v.add(MultiString(t("Default Theme"), arrayOf<String?>("default.omega_colors")))
	    for (i in scanned_themes.indices) {
		v.add(MultiString(scanned_themes[i], arrayOf(scanned_themes[i])))
	    }
	    val n = freeOption()
	    if (n > 0) v.add(MultiString(t("+"), arrayOf("option$n.omega_colors")))
	    return v
	}

    private fun freeOption(): Int {
	for (i in 1..9) {
	    if (omegaAssetsExist("option$i.omega_colors")) continue
	    return i
	}
	return -1
    }

    val spaceKeys: Vector<MultiString>
	get() {
	    val v: Vector<MultiString> = Vector()
	    v.add(MultiString(t("select next"), arrayOf<String?>("next")))
	    v.add(MultiString(t("activate selected"), arrayOf<String?>("select")))
	    return v
	}

    fun getPupilDir(pup: Pupil?): String {
	return if (pupil_ == null) {
	    "register/Guest.p"
	} else "register/" + pup!!.name + ".p"
    }

    fun loadDefault() {
	upd_jcomponent("text", "")
	upd_jcomponent("speech", "")
	upd_jcomponent("image", "media/default/feedback.png")
	upd_jcomponent("image_wrong", "media/default/feedbackwrong.png")
	upd_jcomponent("movie", "")
	upd_jcomponent("text_on", "true")
	upd_jcomponent("speech_on", "false")
	upd_jcomponent("image_on", "false")
	upd_jcomponent("image_wrong_on", "false")
	upd_jcomponent("movie_on", "false")
	upd_jcomponent("speed", "1")
	upd_jcomponent("showSentence", "true")
	upd_jcomponent("showSignWord", "true")
	upd_jcomponent("showSoundWord", "true")
	upd_jcomponent("showSignSentence", "true")
	upd_jcomponent("pingSentence", "true")
	upd_jcomponent("pingAnim", "false")
	upd_jcomponent("frequence", "3")
	upd_jcomponent("theme", "default.omega_colors")
	upd_jcomponent("space_key", "next")
	upd_jcomponent("sign_movie_on", "false")
	upd_jcomponent("sign_movie", "")
    }

    override fun save(): Boolean {
	val dir = getPupilDir(pupil_)
	if (empty(dir)) {
	    return false
	}
	val fname = "$dir/pupil_settings.xml"
	val pel = Element("pupil_settings")
	val el = elements
	pel.add(el)
	try {
	    XML_PW(createPrintWriterUTF8(fname), false).use { xmlpw ->
		xmlpw.put(pel)
		return true
	    }
	} catch (ex: Exception) {
	    ex.printStackTrace()
	}
	return false
    }

    override fun load() {
	val fname = getPupilDir(pupil_) + "/pupil_settings.xml"
	loadDefault()
	try {
	    val el = SAX_node.parse(fname, false)
	    if (el == null) {
		loadDefault()
		return
	    }
	    val vel = el.findElement("values", 0)
	    for (i in 0..999) {
		val el1 = vel!!.findElement("value", i) ?: break
		val key = el1.findAttr("key")
		val `val` = el1.findAttr("val")
		upd_jcomponent(key, `val`!!)
	    }
	} catch (ex: Exception) {
	    loadDefault()
	}
	update_movie_on()
	pack()
	doLayout()
    }

    val params: HashMap<String?, String?>?
	get() {
	    val fname = getPupilDir(pupil_) + "/pupil_settings.xml"
	    try {
		val el = SAX_node.parse(fname, false) ?: return null
		val hm: HashMap<String?, String?> = HashMap()
		val vel = el.findElement("values", 0)
		var i = 0
		while (vel != null && i < 1000) {
		    val el1 = vel.findElement("value", i) ?: break
		    val key = el1.findAttr("key")
		    val `val` = el1.findAttr("val")
		    hm[key] = `val`
		    i++
		}
		return hm
	    } catch (ex: Exception) {
	    }
	    return null
	}

    fun setPupil(pupil: Pupil) {
	this.pupil_ = pupil
	val pname = pupil.name
	var pnameL = pupil.name
	if (pname == "Guest") {
	    pnameL = t("Guest")
	}
	//	secure_jb.cb.setEnabled(! "Guest".equals(pupil.getName()));
	load()
	pupil_name.text = pnameL
	val `val` = "register/$pname.p/id.png"
	val imc2 = createImageIcon(`val`, 80, 60)
	pupim_jl!!.icon = imc2
	pack()
    }

    private fun deletePupil() {
	val file = File("register/" + pupil_!!.name + ".p")
	val file2 = File("register/" + pupil_!!.name + ".deleted")
	if (file2.exists()) {
	    val file3 = File("register/" + pupil_!!.name + ".deleted_" + ct())
	    file2.renameTo(file3)
	}
	file.renameTo(file2)
	was_deleted = true
    }

    fun showMore() {
	secure_jb!!.text = t("Deactivate")
	secure_delete_jb!!.isVisible = true
	secure_warning!!.isVisible = true
	pack()
	doLayout()
    }

    fun showNoMore() {
	secure_jb!!.text = t("Activate 'Delete Pupil'")
	secure_delete_jb!!.isVisible = false
	secure_warning!!.isVisible = false
	pack()
    }

    val selectedColorFile: String?
	get() = (theme_cb!!.selectedItem as MultiString).sa[0]
    val selectedColorFileLabel: String
	get() = (theme_cb!!.selectedItem as MultiString).s
}
