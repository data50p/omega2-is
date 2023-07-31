package com.femtioprocent.omega.anim.appl

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaConfig.FRAME_HEIGHT
import com.femtioprocent.omega.OmegaConfig.FRAME_WIDTH
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.antiOmegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.init
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.anim.panels.cabaret.CabaretPanel
import com.femtioprocent.omega.anim.panels.cabaret.WingsPanel
import com.femtioprocent.omega.anim.panels.timeline.TimeLineComponent
import com.femtioprocent.omega.anim.panels.timeline.TimeLinePanel
import com.femtioprocent.omega.anim.panels.timeline.TimeLinePanelAdapter
import com.femtioprocent.omega.anim.tool.path.Path
import com.femtioprocent.omega.anim.tool.timeline.TimeLine
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker
import com.femtioprocent.omega.appl.OmegaStartManager
import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.media.audio.APlayer
import com.femtioprocent.omega.media.images.xImage
import com.femtioprocent.omega.servers.httpd.Server
import com.femtioprocent.omega.subsystem.Httpd
import com.femtioprocent.omega.swing.ToolAction
import com.femtioprocent.omega.swing.ToolExecute
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.split
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Container
import java.awt.event.InputEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.geom.Point2D
import javax.swing.*
import javax.swing.event.EventListenerList

class AnimEditor : JFrame {
    var mb: JMenuBar? = null
    var c: Container? = null
    var toolbar_cmd: ToolBar_AnimEditor? = null
    var toolbar_top: ToolBar_AnimEditor? = null
    var tlp: TimeLinePanel? = null
    var tlc: TimeLineComponent? = null
    var arun: AnimRuntime? = null
    var httpd: Server
    var cabaret_panel: CabaretPanel? = null
    var wings_panel: WingsPanel? = null
    var anim_repository: Anim_Repository? = null
    var a_ctxt: AnimContext? = AnimContext(this)
    var cab_wing_pan: JPanel? = null
    var cab_wing_pan_card: CardLayout? = null
    var exit_on_close = false

    constructor(verbose: Boolean) : super("Omega - " + t("Animator editor")) {
	if (verbose) OmegaConfig.T = true
	//	setVisible(true);
	val f: JFrame = this
	ApplContext.top_frame = if (ApplContext.top_frame == null) this else ApplContext.top_frame
	f.addWindowListener(object : WindowAdapter() {
	    override fun windowClosing(ev: WindowEvent) {
		maybeClose()
	    }
	})
	AnimContext.top_frame = this
	init("Httpd", null)
	httpd = Httpd.httpd!!
	init(true, null)
    }

    private fun maybeClose() {
	Log.getLogger()
	    .info("LessonRuntime want to close " + (ApplContext.top_frame === this) + ' ' + ApplContext.top_frame + '\n' + this)
	if (ApplContext.top_frame === this) System.exit(0)
    }

    constructor(fname: String) : super("Omega - " + t("Animator editor") + ": " + fname) {         // from lesson
	AnimContext.top_frame = this
	init("Httpd", null)
	httpd = Httpd.httpd!!
	init(false, fname)
    }

    public override fun processEvent(e: AWTEvent) {
	if (e.id != WindowEvent.WINDOW_CLOSING) super.processEvent(e) else {
	    var s = ""
	    if ((a_ctxt != null) && (AnimContext.ae != null) && AnimContext.ae!!.isDirty) s =
		"\n" + t("Changes not saved")
	    if (exit_on_close) {
		val sel = JOptionPane.showConfirmDialog(
		    this@AnimEditor,
		    t("Are you sure to exit Omega?") +
			    s
		)
		if (sel == 0) super.processEvent(e)
	    } else {
		val sel = JOptionPane.showConfirmDialog(
		    this@AnimEditor,
		    t("Are you sure to close Anim Editor?") +
			    s
		)
		if (sel == 0) {
		    try {
			a_ctxt!!.arun!!.clean()
		    } catch (_ex: Exception) {
		    }
		    super.processEvent(e)
		}
	    }
	}
    }

    fun addShiftAcc(m: JMenu, key: Char) {
	val mi = m.getItem(m.menuComponentCount - 1)
	mi.accelerator = KeyStroke.getKeyStroke(key.code, InputEvent.SHIFT_DOWN_MASK, false)
    }

    fun addCtrlAcc(m: JMenu, key: Char) {
	val mi = m.getItem(m.menuComponentCount - 1)
	mi.accelerator = KeyStroke.getKeyStroke(key.code, InputEvent.CTRL_DOWN_MASK, false)
    }

    private fun init(exoc: Boolean, fname: String?) {
	exit_on_close = exoc
	anim_repository = Anim_Repository()
	help = Anim_HelpSystem()
	c = contentPane
	c!!.setLayout(BorderLayout())
	addWindowListener(object : WindowAdapter() {
	    override fun windowClosing(ev: WindowEvent) {
//log		    OmegaContext.sout_log.getLogger().info(":--: " + "closing");
		if (false && exit_on_close) {
		    System.exit(0)
		} else isVisible = false
	    }
	})
	val ae_texec: ToolExecute = object : ToolExecute {
	    override fun execute(cmd: String?) {
		if (OmegaConfig.T) Log.getLogger().info(":--: AnimEditor.texec: execute $cmd")
		if (("exit" == cmd)) {
		    var s = ""
		    if ((a_ctxt != null) && (AnimContext.ae != null) && AnimContext.ae!!.isDirty) s =
			"\n" + t("Changes not saved")
		    if (false && exit_on_close) {
			val sel = JOptionPane.showConfirmDialog(
			    this@AnimEditor,
			    t("Are you sure to exit Omega?") +
				    s
			)
			//log			    OmegaContext.sout_log.getLogger().info(":--: " + "" + sel);
			if (sel == 0) System.exit(0)
		    } else {
			val sel = JOptionPane.showConfirmDialog(
			    this@AnimEditor,
			    t("Are you sure to close Anim Editor?") +
				    s
			)
			//log			    OmegaContext.sout_log.getLogger().info(":--: " + "" + sel);
			if (sel == 0) {
			    try {
				a_ctxt!!.arun!!.clean()
			    } catch (_ex: Exception) {
			    }
			    isVisible = false
			    maybeClose()
			}
		    }
		} else if (("new" == cmd)) {
		    if (isDirty) {
			val sel = JOptionPane.showConfirmDialog(
			    this@AnimEditor,
			    (t("Are you sure to start with a new animation") + "\n" +
				    t("Changes are unsaved!"))
			)
			if (sel == 0) initNew()
		    } else {
			initNew()
		    }
		} else if (("save" == cmd)) {
		    save(false)
		} else if (("saveas" == cmd)) {
		    save(true)
		} else if (("open" == cmd)) {
		    if (isDirty) {
			val sel = JOptionPane.showConfirmDialog(
			    this@AnimEditor,
			    (t("Are you sure to open an animation") + "\n" +
				    t("Changes are unsaved!"))
			)
			if (sel == 0) open()
		    } else {
			open()
		    }
		} else if (("reopen" == cmd)) {
		    val fn = anim_repository!!.name
		    if (fn != null) {
			if (isDirty) {
			    val sel = JOptionPane.showConfirmDialog(
				this@AnimEditor,
				(t("Are you sure to reopen the animation") + "\n" +
					t("Changes are unsaved!"))
			    )
			    if (sel == 0) open(fn)
			} else {
			    open(fn)
			}
		    } else {
			JOptionPane.showMessageDialog(
			    AnimContext.top_frame,
			    t("No name, use open."),
			    "Omega",
			    JOptionPane.INFORMATION_MESSAGE
			)
		    }
		} else if (("about" == cmd)) {
		    help!!.showAbout()
		} else if (("aboutAE" == cmd)) {
		    help!!.showAboutAE()
		} else if (("show manual" == cmd)) {
		    help!!.showManualAE()
		} else if (("show prop act" == cmd)) {
		    AnimContext.ae!!.cabaret_panel!!.popup(0)
		} else if (("show prop wing" == cmd)) {
		    AnimContext.ae!!.wings_panel!!.popup(0)
		} else if (("dep_set background" == cmd)) {
		    val url_s: String? = fileAsURLString
		    setBackground(url_s)
		    wings_panel!!.removeAllWings()
		    a_ctxt!!.anim_canvas!!.toolExecute!!.execute("fit")
		} else if (("dep_set actor0" == cmd)) {
		    val url_s: String? = fileAsURLStringActor
		    loadActor(0, url_s)
		} else if (("dep_set actor1" == cmd)) {
		    val url_s: String? = fileAsURLStringActor
		    loadActor(1, url_s)
		} else if (("dep_set actor2" == cmd)) {
		    val url_s: String? = fileAsURLStringActor
		    loadActor(2, url_s)
		} else if (("dep_set actor3" == cmd)) {
		    val url_s: String? = fileAsURLStringActor
		    loadActor(3, url_s)
		} else if (("flip aw" == cmd)) {
		    cab_wing_pan_card!!.next(cab_wing_pan)
		} else if (("add w" == cmd)) {
		    loadWing()
		}
		if (("play" == cmd)) {
		    validate()
		    arun!!.playAnimation()
		} else if (("stop" == cmd)) {
		    arun!!.stopAnimation()
		    validate()
		} else if (("pause" == cmd)) {
		    a_ctxt!!.tl_player!!.pause()
		}
		if (("prop_act_show" == cmd)) {
		    a_ctxt!!.anim_canvas!!.traceNoWing()
		    AnimContext.ae!!.cabaret_panel!!.setSelected(true)
		    AnimContext.ae!!.wings_panel!!.setSelected(false)
		} else if (("prop_wing_show" == cmd)) {
		    AnimContext.ae!!.cabaret_panel!!.setSelected(!true)
		    AnimContext.ae!!.wings_panel!!.setSelected(!false)
		}
		if (("prop_act" == cmd)) {
		    AnimContext.ae!!.cabaret_panel!!.popup(0)
		} else if (("prop_wing" == cmd)) {
		    AnimContext.ae!!.wings_panel!!.popup(0)
		}
		val gel = a_ctxt!!.anim_canvas!!.toolExecute
		if (gel != null) gel.execute(cmd) else Log.getLogger().info(":--: ! missed $cmd")
	    }
	}
	mb = JMenuBar()
	jMenuBar = mb
	mb!!.isVisible = false
	toolbar_cmd = ToolBar_AnimEditor(ae_texec) //, VERTICAL);
	toolbar_top = ToolBar_AnimEditor(ae_texec)
	val jm = JMenu(t("File"))
	mb!!.add(jm)
	val jmca = JMenu(t("Canvas"))
	mb!!.add(jmca)
	val jmpa = JMenu(t("Path"))
	mb!!.add(jmpa)
	val jmtl = JMenu(t("TimeLine"))
	mb!!.add(jmtl)
	val jmac = JMenu(t("Cast"))
	mb!!.add(jmac)
	val jmh = JMenu(t("Help"))
	mb!!.add(jmh)
	var tac: ToolAction?
	jm.add(ToolAction(t("New"), "general/New", "new", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	toolbar_cmd!!.isVisible = false
	toolbar_cmd!!.addSeparator()
	jm.add(ToolAction(t("Open"), "general/Open", "open", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jm.add(ToolAction(t("Reopen"), "general/ReOpen", "reopen", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jm.add(ToolAction(t("Save"), "general/Save", "save", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jm.add(ToolAction(t("Save as"), "general/SaveAs", "saveas", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jm.addSeparator()
	if (exit_on_close) jm.add(ToolAction("Reset Starter", "resetstarter", ae_texec).also { tac = it })
	jm.add(ToolAction(if (exit_on_close) t("Exit") else t("Close"), "exit", ae_texec).also { tac = it })
	toolbar_cmd!!.addSeparator()
	jmca.add(ToolAction(t("Set background"), "dep_set background", ae_texec).also { tac = it })
	jmca.addSeparator()
	jmca.add(ToolAction(t("Left"), "navigation/Back", "left", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmca.add(ToolAction(t("Right"), "navigation/Forward", "right", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmca.add(ToolAction(t("Up"), "navigation/Up", "up", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmca.add(ToolAction(t("Down"), "navigation/Down", "down", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmca.addSeparator()
	toolbar_cmd!!.addSeparator()
	jmca.add(ToolAction(t("Smaller"), "general/ZoomOut", "smaller", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmca.add(ToolAction(t("Bigger"), "general/ZoomIn", "bigger", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmca.add(ToolAction("1:1", "general/ZoomNo", "upper_left", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmca.add(ToolAction(t("Fit in window"), "general/ZoomFit", "fit", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	toolbar_cmd!!.addSeparator()
	jmpa.add(ToolAction(t("Create new"), "omega/PathNew", "path_create", ae_texec, true).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmpa.add(ToolAction(t("Duplicate"), "omega/PathDup", "path_duplicate", ae_texec, true).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmpa.add(ToolAction(t("Extend at end"), "omega/PathExtend", "path_extend", ae_texec, true).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmpa.add(ToolAction(t("Split in two"), "omega/PathSplit", "path_split", ae_texec, true).also { tac = it })
	toolbar_cmd!!.add(tac)
	jmpa.add(ToolAction(t("Delete segment"), "omega/PathDelete", "path_delete", ae_texec, true).also { tac = it })
	jmpa.add(
	    ToolAction(
		t("Delete whole path and timeline"),
		"omega/PathDeleteAll",
		"path_delete_all",
		ae_texec,
		true
	    ).also { tac = it })
	//toolbar_cmd.add(tac);
	toolbar_cmd!!.addSeparator()
	jmtl.add(ToolAction(t("Play"), "media/Play", "play", ae_texec).also { tac = it })
	toolbar_cmd!!.add(tac)
	addShiftAcc(jmtl, 'P')
	jmtl.add(ToolAction(t("Stop"), "media/Stop", "stop", ae_texec).also { tac = it })
	addShiftAcc(jmtl, 'S')
	toolbar_cmd!!.add(tac)
	val jmac2 = JMenu(t("Set actor"))
	jmac.add(jmac2)
	jmac2.add(ToolAction("1", "dep_set actor0", ae_texec).also { tac = it })
	jmac2.add(ToolAction("2", "dep_set actor1", ae_texec).also { tac = it })
	jmac2.add(ToolAction("3", "dep_set actor2", ae_texec).also { tac = it })
	jmac2.add(ToolAction("4", "dep_set actor3", ae_texec).also { tac = it })
	jmac2.add(ToolAction("5", "dep_set actor4", ae_texec).also { tac = it })
	jmac.add(ToolAction(t("Add a wing"), "add w", ae_texec).also { tac = it })
	jmac.addSeparator()
	jmac.add(ToolAction(t("Show actor properties..."), "show prop act", ae_texec).also { tac = it })
	addCtrlAcc(jmac, 'A')
	jmac.add(ToolAction(t("Show wings properties..."), "show prop wing", ae_texec).also { tac = it })
	addCtrlAcc(jmac, 'W')
	jmac.addSeparator()
	jmac.add(ToolAction(t("Flip actor/wings"), "flip aw", ae_texec).also { tac = it })
	addCtrlAcc(jmac, 'F')
	jmh.add(ToolAction(t("Show manual"), "show manual", ae_texec).also { tac = it })
	jmh.addSeparator()
	jmh.add(ToolAction(t("About") + " Omega", "about", ae_texec).also { tac = it })
	jmh.add(ToolAction(t("About Anim Editor"), "aboutAE", ae_texec).also { tac = it })
	val mainp = JPanel()
	val mainpM = JPanel()
	mainp.layout = BorderLayout()
	mainpM.layout = BorderLayout()
	c!!.add(mainpM, BorderLayout.CENTER)
	mainpM.add(mainp, BorderLayout.CENTER)
	cabaret_panel = CabaretPanel(this)
	wings_panel = WingsPanel(this)
	cab_wing_pan = JPanel()
	cab_wing_pan!!.layout = CardLayout().also { cab_wing_pan_card = it }
	cab_wing_pan!!.add(cabaret_panel, "actor")
	cab_wing_pan!!.add(wings_panel, "wings")
	toolbar_top!!.card = cab_wing_pan_card
	toolbar_top!!.card_pan = cab_wing_pan
	val p = JPanel()
	mainp.add(p, BorderLayout.NORTH)
	toolbar_top!!.populateRest()
	mainpM.add(toolbar_top, BorderLayout.NORTH)
	c!!.add(toolbar_cmd, BorderLayout.NORTH)
	toolbar_top!!.add(cab_wing_pan)
	val main_cpan = JPanel()
	main_cpan.layout = BorderLayout()
	arun = AnimRuntime(this)
	a_ctxt!!.arun = arun
	tlp = TimeLinePanel(a_ctxt!!.mtl!!)
	tlc = TimeLineComponent(tlp!!)
	a_ctxt!!.tl_player!!.addPlayCtrlListener(tlc!!)
	tlp!!.addTimeLinePanelListener(object : TimeLinePanelAdapter() {
	    override fun event(evs: String?, o: Any?) {
		if (("selectTL" == evs)) {
		    if (o != null) {
			val tl = o as TimeLine
			val tl_nid = tl.nid
			val ap = a_ctxt!!.anim_canvas!!.ap
			ap.deselectAll(null)
			val pa = ap[tl_nid]
			if (pa != null) pa.selected = true
			if (a_ctxt!!.anim_canvas != null) a_ctxt!!.anim_canvas!!.setSelectedPath(tl_nid, (pa)!!)
		    }
		} else if (("addMarker" == evs)) {
		    val tl = o as TimeLine
		    val ap = a_ctxt!!.anim_canvas!!.ap
		    val pa = ap.findSelected()
		    if (pa != null) {
			val tm = tl.last_added_tm
			val ta = tl.getAllTimeMarkerType(TimeMarker.TSYNC)
			for (i in ta.indices) {
			    if (tm == ta[i]) {
				var w1: Double
				try {
				    w1 = pa.getMarker(i - 1)!!.where
				} catch (ex: NullPointerException) {
				    w1 = 0.0
				}
				var w2: Double
				try {
				    w2 = pa.getMarker(i)!!.where
				} catch (ex: NullPointerException) {
				    w2 = pa.length
				}
				pa.addMarker(i, TimeMarker.TSYNC, (w1 + w2) / 2)
				a_ctxt!!.anim_canvas!!.repaint()
			    }
			}
		    }
		} else if (("delMarker" == evs)) {
		    val Ix = o as Int
		    val ap = a_ctxt!!.anim_canvas!!.ap
		    val pa = ap.findSelected()
		    if (pa != null) {
			pa.delMarker(Ix)
			a_ctxt!!.anim_canvas!!.repaint()
		    }
		} else {
		}
	    }
	})
	main_cpan.add(arun!!.aC, BorderLayout.CENTER)
	main_cpan.add(tlc, BorderLayout.SOUTH)
	mainp.add(main_cpan, BorderLayout.CENTER)
	pack()
	setSize(FRAME_WIDTH(65), FRAME_HEIGHT(65))
	isVisible = true
	try {
	    var ap = APlayer.createAPlayer("audio/greeting.wav", null as String?, null as String?)
	    ap.play()
	    ap = APlayer.createAPlayer("audio/greeting.mp3", null as String?, null as String?)
	    ap.play()
	} catch (ex: NoClassDefFoundError) {
	    Log.getLogger().info("ERR: " + "WARNING!! No audio")
	}
	SwingUtilities.invokeLater(Runnable { if (fname == null) initNew() else initFile(fname) })
    }

    private fun initNew() {
	wings_panel!!.removeAllWings()
	anim_repository!!.clearName()
	a_ctxt!!.mtl!!.initNew()
	a_ctxt!!.arun!!.initNew()
	tlc!!.repaint()
	cabaret_panel!!.repaint()
	mb!!.isVisible = true
	toolbar_cmd!!.isVisible = true
	isDirty = true
    }

    private fun initFile(fname: String) {
	wings_panel!!.removeAllWings()
	anim_repository!!.clearName()
	a_ctxt!!.mtl!!.initNew()
	a_ctxt!!.arun!!.initNew()
	tlc!!.repaint()
	cabaret_panel!!.repaint()
	mb!!.isVisible = true
	toolbar_cmd!!.isVisible = true
	open(fname)
    }

    fun decode2D(s: String?): Point2D {
	val sa = split(s, ",")
	val a = sa[0]!!.toFloat()
	val b = sa[1]!!.toFloat()
	return Point2D.Float(a, b)
    }

    fun loadWing() {
	val url_s = fileAsURLString
	val ua = Files.splitUrlString(url_s!!)
	if (ua != null) {
	    val w = a_ctxt!!.anim_canvas!!.createWing(ua[1], 100, 100, 4, 1.0, 0)
	    val wing_nid = w.ord
	    wings_panel!!.setWing(w, wing_nid)
	    a_ctxt!!.anim_canvas!!.resetBackground()
	}
	AnimContext.ae!!.isDirty = true
    }

    fun loadActor(ix: Int, url_s: String?) {
	val ua = Files.splitUrlString(url_s!!)
	if (ua != null) {
	    a_ctxt!!.anim_canvas!!.loadActor(ix, ua[1])
	    AnimContext.ae!!.isDirty = true
	} else {
	    Log.getLogger().info(":--: ERROR file: $url_s")
	}
    }

    fun replaceActor(cab_ixx: Int) {
	val url_s = fileAsURLStringActor
	val ua = Files.splitUrlString(url_s!!)
	if (ua != null) {
	    xImage.invalidateCache()
	    a_ctxt!!.anim_canvas!!.loadActor(cab_ixx, ua[1])
	} else {
	    Log.getLogger().info(":--: ERROR file: $url_s")
	}
	AnimContext.ae!!.isDirty = true
    }

    fun deleteActor(cab_ixx: Int) {
	a_ctxt!!.anim_canvas!!.deleteActor(cab_ixx)
	AnimContext.ae!!.isDirty = true
    }

    fun setBackground(url_s: String?) {
	val ua = Files.splitUrlString(url_s.toString())
	if (ua != null) {
	    a_ctxt!!.anim_canvas!!.setBackground(ua[1], ArrayList())
	    if (wings_panel != null) {
		wings_panel!!.removeAllWings()
	    }
	} else {
	    Log.getLogger().info(":--: ERROR file: $url_s")
	}
	AnimContext.ae!!.isDirty = true
    }

    var is_dirty = false
    var editstate_listeners = EventListenerList()
    fun addEditStateListener(l: EditStateListener) {
	editstate_listeners.add(EditStateListener::class.java, l)
    }

    fun removeEditStateListener(l: EditStateListener) {
	editstate_listeners.remove(EditStateListener::class.java, l)
    }

    var isDirty: Boolean
	get() = is_dirty
	set(d) {
	    var fn: String? = this.anim_repository!!.name ?: return
	    is_dirty = d
	    val lia = editstate_listeners.listenerList
	    var i = 0
	    while (i < lia.size) {
		(lia[i + 1] as EditStateListener).dirtyChanged(is_dirty)
		i += 2
	    }
	    if (fn == null) fn = ""
	    var tit: String = "Omega - " + t("Animator editor: ") + antiOmegaAssets(fn)
	    if (is_dirty) tit += t(" (not saved)")
	    title = tit
	}

    fun save(ask: Boolean) {
	val fn = anim_repository!!.getNameDlg(this@AnimEditor, ask, "Save")
	if (fn != null) {
	    anim_repository!!.save(a_ctxt, fn, ask)
	    isDirty = false
	}
    }

    fun loadFile(fname: String?) {
	open(fname)
    }

    fun open() {
	val fn = anim_repository!!.getNameDlg(this@AnimEditor, true, "Open")
	fn?.let { open(it) }
    }

    fun open(fn: String?) {
	try {
	    a_ctxt!!.arun!!.clean()
	} catch (_ex: Exception) {
	}
	wings_panel!!.removeAllWings()
	val el = anim_repository!!.open(a_ctxt, omegaAssets(fn))
	anim_repository!!.load(a_ctxt, el)
	httpd.hm["lesson:loaded resource "] = anim_repository!!.name
	a_ctxt!!.anim_canvas!!.toolExecute!!.execute("fit")
	tlc!!.repaint()
	anim_repository!!.name = fn
	isDirty = false
    }

    fun selectTimeLine(pa: Path) {
	tlp!!.selected_tl = pa.nid
	tlp!!.repaint()
    }

    fun selectTimeLine() {
	tlp!!.selected_tl = -1
	tlp!!.repaint()
    }

    val fileAsURLString: String?
	get() = anim_repository!!.getImageURL_Dlg(this@AnimEditor)
    val fileAsURLStringActor: String?
	get() = anim_repository!!.getImageURL_Dlg(this@AnimEditor)

    companion object {
	var context: OmegaContext? = null
	var help: Anim_HelpSystem? = null
    }
}
