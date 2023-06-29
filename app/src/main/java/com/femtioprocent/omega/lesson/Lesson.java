package com.femtioprocent.omega.lesson;

// has UTF-8 ¬ß

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.adm.register.data.*;
import com.femtioprocent.omega.appl.OmegaAppl;
import com.femtioprocent.omega.appl.OmegaStartManager;
import com.femtioprocent.omega.appl.Splash;
import com.femtioprocent.omega.lesson.actions.ActionI;
import com.femtioprocent.omega.lesson.actions.AnimAction;
import com.femtioprocent.omega.lesson.actions.MpgAction;
import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.lesson.appl.ApplLesson;
import com.femtioprocent.omega.lesson.appl.LessonRuntime;
import com.femtioprocent.omega.lesson.canvas.*;
import com.femtioprocent.omega.lesson.canvas.result.ResultDialogTableSummary;
import com.femtioprocent.omega.lesson.machine.Item;
import com.femtioprocent.omega.lesson.machine.Machine;
import com.femtioprocent.omega.lesson.machine.Target;
import com.femtioprocent.omega.lesson.machine.Target.T_Item;
import com.femtioprocent.omega.lesson.managers.movie.LiuMovieManager;
import com.femtioprocent.omega.lesson.pupil.Pupil;
import com.femtioprocent.omega.lesson.repository.LessonItem;
import com.femtioprocent.omega.lesson.repository.Restore;
import com.femtioprocent.omega.lesson.repository.Save;
import com.femtioprocent.omega.lesson.settings.OmegaSettingsDialog;
import com.femtioprocent.omega.lesson.settings.PupilSettingsDialog;
import com.femtioprocent.omega.media.audio.APlayer;
import com.femtioprocent.omega.media.audio.TTS;
import com.femtioprocent.omega.media.video.VideoUtil;
import com.femtioprocent.omega.message.Listener;
import com.femtioprocent.omega.swing.filechooser.ChooseColorFile;
import com.femtioprocent.omega.swing.filechooser.ChooseLessonFile;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.Log;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Values;
import com.femtioprocent.omega.xml.Element;

import javax.print.PrintService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterJob;
import java.io.*;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

import static com.femtioprocent.omega.util.SundryUtils.empty;

public class Lesson implements LessonCanvasListener {

    boolean globalExit = false;

    public LessonContext l_ctxt;
    int run_mode = 'p';
    public static HashMap story_hm = new HashMap();

    static {
	story_hm.put("sentence_list", new SentenceList());
	// special entry med list sentence
    }

    Log audio_log = OmegaContext.def_log;
    Log lesson_log = OmegaContext.def_log;
    Log msg_log = OmegaContext.def_log;

    public static LessonCanvas le_canvas;
    public static SentenceCanvas sentence_canvas;
    public static LessonMainCanvas lemain_canvas;
    public static PupilCanvas pupil_canvas;
    static BaseCanvas base_canvas;
    Machine machine;
    Window window;
    private CardLayout card;
    private JPanel card_panel;
    public static boolean edit;
    LessonEditorPanel lep;
    public static Lesson static_lesson;
    String saved_name;
    String loaded_fname;
    ActionI action = null;
    MpgAction mpg_action = null;
    Element element_root = null;
    Element element_root2 = null;
    Sequencer seq;
    ResultDialogTableSummary rdlg;
    boolean is_testing;
    private final int TM_CREATE = 0;
    private final int TM_RAND = 1;
    private final int TM_PRE_1 = 10;
    private final int TM_PRE_2 = 11;
    private final int TM_POST_1 = 20;
    private final int TM_POST_2 = 21;
    private final int TMG_CREATE = 0;
    private final int TMG_TEST = 1;
    private int current_test_mode = TM_CREATE;
    private int current_test_mode_group = mkTestModeGroup(current_test_mode);
    LessonItem litm = null;
    private Pupil current_pupil;
    long session_length_start = SundryUtils.ct();
    public String lessonLang = null;

    public class PlayDataList {
    	int ord;
    	Date date;
    	public ArrayList<Object> arr = new ArrayList<>();

	public int nextOrd() {
	    if ( ord != 0 )
	        return ord;
	    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	    String ordinal = prefs.get("story_ordinal", "0");
	    ord = Integer.parseInt(ordinal);
	    ord++;
	    prefs.put("story_ordinal", "" + ord);
	    return ord;
	}

	public void add(PlayData play_data) {
	    arr.add(play_data);
	    date = new Date();
	}
    }

    private LiuMovieManager signMoviePrepare(Target tg, int tg_ix) {
	System.out.println("Liu Prepare sign movie");
	Rectangle tgr = le_canvas.getTargetRectangle(tg_ix);
	LiuMovieManager lmm = new LiuMovieManager(window, le_canvas);
	final T_Item t_Item = tg.getT_Item(tg_ix);
	String smfName = lmm.getSignMovieFileName(t_Item.item, tg, tg_ix);
	if (smfName != null) {
	    boolean liuMovieOk = lmm.prepare(OmegaContext.media(), smfName, true);
	    if (!liuMovieOk) {
		System.err.println("sign move not OK");
		return null;
	    }
	} else {
	    System.err.println("sign move not OK'");
	    return null;
	}
	System.err.println("Sign movie ok");
	return lmm;
    }

    private void playSignFile(Target tg, boolean editMode) {
	LiuMovieManager lmm = new LiuMovieManager(window, le_canvas);
	try {

	    final Color bgCol = omega_settings_dialog.signSentence_background.color;
	    final int alphaCol = omega_settings_dialog.signSentence_alpha.getValue();
	    int sms = omega_settings_dialog.signMovieSentence_scale.getValue();
	    if (sms == 0) {
		sms = 1;
	    }

	    String all_text = tg.getAllText();
	    boolean specificSignMovie = action_specific.isSign(all_text);
	    if (specificSignMovie) {
		try {
		    if (false && all_text.startsWith("MMMM ")) {
			int cnt = 0;
			File dir = new File("../../TextbitarB-F-");
			for (File f : dir.listFiles()) {
			    String sign_mv = f.getName();
			    if (false) {
				if (lmm.prepare(dir.getAbsolutePath() + "/", sign_mv, true)) {
				    Shape sh = le_canvas.getTargetShape();
				    Rectangle tgr = sh.getBounds();
				    startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2);
				    lmm.cleanup();
				    if (cnt++ > 10) {
					break;
				    }
				}
			    } else {
			    }
			}
		    }
		    String specificMovieName = action_specific.getSign(all_text);
		    String sign_mv = specificMovieName;
		    if (lmm.prepare("", sign_mv, true)) {
			Shape sh = le_canvas.getTargetShape();
			Rectangle tgr = sh.getBounds();
			startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2);
		    }
		} catch (Exception ex_mv) {
		    OmegaContext.sout_log.getLogger().info("While play movie: " + ex_mv);
		}
	    } else {
		String smFname = OmegaContext.getMediaFile("sign-" + OmegaContext.getLessonLang() + "/" + all_text + ".mp4");
		String smFileName = VideoUtil.findSupportedFname(smFname);
		File smFile = smFileName == null ? null : new File(smFileName);
		if (smFile != null && smFile.exists() && smFile.canRead()) {
		    try {
			le_canvas.setMarkTargetAll();
			if (lmm.prepare("", smFname, true)) {
			    Shape sh = le_canvas.getTargetShape();
			    Rectangle tgr = sh.getBounds();
			    startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2);
			    lmm.cleanup();
			}
		    } catch (Exception ex_mv) {
			OmegaContext.sout_log.getLogger().info("While play movie: " + ex_mv);
		    } finally {
		    }
		} else {
		    // word by word
		    ArrayList<String> sign_movies = tg.getAllSignMovies(lmm);
		    int tg_ix = 0;
		    for (String sign_mv : sign_movies) {
			try {
			    le_canvas.setMarkTarget(tg_ix, true);
			    if (sign_mv.length() > 0 && lmm.prepare(OmegaContext.media(), sign_mv, true)) {
				Shape sh = le_canvas.getTargetShape();
				Rectangle tgr = sh.getBounds();
				startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 2);
				lmm.cleanup();
			    }
			} catch (Exception ex_mv) {
			    OmegaContext.sout_log.getLogger().info("While play movie: " + tg_ix + ' ' + ex_mv);
			} finally {
			    tg_ix++;
			}
		    }
		}
	    }
	} finally {
	    mistNoMouse = false;
	    le_canvas.setMist(0, null, null, 0);
	    lmm.cleanup();
	}
    }

    private void startMovieAndWait(LiuMovieManager lmm, Rectangle tgr, final Color bgCol, final int alphaCol, int scale, int mistMode) {
	mistNoMouse = true;
	le_canvas.setMist(mistMode, tgr, bgCol, alphaCol);
	Rectangle mRect = lmm.start((int) (tgr.getX() + tgr.getWidth() / 2), (int) (tgr.getY() + tgr.getHeight()), 100 / scale);
	le_canvas.setSignMovieRectangle(mRect);
	lmm.wait((int) (tgr.getX() + tgr.getWidth() / 2), (int) (tgr.getY() + tgr.getHeight()), 100.0 / scale);
    }

    static class SentenceList implements Serializable {

	ArrayList sentence_list;
	String lesson_name;

	SentenceList() {
	    sentence_list = new ArrayList();
	    lesson_name = "";
	}

	public String toString() {
	    return lesson_name + ':' + sentence_list;
	}
    }

    public class RegisterProxy {

	public Pupil pupil;
	public RegLocator rl;
	ResultTest rt;
	long started;
	long last_ct;
	String lesson_name;
	String test_mode;
	Log register_log = OmegaContext.def_log;
	boolean has_shown = false;

	RegisterProxy(Pupil pupil) {
	    //log OmegaContext.sout_log.getLogger().info("ERR: " + "PUPIL REG created ");
	    this.pupil = pupil;
	    this.lesson_name = null;
	    this.test_mode = null;
	    rl = new RegLocator();
	    rt = null;
	}

	RegisterProxy(Pupil pupil, String lesson_name, int test_mode) {
	    //log OmegaContext.sout_log.getLogger().info("ERR: " + "PUPIL REG created ");
	    this.pupil = pupil;
	    this.lesson_name = lesson_name;
	    this.test_mode = getTestModeString(test_mode);
	    rl = new RegLocator();
	    rt = new ResultTest(pupil.getName(), fix(lesson_name), this.test_mode);
	    started = SundryUtils.ct();
	}

	void setStarted() {
	    last_ct = started = SundryUtils.ct();
	    register_log.getLogger().info("started " + started);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + " STARTED ");
	}

	String getTestModeString(int test_mode) {
	    switch (test_mode) {
		case TM_CREATE:
		    return "create";
		case TM_RAND:
		    return "test";
		case TM_PRE_1:
		    return "pre1";
		case TM_PRE_2:
		    return "pre2";
		case TM_POST_1:
		    return "post1";
		case TM_POST_2:
		    return "post2";
		default:
		    return "X";
	    }
	}

	String fix(String lesson_name) {
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "fix " + lesson_name);
	    int ix = lesson_name.lastIndexOf('/');
	    String s = lesson_name.substring(0, ix);
	    ix = s.lastIndexOf('/');
	    s = s.substring(ix + 1);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "fix -> " + s);
	    return s;
	}

	void restart() {
	    register_log.getLogger().info("restart ");
	    has_shown = false;
	}

	void word(String mode, long when_ct, String word, String l_id) {
	    //log OmegaContext.sout_log.getLogger().info("ERR: " + "PUPIL REG " + mode + ' ' + word);
	    int when = (int) (when_ct - last_ct);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "WHEN " + (when_ct - SundryUtils.ct()));
	    last_ct = when_ct;
	    rt.add(new SelectEntry(mode, word, when, l_id));
	    register_log.getLogger().info("word: " + mode + ' ' + word + ' ' + l_id);
	    //	    semiclose();
	    has_shown = false;
	}

	void test(String mode,
		  long when_ct,
		  String sentence,
		  String answer,
		  String correct_words,
		  String l_id_list) {
	    //log OmegaContext.sout_log.getLogger().info("ERR: " + "PUPIL REG " + mode + ' ' + sentence + ' ' + answer);
	    int when = (int) (when_ct - started);
	    rt.add(new TestEntry(mode,
		    sentence,
		    answer,
		    when,
		    correct_words,
		    l_id_list));
	    register_log.getLogger().info("test: " + mode + ' ' + answer + ' ' + sentence);
	    //	    semiclose();
	    has_shown = false;
	}

	void create(String mode,
		    long when_ct,
		    String sentence,
		    int duration,
		    String l_id_list) {
	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "PUPIL REG " + mode + ' ' + sentence + ' ' + duration);
	    int when = (int) (when_ct - started);
	    rt.add(new CreateEntry(sentence, when, l_id_list));
	    register_log.getLogger().info("creat: " + mode + ' ' + sentence);
	    //	    semiclose();
	    has_shown = false;
	}

	// 	void semiclose() {
	// 	    SaveRestore sr = new SaveRestore();
	// 	    sr.save(rl.mkResultsFName(pupil.getName(), rt.mkFname()), rt);
	// 	}
	void close() {
	    int cnt = rt.howManyTestEntries();
	    if (cnt > 0) {
		SaveRestore sr = new SaveRestore();
		rt.session_length = SundryUtils.ct() - session_length_start;
		if (sr.save(rl.mkResultsFName(pupil.getName(), rt.mkFname(getCurrentPupil().getName())), rt) == false) {
		    register_log.getLogger().info("failed saved: " + rl.mkResultsFName(pupil.getName(), rt.mkFname(getCurrentPupil().getName())));
		} else {
		    register_log.getLogger().info("saved: " + rl.mkResultsFName(pupil.getName(), rt.mkFname(getCurrentPupil().getName())));
		}
	    } else {
		register_log.getLogger().info("saved: -empty-");
	    }
	}

	public String[] getAllTestsAsName(String[] with) {
	    String[] sa = rl.getAllResultsFName(pupil.getName(), with);
	    for (int i = 0; i < sa.length; i++) {
		String s = sa[i];
		s = s.replace('\\', '/');
		s = s.substring(0, s.lastIndexOf('.'));
		s = s.substring(s.lastIndexOf('/') + 1);
		sa[i] = s;
	    }
	    Arrays.sort(sa, new Comparator() {
		public int compare(Object o1, Object o2) {
		    String s1 = (String) o1;
		    String s2 = (String) o2;
		    return s2.compareTo(s1);
		}
	    });
	    return sa;
	}

	public String toString() {
	    return "RegPr" + has_shown;
	}
    }

    RegisterProxy register;

    class Canvases {

	HashMap hm;

	Canvases() {
	    hm = new HashMap();
	}

	java.util.Set keySet() {
	    return hm.keySet();
	}

	BaseCanvas get(String id) {
	    return (BaseCanvas) hm.get(id);
	}

	void put(String id, BaseCanvas lbc) {
	    hm.put(id, lbc);
	}
    }

    Canvases canvases = new Canvases();

    private class Progress {

	JFrame f;
	JProgressBar progressBar;

	Progress() {
	    f = new JFrame(T.t("Loading"));
	    Container con = f.getContentPane();
	    progressBar = new JProgressBar();
	    //when the task of (initially) unknown length begins:
	    progressBar.setIndeterminate(true);
	    con.add(progressBar);
	    f.pack();
	    Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = scr.width / 2;
	    int y = scr.height / 2;
	    f.setLocation(x - 100, y);
	    f.setVisible(true);
	}

	void show() {
	    progressBar.setValue(0);
	    progressBar.setIndeterminate(true);
	    f.setVisible(true);
	}

	void dismiss() {
	    f.setVisible(false);
	    progressBar.setIndeterminate(false);
	}
    }

    Progress progress = null;

    //     private class AudioPrefetch {
// 	private HashMap prfHM;
// 	private ObjectCache audio_prefetch_cache = new ObjectCache("audioPrefetch");
// 	AudioPrefetch() {
// 	    loadit();
// 	}
// 	private void prefetchAny(String id) {
// 	    String[] sa = (String[])prfHM.get(id);
// 	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "prefetched " + sa);
// 	    if ( sa == null )
// 		return;
// 	    for(int i = 0; i < sa.length;i++) {
// 		String as = sa[i];
// 		APlayer.prefetch(as);
// 		//		OmegaContext.sout_log.getLogger().info("ERR: " + "prefetched " + as);
// 	    }
// 	}
// 	public void saveit(String lesson_fn, String[] used_file) {
// 	    fpdo.sundry.Timer tm = new fpdo.sundry.Timer();
// 	    prfHM.put(lesson_fn, used_file);
// 	    audio_prefetch_cache.save(prfHM);
// 	    audio_log.getLogger().info("saveit: " + prfHM + ' ' + tm.get());
// 	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "saveit " + prfHM + ' ' + tm.get());
// 	}
// 	private void loadit() {
// 	    prfHM = (HashMap)audio_prefetch_cache.load();
// 	    if ( prfHM == null )
// 		prfHM = new HashMap();
// 	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "PREFETCH CACHE " + prfHM);
// 	    Iterator it = prfHM.keySet().iterator();
// 	    while(it.hasNext()) {
// 		String id = (String)it.next();
// 		String[] afn = (String[])prfHM.get(id);
// 		//		OmegaContext.sout_log.getLogger().info("ERR: " + "PC " + id + ' ' + SundryUtils.a2s(afn));
// 	    }
// 	}
//     }
//     private AudioPrefetch audio_prefetch = new AudioPrefetch();
    String genSeqKey(int a) {
	return SundryUtils.pL(a, 6, '0');
    }

    class ListAndIterator {

	java.util.List list;
	ListIterator it;

	ListAndIterator(java.util.List li) {
	    list = li;
	    it = list.listIterator();
	}
    }

    class Sequencer {

	int index;
	Element el;
	int rand_map[];
	private HashMap texts;
	String current;
	int cnt_sent_correct;
	int cnt_sent_wrong;
	int cnt_word_correct;
	int cnt_word_wrong;
	int cnt_wordlast_correct;
	int cnt_wordlast_wrong;
	private String t_id[] = new String[]{"pre", "post"};

	Sequencer(Element el) {
	    index = 0;
	    rand_map = null;
	    texts = new HashMap();
	    buildTestSentence(el);
	    current = null;
	}

	String getStat(boolean b) {
	    return (b ? "s 1" : "s 0") + ";w +" + cnt_wordlast_correct + " -" + cnt_wordlast_wrong;
	}

	String genKey(int test_mode) {
	    return "" + test_mode;
	}

	String getTestText(int test_mode, boolean full) {
	    String s = getTestText(test_mode);
	    if (s == null) {
		return s;
	    }
	    if (full) {
		return s;
	    }
	    return s.replaceAll("\\{[^\\{\\}]*\\}", "");
	}

	Set<String> getTestTextSet(String txt, int test_mode, boolean full) {
	    Set<String> set = new HashSet<>();

	    String s = getTestText(test_mode);
	    if (s == null) {
		; //return s;
	    }
	    if (full) {
		s = s;
	    }
	    s = s.replaceAll("\\{[^\\{\\}]*\\}", "");
	    set.add(s);

	    return set;
	}

	private String getTestText(int test_mode) {
	    if (current != null) {
		return current;
	    }

	    if (test_mode == TM_RAND) {
		String saAll[] = l_ctxt.getLessonCanvas().getAllTargetCombinations();
		String sa[] = removeOff(action_specific, saAll);

		if (rand_map == null || rand_map.length != sa.length) {
		    rand_map = SundryUtils.upTo(sa.length);
		    SundryUtils.scrambleArr(rand_map);
		}
		try {
		    return sa[rand_map[index]];
		} catch (Exception ex) {
		}
		return null;
	    } else {
		ListIterator it = getIterator(test_mode);

		if (it == null) {
		    return null;
		}

		String test_text;
		if (it.hasNext()) {
		    String s = (String) it.next();
		    it.previous();
		    //log 		    OmegaContext.sout_log.getLogger().info("ERR: " + "test text " + s);
		    int ix = s.indexOf(':');
		    if (ix != -1) {
			test_text = s.substring(ix + 1);
		    } else {
			test_text = s;
		    }
		} else {
		    test_text = null;
		}
		return test_text;
	    }
	}

	private String[] removeOff(ActionSpecific action_specific, String[] saAll) {
	    int nOff = 0;
	    for(Object o : action_specific.hm.values()) {
	        String s = (String)o;
		if (s != null && s.length() > 0 && s.equalsIgnoreCase("!off"))
		    nOff++;
	    }
	    int n = saAll.length - nOff;
	    String[] sa = new String[n];
	    int ix = 0;
	    for(String s : saAll) {
		String k = s;
		String v = (String) action_specific.hm.get(k);
		if (v == null || (v.length() > 0 && !v.equalsIgnoreCase("!off"))) {
		    sa[ix++] = k;
		}
	    }
	    return sa;
	}

	ListIterator getIterator(int test_mode) {
	    String key = genKey(test_mode);
	    ListAndIterator lai = (ListAndIterator) texts.get(key);
	    if (lai == null) {
		return null;
	    }
	    return lai.it;
	}

	ListIterator getNewIterator(int test_mode) {
	    String key = genKey(test_mode);
	    ListAndIterator lai = (ListAndIterator) texts.get(key);
	    if (lai == null) {
		return null;
	    }
	    return lai.list.listIterator();
	}

	java.util.List getList(int test_mode) {
	    String key = genKey(test_mode);
	    ListAndIterator lai = (ListAndIterator) texts.get(key);
	    if (lai == null) {
		return null;
	    }
	    return lai.list;
	}

	void initNewTest() {
	    rand_map = null;
	    index = 0;
	    texts = new HashMap();
	    current = null;
	}

	boolean next(int test_mode) {
	    cnt_wordlast_wrong = 0;
	    cnt_wordlast_correct = 0;

	    if (test_mode == TM_RAND) {
		index++;
		return getTestText(test_mode, false) != null;
	    }
	    current = null;
	    Iterator it = getIterator(test_mode);
	    if (it == null) {
		return false;
	    }
	    if (it.hasNext()) {
		it.next();
		return true;
	    }
	    return false;
	}

	int[][] getTestMatrix(String[] all_sentence) {
	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "###### getTestMatrix");

	    int[][] tmm = new int[all_sentence.length][4];

	    int[] map = new int[]{TM_PRE_1,
		    TM_PRE_2,
		    TM_POST_1,
		    TM_POST_2};

	    for (int i = 0; i < 4; i++) {
		int t_mode = map[i];

		ListIterator it = getNewIterator(t_mode);
		if (it == null) {
		    continue;
		}

		while (it.hasNext()) {
		    String s = (String) it.next();
		    String[] sa = SundryUtils.split(s, ":");
		    int ord = 1 + Integer.parseInt(sa[0]);
		    String txt = sa[1];
		    int ix2 = where(txt, all_sentence);
		    if (ix2 >= 0) {
			tmm[ix2][i] = ord;
		    }
		}
	    }
	    return tmm;
	}

	void setFromMatrix(String sentA[], int[][] tmm) {
	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "###### setFromMatrix");

	    int len = tmm.length;
	    if (len > 0) {
		for (int i = 0; i < tmm[0].length; i++) {
		    java.util.List li = new ArrayList();
		    int t_mode = ((i / 2) + 1) * 10 + (i % 2);
		    for (int j = 0; j < len; j++) {
			int ord = tmm[j][i];
			if (ord > 0) {
			    String txt = sentA[j];

			    //log 			    OmegaContext.sout_log.getLogger().info("ERR: " + "--- dep_set seq " + j + ' ' + ord + ' ' + txt);

			    int ord_i = ord - 1;
			    li.add(genSeqKey(ord_i) + ':' + txt);
			}
		    }

		    texts.put(genKey(t_mode), new ListAndIterator(li));
		}
	    }
	}

	void buildTestSentence(Element el) {
	    if (this.el == el) {
		return;
	    }

	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "###### buildTestSent");

	    Element t_el = el.findElement("test_entries", 0);
	    if (t_el != null) {
		for (int i = 0; i < 10; i++) {
		    Element ty_el = t_el.findElement("test_entry", i);
		    if (ty_el != null) {
			String a_type = (String) ty_el.findAttr("type");
			String a_ord = (String) ty_el.findAttr("ord");

			int t_mode = a_type.equals(t_id[0]) ? 10 : 20;
			t_mode += a_ord.equals("0") ? 0 : 1;

			java.util.List li = new ArrayList();

			for (int ii = 0; ii < 100; ii++) {
			    Element s_el = ty_el.findElement("sentence", ii);
			    if (s_el == null) {
				break;
			    }
			    String ord_s = (String) s_el.findAttr("ord");
			    int ord_i = Integer.parseInt(ord_s);
			    String text = (String) s_el.findAttr("text");

			    li.add(genSeqKey(ord_i) + ':' + text);

			    //log 			    OmegaContext.sout_log.getLogger().info("ERR: " + "sent li " + li);
			}

			li = sortList(li);
			texts.put(genKey(t_mode), new ListAndIterator(li));
		    }
		}
		this.el = el;
		//log 		OmegaContext.sout_log.getLogger().info("ERR: " + "saved test sent " + el);
	    }
	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "++++++++++++++++++++++ " + texts);
	}

	Element getElement() {
	    Element el = new Element("test_entries");

	    for (int i = 0; i < 2; i++) {
		for (int j = 0; j < 2; j++) {
		    int t_mode = 10 * (i + 1) + j;
		    java.util.List li = getList(t_mode);

		    if (li != null && li.size() > 0) {
			String[] sa = (String[]) li.toArray(new String[0]);
			Arrays.sort(sa);

			Element te_el = new Element("test_entry");
			te_el.addAttr("type", t_id[i]);
			te_el.addAttr("ord", "" + j);

			for (int ii = 0; ii < sa.length; ii++) {
			    String s = sa[ii];
			    String[] sa2 = SundryUtils.split(s, ":");
			    int ord = Integer.parseInt(sa2[0]);
			    String txt = sa2[1];

			    Element s_el = new Element("sentence");
			    s_el.addAttr("ord", "" + (ord));
			    s_el.addAttr("text", txt);

			    te_el.add(s_el);
			}

			el.add(te_el);
		    }
		}
	    }
	    return el;
	}

	void dump() {
	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "Seq: texts = " + texts);
	}

	public String toString() {
	    return "seq{"
		    + getTestText(current_test_mode, false)
		    + "}";
	}
    }

    java.util.List sortList(java.util.List li) {
	String[] sa = (String[]) li.toArray(new String[0]);
	Arrays.sort(sa);
	ArrayList nli = new ArrayList();
	for (int i = 0; i < sa.length; i++) {
	    nli.add(sa[i]);
	}
	return nli;
    }

    Pupil getCurrentPupil() {
	if (pupil_canvas == null) {
	    if (current_pupil == null) {
		current_pupil = new Pupil("Guest");
	    }
	    return current_pupil;
	}
	String pupil_name = pupil_canvas.getPupilName();
	if (current_pupil != null && pupil_name.equals(current_pupil.getName())) {
	    return current_pupil;
	}
	current_pupil = new Pupil(pupil_name);

	return current_pupil;
    }

    private void setCurrentPupil(Pupil pupil) {
	current_pupil = pupil;
    }

    public static OmegaSettingsDialog omega_settings_dialog = new OmegaSettingsDialog();

    static {
	omega_settings_dialog.setVisible(false);
    }

    PupilSettingsDialog pupil_settings_dialog = new PupilSettingsDialog(this);
    int last_ord = -1;

    void act_performLesson(String msg) {
	story_hm.put("sentence_list", new SentenceList());
	OmegaContext.story_log.getLogger().info("mew sentL 619 " + story_hm);
	last_story_flag = false;
	try {
	    String s = msg.substring(12);
	    String sn = msg.substring(19);
	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "Load lesson " + s);
	    int ord = Integer.parseInt(sn);
	    BaseCanvas.ImageAreaJB ima = lemain_canvas.lesson[ord];
	    if (ima == null) {
		return;
	    }
	    litm = (LessonItem) (ima.o);
	    if (litm == null) {
	    } else if (litm.isDir()) {
		//log 		    OmegaContext.sout_log.getLogger().info("ERR: " + "Load group " + s);
		lemain_canvas.setRedPush(ord);
		lemain_canvas.addLessonBase(litm.getLessonName(), ord);
		lemain_canvas.requestFocusOrd(0);
	    } else {
		lemain_canvas.setRedPush(ord);
		lemain_canvas.tellLessonBase(litm.getLessonName(), ord);
		String lesson_name = litm.getDefaultLessonFile();
		Log.getLogger().info("Found lesson: " + lesson_name);
		if ( lesson_name == null ) {
		    JOptionPane.showMessageDialog(ApplContext.top_frame,
			    T.t("Can't find lesson file"));
		    return;
		}
		loadTest(lesson_name);
		last_ord = ord;
		//		    }
	    }
	} catch (Exception ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "Exception in performLesson: " + ex);
	    ex.printStackTrace();
	}
    }

    public int mkTestModeGroup(int test_mode) {
	switch (test_mode) {
	    case TM_CREATE:
		return TMG_CREATE;
	    case TM_RAND:
	    case TM_PRE_1:
	    case TM_PRE_2:
	    case TM_POST_1:
	    case TM_POST_2:
		return TMG_TEST;
	}
	throw new Error("no TMG");
    }

    public boolean isTestModeGroup(int test_mode, int test_mode_group) {
	int tmg = mkTestModeGroup(test_mode);
	return tmg == test_mode_group;
    }

    void prepareTest(String mode) {
	session_length_start = SundryUtils.ct();
	//log 	OmegaContext.sout_log.getLogger().info("ERR: " + "prepare test " + mode);
	try {
	    if ("create".equals(mode)) {
		current_test_mode = TM_CREATE;
		return;
	    }
	    if ("pupil_1".equals(mode)) {
		current_test_mode = TM_RAND;
		return;
	    }
	    if ("pre_1".equals(mode)) {
		current_test_mode = TM_PRE_1;
		return;
	    }
	    if ("pre_2".equals(mode)) {
		current_test_mode = TM_PRE_2;
		return;
	    }
	    if ("post_1".equals(mode)) {
		current_test_mode = TM_POST_1;
		return;
	    }
	    if ("post_2".equals(mode)) {
		current_test_mode = TM_POST_2;
		return;
	    }
	} finally {
	    current_test_mode_group = mkTestModeGroup(current_test_mode);
	    lemain_canvas.reload();
	    lemain_canvas.setModeIsTest(current_test_mode_group == TMG_TEST);
	}
    }

    void loadTest(String fn) {
	String lfn = fn; //loaded_fname;

	// 	OmegaContext.sout_log.getLogger().info("ERR: " + "loadTest >>> test_mode = " + current_test_mode);

	register = new RegisterProxy(getCurrentPupil(), fn, current_test_mode);

	le_canvas.disposeOldLesson();

	switch (current_test_mode) {
	    case TM_CREATE:
		card_show("words");
		if (register != null) {
		    register.restart();
		}
		sendMsg("create", lfn, "loadTest1");
		break;
	    case TM_RAND:
		card_show("anim1");
		sendMsg("new_test", lfn, "loadTest2");
		break;
	    case TM_PRE_1:
	    case TM_PRE_2:
	    case TM_POST_1:
	    case TM_POST_2:
		card_show("anim1");
		sendMsg("new_test", lfn, "loadTest3");
		break;
	}
	//log 	OmegaContext.sout_log.getLogger().info("ERR: " + "loadTest <<<");
	le_canvas.hideMsg();
    }

    public MsgItem getResultSummary_MsgItem() {
	OmegaContext.sout_log.getLogger().info("ERR: " + "getRslt " + register);

	if (register != null && register.has_shown) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "2006: register " + register + ' ' + (register != null ? register.has_shown : false));
	    return null;
	}

	try {
	    return new MsgItem('S',
		    T.t("Test Statistics"),
		    T.t("Correct") + ": "
			    + seq.cnt_sent_correct + " "
			    + fixSP(T.t("sentence "), T.t("sentences"), seq.cnt_sent_correct)
			    + " ("
			    + seq.cnt_word_correct + " "
			    + fixSP(T.t("word"), T.t("words"), seq.cnt_word_correct) + ")",
		    T.t("Wrong") + ": "
			    + seq.cnt_sent_wrong + " "
			    + fixSP(T.t("sentence "), T.t("sentences"), seq.cnt_sent_wrong)
			    + " ("
			    + seq.cnt_word_wrong + " "
			    + fixSP(T.t("word"), T.t("words"), seq.cnt_word_wrong) + ")",
		    getCurrentPupil().getImageName(),
		    getCurrentPupil().getImageNameWrongAnswer(),
		    null);
	} catch (Exception ex) {
	    return null;
	}
    }

    Listener om_msg_li = new Listener() {
	public void msg(String msg) {
	    msg_log.getLogger().info("====M=== message " + msg);

	    if (msg.startsWith("imarea main:")) {
		act_performLesson(msg);
		lemain_canvas.requestFocus();
		return;
	    }

	    if (msg.startsWith("button main:quit")) {
		act_main_quit();
		return;
	    }

	    if (msg.startsWith("button main:read_story")) {
		sendMsg("read_story", null, "");
	    }

	    if (msg.startsWith("button sent:")) {
		String submsg = msg.substring(12);
		sendMsg("sent_" + submsg, null, "button");
	    }

	    if (msg.startsWith("button pupil:")) {
		String submsg = msg.substring(13);
		if (submsg.equals("admin")) {
		    return;
		}
		if (submsg.equals("settings")) {
		    act_pupil_settings();
		    return;
		}
		if (submsg.equals("result")) {
		    act_pupil_result();
		    return;
		}
		if (submsg.equals("pupil")) {
		    act_pupil_pupil();
		    return;
		}
		if (submsg.equals("create_p")) {
		    act_pupil_create_p();
		}
		if (submsg.equals("create_t")) {
		    act_pupil_create_t();
		}
		if (submsg.equals("test_t")) {
		    act_pupil_test_t();
		}
		if (submsg.equals("test_p")) {
		    act_pupil_test_p();
		}
		if (submsg.equals("quit")) {
		    globalExit = true;
		    if (ApplContext.top_frame instanceof LessonRuntime)
			System.exit(0);
		    return;
		}
	    }

	    if ("action".equals(msg)) {
		sendMsg("action", null, "L758 ");
	    }

	    if ("show_result".equals(msg)) {
		OmegaContext.sout_log.getLogger().info("ERR: " + "show_result " + register);
		sendMsg("show_result_msg", null, "exit_create");
	    }
	    if ("exit create".equals(msg)) {
		act_exit_create();
		return;
	    }
	    if ("playList".equals(msg)) {
		playFromDataList(play_data_list);
		play_data_list = new PlayDataList();
		play_data_list_is_last = new PlayDataList();
	    }
	    // 		OmegaContext.sout_log.getLogger().info("ERR: " + ">>>>>>> msg DONE " + msg);
	}

	private void act_pupil_test_p() {
	    prepareTest("pupil_1");
	    card_show("main");
	    lemain_canvas.setRedClear();
	    lemain_canvas.requestFocusOrd(0);
	}

	private void act_pupil_test_t() {
	    String[] choise = new String[]{T.t("Pre test 1"),
		    T.t("Pre test 2"),
		    T.t("Post test 1"),
		    T.t("Post test 2")
	    };

	    global_skipF(true);
	    int a = JOptionPane.showOptionDialog(ApplContext.top_frame,
		    T.t("What kind of test?"),
		    T.t("Omega - Test mode"),
		    JOptionPane.OK_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    null,
		    choise,
		    choise[0]);
	    global_skipF(false);

	    if (a == JOptionPane.CLOSED_OPTION) {
		return;
	    }

	    String[] choise2 = new String[]{"pre_1",
		    "pre_2",
		    "post_1",
		    "post_2"
	    };

	    prepareTest(choise2[a]);
	    //			OmegaContext.sout_log.getLogger().info("ERR: " + "choosen test " + a + ' ' + choise[a]);
	    card_show("main");
	    lemain_canvas.setRedClear();
	    lemain_canvas.requestFocusOrd(0);
	}

	private void act_pupil_create_t() {
	    prepareTest("create");
	    card_show("main");
	    lemain_canvas.setRedClear();
	    lemain_canvas.requestFocusOrd(0);
	}

	private void act_pupil_create_p() {
	    prepareTest("create");
	    card_show("main");
	    lemain_canvas.setRedClear();
	    lemain_canvas.requestFocusOrd(0);
	}

	private void act_pupil_settings() {
	    global_skipF(true);
	    //			String fn = "default.omega_colors";
	    //			fn = getCurrentPupil().getString("theme", fn);
	    //			omega_settings_dialog.setSelectedColorFile(fn);
	    omega_settings_dialog.setVisible(true);
	    global_skipF(false);
	}

	private void act_main_quit() {
	    String ds = lemain_canvas.getLessonBase();
	    if (ds == null) {
		card_show("pupil");
	    } else {
		//log 			    OmegaContext.sout_log.getLogger().info("ERR: " + "--- lesson base is now " + ds);
		int ix = ds.lastIndexOf('/');
		if (ix != -1) {
		    //log 				OmegaContext.sout_log.getLogger().info("ERR: " + "--> " + ds.substring(0, ix));
		    String lb = ds.substring(0, ix);
		    int sel_ix = lemain_canvas.setLessonBase(lb);
		} else {
		    //log 				OmegaContext.sout_log.getLogger().info("ERR: " + "--> " + null);
		    lemain_canvas.setLessonBase(null);
		}
		lemain_canvas.requestFocus();
	    }
	    int ord = lemain_canvas.setRedPop();
	}

	private void act_pupil_pupil() {
	    String pn = getCurrentPupil().getName();
	    //			OmegaContext.sout_log.getLogger().info("ERR: " + "---------- pupil " + pn);
	    pupil_settings_dialog.setPupil(getCurrentPupil());
	    global_skipF(true);
	    pupil_settings_dialog.setVisible(true);
	    global_skipF(false);
	    if (pupil_settings_dialog.was_deleted) {
		pupil_settings_dialog.was_deleted = false;
		pupil_canvas.mkList("Guest");
		setPupil("Guest");
		HashMap pparm = pupil_settings_dialog.getParams();
		getCurrentPupil().setParams(pparm);
	    } else {
		HashMap pparm = pupil_settings_dialog.getParams();
		getCurrentPupil().setParams(pparm);
		pupil_canvas.reloadPIM();
		restoreSettings();
	    }
	    if ("next".equals(getCurrentPupil().getString("space_key", "select"))) {
		OmegaConfig.setNextKey();
	    } else {
		OmegaConfig.setSelectKey();
	    }
	    String pupil_lang = getCurrentPupil().getStringNo0("languageSuffix", tryLessonLanguages(T.lang));
	    OmegaContext.lesson_log.getLogger().info("Retr pupil_lang: " + pupil_lang + ' ' + tryLessonLanguages(T.lang) + ' ' + T.lang);
	    OmegaContext.setLessonLang(pupil_lang);
	}

	private void act_exit_create() {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "sm1 " + current_card + ' ' + register);
	    // savePrefetch();
	    if (edit) {
		globalExit = true;
		sendMsg("exitLesson", "", "");
		//System.exit(0);
	    }
	    if (register != null) {
		register.close();
	    }
	    register = null;
	    card_show("main", 2);
	    lemain_canvas.setRedPop();
	    lemain_canvas.requestFocus();
	}

	private void act_pupil_result() throws HeadlessException {
	    if (rdlg == null) {
		rdlg = new ResultDialogTableSummary(ApplContext.top_frame);
	    }
	    try {
		setCurrentPupil(new Pupil(pupil_canvas.getPupilName()));
		register = new RegisterProxy(getCurrentPupil());
		//			    OmegaContext.sout_log.getLogger().info("ERR: " + "got reg " + register);
		rdlg.set(register);
		global_skipF(true);
		OmegaContext.HELP_STACK.push("result_summary");
		rdlg.setVisible(true);
		global_skipF(false);
	    } catch (Exception ex) {
		ex.printStackTrace();
		global_skipF(true);
		JOptionPane.showMessageDialog(ApplContext.top_frame,
			T.t("Can't find pupil result data"));
		global_skipF(false);
	    } finally {
		OmegaContext.HELP_STACK.pop("result_summary");
	    }
	}
    };

    static public boolean isEditMode() {
	return static_lesson.run_mode == 'e';
    }

    public Lesson(char run_mode) { // 'p', 't', 'a' or 'e'
	OmegaContext.lesson_log.getLogger().info("XX");

	String default_pupil;
	if (run_mode == 'e') {
	    default_pupil = "Guest";
	} else {
	    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	    default_pupil = prefs.get("default_pupil", "Guest");
	}
	setCurrentPupil(new Pupil(default_pupil));

	omega_settings_dialog.lesson = this;

	this.run_mode = run_mode;
	static_lesson = this;
	l_ctxt = new LessonContext(this);

	if (true) {
	    APlayer ap = APlayer.createAPlayer("audio/greeting.wav",
		    null,
		    null);
	    ap.play();
	}

	if (true) {
	    APlayer ap = APlayer.createAPlayer("audio/greeting2.mp3",
		    null,
		    null);
	    ap.play();
	}

	SundryUtils.m_sleep(3000);

	machine = new Machine(l_ctxt);

	le_canvas = new LessonCanvas(l_ctxt);
	canvases.put("words", le_canvas);

	sentence_canvas = new SentenceCanvas(l_ctxt);
	canvases.put("sent", sentence_canvas);

	lemain_canvas = new LessonMainCanvas(l_ctxt);
	canvases.put("main", lemain_canvas);

	pupil_canvas = new PupilCanvas(l_ctxt, getCurrentPupil().getName());
	if (run_mode == 'a') {
	    pupil_canvas.setBehaviour(PupilCanvas.BH_ADMINISTRATOR);
	} else if (run_mode == 'e') {
	    pupil_canvas.setBehaviour(PupilCanvas.BH_ADMINISTRATOR);
	} else if (run_mode == 'p') {
	    pupil_canvas.setBehaviour(PupilCanvas.BH_PUPIL);
	}

	canvases.put("pupil", pupil_canvas);

	sentence_canvas.addLessonCanvasListener(this);
	le_canvas.addLessonCanvasListener(this);
	lemain_canvas.addLessonCanvasListener(this);
	pupil_canvas.addLessonCanvasListener(this);

	// Server lessond = new Server();

	sentence_canvas.om_msg_mgr.addListener(om_msg_li);
	le_canvas.om_msg_mgr.addListener(om_msg_li);
	lemain_canvas.om_msg_mgr.addListener(om_msg_li);
	pupil_canvas.om_msg_mgr.addListener(om_msg_li);

	restoreSettings();
    }

    public void mact_New() {
	File file = new File(OmegaContext.omegaAssets("lesson-" + OmegaContext.getLessonLang() + "/new.omega_lesson")); // LESSON-DIR
	String url_s = Files.toURL(file);
	String tfn = Files.mkRelativeCWD(url_s);
	tfn = OmegaContext.antiOmegaAssets(tfn);
	loadFN(tfn);
	saved_name = null;
    }

    public void mact_Save() {
	if (saved_name != null) {
	    save(OmegaContext.omegaAssets(saved_name));
	} else {
	    mact_SaveAs();
	}
    }

    public void mact_SaveAs() {

	String url_s = null;
	try {
	    ChooseLessonFile choose_f = new ChooseLessonFile();
	    global_skipF(true);
	    int rv = choose_f.showDialog(null, T.t("Save"));
	    if (rv == JFileChooser.APPROVE_OPTION) {
		File file = choose_f.getSelectedFile();
		url_s = Files.toURL(file);
		if (!url_s.endsWith("." + ChooseLessonFile.ext)) {
		    url_s = url_s + "." + ChooseLessonFile.ext;
		}

		String tfn = Files.mkRelativeCWD(url_s);

		save(tfn);

		saved_name = tfn;

		if (window instanceof JFrame) {
		    ((JFrame) window).setTitle("Omega - Lesson Editor: " + OmegaContext.antiOmegaAssets(tfn));
		}
	    }
	} finally {
	    global_skipF(false);
	}
    }

    public void mact_Open() {
	String url_s = null;
	try {
	    ChooseLessonFile choose_f = new ChooseLessonFile(2);
	    global_skipF(true);
	    int rv = choose_f.showDialog(null, T.t("Open"));
	    if (rv == JFileChooser.APPROVE_OPTION) {
		File file = choose_f.getSelectedFile();
		choose_f.setLastFile(file);
		String s = "!";
		try {
		    s = file.getCanonicalPath();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		OmegaContext.sout_log.getLogger().info("mact_Open file: " + file + ' ' + s);
		url_s = Files.toURL(file);
		if (!url_s.endsWith("." + ChooseLessonFile.ext)) {
		    url_s = url_s + "." + ChooseLessonFile.ext;
		}

		String atfn = Files.mkRelativeCWD(url_s);
		String tfn = OmegaContext.antiOmegaAssets(atfn);
		loadFN(tfn);
		saved_name = tfn;
	    }
	} finally {
	    global_skipF(false);
	}
    }

    private void loadFN(String fn) {
	restoreSettings();
	sendMsg("load", fn, "loadFN");
    }

    private void save(String fn) {
	Element el = getElement();
	Save.save(fn, el);
    }

    static void initColors(HashMap hm) {
	hm.put("bg_t", new Color(240, 220, 140));
	hm.put("bg_m", new Color(210, 180, 220));
	hm.put("bg_b", new Color(140, 220, 240));
	hm.put("bt_bg", new Color(240, 220, 140));
	hm.put("bt_hi", new Color(240, 220, 140));
	hm.put("bt_hs", new Color(255, 240, 180));
	hm.put("bt_fr", new Color(0, 0, 0));
	hm.put("bt_tx", new Color(0, 0, 0));
	hm.put("bt_fr_hi", new Color(0, 0, 0));
	hm.put("bt_tx_hi", new Color(0, 0, 0));
	hm.put("bt_fr_hs", new Color(0, 0, 0));
	hm.put("bt_tx_hs", new Color(0, 0, 0));
	hm.put("sn_bg", new Color(240, 220, 140));
	hm.put("sn_hi", new Color(240, 220, 140));
	hm.put("sn_fr", new Color(0, 0, 0));
	hm.put("sn_tx", new Color(0, 0, 0));
    }

    static public HashMap getColors(String fname, String who) {
	//	OmegaContext.sout_log.getLogger().info("ERR: " + "restore " + fname + ' ' + who);
	Element el = Restore.restore(fname);
	if (el == null) {
	    return null;
	}

	for (int i = 0; i < 100; i++) {
	    Element fel = el.findElement("canvas", i);
	    if (fel == null) {
		return null;
	    }
	    String name = fel.findAttr("name");
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "found " + name);
	    if (who.equals(name)) {
		try {
		    HashMap hm = new HashMap();
		    initColors(hm);

		    Iterator it = hm.keySet().iterator();
		    while (it.hasNext()) {
			String k = (String) it.next();
			if (!(hm.get(k) instanceof Color))
			    continue;

			Color col = (Color) hm.get(k);
			String c = fel.findAttr("color_" + k);

			//			OmegaContext.sout_log.getLogger().info("ERR: " + "found " + k + ' ' + col + ' ' + c);
			if (c != null) {
			    //			    OmegaContext.sout_log.getLogger().info("ERR: " + "col " + k + ' ' + col + ' ' + c);
			    if (c.charAt(0) == '#') {
				int rgb;
				if (c.length() == 9) {
				    rgb = Integer.parseInt(c.substring(3), 16);
				} else {
				    rgb = Integer.parseInt(c.substring(1), 16);
				}
				hm.put(k, new Color(rgb));
			    }
			}
		    }
		    return hm;
		} catch (Exception ex) {
		    global_skipF(true);
		    JOptionPane.showMessageDialog(ApplContext.top_frame,
			    T.t("Can't create from file ") + '\n' + ex);
		    ex.printStackTrace();
		    global_skipF(false);
		}
	    }
	}
	return null;
    }

    public void restoreSettings() {
	String fn = "default.omega_colors";
	fn = getCurrentPupil().getString("theme", fn);
	Element el = Restore.restore(fn);
	if (el == null) {
	    return;
	}

	for (int i = 0; i < 100; i++) {
	    Element fel = el.findElement("canvas", i);
	    if (fel == null) {
		return;
	    }
	    String name = fel.findAttr("name");
	    Iterator it = canvases.keySet().iterator();
	    while (it.hasNext()) {
		String k = (String) it.next();
		BaseCanvas lbc = canvases.get(k);
		if (k.equals(name)) {
		    lbc.setSettingsFromElement(fel);
		}
	    }
	}
    }

    void saveSettings(String fn) {
	String afn = OmegaContext.omegaAssets(fn);
	OmegaContext.sout_log.getLogger().info("ERR: " + "save settings in file " + afn);
	Element el = getSettingsElement();
	Save.save(afn, el);
    }

    public Element getElement() {
	Element el = new Element("omega_lesson");
	el.addAttr("version", "0.0");

	Element c_el = le_canvas.getElement();
	el.add(c_el);

	Element lel = new Element("lesson");
	lel.addAttr("ord", "0");
	String lesson_name = le_canvas.getLessonName();
	lel.addAttr("name", lesson_name);

	Element tel = machine.getTarget().getTargetElement();
	lel.add(tel);
	Element itel = machine.getTarget().getItemsElement();
	lel.add(itel);

	el.add(lel);

	if (action_specific != null) {
	    Element as_el = action_specific.getElement();
	    el.add(as_el);
	    as_el = action_specific.getSignElement();
	    el.add(as_el);
	}

	Element seq_el = seq.getElement();
	if (seq_el != null) {
	    el.add(seq_el);
	}

	Element st_el = getElementStory();
	if (st_el != null) {
	    el.add(st_el);
	}

	return el;
    }

    Element getElementStory() {
	Element el = new Element("story");
	el.addAttr("isfirst", le_canvas.getLessonIsFirst() ? "yes" : "no");
	String link_next = le_canvas.getLessonLinkNext();
	if (link_next != null && link_next.length() > 0) {
	    Element e = new Element("link");
	    e.addAttr("next", link_next);
	    el.add(e);
	} else {
	    return null;
	}
	return el;
    }

    public Element getSettingsElement() {
	Element el = new Element("omega_settings");
	el.addAttr("version", "0.0");

	Iterator it = canvases.keySet().iterator();
	while (it.hasNext()) {
	    String k = (String) it.next();
	    BaseCanvas lbc = canvases.get(k);
	    Element lbc_el = new Element("canvas");
	    lbc_el.addAttr("name", k);
	    lbc.fillElement(lbc_el);
	    el.add(lbc_el);
	}
	return el;
    }

    public void copySettings(String from, String to) {
	BaseCanvas l1 = canvases.get(from);
	BaseCanvas l2 = canvases.get(to);
	l2.colors = (HashMap) l1.colors.clone();
    }

    synchronized void addValues(Values vs) {
	//	OmegaContext.sout_log.getLogger().info("ERR: " + "addValues " + vs);
	if (action != null) {
	    String s = action.getPathList();
	    vs.setStr("pathlist", s);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "pathlist: " + s);

	    s = action.getActorList();
	    vs.setStr("actorlist", s);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "actorlist: " + s);
	}
    }

    synchronized void addValues(Values vs, ActionI action_) {
	//	OmegaContext.sout_log.getLogger().info("ERR: " + "addValues " + vs);
	if (action_ != null) {
	    String s = action_.getPathList();
	    vs.setStr("pathlist", s);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "pathlist: " + s);

	    s = action_.getActorList();
	    vs.setStr("actorlist", s);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "actorlist: " + s);
	}
    }

    public void hitTarget(int ix, char type) {
	if (lep != null && type == 'p') {
	    lep.setActiveTargetIx(ix);
	    Values vs = lep.le_canvas.getTarget().getTargetValues(ix);
	    OmegaContext.sout_log.getLogger().info("ERR: " + "target " + vs);
	    addValues(vs);
	    lep.setTarget(vs);
	}
    }

    Item item_at_xy;

    public void hitItem(int ix, int iy, int where, char type) {
	if (lep != null && type == 'p') {
	    ActionI action_ = null;
	    if (action != null) {
		action_ = action;
	    }

	    double d = where / 100.0;
	    lep.setActiveItemIx(ix, iy);
	    item_at_xy = lep.le_canvas.getTarget().getItemAt(ix, iy);
	    Values vs = lep.le_canvas.getTarget().getItemAt(ix, iy).getValues(true);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "item " + vs);
	    addValues(vs, action_);
	    lep.setItem(vs);
	}
    }

    public void setPupil(String pname) {
	OmegaContext.lesson_log.getLogger().info("pname: " + pname);
	setCurrentPupil(new Pupil(pname));
	Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	prefs.put("default_pupil", pname);
	try {
	    prefs.sync();
	} catch (Exception ex) {
	}
	pupil_settings_dialog.setPupil(getCurrentPupil());

	HashMap pparm = pupil_settings_dialog.getParams();
	getCurrentPupil().setParams(pparm);
	if ("next".equals(getCurrentPupil().getString("space_key", "select"))) {
	    OmegaConfig.setNextKey();
	} else {
	    OmegaConfig.setSelectKey();
	}
	if (!edit) {
	    String pupil_lang = getCurrentPupil().getStringNo0("languageSuffix", tryLessonLanguages(T.lang));
	    OmegaContext.lesson_log.getLogger().info("pupil_lang: " + pupil_lang);
	    OmegaContext.setLessonLang(pupil_lang);
	}
	register = null; // new RegisterProxy(getCurrentPupil(), null);
	restoreSettings();
	if (feedback_movie != null) {
	    feedback_movie.dispose();
	    feedback_movie = null;
	}
	if (getCurrentPupil().getBool("sign_movie_on", false)) {
	    OmegaContext.lesson_log.getLogger().warning("prepare movie feedback: sign_movie_on true");
	    try {
		if (feedback_movie == null) {
		    feedback_movie = new FeedBackMovie(true);
		    String mn = getCurrentPupil().getString("sign_movie", null);
		    feedback_movie.prepare(mn, null);
		}
	    } catch (Exception ex) {
		OmegaContext.lesson_log.getLogger().warning("prepare sign movie feedback: " + ex);
	    }
	} else if (getCurrentPupil().getBool("movie_on", false)) {
	    OmegaContext.lesson_log.getLogger().warning("prepare movie feedback: movie_on true");
	    try {
		if (feedback_movie == null) {
		    feedback_movie = new FeedBackMovie();
		    String mn = getCurrentPupil().getString("movie", null);
		    feedback_movie.prepare(mn, null);
		}
	    } catch (Exception ex) {
		OmegaContext.lesson_log.getLogger().warning("prepare movie feedback: " + ex);
	    }
	}
    }

    String current_card = null;

    private void card_show(String name) {
	card_show(name, 0);
    }

    private void card_show(String name, int id) {
	OmegaContext.sout_log.getLogger().info("ERR: " + "**************** change " + current_card + " > " + id + ' ' + new Date() + ' ' + name);

	if (name.equals("anim1")) {
	}

	if (current_card != null) {
	    if (current_card.equals("pupil")) {
		// 		getCurrentPupil() = new Pupil(pupil_canvas.getPupilName());
		// 		restoreSettings();
		// //		if ( register != null )
		// //		    register.close();
		// 		if ( pupil_settings_dialog == null )
		// 		    pupil_settings_dialog = new PupilSettingsDialog();
		// 		pupil_settings_dialog.setPupil(getCurrentPupil());
		// 		HashMap pparm = pupil_settings_dialog.getParams();
		// 		getCurrentPupil().setParams(pparm);
		// 		OmegaContext.sout_log.getLogger().info("ERR: " + "Pupil param" + pparm);
		//		register = new RegisterProxy(getCurrentPupil());
	    }
	}

	if (base_canvas != null) {
	    base_canvas.leave();
	}

	if (name.equals("anim1")) {
	    if (action != null) {
		action.clearScreen();
	    }
	}


	card.show(card_panel, name);
	OmegaContext.HELP_STACK.pop("");
	OmegaContext.HELP_STACK.push(name);

	SundryUtils.m_sleep(200);

	if (name.equals("anim1")) {
	    if (action != null) {
		action.getCanvas().requestFocus();
	    }
	    le_canvas.hideMsg();
	}

	boolean rF = false;

	if (name.equals("main")) {
	    lemain_canvas.requestFocusOrd(last_ord);
	    last_ord = -1;
	} else {
	    rF = true;
	}

	base_canvas = (BaseCanvas) canvases.get(name);
	if (base_canvas != null) {
	    base_canvas.enter();
	    if (rF) {
		base_canvas.requestFocus();
	    }
	}
	current_card = name;
    }

    java.util.List msg_list = new ArrayList();
    boolean stop_msg;

    public void sendMsg(String msg, Object o) {
	sendMsg(msg, o, "");
    }

    public void sendMsgWait(String msg, Object o) {
	wait_id[0] = "" + System.nanoTime();
	sendMsg(msg, o, wait_id[0]);
	synchronized (wait_id) {
	    try {
		wait_id.wait();
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    private void sendMsg(String msg, Object o, String id) {
	OmegaContext.sout_log.getLogger().info("ERR: " + "!!!!!!!! sendMsg " + msg + ' ' + SundryUtils.ct() + ' ' + o + ' ' + id);
	synchronized (msg_list) {
	    msg_list.add(new Object[]{msg, o, Long.valueOf(SundryUtils.ct()), id});
	    //log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "%%%%% inserted sendMsg >>> " + msg + ' ' + o);
	    msg_list.notify();
	}
    }

    private Object[] getMsg() {
	synchronized (msg_list) {
	    while (msg_list.size() == 0) {
		try {
		    msg_list.wait();
		} catch (InterruptedException ec) {
		}
	    }
	    int len = msg_list.size();
	    Object[] msg = (Object[]) msg_list.remove(0);
	    return msg;
	}
    }

    public String getLoadedFName() {
	return loaded_fname;
    }

    Object sa_lock = new Object();
    boolean say_all = false;

    void sayAll(Target tg) {
	try {
	    if ( OmegaConfig.tts ) {
		String lang = OmegaContext.getLessonLang();
		if ( TTS.say(lang, tg.getAllTTS(), true) )
		    return;
	    }

	    say_all = true;
	    String[] sa = tg.getAllSounds();

	    APlayer[] apA = new APlayer[sa.length];
	    for (int i = 0; i < sa.length; i++) {
		String ss = sa[i];
		apA[i] = APlayer.createAPlayer(getCurrentPupil().getStringNo0("languageSuffix", null), ss, null, "SA_" + i);
	    }
	    for (int i = 0; i < apA.length; i++) {
		le_canvas.setMarkTarget(i, true);
		apA[i].playWait();
	    }
	} catch (Exception ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "Exception! Lesson.sayAll(): " + ex);
	    ex.printStackTrace();
	} finally {
	    synchronized (sa_lock) {
		say_all = false;
		sa_lock.notifyAll();
	    }
	}
	//	APlayer.unloadAll("SA_[0-9]*");
    }

    private void waitSayAll() {
	synchronized (sa_lock) {
	    while (say_all) {
		try {
		    sa_lock.wait();
		} catch (InterruptedException ex) {
		}
	    }
	}
    }

    //      private void waitBoxPlay() {
// // 	APlayer ap = box_ap;
// // 	if ( ap != null )
// // 	    ap.waitPlay();
//      }
    APlayer ap_svisch;

    private void saySw(String snd, boolean wait) {
	try {
	    if (ap_svisch != null) {
		ap_svisch.close();
	    }

	    ap_svisch = APlayer.createAPlayer(snd, null, "LE_");
	    if (wait) {
		ap_svisch.playWait();
		ap_svisch = null;
	    } else {
		ap_svisch.play();
	    }
	} catch (Exception ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "Exception! Lesson.sayAll(): " + ex);
	}
    }

    APlayer ap_s;

    private void sayS(String snd) {
	try {
	    if (ap_s != null) {
		ap_s.close();
	    }
	    ap_s = APlayer.createAPlayer(snd, null, "LE_");
	    ap_s.play();
	} catch (Exception ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "Exception! Lesson.sayAll(): " + ex);
	}
    }

    private void sayPingSentence() {
	if (getCurrentPupil().getBool("pingSentence", true)) {
	    saySw("svisch.wav", true);
	}
    }

    private void sayPingAnim() {
	if (getCurrentPupil().getBool("pingAnim", false)) {
	    saySw("svisch2.wav", false);
	}
    }

    public class ActionSpecific {

	HashMap hm;
	HashMap hmSign;

	ActionSpecific() {
	    hm = new HashMap();
	    hmSign = new HashMap();
	}

	public List<String> getMedia() {
	    List<String> li = new ArrayList<>();
	    for (Object ov : hm.values())
		li.add(ov.toString());
	    for (Object ov : hmSign.values())
		li.add(ov.toString());
	    return li;
	}

	public boolean is(String s) {
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "try specific action " + s);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + element_root);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + hm);

	    String as = (String) hm.get(s);
	    lesson_log.getLogger().info("action_specific.is:" + s + '<' + as + '>');
	    return as != null;
	}

	public boolean isSign(String s) {
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "try specific action " + s);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + element_root);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + hm);

	    String as = (String) hmSign.get(s);
	    lesson_log.getLogger().info("action_specific.isSign:" + s + '<' + as + '>');
	    return as != null;
	}

	public String getAction(String s) {
	    //  	    OmegaContext.sout_log.getLogger().info("ERR: " + "try specific action " + s);
	    //  	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + element_root);
	    //  	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + hm);

	    String as = (String) hm.get(s);
	    lesson_log.getLogger().info("action_specific.getAction:" + s + '<' + as + '>');
	    return as;
	}

	public String getSign(String s) {
	    //  	    OmegaContext.sout_log.getLogger().info("ERR: " + "try specific action " + s);
	    //  	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + element_root);
	    //  	    OmegaContext.sout_log.getLogger().info("ERR: " + "                     " + hm);

	    String as = (String) hmSign.get(s);
	    lesson_log.getLogger().info("action_specific.getSign:" + s + '<' + as + '>');
	    return as;
	}

	public void setAction(String s, String val) {
	    hm.put(s, val);
	}

	public void setSign(String s, String val) {
	    hmSign.put(s, val);
	}

	Element getElement() {
	    Element el = new Element("action_specific");
	    Iterator it = hm.keySet().iterator();
	    while (it.hasNext()) {
		String key = (String) it.next();
		String val = (String) hm.get(key);
		if ( !SundryUtils.empty(val) ) {
		    Element el1 = new Element("value");
		    el1.addAttr("key", key);
		    el1.addAttr("val", val);
		    el.add(el1);
		}
	    }
	    return el;
	}

	Element getSignElement() {
	    Element el = new Element("sign_specific");
	    Iterator it = hmSign.keySet().iterator();
	    while (it.hasNext()) {
		String key = (String) it.next();
		String val = (String) hmSign.get(key);
		if ( !SundryUtils.empty(val) ) {
		    Element el1 = new Element("value");
		    el1.addAttr("key", key);
		    el1.addAttr("val", val);
		    el.add(el1);
		}
	    }
	    return el;
	}

	void fill(Element el) {
	    Element as_el = el.findElement("action_specific", 0);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "FOUND EL action specific " + as_el);
	    if (as_el != null) {
		for (int i = 0; i < 1000; i++) {
		    Element el1 = as_el.findElement("value", i);
		    if (el1 == null) {
			break;
		    }
		    String key = (String) el1.findAttr("key");
		    String val = (String) el1.findAttr("val");
		    if ( !SundryUtils.empty(val) ) {
			hm.put(key, val);
			lesson_log.getLogger().info("action_specific.fill:" + key + '<' + val + '>');
		    } else {
			lesson_log.getLogger().info("action_specific.fill: empty val, ignored " + key);
		    }
		}
	    }
	}

	void fillSign(Element el) {
	    Element as_el = el.findElement("sign_specific", 0);
	    //	    OmegaContext.sout_log.getLogger().info("ERR: " + "FOUND EL action specific " + as_el);
	    if (as_el != null) {
		for (int i = 0; i < 1000; i++) {
		    Element el1 = as_el.findElement("value", i);
		    if (el1 == null) {
			break;
		    }
		    String key = (String) el1.findAttr("key");
		    String val = (String) el1.findAttr("val");
		    if ( !SundryUtils.empty(val) ) {
			hmSign.put(key, val);
			lesson_log.getLogger().info("sign_specific.fill:" + key + '<' + val + '>');
		    } else {
			lesson_log.getLogger().info("sign_specific.fill: empty val, ignored, " + key);
		    }
		}
	    }
	}
    }

    public ActionSpecific action_specific = new ActionSpecific();

    /**
     * Return true if more than 3 columns
     */
    private boolean heavyLesson(Element el) {
	Element iel = el.findElement("items", 0);
	if (iel != null) {
	    Element ieel = iel.findElement("item-entry", 3);
	    if (ieel != null) {
		return true;
	    }
	}
	return false;
    }

    String wait_id[] = new String[]{""};

    private long last_msg_time = SundryUtils.ct();

    void execLesson(String fn) {
	Target tg = new Target(machine);
	machine.setTarget(tg);
	// APlayer.unloadAll("Box[0-9]*");

	if (action != null) {
	    action.clean();
	}

	action = null;
	//	window.setVisible(true);
	le_canvas.populateGUI();

	while (Splash.keep != null && Splash.keep )
	    SundryUtils.m_sleep(300);
	OmegaAppl.closeSplash();

	if (fn != null) {
	    card_show("words");
	    sendMsg("load", fn, "execLesson");
	}

	Set<int[][]> test_index = null;

	String last_id = "!";

	for (; ; ) {
	    synchronized (wait_id) {
		if (last_id.equals(wait_id[0]))
		    wait_id.notify();
	    }

	    System.err.println(" -----------> done " + last_id);
	    Object o[] = getMsg();
	    String msg = (String) o[0];
	    Object obj = o[1];
	    long msg_time = ((Long) o[2]).longValue();
	    String id = (String) o[3];
	    long delta = msg_time - last_msg_time;
	    last_msg_time = msg_time;

	    msg_log.getLogger().info("%%%%%%%%%%%%%%%%%%% msg " + msg + ' ' + obj + ' ' + delta + ' ' + id);

	    if ("load".equals(msg)) {
		exec_load((String) obj, tg);
		progress.dismiss();
		if (current_test_mode_group == TMG_CREATE) {
		    if (!last_story_flag) {
			card_show("words");
		    }
		    if (register != null) {
			register.setStarted();
		    }
		}

	    } else if ("create".equals(msg)) {
		le_canvas.hideMsg();
		le_canvas.setMarkTargetNo();
		sendMsg("load", (String) obj, "create");

	    } else if ("new_test".equals(msg)) {
		//seq.initNewTest();
		le_canvas.hideMsg();
		//		OmegaContext.sout_log.getLogger().info("ERR: " + "here load_test");
		sendMsg("load", (String) obj, "new_test");
		// 		if ( current_test_mode_group == TMG_CREATE )
		// 		    card_show("words");
		sendMsg("test_cont", null, "new_test2");

	    } else if ("test_cont".equals(msg)) {
		test_index = exec_test_cont();

	    } else if ("show_result_msg".equals(msg)) {
		OmegaContext.sout_log.getLogger().info("ERR: " + "show_result_msg " + +' ' + register);

		if (register != null && register.has_shown == false && current_test_mode == TM_RAND) {
		    le_canvas.showMsg(getResultSummary_MsgItem());
		    register.has_shown = true;
		    register.close();
		}
		le_canvas.fireRealExit();

	    } else if ("hBoxM".equals(msg)) {
		LessonCanvas.Box hBox =
			(LessonCanvas.Box) obj;
		exec_hbox(hBox, tg, test_index);

	    } else if ("hBoxK".equals(msg)) {
		LessonCanvas.Box hBox =
			(LessonCanvas.Box) obj;
		exec_hbox(hBox, tg, test_index);
		le_canvas.gotoNextBox();

	    } else if ("action".equals(msg)) {
		sendMsg("play", null, "action");
		//log 		OmegaContext.sout_log.getLogger().info("ERR: " + "action:play done");
		if (tg.getStoryNext() != null) {
		    //		    show_progress = false;
		    OmegaContext.sout_log.getLogger().info("ERR: " + "STORY NEXT  " + tg.getStoryNext());
		    sendMsg("create", tg.getStoryNext(), "action2");
		    card_show("words", 5);
		} else {
		    //		    show_progress = true;
		    if (!last_story_flag) {
			sendMsg("load", loaded_fname, "action3");
		    }
		}

	    } else if ("listen".equals(msg)) {
		if (tg.isTargetFilled()) {
		    sayAll(tg);
		    le_canvas.setMarkTargetNo();
		}
		card_show("words");

	    } else if (msg != null && msg.startsWith("playSign:")) {
		if (tg.isTargetFilled()) {
		    playSignFile(tg, true);
		    le_canvas.setMarkTargetNo();
		    card_show("words");
		}

	    } else if ("play".equals(msg)) {
		exec_play(tg, true);
		//		OmegaContext.sout_log.getLogger().info("ERR: " + "exec_play done");
		if (register != null) {
		    register.setStarted();
		}

	    } else if ("playAll".equals(msg)) {
		exec_play(tg, false);
		card_show("words");

	    } else if ("play&return".equals(msg)) {
		exec_play(tg, true);
		card_show("words");

	    } else if ("read_story".equals(msg)) {
		sentence_canvas.showMsg(null);
		sentence_canvas.setRead(true);
		card_show("sent");
		sendMsg("sent_select", "");
	    } else if ("sent_quit".equals(msg)) {
		sentence_canvas.hidePopup(3);
		play_data_list = new PlayDataList();
		play_data_list_is_last = new PlayDataList();
		sentence_canvas.showMsg(null);
		//		sentence_canvas.setRead(false);
		card_show("main");

	    } else if ("sent_read".equals(msg)) {
		sentence_canvas.hidePopup(3);
		SentenceList sent_li = (SentenceList) story_hm.get("sentence_list");
		ArrayList ss_li = sent_li.sentence_list;
		OmegaContext.story_log.getLogger().info("sent_read story_hm 1599 " + story_hm);
		OmegaContext.story_log.getLogger().info("sent_read sent_li 1599 " + sent_li);
		OmegaContext.story_log.getLogger().info("sent_read ss_li 1599 " + ss_li);
		OmegaContext.story_log.getLogger().info("sent_read playdatalist 1599 " + play_data_list);
		sentence_canvas.ignorePress(true);
		sentence_canvas.showMsg(null);
		sentence_canvas.showMsg(ss_li);
		sentence_canvas.setStoryData(play_data_list); // strange
		card_show("sent");
		listenFromDataList(play_data_list_is_last); //, sentence_canvas.getListenListener());
		sentence_canvas.ignorePress(false);

	    } else if ("sent_replay".equals(msg)) {
		sentence_canvas.hidePopup(3);
		if (action == null) {
		    action = new AnimAction();
		    card_panel.add(action.getCanvas(), "anim1");
		}
		card_show("anim1");
		hit_key = 0;
		playFromDataList(play_data_list);
		card_show("sent");

	    } else if ("sent_print".equals(msg)) {
		// not anymore                sentence_canvas.togglePopup(3);
		sendMsg("sent_print_print", "");
	    } else if ("sent_print_select".equals(msg)) {
		//not anymore
	    } else if ("sent_print_print".equals(msg)) {
		sentence_canvas.setBusy(true);
		SentenceList sent_li = (SentenceList) story_hm.get("sentence_list");
		ArrayList ss_li = sent_li.sentence_list;
		OmegaContext.story_log.getLogger().info("sent_print story_hm 1599 " + story_hm);
		OmegaContext.story_log.getLogger().info("sent_print sent_li 1599 " + sent_li);
		OmegaContext.story_log.getLogger().info("sent_print ss_li 1599 " + ss_li);
		printFromDataList(play_data_list_is_last);
		sentence_canvas.setBusy(false);
		sentence_canvas.hidePopup(3);
	    } else if ("sent_save".equals(msg)) {
		sentence_canvas.hidePopup(3);
		SentenceList sent_li = (SentenceList) story_hm.get("sentence_list");
		ArrayList ss_li = sent_li.sentence_list;
		String lname = sent_li.lesson_name;
		DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date d = play_data_list.date;
		String date = df.format(d);

		int ord = play_data_list.nextOrd();
		if (register == null) { // null after windows resize
		    register = new RegisterProxy(getCurrentPupil());
		}
		String dir = register.rl.getDirPath(getCurrentPupil().getName());

		String fname =
			getCurrentPupil().getName() + '-'
				+ lname + '-'
				+ date + '-'
				+ ord;
		String fullname = dir + fname + '.' + "omega_story_text";
		String fullname_2 = dir + fname + '.' + "omega_story_replay";

		try  {
		    try (PrintWriter pw = new PrintWriter(new FileWriter(new File(fullname)))) {
			Iterator it = ss_li.iterator();
			while (it.hasNext()) {
			    String sent = (String) it.next();
			    pw.println(sent);
			}
		    }
		    try (ObjectOutput oo = new ObjectOutputStream(new FileOutputStream(fullname_2))) {
			oo.writeObject(sent_li);
			oo.writeObject(play_data_list.arr);
			oo.writeObject(play_data_list_is_last.arr);
		    }
		    global_skipF(true);
		    JOptionPane.showMessageDialog(ApplContext.top_frame, T.t("Saved in file") + ' ' + fullname);
		    global_skipF(false);
		} catch (Exception ex) {
		    ex.printStackTrace();
		    global_skipF(true);
		    JOptionPane.showMessageDialog(ApplContext.top_frame, T.t("File") + ' ' + fullname + ' ' + T.t("not saved") + '.');
		    global_skipF(false);
		    OmegaContext.sout_log.getLogger().info("ERR: " + "" + ex);
		}

	    } else if ("sent_select".equals(msg)) {
		sentence_canvas.hidePopup(3);
		sentence_canvas.setBusy(true);
		try {
		    //		    global_skipF(true);
		    try {
			if (register == null) {
			    register = new RegisterProxy(getCurrentPupil());
			}
			String dir = register.rl.getDirPath(getCurrentPupil().getName());
			lesson_log.getLogger().info("get it from " + dir);
			File dir_file = new File(dir);
			String[] files = dir_file.list(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
				if (name.endsWith(".omega_story_replay")) {
				    return true;
				}
				return false;
			    }
			});

			if (files.length > 0) {
			    sentence_canvas.showMsg(null);
			    sentence_canvas.enableStoryList(true);
			    sentence_canvas.setListData(files);
			    String filename = sentence_canvas.waitDone();

			    File file = new File(dir + '/' + filename);

			    lesson_log.getLogger().info("story reply file is " + file);

			    try (ObjectInput in = new ObjectInputStream(new FileInputStream(file))) {
				SentenceList sent_li = (SentenceList) in.readObject();
				story_hm.put("sentence_list", sent_li);
				play_data_list.arr = (ArrayList) in.readObject();
				play_data_list_is_last.arr = (ArrayList) in.readObject();
			    }
			} else {
			    global_skipF(true);
			    JOptionPane.showMessageDialog(ApplContext.top_frame,
				    T.t("Can't find any saved story"));
			    global_skipF(false);
			}
		    } catch (Exception ex) {
			OmegaContext.sout_log.getLogger().info("ERR: " + "" + ex);
		    }
		} finally {
		    global_skipF(false);
		    sentence_canvas.enableStoryList(false);
		    sentence_canvas.setBusy(false);
		}

	    } else if ("test_dialog".equals(msg)) {
		testDialog();
	    } else if ("exitLesson".equals(msg)) {
		return;
	    }

	    last_id = id;
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
    boolean show_progress = true;

    void startProgress() {
	if (progress == null) {
	    progress = new Progress();
	} else if (show_progress) {
	    progress.show();
	}
    }

    private void initNewLesson(String fn) {
	//	Locator.setLang(getCurrentPupil().getStringNo0("languageSuffix", null));
	startProgress();
	le_canvas.resetNav();
    }

    private String last_lesson_fn = null;

    private void exec_load(String fn, Target tg) { // load a lesson
	try {
	    initNewLesson(fn);
	    try {
		le_canvas.getTarget().releaseAllT_Items();
	    } catch (Exception ex) {
	    }
	    le_canvas.removeDummy();
	    SundryUtils.m_sleep(100);
	    //log 	OmegaContext.sout_log.getLogger().info("ERR: " + "Loading Restoring " + fn + ' ' + last_lesson_fn);

	    final Element el;

	    String lang = getCurrentPupil().getStringNo0("languageSuffix", null);
	    if (!edit && lang != null) {
		String fn_lang = fn.replaceAll("lesson-[a-zA-Z]*/active", "lesson-" + lang + "/active"); // LESSON-DIR-A
		String fn_lang_demo = fn.replaceAll("lesson-[a-zA-Z]*/active", "lesson-" + lang + "/demo"); // LESSON-DIR-A
		String fn_lang_demo2 = fn.replaceAll("lesson-[a-zA-Z]*/active", "lesson/demo"); // LESSON-DIR-A
		OmegaContext.sout_log.getLogger().info("ERR: " + "LANG repl (~A)" + fn + ' ' + fn_lang);
		Element el1 = Restore.restore(fn_lang);
		if (el1 == null) {
		    el1 = Restore.restore(fn);
		}
		if (el1 == null) {
		    el1 = Restore.restore(fn_lang_demo);
		}
		if (el1 == null) {
		    el1 = Restore.restore(fn_lang_demo2);
		}
		el = el1;
	    } else {
		el = Restore.restore(fn);
	    }

	    if (!fn.equals(last_lesson_fn)) {
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
		last_lesson_fn = fn;
	    }
	    //	    audio_prefetch.prefetchAny(fn);

	    boolean dummy = false;
	    if (current_test_mode == TM_POST_1
		    || current_test_mode == TM_POST_2) {
		dummy = true;
	    }

	    if (el != null) {
		action_specific = new ActionSpecific();
		lesson_log.getLogger().info("action_specific.new:" + fn);
		action_specific.fill(el);
		action_specific.fillSign(el);
		element_root2 = el;
		if (current_test_mode == TM_CREATE) {
		    tg.loadFromEl(el, "", story_hm, false, false);
		    seq = new Sequencer(el);
		} else if (current_test_mode == TM_RAND) {
		    tg.loadFromEl(el, "", story_hm, false, true);
		    seq = new Sequencer(el);
		    seq.dump();
		} else { // TM_{PRE,POST}
		    seq = new Sequencer(el);
		    String full_test_txt = seq.getTestText(current_test_mode, true);
		    String test_txt = seq.getTestText(current_test_mode, false);

		    OmegaContext.sout_log.getLogger().info("ERR: " + "EL [][][] FOUND " + full_test_txt + ' ' + test_txt);

		    if (test_txt == null) {
			global_skipF(true);
			JOptionPane.showMessageDialog(ApplContext.top_frame,
				T.t("Can't find any test text"));
			global_skipF(false);
			card_show("main", 0);
			if (register != null) {
			    register.close();
			}
			register = null;
		    } else {
			if (heavyLesson(el)) {
			    tg.loadCompositeFromEl(el, test_txt, story_hm, dummy, true);
			} else {
			    le_canvas.initNewLesson();
			    tg.loadFromEl(el, "", story_hm, dummy, true);
			}
		    }
		}

		//log 	    OmegaContext.sout_log.getLogger().info("ERR: " + "=-= new tm " + current_test_mode);

		le_canvas.setFrom(el, dummy);

		if (le_canvas.getLessonIsFirst()) {
		    play_data_list = new PlayDataList();
		    play_data_list_is_last = new PlayDataList();
		    story_hm.clear();
		    story_hm.put("sentence_list", new SentenceList());
		    OmegaContext.story_log.getLogger().info("lesson Is First " + fn);
		}

		le_canvas.render(true, true);
		loaded_fname = fn;
		if (window instanceof JFrame) {
		    ((JFrame) window).setTitle("Omega - Lesson Editor: " + OmegaContext.antiOmegaAssets(fn));
		}
	    } else {
		global_skipF(true);
		JOptionPane.showMessageDialog(ApplContext.top_frame,
			T.t("Can't load lessonfile ")
				+ fn);
		global_skipF(false);
	    }
	    le_canvas.initAction();
	    progress.dismiss();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    global_skipF(true);
	    if (!fn.equals("new.omega_lesson")) {
		JOptionPane.showMessageDialog(ApplContext.top_frame,
			T.t("Error while loading lesson: \"")
				+ fn + "\"");
	    }
	    global_skipF(false);
	}
    }

    FeedBackMovie feedback_movie;

    Set<int[][]> exec_test_cont() {
	if (!edit && current_test_mode > TM_CREATE) {
	    String full_test_txt = seq.getTestText(current_test_mode, true);

	    OmegaContext.sout_log.getLogger().info("ERR: " + "got this full_test_text: " + full_test_txt);
	    le_canvas.removeDummy();
	    if (current_test_mode == TM_POST_1
		    || current_test_mode == TM_POST_2) {
		le_canvas.sowDummy(full_test_txt);
	    }
	    if (full_test_txt == null) {
		global_skipF(true);
		JOptionPane.showMessageDialog(ApplContext.top_frame,
			T.t("Test text empty (null)"));
		global_skipF(false);
		card_show("main", 4);
	    }
	    String test_txt = full_test_txt.replaceAll("\\{[^\\{\\}]*\\}", "");
	    Set<String> allCorrect = getMatchingSameActionSpecific(action_specific, test_txt);


	    OmegaContext.sout_log.getLogger().info("ERR: " + "got this test_text: " + test_txt + " -> " + allCorrect);

	    try {
		le_canvas.getTarget().reloadComposite(test_txt);
	    } catch (Exception ex) {
		global_skipF(true);
		JOptionPane.showMessageDialog(ApplContext.top_frame,
			T.t("Can't find test test ") + test_txt);
		global_skipF(false);
		card_show("main", 4);
	    }
	    le_canvas.render();

	    Set<int[][]> test_index = new HashSet<>();

	    OmegaContext.sout_log.getLogger().info("ERR: " + "ET[][][] FOUND " + full_test_txt + ' ' + test_txt);
	    if (test_txt != null) {
	        if (allCorrect.size() == 0 ) {
		    int[][] test_index1 = le_canvas.getTarget().getAllTargetCombinationsIndexes(test_txt);
		    test_index.add(test_index1);
		} else {
	            for ( String s : allCorrect ) {
			int[][] test_index1 = le_canvas.getTarget().getAllTargetCombinationsIndexes(s);
			test_index.add(test_index1);
		    }
		}
		OmegaContext.sout_log.getLogger().info("ERR: " + "ET  [][] " + test_index.size());

		if (test_index.size() == 0) {
		    global_skipF(true);
		    JOptionPane.showMessageDialog(ApplContext.top_frame,
			    T.t("Can't find sentence ") + test_txt);
		    global_skipF(false);
		    card_show("main", 3);
		} else {
		    le_canvas.setMarkTarget(0);
		    sendMsg("play", null, "exec_test_cont");
		}
	    } else {
		global_skipF(true);
		JOptionPane.showMessageDialog(ApplContext.top_frame,
			T.t("Can't run test ") + current_test_mode);
		global_skipF(false);
		card_show("main", 4);
	    }
	    return test_index;
	}
	return null;
    }

    private String fixSP(String s, String p, int a) {
	if (a == 1) {
	    return s;
	}
	return p;
    }

    static public boolean inExecHbox = false;
    static public boolean mistNoMouse = false;

    private void exec_hbox(LessonCanvas.Box hBox, Target tg, Set<int[][]> test_index) {
	try {
	    inExecHbox = true;

	    if (hBox != null) {

		long exec_hbox_time = hBox.when_hit; // SundryUtils.ct();

		try {
		    if (tg != null) {
			int i_x = hBox.o_x;
			int i_y = hBox.o_y;
			int tg_ix;
			int where = hBox.where;
			if (current_test_mode_group == TMG_CREATE) {
			    tg_ix = tg.findNextFreeT_ItemIx(hBox.getItem(), edit, where);
			} else {
			    tg_ix = tg.findNextFreeT_ItemIx();
			}
			if (tg_ix == -1) {
			    if (register != null) {
				register.word(":again",
					exec_hbox_time,
					hBox.getItem().getText(),
					hBox.getItem().it_ent.tid);
			    }
			    le_canvas.renderTg();
			} else {
			    boolean was_wrong = true;

			    if (current_test_mode_group != TMG_CREATE) {
				int next_i_x = tg.findEntryIxMatchTargetIx(tg_ix);
				for ( int[][] test_index1 : test_index ) {
				    OmegaContext.sout_log.getLogger().info("ERR: " + "!!! using test index: " + SundryUtils.a2s(test_index) + ' ' + tg_ix);
				    int next_i_y = test_index1[tg_ix][0];
				    int next_i_x_ = test_index1[tg_ix][1];
				    int next_i_y_ = test_index1[tg_ix][2];
				    // next 0 8    i_ 1 0
				    OmegaContext.sout_log.getLogger().info("ERR: " + "HERE:  " + i_x + ' ' + i_y + "    next " + next_i_x + ' ' + next_i_y + "   next_  " + next_i_x_ + ' ' + next_i_y_);
				    if (next_i_x_ == i_x && next_i_y_ == i_y) {
				        was_wrong = false;
				    } else {
//					was_wrong = true;
				    }
				}
			    }

			    Item itm = tg.pickItemAt(i_x,
				    i_y,
				    tg_ix,
				    current_test_mode_group != TMG_CREATE);
			    if (itm == null) {
				global_skipF(true);
				JOptionPane.showMessageDialog(ApplContext.top_frame,
					"Internal error when picking\n"
						+ "item pos (xy)" + i_x + ' ' + i_y + "\n"
						+ "target pos " + tg_ix);
				global_skipF(false);
				OmegaContext.sout_log.getLogger().info("ERR: " + "Internal error when picking\n"
					+ "item pos (xy)" + i_x + ' ' + i_y + "\n"
					+ "target pos " + tg_ix);
			    } else {
				String t_word_ = tg.getTextAt(tg_ix);

				String t_word = fixWithStar(t_word_);
				if (current_test_mode == TM_CREATE) {
				    if (register != null) {
					register.word("create:build",
						exec_hbox_time,
						t_word,
						tg.getTidAt(tg_ix));
				    }
				} else {
				    if (register != null) {
					if (was_wrong) {
					    register.word("test:build:wrong",
						    exec_hbox_time,
						    t_word,
						    hBox.getItem().getEntryTid());
					    seq.cnt_word_wrong++;
					    seq.cnt_wordlast_wrong++;
					} else {
					    register.word("test:build:OK",
						    exec_hbox_time,
						    t_word,
						    hBox.getItem().getEntryTid()); // tg.getTidAt(tg_ix));
					    seq.cnt_word_correct++;
					    seq.cnt_wordlast_correct++;
					}
					for ( int[][] test_index1 : test_index ) {
					    if ( test_index1[tg_ix][1] == i_x && test_index1[tg_ix][2] == i_y ) { // choosen match ok word

					    } else {                               // choosen do not match, -> invalidate it all
						for ( int[] tix1  : test_index1 ) {
						    tix1[1] = -1;
						    tix1[2] = -1;
						}
					    }
					}
				    }
				}


				le_canvas.renderTg();

				long ct0 = SundryUtils.ct();

				System.gc();
				System.gc();

				boolean showSoundWord = getCurrentPupil().getBool("showSoundWord", true);
				if (showSoundWord) {
				    PLAYSND:
				    for (; ; ) {
					Item sitm = tg.getItemAt(i_x, i_y);

					if ( OmegaConfig.tts ) {
					    String lang = OmegaContext.getLessonLang();
					    if ( TTS.say(lang, sitm.getDefaultFilledTTS(), true) )
					        break PLAYSND;
					}

					String sfn = sitm.getSoundD();
					if (sfn == null) {
					    break PLAYSND;
					}
					sfn = tg.fillVarHere(tg_ix, sfn);
					if (sfn == null || sfn.length() == 0) {
					    break PLAYSND;
					}

					String[] sndA;
					sndA = SundryUtils.split(sfn, ",");
					APlayer[] aplayerA = new APlayer[sndA.length];
					for (int i = 0; i < sndA.length; i++) {
					    String s = sndA[i];
					    aplayerA[i] = APlayer.createAPlayer(getCurrentPupil().getStringNo0("languageSuffix", null),
						    s,
						    null,
						    "Box" + i);
					}
					//				    waitBoxPlay();
					for (int i = 0; i < sndA.length; i++) {
					    aplayerA[i].playWait();
					}
					break;
				    }
				}

				boolean showSignWord = getCurrentPupil().getBool("showSignWord", true);

				if (current_test_mode == TM_CREATE && OmegaConfig.isLIU_Mode() && showSignWord && !edit) {
				    LiuMovieManager lmm = signMoviePrepare(tg, tg_ix);
				    if (lmm != null) {
					try {
					    final Color bgCol = omega_settings_dialog.signWord_background.color;
					    final int alphaCol = omega_settings_dialog.signWord_alpha.getValue();
					    int sms = omega_settings_dialog.signMovieWord_scale.getValue();
					    if (sms == 0) {
						sms = 1;
					    }
					    Rectangle tgr = le_canvas.getTargetRectangle(tg_ix);
					    startMovieAndWait(lmm, tgr, bgCol, alphaCol, sms, 1);
					} finally {
					    le_canvas.setMist(0, null, null, 0);
					    mistNoMouse = false;
					    lmm.cleanup();
					}
				    }
				}

				if (itm.isAction) {
				    try {
					String action_s = tg.getActionFileName(0);
					OmegaContext.sout_log.getLogger().info("ERR: " + "Action fn " + action_s);
					if (action_s != null && action_s.length() > 0) {
					    if (false && action == null) { //--
						action = new AnimAction();
						card_panel.add(action.getCanvas(), "anim1");
					    }
					    if (!false) {
//--when pressed button						element_root = action.prefetch(action_s);
						if (element_root == null)
						    ; //JOptionPane.showMessageDialog(ApplContext.top_frame,
						    //			      T.t("Can't find animation: " + action_s));
						else {
						    // patch or else we need to double press item to fill editors panel with actorlist
						    if (lep != null && item_at_xy != null) {
							Values vs = item_at_xy.getValues(true);
							OmegaContext.sout_log.getLogger().info("ERR: " + "item " + vs);
							addValues(vs);
							lep.setItem(vs);
						    }
						}
					    }
					}
				    } catch (Exception ex) {
					OmegaContext.sout_log.getLogger().info("ERR: " + "@@@@@h " + ex);
					ex.printStackTrace();
				    }
				}

				long ct1 = SundryUtils.ct();

				//				    OmegaContext.sout_log.getLogger().info("ERR: " + "time prefetch " + (ct1-ct0));

				if (tg.isTargetFilled() && !edit) {
				    if (current_test_mode == TM_CREATE) {
					String l_id_list = tg.getAll_Tid_Item();
					if (register != null) {
					    register.create(":correct",
						    exec_hbox_time,
						    tg.getAllText(),
						    1,
						    l_id_list);
					}
					boolean do_repeat_whole =
						tg.get_howManyT_Items() > 1;

					le_canvas.enableQuitButton(false);
					le_canvas.resetHboxFocus();

					if (do_repeat_whole) {
					    waitSayAll();
					    //					    waitBoxPlay();
					    SundryUtils.m_sleep(getCurrentPupil().getSpeed(500));
					    le_canvas.setMarkTargetAll();
					    sayPingSentence();
					    le_canvas.eraseHilitedBox();
					    SundryUtils.m_sleep(getCurrentPupil().getSpeed(300));
					    sayAll(tg);
					    waitSayAll();
					    SundryUtils.m_sleep(getCurrentPupil().getSpeed(400));
					} else {
					    waitSayAll();
					    //					    waitBoxPlay();
					    SundryUtils.m_sleep(getCurrentPupil().getSpeed(500));
					}

					le_canvas.setMarkTargetAll();

					boolean showSignSentence = getCurrentPupil().getBool("showSignSentence", true);

					if (OmegaConfig.isLIU_Mode() && showSignSentence && !edit && current_test_mode == TM_CREATE) {
					    playSignFile(tg, false);
					}

					le_canvas.setMarkTargetNo();
					le_canvas.eraseHilitedBox();

					sayPingAnim();
					SundryUtils.m_sleep(300);
					le_canvas.startAction();
				    } else {  // current_test_mode == TM_PRE/POST/RAND
					le_canvas.enableQuitButton(false);
					le_canvas.resetHboxFocus();
					waitSayAll();
					//					waitBoxPlay();
					SundryUtils.m_sleep(getCurrentPupil().getSpeed(500));
					le_canvas.setMarkTargetAll();
					sayPingSentence();
					SundryUtils.m_sleep(getCurrentPupil().getSpeed(300));
					sayAll(tg);
					waitSayAll();
					SundryUtils.m_sleep(getCurrentPupil().getSpeed(500));
					le_canvas.setMarkTargetAll();

					String all_text = tg.getAllText();
					String correct_text = seq.getTestText(current_test_mode, false);

					boolean correctBool = false;

					Set<String> allCorrect = getMatchingSameActionSpecific(action_specific, correct_text);
					for ( String correct : allCorrect ) {
					     if ( correct.equalsIgnoreCase(all_text) ) {
					         correctBool = true;
					     }
					}
					OmegaContext.sout_log.getLogger().info("TestEval: " + "" + correct_text + " <- " + allCorrect + " -> " + correctBool + " | " + all_text.equalsIgnoreCase(correct_text));

					if (correctBool || all_text.equalsIgnoreCase(correct_text)) {
					    seq.cnt_sent_correct++;
					    if (register != null) {
						register.test(":correct",
							exec_hbox_time,
							seq.getTestText(current_test_mode, false),
							tg.getAllText(),
							seq.getStat(true),
							tg.getAll_Tid_Item());
					    }
					    boolean do_prepare = false;
					    if (getCurrentPupil().getBool("movie_on", false) || getCurrentPupil().getBool("sign_movie_on", false)) {
						try {
						    boolean do_it = false;
						    int val = getCurrentPupil().getInt("frequence", 0);
						    switch (val) {
							case 0:
							    do_it = false;
							    break;
							case 1:
							    do_it = SundryUtils.rand(10) < 2;
							    break;
							case 2:
							    do_it = SundryUtils.rand(10) < 7;
							    break;
							case 3:
							    do_it = true;
							    break;
						    }
						    //						OmegaContext.sout_log.getLogger().info("ERR: " + "+++++ do_it " + do_it);
						    if (do_it && feedback_movie == null) {
							feedback_movie = new FeedBackMovie(getCurrentPupil().getBool("sign_movie_on", false));
							String mn = getCurrentPupil().getString(getCurrentPupil().getBool("sign_movie_on", false) ? "sign_movie" : "movie", null);
							feedback_movie.prepare(mn, null);
						    }
						    if (feedback_movie != null && do_it) {
							try {
							    JPanel m_pan = feedback_movie.canvas;
							    m_pan.setLayout(null);
							    m_pan.setBackground(omega_settings_dialog.feedback_movie_background.color);
							    int v_w = feedback_movie.mp.vw;//getW();
							    int v_h = feedback_movie.mp.vh;//getH();
							    double asp = feedback_movie.mp.aspect;
							    int c_w = card_panel.getWidth();
							    int c_h = card_panel.getHeight();

							    double wwd = 0.81415926535897932 * (c_w * 1.0 / v_w);
							    double hhd = 0.81415926535897932 * (c_h * 1.0 / v_h);
							    double ffd = wwd < hhd ? wwd : hhd;
							    if (ffd > 3.1415926535897932384626) {
								ffd = 3.1415926535897932384626;
							    }

							    int www = (int) (ffd * v_w);
							    int hhh = (int) (ffd * v_h);

							    if (asp <= 0) {
								asp = 1.33;
							    }

							    int nwww = c_w / 2;
							    int nhhh = (int) (nwww / asp);

							    int o_w = (int) ((c_w - nwww) / 2);
							    int o_h = (int) ((c_h - nhhh) / 2);
							    feedback_movie.mp.setSize(nwww, nhhh);
							    feedback_movie.mp.setLocation(o_w, (int) (o_h * 0.851415926535897932384626));

							    OmegaContext.lesson_log.getLogger().warning("movie feedback size: "
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
								    + "");
							    //  1 1 1445 1026 1176.460138443725 835.3274062583127 3 3 721 511

							    m_pan.setVisible(true);
							    card_panel.add(m_pan, "feedback_movie");
							    card_show("feedback_movie");
							    feedback_movie.perform();
							    feedback_movie.waitEnd();
							    SundryUtils.m_sleep(200);
							    card_show("words");
							    card_panel.remove(m_pan);
							    le_canvas.requestFocus();
							    le_canvas.showMsg(null);
							    //							OmegaContext.sout_log.getLogger().info("ERR: " + "now ready mpg (Lesson:1853)");
							    do_prepare = true;
							} catch (Exception ex) {
							    OmegaContext.lesson_log.getLogger().warning("movie feedback failed: " + ex);
							}
						    }
						} finally {
						}
					    } else {
					    }

					    if (true) {
						boolean t_b = getCurrentPupil().getBool("text_on", true);
						boolean i_b = getCurrentPupil().getBool("image_on", true);
						boolean v_b = getCurrentPupil().getBool("speech_on", false);
						MsgItem msgitm =
							new MsgItem('R',
								T.t("Right answer"),
								t_b
									? getCurrentPupil().getString("text",
									T.t("Correct answer"))
									: "",
								"",
								i_b
									? getCurrentPupil().getImageName()
									: null,
								null,
								"");
						if (v_b) {
						    String speech = getCurrentPupil().getString("speech", "");
						    if (speech.length() > 0) {
							sayS(speech);
						    }
						}
						if (t_b || i_b) {
						    mistNoMouse = false;
						    le_canvas.showMsg(msgitm);
						}
					    }
					    if (do_prepare) {
						if (getCurrentPupil().getBool("sign_movie_on", false)) {
						    String mn = getCurrentPupil().getString("sign_movie", null);
						    feedback_movie.dispose();
						    feedback_movie.prepare(mn, null);
						} else if (getCurrentPupil().getBool("movie_on", false)) {
						    String mn = getCurrentPupil().getString("movie", null);
						    feedback_movie.dispose();
						    feedback_movie.prepare(mn, null);
						}
					    }
					    mistNoMouse = false;
					    SundryUtils.m_sleep(getCurrentPupil().getSpeed(500));
					    sayPingAnim();
					    waitSayAll();
					    //					    waitBoxPlay();
					} else { // wrong sentence
					    seq.cnt_sent_wrong++;
					    if (register != null) {
						register.test(":wrong",
							exec_hbox_time,
							seq.getTestText(current_test_mode, false),
							tg.getAllText(),
							seq.getStat(false),
							tg.getAll_Tid_Item());
					    }
					    boolean t_b = getCurrentPupil().getBool("text_on", true);
					    boolean i_b = getCurrentPupil().getBool("image_wrong_on", true);
					    if (false && OmegaConfig.isLIU_Mode()) { // on request LIU
						if (feedback_movie == null) {
						    feedback_movie = new FeedBackMovie(true);
						} else {
						    feedback_movie.dispose();
						}
						String mn = "media/sign/aaa.mpg";
						feedback_movie.prepare(mn, null);
						try {
						    JPanel m_pan = feedback_movie.canvas;
						    m_pan.setLayout(null);
						    m_pan.setBackground(omega_settings_dialog.feedback_movie_background.color);
						    int v_w = feedback_movie.getW();
						    int v_h = feedback_movie.getH();
						    int c_w = card_panel.getWidth();
						    int c_h = card_panel.getHeight();

						    double wwd = 0.81415926535897932 * (c_w * 1.0 / v_w);
						    double hhd = 0.81415926535897932 * (c_h * 1.0 / v_h);
						    double ffd = wwd < hhd ? wwd : hhd;
						    if (ffd > 3.1415926535897932384626) {
							ffd = 3.1415926535897932384626;
						    }

						    int www = (int) (ffd * v_w);
						    int hhh = (int) (ffd * v_h);

						    int o_w = (int) ((c_w - www) / 2);
						    int o_h = (int) ((c_h - hhh) / 2);
						    feedback_movie.mp.setSize(www, hhh);
						    feedback_movie.mp.setLocation(o_w, (int) (o_h * 0.851415926535897932384626));

						    m_pan.setVisible(true);
						    card_panel.add(m_pan, "feedback_movie");
						    card_show("feedback_movie");
						    feedback_movie.perform();
						    feedback_movie.waitEnd();
						    SundryUtils.m_sleep(200);
						    card_show("words");
						    card_panel.remove(m_pan);
						    le_canvas.requestFocus();
						    le_canvas.showMsg(null);
						} catch (Exception ex) {
						    OmegaContext.lesson_log.getLogger().warning("movie feedback failed: " + ex);
						}
					    } else {
						MsgItem msgitm = new MsgItem('W',
							T.t("Sorry, wrong answer"),
							allCorrect.size() == 0 ? correct_text : allCorrect.toString(), //correct_text,
							"",
							i_b
								? getCurrentPupil().getImageNameWrongAnswer()
								: null,
							null,
							T.t("Correct answer is" + (allCorrect.size() == 0 ? "" : " one of") + " :"));
						if (t_b || i_b) {
						    mistNoMouse = false;
						    le_canvas.showMsg(msgitm);
						}
					    }
					}
					le_canvas.setMarkTargetNo();
					seq.next(current_test_mode);
					mistNoMouse = false;
					boolean has_more = seq.getTestText(current_test_mode, false) != null;
					//				    OmegaContext.sout_log.getLogger().info("ERR: " + "====))) " + has_more + ' ' + seq);
					if (has_more) {
					    card_show("anim1");
					    sendMsg("test_cont", null, "L1745");
					} else {
					    le_canvas.showMsg(getResultSummary_MsgItem());

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
						register.has_shown = true;
						register.close();
					    }

					    register = null;
					    card_show("main");
					}
				    } // test_mode
				} // tg is filled
				le_canvas.enableQuitButton(true);
				if (current_test_mode != TM_CREATE) {
				    le_canvas.setNextMarkTarget();
				}
			    }
			}
		    }
		} catch (Exception ex) {
		    OmegaContext.sout_log.getLogger().info("ERR: " + "Lesson: " + ex);
		    ex.printStackTrace();
		}
	    }
	    le_canvas.ready();
	} finally {
	    le_canvas.skip_keycode = false;
	    inExecHbox = false;
	    mistNoMouse = false;
	}
    }

    private String fixWithStar(String t_word) {
        if (t_word.contains("*")) {
            if (t_word.contains(":")) {
		String[] sa = t_word.split(":");
		return sa[1];
	    }
	    return "";
	}
	return t_word;
    }

    private Set<String> getMatchingSameActionSpecific(ActionSpecific action_specific, String correct_text) {
        Set kset = action_specific.hm.keySet();
        String val = null;
        for (Object o : kset ) {
            String k = (String) o;
            if ( k.equals(correct_text)) {
		val = (String) action_specific.hm.get(k);
	    }
	}
	Set<String> ss = new HashSet<>();
	for (Object o : kset ) {
           String k = (String) o;
           String v = (String) action_specific.hm.get(k);
           if (v.equals(val))
               ss.add(k);
	}
	return ss;
    }

    boolean last_story_flag = false;

    class MyRunnable implements Runnable {

	Lesson le;

	MyRunnable(Lesson le) {
	    this.le = le;
	}

	public void run() {
	}
    }

    private void exec_play(Target tg, boolean waiting) {
	le_canvas.hideMsg();
	//		le_canvas.init();

	if (tg.isTargetFilled()) {
	    le_canvas.removeHilitedBox();
	    String actions = tg.getActionFileName(999); // all actions from all target boxes
	    String[] action_sa = SundryUtils.split(actions, ",");


	    int larglen = tg.getLessonArgLength();

	    String[] actA = tg.getAll_Lid_Item();
	    String[] actTextA = tg.getAll_TextVars_Item(); // text<PARAGRAF>v1<PARAGRAF>v2<PARAGRAF>v3<PARAGRAF>sound
	    String sound_list = tg.getAll_Sound_Item(); // sound,sound...
	    String[] pathA = tg.getAll_Lid_Target();

	    lesson_log.getLogger().info("ANIMDATA is actA="
		    + SundryUtils.a2s(actA) + " actTextA="
		    + SundryUtils.a2s(actTextA) + " pathA="
		    + SundryUtils.a2s(pathA) + " sound="
		    + sound_list + " action_A="
		    + SundryUtils.a2s(action_sa));
	    if (action == null) {
		action = new AnimAction();
		card_panel.add(action.getCanvas(), "anim1");
	    }

	    try {
		for (int anim_i = 0; anim_i < action_sa.length; anim_i++) {
		    boolean is_last = false;
		    if (anim_i == action_sa.length - 1) {
			is_last = true;
		    }
		    String action_s = action_sa[anim_i];

		    String all_text = tg.getAllText();

		    lesson_log.getLogger().info("loop actions: " + anim_i + ' ' + is_last + ' ' + action_s + ' ' + all_text);

		    if (action_specific.is(all_text)) {
			element_root = action.prefetch(action_s);
			if (element_root == null)
			    ; // JOptionPane.showMessageDialog(ApplContext.top_frame,
			//			      T.t("Can't find animation: " + action_s));

			if (!true) {
			    System.gc();
			}
			boolean anim_twice = getCurrentPupil().getBool("repeatanim", false);
			startProgress();
			JPanel pan = performMpgAction(all_text, action_s, actA, pathA, tg);

			if (!edit) {
			    if (is_last) {
				tg.releaseAllT_Items();
			    }
			}

			card_show("words");
			if (pan != null) {
			    pan.setVisible(false);
			}
			if (pan != null) {
			    card_panel.remove(pan);
			}

			if (!edit) {
			    if (is_last) {
				le_canvas.endLastAction();
			    } else {
				le_canvas.endAction();
			    }
			}

			le_canvas.requestFocus();
			le_canvas.showMsg(null);

			// 			if ( is_last && current_test_mode == TM_CREATE )
			// 			    le_canvas.waitReplyAction((AnimAction)action,
			// 						      all_text,
			// 						      getCurrentPupil().getBool("showSentence", true));
			le_canvas.requestFocus();

		    } else { // normal animation
			element_root = action.prefetch(action_s);
			if (element_root == null) {
			    JOptionPane.showMessageDialog(ApplContext.top_frame,
				    T.t("Can't find animation: " + action_s));
			    continue;
			}
			if (!true) {
			    System.gc();
			}

			action.show();

			HashMap variables_hm = new HashMap();
			tg.putAll_TextVars_Item(variables_hm);

			action.getHm().put("speed", getCurrentPupil().getSpeed(1000));
			action.getHm().put("anim_background", omega_settings_dialog.anim_background.color);
			action.getHm().put("anim_colors", le_canvas.colors);
			action.getHm().put("variables", variables_hm);
			boolean anim_twice = getCurrentPupil().getBool("repeatanim", false);

			card_show("anim1");
			action.perform(window,
				action_s,
				actA,
				pathA,
				anim_twice ? 1 : 0,
				new Runnable() {
				    public void run() {
					//card_show("anim1");
					OmegaContext.sout_log.getLogger().info("ERR: " + "start hook");
				    }
				});

			if (anim_twice) {
			    SundryUtils.m_sleep(getCurrentPupil().getSpeed(800));
			    action.perform(window,
				    action_s,
				    actA,
				    pathA,
				    0,
				    null);
			}

			String allText = tg.getAllText();
			saveRecastAction(le_canvas.getLessonName(), action_s, actA, actTextA, sound_list, pathA, true, tg, is_last, allText);
			SentenceList sent_li = (SentenceList) story_hm.get("sentence_list");
			ArrayList ss_li = sent_li.sentence_list;
			//			OmegaContext.sout_log.getLogger().info("ERR: " + "ALL TEXT1 " + all_text);
			all_text = tg.getAllText();
			//			OmegaContext.sout_log.getLogger().info("ERR: " + "ALL TEXT2 " + all_text);
			if (is_last) {
			    ss_li.add(all_text);
			}
			sent_li.lesson_name = le_canvas.getLessonName();

			lesson_log.getLogger().info("SENTENCE " + ss_li);
			OmegaContext.story_log.getLogger().info("added sent 2214 " + sent_li.lesson_name
				+ ' ' + all_text);

			if (tg.getStoryNext() == null) {
			    if (ss_li.size() <= 1) {
				story_hm.put("sentence_list", new SentenceList());
				OmegaContext.story_log.getLogger().info("new sent_list 2214 " + ss_li);
			    } else {
				OmegaContext.story_log.getLogger().info("Lst in story  2214 " + ss_li);
				last_story_flag = true;
			    }
			} else {
			    last_story_flag = false;
			}

			final Target ftg = tg;
			class MyRA implements Runnable {

			    public void run() {
				OmegaContext.sout_log.getLogger().info("ERR: " + "MyRA called");
				sayAll(ftg);
			    }
			}
			MyRA myra = new MyRA();

			if ( waiting ) {
			    OmegaContext.sout_log.getLogger().info("ERR: " + "waitReply? " + is_last + ' ' + current_test_mode);
			    if (is_last && current_test_mode == TM_CREATE) {
				String end_code_s = le_canvas.waitReplyAction((AnimAction) action,
					all_text,
					getCurrentPupil().getBool("showSentence", true),
					myra);
				OmegaContext.sout_log.getLogger().info("ERR: " + "Lesson: end_code_s " + end_code_s);
				if (end_code_s.equals("left")) {
				    action.perform(window,
					    action_s,
					    actA,
					    pathA,
					    0,
					    null);
				    end_code_s = le_canvas.waitReplyAction((AnimAction) action,
					    all_text,
					    getCurrentPupil().getBool("showSentence", true),
					    myra);
				    OmegaContext.sout_log.getLogger().info("ERR: " + "Lesson: end_code_s2 " + end_code_s);
				}
			    }
			}
			if (!edit) {
			    if (is_last) {
				tg.releaseAllT_Items();
			    }
			}
			if (is_last) {
			    le_canvas.endLastAction();
			} else {
			    le_canvas.endAction();
			}
			le_canvas.requestFocus();
		    }
		}
		if (register != null) {
		    register.restart();
		}
		if (last_story_flag) {
		    sentence_canvas.setRead(false);
		    card_show("sent");
		} else if (current_test_mode != TM_CREATE) {
		    card_show("words");
		}
	    } catch (Exception ex) {
		OmegaContext.sout_log.getLogger().info("ERR: " + "@@@@@p " + ex);
		ex.printStackTrace();
	    }
	}
    }

    private JPanel performMpgAction(String all_text,
				    String action_s,
				    String[] actA,
				    String[] pathA,
				    Target tg) {
	JPanel pan = null;
	try {
	    if (mpg_action == null) {
		mpg_action = new MpgAction();
	    } else {
		Log.getLogger().info("--------------- probably not to come here");
		mpg_action.reset();
	    }
	    mpg_action.prefetch(action_specific.getAction(all_text), window.getWidth(), window.getHeight());

	    if (!false) {
		mpg_action.mpg_player.fxp.waitReady();
		pan = mpg_action.getCanvas();
		//pan.setLayout(null);
		pan.setBackground(omega_settings_dialog.action_movie_background.color);
		int v_w = mpg_action.getW();
		int v_h = mpg_action.getH();
		int c_w = card_panel.getWidth();
		int c_h = card_panel.getHeight();
		Log.getLogger().info("mpg size " + v_w + ' ' + v_h + ' ' + c_w + ' ' + c_h);
		double wwd = 0.81415926535897932 * (c_w * 1.0 / v_w);
		double hhd = 0.81415926535897932 * (c_h * 1.0 / v_h);

		double ffd = wwd < hhd ? wwd : hhd;
		if (ffd > 3.1415926535897932384626) {
		    ffd = 3.1415926535897932384626;
		}
		int www = (int) (ffd * v_w);
		int hhh = (int) (ffd * v_h);
		//mpg_action.setSize((int) www, (int) hhh);
		Log.getLogger().info("mpg size " + www + ' ' + hhh);
		int o_w = (int) ((c_w - www) / 2);
		int o_h = (int) ((c_h - hhh) / 2);

		//mpg_action.setLocation(o_w, (int) (o_h * 0.851415926535897932384626));
		Log.getLogger().info("mpg  loc " + o_w + ' ' + o_h);
	    }

	    pan.setVisible(true);
	    card_panel.add(pan, "msg_anim");
	    //log 			OmegaContext.sout_log.getLogger().info("ERR: " + "CCnt " + card_panel.getComponentCount());
	    card_show("msg_anim");
	    mpg_action.show();
	    mpg_action.getHm().put("speed", getCurrentPupil().getSpeed(1000));
	    progress.dismiss();
//	    mpg_action.setParentWH(c_w, c_h);
	    mpg_action.sentence = all_text;
	    OmegaContext.sout_log.getLogger().info("ERR: " + "sentence " + all_text);
	    boolean anim_twice = getCurrentPupil().getBool("repeatanim", false);
	    boolean show_sentence = getCurrentPupil().getBool("showSentence", true);
	    mpg_action.show_sentence = show_sentence;
	    if (current_test_mode_group == TMG_TEST) {
		mpg_action.show_sentence = false;
	    }

	    final Target ftg = tg;
	    class MyRA implements Runnable {

		public void run() {
		    OmegaContext.sout_log.getLogger().info("ERR: " + "MyRA called");
		    sayAll(ftg);
		}
	    }
	    MyRA myra = new MyRA();

	    mpg_action.perform(window,
		    action_s,
		    actA,
		    pathA,
		    anim_twice ? 1 : 0,
		    myra);

	    SundryUtils.m_sleep(getCurrentPupil().getSpeed(400));
	    if (anim_twice) {
		mpg_action.reset();
		mpg_action.perform(window,
			action_s,
			actA,
			pathA,
			0,
			myra);
	    }
	    mpg_action.dispose();
	    mpg_action = null;
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return pan;
    }

    private void testDialog() {
	String[] choise = new String[]{"Printer",
		"Story",
		"Anim message",
		"Words message"};

	global_skipF(true);
	int a = JOptionPane.showOptionDialog(ApplContext.top_frame,
		T.t("What kind of test?"),
		T.t("Omega - System Test"),
		JOptionPane.OK_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		choise,
		choise[0]);

	global_skipF(false);
	if (a == JOptionPane.CLOSED_OPTION) {
	    return;
	}
	String ccard = current_card;

	switch (a) {
	    case 0:
		TEST_print();
		break;
	    case 1:
		TEST_story();
		break;
	    case 2:
		TEST_anim();
		break;
	    case 3:
		TEST_words();
		break;
	}

	waitHitKey(10);
	card_show(ccard);


    }

    public static class PlayData implements Serializable {
	String lesson_name;
	String action_s;
	String[] actA;
	String[] actTextA;
	String[] pathA;
	String sound_list;
	boolean is_last;
	private final String allText;

	PlayData(String lesson_name,
		 String action_s,
		 String[] actA,
		 String[] actTextA,
		 String sound_list,
		 String[] pathA,
		 boolean is_last,
		 String allText) {
	    this.lesson_name = lesson_name;
	    this.action_s = action_s;
	    this.actA = actA;
	    this.actTextA = actTextA;
	    this.pathA = pathA;
	    this.sound_list = sound_list;
	    this.is_last = is_last;
	    this.allText = allText;
	}

	public String theWord() {
	    return allText;
	}

	public String toString() {
	    return lesson_name + ','
		    + action_s + ','
		    + SundryUtils.a2s(actA) + ','
		    + SundryUtils.a2s(actTextA) + ','
		    + sound_list + ','
		    + SundryUtils.a2s(pathA)+ ','
		    + allText;
	}

    }

    PlayDataList play_data_list = new PlayDataList();
    PlayDataList play_data_list_is_last = new PlayDataList();

    public void playFromDataList(PlayDataList playDataList) {
	ArrayList al = playDataList.arr;
	card_show("anim1");
	action.show();
	action.getHm().put("speed", getCurrentPupil().getSpeed(1000));
	action.getHm().put("anim_background", omega_settings_dialog.anim_background.color);
	action.getHm().put("anim_colors", le_canvas.colors);
	Iterator it = al.iterator();
	while (it.hasNext()) {
	    if (hit_key == '\033') {
		return;
	    }
	    PlayData pd = (PlayData) it.next();
	    action.prefetch(pd.action_s);
	    action.perform(window,
		    pd.action_s,
		    pd.actA,
		    pd.pathA,
		    0,
		    null);
	    saveRecastAction(le_canvas.getLessonName(),
		    pd.action_s,
		    pd.actA,
		    pd.actTextA,
		    pd.sound_list,
		    pd.pathA,
		    false,
		    null,
		    pd.is_last,
		    pd.allText);
	}
    }

    private void TEST_print() {
	if (false) {
	    try {
		PrintMgr pm = new PrintMgr();
		//	    pm.list(true);
		ArrayList ss_li = new ArrayList();
		ss_li.add("Raden 1");
		ss_li.add("Rad 2");
		ss_li.add("Ldkfj lkdjf ldksjf ldksjf lkdsjf lsdkjf ldskjf ldskjf dslkjf sdlkfh sdkjfh dskjf hsdkfjhds SIST.");
		ss_li.add("SLUT");
		pm.print(null, "Omega TEST", ss_li, "TITLE");
	    } catch (Exception ex) {
		OmegaContext.sout_log.getLogger().info("ERR: " + "PRINTER " + ex);
	    }
	}
    }

    void TEST_story() {
	OmegaContext.sout_log.getLogger().info("ERR: " + "TEST story");
	card_show("sent");
	sentence_canvas.showMsg(null);

	int key = waitHitKey(1);

	ArrayList al = new ArrayList();
	al.add("Den talande reven rev en annan rev");
	al.add("");
	al.add("Flamingon flyger lagom");
	al.add("Inga vandrande pinnar skriver klart denna test");
	al.add("SLUT");
	sentence_canvas.showMsg(al);
	sentence_canvas.showMsgMore();
    }

    void TEST_anim() {
	card_show("anim1");
	le_canvas.showMsg(new MsgItem('S',
		"Test Statistics",
		"Correct",
		"Wrong",
		"XX",
		"YY",
		null));
	SundryUtils.m_sleep(2000);
    }

    void TEST_words() {
	card_show("words");
	le_canvas.showMsg(getResultSummary_MsgItem());
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

    PrintService print_service;

    public void printFromDataList(PlayDataList playDataList) {
	try {
	    ArrayList al = playDataList.arr;
	    PrintMgr pm = new PrintMgr();
	    //	    pm.list(true);
	    SentenceList sent_li = (SentenceList) story_hm.get("sentence_list");
	    ArrayList ss_li = sent_li.sentence_list;
	    OmegaContext.story_log.getLogger().info("printed 2402 " + ss_li);
	    global_skipF(true);
	    pm.prepare("Omega", ss_li, sent_li.lesson_name);
	    PrinterJob job = pm.getPrintJob();
	    if (job != null) {
		pm.doThePrint(job);
	    }
	} catch (Exception ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "PRINTER " + ex);
	    ex.printStackTrace();
	}
	global_skipF(false);
    }

    int waitHitKey(int a) {
	int start_cnt_hit_keyOrButton = cnt_hit_keyOrButton;
	while (cnt_hit_keyOrButton < start_cnt_hit_keyOrButton + a) {
	    SundryUtils.m_sleep(100);
	}
	return hit_key;
    }

    public void listenFromDataList(PlayDataList playDataList/*
	     * , ListenListener lili
             */) {
	// 	lili.init();
	sentence_canvas.buttonsEnable(false);
	try {
	    OmegaContext.story_log.getLogger().info("listened 2411 " + playDataList.arr);
	    Iterator it = playDataList.arr.iterator();
	    while (it.hasNext()) {
		sentence_canvas.showMsgMore();
		int key = waitHitKey(1);
		if (key == '\033') {
		    sentence_canvas.showMsg(null);
		    return;
		}
		PlayData pd = (PlayData) it.next();

		if ( OmegaConfig.tts ) {
		    String lang = OmegaContext.getLessonLang();
		    if ( TTS.say(lang, pd.theWord(), true) )
			continue;
		}

		OmegaContext.story_log.getLogger().info("PD is " + pd);
		String[] soundA = SundryUtils.split(pd.sound_list, ",");
		for (int i = 0; i < soundA.length; i++) {
		    String sound = soundA[i];
		    APlayer ap = APlayer.createAPlayer(getCurrentPupil().getStringNo0("languageSuffix", null),
			    sound,
			    null,
			    "SA_" + i);
		    SundryUtils.m_sleep(50);
		    ap.playWait();
		    SundryUtils.m_sleep(50);
		}
		//		APlayer.unloadAll("SA_[0-9]*");
		SundryUtils.m_sleep(500);
	    }
	    sentence_canvas.showMsgMore();
	} finally {
	    sentence_canvas.buttonsEnable(true);
	}
	//	lili.done();
    }

    private void saveRecastAction(String lesson_name,
				  String action_s,
				  String[] actA,
				  String[] actTextA,
				  String sound_list,
				  String[] pathA,
				  boolean add_in_playlist,
				  Target tg,
				  boolean is_last,
				  String allText) {
	if (add_in_playlist) {
	    PlayData play_data = new PlayData(lesson_name, action_s, actA, actTextA, sound_list, pathA, is_last, allText);
	    play_data_list.add(play_data);
	    if (is_last) {
		play_data_list_is_last.add(play_data);
	    }
	}
	try {
	    for (int i = 0; i < actA.length; i++) {
		String txt;
		if (actTextA.length <= i) {
		    txt = "";
		} else {
		    txt = actTextA[i];
		}
		putDynamic(lesson_name, actA[i], txt, pathA[i]);
	    }
	    if (tg != null) {
		for (int i = 0; i < tg.get_howManyT_Items(); i++) {
		    Target.T_Item t_item = tg.getT_Item(i);
		    String actor_text = t_item.getTextVarsOrNull();
		    String lid = t_item.item.getLid();
		    putDynamic(lesson_name, lid, actor_text, "W" + (i + 1));
		}
	    }
	} catch (Exception ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "Cant put dynamic " + ex);
	}
    }

    public static int cnt_hit_keyOrButton = 0, hit_key;

    void putDynamic(String thisLessonName,
		    String actor_lid,
		    String actor_text, // § separated    ////    UTF-8
		    String timeline_lid) {
	try {
	    String sa[] = actor_text.split("§");  ////    UTF-8
	    // 	    OmegaContext.sout_log.getLogger().info("ERR: " + "put dyn " + thisLessonName + ' ' +
	    // 		 actor_lid + ' ' +
	    // 		 actor_text + ' ' +
	    // 		 timeline_lid);
	    story_hm.put(thisLessonName + '.' + timeline_lid + ".text", sa[0]);
	    story_hm.put(thisLessonName + '.' + timeline_lid + ".var-1", sa[1].substring(1));
	    story_hm.put(thisLessonName + '.' + timeline_lid + ".var-2", sa[2].substring(1));
	    story_hm.put(thisLessonName + '.' + timeline_lid + ".var-3", sa[3].substring(1));
	    story_hm.put(thisLessonName + '.' + timeline_lid + ".sound", sa[4].substring(1));
	    story_hm.put(thisLessonName + '.' + timeline_lid + ".Lid", actor_lid);
	    story_hm.put(thisLessonName + '.' + timeline_lid + ".actor", actor_lid);
	    OmegaContext.story_log.getLogger().info("put dynact " + story_hm);
	} catch (Exception ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "put dynact " + actor_text + ' ' + ex);
	}
    }

    public void runLessons(Window w, JPanel mpan, String fn, boolean edit, boolean smaller) {
	this.edit = edit;
	le_canvas.edit = edit;
	window = w;

	KeyboardFocusManager.setCurrentKeyboardFocusManager(new DefaultKeyboardFocusManager() {
	    char last_state = '_';
	    char state = 'r';
	    boolean first_tr = false;
	    boolean P = !false;

	    public boolean dispatchKeyEvent(KeyEvent e) {
		if (skip_F) {
		    return super.dispatchKeyEvent(e);
		}

		char ch = e.getKeyChar();
		int kc = e.getKeyCode();
		String cc = current_card;

		boolean do_own = false;
		boolean was_first = false;

		boolean do_it = false;

		if (e.getID() == KeyEvent.KEY_PRESSED && (OmegaConfig.isKeySelect(kc)
			|| OmegaConfig.isKeyESC(kc)
			|| OmegaConfig.isKeyNext(kc))) {
		    hit_key = kc;
		    cnt_hit_keyOrButton++;
		}

		// 		    if ( e.getKeyCode() == KeyEvent.VK_F1 ) {
		// 			showHelp(OmegaContext.HELP_STACK.get());
		// 		    }

		if (kc != 16 && kc != 17 && kc != 18) {
		    if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (P) {
			    OmegaContext.sout_log.getLogger().info("ERR: " + "KEY: P " + current_card + ' ' + e.getID()
				    + " '" + ch + "' " + kc + "" + ' ' + state + last_state);
			}
			if (state == 'r') {
			    last_state = state;
			    state = 'p';
			    do_own = true;
			} else {
			}
			if (e.getKeyCode() == KeyEvent.VK_F1) {
			    showHelp("");

//				showHelp(OmegaContext.HELP_STACK.get());
			}

			if (e.getKeyCode() == KeyEvent.VK_F2 && (e.isShiftDown() || e.isControlDown())) {
			    if (cc.equals("pupil")) {
				pupil_canvas.changeBehaviour();
			    }
			}

			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			    if (cc.equals("msg_anim") && mpg_action != null) {
				mpg_action.ownKeyCode('l', false);
			    }
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			    if (cc.equals("words") && le_canvas != null) {
				le_canvas.ownKeyCode('r', false);
			    }
			}
			if (e.getKeyCode() == KeyEvent.VK_UP) {
			    if (cc.equals("msg_anim") && mpg_action != null) {
				mpg_action.ownKeyCode('u', false);
			    }
			}

			if (e.getKeyCode() == KeyEvent.VK_F5 && (e.isControlDown() || e.isShiftDown())) {
			    SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    global_skipF(true);
				    omega_settings_dialog.setVisible(true);
				    global_skipF(false);
				}
			    });
			}
			if (e.getKeyCode() == KeyEvent.VK_F12 && e.isControlDown() && e.isShiftDown()) {
			    SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    sendMsg("test_dialog", "", "loadTest1");
				}
			    });
			}
		    }

		    if (e.getID() == KeyEvent.KEY_TYPED) {
			if (P) {
			    OmegaContext.sout_log.getLogger().info("ERR: " + "KEY: T " + current_card + ' ' + e.getID() + " '" + ch + "' " + kc + "" + ' ' + state + last_state);
			}
			state = 't';
			if (first_tr == false) {
			    first_tr = true;
			    was_first = true;
			}
		    }

		    if (e.getID() == KeyEvent.KEY_RELEASED) {
			last_state = state;
			state = 'r';
			if (P) {
			    OmegaContext.sout_log.getLogger().info("ERR: " + "KEY: R " + current_card + ' ' + e.getID() + " '" + ch + "' " + kc + "" + ' ' + state + last_state);
			}
			first_tr = false;
		    }
		} else {
		    do_it = true;
		}

		if (P) {
		    OmegaContext.sout_log.getLogger().info("ERR: " + "KEY: state " + state + last_state + ' ' + do_own);
		}

		boolean ret = true;
		boolean dispatch = true;

		if (state == 'p' && last_state == 'r'
			|| state == 't' && last_state == 'r' && was_first
			|| state == 'r' && last_state == 't') {
		    do_it = true;
		}

		if (do_it && do_own) {
		    if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (kc == 38 || kc == 40) {
			    if ("pupil".equals(cc)) {
				if (pupil_canvas != null) {
				    ret = pupil_canvas.ownKeyCode(kc, e.isShiftDown());
				}
				dispatch = true;
			    }
			}

			if (OmegaConfig.isKeyNext(kc)
				|| OmegaConfig.isKeySelect(kc)
				|| OmegaConfig.isKeyESC(kc)) {
			    if ("anim1".equals(cc)) {
				dispatch = true;
			    }
			    if ("words".equals(cc)) {
				if (le_canvas != null) {
				    ret = le_canvas.ownKeyCode(kc, e.isShiftDown());
				}
				dispatch = false;
			    }
			    if ("pupil".equals(cc)) {
				if (pupil_canvas != null) {
				    ret = pupil_canvas.ownKeyCode(kc, e.isShiftDown());
				}
				dispatch = true;
			    }
			    if ("msg_anim".equals(cc)) {
				if (mpg_action != null) {
				    ret = mpg_action.ownKeyCode(kc, e.isShiftDown());
				}
			    }
			    if ("sent".equals(cc)) {
				if (sentence_canvas != null) {
				    ret = sentence_canvas.ownKeyCode(kc, e.isShiftDown());
				}
			    }
			    if ("main".equals(cc)) {
				ret = lemain_canvas.ownKeyCode(kc, e.isShiftDown());
			    }
			}
		    }
		}
		if (do_it && dispatch) {
		    if (P) {
			OmegaContext.sout_log.getLogger().info("ERR: " + "do dispatch ");
		    }
		    return super.dispatchKeyEvent(e);
		}
		return true;
	    }
	});

	if (fn != null) {
	    is_testing = true;
	}

	window.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent ev) {
		//		    savePrefetch();
		globalExit = true;
		sendMsg("exitLesson", "", "");
		//System.exit(0);
	    }
	});

	JPanel pan = new JPanel();
	if (edit) {
	    lep = new LessonEditorPanel(le_canvas);
	    le_canvas.lep = lep;
	    mpan.add(lep, BorderLayout.NORTH);
	}
	mpan.add(pan, BorderLayout.CENTER);
	card = new CardLayout();
	pan.setLayout(card);
	card_panel = pan;

	pan.add(le_canvas, "words");
	pan.add(lemain_canvas, "main");
	pan.add(pupil_canvas, "pupil");
	pan.add(sentence_canvas, "sent");

	window.setVisible(true);

	smaller |= OmegaConfig.smaller;

	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	if (smaller) {
	    d.width = 600;
	    d.height = 450;
	    window.setSize(d);
	} else {
	    window.setSize(d);
	    window.setLocation(0, 0);
	}

	if (edit) {
	    card_show("words");
	} else {
	    card_show("pupil");
	}
	if (smaller == false && window instanceof JFrame) {
	    ((JFrame) window).setExtendedState(JFrame.MAXIMIZED_BOTH);
	    if (ApplLesson.isMac) {
		try {
		    Class appClass = Class.forName("com.apple.eawt.Application");
		    Class params[] = new Class[]{};

		    Method getApplication = appClass.getMethod("getApplication", params);
		    Object application = getApplication.invoke(appClass);
		    Method requestToggleFulLScreen = application.getClass().getMethod("requestToggleFullScreen", Window.class);

		    requestToggleFulLScreen.invoke(application, window);
//		    Application.getApplication().requestToggleFullScreen(window);
		} catch (Exception e) {
		    Log.getLogger().warning("An exception occurred while trying to toggle full screen mode");
		}
	    }
	}

	for (; ; ) {
	    try {
		execLesson(fn);
		if (globalExit)
		    return;
		break;
	    } catch (Exception ex) {
		OmegaContext.sout_log.getLogger().info("ERR: " + "OOOOPPPSS " + ex);
		ex.printStackTrace();
	    }
	}
    }

    public void displayColor(String name) {
	// 	String fn = "default.omega_colors";
	// 	fn = getCurrentPupil().getString("theme", fn);
	String fn = pupil_settings_dialog.getSelectedColorFile();

	BaseCanvas l = canvases.get(name);
	try {
	    if (l != null) {
		global_skipF(true);
		ColorDisplay cd = new ColorDisplay(l.colors, name);
		cd.setVisible(true);
		if (cd.select) {
		    l.colors = cd.colors;
		    l.updateDisp();
		    l.repaint();

		    if (false) { // do not select file
			ChooseColorFile choose_f = new ChooseColorFile();

			String url_s = null;
			int rv = choose_f.showDialog(null, T.t("Save"));
			if (rv == JFileChooser.APPROVE_OPTION) {
			    File file = choose_f.getSelectedFile();
			    url_s = Files.toURL(file);
			    if (!url_s.endsWith("." + ChooseColorFile.ext)) {
				url_s = url_s + "." + ChooseColorFile.ext;
			    }

			    String tfn = Files.mkRelativeCWD(url_s);
			    saveSettings(tfn);
			}
		    } else {
			saveSettings(fn);
		    }
		}
	    }
	} finally {
	    global_skipF(false);
	}
    }

    public void saveColor() {
	ChooseColorFile choose_f = new ChooseColorFile();

	try {
	    global_skipF(true);
	    String url_s = null;
	    int rv = choose_f.showDialog(null, T.t("Save"));
	    if (rv == JFileChooser.APPROVE_OPTION) {
		File file = choose_f.getSelectedFile();
		url_s = Files.toURL(file);
		if (!url_s.endsWith("." + ChooseColorFile.ext)) {
		    url_s = url_s + "." + ChooseColorFile.ext;
		}

		String tfn = Files.mkRelativeCWD(url_s);

		Element el = getSettingsElement();
		Save.save(tfn, el);
	    }
	} finally {
	    global_skipF(false);
	}
    }

    int where(String s, String[] sa) {
	for (int i = 0; i < sa.length; i++) {
	    if (sa[i].equals(s)) {
		return i;
	    }
	}
	return -1;
    }

    public void setTestMatrix(String[] sentA, int[][] tmm) {
	seq.setFromMatrix(sentA, tmm);
    }

    public int[][] getTestMatrix(String[] all_sentence) {
	return seq.getTestMatrix(all_sentence);
    }

    public static boolean skip_F = false;

    public static void global_skipF(boolean b) {
	skip_F = b;
    }

    public boolean isTestMode() {
	return current_test_mode_group == TMG_TEST;
    }

    void showHelp(String more) {
	ApplLesson.help.showManualL(null);
    }

    static int CnT = 200;

    String tryLessonLanguages(String s) {
	File dot = new File(OmegaContext.omegaAssets("."));
	String[] scanned_lang = dot.list(new java.io.FilenameFilter() {
	    public boolean accept(File dir, String name) {

		if (CnT == 0) {
		    if (name.startsWith("lesson-")) {                         // LESSON_DIR
			return true;
		    }
		    return false;
		}

		CnT--;
		if (name.startsWith("lesson-")) {                         // LESSON_DIR
		    OmegaContext.lesson_log.getLogger().info("Try scan lesson lang: f " + dir.getName() + ' ' + name);
		    return true;
		}
		OmegaContext.lesson_log.getLogger().info("Try scan lesson lang: T " + dir.getName() + ' ' + name);
		return false;
	    }
	});
	for (int i = 0; i < scanned_lang.length; i++) {
	    String l = scanned_lang[i].substring(7);
	    scanned_lang[i] = l;
	    if (l.equals(s)) {
		OmegaContext.lesson_log.getLogger().info("return : " + s);
		return s;
	    }
	}
	OmegaContext.lesson_log.getLogger().info("return : " + "en");
	return "en";
    }
}
