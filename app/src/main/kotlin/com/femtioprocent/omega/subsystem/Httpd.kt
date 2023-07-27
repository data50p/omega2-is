package com.femtioprocent.omega.subsystem

import com.femtioprocent.omega.servers.httpd.Server

class Httpd : Subsystem() {

    override fun init(arg: Any?) {
	if (Companion.httpd == null) {
	    Companion.httpd = Server(8089)
	    Companion.httpd!!.start()
	}
    }

    companion object {
	var httpd: Server? = null
    }
}
