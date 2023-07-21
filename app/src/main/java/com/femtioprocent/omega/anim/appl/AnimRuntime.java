package com.femtioprocent.omega.anim.appl;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.anim.cabaret.Actor;
import com.femtioprocent.omega.anim.cabaret.GImAE;
import com.femtioprocent.omega.anim.canvas.AnimCanvas;
import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.anim.tool.timeline.*;
import com.femtioprocent.omega.media.audio.APlayer;
import com.femtioprocent.omega.media.images.xImage;
import com.femtioprocent.omega.servers.httpd.Server;
import com.femtioprocent.omega.subsystem.Httpd;
import com.femtioprocent.omega.swing.ToolExecute;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Files;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnimRuntime {
    static OmegaContext context;
    Server httpd;
    Anim_HelpSystem help;
    Anim_Repository anim_repository;
    public AnimContext a_ctxt;
    AnimCanvas anim_canvas;

    //    private ArrayList ap_li = new ArrayList();

    public AnimRuntime(AnimEditor ae) {
        a_ctxt = ae.a_ctxt;
        init1(ae);
        init(ae);
    }

    public AnimRuntime() {
        a_ctxt = new AnimContext(this);
        init(null);
        init1(null);
    }

    public AnimRuntime(AnimContext ac) {
        a_ctxt = ac;
        init(null);
        init1(null);
    }

    public AnimCanvas getAC() {
        return anim_canvas;
    }

    public void init1(AnimEditor ae) {
        OmegaContext.init("Httpd", null);
        httpd = ((Httpd) (OmegaContext.getSubsystem("Httpd"))).httpd;

        anim_repository = new Anim_Repository();

        help = new Anim_HelpSystem();

//	setLayout(new BorderLayout());

        if (ae != null) {
            a_ctxt.ae = ae;                             // PATH FIX
            anim_canvas = new AnimCanvas(ae, a_ctxt);
        } else {
            anim_canvas = new AnimCanvas(this, a_ctxt);
        }
    }

    String composeAudio(String base, String alt) {
        if (alt == null || alt.length() == 0)
            return base;

        String alt2 = alt.replace(':', '_');
        int ix = base.lastIndexOf('.');
        String base2l = base.substring(0, ix);
        String base2r = base.substring(ix);

        return base2l + '-' + alt2 + base2r;
    }


    // ${<banid>:<variable#>}
    private boolean hasVar(String s) {
        if (s.indexOf('$') == -1)
            return false;
        return true;
    }

    String composeVar(String anam) {
        String[] sa = SundryUtils.split(anam, "${}");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < sa.length; i++) {
            String s1 = sa[i];
            int ix = s1.indexOf(':');
            if (ix != -1) {
                try {
                    String banid = s1.substring(0, ix); // if [0] == '@' -> target id
                    String varix = s1.substring(ix + 1);
                    OmegaContext.sout_log.getLogger().info(":--: " + "GOT  REF:: " + banid + ':' + varix);
                    int varixi = Integer.parseInt(varix);
                    if (banid.charAt(0) == '@') {
                        String target_id = banid.substring(1);
                        String sv = (String) OmegaContext.variables.get(target_id + ':' + varix);
                        OmegaContext.sout_log.getLogger().info(":--: " + "GOT TARGET REF:: " + target_id + ':' + varix + " = " + sv);
                        sa[i] = sv;
                    } else {
                        // get variable from the actor (actorlist in animator)
                        Actor ac2 = anim_canvas.getAnimatedActor(banid);
                        //log		    OmegaContext.sout_log.getLogger().info(":--: " + "got ac2 " + banid + ' ' + ac2);
                        GImAE gimae2 = ac2.gimae;
                        sa[i] = gimae2.getVariable(varixi);
                    }
                } catch (Exception ex) {
                    OmegaContext.sout_log.getLogger().info("ERR: " + "composeVar " + ex);
                    sa[i] = "";
                }
            }
            sb.append(sa[i]);
        }
        return sb.toString();
    }

    public void init(AnimEditor ae) {
        a_ctxt.mtl = new MasterTimeLine(a_ctxt);
        TimeLinePlayer tl_player = new TimeLinePlayer();
        tl_player.addPlayCtrlListener(a_ctxt.mtl);
        a_ctxt.tl_player = tl_player;

        class MTEA extends TriggerEventAction {
            public void doAction(TriggerEvent te, TimeMarker tm, boolean dry) {
                try {
                    String cmd = te.getCmd();

                    Actor ac = anim_canvas.getAnimatedActor(tm.tl.nid);
//log		    OmegaContext.sout_log.getLogger().info(":--: " + "}}}}} action " + cmd + ' ' + tm.tl.nid + ' ' + ac);

                    if (ac == null)
                        return;

                    GImAE gimae = ac.gimae;
                    if (cmd.equals("ImageAttrib")) {
                        String anam = te.getArgString();
//log			OmegaContext.sout_log.getLogger().info(":--: " + ">>>>> anam " + anam);
                        if (hasVar(anam)) {
                            String scat = composeVar(anam);
//log			    OmegaContext.sout_log.getLogger().info(":--: " + "var attrib " + scat);
                            if (dry)
                                gimae.setAttribName(scat);
                            else
                                gimae.setAttribNameUncommited(scat);
                        } else if (dry)
                            gimae.setAttribName(anam);
                        else
                            gimae.setAttribNameUncommited(anam);
                    }
                    if (cmd.equals("SetAnimSpeed")) {
                        double d = ((TriggerEventSetAnimSpeed) te).getArgDouble();
                        gimae.setAnimSpeed(d);
                    }
                    if (cmd.equals("SetMirror")) {
                        int v = ((TriggerEventSetMirror) te).getArgInt();
                        gimae.setMirror((v & 1) == 1, (v & 2) == 2);
                    }
                    if (cmd.equals("SetLayer"))
                        gimae.setLayer(((TriggerEventSetLayer) te).getArgInt());
                    if (cmd.equals("Rotate")) {
                        double d = ((TriggerEventRotate) te).getArgDouble();
                        double d2 = ((TriggerEventRotate) te).getArgDouble2nd();
                        d /= 360.0;
                        d *= 3.14159265358979 * 2;
                        d /= 1000.0;
                        if (d2 < 19999) {
                            d2 /= 360.0;
                            d2 *= 3.14159265358979 * 2;
                        }
                        gimae.setRotation(d, d2, tm.when);
                    }
                    if (cmd.equals("ResetSequence")) {
                        String arg = ((TriggerEventResetSequence) te).getArgString();
                        gimae.setResetSequence(arg, tm.when, tm.tl.getAllTimeMarkerType('[')[0].when);
                    }
                    if (cmd.equals("Scale")) {
                        double d = ((TriggerEventScale) te).getArgDouble();
                        double d2 = ((TriggerEventScale) te).getArgDouble2nd(d);
                        d /= 1000.0;
                        gimae.setScale(d, d2, tm.when);
                    }
                    if (cmd.equals("SetVisibility"))
                        gimae.setVisibility(((TriggerEventSetVisibility) te).getArgInt());
                    if (cmd.equals("PlaySound")) {
                        String arg = ((TriggerEventPlaySound) te).getArgString();
                        if (hasVar(arg)) {
                            String scat = composeVar(arg);
                            arg = scat;
                        }

                        String arg_alt = gimae.getLessonIdAlt().replace(':', '_');

                        if (dry) {
                            OmegaContext.sout_log.getLogger().info(":--: " + "PLAY SOUND " + arg + ' ' + arg_alt);
                        } else {
                            APlayer ap = APlayer.createAPlayer(arg, arg_alt, "TL_" + tm.tl.nid);
                            if (!ap.isLoaded()) {
                                String arg_alt2 = gimae.getLessonIdAlt();
                                int ix;
                                if ((ix = arg_alt2.lastIndexOf(':')) != -1) {
                                    String arg_alt3 = arg_alt2.substring(0, ix);
                                    ap = APlayer.createAPlayer(arg, arg_alt3, "TL_" + tm.tl.nid);
                                }
                            }
                            //			    ap_li.add(ap);
                            if (ap.isLoaded())
                                ap.play();
                        }
                    }

                    if (cmd.equals("Dinner")) {
                        int v = ((TriggerEventDinner) te).getArgInt();
                        gimae.setDinner((v & 1) == 1, (v & 2) == 2); // can eat, can bee eaten
                    }
                    if (cmd.equals("Option")) {
                        int v = ((TriggerEventOption) te).getArgInt();
                        gimae.setOption(v);
                    }
                } catch (Exception ex) {
                    OmegaContext.exc_log.getLogger().throwing(this.getClass().getName(), "doAction", ex);
                }
            }
        }
        ;
        final MTEA mtea = new MTEA();

        a_ctxt.mtl.addPlayListener(new PlayListener() {

            public void actionAtTime(TimeLine[] tlA, int t, int attr, boolean dry) {
                if (a_ctxt.ae != null)
                    if (!anim_canvas.isCanvasNormal()) {
                        ToolExecute gel = anim_canvas.getToolExecute();
                        if (gel != null)
                            gel.execute("upper_left");
                    }
                if (!dry)
                    anim_canvas.updateAtTime((int) t, tlA);
            }

            public void actionMarkerAtTime(TimeMarker tm, int t, boolean dry) {
                if (tm.type == TimeMarker.BEGIN) {
                    Actor ac = anim_canvas.getAnimatedActor(tm.tl.nid);
                    if (ac != null) {
                        GImAE gimae = ac.gimae;
                        gimae.beginPlay();
                    }
                } else if (tm.type == TimeMarker.END) {
                    a_ctxt.tl_player.stop();
                } else if (tm.type == TimeMarker.START) {
                } else if (tm.type == TimeMarker.STOP) {
                    Actor ac = anim_canvas.getAnimatedActor(tm.tl.nid);
                    if (ac != null) {
                        GImAE gimae = ac.gimae;
                    }
                }
                tm.doAllAction(mtea, dry);
            }
        });
    }

    public void init2() {
        initNew();
    }

    public String[] getLessonId_TimeLines() {
        return a_ctxt.mtl.getLessonId_TimeLines();
    }

    public String[] getLessonId_Actors() {
        return anim_canvas.getLessonId_Actors();
    }

    public boolean bindActor(String actor_lid, String timeline_lid) {
        OmegaContext.sout_log.getLogger().info(":--: " + "BINDING " + actor_lid + " -> " + timeline_lid);
        if (!anim_canvas.bindActor(actor_lid, timeline_lid)) {
            JOptionPane.showMessageDialog(a_ctxt.anim_canvas,
                    T.t("Can't bind actor with timeline; '") +
                            actor_lid + "' '" + timeline_lid + "'",
                    "Omega",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    public void bindAllNoActor() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "BINDING empty tl");
        anim_canvas.bindAllNoActor();
    }

    public void initNew() {
        anim_repository.clearName();
        a_ctxt.mtl.initNew();
        anim_canvas.initNew();
        clean();
    }

    public void playAnimation() {
        playAnimation(null);
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

    public void playAnimation(Runnable after) {
        try {
            clean();
            anim_canvas.initPlay();
            anim_canvas.setVisibilityMode(AnimCanvas.HIDE_PATH);
            System.gc();
            a_ctxt.tl_player.setSpeed(a_ctxt.anim_speed);
            anim_canvas.HIDDEN = false;
            anim_canvas.repaint();
            if (a_ctxt.tl_player.play(after))
                ;
            OmegaContext.sout_log.getLogger().info(":--: " + "playAnimation done zq");
            clean();
        } catch (NullPointerException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "AnimRintime:234: Nullpointerexception " + ex);
            ex.printStackTrace();
        }
    }

    public void dry_playAnimation(Runnable after) {
        try {
            anim_canvas.initPlay();
            anim_canvas.setVisibilityMode(AnimCanvas.HIDE_PATH);
            System.gc();
            a_ctxt.tl_player.setSpeed(a_ctxt.anim_speed);
            a_ctxt.tl_player.dry_play(after, 100);
        } catch (NullPointerException ex) {
            OmegaContext.sout_log.getLogger().info("ERR: " + "AnimRintime:234: Nullpointerexception " + ex);
            ex.printStackTrace();
        }
    }

    public void stopAnimation() {
        a_ctxt.tl_player.stop();
        anim_canvas.hideActors();
    }

    Point2D decode2D(String s) {
        String[] sa = SundryUtils.split(s, ",");
        float a = Float.parseFloat(sa[0]);
        float b = Float.parseFloat(sa[1]);
        return new Point2D.Float(a, b);
    }

//      public void loadActor(int ix, String url_s) {
//  	String ua[] = Files.splitUrlString(url_s);
//  	if ( ua != null ) {
//  	    anim_canvas.loadActor(ix, ua[1]);
//  	} else {
//  	    OmegaContext.sout_log.getLogger().info(":--: " + "ERROR file: " + url_s);
//  	}
//      }

    public void setBackground(String url_s) {
        String ua[] = Files.splitUrlString(url_s);
        if (ua != null) {
            anim_canvas.setBackground(ua[1], new ArrayList<>());
            if (a_ctxt.ae != null) {
                if (a_ctxt.ae.wings_panel != null) {
                    a_ctxt.ae.wings_panel.removeAllWings();
                }
            }
        } else {
            OmegaContext.sout_log.getLogger().info(":--: " + "ERROR file: " + url_s);
        }
    }

    Element open() {
        String fn = anim_repository.getNameDlg(anim_canvas, true, "Open");
        return open(fn);
    }

    Element open(String fn) {
        Element el = anim_repository.open(a_ctxt, OmegaContext.omegaAssets(fn));
        return el;
//	httpd.getHashMap().put("lesson:loaded resource ", anim_repository.getName());
//fix	anim_canvas.getToolExecute().execute("fit");
//fix	tlc.repaint();
//	anim_repository.setName(fn);
        // FIX
    }

    void load(Element el) {
        anim_repository.load(a_ctxt, el);
    }

    private static String toURL(File file) {
        return Files.toURL(file);
    }

    Element anim_el_root;

    public Element getElementRoot() {
        return anim_el_root;
    }

    public void prefetch(String fn) {
        a_ctxt.arun = this;

        initNew();
        getAC().HIDDEN = true;

        anim_el_root = open(fn);
        load(anim_el_root);

        getAC().offCenterBackground();
    }

    public void runAction(Window window,
                          String fn,
                          String[] actA,
                          String[] pathA,
                          HashMap args,
                          Runnable hook) {
        final Window win = window;
        final AnimContext fa_ctxt = a_ctxt;


        xImage.removeAllEntry();

        getAC().HIDDEN = true;
        fa_ctxt.anim_canvas.repaint();

        if (hook != null)
            hook.run();

        fa_ctxt.arun = this;

        int spd = ((Integer) args.get("speed")).intValue();
        OmegaContext.SPEED = spd > 1250 ? "-fast" : spd < 750 ? "-slow" : "";

        a_ctxt.anim_speed = ((Integer) args.get("speed")).intValue() / 1000.0;
        Color col = (Color) args.get("anim_background");
        HashMap colors = (HashMap) args.get("anim_colors");
        fa_ctxt.anim_canvas.background_color = col;
        fa_ctxt.anim_canvas.colors = colors;

        Map variables_hm = (Map) args.get("variables");
        OmegaContext.variables = variables_hm;

        for (int i1 = 0; i1 < 1; i1++) {
            String[] lid_timelines = getLessonId_TimeLines();
            String[] lid_actors = getLessonId_Actors();

            OmegaContext.sout_log.getLogger().info(":--: " + "anim: TL   " + SundryUtils.arrToString(lid_timelines));
            OmegaContext.sout_log.getLogger().info(":--: " + "anim: Act  " + SundryUtils.arrToString(lid_actors));

            String[] aaid = actA;
            OmegaContext.sout_log.getLogger().info(":--: " + "less: act  " + SundryUtils.arrToString(aaid) + ' ' + aaid.length);

            String[] v_pa = pathA;
            OmegaContext.sout_log.getLogger().info(":--: " + "less: path " + SundryUtils.arrToString(v_pa) + ' ' + v_pa.length);

            for (int i = 0; i < v_pa.length; i++)
                try {
                    int a = Integer.parseInt(v_pa[i]);
                    v_pa[i] = lid_timelines[a - 1];
                } catch (NumberFormatException ex) {
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(fa_ctxt.anim_canvas,
                            T.t("No named timeline (lesson id)"),
                            "Omega",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

            OmegaContext.sout_log.getLogger().info(":--: " + "nVt " + SundryUtils.arrToString(v_pa));

            fa_ctxt.anim_canvas.bindAllStatistActor();

            boolean ok = true;
            bindAllNoActor();
            for (int i = 0; i < aaid.length; i++) {
                try {
                    if (bindActor(aaid[i], v_pa[i]))
                        ok &= true;
                    else
                        ok = false;
                } catch (Exception ex) {
                    String ac_s = "?";
                    String v_s = "?";
                    try {
                        ac_s = "" + aaid[i];
                        v_s = "" + v_pa[i];
                    } catch (Exception ex2) {
                    }
                    JOptionPane.showMessageDialog(fa_ctxt.anim_canvas,
                            T.t("Can't bind path and actor") + '\n' +
                                    "" + ac_s + " -> " + v_s,
                            "Omega",
                            JOptionPane.INFORMATION_MESSAGE);
                    ok = false;
                }
            }

            if (ok) {
                //		SundryUtils.m_sleep(100);
// 		if ( hook != null )
// 		    hook.run();
//  		fa_ctxt.anim_canvas.centerBackground();
                final String[] end_code = new String[1];
                end_code[0] = null;

//   		a_ctxt.anim_canvas.requestFocus();
//   		a_ctxt.anim_canvas.repaint();

                final long drct0 = SundryUtils.ct();
                dry_playAnimation(new Runnable() {
                    public void run() {
                        OmegaContext.sout_log.getLogger().info(":--: " + "Dry Running done " + (SundryUtils.ct() - drct0));
                    }
                });

// 		if ( hook != null )
// 		    hook.run();

// 		fa_ctxt.anim_canvas.HIDDEN = false;
//   		fa_ctxt.anim_canvas.repaint();
                fa_ctxt.anim_canvas.centerBackground();
                final long rct0 = SundryUtils.ct();
                getAC().HIDDEN = false;
                playAnimation(new Runnable() {
                    public void run() {
                        OmegaContext.sout_log.getLogger().info(":--: " + "Running done " + (SundryUtils.ct() - rct0));
                        //fa_ctxt.anim_canvas.hideActors(); // LAST
                        String end_code_s = fa_ctxt.anim_canvas.getEndCode();
                        end_code[0] = end_code_s;
                        OmegaContext.sout_log.getLogger().info(":--: " + "Endcode " + end_code_s);
                    }
                });

                while (end_code[0] == null)
                    SundryUtils.m_sleep(200);

                clean();

            } else {
//		OmegaContext.sout_log.getLogger().info(":--: " + "*** Running 1 fail&done. ***");
                fa_ctxt.anim_canvas.hideActors();
            }

            OmegaContext.sout_log.getLogger().info(":--: " + "--------ok-------");
        }
    }

    synchronized public void clean() {

        OmegaContext.sout_log.getLogger().info(":--: " + "AnimRuntime Close");

// 	int i = 0;
// 	Iterator it = ap_li.iterator();
// 	while(it.hasNext()) {
// 	    APlayer ap = (APlayer)it.next();
// 	    // OmegaContext.sout_log.getLogger().info(":--: " + "Close " + i++ + ' ' + ap.nname);
// 	    ap.close();
// 	}
// 	ap_li = new ArrayList();
    }
}
