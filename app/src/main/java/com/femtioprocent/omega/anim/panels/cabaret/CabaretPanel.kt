package com.femtioprocent.omega.anim.panels.cabaret

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.anim.appl.AnimEditor
import com.femtioprocent.omega.anim.cabaret.Actor
import com.femtioprocent.omega.anim.cabaret.Cabaret
import com.femtioprocent.omega.anim.cabaret.GImAE
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.MouseInputAdapter

class CabaretPanel(var ae: AnimEditor?) : JPanel() {
    val IMSIZE = 64
    val ACT_N = OmegaConfig.CABARET_ACTOR_N
    val EMPTY = -1
    var prop: CabaretProperties? = null
    private val m: Mouse
    private val cLiLi: MutableList<ChangeListener> = ArrayList()
    private var selected = 0
    private var fld_state_id = false
    var selected_src_fld = 0
    var selected_dst_fld = 0
    val cab: Cabaret
	get() = ae!!.a_ctxt.anim_canvas!!.cab

    init {
	layout = BorderLayout()

//	actA = new ActA();
	minimumSize = Dimension(IMSIZE * ACT_N, IMSIZE + 20)
	maximumSize = Dimension(IMSIZE * ACT_N, IMSIZE + 20)
	preferredSize = Dimension(IMSIZE * ACT_N, IMSIZE + 20)
	m = Mouse()
    }

    fun replaceActor(ixx: Int) {
	if (ae != null) ae!!.replaceActor(ixx)
    }

    fun deleteActor(ixx: Int) {
	if (ae != null) ae!!.deleteActor(ixx)
    }

    fun getActor(ix: Int): Actor? {
	return cab.getActor(ix)
    }

    fun getGImAE(ix: Int): GImAE? {
	return cab.actA.getGImAE(ix)
    }

    fun setPropTarget(ixx: Int) {
	if (prop != null) prop!!.setTarget(getActor(ixx), ixx)
	if (prop != null) {
	    val tl_nid = cab.getTLnid(ixx)
	    val tl = ae!!.a_ctxt.mtl!!.getTimeLine(tl_nid)
	}
	selected = ixx
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
	    if (ixx >= ACT_N) return
	    if (pt) {
		popup(ixx)
		return
	    }
	    setPropTarget(ixx)
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    if (e.y < IMSIZE) {
	    } else {
		selected_dst_fld = ixx
		selected_src_fld = ixx
	    }
	    repaint()
	}

	override fun mouseMoved(e: MouseEvent) {}
	override fun mouseDragged(e: MouseEvent) {
	    val ixx = e.x / IMSIZE
	    if (ixx >= ACT_N) return
	    fldState(e)
	    selected_dst_fld = ixx
	    repaint()
	}

	override fun mouseReleased(e: MouseEvent) {
	    val pt = e.isPopupTrigger
	    fldState(e)
	    if (pt) {
		val ixx = e.x / IMSIZE
		if (ixx >= ACT_N) return
		popup(ixx)
		return
	    }
	    val ixx = e.x / IMSIZE
	    if (ixx >= ACT_N) return
	    if (fld_state_id && selected_src_fld != EMPTY && selected_dst_fld != EMPTY && selected_src_fld != selected_dst_fld) {
		ae!!.a_ctxt.anim_canvas!!.hideActors()
		val tl_nid1: Int = cab.actA.arr.get(selected_src_fld)!!.tl_nid
		val tl_nid2: Int = cab.actA.arr.get(selected_dst_fld)!!.tl_nid
		cab.actA.arr.get(selected_src_fld)!!.tl_nid = tl_nid2
		cab.actA.arr.get(selected_dst_fld)!!.tl_nid = tl_nid1
		selected_src_fld = EMPTY
		selected_dst_fld = selected_src_fld
		fireStateChange()
	    } else {
		selected_src_fld = EMPTY
		selected_dst_fld = selected_src_fld
	    }
	    repaint()
	}
    }

    fun resetCabaretOrder() {
	selected_src_fld = EMPTY
	selected_dst_fld = selected_src_fld
	cab.newActA()
	fireStateChange()
    }

    fun popup(ix: Int) {
	if (prop == null) {
	    val owner = topLevelAncestor as JFrame
	    prop = CabaretProperties(owner, this)
	    //	    prop_pa = new CabaretPathProperties(owner, this);
	}
	prop!!.isVisible = true
	//	prop_pa.setVisible(true);
	setPropTarget(ix)
    }

    fun getActorInPanel(tl_nid: Int): Actor? {
	val ix = cab.actA.findOrdTL(tl_nid)
	return if (ix >= 0) getActor(ix) else null
    }

    fun getActorInPanelAbs(ix: Int): Actor? {
	return getActor(ix)
    }

    // NOFATAL FIX alloc tl_nid
    fun setActorInPanelAbs(ac: Actor?, ix: Int): Int {
	var ix = ix
	return if (ix >= 0 && ix < cab.actA.arr.size) {
	    val tl_nid = cab.getTLnid(ix) //actA.arr[ix].tl_nid;
	    if (tl_nid == EMPTY) {
		var fix = cab.actA.findOrdTL(ix)
		if (fix == -1) {
		    fix = cab.actA.findFree()
		    if (fix < OmegaConfig.TIMELINES_N) {
			ix = fix
			cab.actA.arr[ix]!!.tl_nid = ix
		    }
		} else ix = fix
	    }
	    cab.actA.arr[ix]!!.ac = ac
	    repaint()
	    setPropTarget(ix)
	    tl_nid
	} else -1
    }

    fun draw(g2: Graphics2D) {
	g2.color = Color(110, 110, 110)
	g2.fillRect(0, 0, 2000, 1000)
	g2.color = Color(35, 35, 110)
	g2.fillRect(0, 0, IMSIZE * ACT_N, IMSIZE)
	for (i in cab.actA.arr.indices) {
	    if (cab.actA.arr[i] != null) {
		val at = AffineTransform()
		val tl_nid = cab.getTLnid(i) // actA.arr[i].tl_nid;
		try {
		    val gimae = getGImAE(i)
		    if (gimae != null) {
			val im = gimae.baseImage
			val im_w = im.getWidth(null)
			val im_h = im.getHeight(null)
			val asp_x = im_w / IMSIZE.toDouble()
			val asp_y = im_h / IMSIZE.toDouble()
			val asp = if (asp_x < asp_y) asp_y else asp_x
			at.translate((i * IMSIZE).toDouble(), 0.0)
			val scale = 1.0 / asp
			at.scale(scale, scale)
			g2.drawImage(im, at, null)
			if (tl_nid < OmegaConfig.TIMELINES_N) {
			    val ww = getGImAE(i)!!.imw
			    val hh = getGImAE(i)!!.imh
			    var fx: Double
			    var fy: Double
			    if (ww > hh) {
				fx = 1.0
				fy = hh.toDouble() / ww
			    } else {
				fy = 1.0
				fx = ww.toDouble() / hh
			    }
			    if (selected == i) {
				val xx = i * IMSIZE
				g2.color = Color.yellow
				g2.drawRect(xx, 0, IMSIZE - 1, IMSIZE - 1)
			    }
			}
		    }
		} catch (ex: NullPointerException) {
		}

		// fld id
		val idf = Color(135, 135, 210)
		g2.color = idf
		if (i == selected_src_fld) g2.color = idf.darker()
		if (i == selected_dst_fld && fld_state_id) g2.color = idf.brighter()
		g2.fillRect(i * IMSIZE, IMSIZE, IMSIZE, 20)
		g2.color = Color.white
		if (tl_nid >= 0 && tl_nid < OmegaConfig.TIMELINES_N) {
		    val gimae = getGImAE(i)

		    // ae.ae_ctxt.mtl;
		    val tl = ae!!.a_ctxt.mtl!!.getTimeLine(tl_nid)
		    var ID = ""
		    if (tl != null) ID = tl.lessonId
		    g2.drawString("" + (tl_nid + 1) + ": " + ID,
			    5 + i * IMSIZE,
			    IMSIZE + 16)
		}
		g2.color = Color(35, 35, 110)
		g2.drawLine(i * IMSIZE, IMSIZE, i * IMSIZE, IMSIZE + 20)
	    }
	}
	if (EMPTY != selected_src_fld &&
		EMPTY != selected_dst_fld) drawArrFld(g2, selected_src_fld, selected_dst_fld)
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
	return Dimension(IMSIZE * ACT_N, IMSIZE + 20)
    }

    public override fun paintComponent(g: Graphics) {
	val g2 = g as Graphics2D
	draw(g2)
    }

    fun setSelected(b: Boolean) {
	if (prop != null) prop!!.enableDelete(b)
    }
}
