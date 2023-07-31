package com.femtioprocent.omega.anim.cabaret

import com.femtioprocent.omega.anim.context.AnimContext
import com.femtioprocent.omega.util.SundryUtils.empty
import com.femtioprocent.omega.xml.Element

class Actor(var anim_ctxt: AnimContext, val gimae: GImAE) {
    fun hide() {
	gimae.hide()
    }

    val element: Element
	//      public String getLessonId() {
	get() {
	    val el = Element("Actor")
	    val peTaskNid = gimae.peTaskNid
	    if (!empty(peTaskNid)) {
		el.addAttr("name", "{*" + peTaskNid + ":" + gimae.fnBase + "}")
	    } else {
		el.addAttr("name", gimae.fnBase)
	    }
	    val lid = gimae.lessonId
	    if (lid != null && !(lid.startsWith("#") || lid.length == 0)) el.addAttr("lesson_id", lid)
	    el.addAttr("var1", gimae.getVariable(1))
	    el.addAttr("var2", gimae.getVariable(2))
	    el.addAttr("var3", gimae.getVariable(3))
	    for (ih in 0 until Hotspot.HOTSPOT_N) {
		el.addAttr(
		    "hotspot_" + Hotspot.getType(ih),
		    "" + gimae.hotspot.getX(ih) + ' ' + gimae.hotspot.getY(ih)
		)
	    }
	    el.addAttr("prim_scale", "" + gimae.primScale)
	    el.addAttr("prim_mirror", "" + gimae.primMirror)
	    return el
	}

    override fun toString(): String {
	return "Actor{" +
		gimae.toString() +
		"}"
    }
}
