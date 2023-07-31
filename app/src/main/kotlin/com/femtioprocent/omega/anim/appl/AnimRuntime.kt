package com.femtioprocent.omega.anim.appl

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.init
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.anim.cabaret.Actor
import com.femtioprocent.omega.anim.cabaret.GImAE
import com.femtioprocent.omega.anim.canvas.AnimCanvas
import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.anim.tool.timeline.*
import com.femtioprocent.omega.lesson.canvas.BaseCanvas.ColorColors
import com.femtioprocent.omega.media.audio.APlayer
import com.femtioprocent.omega.media.images.xImage
import com.femtioprocent.omega.servers.httpd.Server
import com.femtioprocent.omega.subsystem.Httpd
import com.femtioprocent.omega.swing.ToolExecute
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.arrToString
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.xml.Element
import java.awt.Color
import java.awt.Window
import java.awt.geom.Point2D
import java.io.File
import javax.swing.JOptionPane

class AnimRuntime {
    var httpd: Server? = null
    var help: Anim_HelpSystem? = null
    var anim_repository: Anim_Repository? = null
    var a_ctxt: AnimContext?
    var aC: AnimCanvas? = null

    //    private ArrayList ap_li = new ArrayList();
    constructor(ae: AnimEditor) {
	a_ctxt = ae.a_ctxt
	init1(ae)
	init(ae)
    }

    constructor() {
	a_ctxt = AnimContext(this)
	init(null)
	init1(null)
    }

    constructor(ac: AnimContext?) {
	a_ctxt = ac
	init(null)
	init1(null)
    }

    fun init1(ae: AnimEditor?) {
	init("Httpd", null)
	httpd = Httpd.httpd!!
	anim_repository = Anim_Repository()
	help = Anim_HelpSystem()

//	setLayout(new BorderLayout());
	if (ae != null) {
	    AnimContext.ae = ae // PATH FIX
	    aC = AnimCanvas(ae, (a_ctxt)!!)
	} else {
	    aC = AnimCanvas(this, (a_ctxt)!!)
	}
    }

    fun composeAudio(base: String, alt: String?): String {
	if (alt == null || alt.length == 0) return base
	val alt2: String = alt.replace(':', '_')
	val ix: Int = base.lastIndexOf('.')
	val base2l: String = base.substring(0, ix)
	val base2r: String = base.substring(ix)
	return base2l + '-' + alt2 + base2r
    }

    // ${<banid>:<variable#>}
    private fun hasVar(s: String): Boolean {
	return s.indexOf('$') != -1
    }

    fun composeVar(anam: String?): String {
	val sa: Array<String?> = split(anam, "\${}")
	val sb: StringBuffer = StringBuffer()
	for (i in sa.indices) {
	    val s1: String? = sa.get(i)
	    val ix: Int = s1!!.indexOf(':')
	    if (ix != -1) {
		try {
		    val banid: String = s1.substring(0, ix) // if [0] == '@' -> target id
		    val varix: String = s1.substring(ix + 1)
		    Log.getLogger().info(":--: " + "GOT  REF:: " + banid + ':' + varix)
		    val varixi: Int = varix.toInt()
		    if (banid.get(0) == '@') {
			val target_id: String = banid.substring(1)
			val sv: String? = OmegaContext.variables!!.get(target_id + ':' + varix) as String?
			Log.getLogger().info(":--: " + "GOT TARGET REF:: " + target_id + ':' + varix + " = " + sv)
			sa[i] = sv
		    } else {
			// get variable from the actor (actorlist in animator)
			val ac2: Actor? = aC!!.getAnimatedActor(banid)
			//log		    OmegaContext.sout_log.getLogger().info(":--: " + "got ac2 " + banid + ' ' + ac2);
			val gimae2: GImAE = ac2!!.gimae
			sa[i] = gimae2.getVariable(varixi)
		    }
		} catch (ex: Exception) {
		    Log.getLogger().info("ERR: " + "composeVar " + ex)
		    sa[i] = ""
		}
	    }
	    sb.append(sa.get(i))
	}
	return sb.toString()
    }

    fun init(ae: AnimEditor?) {
	a_ctxt!!.mtl = MasterTimeLine((a_ctxt)!!)
	val tl_player: TimeLinePlayer = TimeLinePlayer()
	tl_player.addPlayCtrlListener(a_ctxt!!.mtl!!)
	a_ctxt!!.tl_player = tl_player
	class MTEA : TriggerEventAction() {
	    override fun doAction(te: TriggerEvent?, tm: TimeMarker?, dry: Boolean) {
		try {
		    val cmd: String = te!!.cmd
		    val ac: Actor? = aC!!.getAnimatedActor(tm!!.tl.nid)
		    //log		    OmegaContext.sout_log.getLogger().info(":--: " + "}}}}} action " + cmd + ' ' + tm.tl.nid + ' ' + ac);
		    if (ac == null) return
		    val gimae: GImAE = ac.gimae
		    if ((cmd == "ImageAttrib")) {
			val anam: String = te.argString
			//log			OmegaContext.sout_log.getLogger().info(":--: " + ">>>>> anam " + anam);
			if (hasVar(anam)) {
			    val scat: String = composeVar(anam)
			    //log			    OmegaContext.sout_log.getLogger().info(":--: " + "var attrib " + scat);
			    if (dry) gimae.setAttribName(scat) else gimae.setAttribNameUncommited(scat)
			} else if (dry) gimae.setAttribName(anam) else gimae.setAttribNameUncommited(anam)
		    }
		    if ((cmd == "SetAnimSpeed")) {
			val d: Double = (te as TriggerEventSetAnimSpeed?)!!.argDouble
			gimae.setAnimSpeed(d)
		    }
		    if ((cmd == "SetMirror")) {
			val v: Int = (te as TriggerEventSetMirror?)!!.argInt
			gimae.setMirror((v and 1) == 1, (v and 2) == 2)
		    }
		    if ((cmd == "SetLayer")) gimae.setLayer((te as TriggerEventSetLayer?)!!.argInt)
		    if ((cmd == "Rotate")) {
			var d: Double = (te as TriggerEventRotate?)!!.argDouble
			var d2: Double = (te as TriggerEventRotate?)!!.argDouble2nd
			d /= 360.0
			d *= 3.14159265358979 * 2
			d /= 1000.0
			if (d2 < 19999) {
			    d2 /= 360.0
			    d2 *= 3.14159265358979 * 2
			}
			gimae.setRotation(d, d2, tm.`when`)
		    }
		    if ((cmd == "ResetSequence")) {
			val arg: String = (te as TriggerEventResetSequence?)!!.argString
			gimae.setResetSequence(arg, tm.`when`, tm.tl.getAllTimeMarkerType('[').get(0)!!.`when`)
		    }
		    if ((cmd == "Scale")) {
			var d: Double = (te as TriggerEventScale?)!!.argDouble
			val d2: Double = (te as TriggerEventScale?)!!.getArgDouble2nd(d)
			d /= 1000.0
			gimae.setScale(d, d2, tm.`when`)
		    }
		    if ((cmd == "SetVisibility")) gimae.setVisibility((te as TriggerEventSetVisibility?)!!.argInt)
		    if ((cmd == "PlaySound")) {
			var arg: String = (te as TriggerEventPlaySound?)!!.argString
			if (hasVar(arg)) {
			    val scat: String = composeVar(arg)
			    arg = scat
			}
			val arg_alt: String = gimae.lessonIdAlt.replace(':', '_')
			if (dry) {
			    Log.getLogger().info(":--: " + "PLAY SOUND " + arg + ' ' + arg_alt)
			} else {
			    var ap: APlayer = APlayer.createAPlayer(arg, arg_alt, "TL_" + tm.tl.nid)
			    if (!ap.isLoaded()) {
				val arg_alt2: String = gimae.lessonIdAlt
				var ix: Int
				if ((arg_alt2.lastIndexOf(':').also({ ix = it })) != -1) {
				    val arg_alt3: String = arg_alt2.substring(0, ix)
				    ap = APlayer.createAPlayer(arg, arg_alt3, "TL_" + tm.tl.nid)
				}
			    }
			    //			    ap_li.add(ap);
			    if (ap.isLoaded()) ap.play()
			}
		    }
		    if ((cmd == "Dinner")) {
			val v: Int = (te as TriggerEventDinner?)!!.argInt
			gimae.setDinner((v and 1) == 1, (v and 2) == 2) // can eat, can bee eaten
		    }
		    if ((cmd == "Option")) {
			val v: Int = (te as TriggerEventOption?)!!.argInt
			gimae.setOption(v)
		    }
		} catch (ex: Exception) {
		    Log.getLogger().throwing(this.javaClass.getName(), "doAction", ex)
		}
	    }
	}

	val mtea: MTEA = MTEA()
	a_ctxt!!.mtl!!.addPlayListener(object : PlayListener {
	    override fun actionAtTime(tlA: Array<TimeLine?>?, t: Int, attr: Int, dry: Boolean) {
		if (AnimContext.ae != null) if (!aC!!.isCanvasNormal) {
		    val gel: ToolExecute? = aC!!.toolExecute
		    if (gel != null) gel.execute("upper_left")
		}
		if (!dry) aC!!.updateAtTime(t, (tlA)!!)
	    }

	    override fun actionMarkerAtTime(tm: TimeMarker?, t: Int, dry: Boolean) {
		if (tm!!.type == TimeMarker.BEGIN) {
		    val ac: Actor? = aC!!.getAnimatedActor(tm.tl.nid)
		    if (ac != null) {
			val gimae: GImAE = ac.gimae
			gimae.beginPlay()
		    }
		} else if (tm.type == TimeMarker.END) {
		    a_ctxt!!.tl_player!!.stop()
		} else if (tm.type == TimeMarker.START) {
		} else if (tm.type == TimeMarker.STOP) {
		    val ac: Actor? = aC!!.getAnimatedActor(tm.tl.nid)
		    if (ac != null) {
			val gimae: GImAE = ac.gimae
		    }
		}
		tm.doAllAction(mtea, dry)
	    }
	})
    }

    fun init2() {
	initNew()
    }

    val lessonId_TimeLines: Array<String>
	get() {
	    return a_ctxt!!.mtl!!.lessonId_TimeLines
	}
    val lessonId_Actors: Array<String>
	get() {
	    return aC!!.lessonId_Actors
	}

    fun bindActor(actor_lid: String, timeline_lid: String): Boolean {
	Log.getLogger().info(":--: " + "BINDING " + actor_lid + " -> " + timeline_lid)
	if (!aC!!.bindActor(actor_lid, timeline_lid)) {
	    JOptionPane.showMessageDialog(
		a_ctxt!!.anim_canvas,
		(t("Can't bind actor with timeline; '") +
			actor_lid + "' '" + timeline_lid + "'"),
		"Omega",
		JOptionPane.INFORMATION_MESSAGE
	    )
	    return false
	}
	return true
    }

    fun bindAllNoActor() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "BINDING empty tl");
	aC!!.bindAllNoActor()
    }

    fun initNew() {
	anim_repository!!.clearName()
	a_ctxt!!.mtl!!.initNew()
	aC!!.initNew()
	clean()
    }

    //     boolean[] getActiveActorsEnableState() {
    // 	int n = OmegaConfig.TIMELINES_N;
    // 	boolean bA[] = new boolean[n];
    // 	for(int i = 0; i < bA.length; i++) {
    // 	    Actor act = anim_canvas.getAnimatedActor(i);
    // 	    if ( act != null )
    // 		bA[i] = true;
    // 	}
    // 	return bA;
    //     }
    @JvmOverloads
    fun playAnimation(after: Runnable? = null) {
	try {
	    clean()
	    aC!!.initPlay()
	    aC!!.visibilityMode = AnimCanvas.HIDE_PATH
	    System.gc()
	    a_ctxt!!.tl_player!!.speed = a_ctxt!!.anim_speed
	    aC!!.HIDDEN = false
	    aC!!.repaint()
	    if (a_ctxt!!.tl_player!!.play(after));
	    Log.getLogger().info(":--: " + "playAnimation done zq")
	    clean()
	} catch (ex: NullPointerException) {
	    Log.getLogger().info("ERR: " + "AnimRintime:234: Nullpointerexception " + ex)
	    ex.printStackTrace()
	}
    }

    fun dry_playAnimation(after: Runnable?) {
	try {
	    aC!!.initPlay()
	    aC!!.visibilityMode = AnimCanvas.HIDE_PATH
	    System.gc()
	    a_ctxt!!.tl_player!!.speed = a_ctxt!!.anim_speed
	    a_ctxt!!.tl_player!!.dry_play(after, 100)
	} catch (ex: NullPointerException) {
	    Log.getLogger().info("ERR: " + "AnimRintime:234: Nullpointerexception " + ex)
	    ex.printStackTrace()
	}
    }

    fun stopAnimation() {
	a_ctxt!!.tl_player!!.stop()
	aC!!.hideActors()
    }

    fun decode2D(s: String?): Point2D {
	val sa: Array<String?> = split(s, ",")
	val a: Float = sa.get(0)!!.toFloat()
	val b: Float = sa.get(1)!!.toFloat()
	return Point2D.Float(a, b)
    }

    //      public void loadActor(int ix, String url_s) {
    //  	String ua[] = Files.splitUrlString(url_s);
    //  	if ( ua != null ) {
    //  	    anim_canvas.loadActor(ix, ua[1]);
    //  	} else {
    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "ERROR file: " + url_s);
    //  	}
    //      }
    fun setBackground(url_s: String) {
	val ua: Array<String?>? = Files.splitUrlString(url_s)
	if (ua != null) {
	    aC!!.setBackground(ua.get(1), ArrayList())
	    if (AnimContext.ae != null) {
		if (AnimContext.ae!!.wings_panel != null) {
		    AnimContext.ae!!.wings_panel!!.removeAllWings()
		}
	    }
	} else {
	    Log.getLogger().info(":--: " + "ERROR file: " + url_s)
	}
    }

    fun open(): Element? {
	val fn: String? = anim_repository!!.getNameDlg(aC, true, "Open")
	return open(fn)
    }

    fun open(fn: String?): Element? {
	val el: Element? = anim_repository!!.open(a_ctxt, omegaAssets(fn))
	return el
	//	httpd.getHashMap().put("lesson:loaded resource ", anim_repository.getName());
//fix	anim_canvas.getToolExecute().execute("fit");
//fix	tlc.repaint();
//	anim_repository.setName(fn);
	// FIX
    }

    fun load(el: Element?) {
	anim_repository!!.load(a_ctxt, el)
    }

    var elementRoot: Element? = null
    fun prefetch(fn: String?) {
	a_ctxt!!.arun = this
	initNew()
	aC!!.HIDDEN = true
	elementRoot = open(fn)
	load(elementRoot)
	aC!!.offCenterBackground()
    }

    fun runAction(
	window: Window,
	fn: String?,
	actA: Array<String?>,
	pathA: Array<String?>,
	params: HashMap<String?, Any?>,
	hook: Runnable?
    ) {
	val win: Window = window
	val fa_ctxt: AnimContext? = a_ctxt
	xImage.removeAllEntry()
	aC!!.HIDDEN = true
	fa_ctxt!!.anim_canvas!!.repaint()
	if (hook != null) hook.run()
	fa_ctxt.arun = this
	val spd: Int = (params.get("speed") as Int)
	OmegaContext.SPEED = if (spd > 1250) "-fast" else if (spd < 750) "-slow" else ""
	a_ctxt!!.anim_speed = (params.get("speed") as Int?)!! / 1000.0
	val col: Color? = params.get("anim_background") as Color?
	val colors: HashMap<String, ColorColors>? = params.get("anim_colors") as HashMap<String, ColorColors>?
	fa_ctxt.anim_canvas!!.background_color = (col)!!
	fa_ctxt.anim_canvas!!.colors = (colors)!!
	val variables_hm: Map<*, *>? = params.get("variables") as Map<*, *>?
	OmegaContext.variables = variables_hm
	for (i1 in 0..0) {
	    val lid_timelines: Array<String> = lessonId_TimeLines
	    val lid_actors: Array<String> = lessonId_Actors
	    Log.getLogger().info(":--: " + "anim: TL   " + arrToString(lid_timelines))
	    Log.getLogger().info(":--: " + "anim: Act  " + arrToString(lid_actors))
	    val aaid: Array<String?> = actA
	    Log.getLogger().info(":--: " + "less: act  " + arrToString(aaid) + ' ' + aaid.size)
	    val v_pa: Array<String?> = pathA
	    Log.getLogger().info(":--: " + "less: path " + arrToString(v_pa) + ' ' + v_pa.size)
	    for (i in v_pa.indices) try {
		val a: Int = v_pa.get(i)!!.toInt()
		v_pa[i] = lid_timelines.get(a - 1)
	    } catch (ex: NumberFormatException) {
	    } catch (ex: Exception) {
		JOptionPane.showMessageDialog(
		    fa_ctxt.anim_canvas,
		    t("No named timeline (lesson id)"),
		    "Omega",
		    JOptionPane.ERROR_MESSAGE
		)
		System.exit(1)
	    }
	    Log.getLogger().info(":--: " + "nVt " + arrToString(v_pa))
	    fa_ctxt.anim_canvas!!.bindAllStatistActor()
	    var ok: Boolean = true
	    bindAllNoActor()
	    for (i in aaid.indices) {
		try {
		    if (bindActor(aaid.get(i)!!, v_pa.get(i)!!)) ok = ok and true else ok = false
		} catch (ex: Exception) {
		    var ac_s: String = "?"
		    var v_s: String = "?"
		    try {
			ac_s = "" + aaid.get(i)
			v_s = "" + v_pa.get(i)
		    } catch (ex2: Exception) {
		    }
		    JOptionPane.showMessageDialog(
			fa_ctxt.anim_canvas,
			(t("Can't bind path and actor") + '\n' +
				"" + ac_s + " -> " + v_s),
			"Omega",
			JOptionPane.INFORMATION_MESSAGE
		    )
		    ok = false
		}
	    }
	    if (ok) {
		//		SundryUtils.m_sleep(100);
// 		if ( hook != null )
// 		    hook.run();
//  		fa_ctxt.anim_canvas.centerBackground();
		val end_code: Array<String?> = arrayOfNulls(1)
		end_code[0] = null

//   		a_ctxt.anim_canvas.requestFocus();
//   		a_ctxt.anim_canvas.repaint();
		val drct0: Long = ct()
		dry_playAnimation(object : Runnable {
		    override fun run() {
			Log.getLogger().info(":--: " + "Dry Running done " + (ct() - drct0))
		    }
		})

// 		if ( hook != null )
// 		    hook.run();

// 		fa_ctxt.anim_canvas.HIDDEN = false;
//   		fa_ctxt.anim_canvas.repaint();
		fa_ctxt.anim_canvas!!.centerBackground()
		val rct0: Long = ct()
		aC!!.HIDDEN = false
		playAnimation(object : Runnable {
		    override fun run() {
			Log.getLogger().info(":--: " + "Running done " + (ct() - rct0))
			//fa_ctxt.anim_canvas.hideActors(); // LAST
			val end_code_s: String = fa_ctxt.anim_canvas!!.endCode
			end_code[0] = end_code_s
			Log.getLogger().info(":--: " + "Endcode " + end_code_s)
		    }
		})
		while (end_code.get(0) == null) m_sleep(200)
		clean()
	    } else {
//		OmegaContext.sout_log.getLogger().info(":--: " + "*** Running 1 fail&done. ***");
		fa_ctxt.anim_canvas!!.hideActors()
	    }
	    Log.getLogger().info(":--: " + "--------ok-------")
	}
    }

    @Synchronized
    fun clean() {
	Log.getLogger().info(":--: " + "AnimRuntime Close")

// 	int i = 0;
// 	Iterator it = ap_li.iterator();
// 	while(it.hasNext()) {
// 	    APlayer ap = (APlayer)it.next();
// 	    // OmegaContext.sout_log.getLogger().info(":--: " + "Close " + i++ + ' ' + ap.nname);
// 	    ap.close();
// 	}
// 	ap_li = new ArrayList();
    }

    companion object {
	var context: OmegaContext? = null
	private fun toURL(file: File?): String? {
	    return Files.toURL(file!!)
	}
    }
}
