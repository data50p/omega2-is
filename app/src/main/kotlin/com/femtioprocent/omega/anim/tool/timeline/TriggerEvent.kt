package com.femtioprocent.omega.anim.tool.timeline

import com.femtioprocent.omega.xml.Element

open class TriggerEvent(var arg: String? = null) {
    var name: String? = null
    var arg_human: String?
    var is_on = false

    init {
	arg_human = arg
    }

    open val cmd: String
	get() = ""
    open val cmdLabel: String
	get() = ""
    open val help: String
	get() = ""
    open val selections_cmd: Array<String>?
	get() = null
    open val selections_human: Array<String>?
	get() = null
    val argString: String
	get() = if (arg == null) "" else arg!!
    val argString_human: String
	get() = if (arg_human == null) "" else arg_human!!

    fun setArg_(arg: String?) {
	this.arg = arg
	arg_human = arg
    }

    //      public void setArg_human(String arg) {
    //  	this.arg_human = arg;
    //  	this.arg = arg;
    //      }
    fun setOn(is_on: Boolean) {
	this.is_on = is_on
    }

    override fun toString(): String {
	return cmd + ' ' + arg
    }

    open fun hasSelections(): Boolean {
	return false
    }

    val element: Element
	get() {
	    return Element("TriggerEvent").also {
		it.addAttr("cmd", cmd)
		it.addAttr("arg", arg)
		it.addAttr("isOn", if (is_on) "true" else "false")
	    }
	}

    fun doAction() {}
    open val files: Array<String>?
	get() = null
}
