package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.SundryUtils.ct
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.concurrent.locks.ReentrantLock
import javax.swing.Timer
import javax.swing.event.EventListenerList
import kotlin.concurrent.withLock

class TimeLinePlayer : ActionListener {
    val NULL = 0
    val INIT_PLAY = 1
    val PLAY = 2
    val STOP_REQ = 3
    val STOPPED = 4
    private val playctrl_listeners: EventListenerList
    var tick_start = 0
    var ct0: Long = 0
    @JvmField
    var speed = 1.0
    var MODE = OmegaConfig.RUN_MODE
    var state = NULL
    var timer: Timer

    val lock = ReentrantLock()
    val condition = lock.newCondition()!!

    var isRunning = false
    var after: Runnable? = null
    fun addPlayCtrlListener(pcl: PlayCtrlListener) {
	playctrl_listeners.add(PlayCtrlListener::class.java, pcl)
    }

    private fun callPlay_beginPlay(dry: Boolean) {
	val lia = playctrl_listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as PlayCtrlListener).beginPlay(dry)
	    i += 2
	}
    }

    var cnt = 0
    var last: Long = 0

    init {
	playctrl_listeners = EventListenerList()
	timer = Timer(OmegaConfig.t_step, this)
	timer.isCoalesce = true
	defaultTimeLinePlayer = this
    }

    private fun callPlay_playAt(t: Int): Boolean {
	val ct0 = System.currentTimeMillis()
	val a = ct0 - last
	//	Log.getLogger().info("P " + a + ' ' + t);
	if (last > 0 && a > 300) Log.getLogger().info("playAt: long time $a $t")
	last = ct0
	var b = false
	val lia = playctrl_listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    b = b or (lia[i + 1] as PlayCtrlListener).playAt(t)
	    i += 2
	}
	return b
    }

    private fun callPlay_endPlay() {
	val lia = playctrl_listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as PlayCtrlListener).endPlay()
	    i += 2
	}
    }

    fun firePropertyChange(s: String?) {
	val lia = playctrl_listeners.listenerList
	var i = 0
	while (i < lia.size) {
	    (lia[i + 1] as PlayCtrlListener).propertyChanged(s)
	    i += 2
	}
    }

    fun dry_play(after: Runnable?, clicks: Int) {
	var after = after
	this.after = after
	state = INIT_PLAY
	isRunning = true
	callPlay_beginPlay(true)
	state = PLAY
	var i = 0
	while (i < 30000) {
	    if (callPlay_playAt(i)) break
	    i += clicks
	}
	callPlay_endPlay()
	state = STOP_REQ
	after?.run()
	after = null
	isRunning = false
	state = STOPPED
    }

    fun play(after: Runnable?): Boolean {
	this.after = after
	state = INIT_PLAY
	isRunning = true
	ct0 = ct()
	timer.start()
	return true
    }

    fun waitWhileRunning() {
	while (true) {
	    lock.withLock {
		try {
		    if (isRunning == false) return
		    condition.await()
		} catch (ex: Exception) {
		}
	    }
	}
    }

    fun stop() {
	state = STOP_REQ
    }

    fun pause(): Boolean {
	return false
    }

    fun normalize() {
	val a = (speed * 10 + 0.5).toInt()
	speed = a / 10.0
    }

    fun adjustSpeed(factor: Double): Boolean {
	if (factor > 1.0 && speed < 2.0) {
	    speed += 0.1
	    normalize()
	    firePropertyChange("speed")
	    return true
	}
	if (factor < 1.0 && speed > 0.5) {
	    speed -= 0.1
	    normalize()
	    firePropertyChange("speed")
	    return true
	}
	return false
    }

    override fun actionPerformed(ae: ActionEvent) {
	if (state == INIT_PLAY) {
	    callPlay_beginPlay(false)
	    state = PLAY
	}
	if (state == STOP_REQ) {
	    callPlay_endPlay()
	    state = STOPPED
	    isRunning = false
	    lock.withLock {
		try {
		    condition.signal()
		} catch (ex: Exception) {
		}
	    }
	    timer.stop()
	    if (after != null) after!!.run()
	    after = null
	}
	var delta = ct() - ct0
	delta = (delta * speed).toLong()
	if (callPlay_playAt(delta.toInt())) {
	    return
	}
    }

    companion object {
	fun getDefaultTimeLinePlayer_(): TimeLinePlayer {
		return defaultTimeLinePlayer!!
	}

	var defaultTimeLinePlayer: TimeLinePlayer? = null
	    private set
    }
}
