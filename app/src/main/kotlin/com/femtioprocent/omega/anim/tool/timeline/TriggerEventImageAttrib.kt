package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t

class TriggerEventImageAttrib : TriggerEvent {
    override val cmd: String
	get() = "ImageAttrib"
    override val cmdLabel: String
	get() = t("dep_set Image Attribute")
    override val help: String
	get() = t("Tail part in filename (\${banid:var})")

    constructor() : super("")
    constructor(arg: String?) : super(arg)
}
