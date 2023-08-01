package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.anim.panels.timeline.TimeMarkerDraw.draw
import com.femtioprocent.omega.anim.tool.timeline.MasterTimeLine
import com.femtioprocent.omega.anim.tool.timeline.MasterTimeLine.Companion.maxTimeLineIndex
import com.femtioprocent.omega.anim.tool.timeline.TimeLine
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker
import com.femtioprocent.omega.swing.Popup
import com.femtioprocent.omega.t9n.T.Companion.t
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.event.EventListenerList
import javax.swing.event.MouseInputAdapter

class TimeLinePanel(val mtl: MasterTimeLine) : JPanel() {
    private val listeners: EventListenerList
    var lock = false
    var selected_tl = -1
    var scale = 10
    private val grid = 1
    private var press_p: Point? = null
    var mouseHitMS = 0
	private set
    private var hitMode = HITM_NORMAL
    var tick_start = 0
    var hitMS = 0
	private set
    private var ltick_h = 0
    private var tmp: TimeMarkerProperties? = null
    private var last_was_back = false

    internal inner class Mouse : MouseInputAdapter() {
	override fun mousePressed(e: MouseEvent) {
	    val pd = Point(e.x, e.y)
	    press_p = pd
	    if (e.y < TL_M_SCAN) {
		hitMode = HITM_scan_object
		var t_h = (e.x - TLOFF) * scale / 10
		t_h *= 10
		setTick(t_h)
		firePropertyChange()
		mtl.playAt(hitMS)
		repaint()
	    } else {
		val old_selected_tl = selected_tl
		deselectAllTimeLine()
		val tl_ix = hitTimeLine(pd)
		selected_tl = tl_ix
		val tl = mtl.getTimeLine(selected_tl) ?: return
		fireEvent("selectTL", tl)
		val mod = e.modifiersEx
		val pt = e.isPopupTrigger // macOS ?
		if (pt) {
		    if (e.isControlDown) {
			popup_ctrl_maction(e, tl)
		    } else {
			popup_maction(e, tl)
		    }
		} else {
		    normal_maction(e, tl)
		}
	    }
	}

	override fun mouseDragged(e: MouseEvent) {
	    if (hitMode == HITM_scan_object) {
		var t_h = (e.x - TLOFF) * scale / 10
		t_h *= 10
		setTick(t_h)
		if (hitMS < mtl.lastTimeTick) last_was_back = true
		firePropertyChange()
		if (hitMS > mtl.lastTimeTick && last_was_back) {
		    mtl.lastTimeTick = 0
		    last_was_back = false
		}
		mtl.playAt(hitMS)
		repaint()
	    } else if (hitMode == HITM_NORMAL) {
		if (selected_tl != -1 && lock == false) {
		    val tl = mtl.getTimeLine(selected_tl)
		    if (tl != null) {
			if (e.isShiftDown) {
			    var dx = scale * (e.x - press_p!!.getX()).toInt()
			    dx = tl.adjustMove(dx)
			    AnimContext.ae!!.isDirty = true
			    if (dx != 0) {
				tl.move(dx)
				press_p = Point(e.x, e.y)
			    }
			    firePropertyChange()
			    mtl.playAt(hitMS)
			    repaint()
			} else if (e.isControlDown) {
			    val dx = scale * (e.x - press_p!!.getX()).toInt()
			    if (dx != 0) {
				val tm = tl.selectedTimeMarker
				if (tmp != null) tmp!!.setObject(tm)
				if (tm!!.type == TimeMarker.END) {
				    playEnd = e.x * scale
				    if (playEnd < tick_start) playEnd = tick_start
				    mtl.updateEndMarkers(playEnd)
				    AnimContext.ae!!.isDirty = true
				    repaint()
				} else if (tm.type == TimeMarker.BEGIN) {
				    if (false) {
					tick_start = (e.x - TLOFF) * scale
					if (tick_start < 0) tick_start = 0
					if (tick_start > playEnd) tick_start = playEnd
					mtl.updateBeginMarkers(tick_start)
					AnimContext.ae!!.isDirty = true
					repaint()
				    }
				} else if (tm.type == TimeMarker.STOP) {
				    tl.adjustSomeTimeMarkerRelative(dx.toDouble() / (tm.`when` - tl.offset))
				    tl.moveSelectedTimeMarker(dx, grid)
				    AnimContext.ae!!.isDirty = true
				} else {
				    tl.moveSelectedTimeMarker(dx, grid)
				    AnimContext.ae!!.isDirty = true
				}
				press_p = Point(e.x, e.y)
			    }
			    repaint()
			} else {
			}
		    }
		}
	    }
	}

	override fun mouseMoved(e: MouseEvent) {
	    if (e.y > TL_M_SCAN) {
		mouseHitMS = (e.x - TLOFF) * scale / 10
		mouseHitMS *= 10
		firePropertyChange()
		repaint()
	    }
	}

	override fun mouseReleased(e: MouseEvent) {
	    val pd = Point(e.x - TLOFF, e.y)
	    val old_selected_tl = selected_tl
	    deselectAllTimeLine()
	    val tl_ix = hitTimeLine(pd)
	    selected_tl = tl_ix
	    val tl = mtl.getTimeLine(selected_tl) ?: return
	    fireEvent("selectTL", tl)
	    val mod = e.modifiersEx
	    val pt = e.isPopupTrigger // macOS ?
	    if (pt) {
		if (e.isControlDown) {
		    popup_ctrl_maction(e, tl)
		} else {
		    popup_maction(e, tl)
		}
	    } else {
		hitMode = HITM_NORMAL
		firePropertyChange()
	    }
	}
    }

    var name = arrayOfNulls<JLabel>(4)
    fun updName() {
	if (name[0] == null) for (i in 0..3) {
	    name[i] = JLabel(t("name"))
	    name[i]!!.setLocation(5, TL_M_SCAN + 4 + i * TL_H - 1)
	    name[i]!!.foreground = Color.white
	    add(name[i])
	}
    }

    fun popupProp() {
	try {
	    val nid = 0 //id.charAt(1) - '0';
	    val owner = this@TimeLinePanel.topLevelAncestor as JFrame
	    if (tmp == null) tmp = TimeMarkerProperties(mtl.a_ctxt, owner)
	    val tl = mtl.getTimeLine(nid)
	    val tm = tl!!.getNearestTimeMarker(0)
	    tmp!!.setObject(tm)
	    tmp!!.isVisible = true
	} catch (ex: NullPointerException) {
	}
    }

    fun setSelectedTM(tm: TimeMarker?) {
	if (tm != null) tm.selected = true
	if (tmp != null) tmp!!.setObject(tm)
    }

    fun normal_maction(e: MouseEvent, tl: TimeLine?) {
//		    TimeLine tl = mtl.getTimeLine(selected_tl);
	if (tl != null) {
	    val tm = tl.getNearestTimeMarker((e.x - TLOFF) * scale)
	    setSelectedTM(tm)
	}
	repaint()
	hitMode = HITM_NORMAL
    }

    fun popup_maction(e: MouseEvent, tl: TimeLine) {
	val choice = arrayOf(
		t("Marker properties"),
		t("Add timesync"),
		t("Add trigger"),
		"",
		t("Cancel")
	)
	val pop = Popup(this@TimeLinePanel)
	pop.popup(t("Marker"), choice, e.x, e.y) { ev ->
	    var ix = -1
	    try {
		ix = ev.actionCommand.toInt()
	    } catch (ex: Exception) {
	    }
	    if (ix >= 0 && ix < choice.size) {
		when (ix) {
		    0 -> {
			val owner = this@TimeLinePanel.topLevelAncestor as JFrame
			if (tmp == null) tmp = TimeMarkerProperties(mtl.a_ctxt, owner)
			val tm = tl.getNearestTimeMarker((e.x - TLOFF) * scale)
			setSelectedTM(tm)
			tmp!!.isVisible = true
		    }

		    1, 2 -> {
			var tmty = 0.toChar()
			var duration = 0
			tmty = TimeMarker.TRIGGER
			if (ix == 1) {
			    tmty = TimeMarker.TSYNC
			    duration = 0
			}
			tl.addMarker(tmty, (e.x - TLOFF) * scale - tl.offset, duration)
			AnimContext.ae!!.isDirty = true
			fireEvent("addMarker", tl)
			repaint()
			hitMode = HITM_NORMAL
		    }
		}
	    }
	}
    }

    fun popup_ctrl_maction(e: MouseEvent, tl: TimeLine?) {
//TimeLine tl = mtl.getTimeLine(selected_tl);
	if (tl != null) {
	    val tm = tl.getNearestTimeMarker((e.x - TLOFF) * scale)
	    if (tm != null) {
		tm.selected = true
		val choice = arrayOf(
			t("Marker properties"),
			t("Marker delete"),
			"",
			t("Cancel")
		)
		val pop = Popup(this@TimeLinePanel)
		pop.popup("Marker", choice, e.x, e.y) { ev -> // REWRITE THIS
		    var ix = -1
		    try {
			ix = ev.actionCommand.toInt()
		    } catch (ex: Exception) {
		    }
		    when (ix) {
			0 -> {
			    val owner = this@TimeLinePanel.topLevelAncestor as JFrame
			    if (tmp == null) tmp = TimeMarkerProperties(mtl.a_ctxt, owner)
			    tmp!!.setObject(tm)
			    tmp!!.isVisible = true
			}

			1 -> if (tm.canRemove()) {
			    var tix = -1
			    if (tm.type == TimeMarker.TSYNC) {
				var i = 0
				while (i < 1000) {
				    val tmi = tm.tl.getMarkerAtIndexType(i, TimeMarker.TSYNC)
				    if (tmi == tm) {
					tix = i
					break
				    }
				    i++
				}
				if (tix != -1) {
				    tm.isDeleteCandidate = true
				    repaint()
				    if (JOptionPane.showConfirmDialog(
						    AnimContext.ae,
						    t("Delete selected red timesync?"),
						    "Omega",
						    JOptionPane.YES_NO_OPTION
					    ) == 0
				    ) {
					tm.tl.removeMarker(tm)
					AnimContext.ae!!.isDirty = true
					fireEvent("delMarker", tix)
				    } else tm.isDeleteCandidate = false
				    repaint()
				}
			    } else if (tm.type == TimeMarker.TRIGGER) {
				var i = 0
				while (i < 1000) {
				    val tmi = tm.tl.getMarkerAtIndexType(i, TimeMarker.TRIGGER)
				    if (tmi == tm) {
					tix = i
					break
				    }
				    i++
				}
				if (tix != -1) {
				    tm.isDeleteCandidate = true
				    repaint()
				    if (JOptionPane.showConfirmDialog(
						    AnimContext.ae,
						    t("Delete selected red trigger?"),
						    "Omega",
						    JOptionPane.YES_NO_OPTION
					    ) == 0
				    ) {
					tm.tl.removeMarker(tm)
					AnimContext.ae!!.isDirty = true
				    } else tm.isDeleteCandidate = false
				    repaint()
				}
			    }
			    repaint()
			}
		    }
		}
	    }
	}
	repaint()
	hitMode = HITM_NORMAL
    }

    fun setTick(t: Int) {
	ltick_h = hitMS
	hitMS = t
	if (hitMS < ltick_h) repaint() else repaint(TLOFF + ltick_h / scale - 5, 0, TLOFF + hitMS / scale + 5, 200)
    }

    fun firePropertyChange() {
	val lia = listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as TimeLinePanelListener).updateValues()
	    i += 2
	}
    }

    fun fireEvent(evs: String?, o: Any?) {
	val lia = listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as TimeLinePanelListener).event(evs, o)
	    i += 2
	}
    }

    fun addTimeLinePanelListener(tll: TimeLinePanelListener) {
	listeners.add(TimeLinePanelListener::class.java, tll)
    }

    fun deselectAllTimeLine() {
	for (i in 0 until maxTimeLineIndex) {
	    val tl = mtl.getTimeLine(i)
	    tl?.setDeselectTimeMarker()
	}
    }

    private fun hitTimeLine(p: Point): Int {
	val y = p.getY().toInt()
	return if (y < TL_M_SCAN) -1 else (y - TL_M_SCAN) / TL_H
    }

    fun setLock_(b: Boolean) {
	lock = b
    }

    private fun paintMasterTimeLine(g: Graphics) {
	g.color = gray30
	g.fillRect(0, 0, 2000, TL_M_SCAN)
	g.color = gray60
	g.fillRect(tick_start / scale, 0, (playEnd - tick_start) / scale, TL_M_DUR)
	g.color = gray45
	if (100 / scale >= 5) {
	    var i = 1.0
	    while (i < 10000) {
		g.drawLine((i + 0.5).toInt(), 0, (i + 0.5).toInt(), TL_M_DUR)
		i += 100.0 / scale
	    }
	}
	run {
	    var i = 1.0
	    while (i < 10000) {
		g.drawLine((i + 0.5).toInt(), 0, (i + 0.5).toInt(), TL_M_DUR + 3)
		i += 500.0 / scale
	    }
	}
	run {
	    var i = 1.0
	    while (i < 10000) {
		g.drawLine((i + 0.5).toInt(), 0, (i + 0.5).toInt(), TL_M_SCAN)
		i += 1000.0 / scale
	    }
	}
	g.color = gray80
	if (100 / scale >= 5) {
	    var i = 0.0
	    while (i < 10000) {
		g.drawLine((i + 0.5).toInt(), 0, (i + 0.5).toInt(), TL_M_DUR)
		i += 100.0 / scale
	    }
	}
	run {
	    var i = 0.0
	    while (i < 10000) {
		g.drawLine((i + 0.5).toInt(), 0, (i + 0.5).toInt(), TL_M_DUR + 3)
		i += 500.0 / scale
	    }
	}
	var i = 0.0
	while (i < 10000) {
	    g.drawLine((i + 0.5).toInt(), 0, (i + 0.5).toInt(), TL_M_SCAN)
	    i += 1000.0 / scale
	}
    }

    var low_blue = Color(80, 80, 130)
    var gray80 = Color(220, 220, 220)
    var gray60 = Color(180, 180, 180)
    var gray50 = Color(120, 120, 120)
    var gray45 = Color(110, 110, 110)
    var gray40 = Color(100, 100, 100)
    var gray30 = Color(80, 80, 80)
    var outside_tl_color = Color(100, 100, 100)
    var selected_tl_timeline_color = Color.orange.brighter()!!
    var light_selected_tl_timeline_color = selected_tl_timeline_color.brighter()!!
    var normal_tl_timeline_color = Color.orange.darker()!!
    var light_normal_tl_timeline_color = normal_tl_timeline_color.brighter()!!
    var vertical_tl_mousecursor_color = Color(100, 100, 100)
    var vertical_tl_cursor_color = Color(180, 80, 180)
    var light_vertical_tl_cursor_color = vertical_tl_cursor_color.brighter()!!
    var normal_marker_color = Color(80, 180, 80)
    var normal_TSync_marker_color = Color(220, 80, 200)
    var yellow = Color(180, 180, 80)
    var cyan = Color(50, 180, 240)
    var light_yellow = Color.yellow.brighter()!! // new Color(220, 220, 110);
    var TLOFF = 80

    init {
	layout = null
	listeners = EventListenerList()
	background = low_blue
	val mouse: Mouse = Mouse()
	addMouseListener(mouse)
	addMouseMotionListener(mouse)
	updName()
    }

    private fun drawTLBut(g: Graphics, y: Int) {
	g.color = gray80
	g.fillRect(-TLOFF + 3, y - 5, 10, 10)
    }

    private fun paintTimeLine(g: Graphics, mtl: MasterTimeLine) {
	val g2 = g as Graphics2D
	var at: AffineTransform? = null
	try {
	    g.setColor(gray30)
	    g.fillRect(0, 0, TLOFF, 1000)
	    at = g2.transform
	    at.translate(TLOFF.toDouble(), 0.0)
	    g2.transform = at
	    paintMasterTimeLine(g)
	    val t_x = hitMS / scale
	    val t_mx = mouseHitMS / scale
	    val t_x_ = ltick_h / scale
	    g.setColor(vertical_tl_cursor_color)
	    g.drawLine(t_x, 0, t_x, 200)
	    g.setColor(vertical_tl_mousecursor_color)
	    g.drawLine(t_mx, 0, t_mx, 200)
	    for (i in 0 until maxTimeLineIndex) {
		val y = TL_M_SCAN + 10 + i * TL_H
		g.setColor(outside_tl_color)
		g.drawLine(0, y, 2000, y)
		val tl = mtl.getTimeLine(i) ?: continue
		val start = (tl.offset / scale)
		val stop = ((tl.offset + tl.duration) / scale)
		g.setColor(outside_tl_color)
		g.drawLine(0, y, start, y)
		if (i == selected_tl) g.setColor(selected_tl_timeline_color) else g.setColor(normal_tl_timeline_color)
		g.drawLine(start, y, stop, y)
		if (t_x > start) {
		    if (i == selected_tl) g.setColor(light_selected_tl_timeline_color) else g.setColor(
			    light_normal_tl_timeline_color
		    )
		    g.drawLine(start, y, t_x, y)
		}
		if (t_x_ > start) {
		    g.setColor(light_vertical_tl_cursor_color)
		    g.drawLine(t_x_, y, t_x, y)
		}
		g.setColor(outside_tl_color)
		g.drawLine(stop, y, 2000, y)
		var it: Iterator<*> = tl.getMarkersAbs(-1, 999999, true).iterator()
		while (it.hasNext()) {
		    val tm = it.next() as TimeMarker
		    val xx = (tm.`when` / scale)
		    val dd = (tm.duration / scale)
		    g.setColor(gray30)
		    draw(g, tm, xx + 1, y + 1, dd)
		    var mcol = normal_marker_color
		    if (tm.type == TimeMarker.TSYNC) mcol = normal_TSync_marker_color
		    if (tm.isDeleteCandidate) g.setColor(Color.red) else if (tm.selected) g.setColor(
			    mcol.brighter().brighter()
		    ) else g.setColor(mcol)
		    draw(g, tm, xx, y, dd)
		}
		it = tl.getMarkersAbs(t_x_ * scale, t_x * scale).iterator()
		g.setColor(light_yellow)
		while (it.hasNext()) {
		    val tm = it.next()
		    draw(g, tm, (tm.`when` / scale), y, (tm.duration / scale))
		}

//		drawTLBut(g, y);
		at.translate(-TLOFF.toDouble(), 0.0)
		g2.transform = at
		val xx = 10
		val name = mtl.getTimeLine(i)!!.lessonId
		if (name != null) g2.drawString(name, xx, y + 3)
		at.translate(TLOFF.toDouble(), 0.0)
		g2.transform = at
	    }
	} finally {
	    at!!.translate(-TLOFF.toDouble(), 0.0)
	    g2.transform = at
	}
    }

    override fun getMinimumSize(): Dimension {
	return Dimension(100, 120)
    }

    public override fun paintComponent(g: Graphics) {
	super.paintComponent(g)
	paintTimeLine(g, mtl)
	updName()
    }

    companion object {
	private const val TL_H = 18
	private const val TL_M_DUR = 7
	private const val TL_M_SCAN = 14
	private const val HITM_NORMAL = 0
	private const val HITM_scan_object = 1
	var playEnd = 5000
    }
}
