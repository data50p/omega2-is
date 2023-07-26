package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaConfig.isLIU_Mode
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.antiOmegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.lessonLang
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssetsName
import com.femtioprocent.omega.OmegaContext.Companion.setOmegaAssets
import com.femtioprocent.omega.anim.appl.AnimEditor
import com.femtioprocent.omega.anim.appl.EditStateListener
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.appl.LessonEditor
import com.femtioprocent.omega.lesson.appl.LessonEditor.Companion.setDirty
import com.femtioprocent.omega.lesson.machine.Target
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.swing.filechooser.ChooseLessonFile
import com.femtioprocent.omega.swing.filechooser.ChooseOmegaAssetsDir
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files.mkRelativeCWD
import com.femtioprocent.omega.util.Files.toURL
import com.femtioprocent.omega.util.Log.getLogger
import com.femtioprocent.omega.value.Value
import com.femtioprocent.omega.value.Values
import com.femtioprocent.omega.value.ValuesListener
import java.awt.Color
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.util.*
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LessonEditorPanel(@JvmField var le_canvas: LessonCanvas) : JPanel() {
    var gbcf = GBC_Factory()

    //      JComboBox bgcol;
    //      JComboBox btcol;
    //      JComboBox sncol;
    var redraw: JButton? = null
    var editanim: JButton? = null
    var playSign: JButton? = null
    var setAssets: JButton? = null
    var tg_prop: TargetProperty? = null
    var assets_prop: OmegaAssetsProperty? = null
    var i_prop: ItemProperty? = null

    //    TestProperty tst_prop;
    var snt_prop: SentenceProperty? = null
    var active_target_ix = -1
    var active_item_ix = -1
    var active_item_iy = -1
    var anim_editor: AnimEditor? = null
    var omega_assets_name: JTextField? = null
    var lesson_name: JTextField? = null
    var lesson_link_next: JTextField
    var enable_LLN: JCheckBox
    var first_LLN: JCheckBox
    var getFiles_LLN: JButton? = null
    var getOmegaAssetsDependenciec: JButton? = null
    var jbPlayAll: JButton?
    var editorLessonLang: JTextField? = null
    var skipDirty = false

    inner class myDocumentListener : DocumentListener {
	override fun changedUpdate(de: DocumentEvent) {
	    val doc = de.document
	    if (!skipDirty) setDirty()
	}

	override fun insertUpdate(de: DocumentEvent) {
	    val doc = de.document
	    if (!skipDirty) setDirty()
	}

	override fun removeUpdate(de: DocumentEvent) {
	    val doc = de.document
	    if (!skipDirty) setDirty()
	}
    }

    var mydocl: myDocumentListener = myDocumentListener()
    var mvl_tg: ValuesListener = object : ValuesListener {
	override fun changed(v: Value?) {
//log		OmegaContext.sout_log.getLogger().info(":--: " + "=00= val list " + v);
	    val tit = le_canvas.target!!.getT_Item(active_target_ix) ?: return
	    if (v!!.id == "tid") tit.tid =
		v.str else if (v.id == "lid") tit.lID4TgOrNull_KeepVar_set(v.str) else if (v.id == "type") tit.type =
		v.str
	    le_canvas.repaint(tit)
	}
    }
    var mvl_it: ValuesListener = object : ValuesListener {
	override fun changed(v: Value?) {
	    val it_ix = active_item_ix
	    val it_iy = active_item_iy
	    val itm = le_canvas.target!!.getItemAt(it_ix, it_iy) ?: return
	    val is_action = itm.it_ent!!.type == "action"
	    if (v!!.id == "text") {
		itm.setText_Krull(v.str!!)
	    } else if (v.id == "tts") {
		itm.setTTS_Krull(v.str!!)
	    } else if (v.id == "dummytext") {
		itm.setDummyText_Krull(v.str, true)
	    } else if (v.id == "tid") {
		itm.it_ent!!.tid = v.str
	    } else if (v.id == "v1") {
		itm.setVar(1, v.str)
	    } else if (v.id == "v2") {
		itm.setVar(2, v.str)
	    } else if (v.id == "v3") {
		itm.setVar(3, v.str)
	    } else if (v.id == "v4") {
		itm.setVar(4, v.str)
	    } else if (v.id == "v5") {
		itm.setVar(5, v.str)
	    } else if (v.id == "lid") {
		itm.setLid_Krull(v.str!!)
	    } else if (v.id == "sound") {
		itm.setSound_Krull(v.str!!)
	    } else if (v.id == "sign") {
		itm.setSign_Krull(v.str!!)
	    } else if (v.id == "dummysound") {
		itm.setDummySound_Krull(v.str, true)
	    } else if (v.id == "dummysign") {
		itm.setDummySign_Krull(v.str, true)
	    } else if (v.id == "fname") {
		getLogger().info(":--: " + "FNAME " + itm.it_ent!!.type)
		if (true || itm.it_ent!!.type == "action") { // isAction ) {
		    itm.setAction_Fname(v.str, "omega_anim")
		}
	    } else if (v.id == "ftype") {
		if (itm.isAction) {
		    itm.action_type = v.str
		}
	    }
	    le_canvas.repaint(itm)
	}
    }

    inner class AutoPlayNext {
	var playNext = -1
	var allSentences = arrayOfNulls<String>(0)
	fun reset() {
	    playNext = -1
	    allSentences = arrayOfNulls(0)
	    jbPlayAll!!.text = t("Play All")
	}

	fun firstTime(): Boolean {
	    return playNext == -1
	}

	fun start(allSentences: Array<String?>) {
	    playNext = 0
	    this.allSentences = allSentences
	    jbPlayAll!!.text = t("Play Next")
	}

	fun done(): Boolean {
	    return allSentences.size <= playNext
	}

	fun nextSentence(): String? {
	    return allSentences[playNext++]
	}

	fun prevSentence(): String? {
	    return if (playNext > 0) allSentences[--playNext] else allSentences[playNext]
	}
    }

    var autoPlayNext = AutoPlayNext()

    inner class myActionListener : ActionListener {
	override fun actionPerformed(ev: ActionEvent) {
	    val s = ev.actionCommand
	    if (s == "tg_prop") {
		popupTargetProp()
	    }
	    //  	    if ( s.equals("tst_prop") ) {
//  		popupTestProp();
//  	    }
	    if (s == "snt_prop") {
		popupSentenceProp()
	    }
	    if (s == "itm_prop") {
		popupItemProp()
	    }
	    if (s == "redraw") {
		autoPlayNext.reset()
		le_canvas.reCreateBoxesKeep()
	    }
	    if (s == "play") {
		le_canvas.l_ctxt.lesson.sendMsg("play&return", null)
	    }
	    if (s == "playAll") {
		val target = le_canvas.target
		try {
		    if (autoPlayNext.firstTime()) {
			val tg2 = Target()
			val story_hm = Lesson.story_hm
			tg2.loadFromEl(le_canvas.l_ctxt.lesson.element, "", story_hm, false, false) // FIX nomix?
			val sa = tg2.getAllTargetCombinations(" ")
			autoPlayNext.start(sa)
		    } else {
			if (autoPlayNext.done()) {
			    autoPlayNext.reset()
			    return
			}
			val sentence =
			    if (ev.modifiers and ActionEvent.CTRL_MASK == 0) autoPlayNext.nextSentence() else autoPlayNext.prevSentence()
			val tg2 = Target()
			val story_hm = Lesson.story_hm
			tg2.loadFromEl(le_canvas.l_ctxt.lesson.element, "", story_hm, false, false) // FIX nomix?
			val allTargetCombinationsIndexes = tg2.getAllTargetCombinationsIndexes(
			    sentence!!
			)
			var ixTg = 0
			for (ixArr in allTargetCombinationsIndexes) {
			    OmegaContext.sout_log.getLogger()
				.info("Sentence: " + sentence + ' ' + Arrays.toString(ixArr))
			    le_canvas.l_ctxt.lesson.l_ctxt.target!!.pickItemAt(ixArr[1], ixArr[2], ixTg)
			    ixTg++
			}
			le_canvas.reCreateBoxesKeep()
			val th = Thread {
			    try {
				Thread.sleep(1700)
			    } catch (e: InterruptedException) {
				e.printStackTrace()
			    }
			    if (ev.modifiers and ActionEvent.SHIFT_MASK == 0) {
				le_canvas.l_ctxt.lesson.sendMsg("playAll", null)
			    } else {
			    }
			}
			th.start()
		    }
		} catch (ex: Exception) {
		    ex.printStackTrace()
		}
	    }
	    if (s == "playSign") {
		playSign()
	    }
	    if (s == "listen") {
		le_canvas.l_ctxt.lesson.sendMsg("listen", null)
	    }
	    if (s == "editanim") {
		val fn = le_canvas.l_ctxt.target!!.getActionFileName(0) // main default, first
		getLogger().info(":--: MANY? $fn")
		if (fn == null || fn.length == 0) {
		    JOptionPane.showMessageDialog(
			LessonEditor.TOP_JFRAME,  //le_canvas.l_ctxt.top_frame,
			t("Can't find lesson file: ") +
				fn
		    )
		} else {
		    if (anim_editor == null) {
			anim_editor = AnimEditor(fn)
			anim_editor!!.addEditStateListener(object : EditStateListener {
			    override fun dirtyChanged(is_dirty: Boolean) {
				if (is_dirty) {
				    editanim!!.foreground = Color.red
				} else {
				    editanim!!.foreground = Color.black
				}
			    }
			})
		    } else {
			anim_editor!!.isVisible = true
			anim_editor!!.loadFile(fn)
		    }
		}
	    }
	    if (s == "enableLLN") {
		val b = enable_LLN.isSelected
		lesson_link_next.isEnabled = b
		first_LLN.isEnabled = b
	    }
	    if (s == "getFiles_LLN") {
		val choose_af = ChooseLessonFile(1)
		val rv = choose_af.showDialog(this@LessonEditorPanel, t("Select"))
		if (rv == JFileChooser.APPROVE_OPTION) {
		    try {
			val file = choose_af.selectedFile
			var fname_s = file.name
			//log			OmegaContext.sout_log.getLogger().info(":--: " + "--> " + fname_s);
//                        fname_s = file.toURI().toURL().toString(); // getCanonicalPath();
			fname_s = toURL(file)
			//log			OmegaContext.sout_log.getLogger().info(":--: " + "--> " + fname_s);
			var fn = mkRelativeCWD(fname_s)
			fn = antiOmegaAssets(fn)
			//log			OmegaContext.sout_log.getLogger().info(":--: " + "--> " + fn);
			lesson_link_next.text = fn
			enable_LLN.isSelected = true
			lesson_link_next.isEnabled = true
			//			LessonEditor.setDirty();
		    } catch (ex: Exception) {
			getLogger().info("ERR: can't $ex")
		    }
		}
	    }
	    if (s == "getOmegaAssetsDependenciec") {
		popupOmegaAssetsProp()
	    }
	    if (s == "setassets") {
		val old = UIManager.getBoolean("FileChooser.readOnly")
		val chooseOmegaAssetsDir = ChooseOmegaAssetsDir()
		UIManager.put("FileChooser.readOnly", old)
		var url_s: String? = null
		val rv = chooseOmegaAssetsDir.showDialog(null, t("Select"))
		if (rv == JFileChooser.APPROVE_OPTION) {
		    val file = chooseOmegaAssetsDir.selectedFile
		    try {
			url_s = toURL(file.canonicalFile)
			url_s = url_s!!.replace("/$".toRegex(), "")
			url_s = rmDuplicate(url_s)
			val oa = mkRelativeCWD(url_s)
			OmegaContext.serr_log.getLogger().info("setOmegaAssets: $url_s")
			setOmegaAssets(oa)
			omega_assets_name!!.text = omegaAssetsName()
		    } catch (e: IOException) {
		    }
		}
	    }
	}

	private fun rmDuplicate(url_s: String): String {
	    val i1 = url_s.indexOf(OmegaContext.OMEGA_ASSETS_SUFFIX)
	    val i2 = url_s.lastIndexOf(OmegaContext.OMEGA_ASSETS_SUFFIX)
	    if (i1 == i2) return url_s
	    var s = url_s.substring(0, i1)
	    s += OmegaContext.OMEGA_ASSETS_SUFFIX
	    return s
	}

	private fun playSign() {
	    // LIU
	    lessonLang = editorLessonLang!!.text
	    le_canvas.l_ctxt.lesson.sendMsg("playSign:", "media/sign-sv/M.mpg")
	}
    }

    var myactl: myActionListener = myActionListener()

    inner class myChangeListener : ChangeListener {
	override fun stateChanged(ev: ChangeEvent) {
	    val b = enable_LLN.isSelected
	    lesson_link_next.isEnabled = b
	    first_LLN.isEnabled = b
	    if (!skipDirty) setDirty()
	}
    }

    var mychtl = myChangeListener()

    init {
	layout = GridBagLayout()
	var cb: JComboBox<*>
	var b: JButton
	var Y = 0
	var X = 0
	add(JLabel(t("Omega Assets:")), gbcf.createL(X++, Y, 1))
	add(JTextField(t("")).also { omega_assets_name = it }, gbcf.createL(X++, Y, 2))
	omega_assets_name!!.addActionListener(myactl)
	omega_assets_name!!.setActionCommand("omega_assets_name")
	omega_assets_name!!.isEditable = false
	omega_assets_name!!.text = omegaAssetsName()
	val doc3 = omega_assets_name!!.document
	doc3.addDocumentListener(mydocl)
	X++
	add(JButton(t("Set Assets")).also { setAssets = it }, gbcf.createL(X++, Y, 1))
	setAssets!!.actionCommand = "setassets"
	setAssets!!.addActionListener(myactl)
	add(
	    JButton("Assets Bundle...").also { getOmegaAssetsDependenciec = it }.also { b = it },
	    gbcf.createL(X++, Y, 1)
	)
	b.actionCommand = "getOmegaAssetsDependenciec"
	b.addActionListener(myactl)
	Y++
	X = 0
	add(JLabel(t("Lesson name:")), gbcf.createL(X++, Y, 1))
	add(JTextField(t("")).also { lesson_name = it }, gbcf.createL(X++, Y, 1))
	lesson_name!!.addActionListener(myactl)
	lesson_name!!.setActionCommand("lesson_name")
	var doc2 = lesson_name!!.document
	doc2.addDocumentListener(mydocl)
	add(JLabel(t("Next story:")), gbcf.createL(X++, Y, 1))
	val jplln = JPanel()
	lesson_link_next = JTextField(t(""), 20)
	doc2 = lesson_link_next.document
	doc2.addDocumentListener(mydocl)
	enable_LLN = JCheckBox()
	enable_LLN.actionCommand = "enableLLN"
	enable_LLN.addChangeListener(mychtl)
	jplln.add(enable_LLN)
	jplln.add(lesson_link_next)
	add(jplln, gbcf.createL(X++, Y, 2))
	jplln.add(JButton("...").also { getFiles_LLN = it }.also { b = it }, gbcf.createL(X++, Y, 1))
	b.actionCommand = "getFiles_LLN"
	b.addActionListener(myactl)
	add(JLabel(t("First in story:")), gbcf.createL(X++, Y, 1))
	first_LLN = JCheckBox()
	first_LLN.actionCommand = "firstLLN"
	first_LLN.addChangeListener(mychtl)
	add(first_LLN, gbcf.createL(X++, Y, 1))
	Y++
	X = 0
	add(JLabel(t("Properties:")), gbcf.createL(X++, Y, 1))
	add(JButton(t("Target")).also { b = it }, gbcf.createL(X++, Y, 1))
	b.actionCommand = "tg_prop"
	b.addActionListener(myactl)
	add(JButton(t("Item")).also { b = it }, gbcf.create(X++, Y))
	b.actionCommand = "itm_prop"
	b.addActionListener(myactl)
	add(JButton(t("Sentence")).also { b = it }, gbcf.createL(X++, Y, 1))
	b.actionCommand = "snt_prop"
	b.addActionListener(myactl)
	val jplln2 = JPanel()
	add(jplln2, gbcf.createL(X++, Y, 1))
	jplln2.add(JLabel(t("Lesson lang:")))
	jplln2.add(JTextField(t("Lesson Lang"), 8).also { editorLessonLang = it })
	editorLessonLang!!.text = lessonLang
	Y++
	X = 0
	add(JLabel(t("Commands:")), gbcf.createL(X++, Y, 1))
	add(JButton(t("Redraw")).also { redraw = it }, gbcf.createL(X++, Y, 1))
	redraw!!.actionCommand = "redraw"
	redraw!!.addActionListener(myactl)
	add(JButton(t("Play")).also { redraw = it }, gbcf.createL(X++, Y, 1))
	redraw!!.actionCommand = "play"
	redraw!!.addActionListener(myactl)
	add(JButton(t("Play All")).also { redraw = it }, gbcf.createL(X++, Y, 1))
	redraw!!.actionCommand = "playAll"
	redraw!!.addActionListener(myactl)
	jbPlayAll = redraw
	add(JButton(t("Listen")).also { redraw = it }, gbcf.createL(X++, Y, 1))
	redraw!!.actionCommand = "listen"
	redraw!!.addActionListener(myactl)
	if (isLIU_Mode()) {
	    add(JButton(t("Play Sign Moview")).also { playSign = it }, gbcf.createL(X++, Y, 1))
	    playSign!!.actionCommand = "playSign"
	    playSign!!.addActionListener(myactl)
	}
	add(JButton(t("Edit anim")).also { editanim = it }, gbcf.createL(X++, Y, 1))
	editanim!!.actionCommand = "editanim"
	editanim!!.addActionListener(myactl)
    }

    fun destroyAllPopups() {
	autoPlayNext.reset()
	if (snt_prop != null) {
	    snt_prop!!.destroy()
	    snt_prop!!.removeAll()
	    snt_prop!!.isVisible = false
	    snt_prop = null
	}
	if (tg_prop != null) {
	    tg_prop!!.removeAll()
	    tg_prop!!.isVisible = false
	    tg_prop = null
	}
	if (i_prop != null) {
	    i_prop!!.removeAll()
	    i_prop!!.isVisible = false
	    i_prop = null
	}
    }

    fun popupSentenceProp() {
	if (snt_prop == null) {
	    snt_prop = SentenceProperty(LessonEditor.TOP_JFRAME!!, le_canvas.l_ctxt)
	    snt_prop!!.addValuesListener(mvl_tg)
	} else {
	    snt_prop!!.destroy()
	    snt_prop!!.removeAll()
	    snt_prop!!.isVisible = false
	    snt_prop = SentenceProperty(LessonEditor.TOP_JFRAME!!, le_canvas.l_ctxt)
	    snt_prop!!.addValuesListener(mvl_tg)
	}
	snt_prop!!.isVisible = true
    }

    fun popupTargetProp() {
	if (tg_prop == null) {
	    tg_prop = TargetProperty(LessonEditor.TOP_JFRAME)
	    tg_prop!!.addValuesListener(mvl_tg)
	}
	tg_prop!!.isVisible = true
    }

    fun popupOmegaAssetsProp() {
	if (assets_prop == null) {
	    assets_prop = OmegaAssetsProperty(LessonEditor.TOP_JFRAME, le_canvas.l_ctxt)
	    assets_prop!!.addValuesListener(mvl_tg)
	} else {
	    assets_prop!!.destroy()
	    assets_prop!!.removeAll()
	    assets_prop!!.isVisible = false
	    assets_prop = OmegaAssetsProperty(LessonEditor.TOP_JFRAME, le_canvas.l_ctxt)
	    assets_prop!!.addValuesListener(mvl_tg)
	}
	assets_prop!!.isVisible = true
    }

    fun popupItemProp() {
	if (i_prop == null) {
	    i_prop = ItemProperty(LessonEditor.TOP_JFRAME)
	    i_prop!!.addValuesListener(mvl_it)
	}
	i_prop!!.isVisible = true
    }

    fun setTarget(vs: Values?) {
	if (tg_prop != null) tg_prop!!.updValues(vs!!)
	//	if ( vs.
    }

    fun setActiveTargetIx(ix: Int) {
	active_target_ix = ix
    }

    fun setActiveItemIx(ix: Int, iy: Int) {
	active_item_ix = ix
	active_item_iy = iy
    }

    fun setItem(vs: Values?) {
	if (i_prop != null) i_prop!!.updValues(vs!!)
    }

    var lessonName: String?
	get() = lesson_name!!.text
	set(s) {
	    skipDirty = true
	    lesson_name!!.text = s
	    skipDirty = false
	}
    var lessonLinkNext: String?
	get() = if (enable_LLN.isSelected) lesson_link_next.text else null
	set(s) {
	    var s = s
	    skipDirty = true
	    val b = s == null
	    enable_LLN.isSelected = !b
	    if (s == null) s = ""
	    lesson_link_next.text = s
	    skipDirty = false
	}
    var lessonIsFirst: Boolean
	get() = first_LLN.isSelected
	set(b) {
	    skipDirty = true
	    first_LLN.isSelected = b
	    skipDirty = false
	}
}
