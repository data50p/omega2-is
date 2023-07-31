package com.femtioprocent.omega.anim.cabaret

import com.femtioprocent.omega.anim.canvas.AnimCanvas
import com.femtioprocent.omega.anim.tool.path.AllPath
import com.femtioprocent.omega.anim.tool.timeline.TimeLine
import com.femtioprocent.omega.graphic.render.GIm
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.a2s
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.util.SundryUtils.tD
import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.geom.NoninvertibleTransformException
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

class GImAE : GIm {
    val fnBase: String = "FN_BASE"

    // a + b * dt
    private var rot_a = 0.0
    private var rot_b = 0.0
    private var rot_t = 0.0
    private var rot_lim = 10001.0
    private var scale_a = 1.0
    private var scale_b = 0.0
    private var scale_t = 0.0
    private var scale_lim = 1.0

    @JvmField
    var anim_speed = 0.2

    //    private boolean use_alt_hotspot = false;
    //      public double hotspot_fx = 0.5;
    //      public double hotspot_fy = 0.5;
    //      public double hotspotAlt_fx = 0.5;
    //      public double hotspotAlt_fy = 0.5;
    @JvmField
    var hotspot = Hotspot()

    //      public void setAlternative(int arg) {
    //  	if ( (arg & 1) == 1 )
    //  	    use_alt_hotspot = true;
    //  	else
    //  	    use_alt_hotspot = false;
    //  	OmegaContext.sout_log.getLogger().info(":--: " + "alt hs " + use_alt_hotspot);
    //      }
    var primScale = 1.0
    var prim_mirror = 0
    private var lesson_id: String? = null
    private var can_eat = false
    private var can_bee_eaten = false
    var apa: AllPath

    @JvmField
    var nid: Int

    @JvmField
    var imw: Int

    @JvmField
    var imh: Int
    private var buffered_image: BufferedImage? = null
    private val at = AffineTransform()
    private val variable = arrayOf("", "", "")

    constructor(ca: AnimCanvas, id: String, nid: Int) : super(ca, id) {
	apa = ca.ap
	this.nid = nid
	imw = width
	imh = height
	setHotSpotIx(0, 0.5, 0.5)
    }

    constructor(ca: AnimCanvas, gimae: GImAE, nid: Int) : super(ca, gimae) {
	apa = gimae.apa
	this.nid = nid
	for (ih in 0 until Hotspot.Companion.HOTSPOT_N) setHotSpotIx(ih, gimae.hotspot.getX(ih), gimae.hotspot.getY(ih))
	imw = width
	imh = height
	primMirror = gimae.prim_mirror
	primScale = gimae.primScale
	setAnimSpeed(gimae.anim_speed)
	for (i in 0..3) setVariable(i, gimae.getVariable(i))
	lesson_id = gimae.lesson_id
    }

    fun setVariable(ix: Int, s: String) {
	var ix = ix
	ix--
	if (ix >= 0 && ix < 3) variable[ix] = s
    }

    fun getVariable(ix: Int): String {
	var ix = ix
	if (ix == 0) return lessonId!!
	ix--
	return if (ix >= 0 && ix < 3) variable[ix] else ""
    }

    fun setRotation(`val`: Double, stop_val: Double, `when`: Int) {
	var stop_val = stop_val
	if (`val` == 0.0) {
	    if (stop_val > 10001) stop_val = 0.0
	    rot_a = stop_val
	    rot_b = 0.0
	    rot_t = `when`.toDouble()
	    rot_lim = 10001.0
	    return
	}
	rot_a = rotAt(`when`)
	rot_b = `val`
	rot_t = `when`.toDouble()
	if (stop_val >= 19999.0) {
	    rot_lim = stop_val
	    if (rot_b < 0) rot_lim *= -1.0
	} else {
	    val rot_0_1 = (rot_a + 10000 *
		    (3.141592653589793238 * 2)) % (3.141592653589793238 * 2)
	    val rot_0_1_2 = (stop_val + 10000 *
		    (3.141592653589793238 * 2)) % (3.141592653589793238 * 2)
	    val rot_togo = rot_0_1_2 - rot_0_1
	    rot_lim = if (rot_b > 0) {
		if (rot_togo >= 0) rot_a + rot_togo else rot_a + rot_togo + 3.141592653589793238 * 2
	    } else {
		if (rot_togo <= 0) rot_a + rot_togo else rot_a + rot_togo - 3.141592653589793238 * 2
	    }
	}
    }

    fun setScale(`val`: Double, stop_val: Double, `when`: Int) {
	scale_a = scaleAt(`when`)
	scale_b = `val`
	scale_t = `when`.toDouble()
	scale_lim = stop_val
    }

    fun setAnimSpeed(a: Double) {
	anim_speed = a
    }

    @Synchronized
    override fun initPlay(o: Any?) {
	super.initPlay(o)
	rot_a = 0.0
	rot_b = 0.0
	scale_a = 1.0
	scale_b = 0.0
	scale_t = 0.0
	rot_t = 0.0
	scale_lim = 1.0
	rot_lim = 10001.0
	setAnimSpeed(0.2)
	setAttribName(null)
	apa = o as AllPath
    }

    @Synchronized
    override fun beginPlay() {
	super.beginPlay()
	mask = null
	current_mask_image = null
	buffered_image = null
	setDinner(false, false)
	//	use_alt_hotspot = false;
    }

    fun scaleAt(dt: Int): Double {
	var scale = scale_a + scale_b * (dt - scale_t)
	if (scale_b < 0) {
	    if (scale < scale_lim) {
		scale_b = 0.0
		scale_a = scale_lim
		scale = scale_lim
	    }
	} else {
	    if (scale > scale_lim) {
		scale_b = 0.0
		scale_a = scale_lim
		scale = scale_lim
	    }
	}
	return scale
    }

    fun rotAt(dt: Int): Double {
	var rot = rot_a + rot_b * (dt - rot_t)
	if (rot_lim > 10000 || rot_lim < -10000) return rot
	if (rot_b < 0) {
	    if (rot <= rot_lim) {
		rot_b = 0.0
		rot_a = rot_lim
		rot = rot_lim
	    }
	} else {
	    if (rot >= rot_lim) {
		rot_b = 0.0
		rot_a = rot_lim
		rot = rot_lim
	    }
	}
	return rot
    }

    fun setHotSpot(fx: Double, fy: Double) {
	hotspot[0, fx] = fy
    }

    fun setHotSpotIx(ix: Int, fx: Double, fy: Double) {
	hotspot[ix, fx] = fy
    }

    fun setHotSpotIx(ix: Int, s: String?) {
	val sa = split(s, " ,;")
	val x = tD(sa[0])
	val y = tD(sa[1])
	hotspot[ix, x] = y
    }

    fun setHotSpotIxSame(ix: Int) {
	if (ix > 0) hotspot[ix, hotspot.getX(0)] = hotspot.getY(0)
    }

    private fun fix2(a: Double): Double {
	val i = (a * 100 + 0.5).toInt()
	return i.toDouble() / 100
    }

    fun getHotSpotAsString(ix: Int): String {
	return if (ix == 0) "" + fix2(hotspot.x) + ' ' + fix2(hotspot.y) else ("" + fix2(hotspot.getX(1)) + ' ' + fix2(
	    hotspot.getY(1)
	) + " ->"
		+ " " + fix2(hotspot.getX(2)) + ' ' + fix2(hotspot.getY(2)) + "")
    }

    fun setDinner(can_eat: Boolean, can_bee_eaten: Boolean) {
	this.can_eat = can_eat
	this.can_bee_eaten = can_bee_eaten
    }

    fun setOption(arg: Int) {}
    fun setResetSequence(arg: String?, `when`: Int, beginning: Int) {
	if (arg == null || arg == "") {
	    reset_sequence = `when`
	} else if (arg == "{") {
	    reset_sequence = 0
	} else if (arg == "[") {
	    reset_sequence = beginning
	}
    }

    var primMirror: Int
	get() = prim_mirror
	set(a) {
	    prim_mirror = a
	    when (a) {
		0 -> setPrimMirror(false, false)
		1 -> setPrimMirror(true, false)
		2 -> setPrimMirror(false, true)
		3 -> setPrimMirror(true, true)
	    }
	}
    var lessonId: String?
	get() = if (lesson_id != null) lesson_id else "#$nid"
	set(lid) {
	    lesson_id = lid
	}
    val lessonIdAlt: String
	get() = if (lesson_id != null) lesson_id!! else ""
    var affineTransform: AffineTransform? = null

    override fun getAffineTransformAtTime(
	dt: Int,
	tl: TimeLine,
	bounding_rect: Rectangle2D
    ): AffineTransform? {
	try {
	    rot = rotAt(dt)
	    scale = scaleAt(dt)
	    scale *= primScale
	    val pa = apa[nid]
	    val len = pa!!.length.toInt()
	    val dur = tl.duration
	    val offs = tl.offset
	    if (pa != null) {
		val da = pa.pathLength_TSyncSegments
		val dat = tl.timeMarker_TSyncSegments
		var where = 0.0
		var ii = -1
		for (i in da.indices) {
		    if (dat[i] < dt) ii = i
		}
		val po: Point2D
		if (ii == -1) {
		    po = pa.getPointAt(0.0)
		    //OmegaContext.sout_log.getLogger().info(":--: " + "po1: " + po);
		} else if (ii == da.size - 1) {
		    po = pa.getPointAt(da[ii])
		    //OmegaContext.sout_log.getLogger().info(":--: " + "po2: " + po);
		    where = if (len == 0) da[ii] / 1 else da[ii] / len
		    //OmegaContext.sout_log.getLogger().info(":--: " + "wh2: " + where + ' ' + da[ii] + ' ' + len);
		} else {
		    var len2 = da[ii].toInt()
		    val difft = dt - dat[ii]
		    var fact = 0.1
		    if (dat[ii + 1] - dat[ii] != 0.0) fact = difft / (dat[ii + 1] - dat[ii])
		    val dlen = da[ii + 1] - da[ii]
		    len2 = (len2 + fact * dlen).toInt()
		    //OmegaContext.sout_log.getLogger().info(":--: " + "da3: " + da[ii+1] + ' ' + da[ii] + ' ' + ii);
		    //OmegaContext.sout_log.getLogger().info(":--: " + "f3: " + fact + ' ' + dlen);
		    po = pa.getPointAt(len2.toDouble())
		    //OmegaContext.sout_log.getLogger().info(":--: " + "po3: " + po);
		    where = if (len == 0) len2.toDouble() / 1 else len2.toDouble() / len
		    //OmegaContext.sout_log.getLogger().info(":--: " + "wh3: " + where + ' ' + len2 + ' ' + len);
		}

		//OmegaContext.sout_log.getLogger().info(":--: " + "-------- where " + where);
		var hotsp_x = hotspot.getX(where)
		if (prim_mirror_x xor mirror_x) {
		    hotsp_x = 1.0 - hotsp_x
		}
		var hotsp_y = hotspot.getY(where)
		if (prim_mirror_y xor mirror_y) {
		    hotsp_y = 1.0 - hotsp_y
		}
		imw = width
		imh = height
		at.setToIdentity()
		at.translate(scale * -hotsp_x * imw, scale * -hotsp_y * imh)
		at.translate(po.x, po.y)
		at.rotate(
		    rot,
		    scale * hotspot.getX(0) * imw,
		    scale * hotspot.getY(0) * imh
		)
		if (prim_mirror_x xor mirror_x) {
		    at.translate(scale * imw, 0.0)
		    at.scale(-1.0, 1.0)
		}
		if (prim_mirror_y xor mirror_y) {
		    at.translate(0.0, scale * imh)
		    at.scale(1.0, -1.0)
		}
		at.scale(scale, scale)
		val rT: Rectangle2D = Rectangle2D.Double(
		    0.0, 0.0,
		    imw.toDouble(), imh.toDouble()
		)
		val sh = at.createTransformedShape(rT)
		if (bounding_rect != null) {
		    bounding_rect.setRect(sh.bounds2D)
		    //		    bounding_rect.setRect(expand(sh.getBounds2D()));
		    this.bounding_rect.setRect(bounding_rect)
		}
		affineTransform = at
		return at
	    }
	} catch (ex: NullPointerException) {
	    Log.getLogger().info("ERR: GImAE.getAffineTransformAtTime(): $ex")
	    ex.printStackTrace()
	    return null
	} catch (ex: Exception) {
	    Log.getLogger().info("ERR: GImAE.getAffineTransformAtTime(): $ex")
	    ex.printStackTrace()
	    return null
	}
	return null
    }

    fun expand(r: Rectangle2D): Rectangle2D {
	return Rectangle2D.Double(
	    r.x,
	    r.y,
	    r.width + 2,
	    r.height + 2
	)
    }

    override fun getAffineTransformAtTime(
	dt: Int,
	tlA: Array<TimeLine>,
	bounding_rect: Rectangle2D
    ): AffineTransform? {
	if (nid < tlA.size) {
	    val tl = tlA[nid]
	    if (tl != null) {
		return getAffineTransformAtTime(dt, tl, bounding_rect)
	    }
	}
	return null
    }

    @Synchronized
    override fun render(at: AffineTransform?) {
	if (acomp == null) return
	if ((mask == null || xim.getImage(ca) !== current_mask_image) && can_eat) {
	    mask = this
	    mask!!.buffered_image = BufferedImage(
		width,
		height,
		BufferedImage.TYPE_INT_ARGB
	    )
	    val gb = mask!!.buffered_image!!.graphics as Graphics2D
	    val at2 = AffineTransform()
	    gb.drawImage(xim.getImage(ca).also { current_mask_image = it }, at2, null)
	}
	if (buffered_image == null && can_bee_eaten) {
	    buffered_image = BufferedImage(
		width,
		height,
		BufferedImage.TYPE_INT_ARGB
	    )
	    val gb = buffered_image!!.graphics as Graphics2D
	    gb.drawImage(xim.getImage(ca), 0, 0, null)
	}
	if (mask != null && mask !== this && can_bee_eaten) {
	    try {
		val iat = affineTransform!!.createInverse()
		iat.concatenate(mask!!.affineTransform)
		val gb = buffered_image!!.graphics as Graphics2D
		gb.composite = AlphaComposite.DstOut
		gb.drawImage(mask!!.buffered_image, iat, null)
	    } catch (ex: NoninvertibleTransformException) {
		Log.getLogger().info("ERR: AFFTRAINV $ex")
	    }
	}
	if (buffered_image != null) ca.drawImage(buffered_image, at, acomp) else ca.drawImage(
	    xim.getImage(ca),
	    at,
	    acomp
	)
    }

    override fun toString(): String {
	return "GImAE{fid=" + fid +
		",lesson_id=" + lesson_id +
		",var=" + a2s(variable) +
		",nid=" + nid +
		",im=" + xim.getImage(null) +
		",layer=" + layer +
		",reset_sequence=" + reset_sequence +
		",prim_scale=" + primScale +
		",prim_mirror=" + prim_mirror +
		'}'
    }

    companion object {
	private var mask: GImAE? = null
	private var current_mask_image: Image? = null
    }
}
