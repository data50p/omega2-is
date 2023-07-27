package com.femtioprocent.omega.value

class Value {
    @JvmField
    var id: String
    var obj: Any? = null

    constructor(id: String) {
	this.id = id
    }

    constructor(id: String, v: Int) {
	this.id = id
	val ia = IntArray(1)
	ia[0] = v
	obj = ia
    }

    constructor(id: String, v: String?) {
	this.id = id
	obj = v
    }

    constructor(id: String, v: Any?) {
	this.id = id
	obj = v
    }

    var int: Int
	get() = (obj as IntArray?)!![0]
	set(v) {
	    obj = IntArray(1)
	    (obj as IntArray)[0] = v
	}
    var str: String?
	get() = if (obj is IntArray) "" + int else obj as String?
	set(v) {
	    obj = v
	}

    override fun toString(): String {
	return "Value{" + id + ',' + obj + "}"
    }
}
