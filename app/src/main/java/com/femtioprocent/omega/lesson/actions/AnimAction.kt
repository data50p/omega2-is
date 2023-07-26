package com.femtioprocent.omega.lesson.actions

import com.femtioprocent.omega.anim.appl.AnimRuntime
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.xml.Element
import java.awt.Window
import javax.swing.JPanel

class AnimAction : ActionI {
    @JvmField
    var rt: AnimRuntime? = AnimRuntime()
    override var hm: HashMap<String?, Any?> = HashMap()
    override fun prefetch(action_s: String?): Element? {
	var el: Element? = null
	try {
	    rt!!.prefetch(action_s)
	    el = elementRoot
	} catch (ex: Exception) {
	    Log.getLogger().info("ERR: exception $ex")
	    ex.printStackTrace()
	}
	return el
    }

    val elementRoot: Element?
	get() = rt!!.elementRoot

    override fun show() {
//	rt.a_ctxt.anim_canvas.repaint();
    }

    override val pathList: String?
	get() {
	    val el = elementRoot
	    if (el != null) {
		val mtl_el = el.findElement("MTL", 0)
		if (mtl_el != null) {
		    var s = ""
		    for (i in 0..49) {
			val tel = mtl_el.findElement("TimeLine", i)
			if (tel != null) {
			    val ss = tel.findAttr("lesson_id")
			    if (ss != null) {
				if (s.length == 0) s = ss else s += ",$ss"
			    }
			}
		    }
		    //log		OmegaContext.sout_log.getLogger().info(":--: " + "FIND all tl " + s);
		    return s
		}
	    }
	    return null
	}

    override fun clearScreen() {
	val aca = rt!!.aC
	aca?.setHidden(true)
	if (rt != null) rt!!.clean()
    }

    override val actorList: String?
	get() {
	    val el = elementRoot
	    if (el != null) {
		val allact = el.findElement("AllActors", 0)
		if (allact != null) {
		    var s = ""
		    for (i in 0..49) {
			val ael = allact.findElement("Actor", i)
			if (ael != null) {
			    val ss = ael.findAttr("lesson_id")
			    if (ss != null) {
				if (s.length == 0) s = ss else s += ",$ss"
			    }
			}
		    }
		    //log		OmegaContext.sout_log.getLogger().info(":--: " + "FIND all act " + s);
		    return s
		}
	    }
	    return null
	}
    override val canvas: JPanel?
	get() = rt!!.aC

    override fun perform(window: Window,
			 action_s: String?,
			 actA: Array<String>,
			 pathA: Array<String?>,
			 ord: Int,
			 hook: Runnable?) {
	rt!!.aC!!.setHidden(false)
	rt!!.runAction(window, action_s, actA, pathA, hm, hook)
    }

    override fun clean() {
	rt!!.clean()
    }
}
