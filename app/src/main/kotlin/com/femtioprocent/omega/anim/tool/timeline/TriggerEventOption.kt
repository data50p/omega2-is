package com.femtioprocent.omega.anim.tool.timeline

class TriggerEventOption : TriggerEventSelections {
    constructor() : super("")
    constructor(arg: String?) : super(arg)

    override val cmd: String
	get() = "Option"
    override val cmdLabel: String
	get() = "option"
    override val help: String
	get() = "Options"
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
	    ""
	)
	var st_selections_human = arrayOf(
	    "normal"
	)

	fun getIx(s: String?): Int {
	    for (i in st_selections_cmd.indices) if (st_selections_cmd[i] == s) return i
	    return 2
	}
    }
}
