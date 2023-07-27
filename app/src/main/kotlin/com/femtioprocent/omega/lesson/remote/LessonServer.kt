package com.femtioprocent.omega.lesson.remote

import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.util.SundryUtils.m_sleep
import java.io.IOException
import java.net.ServerSocket

class LessonServer : Runnable {
    var th: Thread
    var port = 9900

    init {
	th = Thread(this)
	th.start()
    }

    override fun run() {
//log	OmegaContext.sout_log.getLogger().info(":--: " + "Server started");
	while (true) {
	    try {
		val sso = ServerSocket(port)
		while (true) {
		    val so = sso.accept()
		    //log		    OmegaContext.sout_log.getLogger().info(":--: " + "lessond: Connection accepted");
		    val con = LessonServerConnection(so, this)
		    con.start()
		}
	    } catch (ex: IOException) {
		OmegaContext.sout_log.getLogger().info("ERR: lessond: Exception: $ex")
		m_sleep(3000)
	    }
	}
    }
}
