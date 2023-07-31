package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaConfig.isKeyESC
import com.femtioprocent.omega.OmegaConfig.isKeyNext
import com.femtioprocent.omega.OmegaConfig.isKeySelect
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.adm.assets.TargetCombinations
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.LessonContext
import com.femtioprocent.omega.lesson.actions.AnimAction
import com.femtioprocent.omega.lesson.machine.Item
import com.femtioprocent.omega.lesson.machine.ItemEntry
import com.femtioprocent.omega.lesson.machine.Target
import com.femtioprocent.omega.lesson.machine.Target.T_Item
import com.femtioprocent.omega.lesson.managers.movie.LiuMovieManager
import com.femtioprocent.omega.message.Manager
import com.femtioprocent.omega.swing.GetOption
import com.femtioprocent.omega.swing.ScaledImageIcon.createImageIcon
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Num.howManyBits
import com.femtioprocent.omega.util.SundryUtils.createUniq
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.xml.Element
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseEvent
import java.awt.geom.Area
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.io.File
import javax.swing.JOptionPane
import javax.swing.event.EventListenerList
import javax.swing.event.MouseInputAdapter

/*

låt TAB enbart gå i den kolumn (-er) som nästa menings ord pekar ut.
välj sedan nästa kolumn

TAB skall fokusera

kvittera med TAB Enter
*/ /*
 --gapE---|=====|--gapB--|=========|---gapE---




 */
class LessonCanvas(l_ctxt: LessonContext?) : BaseCanvas(l_ctxt!!) {
    override var om_msg_mgr = Manager()

    @JvmField
    var lep: LessonEditorPanel? = null

    //    public Target tg;
    private var target_standout_msk = 0 // mask
    private var mark_more = false
    var mouse_mark_target = true
    var box_mark_target = true
    var show_wordbox = true
    private var active_item_action_box: Box? = null
    private var active_item_box: Box? = null
    var active_titem_ix = -1

    @JvmField
    var edit = false
    var tgAddBoxes: ShapeList? = null
    var tgDelBoxes: ShapeList? = null
    var addBoxes: ShapeList? = null
    var delBoxes: ShapeList? = null

    //    EventListenerList lc_listeners;
    private var item_fo: Font? = Font("Arial", Font.PLAIN, ( /*h*/20 * 0.7).toInt())
    private var item_fo2: Font? = Font("Arial", Font.PLAIN, ( /*h*/20 * 0.35).toInt())
    private var trgt_fo: Font? = Font("Arial", Font.PLAIN, ( /*h*/20 * 0.8).toInt())
    private var trgtA_fo: Font? = Font("Arial", Font.PLAIN, ( /*h*/20 * 0.4).toInt())
    var isMsg = false
    var lesson_name = "-noname-"
    var lesson_link_next: String? = null
    var lesson_is_first = false
    var last_nav_kbd = false

    // 0 en svart
    // 1 en röd
    // 2 två röd svart
    // 3 två svart röd
    var quit_state = 0
    var quit_disabled = false
    override var cmp_li: ComponentAdapter = object : ComponentAdapter() {
	override fun componentResized(ev: ComponentEvent) {
	    trgt_fo = null
	    trgtA_fo = null
	    title_fo = null
	    render()
	}
    }

    fun actionQuit() {
	OmegaContext.sout_log.getLogger().info(":--: " + "enter quitAction")
	val msgitem = l_ctxt.lesson.resultSummary_MsgItem
	OmegaContext.sout_log.getLogger().info(":--: " + "enter quitAction")
	msgitem?.let { showMsg(it) }
	OmegaContext.sout_log.getLogger().info(":--: " + "exited quitAction")
	hideMsg()
	fireExit(1)
	m_sleep(300)
    }

    @JvmField
    var skip_keycode = false
    override fun ownKeyCode(kc: Int, is_shift: Boolean): Boolean {
	if (edit) {
	    return true
	}
	OmegaContext.sout_log.getLogger().info(":--: ownKey $kc")
	if (isKeyNext(kc)) {
	    if (isMsg) {
		hideMsg()
		return true
	    }
	    if (skip_keycode) {
		return false
	    }
	    if (quit_state == 2) {
		setQuitState("kN", 3)
	    } else if (quit_state == 3) {
		setQuitState("kS", 2)
	    } else {
		if (is_shift) {
		    gotoPrevBox()
		} else {
		    gotoNextBox()
		}
		last_nav_kbd = true
	    }
	    return false
	}
	if (isKeyESC(kc)) {
	    if (!quit_disabled) {
		setQuitState("Esc", 0)
		hideMsg()
		fireExit(2)
		//		om_msg_mgr.fire("exit create");
		return false
	    }
	}
	if (kc == 'r'.code) {
	    if (LiuMovieManager.repeat_mode === LiuMovieManager.RepeatMode.CAN_REPEAT) {
		LiuMovieManager.repeat_mode = LiuMovieManager.RepeatMode.DO_REPEAT
		repaint()
	    }
	}
	if (isKeySelect(kc)) {
	    if (anim_action_patch != null) {
		anim_action_patch!!.rt!!.a_ctxt!!.anim_canvas!!.setBigButtonText("")
	    } else {
		if (isMsg) {
		    hideMsg()
		    return true
		}
		if (skip_keycode) {
		    return false
		}
		if (quit_state == 1) {
		    setQuitState("kS", 2)
		} else if (quit_state == 2) {
		    setQuitState("kS", 1)
		} else if (quit_state == 3) {
		    setQuitState("kS", 0)
		    hideMsg()
		    fireExit(3)
		    //		    om_msg_mgr.fire("exit create");
		} else {
		    if (isMsg) {
			hideMsg()
		    } else {
			if (skip_keycode) {
			    return false
			}
			selectBox(false, ct())
		    }
		}
	    }
	    return false
	}
	return true
    }

    //     public void addLessonCanvasListener(LessonCanvasListener l) {
    // 	lc_listeners.add(LessonCanvasListener.class, l);
    //     }
    //     public void removeLessonCanvasListener(LessonCanvasListener l) {
    // 	lc_listeners.remove(LessonCanvasListener.class, l);
    //     }
    fun fireLessonEditorHitTarget(ix: Int, type: Char) {
	l_ctxt.lesson.hitTarget(ix, type)
	// 	Object[] lia= lc_listeners.getListenerList();
// 	for(int i = 0; i < lia.length; i += 2) {
// 	    ((LessonCanvasListener)lia[i+1]).hitTarget(ix, type);
// 	}
    }

    @Synchronized
    fun fireLessonEditorHitItem(ix: Int, iy: Int, where: Int, type: Char) {
	l_ctxt.lesson.hitItem(ix, iy, where, type)

// 	Object[] lia= lc_listeners.getListenerList();
// 	for(int i = 0; i < lia.length; i += 2) {
// 	    ((LessonCanvasListener)lia[i+1]).hitItem(ix, iy, where, type);
// 	}
    }

    inner class BoxState {
	fun deselectAllBut(bx0: Box?, state: Int) {
	    if (allBox == null) {
		return
	    }
	    synchronized(allbox_sy) {
		val it: Iterator<*> = allBox!!.all.values.iterator()
		while (it.hasNext()) {
		    val bx = it.next() as Box
		    if (bx.state[state] && bx !== bx0) {
			bx.setState(state, false)
		    }
		}
	    }
	}

	fun selectCol(col: Int, state: Int) {
	    if (allBox == null) {
		return
	    }
	    synchronized(allbox_sy) {
		val it: Iterator<*> = allBox!!.all.values.iterator()
		while (it.hasNext()) {
		    val bx = it.next() as Box
		    if (bx.o_x == col) {
			bx.setState(state, true)
		    } else {
			bx.setState(state, false)
		    }
		}
	    }
	}

	fun setState(bx: Box?, state: Int, b: Boolean) {
	    if (bx == null) {
		deselectAllBut(bx, state)
	    }
	    if (state == MARKED) {
		if (bx != null) {
		    selectCol(bx.o_x, state)
		}
	    } else {
		deselectAllBut(bx, state)
		bx?.setState(state, b)
	    }
	}
    }

    inner class ShapeList internal constructor() {
	inner class ShapeItem(var id: String, var shp: Shape, var col: Color)

	var li: MutableList<ShapeItem>

	init {
	    li = ArrayList()
	}

	fun add(id: String, shp: Shape, col: Color) {
	    li.add(ShapeItem(id, shp, col))
	}

	fun draw(g2: Graphics2D) {
	    val it: Iterator<ShapeItem> = li.iterator()
	    while (it.hasNext()) {
		val sh = it.next()
		g2.color = sh.col
		g2.fill(sh.shp)
	    }
	}
    }

    inner class Box internal constructor(
	var itm_ent: ItemEntry?,
	var x: Int,
	var y: Int,
	var w: Int,
	var h: Int,
	@JvmField var o_x: Int,
	@JvmField var o_y: Int
    ) {
	var r: RoundRectangle2D
	var state = BooleanArray(BOX_MAXSTATE)
	var stroke: Stroke

	@JvmField
	var where = 0
	var marked = false

	@JvmField
	var when_hit: Long = 0
	fun noNewBox(itm_ent: ItemEntry?, x: Int, y: Int, w: Int, h: Int, o_x: Int, o_y: Int) {
	    this.itm_ent = itm_ent
	    this.x = x
	    this.y = y
	    this.w = w
	    this.h = h
	    this.o_x = o_x
	    this.o_y = o_y
	    r = RoundRectangle2D.Double(
		x.toDouble(),
		y.toDouble(),
		w.toDouble(),
		h.toDouble(),
		(caH / 48).toDouble(),
		(caW / 64).toDouble()
	    )
	    stroke = BasicStroke(caH / 300f)
	}

	val item: Item?
	    get() = itm_ent!!.getItemAt(o_y)

	fun hitBox(xx: Int, yy: Int): Int {
	    return if (xx >= x && xx <= x + w && yy >= y && yy <= y + h
	    ) {
		((xx - x).toDouble() / w * 100).toInt()
	    } else -1
	}

	var last_h = 0
	var last_w = 0

	init {
	    r = RoundRectangle2D.Double(
		x.toDouble(),
		y.toDouble(),
		w.toDouble(),
		h.toDouble(),
		(caH / 48).toDouble(),
		(caW / 64).toDouble()
	    )
	    stroke = BasicStroke(caH / 300f)
	}

	fun draw(g2: Graphics2D, ink: Boolean) {
	    var s = ""
	    val itm = item
	    var colorModifier: String? = ""
	    if (itm != null) {
		s = itm.defaultFilledText!!
		colorModifier = itm.it_ent!!.tid
	    }
	    g2.stroke = stroke
	    if (s.length > 0) {
		if (state[SELECTED]) {
		    g2.color = getColor("bt_hs", colorModifier!!)
		    if (ink) {
			g2.fill(r)
		    }
		} else if (state[MARKED]) {
		    g2.color = getColor("bt_hi", colorModifier!!)
		    if (ink) {
			g2.fill(r)
		    }
		} else {
		    g2.color = getColor("bt_bg", colorModifier!!)
		    if (ink) {
			g2.fill(r)
		    }
		}
	    }
	    var col = getColor("bt_fr")
	    val co = col
	    if (edit) {
		if (this === active_item_action_box) {
		    col = Color.red
		}
		if (this === active_item_box) {
		    col = Color.blue
		}
		if (this === active_item_box && this === active_item_action_box) {
		    col = Color.magenta
		}
		if (itm!!.defaultFilledText!!.length == 0) {
		    col = moreGray(col!!)
		}
	    }
	    if (col === co) {
		if (state[SELECTED]) {
		    g2.color = getColor("bt_fr_hs")
		} else if (state[MARKED]) {
		    g2.color = getColor("bt_fr_hi")
		} else {
		    g2.color = getColor("bt_fr")
		}
	    } else {
		g2.color = col
	    }
	    if (ink) {
		g2.draw(r)
	    }
	    if (state[SELECTED]) {
		g2.color = getColor("bt_tx_hs")
	    } else if (state[MARKED]) {
		g2.color = getColor("bt_tx_hi")
	    } else {
		g2.color = getColor("bt_tx")
	    }
	    val ss = if (s.length == 0) " " else s
	    if (item_fo == null || last_h != h || last_w != w) {
		setItemFont(
		    Font(
			"Arial",
			Font.PLAIN,
			getSize(caW.toDouble() / caH, h)
		    )
		)
		setItemFont2(
		    Font(
			"Arial",
			Font.PLAIN,
			getSize(caW.toDouble() / (2 * caH), h)
		    )
		)
	    }
	    last_h = h
	    last_w = w
	    val xQ = (caW * gapE / 100.0).toInt()
	    val WW = caW - xQ - xQ
	    val gB = (WW * gapB / 100.0).toInt()
	    val sw = getStringWidth(item_fo!!, ss) + (gB / 2)
	    if (ss.length > 1);
	    var d = wantRP
	    if (sw > w - gB / 2) {
		d = sw.toDouble() / w
		//OmegaContext.sout_log.getLogger().info(":--: " + "this wRP " + d + " sw=" + sw + "  w = " + w + "   w-gB/2=" + (w-gB/2));
		if (d > wantRP) {
		    wantRP = d
		}
	    }
	    if (wantRP >= 0.95 && wantRP <= 1.005) {
		if (ss.length > 1);
		g2.font = item_fo
		if (ink) {
		    g2.drawString(ss, (x + 0.01 * caW).toInt(), y + (h * 0.75).toInt())
		}
	    } else if (ss.length > 1);
	    if (edit) {
		val msk: Int = this@LessonCanvas.target!!.whatTargetMatchTid(itm!!.entryTid)
		val how_many = howManyBits(msk)
		g2.stroke = BasicStroke(1f)
		g2.color = Color(111, 111, 111)
		if (how_many > 1) {
		    val dw = w / how_many
		    for (i in 1 until how_many) {
			if (ink) {
			    g2.drawLine(
				x + dw * i, y,
				x + dw * i, y + h
			    )
			}
		    }
		}
	    }
	}

	fun draw(g2: Graphics2D) {
	    try {
		draw(g2, true)
	    } catch (ex: Exception) {
		OmegaContext.sout_log.getLogger().info("ERR: Box:draw(): $ex")
	    }
	}

	fun drawNull(g2: Graphics2D) {
	    try {
		draw(g2, false)
	    } catch (ex: Exception) {
		ex.printStackTrace()
		OmegaContext.sout_log.getLogger().info("ERR: Box:draw(): $ex")
	    }
	    // 	    String s = "";
// 	    Item itm = getItem();
// 	    if ( itm != null )
// 		s = itm.getDefaultFilledText();

// 	    g2.setStroke(stroke);

// 	    if ( getItemFont() == null )
// 		last_h = h + 1;

// 	    String ss = s.length() == 0 ? " " : s;
// 	    if ( last_h != h || last_w != w ) {
// 		setItemFont(new Font("Arial",
// 				     Font.PLAIN,
// 				     getSize((double)getCaW() / getCaH(), h)));
// 		setItemFont2(new Font("Arial",
// 				     Font.PLAIN,
// 				     getSize((double)getCaW() / (2*getCaH()), h)));
// 	    }
// 	    last_h = h;
// 	    last_w = w;

// 	    int sw = getStringWidth(getItemFont(), ss) + 10;

// 	    if ( ss.length() >= 25 )
// 		if ( gapB != 1 ) {
// 		    gapB = 1;
// 		    wantRP = 0.95;
// 		    reCreateBoxesKeep();
// 		}

// 	    if ( sw > w ) {
// 		double d = (double)sw / w;
// 		if ( d > wantRP ) {
// 		    wantRP = d;
// 		}
// 	    }
	}

	fun drawFrameOnly(g2: Graphics2D) {
	    val itm = item
	    g2.stroke = stroke
	    g2.color = getColor("bt_fr")
	    if (edit) {
		var col = getColor("bt_fr")
		if (this === active_item_action_box) {
		    col = Color.red
		}
		if (this === active_item_box) {
		    col = Color.blue
		}
		if (this === active_item_box && this === active_item_action_box) {
		    col = Color.magenta
		}
		if (itm!!.defaultFilledText!!.length == 0) {
		    col = moreGray(col!!)
		}
		g2.color = col
	    }
	    g2.draw(r)
	}

	fun setState(state_val: Int, b: Boolean) {
	    if (state[state_val] == b) {
		return
	    }
	    state[state_val] = b
	    if (isVisible) {
		repaintBox()
	    }
	}

	fun repaintBox() {
	    this@LessonCanvas.repaint(x - 4, y - 4, w + 8, h + 8)
	}

	override fun toString(): String {
	    return ("Box{" + o_x + ", "
		    + o_y + ", "
		    + x + ", "
		    + y + ", "
		    + w + ", "
		    + h + ", "
		    + item
		    + "}")
	}
    }

    // ------------------------ Box end -----------------
    inner class AllBox {
	var all: HashMap<String?, Box?>

	init {
	    all = HashMap()
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "AllBox CREATED " + new Date());
	}

	fun getBound(ix: Int, iy: Int): Rectangle2D? {
	    val it: Iterator<*> = all.values.iterator()
	    while (it.hasNext()) {
		val bx = it.next() as Box
		if (bx.o_x == ix
		    && bx.o_y == iy
		) {
		    return Rectangle2D.Double(
			bx.r.x,
			bx.r.y,
			bx.r.width,
			bx.r.height
		    )
		}
	    }
	    return null
	}

	fun getBox(ix: Int, iy: Int): Box? {
	    val it: Iterator<*> = all.values.iterator()
	    while (it.hasNext()) {
		val bx = it.next() as Box
		if (bx.o_x == ix
		    && bx.o_y == iy
		) {
		    return bx
		}
	    }
	    return null
	}

	val lastIx: Int
	    get() {
		var max = -1
		val it: Iterator<*> = all.values.iterator()
		while (it.hasNext()) {
		    val bx = it.next() as Box
		    if (bx.o_x > max) {
			max = bx.o_x
		    }
		}
		return max
	    }

	fun getLastIy(ix: Int): Int {
	    var max = -1
	    val it: Iterator<*> = all.values.iterator()
	    while (it.hasNext()) {
		val bx = it.next() as Box
		if (bx.o_x == ix && bx.o_y > max) {
		    max = bx.o_y
		}
	    }
	    return max
	}

	fun clearAll() {
//	    all.removeAll();
	}

	fun mark_All() {
	    val it: Iterator<*> = all.values.iterator()
	    while (it.hasNext()) {
		val bx = it.next() as Box
		bx.marked = true
	    }
	}

	fun delAllMarked() {
	    val it: Iterator<*> = all.values.iterator()
	    while (it.hasNext()) {
		val bx = it.next() as Box
		if (bx.marked) {
		    all.remove("" + bx.o_x + ':' + bx.o_y)
		    delAllMarked()
		    return
		}
	    }
	}
    }

    val lessonName: String?
	get() = if (lep == null) {
	    lesson_name
	} else {
	    lep!!.lessonName
	}
    val lessonLinkNext: String?
	get() = if (lep == null) {
	    lesson_link_next
	} else {
	    lep!!.lessonLinkNext
	}
    val lessonIsFirst: Boolean
	get() = if (lep == null) {
	    lesson_is_first
	} else {
	    lep!!.lessonIsFirst
	}
    var allBox: AllBox? = null
    var allbox_sy = Any()

    inner class Mouse(var l_canvas: LessonCanvas) : MouseInputAdapter() {
	var mpress_p: Point2D? = null
	var NORM = 0
	var MSG = 1
	var mode = NORM
	var last_hbx: Box? = null
	override fun mousePressed(e: MouseEvent) {
	    if (Lesson.mistNoMouse) {
		return
	    }
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    if (mode == NORM) {
		var ix: Int
		if (hitTarget(e.x, e.y, 'p').also { ix = it } != -1) {
		    if (e.isPopupTrigger) {
			if (lep != null) {
			    lep!!.popupTargetProp()
			}
		    }
		    if (mouse_mark_target) {
			target_standout_msk = 1 shl ix
			mark_more = false
		    }
		    repaintTarget()
		    fireLessonEditorHitTarget(ix, 'p')
		    active_titem_ix = ix
		    setQuitState("mP", 0)
		    return
		}
		if (true) {
		    val ia = IntArray(1)
		    val hbx = hitBox(e.x, e.y, 'p', ia)
		    if (hbx != null) {
			if (e.isPopupTrigger) {
			    if (lep != null) {
				lep!!.popupItemProp()
			    }
			}
			hbx.where = ia[0]
			if (hbx != null) {
			    selectBox(hbx, true, e.getWhen())
			    last_nav_kbd = false
			}
		    }
		}
		if (edit) {
		    var it: Iterator<ShapeList.ShapeItem> = addBoxes!!.li.iterator()
		    while (it.hasNext()) {
			val oa = it.next()
			val id = oa.id
			val shp = oa.shp
			if (shp.contains(mpress_p)) {
			    if (id.length > 0) {
				addItemEntry(id.toInt())
			    }
			}
		    }
		    it = delBoxes!!.li.iterator()
		    while (it.hasNext()) {
			val oa = it.next()
			val id = oa.id
			val shp = oa.shp
			if (shp.contains(mpress_p)) {
			    if (id.length > 0) {
				delItemEntry(id.toInt())
			    }
			}
		    }
		    it = tgAddBoxes!!.li.iterator()
		    while (it.hasNext()) {
			val oa = it.next()
			val id = oa.id
			val shp = oa.shp
			if (shp.contains(mpress_p)) {
			    if (id.length > 0) {
				if (this@LessonCanvas.target!!.get_howManyT_Items() < 6) {
				    addTarget(id.toInt())
				}
			    }
			}
		    }
		    it = tgDelBoxes!!.li.iterator()
		    while (it.hasNext()) {
			val oa = it.next()
			val id = oa.id
			val shp = oa.shp
			if (shp.contains(mpress_p)) {
			    if (id.length > 0) {
				delTarget(id.toInt())
			    }
			}
		    }
		}
	    } else { // mode == MSG
		hideMsg()
	    }
	}

	var last_ix = -1

	init {
	    addMouseListener(this)
	    addMouseMotionListener(this)
	}

	override fun mouseMoved(e: MouseEvent) {
	    if (false && Lesson.mistNoMouse) {
		return
	    }
	    if (last_nav_kbd) {
		return
	    }
	    var ix: Int
	    if (hitTarget(e.x, e.y, 'p').also { ix = it } != -1) {
		if (mouse_mark_target && edit) {
		    target_standout_msk = 1 shl ix
		    mark_more = false
		}
		repaintTarget()
		if (last_ix != ix) {
		    fireLessonEditorHitTarget(ix, 'm')
		    last_ix = ix
		}
		return
	    }
	    val ia = IntArray(1)
	    val hbx = hitBox(e.x, e.y, 'm', ia)
	    if (hbx != null) {
	    }
	    if (hbx != null && hbx !== last_hbx) {
		val msk = l_canvas.target!!.whatTargetMatchTid(hbx.item!!.entryTid)
		if (box_mark_target) {
		    target_standout_msk = msk
		    mark_more = false
		}
		enterBox(hbx)
		repaintTarget()
		last_hbx = hbx
	    }
	    if (hitQuitButton(e.x, e.y)
		|| hitExtraQuitButton(e.x, e.y)
	    ) {
		if (hitQuitButton(e.x, e.y)) {
		    if (quit_state == 0) {
			setQuitState("mM", 1)
		    } else if (quit_state == 3) {
			setQuitState("mM", 2)
		    } else {
		    }
		} else {
		}
		if (hitExtraQuitButton(e.x, e.y)) {
		    if (quit_state == 2) {
			setQuitState("mMx", 3)
		    }
		} else {
// 		    if ( quit_state == 2 || quit_state == 3 )
// 			setQuitState("mM", 1);
		}
	    } else {
	    }
	}

	override fun mouseDragged(e: MouseEvent) {}
	override fun mouseReleased(e: MouseEvent) {
	    if (false && Lesson.mistNoMouse) {
		return
	    }
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    OmegaContext.sout_log.getLogger().info(":--: release $mode")
	    if (mode == NORM && !Lesson.mistNoMouse) {
		var ix: Int
		if (hitTarget(e.x, e.y, 'p').also { ix = it } != -1) {
		    if (e.isPopupTrigger) {
			if (lep != null) {
			    lep!!.popupTargetProp()
			}
		    }
		}
		if (!Lesson.mistNoMouse) {
		    val ia = IntArray(1)
		    val hbx = hitBox(e.x, e.y, 'p', ia)
		    if (hbx != null && e.isPopupTrigger) {
			if (lep != null) {
			    lep!!.popupItemProp()
			}
		    }
		}
	    }
	    if (!Lesson.mistNoMouse) {
		if (hitQuitButton(e.x, e.y)) {
		    if (quit_state == 1) {
			setQuitState("mQ", 2)
		    } else if (quit_state == 2) {
			setQuitState("mQ", 1)
		    } else if (quit_state == 3) {
			setQuitState("mQ", 1)
		    } else {
			setQuitState("mQ", 1)
		    }
		} else {
		    if (hitExtraQuitButton(e.x, e.y)) {
			if (quit_state == 2 || quit_state == 3) {
			    setQuitState("mP", 0)
			    hideMsg()
			    fireExit(5)
			    //om_msg_mgr.fire("exit create");
			}
		    }
		}
	    }
	    OmegaContext.sout_log.getLogger().info(":--: " + "" + isMsg)
	    if (isMsg) {
		hideMsg()
	    }
	}
    }

    fun fireExit(where: Int) {
	OmegaContext.sout_log.getLogger().info(":--: fireExit $where")
	om_msg_mgr.fire("show_result")
    }

    fun fireRealExit() {
	OmegaContext.sout_log.getLogger().info(":--: " + "fireRealExit")
	om_msg_mgr.fire("exit create")
    }

    fun setNextMarkTarget() {
	target_standout_msk = target_standout_msk shl 1
	mark_more = false
    }

    fun setMarkTarget(ix: Int) {
	setMarkTarget(ix, false)
    }

    fun setMarkTarget(ix: Int, mark_more: Boolean) {
	this.mark_more = mark_more
	target_standout_msk = if (ix == -1) {
	    0
	} else {
	    1 shl ix
	}
	mouse_mark_target = false
	box_mark_target = false
	repaintTarget()
    }

    fun setMarkTargetNo() {
	target_standout_msk = 0
	mark_more = false
	mouse_mark_target = true
	box_mark_target = true
	repaintTarget()
    }

    fun setMarkTargetAll() {
	target_standout_msk = 0xff
	mark_more = false
	mouse_mark_target = true
	box_mark_target = true
	repaintTarget()
    }

    var m: Mouse = Mouse(this)
    var last_bx: Box? = null

    @Synchronized
    fun enterBox(bx: Box?) {
	if (bx == null) {
	    return
	}
	if (last_bx !== bx) {
	    fireLessonEditorHitItem(bx.o_x, bx.o_y, 1, 'm')
	    last_bx = bx
	}
    }

    var last_selected_box: Box? = null
    fun selectBox(bx: Box?, with_mouse: Boolean, when_hit: Long) {
	if (bx == null) {
	    return
	}
	setQuitState("selB", 0)
	skip_keycode = true
	last_selected_box = bx

// 	if ( with_mouse == false ) {
// 	    if ( mouse_mark_target )
// 		target_standout_msk = 1 << ix;
// 	}
	box_state.setState(bx, BUSY, true)
	box_state.setState(bx, SELECTED, true)
	bx.repaintBox()
	if (useThisText(bx.item!!.text)) {
	    bx.when_hit = when_hit
	    l_ctxt.lesson.sendMsg("hBox" + if (with_mouse) "M" else "K", bx)
	}
	if (bx.item != null && bx.item!!.isAction) {
	    active_item_action_box = bx
	    //repaint();
	}
	if (bx.item != null && bx.item is Item) {
	    active_item_box = bx
	    //repaint();
	}
	fireLessonEditorHitItem(bx.o_x, bx.o_y, 1, 'p')
    }

    private fun useThisText(text: String?): Boolean {
	if (text!!.length == 0) return false
	return !text.matches("[{]\\*[0-9]*:[}]".toRegex())
    }

    fun ready() {
	box_state.setState(null, BUSY, false)
    }

    var current_ix = 0
    var current_iy = 0
    fun gotoBox(ix: Int, iy: Int) {
	val bx = allBox!!.getBox(ix, iy)
	bx?.let { enterBox(it) }
    }

    fun gotoBoxNoEnter(ix: Int, iy: Int) {
	val bx = allBox!!.getBox(ix, iy)
    }

    fun gotoNextBox() {
	gotoNextSmartBox()
    }

    private fun gotoPrevBox() {
	setQuitState("prevB", 2)
    }

    private fun gotoQuit() {
	setQuitState("goQ", 1)
    }

    private fun gotoCorrectBox() {
	gotoNextSmartBox()
    }

    var last_bix = -1
    var last_bixAix = -1
    private fun gotoNextSmartBox() {
	try {
	    val nix = target!!.findNextFreeT_ItemIx()
	    val bixA = target!!.findEntryIxMatchTargetIxAll(nix)
	    if (last_bixAix == -1 || last_bixAix >= bixA.size) {
		last_bixAix = 0
	    }
	    var bix = bixA[last_bixAix]
	    if (mouse_mark_target) {
		target_standout_msk = 1 shl bix
		mark_more = false
		repaintTarget()
	    }
	    var niy = 0
	    if (bix == last_bix) {
		niy = current_iy + 1
	    }
	    last_bix = bix
	    var bx = allBox!!.getBox(bix, niy)
	    if (bx != null) {
		setQuitState("z1", 0)
		current_iy = niy
		current_ix = bix
		val where = IntArray(1)
		hitBox(
		    bx.r.x.toInt(), bx.r.y.toInt(),
		    'm',
		    where
		)
		enterBox(bx)
	    } else {
		setQuitState("z1", 0)
		current_iy = 0
		last_bixAix++
		if (last_bixAix >= bixA.size) {
		    last_bixAix = 0
		}
		bix = bixA[last_bixAix]
		current_ix = bix
		bx = allBox!!.getBox(bix, current_iy)
		val where = IntArray(1)
		hitBox(
		    bx!!.r.x.toInt(), bx.r.y.toInt(),
		    'm',
		    where
		)
		enterBox(bx)
		last_bix = bix
	    }
	} catch (ex: Exception) {
	    ex.printStackTrace()
	}
    }

    private fun gotoSameBox() {
	try {
	    var bx: Box?
	    var nix = current_ix
	    var niy = current_iy
	    bx = allBox!!.getBox(nix, niy)
	    if (bx == null) {
		nix++
		niy = 0
		bx = allBox!!.getBox(nix, niy)
		if (bx == null) {
		    nix = 0
		    niy = 0
		    setQuitState("z2", 0)
		    bx = null // getAllBox().getBox(nix, niy);
		}
	    }
	    if (bx != null) {
		setQuitState("z3", 0)
		//log		OmegaContext.sout_log.getLogger().info(":--: " + "move " + current_ix + ' ' + current_iy + ' ' + nix + ' ' + niy);
		current_iy = niy
		current_ix = nix
		val where = IntArray(1)
		hitBox(
		    bx.r.x.toInt(), bx.r.y.toInt(),
		    'm',
		    where
		)
		enterBox(bx)
	    }
	} catch (ex: NullPointerException) {
	    ex.printStackTrace()
	}
    }

    fun gotoBoxRel(dx: Int, dy: Int) {
	val bx = allBox!!.getBox(current_ix + dx, current_iy + dy) ?: return
	current_ix += dx
	current_iy += dy
	val where = IntArray(1)
	hitBox(
	    bx.r.x.toInt(), bx.r.y.toInt(),
	    'm',
	    where
	)
	enterBox(bx)
    }

    fun selectBox(with_mouse: Boolean, when_hit: Long) {
	try {
	    val bx = allBox!!.getBox(current_ix, current_iy)
	    val where = IntArray(1)
	    hitBox(
		bx!!.r.x.toInt(), bx.r.y.toInt(),
		'p',
		where
	    )
	    selectBox(bx, with_mouse, when_hit)
	} catch (ex: NullPointerException) {
	}
    }

    fun sowDummy(current_correct_sentence: String?): Int {
	return target!!.sowDummy(current_correct_sentence)
    }

    fun removeDummy() {
	target!!.removeDummy()
    }

    val target: Target?
	get() = l_ctxt.target
    val allTargetCombinations: Array<String?>
	get() = getAllTargetCombinations(" ", false)

    fun getAllTargetCombinationsEx(sep: String?, dummy: Boolean, delim: Char): Array<String?> {
	return try {
	    val tg2 = Target()
	    val story_hm = Lesson.story_hm
	    tg2.loadFromEl(l_ctxt.lesson.element, "", story_hm, dummy, false) // FIX nomix?
	    tg2.getAllTargetCombinationsEx(sep, delim)
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    arrayOfNulls(0)
	}
    }

    fun getAllTargetCombinationsEx2(dummy: Boolean): TargetCombinations {
	return try {
	    val tg2 = Target()
	    val story_hm = Lesson.story_hm
	    tg2.loadFromEl(l_ctxt.lesson.element, "", story_hm, dummy, false) // FIX nomix?
	    tg2.getAllTargetCombinationsEx2(l_ctxt.lesson)
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    TargetCombinations()
	}
    }

    @Deprecated("")
    fun getAllTargetCombinationsEx2(omega_lesson: File?, dummy: Boolean): TargetCombinations {
	return try {
	    val tg2 = Target()
	    val story_hm = Lesson.story_hm
	    tg2.loadFromEl(l_ctxt.lesson.element, "", story_hm, dummy, false) // FIX nomix?
	    tg2.getAllTargetCombinationsEx2(l_ctxt.lesson)
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    TargetCombinations()
	}
    }

    fun getAllTargetCombinations(sep: String?, dummy: Boolean): Array<String?> {
	return try {
	    val tg2 = Target()
	    val story_hm = Lesson.story_hm
	    tg2.loadFromEl(l_ctxt.lesson.element, "", story_hm, dummy, false) // FIX nomix?
	    tg2.getAllTargetCombinations(sep)
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    arrayOfNulls(0)
	}
    }

    @JvmOverloads
    fun askForOneTarget(owner: Component?, def: String? = null): String {
	var def = def
	val sa = allTargetCombinations
	if (def == null) {
	    def = sa[0]
	}
	return JOptionPane.showInputDialog(
	    owner,
	    t("Select a sentence"),
	    "Omega - Option",
	    JOptionPane.QUESTION_MESSAGE,
	    null,
	    sa,
	    def
	) as String
    }

    fun addTarget(where: Int) {
	val sa = arrayOf(
	    t("Cancel"),
	    t("Add new target")
	)
	val sel = GetOption.getOption(
	    t("Adding the target area"),
	    sa
	)
	if (sel > 0) {
	    target!!.addT_Item(where)
	    reCreateBoxesKeep()
	    repaint()
	}
    }

    fun delTarget(where: Int) {
	val sa = arrayOf(
	    t("Cancel"),
	    t("Delete target")
	)
	val sel = GetOption.getOption(
	    t("Deleting the target area"),
	    sa
	)
	if (sel > 0) {
	    target!!.delT_Item(where)
	    reCreateBoxesKeep()
	    repaint()
	}
    }

    fun addItemEntry(ix: Int) {
	val sa = arrayOf(
	    t("Cancel"),
	    t("Add new column")
	)
	val sel = GetOption.getOption(
	    t("Add a new column"),
	    sa
	)
	if (sel > 0) {
	    target!!.addItemEntry(ix, 0)
	    target!!.addItem(ix, 0)
	    reCreateBoxesKeep()
	    if (active_item_box != null) {
		val cix = active_item_box!!.o_x
		val ciy = active_item_box!!.o_y
		if (cix >= ix) {
		    active_item_box = allBox!!.getBox(cix + 1, ciy)
		}
	    }
	    if (active_item_action_box != null) {
		val cix = active_item_action_box!!.o_x
		val ciy = active_item_action_box!!.o_y
		if (cix >= ix) {
		    active_item_action_box = allBox!!.getBox(cix + 1, ciy)
		}
	    }
	    repaint()
	}
    }

    fun delItemEntry(ix: Int) {
	val sa = arrayOf(
	    t("Cancel"),
	    t("Delete column")
	)
	val sel = GetOption.getOption(
	    t("Delete selected column"),
	    sa
	)
	if (sel > 0) {
	    target!!.delItemEntry(ix, 0)
	    reCreateBoxes()
	    repaint()
	}
    }

    fun showWordbox(): Boolean {
	return show_wordbox
    }

    private fun reCreateBoxes() {
	active_item_action_box = null
	active_item_box = null
	reCreateBoxesKeep()
    }

    fun reCreateBoxesKeep() {
	createBoxes()
	repaint()
    }

    var rndA: IntArray? = null
    fun createBoxes() {
	val x = (caW * gapE / 100.0).toInt()
	val y = (caH * (2 * gapE) / 100.0).toInt() + (caH * tgH / 100.0).toInt() * 2 + 25
	val WW = caW - x - x
	val HH = (caH * (100 - tgH - tgH) / 100.0).toInt() - y
	val gB = (WW * gapB / 100.0).toInt()
	val nx = target!!.howManyItemBoxes()
	val ny = if (edit) 8 else target!!.maxItemsInAnyBox
	val fo = Font("Arial", Font.PLAIN, 12)
	// 	Lambda la = new Lambda() {
// 		public void eval(Object oa, Object ob) {
// 		    String s = (String)oa);
// 		}
// 	    };
// 	int sw = getStringWidth(item_fot, msg_item.text);
	val g2 = graphics as Graphics2D
	val max_bxw = target!!.getMaxWidthSumAllBox(fo, g2)
	synchronized(allbox_sy) {

//  	    if ( allbox != null )
//  		allbox.clearAll();
	    if (allBox == null) {
		allBox = AllBox()
	    }
	    allBox!!.mark_All()
	    tgDelBoxes = ShapeList()
	    tgAddBoxes = ShapeList()
	    addBoxes = ShapeList()
	    delBoxes = ShapeList()
	    val hh = height / 15
	    val hhh = hh / 5
	    var XX = 0
	    for (xi in 0 until nx) {
		val ratio = (target!!.getMaxWidthInBox(xi, fo, g2) + 1 * gB * 0.5) / (0.5 * nx * gB + max_bxw)
		val sww = ((WW - gB * (nx - 1)) * ratio).toInt()
		val hm_bx = target!!.getMaxItemsInBox(xi)

		//OmegaContext.sout_log.getLogger().info(":--: " + "ratio " + xi + ' ' + ratio + ' ' + bx_w + ' ' + max_bxw + ' ' + sww + ' ' + gB);
		if (hm_bx > 0 && rndA == null) {
		    rndA = createUniq(hm_bx).asIntArray()
		}
		for (yi in 0 until ny) {
		    var yi_rnd: Int
		    yi_rnd = if (false && !edit && rndA != null && yi < rndA!!.size) // FIX NOFATAL
		    {
			rndA!![yi]
		    } else {
			yi
		    }
		    var itm = target!!.getItemAt(xi, yi_rnd)
		    val xx = XX
		    var bx: Box? = null
		    val o_y = yi_rnd
		    val obx = allBox!!.getBox(xi, o_y)
		    if (itm != null) {
			if (obx == null) {
			    bx = Box(
				itm.it_ent,
				x + xx,
				y + yi * (hh + hhh),
				sww,
				hh,
				xi,
				o_y
			    )
			} else {
			    bx = obx
			    bx.marked = false
			    bx.noNewBox(itm.it_ent, x + xx, y + yi * (hh + hhh), sww, hh, xi, o_y)
			}
		    } else if (edit) { // itm == null
			target!!.addEmptyItem(xi, yi)
			itm = target!!.getItemAt(xi, yi)
			if (obx == null) {
			    bx = Box(
				itm!!.it_ent,
				x + xx,
				y + yi * (hh + hhh),
				sww,
				hh,
				xi,
				o_y
			    )
			} else {
			    bx = obx
			    bx.noNewBox(itm!!.it_ent, x + xx, y + yi * (hh + hhh), sww, hh, xi, o_y)
			}
		    }
		    if (bx != null) {
			if (active_item_box != null) {
			    if (active_item_box!!.o_x == bx.o_x
				&& active_item_box!!.o_y == bx.o_y
			    ) {
				active_item_box = bx
			    }
			}
			if (active_item_action_box != null) {
			    if (active_item_action_box!!.o_x == bx.o_x
				&& active_item_action_box!!.o_y == bx.o_y
			    ) {
				active_item_action_box = bx
			    }
			}
			allBox!!.all["$xi:$yi"] = bx
		    }
		}
		XX += sww + gB
	    }
	    allBox!!.delAllMarked()
	} // sync allbox
	createAddDelBoxes()
	repaint()
    }

    fun render(all: Boolean, reset: Boolean) {
	gapB = 5f
	if (reset) {
	    box_state.setState(null, ACTIVATED, false)
	    allBox = AllBox()
	}
	if (all) {
//  	    active_item_action_box = null;
//  	    active_item_box = null;
	    createBoxes()
	    repaint()
	} else {
	    repaint(targetRectangle)
	}
    }

    fun render() {
	if (target == null) {
	    return
	}
	createBoxes()
	repaint()
	return
    }

    fun renderTg() {
	if (target == null) {
	    return
	}
	createAddDelBoxesTgOnly()
	repaintTarget()
	return
    }

    fun resetNav() {
	last_bixAix = 0
    }

    fun initNewLesson() {
	setQuitState("z4", 0)
	rndA = null
	box_state.setState(null, ACTIVATED, false)
	box_state.setState(null, SELECTED, false)
	box_state.setState(null, BUSY, false)
	resetItemFont()
	last_bixAix = 0
    }

    fun eraseHilitedBox() {
	gotoBox(0, 0)
	box_state.setState(null, ACTIVATED, false)
	box_state.setState(null, SELECTED, false)
	box_state.setState(null, BUSY, false)
	box_state.setState(null, MARKED, false)
    }

    fun startAction() {
	box_state.setState(null, ACTIVATED, false)
	box_state.setState(null, SELECTED, false)
	box_state.setState(null, BUSY, false)
	//	show_action.setVisible(true);
	om_msg_mgr.fire("action")
	gotoBox(0, 0)
    }

    var anim_action_patch: AnimAction? = null
    fun waitReplyAction(
	anim_action: AnimAction,
	text: String?,
	show: Boolean,
	myra: Runnable?
    ): String? {
	var end_code_s: String? = null
	anim_action_patch = anim_action
	if (show) {
	    anim_action.rt!!.a_ctxt!!.anim_canvas!!.setBigButtonText(text)
	    end_code_s = anim_action.rt!!.a_ctxt!!.anim_canvas!!.waitBigButtonText(myra)
	    OmegaContext.sout_log.getLogger().info(":--: LessonCanvas: end_ $end_code_s")
	    if (end_code_s == "normal") {
		anim_action.rt!!.a_ctxt!!.anim_canvas!!.setBigButtonText("")
	    }
	}
	anim_action_patch = null
	return end_code_s
    }

    fun endAction() {}
    fun endLastAction() {
//	show_action.setVisible(false);
	init()
    }

    fun initAction() {
//	show_action.setVisible(false);
    }

    fun removeHilitedBox() {
	eraseHilitedBox()
	last_bix = -1
	repaint()
    }

    fun init() {
	target!!.releaseAllT_Items()
	resetItemFont()
	repaintTarget()
    }

    fun resetItemFont() {
	item_fo = null
	item_fo2 = null
	trgt_fo = null
	trgtA_fo = null
    }

    fun setItemFont(fo: Font?) {
	item_fo = fo
    }

    fun setItemFont2(fo: Font?) {
	item_fo2 = fo
    }

    fun setTargetFont() {
	val h = (1.5 * caH * tgH / 100.0).toInt()
	trgt_fo = Font("Arial", Font.PLAIN, getSize(caW.toDouble() / caH, h))
    }

    fun setTargetFont(f: Double) {
	trgt_fo = Font("Arial", Font.PLAIN, f.toInt())
    }

    val targetFont: Font?
	get() {
	    if (trgt_fo == null) {
		setTargetFont()
	    }
	    return trgt_fo
	}

    fun setTargetFontAlt(f: Double) {
	val h = (caH * tgH / 100.0).toInt()
	trgtA_fo = Font(
	    "Arial",
	    Font.PLAIN, (f * getSize(caW.toDouble() / caH, (h * 0.65).toInt())).toInt()
	)
    }

    val targetFontAltS: Font
	get() {
	    val h = (caH * tgH / 100.0).toInt()
	    return Font(
		"Arial",
		Font.PLAIN, (0.8 * getSize(caW.toDouble() / caH, (h * 0.65).toInt())).toInt()
	    )
	}
    val targetFontAlt: Font?
	get() {
	    if (trgtA_fo == null) {
		setTargetFontAlt(1.0)
	    }
	    return trgtA_fo
	}

    fun getSize(asp: Double, h: Int): Int {
	if (asp == 0.0) {
	    return (h * 0.65).toInt()
	}
	var hh = (h * 0.55).toInt()
	if (asp < 1.0) {
	    hh = (hh * asp).toInt()
	}

//	OmegaContext.sout_log.getLogger().info(":--: " + "      >>>>>>>>     " + asp + ' ' + hh);
	return hh
    }

    // --
    var title_fo: Font? = null
    fun setTitleFont() {
	title_fo = Font("Arial", Font.PLAIN, gX(0.024))
    }

    val titleFont: Font?
	get() {
	    if (title_fo == null) {
		setTitleFont()
	    }
	    return title_fo
	}

    // -- //
    fun getBox(itm: Item): Box? {
	val it: Iterator<*> = allBox!!.all.values.iterator()
	while (it.hasNext()) {
	    val bx = it.next() as Box
	    if (bx.item == itm) {
		return bx
	    }
	}
	return null
    }

    fun repaint(tit: T_Item?) {
	repaintTarget()
    }

    fun repaint(itm: Item) {
	val bx = getBox(itm)
	bx?.repaintBox()
    }

    fun repaintTarget() {
	val r = targetRectangle
	r.y -= 80
	r.height += 80
	repaint(r)
    }

    fun getTargetRectangle(ix: Int): Rectangle {
	var s_left = target!!.getTextUpto(ix, if (edit) 3 else 1)
	if (s_left.length > 0) {
	    s_left += if (edit) "   " else " "
	}
	val s = target!!.getTextAt(ix)
	val w_left = getStringWidth(targetFont!!, s_left)
	val w_s = getStringWidth(targetFont!!, s)
	val r = targetRectangle
	return Rectangle(
	    r.x + (if (edit) 30 else 10) + w_left,
	    r.y + 2,
	    w_s,
	    r.height - 4
	)
    }

    val targetRectangle: Rectangle
	get() {
	    val x = (caW * gapE / 100.0).toInt()
	    val y = (caH * (2 * gapE) / 100.0).toInt()
	    val w = caW - x - x
	    val h = (caH * tgH / 100.0).toInt()
	    return Rectangle(x, y, w, h)
	}
    val targetShape: Shape
	get() = getTargetRectangleMoreR(0.0)
    val targetRectangleMore: Rectangle
	get() = getTargetRectangleMore(0.0)

    fun getTargetRectangleMore(insets: Double): Rectangle {
	val x = insets + caW * (gapE / 2.0) / 100.0
	val y = insets + caH * (1 * gapE) / 100.0
	val w = caW - x - x - insets * 2.0
	val h = 2 * caH * tgH / 100.0 - insets * 2.0
	return Rectangle(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    }

    fun getTargetRectangleMoreR(insets: Double): Shape {
	val x = insets + caW * (gapE / 2.0) / 100.0
	val y = insets + caH * (1.2 * gapE) / 100.0
	val w = caW - x - x
	val h = 1.7 * caH * tgH / 100.0 - insets * 2.0
	return RoundRectangle2D.Double(x, y, w, h, h, h)
    }

    override fun populateGUI() {}
    fun drawTarget(g2: Graphics2D) {
	val tg = target
	val rra = targetRectangle
	val rram = getTargetRectangleMoreR(0.0)
	val baseline = rra.y + rra.height * 0.7
	if (tg != null) {
	    if (!edit) {
		g2.color = markTarget(getColor("sn_fr")!!)
		g2.fill(rram)
		g2.color = markTarget(getColor("sn_bg")!!)
		val rram2 = getTargetRectangleMoreR(3.0)
		g2.fill(rram2)
	    }
	    if (edit) {
		g2.color = getColor("sn_bg")
		g2.fill(rra)
		if (target_standout_msk != 0) {
		    for (i in 0..0xfe) {
			if (1 shl i and target_standout_msk != 0) {
			    val rr = getTargetRectangle(i)
			    var col = moreSaturate(moreSaturate(getColor("sn_hi")!!))
			    if (mark_more) {
				col = moreSaturate(moreSaturate(col))
			    }
			    g2.color = col
			    g2.fill(rr)
			}
		    }
		}
		for (i in 0 until target!!.get_howManyT_Items()) {
		    val rr = getTargetRectangle(i)
		    g2.color = Color(111, 111, 111)
		    if (active_titem_ix == i) {
			g2.color = Color.green
		    }
		    g2.draw(rr)
		}
		g2.stroke = BasicStroke(caH / 300f)
		g2.color = getColor("sn_fr")
		g2.draw(rra)
	    }
	    if (showWordbox()) {
		if (target_standout_msk != 0) {
		    for (i in 0..5) {
			if (1 shl i and target_standout_msk != 0) {
			    val rr = getTargetRectangle(i)
			    var hh = 5
			    if (mark_more) {
				hh = (rram.bounds.getHeight() * 0.7).toInt()
			    }
			    val rr2 = Rectangle(rr.x, rr.y + rr.height - hh, rr.width, hh)
			    var col = getColor("sn_hi")
			    if (mark_more) {
				col = moreSaturate(moreSaturate(col!!))
			    }
			    g2.color = col
			    g2.fill(rr2)
			    g2.color = getColor("sn_hs")
			    g2.draw(rr2)
			} else {
			    val rr = getTargetRectangle(i)
			    val rr2 = Rectangle(rr.x, rr.y + rr.height - 5, rr.width, 5)
			    //  			    g2.setColor(getColor("sn_hi"));
//  			    g2.fill(rr2);
			    g2.color = getColor("sn_hs")
			    g2.draw(rr2)
			}
		    }
		}
	    }
	}

	//--
	if (!true) {
	    val a = Area()
	    val x = rra.getX()
	    val y = rra.getY()
	    val w = rra.getWidth()
	    val h = rra.getHeight()
	    val fr: RoundRectangle2D = RoundRectangle2D.Double(x, y, w, h, 5.0, 5.0)
	    a.add(Area(Rectangle2D.Double(0.0, 0.0, 10000.0, 10000.0)))
	    a.subtract(Area(fr))
	    g2.clip = a
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f)
	    g2.color = Color(15, 15, 15)
	    for (i in 7..7) {
		val frs: RoundRectangle2D = RoundRectangle2D.Double(x + 10 - i, y + 10 - i, w, h, 5.0, 5.0)
		g2.fill(frs)
	    }
	    g2.clip = Area(Rectangle2D.Double(0.0, 0.0, 10000.0, 10000.0))
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	}

	//--
	if (edit && active_titem_ix >= 0) {
	    val rr = getTargetRectangle(active_titem_ix)
	    g2.color = Color.green
	    g2.stroke = BasicStroke(caH / 500f)
	    g2.draw(rr)
	}
	val s = if (tg == null) "-" else target!!.getAllText(if (edit) 3 else 1)
	if (!true) {
	    g2.color = getColor("sn_tx")
	} else {
	    g2.color = getColor("sn_tx")
	}
	val fo = targetFont
	g2.font = fo
	val sw = getStringWidth(targetFont!!, s) + 20
	if (sw > rra.width) {
	    val ff = rra.width / sw.toDouble()
	    setTargetFont(targetFont!!.size * ff)
	    repaint()
	} else {
	    g2.drawString(s, rra.x + if (edit) 30 else 10, (rra.y + rra.height * 0.7).toInt())
	}
    }

    fun drawTargetNull(g2: Graphics2D) {
	val tg = target
	val rra = targetRectangle
	val s = if (tg == null) "-" else target!!.getAllText(if (edit) 3 else 1)
	val fo = targetFont
	g2.font = fo
	val sw = getStringWidth(targetFont!!, s) + 20
	if (sw > rra.width) {
	    val ff = rra.width / sw.toDouble()
	    setTargetFont(targetFont!!.size * ff)
	    repaint()
	}
    }

    var box_state = BoxState()
    fun hitBox(x: Int, y: Int, type: Char, where: IntArray): Box? {
	if (allBox != null) {
	    val it: Iterator<*> = allBox!!.all.values.iterator()
	    while (it.hasNext()) {
		val bx = it.next() as Box
		var bhit: Int
		if (bx.hitBox(x, y).also { bhit = it } >= 0) {
		    where[0] = bhit
		    if (type == 'm') {
			box_state.setState(bx, SELECTED, true)
			box_state.setState(bx, MARKED, true)
		    } else if (type == 'p') {
			box_state.setState(bx, ACTIVATED, true)
			box_state.setState(bx, SELECTED, true)
			box_state.setState(bx, MARKED, true)
		    }
		    return bx
		}
	    }
	}
	box_state.setState(null, SELECTED, false)
	box_state.setState(null, BUSY, false)
	return null
    }

    fun hitTarget(hx: Int, hy: Int, type: Char): Int {
	if (target != null) {
	    for (i in 0 until target!!.get_howManyT_Items()) {
		val r = getTargetRectangle(i)
		if (r.contains(hx, hy)) {
		    return i
		}
	    }
	}
	return -1
    }

    fun addTgAddBox(sbl: ShapeList, da: Array<Double?>, y: Double) {
	val d = (caW / 60).toDouble()
	val col = Color(150, 150, 150)
	var i = 0
	while (i < da.size) {
	    val r: RoundRectangle2D = RoundRectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - d / 2 + 0.5,
		y + 0.5,
		d + 0.5,
		d + 0.5,
		d / 4 + 0.5,
		d / 4 + 0.5
	    )
	    val rs: Rectangle2D = Rectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - 0.35 * d + 0.5,
		y + 0.4 * d + 0.5,
		0.7 * d + 0.5,
		0.2 * d + 0.5
	    )
	    val rt: Rectangle2D = Rectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - 0.1 * d + 0.5,
		y + 0.15 * d + 0.5,
		0.2 * d + 0.5,
		0.7 * d + 0.5
	    )
	    sbl.add("" + i / 2, r, col)
	    val a = Area(rs)
	    a.add(Area(rt))
	    if (da.size < 7 * 2) {
		sbl.add("", a, Color.white)
	    }
	    i += 2
	}
    }

    fun addTgDelBox(sbl: ShapeList, da: Array<Double?>, y: Double) {
	val d = (caW / 60).toDouble()
	val col = Color(150, 150, 150)
	var i = 0
	while (i < da.size) {
	    val r: RoundRectangle2D = RoundRectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - d / 2 + 0.5,
		y + 0.5,
		d + 0.5,
		d + 0.5,
		d / 4 + 0.5,
		d / 4 + 0.5
	    )
	    val rs: Rectangle2D = Rectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - 0.35 * d + 0.5,
		y + 0.4 * d + 0.5,
		0.7 * d + 0.5,
		0.2 * d + 0.5
	    )
	    //  	    Rectangle2D rt = new Rectangle2D.Double((da[i].doubleValue() + da[i+1].doubleValue()) / 2 - 0.1 * d + 0.5,
//  						    y + 0.15 * d + 0.5,
//  						    0.2 * d + 0.5,
//  						    0.7 * d + 0.5);
	    sbl.add("" + i / 2, r, col)
	    val a = Area(rs)
	    //	    a.add(new Area(rt));
	    sbl.add("", a, Color.white)
	    i += 2
	}
    }

    fun addAddBox(sbl: ShapeList, da: Array<Double?>, y: Double) {
	val d = (caW / 60).toDouble()
	val col = Color(150, 150, 150)
	var i = 0
	while (i < da.size) {
	    val r: RoundRectangle2D = RoundRectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - d / 2 + 0.5,
		y + 0.5,
		d + 0.5,
		d + 0.5,
		d / 4 + 0.5,
		d / 4 + 0.5
	    )
	    val rs: Rectangle2D = Rectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - 0.35 * d + 0.5,
		y + 0.4 * d + 0.5,
		0.7 * d + 0.5,
		0.2 * d + 0.5
	    )
	    val rt: Rectangle2D = Rectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - 0.1 * d + 0.5,
		y + 0.15 * d + 0.5,
		0.2 * d + 0.5,
		0.7 * d + 0.5
	    )
	    sbl.add("" + i / 2, r, col)
	    val a = Area(rs)
	    a.add(Area(rt))
	    sbl.add("", a, Color.white)
	    i += 2
	}
    }

    fun addDelBox(sbl: ShapeList, da: Array<Double?>, day: Array<Double?>, ba: BooleanArray) {
	val d = (caW / 60).toDouble()
	val col = Color(150, 150, 150)
	var i = 0
	while (i < da.size) {
	    val r: RoundRectangle2D = RoundRectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - d / 2 + 0.5,
		day[i / 2]!! + 0.5,
		d + 0.5,
		d + 0.5,
		d / 4 + 0.5,
		d / 4 + 0.5
	    )
	    val rs: Rectangle2D = Rectangle2D.Double(
		(da[i]!! + da[i + 1]!!) / 2 - 0.35 * d + 0.5,
		day[i / 2]!! + d * 0.4 + 0.5,
		0.7 * d + 0.5,
		0.2 * d + 0.5
	    )
	    if (ba[i / 2]) {
		sbl.add("" + i / 2, r, col)
		sbl.add("", rs, Color.white)
	    }
	    i += 2
	}
    }

    fun createAddDelBoxes() {
	createAddBoxes()
	createDelBoxes()
	createTgAddBoxes()
	createTgDelBoxes()
    }

    fun createAddDelBoxesTgOnly() {
	createTgAddBoxes()
	createTgDelBoxes()
    }

    fun createAddBoxes() {
	val naddBoxes = ShapeList()
	val li: MutableList<Double?> = ArrayList()
	li.add(java.lang.Double.valueOf(0.0))
	var y = 0.0
	for (i in 0..19) {
	    val bx = allBox!!.getBox(i, 0) ?: continue
	    val rr = allBox!!.getBound(i, 0)
	    li.add(java.lang.Double.valueOf(rr!!.x))
	    li.add(java.lang.Double.valueOf(rr.x + rr.width))
	    y = rr.y
	}
	li.add(java.lang.Double.valueOf(caW.toDouble()))
	val bx = allBox!!.getBox(0, 0)
	if (bx != null) {
	    addAddBox(naddBoxes, li.toTypedArray<Double?>(), bx.r.y)
	}
	addBoxes = naddBoxes
    }

    fun createTgAddBoxes() {
	val naddBoxes = ShapeList()
	val li: MutableList<Double?> = ArrayList()
	val r0: Rectangle2D = targetRectangle
	li.add(java.lang.Double.valueOf(r0.x))
	var d = 0.0
	for (i in 0 until target!!.get_howManyT_Items()) {
	    val rr = getTargetRectangle(i)
	    li.add(java.lang.Double.valueOf(rr.getX()))
	    li.add(java.lang.Double.valueOf(rr.getX() + rr.getWidth().also { d = it }))
	}
	li.add(java.lang.Double.valueOf(d + 20))
	addTgAddBox(naddBoxes, li.toTypedArray<Double?>(), r0.y + 4)
	tgAddBoxes = naddBoxes
    }

    fun createTgDelBoxes() {
	val naddBoxes = ShapeList()
	val li: MutableList<Double?> = ArrayList()
	val r0: Rectangle2D = targetRectangle
	var d = 0.0
	for (i in 0 until target!!.get_howManyT_Items()) {
	    val rr = getTargetRectangle(i)
	    li.add(java.lang.Double.valueOf(rr.getX()))
	    li.add(java.lang.Double.valueOf(rr.getX() + rr.getWidth().also { d = it }))
	}
	val dh = (caW / 60).toDouble()
	addTgDelBox(naddBoxes, li.toTypedArray<Double?>(), r0.y +  /*r0.getHeight() + */-dh - 4)
	tgDelBoxes = naddBoxes
    }

    fun createDelBoxes() {
	val ndelBoxes = ShapeList()
	val ba = BooleanArray(20)
	val li: MutableList<Double?> = ArrayList()
	val liy: MutableList<Double?> = ArrayList()
	var hasmore1 = true
	var bx = allBox!!.getBox(1, 0)
	if (bx == null) {
	    hasmore1 = false
	}
	for (ix in 0..19) {
	    for (iy in 20 downTo 0) {
		bx = allBox!!.getBox(ix, iy)
		if (bx == null) {
		    continue
		}
		if (ix > 0) {
		    hasmore1 = true
		}
		val rr = allBox!!.getBound(ix, iy) ?: continue
		val itm = target!!.getItemAt(ix, iy)
		if (hasmore1) {
		    ba[ix] = true
		}
		li.add(java.lang.Double.valueOf(rr.x))
		li.add(java.lang.Double.valueOf(rr.x + rr.width))
		liy.add(java.lang.Double.valueOf(rr.y + rr.height + 5))
		break
	    }
	}
	bx = allBox!!.getBox(0, 0)
	if (bx != null) {
	    addDelBox(ndelBoxes, li.toTypedArray<Double?>(), liy.toTypedArray<Double?>(), ba)
	}
	delBoxes = ndelBoxes
    }

    var wantRP = 1.0
    fun drawBoxes(g2: Graphics2D) {
	synchronized(allbox_sy) {
	    if (allBox != null) {
		wantRP = 1.0
		var o_x = 0
		var it: Iterator<*> = allBox!!.all.values.iterator()
		while (it.hasNext()) {
		    val bx = it.next() as Box
		    if (bx.o_x > o_x) {
			o_x = bx.o_x
		    }
		    bx.drawNull(g2)
		}
		//OmegaContext.sout_log.getLogger().info(":--: " + "wantRP is' " + wantRP);
		if (wantRP >= 1.005 || wantRP <= 0.95) {
		    val fs = item_fo!!.size
		    item_fo = Font("Arial", Font.PLAIN, (fs / (1.01 * wantRP)).toInt())
		    setItemFont2(Font("Arial", Font.PLAIN, (fs / (2 * (1.01 * wantRP))).toInt()))
		    repaint()
		    return
		}
		it = allBox!!.all.values.iterator()
		while (it.hasNext()) {
		    val bx = it.next() as Box
		    if (bx.o_x > o_x) {
			o_x = bx.o_x
		    }
		    bx.draw(g2)
		}
		if (edit) {
		    addBoxes!!.draw(g2)
		    delBoxes!!.draw(g2)
		    tgAddBoxes!!.draw(g2)
		    tgDelBoxes!!.draw(g2)
		}
	    }
	}
    }

    fun drawArrows(g2: Graphics2D) {
	if (!edit) {
	    return
	}
	val stroke = BasicStroke(1f)
	g2.stroke = stroke
	g2.color = Color(111, 111, 111)
	if (allBox != null) {
	    synchronized(allbox_sy) {
		val it: Iterator<*> = allBox!!.all.values.iterator()
		while (it.hasNext()) {
		    val bx = it.next() as Box
		    if (bx.o_y == 0) {
			val itm = bx.item ?: continue
			val msk = target!!.whatTargetMatchTid(itm.entryTid)
			val how_many = howManyBits(msk)
			var msk_cnt = 0
			for (i in 0 until target!!.get_howManyT_Items()) {
			    if (1 shl i and msk != 0) {
				val tit = target!!.getT_Item(i)
				val tgr = getTargetRectangle(i)
				val msk_cnt_w = bx.w / how_many
				val fo = targetFontAlt
				g2.font = fo
				g2.color = Color.black
				g2.drawString(tit!!.tid, tgr.x + 15, tgr.y - 8) // ordtyp
				val txtH = caH / 30
				if (tit.lID4TgOrNull_KeepVar_() != null) {
				    val ls = tit.lID4TgOrNull // banid
				    if (ls != null) {
					g2.drawString(ls, tgr.x + 15, tgr.y - 8 - txtH)
				    }
				    val lt = tit.lIDText // aktörsid
				    if (lt != null) {
					g2.drawString(lt, tgr.x + 15, tgr.y - 8 - txtH - txtH)
				    }
				}
				if (i == 0) {
				    val fos = targetFontAltS
				    g2.font = fos
				    g2.drawString(t("Label:"), 5, tgr.y - 8) // ordtyp
				    g2.drawString(t("Path id:"), 5, tgr.y - 8 - txtH)
				    g2.drawString(t("Actor id:"), 5, tgr.y - 8 - txtH - txtH)
				}
				g2.color = Color(111, 111, 111)
				val x1 = bx.x + msk_cnt_w * msk_cnt + msk_cnt_w / 2
				val y1 = bx.y - 3
				val x2 = tgr.x + tgr.width / 2
				val y2 = tgr.y + tgr.height + 3
				g2.drawLine(x1, y1, x2, y2)
				msk_cnt++
			    }
			}
		    }
		}
	    }
	}
    }

    fun drawQuitButton(g2: Graphics2D) {
	if (!edit) {
	    if (quit_disabled) {
		return
	    }
	    val d = (caW / 60).toDouble()
	    val xx = 0.05 * caW
	    var yy = (1.0 - 0.02 - 0.04) * caH
	    yy -= 20.0
	    val ww = 0.1 * caW
	    val hh = 0.04 * caH
	    val r: RoundRectangle2D = RoundRectangle2D.Double(
		xx,
		yy,
		ww,
		hh,
		d / 4 + 0.5,
		d / 4 + 0.5
	    )
	    g2.color = getColor("bt_bg")
	    g2.fill(r)
	    g2.color = getColor("bt_fr")
	    if (quit_state == 1 || quit_state == 2) {
		g2.color = Color(222, 44, 44)
	    }
	    g2.draw(r)
	    val xx2 = 0.055 * caW
	    var yy2 = (1.0 - 0.02 - 0.01) * caH
	    yy2 -= 20.0
	    val fo = targetFontAlt
	    g2.font = fo
	    g2.color = getColor("bt_tx")
	    if (quit_state == 2 || quit_state == 3) {
		g2.drawString(t("Cancel"), xx2.toInt(), yy2.toInt())
	    } else {
		g2.drawString(t("Quit"), xx2.toInt(), yy2.toInt())
	    }
	}
    }

    fun drawExtraQuitButton(g2: Graphics2D) {
	if (quit_state == 2 || quit_state == 3) {
	    if (!edit) {
		if (quit_disabled) {
		    return
		}
		val d = (caW / 60).toDouble()
		var xx = 0.05 * caW
		var yy = (1.0 - 0.02 - 0.04) * caH
		yy -= 20.0
		val ww = 0.1 * caW
		val hh = 0.04 * caH
		xx += ww + hh / 2
		val r: RoundRectangle2D = RoundRectangle2D.Double(
		    xx,
		    yy,
		    ww,
		    hh,
		    d / 4 + 0.5,
		    d / 4 + 0.5
		)
		g2.color = getColor("bt_bg")
		g2.fill(r)
		g2.color = getColor("bt_fr")
		if (quit_state == 3) {
		    g2.color = Color(222, 44, 44)
		}
		g2.draw(r)
		var xx2 = 0.055 * caW
		xx2 += ww + hh / 2
		var yy2 = (1.0 - 0.02 - 0.01) * caH
		yy2 -= 20.0
		val fo = targetFontAlt
		g2.font = fo
		g2.color = getColor("bt_tx")
		g2.drawString(t("Quit"), xx2.toInt(), yy2.toInt())
	    }
	}
    }

    fun hitQuitButton(x: Int, y: Int): Boolean {
	if (!edit) {
	    val d = (caW / 60).toDouble()
	    val xx = 0.05 * caW
	    var yy = (1.0 - 0.02 - 0.04) * caH
	    yy -= 20.0
	    val ww = 0.1 * caW
	    val hh = 0.04 * caH
	    if (x > xx && x < xx + ww && y > yy && y < yy + hh) {
		return true
	    }
	}
	return false
    }

    fun hitExtraQuitButton(x: Int, y: Int): Boolean {
	if (!edit) {
	    val d = (caW / 60).toDouble()
	    var xx = 0.05 * caW
	    var yy = (1.0 - 0.02 - 0.04) * caH
	    yy -= 20.0
	    val ww = 0.1 * caW
	    val hh = 0.04 * caH
	    xx += ww + hh / 2
	    if (x > xx && x < xx + ww && y > yy && y < yy + hh) {
		return true
	    }
	}
	return false
    }

    private fun repaintQuitButton() {
	if (!edit) {
	    val d = (caW / 60).toDouble()
	    val xx = 0.05 * caW
	    var yy = (1.0 - 0.02 - 0.04) * caH
	    yy -= 20.0
	    var ww = 0.1 * caW
	    val hh = 0.04 * caH
	    ww *= 3.0
	    repaint(xx.toInt() - 5, yy.toInt() - 5, ww.toInt() + 10, hh.toInt() + 30)
	}
    }

    inner class MsgDialog {
	var msg_item: MsgItem? = null
	var cont_image_fn: String? = "media/default/continue.png"
	fun show(msg: MsgItem?) {
	    set(msg)
	    val ct0 = ct()
	    while (isMsg) {
		m_sleep(200)
		if (ct() - ct0 > 1000 * 30) {
		    return
		}
	    }
	}

	fun showNoWait(msg: MsgItem?) {
	    set(msg)
	}

	fun set(msg: MsgItem?) {
	    if (msg == null) {
		m.mode = m.NORM
		isMsg = false
		OmegaContext.sout_log.getLogger().info(":--: " + "MSGITEM null")
	    } else {
		m.mode = m.MSG
		isMsg = true
		OmegaContext.sout_log.getLogger().info(":--: MSGITEM $msg")
	    }
	    msg_item = msg
	    val w = gX(0.6)
	    val h = gY(0.35)
	    val x = gX(0.2)
	    val y = gY(0.2)
	    repaint(x - 5, y - 5, w + 15, h + 15)
	}

	private fun f(row: Int, tot: Int, rows: Int): Int {
	    return row * tot / rows
	}

	fun draw(g2: Graphics2D) {
	    var w = gX(0.5)
	    val h = gY(0.35)
	    val th = gY(0.039)
	    var x = gX(0.25)
	    val y = gY(0.2)
	    val r = gX(0.02)
	    if (msg_item!!.type == 'W' || msg_item!!.type == 'S') {
		w = gX(0.6)
		x = gX(0.2)
	    }
	    val fr: RoundRectangle2D = RoundRectangle2D.Double(
		x.toDouble(),
		y.toDouble(),
		w.toDouble(),
		h.toDouble(),
		r.toDouble(),
		r.toDouble()
	    )
	    g2.color = getColor("bg_frbg")
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f)
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
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    g2.color = getColor("bg_tx")
	    if (msg_item!!.type == '2') {
		val yy = 0
		var f_f = 30 //item_fo.getSize();
		var item_fot = Font("Arial", Font.PLAIN, f_f)
		while (true) {
		    item_fot = Font("Arial", Font.PLAIN, f_f)
		    val sw = getStringWidth(item_fot, msg_item!!.text)
		    if (sw > w - 20) {
			f_f = (0.9 * f_f).toInt()
		    } else {
			g2.font = item_fot
			break
		    }
		}
		g2.font = item_fot
		g2.drawString(
		    msg_item!!.text,
		    x + f(4, w, 50),
		    y + f(6, h, 10)
		)
	    } else if (msg_item!!.type == 'W') {
		val yy = 0
		var f_f = 30 //item_fo.getSize();
		var item_fot = Font("Arial", Font.PLAIN, f_f)
		while (true) {
		    item_fot = Font("Arial", Font.PLAIN, f_f)
		    val sw = getStringWidth(item_fot, msg_item!!.text)
		    if (sw > w - 20) {
			f_f = (0.9 * f_f).toInt()
		    } else {
			g2.font = item_fot
			break
		    }
		}
		g2.font = item_fot
		g2.drawString(
		    msg_item!!.small_title,
		    x + f(12, w, 50),
		    y + f(3, h, 10)
		)
		g2.drawString(
		    msg_item!!.text,
		    x + f(4, w, 50),
		    y + f(6, h, 10)
		)
	    } else if (msg_item!!.type == 'S') {
		var f_f = 30 //item_fo.getSize();
		var item_fot = Font("Arial", Font.PLAIN, f_f)
		while (true) {
		    item_fot = Font("Arial", Font.PLAIN, f_f)
		    val sw = getStringWidth(item_fot, msg_item!!.text)
		    if (sw > w - 20) {
			f_f = (0.9 * f_f).toInt()
		    } else {
			g2.font = item_fot
			break
		    }
		}
		g2.font = item_fot
		g2.drawString(
		    msg_item!!.text,
		    x + f(12, w, 50),
		    y + f(3, h, 10)
		)
		g2.drawString(
		    msg_item!!.text2,
		    x + f(12, w, 50),
		    y + f(6, h, 10)
		)
	    } else if (msg_item!!.type == 'R') {
		var f_f = 30 //item_fo.getSize();
		var item_fot = Font("Arial", Font.PLAIN, f_f)
		while (true) {
		    item_fot = Font("Arial", Font.PLAIN, f_f)
		    val sw = getStringWidth(item_fot, msg_item!!.text)
		    if (sw > w - 20) {
			f_f = (0.9 * f_f).toInt()
		    } else {
			g2.font = item_fot
			break
		    }
		}
		g2.font = item_fot
		g2.drawString(
		    msg_item!!.text,
		    x + f(2, w, 5),
		    y + f(17, h, 30)
		)
	    }
	    g2.color = getColor("bg_tx")
	    g2.font = titleFont
	    g2.drawString(msg_item!!.title, x + 1 * w / 10, (y + gY(0.03)))
	    var HH = (h * 0.35).toInt()
	    if (msg_item!!.image != null) {
		val hh = (h * 0.35).toInt()
		val ww = 4 * hh / 3
		try {
		    val img = createImageIcon(
			this@LessonCanvas,
			msg_item!!.image!!,
			ww,
			hh
		    )!!.image
		    HH = img.getHeight(null)
		    g2.drawImage(img, x + 3, y + th + 3, null)
		} catch (ex: Exception) {
		}
	    }
	    if (msg_item!!.image2 != null) {
		val hh = (h * 0.35).toInt()
		val ww = 4 * hh / 3
		try {
		    val img = createImageIcon(
			this@LessonCanvas,
			msg_item!!.image2!!,
			ww,
			hh
		    )!!.image
		    g2.drawImage(img, x + 3, y + th + 3 + HH + 5, null)
		} catch (ex: Exception) {
		}
	    }
	    if (cont_image_fn != null) {
		val hh = (h * 0.25).toInt()
		val ww = hh * 4
		try {
		    val img = createImageIcon(
			this@LessonCanvas,
			cont_image_fn!!,
			ww,
			hh
		    )!!.image
		    val imw = img.getWidth(null)
		    g2.drawImage(img, x + w - imw - 3, y + h - hh - 3, null)
		} catch (ex: Exception) {
		}
	    }
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
	}
    }

    var msg_dlg: MsgDialog = MsgDialog()
    fun showMsg(mi: MsgItem?) {
	msg_dlg.show(mi)
    }

    fun showMsgNoWait(mi: MsgItem?) {
	msg_dlg.showNoWait(mi)
	m_sleep(3000)
    }

    fun hideMsg() {
	msg_dlg.set(null)
    }

    var signMovieRectangle: Rectangle? = null

    var mist_blueSky: Shape? = null
    var mistBgCol: Color? = null
    var mistAlpha = 0

    init {
	OmegaContext.lesson_log.getLogger().info("XXX")

//	focus_list = new CycleList(-1);
	requestFocus()
	layout = null
	addComponentListener(cmp_li)
	lc_listeners = EventListenerList()
    }

    fun setMist(mode: Int, blueSky: Shape?, bgCol: Color?, alpha: Int) {
	mist_mode = mode
	mist_blueSky = blueSky
	mistBgCol = bgCol
	mistAlpha = alpha
	repaint()
    }

    override fun paintComponent(g: Graphics) {
	val ct0 = ct()
	val g2 = g as Graphics2D
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	drawBG(g2)
	g.setColor(Color.black)
	val fo = g.getFont()
	drawTargetNull(g)
	drawTarget(g)
	drawBoxes(g)
	drawArrows(g)
	if (isMsg) {
	    try {
		msg_dlg.draw(g2)
	    } catch (ex: Exception) {
		OmegaContext.sout_log.getLogger().info("ERR: " + "Can't show msg")
	    }
	}
	drawQuitButton(g2)
	drawExtraQuitButton(g2)
	if (mist_mode > 0) {
	    if (signMovieRectangle != null);
	    drawMist(
		g2,
		if (LiuMovieManager.repeat_mode === LiuMovieManager.RepeatMode.DO_REPEAT) 2 else 1,
		mist_blueSky,
		mistBgCol!!,
		mistAlpha,
		signMovieRectangle!!
	    )
	}
	val ct1 = ct()
    }

    val element: Element
	get() {
	    val el = Element("lesson_canvas")
	    el.addAttr("show_wordbox", "" + show_wordbox)
	    fillElement(el)
	    return el
	}

    fun setFrom(el: Element, dummy: Boolean) {
	resetItemFont()
	if (lep != null) {
	    lep!!.destroyAllPopups()
	}
	val lel = el.findElement("lesson_canvas", 0)
	show_wordbox = if (lel != null) {
	    val s = lel.findAttr("show_wordbox")
	    s != null && s == "true"
	} else {
	    true
	}
	lesson_name = "-noname-"
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "=-= dep_set lesson_name ");
	val lesson_el = el.findElement("lesson", 0)
	if (lesson_el != null) {
	    val nm = lesson_el.findAttr("name")
	    if (nm != null) {
		lesson_name = nm
	    }
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "=-= lesson_name is " + lesson_name);
	}
	lesson_link_next = null
	val lesson2_el = el.findElement("story", 0)
	if (lesson2_el != null) {
	    val lel2 = el.findElement("link", 0)
	    if (lel2 != null) {
		val nm = lel2.findAttr("next")
		if (nm != null) {
		    lesson_link_next = nm
		}
	    }
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "=-= lesson_link_next is " + lesson_link_next);
	}
	lesson_is_first = false
	val lesson3_el = el.findElement("story", 0)
	if (lesson3_el != null) {
	    val nm = lesson3_el.findAttr("isfirst")
	    if (nm != null) {
		if (nm.equals("yes", ignoreCase = true) || nm == "1") {
		    lesson_is_first = true
		}
	    }
	}
	if (lep != null) {
	    lep!!.lessonName = lesson_name
	    lep!!.lessonLinkNext = lesson_link_next
	    lep!!.lessonIsFirst = lesson_is_first
	}
    }

    fun disposeOldLesson() {
	allBox = null
	repaint()
    }

    override fun enter() {
	super.enter()
	if (allBox == null) {
	    repaint()
	}
	if (false && l_ctxt.lesson.isTestMode) {
	    gotoNextSmartBox()
	} else {
	    gotoQuit()
	}
    }

    fun setQuitState(s: String?, `val`: Int) {
	quit_state = `val`
	repaintQuitButton()
    }

    fun resetHboxFocus() {
	eraseHilitedBox()
	gotoBox(0, 0)
	// 	Box bx = getAllBox().getBox(0, 0);
// 	if ( bx != null ) {
// 	    box_state.setState(bx, BUSY, false);
// 	    box_state.setState(bx, MARKED, false);
// 	    box_state.setState(bx, SELECTED, false);
// 	    bx.repaintBox();
// 	}
	gotoBox(0, 0)
    }

    fun enableQuitButton(b: Boolean) {
	quit_disabled = !b
	repaintQuitButton()
    }

    companion object {
	var gapB = 5f
	const val gapE = 5f
	const val tgH = 8f
	const val SELECTED = 0 // this only when mouseover
	const val ACTIVATED = 1 // clicked with mouse, blue/red frame
	const val MARKED = 2 // whole bureau when mouseover
	const val BUSY = 3 // clicked
	const val BOX_MAXSTATE = 4
	var CH_W = 1
	private var mist_mode = 0
    }
}
