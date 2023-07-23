package com.femtioprocent.omega.anim.appl;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.anim.panels.cabaret.CabaretPanel;
import com.femtioprocent.omega.anim.panels.cabaret.WingsPanel;
import com.femtioprocent.omega.anim.panels.timeline.TimeLineComponent;
import com.femtioprocent.omega.anim.panels.timeline.TimeLinePanel;
import com.femtioprocent.omega.anim.panels.timeline.TimeLinePanelAdapter;
import com.femtioprocent.omega.anim.tool.path.AllPath;
import com.femtioprocent.omega.anim.tool.path.Path;
import com.femtioprocent.omega.anim.tool.timeline.TimeLine;
import com.femtioprocent.omega.anim.tool.timeline.TimeMarker;
import com.femtioprocent.omega.appl.OmegaStartManager;
import com.femtioprocent.omega.graphic.render.Wing;
import com.femtioprocent.omega.lesson.appl.ApplContext;
import com.femtioprocent.omega.media.audio.APlayer;
import com.femtioprocent.omega.media.images.xImage;
import com.femtioprocent.omega.servers.httpd.Server;
import com.femtioprocent.omega.subsystem.Httpd;
import com.femtioprocent.omega.swing.ToolAction;
import com.femtioprocent.omega.swing.ToolExecute;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class AnimEditor extends JFrame {
    JMenuBar mb;
    Container c;
    public ToolBar_AnimEditor toolbar_cmd;
    public ToolBar_AnimEditor toolbar_top;
    public TimeLinePanel tlp;
    public TimeLineComponent tlc;
    public AnimRuntime arun;

    static OmegaContext context;

    Server httpd;

    static public Anim_HelpSystem help;

    public CabaretPanel cabaret_panel;
    public WingsPanel wings_panel;

    Anim_Repository anim_repository;

    public AnimContext a_ctxt = new AnimContext(this);

    JPanel cab_wing_pan;
    CardLayout cab_wing_pan_card;

    boolean exit_on_close;

    public AnimEditor(boolean verbose) {
	super("Omega - " + T.t("Animator editor"));
	if (verbose)
	    OmegaConfig.T = true;
//	setVisible(true);

	JFrame f = this;

	ApplContext.top_frame = ApplContext.top_frame == null ? this : ApplContext.top_frame;

	f.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent ev) {
		maybeClose();
	    }
	});

	AnimContext.top_frame = this;
	OmegaContext.init("Httpd", null);
	httpd = ((Httpd) (OmegaContext.getSubsystem("Httpd"))).httpd;
	init(true, null);
    }

    private void maybeClose() {
	OmegaContext.serr_log.getLogger().info("LessonRuntime want to close " + (ApplContext.top_frame == this) + ' ' + ApplContext.top_frame + '\n' + this);
	if (ApplContext.top_frame == this)
	    System.exit(0);
    }

    public AnimEditor(String fname) {         // from lesson
	super("Omega - " + T.t("Animator editor") + ": " + fname);

	AnimContext.top_frame = this;
	OmegaContext.init("Httpd", null);
	httpd = ((Httpd) (OmegaContext.getSubsystem("Httpd"))).httpd;
	init(false, fname);
    }

    public void processEvent(AWTEvent e) {
	if (e.getID() != WindowEvent.WINDOW_CLOSING)
	    super.processEvent(e);
	else {
	    String s = "";
	    if (a_ctxt != null && AnimContext.ae != null && a_ctxt.ae.isDirty())
		s = "\n" + T.t("Changes not saved");

	    if (exit_on_close) {
		int sel = JOptionPane.showConfirmDialog(AnimEditor.this,
			T.t("Are you sure to exit Omega?") +
				s);
		if (sel == 0)
		    super.processEvent(e);
	    } else {
		int sel = JOptionPane.showConfirmDialog(AnimEditor.this,
			T.t("Are you sure to close Anim Editor?") +
				s);
		if (sel == 0) {
		    try {
			a_ctxt.arun.clean();
		    } catch (Exception _ex) {
		    }

		    super.processEvent(e);
		}
	    }
	}
    }

    void addShiftAcc(JMenu m, char key) {
	JMenuItem mi = m.getItem(m.getMenuComponentCount() - 1);
	mi.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.SHIFT_DOWN_MASK, false));
    }

    void addCtrlAcc(JMenu m, char key) {
	JMenuItem mi = m.getItem(m.getMenuComponentCount() - 1);
	mi.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK, false));
    }

    private void init(boolean exoc, final String fname) {
	exit_on_close = exoc;

	anim_repository = new Anim_Repository();

	help = new Anim_HelpSystem();

	c = getContentPane();
	c.setLayout(new BorderLayout());

	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent ev) {
//log		    OmegaContext.sout_log.getLogger().info(":--: " + "closing");
		if (false && exit_on_close) {
		    System.exit(0);
		} else
		    setVisible(false);
	    }
	});

	ToolExecute ae_texec = new ToolExecute() {
	    public void execute(String cmd) {
		if (OmegaConfig.T)
		    OmegaContext.sout_log.getLogger().info(":--: " + "AnimEditor.texec: execute " + cmd);

		if ("exit".equals(cmd)) {
		    String s = "";
		    if (a_ctxt != null && a_ctxt.ae != null && a_ctxt.ae.isDirty())
			s = "\n" + T.t("Changes not saved");

		    if (false && exit_on_close) {
			int sel = JOptionPane.showConfirmDialog(AnimEditor.this,
				T.t("Are you sure to exit Omega?") +
					s);
//log			    OmegaContext.sout_log.getLogger().info(":--: " + "" + sel);
			if (sel == 0)
			    System.exit(0);
		    } else {
			int sel = JOptionPane.showConfirmDialog(AnimEditor.this,
				T.t("Are you sure to close Anim Editor?") +
					s);
//log			    OmegaContext.sout_log.getLogger().info(":--: " + "" + sel);
			if (sel == 0) {
			    try {
				a_ctxt.arun.clean();
			    } catch (Exception _ex) {
			    }
			    setVisible(false);
			    maybeClose();
			}
		    }
		} else if ("new".equals(cmd)) {
		    if (isDirty()) {
			int sel = JOptionPane.showConfirmDialog(AnimEditor.this,
				T.t("Are you sure to start with a new animation") + "\n" +
					T.t("Changes are unsaved!"));
			if (sel == 0)
			    initNew();
		    } else {
			initNew();
		    }
		} else if ("save".equals(cmd)) {
		    save(false);
		} else if ("saveas".equals(cmd)) {
		    save(true);
		} else if ("open".equals(cmd)) {
		    if (isDirty()) {
			int sel = JOptionPane.showConfirmDialog(AnimEditor.this,
				T.t("Are you sure to open an animation") + "\n" +
					T.t("Changes are unsaved!"));
			if (sel == 0)
			    open();
		    } else {
			open();
		    }
		} else if ("reopen".equals(cmd)) {
		    String fn = anim_repository.getName();
		    if (fn != null) {
			if (isDirty()) {
			    int sel = JOptionPane.showConfirmDialog(AnimEditor.this,
				    T.t("Are you sure to reopen the animation") + "\n" +
					    T.t("Changes are unsaved!"));
			    if (sel == 0)
				open(fn);
			} else {
			    open(fn);
			}
		    } else {
			JOptionPane.showMessageDialog(AnimContext.top_frame,
				T.t("No name, use open."),
				"Omega",
				JOptionPane.INFORMATION_MESSAGE);
		    }
		} else if ("resetstarter".equals(cmd)) {
		    OmegaStartManager.enableStarter();
		} else if ("about".equals(cmd)) {
		    help.showAbout();
		} else if ("aboutAE".equals(cmd)) {
		    help.showAboutAE();
		} else if ("show manual".equals(cmd)) {
		    help.showManualAE();
		} else if ("show prop act".equals(cmd)) {
		    a_ctxt.ae.cabaret_panel.popup(0);
		} else if ("show prop wing".equals(cmd)) {
		    a_ctxt.ae.wings_panel.popup(0);
		} else if ("dep_set background".equals(cmd)) {
		    String url_s = getFileAsURLString();
		    setBackground(url_s);
		    wings_panel.removeAllWings();
		    a_ctxt.anim_canvas.getToolExecute().execute("fit");
		} else if ("dep_set actor0".equals(cmd)) {
		    String url_s = getFileAsURLStringActor();
		    loadActor(0, url_s);
		} else if ("dep_set actor1".equals(cmd)) {
		    String url_s = getFileAsURLStringActor();
		    loadActor(1, url_s);
		} else if ("dep_set actor2".equals(cmd)) {
		    String url_s = getFileAsURLStringActor();
		    loadActor(2, url_s);
		} else if ("dep_set actor3".equals(cmd)) {
		    String url_s = getFileAsURLStringActor();
		    loadActor(3, url_s);
		} else if ("flip aw".equals(cmd)) {
		    cab_wing_pan_card.next(cab_wing_pan);
		} else if ("add w".equals(cmd)) {
		    loadWing();
		}
		if ("play".equals(cmd)) {
		    validate();
		    arun.playAnimation();
		} else if ("stop".equals(cmd)) {
		    arun.stopAnimation();
		    validate();
		} else if ("pause".equals(cmd)) {
		    a_ctxt.tl_player.pause();
		}

		if ("prop_act_show".equals(cmd)) {
		    a_ctxt.anim_canvas.traceNoWing();
		    a_ctxt.ae.cabaret_panel.setSelected(true);
		    a_ctxt.ae.wings_panel.setSelected(false);
		} else if ("prop_wing_show".equals(cmd)) {
		    a_ctxt.ae.cabaret_panel.setSelected(!true);
		    a_ctxt.ae.wings_panel.setSelected(!false);
		}
		if ("prop_act".equals(cmd)) {
		    a_ctxt.ae.cabaret_panel.popup(0);
		} else if ("prop_wing".equals(cmd)) {
		    a_ctxt.ae.wings_panel.popup(0);
		}

		ToolExecute gel = a_ctxt.anim_canvas.getToolExecute();
		if (gel != null)
		    gel.execute(cmd);
		else
		    OmegaContext.sout_log.getLogger().info(":--: " + "! missed " + cmd);
	    }
	};

	mb = new JMenuBar();
	setJMenuBar(mb);
	mb.setVisible(false);

	toolbar_cmd = new ToolBar_AnimEditor(ae_texec);//, VERTICAL);
	toolbar_top = new ToolBar_AnimEditor(ae_texec);

	JMenu jm = new JMenu(T.t("File"));
	mb.add(jm);

	JMenu jmca = new JMenu(T.t("Canvas"));
	mb.add(jmca);

	JMenu jmpa = new JMenu(T.t("Path"));
	mb.add(jmpa);

	JMenu jmtl = new JMenu(T.t("TimeLine"));
	mb.add(jmtl);

	JMenu jmac = new JMenu(T.t("Cast"));
	mb.add(jmac);

	JMenu jmh = new JMenu(T.t("Help"));
	mb.add(jmh);

	ToolAction tac;

	jm.add(tac = new ToolAction(T.t("New"), "general/New", "new", ae_texec));
	toolbar_cmd.add(tac);
	toolbar_cmd.setVisible(false);
	toolbar_cmd.addSeparator();
	jm.add(tac = new ToolAction(T.t("Open"), "general/Open", "open", ae_texec));
	toolbar_cmd.add(tac);
	jm.add(tac = new ToolAction(T.t("Reopen"), "general/ReOpen", "reopen", ae_texec));
	toolbar_cmd.add(tac);
	jm.add(tac = new ToolAction(T.t("Save"), "general/Save", "save", ae_texec));
	toolbar_cmd.add(tac);
	jm.add(tac = new ToolAction(T.t("Save as"), "general/SaveAs", "saveas", ae_texec));
	toolbar_cmd.add(tac);

	jm.addSeparator();
	if (exit_on_close)
	    jm.add(tac = new ToolAction("Reset Starter", "resetstarter", ae_texec));
	jm.add(tac = new ToolAction(exit_on_close ? T.t("Exit") : T.t("Close"), "exit", ae_texec));

	toolbar_cmd.addSeparator();
	jmca.add(tac = new ToolAction(T.t("Set background"), "dep_set background", ae_texec));
	jmca.addSeparator();
	jmca.add(tac = new ToolAction(T.t("Left"), "navigation/Back", "left", ae_texec));
	toolbar_cmd.add(tac);
	jmca.add(tac = new ToolAction(T.t("Right"), "navigation/Forward", "right", ae_texec));
	toolbar_cmd.add(tac);
	jmca.add(tac = new ToolAction(T.t("Up"), "navigation/Up", "up", ae_texec));
	toolbar_cmd.add(tac);
	jmca.add(tac = new ToolAction(T.t("Down"), "navigation/Down", "down", ae_texec));
	toolbar_cmd.add(tac);
	jmca.addSeparator();
	toolbar_cmd.addSeparator();
	jmca.add(tac = new ToolAction(T.t("Smaller"), "general/ZoomOut", "smaller", ae_texec));
	toolbar_cmd.add(tac);
	jmca.add(tac = new ToolAction(T.t("Bigger"), "general/ZoomIn", "bigger", ae_texec));
	toolbar_cmd.add(tac);
	jmca.add(tac = new ToolAction("1:1", "general/ZoomNo", "upper_left", ae_texec));
	toolbar_cmd.add(tac);
	jmca.add(tac = new ToolAction(T.t("Fit in window"), "general/ZoomFit", "fit", ae_texec));
	toolbar_cmd.add(tac);


	toolbar_cmd.addSeparator();
	jmpa.add(tac = new ToolAction(T.t("Create new"), "omega/PathNew", "path_create", ae_texec, true));
	toolbar_cmd.add(tac);
	jmpa.add(tac = new ToolAction(T.t("Duplicate"), "omega/PathDup", "path_duplicate", ae_texec, true));
	toolbar_cmd.add(tac);
	jmpa.add(tac = new ToolAction(T.t("Extend at end"), "omega/PathExtend", "path_extend", ae_texec, true));
	toolbar_cmd.add(tac);
	jmpa.add(tac = new ToolAction(T.t("Split in two"), "omega/PathSplit", "path_split", ae_texec, true));
	toolbar_cmd.add(tac);
	jmpa.add(tac = new ToolAction(T.t("Delete segment"), "omega/PathDelete", "path_delete", ae_texec, true));
	jmpa.add(tac = new ToolAction(T.t("Delete whole path and timeline"), "omega/PathDeleteAll", "path_delete_all", ae_texec, true));
	//toolbar_cmd.add(tac);

	toolbar_cmd.addSeparator();

	jmtl.add(tac = new ToolAction(T.t("Play"), "media/Play", "play", ae_texec));
	toolbar_cmd.add(tac);
	addShiftAcc(jmtl, 'P');
	jmtl.add(tac = new ToolAction(T.t("Stop"), "media/Stop", "stop", ae_texec));
	addShiftAcc(jmtl, 'S');
	toolbar_cmd.add(tac);

	JMenu jmac2 = new JMenu(T.t("Set actor"));
	jmac.add(jmac2);

	jmac2.add(tac = new ToolAction("1", "dep_set actor0", ae_texec));
	jmac2.add(tac = new ToolAction("2", "dep_set actor1", ae_texec));
	jmac2.add(tac = new ToolAction("3", "dep_set actor2", ae_texec));
	jmac2.add(tac = new ToolAction("4", "dep_set actor3", ae_texec));
	jmac2.add(tac = new ToolAction("5", "dep_set actor4", ae_texec));
	jmac.add(tac = new ToolAction(T.t("Add a wing"), "add w", ae_texec));
	jmac.addSeparator();
	jmac.add(tac = new ToolAction(T.t("Show actor properties..."), "show prop act", ae_texec));
	addCtrlAcc(jmac, 'A');
	jmac.add(tac = new ToolAction(T.t("Show wings properties..."), "show prop wing", ae_texec));
	addCtrlAcc(jmac, 'W');
	jmac.addSeparator();
	jmac.add(tac = new ToolAction(T.t("Flip actor/wings"), "flip aw", ae_texec));
	addCtrlAcc(jmac, 'F');

	jmh.add(tac = new ToolAction(T.t("Show manual"), "show manual", ae_texec));
	jmh.addSeparator();
	jmh.add(tac = new ToolAction(T.t("About") + " Omega", "about", ae_texec));
	jmh.add(tac = new ToolAction(T.t("About Anim Editor"), "aboutAE", ae_texec));

	JPanel mainp = new JPanel();
	JPanel mainpM = new JPanel();

	mainp.setLayout(new BorderLayout());
	mainpM.setLayout(new BorderLayout());

	c.add(mainpM, BorderLayout.CENTER);
	mainpM.add(mainp, BorderLayout.CENTER);

	cabaret_panel = new CabaretPanel(this);
	wings_panel = new WingsPanel(this);

	cab_wing_pan = new JPanel();
	cab_wing_pan.setLayout(cab_wing_pan_card = new CardLayout());
	cab_wing_pan.add(cabaret_panel, "actor");
	cab_wing_pan.add(wings_panel, "wings");

	toolbar_top.card = cab_wing_pan_card;
	toolbar_top.card_pan = cab_wing_pan;

	JPanel p = new JPanel();
	mainp.add(p, BorderLayout.NORTH);

	toolbar_top.populateRest();
	mainpM.add(toolbar_top, BorderLayout.NORTH);
	c.add(toolbar_cmd, BorderLayout.NORTH);

	toolbar_top.add(cab_wing_pan);

	JPanel main_cpan = new JPanel();
	main_cpan.setLayout(new BorderLayout());

	arun = new AnimRuntime(this);
	a_ctxt.arun = arun;

	tlp = new TimeLinePanel(a_ctxt.mtl);
	tlc = new TimeLineComponent(tlp);

	a_ctxt.tl_player.addPlayCtrlListener(tlc);

	tlp.addTimeLinePanelListener(new TimeLinePanelAdapter() {
	    public void event(String evs, Object o) {
		if ("selectTL".equals(evs)) {
		    if (o != null) {
			TimeLine tl = (TimeLine) o;
			int tl_nid = tl.nid;
			AllPath ap = a_ctxt.anim_canvas.ap;
			ap.deselectAll(null);
			Path pa = ap.get(tl_nid);
			if (pa != null)
			    pa.setSelected(true);
			if (a_ctxt.anim_canvas != null)
			    a_ctxt.anim_canvas.setSelectedPath(tl_nid, pa);
		    }
		} else if ("addMarker".equals(evs)) {
		    TimeLine tl = (TimeLine) o;
		    AllPath ap = a_ctxt.anim_canvas.ap;
		    Path pa = ap.findSelected();
		    if (pa != null) {
			TimeMarker tm = tl.last_added_tm;
			TimeMarker[] ta = tl.getAllTimeMarkerType(TimeMarker.TSYNC);
			for (int i = 0; i < ta.length; i++) {
			    if (tm == ta[i]) {
				double w1;
				try {
				    w1 = pa.getMarker(i - 1).getWhere();
				} catch (NullPointerException ex) {
				    w1 = 0;
				}
				double w2;
				try {
				    w2 = pa.getMarker(i).getWhere();
				} catch (NullPointerException ex) {
				    w2 = pa.getLength();
				}
				pa.addMarker(i, TimeMarker.TSYNC, (w1 + w2) / 2);
				a_ctxt.anim_canvas.repaint();
			    }
			}
		    }
		} else if ("delMarker".equals(evs)) {
		    Integer Ix = (Integer) o;
		    AllPath ap = a_ctxt.anim_canvas.ap;
		    Path pa = ap.findSelected();
		    if (pa != null) {
			pa.delMarker(Ix.intValue());
			a_ctxt.anim_canvas.repaint();
		    }
		} else {
		}
	    }
	});


	main_cpan.add(arun.getAC(), BorderLayout.CENTER);

	main_cpan.add(tlc, BorderLayout.SOUTH);
	mainp.add(main_cpan, BorderLayout.CENTER);

	pack();
	setSize(OmegaConfig.FRAME_WIDTH(65), OmegaConfig.FRAME_HEIGHT(65));

	setVisible(true);

	try {
	    APlayer ap = APlayer.createAPlayer("audio/greeting.wav", (String) null, (String) null);
	    ap.play();
	    ap = APlayer.createAPlayer("audio/greeting.mp3", (String) null, (String) null);
	    ap.play();
	} catch (NoClassDefFoundError ex) {
	    OmegaContext.sout_log.getLogger().info("ERR: " + "WARNING!! No audio");
	}

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		if (fname == null)
		    initNew();
		else
		    initFile(fname);
	    }
	});
    }

    private void initNew() {
	wings_panel.removeAllWings();
	anim_repository.clearName();
	a_ctxt.mtl.initNew();
	a_ctxt.arun.initNew();
	tlc.repaint();
	cabaret_panel.repaint();
	mb.setVisible(true);
	toolbar_cmd.setVisible(true);
	setDirty(true);
    }

    private void initFile(String fname) {
	wings_panel.removeAllWings();
	anim_repository.clearName();
	a_ctxt.mtl.initNew();
	a_ctxt.arun.initNew();
	tlc.repaint();
	cabaret_panel.repaint();
	mb.setVisible(true);
	toolbar_cmd.setVisible(true);
	open(fname);
    }

    Point2D decode2D(String s) {
	String[] sa = SundryUtils.split(s, ",");
	float a = Float.parseFloat(sa[0]);
	float b = Float.parseFloat(sa[1]);
	return new Point2D.Float(a, b);
    }

    public void loadWing() {
	String url_s = getFileAsURLString();
	String ua[] = Files.splitUrlString(url_s);
	if (ua != null) {
	    Wing w = a_ctxt.anim_canvas.createWing(ua[1], 100, 100, 4, 1.0, 0);
	    int wing_nid = w.ord;
	    wings_panel.setWing(w, wing_nid);
	    a_ctxt.anim_canvas.resetBackground();
	}
	a_ctxt.ae.setDirty(true);
    }

    public void loadActor(int ix, String url_s) {
	String ua[] = Files.splitUrlString(url_s);
	if (ua != null) {
	    a_ctxt.anim_canvas.loadActor(ix, ua[1]);
	    a_ctxt.ae.setDirty(true);
	} else {
	    OmegaContext.sout_log.getLogger().info(":--: " + "ERROR file: " + url_s);
	}
    }

    public void replaceActor(int cab_ixx) {
	String url_s = getFileAsURLStringActor();
	String ua[] = Files.splitUrlString(url_s);
	if (ua != null) {
	    xImage.invalidateCache();
	    a_ctxt.anim_canvas.loadActor(cab_ixx, ua[1]);
	} else {
	    OmegaContext.sout_log.getLogger().info(":--: " + "ERROR file: " + url_s);
	}
	a_ctxt.ae.setDirty(true);
    }

    public void deleteActor(int cab_ixx) {
	a_ctxt.anim_canvas.deleteActor(cab_ixx);
	a_ctxt.ae.setDirty(true);
    }

    public void setBackground(String url_s) {
	String ua[] = Files.splitUrlString(url_s);
	if (ua != null) {
	    a_ctxt.anim_canvas.setBackground(ua[1], new ArrayList<>());
	    if (wings_panel != null) {
		wings_panel.removeAllWings();
	    }
	} else {
	    OmegaContext.sout_log.getLogger().info(":--: " + "ERROR file: " + url_s);
	}
	a_ctxt.ae.setDirty(true);
    }

    boolean is_dirty = false;

    EventListenerList editstate_listeners = new EventListenerList();

    public void addEditStateListener(EditStateListener l) {
	editstate_listeners.add(EditStateListener.class, l);
    }

    public void removeEditStateListener(EditStateListener l) {
	editstate_listeners.remove(EditStateListener.class, l);
    }

    public boolean isDirty() {
	return is_dirty;
    }

    public void setDirty(boolean d) {
	String fn = anim_repository.getName();

	if (fn == null)
	    return;

	is_dirty = d;

	Object[] lia = editstate_listeners.getListenerList();
	for (int i = 0; i < lia.length; i += 2) {
	    ((EditStateListener) lia[i + 1]).dirtyChanged(is_dirty);
	}

	if (fn == null)
	    fn = "";
	String tit = "Omega - " + T.t("Animator editor: ") + OmegaContext.antiOmegaAssets(fn);
	if (is_dirty)
	    tit += T.t(" (not saved)");
	setTitle(tit);
    }

    void save(boolean ask) {
	String fn = anim_repository.getNameDlg(AnimEditor.this, ask, "Save");
	if (fn != null) {
	    anim_repository.save(a_ctxt, fn, ask);
	    setDirty(false);
	}
    }

    public void loadFile(String fname) {
	open(fname);
    }

    void open() {
	String fn = anim_repository.getNameDlg(AnimEditor.this, true, "Open");
	if (fn != null)
	    open(fn);
    }

    void open(String fn) {
	try {
	    a_ctxt.arun.clean();
	} catch (Exception _ex) {
	}

	wings_panel.removeAllWings();
	Element el = anim_repository.open(a_ctxt, OmegaContext.omegaAssets(fn));
	anim_repository.load(a_ctxt, el);
	httpd.getHashMap().put("lesson:loaded resource ", anim_repository.getName());
	a_ctxt.anim_canvas.getToolExecute().execute("fit");
	tlc.repaint();
	anim_repository.setName(fn);
	setDirty(false);
    }

    public void selectTimeLine(Path pa) {
	tlp.selected_tl = pa.getNid();
	tlp.repaint();
    }

    public void selectTimeLine() {
	tlp.selected_tl = -1;
	tlp.repaint();
    }

    String getFileAsURLString() {
	return anim_repository.getImageURL_Dlg(AnimEditor.this);
    }

    String getFileAsURLStringActor() {
	return anim_repository.getImageURL_Dlg(AnimEditor.this);
    }
}
