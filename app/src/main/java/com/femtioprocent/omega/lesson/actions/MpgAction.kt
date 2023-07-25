package com.femtioprocent.omega.lesson.actions

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.media
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.canvas.BaseCanvas.ColorColors
import com.femtioprocent.omega.lesson.canvas.MsgItem
import com.femtioprocent.omega.media.video.MpgPlayer
import com.femtioprocent.omega.swing.ScaledImageIcon
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.xml.Element
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import javax.swing.JPanel
import javax.swing.event.MouseInputAdapter

class MpgAction : ActionI {
    @JvmField
    var mpg_player: MpgPlayer? = null
    var jpan: MyPanel? = null
    override var hm = HashMap<String?, Any?>()
    private var item_fo: Font? = null
    var pW = 100
    var pH = 100
    @JvmField
    var sentence: String? = null
    @JvmField
    var show_sentence = true
    private var again_play_request = false
    private var again_play2 = false
    private var again_audio_request = false
    private var again_audio2 = false
    var msg_dlg: MsgDialog = MsgDialog()

    inner class MyPanel : JPanel() {
	private var m: Mouse

	init {
	    m = Mouse()
	    addMouseListener(m)
	    addMouseMotionListener(m)
	}

	internal inner class Mouse : MouseInputAdapter() {
	    override fun mousePressed(e: MouseEvent) {
		hideMsg()
	    }
	}

	public override fun paintComponent(g: Graphics) {
	    g.color = Lesson.omega_settings_dialog.action_movie_background!!.color
	    g.fillRect(0, 0, 2000, 2000)
	    if (isMsg) msg_dlg.draw(g as Graphics2D)
	    super.paintComponent(g)
	}
    }

    fun ownKeyCode(kc: Int, is_shift: Boolean): Boolean {
	if (kc == ' '.code || kc == '\n'.code || kc == '\r'.code) {
	    hideMsg()
	    return false
	}
	Log.getLogger().info(":--: own kk $kc")
	if (kc == 'l'.code) {
	    again_play_request = true
	}
	if (kc == 'u'.code) {
	    again_audio_request = true
	}
	return true
    }

    override fun prefetch(action_s: String?): Element? {
	return prefetch(action_s, 0, 0)
    }

    fun prefetch(action_s: String?, winW: Int, winH: Int): Element? {
	val el: Element? = null
	mpg_player = MpgPlayer.createMpgPlayer(action_s!!, jpan, winW, winH)
	return el
    }

    fun setParentWH(parent_w: Int, parent_h: Int) {
	pW = parent_w
	pH = parent_h
    }

    val elementRoot: Element?
	get() = null

    override fun show() {
	jpan!!.repaint()
    }

    override val pathList: String?
	get() = null
    override val actorList: String?
	get() = null
    override val canvas: JPanel?
	get() = jpan

    fun stop() {
	if (mpg_player != null) mpg_player!!.stop()
    }

    fun dispose() {
	if (mpg_player != null) mpg_player!!.dispose(jpan!!)
	Log.getLogger().info(":--: " + "mpg disposed")
	jpan!!.isVisible = false
	mpg_player = null
    }

    fun reset() {
	if (mpg_player != null) mpg_player!!.reset()
    }

    val w: Int
	get() {
	    var w = -1
	    if (mpg_player != null) w = mpg_player!!.fxp!!.mediaW
	    Log.getLogger().info("Movie width: $w")
	    return w
	}
    val h: Int
	get() {
	    var h = -1
	    if (mpg_player != null) h = mpg_player!!.fxp!!.mediaH
	    Log.getLogger().info("Movie width: $h")
	    return h
	}

    fun gX(f: Double): Int {
	return (f * pW).toInt()
    }

    fun gY(f: Double): Int {
	return (f * pH).toInt()
    }

    fun setSize(w: Int, h: Int) {
	mpg_player!!.setSize(w, h)
    }

    fun setLocation(x: Int, y: Int) {
	mpg_player!!.setLocation(x, y)
    }

    var isMsg = false

    init {
	if (jpan == null) jpan = MyPanel()
	jpan!!.layout = BorderLayout()
    }

    fun getStringWidth(g2: Graphics2D, fo: Font, s: String?): Int {
	val rh = g2.renderingHints
	rh[RenderingHints.KEY_ANTIALIASING] = RenderingHints.VALUE_ANTIALIAS_ON
	g2.setRenderingHints(rh)
	val frc = g2.fontRenderContext
	val r = fo.getStringBounds(s, frc)
	return r.width.toInt()
    }

    inner class MsgDialog {
	var msg_item: MsgItem? = null
	var cont_image_fn: String? = media() + "default/continue.png"
	fun show(msg: MsgItem?) {
	    set(msg)
	}

	fun set(msg: MsgItem?) {
	    if (msg == null) {
		isMsg = false
	    } else {
		isMsg = true
	    }
	    msg_item = msg
	    jpan!!.repaint()
	}

	fun draw(g2: Graphics2D) {
	    if (true) return  // JavaFX
	    val text = if (msg_item != null) msg_item!!.text else ""
	    if (item_fo == null) {
		var fH = gY(0.04)
		if (fH < 8) fH = 8
		item_fo = Font("Arial", Font.PLAIN, fH)
	    }
	    var sw = getStringWidth(g2, item_fo!!, text)
	    if (sw > gX(0.9)) {
		val fH = gY(0.025)
		item_fo = Font("Arial", Font.PLAIN, fH)
		sw = getStringWidth(g2, item_fo!!, text)
	    }
	    val w = sw + 60
	    val h = gY(0.06)
	    val th = gY(0.026)
	    val x = gX(0.5) - w / 2
	    val y = gY(0.92)
	    val r = gX(0.02)
	    val col = OmegaContext.COLOR_WARP
	    val fr: RoundRectangle2D = RoundRectangle2D.Double(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), r.toDouble(), r.toDouble())
	    Log.getLogger().info(":--: MPG draw: $w $sw $text")
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f)
	    g2.color = col
	    g2.fill(fr)
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    if (false) {
		// titlebar
		g2.color = Color(88, 88, 88)
		g2.clip = fr
		g2.fill(Rectangle2D.Double(x.toDouble(), y.toDouble(), w.toDouble(), th.toDouble()))
	    }
	    val stroke = BasicStroke(pH / 200f)
	    g2.stroke = stroke
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    g2.color = Color(15, 15, 15)
	    g2.setClip(0, 0, 10000, 10000)
	    g2.draw(fr)
	    g2.setClip(0, 0, 10000, 10000) //	    g2.setClip(fr);
	    g2.color = OmegaContext.COLOR_TEXT_WARP
	    g2.font = item_fo
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
	    Log.getLogger().info(":--: MPG text: $x $y $h $item_fo $text")
	    g2.drawString(text,
		    x + 5,
		    y + 2 * h / 3)
	    g2.color = col
	    // 	    g2.setFont(item_fo);
// 	    g2.drawString(msg_item.title, x + 1 * w / 10, (int)(y + gY(0.03)));
	    if (msg_item != null && msg_item!!.image != null) {
		val hh = (h * 0.7).toInt()
		val ww = 4 * hh / 3
		try {
		    val img = ScaledImageIcon.createImageIcon(jpan,
			    msg_item!!.image!!,
			    ww,
			    hh)!!.image
		    g2.drawImage(img, x, y + th + 2, null)
		} catch (ex: Exception) {
		}
	    }
	    if (cont_image_fn != null) {
		val hh = (h * 0.25).toInt()
		val ww = hh * 4
		try {
		    val img = ScaledImageIcon.createImageIcon(jpan,
			    cont_image_fn!!,
			    ww,
			    hh)!!.image
		    val imw = img.getWidth(null)
		    g2.drawImage(img, x + w - imw - 3, y + h - hh - 3, null)
		} catch (ex: Exception) {
		}
	    }
	    val a = Area()
	    a.add(Area(Rectangle2D.Double(0.0, 0.0, 10000.0, 10000.0)))
	    a.subtract(Area(fr))
	    g2.clip = a
	    g2.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f)
	    g2.color = Color(15, 15, 15)
	    for (i in 0..6) {
		val frs: RoundRectangle2D = RoundRectangle2D.Double((x + 10 - i).toDouble(), (y + 10 - i).toDouble(), w.toDouble(), h.toDouble(), r.toDouble(), r.toDouble())
		g2.fill(frs)
	    }
	}
    }

    fun showMsg(mi: MsgItem?) {
	msg_dlg.show(mi)
    }

    fun hideMsg() {
	msg_dlg.set(null)
    }

    override fun perform(window: Window,
			 action_s: String?,
			 actA: Array<String>,
			 pathA: Array<String>,
			 ord: Int,
			 hook: Runnable) {
	if (mpg_player == null) mpg_player = MpgPlayer.createMpgPlayer(action_s!!, jpan, window.width, window.height) else Log.getLogger().info("already created MpgPayer ... ")
	Log.getLogger().info(":--: " + "mpg created " + mpg_player!!.origW + ' ' + mpg_player!!.origH)
	mpg_player!!.fxp!!.waitReady()
	again_play2 = true
	again_audio2 = true
	//mpg_player.setSize(mpg_player.getOrigW(), mpg_player.getOrigH());
	val ww = (w - mpg_player!!.origW) / 2
	val hh = (h - mpg_player!!.origH) / 2
	//	mpg_player.setLocation(ww, hh);
//	mpg_player.setSize(200, 200);
//	mpg_player.visual.setVisible(true);
	mpg_player!!.start()
	mpg_player!!.wait4()
	Log.getLogger().info(":--: " + "mp_waited")
	if (ord == 0) {
//	    dispose();
	    if (show_sentence) {
		val colors = hm["colors"] as HashMap<String, ColorColors>?
		showMsgFx(MsgItem("", sentence!!), colors)
		while (isMsg && mpg_player!!.fxp!!.messageShown) {
		    m_sleep(200)
		    if (again_audio_request && again_audio2) {
			hook.run()
			again_audio2 = false
		    }
		    if (again_play_request) {
			// Do it once
			if (again_play2) {
			    hideMsg()
			    hideMsg(true)
			    mpg_player!!.reset()
			    mpg_player!!.start()
			    mpg_player!!.wait4()
			    again_play2 = false
			    again_play_request = false
			    hideMsg(false)
			    showMsg(MsgItem("", sentence!!))
			} else {
			    return
			}
		    }
		}
	    }
	}
	Log.getLogger().info(":--: " + "mp_shown")
    }

    private fun showMsgFx(mi: MsgItem, colors: HashMap<String, ColorColors>?) {
	val ww = mpg_player!!.visual!!.width
	val hh = mpg_player!!.visual!!.height
	mpg_player!!.fxp!!.showMsg(mi, ww, hh, colors)
	mpg_player!!.visual!!.repaint()
	mpg_player!!.fxp!!.messageShown = true
	isMsg = true
    }

    fun hideMsg(hide: Boolean) {
	mpg_player!!.fxp!!.hideMsg(hide)
	mpg_player!!.visual!!.repaint()
    }

    override fun clearScreen() {}
    override fun clean() {}
}
