package com.femtioprocent.omega.media.video

import com.femtioprocent.omega.OmegaConfig.isDebug
import com.femtioprocent.omega.OmegaContext.Companion.getMediaFile
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.lesson.canvas.BaseCanvas.ColorColors
import com.femtioprocent.omega.lesson.canvas.MsgItem
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.effect.DropShadow
import javafx.scene.media.Media
import javafx.scene.media.MediaException
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage
import java.awt.BorderLayout
import java.awt.Color
import java.io.File
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel

class FxMoviePlayer internal constructor(var winW: Int, var winH: Int) {
    var scene: Scene? = null
    var root: Group? = null
    var jcomp: JComponent? = null
    var player: MediaPlayer? = null
    var initDone = false
    var stopped = false
    var fxPanel: JFXPanel? = null
    var mediaW = 0
    var mediaH = 0
    var ready = false
    var messageShown = false
    private fun initGUI(): JFXPanel {
	stopped = false
	initDone = false
	// This method is invoked on the EDT thread
	val frame = JFrame("Swing and JavaFX")
	val jcomp: JComponent = JPanel()
	jcomp.layout = BorderLayout(0, 0)
	frame.add(jcomp, BorderLayout.CENTER) //setContentPane(jcomp);
	frame.setSize(800, 600)
	frame.isVisible = true
	frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
	return initGUI(jcomp, MEDIA_FN)
    }

    fun initGUI(jcomp: JComponent?, fn: String?): JFXPanel {
	Log.getLogger().info("enter initGUI " + Platform.isFxApplicationThread())
	// This method is invoked on the EDT thread
	this.jcomp = jcomp
	var snd = true
	if (fxPanel == null) {
	    fxPanel = JFXPanel()
	    fxPanel!!.setSize(291, 251)
	    fxPanel!!.setLocation(62, 72)
	    jcomp!!.add(fxPanel) //, BorderLayout.CENTER);
	    snd = false
	    //jcomp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	val snd_ = snd
	Platform.runLater {
	    try {
		Log.getLogger().info("runLater: 100")
		if (snd_) {
		    initFX2(fxPanel!!, fn)
		} else {
		    initFX(fxPanel!!, fn)
		    initFX2(fxPanel!!, fn)
		}
		Log.getLogger().info("runLater: play()...")
		player!!.play()
		Log.getLogger().info("runLater: ...play()")
	    } catch (e: MediaException) {
		e.printStackTrace()
	    } catch (e: URISyntaxException) {
		e.printStackTrace()
	    }
	    initDone = true
	}
	Log.getLogger().info("leave initGUI")
	return fxPanel!!
    }

    fun waitReady() {
	while (!ready) m_sleep(100)
    }

    @Throws(URISyntaxException::class)
    private fun initFX(fxPanel: JFXPanel, fn: String?) {
	Log.getLogger().info("enter initFX FxAppThread => " + Platform.isFxApplicationThread())
	// This method is invoked on the JavaFX thread
	this.root = Group()
	val scene =
	    Scene(this.root, winW.toDouble(), winH.toDouble(), javafx.scene.paint.Color(0.24, 0.44, 0.84, 0.184))
	this.scene = scene
	scene.onMousePressed = EventHandler {
	    Log.getLogger().info("Mouse pressed")
	    messageShown = false
	}
	scene.onKeyPressed = EventHandler { ke ->
	    Log.getLogger().info("Key Pressed: " + ke.text + ' ' + ke.code)
	    //reset();
	}
	scene.onKeyReleased = EventHandler { ke -> Log.getLogger().info("Key Released: " + ke.text + ' ' + ke.code) }
	fxPanel.scene = scene
    }

    @Throws(URISyntaxException::class)
    private fun initFX2(fxPanel: JFXPanel, fn_: String?) {
	val fn = omegaAssets(fn_)
	val file = File(fn)
	val uu = file.toURI().toString()
	Log.getLogger().info("UU is $uu")
	val aClass: Class<out FxMoviePlayer> = javaClass
	Log.getLogger().info("aClass is $aClass")
	var resource: URL? = null //aClass.getResource(MEDIA0_URL);
	try {
	    resource = file.toURI().toURL()
	} catch (e: MalformedURLException) {
	    e.printStackTrace()
	}
	Log.getLogger().info("resource is $resource")
	val u = resource!!.toURI().toString()
	Log.getLogger().info("U is $u")
	player = MediaPlayer(Media(uu))
	val mediaView = MediaView(player)
	mediaView.x = 0.0
	mediaView.y = 0.0
	//root.getChildren().clear();
	root!!.children.add(mediaView)
	player!!.onReady = Runnable {
	    mediaW = player!!.media.width
	    mediaH = player!!.media.height
	    val scal = 1.6
	    val xx = (winW - mediaW) / 2.0
	    val yy = (winH - mediaH) / 2.0
	    mediaView.scaleX = scal
	    mediaView.scaleY = scal
	    mediaView.translateX = xx
	    mediaView.translateY = yy
	    Log.getLogger().info("---++-- win: $winW $winH media: $mediaW $mediaH translate: $xx $yy")
	    Log.getLogger().info("VP " + mediaView.x)
	    ready = true
	    //		player.play();
	}
	player!!.onEndOfMedia = Runnable {
	    Log.getLogger().info("EOF ")
	    stopped = true
	}
	Log.getLogger().info("leave initFX")
    }

    fun play() {
	if (true) return
	for (i in 0..99) if (initDone) break else m_sleep(100)
	if (player != null) {
	    Log.getLogger().info("Play the movie...")
	    player!!.play()
	}
    }

    fun reset() {
	if (player != null) {
	    stopped = false
	    player!!.seek(player!!.startTime)
	    player!!.play()
	}
    }

    fun dispose() {
	if (player != null) {
	    val mp: MediaPlayer = player as MediaPlayer
	    //	    Platform.runLater(() -> {
//	        mp.stop();
//	        mp.dispose();
//	    });
	    player = null
	}
    }

    fun wait4done() {
	while (stopped == false) m_sleep(200)
    }

    fun start(primaryStage: Stage?) {
	Log.getLogger().info("Java Home: " + System.getProperty("java.home"))
	Log.getLogger().info("User Home: " + System.getProperty("user.home"))
	Log.getLogger().info("User dir: " + System.getProperty("user.dir"))
	Thread { initGUI() }.start()
	while (stopped == false) m_sleep(200)
	Thread { initGUI(jcomp, MEDIA_FN2) }.start()
	while (stopped == false) m_sleep(200)
	dispose()
    }

    private fun getColor(colors: HashMap<String, ColorColors>?, key: String, def: Color): javafx.scene.paint.Color {
	val c = if (colors != null) colors[key]!!.color else def
	return javafx.scene.paint.Color.rgb(c!!.red, c.green, c.blue)
    }

    fun hideMsg(hide: Boolean) {
	msgItems.hide(hide)
    }

    class MsgItems {
	var text: Text? = null
	var rect: Rectangle? = null
	fun hide(hide: Boolean) {
	    if (text != null) {
		text!!.isVisible = !hide
	    }
	    if (rect != null) {
		rect!!.isVisible = !hide
	    }
	}
    }

    var msgItems = MsgItems()
    fun showMsg(mi: MsgItem, width: Int, height: Int, colors: HashMap<String, ColorColors>?) {
	Platform.runLater {
	    val fontH = height * 0.037
	    msgItems.text = Text(mi.text)
	    msgItems.text!!.font = Font(fontH)
	    val textW = msgItems.text!!.layoutBounds.width
	    val textH = msgItems.text!!.layoutBounds.height
	    val w_ = textW + 10 + width * 0.03
	    val x = width * 0.5 - w_ / 2
	    val y = height * 0.88
	    val h = height * 0.06
	    val r = width * 0.02
	    val tx = x + w_ / 2 - textW / 2
	    val ty = y + h - 2 * fontH / 5
	    val sw = height / 200.0
	    val ds = DropShadow()
	    ds.offsetX = sw
	    ds.offsetY = sw
	    ds.color = javafx.scene.paint.Color.GRAY
	    msgItems.rect = Rectangle(x, y, w_, h)
	    msgItems.rect!!.effect = ds
	    msgItems.rect!!.arcWidth = r
	    msgItems.rect!!.arcHeight = r
	    msgItems.rect!!.strokeWidth = sw
	    msgItems.rect!!.stroke = getColor(colors, "sn_fr", Color.black)
	    msgItems.rect!!.fill = getColor(colors, "sn_bg", Color.white)
	    msgItems.text!!.x = tx
	    msgItems.text!!.y = ty
	    msgItems.text!!.fill = getColor(colors, "sn_tx", Color.black)
	    root!!.children.addAll(msgItems.rect, msgItems.text)
	    if (isDebug()) {
		plot(root, x, y, w_, h, javafx.scene.paint.Color.RED)
		plot(root, tx, ty, textW, -textH, javafx.scene.paint.Color.ORANGE)
		plot(root, x - 30, y, sw, sw, javafx.scene.paint.Color.BLUE)
	    }
	}
    }

    private fun plot(g: Group?, x: Double, y: Double, w: Double, h: Double, col: javafx.scene.paint.Color) {
	g!!.children.add(plotXY(x, y, col))
	g.children.add(plotWH(x, y, w, h, col))
    }

    private fun plotXY(x: Double, y: Double, col: javafx.scene.paint.Color): Node {
	return plotIt(
	    x, y,
	    -10.0, -10.0,
	    10.0, 10.0,
	    col
	)
    }

    private fun plotWH(x: Double, y: Double, w: Double, h: Double, col: javafx.scene.paint.Color): Node {
	return plotIt(
	    x + w, y + h,
	    -20 * Math.signum(w), -20 * Math.signum(h),
	    0 * Math.signum(w), 0 * Math.signum(h),
	    col
	)
    }

    private fun plotIt(
	x: Double,
	y: Double,
	ax: Double,
	ay: Double,
	a2x: Double,
	a2y: Double,
	col: javafx.scene.paint.Color
    ): Node {
	val n1 = Line(x + ax, y, x + a2x, y)
	val n2 = Line(x, y + ay, x, y + a2y)
	val n3 = Line(x + ax, y, x + a2x, y)
	val n4 = Line(x, y + ay, x, y + a2y)
	n1.stroke = col
	n2.stroke = col
	n1.strokeWidth = 1.0
	n2.strokeWidth = 1.0
	n3.stroke = javafx.scene.paint.Color.BLACK
	n4.stroke = javafx.scene.paint.Color.BLACK
	n3.strokeWidth = 3.0
	n4.strokeWidth = 3.0
	return Group(n3, n4, n1, n2)
    }

    companion object {
	var frame: JFrame? = null
	private val MEDIA_FN = getMediaFile("feedback/film1/feedback1.mp4")
	private val MEDIA_FN2 = getMediaFile("feedback/film1/feedback2.mp4")

	@JvmStatic
	fun main(args: Array<String>) {
	    val fxp = FxMoviePlayer(800, 600)
	    fxp.start(null)
	}
    }
}
