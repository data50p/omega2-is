package com.femtioprocent.omega.media.audio.impl

import com.femtioprocent.omega.util.Log
import com.femtioprocent.omega.util.MilliTimer
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.swing.SwingUtilities
import kotlin.concurrent.withLock

//åäö
class FxPlayer(val realy_name: String) {
    var done = false
    var mediaPlayer: MediaPlayer? = null

    val lock = ReentrantLock()
    val condition = lock.newCondition()

    var fname: String? = null
    fun play(wait: Boolean) {
	playFX(fname)
	if (wait) {
	    lock.withLock {
		try {
		    Log.getLogger().info("fx I am waiting while playing")
		    while (!done) condition.await(200, TimeUnit.MILLISECONDS)
		} catch (ex: InterruptedException) {
		}
		Log.getLogger().info("fxPlayed waited ... notified done")
	    }
	}
    }

    fun playFX(fn: String?) {
	Log.getLogger().info("Enter playFX $fn")
	doOnce()
	Platform.runLater {
	    val mt = MilliTimer()
	    val f = File(fn)
	    var bip: String? = null
	    bip = f.toURI().toString()
	    Log.getLogger().info("fxPrepare " + bip + ' ' + mt.string)
	    val hit = Media(bip)
	    mediaPlayer = MediaPlayer(hit)
	    mediaPlayer!!.onEndOfMedia = Runnable {
		lock.withLock {
		    mediaPlayer!!.dispose()
		    Log.getLogger().info("fxPlayed eof" + ' ' + mt.string)
		    done = true
		    condition.signalAll()
		}
	    }
	    Log.getLogger().info("fxPlay..." + ' ' + mt.string)
	    mediaPlayer!!.play()
	}
	Log.getLogger().info("Leave playFX $fn")
    }

    private fun doOnce() {
	if (once) return
	initFxFramework()
	once = true
	try {
	    Thread.sleep(5)
	} catch (e: InterruptedException) {
	}
    }

    init {
	if (realy_name.endsWith(".wav")) {
	    val fn3 = realy_name.replace("\\.wav".toRegex(), ".mp3")
	    val file3 = File(fn3)
	    val file2 = File(realy_name)
	    if (file3.exists() && !file2.exists()) {
		fname = fn3
		Log.getLogger().info(": FxPlayer: fn -> $fname ($realy_name)")
	    } else {
		fname = realy_name
		Log.getLogger().info(": FxPlayer: fn => $fname")
	    }
	} else {
	    fname = realy_name
	    Log.getLogger().info(": FxPlayer: fn => $fname")
	}
    }

    override fun toString(): String {
	return "" + realy_name
    }

    companion object {
	private var once = false
	var z: JFXPanel? = null

	@Synchronized
	private fun initFxFramework() {
	    if (z == null) {
		SwingUtilities.invokeLater {
		    z = JFXPanel() // this will prepare JavaFX toolkit and environment
		    Platform.setImplicitExit(false)
		}
	    }
	}
    }
}
