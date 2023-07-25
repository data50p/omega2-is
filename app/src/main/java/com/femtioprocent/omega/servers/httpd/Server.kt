package com.femtioprocent.omega.servers.httpd

import com.femtioprocent.omega.OmegaContext
import java.io.IOException
import java.net.ServerSocket
import java.util.*

class Server @JvmOverloads constructor(var port: Int = 8089) : Thread("httpd") {
    var sso: ServerSocket? = null
    var ht: Hashtable<*, *>
    var start_date = Date()
    var connection_cnt = 0
    override fun run() {
	OmegaContext.sout_log.getLogger().info(":--: " + "httpd: Server started")
	try {
	    sso = ServerSocket(port)
	    while (true) {
		val so = sso!!.accept()
		//		OmegaContext.sout_log.getLogger().info(":--: " + "httpd: Connection accepted");
		connection_cnt++
		val con = ServerConnection(so, this)
		con.start()
	    }
	} catch (ex: IOException) {
	    OmegaContext.sout_log.getLogger().info("ERR: httpd: Exception: $ex")
	}
    }

    var hm = HashMap<String?, String?>()

    init {
	ht = Hashtable<Any?, Any?>()
    }

    companion object {
	@JvmStatic
	fun main(args: Array<String>) {
	    val s = Server()
	    s.start()
	}
    }
}
