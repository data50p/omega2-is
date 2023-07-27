package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.util.SundryUtils.tD

class TriggerEventRotate : TriggerEvent {
    override val cmd: String
	get() = "Rotate"
    override val cmdLabel: String
	get() = t("Rotate object")
    override val help: String
	get() = t("<angle change / sec> {end angle=<none>}")

    constructor() : super("0.0")
    constructor(arg: String?) : super(arg)

    val argDouble: Double
	get() {
	    val s = argString
	    val sa = split(s, " ")
	    return if (sa.size == 1) {
		tD(s)
	    } else tD(sa[0])
	}
    val argDouble2nd: Double
	get() {
	    val s = argString
	    val sa = split(s, " ")
	    return if (sa.size == 1) {
		20000.00
	    } else tD(sa[1])
	}
}
