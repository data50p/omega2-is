package com.femtioprocent.omega.anim.context

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.anim.appl.AnimEditor
import com.femtioprocent.omega.anim.appl.AnimRuntime
import com.femtioprocent.omega.anim.canvas.AnimCanvas
import com.femtioprocent.omega.anim.tool.timeline.MasterTimeLine
import com.femtioprocent.omega.anim.tool.timeline.TimeLinePlayer
import com.femtioprocent.omega.xml.Element
import javax.swing.JFrame

//import omega.anim.config.*;
class AnimContext : OmegaContext {
    @JvmField
    var anim_canvas: AnimCanvas? = null
    @JvmField
    var tl_player: TimeLinePlayer? = null
    @JvmField
    var mtl: MasterTimeLine? = null
    @JvmField
    var arun: AnimRuntime?
    @JvmField
    var anim_speed = 1.0

    constructor(ae: AnimEditor) {
	AnimContext.ae = ae
	arun = null
    }

    constructor(arun: AnimRuntime?) {
	AnimContext.ae = null
	this.arun = arun
    }

    fun fillElement(el: Element?) {
	anim_canvas!!.fillElement(el!!)
	mtl!!.fillElement(el)
    } //      public void save(XML_PW xmlpw) {

    //  	anim_canvas.save(xmlpw);
    //  	mtl.save(xmlpw);
    //      }
    companion object {
	@kotlin.jvm.JvmField
	public var ae: AnimEditor? = null
	@JvmField
        var top_frame: JFrame? = null
    }
}