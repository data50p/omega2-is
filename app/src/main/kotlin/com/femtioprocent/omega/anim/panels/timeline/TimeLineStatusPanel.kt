package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.BevelBorder
import javax.swing.event.MouseInputAdapter

class TimeLineStatusPanel(var tlp: TimeLinePanel) : JPanel() {
    var pos_st: JLabel
    var pos_m_st: JLabel
    var info: JLabel
    var status: JLabel

    internal inner class Mouse : MouseInputAdapter() {
	override fun mousePressed(e: MouseEvent) {
	    val pd = Point(e.x, e.y)
	}

	override fun mouseDragged(e: MouseEvent) {
	    val pd = Point(e.x, e.y)
	}
    }

    init {
	val mouse: Mouse = Mouse()
	addMouseListener(mouse)
	addMouseMotionListener(mouse)
	val p = JPanel()
	layout = BorderLayout()
	p.layout = GridBagLayout()
	p.layout = FlowLayout()
	p.background = Color(180, 180, 180)
	val ins = Insets(0, 0, 0, 0)
	val con = GridBagConstraints(
	    0, 0,
	    1, 1,
	    1.0, 1.0,
	    GridBagConstraints.CENTER,
	    GridBagConstraints.HORIZONTAL,
	    ins,
	    0, 0
	)
	info = JLabel(t("Info:") + "                                       ")
	info.border = BevelBorder(BevelBorder.LOWERED)
	info.foreground = Color.black
	p.add(info, con)
	status = JLabel(t("Status:") + "                                    ")
	status.border = BevelBorder(BevelBorder.LOWERED)
	status.foreground = Color.black
	con.gridx++
	p.add(status, con)
	pos_st = JLabel(t("Time Pos:") + " " + tlp.hitMS)
	pos_st.border = BevelBorder(BevelBorder.LOWERED)
	pos_st.foreground = Color.black
	pos_st.preferredSize = Dimension(130, 22)
	con.gridx++
	p.add(pos_st, con)
	pos_m_st = JLabel(t("Mouse Pos:") + " " + tlp.hitMS)
	pos_m_st.border = BevelBorder(BevelBorder.LOWERED)
	pos_m_st.foreground = Color.black
	pos_m_st.preferredSize = Dimension(180, 22)
	con.gridx++
	p.add(pos_m_st, con)
	add(p, BorderLayout.EAST)
	tlp.addTimeLinePanelListener(object : TimeLinePanelAdapter() {
	    override fun updateValues() {
		this@TimeLineStatusPanel.updateValues()
	    }
	})
    }

    fun updateValues() {
	pos_st.text = "Time Pos: " + tlp.hitMS
	pos_m_st.text = t("Mouse Pos:") + " " + tlp.mouseHitMS + ' ' + (tlp.mouseHitMS - tlp.hitMS)
	info.text = t("Info:") + " " + '?' //tlp.th.speed);
    }

    //      public Dimension getPreferredSize() {
    //  	return new Dimension(1000, 30);
    //      }
    public override fun paintComponent(g: Graphics) {
	super.paintComponent(g)
	val g2 = g as Graphics2D
    }
}
