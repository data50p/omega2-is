package com.femtioprocent.omega.help

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.lesson.appl.ApplLesson
import com.femtioprocent.omega.swing.HtmlFrame
import com.femtioprocent.omega.t9n.T
import com.femtioprocent.omega.util.InvokeExternBrowser
import com.femtioprocent.omega.util.Log
import java.io.File
import java.util.regex.Pattern

open class HelpSystem {
    var html_fr: HtmlFrame? = null
    var pat = Pattern.compile("(.*)\\.[a-zA-Z][a-zA-Z]\\.html")

    // konvert xx/hh.xx.html -> xx/hh.html
    private fun rmLang(s: String): String {
	val mat = pat.matcher(s)
	val b = mat.matches()
	return if (b) mat.group(1) + ".html" else s
    }

    protected fun show(doc: String, w: Int, h: Int) {
	var doc = doc
	Log.getLogger().info(":--: --SHOW MANUAL $doc")
	var file_s = OmegaContext.URL_BASE_AS_FILE + "webroot/" + doc
	var file = File(file_s)
	if (!file.exists()) doc = rmLang(doc)
	file_s = OmegaContext.URL_BASE_AS_FILE + "webroot/" + doc
	file = File(file_s)
	if (!file.exists()) return
	val url_s = OmegaContext.URL_BASE + "webroot/" + doc
	if (true || InvokeExternBrowser.show_if(url_s) == false) {
	    if (html_fr == null) html_fr = HtmlFrame(url_s) else html_fr!!.goTo(url_s)
	    html_fr!!.setSize(w, h)
	    html_fr!!.isVisible = true
	}
    }

    private fun base(): String {
	return if (ApplLesson.is_editor != null && ApplLesson.is_editor!!) "editor_manual" else "lesson_manual"
    }

    protected fun mkFileName(base_name: String): String {
	val lang = T.lang
	return "$base_name.$lang.html"
    }

    fun showManualL(more: String?) {
	if (ApplLesson.is_editor != null && ApplLesson.is_editor!!) show(
	    mkFileName(base()),
	    800,
	    600
	) else if (more != null) show(mkFileName(base() + '-' + more), 800, 600) else show(mkFileName(base()), 800, 600)
    }

    fun showManualA() {
	show(mkFileName("editor_manual"), 800, 600)
    }

    fun showAboutLE() {
	show(mkFileName("aboutLessonEditor"), 400, 320)
    }

    fun showAboutAE() {
	show(mkFileName("aboutAnimEditor"), 400, 320)
    }

    fun showAbout() {
	show(mkFileName("about"), 400, 320)
    }
}
