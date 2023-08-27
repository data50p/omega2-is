package com.femtioprocent.omega.anim.canvas

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.anim.appl.AnimEditor
import com.femtioprocent.omega.anim.appl.AnimRuntime
import com.femtioprocent.omega.anim.cabaret.Actor
import com.femtioprocent.omega.anim.cabaret.Cabaret
import com.femtioprocent.omega.anim.cabaret.GImAE
import com.femtioprocent.omega.anim.cabaret.Hotspot
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.anim.panels.cabaret.CabaretPanel
import com.femtioprocent.omega.anim.panels.path.PathProperties
import com.femtioprocent.omega.anim.panels.timeline.TimeLinePanel
import com.femtioprocent.omega.anim.tool.path.AllPath
import com.femtioprocent.omega.anim.tool.path.Path
import com.femtioprocent.omega.anim.tool.path.Probe
import com.femtioprocent.omega.anim.tool.timeline.TimeLine
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker
import com.femtioprocent.omega.graphic.render.Canvas
import com.femtioprocent.omega.lesson.canvas.BaseCanvas.ColorColors
import com.femtioprocent.omega.swing.Popup
import com.femtioprocent.omega.swing.ToolExecute
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.*
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.util.SundryUtils.tD
import com.femtioprocent.omega.xml.Element
import kotlinx.coroutines.*
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Area
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.lang.Runnable
import java.util.*
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.event.MouseInputAdapter
import kotlin.time.measureTime

class AnimCanvas : Canvas {
    var ap = AllPath()
    var m: Mouse? = null
    var key: Key? = null
    var selected_prb: Probe? = null
    var selected_mark: Path.Mark? = null
    var press_p: Point2D? = null
    var sca = 1.0
    var offs_x = 10000.0
    var offs_y = 10000.0
    var offs_w = 0.0
    var offs_h = 0.0
    var a_ctxt: AnimContext
    var gel: GEL? = null
    var gem: GenericEventManager? = null
    var ae: AnimEditor?
    var arun: AnimRuntime?
    private var pa_offs = 10
    var lessonVerb = "jagar"
    var cab: Cabaret
    var actA_animated = arrayOfNulls<Actor>(OmegaConfig.TIMELINES_N)
    var background_color = Color(30, 30, 90)
    var colors = HashMap<String, ColorColors>()
    var hidden_ = false
    override val offsX: Double
	get() = if (true || ae == null) offs_x else 0.0
    override val offsY: Double
	get() = if (true || ae == null) offs_y else 0.0

    fun scaleEventPos(ev: MouseEvent): Point {
	return Point(((ev.x - offs_x) / sca).toInt(), ((ev.y - offs_y) / sca).toInt())
    }

    fun setHidden(b: Boolean) {
	hidden_ = b
	repaint()
    }

    var hook: MouseInputAdapter? = null

    enum class M_TOOL(val code: Int) {
	PATH(0),
	IMAGE(1),
	MARKER(2),
    }

    enum class MT(val code: Int) {
	VOID(0),
	EXTEND(1),
    }

    open inner class Mouse(var anim_canvas: AnimCanvas) : MouseInputAdapter() {
	var was_shift = false
	var m_tool = M_TOOL.PATH
	var m_tool_sub = MT.VOID
	var stack: Stack<*>
	var mpress_p: Point2D? = null

	init {
	    addMouseListener(this)
	    addMouseMotionListener(this)
	    centerBackground()
	    stack = Stack<Any?>()
	}

	fun setM_Tool(mt: M_TOOL, mts: MT) {
	    m_tool = mt
	    m_tool_sub = mts
	    updCursor()
	}

	fun setM_Tool(mt: M_TOOL) {
	    setM_Tool(mt, m_tool_sub)
	}

	fun setMT(mts: MT) {
	    setM_Tool(m_tool, mts)
	}

	override fun mousePressed(e: MouseEvent) {
	    if (hook != null) {
		hook!!.mousePressed(e)
		return
	    }
	    //	OmegaContext.sout_log.getLogger().info(":--: " + "m p " + e.getX() + ',' + e.getY());
	    if (getVisibilityMode_(HIDE_PATH)) {
		hideActors()
		repaint()
	    }
	    setVisibilityMode_(SHOW_PATH)
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())

	    if ( mpress_p!!.x <= 10 && mpress_p!!.y <= 10 ) {
		val mt = measureTime {
		    demo()
		}
		System.err.println("It took (demo) $mt")
		System.err.println("" + l)
		System.err.println("" + l2)
		SundryUtils.m_sleep(3000)
		System.err.println("It took (demo) $mt")
		System.err.println("" + l)
		System.err.println("" + l2)
		return
	    }


	    when (m_tool) {
		M_TOOL.IMAGE, M_TOOL.PATH, M_TOOL.MARKER -> when (m_tool_sub) {
		    MT.EXTEND -> {
			if (selected_prb != null) {
			    if (!e.isShiftDown && was_shift) {
				setMT(MT.VOID)
				was_shift = false
			    } else {
				val p_p: Point2D = Point2D.Double((e.x - offs_x) / sca, (e.y - offs_y) / sca)
				selected_prb!!.seg!!.path!!.extendSegment(p_p)
				// extendTimeLineAsWell();
				repaint()
				if (e.isShiftDown) {
				    setM_Tool(M_TOOL.MARKER, MT.EXTEND)
				    stack = Stack<Any?>()
				    was_shift = true
				} else {
				    setMT(MT.VOID)
				    was_shift = false
				}
				val prb = ap.findNearest(p_p)
				prb!!.seg!!.path!!.selected = true
				//  				if ( pa_prop != null )
//  				    pa_prop.setObject(prb.seg.path);
				prb.seg!!.selectedPoint = prb.sel
				prb.seg!!.path!!.draw(graphics2D)
				ae!!.selectTimeLine(prb.seg!!.path!!)
			    }
			}
		    }

		    MT.VOID -> {
			press_p = Point2D.Double((e.x - offs_x) / sca, (e.y - offs_y) / sca)
			val pt = e.isPopupTrigger
			if (false) { // omega 2
			    popup_maction(e, Path.global_selected)
			} else {
			    if (pt) { // omega 2
				popup_maction(e, Path.global_selected)
			    }
			    ap.deselectAll(graphics2D)
			    if (e.isControlDown) {
				val mk = ap.findNearestMarker(press_p)
				if (OmegaConfig.T) Log.getLogger().info(":--: marker hit $mk")
				if (mk != null) {
				    val pa = mk.pa
				    pa.selected = true
				    //  					if ( pa_prop != null )
//  					    pa_prop.setObject(pa);
				    pa.draw(graphics2D)
				    ae!!.selectTimeLine(pa)
				    selected_prb = null
				    selected_mark = mk
				    setM_Tool(M_TOOL.MARKER)
				}
			    } else {
				var prb = ap.findNearest(press_p)
				if (prb != null && prb.dist > 20) {
				    prb = null
				    ae!!.toolbar_cmd!!.enable_path(0)
				    ae!!.selectTimeLine()
				}
				if (prb != null) {
				    prb.seg!!.path!!.selected = true
				    //  					if ( pa_prop != null )
//  					    pa_prop.setObject(prb.seg.path);
				    prb.seg!!.selectedPoint = prb.sel
				    prb.seg!!.path!!.draw(graphics2D)
				    if (prb.seg == prb.seg!!.path!!.getSq(0) ||
					    prb.seg == prb.seg!!.path!!.getSq(prb.seg!!.path!!.sqN - 1)
				    ) ae!!.toolbar_cmd!!.enable_path(1) else ae!!.toolbar_cmd!!.enable_path(2)
				    ae!!.selectTimeLine(prb.seg!!.path!!)
				    setM_Tool(M_TOOL.PATH)
				} else {
				    setM_Tool(M_TOOL.IMAGE)
				}
				selected_prb = prb
			    }
			}
		    }

		    else -> {
			press_p = Point2D.Double((e.x - offs_x) / sca, (e.y - offs_y) / sca)
			val pt = e.isPopupTrigger
			if (false) {
			    popup_maction(e, Path.global_selected)
			} else {
			    if (pt) {
				popup_maction(e, Path.global_selected)
			    }
			    ap.deselectAll(graphics2D)
			    if (e.isControlDown) {
				val mk = ap.findNearestMarker(press_p)
				if (OmegaConfig.T) Log.getLogger().info(":--: marker hit $mk")
				if (mk != null) {
				    val pa = mk.pa
				    pa.selected = true
				    pa.draw(graphics2D)
				    ae!!.selectTimeLine(pa)
				    selected_prb = null
				    selected_mark = mk
				    setM_Tool(M_TOOL.MARKER)
				}
			    } else {
				var prb = ap.findNearest(press_p)
				if (prb != null && prb.dist > 20) {
				    prb = null
				    ae!!.toolbar_cmd!!.enable_path(0)
				    ae!!.selectTimeLine()
				}
				if (prb != null) {
				    if ( false ) {
					// this code should work, how about this = ... ?
					with(prb.seg!!) {
					    path!!.selected = true
					    selectedPoint = prb.sel
					    path!!.draw(graphics2D)
					    if (this == path!!.getSq(0) ||
						this == path!!.getSq(path!!.sqN - 1)
					    ) ae!!.toolbar_cmd!!.enable_path(1) else ae!!.toolbar_cmd!!.enable_path(2)
					    ae!!.selectTimeLine(path!!)
					    setM_Tool(M_TOOL.PATH)
					}
				    }
				    prb.seg!!.path!!.selected = true
				    prb.seg!!.selectedPoint = prb.sel
				    prb.seg!!.path!!.draw(graphics2D)
				    if (prb.seg == prb.seg!!.path!!.getSq(0) ||
					    prb.seg == prb.seg!!.path!!.getSq(prb.seg!!.path!!.sqN - 1)
				    ) ae!!.toolbar_cmd!!.enable_path(1) else ae!!.toolbar_cmd!!.enable_path(2)
				    ae!!.selectTimeLine(prb.seg!!.path!!)
				    setM_Tool(M_TOOL.PATH)
				} else {
				    setM_Tool(M_TOOL.IMAGE)
				}
				selected_prb = prb
			    }
			}
		    }
		}
	    }
	    if (pa_prop != null) pa_prop!!.setObject(Path.global_selected)
	}

	override fun mouseMoved(e: MouseEvent) {
	    if (hook != null) {
		hook!!.mouseMoved(e)
		return
	    }
	    if (e.isShiftDown) ; else if (e.isControlDown) {
		val mv_p: Point2D = Point2D.Double((e.x - offs_x) / sca, (e.y - offs_y) / sca)
		val mk = ap.findNearestMarker(mv_p)
		if (mk != null) {
		    val pa = mk.pa
		    pa.draw(graphics2D)
		    setM_Tool(M_TOOL.MARKER)
		}
	    } else {
		val mv_p: Point2D = Point2D.Double((e.x - offs_x) / sca, (e.y - offs_y) / sca)
		val prb = ap.findNearest(mv_p)
		if (prb != null && prb.dist > 20) {
		    setM_Tool(M_TOOL.IMAGE)
		} else {
		    setM_Tool(M_TOOL.PATH)
		}
	    }
	    updCursor()
	}

	override fun mouseDragged(e: MouseEvent) {
	    if (hook != null) {
		hook!!.mouseDragged(e)
		return
	    }
	    when (m_tool) {
		M_TOOL.IMAGE -> {
		    val drag2_p: Point2D = Point2D.Double(e.x.toDouble(), e.y.toDouble())
		    offs_w += drag2_p.x - mpress_p!!.x
		    offs_h += drag2_p.y - mpress_p!!.y
		    mpress_p = drag2_p
		    centerBackground()
		    repaint()
		}

		M_TOOL.MARKER -> {
		    val mk = selected_mark
		    if (mk != null) {
			val drag3_p: Point2D = Point2D.Double((e.x - offs_x) / sca, (e.y - offs_y) / sca)
			val where = mk.pa.findNearestPoint(drag3_p)
			mk.moveToPathPosition(where)
			ae!!.isDirty = true
			repaint()
		    }
		}

		M_TOOL.PATH -> when (m_tool_sub) {
		    MT.VOID -> {
			val drag_p: Point2D = Point2D.Double((e.x - offs_x) / sca, (e.y - offs_y) / sca)
			if (e.isShiftDown) {
			    if (selected_prb != null) {
				val drag3_p: Point2D = Point2D.Double(e.x.toDouble(), e.y.toDouble())
				val dx = drag3_p.x - mpress_p!!.x
				val dy = drag3_p.y - mpress_p!!.y
				mpress_p = drag3_p
				selected_prb!!.pa!!.moveAll(dx / sca, dy / sca)
				ae!!.isDirty = true
			    }
			} else {
			    if (selected_prb != null) {
				selected_prb!!.seg!!.moveto(selected_prb!!.sel, drag_p)
				selected_prb!!.seg!!.path!!.updateInternal()
				ae!!.isDirty = true
			    }
			}
			repaint()
		    }
		    MT.EXTEND -> {}
		}
	    }
	    if (pa_prop != null) pa_prop!!.setObject(Path.global_selected)
	}

	override fun mouseReleased(e: MouseEvent) {
	    if (hook != null) {
		hook!!.mouseReleased(e)
		return
	    }
	}

	fun updCursor() {
	    cursor = when (m_tool) {
		M_TOOL.IMAGE -> if (m_tool_sub == MT.EXTEND) Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR) else Cursor.getPredefinedCursor(
			Cursor.HAND_CURSOR
		)

		M_TOOL.MARKER -> Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
		M_TOOL.PATH -> Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
		else -> Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
	    }
	}
    }


    var l = ArrayList<Long>()
    var l2 = ArrayList<Long>()

    private fun demo() {
	var g2 = graphics2D
	g2.drawString("123", 10, 50)
	g2.dispose()
	CoroutineScope(Dispatchers.Default).launch {
		val ct0 = System.currentTimeMillis()
		l = ArrayList<Long>()
		l2 = ArrayList<Long>()

		for (i in 0..<10_000) {
		    launch {
			val ct1 = System.currentTimeMillis()
			val g2 = graphics2D
			l.add(ct1 - ct0)
			delay(SundryUtils.rand(1000).toLong())
			//g2.color = Color(SundryUtils.rand(256), SundryUtils.rand(256), SundryUtils.rand(256))
			//g2.drawRect(SundryUtils.rand(300), SundryUtils.rand(300), SundryUtils.rand(100), SundryUtils.rand(100))
			g2.color = Color(SundryUtils.rand(256), SundryUtils.rand(256), SundryUtils.rand(256))
			g2.drawString("" + i, 10 + SundryUtils.rand(400), 10 + SundryUtils.rand(300))
			g2.dispose()
			val ct2 = System.currentTimeMillis()
			l2.add(ct2 - ct1)
		    }
		}
	}
	g2 = graphics2D
	g2.drawString("_123_", 60, 50)
	g2.dispose()
    }

    internal inner class Mouse2(anim_canvas: AnimCanvas) : Mouse(anim_canvas) {
	override fun mousePressed(e: MouseEvent) {
	    if (big_button_text != null) big_button_text = null
	}

	override fun mouseMoved(e: MouseEvent) {}
	override fun mouseDragged(e: MouseEvent) {}
	override fun mouseReleased(e: MouseEvent) {}
    }

    var pa_prop: PathProperties? = null
    override fun processKeyEvent(ke: KeyEvent) {
	super.processKeyEvent(ke)
	Log.getLogger().info(":--: AnimCanvas:KEYEVENT $ke")
	if (ke.id == KeyEvent.KEY_PRESSED) {
	    if (ke.keyCode == KeyEvent.VK_SPACE) {
		if (big_button_text != null) big_button_text = null
		Log.getLogger().info(":--: " + "SPACE")
	    }
	    if (ke.keyCode == KeyEvent.VK_ENTER) {
		if (big_button_text != null) big_button_text = null
		Log.getLogger().info(":--: " + "ENTER")
	    }
	    if (ke.keyCode == KeyEvent.VK_LEFT) {
		if (trigger_left) {
		    if (big_button_text != null) big_button_text = null
		    Log.getLogger().info(":--: " + "LEFT")
		}
		trigger_left = false
	    }
	    if (ke.keyCode == KeyEvent.VK_UP) {
		if (trigger_up) Log.getLogger().info(":--: " + "UP")
		trigger_up = false
	    }
	}
    }

    fun popup_maction(e: MouseEvent, pa: Path?) {
	val choice = arrayOf("path Properties", "", "Cancel")
	val pop = Popup(this)
	pop.popup("Marker", choice, e.x, e.y) { ev ->
	    if (ev.actionCommand == "0") {
		val owner = topLevelAncestor as JFrame
		if (pa_prop == null) pa_prop = PathProperties(owner)
		pa_prop!!.setObject(pa)
		pa_prop!!.isVisible = true
	    }
	}
    }

    val fit: Double
	get() {
	    val im = imageBackground ?: return 1.0
	    val w = im.getWidth(null)
	    val h = im.getHeight(null)
	    val cw = width
	    val ch = height
	    val fx = w.toDouble() / cw
	    val fy = h.toDouble() / ch
	    return Math.max(fx, fy)
	}

    fun centerBackground() {
	val im = imageBackground ?: return
	//	OmegaContext.sout_log.getLogger().info(":--: " + "centerBackground " + im);
	val w = im.getWidth(null)
	val h = im.getHeight(null)
	var cw = width
	var ch = height
	val ww = sca * w
	val hh = sca * h

//	OmegaContext.sout_log.getLogger().info(":--: " + "CALC " + cw + ' ' + w + ' ' + ww);
//	OmegaContext.sout_log.getLogger().info(":--: " + "CALC " + ch + ' ' + h + ' ' + hh);
	if (cw == 0 && ch == 0) {
	    cw = ww.toInt()
	    ch = hh.toInt()
	}
	offs_x = ((cw - ww) / 2).toInt().toDouble()
	offs_y = ((ch - hh) / 2).toInt().toDouble()
	offs_x += offs_w
	offs_y += offs_h
    }

    fun offCenterBackground() {
	val im = imageBackground ?: return
	//	OmegaContext.sout_log.getLogger().info(":--: " + "offCenterBackground " + im);
	offs_x = 10000.0
	offs_y = 10000.0
	repaint()
    }

    fun createNewPath() {
	val nid = a_ctxt.mtl!!.freeTLIndex
	if (nid == -1) {
	    return
	}
	var p_p: Point2D?
	val pa = Path(
		nid,
		Point2D.Double(10.0, nid * 5 + 10.0).also {
		    p_p = it
		},
		Point2D.Double(300.0, nid * 5 + 10.0)
	)
	ap.add(pa)
	ap.deselectAll(graphics2D)
	val prb = ap.findNearest(p_p)
	prb!!.seg!!.path!!.selected = true
	//  				if ( pa_prop != null )
//  				    pa_prop.setObject(prb.seg.path);
	prb.seg!!.selectedPoint = prb.sel
	prb.seg!!.path!!.draw(graphics2D)
	ae!!.selectTimeLine(prb.seg!!.path!!)
	if (prb.seg == prb.seg!!.path!!.getSq(0) ||
		prb.seg == prb.seg!!.path!!.getSq(prb.seg!!.path!!.sqN - 1)
	) ae!!.toolbar_cmd!!.enable_path(1) else ae!!.toolbar_cmd!!.enable_path(2)
	selected_prb = prb
	repaint()
	val len = pa.length.toInt()
	val tl = TimeLine(nid, 200, 5 * len)
	tl.addMarker(TimeMarker.BEGIN, -tl.offset + 1)
	tl.addMarker(TimeMarker.END, TimeLinePanel.playEnd - tl.offset)
	a_ctxt.mtl!!.addTimeLine(tl)
	ae!!.tlc!!.repaint()
	val act = AnimContext.ae!!.cabaret_panel!!.getActorInPanel(nid)
	allgim[act!!.gimae] = nid
	ae!!.isDirty = true
    }

    val isCanvasNormal: Boolean
	get() = //  	    offs_x == 0 &&
		//  	    offs_y == 0 &&
	    offs_w == 0.0 && offs_h == 0.0 && sca == 1.0

    inner class GEL : GenericEventListener, ToolExecute {
	override fun execute(id: String?) {
	    perform(id!!, 1.0)
	}

	override fun genericEvent(gev: GenericEvent?, a: Any?) {
	    val d = a as Double
	    perform(gev!!.id, d)
	}

	fun perform(cmd: String, d: Double) {
	    when (cmd) {
		"left" -> {
		    offs_w -= if (sca > 1.0) d * 100 else sca * d * 100
		    centerBackground()
		    repaint()
		}
		"right" -> {
		    offs_w += if (sca > 1.0) d * 100 else sca * d * 100
		    centerBackground()
		    repaint()
		}
		"up" -> {
		    offs_h -= if (sca > 1.0) d * 100 else sca * d * 100
		    centerBackground()
		    repaint()
		}
		"down" -> {
		    offs_h += if (sca > 1.0) d * 100 else sca * d * 100
		    centerBackground()
		    repaint()
		}
		"upper_left" -> {
		    offs_w = 0.0
		    offs_h = 0.0
		    //		offs_x = offs_y = 0;
		    sca = 1.0
		    centerBackground()
		    repaint()
		}
		"fit" -> {
		    offs_w = 0.0
		    offs_h = 0.0
		    sca = 1.0
		    val fd: Double = fit * 1.1
		    sca /= d * fd
		    offs_w /= d * fd
		    offs_h /= d * fd
		    centerBackground()
		    repaint()
		}
		"bigger" -> {
		    sca *= d * 1.41
		    offs_w *= d * 1.41
		    offs_h *= d * 1.41
		    centerBackground()
		    repaint()
		}
		"smaller" -> {
		    sca /= d * 1.41
		    offs_w /= d * 1.41
		    offs_h /= d * 1.41
		    centerBackground()
		    repaint()
		}
		"path_tool" -> {
		    m!!.setM_Tool(M_TOOL.PATH)
		    repaint()
		}
		"im_tool" -> {
		    m!!.setM_Tool(M_TOOL.IMAGE)
		    repaint()
		}
		"hideActor" -> {
		    repaint()
		}
		"select_path" -> {
		    m!!.setM_Tool(M_TOOL.PATH)
		    repaint()
		}
		"select_image" -> {
		    m!!.setM_Tool(M_TOOL.IMAGE)
		    repaint()
		}
		"path_create" -> {
		    if (a_ctxt.mtl!!.freeTLIndex == -1) {
			JOptionPane.showMessageDialog(
			    this@AnimCanvas,
			    t("Can't create path, max is " + OmegaConfig.TIMELINES_N),
			    "Omega",
			    JOptionPane.INFORMATION_MESSAGE
			)
		    } else {
			createNewPath()
		    }
		}
		"path_duplicate" -> {
		    if (a_ctxt.mtl!!.freeTLIndex == -1) {
			JOptionPane.showMessageDialog(
			    this@AnimCanvas,
			    t("Can't create path, max is " + OmegaConfig.TIMELINES_N),
			    "Omega",
			    JOptionPane.INFORMATION_MESSAGE
			)
		    } else {
			m!!.setM_Tool(M_TOOL.IMAGE, MT.VOID)
			if (selected_prb != null) {
			    val nid = a_ctxt.mtl!!.freeTLIndex
			    if (nid == -1) {
				return
			    }
			    val pa_src = selected_prb!!.seg!!.path
			    val pa_new = Path(nid, pa_src!!, pa_offs, pa_offs)
			    pa_offs += 10
			    pa_offs %= 50
			    if (pa_offs == 0) pa_offs = 10
			    ap.add(pa_new)
			    repaint()
			    val src_nid = selected_prb!!.seg!!.path!!.nid
			    //			OmegaContext.sout_log.getLogger().info(":--: " + "src " + src_nid + ' ' + nid);
			    val tl_src = a_ctxt.mtl!!.getTimeLine(src_nid)!!
			    val tl = TimeLine(nid, tl_src)
			    a_ctxt.mtl!!.addTimeLine(tl)
			    ae!!.tlc!!.repaint()
			    ae!!.isDirty = true
			}
		    }
		}
		"path_delete_all" -> {
		    m!!.setM_Tool(M_TOOL.IMAGE, MT.VOID)
		    if (selected_prb != null) {
			val pa_src = selected_prb!!.seg!!.path
			val src_nid = pa_src!!.nid
			deleteAllNid(src_nid)
			ae!!.isDirty = true
		    }
		}
		"path_extend" -> {
		    if (selected_prb != null) {
			m!!.setM_Tool(M_TOOL.PATH, MT.EXTEND)
			m!!.stack = Stack<Any?>()
			ae!!.isDirty = true
		    }
		}
		"path_split" -> {
		    if (selected_prb != null) {
			selected_prb!!.seg!!.path!!.splitSegment()
			ae!!.isDirty = true
			repaint()
		    }
		}
		"path_delete" -> {
		    if (selected_prb != null) {
			selected_prb!!.seg!!.path!!.removeSegment()
			ae!!.isDirty = true
			repaint()
		    }
		}
	    }
	}
    }

    fun setSelectedPath(tl_nid: Int, pa: Path) {
	if (pa_prop != null) pa_prop!!.setObject(pa)
	pa.selected = true
	val prb = ap.findNearest(pa.firstPoint)
	selected_prb = prb
	repaint()
    }

    constructor(arun: AnimRuntime?, a_ctxt: AnimContext) {
	this.arun = arun
	ae = null
	this.a_ctxt = a_ctxt
	a_ctxt.anim_canvas = this
	cab = Cabaret(a_ctxt)
	init()
    }

    constructor(ae: AnimEditor?, a_ctxt: AnimContext) {
	arun = null
	this.ae = ae
	this.a_ctxt = a_ctxt
	a_ctxt.anim_canvas = this
	layout = null
	gel = GEL()
	gem = GenericEventManager()
	cab = Cabaret(a_ctxt)
	init()
	//	OmegaContext.sout_log.getLogger().info(":--: " + "AnimCanvas(ae) created " + this);
    }

    fun init() {
	initColors()
	addKeyListener(object : KeyAdapter() {
	    override fun keyTyped(e: KeyEvent) {
		Log.getLogger().info(":--: AnimCanvas:KeyAd $e")
	    }
	})
	if (ae == null) {
	    m = Mouse2(this)
	    offs_w = 0.0
	    offs_h = 0.0
	    offs_x = 0.0
	    offs_y = 0.0
	    sca = 1.0
	    repaint()
	}
	if (ae != null) {
	    m = Mouse(this)
	    key = Key(this)
	    gem!!.addGenericEventListener(gel!!)
	    AnimContext.ae!!.cabaret_panel!!.addChangeListener { ev ->
		val cabp = ev.source as CabaretPanel
		for (i in 0 until OmegaConfig.TIMELINES_N) {
		    val act = cabp.getActorInPanel(i)
		    var gim: GImAE? = null
		    if (act != null) gim = act.gimae
		    allgim[gim] = i
		    // gim.nid = i;
		}
	    }
	    createDefaultActors()
	}
    }

    val toolExecute: ToolExecute?
	get() = gel

    private fun initBG() {
	setBackground("default/omega_splash.gif")
	centerBackground()
	repaint()
    }

    fun initPlay() {
	super.initPlay(ap)
	trigger_up = true
	trigger_left = true
    }

    val lessonId_Actors: Array<String>
	get() = cab.lessonId

    fun findActorByLessonId(s: String?): GImAE? {
	return cab.findActorByLessonId(s!!)
    }

    fun findActorByNId(nid: Int): Actor? {
	return cab.getActor(nid)
    }

    fun findTimeLineNidByLessonId(s: String?): Int {
	return a_ctxt.mtl!!.getNid(s!!)
    }

    fun deleteAllNid(nid: Int) {
	if (JOptionPane.showConfirmDialog(
			AnimContext.ae,
			t("Delete whole path and timeline no") + ' ' +
				(nid + 1) + "?",
			"Omega",
			JOptionPane.YES_NO_OPTION
		) == 0
	) {
	    allgim[null] = nid
	    ap.removePath(nid)
	    selected_prb = null
	    repaint()
	    a_ctxt.mtl!!.removeTimeLine(nid)
	    ae!!.tlc!!.repaint()
	}
    }

    fun bindAllNoActor() {
	(0..< OmegaConfig.TIMELINES_N).forEach {
	    val tl = a_ctxt.mtl!!.getTimeLine(it)
	    if (tl != null) {
		val lid = tl.lessonId
		if (lid != null && lid.length > 0) {
		    bindNoActorOnTL(tl.nid)
		}
	    }
	}
    }

    fun bindActor(actor_lid: String?, timeline_lid: String?): Boolean {
	var gae: GImAE? = null
	if (actor_lid != null) gae = findActorByLessonId(actor_lid)
	val tl_nid = findTimeLineNidByLessonId(timeline_lid)
	if (tl_nid != -1) {
	    bindActorOnTL(tl_nid, gae)
	    return true
	}
	return false
    }

    fun bindAllStatistActor() {
	(0..< OmegaConfig.TIMELINES_N).forEach {
	    val tl = a_ctxt.mtl!!.getTimeLine(it)
	    if (tl != null) {
		val lid = tl.lessonId
		if (lid != null && lid.length == 0) {
		    val act = findActorByNId(it)
		    if (act != null) bindActorOnTL(tl.nid, act.gimae)
		    //		    OmegaContext.sout_log.getLogger().info(":--: " + "--- statist actor " + tl.nid + ' ' + act);
		}
	    }
	}
    }

    fun deleteActor(ix: Int) {
	val nid: Int
	nid = if (AnimContext.ae != null) AnimContext.ae!!.cabaret_panel!!.setActorInPanelAbs(null, ix) else ix
	if (nid >= 0 && nid < OmegaConfig.TIMELINES_N) {
	    allgim[null] = nid
	}
    }

    fun loadActor(ix: Int, fn: String?): Actor {
	val act = cab.createActor(ix, fn!!, null)
	val nid: Int
	nid = if (AnimContext.ae != null) AnimContext.ae!!.cabaret_panel!!.setActorInPanelAbs(act, ix) else ix
	if (nid >= 0 && nid < OmegaConfig.TIMELINES_N) allgim[act.gimae] = nid
	return act
    }

    fun bindActorOnTL(tl_nid: Int, gimae: GImAE?): Actor? {
	if (gimae == null) return null
	if (tl_nid < OmegaConfig.TIMELINES_N) {
	    val gim = GImAE(this, gimae, tl_nid) // make a ghost
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "bound actor " + tl_nid + ' ' + gim);
	    val act = Actor(a_ctxt, gim)
	    actA_animated[tl_nid] = act
	    allgim[gim] = tl_nid
	    return act
	}
	return null
    }

    fun bindNoActorOnTL(tl_nid: Int): Actor? {
	if (tl_nid < OmegaConfig.TIMELINES_N) {
//	    OmegaContext.sout_log.getLogger().info(":--: " + "bound no actor " + tl_nid);
	    actA_animated[tl_nid] = null
	    allgim[null] = tl_nid
	    return null
	}
	return null
    }

    fun getActor(nid: Int): Actor? {
	return if (AnimContext.ae != null) AnimContext.ae!!.cabaret_panel!!.getActorInPanel(nid) else cab.getActor(nid)
    }

    fun getAnimatedActor(nid: Int): Actor? {
	var a: Actor? = null
	a = if (AnimContext.ae != null) AnimContext.ae!!.cabaret_panel!!.getActorInPanel(nid) else actA_animated[nid]
	//log	OmegaContext.sout_log.getLogger().info(":--: " + "getAnimatedActor -> " + nid + ' ' + a + ' ' + SundryUtils.a2s(actA_animated));
	return a
    }

    fun getAnimatedActor(sid: String?): Actor? {
	val nid = findTimeLineNidByLessonId(sid)
	//log	OmegaContext.sout_log.getLogger().info(":--: " + ">>>>>>>> getting animact " + sid + ' ' + nid);
	return getAnimatedActor(nid)
    }

    fun hideActors() {
	allgim.hideActors()
    }

    private fun createDefaultActors() {
	if (AnimContext.ae != null) {
	    AnimContext.ae!!.cabaret_panel!!.resetCabaretOrder()
	    if (AnimContext.ae != null) {
		for (i in cab.actorNum() - 1 downTo 0) loadActor(i, "default/default_actor_$i.gif")
	    } else {
	    }
	}
    }

    var visibilityMode = 0x30
    fun setVisibilityMode_(cmd: Int) {
	when (cmd) {
	    SHOW_PATH -> visibilityMode = visibilityMode or (SHOW_PATH and 0xf0)
	    HIDE_PATH -> visibilityMode = visibilityMode and (SHOW_PATH and 0xf0).inv()
	    SHOW_ACTOR -> visibilityMode = visibilityMode or (SHOW_ACTOR and 0xf0)
	    HIDE_ACTOR -> visibilityMode = visibilityMode and (SHOW_ACTOR and 0xf0).inv()
	}
    }

    fun getVisibilityMode_(cmd: Int): Boolean {
	return if (cmd and 1 == 1) visibilityMode and cmd and 0xf0 != 0 else visibilityMode and cmd and 0xf0 == 0
    }

    val graphics2D: Graphics2D
	get() {
	    val g2 = graphics as Graphics2D
	    val at = g2.transform
	    if (m != null) {
		at.translate(offs_x, offs_y)
		at.scale(sca, sca)
	    }
	    g2.transform = at
	    return g2
	}
    var big_button_text: String? = null
    var trigger_up = false
    var trigger_left = false
    fun setBigButtonText(s: String?) {
	if (s == null) return
	if (s.length == 0) big_button_text = null else {
	    big_button_text = s
	    trigger_up = true
	    trigger_left = true
	}
	repaint()
    }

    class MsgItem2(var title: String, var text: String?)

    val caW: Int
	get() = width
    val caH: Int
	get() = height

    fun gX(f: Double): Int {
	return (f * caW).toInt()
    }

    fun gY(f: Double): Int {
	return (f * caH).toInt()
    }

    val titleFont: Font
	get() {
	    return Font("Arial", Font.PLAIN, gX(0.024))
	}

    fun getStringWidth(g2: Graphics2D, fo: Font, s: String?): Int {
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	val frc = g2.fontRenderContext
	val r = fo.getStringBounds(s, frc)
	return r.width.toInt()
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

    fun getColor(id: String, def: Color): Color {
	val cols = colors[id] ?: return def
	return if (null != cols.color) {
	    cols.color
	} else def
    }

    var isMsg = false

    inner class MsgDialog {
	var msg_item: MsgItem2? = null
	fun show(msg: MsgItem2?) {
	    set(msg)
	    while (isMsg) m_sleep(200)
	}

	fun set(msg: MsgItem2?) {
	    isMsg = msg != null
	    val g2: Graphics2D = graphics2D
	    draw(g2)
	    msg_item = msg
	    val w = gX(0.5)
	    val h = gY(0.25)
	    val x = gX(0.25)
	    val y = gY(0.2)
	    if (msg == null) repaint(x - 5, y - 5, w + 15, h + 15)
	}

	fun draw(g2: Graphics2D) {
	    if (msg_item == null) return
	    var txtH = gY(0.037)
	    var fo = Font("Arial", Font.PLAIN, txtH)
	    var sw = getStringWidth(g2, fo, msg_item!!.text)
	    val ct0 = ct()
	    while (sw > gX(0.94)) {
		txtH = (txtH * 0.98).toInt()
		fo = Font("Arial", Font.PLAIN, txtH)
		val o_sw = sw
		sw = getStringWidth(g2, fo, msg_item!!.text)
		//		OmegaContext.sout_log.getLogger().info(":--: " + "recalc sw " + o_sw + ' ' + sw + ' ' + txtH);
	    }
	    val ct1 = ct()
	    Log.getLogger().info(":--: " + "--> " + (ct1 - ct0))
	    val w = sw + 10 + gX(0.03)
	    val h = gY(0.06)
	    val x = gX(0.5) - w / 2
	    val y = gY(0.88)
	    val r = gX(0.02)
	    val col = getColor("sn_bg", Color(0xe5, 0xe5, 0xe5))
	    OmegaContext.COLOR_WARP = col
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
	    if (false) {
		// titlebar
		val th = gY(0.026)
		g2.color = Color(88, 88, 88)
		g2.clip = fr
		g2.fill(Rectangle2D.Double(x.toDouble(), y.toDouble(), w.toDouble(), th.toDouble()))
	    }
	    g2.setClip(0, 0, 10000, 10000) //	    g2.setClip(fr);
	    g2.color = getColor("sn_tx", Color.black)
	    OmegaContext.COLOR_TEXT_WARP = getColor("sn_tx", Color.black)
	    g2.font = fo
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    g2.drawString(
		    msg_item!!.text,
		    x + w / 2 - sw / 2,
		    y + h - 2 * txtH / 5
	    )
	    g2.color = col
	    g2.font = titleFont
	    g2.drawString(msg_item!!.title, x + 1 * w / 10, (y + gY(0.042)))
	    val a = Area()
	    a.add(Area(Rectangle2D.Double(0.0, 0.0, 10000.0, 10000.0)))
	    a.subtract(Area(fr))
	    g2.clip = a
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f)
	    g2.color = Color(15, 15, 15)
	    (0.. 6).forEach {
		val frs: RoundRectangle2D = RoundRectangle2D.Double(
			(x + 10 - it).toDouble(),
			(y + 10 - it).toDouble(),
			w.toDouble(),
			h.toDouble(),
			r.toDouble(),
			r.toDouble()
		)
		g2.fill(frs)
	    }
	    val stroke = BasicStroke(caH / 200f)
	    g2.stroke = stroke
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    g2.color = getColor("sn_fr", Color(15, 15, 15))
	    g2.setClip(0, 0, 10000, 10000)
	    g2.draw(fr)
	}
    }

    var msg_dlg: MsgDialog = MsgDialog()
    fun showMsg(mi: MsgItem2?) {
	msg_dlg.set(mi)
    }

    fun hideMsg() {
	msg_dlg.set(null)
    }

    fun waitBigButtonText(myra: Runnable?): String {
	var myra = myra
	showMsg(MsgItem2("", big_button_text))
	repaint()
	while (big_button_text != null) {
	    m_sleep(200)
	    //	    SundryUtils.pe_("E");
	    if ("up" == endCode) {
		myra?.run()
		myra = null
	    }
	}
	hideMsg()
	return endCode
    }

    override fun paintComponent(g: Graphics) {
	val g2 = g as Graphics2D
	if (HIDDEN) {
	    g2.color = background_color
	    g2.fillRect(0, 0, 3000, 2000)
	    return
	}
	g2.color = background_color
	g2.fillRect(0, 0, 3000, 2000)
	val at0 = g2.transform
	val at = g2.transform

//	OmegaContext.sout_log.getLogger().info(":--: " + "trans " + offs_x + ' ' + offs_y);
	at.translate(offs_x, offs_y)
	at.scale(sca, sca)
	g2.transform = at
	if (hidden_) return
	super.paintComponent(g)
	if (getVisibilityMode_(SHOW_PATH)) {
	    if (ae != null) ap.redraw(g2)
	}
	g2.transform = at0
	if (false) if (big_button_text != null && big_button_text!!.length > 0) {
	    val ybot = height - 100
	    val h = 60
	    val x = 20
	    val w = width - x - x
	    g2.color = Color(211, 211, 190)
	    g2.fillRect(x, ybot, w, h)
	    g2.color = Color(0, 0, 0)
	    g2.drawRect(x, ybot, w, h)
	    g2.font = Font("Arial", Font.PLAIN, 24)
	    g2.drawString(big_button_text, x + 40, ybot + 25)
	}
	//	g2.setTransform(at0);
	msg_dlg.draw(g2)
    }

    fun initNew() {
	ap = AllPath()
	selected_prb = null
	selected_mark = null
	press_p = null
	if (AnimContext.ae != null) {
	    initBG()
	    createDefaultActors()
	}
	repaint()
    }

    //      public void save(XML_PW xmlpw) {
    //      Element mel = new Element("AnimCanvas");
    //  	mel.add(super.getElement());
    //  	xmlpw.push(mel);
    //  	Element ael = ap.getElement();
    //  	xmlpw.put(ael);
    //  	Element verb_el = new Element("lesson");
    //  	String verb = getLessonVerb();
    //  	if ( verb != null && verb.length() > 0 ) {
    //  	    verb_el.addAttr("verb", verb);
    //  	    xmlpw.put(verb_el);
    //  	}
    //  	Element aacel = new Element("AllActors");
    //  	for(int i = 0; i < cab.actorNum(); i++) {
    //  	    Actor act = a_ctxt.ae.cabaret_panel.getActorInPanelAbs(i);
    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "saving " + i + ' ' + act);
    //  	    if ( act != null ) {
    //  		Element acel = act.getElement();
    //  		acel.addAttr("nid", "" + i);
    //  		aacel.add(acel);
    //  	    }
    //  	}
    //  	Element awel = new Element("AllWings");
    //  	for(int i = 0; i < a_ctxt.ae.wings_panel.wingA.arr.length; i++) {
    //  	    if ( a_ctxt.ae.wings_panel.getWing(i) != null ) {
    //  		Element wel = a_ctxt.ae.wings_panel.getWing(i).getElement();
    //  		wel.addAttr("nid", "" + i);
    //  		awel.add(wel);
    //  	    }
    //  	}
    //  	xmlpw.put(aacel);
    //  	xmlpw.put(awel);
    //  	xmlpw.pop();
    //      }
    fun fillElement(el: Element) {
	val mel = Element("AnimCanvas")
	mel.add(super.element)
	val ael = ap.element
	mel.add(ael)
	val verb_el = Element("lesson")
	val verb = lessonVerb
	if (verb != null && verb.length > 0) {
	    verb_el.addAttr("verb", verb)
	    mel.add(verb_el)
	}
	val aacel = Element("AllActors")
	(0 ..< cab.actorNum()).forEach {
	    val act = AnimContext.ae!!.cabaret_panel!!.getActorInPanelAbs(i)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "saving " + i + ' ' + act);
	    if (act != null) {
		val acel = act.element
		acel.addAttr("nid", "" + it)
		aacel.add(acel)
	    }
	}
	val awel = Element("AllWings")
	for (i in AnimContext.ae!!.wings_panel!!.wingA.arr.indices) {
	    if (AnimContext.ae!!.wings_panel!!.getWing(i) != null) {
		val wel = AnimContext.ae!!.wings_panel!!.getWing(i)?.element
		wel?.addAttr("nid", "" + i)
		awel.add(wel)
	    }
	}
	mel.add(aacel)
	mel.add(awel)
	el.add(mel)
    }

    override fun load(root: Element) {
	if (root == null) return
	var eel = root.findElement("AnimCanvas", 0)
	if (eel == null) eel = root.findElement("EditCanvas", 0)
	if (eel != null) {
	    super.load(eel)
	}
	ap.load(eel!!)
	val lel = eel.findElement("lesson", 0)
	if (lel != null) {
	    val verb = lel.findAttr("verb")
	    lessonVerb = verb!!
	}
	val aael = eel.findElement("AllActors", 0)
	if (aael != null) {
	    createDefaultActors()
	    (0 ..< OmegaConfig.CABARET_ACTOR_N).forEach {
		val acel = aael.findElement("Actor", it)
		if (acel != null) {
		    val ix = acel.findAttr("nid")!!.toInt()
		    val fn = acel.findAttr("name")
		    val id = acel.findAttr("lesson_id")
		    var var1 = acel.findAttr("var1")
		    if (var1 == null) var1 = ""
		    var var2 = acel.findAttr("var2")
		    if (var2 == null) var2 = ""
		    var var3 = acel.findAttr("var3")
		    if (var3 == null) var3 = ""
		    var sc = acel.findAttr("prim_scale")
		    var mi = acel.findAttr("prim_mirror")
		    if (sc == null) sc = "1.0"
		    if (mi == null) mi = "0"
		    var da: DoubleArray
		    val act = loadActor(ix, fn)

//log		    OmegaContext.sout_log.getLogger().info(":--: " + "!!!!!!!!!!! actor loaded " + act + ' ' + var1 + ' ' + var2 + ' ' + var3 + '.');
		    var hs = acel.findAttr("hotspot")
		    if (hs != null) {
			act.gimae.setHotSpotIx(0, hs)
		    }
		    for (ih in 0 until Hotspot.HOTSPOT_N) { // have break
			hs = acel.findAttr("hotspot_" + Hotspot.getType(ih))
			if (hs == null) {
			    (1 ..< Hotspot.HOTSPOT_N).forEach {ih2 -> act.gimae.setHotSpotIxSame(ih2) }
			    break
			}
			act.gimae.setHotSpotIx(ih, hs)
		    }
		    if (id != null) act.gimae.lessonId = id
		    act.gimae.setVariable(1, var1)
		    act.gimae.setVariable(2, var2)
		    act.gimae.setVariable(3, var3)
		    val scd = tD(sc)
		    act.gimae.primScale = scd
		    act.gimae.primMirror = mi!!.toInt()
		}
	    }
	}
	val awel = eel.findElement("AllWings", 0)
	if (awel != null) {
	    (0 ..< OmegaConfig.WINGS_N).forEach {
		val wel = awel.findElement("Wing", it)
		if (wel != null) {
		    val ix = wel.findAttr("nid")!!.toInt()
		    val fn = wel.findAttr("name")
		    var la = wel.findAttr("layer")
		    var mi = wel.findAttr("mirror")
		    if (mi == null) mi = "0"
		    val sc = wel.findAttr("scale")
		    var po = wel.findAttr("position")
		    if (po == null) po = "0.5 0.5"
		    if (la == null) la = "1"
		    val sa = split(po, " ,;")
		    val d1 = tD(sa[0])
		    val d2 = tD(sa[1])
		    val da = doubleArrayOf(d1, d2)
		    var sc_d = 1.0
		    if (sc != null) sc_d = tD(sc)
		    val w = createWing(
			    fn, d1.toInt(), d2.toInt(), la.toInt(),
			    sc_d, mi!!.toInt()
		    )
		    val wing_nid = w.ord
		    if (AnimContext.ae != null) AnimContext.ae!!.wings_panel!!.setWing(w, wing_nid)
		    resetBackground()
		}
	    }
	}
	repaint()
    }

    val endCode: String
	get() {
	    if (trigger_left == false) return "left"
	    return if (trigger_up == false) "up" else "normal"
	}

    companion object {
	const val SHOW_PATH = 0x1011
	const val HIDE_PATH = 0x1010
	const val SHOW_ACTOR = 0x1021
	const val HIDE_ACTOR = 0x1020
    }
}
