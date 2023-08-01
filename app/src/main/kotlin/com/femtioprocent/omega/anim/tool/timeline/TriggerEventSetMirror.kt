package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t

class TriggerEventSetMirror : TriggerEventSelections {
    constructor() : super("")
    constructor(arg: String?) : super(arg)

    override val cmd: String
	get() = "SetMirror"
    override val cmdLabel: String
	get() = t("image Mirror")
    override val help: String
	get() = t("Transform image")
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
		"X",
		"Y",
		"X and Y"
	)
	var st_selections_human = arrayOf(
		t("none"),
		t("X only"),
		t("Y only "),
		t("both X and Y")
	)

	fun getIx(s: String?): Int {
	    for (i in st_selections_cmd.indices) if (st_selections_cmd[i] == s) return i
	    return 2
	}
    }
}
