package com.femtioprocent.omega.lesson

import com.femtioprocent.omega.media.video.MpgPlayer
import javax.swing.JComponent
import javax.swing.JPanel

//import omega.lesson.test.*;
abstract class FeedBack {
    var mp: MpgPlayer? = null
    var canvas: JPanel? = null
    var my_own: JPanel? = null
    var comp: JComponent? = null
    var w = 0
    var h = 0
    var vw = 0
    var vh = 0

    abstract fun prepare(rsrs: String?, jpan: JPanel?): JPanel?
    abstract fun perform()
    abstract fun waitEnd()
    abstract fun dispose()
}
