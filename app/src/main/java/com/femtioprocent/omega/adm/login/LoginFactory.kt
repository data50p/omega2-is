package com.femtioprocent.omega.adm.login

import com.femtioprocent.omega.util.Factory
import com.femtioprocent.omega.util.Log

object LoginFactory {
    fun createLogin(package_name: String, name: String): Login? {
	val n = "$package_name.Login$name"
	try {
	    return Factory.createObject(n) as Login
	} catch (ex: Exception) {
	    Log.getLogger().info("ERR: Can't create $n: $ex")
	}
	return null
    }
}
