package com.femtioprocent.omega.lesson.machine;

import com.femtioprocent.omega.OmegaConfig;
import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.Lesson;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.value.Values;
import com.femtioprocent.omega.xml.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Item {
    public ItemEntry it_ent;
    int ord;
    private String text;
    private String text_orig;
    private String tts;
    private String tts_orig;
    private String dummytext_orig;
    private String lid;
    private String lid_orig;
    public List var;
    String sign;
    private String sign_orig;
    String sound;
    private String sound_orig;
    private String dummysound_orig;
    private String dummysign_orig;
    private String dummytext;
    private String dummysound;
    private String dummysign;
    private String saved_dummytext;
    private String saved_dummysound;
    private String saved_dummysign;
    private boolean dummy_flag = false;  // if replace in self
    public int dummy_extra = -1;  // if extra dummy

    public boolean isAction = false;
    public String action_type;
    public String action_fname;
    public String action_fname_orig;
    boolean dummy_slot = false;

    public Item(String txt) {
        this.ord = 0;
        var = new ArrayList();
        text = txt;
        text_orig = new String(txt);
        tts = "";
        tts_orig = new String("");
        lid = "";
        lid_orig = "";
        sound = "";
        sound_orig = "";
        sign = "";
        sign_orig = "";
        dummytext = "";
        dummysound = "";
        dummysign = "";
        saved_dummytext = "";
        saved_dummysound = "";
        saved_dummysign = "";
        var.add("");
        var.add("");
        var.add("");
        var.add("");
        var.add("");
        var.add(""); // one more for index 0
        isAction = false;
    }

    Item(int ord, Element el) {
        this.ord = ord;
        var = new ArrayList();
        load(el);
    }

    Item(String txt, boolean isAction_) {
        this(txt);
        isAction = true;
        action_type = "";
        action_fname_orig = action_fname = "";
    }

    Item(int ord, Element el, boolean isAction_) {
        this(ord, el);
        isAction = true;
        action_type = el.findAttr("action-type");

        String action_fname_ = el.findAttr("action-fname");
        action_fname_orig = action_fname_;
        this.action_fname = krull(action_fname_);
        if (action_fname_ != null && action_fname_.length() > 0)
            OmegaContext.sout_log.getLogger().info(":--: " + "ACTION " + this.action_fname + ' ' + action_fname_);
    }

    public void setDefaultAction() {
        setDefaultAction(false);
    }

    public void setDefaultAction(boolean empty) {
        if (this.action_fname_orig.length() == 0 || empty) {
            this.action_type = "omega_anim";
            String action_fname_ = empty ? "" : "anim/AnimTemplates/ActorActsWith";
            this.action_fname_orig = action_fname_;
            this.action_fname = krull(action_fname_);
        }
    }

    private boolean onlynumeric(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < '0' || ch > '9')
                return false;
        }
        return true;
    }

    private boolean isAlpha(char ch) {
        return Character.isLetter(ch);
// 	return ch >= 'a' && ch <= 'z' ||
// 	    ch >= 'A' && ch <= 'Z';
    }

    // {nn} where nn is digit -> unicode
    // {-2} {+2:abc} -> variable subst
    // {lesson_name.path_lid.part:def} story chaining

    protected String krull(final String s) {
        try {
            int ix = s.indexOf('{');
            if (ix == -1)
                return s;
            if (ix + 1 >= s.length())
                return s;
            if (!isAlpha(s.charAt(ix + 1))) {     // xxx{yy}zzz
                int ix2 = s.indexOf('}');
                if (ix2 == -1)
                    return s;
                String s1 = s.substring(0, ix);     // xxx
                String s2 = s.substring(ix, ix2 + 1); // {yy}
                String s3 = s.substring(ix2 + 1);     // zzz
                return s1 + s2 + krull(s3);
            }
            int ix2 = s.indexOf('}');
            if (ix2 == -1)
                return s;
            String s1 = s.substring(0, ix);
            String s2 = s.substring(ix2 + 1);
            String ks = s.substring(ix + 1, ix2);
            String[] sa = SundryUtils.split(ks, ":");
            HashMap story_hm = Lesson.story_hm;
            if (story_hm == null)
                return s;
            OmegaContext.story_log.getLogger().info("match " + s + " from " + story_hm);
            String ns = (String) story_hm.get(sa[0]);
            if (ns == null)
                ns = sa.length > 1 ? sa[1] : "";
            OmegaContext.story_log.getLogger().info("-> " + ks + "¶" + s1 + "¶" + ns + "¶" + s2);
            return s1 + ns + krull(s2);
        } catch (Exception ex) {
        }
        return s;
    }

    String decode(String raw_text) {
        if (raw_text == null)
            return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < raw_text.length(); i++) {
            char ch = raw_text.charAt(i);
            if (ch == '{') {
                int v = 0;
                for (int ii = 1; ii < 77; ii++) {
                    char ch2 = raw_text.charAt(i + ii);
                    if (ch2 == '}') {
                        String kr_s = raw_text.substring(i + 1, i + ii);
//log			OmegaContext.sout_log.getLogger().info(":--: " + "got text kr_s: " + kr_s);
                        if (onlynumeric(kr_s)) {
                            sb.append((char) v);
                            i += ii;
                            break;
                        } else {
                            sb.append("{" + kr_s + "}");
                            i += ii;
                            break;
                        }
                    }
                    v = v * 10 + (ch2 - '0');
                }
            } else
                sb.append(ch);
        }
        String text = sb.toString();

        return text;
    }

    String fel(Element el, String key) {
        String s = el.findAttr(key);
        if (s == null)
            s = "";
        return s;
    }

    private void load(Element el) {
        String raw_text = fel(el, "text");
        String text = decode(raw_text);
        String textid = fel(el, "textid");
        this.text_orig = new String(text);
        String raw_dummytext = fel(el, "dummytext");
        String dummytext = decode(raw_dummytext);
        this.dummytext_orig = new String(dummytext);
        String raw_tts = fel(el, "tts");
        String tts = decode(raw_tts);
        this.tts_orig = new String(tts);

        String lid = el.findAttr("Lid");
        lid_orig = lid;
        if (lid == null)
            lid = "";

        String sound = el.findAttr("sound");
        if (sound == null)
            sound = "";
        sound_orig = sound;

        String dummysound = fel(el, "dummysound");
        if (dummysound == null)
            dummysound = "";
        dummysound_orig = dummysound;

        String sign = el.findAttr("sign");
        if (sign == null)
            sign = "";
        sign_orig = sign;

        String dummysign = fel(el, "dummysign");
        if (dummysign == null)
            dummysign = "";
        dummysign_orig = dummysign;

        sound = krull(sound);
        sign = krull(sign);
        lid = krull(lid);
        text = krull(text);
        dummytext = krull(dummytext);
        dummysound = krull(dummysound);
        dummysign = krull(dummysign);

        this.sound = sound;
        this.sign = sign;
        this.text = text;
        this.tts = tts;
        this.lid = lid;
        this.dummytext = dummytext;
        this.dummysound = dummysound;
        this.dummysign = dummysign;
        this.saved_dummytext = dummytext;
        this.saved_dummysound = dummysound;
        this.saved_dummysign = dummysign;


//log	OmegaContext.sout_log.getLogger().info(":--: " + "NEW TXTXO " + text + ' ' + text_orig);

        String v1 = el.findAttr("var-1");
        if (v1 == null) v1 = "";
        String v2 = el.findAttr("var-2");
        if (v2 == null) v2 = "";
        String v3 = el.findAttr("var-3");
        if (v3 == null) v3 = "";
        String v4 = el.findAttr("var-4");
        if (v4 == null) v4 = "";
        String v5 = el.findAttr("var-5");
        if (v5 == null) v5 = "";

        var.add("");
        var.add(v1);
        var.add(v2);
        var.add(v3);
        var.add(v4);
        var.add(v5);
    }

    public String getDefaultFilledText() {    // DUMMY?
        String s = text;
        if (dummy_flag)
            s = dummytext;
        if (s == null || s.length() == 0)
            s = text;
        return getDefaultFilledText(s);
    }

    public String getDefaultFilledTTS() {    // DUMMY?
        String s = tts;
        if (dummy_flag)
            s = dummytext;
        if (s == null || s.length() == 0)
            s = text;
        return getDefaultFilledTTS(s);
    }

    public String getDefaultFilledText(String s) {
        try {
            int ix = s.indexOf('{');
            if (ix == -1)
                return s;
            int ix2 = s.indexOf('}');
            int ix3 = s.indexOf(':');
            if ( ItemEntry.isPeTask(s) && Lesson.edit ) {
                return s;
            }
            if (ix3 != -1 && ix3 < ix2 && ix2 > 0)
                return s.substring(0, ix) + s.substring(ix3 + 1, ix2) + getDefaultFilledText(s.substring(ix2 + 1));
            return s.substring(0, ix) + getDefaultFilledText(s.substring(ix2 + 1));
        } catch (Exception ex) {
            return "";
        }
    }

    public String getDefaultFilledTTS(String s) {
        try {
            int ix = s.indexOf('{');
            if (ix == -1)
                return s;
            int ix2 = s.indexOf('}');
            int ix3 = s.indexOf(':');
            if (ix3 != -1 && ix3 < ix2 && ix2 > 0)
                return s.substring(0, ix) + s.substring(ix3 + 1, ix2) + getDefaultFilledTTS(s.substring(ix2 + 1));
            return s.substring(0, ix) + getDefaultFilledTTS(s.substring(ix2 + 1));
        } catch (Exception ex) {
            return "";
        }
    }

    String getVar(int ix) {
        try {
            String v = (String) var.get(ix);
            return v;
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public void setVar(int ix, String s) {
        try {
            var.set(ix, s);
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    public String getEntryTid() {
        return it_ent.tid;
    }

    public Values getValues(boolean orig) {
        Values vs = new Values();
        vs.setStr("text", text_orig);
        vs.setStr("tts", tts_orig);
//	vs.setStr("text", text);
        vs.setStr("tid", it_ent.tid);
        vs.setStr("lid", lid_orig);

        for (int i = 0; i < OmegaConfig.VAR_NUM; i++) {
            vs.setStr("v" + (i + 1), getVar(i + 1));
        }

        String s = "" + var;
        s = s.substring(1, s.length() - 1);
        vs.setStr("var", s);

        vs.setStr("sound", sound_orig);
        vs.setStr("sign", sign_orig);

        vs.setStr("ftype", "");
        vs.setStr("fname", "");
        vs.setStr("dummytext", dummytext_orig);
        vs.setStr("dummysound", dummysound_orig);
        vs.setStr("dummysign", dummysign_orig);

        if (isAction) {
            vs.setStr("fname", action_fname_orig);
            vs.setStr("ftype", action_type);
        }

//log	OmegaContext.sout_log.getLogger().info(":--: " + "Values is " + vs);
        return vs;
    }

    String encode(String s) {
        if (s == null)
            return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch > 255) {
                sb.append("" + "{" + (int) ch + "}");
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public Element getElement() {
        Element el = new Element("item");
        el.addAttr("ord", "" + ord);

        String t = encode(text_orig);
        el.addAttr("text", t);
        t = encode(tts_orig);
        el.addAttr("tts", t);
        t = encode(dummytext_orig);
        el.addAttr("dummytext", t);
        el.addAttr("sound", sound_orig);
        el.addAttr("dummysound", dummysound_orig);
        el.addAttr("sign", sign_orig);
        el.addAttr("dummysign", dummysign_orig);
        if (lid != null && lid.length() > 0)
            el.addAttr("Lid", lid_orig);
        for (int i = 0; i < OmegaConfig.VAR_NUM; i++)
            el.addAttr("var-" + (i + 1), getVar(i + 1));
        if (isAction) {
            el.addAttr("action-type", "" + action_type);
            el.addAttr("action-fname", action_fname_orig);
        }
        return el;
    }

    public String getText() {
        return text;
    }

    public String getTextD() {              // DUMMY?
        if (dummy_flag && dummytext.length() > 0)
            return dummytext;
        return text;
    }

    public String getTTSD() {              // DUMMY?
        if (dummy_flag && dummytext.length() > 0)
            return dummytext;
        if (SundryUtils.empty(tts) )
            return text;
        return tts;
    }

    public String getTTS() {
        return tts;
    }

    public String getDummyText() {
        return dummytext;
    }

    public String getText_Orig() {
        return text_orig;
    }

    public String getTTS_Orig() {
        return tts_orig;
    }

    public String getLid() {
        return lid;
    }

    public String getLid_Orig() {
        return lid_orig;
    }

    public String getSound() {
        return sound;
    }

    public String getSign() {
        return sign;
    }

    public String getDummySound() {
        return dummysound;
    }

    public String getDummySign() {
        return dummysign;
    }

    public String getSoundD() {
        if (dummy_flag)
            return dummysound;
        else
            return sound;
    }

    public String getSound_Orig() {
        return sound_orig;
    }

    public String getSignD() {
        if (dummy_flag)
            return dummysign;
        else
            return sign;
    }

    public String getSign_Orig() {
        return sign_orig;
    }

    public void setText_Krull(String s) {
        text_orig = s;
        text = krull(s);
    }

    public void setTTS_Krull(String s) {
        tts_orig = s;
        tts = krull(s);
    }

    public void setDummyText_Krull(String s, boolean saved) {
        dummytext_orig = s;
        dummytext = krull(s);
        if (saved)
            saved_dummytext = dummytext;
    }

    public void setLid_Krull(String s) {
        lid_orig = s;
        lid = krull(s);
    }

    public void setSign_Krull(String s) {
        sign_orig = s;
        sign = krull(s);
    }

    public void setSound_Krull(String s) {
        sound_orig = s;
        sound = krull(s);
    }

    public void setDummySound_Krull(String s, boolean saved) {
        dummysound_orig = s;
        dummysound = krull(s);
        if (saved)
            saved_dummysound = dummysound;
    }

    public void setDummySign_Krull(String s, boolean saved) {
        dummysign_orig = s;
        dummysign = krull(s);
        if (saved)
            saved_dummysign = dummysign;
    }

    public boolean isDummySpaceAllocated() {
        return dummy_flag;
    }

    public void allocateDummySpace(Item src_itm) {
        if (src_itm.getDummyText() == null || src_itm.getDummyText().length() == 0)
            return;
        setDummyText_Krull(src_itm.getDummyText(), false);
        setDummySound_Krull(src_itm.getDummySound(), false);
        setDummySign_Krull(src_itm.getDummySign(), false);
        dummy_flag = true;
    }

    public void setDummy(boolean b) {
        dummy_flag = b;
        //	OmegaContext.sout_log.getLogger().info(":--: " + "DUMMY dep_set to " + b + ' ' + dummytext + ' ' + text);
    }

    public void restoreSavedDummy() {
        dummytext = saved_dummytext;
        dummysound = saved_dummysound;
        dummysign = saved_dummysign;
        if (dummy_extra != -1)
            text = "@_._";
        dummy_flag = false;
    }

    public String getActionText() {
        if (action_fname == null ||
                action_fname.length() == 0 ||
                action_type.length() == 0)
            return null;
        String[] sa = SundryUtils.split(action_fname, " ,");
        String ss = "";
        for (int i = 0; i < sa.length; i++) {
            if (i > 0)
                ss += ',';
            String sx = sa[i] + '.' + action_type;
            ss += sx;
        }
        OmegaContext.sout_log.getLogger().info(":--: " + "return getActionText " + ss);
        return ss;
    }

    public void setActionFile(String fn) {
        int ix = fn.lastIndexOf('.');
        if (ix != -1) {
            isAction = true;
            action_type = fn.substring(ix + 1);
            action_fname_orig = fn.substring(0, ix);
            action_fname = krull(action_fname_orig);
        }
    }

    public void setAction_Fname(String s, String s2) {
        if (s != null) {
            isAction = true;
            action_type = s2;
            action_fname_orig = s;
            action_fname = krull(s);
        }
    }

    public String getActionFile() {
        String at = getActionText();
        return at;
    }


    public String toString() {
        return "Item{" + ord +
                ", isdummy=" + dummy_flag +
                ", text=" + text +
                ", dummytext=" + dummytext +
                ", text_orig=" + text_orig +
                ", sound=" + sound +
                ", dummysound=" + dummysound +
                ", sign=" + sign +
                ", dummysign=" + dummysign +
                ", Lid=" + lid +
                ", var=" + var +
                ", tid'=" + it_ent.tid +
                ", action_type=" + action_type +
                ", action_fname_orig=" + action_fname_orig +
                ", action_fname=" + action_fname +
                "}";
    }
}
