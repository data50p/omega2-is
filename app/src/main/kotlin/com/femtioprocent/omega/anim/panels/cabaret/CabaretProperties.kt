package com.femtioprocent.omega.anim.panels.cabaret

import com.femtioprocent.omega.anim.cabaret.Actor
import com.femtioprocent.omega.anim.cabaret.GImAE
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.swing.properties.OmegaProperties
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.tD
import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.MouseInputAdapter
import javax.swing.text.Document

class CabaretProperties(owner: JFrame?, cabp: CabaretPanel) : OmegaProperties(owner), ActionListener {
    var gbcf = GBC_Factory()
    var bound_act: Actor? = null
    var bound_act_ixx = -1
    var cabp: CabaretPanel
    var lesson_id: JTextField? = null
    var var1: JTextField? = null
    var var2: JTextField? = null
    var var3: JTextField? = null
    var image_name: JTextField? = null
    var image_petasknid: JTextField? = null
    var prim_scale: JTextField? = null
    var prim_mirror: JComboBox<String?>? = null
    var hotspot: JTextField? = null
    var rhotspot: JTextField? = null
    var delete: JButton? = null
    var im_pan: JPanel? = null
    private val m: Mouse
    private var hotspot_click = -1
    private var hotspot_near = -1
    private var hotx = 0
    private var hoty = 0
    private var hotx1 = 0
    private var hoty1 = 0
    private var hotx2 = 0
    private var hoty2 = 0
    private var skipDirty = false
    private fun hitNearest(p: Point2D, lim: Double, cnt: Int): Int {
	var r = -1
	var rr = -1
	var d = 9999999.9
	var a: Double
	if (p.distance(hotx.toDouble(), hoty.toDouble()).also { a = it } <= d && a <= lim) {
	    d = a + 1
	    if (cnt == 0) rr = 0
	    r = 0
	}
	if (p.distance(hotx1.toDouble(), hoty1.toDouble()).also { a = it } <= d && a <= lim) {
	    d = a + 1
	    if (cnt == 1) rr = 1
	    r = 1
	}
	if (p.distance(hotx2.toDouble(), hoty2.toDouble()).also { a = it } <= d && a <= lim) {
	    d = a + 1
	    if (cnt == 2) rr = 2
	    r = 2
	}
	return if (rr != -1) rr else r
    }

    internal inner class Mouse(p: JPanel) : MouseInputAdapter() {
	var mpress_p: Point2D? = null
	private var cnt = 0

	init {
	    p.addMouseListener(this)
	    p.addMouseMotionListener(this)
	}

	override fun mousePressed(e: MouseEvent) {
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    hotspot_click = hitNearest(mpress_p as Point2D.Double, 10.0, cnt)
	    repaint()
	}

	override fun mouseMoved(e: MouseEvent) {
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    val lhsn = hotspot_near
	    hotspot_near = hitNearest(mpress_p as Point2D.Double, 10.0, cnt)
	    if (hotspot_near == -1 && lhsn >= 0) {
		cnt++
		cnt %= 3
	    }
	    repaint()
	}

	override fun mouseDragged(e: MouseEvent) {
	    mpress_p = Point2D.Double(e.x.toDouble(), e.y.toDouble())
	    val imsc = ImageScale()
	    val mx = e.x.toDouble()
	    val my = e.y.toDouble()
	    val at = AffineTransform()
	    val iwf = imsc.iw * imsc.f
	    val ihf = imsc.ih * imsc.f
	    val hsx = mx / iwf
	    val hsy = my / ihf
	    imsc.gimae!!.setHotSpotIx(hotspot_click, hsx, hsy)
	    hotspot!!.text = "" + imsc.gimae!!.getHotSpotAsString(0)
	    rhotspot!!.text = "" + imsc.gimae!!.getHotSpotAsString(1)
	    AnimContext.ae!!.isDirty = true
	    repaint()
	}

	override fun mouseReleased(e: MouseEvent) {
	    hotspot_click = -1
	    hotspot_near = -1
	    repaint()
	}
    }

    private fun setDirty() {
	if (skipDirty == false) AnimContext.ae!!.isDirty = true
    }

    fun setLessonId(id: String?) {
	lesson_id!!.text = id
	pack()
    }

    fun setTarget(act: Actor?, ixx: Int) {
	skipDirty = true
	bound_act = act
	bound_act_ixx = ixx
	if (act != null) {
	    image_name!!.text = act.gimae.fNBase()
	    image_petasknid!!.text = act.gimae.peTaskNid
	    lesson_id!!.text = act.gimae.lessonId
	    var1!!.text = act.gimae.getVariable(1)
	    var2!!.text = act.gimae.getVariable(2)
	    var3!!.text = act.gimae.getVariable(3)
	    prim_scale!!.text = "" + act.gimae.primScale
	    prim_mirror!!.selectedIndex = act.gimae.primMirror
	    hotspot!!.text = "" + act.gimae.getHotSpotAsString(0)
	    rhotspot!!.text = "" + act.gimae.getHotSpotAsString(1)
	} else {
	    image_name!!.text = ""
	    image_petasknid!!.text = ""
	    lesson_id!!.text = ""
	    var1!!.text = ""
	    var2!!.text = ""
	    var3!!.text = ""
	    prim_scale!!.text = ""
	    prim_mirror!!.selectedIndex = 0
	    hotspot!!.text = ""
	    hotspot!!.text = ""
	}
	pack()
	repaint()
	skipDirty = false
    }

    inner class myDocumentListener : DocumentListener {
	override fun changedUpdate(de: DocumentEvent) {
	    val doc = de.document
	    updDoc(doc)
	}

	override fun insertUpdate(de: DocumentEvent) {
	    val doc = de.document
	    updDoc(doc)
	}

	override fun removeUpdate(de: DocumentEvent) {
	    val doc = de.document
	    updDoc(doc)
	}
    }

    var mydocl: myDocumentListener = myDocumentListener()

    inner class myItemListener : ItemListener {
	override fun itemStateChanged(ie: ItemEvent) {
	    val cb = ie.itemSelectable as JComboBox<*>
	    if (cb === prim_mirror) {
		val a = prim_mirror!!.selectedIndex
		if (bound_act != null) {
		    bound_act!!.gimae.primMirror = a
		    setDirty()
		}
	    }
	}
    }

    var myiteml: myItemListener = myItemListener()

    init {
	skipDirty = true
	this.cabp = cabp
	title = "Omega - " + t("Actor Properties")
	setSize(300, 200)
	buildProperties()
	m = Mouse(im_pan!!)
	skipDirty = false
    }

    //      class OnOffItemEvent implements ItemListener {
    //          public void itemStateChanged(ItemEvent ie) {
    //  	    JCheckBox cb = (JCheckBox)ie.getItemSelectable();
    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "+++++++++++++ toggle is " + cb.isSelected());
    //  	}
    //      };
    //      OnOffItemEvent onoff_listener = new OnOffItemEvent();
    internal inner class ImageScale {
	var gimae: GImAE? = null
	var im: Image? = null
	var iw = 0.0
	var ih = 0.0
	var fx = 0.0
	var fy = 0.0
	var f = 1.0

	init {
	    if (bound_act != null) {
		gimae = bound_act!!.gimae
		im = gimae!!.baseImage
		iw = im!!.getWidth(null).toDouble()
		ih = im!!.getHeight(null).toDouble()
		fx = IM_W / iw
		fy = IM_H / ih
		f = if (fx < fy) fx else fy
	    }
	}
    }

    fun drawImage(g: Graphics) {
	if (bound_act != null) {
	    val imsc = ImageScale()
	    val at = AffineTransform()
	    at.scale(imsc.f, imsc.f)
	    val g2 = g as Graphics2D
	    g2.color = Color.black
	    g2.fillRect(0, 0, (imsc.iw * imsc.f).toInt(), (imsc.ih * imsc.f).toInt())
	    g.drawImage(imsc.im, at, null)
	    hotx = (imsc.iw * imsc.f * imsc.gimae!!.hotspot.getX(0)).toInt()
	    hoty = (imsc.ih * imsc.f * imsc.gimae!!.hotspot.getY(0)).toInt()
	    hotx1 = (imsc.iw * imsc.f * imsc.gimae!!.hotspot.getX(1)).toInt()
	    hoty1 = (imsc.ih * imsc.f * imsc.gimae!!.hotspot.getY(1)).toInt()
	    hotx2 = (imsc.iw * imsc.f * imsc.gimae!!.hotspot.getX(2)).toInt()
	    hoty2 = (imsc.ih * imsc.f * imsc.gimae!!.hotspot.getY(2)).toInt()
	    drawHS(0, g2, hotx, hoty)
	    drawHS(1, g2, hotx1, hoty1)
	    drawHS(2, g2, hotx2, hoty2)
	    drawL(g2, hotx1, hoty1, hotx2, hoty2)
	}
    }

    private fun drawHS(ix: Int, g2: Graphics2D, hx: Int, hy: Int) {
	drawHS(ix, g2, hx + 1, hy + if (ix == 2) 0 else 1, Color.black)
	drawHS(
		ix,
		g2,
		hx,
		hy,
		if (hotspot_click == ix) Color.red else if (hotspot_near == ix) Color.yellow else Color.green
	)
    }

    private fun drawHS(ix: Int, g2: Graphics2D, hx: Int, hy: Int, col: Color) {
	g2.color = col
	when (ix) {
	    0 -> g2.drawArc(hx - 5, hy - 5, 10, 10, 0, 360)
	    1 -> {
		g2.drawLine(hx - 10, hy, hx + 10, hy)
		g2.drawLine(hx, hy - 10, hx, hy + 10)
	    }

	    2 -> {
		g2.drawLine(hx - 6, hy - 6, hx + 6, hy + 6)
		g2.drawLine(hx - 6, hy + 6, hx + 6, hy - 6)
	    }
	}
    }

    private fun drawL(
	    g2: Graphics2D,
	    hx: Int, hy: Int,
	    hx2: Int, hy2: Int
    ) {
	g2.drawLine(hx, hy, hx2, hy2)
    }

    private fun buildProperties() {
	val con = contentPane
	con.layout = BorderLayout()
	val impan: JPanel = object : JPanel() {
	    public override fun paintComponent(g: Graphics) {
		g.color = Color.white
		g.fillRect(0, 0, 1000, 1000)
		drawImage(g)
	    }
	}
	impan.preferredSize = Dimension(IM_W, IM_H)
	con.add(impan, BorderLayout.NORTH)
	im_pan = impan
	val pan = JPanel()
	pan.layout = GridBagLayout()
	var jb: JButton
	var Y = 0
	pan.add(JLabel(t("Actor ID")), gbcf.createL(0, Y, 1))
	pan.add(JTextField("            ", 20).also { lesson_id = it }, gbcf.create(1, Y))
	if (true) {
	    val doc2 = lesson_id!!.document
	    doc2.addDocumentListener(mydocl)
	}
	Y++
	pan.add(JLabel(t("Variables")), gbcf.createL(0, Y, 1))
	pan.add(JTextField("            ", 15).also { var1 = it }, gbcf.create(1, Y))
	pan.add(JTextField("            ", 15).also { var2 = it }, gbcf.create(2, Y))
	pan.add(JTextField("            ", 15).also { var3 = it }, gbcf.create(3, Y))
	if (true) {
	    var doc2 = var1!!.document
	    doc2.addDocumentListener(mydocl)
	    doc2 = var2!!.document
	    doc2.addDocumentListener(mydocl)
	    doc2 = var3!!.document
	    doc2.addDocumentListener(mydocl)
	}
	Y++
	pan.add(JLabel(t("Image name")), gbcf.createL(0, Y, 1))
	pan.add(JTextField("            ", 20).also { image_name = it }, gbcf.create(1, Y))
	pan.add(JButton(t("Set")).also { jb = it }, gbcf.create(2, Y))
	//image_name.setEditable(false);
	jb.actionCommand = "setImName"
	jb.addActionListener(this)
	pan.add(JButton(t("Delete")).also { jb = it }, gbcf.createR(3, Y))
	jb.foreground = Color(140, 0, 0)
	jb.actionCommand = "delete"
	jb.addActionListener(this)
	delete = jb
	Y++
	pan.add(JLabel(t("PeTask Nid")), gbcf.create(0, Y))
	pan.add(JTextField("", 2).also { image_petasknid = it }, gbcf.create(1, Y))
	if (true) {
	    val doc2 = image_petasknid!!.document
	    doc2.addDocumentListener(mydocl)
	}
	Y++
	pan.add(JLabel(t("Primary scale")), gbcf.createL(0, Y, 1))
	pan.add(JTextField(20).also { prim_scale = it }, gbcf.create(1, Y))
	if (true) {
	    val doc2 = prim_scale!!.document
	    doc2.addDocumentListener(mydocl)
	}
	Y++
	pan.add(JLabel(t("Primary mirror")), gbcf.createL(0, Y, 1))
	pan.add(JComboBox<String?>().also { prim_mirror = it }, gbcf.create(1, Y))
	prim_mirror!!.addItem(t("no mirror"))
	prim_mirror!!.addItem(t("mirror X"))
	prim_mirror!!.addItem(t("mirror Y"))
	prim_mirror!!.addItem(t("mirror X and Y"))
	prim_mirror!!.selectedIndex = 0
	prim_mirror!!.addItemListener(myiteml)
	Y++
	pan.add(JLabel(t("Rotation spot")), gbcf.createL(0, Y, 1))
	pan.add(JTextField(20).also { hotspot = it }, gbcf.create(1, Y))
	Y++
	pan.add(JLabel(t("Path spot")), gbcf.createL(0, Y, 1))
	pan.add(JTextField(20).also { rhotspot = it }, gbcf.create(1, Y))
	Y++
	hotspot!!.isEditable = false
	rhotspot!!.isEditable = false
	con.add(pan, BorderLayout.CENTER)
	val pan2 = JPanel()
	pan2.layout = FlowLayout()
	jb = JButton(t("Close"))
	jb.actionCommand = "Close"
	jb.addActionListener(this)
	pan2.add(jb)
	con.add(pan2, BorderLayout.SOUTH)
    }

    fun updDoc(doc: Document) {
	setDirty()
	if (doc === prim_scale!!.document) {
	    try {
		val d = tD(prim_scale!!.text)
		if (d == 0.0) prim_scale!!.foreground = Color.red else {
		    if (bound_act != null) bound_act!!.gimae.primScale = d
		    prim_scale!!.foreground = Color.black
		}
	    } catch (ex: Exception) {
		prim_scale!!.foreground = Color.red
	    }
	    repaint()
	}
	if (doc === lesson_id!!.document) {
	    val lid = lesson_id!!.text
	    if (bound_act != null) bound_act!!.gimae.lessonId = lid
	}
	if (doc === var1!!.document) {
	    val ss = var1!!.text
	    if (bound_act != null) bound_act!!.gimae.setVariable(1, ss)
	}
	if (doc === var2!!.document) {
	    val ss = var2!!.text
	    if (bound_act != null) bound_act!!.gimae.setVariable(2, ss)
	}
	if (doc === var3!!.document) {
	    val ss = var3!!.text
	    if (bound_act != null) bound_act!!.gimae.setVariable(3, ss)
	}
	if (doc === image_petasknid!!.document) {
	    val petnid = image_petasknid!!.text
	    if (bound_act != null) bound_act!!.gimae.xim.peTaskNid = petnid
	}
    }

    override fun actionPerformed(ev: ActionEvent) {
	if (ev.actionCommand == "setImName") {
	    cabp.replaceActor(bound_act_ixx)
	    repaint()
	    return
	}
	if (ev.actionCommand == "delete") {
	    val rsp = JOptionPane.showConfirmDialog(
		    this,
		    t("Are you sure to delete the Actor?"),
		    "Omega",
		    JOptionPane.YES_NO_OPTION
	    )
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "*******) " + rsp);
	    if (rsp == 0) cabp.deleteActor(bound_act_ixx)
	    repaint()
	    return
	}
	if (ev.actionCommand == "Close") {
	    isVisible = false
	    return
	}
    }

    //      public void valueChanged(ListSelectionEvent ev) {
    //  	JList jl = (JList)ev.getSource();
    //  	int ix = jl.getSelectedIndex();
    //  	hotspot_selected_ix = ix;
    //  	repaint();
    //      }
    fun enableDelete(b: Boolean) {
	delete!!.isEnabled = b
    }

    companion object {
	//ListSelectionListener {
	const val IM_W = 300
	const val IM_H = 100
    }
}
