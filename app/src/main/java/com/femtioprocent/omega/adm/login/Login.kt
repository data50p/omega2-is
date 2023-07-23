package com.femtioprocent.omega.adm.login

import com.femtioprocent.omega.adm.people.PeopleTeacher
import com.femtioprocent.omega.adm.people.PeopleUser
import com.femtioprocent.omega.util.Log
import java.util.concurrent.locks.ReentrantLock
import javax.swing.JComponent
import kotlin.concurrent.withLock

abstract class Login internal constructor() {
    var user: PeopleUser
    var teacher: PeopleTeacher
    var ready = false

    var o = Any()
    val lock = ReentrantLock()
    val condition = lock.newCondition()

    var mode_ = USER

    init {
	user = PeopleUser()
	teacher = PeopleTeacher()
    }

    fun setMode(m: Int) {
	mode_ = m
    }

    fun setName(name: String) {
	Log.getLogger().info(":--: setName $name$mode_")
	when (mode_) {
	    USER -> user.jname = name
	    TEACHER -> teacher.jname = name
	}
    }

    fun waitDone() {
	lock.withLock {
	    while (!ready) {
		try {
		    condition.await()
		} catch (ex: InterruptedException) {
		}
	    }
	}
    }

    abstract fun getComp_(): JComponent

    companion object {
	const val USER = 1
	const val TEACHER = 2
	const val BOTH = 3
    }
}
