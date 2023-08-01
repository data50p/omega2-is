package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t

class TriggerEventDinner : TriggerEventSelections {
    constructor() : super("")
    constructor(arg: String?) : super(arg)

    override val cmd: String
	get() = "Dinner"
    override val cmdLabel: String
	get() = t("image eat/eaten")
    override val help: String
	get() = t("Eat images")
    override val selections_cmd: Array<String>
	get() = st_selections_cmd
    override val selections_human: Array<String>
	get() = st_selections_human
    val argInt: Int
	get() {
	    val s = argString
	    return getIx(s)
	}

    companion object {
	var st_selections_cmd = arrayOf(
		"",
		"eat",
		"eaten"
	)
	var st_selections_human = arrayOf(
		t("none"),
		t("Can eat"),
		t("Can bee eaten")
	)

	fun getIx(s: String?): Int {
	    for (i in st_selections_cmd.indices) if (st_selections_cmd[i] == s) return i
	    return 2
	}
    }
}
