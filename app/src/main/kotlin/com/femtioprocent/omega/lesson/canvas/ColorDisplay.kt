package com.femtioprocent.omega.lesson.canvas

import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.canvas.BaseCanvas.ColorColors
import com.femtioprocent.omega.swing.ColorChooser.select
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.io.File
import java.io.FilenameFilter
import javax.swing.*

class ColorDisplay(var colors_orig: HashMap<String, ColorColors>, var who: String, val callback: (me: ColorDisplay) -> Unit) : JDialog(), ActionListener {
    //    JDialog(LessonEditor.TOP_JFRAME, true), ActionListener {
    var WW = 700
    var HH = 500
    var colors: HashMap<String, ColorColors>? = null
    var select = false
    var color_file: JComboBox<String>? = null

    inner class myItemListener : ItemListener {
	override fun itemStateChanged(ie: ItemEvent) {
	    val cb = ie.itemSelectable as JComboBox<*>
	    if (ie.stateChange == ItemEvent.SELECTED) if (cb === color_file) {
		val fn = cb.selectedItem as String
		//log		    OmegaContext.sout_log.getLogger().info(":--: " + "FILE sel " + fn);
		val hm = Lesson.getColors(fn, who)
		if (hm != null) {
		    colors_orig = HashMap()
		    for ((key, value) in hm) colors_orig[key!!] = ColorColors(value, null)
		    repaint()
		}
	    }
	}
    }

    var myiteml: myItemListener = myItemListener()

    inner class Canvas : JPanel() {
	val caW: Int
	    get() = width
	val caH: Int
	    get() = height

	fun gW(f: Double): Int {
	    return (f * caW).toInt()
	}

	fun gH(f: Double): Int {
	    return (f * caH).toInt()
	}

	public override fun paintComponent(g: Graphics) {
	    val g2 = g as Graphics2D
	    val rh = g2.renderingHints
	    rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	    g2.setRenderingHints(rh)
	    val hh = gW(0.3)
	    var pa = GradientPaint(
		    0.0f, 0.0f, getColor("bg_t"),
		    0.0f, hh.toFloat(), getColor("bg_m")
	    )
	    g2.paint = pa
	    g2.fill(Rectangle(0, 0, caW, hh))
	    pa = GradientPaint(
		    0.0f, hh.toFloat(), getColor("bg_m"),
		    0.0f, caH.toFloat(), getColor("bg_b")
	    )
	    g2.paint = pa
	    g2.fill(Rectangle(0, hh - 1, caW, caH - hh + 1))
	    if (who == "selector") {
		val pa2 = GradientPaint(
			0.0f, gH(0.01).toFloat(), getColor("bg_b"),
			0.0f, gH(0.98).toFloat(), getColor("bg_t")
		)
		g2.paint = pa2
		g2.fill(Rectangle(gW(0.25), gH(0.1), gW(0.7), gH(0.8)))
	    }
	    if (who != "words") {
		val l1 = (0.1 * caW).toInt()
		val w1 = (0.8 * caW).toInt()
		val t1 = (0.1 * caH).toInt()
		val h1 = (0.2 * caH).toInt()
		val h1lowoffs = (0.15 * caH).toInt()
		val h1low = (0.05 * caH).toInt()
		val l1_1 = (0.3 * caW).toInt()
		val w1_1 = (0.25 * caW).toInt()
		val rad = 15
		if (who != "main") {
		    val rr: RoundRectangle2D = RoundRectangle2D.Double(
			    l1.toDouble(),
			    t1.toDouble(),
			    w1.toDouble(),
			    h1.toDouble(),
			    rad.toDouble(),
			    rad.toDouble()
		    )
		    g2.color = getColor("bg_frbg")
		    g2.fill(rr)
		    g2.color = getColor("bg_fr")
		    val stroke = BasicStroke(caH / 100.0f)
		    g2.stroke = stroke
		    g2.draw(rr)
		    g2.color = getColor("bg_tx")
		    val fo = Font("Arial", Font.PLAIN, (h1 * 0.65).toInt())
		    g2.font = fo
		    g2.drawString("Message area", l1 + 10, (t1 + h1 * 0.7).toInt())
		}
	    } else {
		if (true) {
		    val l1 = (0.1 * caW).toInt()
		    val w1 = (0.8 * caW).toInt()
		    val t1 = (0.1 * caH).toInt()
		    val h1 = (0.2 * caH).toInt()
		    val h1lowoffs = (0.15 * caH).toInt()
		    val h1low = (0.05 * caH).toInt()
		    val l1_1 = (0.3 * caW).toInt()
		    val w1_1 = (0.25 * caW).toInt()
		    var r: Rectangle2D = Rectangle2D.Double(l1.toDouble(), t1.toDouble(), w1.toDouble(), h1.toDouble())
		    g2.color = getColor("sn_bg")
		    g2.fill(r)
		    r = Rectangle2D.Double(
			    l1_1.toDouble(),
			    (t1 + h1lowoffs).toDouble(),
			    w1_1.toDouble(),
			    h1low.toDouble()
		    )
		    g2.color = getColor("sn_hi")
		    g2.fill(r)
		    r = Rectangle2D.Double(l1.toDouble(), t1.toDouble(), w1.toDouble(), h1.toDouble())
		    g2.color = getColor("sn_fr")
		    val stroke = BasicStroke(caH / 100.0f)
		    g2.stroke = stroke
		    g2.draw(r)
		    g2.color = getColor("sn_tx")
		    val fo = Font("Arial", Font.PLAIN, (h1 * 0.65).toInt())
		    g2.font = fo
		    g2.drawString("The fox jumps down", l1 + 10, (t1 + h1 * 0.7).toInt())
		}
		val sa = arrayOf("The fox", "jumps", "down")
		for (i in 0..2) {
		    val l1 = (0.1 * caW).toInt()
		    val w1 = (0.8 * caW).toInt()
		    val t1 = ((0.4 + i * 0.18) * caH).toInt()
		    val h1 = (0.15 * caH).toInt()
		    val ra = 15.0
		    val r: RoundRectangle2D =
			    RoundRectangle2D.Double(l1.toDouble(), t1.toDouble(), w1.toDouble(), h1.toDouble(), ra, ra)
		    g2.color = getColor(if (i == 1) "bt_hi" else if (i == 2) "bt_hs" else "bt_bg")
		    g2.fill(r)
		    g2.color = getColor(if (i == 1) "bt_fr_hi" else if (i == 2) "bt_fr_hs" else "bt_fr")
		    val stroke = BasicStroke(caH / 100.0f)
		    g2.stroke = stroke
		    g2.draw(r)
		    g2.color = getColor(if (i == 1) "bt_tx_hi" else if (i == 2) "bt_tx_hs" else "bt_tx")
		    val fo = Font("Arial", Font.PLAIN, (h1 * 0.65).toInt())
		    g2.font = fo
		    g2.drawString(sa[i], l1 + 10, (t1 + h1 * 0.8).toInt())
		}
		if (true) {
		    val x = (0.45 * caW).toInt()
		    val y = (0.5 * caH).toInt()
		    val w = (0.48 * caW).toInt()
		    val h = (0.3 * caH).toInt()
		    val r: RoundRectangle2D =
			    RoundRectangle2D.Double(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), 15.0, 15.0)
		    g2.color = getColor("bg_frbg")
		    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f)
		    g2.fill(r)
		    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
		    g2.color = getColor("bg_fr")
		    val stroke = BasicStroke(caH / 100.0f)
		    g2.stroke = stroke
		    g2.draw(r)
		    g2.color = getColor("bg_tx")
		    val fo = Font("Arial", Font.PLAIN, (h * 0.35).toInt())
		    g2.font = fo
		    g2.drawString("Message", x + 10, (y + h * 0.6).toInt())
		}
	    }
	}
    }

    var can: Canvas? = null
    fun getColor(id: String): Color {
	return colors_orig[id]!!.color as Color
    }

    init {
	colors_orig = colors_orig.clone() as HashMap<String, ColorColors>
	build()
	setSize(WW, HH)
    }

    fun ma(jm: JMenu, txt: String?, cmd: String?) {
	val mi = JMenuItem(txt)
	mi.actionCommand = cmd
	mi.addActionListener(this)
	jm.add(mi)
    }

    override fun actionPerformed(ae: ActionEvent) {
	val cmd: String
	//  	cmd = (String)mi.getActionCommand();
	cmd = ae.actionCommand as String
	if (cmd == "select") {
	    colors = colors_orig
	    colors_orig.putAll(colors_orig)
	    select = true
	    isVisible = false
	    callback(this)
	    return
	}
	if (cmd == "restore") {
	    //	    color_file.setSelectedIndex(0);
	    repaint()
	    return
	}
	if (cmd == "dismiss") {
	    isVisible = false
	    return
	}
	//	JMenuItem mi = (JMenuItem)ae.getSource();
	val c = colors_orig[cmd]!!.color
	val nc = select(c)
	if (nc != null) colors_orig[cmd] = ColorColors(nc, null)
	repaint()
    }

    private fun fillColorFiles() {
	val dir = File(omegaAssets("."))
	val sa = dir.list(fnf)
	for (i in sa.indices) if (color_file != null) color_file!!.addItem(sa[i])
    }

    fun build() {
	can = Canvas()
	val c = contentPane

// 	color_file = new JComboBox();
// 	color_file.addItemListener(myiteml);
// 	color_file.addItem("");
	fillColorFiles()

	c.add(JPanel().also {
// 	    it.add(color_file);
	    it.add(JButton("Restore").also {
		it.actionCommand = "restore"
		it.addActionListener(this)
	    })
	}, BorderLayout.NORTH)

	c.add(can, BorderLayout.CENTER)

	c.add(JPanel().also {
	    it.add(JButton(t("Select")).also {
		it.actionCommand = "select"
		it.addActionListener(this)
	    })
	    it.add(JButton(t("Cancel")).also {
		it.actionCommand = "dismiss"
		it.addActionListener(this)
	    })
	}, BorderLayout.SOUTH)

	val mb = JMenuBar()
	jMenuBar = mb
	var jm = JMenu(t("Background"))
	mb.add(jm)
	ma(jm, t("Top"), "bg_t")
	ma(jm, t("Middle"), "bg_m")
	ma(jm, t("Bottom"), "bg_b")
	if (who != "main") {
	    jm = JMenu(t("MessageArea"))
	    mb.add(jm)
	    ma(jm, t("Background"), "bg_frbg")
	    ma(jm, t("Text"), "bg_tx")
	    ma(jm, t("Frame"), "bg_fr")
	}
	if (who != "words") {
	} else {
	    jm = JMenu(t("Sentence"))
	    mb.add(jm)
	    ma(jm, t("Background"), "sn_bg")
	    ma(jm, t("Hilite"), "sn_hi")
	    jm.addSeparator()
	    ma(jm, t("Text"), "sn_tx")
	    ma(jm, t("Frame"), "sn_fr")
	    jm = JMenu(t("Words"))
	    mb.add(jm)
	    ma(jm, t("Background"), "bt_bg")
	    ma(jm, t("Background Hilite"), "bt_hi")
	    ma(jm, t("Background Hilite Selected"), "bt_hs")
	    jm.addSeparator()
	    ma(jm, t("Text"), "bt_tx")
	    ma(jm, t("Text Hilite"), "bt_tx_hi")
	    ma(jm, t("Text Hilite Selected"), "bt_tx_hs")
	    jm.addSeparator()
	    ma(jm, t("Frame"), "bt_fr")
	    ma(jm, t("Frame Hilite"), "bt_fr_hi")
	    ma(jm, t("Frame Hilite Selected"), "bt_fr_hs")
	}
    }

    companion object {
	var fnf = FilenameFilter { dir, fname -> fname.endsWith(".omega_colors") }
    }
}
