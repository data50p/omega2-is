package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t
import com.femtioprocent.omega.util.SundryUtils.tD

class TriggerEventSetAnimSpeed : TriggerEvent {
    override val cmd: String
	get() = "SetAnimSpeed"
    override val cmdLabel: String
	get() = t("Anim speed")
    override val help: String
	get() = t("<sec / frame>")

    constructor() : super("0.2")
    constructor(arg: String?) : super(arg)

    val argDouble: Double
	get() {
	    val s = argString
	    return tD(s!!.trim { it <= ' ' })
	}
}
