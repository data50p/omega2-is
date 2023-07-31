package com.femtioprocent.omega.anim.tool.timeline

open class TriggerEventSelections(arg: String? = null) : TriggerEvent(arg) {
    init {
	setArgFromCmd(arg)
    }

    override fun hasSelections(): Boolean {
	return true
    }

    fun setArgFromHuman(arg: String) {
	for (i in selections_human!!.indices) if (selections_human!![i] == arg) {
	    arg_human = selections_human!![i]
	    this.setArg_(selections_cmd!![i])
	    return
	}
	this.setArg_(arg)
	arg_human = arg
    }

    fun setArgFromCmd(arg: String?) {
	for (i in selections_cmd!!.indices) if (selections_cmd!![i] == arg) {
	    arg_human = selections_human!![i]
	    this.setArg_(selections_cmd!![i])
	    return
	}
	this.setArg_(arg)
	arg_human = arg
    }

    companion object {
	fun getIx(sa: Array<String>, s: String?, def: Int): Int {
	    for (i in sa.indices) if (sa[i] == s) return i
	    return def
	}
    }
}
