package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaConfig.isKeyNext
import com.femtioprocent.omega.OmegaConfig.isKeySelect
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.message.Manager
import com.femtioprocent.omega.swing.ScaledImageIcon.createImageIcon
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.empty
import com.femtioprocent.omega.util.SundryUtils.formatDisplayText
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.xml.Element
import java.awt.*
import java.awt.event.*
import java.awt.geom.Arc2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.event.EventListenerList
import javax.swing.event.MouseInputAdapter

open class BaseCanvas(l_ctxt: LessonContext) : JPanel() {
    var om_msg_mgr = Manager()
    var lc_listeners: EventListenerList
    var l_ctxt: LessonContext
    var colors = HashMap<String, ColorColors>()
    var fo: Font? = null
    var ignore_press = false
    open var cmp_li: ComponentAdapter = object : ComponentAdapter() {
	override fun componentResized(ev: ComponentEvent) {
	    resized()
	    val th = Thread {
		m_sleep(500)
		this@BaseCanvas.resized()
	    }
	    th.start()
	}
    }
    var act_li = ActionListener { ae ->
	val ac = ae.actionCommand
	OmegaContext.def_log.getLogger().info("Fire... $ac")
	if (!ignore_press) {
	    val mb = ae.source as MyButton
	    val ob = focus_list!!.get()
	    if (ob != null) {
		ob.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
		ob.repaint()
	    }
	    mb.border = BorderFactory.createLineBorder(Color(0, 0, 120), 5)
	    om_msg_mgr.fire("button $ac")
	    mb.border = BorderFactory.createLineBorder(Color(242, 80, 80), 5)
	}
	OmegaContext.def_log.getLogger().info("Fired $ac")
    }

    data class ColorColors(val color: Color?, val colors: HashMap<String, Color>?) {
	companion object {
	    fun create(r: Int, g: Int, b: Int): ColorColors {
		return ColorColors(Color(r, g, b), null)
	    }

	    fun create(rgb: Int): ColorColors {
		return ColorColors(Color(rgb), null)
	    }
	}
    }

    inner class FrameState {
	var states = "A"
	var ix = 0

	init {
	    setStates("A", 0)
	}

	operator fun next() {
	    ix++
	    if (ix >= states.length) ix = 0
	}

	fun prev() {
	    ix--
	    if (ix < 0) ix = states.length - 1
	}

	fun get(): Char {
	    return states[ix]
	}

	fun set(ch: Char) {
	    for (i in 0 until states.length) if (states[i] == ch) {
		ix = i
		return
	    }
	}

	fun setStates(ss: String?, n: Int) {
	    var ss = ss
	    val ch = get()
	    for (i in 0 until n) ss += "" + i
	    set(ch)
	}
    }

    var frame_state = FrameState()

    inner class ImageArea internal constructor(
	txt: String?,
	var ord: Int,
	var im_n: String,
	var im_enter_n: String,
	var w: Int,
	var h: Int,
	var o: Any
    ) : JLabel(txt) {
	var im_ic: ImageIcon? = null
	var im_enter_ic: ImageIcon? = null

	inner class Mouse : MouseInputAdapter() {
	    var mpress_p: Point2D? = null

	    init {
		addMouseListener(this)
		addMouseMotionListener(this)
	    }

	    override fun mousePressed(e: MouseEvent) {
		mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    }

	    override fun mouseMoved(e: MouseEvent) {}
	    override fun mouseDragged(e: MouseEvent) {}
	    override fun mouseReleased(e: MouseEvent) {
		om_msg_mgr.fire("imarea " + panelName + ":lesson_" + ord)
	    }

	    override fun mouseEntered(e: MouseEvent) {
		showEnter()
	    }

	    override fun mouseExited(e: MouseEvent) {
		showNormal()
	    }
	}

	var m: Mouse = Mouse()

	init {
	    foreground = Color.black
	}

	fun setNewImage(fn: String, fn_e: String) {
	    im_enter_n = fn_e
	    im_n = fn
	    im_enter_ic = null
	    im_ic = null
	    showEnter()
	}

	fun showNormal() {
	    if (im_ic == null) im_ic = createImageIcon(im_n, w, h)
	    icon = im_ic
	    verticalTextPosition = BOTTOM
	    horizontalTextPosition = CENTER
	    horizontalAlignment = CENTER
	    verticalAlignment = CENTER
	    repaint()
	}

	fun showEnter() {
	    if (im_enter_ic == null) im_enter_ic = createImageIcon(im_enter_n, w, h)
	    icon = im_enter_ic
	    repaint()
	}

	fun createImageIcon(fn: String?, max_w: Int, max_h: Int): ImageIcon? {
	    return createImageIcon(
		this@BaseCanvas,
		fn!!,
		max_w,
		max_h
	    )
	}

	override fun setText(s: String) {
	    super.setText(s)
	}

	public override fun paintComponent(g: Graphics) {
	    if (im_ic == null) showNormal()
	    super.paintComponent(g)
	}
    }

    inner class ImageAreaJB internal constructor(
	txt: String?,
	var ord: Int,
	var w: Int,
	var h: Int,
	dispName: String?
    ) : JButton(
	dispName ?: txt
    ), ActionListener {
	var im_n: String? = null
	var im_enter_n: String? = null
	var im_ic: ImageIcon? = null
	var im_enter_ic: ImageIcon? = null
	var o: Any? = null
	fun setNew(txt: String?, im_n: String?, im_enter_n: String?, o: Any?, dispName: String?) {
	    text = dispName ?: formatDisplayText(txt!!)
	    this.im_n = im_n
	    this.im_enter_n = im_enter_n
	    this.o = o
	    im_enter_ic = null
	    im_ic = im_enter_ic
	    foreground = Color.black
	    isVisible = true
	    repaint()
	}

	override fun getInsets(): Insets {
	    return Insets(5, 5, 5, 5)
	}

	override fun actionPerformed(ae: ActionEvent) {
	    val ima = ae.source as ImageAreaJB
	    om_msg_mgr.fire("imarea " + panelName + ":lesson_" + ima.ord)
	}

	inner class Mouse : MouseInputAdapter() {
	    var mpress_p: Point2D? = null

	    init {
		addMouseListener(this)
		addMouseMotionListener(this)
	    }

	    override fun mousePressed(e: MouseEvent) {
		mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
		setRedOtherOffIMJB()
	    }

	    override fun mouseMoved(e: MouseEvent) {}
	    override fun mouseDragged(e: MouseEvent) {}
	    override fun mouseReleased(e: MouseEvent) {
		//om_msg_mgr.fire("imarea " + getPanelName() + ":lesson_" + ord);
	    }

	    override fun mouseEntered(e: MouseEvent) {
		showEnter()
	    }

	    override fun mouseExited(e: MouseEvent) {
		showNormal()
	    }
	}

	var m: Mouse = Mouse()

	init {
	    foreground = Color.black
	    addActionListener(this)
	    //	    addFocusListener(background_FA);
	    border = BorderFactory.createLineBorder(Color(80, 80, 80), 5)
	    focusTraversalKeysEnabled = false
	    isRequestFocusEnabled = false
	    isFocusable = false
	}

	fun setNewImage(fn: String?, fn_e: String?) {
	    im_enter_n = fn_e
	    im_n = fn
	    im_enter_ic = null
	    im_ic = null
	    showEnter()
	}

	fun showNormal() {
	    if (im_n != null) {
		im_ic = createImageIcon(im_n, w, h - 13)
		icon = im_ic
	    }
	    verticalTextPosition = BOTTOM
	    horizontalTextPosition = CENTER
	    horizontalAlignment = CENTER
	    verticalAlignment = CENTER
	    repaint()
	}

	fun showEnter() {
	    if (im_enter_n != null) {
		if (im_enter_ic == null) im_enter_ic = createImageIcon(im_enter_n, w, h - 13)
		icon = im_enter_ic
	    }
	    repaint()
	}

	fun createImageIcon(fn: String?, max_w: Int, max_h: Int): ImageIcon? {
	    return createImageIcon(
		this@BaseCanvas,
		fn!!,
		max_w,
		max_h
	    )
	}

	override fun setText(s: String) {
	    super.setText(formatDisplayText(s))
	}

	public override fun paintComponent(g: Graphics) {
	    val g2 = g as Graphics2D
	    val rh = g2.renderingHints
	    rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	    g2.setRenderingHints(rh)
	    if (im_ic == null) {
		showNormal()
	    }
	    super.paintComponent(g)
	}

	fun setNoRedIMJB() {
	    border = BorderFactory.createLineBorder(Color(80, 80, 80), 5)
	}

	fun setRedIMJB() {
	    border = BorderFactory.createLineBorder(Color(242, 80, 80), 5)
	}

	fun setRedOtherOffIMJB() {
	    if (this@BaseCanvas is LessonMainCanvas) this@BaseCanvas.setRed(ord)
	}
    }

    fun initColors() {
	colors["bg_t"] = ColorColors.create(240, 220, 140)
	colors["bg_m"] = ColorColors.create(210, 180, 220)
	colors["bg_b"] = ColorColors.create(140, 220, 240)
	colors["bg_tx"] = ColorColors.create(0, 0, 0)
	colors["bg_fr"] = ColorColors.create(0, 0, 0)
	colors["bg_frbg"] = ColorColors.create(240, 220, 140)
	colors["sn_bg"] = ColorColors.create(240, 220, 140)
	colors["sn_hi"] = ColorColors(moreSaturate(Color(240, 220, 140)), null)
	colors["sn_fr"] = ColorColors.create(0, 0, 0)
	colors["sn_tx"] = ColorColors.create(0, 0, 0)
	colors["bt_bg"] = ColorColors.create(0, 0, 0)
	colors["bt_hi"] = ColorColors(moreSaturate(Color(240, 220, 140)), null)
	colors["bt_hs"] = ColorColors.create(255, 240, 180)
	colors["bt_fr"] = ColorColors.create(0, 0, 0)
	colors["bt_tx"] = ColorColors.create(0, 0, 0)
	colors["bt_fr_hi"] = ColorColors.create(0, 0, 0)
	colors["bt_tx_hi"] = ColorColors.create(0, 0, 0)
	colors["bt_fr_hs"] = ColorColors.create(0, 0, 0)
	colors["bt_tx_hs"] = ColorColors.create(0, 0, 0)
    }

    open val panelName: String
	get() = "base"

    protected open fun resized() {}
    fun gX(f: Double): Int {
	return (f * caW).toInt()
    }

    fun gY(f: Double): Int {
	return (f * caH).toInt()
    }

    fun set(b: JButton?) {
	b!!.font = fo
	b.verticalTextPosition = SwingConstants.BOTTOM
	b.horizontalTextPosition = SwingConstants.CENTER
    }

    fun set(b: JLabel) {
	b.font = fo
	b.verticalTextPosition = SwingConstants.TOP
	b.horizontalTextPosition = SwingConstants.CENTER
    }

    fun setAlt(b: JLabel) {
	b.font = fo
	b.verticalTextPosition = SwingConstants.BOTTOM
	b.horizontalTextPosition = SwingConstants.CENTER
	b.horizontalAlignment = SwingConstants.CENTER
    }

    fun setAlt(b: JButton) {
	b.font = fo
	b.verticalTextPosition = SwingConstants.BOTTOM
	b.horizontalTextPosition = SwingConstants.CENTER
	b.horizontalAlignment = SwingConstants.CENTER
    }

    inner class MouseB : MouseInputAdapter() {
	override fun mousePressed(e: MouseEvent) {
	    if (!buttons_enabled) return
	    val jb = e.source as JButton
	    val ord = jb.getClientProperty("ord") as Int
	    for (i in 0..4) if (i == ord) {
		setRedBut(i)
	    } else {
		setNoRedBut(i)
	    }
	}

	override fun mouseMoved(e: MouseEvent) {}
	override fun mouseDragged(e: MouseEvent) {}
	override fun mouseReleased(e: MouseEvent) {}
	override fun mouseEntered(e: MouseEvent) {}
	override fun mouseExited(e: MouseEvent) {}
    }

    var mouseb = MouseB()
    fun adda(b: JButton?, cmd: String?) {
	b!!.addActionListener(act_li)
	b.actionCommand = panelName + ':' + cmd
	b.addMouseListener(mouseb)
	b.addMouseMotionListener(mouseb)
	//	b.addFocusListener(background_FA_Alt);
	add(b)
    }

    fun setJBIcon(b: JButton?, fn: String) {
	try {
	    var w = (b!!.width * 0.75).toInt()
	    var h = (b.height * 0.75).toInt()
	    if (w == 0) w = 20
	    if (h == 0) h = 20
	    val imic = createImageIcon(
		b,
		fn,
		w,
		h
	    )
	    b.icon = imic
	    setJBROIcon(b, fn.replace("\\.png".toRegex(), "-over.png"))
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "ImageIco size 0,0")
	}
    }

    private fun setJBROIcon(b: JButton?, fn: String) {
	val w = (b!!.width * 0.75).toInt()
	val h = (b.height * 0.75).toInt()
	val imic = createImageIcon(
	    b,
	    fn,
	    w,
	    h
	)
	if (imic != null) {
	    b.rolloverIcon = imic
	    b.isRolloverEnabled = true
	}
    }

    fun getImage(fn: String?): Image {
	return Toolkit.getDefaultToolkit().getImage(omegaAssets(fn))
    }

    inner class CycleList(want_first: Int) {
	var want_first = 0
	var ix: Int
	var li: ArrayList<MyButton?>

	init {
	    this.want_first = want_first
	    li = ArrayList()
	    ix = -1
	}

	fun reset() {
	    ix = -1
	    li = ArrayList()
	}

	fun add(b: MyButton?, grp: Int) {
	    li.add(b)
	    // 	    if ( ix == -1 )
// 		ix = 0;
	}

	operator fun next() {
	    if (ix == -1) ix = want_first else ix++
	    if (ix >= li.size) ix = 0
	    val mb = get()
	    val B = mb!!.getClientProperty("skipred") as Boolean
	    if (!mb.isVisible || B != null && B) next()
	}

	fun prev() {
// 	    if ( ix == -1 )
// 		return;
	    ix--
	    if (ix < 0) ix = li.size - 1
	    val mb = get()
	    val B = mb!!.getClientProperty("skipred") as Boolean
	    if (!mb.isVisible || B != null && B) prev()
	}

	fun set(ix: Int) {
	    this.ix = ix
	}

	fun get(): MyButton? {
	    return if (ix == -1) null else li[ix]
	}

	val isPopup: Boolean
	    get() = ix == 4 || ix == 5

	fun dump() {}
    }
    var focus_list: CycleList? = null

    inner class MyButton(txt: String?) : JButton(txt) {
	var popup: Array<MyButton?>? = null
	fun remove() {
	    if (popup == null) return
	    for (i in popup!!.indices) {
		remove(popup!![i])
	    }
	    popup = null
	}

	fun setBound(x: Int, y: Int, w: Int, h: Int) {
	    if (popup == null) return
	    for (i in popup!!.indices) {
		popup!![i]!!.setLocation(x, y - i * h - h)
		popup!![i]!!.setSize(w, h)
	    }
	}

	private fun setPopupVisible(b: Boolean) {
	    if (popup != null) for (i in popup!!.indices) {
		if (b == false) popup!![i]!!.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
		popup!![i]!!.isVisible = b
	    }
	}

	fun toggleVisible() {
	    if (popup != null && popup!![0]!!.isVisible) setPopupVisible(false) else setPopupVisible(true)
	}

	fun popupVisible(b: Boolean) {
	    if (popup != null) setPopupVisible(b)
	}

	fun add_fucus_list(cl: CycleList?, grp: Int) {
	    if (popup != null) for (i in popup!!.indices) {
		cl!!.add(popup!![i], grp)
	    }
	}

	override fun toString(): String {
	    return text
	}

	public override fun paintComponent(g: Graphics) {
	    val ic = icon
	    if (ic == null) {
		val id: String = panelName
		var s = "toolbarButtonGraphics/omega/$actionCommand.png"
		s = s.replace("\\:".toRegex(), "_")
		setJBIcon(this, s)
	    }
	    super.paintComponent(g)
	}
    }

    var buttons = arrayOfNulls<MyButton>(5)
    var buttons_enabled = true
    fun togglePopup(ix: Int) {
	buttons[ix]!!.toggleVisible()
    }

    fun hidePopup(ix: Int) {
	buttons[ix]!!.popupVisible(false)
	if (focus_list!!.isPopup) setRedBut(ix)
    }

    fun setBusy(b: Boolean) {
	val mb = focus_list!!.get()
	if (mb != null) {
	    if (b) mb.border = BorderFactory.createLineBorder(Color(0, 0, 120), 5) else mb.border =
		BorderFactory.createLineBorder(
		    Color(242, 80, 80), 5
		)
	}
    }

    fun buttonsEnable(b: Boolean) {
	buttons_enabled = b
	if (b == false) {
	    setAllNoRed()
	} else {
	    setRedBut(1)
	}
    }

    fun eraseAllOldButtons() {
	var comp = components
	for (i in comp!!.indices) if (comp[i] is MyButton) remove(comp[i] as MyButton)
	comp = null
	buttons = arrayOfNulls(5)
    }

    fun populateButtons(text: Array<String?>, cmd: Array<String>) {
	eraseAllOldButtons()
	focus_list!!.reset()
	val id = panelName
	val h1 = 0.25
	val h2 = 0.02
	val h3 = 0.19
	val hh = (1.0 - h1 - 2 * h2 - h3) / 2
	val v0 = gY(h2)
	val v1 = gY(h1)
	val v2 = gY(h1 + hh + h2)
	val v3 = gY(h1 + hh + h2 + hh + h2)
	val l1 = 0.005
	val l2 = 0.2
	val l25 = 0.30
	val l3 = 0.4
	val l4 = 0.6
	val l5 = 0.8
	val lA = doubleArrayOf(l1, l2, l3, l4, l5)
	val bw = gX(hh * 0.75)
	val bw1 = gX(hh * 0.73)
	val bh = gY(hh * 0.9)
	val bh2 = gY(hh * 0.55)
	val fs = caH / 48
	fo = Font("arial", Font.PLAIN, fs)
	var b: MyButton?
	for (i in 0..4) {
	    if (buttons[i] == null) {
		val textA = split(text[i], "§")
		val cmdA = split(cmd[i], "§")
		if (cmdA.size > 1) {
		    buttons[i] = MyButton(t(textA[0]!!))
		    b = buttons[i]
		    b!!.isRequestFocusEnabled = false
		    b.focusTraversalKeysEnabled = false
		    b.isFocusable = false
		    b.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
		    b.putClientProperty("ord", i)
		    adda(b, cmdA[0])
		    if (cmdA[0] == "") b.isVisible = false
		    val bA = arrayOfNulls<MyButton>(cmdA.size - 1)
		    for (ii in 1 until cmdA.size) {
			bA[ii - 1] = MyButton(t(textA[ii]!!))
			b = bA[ii - 1]
			b!!.isRequestFocusEnabled = false
			b.focusTraversalKeysEnabled = false
			b.isFocusable = false
			b.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
			b.putClientProperty("ord", i)
			adda(b, cmdA[ii])
			if (ii == 2) b.putClientProperty("skipred", java.lang.Boolean.valueOf(true))
			//			setJBIcon(b, "toolbarButtonGraphics/omega/" + id + "_" + cmdA[ii] + ".png");
			set(b)
			b.isVisible = false
		    }
		    buttons[i]!!.popup = bA
		} else {
		    buttons[i] = MyButton(t(text[i]!!))
		    b = buttons[i]
		    b!!.isRequestFocusEnabled = false
		    b.focusTraversalKeysEnabled = false
		    b.isFocusable = false
		    b.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
		    b.putClientProperty("ord", i)
		    adda(b, cmd[i])
		    if (cmd[i] == "") b.isVisible = false
		}
	    }
	}
	for (i in 0..4) {
	    b = buttons[i]
	    b!!.setSize(if (i == 0) bw1 else bw, bh2)
	    b.setLocation(gX(lA[i]), v3)
	    b.setBound(gX(lA[i]), v3, if (i == 0) bw1 else bw, bh2)
	    setJBIcon(b, "toolbarButtonGraphics/omega/" + id + "_" + cmd[i] + ".png")
	    set(b)
	    focus_list!!.add(b, i)
	    b.add_fucus_list(focus_list, i)
	}
	focus_list!!.dump()
    }

    open fun populate() {}
    fun addLessonCanvasListener(l: LessonCanvasListener) {
	lc_listeners.add(LessonCanvasListener::class.java, l)
    }

    fun removeLessonCanvasListener(l: LessonCanvasListener) {
	lc_listeners.remove(LessonCanvasListener::class.java, l)
    }

    fun setColor(id: String, col: Color?) {
	colors[id] = ColorColors(col, null)
	repaint(100)
    }

    fun getColor(id: String): Color? {
	val cols = colors[id] ?: return Color.gray
	var col = cols.color
	if (col == null) col = Color.black
	return col
    }

    fun getColor(id: String, mod: String): Color? {
	if (empty(mod)) return getColor(id)
	val idMod = id.replace("hi", "bg").replace("hs", "bg")
	val ccs = colors[":$idMod"]
	if (ccs != null) {
	    val cols = ccs.colors
	    var col = cols!![mod]
	    if (col != null) {
		when (id) {
		    "bt_hi" -> col = col.brighter()
		    "bt_hs" -> col = moreGray(moreGray(col.brighter()))
		}
		return col
	    }
	}
	var col = colors[id]!!.color
	if (col == null) col = Color.black
	return col
    }

    fun navigate_gotoRel(dx: Int, dy: Int): Boolean {
	return false
    }

    fun navigate_goto(x: Int, y: Int): Boolean {
	return false
    }

    fun moreGray(col: Color): Color {
	var r = col.red
	var g = col.green
	var b = col.blue
	var gray = (r + g + b) / 3
	gray += (255 - gray) / 2
	r = (((r - gray) * 0.3 + gray) * 1.0).toInt()
	g = (((g - gray) * 0.3 + gray) * 1.0).toInt()
	b = (((b - gray) * 0.3 + gray) * 1.0).toInt()
	return Color(r, g, b)
    }

    fun lessSaturate(col: Color): Color {
	var r = col.red
	var g = col.green
	var b = col.blue
	val gray = (r + g + b) / 3
	r = ((r - gray) * 0.7 + gray).toInt()
	g = ((g - gray) * 0.7 + gray).toInt()
	b = ((b - gray) * 0.7 + gray).toInt()
	return Color(r, g, b)
    }

    private fun limit(a: Int): Int {
	var a = a
	if (a > 255) a = 255
	if (a < 0) a = 0
	return a
    }

    fun moreSaturate(col: Color): Color {
	var r = col.red
	var g = col.green
	var b = col.blue
	val gray = (r + g + b) / 3
	r = ((r - gray) * 1.3 + gray).toInt()
	g = ((g - gray) * 1.3 + gray).toInt()
	b = ((b - gray) * 1.3 + gray).toInt()
	return Color(limit(r), limit(g), limit(b))
    }

    fun invert(col: Color): Color {
	var r = col.red
	var g = col.green
	var b = col.blue
	val gray = (r + g + b) / 3
	r = ((-r + gray) * 1.0 + gray).toInt()
	g = ((-g + gray) * 1.0 + gray).toInt()
	b = ((-b + gray) * 1.0 + gray).toInt()
	return Color(limit(r), limit(g), limit(b))
    }

    fun left(col: Color): Color {
	val r = col.red
	val g = col.green
	val b = col.blue
	return Color(limit(g), limit(b), limit(r))
    }

    fun right(col: Color): Color {
	val r = col.red
	val g = col.green
	val b = col.blue
	return Color(limit(b), limit(r), limit(g))
    }

    fun diag(col: Color): Color {
	val r = col.red
	val g = col.green
	val b = col.blue
	val rr = b + g
	val gg = r + b
	val bb = g + r
	return Color(limit(rr / 2), limit(gg / 2), limit(bb / 2))
    }

    fun markTarget(col: Color): Color {
	return moreSaturate(col)
    }

    fun markItem(col: Color): Color {
	return moreSaturate(col)
    }

    fun markSelectedItem(col: Color): Color {
	return moreSaturate(moreSaturate(col))
    }

    fun getStringWidth(fo: Font, s: String?): Int {
	val g2 = graphics as Graphics2D
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	val frc = g2.fontRenderContext
	val r = fo.getStringBounds(s, frc)
	return r.width.toInt()
    }

    fun getStringHeight(fo: Font, s: String?): Int {
	val g2 = graphics as Graphics2D
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	val frc = g2.fontRenderContext
	val r = fo.getStringBounds(s, frc)
	return r.height.toInt()
    }

    val caW: Int
	get() {
	    val r = width
	    return if (r < 0) 0 else r
	}
    val caH: Int
	get() {
	    val r = height
	    return if (r < 0) 0 else r
	}

    open fun ownKeyCode(kc: Int, is_shift: Boolean): Boolean {
	if (Lesson.skip_F) return true
	if (ignore_press) return true
	if (isKeyNext(kc)) if (buttons_enabled) if (is_shift) setPrevRed() else setNextRed()
	if (isKeySelect(kc)) {
	    val b = focus_list!!.get()
	    if (b != null) {
		if (buttons_enabled) b.doClick()
	    }
	}
	return true
    }

    var _bg_: Color? = null

    init {
	initColors()
	this.l_ctxt = l_ctxt
	layout = null
	addComponentListener(cmp_li)
	lc_listeners = EventListenerList()
	enableEvents(Event.KEY_PRESS.toLong())
    }

    open fun enter() {}
    open fun leave() {}
    open fun populateGUI() {}
    fun drawBG(g2: Graphics2D) {
	val hh = (0.23 * caH).toInt()
	var pa = GradientPaint(
	    0.0f, 0.0f, getColor("bg_t"),
	    0.0f, hh.toFloat(), getColor("bg_m")
	)
	g2.paint = pa
	g2.fill(Rectangle(0, 0, caW, hh))
	pa = GradientPaint(
	    0.0f, hh.toFloat(), getColor("bg_m"),
	    0.0f, caH.toFloat(), getColor("bg_b")
	)
	g2.paint = pa
	g2.fill(Rectangle(0, hh - 1, caW, caH - hh + 1))
    }

    fun drawMist(g2: Graphics2D, marker: Int, blueSky: Shape?, bgColor: Color, alpha: Int, mRect: Rectangle) {
	g2.color = Color(bgColor.red, bgColor.green, bgColor.blue, 255 * alpha / 100)
	val all = Rectangle(0, 0, caW, caH)
	val p: Path2D = Path2D.Double(Path2D.WIND_EVEN_ODD)
	p.append(all, false)
	p.append(blueSky, false)
	g2.clip(p)
	g2.fill(Rectangle(0, 0, caW, caH))
	if (marker == 2) {
//	    g2.setColor(new Color(200, 0, 0, 255));
//	    g2.fill(new Arc2D.Double((int)(blueSky.getBounds().getX() + blueSky.getBounds().getWidth()),
//		    (int)blueSky.getBounds().getY(),
//		    10, 10, 0, 360, 0));
	    g2.color = Color(200, 0, 165, 255)
	    g2.fill(
		Arc2D.Double(
		    ((mRect.getX() + mRect.getWidth()).toInt() + 5).toDouble(),
		    (
			    mRect.getY().toInt() + 20).toDouble(),
		    10.0, 10.0, 0.0, 360.0, 0
		)
	    )
	}
    }

    public override fun paintComponent(g: Graphics) {
	populate()
	val g2 = g as Graphics2D
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	drawBG(g2)
    }

    fun fillElement(el: Element) {
	val it: Iterator<String> = colors.keys.iterator()
	while (it.hasNext()) {
	    val k = it.next()
	    if (colors[k]!!.color != null) {
		el.addAttr("color_$k", "#" + Integer.toHexString(0xffffff and colors[k]!!.color!!.rgb))
	    } else {
	    }
	}
    }

    fun fillSettingsElement(el: Element) {
	val it: Iterator<String> = colors.keys.iterator()
	while (it.hasNext()) {
	    val k = it.next()
	    if (colors[k]!!.color != null) {
		el.addAttr("color_$k", "#" + Integer.toHexString(0xffffff and colors[k]!!.color!!.rgb))
	    } else {
	    }
	}
    }

    open fun setSettingsFromElement(el: Element?) {
	try {
	    if (el != null) {
		initColors()
		val newColors = HashMap<String, ColorColors>()
		for (o in colors.keys) {
		    //OmegaContext.serr_log.getLogger().info("o is " + o);
		    val k = o
		    /*
                    if (!(colors.get(k) instanceof Color))
                        continue;
*/try {
			if (colors[k]!!.colors != null) continue
			val col = colors[k]!!.color
			var c = el.findAttr("color_$k")
			if (c != null) {
//			OmegaContext.sout_log.getLogger().info(":--: " + "col " + k + ' ' + col + ' ' + c);
			    if (c[0] == '#') {
				var rgb: Int
				rgb = if (c.length == 9) c.substring(3).toInt(16) else c.substring(1).toInt(16)
				newColors[k] = ColorColors.create(rgb)
			    }
			}
			c = el.findAttr("colorTid_$k")
			// s#123456,v#123456,o#123456
			if (c != null) {
//			OmegaContext.sout_log.getLogger().info(":--: " + "col " + k + ' ' + col + ' ' + c);
			    val cols = HashMap<String, Color>()
			    val ca = c.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			    for (c1 in ca) {
				val ca2 = c1.split("#".toRegex()).dropLastWhile { it.isEmpty() }
				    .toTypedArray()
				if (ca2.size == 2) {
				    var rgb: Int
				    rgb = if (ca2[1].length == 9) ca2[1].substring(3).toInt(16) else ca2[1].substring(0)
					.toInt(16)
				    cols[ca2[0]] = Color(rgb)
				}
			    }
			    newColors[":$k"] = ColorColors(null, cols)
			}
		    } catch (ex: ClassCastException) {
			ex.printStackTrace()
		    }
		}
		colors.putAll(newColors)
		repaint()
	    } else {
	    }
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    Lesson.global_skipF(true)
	    JOptionPane.showMessageDialog(
		ApplContext.top_frame,
		"Can't create from file\n$ex"
	    )
	    Lesson.global_skipF(false)
	    ex.printStackTrace()
	}
    }

    open fun setPrevRed() {
	if (!buttons_enabled) return
	val ob = focus_list!!.get()
	focus_list!!.prev()
	val nb = focus_list!!.get()
	setCurrentRed()
	return
    }

    open fun setNextRed() {
	if (!buttons_enabled) return
	val ob = focus_list!!.get()
	focus_list!!.next()
	val nb = focus_list!!.get()
	setCurrentRed()
	return
    }

    fun setCurrentRed() {
	val mbcur = focus_list!!.get()
	val it: Iterator<*> = focus_list!!.li.iterator()
	while (it.hasNext()) {
	    val mb = it.next() as MyButton
	    if (mb !== mbcur) mb.border = BorderFactory.createBevelBorder(BevelBorder.RAISED) else mb.border =
		BorderFactory.createLineBorder(
		    Color(242, 80, 80), 5
		)
	}
    }

    open fun setRed(ix: Int) {
	setRedBut(ix)
    }

    fun setNoRed(ix: Int) {
	setNoRedBut(ix)
    }

    fun setAllNoRed() {
	for (i in 0..4) setNoRedBut(i)
    }

    fun setRedBut(ix: Int) {
	if (!buttons_enabled) return
	try {
	    val ob = focus_list!!.get()
	    focus_list!!.set(ix)
	    val nb = focus_list!!.get()
	    if (ob != null) {
		ob.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
		ob.repaint()
	    }
	    if (nb != null) {
		nb.border = BorderFactory.createLineBorder(Color(242, 80, 80), 5)
		nb.repaint()
	    }
	} catch (ex: Exception) {
	}
    }

    fun setNoRedBut(ix: Int) {
	val ob = focus_list!!.get()
	if (ob != null) {
	    ob.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
	    ob.repaint()
	}
	focus_list!!.set(ix)
    }

    fun ignorePress(b: Boolean) {
	ignore_press = b
    }

    open fun updateDisp() {}
}
