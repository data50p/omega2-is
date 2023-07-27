package com.femtioprocent.omega.adm.register.data

import com.femtioprocent.omega.xml.Element
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

abstract class Result internal constructor() {
    fun mkFname(pname: String): String {
	val d = firstPerformDate
	//	DateFormat df = DateFormat.getDateTimeInstance();
	val df: DateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
	val s = pname + '-' + df.format(d) + '-' + lessonName
	return s.replace(':', '-').replace(' ', '_')
    }

    abstract var element: Element?
    open val type: String
	get() = "generic"
    abstract val lessonName: String
    abstract val performDate: Date
    abstract val firstPerformDate: Date
}
