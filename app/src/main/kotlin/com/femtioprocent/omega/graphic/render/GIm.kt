package com.femtioprocent.omega.graphic.render

import com.femtioprocent.omega.anim.tool.timeline.TimeLine
import com.femtioprocent.omega.media.images.xImage
import java.awt.AlphaComposite
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D

abstract class GIm {
    protected val fid: String
    protected var delayed_id_attrib: String? = null
    var layer = 2
    var reset_sequence = 0
    protected var acomp = AlphaComposite.getInstance(
	    AlphaComposite.SRC_OVER,
	    1.0f
    )
    protected var ca: Canvas
    var xim: xImage
    protected var bounding_rect: Rectangle2D = Rectangle2D.Double(0.0, 0.0, 0.0, 0.0)
    private val prev_bounding_rect: Rectangle2D = Rectangle2D.Double(0.0, 0.0, 0.0, 0.0)
    private var maxW = 0.0
    private var maxH = 0.0
    private var maxWH = 0.0

    // Object geometrical state
    //    protected Point2D pos;
    protected var rot = 0.0
    protected var scale = 1.0
    var mirror_x = false
    var mirror_y = false
    var prim_mirror_x = false
    var prim_mirror_y = false

    protected constructor(ca: Canvas, fid: String) {
	this.ca = ca
	this.fid = fid
	xim = xImage(fid)
	initIm()
    }

    protected constructor(ca: Canvas, gim: GIm) {
	this.ca = ca
	fid = gim.fid
	xim = xImage(fid)
	initIm()
    }

    fun initIm() {
	val ima = xim.getImage(ca)
	val w = ima.getWidth(null)
	val h = ima.getHeight(null)
	maxW = w.toDouble() // ima.getWidth(null);
	maxH = h.toDouble() // ima.getHeight(null);
	maxWH = Math.max(maxW, maxH)
	//	prev_bounding_rect = new Rectangle2D.Double(0, 0, 0, 0);
//	pos = new Point2D.Double(11110, 11110);
    }

    fun setAttribName(an: String?) {
	if (an != null && an.length == 0) xim.attrib = null else xim.attrib = an
	initIm()
    }

    fun commitAttribName() {
	if (delayed_id_attrib == null) return
	if (delayed_id_attrib == "@@@ null @@@") delayed_id_attrib = null
	setAttribName(delayed_id_attrib)
	delayed_id_attrib = null
    }

    fun setAttribNameUncommited(an: String?) {
	delayed_id_attrib = an ?: "@@@ null @@@"
    }

    fun setMirror(m_x: Boolean, m_y: Boolean) {
	mirror_x = m_x
	mirror_y = m_y
    }

    fun setPrimMirror(m_x: Boolean, m_y: Boolean) {
	prim_mirror_x = m_x
	prim_mirror_y = m_y
    }

    fun setVisibility(percent: Int) {
	acomp = if (percent == 0) AlphaComposite.getInstance(
		AlphaComposite.SRC_OVER,
		0.0.toFloat()
	) else AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (percent / 100.0).toFloat())
    }

    @Synchronized
    open fun initPlay(o: Any?) {
	beginPlay()
    }

    @Synchronized
    open fun beginPlay() {
	hide()
	layer = 2
	setVisibility(100)
	mirror_y = false
	mirror_x = mirror_y
    }

    @Synchronized
    fun hide() {
	//pos.setLocation(-2000, -2000);
	val at = AffineTransform()
	at.translate(10000.0, 10000.0)
	render(at)
    }

    fun fNBase(): String {
	return xim.fNBase()
    }

    val peTaskNid: String
	get() = xim.peTaskNid
    val baseImage: Image
	//     public Image getImage() {
	get() = xim.getBaseImage(ca)
    var last_im: Image? = null
    var last_w = 0
    var last_h = 0
    val width: Int
	get() {
	    val im = xim.getImage(ca)
	    if (last_im !== im) {
		last_im = im
		last_w = im.getWidth(null)
		last_h = im.getHeight(null)
	    }
	    return last_w
	}
    val height: Int
	get() {
	    width
	    return last_h
	}

    @Synchronized
    fun restoreBackground() {
	ca.restoreImage(prevBoundingRect)
    }

    @Synchronized
    open fun render(at: AffineTransform?) {
//	OmegaContext.sout_log.getLogger().info(":--: " + "GIm.render " + this + ' ' + at);
	ca.drawImage(xim.getImage(ca), at, acomp)
    }

    var prevBoundingRect: Rectangle2D
	get() = prev_bounding_rect
	set(br) {
	    prev_bounding_rect.setRect(br)
	}

    abstract fun getAffineTransformAtTime(dt: Int, tl: TimeLine, bounding_rect: Rectangle2D): AffineTransform?
    abstract fun getAffineTransformAtTime(dt: Int, tlA: Array<TimeLine>, bounding_rect: Rectangle2D): AffineTransform?
}
