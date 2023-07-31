package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.media.video.MpgPlayer
import javax.swing.JComponent
import javax.swing.JPanel

//import omega.lesson.test.*;
abstract class FeedBack {
    @JvmField
    var mp: MpgPlayer? = null

    @JvmField
    var canvas: JPanel? = null

    @JvmField
    var my_own: JPanel? = null

    @JvmField
    var comp: JComponent? = null

    @JvmField
    var w = 0

    @JvmField
    var h = 0

    @JvmField
    var vw = 0

    @JvmField
    var vh = 0
    fun getW(): Int {
	return vw
    }

    fun getH(): Int {
	return vh
    }

    abstract fun prepare(rsrs: String?, jpan: JPanel?): JPanel?
    abstract fun perform()
    abstract fun waitEnd()
    abstract fun dispose()
}
