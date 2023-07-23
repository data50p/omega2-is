package com.femtioprocent.omega.graphic.render

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.anim.tool.timeline.TimeLine
import com.femtioprocent.omega.graphic.util.LoadImage
import com.femtioprocent.omega.servers.httpd.Server
import com.femtioprocent.omega.subsystem.Httpd
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.rand
import com.femtioprocent.omega.xml.Element
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.ImageObserver
import javax.swing.JPanel

open class Canvas : JPanel(), ImageObserver {
    private var im_name: String? = null
    protected var bg: Image? = null
    var imageBackground: Image? = null
	protected set
    protected var off_im: Image? = null
    protected var off_im_wings: Image? = null
    protected var off_g2: Graphics2D? = null
    var im_size = Dimension(0, 0)
    @JvmField
    var allgim: AllGIm
    @JvmField
    var wings: MutableList<Wing> = ArrayList()
    @JvmField
    var HIDDEN = false
    fun resetBackground() {
	setBackground(im_name, wings)
    }

    open var background: String?
	get() = "image_background" // TODO: super.getBackground()
	set(im_name) {
	    setBackground(im_name, null)
	}

    fun setBackground(im_name: String?, wings: MutableList<Wing>?) {
	var wings = wings
	this.im_name = im_name
	if (wings == null) wings = ArrayList()
	this.wings = wings
	val imn = im_name
	val bg = LoadImage.loadAndWaitOrNull(this, imn!!, false)
	//	OmegaContext.sout_log.getLogger().info(":--: " + "bg " + bg + ' ' + imn);
	im_size = Dimension(bg!!.getWidth(null), bg.getHeight(null))
	this.bg = bg
	updWings()
	val httpd: Httpd = OmegaContext.getSubsystem("Httpd") as Httpd
	Httpd.httpd.hashMap["lesson:background"] = imn
	repaint()
    }

    fun createWing(fn: String?, x: Int, y: Int, layer: Int, scale: Double, mirror: Int): Wing {
	val w = Wing(this, fn!!, x, y, layer, wings.size)
	w.mirror = mirror
	w.scale = scale
	wings.add(w)
	return w
    }

    fun removeWing(ix: Int): List<Wing>? {
	if (ix >= wings.size) return null
	wings.removeAt(ix)
	resetBackground()
	return wings
    }

    fun updWings() {
	if (bg != null) imageBackground = bg
	imageBackground = createWithWings(imageBackground, im_size, wings)
	bg = null
	off_im = createImage(im_size.width, im_size.height)
	if (off_im != null) {
	    off_g2 = off_im!!.graphics as Graphics2D
	    restoreImage(0, 0, im_size.width, im_size.height)
	    //	    repaint();
	}
    }

    fun getWing(ix: Int): Wing {
	return wings[ix]
    }

    var trace_wing = -1
    var trace_wing_drag = false
    var trace_wing_dx = 0
    var trace_wing_dy = 0

    init {
	allgim = AllGIm(this)
    }

    fun traceNoWing() {
	trace_wing = -1
	trace_wing_drag = false
	repaint()
    }

    fun traceWing(ixx: Int, dx: Double, dy: Double, is_drag: Boolean) {
	trace_wing = ixx
	trace_wing_drag = is_drag
	trace_wing_dx = dx.toInt()
	trace_wing_dy = dy.toInt()
	repaint()
    }

    fun drawWing(w: Wing) {
	drawWing(off_g2, w)
    }

    fun drawWing(g2: Graphics2D?, w: Wing) {
	val at = AffineTransform()
	at.translate(w.pos.getX(), w.pos.getY())
	var sc = w.scale
	if (sc == 0.0) sc = 1.0
	at.scale(w.scale, w.scale)
	when (w.mirror) {
	    1 -> {
		at.translate(w.width.toDouble(), 0.0)
		at.scale(-1.0, 1.0)
	    }

	    2 -> {
		at.translate(0.0, w.height.toDouble())
		at.scale(1.0, -1.0)
	    }

	    3 -> {
		at.translate(w.width.toDouble(), w.height.toDouble())
		at.scale(-1.0, -1.0)
	    }
	}
	g2!!.drawImage(w.im, at, null)
    }

    fun createWithWings(bg: Image?, im_size: Dimension, wings: List<Wing>?): Image? {
	return try {
	    val im = createImage(im_size.width, im_size.height) ?: return bg
	    //	OmegaContext.sout_log.getLogger().info(":--: " + "withW im is " + im + ' ' + im_size);
	    val gg = im.graphics
	    gg.drawImage(bg, 0, 0, null)
	    if (wings != null) {
		for (il in 0..4) {
		    for (i in wings.indices) {
			val wing = getWing(i)
			if (wing != null && il == wing.layer) {
			    drawWing(gg as Graphics2D, wing)
			}
		    }
		}
	    }
	    im
	} catch (ex: IllegalArgumentException) {
	    Log.getLogger().info("ERR: createWithWings: $ex")
	    null
	}
    }

    override fun getPreferredSize(): Dimension {
	return im_size
    }

    @Synchronized
    fun drawImage(im: Image?, at: AffineTransform?, acomp: AlphaComposite?) {
	var acomp = acomp
	if (acomp == null) acomp =
	    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0.toFloat()) else off_g2!!.composite = acomp
	off_g2!!.drawImage(im, at, null)
	off_g2!!.composite = AlphaComposite.SrcOver
    }

    open val offsX: Double
	get() = 0.0
    open val offsY: Double
	get() = 0.0

    @Synchronized
    fun off_upd(x: Int, y: Int, w: Int, h: Int) {
	var x = x
	var y = y
	var w = w
	var h = h
	if (h < 0) {
	    y += h
	    h = -h
	}
	if (w < 0) {
	    x += w
	    w = -w
	}
	val offs_x = offsX
	val offs_y = offsY
	val gg = graphics.create(
	    x + offs_x.toInt(),
	    y + offs_y.toInt(), w, h
	) as Graphics2D
	gg.drawImage(off_im, -x, -y, this) // Rxy
	gg.dispose()
    }

    @Synchronized
    fun off_upd(ra: Array<Rectangle2D>) {
	val a = rand(255)
	for (i in ra.indices) {
	    off_upd(ra[i].x.toInt(), ra[i].y.toInt(), ra[i].width.toInt(), ra[i].height.toInt())
	    if (gdbg) {
		val g2 = graphics as Graphics2D
		val at = g2.transform
		val offs_x = offsX.toInt()
		val offs_y = offsY.toInt()
		at.translate(offs_x.toDouble(), offs_y.toDouble())
		g2.transform = at
		g2.color = Color(a, a, a)
		g2.drawRect(ra[i].x.toInt(), ra[i].y.toInt(), ra[i].width.toInt(), ra[i].height.toInt())
	    }
	}
    }

    @Synchronized
    fun restoreImage(x: Int, y: Int, w: Int, h: Int) {
	if (imageBackground == null) return
	if (off_g2 != null) {
	    val gg = off_g2!!.create(x, y, w + 2, h + 2)
	    gg.drawImage(imageBackground, -x, -y, null)
	    gg.dispose()
	}
    }

    @Synchronized
    fun restoreImage(r: Rectangle2D) {
	restoreImage(r.x.toInt(), r.y.toInt(), r.width.toInt(), r.height.toInt())
    }

    public override fun paintComponent(g: Graphics) {
	val g2 = g as Graphics2D
	if (off_im != null) {
	    var offs_x = offsX
	    var offs_y = offsY
	    offs_x = 0.0
	    offs_y = offs_x
	    g2.drawImage(off_im, offs_x.toInt(), offs_y.toInt(), null)
	}
	if (trace_wing != -1) {
	    if (trace_wing < wings.size) {
		if (trace_wing_drag) g2.color = Color.red else g2.color = Color.yellow
		g2.drawRect(
		    getWing(trace_wing).pos.getX().toInt() + trace_wing_dx,
		    getWing(trace_wing).pos.getY().toInt() + trace_wing_dy,
		    (getWing(trace_wing).scale * getWing(trace_wing).im.getWidth(null)).toInt(),
		    (getWing(trace_wing).scale * getWing(trace_wing).im.getHeight(null)).toInt()
		)
	    }
	}
    }

    fun initPlay(o: Any?) {
	traceNoWing()
	//	repaint();
	allgim.initPlay(o)
    }

    fun updateAtTime(dt: Int, tlA: Array<TimeLine?>) {
	allgim.updateAtTime(dt, tlA)
    }

    val element: Element
	get() {
	    val el = Element("Canvas")
	    val bel = Element("background")
	    bel.addAttr("name", im_name)
	    bel.addAttr("width", "" + imageBackground!!.getWidth(null))
	    bel.addAttr("height", "" + imageBackground!!.getHeight(null))
	    el.add(bel)
	    return el
	}

    open fun load(el: Element) {
	val cel = el.findElement("Canvas", 0)
	if (cel != null) {
	    val eb = el.findElement("background", 0)
	    if (eb != null) {
		val s = eb.findAttr("name")
		if (s != null) {
		    setBackground(s, ArrayList())
		    //fix		    wings_panel.removeAllWings();
		}
	    }
	}
    }

    companion object {
	private const val gdbg = false
    }
}
