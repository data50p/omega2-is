package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t

class TriggerEventResetSequence : TriggerEvent {
    constructor() : super("")
    constructor(arg: String?) : super(arg)

    override val cmd: String
	get() = "ResetSequence"
    override val cmdLabel: String
	get() = t("Reset Sequence")
    override val help: String
	get() = t("relative {, [, or empty for here")
}
