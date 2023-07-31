package com.femtioprocent.omega.media.audio.impl

import com.femtioprocent.omega.appl.Settings.Companion.getSettings
import com.femtioprocent.omega.util.Log
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.sound.sampled.*
import kotlin.concurrent.withLock

//åäö
class JPlayer(fn: String) : LineListener {
    var fn: String
    var ais: AudioInputStream? = null
    var aformat: AudioFormat? = null
    var sdataline: SourceDataLine? = null

    val tlock = ReentrantLock()
    val tcondition = tlock.newCondition()
    val lock = ReentrantLock()
    val condition = lock.newCondition()

    var started = false
    var eom = false
    var opened = false
    var done = false
    var realy_name: String
    var w_w = Any()
    fun waitAudio() {
	lock.withLock {
	    try {
		while (!done) condition.await(1000, TimeUnit.MILLISECONDS)
	    } catch (ex: InterruptedException) {
	    }
	}
    }

    init {
	Log.getLogger().info("INIT: JPlayer: fn = $fn")
	this.fn = fn
	realy_name = fn
	try {
	    val file = File(fn)
	    ais = AudioSystem.getAudioInputStream(file)
	    aformat = ais!!.format
	    sdataline = getSourceDataLine(aformat)
	    sdataline!!.addLineListener(this)
	    Log.getLogger().info("<init>: JPlayer0: $ais $aformat $sdataline")
	} catch (ex: Exception) {
	    ais = null
	    aformat = null
	    sdataline = null
	    done = true
	    Log.getLogger().info(":--: JPlayer1: $ex")
	}
    }

    fun play() {
	try {
	    val th = Thread(Runnable {
		try {
		    Log.getLogger().info("JPlayer started Thread " + sdataline + ' ' + aformat)
		    //Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 3);
		    sdataline!!.open(aformat)
		    waitOpen()
		    sdataline!!.start()
		    val ba = ByteArray(N)
		    var i = 0
		    while (true) {
			val n = ais!!.read(ba)
			if (n < 0) break
			sdataline!!.write(ba, 0, n)
			if (i < wah) s_pe_("{w}") else s_pe_("{W}")
			if (wah > 0 && i == wah) {
			    s_pe("-w-")
			    waitStart()
			    s_pe("-W-")
			}
			i++
		    }
		    s_pe(">d>")
		    sdataline!!.drain()
		    done = true
		    lock.withLock { condition.signalAll() }
		    if (silent) {
			sdataline!!.write(silent_buf, 0, silent_buf.size)
			s_pe(">D>")
			sdataline!!.drain()
		    }
		    if (ais != null) {
			ais!!.close()
		    } else {
			Log.getLogger().info("ais is null")
		    }
		    ais = null
		    s_pe(">ST>")
		    sdataline!!.stop()
		    s_pe(">C>")
		    sdataline!!.close()
		} catch (ex: Exception) {
		    Log.getLogger().info("ERR: JPlayer2: $ex")
		    ex.printStackTrace()
		} finally {
		    //			    sdataline.removeLineListener(JPlayer.this);
		    sdataline = null
		    lock.withLock { condition.signalAll() }
		}
	    })
	    th.start()
	    Log.getLogger().info("JPlayer thread start()")
	} catch (ex: Exception) {
	    Log.getLogger().info("ERR: JPlayer3: $ex")
	}
    }

    private fun waitOpen() {
	tlock.withLock {
	    while (!opened) {
		try {
		    tcondition.await(5000, TimeUnit.MILLISECONDS)
		    return
		} catch (ie: InterruptedException) {
		}
	    }
	}
    }

    private fun waitStart() {
	tlock.withLock {
	    if (!started) {
		while (!started) {
		    try {
			tcondition.await(5000, TimeUnit.MILLISECONDS)
		    } catch (ie: InterruptedException) {
		    }
		}
	    }
	}
    }

    private fun waitEOM() {
	tlock.withLock {
	    while (!eom) {
		try {
		    tcondition.await(5000, TimeUnit.MILLISECONDS)
		    return
		} catch (ie: InterruptedException) {
		}
	    }
	}
    }

    @Synchronized
    override fun update(le: LineEvent) {
	tlock.withLock {
	    val t = le.type
	    if (t === LineEvent.Type.OPEN) {
		opened = true
		s_pe("+open+")
	    } else if (t === LineEvent.Type.START) {
		started = true
		s_pe("+start+")
	    } else if (t === LineEvent.Type.CLOSE) {
		eom = true
		s_pe("+close+")
	    } else if (t === LineEvent.Type.STOP) {
		s_pe("+stop+")
	    }
	    tcondition.signalAll()
	    s_pe("signalAll()")
	}
    }

    companion object {
	var wah = 2
	var silent = !true
	var o = !false
	var N = 4096 * 16
	private fun s_pe(s: String) {
	    if (o) Log.getLogger().info(s)
	}

	private fun s_pe_(s: String) {
	    if (o) Log.getLogger().info(s)
	}

	init {
	    var bs: String = getSettings()!!.getString("audio-bufsize")
	    if (bs != null) {
		val bs_i = 1024 * bs.toInt()
		N = bs_i
	    }
	    bs = getSettings()!!.getString("audio-write-ahead")
	    if (bs != null) {
		wah = bs.toInt()
	    }
	    if (getSettings()!!.getBoolean("audio-silent")) silent = true else silent = false
	    if (getSettings()!!.getBoolean("audio-debug")) o = true else o = false
	    o = true
	    Log.getLogger().info("STATIC: " + "" + getSettings()!!.settingsHashMap)
	    Log.getLogger().info("STATIC: " + "" + getSettings()!!.settingsHashMap)
	}

	@Throws(LineUnavailableException::class)
	fun getSourceDataLine(format: AudioFormat?): SourceDataLine {
	    val info = DataLine.Info(SourceDataLine::class.java, format)
	    return AudioSystem.getLine(info) as SourceDataLine
	}

	val silent_buf = ByteArray(4096 * 4)
    }
}
