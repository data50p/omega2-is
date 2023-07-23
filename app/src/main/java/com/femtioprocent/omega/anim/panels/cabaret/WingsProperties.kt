package com.femtioprocent.omega.anim.panels.cabaret

import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventSetLayer
import com.femtioprocent.omega.anim.tool.timeline.TriggerEventSetMirror
import com.femtioprocent.omega.graphic.render.Wing
import com.femtioprocent.omega.swing.GBC_Factory
import com.femtioprocent.omega.swing.properties.OmegaProperties
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.tD
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.geom.AffineTransform
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

class WingsProperties(owner: JFrame?, wings_pan: WingsPanel) : OmegaProperties(owner), ActionListener {
    var gbcf = GBC_Factory()
    var boundWing: Wing? = null
    var bound_wing_ixx = -1
    var wings_pan: WingsPanel
    var image_name: JTextField? = null
    var layer: JComboBox<*>? = null
    var mirror: JComboBox<*>? = null
    var scale: JTextField? = null
    var position: JTextField? = null
    var delete: JButton? = null
    var set_pos: JButton? = null
    var selections = TriggerEventSetLayer.st_selections_cmd
    var mirror_selections = TriggerEventSetMirror.st_selections_human
    private var skipDirty = false

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
	    if (cb === layer) {
		if (boundWing != null) {
		    val l = layer!!.selectedIndex
		    if (l >= 0 && l <= 5) {
			boundWing!!.layer = l
			wings_pan.ae.a_ctxt!!.anim_canvas!!.updWings()
			setDirty()
		    } else {
		    }
		}
	    }
	    if (cb === mirror) {
		if (boundWing != null) {
		    val l = mirror!!.selectedIndex
		    if (l >= 0 && l < 4) {
			boundWing!!.mirror = l
			wings_pan.ae.a_ctxt!!.anim_canvas!!.updWings()
			wings_pan.ae.a_ctxt!!.anim_canvas!!.resetBackground()
			setDirty()
		    } else {
		    }
		}
	    }
	}
    }

    var myiteml: myItemListener = myItemListener()
    var timer: Timer? = null
    fun updDoc(doc: Document) {
	if (doc === scale!!.document) {
	    if (timer == null) {
		timer = Timer(1000,
			object : ActionListener {
			    override fun actionPerformed(ae: ActionEvent) {
				val fbound_wing: Wing = boundWing!!
				if (fbound_wing != null) {
				    fbound_wing.scale = tD(scale!!.text)
				    if (fbound_wing.scale == 0.0) {
					fbound_wing.scale = 1.0
					scale!!.foreground = Color.red
				    } else {
					scale!!.foreground = Color.black
				    }
				    wings_pan.ae.a_ctxt!!.anim_canvas!!.updWings()
				    wings_pan.ae.a_ctxt!!.anim_canvas!!.resetBackground()
				    timer!!.stop()
				}
			    }
			})
		timer!!.isCoalesce = true
	    }
	    timer!!.start()
	}
	setDirty()
	repaint()
    }

    init {
	skipDirty = true
	this.wings_pan = wings_pan
	title = t("Wings") + " - " + t("Properties")
	setSize(300, 200)
	buildProperties()
	pack()
	skipDirty = false
    }

    private fun setDirty() {
	if (skipDirty == false) AnimContext.ae!!.isDirty = true
    }

    fun updPos(w: Wing?, dx: Int, dy: Int) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updPOS " + w + ' ' + bound_wing);
	if (w == boundWing) position!!.text = "" + (boundWing!!.pos.getX() + dx) + ' ' + (boundWing!!.pos.getY() + dy)
    }

    fun setTarget(wing: Wing?, ixx: Int) {
	skipDirty = true
	boundWing = wing
	bound_wing_ixx = ixx
	if (wing != null) {
	    image_name!!.text = wing.name
	    position!!.text = "" + wing.pos.getX() + ' ' + wing.pos.getY()
	    layer!!.selectedItem = selections[wing.layer]
	    scale!!.text = "" + wing.scale
	    mirror!!.setSelectedItem(mirror_selections[wing.mirror])
	} else {
	    image_name!!.text = ""
	    position!!.text = ""
	    layer!!.selectedIndex = 2
	    scale!!.text = ""
	    mirror!!.setSelectedIndex(0)
	}
	pack()
	repaint()
	skipDirty = false
    }

    fun drawImage(g: Graphics) {
	if (boundWing != null) {
	    val im = boundWing!!.im
	    val at = AffineTransform()
	    val iw = im.getWidth(null).toDouble()
	    val ih = im.getHeight(null).toDouble()
	    val fx = IM_W / iw
	    val fy = IM_H / ih
	    val f = if (fx < fy) fx else fy
	    at.scale(f, f)
	    val g2 = g as Graphics2D
	    g2.color = Color.black
	    g2.fillRect(0, 0, (iw * f).toInt(), (ih * f).toInt())
	    g.drawImage(im, at, null)
	}
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
	val pan = JPanel()
	pan.layout = GridBagLayout()
	var jb: JButton
	var Y = 0
	pan.add(JLabel(t("Image name")), gbcf.createL(0, Y, 1))
	pan.add(JTextField("            ", 20).also { image_name = it }, gbcf.create(1, Y))
	image_name!!.isEditable = false
	pan.add(JButton(t("Add")).also { jb = it }, gbcf.create(2, Y))
	jb.actionCommand = "addImName"
	jb.addActionListener(this)
	pan.add(JButton(t("Delete")).also { jb = it }, gbcf.createR(3, Y))
	jb.foreground = Color(140, 0, 0)
	jb.actionCommand = "delete"
	jb.addActionListener(this)
	delete = jb
	Y++
	pan.add(JLabel(t("Layer")), gbcf.createL(0, Y, 1))
	pan.add(JComboBox<Any?>(selections).also { layer = it }, gbcf.create(1, Y))
	layer!!.selectedIndex = 2
	layer!!.isEditable = false
	layer!!.addItemListener(myiteml)
	Y++
	pan.add(JLabel(t("Mirror")), gbcf.createL(0, Y, 1))
	pan.add(JComboBox<Any?>(mirror_selections).also { mirror = it }, gbcf.create(1, Y))
	mirror!!.selectedIndex = 0
	mirror!!.isEditable = false
	mirror!!.addItemListener(myiteml)
	Y++
	pan.add(JLabel(t("Scale")), gbcf.createL(0, Y, 1))
	pan.add(JTextField("1.0").also { scale = it }, gbcf.create(1, Y))
	scale!!.isEditable = true
	if (true) {
	    val doc = scale!!.document
	    doc.addDocumentListener(mydocl)
	}
	Y++
	pan.add(JLabel(t("Position")), gbcf.createL(0, Y, 1))
	pan.add(JTextField(20).also { position = it }, gbcf.create(1, Y))
	pan.add(JButton(t("Set position")).also { jb = it }, gbcf.createR(2, Y))
	position!!.isEditable = false
	jb.actionCommand = "setPos"
	jb.addActionListener(this)
	set_pos = jb
	Y++
	con.add(pan, BorderLayout.CENTER)
	val pan2 = JPanel()
	pan2.layout = FlowLayout()
	jb = JButton(t("Close"))
	jb.actionCommand = "Close"
	jb.addActionListener(this)
	pan2.add(jb)
	con.add(pan2, BorderLayout.SOUTH)
    }

    fun cancelPos() {
	set_pos!!.text = t("Set position")
	set_pos!!.actionCommand = "setPos"
    }

    override fun actionPerformed(ev: ActionEvent) {
	if (ev.actionCommand == "addImName") {
	    wings_pan.replaceWing(bound_wing_ixx)
	    wings_pan.on(bound_wing_ixx)
	    setDirty()
	    return
	}
	if (ev.actionCommand == "setPos") {
	    wings_pan.on(bound_wing_ixx)
	    setDirty()
	    return
	}
	if (ev.actionCommand == "delete") {
	    val rsp = JOptionPane.showConfirmDialog(this,
		    t("Are you sure to delete the Wing?"),
		    "Omega",
		    JOptionPane.YES_NO_OPTION)
	    //log	    OmegaContext.sout_log.getLogger().info(":--: " + "*******) " + rsp);
	    if (rsp == 0) {
		wings_pan.removeWing(bound_wing_ixx)
		setDirty()
	    }
	    return
	}
	if (ev.actionCommand == "Close") {
	    isVisible = false
	    return
	}
    }

    override fun setVisible(b: Boolean) {
	super.setVisible(b)
    }

    fun enableDelete(b: Boolean) {
	delete!!.isEnabled = b
    }

    companion object {
	const val IM_W = 300
	const val IM_H = 100
    }
}
