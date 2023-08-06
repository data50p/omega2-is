package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaConfig.isKeyESC
import com.femtioprocent.omega.OmegaConfig.isKeyNext
import com.femtioprocent.omega.OmegaConfig.isKeySelect
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.Lesson.PlayData
import com.femtioprocent.omega.lesson.Lesson.PlayDataList
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.swing.ScaledImageIcon.createImageIcon
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import java.awt.*
import java.awt.event.*
import java.awt.geom.Area
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import javax.swing.*
import javax.swing.event.ListSelectionListener
import javax.swing.event.MouseInputAdapter

class SentenceCanvas(l_ctxt: LessonContext?) : BaseCanvas(l_ctxt!!) {
    private var item_fo: Font? = null
    private var item2_fo: Font? = null
    var read_mode = false
    var story_list_sp: JScrollPane? = null
    var story_list: JList<String?>? = null
    var story_list_x = gX(0.5 - 0.25)
    var story_list_y = gY(0.07)
    var story_list_w = gX(0.5)
    var story_list_h = gY(0.7)
    override var cmp_li: ComponentAdapter = object : ComponentAdapter() {
	override fun componentResized(ev: ComponentEvent) {
	    item_fo = null
	    item2_fo = null
	    populate()
	}
    }

    fun actionQuit() {
	hideMsg()
	om_msg_mgr.fire("exit create")
    }

    override val panelName: String
	get() = "sent"

    override fun ownKeyCode(kc: Int, is_shift: Boolean): Boolean {
	if (Lesson.skip_F) return true
	if (ignore_press) {
	    if (isKeySelect(kc)) {
	    }
	    return true
	}
	OmegaContext.sout_log.getLogger().info(":--: pupil own $kc")
	if (isKeyNext(kc)) if (is_shift) setMyPrevRed() else setMyNextRed()
	if (isKeySelect(kc)) if (story_list_sp!!.isVisible) {
	    enableStoryList(false)
	    focus_list!!.set(1)
	} else {
	    val mb = focus_list!!.get()
	    mb?.doClick()
	}
	if (isKeyESC(kc)) {
	    hideMsg()
	    om_msg_mgr.fire("exit create")
	    return false
	}
	return true
    }

    fun waitDone(): String {
	while (story_list_sp!!.isVisible) m_sleep(100)
	setRed(1)
	return story_list!!.selectedValue as String
    }

    val font1: Font
	get() {
	    if (item2_fo == null) item2_fo = Font("Arial", Font.PLAIN, (caH / 48))
	    return item2_fo!!
	}

    inner class Mouse(var l_canvas: SentenceCanvas) : MouseInputAdapter() {
	var mpress_p: Point2D? = null

	init {
	    addMouseListener(this)
	    addMouseMotionListener(this)
	}

	override fun mousePressed(e: MouseEvent) {
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    Lesson.cnt_hit_keyOrButton++
	}

	override fun mouseMoved(e: MouseEvent) {}
	override fun mouseDragged(e: MouseEvent) {}
	override fun mouseReleased(e: MouseEvent) {
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())

// 	    if ( hitQuitButton((int)e.getX(), (int)e.getY()) ) {
// 		om_msg_mgr.fire("exit create");
// 	    }
	}
    }

    var m: Mouse = Mouse(this)
    fun init() {}
    var list_data = arrayOfNulls<String>(0)
    fun setMyNextRed() {
	if (story_list_sp!!.isVisible) {
	    var ix = story_list!!.selectedIndex
	    // 	    ListSelectionModel lsm = story_list.getSelectionModel();
	    val lix = list_data.size
	    ix++
	    if (ix >= lix) ix = 0
	    story_list!!.selectedIndex = ix
	    setAllNoRed()
	    story_list!!.ensureIndexIsVisible(story_list!!.selectedIndex)
	} else setNextRed()
    }

    fun setMyPrevRed() {
	if (story_list_sp!!.isVisible) {
	    var ix = story_list!!.selectedIndex
	    // 	    ListSelectionModel lsm = story_list.getSelectionModel();
	    val lix = list_data.size
	    ix--
	    if (ix < 0) ix = lix - 1
	    story_list!!.selectedIndex = ix
	    setAllNoRed()
	    story_list!!.ensureIndexIsVisible(story_list!!.selectedIndex)
	} else setPrevRed()
    }

    fun setListData(files: Array<String?>) {
	list_data = files
	val ix = story_list!!.selectedIndex
	story_list!!.setListData(files)
	if (ix == -1) story_list!!.selectedIndex = 0
	setAllNoRed()
    }

    var selected_index = -1
    var lsl = ListSelectionListener { e ->
	val l = e.source as JList<*>
	if (l === story_list) {
	    val filename = l.selectedValue as String
	    if (e.valueIsAdjusting) selected_index = story_list!!.selectedIndex
	    OmegaContext.sout_log.getLogger().info(":--: Selected $filename $e")
	}
    }

    override fun populate() {
	if (story_list == null) {
	    story_list = JList<String?>()
	    story_list_sp = JScrollPane(story_list)
	    add(story_list_sp)
	    if (cell_renderer == null) cell_renderer = CellRenderer()
	    //	    story_list.setVisibleRowCount(7);
	    story_list!!.setCellRenderer(cell_renderer)
	    story_list_sp!!.setSize(story_list_w, story_list_h)
	    story_list_sp!!.setLocation(story_list_x, story_list_y)
	    story_list!!.setBackground(getColor("bg_frbg"))
	    story_list!!.addListSelectionListener(lsl)
	    story_list!!.setFocusTraversalKeysEnabled(false)
	    story_list!!.isRequestFocusEnabled = false
	    story_list!!.setFocusable(false)
	    story_list_sp!!.isVisible = false
	    val mouseListener: MouseListener = object : MouseAdapter() {
		override fun mouseClicked(e: MouseEvent) {
		    val index = story_list!!.locationToIndex(e.point)
		    val ix = story_list!!.selectedIndex
		    if (0 == selected_index) story_list_sp!!.isVisible = false
		    selected_index = 0
		    OmegaContext.sout_log.getLogger().info(":--: Clicked on Item $index $ix")
		}
	    }
	    story_list!!.addMouseListener(mouseListener)
	}
	mkButtons()
    }

    fun mkButtons() {
	if (read_mode) populateButtons(
		arrayOf(
			"Quit",
			"Read and Listen",
			"Replay",
			"Printer...§Print§Select Printer",
			"Select"
		), arrayOf("quit", "read", "replay", "print§print_print§print_select", "select")
	) else populateButtons(
		arrayOf("Quit", "Read and Listen", "Replay", "Printer...§Print§Select Printer", "Save"),
		arrayOf("quit", "read", "replay", "print§print_print§print_select", "save")
	)
    }

    fun enableStoryList(on: Boolean) {
	story_list_x = gX(0.5 - 0.25)
	story_list_y = gY(0.07)
	story_list_w = gX(0.5)
	story_list_h = gY(0.7)
	story_list_sp!!.setSize(story_list_w, story_list_h)
	story_list_sp!!.setLocation(story_list_x, story_list_y)
	story_list!!.background = getColor("bg_frbg")
	story_list_sp!!.isVisible = on
	repaint()
    }

    var itemFont: Font?
	get() {
	    if (item_fo == null) item_fo = Font("Arial", Font.PLAIN, gY(0.05))
	    return item_fo
	}
	set(fo) {
	    item_fo = fo
	}

    fun getSize(asp: Double, h: Int): Int {
	if (asp == 0.0) return (h * 0.65).toInt()
	var hh = (h * 0.55).toInt()
	if (asp < 1.0) hh = (hh * asp).toInt()
	return hh
    }

    // --
    var title_fo: Font? = null
    fun setTitleFont() {
	title_fo = Font("Arial", Font.PLAIN, gX(0.024))
    }

    val titleFont: Font?
	get() {
	    if (title_fo == null) setTitleFont()
	    return title_fo
	}

    // -- //
    override fun populateGUI() {}
    var show_msg = false

    inner class MsgDialog {
	var li: ArrayList<String?>? = null
	var cnt_show = 0
	var imic: ImageIcon? = null
	var imic_done: ImageIcon? = null
	fun set(li: java.util.ArrayList<String?>?) {
	    this.li = li
	    cnt_show = 0
	    repaint()
	}

	val bounding: IntArray
	    get() {
		var WW = 0
		var HH = 0
		if (li == null) return intArrayOf(500, 350)
		li!!.forEach {sent ->
		    val sh = getStringHeight(itemFont!!, sent)
		    val sw = getStringWidth(itemFont!!, sent)
		    HH += sh + 5
		    WW = if (sw > WW) sw else WW
		}
		return intArrayOf(WW, HH)
	    }

	fun draw(g2: Graphics2D, bounding: IntArray) {
	    var bounding = bounding
	    if (li == null || li!!.size == 0) return
	    if (gX(0.02) > 0 && gY(0.02) > 0) {
		imic = createImageIcon(
			this@SentenceCanvas,
			"media/default/story-listen-continue.png",
			gX(0.035),
			gY(0.035),
			false
		)
		imic_done = createImageIcon(
			this@SentenceCanvas,
			"media/default/story-listen-done.png",
			gX(0.035),
			gY(0.035),
			false
		)
	    }
	    val txw = gX(0.85)
	    val txh = gY(0.70)
	    val w = gX(0.9)
	    val h = gY(0.75)
	    val th = gY(0.042)
	    val x = gX(0.05)
	    val y = gY(0.05)
	    val r = gX(0.02)
	    while (bounding[0] > txw - 10 || bounding[1] > txh - 15) {
		val size: Int = itemFont!!.size
		itemFont = Font("Arial", Font.PLAIN, (size * 0.9).toInt())
		bounding = this.bounding
	    }
	    val col = getColor("bg_frbg") // new Color(0xe5, 0xe5, 0xe5);
	    val fr: RoundRectangle2D = RoundRectangle2D.Double(
		    x.toDouble(),
		    y.toDouble(),
		    w.toDouble(),
		    h.toDouble(),
		    r.toDouble(),
		    r.toDouble()
	    )
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f)
	    g2.color = col
	    g2.fill(fr)
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)

	    // titlebar
	    g2.color = Color(88, 88, 88)
	    g2.clip = fr
	    g2.fill(Rectangle2D.Double(x.toDouble(), y.toDouble(), w.toDouble(), th.toDouble()))
	    val stroke = BasicStroke(caH / 200f)
	    g2.stroke = stroke
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    g2.color = getColor("bg_fr")
	    g2.setClip(0, 0, 10000, 10000)
	    g2.draw(fr)
	    g2.setClip(0, 0, 10000, 10000) //	    g2.setClip(fr);
	    g2.color = Color.black
	    g2.font = itemFont
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    g2.color = getColor("bg_tx")
	    val sh = getStringHeight(itemFont!!, "ABC")
	    var row = sh + 2
	    var cnt = 0
	    val gap = 0
	    li!!.forEach {sent ->
		if (cnt < cnt_show) g2.drawString(
			sent,
			x + w / 20,
			y + th + row
		)
		//		sh = getStringHeight(getItemFont(), sent);
		row += sh + gap
		cnt++
	    }
	    val cnt_max = li!!.size + 1
	    if (imic != null && imic_done != null) if (cnt_show >= cnt_max) g2.drawImage(
		    imic_done!!.image,
		    x + gX(0.015),
		    y + gY(0.013) + (sh + gap) * cnt_max,
		    null
	    ) else g2.drawImage(
		    imic!!.image,
		    x + gX(0.015),
		    y + gY(0.013) + (sh + gap) * cnt_show,
		    null
	    )
	    g2.color = col
	    g2.font = titleFont
	    g2.drawString("", x + 1 * w / 10, (y + gY(0.03)))
	    val a = Area()
	    a.add(Area(Rectangle2D.Double(0.0, 0.0, 10000.0, 10000.0)))
	    a.subtract(Area(fr))
	    g2.clip = a
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f)
	    g2.color = Color(15, 15, 15)
	    for (i in 0..6) {
		val frs: RoundRectangle2D = RoundRectangle2D.Double(
			(x + 10 - i).toDouble(),
			(y + 10 - i).toDouble(),
			w.toDouble(),
			h.toDouble(),
			r.toDouble(),
			r.toDouble()
		)
		g2.fill(frs)
	    }
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	}
    }

    var msg_dlg: MsgDialog? = MsgDialog()

    inner class CellRenderer : JLabel(), ListCellRenderer<Any?> {
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
	    val `val` = value as String?
	    verticalTextPosition = CENTER
	    horizontalTextPosition = RIGHT
	    horizontalAlignment = LEFT
	    verticalAlignment = CENTER
	    font = font1
	    if (value != null) text = `val`
	    background = if (isSelected) getColor("bg_tx") else getColor("bg_frbg")
	    foreground = if (isSelected) getColor("bg_frbg") else getColor("bg_tx")
	    return this
	}
    }

    var cell_renderer: CellRenderer? = null

    init {
	focus_list = CycleList(6)
	requestFocus()
	layout = null
	addComponentListener(cmp_li)
    }

    fun drawList(g2: Graphics2D) {
	if (story_list_sp!!.isVisible) {
	    OmegaContext.sout_log.getLogger().info(":--: " + "story is visible")
	    story_list_x = gX(0.5 - 0.25)
	    story_list_y = gY(0.07)
	    story_list_w = gX(0.5)
	    story_list_h = gY(0.7)
	    val th = gY(0.042)
	    val x = story_list_x - 10
	    val y = story_list_y - 10 - th
	    val w = story_list_w + 20
	    val h = story_list_h + 20 + th
	    val r = 15
	    val col = getColor("bg_frbg") // new Color(0xe5, 0xe5, 0xe5);
	    val fr: RoundRectangle2D = RoundRectangle2D.Double(
		    x.toDouble(),
		    y.toDouble(),
		    w.toDouble(),
		    h.toDouble(),
		    r.toDouble(),
		    r.toDouble()
	    )
	    val story_r = story_list_sp!!.bounds
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f)
	    g2.color = col
	    var a = Area()
	    a.add(Area(Rectangle2D.Double(0.0, 0.0, 10000.0, 10000.0)))
	    a.subtract(Area(story_r))
	    g2.clip = a
	    g2.fill(fr)
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)

	    // titlebar
// 	    g2.setColor(new Color(88, 88, 88));
// //	    g2.setClip(fr);
// 	    g2.fill(new Rectangle2D.Double(x, y, w, th));
	    val stroke = BasicStroke(caH / 200f)
	    g2.stroke = stroke
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    g2.color = getColor("bg_fr")
	    //	    g2.setClip(0, 0, 10000, 10000);
	    g2.draw(fr)
	    a = Area()
	    a.add(Area(Rectangle2D.Double(0.0, 0.0, 10000.0, 10000.0)))
	    a.subtract(Area(fr))
	    g2.clip = a
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f)
	    g2.color = Color(15, 15, 15)
	    for (i in 0..6) {
		val frs: RoundRectangle2D = RoundRectangle2D.Double(
			(x + 10 - i).toDouble(),
			(y + 10 - i).toDouble(),
			w.toDouble(),
			h.toDouble(),
			r.toDouble(),
			r.toDouble()
		)
		g2.fill(frs)
	    }
	    g2.setClip(0, 0, 10000, 10000)
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	}
    }

    fun showMsg(sentences: java.util.ArrayList<String?>?) {
	item_fo = null
	msg_dlg!!.set(sentences)
    }

    fun showMsgMore() {
	msg_dlg!!.cnt_show++
	repaint()
    }

    fun hideMsg() {
	msg_dlg!!.set(null)
    }

    override fun paintComponent(g: Graphics) {
	if (buttons[0] == null) populate()
	val ct0 = ct()
	val g2 = g as Graphics2D
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	drawBG(g2)
	g.setColor(Color.black)
	val fo = g.getFont()
	val bounding = msg_dlg!!.bounding
	msg_dlg!!.draw(g2, bounding)
	drawList(g2)
	val ct1 = ct()
    }

    override fun enter() {
	super.enter()
	setRed(6)
    }

    fun setStoryData(playDataList: PlayDataList) {
	val story_list = playDataList.arr
	val pd: PlayData? = null
    }

    val listenListener: ListenListener?
	get() = null

    fun setRead(b: Boolean) {
	if (read_mode != b) {
	    read_mode = b
	    if (buttons[0] != null) { // else swing crash
		mkButtons()
	    }
	}
    }
}
