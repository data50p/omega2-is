package com.femtioprocent.omega.lesson.appl

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.OmegaContext.Companion.lessonLang
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.lesson.ToolBar_LessonEditor
import com.femtioprocent.omega.swing.ToolAction
import com.femtioprocent.omega.swing.ToolExecute
import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.Log
import java.awt.AWTEvent
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class LessonEditor(title: String?, fn: String?) : ApplLesson(title, true) {
    var globalExit2 = false
    var le: Lesson? = null
    fun maybeClose() {
	Log.getLogger().info(
		"""LessonEditor want to close ${ApplContext.top_frame === this@LessonEditor} ${ApplContext.top_frame}
$this"""
	)
	if (ApplContext.top_frame === this@LessonEditor) System.exit(0)
    }

    public override fun processEvent(e: AWTEvent) {
	if (e.id != WindowEvent.WINDOW_CLOSING) super.processEvent(e) else {
	    val sel = JOptionPane.showConfirmDialog(
		    this@LessonEditor,
		    t("Are you sure to exit Omega?")
	    )
	    if (sel == 0) super.processEvent(e)
	}
    }

    var ae_texec = ToolExecute { cmd ->
	if (OmegaConfig.T) Log.getLogger().info(":--: LessonEditor.texec: execute $cmd")
	if ("exit" == cmd) {
	    val sel = JOptionPane.showConfirmDialog(
		    this@LessonEditor,
		    t("Are you sure to exit Omega?") +
			    if (is_dirty) """
 	
 	${t("Changes are unsaved!")}
 	""".trimIndent() else ""
	    )
	    if (sel == 0) {
		le!!.sendMsg("exitLesson", "")
		globalExit2 = true //System.exit(0);
		isVisible = false
		maybeClose()
	    }
	} else if ("new" == cmd) {
	    var do_open = false
	    if (is_dirty) {
		val sel = JOptionPane.showConfirmDialog(
			this@LessonEditor,
			"""
		     	${t("Are you sure to open?")}
		     	${t("Changes are unsaved!")}
		     	""".trimIndent()
		)
		if (sel == 0) do_open = true
	    } else do_open = true
	    if (do_open) {
		le!!.mact_New()
		unsetDirty()
	    }
	} else if ("save" == cmd) {
	    le!!.mact_Save()
	    unsetDirty()
	} else if ("saveas" == cmd) {
	    le!!.mact_SaveAs()
	    unsetDirty()
	} else if ("open" == cmd) {
	    var do_open = false
	    if (is_dirty) {
		val sel = JOptionPane.showConfirmDialog(
			this@LessonEditor,
			"""
		     	${t("Are you sure to open?")}
		     	${t("Changes are unsaved!")}
		     	""".trimIndent()
		)
		if (sel == 0) do_open = true
	    } else do_open = true
	    if (do_open) {
		le!!.mact_Open()
		unsetDirty()
	    }
	} else if ("save_color_main" == cmd) {
	    le!!.displayColor("main")
	} else if ("save_color_pupil" == cmd) {
	    le!!.displayColor("pupil")
	} else if ("save_color_words" == cmd) {
	    le!!.displayColor("words")
	} else if ("save_colors_theme" == cmd) {
	    le!!.saveColor()
	} else if ("manualLE" == cmd) {
	    help!!.showManualL(null)
	} else if ("manualAE" == cmd) {
	    help!!.showManualA()
	} else if ("about" == cmd) {
	    help!!.showAbout()
	} else if ("aboutLE" == cmd) {
	    help!!.showAboutLE()
	} else if ("show manual" == cmd) {
	}
    }

    init {
	var fn = fn
	is_editor = true
	TOP_JFRAME = this
	ApplContext.top_frame = this
	if (fn == null) fn = "lesson-" + lessonLang + "/new.omega_lesson" // LESSON-DIR
	addWindowListener(object : WindowAdapter() {
	    override fun windowClosing(ev: WindowEvent) {
		globalExit2 = true
		maybeClose()
	    }
	})
	val mpan = init()
	le = Lesson('e')
	le!!.mact_New()
	le!!.runLessons(this, mpan, fn, true, OmegaContext.small != null)   // // // This never return
	Log.getLogger().info("LessonEditor done $globalExit2")
    }

    fun init(): JPanel {
	val mb = JMenuBar()
	jMenuBar = mb
	//	mb.setVisible(false);
	val toolbar = ToolBar_LessonEditor(ae_texec, JToolBar.VERTICAL)

	/* -- */
	var jm = JMenu(t("File"))
	mb.add(jm)
	var tac: ToolAction?
	jm.add(ToolAction(t("New"), "general/New", "new", ae_texec).also { tac = it })
	toolbar.add(tac)
	jm.add(ToolAction(t("Open"), "general/Open", "open", ae_texec).also { tac = it })
	toolbar.add(tac)
	jm.add(ToolAction(t("Save"), "general/Save", "save", ae_texec).also { tac = it })
	toolbar.add(tac)
	jm.add(ToolAction(t("Save as"), "general/SaveAs", "saveas", ae_texec).also { tac = it })
	toolbar.add(tac)
	jm.addSeparator()
	toolbar.addSeparator()
	jm.add(ToolAction(t("Reset Starter"), null, "resetstarter", ae_texec).also { tac = it })
	jm.add(ToolAction(t("Exit"), null, "exit", ae_texec).also { tac = it })

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


	/* -- */jm = JMenu(t("Help"))
	mb.add(jm)
	jm.add(ToolAction(t("Manual"), "general/About", "manualLE", ae_texec).also { tac = it })
	jm.addSeparator()
	jm.add(ToolAction(t("About") + " Omega", "general/About", "about", ae_texec).also { tac = it })
	jm.add(ToolAction(t("About Lesson Editor"), "general/About", "aboutLE", ae_texec).also { tac = it })

	/* -- */
	val con = contentPane
	con.add(toolbar, BorderLayout.WEST)
	val mpan = JPanel()
	mpan.layout = BorderLayout()
	con.add(mpan, BorderLayout.CENTER)
	return mpan
    }

    companion object {
	var TOP_JFRAME: LessonEditor? = null
	var title: String? = null
	private var is_editor = false
	private var is_dirty = false

	fun setDirty() {
	    if (is_editor) {
		is_dirty = true
		val ctitle = TOP_JFRAME!!.title
		if (!ctitle.endsWith(")")) {
		    title = ctitle + " - (" + t("unsaved" + ")")
		    TOP_JFRAME!!.title = title
		}
	    }
	}

	fun unsetDirty() {
	    if (is_editor) {
		is_dirty = false
		val ctitle = TOP_JFRAME!!.title
		if (ctitle.endsWith(")")) {
		    val ll = " - (" + t("unsaved") + ")"
		    title = ctitle.substring(0, ctitle.length - ll.length)
		    TOP_JFRAME!!.title = title
		}
	    }
	}
    }
}
