package com.femtioprocent.omega.adm.login

import com.femtioprocent.omega.util.Log
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JFrame

class LoginAppl internal constructor() {
    var l: Login?

    init {
	l = LoginFactory.createLogin(javaClass.packageName, "Simple")
    }

    fun start() {
	if (l != null) {
	    Log.getLogger().info("Loaded $l")
	    val f = JFrame("LoginAppl")
	    val c = f.contentPane
	    c.layout = BorderLayout()
	    l!!.setMode(Login.Companion.USER)
	    c.add(l?.getComp_(), BorderLayout.CENTER)
	    f.pack()
	    f.isVisible = true
	    l!!.waitDone()

//log	    OmegaContext.sout_log.getLogger().info("Login user name is " + l.user);
//log	    OmegaContext.sout_log.getLogger().info("Login teacher name is " + l.teacher);
	    f.isVisible = false
	    f.dispose()
	}
    }

    companion object {
	@JvmStatic
	fun main(args: Array<String>) {
	    val la = LoginAppl()
	    la.start()
	}
    }
}
