package com.femtioprocent.omega.help;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.appl.ApplLesson;
import com.femtioprocent.omega.swing.HtmlFrame;
import com.femtioprocent.omega.t9n.T;
import com.femtioprocent.omega.util.InvokeExternBrowser;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpSystem {
    HtmlFrame html_fr;
    Pattern pat = Pattern.compile("(.*)\\.[a-zA-Z][a-zA-Z]\\.html");

    // konvert xx/hh.xx.html -> xx/hh.html
    private String rmLang(String s) {
        Matcher mat = pat.matcher(s);
        boolean b = mat.matches();

        if (b)
            return mat.group(1) + ".html";
        return s;
    }

    protected void show(String doc, int w, int h) {
        OmegaContext.sout_log.getLogger().info(":--: " + "--SHOW MANUAL " + doc);

        String file_s = OmegaContext.URL_BASE_AS_FILE + "webroot/" + doc;
        File file = new File(file_s);
        if (!file.exists())
            doc = rmLang(doc);
        file_s = OmegaContext.URL_BASE_AS_FILE + "webroot/" + doc;
        file = new File(file_s);
        if (!file.exists())
            return;

        String url_s = OmegaContext.URL_BASE + "webroot/" + doc;
        if (true || InvokeExternBrowser.show_if(url_s) == false) {
            if (html_fr == null)
                html_fr = new HtmlFrame(url_s);
            else
                html_fr.goTo(url_s);
            html_fr.setSize(w, h);
            html_fr.setVisible(true);
        }
    }

    private String base() {
        return ApplLesson.is_editor ? "editor_manual" : "lesson_manual";
    }

    protected String mkFileName(String base_name) {
        String lang = T.lang;

        return base_name + "." + lang + ".html";
    }

    public void showManualL(String more) {
        if (ApplLesson.is_editor)
            show(mkFileName(base()), 800, 600);
        else if (more != null)
            show(mkFileName(base() + '-' + more), 800, 600);
        else
            show(mkFileName(base()), 800, 600);
    }

    public void showManualA() {
        show(mkFileName("editor_manual"), 800, 600);
    }

    public void showAboutLE() {
        show(mkFileName("aboutLessonEditor"), 400, 320);
    }

    public void showAboutAE() {
        show(mkFileName("aboutAnimEditor"), 400, 320);
    }

    public void showAbout() {
        show(mkFileName("about"), 400, 320);
    }

}
