package com.femtioprocent.omega.anim.cabaret;

import com.femtioprocent.omega.anim.context.AnimContext;
import com.femtioprocent.omega.swing.OmegaSwingUtils;
import com.femtioprocent.omega.util.SundryUtils;
import com.femtioprocent.omega.xml.Element;

public class Actor {
    final public GImAE gimae;
    AnimContext anim_ctxt;

    public Actor(AnimContext a, GImAE gimae) {
        anim_ctxt = a;
        this.gimae = gimae;
    }

    public void hide() {
        gimae.hide();

    }

//      public String getLessonId() {
//  	return "A@" + hashCode();
//      }

    public Element getElement() {
        Element el = new Element("Actor");
        String peTaskNid = gimae.getPeTaskNid();
        if ( !SundryUtils.empty(peTaskNid) ) {
            el.addAttr("name", "{*" + peTaskNid + ":" + gimae.getFNBase() + "}");
        } else {
            el.addAttr("name", gimae.getFNBase());
        }
        String lid = gimae.getLessonId();
        if (lid != null && !(lid.startsWith("#") || lid.length() == 0))
            el.addAttr("lesson_id", lid);
        el.addAttr("var1", gimae.getVariable(1));
        el.addAttr("var2", gimae.getVariable(2));
        el.addAttr("var3", gimae.getVariable(3));
        for (int ih = 0; ih < Hotspot.HOTSPOT_N; ih++) {
            el.addAttr("hotspot_" + Hotspot.getType(ih),
                    "" + gimae.hotspot.getX(ih) + ' ' + gimae.hotspot.getY(ih));
        }
        el.addAttr("prim_scale", "" + gimae.prim_scale);
        el.addAttr("prim_mirror", "" + gimae.prim_mirror);
        return el;
    }

    public String toString() {
        return "Actor{" +
                gimae.toString() +
                "}";
    }
}
