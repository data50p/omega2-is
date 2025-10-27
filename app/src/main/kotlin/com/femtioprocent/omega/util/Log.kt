package com.femtioprocent.omega.util

import com.femtioprocent.omega.OmegaContext.Companion.isDeveloper
import com.femtioprocent.omega.appl.Omega_IS
import com.femtioprocent.omega.util.SundryUtils.ct
import com.femtioprocent.omega.util.SundryUtils.padLeft
import com.femtioprocent.omega.util.SundryUtils.padRight
import femtioprocent.ansi.Ansi
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter
import femtioprocent.ansi.Color5

object Log {
    var fh: FileHandler? = null
    var h2: Handler? = null
    private val my_formatter = MyFormatter()
    private val myMap = HashMap<String, Logger?>()

    @JvmStatic
    fun getLogger(): Logger {
	return getLogger(Omega_IS::class.java)
    }

    fun getLogger(clazz: Class<*>): Logger {
	try {
	    var logger = myMap[clazz.name]
	    if (logger != null) return logger
	    logger = Logger.getLogger(clazz.name)
	    myMap[clazz.name] = logger
	    //logger.getParent().setLevel(Level.OFF);
	    logger.level = Level.ALL
	    if (fh == null) {
		val d = File("logs")
		d.mkdir()
		fh = FileHandler(d.name + '/' + "omega.log")
	    }
	    fh!!.formatter = my_formatter
	    logger.addHandler(fh)
	    if (isDeveloper) {
		h2 = MyHandler(System.err, my_formatter)
		logger.addHandler(h2)
	    }
	    logger.useParentHandlers = false
	    return logger
	} catch (ex: IOException) {
	} catch (ex: NoClassDefFoundError) {
	}
	val logger = Logger.getLogger(clazz.name)
	return logger
    }

    internal class MyHandler(out: OutputStream?, f: Formatter?) : StreamHandler(out, f) {
	@Synchronized
	override fun publish(record: LogRecord) {
	    super.publish(record)
	    flush()
	}
    }

    private class MyFormatter : Formatter() {
	var last = ct()
	override fun format(record: LogRecord): String {
	    var s = record.sourceClassName
	    s = s.substring(s.lastIndexOf('.') + 1)
	    val ms = record.millis
	    val th_id = record.longThreadID
	    val dt = Date(ms)
	    val d = dformat.format(dt)
	    val lt = (ms - last).toInt()
	    last = ms
	    return "" +
		   Color5.fg5(Color5.ColorValue.MAGENTA, padRight("" + record.level, 10, ' ')) +
		   d + ' ' +
		   padLeft("" + th_id, 12, ' ') + ' ' +
		   padLeft("" + lt, 5, ' ') + ' ' +
		   padRight("" + s, 23, ' ') + ' ' +
		   padRight("" + record.sourceMethodName, 22, ' ') + ' ' +
		   record.message + '\n'
	}

	companion object {
	    var dformat: DateFormat = SimpleDateFormat("dd/MM HH:mm:ss.SSS")
	}
    }
}
