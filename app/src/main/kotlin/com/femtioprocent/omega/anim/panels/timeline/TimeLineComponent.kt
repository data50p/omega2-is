package com.femtioprocent.omega.anim.panels.timeline

import com.femtioprocent.omega.anim.tool.timeline.PlayCtrlListener
import java.awt.BorderLayout
import javax.swing.JPanel

class TimeLineComponent(var tlp: TimeLinePanel) : JPanel(), PlayCtrlListener {
    var tlsp: TimeLineStatusPanel
    var tlcp: TimeLineControlPanel

    init {
	tlsp = TimeLineStatusPanel(tlp)
	tlcp = TimeLineControlPanel(tlp)
	layout = BorderLayout()
	val p = JPanel()
	p.layout = BorderLayout()
	p.add(tlp, BorderLayout.CENTER)
	p.add(tlsp, BorderLayout.SOUTH)
	add(p, BorderLayout.CENTER)
	add(tlcp, BorderLayout.WEST)
    }

    // --------- interface PlayCtrlListener
    override fun beginPlay(dry: Boolean) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera begin");
    }

    fun playAt(lt: Int, t: Int): Boolean {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera " + lt + ' ' + t);
	return false
    }

    override fun playAt(t: Int): Boolean {
	tlsp.updateValues()
	tlp.setTick(t)
	return false
    }

    override fun endPlay() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera end");
    }

    override fun propertyChanged(s: String?) {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "updatera prop " + s);
    }
}
