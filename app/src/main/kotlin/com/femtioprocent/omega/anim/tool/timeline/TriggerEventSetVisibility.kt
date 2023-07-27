package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.t9n.T.Companion.t

class TriggerEventSetVisibility : TriggerEventSelections {
    constructor() : super("100")
    constructor(arg: String?) : super(arg)

    override val cmd: String
	get() = "SetVisibility"
    override val cmdLabel: String
	get() = t("dep_set Visibility")
    override val help: String
	get() = ""
    override val selections_cmd: Array<String>?
	get() = st_selections_cmd
    override val selections_human: Array<String>?
	get() = st_selections_human
    val argInt: Int
	get() {
	    val s = argString
	    val ix: Int = TriggerEventSelections.Companion.getIx(st_selections_cmd, s, 0)
	    return tab[ix]
	}

    companion object {
	var st_selections_cmd = arrayOf(
		"100",
		"0",
		"5",
		"10",
		"30",
		"50",
		"70",
		"90"
	)
	var st_selections_human = arrayOf(
		t("Visible"),
		t("Invisible"),
		t(" 5% visible"),
		t("10% visible"),
		t("30% visible"),
		t("50% visible"),
		t("70% visible"),
		t("90% visible")
	)
	var tab = intArrayOf(
		100,
		0,
		5,
		10,
		30,
		50,
		70,
		90
	)

	fun getIx(sa: Array<String>, s: String?): Int {
	    return TriggerEventSelections.Companion.getIx(sa, s, 0)
	}
    }
}
