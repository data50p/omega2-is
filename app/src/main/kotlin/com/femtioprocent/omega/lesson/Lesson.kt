package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaConfig.isKeyESC
import com.femtioprocent.omega.OmegaConfig.isKeyNext
import com.femtioprocent.omega.OmegaConfig.isKeySelect
import com.femtioprocent.omega.OmegaConfig.isLIU_Mode
import com.femtioprocent.omega.OmegaConfig.setNextKey
import com.femtioprocent.omega.OmegaConfig.setSelectKey
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.antiOmegaAssets
import com.femtioprocent.omega.OmegaContext.Companion.getMediaFile
import com.femtioprocent.omega.OmegaContext.Companion.media
import com.femtioprocent.omega.OmegaContext.Companion.omegaAssets
import com.femtioprocent.omega.adm.register.data.*
import com.femtioprocent.omega.appl.OmegaAppl.Companion.waitAndCloseSplash
import com.femtioprocent.omega.lesson.actions.Action
import com.femtioprocent.omega.lesson.actions.AnimAction
import com.femtioprocent.omega.lesson.actions.MpgAction
import com.femtioprocent.omega.lesson.appl.ApplContext
import com.femtioprocent.omega.lesson.appl.ApplLesson
import com.femtioprocent.omega.lesson.appl.LessonRuntime
import com.femtioprocent.omega.lesson.canvas.*
import com.femtioprocent.omega.lesson.canvas.BaseCanvas.ColorColors
import com.femtioprocent.omega.lesson.canvas.result.ResultDialogTableSummary
import com.femtioprocent.omega.lesson.machine.Item
import com.femtioprocent.omega.lesson.machine.Machine
import com.femtioprocent.omega.lesson.machine.Target
import com.femtioprocent.omega.lesson.managers.movie.LiuMovieManager
import com.femtioprocent.omega.lesson.pupil.Pupil
import com.femtioprocent.omega.lesson.repository.LessonItem
import com.femtioprocent.omega.lesson.repository.Restore.restore
import com.femtioprocent.omega.lesson.repository.Save.save
import com.femtioprocent.omega.lesson.settings.OmegaSettingsDialog
import com.femtioprocent.omega.lesson.settings.PupilSettingsDialog
import com.femtioprocent.omega.media.audio.APlayer
import com.femtioprocent.omega.media.audio.APlayer.Companion.createAPlayer
import com.femtioprocent.omega.media.audio.TTS.say
import com.femtioprocent.omega.media.video.VideoUtil.findSupportedFname
import com.femtioprocent.omega.message.Listener
import com.femtioprocent.omega.swing.filechooser.ChooseColorFile
import com.femtioprocent.omega.swing.filechooser.ChooseLessonFile
import com.femtioprocent.omega.t9n.T
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Files.mkRelativeCWD
import com.femtioprocent.omega.util.Files.toURL
import com.femtioprocent.omega.util.Log.getLogger
import com.femtioprocent.omega.util.SundryUtils.a2s
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.empty
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import com.femtioprocent.omega.util.SundryUtils.pL
import com.femtioprocent.omega.util.SundryUtils.rand
import com.femtioprocent.omega.util.SundryUtils.scrambleArr
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.util.SundryUtils.upTo
import com.femtioprocent.omega.value.Values
import com.femtioprocent.omega.xml.Element
import javafx.scene.media.AudioClip
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.prefs.Preferences
import javax.print.PrintService
import javax.swing.*
import kotlin.concurrent.withLock
import kotlin.time.measureTime

// has UTF-8 ¬ß
class Lesson(run_mode: Char) : LessonCanvasListener {
    var globalExit = false
    var l_ctxt: LessonContext
    var run_mode = 'p'.code
    var audio_log = OmegaContext.def_log
    var lesson_log = OmegaContext.def_log
    var msg_log = OmegaContext.def_log
    var machine: Machine
    var window: Window? = null
    private var card: CardLayout? = null
    private var card_panel: JPanel? = null
    var lep: LessonEditorPanel? = null
    var saved_name: String? = null
    var loadedFName: String? = null
    var action: Action? = null
    var mpg_action: MpgAction? = null
    var element_root: Element? = null
    var element_root2: Element? = null
    var seq: Sequencer? = null
    var rdlg: ResultDialogTableSummary? = null
    var is_testing = false
    private var current_test_mode = TM.CREATE
    private var current_test_mode_group = mkTestModeGroup(current_test_mode)
    var litm: LessonItem? = null
    private var current_pupil: Pupil? = null
    var session_length_start = ct()
    var lessonLang: String? = null

    enum class TM(val code: Int) {
	CREATE(0),
	RAND(1),
	PRE_1(10),
	PRE_2(11),
	POST_1(20),
	POST_2(21)
    }

    enum class TMG(val code: Int) {
	CREATE(0),
	TEST(1)
    }

    fun constructTM(t_mode: Int): TM {
	when (t_mode) {
	    TM.CREATE.code -> return TM.CREATE
	    TM.RAND.code -> return TM.RAND
	    TM.PRE_1.code -> return TM.PRE_1
	    TM.PRE_2.code -> return TM.PRE_2
	    TM.POST_1.code -> return TM.POST_1
	    TM.POST_2.code -> return TM.POST_2
	    else -> throw IllegalArgumentException("No such TM enum: " + t_mode)
	}
    }

    fun constructTMG(t_mode_group: Int): TMG {
	when (t_mode_group) {
	    TMG.CREATE.code -> return TMG.CREATE
	    TMG.TEST.code -> return TMG.TEST
	    else -> throw IllegalArgumentException("No such TMG enum: " + t_mode_group)
	}
    }

    class Message(var msg: String, var obj: Any?, var msg_time: Long, var id: String)
    inner class PlayDataList {
	var ord = 0
	var date: Date? = null
	var arr = ArrayList<PlayData>()
	fun nextOrd(): Int {
	    if (ord != 0) return ord
	    val prefs = Preferences.userNodeForPackage(this.javaClass)
	    val ordinal = prefs["story_ordinal", "0"]
	    ord = ordinal.toInt()
	    ord++
	    prefs.put("story_ordinal", "" + ord)
	    return ord
	}

	fun add(play_data: PlayData) {
	    arr.add(play_data)
	    date = Date()
	}
    }

    private fun signMoviePrepare(tg: Target, tg_ix: Int): LiuMovieManager? {
	OmegaContext.sout_log.getLogger().info("Liu Prepare sign movie")
	val tgr = le_canvas!!.getTargetRectangle(tg_ix)
	val lmm = LiuMovieManager(window!!, le_canvas!!)
	val t_Item = tg.getT_Item(tg_ix)
	val smfName = lmm.getSignMovieFileName(t_Item!!.item!!, tg, tg_ix)
	if (smfName != null) {
	    val liuMovieOk = lmm.prepare(media(), smfName, true)
	    if (!liuMovieOk) {
		OmegaContext.serr_log.getLogger().info("sign move not OK")
		return null
	    }
	} else {
	    OmegaContext.serr_log.getLogger().info("sign move not OK'")
	    return null
	}
	OmegaContext.serr_log.getLogger().info("Sign movie ok")
	return lmm
    }

    private fun playSignFile(tg: Target, editMode: Boolean) {
	val lmm = LiuMovieManager(window!!, le_canvas!!)
	try {
	    val bgCol = omega_settings_dialog.signSentence_background!!.color
	    val alphaCol = omega_settings_dialog.signSentence_alpha!!.value
	    var sms = omega_settings_dialog.signMovieSentence_scale!!.value
	    if (sms == 0) {
		sms = 1
	    }
	    val all_text = tg.allText
	    val specificSignMovie = action_specific!!.isSign(all_text)
	    if (specificSignMovie) {
		try {
		    if (false && all_text.startsWith("MMMM ")) {
			var cnt = 0
			val dir = File("../../TextbitarB-F-")
			for (f in dir.listFiles()) {
			    val sign_mv = f.name
			    if (false) {
				if (lmm.prepare(dir.absolutePath + "/", sign_mv, true)) {
				    val sh = le_canvas!!.targetShape
				    val tgr = sh.bounds
				    startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2)
				    lmm.cleanup()
				    if (cnt++ > 10) {
					break
				    }
				}
			    } else {
			    }
			}
		    }
		    val specificMovieName = action_specific!!.getSign(all_text)
		    if (lmm.prepare("", specificMovieName!!, true)) {
			val sh = le_canvas!!.targetShape
			val tgr = sh.bounds
			startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2)
		    }
		} catch (ex_mv: Exception) {
		    OmegaContext.sout_log.getLogger().info("While play movie: $ex_mv")
		}
	    } else {
		val smFname = getMediaFile("sign-" + OmegaContext.lessonLang + "/" + all_text + ".mp4")
		val smFileName = findSupportedFname(smFname!!)
		val smFile = smFileName?.let { File(it) }
		if (smFile != null && smFile.exists() && smFile.canRead()) {
		    try {
			le_canvas!!.setMarkTargetAll()
			if (lmm.prepare("", smFname, true)) {
			    val sh = le_canvas!!.targetShape
			    val tgr = sh.bounds
			    startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2)
			    lmm.cleanup()
			}
		    } catch (ex_mv: Exception) {
			OmegaContext.sout_log.getLogger().info("While play movie: $ex_mv")
		    } finally {
		    }
		} else {
		    // word by word
		    val sign_movies = tg.getAllSignMovies(lmm)
		    var tg_ix = 0
		    for (sign_mv in sign_movies) {
			try {
			    le_canvas!!.setMarkTarget(tg_ix, true)
			    if (sign_mv.length > 0 && lmm.prepare(media(), sign_mv, true)) {
				val sh = le_canvas!!.targetShape
				val tgr = sh.bounds
				startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2)
				lmm.cleanup()
			    }
			} catch (ex_mv: Exception) {
			    OmegaContext.sout_log.getLogger().info("While play movie: $tg_ix $ex_mv")
			} finally {
			    tg_ix++
			}
		    }
		}
	    }
	} finally {
	    mistNoMouse = false
	    le_canvas!!.setMist(0, null, null, 0)
	    lmm.cleanup()
	}
    }

    private fun startMovieAndWait(
	    lmm: LiuMovieManager,
	    tgr: Rectangle,
	    bgCol: Color,
	    alphaCol: Int,
	    scale: Int,
	    mistMode: Int
    ) {
	mistNoMouse = true
	le_canvas!!.setMist(mistMode, tgr, bgCol, alphaCol)
	val mRect = lmm.start(
		(tgr.getX() + tgr.getWidth() / 2).toInt(),
		(tgr.getY() + tgr.getHeight()).toInt(),
		(100 / scale).toDouble()
	)
	le_canvas!!.signMovieRectangle = mRect
	lmm.wait((tgr.getX() + tgr.getWidth() / 2).toInt(), (tgr.getY() + tgr.getHeight()).toInt(), 100.0 / scale)
    }

    class SentenceList : Serializable {
	var sentence_list: ArrayList<String?>?
	var asString: String? = null
	var lesson_name: String

	internal constructor(s: String?) {
	    sentence_list = ArrayList()
	    sentence_list!!.add(s)
	    asString = s
	    lesson_name = ""
	}

	internal constructor() {
	    sentence_list = ArrayList()
	    lesson_name = ""
	}

	override fun toString(): String {
	    return "$lesson_name:$sentence_list:$asString"
	}
    }

    inner class RegisterProxy {
	var pupil: Pupil
	var rl: RegLocator
	var rt: ResultTest?
	var started: Long = 0
	var last_ct: Long = 0
	var lesson_name: String?
	var test_mode: String?
	var register_log = OmegaContext.def_log
	var has_shown = false

	internal constructor(pupil: Pupil) {
	    //log OmegaContext.sout_log.getLogger().info(":--: " + "PUPIL REG created ");
	    this.pupil = pupil
	    lesson_name = null
	    test_mode = null
	    rl = RegLocator()
	    rt = null
	}

	internal constructor(pupil: Pupil, lesson_name: String, test_mode: TM) {
	    //log OmegaContext.sout_log.getLogger().info(":--: " + "PUPIL REG created ");
	    this.pupil = pupil
	    this.lesson_name = lesson_name
	    this.test_mode = getTestModeString(test_mode)
	    rl = RegLocator()
	    rt = ResultTest(pupil.name, fix(lesson_name), this.test_mode!!)
	    started = ct()
	}

	fun setStarted() {
	    started = ct()
	    last_ct = started
	    register_log.getLogger().info("started $started")
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + " STARTED ");
	}

	private fun getTestModeString(test_mode: TM): String {
	    return when (test_mode) {
		TM.CREATE -> "create"
		TM.RAND -> "test"
		TM.PRE_1 -> "pre1"
		TM.PRE_2 -> "pre2"
		TM.POST_1 -> "post1"
		TM.POST_2 -> "post2"
		else -> "X"
	    }
	}

	fun fix(lesson_name: String): String {
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "fix " + lesson_name);
	    var ix = lesson_name.lastIndexOf('/')
	    var s = lesson_name.substring(0, ix)
	    ix = s.lastIndexOf('/')
	    s = s.substring(ix + 1)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "fix -> " + s);
	    return s
	}

	fun restart() {
	    register_log.getLogger().info("restart ")
	    has_shown = false
	}

	fun word(mode: String, when_ct: Long, word: String?, l_id: String?) {
	    //log OmegaContext.sout_log.getLogger().info(":--: " + "PUPIL REG " + mode + ' ' + word);
	    val `when` = (when_ct - last_ct).toInt()
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "WHEN " + (when_ct - SundryUtils.ct()));
	    last_ct = when_ct
	    rt!!.add(SelectEntry(mode, word!!, `when`, l_id!!))
	    register_log.getLogger().info("word: $mode $word $l_id")
	    //	    semiclose();
	    has_shown = false
	}

	fun test(
		mode: String,
		when_ct: Long,
		sentence: String?,
		answer: String,
		correct_words: String?,
		l_id_list: String?
	) {
	    //log OmegaContext.sout_log.getLogger().info(":--: " + "PUPIL REG " + mode + ' ' + sentence + ' ' + answer);
	    val `when` = (when_ct - started).toInt()
	    rt!!.add(
		    TestEntry(
			    mode,
			    sentence!!,
			    answer,
			    `when`,
			    correct_words!!,
			    l_id_list!!
		    )
	    )
	    register_log.getLogger().info("test: $mode $answer $sentence")
	    //	    semiclose();
	    has_shown = false
	}

	fun create(
		mode: String,
		when_ct: Long,
		sentence: String,
		duration: Int,
		l_id_list: String?
	) {
	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "PUPIL REG " + mode + ' ' + sentence + ' ' + duration);
	    val `when` = (when_ct - started).toInt()
	    rt!!.add(CreateEntry(sentence, `when`, l_id_list!!))
	    register_log.getLogger().info("creat: $mode $sentence")
	    //	    semiclose();
	    has_shown = false
	}

	// 	void semiclose() {
	// 	    SaveRestore sr = new SaveRestore();
	// 	    sr.save(rl.mkResultsFName(pupil.getName(), rt.mkFname()), rt);
	// 	}
	fun close() {
	    val cnt = rt!!.howManyTestEntries()
	    if (cnt > 0) {
		val sr = SaveRestore()
		rt!!.session_length = ct() - session_length_start
		if (sr.save(rl.mkResultsFName(pupil.name, rt!!.mkFname(currentPupil.name)), rt!!) == false) {
		    register_log.getLogger().info(
			    "failed saved: " + rl.mkResultsFName(
				    pupil.name, rt!!.mkFname(
				    currentPupil.name
			    )
			    )
		    )
		} else {
		    register_log.getLogger()
			    .info("saved: " + rl.mkResultsFName(pupil.name, rt!!.mkFname(currentPupil.name)))
		}
	    } else {
		register_log.getLogger().info("saved: -empty-")
	    }
	}

	fun getAllTestsAsName(with: Array<String>?): Array<String> {
	    val sa = rl.getAllResultsFName(pupil.name, with!!)
	    for (i in sa!!.indices) {
		var s = sa[i]
		s = s.replace('\\', '/')
		s = s.substring(0, s.lastIndexOf('.'))
		s = s.substring(s.lastIndexOf('/') + 1)
		sa[i] = s
	    }
	    Arrays.sort(sa) { o1, o2 ->
		val s1 = o1 as String
		val s2 = o2 as String
		s2.compareTo(s1)
	    }
	    return sa
	}

	override fun toString(): String {
	    return "RegPr$has_shown"
	}
    }

    var register: RegisterProxy? = null

    inner class Canvases {
	var hm: HashMap<String?, BaseCanvas?>

	init {
	    hm = HashMap()
	}

	fun keySet(): Set<String?> {
	    return hm.keys
	}

	operator fun get(id: String?): BaseCanvas? {
	    return hm[id]
	}

	fun put(id: String?, lbc: BaseCanvas?) {
	    hm[id] = lbc
	}
    }

    var canvases = Canvases()

    inner class Progress internal constructor() {
	var f: JFrame
	var progressBar: JProgressBar

	init {
	    f = JFrame(t("Loading"))
	    val con = f.contentPane
	    progressBar = JProgressBar()
	    //when the task of (initially) unknown length begins:
	    progressBar.isIndeterminate = true
	    con.add(progressBar)
	    f.pack()
	    val scr = Toolkit.getDefaultToolkit().screenSize
	    val x = scr.width / 2
	    val y = scr.height / 2
	    f.setLocation(x - 100, y)
	    f.isVisible = true
	}

	fun show() {
	    progressBar.value = 0
	    progressBar.isIndeterminate = true
	    f.isVisible = true
	}

	fun dismiss() {
	    f.isVisible = false
	    progressBar.isIndeterminate = false
	}
    }

    var progress: Progress? = null

    //     private class AudioPrefetch {
    // 	private HashMap prfHM;
    // 	private ObjectCache audio_prefetch_cache = new ObjectCache("audioPrefetch");
    // 	AudioPrefetch() {
    // 	    loadit();
    // 	}
    // 	private void prefetchAny(String id) {
    // 	    String[] sa = (String[])prfHM.get(id);
    // 	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "prefetched " + sa);
    // 	    if ( sa == null )
    // 		return;
    // 	    for(int i = 0; i < sa.length;i++) {
    // 		String as = sa[i];
    // 		APlayer.prefetch(as);
    // 		//		OmegaContext.sout_log.getLogger().info(":--: " + "prefetched " + as);
    // 	    }
    // 	}
    // 	public void saveit(String lesson_fn, String[] used_file) {
    // 	    fpdo.sundry.Timer tm = new fpdo.sundry.Timer();
    // 	    prfHM.put(lesson_fn, used_file);
    // 	    audio_prefetch_cache.save(prfHM);
    // 	    audio_log.getLogger().info("saveit: " + prfHM + ' ' + tm.get());
    // 	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "saveit " + prfHM + ' ' + tm.get());
    // 	}
    // 	private void loadit() {
    // 	    prfHM = (HashMap)audio_prefetch_cache.load();
    // 	    if ( prfHM == null )
    // 		prfHM = new HashMap();
    // 	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "PREFETCH CACHE " + prfHM);
    // 	    Iterator it = prfHM.keySet().iterator();
    // 	    while(it.hasNext()) {
    // 		String id = (String)it.next();
    // 		String[] afn = (String[])prfHM.get(id);
    // 		//		OmegaContext.sout_log.getLogger().info(":--: " + "PC " + id + ' ' + SundryUtils.a2s(afn));
    // 	    }
    // 	}
    //     }
    //     private AudioPrefetch audio_prefetch = new AudioPrefetch();

    fun genSeqKey(a: Int): String {
	return pL(a, 6, '0')
    }

    internal inner class ListAndIterator<T>(var list: List<T>) {
	var it: ListIterator<T>

	init {
	    it = list.listIterator()
	}
    }

    inner class Sequencer(el: Element) {
	var index = 0
	var el: Element? = null
	var rand_map: IntArray? = null
	private var texts: HashMap<String?, ListAndIterator<String?>?>
	var current: String?
	var cnt_sent_correct = 0
	var cnt_sent_wrong = 0
	var cnt_word_correct = 0
	var cnt_word_wrong = 0
	var cnt_wordlast_correct = 0
	var cnt_wordlast_wrong = 0
	private val t_id = arrayOf("pre", "post")

	init {
	    texts = HashMap()
	    buildTestSentence(el)
	    current = null
	}

	fun getStat(b: Boolean): String {
	    return (if (b) "s 1" else "s 0") + ";w +" + cnt_wordlast_correct + " -" + cnt_wordlast_wrong
	}

	fun genKey(test_mode: TM): String {
	    return "" + test_mode
	}

	fun getTestText(test_mode: TM, full: Boolean): String? {
	    val s = getTestText(test_mode)
	    if (s == null) {
		return s
	    }
	    return if (full) {
		s
	    } else s.replace("\\{[^\\{\\}]*\\}".toRegex(), "")
	}

	fun getTestTextSet(txt: String?, test_mode: TM, full: Boolean): Set<String> {
	    val set: MutableSet<String> = HashSet()
	    var s = getTestText(test_mode)
	    if (s == null) {
		//return s;
	    }
	    if (full) {
	    }
	    s = s!!.replace("\\{[^\\{\\}]*\\}".toRegex(), "")
	    set.add(s)
	    return set
	}

	private fun getTestText(test_mode: TM): String? {
	    if (current != null) {
		return current
	    }
	    return if (test_mode == TM.RAND) {
		val saAll = l_ctxt.lessonCanvas.allTargetCombinations
		val sa = removeOff(action_specific, saAll)
		if (rand_map == null || rand_map!!.size != sa.size) {
		    rand_map = upTo(sa.size)
		    scrambleArr(rand_map)
		}
		try {
		    return sa[rand_map!![index]]
		} catch (ex: Exception) {
		}
		null
	    } else {
		val it = getIterator(test_mode) as ListIterator<String>
		val test_text: String?
		if (it.hasNext()) {
		    val s = it.next()
		    it.previous()
		    //log 		    OmegaContext.sout_log.getLogger().info(":--: " + "test text " + s);
		    val ix = s.indexOf(':')
		    if (ix != -1) {
			test_text = s.substring(ix + 1)
		    } else {
			test_text = s
		    }
		} else {
		    test_text = null
		}
		return test_text
	    }
	}

	private fun removeOff(action_specific: ActionSpecific?, saAll: Array<String?>): Array<String?> {
	    var nOff = 0
	    for (o in action_specific!!.hm.values) {
		val s = o
		if (s != null && s.length > 0 && s.equals("!off", ignoreCase = true)) nOff++
	    }
	    val n = saAll.size - nOff
	    val sa = arrayOfNulls<String>(n)
	    var ix = 0
	    for (s in saAll) {
		val v = action_specific.hm[s]
		if (v == null || v.length > 0 && !v.equals("!off", ignoreCase = true)) {
		    sa[ix++] = s
		}
	    }
	    return sa
	}

	fun getIterator(test_mode: TM): ListIterator<Any?>? {
	    val key = genKey(test_mode)
	    val lai = texts[key] as ListAndIterator<*>? ?: return null
	    return lai.it
	}

	fun getNewIterator(test_mode: TM): ListIterator<Any?>? {
	    val key = genKey(test_mode)
	    val lai = texts[key] as ListAndIterator<*>? ?: return null
	    return lai.list.listIterator()
	}

	fun getList(test_mode: TM): List<Any?>? {
	    val key = genKey(test_mode)
	    val lai = texts[key] as ListAndIterator<*>? ?: return null
	    return lai.list
	}

	fun initNewTest() {
	    rand_map = null
	    index = 0
	    texts = HashMap()
	    current = null
	}

	fun next(test_mode: TM): Boolean {
	    cnt_wordlast_wrong = 0
	    cnt_wordlast_correct = 0
	    if (test_mode == TM.RAND) {
		index++
		return getTestText(test_mode, false) != null
	    }
	    current = null
	    val it = getIterator(test_mode) ?: return false
	    if (it.hasNext()) {
		it.next()
		return true
	    }
	    return false
	}

	fun getTestMatrix(all_sentence: Array<String?>): Array<IntArray> {
	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "###### getTestMatrix");
	    val tmm = Array(all_sentence.size) { IntArray(4) }
	    val map = arrayOf(
		    TM.PRE_1,
		    TM.PRE_2,
		    TM.POST_1,
		    TM.POST_2
	    )
	    for (i in 0..3) {
		val t_mode = map[i]
		val it = getNewIterator(t_mode) as ListIterator<String?>?
		if (it != null) {
		    while (it.hasNext()) {
			val sa = split(it.next(), ":")
			val ord = 1 + sa[0].toInt()
			val txt = sa[1]
			val ix2 = where(txt, all_sentence)
			if (ix2 >= 0) {
			    tmm[ix2][i] = ord
			}
		    }
		} else {
		    getLogger().info("NPE")
		}
	    }
	    return tmm
	}

	fun setFromMatrix(sentA: Array<String?>, tmm: Array<IntArray>) {
	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "###### setFromMatrix");
	    val len = tmm.size
	    if (len > 0) {
		for (i in tmm[0].indices) {
		    val li: MutableList<String> = ArrayList()
		    val t_mode = (i / 2 + 1) * 10 + i % 2
		    val t_mode_e = constructTM(t_mode)
		    for (j in 0 until len) {
			val ord = tmm[j][i]
			if (ord > 0) {
			    val txt = sentA[j]

			    //log 			    OmegaContext.sout_log.getLogger().info(":--: " + "--- dep_set seq " + j + ' ' + ord + ' ' + txt);
			    val ord_i = ord - 1
			    li.add(genSeqKey(ord_i) + ':' + txt)
			}
		    }
		    texts[genKey(t_mode_e)] = ListAndIterator(li)
		}
	    }
	}

	fun buildTestSentence(el: Element) {
	    if (this.el === el) {
		return
	    }

	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "###### buildTestSent");
	    val t_el = el.findElement("test_entries", 0)
	    if (t_el != null) {
		for (i in 0..9) {
		    val ty_el = t_el.findElement("test_entry", i)
		    if (ty_el != null) {
			val a_type = ty_el.findAttr("type") as String
			val a_ord = ty_el.findAttr("ord") as String
			var t_mode = if (a_type == t_id[0]) 10 else 20
			t_mode += if (a_ord == "0") 0 else 1
			val t_mode_e = constructTM(t_mode)
			var li: MutableList<String?> = ArrayList()
			for (ii in 0..99) {
			    val s_el = ty_el.findElement("sentence", ii) ?: break
			    val ord_s = s_el.findAttr("ord") as String
			    val ord_i = ord_s.toInt()
			    val text = s_el.findAttr("text") as String
			    li.add(genSeqKey(ord_i) + ':' + text)

			    //log 			    OmegaContext.sout_log.getLogger().info(":--: " + "sent li " + li);
			}
			li = sortList(li)
			texts[genKey(t_mode_e)] = ListAndIterator(li)
		    }
		}
		this.el = el
		//log 		OmegaContext.sout_log.getLogger().info(":--: " + "saved test sent " + el);
	    }
	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "++++++++++++++++++++++ " + texts);
	}

	val element: Element
	    get() {
		val el = Element("test_entries")
		for (i in 0..1) {
		    for (j in 0..1) {
			val t_mode = 10 * (i + 1) + j
			val t_mode_e = constructTM(t_mode)
			val li_ = getList(t_mode_e)
			if (li_ != null && li_.size > 0) {
			    val li = li_.shuffled() as ArrayList<String> // why shuffle when we sort below
			    val sa = li.toTypedArray<String>()
			    Arrays.sort(sa)
			    val te_el = Element("test_entry")
			    te_el.addAttr("type", t_id[i])
			    te_el.addAttr("ord", "" + j)
			    for (ii in sa.indices) {
				val s = sa[ii]
				val sa2 = split(s, ":")
				val ord = sa2[0].toInt()
				val txt = sa2[1]
				val s_el = Element("sentence")
				s_el.addAttr("ord", "" + ord)
				s_el.addAttr("text", txt)
				te_el.add(s_el)
			    }
			    el.add(te_el)
			}
		    }
		}
		return el
	    }

	fun dump() {
	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "Seq: texts = " + texts);
	}

	override fun toString(): String {
	    return ("seq{"
		    + getTestText(current_test_mode, false)
		    + "}")
	}
    }

    fun sortList(li: List<String?>): MutableList<String?> {
	val sa = li.toTypedArray<String?>()
	Arrays.sort(sa)
	val nli = ArrayList<String?>()
	for (i in sa.indices) {
	    nli.add(sa[i])
	}
	return nli
    }

    var currentPupil: Pupil
	get() {
	    if (pupil_canvas == null) {
		if (current_pupil == null) {
		    current_pupil = Pupil("Guest")
		}
		return current_pupil as Pupil
	    }
	    val pupil_name = pupil_canvas!!.pupilName
	    if (current_pupil != null && pupil_name == current_pupil!!.name) {
		return current_pupil!!
	    }
	    current_pupil = Pupil(pupil_name)
	    return current_pupil!!
	}
	private set(pupil) {
	    current_pupil = pupil
	}
    var pupil_settings_dialog = PupilSettingsDialog(this)
    var last_ord = -1
    fun act_performLesson(msg: String?) {
	story_hm["sentence_list"] = SentenceList()
	OmegaContext.story_log.getLogger().info("mew sentL 619 " + story_hm)
	last_story_flag = false
	try {
	    val s = msg!!.substring(12)
	    val sn = msg.substring(19)
	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "Load lesson " + s);
	    val ord = sn.toInt()
	    val ima = lemain_canvas!!.lesson[ord] ?: return
	    litm = ima.o as LessonItem?
	    if (litm == null) {
	    } else if (litm!!.isDir) {
		//log 		    OmegaContext.sout_log.getLogger().info(":--: " + "Load group " + s);
		lemain_canvas!!.setRedPush(ord)
		lemain_canvas!!.addLessonBase(litm!!.lessonName, ord)
		lemain_canvas!!.requestFocusOrd(0)
	    } else {
		lemain_canvas!!.setRedPush(ord)
		lemain_canvas!!.tellLessonBase(litm!!.lessonName, ord)
		val lesson_name = litm!!.defaultLessonFile
		getLogger().info("Found lesson: $lesson_name")
		if (lesson_name == null) {
		    JOptionPane.showMessageDialog(
			    ApplContext.top_frame,
			    t("Can't find lesson file")
		    )
		    return
		}
		loadTest(lesson_name)
		last_ord = ord
		//		    }
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: Exception in performLesson: $ex")
	    ex.printStackTrace()
	}
    }

    fun mkTestModeGroup(test_mode: TM): TMG {
	when (test_mode) {
	    TM.CREATE -> return TMG.CREATE
	    TM.RAND, TM.PRE_1, TM.PRE_2, TM.POST_1, TM.POST_2 -> return TMG.TEST
	}
	throw Error("no TMG")
    }

    fun isTestModeGroup(test_mode: TM, test_mode_group: TMG): Boolean {
	val tmg = mkTestModeGroup(test_mode)
	return tmg == test_mode_group
    }

    fun prepareTest(mode: String) {
	session_length_start = ct()
	//log 	OmegaContext.sout_log.getLogger().info(":--: " + "prepare test " + mode);
	try {
	    if ("create" == mode) {
		current_test_mode = TM.CREATE
		return
	    }
	    if ("pupil_1" == mode) {
		current_test_mode = TM.RAND
		return
	    }
	    if ("pre_1" == mode) {
		current_test_mode = TM.PRE_1
		return
	    }
	    if ("pre_2" == mode) {
		current_test_mode = TM.PRE_2
		return
	    }
	    if ("post_1" == mode) {
		current_test_mode = TM.POST_1
		return
	    }
	    if ("post_2" == mode) {
		current_test_mode = TM.POST_2
		return
	    }
	} finally {
	    current_test_mode_group = mkTestModeGroup(current_test_mode)
	    lemain_canvas!!.reload()
	    lemain_canvas!!.modeIsTest = current_test_mode_group == TMG.TEST
	}
    }

    fun loadTest(fn: String) {

	// 	OmegaContext.sout_log.getLogger().info(":--: " + "loadTest >>> test_mode = " + current_test_mode);
	register = RegisterProxy(currentPupil, fn, current_test_mode)
	le_canvas!!.disposeOldLesson()
	when (current_test_mode) {
	    TM.CREATE -> {
		card_show("words")
		if (register != null) {
		    register!!.restart()
		}
		sendMsg("create", fn, "loadTest1")
	    }

	    TM.RAND -> {
		card_show("anim1")
		sendMsg("new_test", fn, "loadTest2")
	    }

	    TM.PRE_1, TM.PRE_2, TM.POST_1, TM.POST_2 -> {
		card_show("anim1")
		sendMsg("new_test", fn, "loadTest3")
	    }
	}
	//log 	OmegaContext.sout_log.getLogger().info(":--: " + "loadTest <<<");
	le_canvas!!.hideMsg()
    }

    val resultSummary_MsgItem: MsgItem?
	get() {
	    OmegaContext.sout_log.getLogger().info(":--: getRslt $register")
	    if (register != null && register!!.has_shown) {
		OmegaContext.sout_log.getLogger()
			.info(":--: " + "2006: register " + register + ' ' + if (register != null) register!!.has_shown else false)
		return null
	    }
	    return try {
		MsgItem(
			'S',
			t("Test Statistics"),
			t("Correct") + ": "
				+ seq!!.cnt_sent_correct + " "
				+ fixSP(t("sentence "), t("sentences"), seq!!.cnt_sent_correct)
				+ " ("
				+ seq!!.cnt_word_correct + " "
				+ fixSP(t("word"), t("words"), seq!!.cnt_word_correct) + ")",
			t("Wrong") + ": "
				+ seq!!.cnt_sent_wrong + " "
				+ fixSP(t("sentence "), t("sentences"), seq!!.cnt_sent_wrong)
				+ " ("
				+ seq!!.cnt_word_wrong + " "
				+ fixSP(t("word"), t("words"), seq!!.cnt_word_wrong) + ")",
			currentPupil.imageName,
			currentPupil.imageNameWrongAnswer,
			null
		)
	    } catch (ex: Exception) {
		null
	    }
	}
    var om_msg_li: Listener = object : Listener {
	override fun msg(msg: String?) {
	    msg_log.getLogger().info("====M=== message $msg")
	    if (msg!!.startsWith("imarea main:")) {
		act_performLesson(msg)
		lemain_canvas!!.requestFocus()
		return
	    }
	    if (msg.startsWith("button main:quit")) {
		act_main_quit()
		return
	    }
	    if (msg.startsWith("button main:read_story")) {
		sendMsg("read_story", null, "")
	    }
	    if (msg.startsWith("button sent:")) {
		val submsg = msg.substring(12)
		sendMsg("sent_$submsg", null, "button")
	    }
	    if (msg.startsWith("button pupil:")) {
		val submsg = msg.substring(13)
		if (submsg == "admin") {
		    return
		}
		if (submsg == "settings") {
		    act_pupil_settings()
		    return
		}
		if (submsg == "result") {
		    act_pupil_result()
		    return
		}
		if (submsg == "pupil") {
		    act_pupil_pupil()
		    return
		}
		if (submsg == "create_p") {
		    act_pupil_create_p()
		}
		if (submsg == "create_t") {
		    act_pupil_create_t()
		}
		if (submsg == "test_t") {
		    act_pupil_test_t()
		}
		if (submsg == "test_p") {
		    act_pupil_test_p()
		}
		if (submsg == "quit") {
		    globalExit = true
		    if (ApplContext.top_frame is LessonRuntime) System.exit(0)
		    return
		}
	    }
	    if ("action" == msg) {
		sendMsg("action", null, "L758 ")
	    }
	    if ("show_result" == msg) {
		OmegaContext.sout_log.getLogger().info(":--: show_result $register")
		sendMsg("show_result_msg", null, "exit_create")
	    }
	    if ("exit create" == msg) {
		act_exit_create()
		return
	    }
	    if ("playList" == msg) {
		playFromDataList(play_data_list)
		play_data_list = PlayDataList()
		play_data_list_is_last = PlayDataList()
	    }
	    // 		OmegaContext.sout_log.getLogger().info(":--: " + ">>>>>>> msg DONE " + msg);
	}

	private fun act_pupil_test_p() {
	    prepareTest("pupil_1")
	    card_show("main")
	    lemain_canvas!!.setRedClear()
	    lemain_canvas!!.requestFocusOrd(0)
	}

	private fun act_pupil_test_t() {
	    val choise = arrayOf(
		    t("Pre test 1"),
		    t("Pre test 2"),
		    t("Post test 1"),
		    t("Post test 2")
	    )
	    global_skipF(true)
	    val a = JOptionPane.showOptionDialog(
		    ApplContext.top_frame,
		    t("What kind of test?"),
		    t("Omega - Test mode"),
		    JOptionPane.OK_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    null,
		    choise,
		    choise[0]
	    )
	    global_skipF(false)
	    if (a == JOptionPane.CLOSED_OPTION) {
		return
	    }
	    val choise2 = arrayOf(
		    "pre_1",
		    "pre_2",
		    "post_1",
		    "post_2"
	    )
	    prepareTest(choise2[a])
	    //			OmegaContext.sout_log.getLogger().info(":--: " + "choosen test " + a + ' ' + choise[a]);
	    card_show("main")
	    lemain_canvas!!.setRedClear()
	    lemain_canvas!!.requestFocusOrd(0)
	}

	private fun act_pupil_create_t() {
	    prepareTest("create")
	    card_show("main")
	    lemain_canvas!!.setRedClear()
	    lemain_canvas!!.requestFocusOrd(0)
	}

	private fun act_pupil_create_p() {
	    prepareTest("create")
	    card_show("main")
	    lemain_canvas!!.setRedClear()
	    lemain_canvas!!.requestFocusOrd(0)
	}

	private fun act_pupil_settings() {
	    global_skipF(true)
	    //			String fn = "default.omega_colors";
	    //			fn = getCurrentPupil().getString("theme", fn);
	    //			omega_settings_dialog.setSelectedColorFile(fn);
	    omega_settings_dialog.isVisible = true
	    global_skipF(false)
	}

	private fun act_main_quit() {
	    val ds = lemain_canvas!!.lessonBase
	    if (ds == null) {
		card_show("pupil")
	    } else {
		//log 			    OmegaContext.sout_log.getLogger().info(":--: " + "--- lesson base is now " + ds);
		val ix = ds.lastIndexOf('/')
		if (ix != -1) {
		    //log 				OmegaContext.sout_log.getLogger().info(":--: " + "--> " + ds.substring(0, ix));
		    val lb = ds.substring(0, ix)
		    val sel_ix = lemain_canvas!!.setLessonBase(lb)
		} else {
		    //log 				OmegaContext.sout_log.getLogger().info(":--: " + "--> " + null);
		    lemain_canvas!!.setLessonBase(null)
		}
		lemain_canvas!!.requestFocus()
	    }
	    val ord = lemain_canvas!!.setRedPop()
	}

	private fun act_pupil_pupil() {
	    val pn: String = currentPupil.name
	    //			OmegaContext.sout_log.getLogger().info(":--: " + "---------- pupil " + pn);
	    pupil_settings_dialog.setPupil(currentPupil)
	    global_skipF(true)
	    pupil_settings_dialog.isVisible = true
	    global_skipF(false)
	    if (pupil_settings_dialog.was_deleted) {
		pupil_settings_dialog.was_deleted = false
		pupil_canvas!!.mkList("Guest")
		setPupil("Guest")
		val pparm = pupil_settings_dialog.params
		currentPupil.setParams(pparm)
	    } else {
		val pparm = pupil_settings_dialog.params
		currentPupil.setParams(pparm)
		pupil_canvas!!.reloadPIM()
		restoreSettings()
	    }
	    if ("next" == currentPupil.getString("space_key", "select")) {
		setNextKey()
	    } else {
		setSelectKey()
	    }
	    val pupil_lang: String? = currentPupil.getStringNo0("languageSuffix", tryLessonLanguages(T.lang))
	    OmegaContext.lesson_log.getLogger()
		    .info("Retr pupil_lang: " + pupil_lang + ' ' + tryLessonLanguages(T.lang) + ' ' + T.lang)
	    OmegaContext.lessonLang = pupil_lang!!
	}

	private fun act_exit_create() {
	    OmegaContext.sout_log.getLogger().info(":--: sm1 $current_card $register")
	    // savePrefetch();
	    if (edit) {
		globalExit = true
		sendMsg("exitLesson", "", "")
		//System.exit(0);
	    }
	    if (register != null) {
		register!!.close()
	    }
	    register = null
	    card_show("main", 2)
	    lemain_canvas!!.setRedPop()
	    lemain_canvas!!.requestFocus()
	}

	@Throws(HeadlessException::class)
	private fun act_pupil_result() {
	    if (rdlg == null) {
		rdlg = ResultDialogTableSummary(ApplContext.top_frame!!)
	    }
	    try {
		currentPupil = Pupil(pupil_canvas!!.pupilName)
		register = RegisterProxy(currentPupil)
		//			    OmegaContext.sout_log.getLogger().info(":--: " + "got reg " + register);
		rdlg!!.set(register!!)
		global_skipF(true)
		OmegaContext.HELP_STACK.push("result_summary")
		rdlg!!.isVisible = true
		global_skipF(false)
	    } catch (ex: Exception) {
		ex.printStackTrace()
		global_skipF(true)
		JOptionPane.showMessageDialog(
			ApplContext.top_frame,
			t("Can't find pupil result data")
		)
		global_skipF(false)
	    } finally {
		OmegaContext.HELP_STACK.pop("result_summary")
	    }
	}
    }

    fun mact_New() {
	val file = File(omegaAssets("lesson-" + OmegaContext.lessonLang + "/new.omega_lesson")) // LESSON-DIR
	val url_s = toURL(file)
	var tfn = mkRelativeCWD(url_s!!)
	tfn = antiOmegaAssets(tfn)
	loadFN(tfn)
	saved_name = null
    }

    fun mact_Save() {
	if (saved_name != null) {
	    save(omegaAssets(saved_name))
	} else {
	    mact_SaveAs()
	}
    }

    fun mact_SaveAs() {
	var url_s: String? = null
	try {
	    val choose_f = ChooseLessonFile()
	    global_skipF(true)
	    val rv = choose_f.showDialog(null, t("Save"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		url_s = toURL(file)
		if (!url_s!!.endsWith("." + ChooseLessonFile.ext)) {
		    url_s = url_s + "." + ChooseLessonFile.ext
		}
		val tfn = mkRelativeCWD(url_s)
		save(tfn)
		saved_name = tfn
		if (window is JFrame) {
		    (window as JFrame).title = "Omega - Lesson Editor: " + antiOmegaAssets(tfn)
		}
	    }
	} finally {
	    global_skipF(false)
	}
    }

    fun mact_Open() {
	var url_s: String? = null
	try {
	    val choose_f = ChooseLessonFile(2)
	    global_skipF(true)
	    val rv = choose_f.showDialog(null, t("Open"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		choose_f.setLastFile(file)
		var s = "!"
		try {
		    s = file.canonicalPath
		} catch (e: IOException) {
		    e.printStackTrace()
		}
		OmegaContext.sout_log.getLogger().info("mact_Open file: $file $s")
		url_s = toURL(file)
		if (!url_s!!.endsWith("." + ChooseLessonFile.ext)) {
		    url_s = url_s + "." + ChooseLessonFile.ext
		}
		val atfn = mkRelativeCWD(url_s)
		val tfn = antiOmegaAssets(atfn)
		loadFN(tfn)
		saved_name = tfn
	    }
	} finally {
	    global_skipF(false)
	}
    }

    private fun loadFN(fn: String?) {
	restoreSettings()
	sendMsg("load", fn, "loadFN")
    }

    private fun save(fn: String?) {
	val el = element
	save(fn!!, el)
    }

    fun restoreSettings() {
	var fn = "default.omega_colors"
	fn = currentPupil.getString("theme", fn)!!
	val el = restore(fn) ?: return
	for (i in 0..99) {
	    val fel = el.findElement("canvas", i) ?: return
	    val name = fel.findAttr("name")
	    canvases.keySet().forEach {k ->
		val lbc = canvases[k as String]
		if (k == name) {
		    lbc!!.setSettingsFromElement(fel)
		}
	    }
	}
    }

    fun saveSettings(fn: String?) {
	val afn = omegaAssets(fn)
	OmegaContext.sout_log.getLogger().info(":--: save settings in file $afn")
	val el = settingsElement
	save(afn!!, el)
    }

    val element: Element
	get() {
	    val el = Element("omega_lesson")
	    el.addAttr("version", "0.0")
	    val c_el = le_canvas!!.element
	    el.add(c_el)
	    val lel = Element("lesson")
	    lel.addAttr("ord", "0")
	    val lesson_name = le_canvas!!.lessonName
	    lel.addAttr("name", lesson_name)
	    val tel = machine.target!!.targetElement
	    lel.add(tel)
	    val itel = machine.target!!.itemsElement
	    lel.add(itel)
	    el.add(lel)
	    if (action_specific != null) {
		var as_el = action_specific!!.element
		el.add(as_el)
		as_el = action_specific!!.signElement
		el.add(as_el)
	    }
	    val seq_el = seq!!.element
	    if (seq_el != null) {
		el.add(seq_el)
	    }
	    val st_el = elementStory
	    if (st_el != null) {
		el.add(st_el)
	    }
	    return el
	}
    val elementStory: Element?
	get() {
	    val el = Element("story")
	    el.addAttr("isfirst", if (le_canvas!!.lessonIsFirst) "yes" else "no")
	    val link_next = le_canvas!!.lessonLinkNext
	    if (link_next != null && link_next.length > 0) {
		val e = Element("link")
		e.addAttr("next", link_next)
		el.add(e)
	    } else {
		return null
	    }
	    return el
	}
    val settingsElement: Element
	get() {
	    val el = Element("omega_settings")
	    el.addAttr("version", "0.0")
	    canvases.keySet().forEach {k ->
		val lbc = canvases[k!!]
		val lbc_el = Element("canvas")
		lbc_el.addAttr("name", k!! as String)
		lbc!!.fillElement(lbc_el)
		el.add(lbc_el)
	    }
	    return el
	}

    fun copySettings(from: String?, to: String?) {
	val l1 = canvases[from]
	val l2 = canvases[to]
	l2!!.colors = l1!!.colors.clone() as HashMap<String, ColorColors>
    }

    @Synchronized
    fun addValues(vs: Values) {
	//	OmegaContext.sout_log.getLogger().info(":--: " + "addValues " + vs);
	if (action != null) {
	    var s = action!!.pathList
	    vs.setStr("pathlist", s)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "pathlist: " + s);
	    s = action!!.actorList
	    vs.setStr("actorlist", s)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "actorlist: " + s);
	}
    }

    @Synchronized
    fun addValues(vs: Values, action_: Action?) {
	//	OmegaContext.sout_log.getLogger().info(":--: " + "addValues " + vs);
	if (action_ != null) {
	    var s = action_.pathList
	    vs.setStr("pathlist", s)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "pathlist: " + s);
	    s = action_.actorList
	    vs.setStr("actorlist", s)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "actorlist: " + s);
	}
    }

    override fun hitTarget(ix: Int, type: Char) {
	if (lep != null && type == 'p') {
	    lep!!.setActiveTargetIx(ix)
	    val vs = lep!!.le_canvas.target!!.getTargetValues(ix)
	    OmegaContext.sout_log.getLogger().info(":--: target $vs")
	    addValues(vs)
	    lep!!.setTarget(vs)
	}
    }

    var item_at_xy: Item? = null
    override fun hitItem(ix: Int, iy: Int, where: Int, type: Char) {
	if (lep != null && type == 'p') {
	    var action_: Action? = null
	    if (action != null) {
		action_ = action
	    }
	    val d = where / 100.0
	    lep!!.setActiveItemIx(ix, iy)
	    item_at_xy = lep!!.le_canvas.target!!.getItemAt(ix, iy)
	    val vs = lep!!.le_canvas.target!!.getItemAt(ix, iy)!!.getValues(true)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "item " + vs);
	    addValues(vs, action_)
	    lep!!.setItem(vs)
	}
    }

    fun setPupil(pname: String) {
	OmegaContext.lesson_log.getLogger().info("pname: $pname")
	currentPupil = Pupil(pname)
	val prefs = Preferences.userNodeForPackage(this.javaClass)
	prefs.put("default_pupil", pname)
	try {
	    prefs.sync()
	} catch (ex: Exception) {
	}
	pupil_settings_dialog.setPupil(currentPupil)
	val pparm = pupil_settings_dialog.params
	currentPupil.setParams(pparm)
	if ("next" == currentPupil.getString("space_key", "select")) {
	    setNextKey()
	} else {
	    setSelectKey()
	}
	if (!edit) {
	    val pupil_lang = currentPupil.getStringNo0("languageSuffix", tryLessonLanguages(T.lang))
	    OmegaContext.lesson_log.getLogger().info("pupil_lang: $pupil_lang")
	    OmegaContext.lessonLang = pupil_lang!!
	}
	register = null // new RegisterProxy(getCurrentPupil(), null);
	restoreSettings()
	if (feedback_movie != null) {
	    feedback_movie!!.dispose()
	    feedback_movie = null
	}
	if (currentPupil.getBool("sign_movie_on", false)) {
	    OmegaContext.lesson_log.getLogger().warning("prepare movie feedback: sign_movie_on true")
	    try {
		if (feedback_movie == null) {
		    feedback_movie = FeedBackMovie(true)
		    val mn = currentPupil.getString("sign_movie", null)
		    feedback_movie!!.prepare(mn, null)
		}
	    } catch (ex: Exception) {
		OmegaContext.lesson_log.getLogger().warning("prepare sign movie feedback: $ex")
	    }
	} else if (currentPupil.getBool("movie_on", false)) {
	    OmegaContext.lesson_log.getLogger().warning("prepare movie feedback: movie_on true")
	    try {
		if (feedback_movie == null) {
		    feedback_movie = FeedBackMovie()
		    val mn = currentPupil.getString("movie", null)
		    feedback_movie!!.prepare(mn, null)
		}
	    } catch (ex: Exception) {
		OmegaContext.lesson_log.getLogger().warning("prepare movie feedback: $ex")
	    }
	}
    }

    var current_card: String? = null
    private fun card_show(name: String?, id: Int = 0) {
	OmegaContext.sout_log.getLogger()
		.info(":--: " + "**************** change " + current_card + " > " + id + ' ' + Date() + ' ' + name)
	if (name == "anim1") {
	}
	if (current_card != null) {
	    if (current_card == "pupil") {
		// 		getCurrentPupil() = new Pupil(pupil_canvas.getPupilName());
		// 		restoreSettings();
		// //		if ( register != null )
		// //		    register.close();
		// 		if ( pupil_settings_dialog == null )
		// 		    pupil_settings_dialog = new PupilSettingsDialog();
		// 		pupil_settings_dialog.setPupil(getCurrentPupil());
		// 		HashMap pparm = pupil_settings_dialog.getParams();
		// 		getCurrentPupil().setParams(pparm);
		// 		OmegaContext.sout_log.getLogger().info(":--: " + "Pupil param" + pparm);
		//		register = new RegisterProxy(getCurrentPupil());
	    }
	}
	if (base_canvas != null) {
	    base_canvas!!.leave()
	}
	if (name == "anim1") {
	    if (action != null) {
		action!!.clearScreen()
	    }
	}
	card!!.show(card_panel, name)
	OmegaContext.HELP_STACK.pop("")
	OmegaContext.HELP_STACK.push(name!!)
	m_sleep(200)
	if (name == "anim1") {
	    if (action != null) {
		action!!.canvas!!.requestFocus()
	    }
	    le_canvas!!.hideMsg()
	}
	var rF = false
	if (name == "main") {
	    lemain_canvas!!.requestFocusOrd(last_ord)
	    last_ord = -1
	} else {
	    rF = true
	}
	base_canvas = canvases[name]
	if (base_canvas != null) {
	    base_canvas!!.enter()
	    if (rF) {
		base_canvas!!.requestFocus()
	    }
	}
	current_card = name
    }

    var msg_list: MutableList<Message> = ArrayList()
    val msg_list_lock = ReentrantLock()
    val msg_list_condition = msg_list_lock.newCondition()

    var stop_msg = false
    fun sendMsg(msg: String, o: Any?) {
	sendMsg(msg, o, "")
    }

    fun sendMsgWait(msg: String, o: Any?) {
	wait_id[0] = "" + System.nanoTime()
	sendMsg(msg, o, wait_id[0])
	wait_id_lock.withLock {
	    try {
		wait_id_condition.await()
	    } catch (e: InterruptedException) {
		e.printStackTrace()
	    }
	}
    }

    private fun sendMsg(msg: String, o: Any?, id: String) {
	val m = Message(msg, o, java.lang.Long.valueOf(ct()), id)
	OmegaContext.sout_log.getLogger().info(":--: " + "!!!!!!!! sendMsg " + msg + ' ' + ct() + ' ' + o + ' ' + id)
	msg_list_lock.withLock {
	    msg_list.add(m)
	    //log 	    OmegaContext.sout_log.getLogger().info(":--: " + "%%%%% inserted sendMsg >>> " + msg + ' ' + o);
	    msg_list_condition.signal()
	}
    }

    private val msg: Message
	private get() {
	    msg_list_lock.withLock {
		while (msg_list.size == 0) {
		    try {
			msg_list_condition.await()
		    } catch (ec: InterruptedException) {
		    }
		}
		val len = msg_list.size
		return msg_list.removeAt(0)
	    }
	}

    var sa_lock = ReentrantLock()
    val sa_condition = sa_lock.newCondition()

    var say_all = false
    fun sayAll(tg: Target) {
	try {
	    if (OmegaConfig.tts) {
		val lang = OmegaContext.lessonLang
		if (say(lang, tg.allTTS, true)) return
	    }
	    say_all = true
	    val sa = tg.allSounds
	    val apA = arrayOfNulls<APlayer>(sa.size)
	    for (i in sa.indices) {
		val ss = sa[i]
		apA[i] = createAPlayer(currentPupil.getStringNo0("languageSuffix", null), ss, null, "SA_$i")
	    }
	    for (i in apA.indices) {
		le_canvas!!.setMarkTarget(i, true)
		apA[i]!!.playWait()
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: Exception! Lesson.sayAll(): $ex")
	    ex.printStackTrace()
	} finally {
	    sa_lock.withLock {
		say_all = false
		sa_condition.signalAll()
	    }
	}
	//	APlayer.unloadAll("SA_[0-9]*");
    }

    private fun waitSayAll() {
	sa_lock.withLock {
	    while (say_all) {
		try {
		    sa_condition.await()
		} catch (ex: InterruptedException) {
		}
	    }
	}
    }

    //      private void waitBoxPlay() {
    // // 	APlayer ap = box_ap;
    // // 	if ( ap != null )
    // // 	    ap.waitPlay();
    //      }
    var ap_svisch: APlayer? = null
    private fun saySw(snd: String, wait: Boolean) {
	try {
	    if (ap_svisch != null) {
		ap_svisch!!.close()
	    }
	    ap_svisch = createAPlayer(snd, null, "LE_")
	    if (wait) {
		ap_svisch!!.playWait()
		ap_svisch = null
	    } else {
		ap_svisch!!.play()
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: Exception! Lesson.sayAll(): $ex")
	}
    }

    var ap_s: APlayer? = null
    private fun sayS(snd: String) {
	try {
	    if (ap_s != null) {
		ap_s!!.close()
	    }
	    ap_s = createAPlayer(snd, null, "LE_")
	    ap_s!!.play()
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: Exception! Lesson.sayAll(): $ex")
	}
    }

    private fun sayPingSentence() {
	if (currentPupil.getBool("pingSentence", true)) {
	    saySw("svisch.wav", true)
	}
    }

    private fun sayPingAnim() {
	if (currentPupil.getBool("pingAnim", false)) {
	    saySw("svisch2.wav", false)
	}
    }

    inner class ActionSpecific internal constructor() {
	var hm: HashMap<String?, String?>
	var hmSign: HashMap<String?, String?>

	init {
	    hm = HashMap()
	    hmSign = HashMap()
	}

	val media: List<String>
	    get() {
		val li: MutableList<String> = ArrayList()
		for (ov in hm.values) li.add(ov.toString())
		for (ov in hmSign.values) li.add(ov.toString())
		return li
	    }

	fun `is`(s: String): Boolean {
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "try specific action " + s);
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + element_root);
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + hm);
	    val `as` = hm[s]
	    lesson_log.getLogger().info("action_specific.is:$s<$`as`>")
	    return `as` != null
	}

	fun isSign(s: String): Boolean {
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "try specific action " + s);
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + element_root);
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + hm);
	    val `as` = hmSign[s]
	    lesson_log.getLogger().info("action_specific.isSign:$s<$`as`>")
	    return `as` != null
	}

	fun getAction(s: String): String? {
	    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "try specific action " + s);
	    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + element_root);
	    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + hm);
	    val `as` = hm[s]
	    lesson_log.getLogger().info("action_specific.getAction:$s<$`as`>")
	    return `as`
	}

	fun getSign(s: String): String? {
	    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "try specific action " + s);
	    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + element_root);
	    //  	    OmegaContext.sout_log.getLogger().info(":--: " + "                     " + hm);
	    val `as` = hmSign[s]
	    lesson_log.getLogger().info("action_specific.getSign:$s<$`as`>")
	    return `as`
	}

	fun setAction(s: String?, `val`: String?) {
	    hm[s] = `val`
	}

	fun setSign(s: String?, `val`: String?) {
	    hmSign[s] = `val`
	}

	val element: Element
	    get() {
		val el = Element("action_specific")
		hm.keys.forEach {key ->
		    val `val` = hm[key]
		    if (!empty(`val`)) {
			val el1 = Element("value")
			el1.addAttr("key", key)
			el1.addAttr("val", `val`)
			el.add(el1)
		    }
		}
		return el
	    }
	val signElement: Element
	    get() {
		val el = Element("sign_specific")
		hmSign.keys.forEach {key ->
		    val `val` = hmSign[key]
		    if (!empty(`val`)) {
			val el1 = Element("value")
			el1.addAttr("key", key)
			el1.addAttr("val", `val`)
			el.add(el1)
		    }
		}
		return el
	    }

	fun fill(el: Element) {
	    val as_el = el.findElement("action_specific", 0)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "FOUND EL action specific " + as_el);
	    if (as_el != null) {
		for (i in 0..999) {
		    val el1 = as_el.findElement("value", i) ?: break
		    val key = el1.findAttr("key") as String
		    val `val` = el1.findAttr("val") as String
		    if (!empty(`val`)) {
			hm[key] = `val`
			lesson_log.getLogger().info("action_specific.fill:$key<$`val`>")
		    } else {
			lesson_log.getLogger().info("action_specific.fill: empty val, ignored $key")
		    }
		}
	    }
	}

	fun fillSign(el: Element) {
	    val as_el = el.findElement("sign_specific", 0)
	    //	    OmegaContext.sout_log.getLogger().info(":--: " + "FOUND EL action specific " + as_el);
	    if (as_el != null) {
		for (i in 0..999) {
		    val el1 = as_el.findElement("value", i) ?: break
		    val key = el1.findAttr("key") as String
		    val `val` = el1.findAttr("val") as String
		    if (!empty(`val`)) {
			hmSign[key] = `val`
			lesson_log.getLogger().info("sign_specific.fill:$key<$`val`>")
		    } else {
			lesson_log.getLogger().info("sign_specific.fill: empty val, ignored, $key")
		    }
		}
	    }
	}
    }

    var action_specific: ActionSpecific? = ActionSpecific()

    /**
     * Return true if more than 3 columns
     */
    private fun heavyLesson(el: Element): Boolean {
	val iel = el.findElement("items", 0)
	if (iel != null) {
	    val ieel = iel.findElement("item-entry", 3)
	    if (ieel != null) {
		return true
	    }
	}
	return false
    }

    var wait_id = arrayOf("")
    val wait_id_lock = ReentrantLock()
    val wait_id_condition = wait_id_lock.newCondition()

    private var last_msg_time = ct()
    fun execLesson(fn: String?) {
	val tg = Target(machine)
	machine.target = tg
	// APlayer.unloadAll("Box[0-9]*");
	if (action != null) {
	    action!!.clean()
	}
	action = null
	//	window.setVisible(true);
	le_canvas!!.populateGUI()
	waitAndCloseSplash()
	if (fn != null) {
	    card_show("words")
	    sendMsg("load", fn, "execLesson")
	}
	var test_index: Set<Array<IntArray>>? = null
	var last_id = "!"
	while (true) {
	    wait_id_lock.withLock { if (last_id == wait_id[0]) wait_id_condition.signal() }
	    OmegaContext.serr_log.getLogger().info(" -----------> done $last_id")
	    val m = msg
	    val msg = m.msg
	    val obj = m.obj
	    val msg_time = m.msg_time
	    val id = m.id
	    val delta = msg_time - last_msg_time
	    last_msg_time = msg_time
	    msg_log.getLogger().info("%%%%%%%%%%%%%%%%%%% msg $msg $obj $delta $id")
	    if ("load" == msg) {
		exec_load(obj as String?, tg)
		progress!!.dismiss()
		if (current_test_mode_group == TMG.CREATE) {
		    if (!last_story_flag) {
			card_show("words")
		    }
		    if (register != null) {
			register!!.setStarted()
		    }
		}
	    } else if ("create" == msg) {
		le_canvas!!.hideMsg()
		le_canvas!!.setMarkTargetNo()
		sendMsg("load", obj as String?, "create")
	    } else if ("new_test" == msg) {
		//seq.initNewTest();
		le_canvas!!.hideMsg()
		//		OmegaContext.sout_log.getLogger().info(":--: " + "here load_test");
		sendMsg("load", obj as String?, "new_test")
		// 		if ( current_test_mode_group == TMG_CREATE )
		// 		    card_show("words");
		sendMsg("test_cont", null, "new_test2")
	    } else if ("test_cont" == msg) {
		test_index = exec_test_cont()
	    } else if ("show_result_msg" == msg) {
		OmegaContext.sout_log.getLogger().info(":--: " + "show_result_msg " + ' ' + register)
		if (register != null && register!!.has_shown == false && current_test_mode == TM.RAND) {
		    le_canvas!!.showMsg(resultSummary_MsgItem)
		    register!!.has_shown = true
		    register!!.close()
		}
		le_canvas!!.fireRealExit()
	    } else if ("hBoxM" == msg) {
		val hBox = obj as LessonCanvas.Box?
		exec_hbox(hBox, tg, test_index)
	    } else if ("hBoxK" == msg) {
		val hBox = obj as LessonCanvas.Box?
		exec_hbox(hBox, tg, test_index)
		le_canvas!!.gotoNextBox()
	    } else if ("action" == msg) {
		sendMsg("play", null, "action")
		//log 		OmegaContext.sout_log.getLogger().info(":--: " + "action:play done");
		if (tg.storyNext != null) {
		    //		    show_progress = false;
		    OmegaContext.sout_log.getLogger().info(":--: " + "STORY NEXT  " + tg.storyNext)
		    sendMsg("create", tg.storyNext, "action2")
		    card_show("words", 5)
		} else {
		    //		    show_progress = true;
		    if (!last_story_flag) {
			sendMsg("load", loadedFName, "action3")
		    }
		}
	    } else if ("listen" == msg) {
		if (tg.isTargetFilled) {
		    sayAll(tg)
		    le_canvas!!.setMarkTargetNo()
		}
		card_show("words")
	    } else if (msg != null && msg.startsWith("playSign:")) {
		if (tg.isTargetFilled) {
		    playSignFile(tg, true)
		    le_canvas!!.setMarkTargetNo()
		    card_show("words")
		}
	    } else if ("play" == msg) {
		exec_play(tg, true)
		//		OmegaContext.sout_log.getLogger().info(":--: " + "exec_play done");
		if (register != null) {
		    register!!.setStarted()
		}
	    } else if ("playAll" == msg) {
		exec_play(tg, false)
		card_show("words")
	    } else if ("play&return" == msg) {
		exec_play(tg, true)
		card_show("words")
	    } else if ("read_story" == msg) {
		sentence_canvas!!.showMsg(null)
		sentence_canvas!!.setRead(true)
		card_show("sent")
		sendMsg("sent_select", "")
	    } else if ("sent_quit" == msg) {
		sentence_canvas!!.hidePopup(3)
		play_data_list = PlayDataList()
		play_data_list_is_last = PlayDataList()
		sentence_canvas!!.showMsg(null)
		//		sentence_canvas.setRead(false);
		card_show("main")
	    } else if ("sent_read" == msg) {
		sentence_canvas!!.hidePopup(3)
		val sent_li = story_hm["sentence_list"]
		val ss_li = sent_li!!.sentence_list
		OmegaContext.story_log.getLogger().info("sent_read story_hm 1599 " + story_hm)
		OmegaContext.story_log.getLogger().info("sent_read sent_li 1599 $sent_li")
		OmegaContext.story_log.getLogger().info("sent_read ss_li 1599 $ss_li")
		OmegaContext.story_log.getLogger().info("sent_read playdatalist 1599 $play_data_list")
		sentence_canvas!!.ignorePress(true)
		sentence_canvas!!.showMsg(null)
		sentence_canvas!!.showMsg(ss_li)
		sentence_canvas!!.setStoryData(play_data_list) // strange
		card_show("sent")
		listenFromDataList(play_data_list_is_last) //, sentence_canvas.getListenListener());
		sentence_canvas!!.ignorePress(false)
	    } else if ("sent_replay" == msg) {
		sentence_canvas!!.hidePopup(3)
		if (action == null) {
		    action = AnimAction()
		    card_panel!!.add(action!!.canvas, "anim1")
		}
		card_show("anim1")
		hit_key = 0
		playFromDataList(play_data_list)
		card_show("sent")
	    } else if ("sent_print" == msg) {
		// not anymore                sentence_canvas.togglePopup(3);
		sendMsg("sent_print_print", "")
	    } else if ("sent_print_select" == msg) {
		//not anymore
	    } else if ("sent_print_print" == msg) {
		sentence_canvas!!.setBusy(true)
		val sent_li = story_hm["sentence_list"]
		val ss_li = sent_li!!.sentence_list
		OmegaContext.story_log.getLogger().info("sent_print story_hm 1599 " + story_hm)
		OmegaContext.story_log.getLogger().info("sent_print sent_li 1599 $sent_li")
		OmegaContext.story_log.getLogger().info("sent_print ss_li 1599 $ss_li")
		printFromDataList(play_data_list_is_last)
		sentence_canvas!!.setBusy(false)
		sentence_canvas!!.hidePopup(3)
	    } else if ("sent_save" == msg) {
		sentence_canvas!!.hidePopup(3)
		val sent_li = story_hm["sentence_list"]
		val ss_li = sent_li!!.sentence_list
		val lname = sent_li.lesson_name
		val df: DateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
		val d = play_data_list.date
		val date = df.format(d)
		val ord = play_data_list.nextOrd()
		if (register == null) { // null after windows resize
		    register = RegisterProxy(currentPupil)
		}
		val dir = register!!.rl.getDirPath(currentPupil.name)
		val fname = (currentPupil.name + '-'
			+ lname + '-'
			+ date + '-'
			+ ord)
		val fullname = "$dir$fname.omega_story_text"
		val fullname_2 = "$dir$fname.omega_story_replay"
		try {
		    PrintWriter(FileWriter(File(fullname))).use { pw ->
			ss_li!!.forEach {sent -> pw.println(sent) }
		    }
		    ObjectOutputStream(FileOutputStream(fullname_2)).use { oo ->
			oo.writeObject(sent_li)
			oo.writeObject(play_data_list.arr)
			oo.writeObject(play_data_list_is_last.arr)
		    }
		    global_skipF(true)
		    JOptionPane.showMessageDialog(ApplContext.top_frame, t("Saved in file") + ' ' + fullname)
		    global_skipF(false)
		} catch (ex: Exception) {
		    ex.printStackTrace()
		    global_skipF(true)
		    JOptionPane.showMessageDialog(
			    ApplContext.top_frame,
			    t("File") + ' ' + fullname + ' ' + t("not saved") + '.'
		    )
		    global_skipF(false)
		    OmegaContext.sout_log.getLogger().info(":--: $ex")
		}
	    } else if ("sent_select" == msg) {
		sentence_canvas!!.hidePopup(3)
		sentence_canvas!!.setBusy(true)
		try {
		    //		    global_skipF(true);
		    try {
			if (register == null) {
			    register = RegisterProxy(currentPupil)
			}
			val dir = register!!.rl.getDirPath(currentPupil.name)
			lesson_log.getLogger().info("get it from $dir")
			val dir_file = File(dir)
			val files = dir_file.list { dir, name ->
			    name.endsWith(".omega_story_replay")
			}
			if (files.size > 0) {
			    sentence_canvas!!.showMsg(null)
			    sentence_canvas!!.enableStoryList(true)
			    sentence_canvas!!.setListData(files)
			    val filename = sentence_canvas!!.waitDone()
			    val file = File("$dir/$filename")
			    lesson_log.getLogger().info("story reply file is $file")
			    ObjectInputStream(FileInputStream(file)).use { `in` ->
				val sent_li = `in`.readObject() as SentenceList
				story_hm["sentence_list"] = sent_li
				play_data_list.arr = `in`.readObject() as ArrayList<PlayData>
				play_data_list_is_last.arr = `in`.readObject() as ArrayList<PlayData>
			    }
			} else {
			    global_skipF(true)
			    JOptionPane.showMessageDialog(
				    ApplContext.top_frame,
				    t("Can't find any saved story")
			    )
			    global_skipF(false)
			}
		    } catch (ex: Exception) {
			OmegaContext.sout_log.getLogger().info("ERR: $ex")
		    }
		} finally {
		    global_skipF(false)
		    sentence_canvas!!.enableStoryList(false)
		    sentence_canvas!!.setBusy(false)
		}
	    } else if ("test_dialog" == msg) {
		testDialog()
	    } else if ("exitLesson" == msg) {
		return
	    }
	    last_id = id
	}
    }

    /**
     * Save all used file for current lesson
     */
    //     void savePrefetch() {
    // 	if ( last_lesson_fn != null ) {
    // 	    String used_file[];
    // 	    if ( OmegaContext.CACHE_FEW )
    // 		used_file = APlayer.getAllUsedFile("(Box|SA_)[0-9]*");
    // 	    else
    // 		used_file = APlayer.getAllUsedFile("(TL_|Box|SA_)[0-9]*");
    // 	    audio_prefetch.saveit(last_lesson_fn, used_file);
    // 	}
    //     }
    var show_progress = true
    fun startProgress() {
	if (progress == null) {
	    progress = Progress()
	} else if (show_progress) {
	    progress!!.show()
	}
    }

    private fun initNewLesson(fn: String?) {
	//	Locator.setLang(getCurrentPupil().getStringNo0("languageSuffix", null));
	startProgress()
	le_canvas!!.resetNav()
    }

    private var last_lesson_fn: String? = null
    private fun exec_load(fn: String?, tg: Target) { // load a lesson
	try {
	    initNewLesson(fn)
	    try {
		le_canvas!!.target!!.releaseAllT_Items()
	    } catch (ex: Exception) {
	    }
	    le_canvas!!.removeDummy()
	    m_sleep(100)
	    //log 	OmegaContext.sout_log.getLogger().info(":--: " + "Loading Restoring " + fn + ' ' + last_lesson_fn);
	    val el: Element?
	    val lang = currentPupil.getStringNo0("languageSuffix", null)
	    if (!edit && lang != null) {
		val fn_lang = fn!!.replace("lesson-[a-zA-Z]*/active".toRegex(), "lesson-$lang/active") // LESSON-DIR-A
		val fn_lang_demo = fn.replace("lesson-[a-zA-Z]*/active".toRegex(), "lesson-$lang/demo") // LESSON-DIR-A
		val fn_lang_demo2 = fn.replace("lesson-[a-zA-Z]*/active".toRegex(), "lesson/demo") // LESSON-DIR-A
		OmegaContext.sout_log.getLogger().info(":--: LANG repl (~A)$fn $fn_lang")
		var el1 = restore(fn_lang)
		if (el1 == null) {
		    el1 = restore(fn)
		}
		if (el1 == null) {
		    el1 = restore(fn_lang_demo)
		}
		if (el1 == null) {
		    el1 = restore(fn_lang_demo2)
		}
		el = el1
	    } else {
		el = restore(fn!!)
	    }
	    if (fn != last_lesson_fn) {
// 		if ( last_lesson_fn != null ) {
// 		    String used_file[];
// 		    if ( OmegaContext.CACHE_FEW )
// 			used_file = APlayer.getAllUsedFile("(Box|SA_)[0-9]*");
// 		    else
// 			used_file = APlayer.getAllUsedFile("(TL_|Box|SA_)[0-9]*");
// 		    audio_prefetch.saveit(last_lesson_fn, used_file);
// 		}
// 		APlayer.unloadAll("TL_[0-9]*");
// 		APlayer.unloadAll("Box[0-9]*");
// 		APlayer.unloadAll("SA_[0-9]*");
		last_lesson_fn = fn
	    }
	    //	    audio_prefetch.prefetchAny(fn);
	    var dummy = false
	    if (current_test_mode == TM.POST_1
		    || current_test_mode == TM.POST_2
	    ) {
		dummy = true
	    }
	    if (el != null) {
		action_specific = ActionSpecific()
		lesson_log.getLogger().info("action_specific.new:$fn")
		action_specific!!.fill(el)
		action_specific!!.fillSign(el)
		element_root2 = el
		if (current_test_mode == TM.CREATE) {
		    tg.loadFromEl(el, "", story_hm, false, false)
		    seq = Sequencer(el)
		} else if (current_test_mode == TM.RAND) {
		    tg.loadFromEl(el, "", story_hm, false, true)
		    seq = Sequencer(el)
		    seq!!.dump()
		} else { // TM_{PRE,POST}
		    seq = Sequencer(el)
		    val full_test_txt = seq!!.getTestText(current_test_mode, true)
		    val test_txt = seq!!.getTestText(current_test_mode, false)
		    OmegaContext.sout_log.getLogger().info(":--: EL [][][] FOUND $full_test_txt $test_txt")
		    if (test_txt == null) {
			global_skipF(true)
			JOptionPane.showMessageDialog(
				ApplContext.top_frame,
				t("Can't find any test text")
			)
			global_skipF(false)
			card_show("main", 0)
			if (register != null) {
			    register!!.close()
			}
			register = null
		    } else {
			if (heavyLesson(el)) {
			    tg.loadCompositeFromEl(el, test_txt, story_hm, dummy, true)
			} else {
			    le_canvas!!.initNewLesson()
			    tg.loadFromEl(el, "", story_hm, dummy, true)
			}
		    }
		}

		//log 	    OmegaContext.sout_log.getLogger().info(":--: " + "=-= new tm " + current_test_mode);
		le_canvas!!.setFrom(el, dummy)
		if (le_canvas!!.lessonIsFirst) {
		    play_data_list = PlayDataList()
		    play_data_list_is_last = PlayDataList()
		    story_hm.clear()
		    story_hm["sentence_list"] = SentenceList()
		    OmegaContext.story_log.getLogger().info("lesson Is First $fn")
		}
		le_canvas!!.render(true, true)
		loadedFName = fn
		if (window is JFrame) {
		    (window as JFrame).title = "Omega - Lesson Editor: " + antiOmegaAssets(fn)
		}
	    } else {
		global_skipF(true)
		JOptionPane.showMessageDialog(
			ApplContext.top_frame, t("Can't load lessonfile ")
			+ fn
		)
		global_skipF(false)
	    }
	    le_canvas!!.initAction()
	    progress!!.dismiss()
	} catch (ex: Exception) {
	    ex.printStackTrace()
	    global_skipF(true)
	    if (fn != "new.omega_lesson") {
		JOptionPane.showMessageDialog(
			ApplContext.top_frame,
			t("Error while loading lesson: \"")
				+ fn + "\""
		)
	    }
	    global_skipF(false)
	}
    }

    var feedback_movie: FeedBackMovie? = null
    fun exec_test_cont(): Set<Array<IntArray>>? {
	if (!edit && current_test_mode > TM.CREATE) {
	    val full_test_txt = seq!!.getTestText(current_test_mode, true)
	    OmegaContext.sout_log.getLogger().info(":--: got this full_test_text: $full_test_txt")
	    le_canvas!!.removeDummy()
	    if (current_test_mode == TM.POST_1 || current_test_mode == TM.POST_2) {
		le_canvas!!.sowDummy(full_test_txt)
	    }
	    if (full_test_txt == null) {
		global_skipF(true)
		JOptionPane.showMessageDialog(
			ApplContext.top_frame,
			t("Test text empty (null)")
		)
		global_skipF(false)
		card_show("main", 4)
	    }
	    val test_txt = full_test_txt!!.replace("\\{[^\\{\\}]*\\}".toRegex(), "")
	    val allCorrect = getMatchingSameActionSpecific(action_specific, test_txt)
	    OmegaContext.sout_log.getLogger().info(":--: got this test_text: $test_txt -> $allCorrect")
	    try {
		le_canvas!!.target!!.reloadComposite(test_txt)
	    } catch (ex: Exception) {
		ex.printStackTrace()
		global_skipF(true)
		JOptionPane.showMessageDialog(
			ApplContext.top_frame,
			t("Can't find test test ") + test_txt
		)
		global_skipF(false)
		card_show("main", 4)
	    }
	    le_canvas!!.render()
	    val test_index: MutableSet<Array<IntArray>> = HashSet()
	    OmegaContext.sout_log.getLogger().info(":--: ET[][][] FOUND $full_test_txt $test_txt")
	    if (test_txt != null) {
		if (allCorrect.size == 0) {
		    val test_index1 = le_canvas!!.target!!.getAllTargetCombinationsIndexes(test_txt)
		    test_index.add(test_index1)
		} else {
		    for (s in allCorrect) {
			val test_index1 = le_canvas!!.target!!.getAllTargetCombinationsIndexes(
				s
			)
			test_index.add(test_index1)
		    }
		}
		OmegaContext.sout_log.getLogger().info(":--: " + "ET  [][] " + test_index.size)
		if (test_index.size == 0) {
		    global_skipF(true)
		    JOptionPane.showMessageDialog(
			    ApplContext.top_frame,
			    t("Can't find sentence ") + test_txt
		    )
		    global_skipF(false)
		    card_show("main", 3)
		} else {
		    le_canvas!!.setMarkTarget(0)
		    sendMsg("play", null, "exec_test_cont")
		}
	    } else {
		global_skipF(true)
		JOptionPane.showMessageDialog(
			ApplContext.top_frame,
			t("Can't run test ") + current_test_mode
		)
		global_skipF(false)
		card_show("main", 4)
	    }
	    return test_index
	}
	return null
    }

    private fun fixSP(s: String, p: String, a: Int): String {
	return if (a == 1) {
	    s
	} else p
    }

    private fun exec_hbox(hBox: LessonCanvas.Box?, tg: Target?, test_index: Set<Array<IntArray>>?) {
	try {
	    inExecHbox = true
	    if (hBox != null) {
		val exec_hbox_time = hBox.when_hit // SundryUtils.ct();
		try {
		    if (tg != null) {
			val i_x = hBox.o_x
			val i_y = hBox.o_y
			val tg_ix: Int
			val where = hBox.where
			tg_ix = if (current_test_mode_group == TMG.CREATE) {
			    tg.findNextFreeT_ItemIx(hBox.item!!, edit, where)
			} else {
			    tg.findNextFreeT_ItemIx()
			}
			if (tg_ix == -1) {
			    if (register != null) {
				register!!.word(
					":again",
					exec_hbox_time,
					hBox.item!!.text,
					hBox.item!!.it_ent!!.tid
				)
			    }
			    le_canvas!!.renderTg()
			} else {
			    var was_wrong = true
			    if (current_test_mode_group != TMG.CREATE) {
				val next_i_x = tg.findEntryIxMatchTargetIx(tg_ix)
				for (test_index1 in test_index!!) {
				    OmegaContext.sout_log.getLogger()
					    .info(":--: " + "!!! using test index: " + a2s(test_index) + ' ' + tg_ix)
				    val next_i_y = test_index1[tg_ix][0]
				    val next_i_x_ = test_index1[tg_ix][1]
				    val next_i_y_ = test_index1[tg_ix][2]
				    // next 0 8    i_ 1 0
				    OmegaContext.sout_log.getLogger()
					    .info(":--: HERE:  $i_x $i_y    next $next_i_x $next_i_y   next_  $next_i_x_ $next_i_y_")
				    if (next_i_x_ == i_x && next_i_y_ == i_y) {
					was_wrong = false
				    } else {
//					was_wrong = true;
				    }
				}
			    }
			    val itm = tg.pickItemAt(
				    i_x,
				    i_y,
				    tg_ix,
				    current_test_mode_group != TMG.CREATE
			    )
			    if (itm == null) {
				global_skipF(true)
				JOptionPane.showMessageDialog(
					ApplContext.top_frame,
					"""Internal error when picking
item pos (xy)$i_x $i_y
target pos $tg_ix"""
				)
				global_skipF(false)
				OmegaContext.sout_log.getLogger().info(
					""":--: Internal error when picking
item pos (xy)$i_x $i_y
target pos $tg_ix"""
				)
			    } else {
				val t_word_ = tg.getTextAt(tg_ix)
				val t_word = fixWithStar(t_word_)
				if (current_test_mode == TM.CREATE) {
				    if (register != null) {
					register!!.word(
						"create:build",
						exec_hbox_time,
						t_word,
						tg.getTidAt(tg_ix)
					)
				    }
				} else {
				    if (register != null) {
					if (was_wrong) {
					    register!!.word(
						    "test:build:wrong",
						    exec_hbox_time,
						    t_word,
						    hBox.item!!.entryTid
					    )
					    seq!!.cnt_word_wrong++
					    seq!!.cnt_wordlast_wrong++
					} else {
					    register!!.word(
						    "test:build:OK",
						    exec_hbox_time,
						    t_word,
						    hBox.item!!.entryTid
					    ) // tg.getTidAt(tg_ix));
					    seq!!.cnt_word_correct++
					    seq!!.cnt_wordlast_correct++
					}
					for (test_index1 in test_index!!) {
					    if (test_index1[tg_ix][1] == i_x && test_index1[tg_ix][2] == i_y) { // choosen match ok word
					    } else {                               // choosen do not match, -> invalidate it all
						for (tix1 in test_index1) {
						    tix1[1] = -1
						    tix1[2] = -1
						}
					    }
					}
				    }
				}
				le_canvas!!.renderTg()
				val ct0 = ct()
				System.gc()
				System.gc()
				val showSoundWord = currentPupil.getBool("showSoundWord", true)
				if (showSoundWord) {
				    PLAYSND@ while (true) {
					val sitm = tg.getItemAt(i_x, i_y)
					if (OmegaConfig.tts) {
					    val lang = OmegaContext.lessonLang
					    if (say(lang, sitm!!.defaultFilledTTS, true)) break@PLAYSND
					}
					var sfn = sitm!!.soundD ?: break@PLAYSND
					sfn = tg.fillVarHere(tg_ix, sfn)!!
					if (sfn == null || sfn.length == 0) {
					    break@PLAYSND
					}
					val sndA = split(sfn, ",")
					val aplayerA = arrayOfNulls<APlayer>(sndA.size)
					for (i in sndA.indices) {
					    val s = sndA[i]
					    aplayerA[i] = createAPlayer(
						    currentPupil.getStringNo0("languageSuffix", null),
						    s,
						    null,
						    "Box$i"
					    )
					}
					//				    waitBoxPlay();
					for (i in sndA.indices) {
					    aplayerA[i]!!.playWait()
					}
					break
				    }
				}
				val showSignWord = currentPupil.getBool("showSignWord", true)
				if (current_test_mode == TM.CREATE && isLIU_Mode() && showSignWord && !edit) {
				    val lmm = signMoviePrepare(tg, tg_ix)
				    if (lmm != null) {
					try {
					    val bgCol = omega_settings_dialog.signWord_background!!.color
					    val alphaCol = omega_settings_dialog.signWord_alpha!!.value
					    var sms = omega_settings_dialog.signMovieWord_scale!!.value
					    if (sms == 0) {
						sms = 1
					    }
					    val tgr = le_canvas!!.getTargetRectangle(tg_ix)
					    startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 1)
					} finally {
					    le_canvas!!.setMist(0, null, null, 0)
					    mistNoMouse = false
					    lmm.cleanup()
					}
				    }
				}
				if (itm.isAction) {
				    try {
					val action_s = tg.getActionFileName(0)
					OmegaContext.sout_log.getLogger().info(":--: Action fn $action_s")
					if (action_s != null && action_s.length > 0) {
					    if (false && action == null) { //--
						action = AnimAction()
						card_panel!!.add(action!!.canvas, "anim1")
					    }
					    if (!false) {
//--when pressed button						element_root = action.prefetch(action_s);
						if (element_root == null) ; else {
						    // patch or else we need to double press item to fill editors panel with actorlist
						    if (lep != null && item_at_xy != null) {
							val vs = item_at_xy!!.getValues(true)
							OmegaContext.sout_log.getLogger().info(":--: item $vs")
							addValues(vs)
							lep!!.setItem(vs)
						    }
						}
					    }
					}
				    } catch (ex: Exception) {
					OmegaContext.sout_log.getLogger().info("ERR: @@@@@h $ex")
					ex.printStackTrace()
				    }
				}
				val ct1 = ct()

				//				    OmegaContext.sout_log.getLogger().info(":--: " + "time prefetch " + (ct1-ct0));
				if (tg.isTargetFilled && !edit) {
				    if (current_test_mode == TM.CREATE) {
					val l_id_list = tg.all_Tid_Item
					if (register != null) {
					    register!!.create(
						    ":correct",
						    exec_hbox_time,
						    tg.allText,
						    1,
						    l_id_list
					    )
					}
					val do_repeat_whole = tg.get_howManyT_Items() > 1
					le_canvas!!.enableQuitButton(false)
					le_canvas!!.resetHboxFocus()
					if (do_repeat_whole) {
					    waitSayAll()
					    //					    waitBoxPlay();
					    m_sleep(currentPupil.getSpeed(500))
					    le_canvas!!.setMarkTargetAll()
					    sayPingSentence()
					    le_canvas!!.eraseHilitedBox()
					    m_sleep(currentPupil.getSpeed(300))
					    sayAll(tg)
					    waitSayAll()
					    m_sleep(currentPupil.getSpeed(400))
					} else {
					    waitSayAll()
					    //					    waitBoxPlay();
					    m_sleep(currentPupil.getSpeed(500))
					}
					le_canvas!!.setMarkTargetAll()
					val showSignSentence = currentPupil.getBool("showSignSentence", true)
					if (isLIU_Mode() && showSignSentence && !edit && current_test_mode == TM.CREATE) {
					    playSignFile(tg, false)
					}
					le_canvas!!.setMarkTargetNo()
					le_canvas!!.eraseHilitedBox()
					sayPingAnim()
					m_sleep(300)
					le_canvas!!.startAction()
				    } else {  // current_test_mode == TM_PRE/POST/RAND
					le_canvas!!.enableQuitButton(false)
					le_canvas!!.resetHboxFocus()
					waitSayAll()
					//					waitBoxPlay();
					m_sleep(currentPupil.getSpeed(500))
					le_canvas!!.setMarkTargetAll()
					sayPingSentence()
					m_sleep(currentPupil.getSpeed(300))
					sayAll(tg)
					waitSayAll()
					m_sleep(currentPupil.getSpeed(500))
					le_canvas!!.setMarkTargetAll()
					val all_text = tg.allText
					val correct_text = seq!!.getTestText(current_test_mode, false)
					var correctBool = false
					val allCorrect = getMatchingSameActionSpecific(action_specific, correct_text)
					for (correct in allCorrect) {
					    if (correct.equals(all_text, ignoreCase = true)) {
						correctBool = true
					    }
					}
					OmegaContext.sout_log.getLogger().info(
						"TestEval: " + "" + correct_text + " <- " + allCorrect + " -> " + correctBool + " | " + all_text.equals(
							correct_text,
							ignoreCase = true
						)
					)
					if (correctBool || all_text.equals(correct_text, ignoreCase = true)) {
					    seq!!.cnt_sent_correct++
					    if (register != null) {
						register!!.test(
							":correct",
							exec_hbox_time,
							seq!!.getTestText(current_test_mode, false),
							tg.allText,
							seq!!.getStat(true),
							tg.all_Tid_Item
						)
					    }
					    var do_prepare = false
					    if (currentPupil.getBool(
							    "movie_on",
							    false
						    ) || currentPupil.getBool("sign_movie_on", false)
					    ) {
						try {
						    var do_it = false
						    val `val` = currentPupil.getInt("frequence", 0)
						    when (`val`) {
							0 -> do_it = false
							1 -> do_it = rand(10) < 2
							2 -> do_it = rand(10) < 7
							3 -> do_it = true
						    }
						    //						OmegaContext.sout_log.getLogger().info(":--: " + "+++++ do_it " + do_it);
						    if (do_it && feedback_movie == null) {
							feedback_movie =
								FeedBackMovie(currentPupil.getBool("sign_movie_on", false))
							val mn = currentPupil.getString(
								if (currentPupil.getBool(
										"sign_movie_on",
										false
									)
								) "sign_movie" else "movie", null
							)
							feedback_movie!!.prepare(mn, null)
						    }
						    if (feedback_movie != null && do_it) {
							try {
							    val m_pan = feedback_movie!!.canvas
							    m_pan!!.layout = null
							    m_pan.background =
								    omega_settings_dialog.feedback_movie_background!!.color
							    val v_w = feedback_movie!!.mp!!.vw //getW();
							    val v_h = feedback_movie!!.mp!!.vh //getH();
							    var asp = feedback_movie!!.mp!!.aspect
							    val c_w = card_panel!!.width
							    val c_h = card_panel!!.height
							    val wwd = 0.81415926535897932 * (c_w * 1.0 / v_w)
							    val hhd = 0.81415926535897932 * (c_h * 1.0 / v_h)
							    var ffd = if (wwd < hhd) wwd else hhd
							    if (ffd > 3.1415926535897932384626) {
								ffd = 3.1415926535897932384626
							    }
							    val www = (ffd * v_w).toInt()
							    val hhh = (ffd * v_h).toInt()
							    if (asp <= 0) {
								asp = 1.33
							    }
							    val nwww = c_w / 2
							    val nhhh = (nwww / asp).toInt()
							    val o_w = ((c_w - nwww) / 2)
							    val o_h = ((c_h - nhhh) / 2)
							    feedback_movie!!.mp!!.setSize(nwww, nhhh)
							    feedback_movie!!.mp!!.setLocation(
								    o_w,
								    (o_h * 0.851415926535897932384626).toInt()
							    )
							    OmegaContext.lesson_log.getLogger().warning(
								    "movie feedback size: "
									    + v_w + ' '
									    + v_h + ' '
									    + c_w + ' '
									    + c_h + ' '
									    + wwd + ' '
									    + hhd + ' '
									    + www + ' '
									    + hhh + ' '
									    + o_w + ' '
									    + o_h + ' '
									    + asp
									    + ""
							    )
							    //  1 1 1445 1026 1176.460138443725 835.3274062583127 3 3 721 511
							    m_pan.isVisible = true
							    card_panel!!.add(m_pan, "feedback_movie")
							    card_show("feedback_movie")
							    feedback_movie!!.perform()
							    feedback_movie!!.waitEnd()
							    m_sleep(200)
							    card_show("words")
							    card_panel!!.remove(m_pan)
							    le_canvas!!.requestFocus()
							    le_canvas!!.showMsg(null)
							    //							OmegaContext.sout_log.getLogger().info(":--: " + "now ready mpg (Lesson:1853)");
							    do_prepare = true
							} catch (ex: Exception) {
							    OmegaContext.lesson_log.getLogger()
								    .warning("movie feedback failed: $ex")
							}
						    }
						} finally {
						}
					    } else {
					    }
					    if (true) {
						val t_b = currentPupil.getBool("text_on", true)
						val i_b = currentPupil.getBool("image_on", true)
						val v_b = currentPupil.getBool("speech_on", false)
						val msgitm = MsgItem(
							'R',
							t("Right answer"),
							if (t_b) currentPupil.getString(
								"text",
								t("Correct answer")
							) else "",
							"",
							if (i_b) currentPupil.imageName else null,
							null,
							""
						)
						if (v_b) {
						    val speech = currentPupil.getString("speech", "")
						    if (speech!!.length > 0) {
							sayS(speech)
						    }
						}
						if (t_b || i_b) {
						    mistNoMouse = false
						    le_canvas!!.showMsg(msgitm)
						}
					    }
					    if (do_prepare) {
						if (currentPupil.getBool("sign_movie_on", false)) {
						    val mn = currentPupil.getString("sign_movie", null)
						    feedback_movie!!.dispose()
						    feedback_movie!!.prepare(mn, null)
						} else if (currentPupil.getBool("movie_on", false)) {
						    val mn = currentPupil.getString("movie", null)
						    feedback_movie!!.dispose()
						    feedback_movie!!.prepare(mn, null)
						}
					    }
					    mistNoMouse = false
					    m_sleep(currentPupil.getSpeed(500))
					    sayPingAnim()
					    waitSayAll()
					    //					    waitBoxPlay();
					} else { // wrong sentence
					    seq!!.cnt_sent_wrong++
					    if (register != null) {
						register!!.test(
							":wrong",
							exec_hbox_time,
							seq!!.getTestText(current_test_mode, false),
							tg.allText,
							seq!!.getStat(false),
							tg.all_Tid_Item
						)
					    }
					    val t_b = currentPupil.getBool("text_on", true)
					    val i_b = currentPupil.getBool("image_wrong_on", true)
					    if (false && isLIU_Mode()) { // on request LIU
						if (feedback_movie == null) {
						    feedback_movie = FeedBackMovie(true)
						} else {
						    feedback_movie!!.dispose()
						}
						val mn = "media/sign/aaa.mpg"
						feedback_movie!!.prepare(mn, null)
						try {
						    val m_pan = feedback_movie!!.canvas
						    m_pan!!.layout = null
						    m_pan.background =
							    omega_settings_dialog.feedback_movie_background!!.color
						    val v_w = feedback_movie!!.w
						    val v_h = feedback_movie!!.h
						    val c_w = card_panel!!.width
						    val c_h = card_panel!!.height
						    val wwd = 0.81415926535897932 * (c_w * 1.0 / v_w)
						    val hhd = 0.81415926535897932 * (c_h * 1.0 / v_h)
						    var ffd = if (wwd < hhd) wwd else hhd
						    if (ffd > 3.1415926535897932384626) {
							ffd = 3.1415926535897932384626
						    }
						    val www = (ffd * v_w).toInt()
						    val hhh = (ffd * v_h).toInt()
						    feedback_movie!!.mp!!.setSize(www, hhh)
						    feedback_movie!!.mp!!.setLocation(
							    ((c_w - www) / 2),
							    (((c_h - hhh) / 2) * 0.851415926535897932384626).toInt()
						    )
						    m_pan.isVisible = true
						    card_panel!!.add(m_pan, "feedback_movie")
						    card_show("feedback_movie")
						    feedback_movie!!.perform()
						    feedback_movie!!.waitEnd()
						    m_sleep(200)
						    card_show("words")
						    card_panel!!.remove(m_pan)
						    le_canvas!!.requestFocus()
						    le_canvas!!.showMsg(null)
						} catch (ex: Exception) {
						    OmegaContext.lesson_log.getLogger()
							    .warning("movie feedback failed: $ex")
						}
					    } else {
						val msgitm = MsgItem(
							'W',
							t("Sorry, wrong answer"),
							(if (allCorrect.size == 0) correct_text else allCorrect.toString())!!,  //correct_text,
							"",
							if (i_b) currentPupil.imageNameWrongAnswer else null,
							null,
							t("Correct answer is" + (if (allCorrect.size == 0) "" else " one of") + " :")
						)
						if (t_b || i_b) {
						    mistNoMouse = false
						    le_canvas!!.showMsg(msgitm)
						}
					    }
					}
					le_canvas!!.setMarkTargetNo()
					seq!!.next(current_test_mode)
					mistNoMouse = false
					val has_more = seq!!.getTestText(current_test_mode, false) != null
					//				    OmegaContext.sout_log.getLogger().info(":--: " + "====))) " + has_more + ' ' + seq);
					if (has_more) {
					    card_show("anim1")
					    sendMsg("test_cont", null, "L1745")
					} else {
					    le_canvas!!.showMsg(resultSummary_MsgItem)

// 							      new MsgItem('SundryUtil',
// 									  T.t("Test Statistics"),

// 									  T.t("Correct") + ": " +
// 									  seq.cnt_sent_correct + " " +
// 									  fixSP(T.t("sentence "), T.t("sentences"), seq.cnt_sent_correct) +
// 									  " (" +
// 									  seq.cnt_word_correct + " " +
// 									  fixSP(T.t("word"), T.t("words"), seq.cnt_word_correct) + ")",

// 									  T.t("Wrong") + ": " +
// 									  seq.cnt_sent_wrong + " " +
// 									  fixSP(T.t("sentence "), T.t("sentences"), seq.cnt_sent_wrong) +
// 									  " (" +
// 									  seq.cnt_word_wrong + " " +
// 									  fixSP(T.t("word"), T.t("words"), seq.cnt_word_wrong) + ")",

// 									  getCurrentPupil().getImageName(),
// 									  getCurrentPupil().getImageNameWrongAnswer(),
// 									  null));
					    if (register != null) {
						register!!.has_shown = true
						register!!.close()
					    }
					    register = null
					    card_show("main")
					}
				    } // test_mode
				} // tg is filled
				le_canvas!!.enableQuitButton(true)
				if (current_test_mode != TM.CREATE) {
				    le_canvas!!.setNextMarkTarget()
				}
			    }
			}
		    }
		} catch (ex: Exception) {
		    OmegaContext.sout_log.getLogger().info("ERR: Lesson: $ex")
		    ex.printStackTrace()
		}
	    }
	    le_canvas!!.ready()
	} finally {
	    le_canvas!!.skip_keycode = false
	    inExecHbox = false
	    mistNoMouse = false
	}
    }

    private fun fixWithStar(t_word: String?): String {
	if (t_word!!.contains("*")) {
	    if (t_word.contains(":")) {
		val sa = t_word.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		return sa[1]
	    }
	    return ""
	}
	return t_word
    }

    private fun getMatchingSameActionSpecific(action_specific: ActionSpecific?, correct_text: String?): Set<String> {
	val kset: Set<*> = action_specific!!.hm.keys
	var `val`: String? = null
	for (o in kset) {
	    val k = o as String
	    if (k == correct_text) {
		`val` = action_specific.hm[k]
	    }
	}
	val ss: MutableSet<String> = HashSet()
	for (o in kset) {
	    val k = o as String
	    if (action_specific.hm[k] == `val`) ss.add(k)
	}
	return ss
    }

    var last_story_flag = false

    internal inner class MyRunnable(var le: Lesson) : Runnable {
	override fun run() {}
    }

    private fun exec_play(tg: Target, waiting: Boolean) {
	le_canvas!!.hideMsg()
	//		le_canvas.init();
	if (tg.isTargetFilled) {
	    le_canvas!!.removeHilitedBox()
	    val actions = tg.getActionFileName(999) // all actions from all target boxes
	    val action_sa = split(actions, ",")
	    val larglen = tg.lessonArgLength
	    val actA = tg.all_Lid_Item
	    val actTextA = tg.all_TextVars_Item // text<PARAGRAF>v1<PARAGRAF>v2<PARAGRAF>v3<PARAGRAF>sound
	    val sound_list = tg.all_Sound_Item // sound,sound...
	    val pathA = tg.all_Lid_Target
	    lesson_log.getLogger().info(
		    "ANIMDATA is actA="
			    + a2s(actA) + " actTextA="
			    + a2s(actTextA) + " pathA="
			    + a2s(pathA) + " sound="
			    + sound_list + " action_A="
			    + a2s(action_sa)
	    )
	    if (action == null) {
		action = AnimAction()
		card_panel!!.add(action!!.canvas, "anim1")
	    }
	    try {
		for (anim_i in action_sa.indices) {
		    var is_last = false
		    if (anim_i == action_sa.size - 1) {
			is_last = true
		    }
		    val action_s = action_sa[anim_i]
		    var all_text = tg.allText
		    lesson_log.getLogger().info("loop actions: $anim_i $is_last $action_s $all_text")
		    if (action_specific!!.`is`(all_text)) {
			element_root = action!!.prefetch(action_s)
			if (element_root == null);
			//			      T.t("Can't find animation: " + action_s));
			if (!true) {
			    System.gc()
			}
			val anim_twice = currentPupil.getBool("repeatanim", false)
			startProgress()
			val pan = performMpgAction(all_text, action_s, actA, pathA, tg)
			if (!edit) {
			    if (is_last) {
				tg.releaseAllT_Items()
			    }
			}
			card_show("words")
			if (pan != null) {
			    pan.isVisible = false
			}
			if (pan != null) {
			    card_panel!!.remove(pan)
			}
			if (!edit) {
			    if (is_last) {
				le_canvas!!.endLastAction()
			    } else {
				le_canvas!!.endAction()
			    }
			}
			le_canvas!!.requestFocus()
			le_canvas!!.showMsg(null)

			// 			if ( is_last && current_test_mode == TM_CREATE )
			// 			    le_canvas.waitReplyAction((AnimAction)action,
			// 						      all_text,
			// 						      getCurrentPupil().getBool("showSentence", true));
			le_canvas!!.requestFocus()
		    } else { // normal animation
			val ti = measureTime {
			    element_root = action!!.prefetch(action_s)
			}
			getLogger().info("It took $ti")
			if (element_root == null) {
			    JOptionPane.showMessageDialog(
				    ApplContext.top_frame,
				    t("Can't find animation: $action_s")
			    )
			    continue
			}
			if (!true) {
			    System.gc()
			}
			action!!.show()
			val variables_hm = HashMap<String?, String?>()
			tg.putAll_TextVars_Item(variables_hm)
			action!!.hm["speed"] = currentPupil.getSpeed(1000)
			action!!.hm["anim_background"] = omega_settings_dialog.anim_background!!.color
			action!!.hm["anim_colors"] = le_canvas!!.colors
			action!!.hm["variables"] = variables_hm
			action!!.hm["colors"] = canvases["words"]!!.colors
			val anim_twice = currentPupil.getBool("repeatanim", false)
			card_show("anim1")
			action!!.perform(
				window!!,
				action_s,
				actA,
				pathA,
				if (anim_twice) 1 else 0,
				Runnable { //card_show("anim1");
				    OmegaContext.sout_log.getLogger().info(":--: " + "start hook")
				}
			)
			if (anim_twice) {
			    m_sleep(currentPupil.getSpeed(800))
			    action!!.perform(
				    window!!,
				    action_s,
				    actA,
				    pathA,
				    0,
				    null
			    )
			}
			val allText = tg.allText
			saveRecastAction(
				le_canvas!!.lessonName!!,
				action_s,
				actA,
				actTextA,
				sound_list,
				pathA,
				true,
				tg,
				is_last,
				allText
			)
			val sent_li = story_hm["sentence_list"]
			val ss_li = sent_li!!.sentence_list
			//			OmegaContext.sout_log.getLogger().info(":--: " + "ALL TEXT1 " + all_text);
			all_text = tg.allText
			//			OmegaContext.sout_log.getLogger().info(":--: " + "ALL TEXT2 " + all_text);
			if (is_last) {
			    ss_li!!.add(all_text)
			}
			sent_li.lesson_name = le_canvas!!.lessonName!!
			lesson_log.getLogger().info("SENTENCE $ss_li")
			OmegaContext.story_log.getLogger().info(
				"added sent 2214 " + sent_li.lesson_name
					+ ' ' + all_text
			)
			if (tg.storyNext == null) {
			    if (ss_li!!.size <= 1) {
				story_hm["sentence_list"] = SentenceList()
				OmegaContext.story_log.getLogger().info("new sent_list 2214 $ss_li")
			    } else {
				OmegaContext.story_log.getLogger().info("Lst in story  2214 $ss_li")
				last_story_flag = true
			    }
			} else {
			    last_story_flag = false
			}
			class MyRA : Runnable {
			    override fun run() {
				OmegaContext.sout_log.getLogger().info(":--: " + "MyRA called")
				sayAll(tg)
			    }
			}

			val myra = MyRA()
			if (waiting) {
			    OmegaContext.sout_log.getLogger().info(":--: waitReply? $is_last $current_test_mode")
			    if (is_last && current_test_mode == TM.CREATE) {
				var end_code_s = le_canvas!!.waitReplyAction(
					(action as AnimAction?)!!,
					all_text,
					currentPupil.getBool("showSentence", true),
					myra
				)
				OmegaContext.sout_log.getLogger().info(":--: Lesson: end_code_s $end_code_s")
				if (end_code_s == "left") {
				    action!!.perform(
					    window!!,
					    action_s,
					    actA,
					    pathA,
					    0,
					    null
				    )
				    end_code_s = le_canvas!!.waitReplyAction(
					    (action as AnimAction?)!!,
					    all_text,
					    currentPupil.getBool("showSentence", true),
					    myra
				    )
				    OmegaContext.sout_log.getLogger().info(":--: Lesson: end_code_s2 $end_code_s")
				}
			    }
			}
			if (!edit) {
			    if (is_last) {
				tg.releaseAllT_Items()
			    }
			}
			if (is_last) {
			    le_canvas!!.endLastAction()
			} else {
			    le_canvas!!.endAction()
			}
			le_canvas!!.requestFocus()
		    }
		}
		if (register != null) {
		    register!!.restart()
		}
		if (last_story_flag) {
		    sentence_canvas!!.setRead(false)
		    card_show("sent")
		} else if (current_test_mode != TM.CREATE) {
		    card_show("words")
		}
	    } catch (ex: Exception) {
		OmegaContext.sout_log.getLogger().info("ERR: @@@@@p $ex")
		ex.printStackTrace()
	    }
	}
    }

    private fun performMpgAction(
	    all_text: String,
	    action_s: String?,
	    actA: Array<String>,
	    pathA: Array<String>,
	    tg: Target
    ): JPanel? {
	var pan: JPanel? = null
	try {
	    if (mpg_action == null) {
		mpg_action = MpgAction()
	    } else {
		getLogger().info("--------------- probably not to come here")
		mpg_action!!.reset()
	    }
	    mpg_action!!.prefetch(action_specific!!.getAction(all_text), window!!.width, window!!.height)
	    if (!false) {
		mpg_action!!.mpg_player!!.fxp!!.waitReady()
		pan = mpg_action!!.canvas
		//pan.setLayout(null);
		pan!!.background = omega_settings_dialog.action_movie_background!!.color
		val v_w = mpg_action!!.w
		val v_h = mpg_action!!.h
		val c_w = card_panel!!.width
		val c_h = card_panel!!.height
		getLogger().info("mpg size $v_w $v_h $c_w $c_h")
		val wwd = 0.81415926535897932 * (c_w * 1.0 / v_w)
		val hhd = 0.81415926535897932 * (c_h * 1.0 / v_h)
		var ffd = if (wwd < hhd) wwd else hhd
		if (ffd > 3.1415926535897932384626) {
		    ffd = 3.1415926535897932384626
		}
		val www = (ffd * v_w).toInt()
		val hhh = (ffd * v_h).toInt()
		//mpg_action.setSize((int) www, (int) hhh);
		getLogger().info("mpg size $www $hhh")

		//mpg_action.setLocation(o_w, (int) (o_h * 0.851415926535897932384626));
		getLogger().info("mpg  loc ${((c_w - www) / 2)} ${((c_h - hhh) / 2)}")
	    }
	    pan!!.isVisible = true
	    card_panel!!.add(pan, "msg_anim")
	    //log 			OmegaContext.sout_log.getLogger().info(":--: " + "CCnt " + card_panel.getComponentCount());
	    card_show("msg_anim")
	    mpg_action!!.show()
	    mpg_action!!.hm["speed"] = currentPupil.getSpeed(1000)
	    progress!!.dismiss()
	    //	    mpg_action.setParentWH(c_w, c_h);
	    mpg_action!!.sentence = all_text
	    OmegaContext.sout_log.getLogger().info(":--: sentence $all_text")
	    val anim_twice = currentPupil.getBool("repeatanim", false)
	    val show_sentence = currentPupil.getBool("showSentence", true)
	    mpg_action!!.show_sentence = show_sentence
	    if (current_test_mode_group == TMG.TEST) {
		mpg_action!!.show_sentence = false
	    }
	    class MyRA : Runnable {
		override fun run() {
		    OmegaContext.sout_log.getLogger().info(":--: " + "MyRA called")
		    sayAll(tg)
		}
	    }

	    val myra = MyRA()
	    val scan = canvases["words"]
	    mpg_action!!.hm["colors"] = scan!!.colors
	    mpg_action!!.perform(
		    window!!,
		    action_s,
		    actA,
		    pathA,
		    if (anim_twice) 1 else 0,
		    myra
	    )
	    m_sleep(currentPupil.getSpeed(400))
	    if (anim_twice) {
		mpg_action!!.reset()
		mpg_action!!.perform(
			window!!,
			action_s,
			actA,
			pathA,
			0,
			myra
		)
	    }
	    mpg_action!!.stop()
	    mpg_action!!.dispose()
	    mpg_action = null
	} catch (ex: Exception) {
	    ex.printStackTrace()
	}
	return pan
    }

    private fun testDialog() {
	val choise = arrayOf(
		"Printer",
		"Story",
		"Anim message",
		"Words message"
	)
	global_skipF(true)
	val a = JOptionPane.showOptionDialog(
		ApplContext.top_frame,
		t("What kind of test?"),
		t("Omega - System Test"),
		JOptionPane.OK_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		choise,
		choise[0]
	)
	global_skipF(false)
	if (a == JOptionPane.CLOSED_OPTION) {
	    return
	}
	val ccard = current_card
	when (a) {
	    0 -> TEST_print()
	    1 -> TEST_story()
	    2 -> TEST_anim()
	    3 -> TEST_words()
	}
	waitHitKey(10)
	card_show(ccard)
    }

    class PlayData internal constructor(
	    var lesson_name: String,
	    var action_s: String?,
	    var actA: Array<String>,
	    var actTextA: Array<String>,
	    var sound_list: String?,
	    var pathA: Array<String>,
	    var is_last: Boolean,
	    val allText: String
    ) : Serializable {
	fun theWord(): String {
	    return allText
	}

	override fun toString(): String {
	    return (lesson_name + ','
		    + action_s + ','
		    + a2s(actA) + ','
		    + a2s(actTextA) + ','
		    + sound_list + ','
		    + a2s(pathA) + ','
		    + allText)
	}
    }

    var play_data_list = PlayDataList()
    var play_data_list_is_last = PlayDataList()
    fun playFromDataList(playDataList: PlayDataList) {
	val al = playDataList.arr
	card_show("anim1")
	action!!.show()
	action!!.hm["speed"] = currentPupil.getSpeed(1000)
	action!!.hm["anim_background"] = omega_settings_dialog.anim_background!!.color
	action!!.hm["anim_colors"] = le_canvas!!.colors
	action!!.hm["colors"] = canvases["words"]!!.colors
	al.forEach {pd ->
	    if (hit_key == '\u001b'.code) {
		return
	    }
	    action!!.prefetch(pd.action_s)
	    action!!.perform(
		    window!!,
		    pd.action_s,
		    pd.actA,
		    pd.pathA,
		    0,
		    null
	    )
	    saveRecastAction(
		    le_canvas!!.lessonName!!,
		    pd.action_s,
		    pd.actA,
		    pd.actTextA,
		    pd.sound_list,
		    pd.pathA,
		    false,
		    null,
		    pd.is_last,
		    pd.allText
	    )
	}
    }

    private fun TEST_print() {
	if (false) {
	    try {
		val pm = PrintMgr()
		//	    pm.list(true);
		val ss_li: ArrayList<String?> = ArrayList()
		ss_li.add("Raden 1")
		ss_li.add("Rad 2")
		ss_li.add("Ldkfj lkdjf ldksjf ldksjf lkdsjf lsdkjf ldskjf ldskjf dslkjf sdlkfh sdkjfh dskjf hsdkfjhds SIST.")
		ss_li.add("SLUT")
		pm.print(null, "Omega TEST", ss_li, "TITLE")
	    } catch (ex: Exception) {
		OmegaContext.sout_log.getLogger().info("ERR: PRINTER $ex")
	    }
	}
    }

    fun TEST_story() {
	OmegaContext.sout_log.getLogger().info(":--: " + "TEST story")
	card_show("sent")
	sentence_canvas!!.showMsg(null)
	val key = waitHitKey(1)
	val al: ArrayList<String?> = ArrayList()
	al.add("Den talande reven rev en annan rev")
	al.add("")
	al.add("Flamingon flyger lagom")
	al.add("Inga vandrande pinnar skriver klart denna test")
	al.add("SLUT")
	sentence_canvas!!.showMsg(al)
	sentence_canvas!!.showMsgMore()
    }

    fun TEST_anim() {
	card_show("anim1")
	le_canvas!!.showMsg(
		MsgItem(
			'S',
			"Test Statistics",
			"Correct",
			"Wrong",
			"XX",
			"YY",
			null
		)
	)
	m_sleep(2000)
    }

    fun TEST_words() {
	card_show("words")
	le_canvas!!.showMsg(resultSummary_MsgItem)
	// new MsgItem('SundryUtil',
// 				       T.t("Test Statistics"),

// 				       T.t("Correct") + ": " +
// 				       seq.cnt_sent_correct + " " +
// 				       fixSP(T.t("sentence "), T.t("sentences"), seq.cnt_sent_correct) +
// 				       " (" +
// 				       seq.cnt_word_correct + " " +
// 				       fixSP(T.t("word"), T.t("words"), seq.cnt_word_correct) + ")",

// 				       T.t("Wrong") + ": " +
// 				       seq.cnt_sent_wrong + " " +
// 				       fixSP(T.t("sentence "), T.t("sentences"), seq.cnt_sent_wrong) +
// 				       " (" +
// 				       seq.cnt_word_wrong + " " +
// 				       fixSP(T.t("word"), T.t("words"), seq.cnt_word_wrong) + ")",

// 				       getCurrentPupil().getImageName(),
// 				       getCurrentPupil().getImageNameWrongAnswer(),
// 				       null));
    }

    var print_service: PrintService? = null
    fun printFromDataList(playDataList: PlayDataList) {
	try {
	    val al = playDataList.arr
	    val pm = PrintMgr()
	    //	    pm.list(true);
	    val sent_li = story_hm["sentence_list"]
	    val ss_li = sent_li!!.sentence_list
	    OmegaContext.story_log.getLogger().info("printed 2402 $ss_li")
	    global_skipF(true)
	    pm.prepare("Omega", ss_li, sent_li.lesson_name)
	    val job = pm.printJob
	    if (job != null) {
		pm.doThePrint(job)
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: PRINTER $ex")
	    ex.printStackTrace()
	}
	global_skipF(false)
    }

    fun waitHitKey(a: Int): Int {
	val start_cnt_hit_keyOrButton = cnt_hit_keyOrButton
	while (cnt_hit_keyOrButton < start_cnt_hit_keyOrButton + a) {
	    m_sleep(100)
	}
	return hit_key
    }

    fun listenFromDataList(
	    playDataList: PlayDataList /*
	     * , ListenListener lili
             */
    ) {
	// 	lili.init();
	sentence_canvas!!.buttonsEnable(false)
	try {
	    OmegaContext.story_log.getLogger().info("listened 2411 " + playDataList.arr)
	    val it: Iterator<*> = playDataList.arr.iterator() // has continue
	    while (it.hasNext()) {
		sentence_canvas!!.showMsgMore()
		val key = waitHitKey(1)
		if (key == '\u001b'.code) {
		    sentence_canvas!!.showMsg(null)
		    return
		}
		val pd = it.next() as PlayData
		if (OmegaConfig.tts) {
		    val lang = OmegaContext.lessonLang
		    if (say(lang, pd.theWord(), true)) continue
		}
		OmegaContext.story_log.getLogger().info("PD is $pd")
		val soundA = split(pd.sound_list, ",")
		for (i in soundA.indices) {
		    val sound = soundA[i]
		    val ap = createAPlayer(
			    currentPupil.getStringNo0("languageSuffix", null),
			    sound,
			    null,
			    "SA_$i"
		    )
		    m_sleep(50)
		    ap.playWait()
		    m_sleep(50)
		}
		//		APlayer.unloadAll("SA_[0-9]*");
		m_sleep(500)
	    }
	    sentence_canvas!!.showMsgMore()
	} finally {
	    sentence_canvas!!.buttonsEnable(true)
	}
	//	lili.done();
    }

    private fun saveRecastAction(
	    lesson_name: String,
	    action_s: String?,
	    actA: Array<String>,
	    actTextA: Array<String>,
	    sound_list: String?,
	    pathA: Array<String>,
	    add_in_playlist: Boolean,
	    tg: Target?,
	    is_last: Boolean,
	    allText: String
    ) {
	if (add_in_playlist) {
	    val play_data = PlayData(lesson_name, action_s, actA, actTextA, sound_list, pathA, is_last, allText)
	    play_data_list.add(play_data)
	    if (is_last) {
		play_data_list_is_last.add(play_data)
	    }
	}
	try {
	    for (i in actA.indices) {
		var txt: String?
		txt = if (actTextA.size <= i) {
		    ""
		} else {
		    actTextA[i]
		}
		putDynamic(lesson_name, actA[i], txt, pathA[i])
	    }
	    if (tg != null) {
		for (i in 0 until tg.get_howManyT_Items()) {
		    val t_item = tg.getT_Item(i)
		    val actor_text = t_item!!.textVarsOrNull
		    val lid = t_item.item!!.lid
		    putDynamic(lesson_name, lid, actor_text, "W" + (i + 1))
		}
	    }
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: Cant put dynamic $ex")
	}
    }

    fun putDynamic(
	    thisLessonName: String,
	    actor_lid: String?,
	    actor_text: String?,  // § separated    ////    UTF-8
	    timeline_lid: String?
    ) {
	try {
	    val sa = actor_text!!.split("§".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() ////    UTF-8
	    // 	    OmegaContext.sout_log.getLogger().info(":--: " + "put dyn " + thisLessonName + ' ' +
	    // 		 actor_lid + ' ' +
	    // 		 actor_text + ' ' +
	    // 		 timeline_lid);
	    story_hm["$thisLessonName.$timeline_lid.text"] = SentenceList(sa[0])
	    story_hm["$thisLessonName.$timeline_lid.var-1"] = SentenceList(sa[1].substring(1))
	    story_hm["$thisLessonName.$timeline_lid.var-2"] = SentenceList(sa[2].substring(1))
	    story_hm["$thisLessonName.$timeline_lid.var-3"] = SentenceList(sa[3].substring(1))
	    story_hm["$thisLessonName.$timeline_lid.sound"] = SentenceList(sa[4].substring(1))
	    story_hm["$thisLessonName.$timeline_lid.Lid"] = SentenceList(actor_lid)
	    story_hm["$thisLessonName.$timeline_lid.actor"] = SentenceList(actor_lid)
	    OmegaContext.story_log.getLogger().info("put dynact " + story_hm)
	} catch (ex: Exception) {
	    OmegaContext.sout_log.getLogger().info("ERR: put dynact $actor_text $ex")
	}
    }

    fun runLessons(w: Window?, mpan: JPanel, fn: String?, edit: Boolean, winSize: OmegaConfig.WinSize) {
	var winSize= winSize
	Lesson.edit = edit
	le_canvas!!.edit = edit
	window = w
	KeyboardFocusManager.setCurrentKeyboardFocusManager(object : DefaultKeyboardFocusManager() {
	    var last_state = '_'
	    var state = 'r'
	    var first_tr = false
	    var P = !false
	    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
		if (skip_F) {
		    return super.dispatchKeyEvent(e)
		}
		val ch = e.keyChar
		val kc = e.keyCode
		val cc = current_card
		var do_own = false
		var was_first = false
		var do_it = false
		if (e.id == KeyEvent.KEY_PRESSED && (isKeySelect(kc)
				|| isKeyESC(kc)
				|| isKeyNext(kc))
		) {
		    hit_key = kc
		    cnt_hit_keyOrButton++
		}

		// 		    if ( e.getKeyCode() == KeyEvent.VK_F1 ) {
		// 			showHelp(OmegaContext.HELP_STACK.get());
		// 		    }
		if (kc != 16 && kc != 17 && kc != 18) {
		    if (e.id == KeyEvent.KEY_PRESSED) {
			if (P) {
			    OmegaContext.sout_log.getLogger().info(
				    ":--: " + "KEY: P " + current_card + ' ' + e.id
					    + " '" + ch + "' " + kc + "" + ' ' + state + last_state
			    )
			}
			if (state == 'r') {
			    last_state = state
			    state = 'p'
			    do_own = true
			} else {
			}
			if (e.keyCode == KeyEvent.VK_F1) {
			    showHelp("")

//				showHelp(OmegaContext.HELP_STACK.get());
			}
			if (e.keyCode == KeyEvent.VK_F2 && (e.isShiftDown || e.isControlDown)) {
			    if (cc == "pupil") {
				pupil_canvas!!.changeBehaviour()
			    }
			}
			if (e.keyCode == KeyEvent.VK_LEFT) {
			    if (cc == "msg_anim" && mpg_action != null) {
				mpg_action!!.ownKeyCode('l'.code, false)
			    }
			}
			if (e.keyCode == KeyEvent.VK_RIGHT) {
			    if (cc == "words" && le_canvas != null) {
				le_canvas!!.ownKeyCode('r'.code, false)
			    }
			}
			if (e.keyCode == KeyEvent.VK_UP) {
			    if (cc == "msg_anim" && mpg_action != null) {
				mpg_action!!.ownKeyCode('u'.code, false)
			    }
			}
			if (e.keyCode == KeyEvent.VK_F5 && (e.isControlDown || e.isShiftDown)) {
			    SwingUtilities.invokeLater {
				global_skipF(true)
				omega_settings_dialog.isVisible = true
				global_skipF(false)
			    }
			}
			if (e.keyCode == KeyEvent.VK_F12 && e.isControlDown && e.isShiftDown) {
			    SwingUtilities.invokeLater { sendMsg("test_dialog", "", "loadTest1") }
			}
		    }
		    if (e.id == KeyEvent.KEY_TYPED) {
			if (P) {
			    OmegaContext.sout_log.getLogger()
				    .info(":--: " + "KEY: T " + current_card + ' ' + e.id + " '" + ch + "' " + kc + "" + ' ' + state + last_state)
			}
			state = 't'
			if (first_tr == false) {
			    first_tr = true
			    was_first = true
			}
		    }
		    if (e.id == KeyEvent.KEY_RELEASED) {
			last_state = state
			state = 'r'
			if (P) {
			    OmegaContext.sout_log.getLogger()
				    .info(":--: " + "KEY: R " + current_card + ' ' + e.id + " '" + ch + "' " + kc + "" + ' ' + state + last_state)
			}
			first_tr = false
		    }
		} else {
		    do_it = true
		}
		if (P) {
		    OmegaContext.sout_log.getLogger().info(":--: KEY: state $state$last_state $do_own")
		}
		var ret = true
		var dispatch = true
		if (state == 'p' && last_state == 'r' || state == 't' && last_state == 'r' && was_first || state == 'r' && last_state == 't') {
		    do_it = true
		}
		if (do_it && do_own) {
		    if (e.id == KeyEvent.KEY_PRESSED) {
			if (kc == 38 || kc == 40) {
			    if ("pupil" == cc) {
				if (pupil_canvas != null) {
				    ret = pupil_canvas!!.ownKeyCode(kc, e.isShiftDown)
				}
				dispatch = true
			    }
			}
			if (isKeyNext(kc)
				|| isKeySelect(kc)
				|| isKeyESC(kc)
			) {
			    if ("anim1" == cc) {
				dispatch = true
			    }
			    if ("words" == cc) {
				if (le_canvas != null) {
				    ret = le_canvas!!.ownKeyCode(kc, e.isShiftDown)
				}
				dispatch = false
			    }
			    if ("pupil" == cc) {
				if (pupil_canvas != null) {
				    ret = pupil_canvas!!.ownKeyCode(kc, e.isShiftDown)
				}
				dispatch = true
			    }
			    if ("msg_anim" == cc) {
				if (mpg_action != null) {
				    ret = mpg_action!!.ownKeyCode(kc, e.isShiftDown)
				}
			    }
			    if ("sent" == cc) {
				if (sentence_canvas != null) {
				    ret = sentence_canvas!!.ownKeyCode(kc, e.isShiftDown)
				}
			    }
			    if ("main" == cc) {
				ret = lemain_canvas!!.ownKeyCode(kc, e.isShiftDown)
			    }
			}
		    }
		}
		if (do_it && dispatch) {
		    if (P) {
			OmegaContext.sout_log.getLogger().info(":--: " + "do dispatch ")
		    }
		    return super.dispatchKeyEvent(e)
		}
		return true
	    }
	})
	if (fn != null) {
	    is_testing = true
	}
	window!!.addWindowListener(object : WindowAdapter() {
	    override fun windowClosing(ev: WindowEvent) {
		//		    savePrefetch();
		globalExit = true
		sendMsg("exitLesson", "", "")
		//System.exit(0);
	    }
	})
	val pan = JPanel()
	if (edit) {
	    lep = LessonEditorPanel(le_canvas!!)
	    le_canvas!!.lep = lep
	    mpan.add(lep, BorderLayout.NORTH)
	}
	mpan.add(pan, BorderLayout.CENTER)
	card = CardLayout()
	pan.layout = card
	card_panel = pan
	pan.add(le_canvas, "words")
	pan.add(lemain_canvas, "main")
	pan.add(pupil_canvas, "pupil")
	pan.add(sentence_canvas, "sent")
	window!!.isVisible = true
	if (OmegaContext.isDeveloper) winSize = OmegaConfig.WinSize.SMALLER
	val d = Toolkit.getDefaultToolkit().screenSize
	when (winSize) {
	    OmegaConfig.WinSize.DEFAULT -> {
		window!!.size = d
		window!!.setLocation(0, 0)
	    }

	    OmegaConfig.WinSize.SMALL -> {
		d.width = OmegaConfig.FRAME_WIDTH
		d.height = OmegaConfig.FRAME_HEIGHT
		window!!.size = d
	    }

	    OmegaConfig.WinSize.SMALLER -> {
		d.width = OmegaConfig.FRAME_WIDTH(50)
		d.height = OmegaConfig.FRAME_HEIGHT(50)
		window!!.size = d
	    }

	    OmegaConfig.WinSize.SMALLEST -> {
		d.width = OmegaConfig.FRAME_WIDTH(25)
		d.height = OmegaConfig.FRAME_HEIGHT(25)
		window!!.size = d
	    }

	    OmegaConfig.WinSize.FULLSCREEN -> {
		d.width = OmegaConfig.FRAME_WIDTH
		d.height = OmegaConfig.FRAME_HEIGHT
		window!!.size = d
	    }
	}
	if (edit) {
	    card_show("words")
	} else {
	    card_show("pupil")
	}
	if (winSize == OmegaConfig.WinSize.FULLSCREEN && window is JFrame) {
	    (window as JFrame).extendedState = JFrame.MAXIMIZED_BOTH
	    if (ApplLesson.isMac) {
		try {
		    val appClass = Class.forName("com.apple.eawt.Application")
		    val params = arrayOf<Class<*>>()
		    val getApplication = appClass.getMethod("getApplication", *params)
		    val application = getApplication.invoke(appClass)
		    val requestToggleFulLScreen =
			    application.javaClass.getMethod("requestToggleFullScreen", Window::class.java)
		    requestToggleFulLScreen.invoke(application, window)
		    //		    Application.getApplication().requestToggleFullScreen(window);
		} catch (e: Exception) {
		    getLogger().warning("An exception occurred while trying to toggle full screen mode")
		}
	    }
	}
	while (true) {
	    try {
		execLesson(fn)
		if (globalExit) return
		break
	    } catch (ex: Exception) {
		OmegaContext.sout_log.getLogger().info("ERR: OOOOPPPSS $ex")
		ex.printStackTrace()
	    }
	}
    }

    fun displayColor(name: String?) {
	// 	String fn = "default.omega_colors";
	// 	fn = getCurrentPupil().getString("theme", fn);
	val fn = pupil_settings_dialog.selectedColorFile
	val l = canvases[name]
	try {
	    if (l != null) {
		global_skipF(true)
		val cd = ColorDisplay(l.colors, name!!)
		cd.isVisible = true
		if (cd.select) {
		    for ((key, value) in cd.colors!!) {
			l.colors[key] = value
		    }
		    l.updateDisp()
		    l.repaint()
		    if (false) { // do not select file
			val choose_f = ChooseColorFile()
			var url_s: String? = null
			val rv = choose_f.showDialog(null, t("Save"))
			if (rv == JFileChooser.APPROVE_OPTION) {
			    val file = choose_f.selectedFile
			    url_s = toURL(file)
			    if (!url_s!!.endsWith("." + ChooseColorFile.ext)) {
				url_s = url_s + "." + ChooseColorFile.ext
			    }
			    val tfn = mkRelativeCWD(url_s)
			    saveSettings(tfn)
			}
		    } else {
			saveSettings(fn)
		    }
		}
	    }
	} finally {
	    global_skipF(false)
	}
    }

    fun saveColor() {
	val choose_f = ChooseColorFile()
	try {
	    global_skipF(true)
	    var url_s: String? = null
	    val rv = choose_f.showDialog(null, t("Save"))
	    if (rv == JFileChooser.APPROVE_OPTION) {
		val file = choose_f.selectedFile
		url_s = toURL(file)
		if (!url_s!!.endsWith("." + ChooseColorFile.ext)) {
		    url_s = url_s + "." + ChooseColorFile.ext
		}
		val tfn = mkRelativeCWD(url_s)
		val el = settingsElement
		save(tfn!!, el)
	    }
	} finally {
	    global_skipF(false)
	}
    }

    fun where(s: String?, sa: Array<String?>): Int {
	for (i in sa.indices) {
	    if (sa[i] == s) {
		return i
	    }
	}
	return -1
    }

    fun setTestMatrix(sentA: Array<String?>, tmm: Array<IntArray>) {
	seq!!.setFromMatrix(sentA, tmm)
    }

    fun getTestMatrix(all_sentence: Array<String?>): Array<IntArray> {
	return seq!!.getTestMatrix(all_sentence)
    }

    val isTestMode: Boolean
	get() = current_test_mode_group == TMG.TEST

    fun showHelp(more: String?) {
	ApplLesson.help!!.showManualL(null)
    }

    init { // 'p', 't', 'a' or 'e'
	OmegaContext.lesson_log.getLogger().info("XX")
	val default_pupil: String
	default_pupil = if (run_mode == 'e') {
	    "Guest"
	} else {
	    val prefs = Preferences.userNodeForPackage(this.javaClass)
	    prefs["default_pupil", "Guest"]
	}
	currentPupil = Pupil(default_pupil)
	omega_settings_dialog.lesson = this
	this.run_mode = run_mode.code
	static_lesson = this
	l_ctxt = LessonContext(this)
	if (true) {
	    val ap = createAPlayer(
		    "audio/greeting.wav",
		    null,
		    null
	    )
	    ap.play()
	} else {
	    val name = "file:" + getMediaFile("audio/greeting.wav")
	    val ac = AudioClip(name)
	    ac.play()
	}
	if (true) {
	    val ap = createAPlayer(
		    "audio/greeting2.mp3",
		    null,
		    null
	    )
	    ap.play()
	}
	m_sleep(3000)
	machine = Machine(l_ctxt)
	le_canvas = LessonCanvas(l_ctxt)
	canvases.put("words", le_canvas)
	sentence_canvas = SentenceCanvas(l_ctxt)
	canvases.put("sent", sentence_canvas)
	lemain_canvas = LessonMainCanvas(l_ctxt)
	canvases.put("main", lemain_canvas)
	pupil_canvas = PupilCanvas(l_ctxt, currentPupil.name)
	if (run_mode == 'a') {
	    pupil_canvas!!.behaviour = PupilCanvas.BH_ADMINISTRATOR
	} else if (run_mode == 'e') {
	    pupil_canvas!!.behaviour = PupilCanvas.BH_ADMINISTRATOR
	} else if (run_mode == 'p') {
	    pupil_canvas!!.behaviour = PupilCanvas.BH_PUPIL
	}
	canvases.put("pupil", pupil_canvas)
	sentence_canvas!!.addLessonCanvasListener(this)
	le_canvas!!.addLessonCanvasListener(this)
	lemain_canvas!!.addLessonCanvasListener(this)
	pupil_canvas!!.addLessonCanvasListener(this)

	// Server lessond = new Server();
	sentence_canvas!!.om_msg_mgr.addListener(om_msg_li)
	le_canvas!!.om_msg_mgr.addListener(om_msg_li)
	lemain_canvas!!.om_msg_mgr.addListener(om_msg_li)
	pupil_canvas!!.om_msg_mgr.addListener(om_msg_li)
	restoreSettings()
    }

    fun tryLessonLanguages(s: String): String {
	val dot = File(omegaAssets("."))
	val scanned_lang = dot.list(FilenameFilter { dir, name ->
	    if (CnT == 0) {
		return@FilenameFilter name.startsWith("lesson-")
	    }
	    CnT--
	    if (name.startsWith("lesson-")) {                         // LESSON_DIR
		OmegaContext.lesson_log.getLogger().info("Try scan lesson lang: f " + dir.name + ' ' + name)
		return@FilenameFilter true
	    }
	    OmegaContext.lesson_log.getLogger().info("Try scan lesson lang: T " + dir.name + ' ' + name)
	    false
	})
	val selected_lang = if (scanned_lang.map { it.substring(7) }.contains(s)) s else "en"
	getLogger().info("I got scanned lang: $scanned_lang")
	return selected_lang
    }

    companion object {
	var story_hm = HashMap<String?, SentenceList?>()

	init {
	    story_hm["sentence_list"] = SentenceList()
	    // special entry med list sentence
	}

	var le_canvas: LessonCanvas? = null
	var sentence_canvas: SentenceCanvas? = null
	var lemain_canvas: LessonMainCanvas? = null
	var pupil_canvas: PupilCanvas? = null
	var base_canvas: BaseCanvas? = null
	var edit = false
	var static_lesson: Lesson? = null
	var omega_settings_dialog = OmegaSettingsDialog()

	init {
	    omega_settings_dialog.isVisible = false
	}

	val isEditMode: Boolean
	    get() = static_lesson!!.run_mode == 'e'.code

	fun initColors(hm: HashMap<String?, Color?>) {
	    hm["bg_t"] = Color(240, 220, 140)
	    hm["bg_m"] = Color(210, 180, 220)
	    hm["bg_b"] = Color(140, 220, 240)
	    hm["bt_bg"] = Color(240, 220, 140)
	    hm["bt_hi"] = Color(240, 220, 140)
	    hm["bt_hs"] = Color(255, 240, 180)
	    hm["bt_fr"] = Color(0, 0, 0)
	    hm["bt_tx"] = Color(0, 0, 0)
	    hm["bt_fr_hi"] = Color(0, 0, 0)
	    hm["bt_tx_hi"] = Color(0, 0, 0)
	    hm["bt_fr_hs"] = Color(0, 0, 0)
	    hm["bt_tx_hs"] = Color(0, 0, 0)
	    hm["sn_bg"] = Color(240, 220, 140)
	    hm["sn_hi"] = Color(240, 220, 140)
	    hm["sn_fr"] = Color(0, 0, 0)
	    hm["sn_tx"] = Color(0, 0, 0)
	}

	fun getColors(fname: String?, who: String): HashMap<String?, Color?>? {
	    //	OmegaContext.sout_log.getLogger().info(":--: " + "restore " + fname + ' ' + who);
	    val el = restore(fname!!) ?: return null
	    for (i in 0..99) {
		val fel = el.findElement("canvas", i) ?: return null
		val name = fel.findAttr("name")
		//	    OmegaContext.sout_log.getLogger().info(":--: " + "found " + name);
		if (who == name) {
		    try {
			val hm: HashMap<String?, Color?> = HashMap()
			initColors(hm)
			hm.keys.forEach {k ->
			    val col = hm[k]
			    val c = fel.findAttr("color_$k")

			    //			OmegaContext.sout_log.getLogger().info(":--: " + "found " + k + ' ' + col + ' ' + c);
			    if (c != null) {
				//			    OmegaContext.sout_log.getLogger().info(":--: " + "col " + k + ' ' + col + ' ' + c);
				if (c[0] == '#') {
				    var rgb: Int
				    rgb = if (c.length == 9) {
					c.substring(3).toInt(16)
				    } else {
					c.substring(1).toInt(16)
				    }
				    hm[k] = Color(rgb)
				}
			    }
			}
			return hm
		    } catch (ex: Exception) {
			global_skipF(true)
			JOptionPane.showMessageDialog(
				ApplContext.top_frame,
				"""
			    	${t("Can't create from file ")}
			    	$ex
			    	""".trimIndent()
			)
			ex.printStackTrace()
			global_skipF(false)
		    }
		}
	    }
	    return null
	}

	var inExecHbox = false
	var mistNoMouse = false
	var cnt_hit_keyOrButton = 0
	var hit_key = 0
	var skip_F = false
	fun global_skipF(b: Boolean) {
	    skip_F = b
	}

	var CnT = 200
    }
}
