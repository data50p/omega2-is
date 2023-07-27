package com.femtioprocent.omega.anim.panels.cabaret

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.anim.appl.AnimEditor
import com.femtioprocent.omega.graphic.render.Wing
import com.femtioprocent.omega.util.Log
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.MouseInputAdapter

class WingsPanel(var ae: AnimEditor) : JPanel() {
    val IMSIZE = 64
    val WING_N = OmegaConfig.WINGS_N
    val EMPTY = -1
    var wingA: WingA
    var prop: WingsProperties? = null
    private val m: Mouse
    private val cLiLi: MutableList<ChangeListener> = ArrayList()
    private var selected = -1
    private var fld_state_id = false
    var selected_wing_ix = -1

    inner class WingA internal constructor() {
	inner class WingItem internal constructor(var ord: Int) {
	    var wing: Wing? = null
	    var nid: Int

	    init {
		nid = EMPTY
	    }
	}

	var arr: Array<WingItem?>
	var selected_src_fld: Int
	var selected_dst_fld: Int

	init {
	    arr = arrayOfNulls(WING_N)
	    for (i in arr.indices) {
		arr[i] = WingItem(i)
	    }
	    selected_dst_fld = EMPTY
	    selected_src_fld = selected_dst_fld
	}

	fun getIm(ix: Int): Image? {
	    return try {
		arr[ix]!!.wing!!.im
	    } catch (ex: NullPointerException) {
		null
	    }
	}

	fun findFree(): Int {
	    for (i in arr.indices) {
		if (arr[i]!!.nid == EMPTY) return i
	    }
	    return -1
	}

	fun findOrd(nid: Int): Int {
	    for (i in arr.indices) {
		if (arr[i]!!.nid == nid) return i
	    }
	    return -1
	}
    }

    fun setPropTarget(ixx: Int) {
	if (prop != null) prop!!.setTarget(wingA.arr[ixx]!!.wing, ixx)
	selected = ixx
	wingA.selected_dst_fld = ixx
	wingA.selected_src_fld = ixx
	repaint()
    }

    fun addChangeListener(li: ChangeListener) {
	cLiLi.add(li)
    }

    fun fireStateChange() {
	val it: Iterator<ChangeListener> = cLiLi.iterator()
	while (it.hasNext()) {
	    val cli = it.next()
	    cli.stateChanged(ChangeEvent(this))
	}
    }

    internal inner class Mouse : MouseInputAdapter() {
	var mpress_p: Point2D? = null

	init {
	    addMouseListener(this)
	    addMouseMotionListener(this)
	}

	private fun fldState(e: MouseEvent) {
	    fld_state_id = if (e.y > IMSIZE &&
		    e.y < IMSIZE + 20) true else false
	}

	override fun mousePressed(e: MouseEvent) {
	    fldState(e)
	    val pt = e.isPopupTrigger
	    val ixx = e.x / IMSIZE
	    if (ixx >= WING_N) return
	    val ca = ae.a_ctxt!!.anim_canvas
	    ca!!.traceWing(ixx, 0.0, 0.0, false)
	    selected_wing_ix = ixx
	    if (pt) {
		popup(ixx)
		return
	    }
	    setPropTarget(ixx)
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    if (e.y < IMSIZE) {
	    } else {
		wingA.selected_dst_fld = ixx
		wingA.selected_src_fld = ixx
	    }
	    repaint()
	}

	override fun mouseMoved(e: MouseEvent) {}
	override fun mouseDragged(e: MouseEvent) {
	    val ixx = e.x / IMSIZE
	    if (ixx >= WING_N) return
	    fldState(e)
	    wingA.selected_dst_fld = ixx
	    if (prop != null) prop!!.cancelPos()
	    repaint()
	}

	override fun mouseReleased(e: MouseEvent) {
	    val pt = e.isPopupTrigger
	    fldState(e)
	    if (pt) {
		val ixx = e.x / IMSIZE
		if (ixx >= WING_N) return
		popup(ixx)
		return
	    }
	    val ixx = e.x / IMSIZE
	    if (ixx >= WING_N) return
	    if (fld_state_id && wingA.selected_src_fld != EMPTY && wingA.selected_dst_fld != EMPTY && wingA.selected_src_fld != wingA.selected_dst_fld) {
		val nid1 = wingA.arr[wingA.selected_src_fld]!!.nid
		val nid2 = wingA.arr[wingA.selected_dst_fld]!!.nid
		wingA.arr[wingA.selected_src_fld]!!.nid = nid2
		wingA.arr[wingA.selected_dst_fld]!!.nid = nid1
		wingA.selected_src_fld = EMPTY
		wingA.selected_dst_fld = wingA.selected_src_fld
		fireStateChange()
	    } else {
		wingA.selected_src_fld = EMPTY
		wingA.selected_dst_fld = wingA.selected_src_fld
	    }
	    repaint()
	}
    }

    fun popup(ix: Int) {
	if (prop == null) {
	    val owner = topLevelAncestor as JFrame
	    prop = WingsProperties(owner, this)
	}
	prop!!.isVisible = true
	//	prop_pa.setVisible(true);
	setPropTarget(ix)
    }

    fun getWing(nid: Int): Wing? {
	val ix = wingA.findOrd(nid)
	return if (ix >= 0) wingA.arr[ix]!!.wing else null
    }

    fun removeWing(ix: Int) {
	removeAllWings()
	val li = ae.a_ctxt!!.anim_canvas!!.removeWing(ix)
	if (li != null) {
	    var cnt = 0
	    val it = li.iterator()
	    while (it.hasNext()) {
		setWing(it.next(), cnt++)
	    }
	}
	repaint()
    }

    fun removeAllWings() {
	wingA = WingA()
	repaint()
    }

    fun replaceWing(bound_wing_ixx: Int) {
	ae.loadWing()
    }

    fun setWing(wing: Wing?, nid: Int): Int {
	var ix = wingA.findOrd(nid)
	if (ix >= 0) {
	    wingA.arr[ix]!!.wing = wing
	    repaint()
	} else {
	    ix = wingA.findFree()
	    if (ix >= 0) {
		wingA.arr[ix]!!.wing = wing
		wingA.arr[ix]!!.nid = nid
		repaint()
	    } else {
		Log.getLogger().info(":--: " + "NO SLOT")
	    }
	}
	setPropTarget(ix)
	return nid
    }

    inner class MouseCa(var ixx: Int) : MouseInputAdapter() {
	var mpress_p: Point2D? = null
	var p_p: Point2D? = null

	init {
	    val ca = ae.a_ctxt!!.anim_canvas
	    ca!!.traceWing(ixx, 0.0, 0.0, true)
	}

	override fun mousePressed(e: MouseEvent) {
	    val ca = ae.a_ctxt!!.anim_canvas
	    val p = ca!!.scaleEventPos(e)
	    p_p = p // new Point2D.Double(e.getX(), e.getY());
	}

	override fun mouseMoved(e: MouseEvent) {}
	override fun mouseDragged(e: MouseEvent) {
	    val ca = ae.a_ctxt!!.anim_canvas
	    val p = ca!!.scaleEventPos(e)
	    var dx: Double
	    var dy: Double
	    ca.traceWing(ixx,
		    p.getX() - p_p!!.x.also {
			dx = it
		    },
		    p.getY() - p_p!!.y.also {
			dy = it
		    }, true)
	    if (prop != null) prop!!.updPos(wingA.arr[ixx]!!.wing, dx.toInt(), dy.toInt())
	}

	override fun mouseReleased(e: MouseEvent) {
	    off()
	    val ca = ae.a_ctxt!!.anim_canvas
	    //	    ca.traceNoWing();
	    val p = ca!!.scaleEventPos(e)
	    val p_w = wingA.arr[ixx]!!.wing!!.pos
	    val p_n = Point((p_w.getX() + p.getX() - p_p!!.x).toInt(), (p_w.getY() + p.getY() - p_p!!.y).toInt())
	    wingA.arr[ixx]!!.wing!!.pos = p_n
	    ae.a_ctxt!!.anim_canvas!!.resetBackground()
	    ca.traceWing(ixx, 0.0, 0.0, false)
	}
    }

    var mca: MouseCa? = null

    init {
	layout = BorderLayout()
	wingA = WingA()
	minimumSize = Dimension(IMSIZE * WING_N, IMSIZE + 20)
	maximumSize = Dimension(IMSIZE * WING_N, IMSIZE + 20)
	m = Mouse()
    }

    fun on(ixx: Int) {
	val ca = ae.a_ctxt!!.anim_canvas
	mca = MouseCa(ixx)
	ca!!.hook = mca
    }

    fun off() {
	val ca = ae.a_ctxt!!.anim_canvas
	ca!!.hook = null
	mca = null
    }

    fun draw(g2: Graphics2D) {
	g2.color = Color(110, 110, 110)
	g2.fillRect(0, 0, 2000, 1000)
	g2.color = Color(35, 35, 110)
	g2.fillRect(0, 0, IMSIZE * WING_N, IMSIZE)
	for (i in wingA.arr.indices) {
	    if (wingA.arr[i] != null) {
		val at = AffineTransform()
		val nid = wingA.arr[i]!!.nid
		try {
		    val im = wingA.getIm(i)
		    if (im != null) {
			val im_w = im.getWidth(null)
			val im_h = im.getHeight(null)
			val asp_x = im_w / IMSIZE.toDouble()
			val asp_y = im_h / IMSIZE.toDouble()
			val asp = if (asp_x < asp_y) asp_y else asp_x
			at.translate((i * IMSIZE).toDouble(), 0.0)
			val scale = 1.0 / asp
			at.scale(scale, scale)
			g2.drawImage(im, at, null)
			if (nid < OmegaConfig.TIMELINES_N) {
			    val ww = wingA.getIm(i)!!.getWidth(null)
			    val hh = wingA.getIm(i)!!.getHeight(null)
			    var fx: Double
			    var fy: Double
			    if (ww > hh) {
				fx = 1.0
				fy = hh.toDouble() / ww
			    } else {
				fy = 1.0
				fx = ww.toDouble() / hh
			    }
			}
			if (i == selected_wing_ix) {
			    val xx = i * IMSIZE
			    g2.color = Color.yellow
			    g2.drawRect(xx, 0, IMSIZE - 1, IMSIZE - 1)
			}
		    }
		} catch (ex: NullPointerException) {
		}

		// fld id
		val idf = Color(135, 135, 210)
		g2.color = idf
		if (i == wingA.selected_src_fld) g2.color = idf.darker()
		if (i == wingA.selected_dst_fld && fld_state_id) g2.color = idf.brighter()
		g2.fillRect(i * IMSIZE, IMSIZE, IMSIZE, 20)
		g2.color = Color.white
		if (nid >= 0 && nid < OmegaConfig.TIMELINES_N) {
		    val im = wingA.getIm(i)
		    val ID = ""
		    g2.drawString("" + (nid + 1) + ": " + ID,
			    5 + i * IMSIZE,
			    IMSIZE + 16)
		}
		g2.color = Color(35, 35, 110)
		g2.drawLine(i * IMSIZE, IMSIZE, i * IMSIZE, IMSIZE + 20)
	    }
	}
	if (EMPTY != wingA.selected_src_fld &&
		EMPTY != wingA.selected_dst_fld) drawArrFld(g2, wingA.selected_src_fld, wingA.selected_dst_fld)
    }

    private fun drawArrFld(g2: Graphics2D, src: Int, dst: Int) {
	g2.color = Color.black
	val h = IMSIZE + 10
	if (src == dst) {
	    g2.drawLine(src * IMSIZE, h,
		    (dst + 1) * IMSIZE, h)
	    g2.drawLine(dst * IMSIZE, h,
		    dst * IMSIZE + 5, h - 5)
	    g2.drawLine(dst * IMSIZE, h,
		    dst * IMSIZE + 5, h + 5)
	    g2.drawLine((dst + 1) * IMSIZE, h,
		    (dst + 1) * IMSIZE - 5, h - 5)
	    g2.drawLine((dst + 1) * IMSIZE, h,
		    (dst + 1) * IMSIZE - 5, h + 5)
	} else if (src < dst) {
	    g2.drawLine(src * IMSIZE, h,
		    dst * IMSIZE, h)
	    g2.drawLine(dst * IMSIZE, h,
		    dst * IMSIZE - 5, h - 5)
	    g2.drawLine(dst * IMSIZE, h,
		    dst * IMSIZE - 5, h + 5)
	} else {
	    g2.drawLine(src * IMSIZE, h,
		    dst * IMSIZE + 15, h)
	    g2.drawLine(dst * IMSIZE + 15, h,
		    dst * IMSIZE + 15 + 5, h - 5)
	    g2.drawLine(dst * IMSIZE + 15, h,
		    dst * IMSIZE + 15 + 5, h + 5)
	}
    }

    override fun getPreferredSize(): Dimension {
	return Dimension(IMSIZE * WING_N, IMSIZE + 20)
    }

    public override fun paintComponent(g: Graphics) {
	val g2 = g as Graphics2D
	draw(g2)
    }

    fun setSelected(b: Boolean) {
	if (prop != null) prop!!.enableDelete(b)
    }
}
