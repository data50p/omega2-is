package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t

class TriggerEventPlaySound : TriggerEvent {
    override val cmd: String
	get() = "PlaySound"
    override val cmdLabel: String
	get() = t("Play Sound")
    override val help: String
	get() = t("audio file (\${banid:var})")

    constructor() : super("")
    constructor(fname: String?) : super(fname)

    override val files: Array<String>
	get() = arrayOf("au", "wav", "mp3")
}
