package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaConfig.isKeyNext
import com.femtioprocent.omega.OmegaConfig.isKeySelect
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.adm.register.data.RegLocator
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.lesson.pupil.Pupil
import com.femtioprocent.omega.lesson.settings.PupilSettingsDialog
import com.femtioprocent.omega.swing.ScaledImageIcon.createImageIcon
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.xml.Element
import java.awt.*
import javax.swing.*
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

/*
--gapE---|=====|--gapB--|=========|---gapE---




 */
class PupilCanvas(l_ctxt: LessonContext?, pname: String) : BaseCanvas(l_ctxt!!), ListSelectionListener {
    private var item_fo: Font? = null
    var ppic: JLabel? = null
    var welcome: JLabel? = null
    var names_sp: JScrollPane? = null
    var names: JList<PupilItem?>? = null
    var pcr: PupilCellRenderer? = null
    var mode_gr = ButtonGroup()
    var pupilName = "Guest"
    var greeting_ic: ImageIcon? = null
    var behaviour = BH_PUPIL

    inner class PupilCellRenderer : JLabel(), ListCellRenderer<Any?> {
	init {
	    isOpaque = true
	}

	override fun getInsets(): Insets {
	    return Insets(5, 5, 5, 5)
	}

	override fun getListCellRendererComponent(
		list: JList<*>?,
		value: Any?,
		index: Int,
		isSelected: Boolean,
		cellHasFocus: Boolean
	): Component {
	    val pi = value as PupilItem?
	    //		setIcon(pi.im_ic);
	    verticalTextPosition = CENTER
	    horizontalTextPosition = RIGHT
	    horizontalAlignment = LEFT
	    verticalAlignment = CENTER
	    font = font1
	    if (value != null) text = t(value.toString())
	    background = if (isSelected) getColor("bg_tx") else getColor("bg_frbg")
	    foreground = if (isSelected) getColor("bg_frbg") else getColor("bg_tx")
	    return this
	}
    }

    inner class PupilItem(component: Component?, var name: String) {
	var im_name: String
	var w = 0
	var h = 0
	var im_ic: ImageIcon?

	init {
	    im_name = "register/$name.p/id.jpg"
	    var w = 52
	    w = caH / 7
	    val h = (w * 1.3).toInt()
	    im_ic = createImageIcon(
		    component,
		    im_name,
		    w,
		    h
	    )
	    if (im_ic == null) {
		im_name = "register/" + "Guest" + ".p/" + "id.jpg"
		im_ic = createImageIcon(
			component,
			im_name,
			w,
			h
		)
	    }
	}

	override fun toString(): String {
	    return name
	}
    }

    override fun ownKeyCode(kc: Int, is_shift: Boolean): Boolean {
	if (Lesson.skip_F) return true
	OmegaContext.def_log.getLogger().info("pupil own $kc")
	if (isKeyNext(kc)) if (is_shift) setPrevRed() else setNextRed()
	if (isKeySelect(kc)) {
	    val mb = focus_list!!.get()
	    mb?.doClick()
	}
	if (kc == 40) {
	    var ix = names!!.selectedIndex
	    ix++
	    val lix = names!!.model.size
	    OmegaContext.def_log.getLogger().info("++ $lix $ix")
	    if (ix >= lix) ix = 0
	    names!!.selectedIndex = ix
	}
	if (kc == 38) {
	    var ix = names!!.selectedIndex
	    ix--
	    val lix = names!!.model.size
	    OmegaContext.def_log.getLogger().info("-- $lix $ix")
	    if (ix < 0) ix = lix - 1
	    names!!.selectedIndex = ix
	}
	return true
    }

    val font1: Font
	get() {
	    if (item_fo == null) item_fo = Font("Arial", Font.PLAIN, (caH / 24))
	    return item_fo!!
	}
    override val panelName: String
	get() = "pupil"

    override fun enter() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "ENTER pupil");
	super.enter()
	if (buttons[0] == null) {
	    return
	} else {
//	    login.setText("Login");
	}
    }

    override fun resized() {
	greeting_ic = null
	item_fo = null
	populate()
    }

    override fun valueChanged(e: ListSelectionEvent) {
	try {
	    val l = e.source as JList<*>
	    if (l === names) {
		val pi = l.selectedValue
		if (pi != null) {
		    if (pi.name == "<New Pupil>" || pi.name == t("<New Pupil>")) {
			var pn: String? = null
			pn = JOptionPane.showInputDialog(
				ApplContext.top_frame,
				t("What is the new pupils name?")
			)
			if (pn != null && pn.length > 0) {
			    val loc = RegLocator()
			    loc.createPupilName(pn)
			    var pupil_settings_dialog: PupilSettingsDialog? = PupilSettingsDialog(Lesson.static_lesson)
			    pupil_settings_dialog!!.setPupil(Pupil(pn))
			    pupil_settings_dialog.isVisible = true
			    OmegaContext.def_log.getLogger().info("hidden +++++++++++++?")
			    pupil_settings_dialog = null
			    mkList(pn)
			}
			return
		    }
		    pupilName = pi.name
		    setPupil(pupilName)
		    val fpname = pupilName
		    SwingUtilities.invokeLater { l_ctxt.lesson.setPupil(fpname) }
		    //		mkButtons();
		}
	    }
	} catch (ex: Exception) {
	    Log.getLogger().severe("::: " + ex)
	}
    }

    fun changeBehaviour() {
	when (behaviour) {
	    BH_PUPIL -> behaviour = BH_ADMINISTRATOR
	    BH_ADMINISTRATOR -> behaviour = BH_PUPIL
	}
	mkButtons()
	mkList()
	m_sleep(200)
	repaint()
    }

    fun mkButtons() {
	if (behaviour == BH_ADMINISTRATOR) {
	    populateButtons(
		    arrayOf("Quit", "Pupil", "", "Test", "Result"),
		    arrayOf("quit", "pupil", "", "test_t", "result")
	    )
	} else if (behaviour == BH_PUPIL) {
	    populateButtons(
		    arrayOf("Quit", "", "Create sentence", "Test", ""),
		    arrayOf("quit", "", "create_p", "test_p", "")
	    )
	}
    }

    @JvmOverloads
    fun mkList(selected_name: String? = pupilName) {
	OmegaContext.def_log.getLogger().info("________-$selected_name")
	val new_pupil = behaviour == BH_ADMINISTRATOR
	val loc = RegLocator()
	val sa = loc.allPupilsName
	val pA = arrayOfNulls<PupilItem>(sa.size + if (new_pupil) 1 else 0)
	var selected_pupil: PupilItem? = null
	for (i in 0 until pA.size - if (new_pupil) 1 else 0) {
	    val pn = sa[i]
	    pA[i] = PupilItem(this, pn)
	    if (pn == selected_name) selected_pupil = pA[i]
	}
	if (new_pupil) pA[pA.size - 1] = PupilItem(this, t("<New Pupil>"))
	names!!.setListData(pA)
	if (selected_name != null && selected_pupil != null) {
	    names!!.setSelectedValue(selected_pupil, true)
	    OmegaContext.def_log.getLogger().info("selected value 1 " + names!!.selectedValue + ' ' + selected_name)
	} else {
	    names!!.setSelectedValue(t("Guest"), true)
	}
    }

    override fun populate() {
	val xW = 0.27
	val list_start_sx = 0.1
	val pupil_im_x = 0.5
	val Top_H = 0.15
	val Bot_H = 0.22
	val Cen_H = 1.0 - Top_H - Bot_H
	val Bot_Y = 1.0 - Bot_H
	val list_pupim_top = Top_H + 0.05
	val fs = caH / 33
	fo = Font("arial", Font.PLAIN, fs)
	mkButtons()
	if (ppic == null) {
	    ppic = JLabel(t(""))
	    add(ppic)
	    ppic!!.border = BorderFactory.createLineBorder(getColor("bg_fr"), 5)
	    welcome = JLabel(t("Welcome"))
	    welcome!!.font = font1
	    val color = getColor("bg_tx")
	    OmegaContext.def_log.getLogger().info("color bg_tx $color")
	    welcome!!.foreground = color
	    add(welcome)
	}
	if (pcr == null) pcr = PupilCellRenderer()
	if (names != null) {
	    val lsm = names!!.selectionModel
	    val lix = lsm.maxSelectionIndex
	    if (lix != -1) lsm.removeIndexInterval(0, lix)
	} else {
	    names = JList()
	    names_sp = JScrollPane(names)
	    add(names_sp)
	    names!!.setVisibleRowCount(7)
	    names!!.setCellRenderer(pcr)
	    names!!.addListSelectionListener(this)
	    names!!.setFocusTraversalKeysEnabled(false)
	    names!!.isRequestFocusEnabled = false
	    names!!.setFocusable(false)
	    // 	    names.setForeground(getColor("bg_tx"));
	    names!!.setBackground(getColor("bg_frbg"))
	    //	    names.addFocusListener(background_FA);
	}
	mkList()
	val h = caH / 5
	setPupil(pupilName)
	ppic!!.setLocation(gX(pupil_im_x), gY(list_pupim_top))
	set(ppic!!)
	welcome!!.font = font1
	welcome!!.setLocation(gX(pupil_im_x), gY(list_pupim_top + 0.05) + pupil_image_h)
	welcome!!.setSize(gX(0.5), gY(0.13))
	val sp = names_sp
	val l = names!!
	sp!!.setSize(gX(0.45 - list_start_sx), gY(Cen_H - 0.15))
	sp.setLocation(gX(list_start_sx), gY(list_pupim_top))
	//l.setFont(fo);
	l.ensureIndexIsVisible(l.selectedIndex)
	//	sp.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 5));
	repaint()
    }

    var pupil_image_h = 0
    private fun setPupil(name: String) {
	val w = (0.35 * caW).toInt()
	val h = (0.35 * caH).toInt()
	var imic: ImageIcon? = null
	try {
	    imic = createImageIcon(
		    this,
		    "register/$name.p/id.jpg",
		    w,
		    h,
		    false
	    )
	    if (imic == null) imic = createImageIcon(
		    this,
		    omegaAssets("media/default/pupil.jpg")!!,
		    w,
		    h,
		    false
	    )
	} catch (_ex: Exception) {
	}
	if (imic != null) {
	    ppic!!.setSize(imic.image.getWidth(null), imic.image.getHeight(null))
	    ppic!!.icon = imic
	    pupil_image_h = imic.image.getHeight(null)
	} else {
// 	    if ( name != "Guest")
// 		setPupil("Guest");
	}
	var name2 = name
	if (name == "Guest") {
	    name2 = t("Guest")
	    welcome!!.text = t("Welcome")
	} else {
	    welcome!!.text = t("Welcome") + ' ' + name
	}
	setSelectedPupil(name2)
	OmegaContext.def_log.getLogger().info("selected value 2 " + names!!.selectedValue + ' ' + name + ' ' + name2)
    }

    private fun setSelectedPupil(name: String) {
	for (i in 0..999) {
	    try {
		val pi = names!!.model.getElementAt(i) as PupilItem
		if (pi.name == name) names!!.selectedIndex = i
	    } catch (ex: Exception) {
	    }
	}
    }

    var current_ix = 0
    var current_iy = 0

    init {
	focus_list = CycleList(2)
	pupilName = pname
	focus_list!!.set(1)
    }

    fun gotoBox(ix: Int, iy: Int) {}
    fun gotoBoxRel(dx: Int, dy: Int) {}
    fun selectBox() {}
    override fun populateGUI() {}
    override fun paintComponent(g: Graphics) {
	if (buttons[0] == null) populate()
	if (names != null) names!!.ensureIndexIsVisible(names!!.selectedIndex)
	val g2 = g as Graphics2D
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	drawBG(g2)
	if (greeting_ic == null) {
	    greeting_ic = createImageIcon(
		    this,
		    "media/default/pupil_greeting.png",
		    gX(0.8),
		    gY(0.1)
	    )
	}
	if (greeting_ic != null) {
	    try {
		g2.drawImage(greeting_ic!!.image, gX(0.05), gY(0.02), null)
	    } catch (ex: NullPointerException) {
		ex.printStackTrace()
	    }
	}
	g.setColor(Color.black)
	val fo = g.getFont()
    }

    fun reloadPIM() {
	updateDisp()
	setPupil(pupilName)
    }

    override fun updateDisp() {
	val color = getColor("bg_tx")
	welcome!!.foreground = color
	names!!.background = getColor("bg_frbg")
	ppic!!.border = BorderFactory.createLineBorder(getColor("bg_fr"), 5)
	names!!.border = BorderFactory.createLineBorder(getColor("bg_fr"), 5)
    }

    override fun setSettingsFromElement(el: Element?) {
	super.setSettingsFromElement(el)
	if (welcome != null) {
// 	    Color color = getColor("bg_tx");
// 	    welcome.setForeground(color);
	    updateDisp()
	}
    }

    companion object {
	const val BH_PUPIL = 1
	const val BH_ADMINISTRATOR = 2
	const val BH_DEFAULT = BH_ADMINISTRATOR
	const val BH_OTHER = BH_PUPIL
	const val gapB = 5f
	const val gapE = 5f
	const val tgH = 8f
    }
}
