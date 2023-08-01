package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t

class TriggerEventSetLayer : TriggerEventSelections {
    constructor() : super("Middle")
    constructor(arg: String?) : super(arg)

    override val cmd: String
	get() = "SetLayer"
    override val cmdLabel: String
	get() = t("dep_set Layer")
    override val help: String
	get() = t("Z order")
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
		"Behind",
		"Back",
		"Middle",
		"Front",
		"Top"
	)
	var st_selections_human = arrayOf(
		t("Behind all"),
		t("Back"),
		t("Middle"),
		t("Front"),
		t("On Top")
	)

	fun getIx(s: String?): Int {
	    return TriggerEventSelections.Companion.getIx(st_selections_cmd, s, 2)
	}
    }
}
