package com.femtioprocent.omega.anim.tool.path

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.graphic.render.Canvas
import com.femtioprocent.omega.util.DelimitedStringBuilder
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.argAsList
import com.femtioprocent.omega.util.SundryUtils.flagAsMap
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.xml.Element
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.GeneralPath
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.*
import javax.swing.JFrame

class Path {
    var selected: Boolean = false
	set(value) {
	    field = value
	    if (field) {
		global_selected = this
	    }
	    val it: Iterator<*> = seg_l.iterator()
	    while (it.hasNext()) {
		val sq = it.next() as Segment_Q
		sq.selectedPoint = -1
	    }
	}

    var nid: Int

    inner class Mark(var id: Int, var type: Char, var where: Double) {
	var pa: Path
	var ord = 0

	init {
	    pa = this@Path
	}

	fun moveToPathPosition(a: Double) {
	    where = a
	    pa.reNumerateMarker()
	}

	val pos: Point2D
	    get() = pa.getPointAt(where)

	override fun toString(): String {
	    return "Path.Mark{" +
		    "ord=" + ord +
		    ", id=" + id +
		    ", type=" + type +
		    ", where=" + where +
		    ", pa=" + pa +
		    "}"
	}

	val element: Element
	    get() {
		val em = Element("mark")
		em.addAttr("id", "" + id)
		em.addAttr("type", "" + type)
		em.addAttr("where", "" + where)
		return em
	    }
    }

    var marker // Mark
	    : MutableList<Mark>
    var gp: GeneralPath? = null
    var seg_l: MutableList<Segment_Q>
    var lenA: DoubleArray? = null
    var point2D: Array<Point2D>? = null
    var len = 0.0

    constructor(el: Element) {       // TPath
	seg_l = ArrayList()
	marker = ArrayList()
	val nidp_s = el.findAttr("nid")
	val nidp_i = nidp_s.toInt()
	nid = nidp_i
	for (i in 0..999) {
	    val eq = el.findElement("q", i) ?: break
	    // <q nid="0" p1="146.0,169.0" p2="206.0,139.0" pc="176.0,154.0"/>
	    val ord_s = eq.findAttr("ord")
	    val p1_s = eq.findAttr("p1")
	    val p2_s = eq.findAttr("p2")
	    val pc_s = eq.findAttr("pc")
	    val ord_i = ord_s.toInt()
	    val p1 = decode2D(p1_s)
	    val p2 = decode2D(p2_s)
	    val pc = decode2D(pc_s)
	    val sq = Segment_Q(seg_l.size, p1, pc, p2)
	    // sq.ord = ord_i;
	    sq.path = this
	    seg_l.add(sq)
	}
	for (i in 0..999) {
	    val em = el.findElement("mark", i) ?: break
	    val id = em.findAttr("id")
	    val type = em.findAttr("type")
	    val where = em.findAttr("where")
	    val iid = id.toInt()
	    val wd = tD(where)
	    addMarker(iid, type[0], wd)
	}
	rebuildGP()
    }

    constructor(nid: Int, sp: Point2D, ep: Point2D) {
	this.nid = nid
	seg_l = ArrayList()
	marker = ArrayList()
	val dx = ep.x - sp.x
	val dy = ep.y - sp.y
	val sq = Segment_Q(seg_l.size,
		sp,
		Point2D.Double(sp.x + dx / 2,
			sp.y + dy / 2),
		ep)
	sq.path = this
	seg_l.add(sq)
	rebuildGP()
    }

    fun move(p: Point2D, offx: Int, offy: Int): Point2D {
	return Point2D.Double(p.x + offx,
		p.y + offy)
    }

    constructor(nid: Int, pa_src: Path, offx: Int, offy: Int) {
	this.nid = nid
	seg_l = ArrayList()
	marker = ArrayList()
	var it: Iterator<*> = pa_src.seg_l.iterator()
	while (it.hasNext()) {
	    val sq = it.next() as Segment_Q
	    val sq_c = Segment_Q(nid,
		    move(sq.p1, offx, offy),
		    move(sq.pc, offx, offy),
		    move(sq.p2, offx, offy))
	    sq_c.path = this
	    seg_l.add(sq_c)
	}
	it = pa_src.marker.iterator()
	while (it.hasNext()) {
	    val mk = it.next()
	    addMarker(mk.id, mk.type, mk.where)
	}
	rebuildGP()
    }

    fun decode2D(s: String?): Point2D {
	val sa = split(s, ",")
	val a = sa[0]!!.toFloat()
	val b = sa[1]!!.toFloat()
	return Point2D.Float(a, b)
    }

    fun moveAll(dx: Double, dy: Double) {
	val it: Iterator<*> = seg_l.iterator()
	while (it.hasNext()) {
	    val sq = it.next() as Segment_Q
	    sq.moveAllBy(dx, dy)
	}
	rebuildGP()
    }

    fun extendSegment(np: Point2D) {
	val lp = lastPoint
	val dx = np.x - lp.x
	val dy = np.y - lp.y
	val sq = Segment_Q(seg_l.size,
		lp,
		Point2D.Double(lp.x + dx / 2, lp.y + dy / 2),
		np)
	sq.path = this
	seg_l.add(sq)
	rebuildGP()
    }

    fun createSegment(): Path {
	val sp: Point2D = Point2D.Double(100.0, 100.0)
	val ep: Point2D = Point2D.Double(200.0, 300.0)
	return Path(0, sp, ep)
    }

    fun splitSegment() {
	val nseg_l: MutableList<Segment_Q> = ArrayList()
	var b = false
	val it: Iterator<Segment_Q> = seg_l.iterator()
	while (it.hasNext()) {
	    val sq = it.next()
	    if (sq.selectedPoint >= 0) {
		val nsq = sq.split()
		nsq.path = this
		nseg_l.add(sq)
		nseg_l.add(nsq)
		b = true
	    } else {
		nseg_l.add(sq)
	    }
	}
	if (b) {
	    seg_l = nseg_l
	    rebuildGP()
	}
    }

    fun removeSegment() {
	val nseg_l: MutableList<Segment_Q> = ArrayList()
	var b = false
	val it: Iterator<Segment_Q> = seg_l.iterator()
	while (it.hasNext()) {
	    val sq = it.next()
	    if (sq.selectedPoint >= 0) {
		b = true
	    } else {
		nseg_l.add(sq)
	    }
	}
	if (b && nseg_l.size > 0) {
	    seg_l = nseg_l
	    rebuildGP()
	}
    }

    fun getSegment(ix: Int): Segment_Q {
	return seg_l[ix]
    }

    fun howManyTSync(): Int {
	return marker.size
    }

    fun addMarker(id: Int, type: Char, where: Double) {
	var mk: Mark
	marker.add(Mark(id, type, where).also { mk = it })
	mk.ord = marker.size - 1
	reNumerateMarker()
	/*
	Mark[] ma = (Mark[])(marker.toArray(new Mark[0]));
	Arrays.sort(ma, new Comparator() {
	    public int compare(Object o1, Object o2) {
		Mark tm1 = (Mark)o1;
		Mark tm2 = (Mark)o2;
		return (int)(tm1.where - tm2.where);
	    }
	});	
	marker = new ArrayList();
	for(int i = 0; i < ma.length; i++)
	    ma[i].ord = i;
	Collection col = Arrays.asList(ma);
	marker.addAll(col);
	    */
    }

    fun delMarker(ix: Int) {
	marker.removeAt(ix)
	reNumerateMarker()
    }

    fun reNumerateMarker() {
	val ma = marker.toTypedArray<Mark>()
	Arrays.sort(ma) { o1, o2 ->
	    val tm1 = o1 as Mark
	    val tm2 = o2 as Mark
	    (tm1.where - tm2.where).toInt()
	}
	marker = ArrayList()
	for (i in ma.indices) ma[i].ord = i
	val col: Collection<Mark> = Arrays.asList(*ma)
	marker.addAll(col)
    }

    fun updateInternal() {
	rebuildGP()
    }

    fun rebuildGP() {
	val ngp = GeneralPath()
	for (i in seg_l.indices) {
	    if (i != 0) getSq(i).adjust(ngp.currentPoint)
	    getSq(i).addMe(ngp)
	}
	gp = ngp
	rebuildLength()
    }

    val shape: Shape?
	get() = gp

    fun getSq(ix: Int): Segment_Q {
	return seg_l[ix]
    }

    val sqN: Int
	get() = seg_l.size
    val lastPoint: Point2D
	get() = gp!!.currentPoint
    val firstPoint: Point2D
	get() = getPointAt(0.0)

    fun getPathCoordinates(shape: Shape): Array<Any> {
	val fa = DoubleArray(6)
	var cnt = 0
	var pi = shape.getPathIterator(null, OmegaConfig.FLATNESS)
	LOOP@ while (!pi.isDone) {
	    val a = pi.currentSegment(fa)
	    when (a) {
		PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> cnt++
		else -> Log.getLogger().info(":--: " + "pi ?")
	    }
	    pi.next()
	}
	pi = shape.getPathIterator(null, OmegaConfig.FLATNESS)
	val pa = arrayOfNulls<Point2D>(cnt)
	val lena = DoubleArray(cnt)
	var le = 0.0
	var x = 0.0
	var y = 0.0
	var xx = 0.0
	var yy = 0.0
	var cnt2 = 0
	LOOP2@ while (!pi.isDone) {
	    val a = pi.currentSegment(fa)
	    when (a) {
		PathIterator.SEG_MOVETO -> {
		    pa[cnt2] = Point2D.Double(fa[0], fa[1])
		    lena[cnt2] = le
		    x = fa[0]
		    y = fa[1]
		}

		PathIterator.SEG_LINETO -> {
		    xx = fa[0]
		    yy = fa[1]
		    le += Point2D.distance(xx, yy, x, y) // (int)Math.sqrt(xxx*xxx+yyy*yyy);
		    pa[cnt2] = Point2D.Double(fa[0], fa[1])
		    lena[cnt2] = le
		    x = xx
		    y = yy
		}

		else -> Log.getLogger().info(":--: " + "pi ?")
	    }
	    cnt2++
	    pi.next()
	}
	return arrayOf(lena, pa)
    }

    private fun rebuildLength(): Double {
	val shape = shape
	if (shape == null) {
	    point2D = null
	    lenA = null
	    return 0.0
	}
	val oa = getPathCoordinates(shape)
	lenA = oa[0] as DoubleArray
	point2D = oa[1] as Array<Point2D>
	len = lenA!![lenA!!.size - 1]
	return len
    }

    val pathLength_TSyncSegments: DoubleArray
	get() {
	    val da = DoubleArray(howManyTSync() + 2)
	    if (da.size == 2) {
		da[0] = 0.0
		da[1] = length
	    } else {
		da[0] = 0.0
		for (i in da.indices) if (i == 0) da[i] = 0.0 else if (i == 1) da[i] = marker[i - 1].where else if (i == da.size - 1) da[i] = length else da[i] = marker[i - 1].where
	    }
	    return da
	}

    fun getPointAt(l: Double): Point2D {
	return getPointAt(l, true)
    }

    fun getPointAt(l: Double, hide_after: Boolean): Point2D {
	var ix = Arrays.binarySearch(lenA, l)
	if (ix < 0) ix = -ix - 1
	if (ix >= lenA!!.size) ix = lenA!!.size - 1
	val i = ix
	// 	for(int i = 0; i < lenA.length; i++) {
// 	    if ( lenA[i] >= l ) {
//		OmegaContext.sout_log.getLogger().info(":--: " + "bin " + ix + ' ' + i);
	var prev = 0.0
	if (i != 0) prev = lenA!![i - 1]
	// return pointA[i];
	val df = l - prev
	if (df < 0.001) {
	    return point2D!![i]
	}
	val d_1 = lenA!![i] - prev
	val frac = df / d_1
	val p_1 = if (i == 0) point2D!![i] else point2D!![i - 1]
	val p = point2D!![i]
	return Point2D.Double(p_1.x + (p.x - p_1.x) * frac,
		p_1.y + (p.y - p_1.y) * frac)
	// 	    }
// 	}
	//return hide_after ? new Point2D.Double(-11100.0, -11100.0) : pointA[pointA.length-1];
    }

    fun getPointAtPercent(percent: Double): Point2D {
	return getPointAt(percent * length)
    }

    fun isSelected(): Boolean {
	return selected
    }

    fun findNearest(p: Point2D?): Probe {
	var nearest = Probe()
	val it: Iterator<*> = seg_l.iterator()
	while (it.hasNext()) {
	    val sq = it.next() as Segment_Q
	    val n = sq.findNearest(p)
	    if (n.dist < nearest.dist) {
		nearest = n
		nearest.pa = this
	    }
	}
	return nearest
    }

    fun findNearestMarker(p: Point2D?): Mark? {
	var mk: Mark? = null
	var dist = 0.0
	val it: Iterator<*> = marker.iterator()
	while (it.hasNext()) {
	    val mk2 = it.next() as Mark
	    val d = getPointAt(mk2.where).distance(p)
	    if (mk == null || d < dist) {
		dist = d
		mk = mk2
	    }
	}
	return mk
    }

    fun distMarker(mk: Mark, p: Point2D?): Double {
	return getPointAt(mk.where).distance(p)
    }

    fun getMarker(m_ord: Int): Mark? {
	if (m_ord < 0) return null
	return if (m_ord >= marker.size) null else marker[m_ord]
    }

    fun findNearestPoint(p: Point2D?): Double {
	var dist = 99999999.0
	var fp = 0.0
	var i = 0
	while (i <= length) {
	    val pp = getPointAt(i.toDouble())
	    if (pp != null) {
		val d = pp.distance(p)
		if (d < dist) {
		    dist = d
		    fp = i.toDouble()
		}
	    }
	    i++
	}
	return findNearestPointScale(p, fp)
    }

    fun findNearestPointScale(p: Point2D?, here: Double): Double {
	val scale = 0.01
	var dist = 99999999.0
	var fp = here
	for (i in -200..200) {
	    val pp = getPointAt(here + i * scale)
	    if (pp != null) {
		val d = pp.distance(p)
		if (d < dist) {
		    dist = d
		    fp = here + i * scale
		}
	    }
	}
	return fp
    }

    val length: Double
	get() {
	    if (len == 0.0) rebuildLength()
	    return len
	}

    fun draw(g2: Graphics2D) {
	var selected_sq: Segment_Q? = null
	if (true /*active*/) {
	    val col = if (selected) Color.orange else Color.orange.darker()
	    val it: Iterator<*> = seg_l.iterator()
	    while (it.hasNext()) {
		val sq = it.next() as Segment_Q
		if (sq.selectedPoint >= 0) selected_sq = sq else {
		    g2.color = col
		    sq.draw(g2)
		}
	    }
	    if (selected_sq != null) {
		g2.color = Color.gray
		selected_sq.drawConnector(g2)
		g2.color = Color.orange.brighter()
		selected_sq.draw(g2)
	    }
	} else {
	    g2.color = Color.green
	    g2.draw(shape)
	}
	drawMarker(g2)
    }

    private fun drawSmallBox(g2: Graphics2D, p: Point2D, w: Int) {
	g2.draw(Rectangle2D.Double(p.x - w / 2.0,
		p.y - w / 2.0,
		w.toDouble(), w.toDouble()))
    }

    private fun drawMarker(g2: Graphics2D) {
	val it: Iterator<*> = marker.iterator()
	while (it.hasNext()) {
	    val mk = it.next() as Mark
	    val pp = getPointAt(mk.where)
	    if (pp != null) {
		g2.color = Color.magenta.brighter()
		drawSmallBox(g2, pp, 5)
	    }
	}
    }

    private fun encodePoint(p: Point2D): String {
	return "" + p.x + ',' + p.y
    }

    fun element(): Element {
	val el = Element("TPath")
	for (i in seg_l.indices) {
	    val qq = getSq(i)
	    val eq = Element("q")
	    eq.addAttr("ord", "" + i)
	    eq.addAttr("p1", "" + encodePoint(qq.p1))
	    eq.addAttr("p2", "" + encodePoint(qq.p2))
	    eq.addAttr("pc", "" + encodePoint(qq.pc))
	    el.add(eq)
	}
	return el
    }

    val element: Element
	get() {
	    val el = Element("TPath")
	    el.addAttr("nid", "" + nid)
	    for (i in seg_l.indices) {
		val qq = getSq(i)
		val eq = Element("q")
		eq.addAttr("ord", "" + i)
		eq.addAttr("p1", "" + encodePoint(qq.p1))
		eq.addAttr("p2", "" + encodePoint(qq.p2))
		eq.addAttr("pc", "" + encodePoint(qq.pc))
		el.add(eq)
	    }
	    val lenArr = lenA
	    val point2d = point2D
	    val help = Element("help")
	    help.addAttr("len", format(lenArr))
	    help.addAttr("seg", format(point2d))
	    el.add(help)
	    val info = Element("info")
	    info.addAttr("flatness", "" + OmegaConfig.FLATNESS)
	    info.addAttr("size", "" + point2d!!.size)
	    el.add(info)
	    for (i in marker.indices) {
		el.add(marker[i].element)
	    }
	    return el
	}

    companion object {
	@kotlin.jvm.JvmField
	var global_selected: Path? = null

	fun tD(s: String?): Double {
	    return try {
		val dval = java.lang.Double.valueOf(s)
		dval
	    } catch (ex: Exception) {
		0.0
	    }
	}

	fun format(point2d: Array<Point2D>?): String {
	    val sb = DelimitedStringBuilder(";")
	    for (p in point2d!!) {
		sb.append("" + p.x + "," + p.y)
	    }
	    return sb.toString()
	}

	fun format(lenArr: DoubleArray?): String {
	    val sb = DelimitedStringBuilder(";")
	    for (d in lenArr!!) {
		sb.append("" + d)
	    }
	    return sb.toString()
	}

	@JvmStatic
	fun main(argv: Array<String>) {
	    val flag: HashMap<String, String> = flagAsMap(argv)
	    val argl = argAsList(argv)
	    val f = JFrame("Path - test")
	    val c = f.contentPane
	    val ca = Canvas()
	    if (flag["g"] != null);
	    c.add(ca)
	    f.pack()
	    f.setSize(870, 640)
	    f.isVisible = true
	    //ca.setBackground("developer.omega_assets/media/background/Barn1.jpg");
	    m_sleep(300)
	    val sp: Point2D = Point2D.Double(100.0, 100.0)
	    val ep: Point2D = Point2D.Double(200.0, 300.0)
	    val pa = Path(1, sp, ep)
	    pa.selected = true
	    pa.draw(ca.graphics as Graphics2D)
	    pa.extendSegment(Point2D.Double(300.0, 100.0))
	    pa.draw(ca.graphics as Graphics2D)
	    val sp2: Point2D = Point2D.Double(300.0, 200.0)
	    val ep2: Point2D = Point2D.Double(400.0, 500.0)
	    val pa2 = Path(2, sp2, ep2)
	    pa2.draw(ca.graphics as Graphics2D)
	    pa2.extendSegment(Point2D.Double(350.0, 300.0))
	    pa2.draw(ca.graphics as Graphics2D)

//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(0.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(10.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(100.0));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(100.2));
//log	OmegaContext.sout_log.getLogger().info(":--: " + "" + pa.getPointAt(pa.getLength()));
	}

	@JvmStatic
	fun format_(lenArr: DoubleArray): String {
	    return "" + lenArr
	}

	@JvmStatic
	fun format_(point2d: Array<Point2D>): String {
	    return "" + point2d
	}
    }
}
