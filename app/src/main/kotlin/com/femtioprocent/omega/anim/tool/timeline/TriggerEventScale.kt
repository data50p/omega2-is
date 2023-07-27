package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.split
import com.femtioprocent.omega.util.SundryUtils.tD

class TriggerEventScale : TriggerEvent {
    override val cmd: String
	get() = "Scale"
    override val cmdLabel: String
	get() = t("Scale object")
    override val help: String
	get() = t("<factor change / sec> <end factor=1.0>")

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

    fun getArgDouble2nd(dsp: Double): Double {
	val s = argString
	val sa = split(s, " ")
	return if (sa.size == 1) {
	    if (dsp >= 0.0) 100.0 else 0.001
	} else tD(sa[1])
    }
}
