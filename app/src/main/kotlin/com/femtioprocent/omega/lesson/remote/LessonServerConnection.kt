package com.femtioprocent.omega.lesson.remote

import com.femtioprocent.omega.OmegaConfig
import com.femtioprocent.omega.OmegaContext
import com.femtioprocent.omega.lesson.Lesson
import com.femtioprocent.omega.util.SundryUtils.ct
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.Socket

class LessonServerConnection internal constructor(var so: Socket, var server: LessonServer) :
    Thread("lessond.Connection") {
    fun serve(`is`: InputStream, dos: DataOutputStream?) { // BufferedReader rd, DataOutputStream dos) {
	try {
	    LOOP@ while (true) {
		val ch = `is`.read()
		if (ch == -1) break
		val chch = ch as Char
		try {
		    when (chch) {
			'l' -> Lesson.le_canvas!!.gotoBoxRel(1, 0)
			'h' -> Lesson.le_canvas!!.gotoBoxRel(-1, 0)
			'k' -> Lesson.le_canvas!!.gotoBoxRel(0, -1)
			'j' -> Lesson.le_canvas!!.gotoBoxRel(0, 1)
			' ' -> Lesson.le_canvas!!.selectBox(false, ct())
			'Q' -> break@LOOP
		    }
		} catch (ex: NullPointerException) {
		}
	    }
	} catch (ex: IOException) {
	    OmegaContext.sout_log.getLogger().info("ERR: serve(): Exception $ex")
	}
    }

    override fun run() {
	val ct0 = ct()
	if (OmegaConfig.T) OmegaContext.sout_log.getLogger().info(":--: " + "lessond Connection established")
	try {
	    val `is` = so.getInputStream() // ));
	    val dos = DataOutputStream(BufferedOutputStream(so.getOutputStream()))
	    serve(`is`, dos)
	    `is`.close()
	    dos.close()
	    so.close()
	} catch (ex: IOException) {
	}
    }
}
