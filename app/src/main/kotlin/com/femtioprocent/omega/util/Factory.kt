package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaContext
import java.lang.reflect.InvocationTargetException

object Factory {
    fun createObject(clazz_name: String): Any? {
	try {
	    val clazz = Class.forName(clazz_name)
	    return clazz.getDeclaredConstructor().newInstance()
	} catch (ex: ClassNotFoundException) {
	    OmegaContext.sout_log.getLogger().info("ERR: Can't load class $clazz_name: $ex")
	} catch (ex: IllegalAccessException) {
	    OmegaContext.sout_log.getLogger().info("ERR: Can't access class $clazz_name: $ex")
	} catch (ex: InstantiationException) {
	    OmegaContext.sout_log.getLogger().info("ERR: Can't instantiate class $clazz_name: $ex")
	} catch (ex: InvocationTargetException) {
	    OmegaContext.sout_log.getLogger().info("ERR: Can't instantiate class $clazz_name: $ex")
	} catch (e: NoSuchMethodException) {
	    throw RuntimeException(e)
	}
	return null
    }
}
