package com.femtioprocent.omega.lesson.appl;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.appl.OmegaStartManager;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.lesson.ToolBar_LessonEditor;
import com.femtioprocent.omega.swing.ToolAction;
import com.femtioprocent.omega.swing.ToolExecute;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LessonEditor extends ApplLesson {
    boolean globalExit2 = false;

    Lesson le;
    static String title;

    public LessonEditor(String title, String fn) {
        super(title, true);
        is_editor = true;

        TOP_JFRAME = this;
        ApplContext.top_frame = this;

        if (fn == null)
            fn = "lesson-" + OmegaContext.getLessonLang() + "/new.omega_lesson";  // LESSON-DIR

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                globalExit2 = true;
                maybeClose();
            }
        });

        JPanel mpan = init();

        le = new Lesson('e');
        le.mact_New();
        le.runLessons(this, mpan, fn, true, OmegaContext.small != null);
        Log.getLogger().info("LessonEditor done " + globalExit2);
    }

    public void maybeClose() {
        OmegaContext.serr_log.getLogger().info("LessonEditor want to close " + (ApplContext.top_frame == LessonEditor.this) + ' ' + ApplContext.top_frame + '\n' + this);
        if (ApplContext.top_frame == LessonEditor.this)
            System.exit(0);
    }

    public void processEvent(AWTEvent e) {
        if (e.getID() != WindowEvent.WINDOW_CLOSING)
            super.processEvent(e);
        else {
            int sel = JOptionPane.showConfirmDialog(LessonEditor.this,
                    T.t("Are you sure to exit Omega?")
            );
            if (sel == 0)
                super.processEvent(e);
        }
    }

    private static boolean is_editor = false;
    private static boolean is_dirty = false;

    public static void setDirty() {
        if (is_editor) {
            is_dirty = true;
            String ctitle = TOP_JFRAME.getTitle();
            if (!ctitle.endsWith(")")) {
                title = ctitle + " - (" + T.t("unsaved" + ")");
                TOP_JFRAME.setTitle(title);
            }
        }
    }

    public static void unsetDirty() {
        if (is_editor) {
            is_dirty = false;
            String ctitle = TOP_JFRAME.getTitle();
            if (ctitle.endsWith(")")) {
                String ll = " - (" + T.t("unsaved") + ")";
                title = ctitle.substring(0, ctitle.length() - ll.length());
                TOP_JFRAME.setTitle(title);
            }
        }
    }


    ToolExecute ae_texec = new ToolExecute() {
        public void execute(String cmd) {
            if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "LessonEditor.texec: execute " + cmd);

            if ("exit".equals(cmd)) {
                int sel = JOptionPane.showConfirmDialog(LessonEditor.this,
                        T.t("Are you sure to exit Omega?") +
                                (is_dirty ? ("\n" + T.t("Changes are unsaved!")) : "")
                );
                if (sel == 0) {
                    le.sendMsg("exitLesson", "");
                    globalExit2 = true;//System.exit(0);
                    setVisible(false);
                    maybeClose();
                }
            } else if ("new".equals(cmd)) {
                boolean do_open = false;
                if (is_dirty) {
                    int sel = JOptionPane.showConfirmDialog(LessonEditor.this,
                            T.t("Are you sure to open?") +
                                    "\n" + T.t("Changes are unsaved!"));
                    if (sel == 0)
                        do_open = true;
                } else
                    do_open = true;
                if (do_open) {
                    le.mact_New();
                    unsetDirty();
                }
            } else if ("save".equals(cmd)) {
                le.mact_Save();
                unsetDirty();
            } else if ("resetstarter".equals(cmd)) {
                OmegaStartManager.enableStarter();
            } else if ("saveas".equals(cmd)) {
                le.mact_SaveAs();
                unsetDirty();
            } else if ("open".equals(cmd)) {
                boolean do_open = false;
                if (is_dirty) {
                    int sel = JOptionPane.showConfirmDialog(LessonEditor.this,
                            T.t("Are you sure to open?") +
                                    "\n" + T.t("Changes are unsaved!"));
                    if (sel == 0)
                        do_open = true;
                } else
                    do_open = true;
                if (do_open) {
                    le.mact_Open();
                    unsetDirty();
                }
            } else if ("save_color_main".equals(cmd)) {
                le.displayColor("main");
            } else if ("save_color_pupil".equals(cmd)) {
                le.displayColor("pupil");
            } else if ("save_color_words".equals(cmd)) {
                le.displayColor("words");
            } else if ("save_colors_theme".equals(cmd)) {
                le.saveColor();
            } else if ("manualLE".equals(cmd)) {
                help.showManualL(null);
            } else if ("manualAE".equals(cmd)) {
                help.showManualA();
            } else if ("about".equals(cmd)) {
                help.showAbout();
            } else if ("aboutLE".equals(cmd)) {
                help.showAboutLE();
            } else if ("show manual".equals(cmd)) {
            }
        }
    };

    JPanel init() {
        JMenuBar mb = new JMenuBar();
        setJMenuBar(mb);
//	mb.setVisible(false);

        ToolBar_LessonEditor toolbar = new ToolBar_LessonEditor(ae_texec, JToolBar.VERTICAL);

/* -- */
        JMenu jm = new JMenu(T.t("File"));
        mb.add(jm);

        ToolAction tac;
        jm.add(tac = new ToolAction(T.t("New"), "general/New", "new", ae_texec));
        toolbar.add(tac);
        jm.add(tac = new ToolAction(T.t("Open"), "general/Open", "open", ae_texec));
        toolbar.add(tac);
        jm.add(tac = new ToolAction(T.t("Save"), "general/Save", "save", ae_texec));
        toolbar.add(tac);
        jm.add(tac = new ToolAction(T.t("Save as"), "general/SaveAs", "saveas", ae_texec));
        toolbar.add(tac);

        jm.addSeparator();
        toolbar.addSeparator();
        jm.add(tac = new ToolAction(T.t("Reset Starter"), null, "resetstarter", ae_texec));
        jm.add(tac = new ToolAction(T.t("Exit"), null, "exit", ae_texec));

/* -- * /
        jm = new JMenu(T.t("Canvas"));
	mb.add(jm);

	jm.add(tac = new ToolAction(T.t("Set colors for Main"), null, "save_color_main", ae_texec));
	jm.add(tac = new ToolAction(T.t("Set colors for Pupil"), null, "save_color_pupil", ae_texec));
	jm.add(tac = new ToolAction(T.t("Set colors for Words"), null, "save_color_words", ae_texec));
	jm.addSeparator();
	jm.add(tac = new ToolAction(T.t("Save all colors in theme file"), null, "save_colors_theme", ae_texec));
/ * -- */

//  	jm = new JMenu(T.t("Target"));
//  	mb.add(jm);


/* -- */
        jm = new JMenu(T.t("Help"));
        mb.add(jm);

        jm.add(tac = new ToolAction(T.t("Manual"), "general/About", "manualLE", ae_texec));
        jm.addSeparator();
        jm.add(tac = new ToolAction(T.t("About") + " Omega", "general/About", "about", ae_texec));
        jm.add(tac = new ToolAction(T.t("About Lesson Editor"), "general/About", "aboutLE", ae_texec));

/* -- */

        Container con = getContentPane();
        con.add(toolbar, BorderLayout.WEST);

        JPanel mpan = new JPanel();
        mpan.setLayout(new BorderLayout());
        con.add(mpan, BorderLayout.CENTER);

        return mpan;
    }
}
